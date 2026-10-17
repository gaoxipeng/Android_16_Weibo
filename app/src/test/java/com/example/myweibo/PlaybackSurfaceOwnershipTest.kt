package com.example.myweibo

import com.example.myweibo.ui.PlaybackSurfaceOwnership
import com.example.myweibo.ui.isOutgoingInlineVideoHandoff
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackSurfaceOwnershipTest {
    @Test fun autoFloatHandoffIsNotAnIncomingDetailResume() {
        assertTrue(isOutgoingInlineVideoHandoff(true, true, false, false))
        assertTrue(isOutgoingInlineVideoHandoff(false, false, true, false))
        assertFalse(isOutgoingInlineVideoHandoff(false, true, false, false))
        assertFalse(isOutgoingInlineVideoHandoff(true, true, false, true))
    }

    @Test fun delayedCleanupCannotInvalidateReacquiredPlayer() {
        val leases = PlaybackSurfaceOwnership<Any, Any>()
        val player = Any()
        val previousGeneration = Any()
        val replacementGeneration = Any()
        val oldView = Any()
        val newView = Any()
        leases.claim(player, previousGeneration)
        leases.bindView(player, previousGeneration, oldView)
        leases.claim(player, replacementGeneration)
        leases.bindView(player, replacementGeneration, newView)
        assertFalse(leases.detachView(player, previousGeneration, oldView))
        assertFalse(leases.release(player, previousGeneration))
        assertTrue(leases.owns(player, replacementGeneration))
        assertTrue(leases.detachView(player, replacementGeneration, newView))
    }

    @Test fun recreatingNativeViewDoesNotRevokeTheHostLease() {
        val leases = PlaybackSurfaceOwnership<Any, Any>()
        val player = Any()
        val host = Any()
        val oldView = Any()
        val newView = Any()
        leases.claim(player, host)
        assertTrue(leases.bindView(player, host, oldView))
        assertTrue(leases.bindView(player, host, newView))
        assertFalse(leases.detachView(player, host, oldView))
        assertTrue(leases.owns(player, host))
        assertTrue(leases.detachView(player, host, newView))
        assertTrue(leases.owns(player, host))
        assertTrue(leases.bindView(player, host, Any()))
    }

    @Test fun oldCardCannotRevokeFloatingOutput() {
        val leases = PlaybackSurfaceOwnership<Any, Any>()
        val player = Any()
        val card = Any()
        val floating = Any()
        leases.claim(player, card)
        leases.claim(player, floating)
        assertFalse(leases.owns(player, card))
        assertFalse(leases.release(player, card))
        assertTrue(leases.owns(player, floating))
    }

    @Test fun fullscreenReturnInvalidatesOldOwner() {
        val leases = PlaybackSurfaceOwnership<Any, Any>()
        val player = Any()
        val fullscreen = Any()
        val card = Any()
        leases.claim(player, fullscreen)
        leases.claim(player, card)
        assertFalse(leases.release(player, fullscreen))
        assertTrue(leases.release(player, card))
        assertFalse(leases.owns(player, card))
    }
}
