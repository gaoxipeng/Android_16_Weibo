package com.example.myweibo.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

class EmoticonCacheStore(context: Context) {
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
}
