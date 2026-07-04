package com.example.myweibo.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class TimelineCacheStore(context: Context) {
    private val cacheFile = File(context.filesDir, "following_timeline_cache.json")
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

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
            runCatching {
                cacheFile.writeText(rawJson, Charsets.UTF_8)
            }
        }
    }

    fun readFollowingScroll(): Pair<Int, Int> =
        prefs.getInt(KEY_SCROLL_INDEX, 0) to prefs.getInt(KEY_SCROLL_OFFSET, 0)

    fun writeFollowingScroll(index: Int, offset: Int) {
        prefs.edit()
            .putInt(KEY_SCROLL_INDEX, index.coerceAtLeast(0))
            .putInt(KEY_SCROLL_OFFSET, offset.coerceAtLeast(0))
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "following_timeline_state"
        private const val KEY_SCROLL_INDEX = "scroll_index"
        private const val KEY_SCROLL_OFFSET = "scroll_offset"
    }
}
