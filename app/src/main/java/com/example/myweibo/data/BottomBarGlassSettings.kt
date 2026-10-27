package com.example.myweibo.data

internal fun normalizeBottomBarGlassAmount(value: Float): Float =
    if (value.isFinite()) value.coerceIn(0f, 1f) else 0f

internal fun bottomBarGlassSurfaceAlpha(value: Float): Float {
    val amount = normalizeBottomBarGlassAmount(value)
    return 0.90f * amount
}

internal fun bottomBarGlassBlurDp(value: Float): Float = 24f * normalizeBottomBarGlassAmount(value)

internal fun bottomBarGlassRefractionHeightDp(value: Float): Float =
    24f * normalizeBottomBarGlassAmount(value)

internal fun bottomBarGlassRefractionAmountDp(value: Float): Float =
    40f * normalizeBottomBarGlassAmount(value)

internal fun bottomBarPressScale(widthPx: Float, expansionPx: Float, pressProgress: Float): Float =
    if (widthPx.isFinite() && widthPx > 0f && expansionPx.isFinite()) {
        1f + expansionPx.coerceAtLeast(0f) / widthPx * normalizeBottomBarGlassAmount(pressProgress)
    } else 1f
