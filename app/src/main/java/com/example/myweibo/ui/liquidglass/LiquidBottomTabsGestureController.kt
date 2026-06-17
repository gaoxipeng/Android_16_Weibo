package com.example.myweibo.ui.liquidglass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset

class LiquidBottomTabsGestureController internal constructor() {
    internal var impl: GestureImpl? = null

    fun beginAt(position: Offset) {
        impl?.begin(position)
    }

    fun dragTo(position: Offset, dragAmount: Offset) {
        impl?.drag(position, dragAmount)
    }

    fun end() {
        impl?.end()
    }

    fun cancel() {
        impl?.cancel()
    }

    internal interface GestureImpl {
        fun begin(position: Offset)
        fun drag(position: Offset, dragAmount: Offset)
        fun end()
        fun cancel()
    }
}

@Composable
fun rememberLiquidBottomTabsGestureController(): LiquidBottomTabsGestureController =
    remember { LiquidBottomTabsGestureController() }
