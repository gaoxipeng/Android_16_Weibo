package com.example.myweibo

import com.example.myweibo.ui.isDetailVideoInViewport
import com.example.myweibo.ui.shouldAutoFloatDetailVideo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DetailVideoAutoFloatPolicyTest {
    @Test fun longPostVideoBelowScreenDoesNotArmOrFloat() {
        assertFalse(isDetailVideoInViewport(1800f, 2200f, 100f, 1000f))
        assertFalse(shouldAutoFloatDetailVideo(false, 1800f, 2200f, 100f))
    }

    @Test fun topTouchingViewportEdgeStartsFloatingImmediately() {
        assertFalse(shouldAutoFloatDetailVideo(true, 101f, 501f, 100f))
        assertTrue(shouldAutoFloatDetailVideo(true, 100f, 500f, 100f))
        assertTrue(isDetailVideoInViewport(50f, 450f, 100f, 1000f))
        assertTrue(shouldAutoFloatDetailVideo(true, 50f, 450f, 100f))
    }

    @Test fun unseenVideoCannotFloat() {
        assertTrue(shouldAutoFloatDetailVideo(true, -300f, 100f, 100f))
        assertFalse(shouldAutoFloatDetailVideo(false, -300f, 100f, 100f))
        assertFalse(shouldAutoFloatDetailVideo(false, 100f, 500f, 100f))
    }

    @Test fun clippedEmptyBoundsCannotTriggerFloat() {
        assertFalse(isDetailVideoInViewport(0f, 0f, 100f, 1000f))
        assertFalse(shouldAutoFloatDetailVideo(true, 0f, 0f, 100f))
    }
}
