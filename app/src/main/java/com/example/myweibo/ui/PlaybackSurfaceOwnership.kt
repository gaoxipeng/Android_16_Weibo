package com.example.myweibo.ui

import java.util.IdentityHashMap

internal fun isOutgoingInlineVideoHandoff(
    peekOwnsPlayback: Boolean,
    peekHandoffPending: Boolean,
    fullscreenHandoffPending: Boolean,
    inlineReturnPending: Boolean,
): Boolean = !inlineReturnPending &&
    (fullscreenHandoffPending || (peekOwnsPlayback && peekHandoffPending))

/** One output owner per player; an outgoing view cannot revoke its successor. */
internal class PlaybackSurfaceOwnership<P : Any, O : Any> {
    private val owners = IdentityHashMap<P, O>()
    private val views = IdentityHashMap<P, Any>()

    fun claim(player: P, owner: O) { owners[player] = owner }
    fun owns(player: P, owner: O): Boolean = owners[player] === owner
    fun bindView(player: P, owner: O, view: Any): Boolean {
        if (!owns(player, owner)) return false
        views[player] = view
        return true
    }

    /** Releasing a native view must not invalidate the longer-lived host lease. */
    fun detachView(player: P, owner: O, view: Any): Boolean {
        if (!owns(player, owner) || views[player] !== view) return false
        views.remove(player)
        return true
    }

    fun release(player: P, owner: O): Boolean {
        if (!owns(player, owner)) return false
        owners.remove(player)
        views.remove(player)
        return true
    }
}
