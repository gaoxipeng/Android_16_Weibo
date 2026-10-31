package com.example.myweibo.data

import org.json.JSONArray
import org.json.JSONObject

internal data class TimelineChannel(val groupId: String, val containerId: String)

data class TimelineGroup(val id: String, val name: String, val member: Boolean? = null)

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

internal fun parseTimelineGroups(raw: String): List<TimelineGroup> {
    val groups = linkedMapOf<String, TimelineGroup>()
    fun visit(value: Any?) {
        when (value) {
            is JSONObject -> {
                val id = listOf("list_id", "listid", "gid", "group_id", "id")
                    .map { value.optString(it) }
                    .firstOrNull { it.isNotBlank() && it != "null" && it != "0" }
                val name = listOf("name", "title", "name_cn")
                    .map { value.optString(it).trim() }
                    .firstOrNull { it.isNotBlank() && it != "null" }
                if (id != null && name != null && name.length <= 32) {
                    val member = when {
                        value.has("is_member") -> value.optBoolean("is_member")
                        value.has("member") -> value.optBoolean("member")
                        else -> null
                    }
                    groups.putIfAbsent(id, TimelineGroup(id, name, member))
                }
                value.keys().forEach { visit(value.opt(it)) }
            }
            is JSONArray -> for (index in 0 until value.length()) visit(value.opt(index))
        }
    }
    visit(JSONObject(raw))
    val reserved = setOf("未分组", "好友圈", "特别关注", "热门", "推荐", "同城")
    return groups.values.filterNot { it.name in reserved }
}
