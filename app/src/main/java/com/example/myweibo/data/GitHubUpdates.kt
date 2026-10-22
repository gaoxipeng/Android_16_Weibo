package com.example.myweibo.data

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

internal const val WeiboGitHubUrl = "https://github.com/gaoxipeng/Android_16_Weibo"
internal const val WeiboReleaseUrl = "$WeiboGitHubUrl/releases/latest"

internal fun isNewerRelease(local: String, remote: String): Boolean {
    fun parts(value: String): List<Long>? {
        val normalized = value.trim().removePrefix("v").removePrefix("V")
        if (!normalized.matches(Regex("[0-9]+(?:\\.[0-9]+)*"))) return null
        return normalized.split('.').map { it.toLongOrNull() ?: return null }
    }
    val a = parts(local) ?: return false
    val b = parts(remote) ?: return false
    for (index in 0 until maxOf(a.size, b.size)) {
        val delta = (b.getOrElse(index) { 0L }).compareTo(a.getOrElse(index) { 0L })
        if (delta != 0) return delta > 0
    }
    return false
}

internal data class WeiboReleaseInfo(val version: String, val versionCode: Long?)

internal fun isWeiboUpdateAvailable(localVersion: String, localCode: Long, release: WeiboReleaseInfo): Boolean =
    release.versionCode?.let { it > localCode } ?: isNewerRelease(localVersion, release.version)

internal fun releaseVersionCode(body: String): Long? =
    Regex("<!--\\s*versionCode:\\s*([0-9]+)\\s*-->").find(body)
        ?.groupValues?.get(1)?.toLongOrNull()?.takeIf { it > 0 }

internal suspend fun latestWeiboRelease(): WeiboReleaseInfo = withContext(Dispatchers.IO) {
    val connection = URL("https://api.github.com/repos/gaoxipeng/Android_16_Weibo/releases/latest")
        .openConnection() as HttpURLConnection
    try {
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.setRequestProperty("Accept", "application/vnd.github+json")
        connection.setRequestProperty("User-Agent", "Android_16_Weibo-update-check")
        val code = connection.responseCode
        check(code == 200) {
            if (code == 403 || code == 429) "检查过于频繁，请稍后重试或打开 GitHub Release"
            else "检查更新失败（HTTP $code），可打开 GitHub Release 查看"
        }
        val release = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
        check(!release.optBoolean("draft") && !release.optBoolean("prerelease")) { "暂无正式发布版本" }
        val body = release.optString("body")
        val version = (Regex("<!--\\s*versionName:\\s*([0-9]+(?:\\.[0-9]+)*)\\s*-->")
            .find(body)?.groupValues?.get(1) ?: release.getString("tag_name"))
            .also { check(it.matches(Regex("[vV]?[0-9]+(?:\\.[0-9]+)*"))) { "无法识别发布版本号" } }
        WeiboReleaseInfo(version, releaseVersionCode(body))
    } finally {
        connection.disconnect()
    }
}
