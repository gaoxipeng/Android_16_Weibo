package com.example.myweibo.ui

internal const val DetailCardOvershoot = 0.006f

/** Preserve the small overshoot instead of clamping it at the source card. */
internal fun detailCardReturnAmount(pageReveal: Float): Float =
    (1f - pageReveal).coerceIn(0f, 1f + DetailCardOvershoot)

/** Keeps the captured source visible until the new card has a measured position. */
internal fun detailEntrySnapshotAlpha(progress: Float, cardLaidOut: Boolean): Float {
    if (!cardLaidOut) return 1f
    return (1f - progress / 0.22f).coerceIn(0f, 1f)
}

/**
 * Keep the returning overlay composed on the same frame the visibility
 * transition goes idle. Setting the hold only inside LaunchedEffect would
 * drop the overlay for one frame while the live source card is still hidden.
 */
internal fun shouldKeepDetailExitHandoff(
    overlayVisible: Boolean,
    transitionIdle: Boolean,
    transitionVisible: Boolean,
    hasReturnSource: Boolean,
    nestedDetailReturn: Boolean,
    alreadyHolding: Boolean,
): Boolean {
    if (overlayVisible || !hasReturnSource || nestedDetailReturn) return false
    if (alreadyHolding) return true
    return transitionIdle && !transitionVisible
}
