package com.zxm965.cullpear.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zxm965.cullpear.core.designsystem.theme.color
import com.zxm965.cullpear.core.preferences.Accent
import com.zxm965.cullpear.core.preferences.ThemeMode
import com.zxm965.cullpear.core.preferences.ThemePreferences
import com.zxm965.cullpear.BuildConfig
import com.zxm965.cullpear.core.update.AppUpdateManager
import com.zxm965.cullpear.core.update.UpdateState
import com.zxm965.cullpear.ui.components.ContentCard
import com.zxm965.cullpear.ui.components.ContentList
import com.zxm965.cullpear.ui.components.InfoRow
import com.zxm965.cullpear.ui.components.PageHeader
import kotlinx.coroutines.launch

@Composable
fun SettingsRoute(preferences: ThemePreferences, updateManager: AppUpdateManager) {
    val scope = rememberCoroutineScope()
    ContentList {
        item { PageHeader("偏好", "设置") }
        item {
            Text("外观模式", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ThemeMode.entries.forEach { mode ->
                    SelectButton(
                        label = when (mode) { ThemeMode.SYSTEM -> "跟随系统"; ThemeMode.LIGHT -> "浅色"; ThemeMode.DARK -> "深色" },
                        selected = preferences.mode == mode,
                        onClick = { preferences.updateMode(mode) },
                    )
                }
            }
        }
        item {
            Text("主题色", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Accent.entries.forEach { accent ->
                    SelectButton(
                        label = when (accent) { Accent.PEAR -> "青梨"; Accent.OCEAN -> "海蓝"; Accent.BERRY -> "莓红"; Accent.AMBER -> "琥珀" },
                        selected = preferences.accent == accent,
                        selectedColor = accent.color(),
                        onClick = { preferences.updateAccent(accent) },
                    )
                }
            }
        }
        item {
            ContentCard {
                InfoRow("外观", preferences.mode.name)
                InfoRow("主题色", preferences.accent.name)
            }
        }
        item {
            Text("应用更新", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            ContentCard {
                InfoRow("当前版本", BuildConfig.VERSION_NAME)
                Text(updateStateLabel(updateManager.state), color = MaterialTheme.colorScheme.onSurfaceVariant)
                when (updateManager.state) {
                    UpdateState.Disabled -> Unit
                    is UpdateState.Available -> {
                        OutlinedButton(onClick = updateManager::downloadAvailableUpdate) { Text("下载更新") }
                    }
                    is UpdateState.ReadyToInstall -> {
                        OutlinedButton(onClick = updateManager::installDownloadedUpdate) { Text("安装更新") }
                    }
                    is UpdateState.PermissionRequired -> {
                        OutlinedButton(onClick = updateManager::installDownloadedUpdate) { Text("继续安装") }
                    }
                    is UpdateState.Checking, is UpdateState.Downloading -> Unit
                    else -> {
                        OutlinedButton(onClick = { scope.launch { updateManager.checkForUpdate(force = true) } }) {
                            Text("检查更新")
                        }
                    }
                }
            }
        }
    }
}

private fun updateStateLabel(state: UpdateState): String = when (state) {
    UpdateState.Disabled -> "开发版本不检查应用更新。"
    UpdateState.Idle -> "启动后会自动检查更新。"
    UpdateState.Checking -> "正在检查更新…"
    is UpdateState.UpToDate -> "已是最新版本 ${state.version}。"
    is UpdateState.Available -> "发现新版本 ${state.update.version}。"
    is UpdateState.Downloading -> "版本 ${state.version} 正在后台下载…"
    is UpdateState.ReadyToInstall -> "版本 ${state.version} 已下载，等待安装。"
    is UpdateState.PermissionRequired -> "需要允许本应用安装更新包。"
    is UpdateState.Error -> state.message
}

@Composable
private fun SelectButton(label: String, selected: Boolean, selectedColor: Color = MaterialTheme.colorScheme.primary, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) selectedColor else MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label)
            if (selected) Text("已选", color = selectedColor, fontWeight = FontWeight.Bold)
        }
    }
}
