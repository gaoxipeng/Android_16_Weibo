package com.example.myweibo.ui.liquidglass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset

class LiquidBottomTabsGestureController internal constructor() {
    private var pendingPositionFraction = Offset.Zero
    private var pendingDragAmount = Offset.Zero
    private var pendingGesture = false
    private var pendingEnd = false

    internal var impl: GestureImpl? = null
        set(value) {
            field = value
            if (value != null && pendingGesture) {
                value.begin(pendingPositionFraction)
                if (pendingDragAmount != Offset.Zero) {
                    value.drag(pendingPositionFraction, pendingDragAmount)
                }
                if (pendingEnd) {
                    value.end()
                    clearPending()
                }
            }
        }

    fun beginAt(positionFraction: Offset) {
        pendingPositionFraction = positionFraction
        pendingDragAmount = Offset.Zero
        pendingGesture = true
        pendingEnd = false
        impl?.begin(positionFraction)
    }

    fun dragTo(positionFraction: Offset, dragAmount: Offset) {
        pendingPositionFraction = positionFraction
        pendingDragAmount = dragAmount
        impl?.drag(positionFraction, dragAmount)
    }

    fun end() {
        val target = impl
        if (target != null) {
            target.end()
            clearPending()
        } else if (pendingGesture) {
            pendingEnd = true
        }
    }

    fun cancel() {
        impl?.cancel()
        clearPending()
    }

    private fun clearPending() {
        pendingGesture = false
        pendingEnd = false
        pendingDragAmount = Offset.Zero
    }

    internal interface GestureImpl {
        fun begin(positionFraction: Offset)
        fun drag(positionFraction: Offset, dragAmount: Offset)
        fun end()
        fun cancel()
    }
}

@Composable
fun rememberLiquidBottomTabsGestureController(): LiquidBottomTabsGestureController =
    remember { LiquidBottomTabsGestureController() }
