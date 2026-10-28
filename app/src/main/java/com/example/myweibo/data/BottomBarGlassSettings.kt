package com.example.myweibo.data

internal fun normalizeBottomBarGlassAmount(value: Float): Float =
    if (value.isFinite()) value.coerceIn(0f, 1f) else 0f

internal fun bottomBarGlassSurfaceAlpha(value: Float): Float {
    val amount = normalizeBottomBarGlassAmount(value)
    return 0.90f * amount
}

internal fun bottomBarGlassBlurDp(value: Float): Float = 24f * normalizeBottomBarGlassAmount(value)

// lens() consumes refractionHeight from the effect padding. Reserve that in
// addition to the blur's surrounding source pixels so a moving line of text
// at the capsule boundary is not clamped across the whole frosted edge.
internal fun bottomBarBlurSamplingPadding(blurRadius: Float, refractionHeight: Float): Float {
    val radius = blurRadius.takeIf { it.isFinite() }?.coerceAtLeast(0f) ?: 0f
    if (radius == 0f) return 0f
    val lensHeight = refractionHeight.takeIf { it.isFinite() }?.coerceAtLeast(0f) ?: 0f
    return radius * 2f + lensHeight
}

internal fun bottomBarGlassRefractionHeightDp(value: Float): Float =
    24f * normalizeBottomBarGlassAmount(value)

internal fun bottomBarGlassRefractionAmountDp(value: Float): Float =
    40f * normalizeBottomBarGlassAmount(value)

internal fun bottomBarPressScale(widthPx: Float, expansionPx: Float, pressProgress: Float): Float =
    if (widthPx.isFinite() && widthPx > 0f && expansionPx.isFinite()) {
        1f + expansionPx.coerceAtLeast(0f) / widthPx * normalizeBottomBarGlassAmount(pressProgress)
    } else 1f
