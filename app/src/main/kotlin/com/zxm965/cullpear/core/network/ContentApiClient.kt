package com.zxm965.cullpear.core.network

import com.zxm965.cullpear.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ContentApiClient(
    private val baseUrl: String = BuildConfig.API_BASE_URL.trimEnd('/'),
) {
    suspend fun get(path: String, query: Map<String, String> = emptyMap()): JSONObject =
        withContext(Dispatchers.IO) {
            val suffix = if (query.isEmpty()) "" else query.entries.joinToString("&", prefix = "?") {
                "${it.key}=${java.net.URLEncoder.encode(it.value, Charsets.UTF_8.name())}"
            }
            val connection = URL("$baseUrl/${path.trimStart('/')}$suffix")
                .openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "GET"
                connection.connectTimeout = 15_000
                connection.readTimeout = 15_000
                connection.setRequestProperty("Accept", "application/json")

                val status = connection.responseCode
                val stream = if (status in 200..299) connection.inputStream else connection.errorStream
                val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                if (status !in 200..299) {
                    val message = runCatching { JSONObject(body).optString("message") }.getOrNull()
                    throw ApiException(message?.takeIf(String::isNotBlank) ?: "服务请求失败（$status）")
                }

                val response = JSONObject(body)
                if (response.optInt("status", 200) != 200) {
                    throw ApiException(response.optString("message", "服务返回异常"))
                }
                response
            } catch (error: ApiException) {
                throw error
            } catch (error: Exception) {
                throw ApiException("网络连接失败，请检查网络后重试。", error)
            } finally {
                connection.disconnect()
            }
        }
}

class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause)
