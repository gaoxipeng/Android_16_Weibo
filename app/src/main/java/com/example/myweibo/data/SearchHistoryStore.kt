package com.example.myweibo.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class SearchHistoryEntry(val query: String, val userId: String? = null, val avatarUrl: String? = null,
    val verification: UserVerification? = null) {
    val key: String get() = userId?.let { "user:$it" } ?: "query:$query"
}

class SearchHistoryStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun read(limit: Int = MAX_ITEMS): List<SearchHistoryEntry> {
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList<SearchHistoryEntry> {
                for (index in 0 until array.length()) {
                    val obj = array.optJSONObject(index)
                    val query = (obj?.optString("query") ?: array.optString(index)).trim()
                    if (query.isBlank()) continue
                    val entry = SearchHistoryEntry(query,
                        obj?.optString("userId")?.takeIf { it.isNotBlank() && it != "null" },
                        obj?.optString("avatarUrl")?.takeIf { it.isNotBlank() && it != "null" },
                        UserVerification.entries.firstOrNull { it.name == obj?.optString("verification") })
                    if (none { it.key == entry.key }) add(entry)
                    if (size >= limit) break
                }
            }
        }.getOrDefault(emptyList())
    }

    fun touch(query: String): List<SearchHistoryEntry> {
        val cleaned = query.trim()
        if (cleaned.isBlank()) return read()
        return touchEntry(SearchHistoryEntry(cleaned))
    }

    fun touchUser(user: SearchUserItem): List<SearchHistoryEntry> {
        if (user.id.isBlank()) return read()
        return touchEntry(SearchHistoryEntry(user.screenName.ifBlank { user.name }.ifBlank { user.id }, user.id, user.avatarUrl, user.verification))
    }

    fun touchEntry(entry: SearchHistoryEntry): List<SearchHistoryEntry> =
        save((listOf(entry) + read()).distinctBy { it.key }.take(MAX_ITEMS))

    fun remove(key: String): List<SearchHistoryEntry> = save(read().filterNot { it.key == key })

    private fun save(updated: List<SearchHistoryEntry>): List<SearchHistoryEntry> {
        val array = JSONArray()
        updated.forEach { entry -> array.put(JSONObject().put("query", entry.query)
            .put("userId", entry.userId).put("avatarUrl", entry.avatarUrl).put("verification", entry.verification?.name)) }
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply()
        return updated
    }

    fun clear(): List<SearchHistoryEntry> {
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
