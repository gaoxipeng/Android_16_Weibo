package com.example.myweibo.data

/**
 * Shares Live Photo mirror decisions between in-app playback and Motion Photo export.
 */
object LivePhotoMirrorCorrectionStore {
    private const val MaxEntries = 80
    private val entries = object : LinkedHashMap<String, Boolean>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Boolean>?): Boolean =
            size > MaxEntries
    }

    fun correctionKey(image: FeedImage): String? {
        val videoUrl = image.livePhotoVideoUrl?.takeIf { it.isNotBlank() } ?: return null
        val stillKey = image.largeUrl.ifBlank { image.thumbnailUrl }.ifBlank { image.id }
        if (stillKey.isBlank()) return null
        return "$stillKey|$videoUrl"
    }

    @Synchronized
    fun getMirrorVideo(key: String): Boolean? = entries[key]

    @Synchronized
    fun putMirrorVideo(key: String, mirrorVideo: Boolean) {
        entries[key] = mirrorVideo
    }

    fun getMirrorVideo(image: FeedImage): Boolean? =
        correctionKey(image)?.let(::getMirrorVideo)

    fun putMirrorVideo(image: FeedImage, mirrorVideo: Boolean) {
        correctionKey(image)?.let { putMirrorVideo(it, mirrorVideo) }
    }
}
