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
        return RepostsPage(items = items, nextPage = nextPage)
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
        media: FeedMedia?,
        inlineImageLinks: Map<String, List<FeedImage>> = emptyMap(),
        urlEntities: List<FeedUrlEntity> = emptyList(),
    ): String {
        val preservedUrls = inlineImageLinks.keys + urlEntities.map { it.shortUrl }.toSet()
        var text = plainText(sourceRaw)
        val urlStruct = status.optJSONArray("url_struct")
        val imageTokens = imageUrlTokensFromUrlStruct(urlStruct, sourceRaw)
            .filter { it !in preservedUrls }
        text = stripEntityTokens(text, imageTokens)
        if (media != null) {
            val mediaTokens = mediaUrlTokensFromUrlStruct(urlStruct, sourceRaw, preservedUrls) +
                mediaPageInfoTokens(status, sourceRaw)
            text = stripEntityTokens(text, mediaTokens)
        }
        if ((images.isNotEmpty() || media != null) && inlineImageLinks.isEmpty() && urlEntities.isEmpty()) {
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
        }
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

    private fun parseCommentImages(comment: JSONObject): List<FeedImage> {
        val direct = parseImages(comment)
        if (direct.isNotEmpty()) return direct

        val seen = mutableSetOf<String>()
        val urlStruct = comment.optJSONArray("url_struct") ?: return emptyList()
        return buildList {
            for (index in 0 until urlStruct.length()) {
                val entity = urlStruct.optJSONObject(index) ?: continue
                imagesFromParts(entity.optJSONArray("pic_ids"), entity.optJSONObject("pic_infos"))
                    .forEach { image ->
                        if (seen.add(image.id)) add(image)
                    }
            }
        }
    }

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
        }
    }

    fun hasLongTextPayload(data: JSONObject): Boolean =
        listOf("longTextContent", "longTextContent_raw", "text", "text_raw", "raw_text")
            .any { data.optNullableString(it)?.isNotBlank() == true }

    fun mergeLongTextIntoFeedItem(item: FeedItem, data: JSONObject): FeedItem {
        val contentHtml = data.optNullableString("longTextContent")
            ?: data.optNullableString("text")
            ?: ""
        val contentRaw = data.optNullableString("longTextContent_raw")
            ?: data.optNullableString("text_raw")
            ?: data.optNullableString("raw_text")
            ?: contentHtml
        if (!hasLongTextPayload(data)) return item

        val sourceForParse = stripLongTextPreviewLabel(
            contentRaw.takeIf { it.isNotBlank() } ?: contentHtml,
        )
        val inlineImageLinks = parseInlineImageLinks(data, sourceForParse)
        val urlEntities = parseUrlEntities(data, sourceForParse, inlineImageLinks.keys)
        val additionalImages = parseImages(data, inlineImageLinks.keys).map {
            it.copy(createdAt = it.createdAt ?: item.createdAt)
        }
        val mergedImages = (item.images + additionalImages).distinctBy { it.largeUrl }
        val mergedInlineImageLinks = item.inlineImageLinks + inlineImageLinks
        val mergedUrlEntities = (item.urlEntities + urlEntities).distinctBy { it.shortUrl }
        // 长文合并只依据接口返回的正文与媒体，不复用卡片上的 video 等媒体字段，避免转发场景误删正文。
        var text = parseStatusText(
            data,
            sourceForParse,
            mergedImages,
            media = null,
            mergedInlineImageLinks,
            mergedUrlEntities,
        )
        if (text.isBlank()) {
            text = plainText(sourceForParse)
        }
        if (text.isBlank()) {
            text = sourceForParse.trim()
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
                emoticons = mergedEmoticons,
                images = mergedImages,
                inlineImageLinks = mergedInlineImageLinks,
                urlEntities = mergedUrlEntities,
            )
        }
        return item.copy(
            isLongText = false,
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
            else -> value.toString().trim().takeIf { it.isNotBlank() && it != "-1" && it != "0" }
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
            type = imgType,
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
            else -> value.toString().trim().takeIf { it.isNotBlank() && it != "0" }
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
        val media = if (mediaBelongsToRetweeted) null else parseMedia(status)

        val htmlText = status.optNullableString("text") ?: ""
        val rawText = status.optNullableString("text_raw")
            ?: status.optNullableString("raw_text")
            ?: htmlText
        val isLongText = status.isLongTextPreview()
        val displayText = if (isLongText) stripLongTextPreviewLabel(rawText) else rawText
        val inlineImageLinks = parseInlineImageLinks(status, displayText)
        val urlEntities = parseUrlEntities(status, displayText, inlineImageLinks.keys)

        val images = if (mediaBelongsToRetweeted) {
            emptyList()
        } else {
            parseImages(status, inlineImageLinks.keys).map {
                it.copy(createdAt = it.createdAt ?: status.optNullableString("created_at"))
            }
        }
        val retweeted = retweetedJson?.let { json ->
            val normalized = normalizeRetweetedStatus(status, json)
            parseStatus(normalized, allowRetweeted = false)
        }
        val resolvedMedia = when {
            mediaBelongsToRetweeted -> null
            retweeted?.media != null && media != null -> null
            else -> media
        }

        val (isEdited, editCount) = status.parseEditMetadata()
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
            text = parseStatusText(status, displayText, images, resolvedMedia, inlineImageLinks, urlEntities),
            isLongText = isLongText,
            emoticons = extractEmoticonsFromHtml(htmlText),
            repostsCount = formatCount(status.opt("reposts_count")),
            commentsCount = formatCount(status.opt("comments_count")),
            likesCount = formatCount(status.opt("attitudes_count")),
            liked = status.optBoolean("attitudes_status"),
            images = images,
            media = resolvedMedia,
            inlineImageLinks = inlineImageLinks,
            urlEntities = urlEntities,
            retweetedStatus = retweeted,
            isEdited = isEdited,
            editCount = editCount,
        )
        val embeddedLongText = status.optJSONObject("longText")
            ?.takeIf { hasLongTextPayload(it) }
        return embeddedLongText?.let { mergeLongTextIntoFeedItem(item, it) } ?: item
    }

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
        return (fromStatus + fromUrlStruct + fromMixMedia).distinctBy { it.largeUrl }
    }

    private fun imagesFromParts(picIds: JSONArray?, picInfos: JSONObject?): List<FeedImage> {
        if (picIds == null || picInfos == null) return emptyList()
        return buildList {
            for (index in 0 until picIds.length()) {
                val picId = picIds.optString(index).takeIf { it.isNotBlank() } ?: continue
                val info = picInfos.optJSONObject(picId) ?: continue
                val thumbnail = imageUrl(info, "bmiddle")
                    ?: imageUrl(info, "orj360")
                    ?: imageUrl(info, "thumbnail")
                    ?: imageUrl(info, "large")
                    ?: continue
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
                val imgType = info.optNullableString("type")
                val isLive = imgType == "livephoto"
                val isGif = imgType == "gif"
                add(
                    FeedImage(
                        id = picId,
                        thumbnailUrl = normalizeUrl(thumbnail),
                        largeUrl = normalizeUrl(large),
                        downloadUrls = downloadUrls.map(::normalizeUrl),
                        livePhotoVideoUrl = if (isLive || isGif)
                            (info.optNullableString("video") ?: info.optNullableString("video_hd"))
                        else null,
                        type = imgType,
                        width = largeInfo?.optInt("width")?.takeIf { it > 0 },
                        height = largeInfo?.optInt("height")?.takeIf { it > 0 },
                    )
                )
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
                val thumbnail = imageUrl(data, "bmiddle")
                    ?: imageUrl(data, "orj360")
                    ?: imageUrl(data, "thumbnail")
                    ?: imageUrl(data, "large")
                    ?: continue
                val largeInfo = data.optJSONObject("largest")
                    ?: data.optJSONObject("mw2000")
                    ?: data.optJSONObject("original")
                    ?: data.optJSONObject("large")
                val large = largeInfo?.optNullableString("url") ?: imageUrl(data, "large") ?: thumbnail
                add(
                    FeedImage(
                        id = large,
                        thumbnailUrl = normalizeUrl(thumbnail),
                        largeUrl = normalizeUrl(large),
                        downloadUrls = listOfNotNull(
                            imageUrl(data, "largest"),
                            imageUrl(data, "mw2000"),
                            imageUrl(data, "original"),
                            imageUrl(data, "large"),
                            imageUrl(data, "bmiddle"),
                            imageUrl(data, "thumbnail"),
                        ).distinct().map(::normalizeUrl),
                        width = largeInfo?.optInt("width")?.takeIf { it > 0 },
                        height = largeInfo?.optInt("height")?.takeIf { it > 0 },
                    )
                )
            }
        }
    }

    private fun parseVideoOrientation(mediaInfo: JSONObject): String? =
        mediaInfo.optNullableString("video_orientation")?.trim()?.takeIf { it.isNotBlank() }

    private fun parseMediaCoverDimensions(pageInfo: JSONObject, mediaInfo: JSONObject): Pair<Int?, Int?> {
        val bigPic = mediaInfo.optJSONObject("big_pic_info")?.optJSONObject("pic_big")
        val width = bigPic?.optInt("width")?.takeIf { it > 0 }
        val height = bigPic?.optInt("height")?.takeIf { it > 0 }
        if (width != null && height != null) return width to height
        return null to null
    }

    private fun parseMedia(status: JSONObject): FeedMedia? {
        val pageInfo = status.optJSONObject("page_info") ?: return parseMixVideo(status)
        val mediaInfo = pageInfo.optJSONObject("media_info") ?: return parseMixVideo(status)
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
                return parseMixVideo(status)
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
        val streamUrl = progressiveVideoUrl(mediaInfo)?.let(::normalizeMediaUrl) ?: return parseMixVideo(status)
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

    private fun parseMixVideo(status: JSONObject): FeedMedia? {
        val items = status.optJSONObject("mix_media_info")?.optJSONArray("items") ?: return null
        for (index in 0 until items.length()) {
            val item = items.optJSONObject(index) ?: continue
            if (item.optNullableString("type") != "video") continue
            val data = item.optJSONObject("data") ?: continue
            val mediaInfo = data.optJSONObject("media_info") ?: continue
            val streamUrl = progressiveVideoUrl(mediaInfo)?.let(::normalizeMediaUrl) ?: continue
            val cover = data.optNullableString("page_pic")
                ?: mediaInfo.optJSONObject("big_pic_info")?.optJSONObject("pic_big")?.optNullableString("url")
            val (coverWidth, coverHeight) = parseMediaCoverDimensions(data, mediaInfo)
            return FeedMedia(
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
            )
        }
        return null
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
        for (index in 0 until outerStruct.length()) {
            val entity = outerStruct.optJSONObject(index) ?: continue
            val shortUrl = entity.optNullableString("short_url") ?: continue
            if (shortUrl in existingShortUrls) continue
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
            else -> value.toString().trim() in setOf("1", "true", "TRUE")
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

    private fun JSONObject.optIntOrNull(name: String): Int? {
        if (!has(name) || isNull(name)) return null
        return when (val value = opt(name)) {
            is Number -> value.toInt()
            else -> value.toString().trim().toIntOrNull()
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
        optBoolean("isLongText") && !(has("longText") && !isNull("longText"))

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
                    """(?:\.{3}|…|⋯)\s*(?:<a\b[^>]*>\s*)?全文\s*(?:</a>)?(?=\s*//[@＠])""",
                    RegexOption.IGNORE_CASE,
                ),
                "",
            )
            .replace(Regex("""(?:\.{3}|…|⋯)?\s*全文(?=\s*//[@＠])"""), "")
            .replace(
                Regex(
                    """((?:\.{3}|…|⋯)\s*)(?:<a\b[^>]*>\s*)?全文\s*(?:</a>)?\s*$""",
                    RegexOption.IGNORE_CASE,
                ),
                "$1",
            )
            .replace(Regex("""(?<=\S)(?:\.{3}|…|⋯)?\s*全文\s*$"""), "")
            .trimEnd()

    private fun plainText(value: String): String =
        value
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<[^>]+>"), "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .trim()

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
            """<p\b[^>]*class=['"][^'"]*\bfrom\b[^'"]*['"][^>]*>(.*?)</p>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
        ).find(card)?.groupValues?.getOrNull(1).orEmpty()
        val fromMeta = parseSWeiboFromMetadata(fromBlock)
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
            repostsCount = parseSWeiboActionCount(card, "转发"),
            commentsCount = parseSWeiboActionCount(card, "评论"),
            likesCount = parseSWeiboActionCount(card, "赞"),
            images = images,
            media = null,
        )
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
        val match = Regex("""$label\s*([0-9万.]+)?""").find(text)
        return match?.groupValues?.getOrNull(1)?.takeIf { it.isNotBlank() } ?: "0"
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
            .replace("&#39;", "'")
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
