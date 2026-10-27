package com.example.myweibo

import com.example.myweibo.data.bottomBarGlassBlurDp
import com.example.myweibo.data.bottomBarGlassSurfaceAlpha
import com.example.myweibo.data.bottomBarGlassRefractionHeightDp
import com.example.myweibo.data.bottomBarGlassRefractionAmountDp
import com.example.myweibo.data.normalizeBottomBarGlassAmount
import com.example.myweibo.data.bottomBarPressScale
import org.junit.Assert.assertEquals
import org.junit.Test

class BottomBarGlassSettingsTest {
    @Test fun leftEndpointIsTransparent() {
        assertEquals(0f, bottomBarGlassBlurDp(0f), 0.0001f)
        assertEquals(0f, bottomBarGlassSurfaceAlpha(0f), 0.0001f)
    }

    @Test fun rightEndpointAndIntermediateValuesIncreaseFrost() {
        assertEquals(24f, bottomBarGlassBlurDp(1f), 0.0001f)
        assertEquals(12f, bottomBarGlassBlurDp(0.5f), 0.0001f)
        assertEquals(0.90f, bottomBarGlassSurfaceAlpha(1f), 0.0001f)
        assertEquals(0.45f, bottomBarGlassSurfaceAlpha(0.5f), 0.0001f)
    }

    @Test fun invalidAndOutOfRangeValuesAreSafe() {
        assertEquals(0f, normalizeBottomBarGlassAmount(Float.NaN), 0.0001f)
        assertEquals(0f, normalizeBottomBarGlassAmount(Float.POSITIVE_INFINITY), 0.0001f)
        assertEquals(0f, normalizeBottomBarGlassAmount(-1f), 0.0001f)
        assertEquals(1f, normalizeBottomBarGlassAmount(2f), 0.0001f)
    }

    @Test fun glassSliderControlsRefraction() {
        assertEquals(0f, bottomBarGlassRefractionHeightDp(0f), 0.0001f)
        assertEquals(0f, bottomBarGlassRefractionAmountDp(0f), 0.0001f)
        assertEquals(12f, bottomBarGlassRefractionHeightDp(0.5f), 0.0001f)
        assertEquals(20f, bottomBarGlassRefractionAmountDp(0.5f), 0.0001f)
        assertEquals(24f, bottomBarGlassRefractionHeightDp(1f), 0.0001f)
        assertEquals(40f, bottomBarGlassRefractionAmountDp(1f), 0.0001f)
    }

    @Test fun barAndContentsSharePressScale() {
        assertEquals(1f, bottomBarPressScale(400f, 16f, 0f), 0.0001f)
        assertEquals(1.02f, bottomBarPressScale(400f, 16f, 0.5f), 0.0001f)
        assertEquals(1.04f, bottomBarPressScale(400f, 16f, 1f), 0.0001f)
        assertEquals(1f, bottomBarPressScale(0f, 16f, 1f), 0.0001f)
    }
}
