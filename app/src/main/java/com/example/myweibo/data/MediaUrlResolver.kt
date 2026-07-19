package com.example.myweibo.data

object MediaUrlResolver {
    private const val PLAYBACK_URL_EXPIRY_SKEW_MS = 60_000L

    fun playbackUrlCandidates(url: String): List<String> =
        protocolCandidates(url, allowDataUri = true)

    fun downloadUrlCandidates(url: String): List<String> =
        protocolCandidates(url, allowDataUri = false)

    fun livePhotoVideoCandidates(url: String): List<String> =
        protocolCandidates(url, allowDataUri = false)

    fun downloadableVideoCandidates(media: FeedMedia): List<String> =
        listOfNotNull(media.downloadUrl, media.streamUrl)
            .flatMap(::downloadUrlCandidates)
            .filter { it.isNotBlank() && !it.contains(".m3u8", ignoreCase = true) }
            .distinct()

    fun pipPlaybackCandidates(
        streamUrl: String,
        downloadUrl: String?,
        replayUrl: String?,
        liveStatus: Int?,
        dashDataUri: String? = null,
    ): List<String> {
        val primaryUrl = when (liveStatus) {
            3 -> replayUrl?.takeIf { it.isNotBlank() } ?: streamUrl
            else -> streamUrl
        }
        return listOfNotNull(dashDataUri, primaryUrl, downloadUrl, replayUrl)
            .flatMap(::playbackUrlCandidates)
            .distinct()
    }

    fun isPlaybackUrlLikelyExpired(
        url: String,
        nowMs: Long = System.currentTimeMillis(),
    ): Boolean {
        val deadlineMs = parseUrlDeadlineMs(url) ?: return false
        return nowMs >= deadlineMs - PLAYBACK_URL_EXPIRY_SKEW_MS
    }

    fun isFeedMediaPlaybackLikelyExpired(
        media: FeedMedia,
        nowMs: Long = System.currentTimeMillis(),
    ): Boolean {
        val urls = listOfNotNull(
            media.resolvedPlaybackUrl(),
            media.streamUrl.takeIf { it.isNotBlank() },
            media.downloadUrl,
            media.replayUrl,
        ).distinct()
        if (urls.isEmpty()) return false
        // Timeline payloads keep signed CDN URLs; once the primary playback URL expires,
        // alternate quality links from the same snapshot are usually dead as well.
        return urls.any { isPlaybackUrlLikelyExpired(it, nowMs) }
    }

    fun mediaPlaybackUrlsChanged(before: FeedMedia, after: FeedMedia): Boolean =
        before.streamUrl != after.streamUrl ||
            before.downloadUrl != after.downloadUrl ||
            before.replayUrl != after.replayUrl ||
            before.liveStatus != after.liveStatus

    fun pickRefreshedMedia(previous: FeedMedia, detail: FeedItem): FeedMedia? {
        val candidates = detail.medias.filter { it.isStreamPlayable() }
        if (candidates.isEmpty()) return null
        candidates.firstOrNull { media ->
            !previous.coverUrl.isNullOrBlank() && media.coverUrl == previous.coverUrl
        }?.let { return it }
        candidates.firstOrNull { media ->
            previous.title.isNotBlank() && media.title == previous.title && media.type == previous.type
        }?.let { return it }
        candidates.firstOrNull { it.type == previous.type }?.let { return it }
        return candidates.firstOrNull()
    }

    fun parseUrlDeadlineMs(url: String): Long? {
        val query = url.substringAfter('?', missingDelimiterValue = "")
            .substringBefore('#')
        if (query.isBlank()) return null
        val keys = setOf("expires", "deadline", "expire", "e")
        query.split('&').forEach { part ->
            val name = part.substringBefore('=', missingDelimiterValue = "").trim()
            if (name.isEmpty() || name.lowercase() !in keys) return@forEach
            val raw = part.substringAfter('=', missingDelimiterValue = "")
                .substringBefore(',')
                .trim()
            val asLong = raw.toLongOrNull() ?: return@forEach
            // Weibo CDN commonly uses unix seconds; accept millis for safety.
            return when {
                asLong > 10_000_000_000L -> asLong
                asLong > 0L -> asLong * 1000L
                else -> null
            }
        }
        return null
    }

    private fun protocolCandidates(url: String, allowDataUri: Boolean): List<String> {
        val trimmed = url.trim()
        if (trimmed.isBlank()) return emptyList()
        if (allowDataUri && trimmed.startsWith("data:", ignoreCase = true)) return listOf(trimmed)
        if (trimmed.startsWith("http://", ignoreCase = true)) {
            return listOf(trimmed.replaceFirst("http://", "https://", ignoreCase = true), trimmed)
        }
        return listOf(trimmed)
    }
}
