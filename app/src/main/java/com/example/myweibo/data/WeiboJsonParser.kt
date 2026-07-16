package com.example.myweibo.data

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object WeiboJsonParser {
    fun parseTimeline(raw: String): TimelinePage {
        val root = JSONObject(raw)
        val statuses = root.optJSONArray("statuses")
            ?: root.optJSONObject("data")?.optJSONArray("statuses")
            ?: root.optJSONObject("data")?.optJSONArray("list")
            ?: root.optJSONArray("list")
            ?: JSONArray()
        val items = buildList {
            for (index in 0 until statuses.length()) {
                val status = statuses.optJSONObject(index) ?: continue
                if (status.looksLikeAd()) continue
                parseStatus(status, allowRetweeted = true)?.let(::add)
            }
        }
        val nextCursor = root.optNullableString("max_id")
            ?: root.optJSONObject("data")?.optNullableString("max_id")
            ?: root.optJSONObject("data")?.optNullableString("next_cursor")
        return TimelinePage(items = items, nextCursor = nextCursor)
    }

    fun parseUserTimeline(raw: String, uid: String, pageForCursor: Int? = null): TimelinePage {
        val expectedUid = uid.trim()
        val timelinePage = parseTimeline(raw)
        val nextCursor = timelinePage.nextCursor
            ?: pageForCursor
                ?.takeIf { timelinePage.items.isNotEmpty() }
                ?.let { (it + 1).toString() }
        if (expectedUid.isBlank()) return timelinePage.copy(nextCursor = nextCursor)
        return timelinePage.copy(
            items = timelinePage.items.filter { item ->
                item.authorId.trim() == expectedUid
            },
            nextCursor = nextCursor,
        )
    }

    fun parseStatusDetail(raw: String): FeedItem? {
        val root = JSONObject(raw)
        val status = unwrapStatusDetailPayload(root) ?: return null
        if (status.looksLikeAd()) return null
        return parseStatus(status, allowRetweeted = true)
    }

    fun parseComments(raw: String): List<CommentItem> {
        val root = JSONObject(raw)
        val data = root.optJSONArray("data")
            ?: root.optJSONObject("data")?.optJSONArray("comments")
            ?: JSONArray()
        return buildList {
            for (index in 0 until data.length()) {
                val comment = data.optJSONObject(index) ?: continue
                val user = comment.optJSONObject("user")
                val id = comment.optNullableString("idstr")
                    ?: comment.optNullableString("id")
                    ?: continue
                val htmlText = comment.optNullableString("text") ?: ""
                val images = parseCommentImages(comment)
                add(
                    CommentItem(
                        id = id,
                        authorId = userId(user),
                        authorName = parseUserDisplayName(user),
                        authorAvatarUrl = user?.optNullableString("avatar_hd")
                            ?: user?.optNullableString("profile_image_url"),
                        text = parseCommentText(comment, images),
                        createdAt = comment.optNullableString("created_at"),
                        likesCount = formatCount(comment.opt("like_counts")),
                        ipLocation = parseIpLocation(comment),
                        emoticons = extractEmoticonsFromHtml(htmlText),
                        images = images,
                        comments = parseNestedComments(comment.optJSONArray("comments")),
                        replyToAuthor = comment.optJSONObject("reply_comment")
                            ?.optJSONObject("user")
                            ?.let { parseUserDisplayName(it) },
                        replyToAuthorId = userId(comment.optJSONObject("reply_comment")?.optJSONObject("user"))
                            .takeIf { it.isNotBlank() },
                        moreInfoText = comment.optJSONObject("more_info")?.optNullableString("text"),
                    )
                )
            }
        }
    }

    fun parseCommentsTotalCount(raw: String): Int? {
        val root = JSONObject(raw)
        val data = root.optJSONObject("data")
        return listOf(
            data?.optInt("total_number", 0) ?: 0,
            data?.optInt("count", 0) ?: 0,
            data?.optInt("total", 0) ?: 0,
            data?.optJSONObject("status")?.optInt("comments_count", 0) ?: 0,
            root.optInt("total_number", 0),
            root.optInt("comments_count", 0),
        ).firstOrNull { it > 0 }
    }

    fun parseMweiboAttitudes(raw: String, page: Int = 1): LikeUsersPage {
        val root = JSONObject(raw)
        if (root.optInt("ok", 1) <= 0) {
            throw IllegalStateException(
                root.optNullableString("msg")
                    ?: root.optNullableString("message")
                    ?: "加载点赞列表失败",
            )
        }
        val data = root.optJSONObject("data") ?: JSONObject()
        val usersArray = data.optJSONArray("data") ?: JSONArray()
        val users = buildList {
            for (index in 0 until usersArray.length()) {
                val entry = usersArray.optJSONObject(index) ?: continue
                val user = entry.optJSONObject("user") ?: entry
                val id = user.optNullableString("idstr")
                    ?: user.optNullableString("id")
                    ?: continue
                add(
                    MentionCandidate(
                        id = id,
                        name = user.optNullableString("screen_name")
                            ?: user.optNullableString("name")
                            ?: "微博用户",
                        avatarUrl = user.optNullableString("profile_image_url")
                            ?: user.optNullableString("avatar_hd"),
                    ),
                )
            }
        }
        val totalCount = listOf(
            data.optInt("total_number", 0),
            data.optInt("count", 0),
            data.optInt("total", 0),
            data.optInt("attitudes_count", 0),
            root.optInt("total_number", 0),
        ).firstOrNull { it > 0 }
        val maxPage = listOf(
            data.optInt("max", 0),
            data.optInt("max_page", 0),
            root.optInt("max", 0),
        ).firstOrNull { it > 0 }
        val nextPage = when {
            users.isEmpty() -> null
            users.size >= MWEIBO_ATTITUDES_PAGE_SIZE -> page + 1
            maxPage != null && page < maxPage -> page + 1
            else -> null
        }
        return LikeUsersPage(users = users, nextPage = nextPage, totalCount = totalCount)
    }

    private const val MWEIBO_ATTITUDES_PAGE_SIZE = 20

    fun parsePcReposts(raw: String, page: Int = 1): RepostsPage {
        val root = JSONObject(raw)
        if (root.has("ok") && root.optInt("ok", 1) != 1) {
            val message = root.optNullableString("msg")
                ?: root.optNullableString("message")
                ?: root.optNullableString("error")
                ?: ""
            if (message.isBlank() || message.contains("暂无") || message.contains("没有") || message.contains("为空")) {
                return RepostsPage(items = emptyList(), nextPage = null)
            }
            throw IllegalStateException(message)
        }
        val data = root.optJSONObject("data")
        val arr = data?.optJSONArray("data")
            ?: data?.optJSONArray("list")
            ?: data?.optJSONArray("statuses")
            ?: data?.optJSONArray("reposts")
            ?: root.optJSONArray("data")
            ?: root.optJSONArray("list")
            ?: root.optJSONArray("statuses")
            ?: root.optJSONArray("reposts")
            ?: JSONArray()
        val items = buildList {
            for (index in 0 until arr.length()) {
                val repost = arr.optJSONObject(index) ?: continue
                parseRepostItem(repost)?.let(::add)
            }
        }
        val nextCursor = data?.optNullableString("max_id")
            ?: data?.optNullableString("next_cursor")
            ?: root.optNullableString("max_id")
            ?: root.optNullableString("next_cursor")
        val totalNumber = data?.optLong("total_number", 0L) ?: root.optLong("total_number", 0L)
        val hasMore = !nextCursor.isNullOrBlank() && nextCursor != "0"
        val nextPage = when {
            items.isEmpty() -> null
            hasMore -> page + 1
            totalNumber > 0L && page * PC_REPOST_PAGE_SIZE < totalNumber -> page + 1
            items.size >= PC_REPOST_PAGE_SIZE -> page + 1
            else -> null
        }
        return RepostsPage(
            items = items,
            nextPage = nextPage,
            totalCount = totalNumber.takeIf { it > 0L }?.toInt(),
        )
    }

    private const val PC_REPOST_PAGE_SIZE = 20

    private fun parseRepostItem(repost: JSONObject): CommentItem? {
        val normalized = normalizeMweiboStatus(repost)
        val user = normalized.optJSONObject("user")
        val id = normalized.optNullableString("idstr")
            ?: normalized.optNullableString("id")
            ?: return null
        val htmlText = normalized.optNullableString("text") ?: ""
        val images = parseCommentImages(normalized)
        val text = parseMweiboRepostText(normalized, htmlText, images)
        return CommentItem(
            id = id,
            authorId = userId(user),
            authorName = parseUserDisplayName(user),
            authorAvatarUrl = user?.optNullableString("avatar_hd")
                ?: user?.optNullableString("profile_image_url"),
            text = text,
            createdAt = normalized.optNullableString("created_at"),
            likesCount = formatCount(normalized.opt("like_counts")),
            emoticons = extractEmoticonsFromHtml(htmlText),
            images = images,
        )
    }

    private fun parseMweiboRepostText(
        repost: JSONObject,
        htmlText: String,
        images: List<FeedImage>,
    ): String {
        val rawText = repost.optNullableString("raw_text")?.trim().orEmpty()
        if (rawText.isNotBlank()) {
            var text = rawText
            text = stripEntityTokens(
                text,
                imageUrlTokensFromUrlStruct(repost.optJSONArray("url_struct"), rawText),
            )
            if (images.isNotEmpty()) {
                text = stripOrphanMediaLinks(text)
            }
            return text
        }
        return parseCommentText(repost, images).ifBlank { plainText(htmlText) }
    }

    private fun parseCommentText(comment: JSONObject, images: List<FeedImage>): String {
        val raw = comment.optNullableString("text_raw")
            ?: comment.optNullableString("text")
            ?: ""
        var text = plainText(raw)
        text = stripEntityTokens(
            text,
            imageUrlTokensFromUrlStruct(comment.optJSONArray("url_struct"), raw),
        )
        if (images.isNotEmpty()) {
            text = stripOrphanMediaLinks(text)
        }
        return text
    }

    private fun parseStatusText(
        status: JSONObject,
        sourceRaw: String,
        images: List<FeedImage>,
        medias: List<FeedMedia> = emptyList(),
        inlineImageLinks: Map<String, List<FeedImage>> = emptyMap(),
        urlEntities: List<FeedUrlEntity> = emptyList(),
        forceStripMediaUrlTokens: Boolean = false,
    ): String {
        val preservedUrls = inlineImageLinks.keys + urlEntities.map { it.shortUrl }.toSet()
        var text = plainText(sourceRaw)
        val urlStruct = status.optJSONArray("url_struct")
        val imageTokens = imageUrlTokensFromUrlStruct(urlStruct, sourceRaw)
            .filter { it !in preservedUrls }
        text = stripEntityTokens(text, imageTokens)
        val shouldStripMediaTokens = medias.isNotEmpty() || forceStripMediaUrlTokens
        if (shouldStripMediaTokens) {
            val mediaTokens = mediaUrlTokensFromUrlStruct(urlStruct, sourceRaw, preservedUrls) +
                mediaPageInfoTokens(status, sourceRaw)
            text = stripEntityTokens(text, mediaTokens)
        }
        if ((images.isNotEmpty() || shouldStripMediaTokens) && inlineImageLinks.isEmpty() && urlEntities.isEmpty()) {
            text = stripOrphanMediaLinks(text)
        } else if (urlEntities.isNotEmpty() || inlineImageLinks.isNotEmpty()) {
            text = stripOrphanMediaLinksExcept(text, preservedUrls)
        }
        return text
    }

    private fun parseUrlEntities(
        status: JSONObject,
        sourceText: String,
        inlineImageUrls: Set<String> = emptySet(),
        suppressAttachedMediaLinks: Boolean = false,
    ): List<FeedUrlEntity> {
        val urlStruct = status.optJSONArray("url_struct") ?: return emptyList()
        return buildList {
            for (index in 0 until urlStruct.length()) {
                val entity = urlStruct.optJSONObject(index) ?: continue
                val shortUrl = entity.optNullableString("short_url") ?: continue
                if (shortUrl in inlineImageUrls || !sourceText.contains(shortUrl)) continue
                val picIds = entity.optJSONArray("pic_ids")
                if (picIds != null && picIds.length() > 0 && entity.optJSONObject("pic_infos") != null) {
                    continue
                }
                if (suppressAttachedMediaLinks && isAttachedMediaUrlStructEntity(entity)) {
                    continue
                }
                if (!entity.has("url_type") || entity.isNull("url_type")) continue
                if (entity.optInt("url_type") == 1) continue
                val title = entity.optNullableString("url_title") ?: continue
                val targetUrl = entity.optNullableString("h5_target_url")
                    ?: entity.optNullableString("long_url")
                    ?: entity.optNullableString("ori_url")
                    ?: shortUrl
                add(
                    FeedUrlEntity(
                        shortUrl = shortUrl,
                        title = title,
                        url = targetUrl,
                    ),
                )
            }
        }.distinctBy { it.shortUrl }
            .let(::dedupeAttachedMediaUrlEntities)
    }

    /** 同一「xxx的微博视频」常对应多条不同短链，正文只保留一条。 */
    private fun dedupeAttachedMediaUrlEntities(entities: List<FeedUrlEntity>): List<FeedUrlEntity> {
        val seenVideoTitles = mutableSetOf<String>()
        return entities.filter { entity ->
            if (!isAttachedMediaFeedUrlEntity(entity)) return@filter true
            seenVideoTitles.add(entity.title.trim())
        }
    }

    /** 已有内嵌视频/直播卡片时，正文不应再把对应媒体短链渲染成普通链接。 */
    private fun isVideoProgressTimestampTitle(title: String): Boolean =
        title.trim().matches(Regex("""^\d{1,2}:\d{2}(?::\d{2})?$"""))

    private fun isAttachedMediaUrlStructEntity(entity: JSONObject): Boolean {
        val title = entity.optNullableString("url_title")
            ?: entity.optNullableString("title")
            ?: ""
        // 「10:29」这类进度锚点也常带 object_type=video / video.weibo.com，但不能当附属卡片剥掉。
        if (isVideoProgressTimestampTitle(title)) return false
        val objectType = entity.optNullableString("object_type")?.lowercase().orEmpty()
        if (objectType in setOf("video", "live", "story", "movie", "audio", "gif")) {
            return true
        }
        if (title.contains("微博视频") ||
            title.contains("微博直播") ||
            title.contains("微博故事") ||
            title.contains("的视频")
        ) {
            return true
        }
        val candidateUrls = listOfNotNull(
            entity.optNullableString("h5_target_url"),
            entity.optNullableString("long_url"),
            entity.optNullableString("ori_url"),
            entity.optNullableString("short_url"),
        )
        return candidateUrls.any { url ->
            val lower = url.lowercase()
            lower.contains("video.weibo.com") ||
                lower.contains("weibo.com/tv") ||
                lower.contains("media.weibo.cn") ||
                lower.contains("/tv/show/") ||
                lower.contains("livestream")
        }
    }

    private fun isAttachedMediaFeedUrlEntity(entity: FeedUrlEntity): Boolean {
        if (isVideoProgressTimestampTitle(entity.title)) return false
        val title = entity.title
        if (title.contains("微博视频") ||
            title.contains("微博直播") ||
            title.contains("微博故事") ||
            title.contains("的视频")
        ) {
            return true
        }
        val lower = "${entity.url} ${entity.shortUrl}".lowercase()
        return lower.contains("video.weibo.com") ||
            lower.contains("weibo.com/tv") ||
            lower.contains("media.weibo.cn") ||
            lower.contains("/tv/show/") ||
            lower.contains("livestream")
    }

    /** 转发外层剥链：收集原博/本层视频短链与标题，避免 //@ 末尾残留「xxx的微博视频」。 */
    private fun collectWeiboHostedVideoLinkTokens(status: JSONObject?): List<String> {
        if (status == null) return emptyList()
        val tokens = linkedSetOf<String>()
        val urlStruct = status.optJSONArray("url_struct")
        for (index in 0 until (urlStruct?.length() ?: 0)) {
            val entity = urlStruct?.optJSONObject(index) ?: continue
            if (!isAttachedMediaUrlStructEntity(entity)) continue
            listOfNotNull(
                entity.optNullableString("short_url"),
                entity.optNullableString("long_url"),
                entity.optNullableString("ori_url"),
                entity.optNullableString("h5_target_url"),
                entity.optNullableString("url_title"),
                entity.optNullableString("title"),
            ).forEach { token ->
                if (token.isNotBlank()) tokens.add(token)
            }
        }
        status.optJSONObject("page_info")?.let { pageInfo ->
            val type = pageInfo.optNullableString("type")?.lowercase().orEmpty()
            val objectType = pageInfo.optNullableString("object_type")?.lowercase().orEmpty()
            val title = pageInfo.optNullableString("page_title")
                ?: pageInfo.optNullableString("title")
                ?: ""
            val looksVideo = type in setOf("video", "live", "story") ||
                objectType in setOf("video", "live", "story") ||
                title.contains("微博视频") ||
                title.contains("微博直播") ||
                title.contains("微博故事")
            if (looksVideo) {
                listOfNotNull(
                    pageInfo.optNullableString("page_url"),
                    pageInfo.optNullableString("url_ori"),
                    pageInfo.optJSONObject("media_info")?.optNullableString("h5_url"),
                    title.takeIf { it.isNotBlank() },
                ).forEach { token ->
                    if (token.isNotBlank()) tokens.add(token)
                }
            }
        }
        return tokens.toList()
    }

    private fun stripWeiboHostedVideoTitles(text: String): String {
        if (text.isBlank()) return text
        return text
            .replace(Regex("""L?\S{1,40}的微博视频"""), "")
            .replace(Regex("""L?\S{1,40}的微博直播"""), "")
            .replace(Regex("""L?\S{1,40}的微博故事"""), "")
            .replace(Regex("[ \\t]{2,}"), " ")
            .replace(Regex("[ \\t]+\\n"), "\n")
            .replace(Regex("\\n[ \\t]+"), "\n")
            .trim()
    }

    private fun stripOrphanMediaLinksExcept(text: String, preserveUrls: Set<String>): String {
        var result = text
        Regex("""https?://t\.cn/\S+""").findAll(text).forEach { match ->
            val token = match.value
            if (token !in preserveUrls) {
                result = result.replace(token, "")
            }
        }
        return result
            .replace(Regex("[ \\t]{2,}"), " ")
            .replace(Regex("[ \\t]+\\n"), "\n")
            .replace(Regex("\\n[ \\t]+"), "\n")
            .trim()
    }

    private fun parseInlineImageLinks(
        status: JSONObject,
        sourceText: String,
    ): Map<String, List<FeedImage>> {
        val urlStruct = status.optJSONArray("url_struct") ?: return emptyMap()
        val links = linkedMapOf<String, List<FeedImage>>()
        for (index in 0 until urlStruct.length()) {
            val entity = urlStruct.optJSONObject(index) ?: continue
            val shortUrl = entity.optNullableString("short_url") ?: continue
            if (!sourceText.contains(shortUrl)) continue
            val pics = imagesFromParts(entity.optJSONArray("pic_ids"), entity.optJSONObject("pic_infos"))
            if (pics.isNotEmpty()) links[shortUrl] = pics
        }
        return links
    }

    private fun imageUrlTokensFromUrlStruct(urlStruct: JSONArray?, sourceText: String): List<String> {
        if (urlStruct == null) return emptyList()
        return buildList {
            for (index in 0 until urlStruct.length()) {
                val entity = urlStruct.optJSONObject(index) ?: continue
                val picIds = entity.optJSONArray("pic_ids") ?: continue
                if (picIds.length() == 0) continue
                if (entity.optJSONObject("pic_infos") == null) continue
                val shortUrl = entity.optNullableString("short_url") ?: continue
                if (sourceText.contains(shortUrl)) add(shortUrl)
            }
        }
    }

    private fun mediaUrlTokensFromUrlStruct(
        urlStruct: JSONArray?,
        sourceText: String,
        excludeShortUrls: Set<String> = emptySet(),
    ): List<String> {
        if (urlStruct == null) return emptyList()
        val tokens = linkedSetOf<String>()
        for (index in 0 until urlStruct.length()) {
            val entity = urlStruct.optJSONObject(index) ?: continue
            val picIds = entity.optJSONArray("pic_ids")
            if (picIds != null && picIds.length() > 0 && entity.optJSONObject("pic_infos") != null) {
                continue
            }
            val title = entity.optNullableString("url_title")
                ?: entity.optNullableString("title")
                ?: ""
            if (isVideoProgressTimestampTitle(title)) continue
            // 仅剥附属视频卡片类链接；进度锚点等已由 exclude / 上文跳过。
            if (!isAttachedMediaUrlStructEntity(entity)) continue
            for (key in listOf("short_url", "long_url", "ori_url", "h5_target_url")) {
                entity.optNullableString(key)
                    ?.takeIf { it.isNotBlank() && it !in excludeShortUrls && sourceText.contains(it) }
                    ?.let { tokens.add(it) }
            }
        }
        return tokens.toList()
    }

    private fun mediaPageInfoTokens(status: JSONObject, sourceText: String): List<String> {
        val pageInfo = status.optJSONObject("page_info") ?: return emptyList()
        return listOfNotNull(
            pageInfo.optNullableString("page_url"),
            pageInfo.optNullableString("url_ori"),
            pageInfo.optJSONObject("media_info")?.optNullableString("h5_url"),
        ).filter { it.isNotBlank() && sourceText.contains(it) }
    }

    private fun stripOrphanMediaLinks(text: String): String =
        text
            .replace(Regex("""https?://t\.cn/\S+"""), "")
            .replace(Regex("""(?<!\S)https?:(?=\s|$)"""), "")
            .replace(Regex("""(?:[ \t]*https?://\S+)+[ \t]*$"""), "")
            .replace(Regex("[ \\t]{2,}"), " ")
            .replace(Regex("[ \\t]+\\n"), "\n")
            .replace(Regex("\\n[ \\t]+"), "\n")
            .trim()

    private fun stripEntityTokens(text: String, tokens: List<String>): String {
        if (tokens.isEmpty()) return text.trim()
        var result = text
        for (token in tokens) {
            result = result.replace(token, "")
            result = result.replace(Regex("[ \\t]{2,}"), " ")
        }
        return result
            .replace(Regex("[ \\t]+\\n"), "\n")
            .replace(Regex("\\n[ \\t]+"), "\n")
            .trim()
    }

    private fun parseCommentImages(comment: JSONObject): List<FeedImage> =
        parseImages(comment)

    private fun parseNestedComments(arr: org.json.JSONArray?): List<CommentItem> {
        if (arr == null) return emptyList()
        return buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val user = obj.optJSONObject("user")
                val id = obj.optNullableString("idstr") ?: obj.optNullableString("id") ?: continue
                val htmlText = obj.optNullableString("text") ?: ""
                val images = parseCommentImages(obj)
                add(
                    CommentItem(
                        id = id,
                        authorId = userId(user),
                        authorName = parseUserDisplayName(user),
                        authorAvatarUrl = user?.optNullableString("avatar_hd")
                            ?: user?.optNullableString("profile_image_url"),
                        text = parseCommentText(obj, images),
                        createdAt = obj.optNullableString("created_at"),
                        likesCount = formatCount(obj.opt("like_counts")),
                        ipLocation = parseIpLocation(obj),
                        emoticons = extractEmoticonsFromHtml(htmlText),
                        images = images,
                        replyToAuthor = obj.optJSONObject("reply_comment")
                            ?.optJSONObject("user")
                            ?.let { parseUserDisplayName(it) },
                        replyToAuthorId = userId(obj.optJSONObject("reply_comment")?.optJSONObject("user"))
                            .takeIf { it.isNotBlank() },
                        comments = parseNestedComments(obj.optJSONArray("comments")),
                        moreInfoText = obj.optJSONObject("more_info")?.optNullableString("text"),
                    )
                )
            }
        }.sortedBy { parseWeiboCreatedAtMillis(it.createdAt) ?: Long.MAX_VALUE }
    }

    fun hasLongTextPayload(data: JSONObject): Boolean =
        listOf("longTextContent", "longTextContent_raw", "text", "text_raw", "raw_text")
            .any { data.optNullableString(it)?.isNotBlank() == true }

    fun hasMeaningfulLongTextExpansion(preview: FeedItem, expanded: FeedItem): Boolean {
        val hasNewImages = expanded.images.any { candidate ->
            preview.images.none { it.largeUrl == candidate.largeUrl }
        }
        if (hasNewImages) return true
        return !isClientSupplementOnlyLongText(preview.text, expanded.text)
    }

    fun mergeLongTextIntoFeedItem(item: FeedItem, data: JSONObject): FeedItem {
        val contentHtml = data.optNullableString("longTextContent")
            ?: data.optNullableString("text")
            ?: ""
        val contentRaw = data.optNullableString("longTextContent_raw")
            ?: data.optNullableString("text_raw")
            ?: data.optNullableString("raw_text")
            ?: contentHtml
        if (!hasLongTextPayload(data)) return item

        val sourceForParse = buildStatusDisplayText(
            rawText = contentRaw.takeIf { it.isNotBlank() } ?: contentHtml,
            htmlText = contentHtml,
            isLongTextPreview = false,
        )
        val inlineImageLinks = parseInlineImageLinks(data, sourceForParse)
        val suppressAttachedMediaLinks = item.medias.isNotEmpty() ||
            item.retweetedStatus?.medias?.isNotEmpty() == true
        val urlEntities = parseUrlEntities(
            status = data,
            sourceText = sourceForParse,
            inlineImageUrls = inlineImageLinks.keys,
            suppressAttachedMediaLinks = suppressAttachedMediaLinks,
        )
        val additionalImages = parseImages(data, inlineImageLinks.keys).map {
            it.copy(createdAt = it.createdAt ?: item.createdAt)
        }
        val mergedImages = (item.images + additionalImages).distinctBy { it.largeUrl }
        val mergedInlineImageLinks = item.inlineImageLinks + inlineImageLinks
        val existingUrlEntities = if (suppressAttachedMediaLinks) {
            item.urlEntities.filterNot(::isAttachedMediaFeedUrlEntity)
        } else {
            item.urlEntities
        }
        val mergedUrlEntities = (existingUrlEntities + urlEntities).distinctBy { it.shortUrl }
        // 长文合并只依据接口返回的正文与媒体，不复用卡片上的 video 等媒体字段，避免转发场景误删正文。
        var text = parseStatusText(
            data,
            sourceForParse,
            mergedImages,
            medias = if (suppressAttachedMediaLinks) item.medias else emptyList(),
            mergedInlineImageLinks,
            mergedUrlEntities,
        )
        if (text.isBlank()) {
            text = plainText(sourceForParse)
        }
        if (text.isBlank()) {
            text = sourceForParse.trim()
        }
        // `isLongText` 偶尔只代表微博客户端会追加「大家都在搜」「超话」等
        // 导流模块。它们不是博主正文；若长文接口没有带来实际的新正文，直接收起
        // 「阅读全文」，也不要把这些模块混入微博内容。
        if (isClientSupplementOnlyLongText(item.text, text)) {
            return item.copy(isLongText = false, requiresLongTextFetch = false)
        }
        val emoticonSources = buildList {
            contentHtml.takeIf { it.isNotBlank() }?.let(::add)
            contentRaw.takeIf { it.isNotBlank() }?.let(::add)
            collectRetweetedStatusTexts(data).forEach(::add)
        }
        val mergedEmoticons = item.collectEmoticons() + emoticonSources
            .flatMap { extractEmoticonsFromHtml(it).entries }
            .associate { it.key to it.value }
        val hasMoreImages = mergedImages.size > item.images.size
        if (text.isBlank()) {
            if (!hasMoreImages) return item
            return item.copy(
                isLongText = false,
                requiresLongTextFetch = false,
                emoticons = mergedEmoticons,
                images = mergedImages,
                inlineImageLinks = mergedInlineImageLinks,
                urlEntities = mergedUrlEntities,
            )
        }
        return item.copy(
            isLongText = false,
            requiresLongTextFetch = false,
            text = text,
            emoticons = mergedEmoticons,
            images = mergedImages,
            inlineImageLinks = mergedInlineImageLinks,
            urlEntities = mergedUrlEntities,
        )
    }

    fun parseUserProfile(raw: String, fallback: JSONObject? = null): UserProfile {
        val root = JSONObject(raw)
        val data = root.optJSONObject("data") ?: root
        val user = data.optJSONObject("user")
            ?: data.optJSONObject("userInfo")
            ?: data
        return UserProfile(
            id = user.optNullableString("idstr")
                ?: user.optNullableString("id")
                ?: fallback?.optNullableString("uid")
                ?: "",
            screenName = user.optNullableString("screen_name")
                ?: user.optNullableString("name")
                ?: fallback?.optNullableString("screen_name")
                ?: "\u5FAE\u535A\u7528\u6237",
            avatarUrl = user.optNullableString("avatar_hd")
                ?: user.optNullableString("avatar_large")
                ?: user.optNullableString("profile_image_url")
                ?: fallback?.optNullableString("avatar"),
            description = user.optNullableString("description"),
            location = user.optNullableString("location"),
            ipLocation = normalizeUserIpLocation(
                user.optNullableString("ip_location")
                    ?: user.optNullableString("region_name")
                    ?: data.optNullableString("ip_location"),
            ),
            verifiedReason = user.optNullableString("verified_reason"),
            followingCount = formatCount(
                user.opt("friends_count")
                    ?: user.opt("follow_count")
                    ?: user.opt("following_count")
            ),
            followersCount = formatCount(user.opt("followers_count")),
            statusesCount = formatCount(user.opt("statuses_count")),
            photosCount = user.opt("photos_count")?.let(::formatCount),
            coverUrls = parseProfileCoverUrls(user),
            following = user.optTruthy("following"),
            followMe = user.optTruthy("follow_me"),
        )
    }

    fun mergeProfileDetail(profile: UserProfile, raw: String): UserProfile {
        val data = JSONObject(raw).optJSONObject("data") ?: return profile
        val ipLocation = normalizeUserIpLocation(data.optNullableString("ip_location")) ?: profile.ipLocation
        return profile.copy(ipLocation = ipLocation)
    }

    private fun normalizeUserIpLocation(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return raw.trim()
            .removePrefix("IP属地：")
            .removePrefix("IP属地:")
            .removePrefix("来自")
            .removePrefix("发布于")
            .trim()
            .takeIf { it.isNotBlank() }
    }

    fun mergeFollowMutationProfile(
        raw: String,
        existing: UserProfile,
        expectedFollowing: Boolean,
    ): UserProfile {
        val root = JSONObject(raw)
        if (root.optInt("ok", 0) != 1 && !root.optBoolean("result", false)) {
            throw IllegalStateException(root.optNullableString("msg") ?: "\u5173\u6CE8\u64CD\u4F5C\u5931\u8D25")
        }
        val parsed = runCatching { parseUserProfile(raw) }.getOrNull()
        return if (parsed != null && parsed.id.isNotBlank()) {
            existing.copy(
                following = expectedFollowing,
                followMe = parsed.followMe,
            )
        } else {
            existing.copy(following = expectedFollowing)
        }
    }

    fun assertMutationSuccess(raw: String, defaultError: String) {
        val root = JSONObject(raw)
        if (root.optInt("ok", 0) != 1 && !root.optBoolean("result", false)) {
            throw IllegalStateException(root.optNullableString("msg") ?: defaultError)
        }
    }

    fun parseFriendsPage(raw: String): RelationPage {
        val root = JSONObject(raw)
        if (root.optInt("ok", 1) <= 0) {
            return RelationPage(
                items = emptyList(),
                hasNextPage = false,
                errorMsg = root.optNullableString("msg") ?: "加载失败",
            )
        }
        val users = root.optJSONArray("users") ?: JSONArray()
        val items = buildList {
            for (index in 0 until users.length()) {
                val user = users.optJSONObject(index) ?: continue
                val id = user.optNullableString("idstr")
                    ?: user.optNullableString("id")
                    ?: continue
                add(
                    RelationUser(
                        id = id,
                        name = user.optNullableString("name")
                            ?: user.optNullableString("screen_name")
                            ?: "微博用户",
                        screenName = user.optNullableString("screen_name") ?: id,
                        avatarUrl = user.optNullableString("profile_image_url"),
                        avatarLarge = user.optNullableString("avatar_large"),
                        description = user.optNullableString("description"),
                        followersCount = formatCount(user.opt("followers_count")),
                        followersCountStr = user.optNullableString("followers_count_str"),
                        friendsCount = formatCount(user.opt("friends_count")),
                        following = user.optBoolean("following"),
                        followMe = user.optBoolean("follow_me"),
                        verified = user.optBoolean("verified"),
                        verifiedReason = user.optNullableString("verified_reason"),
                        location = user.optNullableString("location"),
                    ),
                )
            }
        }
        val nextCursor = root.opt("next_cursor")?.toString()?.toIntOrNull() ?: 0
        return RelationPage(
            items = items,
            hasNextPage = nextCursor > 0,
            totalNumber = root.optInt("total_number", items.size),
        )
    }

    fun formatDisplayCount(count: Number): String = formatCount(count)

    fun bumpDisplayCount(value: String, delta: Int): String {
        if (delta == 0) return value
        val trimmed = value.trim()
        val wanMatch = Regex("""^([\d.]+)万$""").find(trimmed)
        val count = when {
            wanMatch != null -> {
                val num = wanMatch.groupValues[1].toDoubleOrNull() ?: return value
                (num * 10_000).toLong()
            }
            else -> trimmed.toLongOrNull() ?: return value
        }
        return formatCount((count + delta).coerceAtLeast(0))
    }

    fun profileCoverImages(urls: List<String>): List<FeedImage> =
        urls.map { url ->
            val downloadUrls = profileCoverDownloadUrls(url)
            FeedImage(
                id = url,
                thumbnailUrl = downloadUrls.first(),
                largeUrl = downloadUrls.first(),
                downloadUrls = downloadUrls,
            )
        }

    private fun parseProfileCoverUrls(user: JSONObject): List<String> {
        val rawValues = listOfNotNull(
            user.optNullableString("cover_image_phone"),
            user.optNullableString("cover_image"),
        )
        return rawValues
            .flatMap { raw -> raw.split(';') }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map(::normalizeProfileCoverUrl)
            .distinct()
    }

    private fun normalizeProfileCoverUrl(url: String): String {
        var normalized = url.trim()
        if (normalized.startsWith("//")) normalized = "https:$normalized"
        if (normalized.startsWith("http://")) {
            normalized = normalized.replaceFirst("http://", "https://")
        }
        normalized = normalized.substringBefore('?')
        normalized = normalized.replace(Regex("""crop\.\d+\.\d+\.\d+\.\d+"""), "mw2000")
        return normalized
    }

    private fun profileCoverDownloadUrls(url: String): List<String> {
        val normalized = normalizeProfileCoverUrl(url)
        val match = Regex("""(https?://[^/]+)/([^/]+)/(.+)""").find(normalized)
        if (match == null) return listOf(normalized)
        val host = match.groupValues[1]
        val path = match.groupValues[3]
        return listOf("mw2000", "woriginal", "large", "orj960", "bmiddle")
            .map { size -> "$host/$size/$path" }
            .distinct()
            .filter(::looksLikeImageUrl)
    }

    data class ImageWallParseResult(
        val images: List<FeedImage>,
        val nextSinceId: String?,
        val nextCursor: String? = null,
    )

    class AlbumMonthContext(
        var month: String? = null,
        var year: String? = null,
    )

    fun parseImageWallPage(
        raw: String,
        monthContext: AlbumMonthContext = AlbumMonthContext(),
    ): ImageWallParseResult {
        val root = runCatching { JSONObject(raw) }.getOrNull()
            ?: return ImageWallParseResult(emptyList(), null)
        when (val ok = root.optInt("ok", 0)) {
            1 -> Unit
            -100 -> throw IllegalStateException("\u76F8\u518C\u9700\u8981\u767B\u5F55\u540E\u67E5\u770B")
            else -> {
                val hint = root.optNullableString("msg")
                    ?: root.opt("url")?.toString()?.takeIf { it.isNotBlank() }
                    ?: "ok=$ok"
                throw IllegalStateException("\u76F8\u518C\u63A5\u53E3\u9519\u8BEF: $hint")
            }
        }
        val data = root.optJSONObject("data")
            ?: return ImageWallParseResult(emptyList(), null)
        val responseSinceId = data.optSinceId()
            ?: data.optNullableString("sinceid")
            ?: data.optNullableString("next_since_id")
        val responseCursor = data.optAlbumCursor()
        val images = (parseAlbumMediaList(data.optJSONArray("list"), monthContext) +
            parseAlbumMediaList(data.optJSONArray("photo_wall"), monthContext))
            .distinctBy { "${it.type.orEmpty()}:${it.id}:${it.largeUrl}" }
        return ImageWallParseResult(
            images = images,
            nextSinceId = responseSinceId,
            nextCursor = responseCursor,
        )
    }

    private fun JSONObject.optAlbumCursor(): String? {
        if (!has("next_cursor") || isNull("next_cursor")) return null
        return when (val value = opt("next_cursor")) {
            is Number -> {
                val cursor = value.toLong()
                if (cursor < 0L) null else cursor.toString()
            }
            else -> value?.toString()?.trim()?.takeIf { it.isNotBlank() && it != "-1" && it != "0" }
        }
    }

    private fun parseAlbumMediaList(
        list: JSONArray?,
        monthContext: AlbumMonthContext = AlbumMonthContext(),
    ): List<FeedImage> {
        if (list == null) return emptyList()
        return buildList {
            for (index in 0 until list.length()) {
                parseAlbumMediaItem(
                    item = list.optJSONObject(index),
                    monthContext = monthContext,
                )?.let(::add)
            }
        }
    }

    private fun parseAlbumMediaItem(
        item: JSONObject?,
        monthContext: AlbumMonthContext = AlbumMonthContext(),
    ): FeedImage? {
        item ?: return null
        val payload = item.optJSONObject("video_detail_vo") ?: item
        if (isAlbumVideoItem(payload)) {
            parseAlbumVideoItem(payload, monthContext)?.let { return it }
        }
        return parseAlbumPhotoItem(payload, monthContext)
    }

    private fun isAlbumVideoItem(item: JSONObject): Boolean {
        val declaredType = item.optNullableString("type")
            ?: item.optNullableString("pic_type")
            ?: item.optNullableString("object_type")
        if (declaredType.equals("video", ignoreCase = true) ||
            declaredType.equals("videos", ignoreCase = true)
        ) {
            return true
        }
        val pageInfo = item.optJSONObject("page_info") ?: return false
        val objectType = pageInfo.optNullableString("object_type")
        if (objectType.equals("live", ignoreCase = true) ||
            objectType.equals("audio", ignoreCase = true)
        ) {
            return false
        }
        return pageInfo.optJSONObject("media_info") != null
    }

    private fun parseAlbumVideoItem(
        item: JSONObject,
        monthContext: AlbumMonthContext = AlbumMonthContext(),
    ): FeedImage? {
        item.optBlankString("timeline_month")?.let { monthContext.month = it.trim() }
        item.optBlankString("timeline_year")?.let { monthContext.year = it.trim() }

        val media = parseMedia(item) ?: return null
        if (!media.isStreamPlayable()) return null
        val cover = media.coverUrl?.let(::normalizeUrl)
            ?: item.optNullableString("pic")?.let(::normalizeUrl)
            ?: item.optNullableString("cover")?.let(::normalizeUrl)
            ?: item.optNullableString("page_pic")?.let(::normalizeUrl)
            ?: return null
        if (!looksLikeAlbumCoverUrl(cover)) return null

        val statusId = statusId(item).takeIf { it.isNotBlank() }
            ?: item.optBlankString("mid")
        val createdAt = item.optNullableString("created_at")
            ?: formatAlbumMid(item.optBlankString("mid"))
            ?: formatAlbumTimestamp(item.optNullableString("time"))
            ?: formatAlbumTimestamp(item.optNullableString("upload_time"))
            ?: formatAlbumTimestamp(item.optNullableString("shoot_time"))
            ?: item.optNullableString("date")
            ?: extractAlbumDateFromId(item.optBlankString("object_id"))
            ?: extractAlbumDateFromId(item.optNullableString("id"))
            ?: formatAlbumTimeline(
                item.optBlankString("timeline_year") ?: monthContext.year,
                item.optBlankString("timeline_month") ?: monthContext.month,
            )
        createdAt?.let { applyAlbumDateToContext(it, monthContext) }

        return FeedImage(
            id = statusId?.ifBlank { cover } ?: cover,
            thumbnailUrl = cover,
            largeUrl = cover,
            downloadUrls = listOfNotNull(
                cover,
                media.downloadUrl?.let(::normalizeMediaUrl),
            ).distinct(),
            videoStreamUrl = media.streamUrl,
            createdAt = createdAt,
            statusId = statusId,
            type = "video",
        )
    }

    private fun looksLikeAlbumCoverUrl(value: String): Boolean =
        looksLikeImageUrl(value) || value.contains("sinaimg.cn", ignoreCase = true)

    private fun parseAlbumPhotoList(
        list: JSONArray?,
        monthContext: AlbumMonthContext = AlbumMonthContext(),
    ): List<FeedImage> = parseAlbumMediaList(list, monthContext)

    private fun parseAlbumPhotoItem(
        item: JSONObject?,
        monthContext: AlbumMonthContext = AlbumMonthContext(),
    ): FeedImage? {
        item ?: return null
        item.optBlankString("timeline_month")?.let { monthContext.month = it.trim() }
        item.optBlankString("timeline_year")?.let { monthContext.year = it.trim() }

        val pid = item.optNullableString("pid")
            ?: item.optNullableString("pic_id")
        val thumbnail = item.optNullableString("pic")
            ?: item.optNullableString("thumbnail")
            ?: item.optNullableString("bmiddle")
            ?: item.optNullableString("orj360")
            ?: pid?.let(::pidToLargeUrl)
            ?: return null
        val large = item.optNullableString("largest")
            ?: item.optNullableString("original")
            ?: item.optNullableString("large")
            ?: item.optNullableString("mw2000")
            ?: upgradeSinaimgUrl(thumbnail)
            ?: pid?.let(::pidToLargeUrl)
            ?: return null
        if (!looksLikeImageUrl(large)) {
            return parseAlbumVideoItem(item, monthContext)
        }
        val timelineMonth = item.optBlankString("timeline_month")
        val timelineYear = item.optBlankString("timeline_year")
        val createdAt = item.optNullableString("created_at")
            ?: item.optNullableString("created")
            ?: formatAlbumMid(item.optBlankString("mid"))
            ?: formatAlbumTimeline(timelineYear ?: monthContext.year, timelineMonth ?: monthContext.month)
            ?: formatAlbumTimestamp(item.optNullableString("time"))
            ?: formatAlbumTimestamp(item.optNullableString("upload_time"))
            ?: formatAlbumTimestamp(item.optNullableString("shoot_time"))
            ?: item.optNullableString("date")
            ?: extractAlbumDateFromId(item.optBlankString("object_id"))
            ?: extractAlbumDateFromId(item.optNullableString("id"))
            ?: formatAlbumTimeline(monthContext.year, monthContext.month)
        createdAt?.let { applyAlbumDateToContext(it, monthContext) }
        val normalizedLarge = normalizeUrl(large)
        val normalizedThumb = normalizeUrl(thumbnail)
        val picInfo = listOfNotNull(
            item.optJSONObject("pic_info"),
            item.optJSONObject("picInfo"),
            item.optJSONObject("pic_infos")?.let { infos ->
                pid?.let { infos.optJSONObject(it) }
                    ?: item.optNullableString("pic_id")?.let { infos.optJSONObject(it) }
            },
        ).firstOrNull()
        val imgType = picInfo?.optNullableString("type")
            ?: item.optNullableString("type")
            ?: item.optNullableString("pic_type")
            ?: item.optNullableString("object_type")
        val livePhotoVideoUrl = listOfNotNull(
            picInfo?.optNullableString("video"),
            picInfo?.optNullableString("video_hd"),
            picInfo?.optNullableString("live_photo_video_url"),
            item.optNullableString("video"),
            item.optNullableString("video_hd"),
            item.optNullableString("live_photo_video_url"),
            item.optNullableString("livephoto_video_url"),
            item.optJSONObject("live_photo")?.optNullableString("video"),
            item.optJSONObject("live_photo")?.optNullableString("video_hd"),
        ).firstOrNull { it.isNotBlank() }?.let(::normalizeUrl)
        val largeInfo = picInfo?.optJSONObject("largest")
            ?: picInfo?.optJSONObject("mw2000")
            ?: picInfo?.optJSONObject("woriginal")
            ?: picInfo?.optJSONObject("original")
            ?: picInfo?.optJSONObject("large")
        val captureMeta = parseImageCaptureMetadata(picInfo ?: item)
        return FeedImage(
            id = pid?.ifBlank { normalizedLarge } ?: normalizedLarge,
            thumbnailUrl = normalizedThumb,
            largeUrl = normalizedLarge,
            downloadUrls = listOfNotNull(
                normalizedLarge,
                normalizedThumb,
                pid?.let(::pidToLargeUrl),
            ).distinct(),
            livePhotoVideoUrl = livePhotoVideoUrl,
            createdAt = createdAt,
            statusId = item.optBlankString("mid"),
            width = largeInfo?.optInt("width")?.takeIf { it > 0 }
                ?: item.optInt("width").takeIf { it > 0 },
            height = largeInfo?.optInt("height")?.takeIf { it > 0 }
                ?: item.optInt("height").takeIf { it > 0 },
            shootTime = captureMeta.shootTime ?: createdAt,
            cameraMake = captureMeta.cameraMake,
            cameraModel = captureMeta.cameraModel,
            type = when (imgType?.lowercase()) {
                "live", "live_photo", "livephoto" -> "livephoto"
                "gif" -> "gif"
                "video", "videos" -> "video"
                else -> when {
                    !livePhotoVideoUrl.isNullOrBlank() -> "livephoto"
                    else -> imgType
                }
            },
        )
    }

    private fun JSONObject.optBlankString(name: String): String? =
        optString(name).trim().takeIf { it.isNotBlank() }

    private fun applyAlbumDateToContext(date: String, context: AlbumMonthContext) {
        Regex("""((?:19|20)\d{2})[-/](\d{1,2})""").find(date)?.let { match ->
            context.year = match.groupValues[1]
            context.month = match.groupValues[2]
        }
    }

    private fun pidToLargeUrl(pid: String): String {
        val cleanPid = pid.substringBeforeLast('.')
        val ext = when {
            pid.contains(".gif", ignoreCase = true) -> "gif"
            else -> "jpg"
        }
        val host = when {
            cleanPid.startsWith("006") || cleanPid.startsWith("007") -> "wx2"
            cleanPid.startsWith("008") || cleanPid.startsWith("009") -> "wx3"
            else -> "wx${((cleanPid.hashCode() and Int.MAX_VALUE) % 4) + 1}"
        }
        return "https://$host.sinaimg.cn/large/$cleanPid.$ext"
    }

    private fun upgradeSinaimgUrl(url: String): String? {
        if (!looksLikeImageUrl(url)) return null
        val upgraded = url.replace(
            Regex("""/(orj360|wap360|bmiddle|thumb(?:180|300|150)?|small|thumbnail)/"""),
            "/large/",
        )
        return normalizeUrl(upgraded)
    }

    private fun formatAlbumTimeline(year: String?, month: String?): String? {
        if (year.isNullOrBlank() || month.isNullOrBlank()) return null
        val normalizedMonth = month.trim().padStart(2, '0')
        return "$year-$normalizedMonth-01"
    }

    private fun formatAlbumMid(mid: String?): String? {
        val id = mid?.trim()?.toLongOrNull() ?: return null
        val seconds = (id shr 22) + WEIBO_MID_EPOCH_OFFSET
        if (seconds < 1_000_000_000L || seconds > 4_102_444_800L) return null
        return ALBUM_DATE_FORMAT.format(Date(seconds * 1000))
    }

    private fun JSONObject.optSinceId(): String? {
        if (!has("since_id") || isNull("since_id")) return null
        return when (val value = opt("since_id")) {
            is Number -> if (value.toLong() == 0L) null else value.toString()
            else -> value?.toString()?.trim()?.takeIf { it.isNotBlank() && it != "0" }
        }
    }

    private fun formatAlbumTimestamp(value: String?): String? {
        val raw = value?.trim().orEmpty()
        if (raw.isEmpty()) return null
        if (!raw.all { it.isDigit() }) return raw
        val millis = raw.toLongOrNull() ?: return null
        if (millis < 1_000_000_000_000L) return null
        return ALBUM_DATE_FORMAT.format(Date(millis))
    }

    private fun extractAlbumDateFromId(id: String?): String? {
        val value = id.orEmpty()
        val patterns = listOf(
            Regex("""_(\d{8})_"""),
            Regex("""\|[^:]+:\d+_(\d{8})_-"""),
            Regex("""[_|](\d{8})_-"""),
        )
        for (pattern in patterns) {
            val ymd = pattern.find(value)?.groupValues?.getOrNull(1) ?: continue
            if (!ymd.startsWith("19") && !ymd.startsWith("20")) continue
            return "${ymd.take(4)}-${ymd.drop(4).take(2)}-${ymd.takeLast(2)}"
        }
        return null
    }

    private val ALBUM_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("Asia/Shanghai")
    }

    private const val WEIBO_MID_EPOCH_OFFSET = 515483463L

    private fun parseStatus(status: JSONObject, allowRetweeted: Boolean): FeedItem? {
        val id = status.optNullableString("idstr")
            ?: status.optNullableString("mid")
            ?: status.optNullableString("id")
            ?: return null
        val user = status.optJSONObject("user")
        val authorId = user?.optNullableString("idstr")
            ?: user?.optNullableString("id")
            ?: ""
        val retweetedJson = if (allowRetweeted) {
            status.optJSONObject("retweeted_status")?.takeUnless { it.looksLikeAd() }
        } else {
            null
        }
        val mediaBelongsToRetweeted =
            retweetedJson != null && shouldAttachMediaToRetweeted(status, retweetedJson)
        val medias = if (mediaBelongsToRetweeted) emptyList() else parseMedias(status)

        val htmlText = status.optNullableString("text") ?: ""
        val rawText = status.optNullableString("text_raw")
            ?: status.optNullableString("raw_text")
            ?: htmlText
        val isLongTextPreview = status.isLongTextPreview()
        val displayText = buildStatusDisplayText(
            rawText = rawText,
            htmlText = htmlText,
            isLongTextPreview = isLongTextPreview,
        )

        val retweeted = retweetedJson?.let { json ->
            val normalized = normalizeRetweetedStatus(status, json)
            parseStatus(normalized, allowRetweeted = false)
        }
        val resolvedMedias = when {
            mediaBelongsToRetweeted -> emptyList()
            retweeted?.medias?.isNotEmpty() == true && medias.isNotEmpty() -> emptyList()
            else -> medias
        }
        val retweetedHasVideo = mediaBelongsToRetweeted ||
            retweeted?.medias?.isNotEmpty() == true ||
            retweetedJson?.hasVideoOrLivePageInfo() == true ||
            collectWeiboHostedVideoLinkTokens(retweetedJson).isNotEmpty()
        val hasOwnVideo = resolvedMedias.isNotEmpty() || status.hasVideoOrLivePageInfo()
        val hasAttachedVideo = hasOwnVideo || retweetedHasVideo
        // “阅读全文”入口已停用。旧的 shouldShowLongTextExpand(...) 规则保留在下方
        // 作为历史实现，但不再参与 UI；长文只通过 requiresLongTextFetch 自动加载。
        // val isLongText = status.shouldShowLongTextExpand(
        //     rawText = rawText,
        //     displayText = displayText,
        //     hasAttachedVideo = hasAttachedVideo,
        // )
        val isLongText = false
        val inlineImageLinks = parseInlineImageLinks(status, displayText)
        val images = if (mediaBelongsToRetweeted) {
            emptyList()
        } else {
            parseImages(status, inlineImageLinks.keys).map {
                it.copy(createdAt = it.createdAt ?: status.optNullableString("created_at"))
            }
        }
        // 转发外层：去掉「xxx的微博视频」，只留原博引用区（至多一条）。
        // 独立视频帖：去掉短链，避免与视频卡片重复。
        val stripVideoLinksFromOuter = allowRetweeted && retweeted != null
        val suppressAttachedMediaLinks = when {
            stripVideoLinksFromOuter -> true
            allowRetweeted && hasOwnVideo -> true
            else -> false
        }
        var urlEntities = parseUrlEntities(
            status = status,
            sourceText = displayText,
            inlineImageUrls = inlineImageLinks.keys,
            suppressAttachedMediaLinks = suppressAttachedMediaLinks,
        )
        if (stripVideoLinksFromOuter) {
            urlEntities = urlEntities.filterNot(::isAttachedMediaFeedUrlEntity)
        }
        val textStripMedias = when {
            resolvedMedias.isNotEmpty() -> resolvedMedias
            stripVideoLinksFromOuter && retweeted?.medias?.isNotEmpty() == true -> retweeted.medias
            else -> emptyList()
        }
        var text = parseStatusText(
            status = status,
            sourceRaw = displayText,
            images = images,
            medias = textStripMedias,
            inlineImageLinks = inlineImageLinks,
            urlEntities = urlEntities,
            forceStripMediaUrlTokens = suppressAttachedMediaLinks,
        )
        if (stripVideoLinksFromOuter) {
            val extraMediaTokens = collectWeiboHostedVideoLinkTokens(status) +
                collectWeiboHostedVideoLinkTokens(retweetedJson)
            text = stripEntityTokens(text, extraMediaTokens)
            text = stripWeiboHostedVideoTitles(text)
        }

        val (isEdited, editCount) = status.parseEditMetadata()
        val statusLocation = parseStatusLocation(status)
        val item = FeedItem(
            id = id,
            statusId = status.optNullableString("mblogid") ?: id,
            authorId = authorId,
            authorName = user?.optNullableString("screen_name") ?: "微博用户",
            authorAvatarUrl = user?.optNullableString("avatar_hd")
                ?: user?.optNullableString("profile_image_url"),
            createdAt = status.optNullableString("created_at"),
            source = plainText(status.optNullableString("source") ?: ""),
            ipLocation = parseIpLocation(status),
            locationName = statusLocation?.name,
            locationUrl = statusLocation?.url,
            text = text,
            isLongText = isLongText,
            requiresLongTextFetch = status.hasLongTextFlag() ||
                hasTextTruncationSignals(rawText, displayText),
            emoticons = extractEmoticonsFromHtml(htmlText),
            repostsCount = formatCount(status.opt("reposts_count")),
            commentsCount = formatCount(status.opt("comments_count")),
            likesCount = formatCount(status.opt("attitudes_count")),
            liked = status.parseAttitudesStatus(),
            images = images,
            medias = resolvedMedias,
            inlineImageLinks = inlineImageLinks,
            urlEntities = urlEntities,
            retweetedStatus = retweeted,
            isEdited = isEdited,
            editCount = editCount,
        )
        val embeddedLongText = status.optJSONObject("longText")
            ?.takeIf { hasLongTextPayload(it) }
        val merged = embeddedLongText?.let { mergeLongTextIntoFeedItem(item, it) } ?: item
        // 只在最外层做展示净化；嵌套原博若在此处按独立视频帖处理会误删「应保留的一条」链接。
        return if (allowRetweeted) sanitizeRepostVideoLinks(merged) else merged
    }

    /**
     * 转发视频展示约定：
     * - 外层 //@：去掉全部「xxx的微博视频」链接
     * - 原博引用：同一标题只保留一条，并折叠正文里重复的 token
     * - 独立视频帖：去掉正文链接，只留视频卡片
     */
    fun sanitizeRepostVideoLinks(item: FeedItem): FeedItem {
        val cleanedRetweet = item.retweetedStatus?.let { retweeted ->
            clearMisparsedVideoLocation(keepAtMostOneWeiboVideoLink(retweeted))
        }
        val base = if (cleanedRetweet != null) {
            stripAllWeiboVideoLinks(item.copy(retweetedStatus = cleanedRetweet))
        } else {
            val hasOwnVideo = item.medias.any { media ->
                media.type == MediaType.Video || media.type == MediaType.Live
            }
            if (hasOwnVideo) {
                stripAllWeiboVideoLinks(item)
            } else {
                keepAtMostOneWeiboVideoLink(item)
            }
        }
        return clearMisparsedVideoLocation(base)
    }

    private fun clearMisparsedVideoLocation(item: FeedItem): FeedItem {
        val name = item.locationName ?: return item
        if (!looksLikeNonPlaceLocationTitle(name)) return item
        return item.copy(locationName = null, locationUrl = null)
    }

    private fun stripAllWeiboVideoLinks(item: FeedItem): FeedItem {
        val removed = item.urlEntities.filter(::isAttachedMediaFeedUrlEntity)
        if (removed.isEmpty() && !containsWeiboHostedVideoTitle(item.text)) {
            return item
        }
        val kept = item.urlEntities.filterNot(::isAttachedMediaFeedUrlEntity)
        var text = stripEntityTokens(
            item.text,
            removed.flatMap { entity ->
                listOfNotNull(entity.shortUrl, entity.title, "L${entity.title}")
            },
        )
        text = stripWeiboHostedVideoTitles(text)
        return item.copy(text = text, urlEntities = kept)
    }

    private fun keepAtMostOneWeiboVideoLink(item: FeedItem): FeedItem {
        val videoLinks = item.urlEntities.filter(::isAttachedMediaFeedUrlEntity)
        val otherLinks = item.urlEntities.filterNot(::isAttachedMediaFeedUrlEntity)
        if (videoLinks.isEmpty()) {
            return item
        }
        val keep = videoLinks.first()
        val dropped = videoLinks.drop(1)
        var text = item.text
        if (dropped.isNotEmpty()) {
            text = stripEntityTokens(
                text,
                dropped.flatMap { entity ->
                    listOfNotNull(entity.shortUrl, entity.title, "L${entity.title}")
                },
            )
        }
        text = collapseDuplicateToken(text, keep.shortUrl)
        if (keep.title.isNotBlank() && keep.title != keep.shortUrl) {
            text = collapseDuplicateToken(text, keep.title)
            text = collapseDuplicateToken(text, "L${keep.title}")
        }
        return item.copy(
            text = text,
            urlEntities = otherLinks + keep,
        )
    }

    private fun collapseDuplicateToken(text: String, token: String): String {
        if (token.isBlank()) return text
        val first = text.indexOf(token)
        if (first < 0) return text
        var result = text
        var searchFrom = first + token.length
        while (true) {
            val next = result.indexOf(token, searchFrom)
            if (next < 0) break
            result = result.removeRange(next, next + token.length)
        }
        return result
            .replace(Regex("[ \\t]{2,}"), " ")
            .replace(Regex("[ \\t]+\\n"), "\n")
            .replace(Regex("\\n[ \\t]+"), "\n")
            .trim()
    }

    private fun containsWeiboHostedVideoTitle(text: String): Boolean =
        Regex("""L?\S{1,40}的微博(?:视频|直播|故事)""").containsMatchIn(text)

    private fun unwrapStatusDetailPayload(root: JSONObject): JSONObject? {
        root.optJSONObject("data")?.let { data ->
            if (data.isStatusLike()) return data
            data.optJSONObject("mblog")?.let { if (it.isStatusLike()) return it }
            data.optJSONObject("status")?.let { if (it.isStatusLike()) return it }
        }
        root.optJSONObject("mblog")?.let { if (it.isStatusLike()) return it }
        root.optJSONObject("status")?.let { if (it.isStatusLike()) return it }
        return root.takeIf { it.isStatusLike() }
    }

    private fun JSONObject.isStatusLike(): Boolean =
        optJSONObject("user") != null ||
            optNullableString("idstr") != null ||
            optNullableString("mid") != null ||
            optNullableString("id") != null ||
            optJSONObject("retweeted_status") != null

    private fun parseImages(
        status: JSONObject,
        inlineLinkUrls: Set<String> = emptySet(),
    ): List<FeedImage> {
        val sourceText = status.optNullableString("text_raw")
            ?: status.optNullableString("text")
            ?: ""
        val fromStatus = imagesFromParts(status.optJSONArray("pic_ids"), status.optJSONObject("pic_infos"))
        val fromUrlStruct = buildList {
            val urlStruct = status.optJSONArray("url_struct") ?: return@buildList
            for (index in 0 until urlStruct.length()) {
                val entity = urlStruct.optJSONObject(index) ?: continue
                val picIds = entity.optJSONArray("pic_ids") ?: continue
                if (picIds.length() == 0) continue
                if (entity.optJSONObject("pic_infos") == null) continue
                val shortUrl = entity.optNullableString("short_url") ?: continue
                if (shortUrl in inlineLinkUrls) continue
                if (!sourceText.contains(shortUrl)) continue
                addAll(imagesFromParts(picIds, entity.optJSONObject("pic_infos")))
            }
        }
        val fromMixMedia = imagesFromMixMedia(status.optJSONObject("mix_media_info"))
        return mergeDuplicateFeedImages(fromStatus + fromUrlStruct + fromMixMedia)
    }

    private fun mergeDuplicateFeedImages(images: List<FeedImage>): List<FeedImage> =
        images.groupBy { image ->
            image.largeUrl.ifBlank { image.id }.substringBefore('?')
        }.map { (_, group) ->
            group.reduce(::mergeFeedImagePair)
        }

    private fun mergeFeedImagePair(primary: FeedImage, secondary: FeedImage): FeedImage {
        val liveVideo = primary.livePhotoVideoUrl ?: secondary.livePhotoVideoUrl
        val resolvedType = when {
            primary.type == "livephoto" || secondary.type == "livephoto" ->
                if (!liveVideo.isNullOrBlank()) "livephoto" else primary.type ?: secondary.type
            primary.type == "gif" || secondary.type == "gif" -> "gif"
            else -> primary.type ?: secondary.type
        }
        return primary.copy(
            livePhotoVideoUrl = liveVideo,
            videoStreamUrl = primary.videoStreamUrl ?: secondary.videoStreamUrl,
            type = resolvedType,
            downloadUrls = (primary.downloadUrls + secondary.downloadUrls).distinct(),
            width = primary.width ?: secondary.width,
            height = primary.height ?: secondary.height,
            shootTime = primary.shootTime ?: secondary.shootTime,
            cameraMake = primary.cameraMake ?: secondary.cameraMake,
            cameraModel = primary.cameraModel ?: secondary.cameraModel,
            createdAt = primary.createdAt ?: secondary.createdAt,
            statusId = primary.statusId ?: secondary.statusId,
        )
    }

    private fun parseImageType(info: JSONObject): String? =
        when (info.optNullableString("type")?.lowercase()) {
            "live", "live_photo", "livephoto" -> "livephoto"
            "gif" -> "gif"
            "video", "videos" -> "video"
            else -> info.optNullableString("type")
        }

    private fun parseLivePhotoVideoUrl(info: JSONObject): String? =
        listOfNotNull(
            info.optNullableString("video"),
            info.optNullableString("video_hd"),
            info.optNullableString("live_photo_video_url"),
            info.optNullableString("livephoto_video_url"),
            info.optJSONObject("live_photo")?.optNullableString("video"),
            info.optJSONObject("live_photo")?.optNullableString("video_hd"),
            info.optJSONObject("live_photo_info")?.optNullableString("video"),
            info.optJSONObject("live_photo_info")?.optNullableString("video_hd"),
        ).firstOrNull { it.isNotBlank() }?.let(::normalizeUrl)

    private fun feedImageFromPicInfo(picId: String, info: JSONObject): FeedImage? {
        val thumbnail = imageUrl(info, "bmiddle")
            ?: imageUrl(info, "orj360")
            ?: imageUrl(info, "thumbnail")
            ?: imageUrl(info, "large")
            ?: return null
        val large = imageUrl(info, "largest")
            ?: imageUrl(info, "mw2000")
            ?: imageUrl(info, "woriginal")
            ?: imageUrl(info, "original")
            ?: imageUrl(info, "large")
            ?: thumbnail
        val largeInfo = info.optJSONObject("largest")
            ?: info.optJSONObject("mw2000")
            ?: info.optJSONObject("woriginal")
            ?: info.optJSONObject("original")
            ?: info.optJSONObject("large")
        val downloadUrls = listOfNotNull(
            imageUrl(info, "largest"),
            imageUrl(info, "mw2000"),
            imageUrl(info, "woriginal"),
            imageUrl(info, "original"),
            imageUrl(info, "large"),
            imageUrl(info, "bmiddle"),
            imageUrl(info, "thumbnail"),
        ).distinct()
        val imgType = parseImageType(info)
        val liveVideo = parseLivePhotoVideoUrl(info)
        val resolvedType = when {
            imgType == "gif" -> "gif"
            imgType == "livephoto" || !liveVideo.isNullOrBlank() -> "livephoto"
            else -> imgType
        }
        val captureMeta = parseImageCaptureMetadata(info)
        return FeedImage(
            id = picId,
            thumbnailUrl = normalizeUrl(thumbnail),
            largeUrl = normalizeUrl(large),
            downloadUrls = downloadUrls.map(::normalizeUrl),
            livePhotoVideoUrl = liveVideo,
            type = resolvedType,
            width = largeInfo?.optInt("width")?.takeIf { it > 0 },
            height = largeInfo?.optInt("height")?.takeIf { it > 0 },
            shootTime = captureMeta.shootTime,
            cameraMake = captureMeta.cameraMake,
            cameraModel = captureMeta.cameraModel,
        )
    }

    private data class ImageCaptureMetadata(
        val shootTime: String?,
        val cameraMake: String?,
        val cameraModel: String?,
    )

    private fun parseImageCaptureMetadata(info: JSONObject): ImageCaptureMetadata {
        val exif = info.optJSONObject("exif")
            ?: info.optJSONObject("photo_meta")
            ?: info.optJSONObject("meta")
        val shootTime = sequenceOf(
            info.optNullableString("shoot_time"),
            info.optNullableString("photo_time"),
            exif?.optNullableString("shoot_time"),
            exif?.optNullableString("date_time_original"),
            exif?.optNullableString("DateTimeOriginal"),
        ).firstNotNullOfOrNull { it?.takeIf(String::isNotBlank) }
        val make = sequenceOf(
            info.optNullableString("make"),
            info.optNullableString("camera_make"),
            exif?.optNullableString("make"),
            exif?.optNullableString("Make"),
        ).firstNotNullOfOrNull { it?.takeIf(String::isNotBlank) }
        val model = sequenceOf(
            info.optNullableString("model"),
            info.optNullableString("camera_model"),
            info.optNullableString("camera"),
            exif?.optNullableString("model"),
            exif?.optNullableString("Model"),
        ).firstNotNullOfOrNull { it?.takeIf(String::isNotBlank) }
        return ImageCaptureMetadata(
            shootTime = shootTime,
            cameraMake = make,
            cameraModel = model,
        )
    }

    private fun imagesFromParts(picIds: JSONArray?, picInfos: JSONObject?): List<FeedImage> {
        if (picIds == null || picInfos == null) return emptyList()
        return buildList {
            for (index in 0 until picIds.length()) {
                val picId = picIds.optString(index).takeIf { it.isNotBlank() } ?: continue
                val info = picInfos.optJSONObject(picId) ?: continue
                feedImageFromPicInfo(picId, info)?.let(::add)
            }
        }
    }

    private fun imagesFromMixMedia(mixMediaInfo: JSONObject?): List<FeedImage> {
        val items = mixMediaInfo?.optJSONArray("items") ?: return emptyList()
        return buildList {
            for (index in 0 until items.length()) {
                val item = items.optJSONObject(index) ?: continue
                if (item.optNullableString("type") != "pic") continue
                val data = item.optJSONObject("data") ?: continue
                val picId = data.optBlankString("pic_id")
                    ?: data.optBlankString("object_id")
                    ?: data.optBlankString("id")
                    ?: run {
                        val largeCandidate = imageUrl(data, "largest")
                            ?: imageUrl(data, "large")
                            ?: imageUrl(data, "bmiddle")
                        largeCandidate?.let(::sinaimgPidFromUrl)
                    }
                    ?: continue
                feedImageFromPicInfo(picId, data)?.let(::add)
            }
        }
    }

    private fun sinaimgPidFromUrl(url: String): String? =
        Regex("""/([^/?#]+)\.(?:jpg|jpeg|png|gif|webp)""", RegexOption.IGNORE_CASE)
            .find(url)
            ?.groupValues
            ?.getOrNull(1)

    private fun parseVideoOrientation(mediaInfo: JSONObject): String? =
        mediaInfo.optNullableString("video_orientation")?.trim()?.takeIf { it.isNotBlank() }

    private fun parseMediaCoverDimensions(pageInfo: JSONObject, mediaInfo: JSONObject): Pair<Int?, Int?> {
        val bigPic = mediaInfo.optJSONObject("big_pic_info")?.optJSONObject("pic_big")
        val width = bigPic?.optInt("width")?.takeIf { it > 0 }
        val height = bigPic?.optInt("height")?.takeIf { it > 0 }
        if (width != null && height != null) return width to height
        return null to null
    }

    private fun parseMedias(status: JSONObject): List<FeedMedia> {
        val mixVideos = parseMixVideos(status)
        if (mixVideos.isNotEmpty()) return mixVideos
        return listOfNotNull(parsePageInfoMedia(status))
    }

    private fun parseMedia(status: JSONObject): FeedMedia? = parseMedias(status).firstOrNull()

    private fun parsePageInfoMedia(status: JSONObject): FeedMedia? {
        val pageInfo = status.optJSONObject("page_info") ?: return null
        val mediaInfo = pageInfo.optJSONObject("media_info") ?: return null
        val type = when (pageInfo.optNullableString("object_type")) {
            "live" -> MediaType.Live
            "audio" -> MediaType.Audio
            else -> MediaType.Video
        }
        if (type == MediaType.Live) {
            val liveStatus = parseLiveStatus(mediaInfo)
            val liveStream = (mediaInfo.optNullableString("live_ld")
                ?: mediaInfo.optNullableString("stream_url"))
                ?.let(::normalizeMediaUrl)
            val replayUrl = mediaInfo.optNullableString("replay_hd")?.let(::normalizeMediaUrl)
            if (liveStream.isNullOrBlank() && replayUrl.isNullOrBlank()) {
                return null
            }
            val cover = pageInfo.optNullableString("page_pic")
                ?: mediaInfo.optJSONObject("big_pic_info")?.optJSONObject("pic_big")?.optNullableString("url")
                ?: mediaInfo.optJSONObject("subscribe")?.optNullableString("cover")
            val (coverWidth, coverHeight) = parseMediaCoverDimensions(pageInfo, mediaInfo)
            return FeedMedia(
                type = MediaType.Live,
                title = mediaInfo.optNullableString("video_title")
                    ?: pageInfo.optNullableString("page_title")
                    ?: "微博直播",
                coverUrl = cover?.let(::normalizeUrl),
                streamUrl = liveStream.orEmpty(),
                replayUrl = replayUrl,
                liveStatus = liveStatus,
                downloadUrl = null,
                durationSeconds = if (liveStatus == 3) parseMediaDuration(pageInfo, mediaInfo) else null,
                videoOrientation = parseVideoOrientation(mediaInfo),
                coverWidth = coverWidth,
                coverHeight = coverHeight,
            )
        }
        val streamUrl = progressiveVideoUrl(mediaInfo)?.let(::normalizeMediaUrl) ?: return null
        val cover = pageInfo.optNullableString("page_pic")
            ?: mediaInfo.optJSONObject("big_pic_info")?.optJSONObject("pic_big")?.optNullableString("url")
            ?: mediaInfo.optJSONObject("subscribe")?.optNullableString("cover")
        val (coverWidth, coverHeight) = parseMediaCoverDimensions(pageInfo, mediaInfo)
        return FeedMedia(
            type = type,
            title = mediaInfo.optNullableString("video_title")
                ?: pageInfo.optNullableString("page_title")
                ?: when (type) {
                    MediaType.Live -> "微博直播"
                    MediaType.Audio -> "微博音频"
                    MediaType.Video -> "微博视频"
                },
            coverUrl = cover?.let(::normalizeUrl),
            streamUrl = streamUrl,
            downloadUrl = downloadVideoUrl(mediaInfo)?.let(::normalizeMediaUrl),
            durationSeconds = parseMediaDuration(pageInfo, mediaInfo),
            videoOrientation = parseVideoOrientation(mediaInfo),
            coverWidth = coverWidth,
            coverHeight = coverHeight,
        )
    }

    private fun parseLiveStatus(mediaInfo: JSONObject): Int? {
        if (!mediaInfo.has("live_status") || mediaInfo.isNull("live_status")) return null
        return when (val value = mediaInfo.opt("live_status")) {
            is Number -> value.toInt()
            else -> value?.toString()?.trim()?.toIntOrNull()
        }
    }

    private fun parseMixVideos(status: JSONObject): List<FeedMedia> {
        val items = status.optJSONObject("mix_media_info")?.optJSONArray("items") ?: return emptyList()
        return buildList {
            for (index in 0 until items.length()) {
                val item = items.optJSONObject(index) ?: continue
                if (item.optNullableString("type") != "video") continue
                val data = item.optJSONObject("data") ?: continue
                val mediaInfo = data.optJSONObject("media_info") ?: continue
                val streamUrl = progressiveVideoUrl(mediaInfo)?.let(::normalizeMediaUrl) ?: continue
                val cover = data.optNullableString("page_pic")
                    ?: mediaInfo.optJSONObject("big_pic_info")?.optJSONObject("pic_big")?.optNullableString("url")
                val (coverWidth, coverHeight) = parseMediaCoverDimensions(data, mediaInfo)
                add(
                    FeedMedia(
                        type = MediaType.Video,
                        title = mediaInfo.optNullableString("video_title")
                            ?: data.optNullableString("content1")
                            ?: "微博视频",
                        coverUrl = cover?.let(::normalizeUrl),
                        streamUrl = streamUrl,
                        downloadUrl = downloadVideoUrl(mediaInfo)?.let(::normalizeMediaUrl),
                        durationSeconds = parseMediaDuration(data, mediaInfo),
                        videoOrientation = parseVideoOrientation(mediaInfo),
                        coverWidth = coverWidth,
                        coverHeight = coverHeight,
                    ),
                )
            }
        }
    }

    private fun parseMediaDuration(vararg sources: JSONObject): Int? {
        for (source in sources) {
            parseDurationSeconds(source)?.let { return it }
        }
        return null
    }

    private fun parseDurationSeconds(json: JSONObject): Int? {
        json.optIntOrNull("duration")?.let { return normalizeDurationSeconds(it) }
        json.optIntOrNull("video_duration")?.let { return normalizeDurationSeconds(it) }
        json.optIntOrNull("duration_time")?.let { return normalizeDurationSeconds(it) }
        json.optNullableString("duration_time")?.let { parseDurationTimeString(it) }?.let { return it }
        json.optNullableString("duration_label")?.let { parseDurationTimeString(it) }?.let { return it }
        json.optNullableString("video_duration")?.let { parseDurationTimeString(it) }?.let { return it }
        return null
    }

    private fun normalizeDurationSeconds(raw: Int): Int? {
        if (raw <= 0) return null
        val seconds = if (raw > 10_000) raw / 1000 else raw
        return seconds.takeIf { it > 0 }
    }

    private fun parseDurationTimeString(raw: String): Int? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null
        trimmed.toIntOrNull()?.let { return normalizeDurationSeconds(it) }
        val parts = trimmed.split(":")
        return when (parts.size) {
            2 -> {
                val minutes = parts[0].toIntOrNull() ?: return null
                val seconds = parts[1].toIntOrNull() ?: return null
                (minutes * 60 + seconds).takeIf { it > 0 }
            }
            3 -> {
                val hours = parts[0].toIntOrNull() ?: return null
                val minutes = parts[1].toIntOrNull() ?: return null
                val seconds = parts[2].toIntOrNull() ?: return null
                (hours * 3600 + minutes * 60 + seconds).takeIf { it > 0 }
            }
            else -> null
        }
    }

    private fun progressiveVideoUrl(mediaInfo: JSONObject): String? {
        firstMediaUrl(
            mediaInfo,
            "mp4_1080p_mp4",
            "mp4_720p_mp4",
            "mp4_hd_url",
            "mp4_sd_url",
            "mp4_ld_url",
            "stream_url_hd",
            "stream_url",
        )?.takeIf { isPreferredVideoUrl(it) }?.let { return it }

        bestProgressiveFromPlaybackList(mediaInfo)?.let { return it }

        firstMediaUrl(
            mediaInfo,
            "mp4_1080p_mp4",
            "mp4_720p_mp4",
            "mp4_hd_url",
            "mp4_sd_url",
            "mp4_ld_url",
            "stream_url_hd",
            "stream_url",
        )?.let { return it }

        return firstMediaUrl(
            mediaInfo,
            "h265_mp4_hd",
            "h265_mp4_ld",
            "hevc_mp4_720p",
            "hevc_mp4_hd",
        )
    }

    private fun bestProgressiveFromPlaybackList(mediaInfo: JSONObject): String? {
        val playback = mediaInfo.optJSONObject("playback_list")
            ?.optJSONArray("play_info")
            ?: mediaInfo.optJSONArray("playback_list")
            ?: return null

        val candidates = buildList {
            for (index in 0 until playback.length()) {
                val item = playback.optJSONObject(index) ?: continue
                val meta = item.optJSONObject("meta")
                val playInfo = item.optJSONObject("play_info") ?: item
                if (meta?.optBoolean("is_hidden") == true) continue
                val type = meta?.optInt("type", -1)
                    ?: playInfo.optInt("type", -1)
                if (type == 2) continue
                val protocol = playInfo.optNullableString("protocol")
                    ?: item.optNullableString("protocol")
                if (protocol.equals("dash", ignoreCase = true)) continue
                val url = playInfo.optNullableString("url") ?: continue
                val quality = meta?.optInt("quality_index", -1)
                    ?: playInfo.optInt("quality_index", -1)
                        .takeIf { it >= 0 }
                    ?: item.optInt("quality_index", -1)
                        .takeIf { it >= 0 }
                    ?: item.optInt("quality", item.optInt("q", 0))
                add(VideoCandidate(url = url, quality = quality, score = videoCompatibilityScore(url)))
            }
        }
        return candidates
            .filter { it.score > 0 }
            .maxWithOrNull(compareBy<VideoCandidate> { it.score }.thenBy { it.quality })
            ?.url
    }

    private fun downloadVideoUrl(mediaInfo: JSONObject): String? =
        firstMediaUrl(
            mediaInfo,
            "mp4_1080p_mp4",
            "mp4_720p_mp4",
            "mp4_hd_url",
            "mp4_sd_url",
            "mp4_ld_url",
            "stream_url_hd",
            "stream_url",
            "h265_mp4_hd",
            "h265_mp4_ld",
            "hevc_mp4_720p",
            "hevc_mp4_hd",
        )

    private data class VideoCandidate(
        val url: String,
        val quality: Int,
        val score: Int,
    )

    private fun firstMediaUrl(mediaInfo: JSONObject, vararg keys: String): String? =
        keys.firstNotNullOfOrNull { key -> mediaInfo.optNullableString(key) }

    private fun isPreferredVideoUrl(url: String): Boolean =
        videoCompatibilityScore(url) >= 3

    private fun videoCompatibilityScore(url: String): Int {
        val lower = url.lowercase()
        return when {
            lower.contains("h265") || lower.contains("hevc") -> 1
            lower.contains(".m3u8") || lower.contains("m3u8") -> 2
            lower.contains(".mp4") || lower.contains("mp4") -> 3
            else -> 1
        }
    }

    private fun imageUrl(info: JSONObject, key: String): String? =
        info.optJSONObject(key)?.optNullableString("url")

    private fun looksLikeImageUrl(value: String): Boolean {
        val lower = value.lowercase()
        return lower.contains("sinaimg") ||
            lower.contains(".jpg") ||
            lower.contains(".jpeg") ||
            lower.contains(".png") ||
            lower.contains(".webp")
    }

    private fun parseIpLocation(status: JSONObject): String? {
        val directly = status.optNullableString("region_name")
            ?: status.optNullableString("status_region")
            ?: status.optNullableString("ip_location")
            ?: status.optJSONObject("region_info")?.optNullableString("region_name")
            ?: status.optJSONObject("region_info")?.optNullableString("name")
            ?: status.optJSONObject("user")?.optNullableString("location")
            ?: status.optJSONObject("user")?.optNullableString("region_name")
        if (!directly.isNullOrBlank()) return directly

        // 部分 API 将 IP 属地放在 source 字段中，如 "来自 xxx"
        val source = status.optNullableString("source")
        return source
            ?.takeIf { it.startsWith("来自") || it.startsWith("发布于") }
            ?.removePrefix("来自")
            ?.removePrefix("发布于")
            ?.trim()
    }

    private data class ParsedStatusLocation(
        val name: String,
        val url: String?,
    )

    private fun pickLocationUrl(obj: JSONObject?): String? {
        if (obj == null) return null
        return sequenceOf(
            obj.optNullableString("h5_target_url"),
            obj.optNullableString("page_url"),
            obj.optNullableString("long_url"),
            obj.optNullableString("ori_url"),
            obj.optNullableString("target_url"),
            obj.optNullableString("url"),
            obj.optNullableString("scheme"),
            obj.optNullableString("short_url"),
        ).firstOrNull { !it.isNullOrBlank() }
    }

    private fun pickPoiId(obj: JSONObject?): String? {
        if (obj == null) return null
        sequenceOf(
            obj.optNullableString("poiid"),
            obj.optNullableString("poi_id"),
            obj.optNullableString("object_id"),
            obj.optNullableString("page_id"),
            obj.optNullableString("containerid"),
        ).forEach { raw ->
            val normalized = normalizePoiId(raw)
            if (normalized != null) return normalized
        }
        return null
    }

    private fun normalizePoiId(raw: String?): String? {
        var id = raw?.trim()?.takeIf { it.isNotBlank() } ?: return null
        id = id.substringAfterLast(':').trim()
        if (id.isBlank()) return null
        // Weibo place page ids usually look like 100101 + poiid
        if (id.startsWith("100101") && id.length > 6) return id
        // 真实 POI 常见 B 开头十六进制；视频 object_id（纯数字等）不能当地点。
        if (id.matches(Regex("""B[0-9A-F]{15,}""", RegexOption.IGNORE_CASE))) return id
        return null
    }

    private fun buildWeiboPlacePageUrl(poiIdRaw: String?): String? {
        val id = normalizePoiId(poiIdRaw) ?: return null
        val pageId = if (id.startsWith("100101")) id else "100101$id"
        return "https://weibo.com/p/$pageId"
    }

    private fun resolveStatusLocationUrl(
        primary: JSONObject?,
        vararg fallbacks: JSONObject?,
    ): String? {
        sequenceOf(primary, *fallbacks).forEach { obj ->
            pickLocationUrl(obj)?.let { return it }
        }
        sequenceOf(primary, *fallbacks).forEach { obj ->
            buildWeiboPlacePageUrl(pickPoiId(obj))?.let { return it }
        }
        return null
    }

    private fun isPlaceObjectType(type: String?): Boolean {
        val normalized = type?.lowercase().orEmpty()
        return normalized in setOf("place", "poi", "location", "checkin")
    }

    private fun looksLikeNonPlaceLocationTitle(name: String): Boolean {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return true
        if (trimmed.contains("微博视频") ||
            trimmed.contains("微博直播") ||
            trimmed.contains("微博故事") ||
            trimmed.contains("的视频")
        ) {
            return true
        }
        // 视频进度锚点（如 10:29）不是地点。
        return trimmed.matches(Regex("""^\d{1,2}:\d{2}(?::\d{2})?$"""))
    }

    private fun parseStatusLocation(status: JSONObject): ParsedStatusLocation? {
        val urls = status.optJSONArray("url_struct")
        for (index in 0 until (urls?.length() ?: 0)) {
            val entry = urls?.optJSONObject(index) ?: continue
            if (isAttachedMediaUrlStructEntity(entry)) continue
            val type = entry.optNullableString("object_type")
            val hasPoi = pickPoiId(entry) != null ||
                pickPoiId(entry.optJSONObject("object")) != null
            if (!isPlaceObjectType(type) && !hasPoi) continue
            val title = entry.optNullableString("url_title")
                ?: entry.optNullableString("title")
                ?: entry.optJSONObject("object")?.optNullableString("object_name")
                ?: continue
            val name = plainText(title).takeIf { it.isNotBlank() } ?: continue
            if (looksLikeNonPlaceLocationTitle(name)) continue
            return ParsedStatusLocation(
                name = name,
                url = resolveStatusLocationUrl(
                    entry,
                    entry.optJSONObject("object"),
                    entry.optJSONObject("actionlog"),
                ),
            )
        }

        val annotations = status.optJSONArray("annotations")
        for (index in 0 until (annotations?.length() ?: 0)) {
            val annotation = annotations?.optJSONObject(index) ?: continue
            val place = annotation.optJSONObject("place") ?: annotation.optJSONObject("poi")
            val title = place?.optNullableString("title")
                ?: place?.optNullableString("name")
                ?: annotation.optNullableString("title")
            if (title.isNullOrBlank()) continue
            val name = plainText(title).takeIf { it.isNotBlank() } ?: continue
            if (looksLikeNonPlaceLocationTitle(name)) continue
            return ParsedStatusLocation(
                name = name,
                url = resolveStatusLocationUrl(place, annotation),
            )
        }

        val pageInfo = status.optJSONObject("page_info") ?: return null
        val type = pageInfo.optNullableString("type")
            ?: pageInfo.optNullableString("object_type")
        if (type?.lowercase() in setOf("video", "live", "story", "movie", "audio", "gif")) {
            return null
        }
        val hasPoi = pickPoiId(pageInfo) != null ||
            pickPoiId(pageInfo.optJSONObject("poi")) != null ||
            pickPoiId(pageInfo.optJSONObject("object")) != null
        if (!isPlaceObjectType(type) && !hasPoi) return null
        val title = pageInfo.optNullableString("page_title")
            ?: pageInfo.optNullableString("title")
            ?: pageInfo.optJSONObject("poi")?.optNullableString("title")
            ?: pageInfo.optJSONObject("object")?.optNullableString("object_name")
            ?: return null
        val name = plainText(title).takeIf { it.isNotBlank() } ?: return null
        if (looksLikeNonPlaceLocationTitle(name)) return null
        return ParsedStatusLocation(
            name = name,
            url = resolveStatusLocationUrl(
                pageInfo,
                pageInfo.optJSONObject("poi"),
                pageInfo.optJSONObject("object"),
            ),
        )
    }

    private fun statusId(status: JSONObject): String =
        status.optNullableString("idstr")
            ?: status.optNullableString("mid")
            ?: status.optNullableString("id")
            ?: ""

    private fun pickAnalysisExtraValue(analysisExtra: String?, key: String): String? {
        if (analysisExtra.isNullOrBlank()) return null
        val pattern = Regex("""$key:([^|]+)""")
        return pattern.find(analysisExtra)?.groupValues?.getOrNull(1)
    }

    private fun shouldAttachMediaToRetweeted(outer: JSONObject, retweeted: JSONObject): Boolean {
        val retweetedId = statusId(retweeted)
        if (retweetedId.isBlank()) return false

        val pageInfo = outer.optJSONObject("page_info")
        val mediaAuthorMid = pageInfo?.optJSONObject("media_info")?.optNullableString("author_mid")
            ?: pageInfo?.optNullableString("author_mid")
        if (!mediaAuthorMid.isNullOrBlank() && mediaAuthorMid == retweetedId) {
            return true
        }

        val rootMid = pickAnalysisExtraValue(outer.optNullableString("analysis_extra"), "mblog_rt_mid")
        return rootMid == retweetedId
    }

    private fun normalizeRetweetedStatus(outer: JSONObject, retweeted: JSONObject): JSONObject {
        val mediaBelongsToRetweeted = shouldAttachMediaToRetweeted(outer, retweeted)
        val merged = if (mediaBelongsToRetweeted) {
            mergeRetweetedMediaFields(outer, retweeted)
        } else {
            JSONObject(retweeted.toString())
        }
        mergeRetweetedUrlStruct(outer, merged, mediaBelongsToRetweeted)
        return merged
    }

    private fun mergeRetweetedUrlStruct(
        outer: JSONObject,
        retweeted: JSONObject,
        inheritAllFromOuter: Boolean,
    ) {
        val outerStruct = outer.optJSONArray("url_struct") ?: return
        if (isJsonArrayEmpty(outerStruct)) return

        val retweetedStruct = retweeted.optJSONArray("url_struct")
        val mergedStruct = JSONArray()
        val existingShortUrls = linkedSetOf<String>()

        if (!isJsonArrayEmpty(retweetedStruct)) {
            for (index in 0 until retweetedStruct!!.length()) {
                retweetedStruct.optJSONObject(index)?.let { entity ->
                    entity.optNullableString("short_url")?.let(existingShortUrls::add)
                    mergedStruct.put(entity)
                }
            }
        }

        val retweetedText = retweetedTextForUrlMatch(retweeted)
        val inheritAll = inheritAllFromOuter && isJsonArrayEmpty(retweetedStruct)
        val retweetedAlreadyHasVideoLink = (0 until mergedStruct.length()).any { index ->
            mergedStruct.optJSONObject(index)?.let(::isAttachedMediaUrlStructEntity) == true
        }
        for (index in 0 until outerStruct.length()) {
            val entity = outerStruct.optJSONObject(index) ?: continue
            val shortUrl = entity.optNullableString("short_url") ?: continue
            if (shortUrl in existingShortUrls) continue
            // 外层 //@ 里的视频短链若并入原博，会与原博自身短链叠成两条「xxx的微博视频」。
            if (!inheritAll &&
                retweetedAlreadyHasVideoLink &&
                isAttachedMediaUrlStructEntity(entity)
            ) {
                continue
            }
            if (inheritAll || retweetedText.contains(shortUrl)) {
                mergedStruct.put(entity)
                existingShortUrls.add(shortUrl)
            }
        }

        if (mergedStruct.length() > 0) {
            retweeted.put("url_struct", mergedStruct)
        }
    }

    private fun retweetedTextForUrlMatch(retweeted: JSONObject): String =
        listOfNotNull(
            retweeted.optNullableString("text"),
            retweeted.optNullableString("text_raw"),
            retweeted.optNullableString("raw_text"),
        ).joinToString("\n")

    private fun mergeRetweetedMediaFields(outer: JSONObject, retweeted: JSONObject): JSONObject {
        val merged = JSONObject(retweeted.toString())
        if (merged.optJSONObject("page_info") == null) {
            outer.optJSONObject("page_info")?.let { merged.put("page_info", it) }
        }
        if (isJsonArrayEmpty(merged.optJSONArray("pic_ids"))) {
            outer.optJSONArray("pic_ids")?.let { merged.put("pic_ids", it) }
        }
        if (isJsonObjectEmpty(merged.optJSONObject("pic_infos"))) {
            outer.optJSONObject("pic_infos")?.let { merged.put("pic_infos", it) }
        }
        if (!hasMixMediaVideo(merged) && hasMixMediaVideo(outer)) {
            outer.optJSONObject("mix_media_info")?.let { merged.put("mix_media_info", it) }
        }
        return merged
    }

    private fun isJsonArrayEmpty(array: JSONArray?): Boolean =
        array == null || array.length() == 0

    private fun isJsonObjectEmpty(obj: JSONObject?): Boolean =
        obj == null || obj.length() == 0

    private fun hasMixMediaVideo(status: JSONObject): Boolean {
        val items = status.optJSONObject("mix_media_info")?.optJSONArray("items") ?: return false
        for (index in 0 until items.length()) {
            if (items.optJSONObject(index)?.optNullableString("type") == "video") {
                return true
            }
        }
        return false
    }

    private fun JSONObject.looksLikeAd(): Boolean {
        if (optBoolean("is_ad")) return true
        if (has("ad_state") || has("promotion") || has("ad_marked")) return true
        val typeName = optNullableString("mblogtypename").orEmpty()
        val title = optNullableString("title").orEmpty()
        return typeName.contains("广告") || title.contains("广告")
    }

    private fun JSONObject.optNullableString(name: String): String? {
        if (!has(name) || isNull(name)) return null
        val value = opt(name)?.toString()?.trim().orEmpty()
        return value.takeIf { it.isNotBlank() && it != "0" }
    }

    private fun JSONObject.optTruthy(name: String): Boolean {
        if (!has(name) || isNull(name)) return false
        return when (val value = opt(name)) {
            is Boolean -> value
            is Number -> value.toInt() != 0
            else -> value?.toString()?.trim() in setOf("1", "true", "TRUE")
        }
    }

    private fun JSONObject.parseEditMetadata(): Pair<Boolean, Int> {
        val editCount = optIntOrNull("edit_count")
            ?: optIntOrNull("editCount")
            ?: 0
        val editAt = optNullableString("edit_at") ?: optNullableString("editAt")
        val editedFlag = optTruthy("edited") || optTruthy("is_edit") || optTruthy("isEdit")
        val isEdited = editCount > 0 || editAt != null || editedFlag
        return isEdited to editCount
    }

    private fun JSONObject.parseAttitudesStatus(): Boolean {
        for (key in listOf("attitudes_status", "liked", "is_like", "isLike")) {
            if (!has(key) || isNull(key)) continue
            when (val raw = opt(key)) {
                is Boolean -> return raw
                is Number -> return raw.toInt() != 0
                is String -> {
                    when (raw.trim().lowercase()) {
                        "true", "1", "yes" -> return true
                        "false", "0", "no" -> return false
                    }
                }
            }
        }
        return false
    }

    private fun JSONObject.optIntOrNull(name: String): Int? {
        if (!has(name) || isNull(name)) return null
        return when (val value = opt(name)) {
            is Number -> value.toInt()
            else -> value?.toString()?.trim()?.toIntOrNull()
        }
    }

    private fun userId(user: JSONObject?): String =
        user?.optNullableString("idstr") ?: user?.optNullableString("id") ?: ""

    private fun parseUserDisplayName(user: JSONObject?): String {
        if (user == null) return "微博用户"
        val raw = user.optNullableString("screen_name")
            ?: user.optNullableString("name")
            ?: user.optNullableString("remark")
            ?: ""
        return plainText(raw).ifBlank { "微博用户" }
    }

    private fun collectRetweetedStatusTexts(status: JSONObject): List<String> {
        val texts = mutableListOf<String>()
        var current: JSONObject? = status
        while (current != null) {
            current.optNullableString("text")?.takeIf { it.isNotBlank() }?.let(texts::add)
            current = current.optJSONObject("retweeted_status")
        }
        return texts
    }

    private fun extractEmoticonsFromHtml(html: String): Map<String, String> {
        if (html.isBlank()) return emptyMap()
        val map = mutableMapOf<String, String>()
        val pattern = Regex(
            """<img\b[^>]*\balt="(\[[^"\]]+\])"[^>]*\bsrc="([^"]+)"[^>]*>|<img\b[^>]*\bsrc="([^"]+)"[^>]*\balt="(\[[^"\]]+\])"[^>]*>""",
            RegexOption.IGNORE_CASE,
        )
        pattern.findAll(html).forEach { match ->
            val phrase = match.groupValues[1].ifBlank { match.groupValues[4] }
            val url = match.groupValues[2].ifBlank { match.groupValues[3] }
            if (phrase.isBlank() || url.isBlank()) return@forEach
            map[phrase] = normalizeUrl(url)
        }
        return map
    }

    private fun JSONObject.isLongTextPreview(): Boolean =
        hasLongTextFlag() && !(has("longText") && !isNull("longText"))

    private fun JSONObject.hasLongTextFlag(): Boolean =
        optTruthy("isLongText") || optTruthy("is_long_text")

    private fun JSONObject.shouldShowLongTextExpand(
        rawText: String,
        displayText: String,
        hasAttachedVideo: Boolean,
    ): Boolean {
        if (!isLongTextPreview()) return false
        // `isLongText` 也会用于超话、热搜等客户端附加卡片。只有正文确实带有
        // 截断迹象，或落在微博常见的预览长度时才展示「阅读全文」。部分接口会移除
        // 预览末尾的“展开”标签，但仍保留约 140 字的截断正文。
        // Media is already rendered independently (all image thumbnails are visible), so
        // image/video counts must never decide whether text is expandable. URL/super-topic
        // rewriting can also change rawText without hiding a single character of正文.
        if (hasExplicitTextTruncationSignal(rawText, displayText)) return true
        // Without an explicit marker, trust only the visible text length together with the
        // API long-text flag. Short captions remain complete regardless of attached media.
        return isLikelyTruncatedLongTextPreview(displayText) ||
            isSubstantialLongTextPreview(displayText)
    }

    private fun JSONObject.hasVideoOrLivePageInfo(): Boolean {
        val pageInfo = optJSONObject("page_info") ?: return false
        val type = pageInfo.optNullableString("type")?.lowercase()
            ?: pageInfo.optNullableString("object_type")?.lowercase()
        if (type in setOf("video", "live")) return true
        if (type in setOf("article", "webpage", "topic", "place", "poi")) return false
        return pageInfo.optJSONObject("media_info") != null
    }

    private fun JSONObject.isLongTextExpandForExtraImagesOnly(
        rawText: String,
        displayText: String,
    ): Boolean {
        if (resolvePicCount() <= 9) return false
        if (hasTextTruncationSignals(rawText, displayText)) return false
        // 图文混排多个视频时 pic_num 会虚高；不能按「纯九图以上」误藏阅读全文。
        if (hasVideoOrLivePageInfo() || hasMixMediaVideo(this) || optJSONObject("mix_media_info") != null) {
            return false
        }
        // 超过九图：仅当配文已完整（偏短）时才视为误标；长配文仍应显示「阅读全文」。
        return isShortMediaCaption(displayText)
    }

    private fun JSONObject.resolvePicCount(): Int {
        val fromPicIds = optJSONArray("pic_ids")?.length() ?: 0
        val fromPicNum = optInt("pic_num").takeIf { it > 0 } ?: 0
        return maxOf(fromPicIds, fromPicNum)
    }

    private fun hasTextTruncationSignals(rawText: String, displayText: String): Boolean {
        if (containsLongTextExpandMarker(rawText)) return true
        val plain = plainText(displayText).trimEnd()
        return plain.endsWith("...") || plain.endsWith("…") || plain.endsWith("⋯")
    }

    private fun hasExplicitTextTruncationSignal(rawText: String, displayText: String): Boolean {
        if (containsLongTextExpandMarker(rawText) || containsLongTextExpandMarker(displayText)) {
            return true
        }
        val plain = plainText(displayText).trimEnd()
        return plain.endsWith("...") || plain.endsWith("…") || plain.endsWith("⋯")
    }

    private fun isLikelyTruncatedLongTextPreview(text: String): Boolean {
        val length = plainText(text).replace(Regex("""\s+"""), "").length
        return length in 120..170
    }

    /** 接口去掉「全文/展开」后，预览仍明显偏长，说明还有正文未展示。 */
    private fun isSubstantialLongTextPreview(text: String): Boolean {
        val length = plainText(text).replace(Regex("""\s+"""), "").length
        return length > 170
    }

    private fun isShortMediaCaption(text: String): Boolean =
        plainText(text).replace(Regex("""\s+"""), "").length < 120

    private fun isClientSupplementOnlyLongText(preview: String, expanded: String): Boolean {
        val normalizedPreview = normalizeLongTextForComparison(preview)
        val normalizedExpanded = normalizeLongTextForComparison(expanded)
        if (normalizedExpanded.isBlank() || normalizedExpanded == normalizedPreview) return true
        if (!normalizedExpanded.startsWith(normalizedPreview)) return false
        val appended = normalizedExpanded.removePrefix(normalizedPreview)
        if (appended.isBlank()) return true
        // The long-text endpoint sometimes appends only client cards. Their rendered text
        // can contain a super-topic token, a video title and a t.cn/http link, so checking
        // the marker words alone incorrectly treats the response as new article content.
        val meaningfulRemainder = CLIENT_SUPPLEMENT_MARKERS
            .fold(appended) { remaining, marker -> remaining.replace(marker, "") }
            .replace(
                Regex("""https?://[A-Za-z0-9._~:/?#\[\]@!$&'()*+,;=%-]+""", RegexOption.IGNORE_CASE),
                "",
            )
            .replace(Regex("""(?:https?://)?t\.cn/[A-Za-z0-9]+""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""#?[^#\s]{1,80}\[超话]\]#?"""), "")
            .replace(Regex("""#?[^#\s]{1,80}超话#?"""), "")
            .replace(Regex("""[^\s/，。！？、:：;；]{0,60}的微博视频"""), "")
            .replace(Regex("""微博视频"""), "")
            .replace(Regex("""[\s\p{P}\p{S}]+"""), "")
        return meaningfulRemainder.isBlank()
    }

    private fun normalizeLongTextForComparison(text: String): String =
        plainText(stripLongTextPreviewLabel(text))
            .replace(Regex("""\s+"""), "")
            .trim()

    private val CLIENT_SUPPLEMENT_MARKERS = listOf(
        "大家都在搜",
        "微博热搜",
        "超话社区",
        "微博超话",
        "超话",
    )

    private fun containsLongTextExpandMarker(text: String): Boolean =
        LONG_TEXT_EXPAND_MARKER.containsMatchIn(text)

    private val LONG_TEXT_EXPAND_MARKER = Regex(
        """<span\b[^>]*\bclass=(["'])[^"']*\bexpand\b[^"']*\1|""" +
            """(?:\.{3}|…|⋯)\s*(?:<a\b[^>]*>\s*)?(?:全文|展开)|""" +
            """(?<=\S)(?:\.{3}|…|⋯)?\s*(?:全文|展开)\s*</""",
        RegexOption.IGNORE_CASE,
    )

    private fun stripLongTextPreviewLabel(text: String): String =
        text
            .replace(
                Regex(
                    """<span\b[^>]*\bclass=(["'])[^"']*\bexpand\b[^"']*\1[^>]*>.*?</span>""",
                    RegexOption.IGNORE_CASE,
                ),
                "",
            )
            .replace(
                Regex(
                    """(?:\.{3}|…|⋯)\s*(?:<a\b[^>]*>\s*)?(?:全文|展开)\s*(?:</a>)?(?=\s*//[@＠])""",
                    RegexOption.IGNORE_CASE,
                ),
                "",
            )
            .replace(Regex("""(?:\.{3}|…|⋯)?\s*(?:全文|展开)(?=\s*//[@＠])"""), "")
            // 客户端已有「阅读全文」按钮，正文末尾的 “…全文 / …展开”整段去掉（含省略号）。
            .replace(
                Regex(
                    """(?:\.{3}|…|⋯)\s*(?:<a\b[^>]*>\s*)?(?:全文|展开)\s*(?:</a>)?\s*$""",
                    RegexOption.IGNORE_CASE,
                ),
                "",
            )
            .replace(Regex("""(?<=\S)\s*(?:全文|展开)\s*$"""), "")
            .trimEnd()

    private fun plainText(value: String): String =
        value
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            // 表情在 HTML 里是 <img alt="[微笑]">；剥标签前先还原成 [微笑]，否则首页正文会丢表情。
            .replace(HTML_EMOTICON_IMG_PATTERN) { match ->
                match.groupValues[2]
            }
            .replace(Regex("<[^>]+>"), "")
            .replace("&nbsp;", " ")
            .let(::htmlDecode)
            .trim()

    private val HTML_EMOTICON_IMG_PATTERN = Regex(
        """<img\b[^>]*\balt\s*=\s*(["'])(\[[^\[\]]+\])\1[^>]*/?>""",
        RegexOption.IGNORE_CASE,
    )

    /**
     * 用 HTML 正文里的锚点位置还原短链，避免 text_raw 把「▷10:29」等时间戳挤到段落末尾，
     * 同时仍保留 t.cn 以便 url_struct 渲染成可点链接。
     */
    private fun buildStatusDisplayText(
        rawText: String,
        htmlText: String,
        isLongTextPreview: Boolean,
    ): String {
        val preferredRaw = if (isLongTextPreview) stripLongTextPreviewLabel(rawText) else rawText
        val fromHtml = plainText(expandHtmlAnchorsToShortUrls(htmlText))
            .let { if (isLongTextPreview) stripLongTextPreviewLabel(it) else it }
            .trim()
        if (fromHtml.isBlank()) return preferredRaw
        val htmlLinkCount = countTcShortLinks(fromHtml)
        val rawLinkCount = countTcShortLinks(preferredRaw)
        val htmlTopicCount = countTopicMarkers(fromHtml)
        val rawTopicCount = countTopicMarkers(preferredRaw)
        // HTML 若丢掉了 text_raw 里的短链（投票/网页链接常见无 data-url），必须回退，
        // 否则「微博投票」会从可点链接变成纯文字甚至被后续剥链弄没。
        // 同样：超话锚点常无 data-url，HTML 只剩「话题名」时要回退保留 #话题[超话]#。
        return when {
            rawLinkCount > htmlLinkCount -> preferredRaw
            rawTopicCount > htmlTopicCount -> preferredRaw
            htmlLinkCount > 0 -> fromHtml
            fromHtml.length >= preferredRaw.length * 0.6f -> fromHtml
            else -> preferredRaw
        }
    }

    private fun countTcShortLinks(text: String): Int =
        Regex("""https?://t\.cn/\S+""").findAll(text).count()

    private fun countTopicMarkers(text: String): Int {
        if (text.isBlank()) return 0
        val hashTopics = Regex("""#[^#\n]+#""").findAll(text).count()
        val chaohua = Regex("""\[[\s\u200b]*超话[\s\u200b]*\]""").findAll(text).count()
        return hashTopics + chaohua
    }

    private fun expandHtmlAnchorsToShortUrls(html: String): String {
        if (html.isBlank() || !html.contains("<a", ignoreCase = true)) return html
        return html.replace(
            Regex(
                """<a\b([^>]*)>(.*?)</a>""",
                setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
            ),
        ) { match ->
            val attrs = match.groupValues[1]
            val inner = match.groupValues[2]
            val dataUrl = Regex(
                """\bdata-url\s*=\s*["']([^"']+)["']""",
                RegexOption.IGNORE_CASE,
            ).find(attrs)?.groupValues?.getOrNull(1)?.trim()
            val href = Regex(
                """\bhref\s*=\s*["']([^"']+)["']""",
                RegexOption.IGNORE_CASE,
            ).find(attrs)?.groupValues?.getOrNull(1)?.trim()
            val candidate = sequenceOf(dataUrl, href)
                .firstOrNull { !it.isNullOrBlank() }
                ?.let(::normalizeUrl)
            when {
                !candidate.isNullOrBlank() && looksLikeExpandableStatusLink(candidate) -> {
                    " $candidate "
                }
                // 投票/网页等锚点若只有长链、无 data-url，保留文案，短链交给 text_raw 回退逻辑。
                else -> inner
            }
        }
    }

    private fun looksLikeExpandableStatusLink(url: String): Boolean {
        val lower = url.lowercase()
        // 仅还原 t.cn 短链，保留话题/昵称等锚点可见文案（#话题#、@用户）。
        // 若连 weibo.com 一起替换，首页会露出 s.weibo.com/weibo?q=%23... 这种脏链。
        return lower.contains("t.cn/") || lower.startsWith("sinaweibo://")
    }

    private fun normalizeUrl(url: String): String =
        when {
            url.startsWith("//") -> "https:$url"
            url.startsWith("http://") && url.contains("sinaimg.cn") -> url.replaceFirst("http://", "https://")
            else -> url
        }

    private fun normalizeMediaUrl(url: String): String =
        when {
            url.startsWith("//") -> "https:$url"
            else -> url
        }

    private fun formatCount(value: Any?): String {
        val count = when (value) {
            is Number -> value.toLong()
            is String -> value.toLongOrNull()
            else -> null
        } ?: return "0"
        return when {
            count >= 10_000 -> {
                val value = String.format("%.1f", count / 10_000.0).removeSuffix(".0")
                "${value}万"
            }
            else -> count.toString()
        }
    }

    fun parseEmotions(raw: String): List<WeiboEmoticon> {
        val root = org.json.JSONObject(raw)
        val phraseMap = linkedMapOf<String, WeiboEmoticon>()

        root.optJSONObject("data")?.optJSONArray("emoticons")?.let { legacy ->
            for (index in 0 until legacy.length()) {
                legacy.optJSONObject(index)?.let(::normalizeEmoticonEntry)?.let { entry ->
                    phraseMap[entry.phrase] = entry
                }
            }
        }

        for ((_, entries) in resolveEmoticonLocaleGroups(root)) {
            for (index in 0 until entries.length()) {
                entries.optJSONObject(index)?.let(::normalizeEmoticonEntry)?.let { entry ->
                    phraseMap[entry.phrase] = entry
                }
            }
        }

        return phraseMap.values.toList()
    }

    private fun normalizeEmoticonEntry(obj: JSONObject): WeiboEmoticon? {
        val phrase = obj.optNullableString("phrase") ?: return null
        var url = obj.optNullableString("url")
            ?: obj.optNullableString("icon")
            ?: return null
        if (url.startsWith("//")) url = "https:$url"
        return WeiboEmoticon(phrase = phrase, url = url)
    }

    private fun resolveEmoticonLocaleGroups(root: JSONObject): Map<String, JSONArray> {
        val nested = root.optJSONObject("data")?.optJSONObject("emoticon")
        val topLevel = root.optJSONObject("emoticon")
        unwrapEmoticonLocale(nested)?.let { return collectEmoticonGroups(it) }
        unwrapEmoticonLocale(topLevel)?.let { return collectEmoticonGroups(it) }
        resolveFlatEmoticonGroups(nested)?.let { return it }
        resolveFlatEmoticonGroups(topLevel)?.let { return it }
        return emptyMap()
    }

    private fun unwrapEmoticonLocale(emoticon: JSONObject?): JSONObject? {
        if (emoticon == null) return null
        emoticon.optJSONObject("ZH_CN")?.let { return it }
        val keys = emoticon.keys()
        while (keys.hasNext()) {
            val value = emoticon.opt(keys.next())
            if (value is JSONObject) return value
        }
        return emoticon
    }

    private fun collectEmoticonGroups(groups: JSONObject): Map<String, JSONArray> {
        val result = linkedMapOf<String, JSONArray>()
        val keys = groups.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            groups.optJSONArray(key)?.takeIf { it.length() > 0 }?.let { result[key] = it }
        }
        return result
    }

    private fun resolveFlatEmoticonGroups(emoticon: JSONObject?): Map<String, JSONArray>? {
        if (emoticon == null) return null
        val keys = emoticon.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val entries = emoticon.optJSONArray(key) ?: continue
            if (entries.length() > 0 && entries.opt(0) is JSONObject) {
                return mapOf(key to entries)
            }
        }
        return null
    }

    fun parseHotSearch(raw: String): List<HotSearchItem> {
        val root = JSONObject(raw)
        val realtime = root.optJSONObject("data")?.optJSONArray("realtime") ?: JSONArray()
        return buildList {
            for (index in 0 until realtime.length()) {
                val item = realtime.optJSONObject(index) ?: continue
                if (item.optInt("is_ad", 0) == 1) continue
                val word = item.optNullableString("word")?.trim().orEmpty()
                if (word.isBlank()) continue
                add(
                    HotSearchItem(
                        word = word,
                        label = item.optNullableString("labelName")
                            ?: item.optNullableString("iconDesc")
                            ?: item.optNullableString("small_icon_desc")
                            ?: "",
                        heat = item.optLong("num", 0L),
                    ),
                )
            }
        }
    }

    fun parseSearchSuggest(raw: String): SearchSuggestResult {
        val root = JSONObject(raw)
        val data = root.optJSONObject("data") ?: return SearchSuggestResult()
        val hotQueries = buildList {
            val hotquery = data.optJSONArray("hotquery") ?: JSONArray()
            for (index in 0 until hotquery.length()) {
                val item = hotquery.optJSONObject(index) ?: continue
                item.optNullableString("suggestion")?.trim()?.takeIf { it.isNotBlank() }?.let(::add)
            }
        }
        val users = buildList {
            val userArray = data.optJSONArray("users")
                ?: data.optJSONObject("user")?.let { JSONArray().put(it) }
                ?: JSONArray()
            for (index in 0 until userArray.length()) {
                val user = userArray.optJSONObject(index) ?: continue
                val id = userId(user)
                if (id.isBlank()) continue
                add(
                    SearchUserItem(
                        id = id,
                        screenName = user.optNullableString("screen_name") ?: user.optNullableString("name").orEmpty(),
                        name = user.optNullableString("name") ?: user.optNullableString("screen_name").orEmpty(),
                        avatarUrl = user.optNullableString("avatar_large")
                            ?: user.optNullableString("profile_image_url"),
                        description = plainText(user.optNullableString("description").orEmpty()),
                        followersCount = user.optNullableString("followers_count_str")
                            ?: formatCount(user.opt("followers_count")),
                    ),
                )
            }
        }
        return SearchSuggestResult(hotQueries = hotQueries, users = users)
    }

    fun parseMweiboTopicTimeline(raw: String, page: Int = 1): TimelinePage {
        val root = JSONObject(raw)
        if (root.optInt("ok", 0) != 1) {
            val message = root.optNullableString("msg")?.trim().orEmpty()
            throw IllegalStateException(message.ifBlank { "话题搜索失败，请稍后重试" })
        }
        val data = root.optJSONObject("data")
            ?: throw IllegalStateException("话题搜索返回数据为空")
        val cards = data.optJSONArray("cards") ?: JSONArray()
        val cardlistInfo = data.optJSONObject("cardlistInfo")
        val items = buildList {
            for (index in 0 until cards.length()) {
                val card = cards.optJSONObject(index) ?: continue
                collectMweiboCards(card).forEach { mblog ->
                    if (mblog.optInt("isAd", 0) == 1) return@forEach
                    parseStatus(normalizeMweiboStatus(mblog), allowRetweeted = true)?.let(::add)
                }
            }
        }
        val total = cardlistInfo?.optLong("total", 0L) ?: 0L
        val pageSize = cardlistInfo?.opt("page_size")?.toString()?.toLongOrNull() ?: 10L
        val currentPage = cardlistInfo?.optInt("page", page) ?: page
        val hasMore = total > currentPage * pageSize
        return TimelinePage(
            items = items,
            nextCursor = if (hasMore) (currentPage + 1).toString() else null,
        )
    }

    fun parseMweiboSearchTimeline(raw: String, page: Int = 1): TimelinePage =
        parseMweiboTopicTimeline(raw, page)

    fun parseSWeiboSearchTimeline(raw: String, page: Int = 1): TimelinePage {
        val items = buildList {
            val seen = linkedSetOf<String>()
            extractSWeiboHtmlFragments(raw).forEach { fragment ->
                extractSWeiboCards(fragment).forEach { card ->
                    parseSWeiboSearchCard(card)?.let { item ->
                        if (seen.add(item.statusId)) add(item)
                    }
                }
            }
        }
        return TimelinePage(
            items = items,
            nextCursor = if (items.isNotEmpty() && page < 50) (page + 1).toString() else null,
        )
    }

    fun parseSWeiboUserSearch(raw: String, page: Int = 1): SearchUserPage {
        val users = buildList {
            val seen = linkedSetOf<String>()
            extractSWeiboHtmlFragments(raw).forEach { fragment ->
                extractSWeiboUserCards(fragment).forEach { card ->
                    parseSWeiboUserCard(card)?.let { user ->
                        val key = user.id.ifBlank { user.screenName }
                        if (key.isNotBlank() && seen.add(key)) add(user)
                    }
                }
            }
        }
        return SearchUserPage(
            items = users,
            nextCursor = if (users.isNotEmpty() && page < 50) (page + 1).toString() else null,
        )
    }

    private fun extractSWeiboUserCards(html: String): List<String> {
        val starts = Regex(
            """<div\b[^>]*class=['"][^'"]*(?:card-user|usercard|person-card)[^'"]*['"][^>]*>""",
            RegexOption.IGNORE_CASE,
        ).findAll(html).map { it.range.first }.toList()
        if (starts.isEmpty()) {
            return extractSWeiboGenericUserCards(html)
        }
        return starts.mapIndexed { index, start ->
            val end = starts.getOrNull(index + 1) ?: html.length
            html.substring(start, end)
        }
    }

    private fun extractSWeiboGenericUserCards(html: String): List<String> {
        val starts = Regex("""<div\b[^>]*(?:uid=|href=['"](?:https?:)?//weibo\.com/(?:u/)?\d+)""", RegexOption.IGNORE_CASE)
            .findAll(html)
            .map { it.range.first }
            .toList()
        return starts.mapIndexedNotNull { index, start ->
            val end = starts.getOrNull(index + 1) ?: html.length
            html.substring(start, end).takeIf { it.contains("profile_image") || it.contains("avator") || it.contains("avatar") }
        }
    }

    private fun parseSWeiboUserCard(card: String): SearchUserItem? {
        val id = Regex("""(?:uid=|/u/|weibo\.com/u/)(\d+)""", RegexOption.IGNORE_CASE)
            .find(card)
            ?.groupValues
            ?.getOrNull(1)
            ?: Regex("""href=['"](?:https?:)?//weibo\.com/(\d+)""", RegexOption.IGNORE_CASE)
                .find(card)
                ?.groupValues
                ?.getOrNull(1)
            ?: Regex("""\bid=['"]?(\d{5,})""", RegexOption.IGNORE_CASE)
                .find(card)
                ?.groupValues
                ?.getOrNull(1)
            ?: ""
        val nameBlock = Regex(
            """<a\b[^>]*(?:class=['"][^'"]*\bname\b[^'"]*['"]|nick-name=)[^>]*>.*?</a>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
        ).find(card)?.value.orEmpty()
        val name = htmlAttribute(nameBlock, "nick-name")
            ?: htmlAttribute(nameBlock, "title")
            ?: plainText(nameBlock)
        if (id.isBlank() && name.isBlank()) return null
        val avatarUrl = Regex("""<img\b[^>]*(?:src|data-src)=['"]([^'"]+)['"]""", RegexOption.IGNORE_CASE)
            .find(card)
            ?.groupValues
            ?.getOrNull(1)
            ?.let(::htmlDecode)
            ?.let(::normalizeUrl)
        val cardText = plainText(card)
        val description = parseSWeiboUserDescription(card, cardText)
        val followers = parseSWeiboFollowers(cardText)
        val followText = parseSWeiboFollowText(card, cardText)
        return SearchUserItem(
            id = id,
            screenName = name,
            name = name,
            avatarUrl = avatarUrl,
            description = description,
            followersCount = followers,
            followText = followText,
        )
    }

    private fun parseSWeiboUserDescription(card: String, cardText: String): String {
        val labeled = listOf(
            Regex("""(?:简介|个人简介|认证)\s*[:：]\s*(.+?)(?=\s*(?:粉丝|关注|微博|所在地|认证|$))"""),
            Regex("""(?:简介|个人简介|认证)\s+(.+?)(?=\s*(?:粉丝|关注|微博|所在地|认证|$))"""),
        ).firstNotNullOfOrNull { pattern ->
            pattern.find(cardText)?.groupValues?.getOrNull(1)?.trim()
        }
        if (!labeled.isNullOrBlank()) return labeled

        return Regex(
            """<(?:p|div)\b[^>]*class=['"][^'"]*(?:txt|info|desc|s-nobr|person-info)[^'"]*['"][^>]*>(.*?)</(?:p|div)>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
        ).findAll(card)
            .map { plainText(it.groupValues[1]).trim() }
            .firstOrNull { text ->
                text.isNotBlank() &&
                    !text.contains("关注") &&
                    !text.contains("粉丝") &&
                    !text.contains("微博") &&
                    text != "简介"
            }
            .orEmpty()
    }

    private fun parseSWeiboFollowers(cardText: String): String =
        Regex("""粉丝\s*[:：]?\s*([0-9.万亿亿]+)""")
            .find(cardText)
            ?.groupValues
            ?.getOrNull(1)
            ?.takeIf { it.isNotBlank() }
            ?.let { "$it 粉丝" }
            .orEmpty()

    private fun parseSWeiboFollowText(card: String, cardText: String): String {
        val actionText = Regex(
            """<(?:a|button)\b[^>]*(?:class=['"][^'"]*(?:follow|btn|s-btn)[^'"]*['"]|action-type=['"][^'"]*follow[^'"]*['"])[^>]*>(.*?)</(?:a|button)>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
        ).findAll(card)
            .map { plainText(it.groupValues[1]).trim() }
            .firstOrNull { it in setOf("关注", "已关注", "相互关注") }
        if (!actionText.isNullOrBlank()) return actionText

        return when {
            cardText.contains("相互关注") -> "相互关注"
            cardText.contains("已关注") -> "已关注"
            Regex("""(^|\s|\|)关注($|\s|\|)""").containsMatchIn(cardText) -> "关注"
            else -> ""
        }
    }

    private fun extractSWeiboHtmlFragments(raw: String): List<String> {
        val fragments = linkedSetOf(raw)
        Regex("""FM\.view\((\{.*?\})\)""", RegexOption.DOT_MATCHES_ALL)
            .findAll(raw)
            .forEach { match ->
                runCatching {
                    val obj = JSONObject(match.groupValues[1])
                    obj.optNullableString("html")
                }.getOrNull()
                    ?.takeIf { it.looksLikeSWeiboFragment() }
                    ?.let(fragments::add)
            }
        Regex(""""html"\s*:\s*"((?:\\.|[^"\\])*)"""", RegexOption.DOT_MATCHES_ALL)
            .findAll(raw)
            .forEach { match ->
                runCatching {
                    JSONObject("""{"html":"${match.groupValues[1]}"}""").getString("html")
                }.getOrNull()
                    ?.takeIf { it.looksLikeSWeiboFragment() }
                    ?.let(fragments::add)
            }
        return fragments.toList()
    }

    private fun String.looksLikeSWeiboFragment(): Boolean =
        contains("feed_list_item") ||
            contains("pl_feedlist_index") ||
            contains("card-wrap") ||
            contains("card-user") ||
            contains("person-card") ||
            contains("usercard") ||
            contains("pl_user_feedList")

    private fun extractSWeiboCards(html: String): List<String> {
        val starts = Regex(
            """<div\b[^>]*class=['"][^'"]*\bcard-wrap\b[^'"]*['"][^>]*>""",
            RegexOption.IGNORE_CASE,
        ).findAll(html).map { it.range.first }.toList()
        if (starts.isEmpty()) return emptyList()
        return starts.mapIndexedNotNull { index, start ->
            val end = starts.getOrNull(index + 1) ?: html.length
            html.substring(start, end)
                .takeIf { it.contains("feed_list_item") || it.contains(Regex("""\bmid=['"]\d+""")) }
        }
    }

    private fun parseSWeiboSearchCard(card: String): FeedItem? {
        val firstTag = Regex("""<div\b[^>]*>""", RegexOption.IGNORE_CASE)
            .find(card)
            ?.value
            .orEmpty()
        val hrefPair = Regex("""href=['"](?:https?:)?//weibo\.com/(\d+)/([A-Za-z0-9]+)""")
            .find(card)
        val mid = htmlAttribute(firstTag, "mid")
            ?: Regex("""\bmid=['"](\d+)['"]""").find(card)?.groupValues?.getOrNull(1)
            ?: hrefPair?.groupValues?.getOrNull(2)
            ?: return null
        val statusId = hrefPair?.groupValues?.getOrNull(2) ?: mid
        val authorId = hrefPair?.groupValues?.getOrNull(1)
            ?: Regex("""(?:/u/|uid=)(\d+)""").find(card)?.groupValues?.getOrNull(1)
            ?: ""
        val authorBlock = Regex(
            """<a\b[^>]*class=['"][^'"]*\bname\b[^'"]*['"][^>]*>.*?</a>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
        ).find(card)?.value.orEmpty()
        val authorName = htmlAttribute(authorBlock, "nick-name")
            ?: plainText(authorBlock).ifBlank { "微博用户" }
        val avatarUrl = Regex(
            """<div\b[^>]*class=['"][^'"]*\bavator\b[^'"]*['"][\s\S]*?<img\b[^>]*src=['"]([^'"]+)['"]""",
            RegexOption.IGNORE_CASE,
        ).find(card)?.groupValues?.getOrNull(1)?.let(::normalizeUrl)
        val fromBlock = Regex(
            """<(?:p|div)\b[^>]*class=['"][^'"]*\bfrom\b[^'"]*['"][^>]*>(.*?)</(?:p|div)>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
        ).find(card)?.groupValues?.getOrNull(1).orEmpty()
        val fromMeta = parseSWeiboFromMetadata(fromBlock)
        val actionCounts = parseSWeiboActionCounts(card)
        val fullTextHtml = Regex(
            """<p\b[^>]*node-type=['"]feed_list_content_full['"][^>]*>(.*?)</p>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
        ).find(card)?.groupValues?.getOrNull(1)
        val shortTextHtml = Regex(
            """<p\b[^>]*node-type=['"]feed_list_content['"][^>]*>(.*?)</p>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
        ).find(card)?.groupValues?.getOrNull(1)
        val textHtml = fullTextHtml ?: shortTextHtml.orEmpty()
        val images = parseSWeiboSearchImages(card, avatarUrl, statusId)
        return FeedItem(
            id = mid,
            statusId = statusId,
            authorId = authorId,
            authorName = authorName,
            authorAvatarUrl = avatarUrl,
            createdAt = fromMeta.createdAt,
            source = fromMeta.source,
            ipLocation = fromMeta.ipLocation,
            text = plainText(textHtml)
                .replace(Regex("""\s*展开\s*$"""), "")
                .trim(),
            isLongText = fullTextHtml == null && card.contains("feed_list_content_full"),
            emoticons = extractEmoticonsFromHtml(textHtml),
            repostsCount = actionCounts.first,
            commentsCount = actionCounts.second,
            likesCount = actionCounts.third,
            images = images,
        )
    }

    private fun parseSWeiboActionCounts(card: String): Triple<String, String, String> {
        val actionBlock = extractSWeiboCardActBlock(card)
        if (actionBlock.isBlank()) {
            return Triple(
                parseSWeiboActionCount(card, "转发"),
                parseSWeiboActionCount(card, "评论"),
                parseSWeiboActionCount(card, "赞"),
            )
        }
        val liBlocks = Regex(
            """<li\b[^>]*>(.*?)</li>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
        ).findAll(actionBlock).map { it.groupValues[1] }.toList()
        if (liBlocks.isEmpty()) {
            return Triple(
                parseSWeiboActionCount(actionBlock, "转发"),
                parseSWeiboActionCount(actionBlock, "评论"),
                parseSWeiboActionCount(actionBlock, "赞"),
            )
        }
        return Triple(
            parseSWeiboLiActionCount(liBlocks.getOrNull(0).orEmpty(), "转发"),
            parseSWeiboLiActionCount(liBlocks.getOrNull(1).orEmpty(), "评论"),
            parseSWeiboLiActionCount(liBlocks.getOrNull(2).orEmpty(), "赞"),
        )
    }

    private fun extractSWeiboCardActBlock(card: String): String {
        val start = Regex(
            """<div\b[^>]*class=['"][^'"]*\bcard-act\b[^'"]*['"]""",
            RegexOption.IGNORE_CASE,
        ).find(card)?.range?.first ?: return ""
        val nextCard = Regex(
            """<div\b[^>]*class=['"][^'"]*\bcard-wrap\b[^'"]*['"]""",
            RegexOption.IGNORE_CASE,
        ).find(card, startIndex = start + 1)?.range?.first ?: card.length
        return card.substring(start, nextCard)
    }

    private fun parseSWeiboLiActionCount(liBlock: String, label: String): String {
        if (liBlock.isBlank()) return "0"
        Regex(
            """<em\b[^>]*>(.*?)</em>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
        ).find(liBlock)?.groupValues?.getOrNull(1)?.let(::plainText)?.trim()?.let { count ->
            if (count.isNotBlank() && count.any { it.isDigit() }) {
                return count
            }
        }
        val text = plainText(liBlock).replace(label, "").trim()
        Regex("""([0-9]+(?:\.[0-9]+)?万?)""").find(text)?.groupValues?.getOrNull(1)?.let { return it }
        return "0"
    }

    private fun parseSWeiboSearchImages(card: String, avatarUrl: String?, statusId: String): List<FeedImage> {
        val mediaStart = card.indexOf("class=\"media\"").takeIf { it >= 0 }
            ?: card.indexOf("class='media'").takeIf { it >= 0 }
            ?: return emptyList()
        val actionStart = card.indexOf("class=\"card-act\"", startIndex = mediaStart)
            .takeIf { it > mediaStart }
            ?: card.indexOf("class='card-act'", startIndex = mediaStart)
                .takeIf { it > mediaStart }
            ?: card.length
        val mediaBlock = card.substring(mediaStart, actionStart)
        val urls = linkedSetOf<String>()
        Regex("""(?:src|action-data)=['"]([^'"]+)['"]""", RegexOption.IGNORE_CASE)
            .findAll(mediaBlock)
            .forEach { match ->
                val value = htmlDecode(match.groupValues[1])
                Regex("""(?:pic_src|src)=([^&]+)""").find(value)
                    ?.groupValues
                    ?.getOrNull(1)
                    ?.let { runCatching { URLDecoder.decode(it, Charsets.UTF_8.name()) }.getOrDefault(it) }
                    ?.let(urls::add)
                if (value.contains("sinaimg.cn")) urls.add(value)
            }
        return urls
            .map(::normalizeUrl)
            .filter { url ->
                url.contains("sinaimg.cn") &&
                    url != avatarUrl &&
                    !url.contains("/face/") &&
                    !url.contains("default_avatar")
            }
            .distinct()
            .mapIndexed { index, url ->
                val large = url.replace(
                    Regex("""/(?:thumb\d+|orj\d+|mw\d+|bmiddle|small|thumbnail)/"""),
                    "/large/",
                )
                FeedImage(
                    id = url.substringAfterLast("/").substringBefore(".").ifBlank { "$statusId-$index" },
                    thumbnailUrl = url,
                    largeUrl = large,
                    downloadUrls = listOf(large, url).distinct(),
                    statusId = statusId,
                )
            }
    }

    private fun parseSWeiboFromMetadata(fromBlock: String): SWeiboFromMeta {
        if (fromBlock.isBlank()) return SWeiboFromMeta(null, null, null)
        val plainFrom = plainText(fromBlock)
        val firstAnchor = Regex(
            """<a\b([^>]*)>(.*?)</a>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
        ).find(fromBlock)
        val createdAt = firstAnchor?.groupValues?.getOrNull(1)?.let { attrs ->
            htmlAttribute("<a $attrs>", "title")
        }?.takeIf { it.isNotBlank() }
            ?: firstAnchor?.groupValues?.getOrNull(2)?.let(::plainText)?.takeIf { it.isNotBlank() }

        val source = Regex(
            """来自\s*(?:&nbsp;|\s)*<a\b[^>]*>(.*?)</a>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
        ).find(fromBlock)?.groupValues?.getOrNull(1)?.let(::plainText)?.trim()
            ?.removePrefix("来自")
            ?.trim()
            ?: Regex("""来自\s*([^<\n]+)""").find(plainFrom)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }
                ?.removePrefix("来自")
                ?.trim()

        val ipLocation = Regex(
            """<span\b[^>]*\blocation\b[^>]*>(.*?)</span>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
        ).find(fromBlock)?.groupValues?.getOrNull(1)?.let(::plainText)?.trim()
            ?.removePrefix("发布于")
            ?.trim()
            ?: Regex("""发布于\s*([^<\n]+)""").find(plainFrom)?.groupValues?.getOrNull(1)?.trim()
            ?: Regex("""IP属地[：:]\s*([^<\n]+)""").find(plainFrom)?.groupValues?.getOrNull(1)?.trim()

        return SWeiboFromMeta(
            createdAt = createdAt,
            source = source,
            ipLocation = ipLocation?.takeIf { it.isNotBlank() },
        )
    }

    private data class SWeiboFromMeta(
        val createdAt: String?,
        val source: String?,
        val ipLocation: String?,
    )

    private fun parseSWeiboActionCount(card: String, label: String): String {
        val text = plainText(card)
        Regex("""$label\s*([0-9]+(?:\.[0-9]+)?万?)""").find(text)?.groupValues?.getOrNull(1)
            ?.takeIf { it.isNotBlank() }
            ?.let { return it }
        Regex("""$label\s*([0-9万.]+)?""").find(text)?.groupValues?.getOrNull(1)
            ?.takeIf { it.isNotBlank() }
            ?.let { return it }
        return "0"
    }

    private fun htmlAttribute(tag: String, name: String): String? =
        Regex("""\b${Regex.escape(name)}=['"]([^'"]*)['"]""", RegexOption.IGNORE_CASE)
            .find(tag)
            ?.groupValues
            ?.getOrNull(1)
            ?.let(::htmlDecode)
            ?.takeIf { it.isNotBlank() }

    private fun htmlDecode(value: String): String =
        value
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#34;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")

    private fun collectMweiboCards(card: JSONObject): List<JSONObject> {
        return when (card.optInt("card_type", -1)) {
            9 -> listOfNotNull(card.optJSONObject("mblog"))
            11 -> buildList {
                val group = card.optJSONArray("card_group") ?: return@buildList
                for (index in 0 until group.length()) {
                    val sub = group.optJSONObject(index) ?: continue
                    if (sub.optInt("card_type", -1) == 9) {
                        sub.optJSONObject("mblog")?.let(::add)
                    }
                }
            }
            else -> emptyList()
        }
    }

    private fun normalizeMweiboStatus(mblog: JSONObject): JSONObject {
        val normalized = JSONObject(mblog.toString())
        if (normalized.optNullableString("idstr").isNullOrBlank()) {
            normalized.put("idstr", normalized.opt("id")?.toString().orEmpty())
        }
        val pageInfo = normalized.optJSONObject("page_info")
        if (pageInfo != null && pageInfo.optNullableString("object_type").isNullOrBlank()) {
            when (pageInfo.optNullableString("type")) {
                "video" -> pageInfo.put("object_type", "video")
                "live" -> pageInfo.put("object_type", "live")
            }
        }
        if (normalized.optJSONArray("pic_ids") == null) {
            val pics = normalized.optJSONArray("pics") ?: return normalized
            val picIds = JSONArray()
            val picInfos = JSONObject()
            for (index in 0 until pics.length()) {
                val pic = pics.optJSONObject(index) ?: continue
                val pid = pic.optNullableString("pid") ?: continue
                picIds.put(pid)
                picInfos.put(
                    pid,
                    JSONObject()
                        .put("thumbnail", JSONObject().put("url", pic.optNullableString("url").orEmpty()))
                        .put(
                            "large",
                            JSONObject().put(
                                "url",
                                pic.optJSONObject("large")?.optNullableString("url")
                                    ?: pic.optNullableString("url").orEmpty(),
                            ),
                        ),
                )
            }
            if (picIds.length() > 0) {
                normalized.put("pic_ids", picIds)
                normalized.put("pic_infos", picInfos)
            }
        }
        return normalized
    }
}
