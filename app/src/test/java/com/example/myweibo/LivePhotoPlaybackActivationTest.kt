package com.example.myweibo

import androidx.media3.common.Player
import com.example.myweibo.ui.LivePhotoPlaybackAction
import com.example.myweibo.ui.LivePhotoPlaybackActivation
import org.junit.Assert.assertEquals
import org.junit.Test

class LivePhotoPlaybackActivationTest {
    @Test
    fun activeIdlePlayerPreparesAndEndedPlayerRewinds() {
        val activation = LivePhotoPlaybackActivation(initialExplicitRequest = 0)
        assertEquals(LivePhotoPlaybackAction.Pause, activation.onActivation(false, 0, Player.STATE_IDLE, false))
        assertEquals(LivePhotoPlaybackAction.Prepare, activation.onActivation(true, 0, Player.STATE_IDLE, false))
        assertEquals(LivePhotoPlaybackAction.Resume, activation.onActivation(true, 0, Player.STATE_READY, false))
        assertEquals(LivePhotoPlaybackAction.Rewind, activation.onActivation(true, 1, Player.STATE_ENDED, false))
    }

    @Test
    fun aTerminalErrorNeedsOneNewExplicitPlayRequestPerRetry() {
        val activation = LivePhotoPlaybackActivation(initialExplicitRequest = 0)
        assertEquals(LivePhotoPlaybackAction.Prepare, activation.onActivation(true, 0, Player.STATE_IDLE, false))

        // A failed player stays idle. Returning to the page cannot retry by itself.
        assertEquals(LivePhotoPlaybackAction.Pause, activation.onActivation(false, 0, Player.STATE_IDLE, true))
        assertEquals(LivePhotoPlaybackAction.WaitForRetry, activation.onActivation(true, 0, Player.STATE_IDLE, true))

        assertEquals(LivePhotoPlaybackAction.Prepare, activation.onActivation(true, 1, Player.STATE_IDLE, true))
        assertEquals(LivePhotoPlaybackAction.WaitForRetry, activation.onActivation(true, 1, Player.STATE_IDLE, true))
        assertEquals(LivePhotoPlaybackAction.Prepare, activation.onActivation(true, 2, Player.STATE_IDLE, true))
    }
}
