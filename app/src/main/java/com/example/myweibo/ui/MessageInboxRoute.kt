package com.example.myweibo.ui

import android.net.Uri

internal fun isWeiboMessageInboxUrl(url: String?): Boolean {
    if (url.isNullOrBlank()) return true
    val route = runCatching { Uri.parse(url) }.getOrNull() ?: return true
    val path = route.path.orEmpty().lowercase()
    val fragment = route.fragment.orEmpty()
    val combined = "$path $fragment"
    if (Regex("/(chat|conversation|dialog|private|directmessage)(/|$)").containsMatchIn(combined)) return false
    if (listOf("uid", "to_uid", "toid", "to_id", "conversation_id")
            .any { !route.getQueryParameter(it).isNullOrBlank() }
    ) return false
    return true
}

internal fun isWeiboMessageInboxTitle(title: String?): Boolean {
    val text = title?.trim().orEmpty()
    if (text.isEmpty()) return true
    val inboxTitles = listOf("消息箱", "消息", "@我的", "评论", "赞", "未关注人私信", "提到我的")
    return inboxTitles.any { marker -> text == marker || text.contains(marker) }
}
