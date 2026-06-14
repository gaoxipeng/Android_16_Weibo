package com.example.myweibo.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class EmoticonCacheStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("weibo_emoticon_cache", Context.MODE_PRIVATE)
    private val cacheFile = File(context.filesDir, "emoticon_config_cache.json")

    suspend fun read(): Map<String, String> =
        withContext(Dispatchers.IO) {
            runCatching {
                if (!cacheFile.exists()) return@withContext emptyMap()
                val root = JSONObject(cacheFile.readText(Charsets.UTF_8))
                buildMap {
                    root.keys().forEach { phrase ->
                        val url = root.optString(phrase).trim()
                        if (phrase.isNotBlank() && url.isNotBlank()) {
                            put(phrase, url)
                        }
                    }
                }
            }.getOrDefault(emptyMap())
        }

    suspend fun write(map: Map<String, String>) {
        withContext(Dispatchers.IO) {
            runCatching {
                val root = JSONObject()
                map.forEach { (phrase, url) ->
                    if (phrase.isNotBlank() && url.isNotBlank()) {
                        root.put(phrase, url)
                    }
                }
                cacheFile.writeText(root.toString(), Charsets.UTF_8)
            }
        }
    }

    fun readRecent(limit: Int = RECENT_LIMIT): List<String> {
        val raw = prefs.getString(KEY_RECENT, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val phrase = array.optString(index).trim()
                    if (phrase.isNotBlank() && phrase !in this) add(phrase)
                    if (size >= limit) break
                }
            }
        }.getOrDefault(emptyList())
    }

    fun touchRecent(phrase: String, limit: Int = RECENT_LIMIT): List<String> {
        val cleaned = phrase.trim()
        if (cleaned.isBlank()) return readRecent(limit)
        val updated = (listOf(cleaned) + readRecent(limit)).distinct().take(limit)
        val array = JSONArray()
        updated.forEach(array::put)
        prefs.edit().putString(KEY_RECENT, array.toString()).apply()
        return updated
    }

    private companion object {
        const val KEY_RECENT = "recent_emoticons"
        const val RECENT_LIMIT = 21
    }
}
