package com.example.myweibo.data

object ImageUrlResolver {
    private fun sinaImageId(url: String): String? {
        val uri = runCatching { java.net.URI(url) }.getOrNull() ?: return null
        val host = uri.host?.lowercase() ?: return null
        if (host != "sinaimg.cn" && !host.endsWith(".sinaimg.cn")) return null
        return uri.path?.substringAfterLast('/')?.substringBeforeLast('.')?.takeIf { it.isNotBlank() }
    }

    // Feed/detail can represent the same photo by PID or by a different CDN URL.
    fun thumbnailIdentity(image: FeedImage): String =
        (listOf(image.largeUrl, image.thumbnailUrl) + image.downloadUrls)
            .firstNotNullOfOrNull(::sinaImageId)
            ?: image.id.takeIf { it.isNotBlank() && !it.startsWith("http") }
            ?: image.largeUrl.ifBlank { image.thumbnailUrl }

    fun thumbnailCacheKey(image: FeedImage, quality: FeedThumbnailQuality, previewUrl: String? = null): String =
        "${thumbnailIdentity(image)}:$quality:$previewUrl"

    fun videoCoverCacheKey(url: String?): String? = url?.takeIf { it.isNotBlank() }?.let {
        "video-cover:${sinaImageId(it) ?: it}"
    }

    private val SinaimgVariantPattern =
        Regex(
            """/(?:large|mw2000|woriginal|original|bmiddle|orj360|orj480|mw690|mw1024|thumbnail|thumb(?:180|300|150)?|small|wap360)/""",
            RegexOption.IGNORE_CASE,
        )

    private val LowQualityImageUrlPattern =
        Regex(
            """/(?:orj360|orj480|bmiddle|thumbnail|thumb(?:180|300|150)?|small|wap360)/""",
            RegexOption.IGNORE_CASE,
        )

    fun withSinaimgVariant(url: String, variant: String?): String {
        if (variant.isNullOrBlank()) return url
        if (!url.contains("sinaimg.cn", ignoreCase = true)) return url
        return url.replace(SinaimgVariantPattern, "/$variant/")
    }

    fun feedBitmapCandidates(image: FeedImage): List<String> =
        (listOf(image.thumbnailUrl, withSinaimgVariant(image.largeUrl, "mw690")) +
            image.downloadUrls.map { withSinaimgVariant(it, "mw690") })
            .filter { it.isNotBlank() }
            .distinct()

    fun feedDisplayCandidates(
        image: FeedImage,
        quality: FeedThumbnailQuality,
    ): List<String> =
        listOfNotNull(
            quality.displayUrl(image).takeIf { it.isNotBlank() },
            quality.fallbackUrl(image),
        ).distinct()

    fun fullscreenCandidates(image: FeedImage): List<String> {
        val highQuality = (image.downloadUrls + listOf(image.largeUrl))
            .filter { it.isNotBlank() }
            .distinct()
            .filterNot { LowQualityImageUrlPattern.containsMatchIn(it) }
        if (highQuality.isNotEmpty()) return highQuality
        return listOfNotNull(
            image.largeUrl.takeIf { it.isNotBlank() },
            image.thumbnailUrl.takeIf { it.isNotBlank() },
        ).distinct()
    }

    fun saveCandidates(image: FeedImage): List<String> =
        fullscreenCandidates(image)

    fun albumGridCandidates(image: FeedImage): List<String> = buildList {
        image.thumbnailUrl.takeIf { it.isNotBlank() }
            ?.let(::downgradeForAlbumGrid)
            ?.let(::add)
        image.largeUrl.takeIf { it.isNotBlank() }
            ?.let(::downgradeForAlbumGrid)
            ?.let(::add)
        image.downloadUrls.forEach { url ->
            if (url.isNotBlank()) {
                add(downgradeForAlbumGrid(url))
            }
        }
    }.distinct()

    fun livePhotoStillCandidates(image: FeedImage): List<String> =
        listOfNotNull(
            image.largeUrl.takeIf { it.isNotBlank() },
            image.thumbnailUrl.takeIf { it.isNotBlank() },
        ).distinct()

    private fun downgradeForAlbumGrid(url: String): String =
        if (url.contains("sinaimg.cn", ignoreCase = true)) {
            url.replace(
                Regex("""/(?:large|mw2000|woriginal|original|bmiddle)/""", RegexOption.IGNORE_CASE),
                "/orj360/",
            )
        } else {
            url
        }
}
