package com.example.myweibo.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class TimelineCacheStore(context: Context) {
    private val cacheFile = File(context.filesDir, "following_timeline_cache.json")
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val cacheMutex = Mutex()

    suspend fun readFollowingTimeline(): TimelinePage? =
        withContext(Dispatchers.IO) {
            runCatching {
                if (!cacheFile.exists()) return@withContext null
                val raw = cacheFile.readText(Charsets.UTF_8)
                WeiboJsonParser.parseTimeline(raw)
            }.getOrNull()
        }

    suspend fun writeFollowingTimeline(rawJson: String) {
        withContext(Dispatchers.IO) {
            cacheMutex.withLock {
                runCatching { writeCacheAtomically(rawJson) }
            }
        }
    }

    suspend fun appendFollowingTimeline(rawJson: String) = withContext(Dispatchers.IO) {
        cacheMutex.withLock {
            runCatching {
                val previous = if (cacheFile.exists()) JSONObject(cacheFile.readText(Charsets.UTF_8)) else JSONObject()
                val incoming = JSONObject(rawJson)
                val statuses = JSONArray()
                val seen = mutableSetOf<String>()
                for (root in listOf(previous, incoming)) {
                    val source = root.optJSONArray("statuses")
                        ?: root.optJSONObject("data")?.optJSONArray("statuses")
                        ?: root.optJSONObject("data")?.optJSONArray("list")
                        ?: root.optJSONArray("list")
                        ?: JSONArray()
                    for (index in 0 until source.length()) {
                        val status = source.optJSONObject(index) ?: continue
                        val id = status.optString("idstr").ifBlank { status.optString("id") }
                        if (id.isBlank() || seen.add(id)) statuses.put(status)
                    }
                }
                val cursor = WeiboJsonParser.parseTimeline(rawJson).nextCursor
                writeCacheAtomically(JSONObject().put("statuses", statuses).put("max_id", cursor).toString())
            }
        }
        Unit
    }

    private fun writeCacheAtomically(rawJson: String) {
        val temporary = File(cacheFile.parentFile, "${cacheFile.name}.tmp")
        temporary.writeText(rawJson, Charsets.UTF_8)
        check(temporary.renameTo(cacheFile)) { "Unable to persist timeline cache" }
    }

    fun readFollowingScroll(): Pair<Int, Int> =
        prefs.getInt(KEY_SCROLL_INDEX, 0) to prefs.getInt(KEY_SCROLL_OFFSET, 0)

    fun readFollowingAnchor(): String? = prefs.getString("scroll_status_id", null)

    fun writeFollowingScroll(index: Int, offset: Int, statusId: String? = null) {
        prefs.edit()
            .putInt(KEY_SCROLL_INDEX, index.coerceAtLeast(0))
            .putInt(KEY_SCROLL_OFFSET, offset.coerceAtLeast(0))
            .putString("scroll_status_id", statusId)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "following_timeline_state"
        private const val KEY_SCROLL_INDEX = "scroll_index"
        private const val KEY_SCROLL_OFFSET = "scroll_offset"
    }
}
