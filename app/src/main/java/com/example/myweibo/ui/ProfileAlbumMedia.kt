package com.example.myweibo.ui

import com.example.myweibo.data.FeedImage

/** Rendering and return anchors must use exactly the same media subset. */
internal fun profileAlbumMedia(images: List<FeedImage>, videos: Boolean = false): List<FeedImage> =
    images.filter { it.isAlbumVideo == videos }

internal fun shouldRepositionAlbumViewer(
    initialIndex: Int,
    currentIndex: Int,
    targetRowVisible: Boolean,
): Boolean = currentIndex != initialIndex && !targetRowVisible
