package com.example.myweibo.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class TimelineCacheStore(context: Context) {
    private val cacheFile = File(context.filesDir, "following_timeline_cache.json")

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
}
