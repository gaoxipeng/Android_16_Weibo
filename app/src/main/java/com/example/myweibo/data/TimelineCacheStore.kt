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
    private val appContext = context.applicationContext
    // Only verified account IDs are accepted. Never adopt the old unowned file or prefs.
    private fun accountSuffix(accountId: String): String {
        require(accountId.isNotEmpty() && accountId != "0" && accountId.all { it in '0'..'9' })
        return "_account_$accountId"
    }

    private fun cacheFile(accountId: String) =
        File(appContext.filesDir, "following_timeline_cache${accountSuffix(accountId)}.json")

    private fun prefs(accountId: String) = appContext.getSharedPreferences(
        PREFS_NAME + accountSuffix(accountId), Context.MODE_PRIVATE,
    )
    private val cacheMutex = Mutex()

    suspend fun readFollowingTimeline(accountId: String): TimelinePage? =
        withContext(Dispatchers.IO) {
            runCatching {
                val cacheFile = cacheFile(accountId)
                if (!cacheFile.exists()) return@withContext null
                val raw = cacheFile.readText(Charsets.UTF_8)
                WeiboJsonParser.parseTimeline(raw)
            }.getOrNull()
        }

    suspend fun writeFollowingTimeline(accountId: String, rawJson: String): Set<String>? =
        withContext(Dispatchers.IO) {
            cacheMutex.withLock {
                runCatching { writeCacheAtomically(cacheFile(accountId), rawJson) }.getOrNull()
            }
        }

    suspend fun appendFollowingTimeline(accountId: String, rawJson: String): Set<String>? = withContext(Dispatchers.IO) {
        cacheMutex.withLock {
            runCatching {
                val cacheFile = cacheFile(accountId)
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
                writeCacheAtomically(cacheFile, JSONObject().put("statuses", statuses).put("max_id", cursor).toString())
            }.getOrNull()
        }
    }

    // Return the IDs the same parser can actually restore, only after the atomic rename succeeds.
    private fun writeCacheAtomically(cacheFile: File, rawJson: String): Set<String> {
        val ids = WeiboJsonParser.parseTimeline(rawJson).items.mapTo(HashSet()) { it.id }
        val temporary = File(cacheFile.parentFile, "${cacheFile.name}.tmp")
        temporary.writeText(rawJson, Charsets.UTF_8)
        check(temporary.renameTo(cacheFile)) { "Unable to persist timeline cache" }
        return ids
    }

    fun readFollowingScroll(accountId: String): Pair<Int, Int> = prefs(accountId).let {
        it.getInt(KEY_SCROLL_INDEX, 0) to it.getInt(KEY_SCROLL_OFFSET, 0)
    }

    fun readFollowingAnchor(accountId: String): String? = prefs(accountId).getString("scroll_status_id", null)

    fun writeFollowingScroll(accountId: String, index: Int, offset: Int, statusId: String) {
        prefs(accountId).edit()
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
