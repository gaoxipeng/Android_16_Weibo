package com.example.myweibo.data

import org.json.JSONObject

data class WeiboArticle(
    val id: String,
    val title: String,
    val authorName: String?,
    val htmlContent: String,
    val showUrl: String,
)

fun resolveArticleId(url: String): String? {
    val trimmed = url.trim()
    if (trimmed.isBlank()) return null

    Regex("""[?&]id=(\d+)""").find(trimmed)?.groupValues?.getOrNull(1)?.let { return it }
    Regex("""/article/m/show/id/(\d+)""").find(trimmed)?.groupValues?.getOrNull(1)?.let { return it }
    Regex("""/ttarticle/p/show""").find(trimmed)?.let {
        return Regex("""[?&]id=(\d+)""").find(trimmed)?.groupValues?.getOrNull(1)
    }
    Regex("""object_id=1022:(\d+)""").find(trimmed)?.groupValues?.getOrNull(1)?.let { return it }
    return null
}

fun articleShowUrl(articleId: String): String =
    "https://card.weibo.com/article/m/show/id/$articleId"

object WeiboArticleParser {
    fun parseDetail(raw: String, articleId: String): WeiboArticle {
        val root = JSONObject(raw)
        if (root.optInt("ok", 1) <= 0 && root.optNullableString("code") != "100000") {
            throw IllegalStateException(
                root.optNullableString("msg")
                    ?: root.optNullableString("message")
                    ?: "文章加载失败",
            )
        }
        val data = root.optJSONObject("data")
            ?: throw IllegalStateException("文章接口未返回内容")
        val title = data.optNullableString("title")
            ?: data.optNullableString("article_title")
            ?: "微博文章"
        val content = data.optNullableString("content")
            ?: data.optNullableString("article")
            ?: throw IllegalStateException("文章内容为空")
        val authorName = data.optJSONObject("userinfo")?.optNullableString("screen_name")
            ?: data.optJSONObject("user")?.optNullableString("screen_name")
        return WeiboArticle(
            id = articleId,
            title = title,
            authorName = authorName,
            htmlContent = content,
            showUrl = articleShowUrl(articleId),
        )
    }

    private fun JSONObject.optNullableString(name: String): String? {
        if (!has(name) || isNull(name)) return null
        val value = opt(name)?.toString()?.trim().orEmpty()
        return value.takeIf { it.isNotBlank() && it != "0" }
    }
}
