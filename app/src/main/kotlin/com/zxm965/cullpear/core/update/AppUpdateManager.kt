package com.zxm965.cullpear.core.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Environment
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import com.zxm965.cullpear.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data class UpToDate(val version: String) : UpdateState
    data class Available(val update: ReleaseUpdate) : UpdateState
    data class Downloading(val version: String) : UpdateState
    data class ReadyToInstall(val version: String) : UpdateState
    data class PermissionRequired(val version: String) : UpdateState
    data class Error(val message: String) : UpdateState
}

data class ReleaseUpdate(
    val version: String,
    val apkUrl: String,
    val releaseNotes: String,
)

class AppUpdateManager(context: Context) {
    private val appContext = context.applicationContext
    private val downloadManager = appContext.getSystemService(DownloadManager::class.java)
    private val preferences = appContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    var state by mutableStateOf<UpdateState>(UpdateState.Idle)
        private set

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return
            val completedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            if (completedId == savedDownloadId()) handleCompletedDownload(completedId)
        }
    }

    init {
        ContextCompat.registerReceiver(
            appContext,
            downloadReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_EXPORTED,
        )
        restoreDownloadState()
    }

    suspend fun checkForUpdate(force: Boolean = false) {
        if (!force && wasCheckedRecently()) {
            if (!restoreDownloadState()) state = UpdateState.UpToDate(BuildConfig.VERSION_NAME)
            return
        }
        state = UpdateState.Checking
        state = try {
            val update = fetchLatestRelease()
            preferences.edit { putLong(KEY_LAST_CHECKED_AT, System.currentTimeMillis()) }
            if (update == null || compareVersions(update.version, BuildConfig.VERSION_NAME) <= 0) {
                UpdateState.UpToDate(BuildConfig.VERSION_NAME)
            } else if (restoreDownloadState(update.version)) {
                state
            } else {
                UpdateState.Available(update)
            }
        } catch (error: Exception) {
            UpdateState.Error(error.message ?: "检查更新失败，请稍后重试。")
        }
    }

    fun downloadAvailableUpdate() {
        val update = (state as? UpdateState.Available)?.update ?: return
        state = runCatching {
            enqueueDownload(update)
            UpdateState.Downloading(update.version)
        }.getOrElse { error ->
            UpdateState.Error(error.message ?: "无法开始下载更新。")
        }
    }

    fun openInstallPermissionSettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            "package:${appContext.packageName}".toUri(),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        appContext.startActivity(intent)
    }

    fun installDownloadedUpdate() {
        val downloadId = savedDownloadId()
        val version = savedDownloadVersion()
        if (downloadId < 0) {
            state = UpdateState.Error("没有可安装的更新包。")
            return
        }
        if (!appContext.packageManager.canRequestPackageInstalls()) {
            state = UpdateState.PermissionRequired(version)
            openInstallPermissionSettings()
            return
        }
        val uri = downloadManager.getUriForDownloadedFile(downloadId)
        if (uri == null) {
            clearSavedDownload()
            state = UpdateState.Error("更新包不可用，请重新检查更新。")
            return
        }
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, APK_MIME_TYPE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching { appContext.startActivity(intent) }
            .onFailure { state = UpdateState.Error("无法打开系统安装器。") }
    }

    private suspend fun fetchLatestRelease(): ReleaseUpdate? = withContext(Dispatchers.IO) {
        val connection = URL(RELEASES_FEED_URL).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.setRequestProperty("Accept", "application/atom+xml")
            connection.setRequestProperty("User-Agent", "CullPear-Android/${BuildConfig.VERSION_NAME}")
            val status = connection.responseCode
            if (status !in 200..299) throw IllegalStateException("更新服务请求失败（$status）")
            parseLatestReleaseFeed(connection.inputStream.bufferedReader().use { it.readText() })
        } finally {
            connection.disconnect()
        }
    }

    private fun enqueueDownload(update: ReleaseUpdate) {
        clearSavedDownload(removeDownload = true)
        val request = DownloadManager.Request(update.apkUrl.toUri())
            .setTitle("CullPear ${update.version}")
            .setDescription("正在下载应用更新")
            .setMimeType(APK_MIME_TYPE)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
            .setDestinationInExternalFilesDir(
                appContext,
                Environment.DIRECTORY_DOWNLOADS,
                "CullPear-${update.version}.apk",
            )
        val id = downloadManager.enqueue(request)
        preferences.edit {
            putLong(KEY_DOWNLOAD_ID, id)
            putString(KEY_DOWNLOAD_VERSION, update.version)
        }
    }

    private fun restoreDownloadState(expectedVersion: String? = null): Boolean {
        val id = savedDownloadId()
        val version = savedDownloadVersion()
        if (id < 0 || version.isBlank() || (expectedVersion != null && version != expectedVersion)) return false
        val cursor = downloadManager.query(DownloadManager.Query().setFilterById(id)) ?: return false
        cursor.use {
            if (!it.moveToFirst()) return false
            return when (it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))) {
                DownloadManager.STATUS_PENDING, DownloadManager.STATUS_PAUSED, DownloadManager.STATUS_RUNNING -> {
                    state = UpdateState.Downloading(version)
                    true
                }
                DownloadManager.STATUS_SUCCESSFUL -> {
                    state = UpdateState.ReadyToInstall(version)
                    true
                }
                else -> {
                    clearSavedDownload()
                    false
                }
            }
        }
    }

    private fun handleCompletedDownload(id: Long) {
        if (id != savedDownloadId()) return
        if (!restoreDownloadState()) state = UpdateState.Error("更新包下载失败，请重新检查更新。")
    }

    private fun savedDownloadId() = preferences.getLong(KEY_DOWNLOAD_ID, -1L)
    private fun savedDownloadVersion() = preferences.getString(KEY_DOWNLOAD_VERSION, "").orEmpty()
    private fun wasCheckedRecently(): Boolean {
        val lastCheckedAt = preferences.getLong(KEY_LAST_CHECKED_AT, 0L)
        return lastCheckedAt > 0L && System.currentTimeMillis() - lastCheckedAt < AUTO_CHECK_INTERVAL_MS
    }

    private fun clearSavedDownload(removeDownload: Boolean = false) {
        val id = savedDownloadId()
        if (removeDownload && id >= 0) downloadManager.remove(id)
        preferences.edit {
            remove(KEY_DOWNLOAD_ID)
            remove(KEY_DOWNLOAD_VERSION)
        }
    }

    companion object {
        private const val RELEASES_FEED_URL = "https://github.com/zxm965/CullPear-Native/releases.atom"
        private const val RELEASE_DOWNLOAD_ROOT = "https://github.com/zxm965/CullPear-Native/releases/download"
        private const val APK_MIME_TYPE = "application/vnd.android.package-archive"
        private const val PREFERENCES_NAME = "app_updates"
        private const val KEY_DOWNLOAD_ID = "download_id"
        private const val KEY_DOWNLOAD_VERSION = "download_version"
        private const val KEY_LAST_CHECKED_AT = "last_checked_at"
        private const val AUTO_CHECK_INTERVAL_MS = 6 * 60 * 60 * 1_000L

        fun parseLatestReleaseFeed(feed: String): ReleaseUpdate? = ENTRY_REGEX.findAll(feed)
            .mapNotNull { match ->
                val entry = match.value
                val version = RELEASE_VERSION_REGEX.find(entry)?.groupValues?.getOrNull(1).orEmpty()
                if (version.isBlank() || '-' in version) return@mapNotNull null
                val encodedContent = RELEASE_CONTENT_REGEX.find(entry)?.groupValues?.getOrNull(1).orEmpty()
                ReleaseUpdate(
                    version = version,
                    apkUrl = "$RELEASE_DOWNLOAD_ROOT/v$version/CullPear-$version.apk",
                    releaseNotes = formatReleaseNotes(atomHtmlToPlainText(encodedContent)),
                )
            }
            .firstOrNull()

        private fun atomHtmlToPlainText(encodedHtml: String): String = encodedHtml
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&amp;", "&")
            .replace(Regex("(?i)<br\\s*/?>"), "\n")
            .replace(Regex("(?i)</(?:p|li|h[1-6])>"), "\n")
            .replace(Regex("(?i)<li[^>]*>"), "- ")
            .replace(Regex("<[^>]+>"), "")
            .trim()

        fun formatReleaseNotes(markdown: String): String {
            val notes = markdown.lineSequence()
                .map(String::trim)
                .filter(String::isNotBlank)
                .filterNot { line ->
                    line.startsWith("**Full Changelog**", ignoreCase = true) ||
                        line.startsWith("Full Changelog", ignoreCase = true)
                }
                .map { line ->
                    line.removePrefix("### ")
                        .removePrefix("## ")
                        .removePrefix("# ")
                        .removePrefix("* ")
                        .removePrefix("- ")
                        .replace(Regex("\\[([^]]+)]\\([^)]+\\)"), "$1")
                        .replace("**", "")
                        .trim()
                }
                .filter(String::isNotBlank)
                .take(MAX_RELEASE_NOTE_LINES)
                .joinToString("\n")
            return notes.ifBlank { "修复已知问题并优化使用体验。" }
        }

        fun compareVersions(left: String, right: String): Int {
            val leftParts = left.substringBefore('-').split('.').map { it.toIntOrNull() ?: 0 }
            val rightParts = right.substringBefore('-').split('.').map { it.toIntOrNull() ?: 0 }
            val size = maxOf(leftParts.size, rightParts.size)
            for (index in 0 until size) {
                val comparison = (leftParts.getOrElse(index) { 0 }).compareTo(rightParts.getOrElse(index) { 0 })
                if (comparison != 0) return comparison
            }
            return 0
        }

        private const val MAX_RELEASE_NOTE_LINES = 8
        private val ENTRY_REGEX = Regex("<entry\\b.*?</entry>", RegexOption.DOT_MATCHES_ALL)
        private val RELEASE_VERSION_REGEX = Regex("<id>[^<]*/v([^<]+)</id>")
        private val RELEASE_CONTENT_REGEX = Regex(
            "<content\\b[^>]*>(.*?)</content>",
            RegexOption.DOT_MATCHES_ALL,
        )
    }
}
