package com.example.myweibo.data

import org.json.JSONObject

/** Read only fields present in the response. Missing counts are not zero votes. */
internal fun parseFeedPoll(status: JSONObject, links: List<FeedUrlEntity>): FeedPoll? {
    val page = status.optJSONObject("page_info")
    val explicit = page?.optJSONObject("card_info")?.optJSONObject("vote_object")
        ?: status.optJSONObject("vote_info") ?: status.optJSONObject("vote")
        ?: page?.optJSONObject("vote_info") ?: page?.optJSONObject("vote")
        ?: page?.optJSONObject("vote_object")
    val link = links.firstOrNull {
        it.title.trim() == "微博投票" || Regex("""https?://vote\.weibo\.(?:com|cn)(?:/|$)""", RegexOption.IGNORE_CASE).containsMatchIn(it.url)
    }
    val isVotePage = listOf("object_type", "type", "page_type").any {
        page?.optString(it)?.lowercase() in setOf("vote", "poll", "hudongvote")
    }
    if (explicit == null && link == null && !isVotePage) return null
    val data = explicit ?: page ?: JSONObject()
    fun JSONObject.label(vararg names: String): String? = names.firstNotNullOfOrNull { key ->
        opt(key)?.takeUnless { it == JSONObject.NULL }?.toString()?.trim()?.takeIf { it.isNotEmpty() }
    }
    fun JSONObject.count(vararg names: String): Long? = names.firstNotNullOfOrNull {
        opt(it)?.toString()?.toLongOrNull()?.takeIf { value -> value >= 0 }
    }
    val options = data.optJSONArray("options") ?: data.optJSONArray("option_list")
        ?: data.optJSONArray("vote_items") ?: data.optJSONArray("vote_list")
    val parsedOptions = buildList {
        if (options != null) for (index in 0 until options.length()) {
            val option = options.optJSONObject(index)
            val text = option?.label("text", "title", "content", "option", "name")
                ?: options.optString(index).takeIf { option == null && it.isNotBlank() }
                ?: continue
            add(FeedPollOption(text.replace(Regex("<[^>]+>"), ""),
                option?.count("part_num", "vote_count", "count", "num"),
                option?.optBoolean("selected", false) == true || option?.optInt("selected", 0) == 1 || option?.optInt("is_voted", 0) == 1,
                option?.label("id", "option_id"),
                option?.label("part_num", "vote_count", "count", "num")))
        }
    }
    val title = data.label("title", "vote_title", "question", "page_title", "content")
        ?: page?.label("page_title", "title") ?: link?.title ?: "微博投票"
    val url = data.label("target_url", "page_url", "url", "h5_url") ?: page?.label("page_url", "url") ?: link?.url
    return FeedPoll(
        title = title.replace(Regex("<[^>]+>"), ""),
        options = parsedOptions,
        participantCount = data.count("participant_count", "total_count", "total", "join_num", "part_info", "part_num"),
        endTime = data.label("end_time", "expire_time", "endtime", "expire_date", "end_time_str"),
        url = url?.let { if (it.startsWith("//")) "https:$it" else it },
        id = data.label("id", "vote_id"),
        isEnded = data.optInt("status", 1) == 0 || data.optInt("state", 1) == 0,
        hasVoted = data.optInt("parted", 0) == 1 || parsedOptions.any { it.selected },
        maximum = data.optInt("maximum", 1).coerceAtLeast(1),
        minimum = data.optInt("minimum", 1).coerceAtLeast(1),
    )
}

/** Parse JSON bootstrap data without executing any page JavaScript. Never expose sk to UI/logs. */
internal fun parseVotePageBootstrap(html: String): JSONObject {
    val marker = Regex("""(?:window\.)?__DATA__\s*=\s*""").find(html)
        ?: throw IllegalStateException("投票页面未返回登录数据，请重新登录后再试")
    val source = html.substring(marker.range.last + 1).trimStart()
    val value = if (source.startsWith("JSON.parse(")) {
        val serialized = org.json.JSONTokener(source.removePrefix("JSON.parse(")).nextValue() as? String
            ?: throw IllegalStateException("投票页面数据格式无法解析")
        JSONObject(serialized)
    } else org.json.JSONTokener(source).nextValue() as? JSONObject
        ?: throw IllegalStateException("投票页面数据格式无法解析")
    if (value.optJSONObject("vote_info") == null) throw IllegalStateException("投票数据不可用，可能已删除或当前账号无权参与")
    return value
}

internal fun pollFromVoteInfo(info: JSONObject): FeedPoll = requireNotNull(parseFeedPoll(
    JSONObject().put("vote_info", info), emptyList(),
))
