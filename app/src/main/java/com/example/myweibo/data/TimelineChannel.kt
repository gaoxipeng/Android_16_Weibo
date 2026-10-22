package com.example.myweibo.data

import org.json.JSONArray
import org.json.JSONObject

internal data class TimelineChannel(val groupId: String, val containerId: String)

internal fun webRecommendationChannel() = TimelineChannel("102803", "102803")

// Use the account's server-provided channel rather than hard-coding a city or
// requesting device location without consent. Never substitute another feed.
internal fun findTimelineChannel(raw: String, label: String): TimelineChannel? {
    fun visit(value: Any?): TimelineChannel? {
        when (value) {
            is JSONObject -> {
                val title = listOf("title", "name", "name_cn").map { value.optString(it) }
                if (title.any { it.trim() == label }) {
                    val group = listOf("gid", "group_id", "id").map { value.optString(it) }
                        .firstOrNull { it.isNotBlank() && it != "null" && it != "0" }
                    val container = value.optString("containerid").takeIf { it.isNotBlank() && it != "null" }
                    if (group != null) return TimelineChannel(group, container ?: group)
                }
                for (key in value.keys()) visit(value.opt(key))?.let { return it }
            }
            is JSONArray -> for (index in 0 until value.length()) visit(value.opt(index))?.let { return it }
        }
        return null
    }
    return visit(JSONObject(raw))
}
