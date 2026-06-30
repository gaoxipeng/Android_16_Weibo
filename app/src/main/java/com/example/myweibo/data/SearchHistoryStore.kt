package com.example.myweibo.data

import android.content.Context
import org.json.JSONArray

class SearchHistoryStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun read(limit: Int = MAX_ITEMS): List<String> {
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val query = array.optString(index).trim()
                    if (query.isNotBlank() && query !in this) add(query)
                    if (size >= limit) break
                }
            }
        }.getOrDefault(emptyList())
    }

    fun touch(query: String): List<String> {
        val cleaned = query.trim()
        if (cleaned.isBlank()) return read()
        val updated = (listOf(cleaned) + read(MAX_ITEMS)).distinct().take(MAX_ITEMS)
        val array = JSONArray()
        updated.forEach(array::put)
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply()
        return updated
    }

    fun remove(query: String): List<String> {
        val cleaned = query.trim()
        if (cleaned.isBlank()) return read()
        val updated = read(MAX_ITEMS).filterNot { it.equals(cleaned, ignoreCase = false) }
        val array = JSONArray()
        updated.forEach(array::put)
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply()
        return updated
    }

    fun clear(): List<String> {
        prefs.edit().remove(KEY_HISTORY).apply()
        return emptyList()
    }

    companion object {
        const val PREFS_NAME = "weibo_app_prefs"
        const val KEY_HISTORY = "search_history"
        const val MAX_ITEMS = 100
        const val DISPLAY_MAX_ROWS = 4
    }
}
