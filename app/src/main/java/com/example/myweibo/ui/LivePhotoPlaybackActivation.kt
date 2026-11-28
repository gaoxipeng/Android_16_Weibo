package com.example.myweibo.ui

import androidx.media3.common.Player

internal enum class LivePhotoPlaybackAction { Pause, WaitForRetry, Prepare, Rewind, Resume }

/** A failed player is prepared again only for a new tap or long press. */
internal class LivePhotoPlaybackActivation(initialExplicitRequest: Int) {
    private var lastExplicitRequest = initialExplicitRequest

    fun onActivation(
        isActive: Boolean,
        explicitRequest: Int,
        playbackState: Int,
        hasTerminalError: Boolean,
    ): LivePhotoPlaybackAction {
        val explicitlyRequested = explicitRequest != lastExplicitRequest
        lastExplicitRequest = explicitRequest
        if (!isActive) return LivePhotoPlaybackAction.Pause
        if (hasTerminalError) {
            return if (explicitlyRequested) LivePhotoPlaybackAction.Prepare else LivePhotoPlaybackAction.WaitForRetry
        }
        return when (playbackState) {
            Player.STATE_IDLE -> LivePhotoPlaybackAction.Prepare
            Player.STATE_ENDED -> LivePhotoPlaybackAction.Rewind
            else -> LivePhotoPlaybackAction.Resume
        }
    }
}
