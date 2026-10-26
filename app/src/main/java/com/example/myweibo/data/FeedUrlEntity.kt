package com.example.myweibo.data

import androidx.compose.runtime.Immutable

@Immutable
data class FeedUrlEntity(
    val shortUrl: String,
    val title: String,
    val url: String,
    val nativeUrl: String? = null,
)

/** Preserve structured link targets; recognize bare URLs without swallowing emoji or prose. */
fun completeBareUrlEntities(
    text: String,
    entities: Map<String, FeedUrlEntity>,
    imageLinks: Set<String> = emptySet(),
): Map<String, FeedUrlEntity> = buildMap {
    putAll(entities)
    Regex("""https?://[^\s<>"'\[\]\u4e00-\u9fff，。！？；（）【】]+""", RegexOption.IGNORE_CASE)
        .findAll(text).forEach { match ->
            val url = match.value.trimEnd('.', ',', '!', '?', ')', ';', ':')
            if (url.isNotBlank() && url !in imageLinks && url !in this) {
                put(url, FeedUrlEntity(shortUrl = url, title = url, url = url))
            }
        }
}
