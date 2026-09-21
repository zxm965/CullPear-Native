package com.zxm965.cullpear.core.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

interface ImageLoader {
    suspend fun load(url: String): Result<Bitmap>
}

object RemoteImageLoader : ImageLoader {
    private val memoryCache = object : LruCache<String, Bitmap>(cacheSizeKilobytes()) {
        override fun sizeOf(key: String, value: Bitmap): Int =
            (value.allocationByteCount / 1_024).coerceAtLeast(1)
    }

    override suspend fun load(url: String): Result<Bitmap> {
        if (url.isBlank()) return Result.failure(IllegalArgumentException("图片地址为空"))
        synchronized(memoryCache) { memoryCache.get(url) }?.let { return Result.success(it) }

        return withContext(Dispatchers.IO) {
            runCatching {
                val connection = URL(url).openConnection() as HttpURLConnection
                try {
                    connection.connectTimeout = CONNECT_TIMEOUT_MS
                    connection.readTimeout = READ_TIMEOUT_MS
                    connection.instanceFollowRedirects = true
                    connection.setRequestProperty("Accept", "image/*")
                    val status = connection.responseCode
                    if (status !in 200..299) throw IllegalStateException("图片请求失败（$status）")
                    val bitmap = connection.inputStream.use(BitmapFactory::decodeStream)
                        ?: throw IllegalStateException("无法解析图片")
                    synchronized(memoryCache) { memoryCache.put(url, bitmap) }
                    bitmap
                } finally {
                    connection.disconnect()
                }
            }
        }
    }

    private fun cacheSizeKilobytes(): Int =
        (Runtime.getRuntime().maxMemory() / 1_024L / MEMORY_CACHE_DIVISOR).toInt()

    private const val CONNECT_TIMEOUT_MS = 10_000
    private const val READ_TIMEOUT_MS = 10_000
    private const val MEMORY_CACHE_DIVISOR = 8
}
