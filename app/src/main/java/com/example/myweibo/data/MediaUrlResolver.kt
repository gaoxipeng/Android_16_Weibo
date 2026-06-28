package com.example.myweibo.data

object MediaUrlResolver {
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
