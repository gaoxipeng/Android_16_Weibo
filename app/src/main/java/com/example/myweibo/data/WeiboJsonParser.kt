package com.example.myweibo.data

import org.json.JSONArray
import org.json.JSONObject

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
                add(
                    CommentItem(
                        id = id,
                        authorName = user?.optNullableString("screen_name") ?: "微博用户",
                        authorAvatarUrl = user?.optNullableString("avatar_hd")
                            ?: user?.optNullableString("profile_image_url"),
                        text = plainText(
                            comment.optNullableString("text_raw")
                                ?: htmlText
                        ),
                        createdAt = comment.optNullableString("created_at"),
                        likesCount = formatCount(comment.opt("like_counts")),
                        ipLocation = parseIpLocation(comment),
                        emoticons = extractEmoticonsFromHtml(htmlText),
                        comments = parseNestedComments(comment.optJSONArray("comments")),
                        replyToAuthor = comment.optJSONObject("reply_comment")?.optJSONObject("user")
                            ?.optNullableString("screen_name"),
                    )
                )
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
                add(
                    CommentItem(
                        id = id,
                        authorName = user?.optNullableString("screen_name") ?: "微博用户",
                        authorAvatarUrl = user?.optNullableString("avatar_hd")
                            ?: user?.optNullableString("profile_image_url"),
                        text = plainText(obj.optNullableString("text_raw") ?: htmlText),
                        createdAt = obj.optNullableString("created_at"),
                        likesCount = formatCount(obj.opt("like_counts")),
                        ipLocation = parseIpLocation(obj),
                        emoticons = extractEmoticonsFromHtml(htmlText),
                        replyToAuthor = obj.optJSONObject("reply_comment")?.optJSONObject("user")
                            ?.optNullableString("screen_name"),
                    )
                )
            }
        }
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
            verifiedReason = user.optNullableString("verified_reason"),
            followingCount = formatCount(
                user.opt("friends_count")
                    ?: user.opt("follow_count")
                    ?: user.opt("following_count")
            ),
            followersCount = formatCount(user.opt("followers_count")),
            statusesCount = formatCount(user.opt("statuses_count")),
            photosCount = user.opt("photos_count")?.let(::formatCount),
            coverUrl = user.optNullableString("cover_image_phone")
                ?: user.optNullableString("cover_image"),
        )
    }

    fun parseAlbumImages(raw: String): List<FeedImage> = parseAlbumPage(raw).images

    fun parseAlbumPage(raw: String): AlbumPage {
        val root = JSONObject(raw)
        val found = linkedMapOf<String, FeedImage>()

        fun addImage(id: String, thumbnail: String?, large: String?, createdAt: String?) {
            val resolved = large ?: thumbnail ?: return
            if (!looksLikeImageUrl(resolved)) return
            val normalizedLarge = normalizeUrl(resolved)
            val normalizedThumb = normalizeUrl(thumbnail ?: resolved)
            found.putIfAbsent(
                normalizedLarge,
                FeedImage(
                    id = id.ifBlank { normalizedLarge },
                    thumbnailUrl = normalizedThumb,
                    largeUrl = normalizedLarge,
                    downloadUrls = listOf(normalizedLarge).distinct(),
                    createdAt = createdAt,
                )
            )
        }

        fun visit(value: Any?) {
            when (value) {
                is JSONObject -> {
                    val id = value.optNullableString("pic_id")
                        ?: value.optNullableString("pid")
                        ?: value.optNullableString("id")
                        ?: ""
                    val thumbnail = imageUrl(value, "thumbnail")
                        ?: imageUrl(value, "bmiddle")
                        ?: value.optNullableString("thumbnail")
                        ?: value.optNullableString("thumbnail_pic")
                        ?: value.optNullableString("pic")
                    val large = imageUrl(value, "largest")
                        ?: imageUrl(value, "mw2000")
                        ?: imageUrl(value, "woriginal")
                        ?: imageUrl(value, "original")
                        ?: imageUrl(value, "large")
                        ?: value.optNullableString("large")
                        ?: value.optNullableString("url")
                    val createdAt = value.optNullableString("created_at")
                        ?: value.optNullableString("created")
                        ?: value.optNullableString("time")
                        ?: value.optNullableString("date")
                    addImage(id, thumbnail, large, createdAt)

                    val keys = value.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        if (key == "retweeted_status" || key == "retweeted_statuses") continue
                        visit(value.opt(key))
                    }
                }

                is JSONArray -> {
                    for (index in 0 until value.length()) {
                        visit(value.opt(index))
                    }
                }
            }
        }

        visit(root)
        val data = root.optJSONObject("data")
        val nextCursor = data?.optNullableString("sinceid")
            ?: data?.optNullableString("since_id")
            ?: data?.optNullableString("next_since_id")
            ?: data?.optNullableString("next_cursor")
            ?: root.optNullableString("sinceid")
            ?: root.optNullableString("since_id")
            ?: root.optNullableString("next_since_id")
            ?: root.optNullableString("next_cursor")
        return AlbumPage(images = found.values.toList(), nextCursor = nextCursor)
    }

    private fun parseStatus(status: JSONObject, allowRetweeted: Boolean): FeedItem? {
        val id = status.optNullableString("idstr")
            ?: status.optNullableString("mid")
            ?: status.optNullableString("id")
            ?: return null
        val user = status.optJSONObject("user")
        val authorId = user?.optNullableString("idstr")
            ?: user?.optNullableString("id")
            ?: ""
        val media = parseMedia(status)
        val images = parseImages(status).map {
            it.copy(createdAt = it.createdAt ?: status.optNullableString("created_at"))
        }
        val retweeted = if (allowRetweeted) {
            status.optJSONObject("retweeted_status")?.takeUnless { it.looksLikeAd() }?.let {
                parseStatus(it, allowRetweeted = false)
            }
        } else {
            null
        }

        return FeedItem(
            id = id,
            statusId = status.optNullableString("mblogid") ?: id,
            authorId = authorId,
            authorName = user?.optNullableString("screen_name") ?: "微博用户",
            authorAvatarUrl = user?.optNullableString("avatar_hd")
                ?: user?.optNullableString("profile_image_url"),
            createdAt = status.optNullableString("created_at"),
            source = plainText(status.optNullableString("source") ?: ""),
            ipLocation = parseIpLocation(status),
            text = plainText(
                status.optNullableString("text_raw")
                    ?: status.optNullableString("raw_text")
                    ?: status.optNullableString("text")
                    ?: ""
            ),
            emoticons = extractEmoticonsFromHtml(status.optNullableString("text") ?: ""),
            repostsCount = formatCount(status.opt("reposts_count")),
            commentsCount = formatCount(status.opt("comments_count")),
            likesCount = formatCount(status.opt("attitudes_count")),
            images = images,
            media = media,
            retweetedStatus = retweeted,
        )
    }

    private fun parseImages(status: JSONObject): List<FeedImage> {
        val fromStatus = imagesFromParts(status.optJSONArray("pic_ids"), status.optJSONObject("pic_infos"))
        val fromUrlStruct = buildList {
            val urlStruct = status.optJSONArray("url_struct") ?: return@buildList
            for (index in 0 until urlStruct.length()) {
                val entity = urlStruct.optJSONObject(index) ?: continue
                addAll(imagesFromParts(entity.optJSONArray("pic_ids"), entity.optJSONObject("pic_infos")))
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
                val thumbnail = imageUrl(info, "large")
                    ?: imageUrl(info, "bmiddle")
                    ?: imageUrl(info, "thumbnail")
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
                add(
                    FeedImage(
                        id = picId,
                        thumbnailUrl = normalizeUrl(thumbnail),
                        largeUrl = normalizeUrl(large),
                        downloadUrls = downloadUrls.map(::normalizeUrl),
                        livePhotoVideoUrl = info.optNullableString("video")
                            ?: info.optNullableString("video_hd"),
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
                val thumbnail = imageUrl(data, "large")
                    ?: imageUrl(data, "bmiddle")
                    ?: imageUrl(data, "thumbnail")
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

    private fun parseMedia(status: JSONObject): FeedMedia? {
        val pageInfo = status.optJSONObject("page_info") ?: return parseMixVideo(status)
        val mediaInfo = pageInfo.optJSONObject("media_info") ?: return parseMixVideo(status)
        val type = when (pageInfo.optNullableString("object_type")) {
            "live" -> MediaType.Live
            "audio" -> MediaType.Audio
            else -> MediaType.Video
        }
        val streamUrl = when (type) {
            MediaType.Live -> mediaInfo.optNullableString("live_ld")
                ?: mediaInfo.optNullableString("stream_url")
            else -> progressiveVideoUrl(mediaInfo)
        }?.let(::normalizeMediaUrl) ?: return parseMixVideo(status)
        val cover = pageInfo.optNullableString("page_pic")
            ?: mediaInfo.optJSONObject("big_pic_info")?.optJSONObject("pic_big")?.optNullableString("url")
            ?: mediaInfo.optJSONObject("subscribe")?.optNullableString("cover")
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
        )
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
            return FeedMedia(
                type = MediaType.Video,
                title = mediaInfo.optNullableString("video_title")
                    ?: data.optNullableString("content1")
                    ?: "微博视频",
                coverUrl = cover?.let(::normalizeUrl),
                streamUrl = streamUrl,
                downloadUrl = downloadVideoUrl(mediaInfo)?.let(::normalizeMediaUrl),
            )
        }
        return null
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

    private fun extractEmoticonsFromHtml(html: String): Map<String, String> {
        if (html.isBlank()) return emptyMap()
        val map = mutableMapOf<String, String>()
        val pattern = Regex("""<img\b[^>]*\balt="(\[[^"\]]+\])"[^>]*\bsrc="([^"]+)"[^>]*>""")
        pattern.findAll(html).forEach { match ->
            val phrase = match.groupValues[1]
            val url = match.groupValues[2]
            if (url.startsWith("//")) {
                map[phrase] = "https:$url"
            } else {
                map[phrase] = url
            }
        }
        return map
    }

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
        val emoticons = root.optJSONObject("data")?.optJSONArray("emoticons")
            ?: return emptyList()
        return buildList {
            for (i in 0 until emoticons.length()) {
                val obj = emoticons.optJSONObject(i) ?: continue
                val phrase = obj.optNullableString("phrase") ?: continue
                var url = obj.optNullableString("url")
                    ?: obj.optNullableString("icon")
                    ?: continue
                if (url.startsWith("//")) url = "https:$url"
                add(WeiboEmoticon(phrase = phrase, url = url))
            }
        }
    }
}
