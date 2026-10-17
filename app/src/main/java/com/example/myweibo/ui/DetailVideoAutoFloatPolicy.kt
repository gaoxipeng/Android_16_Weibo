package com.example.myweibo.ui

internal fun isDetailVideoInViewport(
    videoTop: Float,
    videoBottom: Float,
    viewportTop: Float,
    viewportBottom: Float,
): Boolean = videoBottom > videoTop && viewportBottom > viewportTop &&
    videoBottom > viewportTop && videoTop < viewportBottom

internal fun shouldAutoFloatDetailVideo(
    hasBeenVisible: Boolean,
    videoTop: Float,
    videoBottom: Float,
    viewportTop: Float,
): Boolean = hasBeenVisible && videoBottom > videoTop && videoTop <= viewportTop
