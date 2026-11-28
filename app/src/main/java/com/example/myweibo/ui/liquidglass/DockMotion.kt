package com.example.myweibo.ui.liquidglass

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp

internal fun <T> dockSelectionTween() = tween<T>(150, easing = FastOutSlowInEasing)

@Composable
internal fun animateDockPress(pressed: Boolean): State<Float> = animateFloatAsState(
    targetValue = if (pressed) 1f else 0f,
    animationSpec = tween(if (pressed) 90 else 140, easing = FastOutSlowInEasing),
    label = "dock-press",
)

internal fun Modifier.dockPressScale(progress: State<Float>): Modifier = graphicsLayer {
    val scale = 1f - 0.04f * progress.value
    scaleX = scale
    scaleY = scale
}

/** Visual only. The existing selectable/combinedClickable still owns the action and cancellation. */
internal fun Modifier.observeDockPress(onPressedChange: (Boolean) -> Unit): Modifier =
    pointerInput(onPressedChange) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            onPressedChange(true)
            try {
                // Skip the rest of the DOWN event: consuming it is expected. Later consumption
                // (e.g. a surrounding scroll taking over) cancels only this visual feedback.
                awaitPointerEvent(PointerEventPass.Final)
                waitForUpOrCancellation()
            } finally {
                onPressedChange(false)
            }
        }
    }

/** Enter only: closing removes the picker and all its input/semantics immediately. */
@Composable
internal fun DockTimelinePickerReveal(
    lift: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val reveal = remember { Animatable(0f) }
    // Standard Compose animations respect the system animator duration scale.
    LaunchedEffect(reveal) {
        reveal.animateTo(1f, tween(160, easing = FastOutSlowInEasing))
    }
    Box(
        modifier.graphicsLayer {
            val progress = reveal.value
            alpha = progress
            translationY = lift.toPx() * (1f - progress)
            clip = false
        },
    ) { content() }
}
