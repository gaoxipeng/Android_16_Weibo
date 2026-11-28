package com.example.myweibo.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

private class FeedPositionCoordinates {
    var track: LayoutCoordinates? = null
    var thumb: LayoutCoordinates? = null
}

/** The label belongs to the card intersecting the marker's center. */
@Composable
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
internal fun BoxScope.FeedPositionIndicator(
    state: LazyListState,
    itemCount: Int,
    pullFraction: Float,
    refreshing: Boolean,
) {
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current
    var dragging by remember { mutableStateOf(false) }
    var dragCenter by remember { mutableFloatStateOf(Float.NaN) }
    val coordinates = remember { FeedPositionCoordinates() }
    var visible by remember(state) { mutableStateOf(true) }
    LaunchedEffect(state.isScrollInProgress, dragging, refreshing, pullFraction > 0f) {
        when {
            dragging || refreshing || pullFraction > 0f || state.isScrollInProgress -> visible = true
            else -> {
                delay(2_000L)
                visible = false
            }
        }
    }
    val reveal by animateFloatAsState(if (visible) 1f else 0f, tween(160), label = "feedIndicatorReveal")
    val currentCount by rememberUpdatedState(itemCount)
    val currentIndex = state.firstVisibleItemIndex.coerceIn(0, (itemCount - 1).coerceAtLeast(0))
    val first = state.layoutInfo.visibleItemsInfo.firstOrNull { it.index == currentIndex }
    val insideCard = if (first != null && first.size > 0) {
        state.firstVisibleItemScrollOffset.toFloat() / first.size
    } else 0f
    val progress = if (!state.canScrollForward && state.canScrollBackward) 1f else
        ((currentIndex + insideCard) / itemCount.coerceAtLeast(1)).coerceIn(0f, 1f)
    val shrinkTarget = if (refreshing && !state.canScrollBackward) 1f else pullFraction.coerceIn(0f, 1f)
    val shrink by animateFloatAsState(shrinkTarget, tween(120), label = "feedIndicatorPull")
    val emphasis by animateFloatAsState(if (dragging) 1f else 0f, tween(120), label = "feedIndicatorHold")
    val topInset = WindowInsets.statusBarsIgnoringVisibility.asPaddingValues().calculateTopPadding()

    BoxWithConstraints(
        Modifier.align(Alignment.CenterEnd).offset(x = 1.dp).zIndex(20f).fillMaxHeight().width(44.dp)
            .padding(top = topInset + 12.dp, bottom = 160.dp)
            .onGloballyPositioned { coordinates.track = it },
    ) {
        val trackHeight = with(density) { maxHeight.toPx() }
        val thumbHeight = with(density) { (36f * (1f - shrink) + 7f * shrink).dp.toPx() }
        val travel = (trackHeight - thumbHeight).coerceAtLeast(1f)
        val thumbTop = if (dragging && !dragCenter.isNaN()) {
            (dragCenter - thumbHeight / 2f).coerceIn(0f, travel)
        } else progress * travel
        // Lazy item offsets use the content origin; account for the list's top
        // padding before comparing them with the marker's position on screen.
        val markerOffset = with(density) { (topInset + 12.dp).toPx() } +
            thumbTop + thumbHeight / 2f + state.layoutInfo.viewportStartOffset
        val markerIndex = state.layoutInfo.visibleItemsInfo
            .lastOrNull { it.index < itemCount && it.offset <= markerOffset }
            ?.index ?: currentIndex
        val currentTravel by rememberUpdatedState(travel)
        val currentThumbHeight by rememberUpdatedState(thumbHeight)
        val hitHeight = with(density) { 52.dp.toPx() }
        Box(
            Modifier.align(Alignment.TopEnd)
                .offset { IntOffset(0, (thumbTop - (hitHeight - thumbHeight) / 2f).roundToInt()) }
                .size(44.dp, 52.dp)
                .onGloballyPositioned { coordinates.thumb = it }
                .graphicsLayer { alpha = reveal }
                .pointerInput(state, visible) {
                    if (!visible) return@pointerInput
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        down.consume()
                        var fingerPosition = down.position
                        val released = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                            var pressed = true
                            while (pressed) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                event.changes.firstOrNull { it.id == down.id }?.let { fingerPosition = it.position }
                                event.changes.forEach { it.consume() }
                                pressed = event.changes.any { it.id == down.id && it.pressed }
                            }
                            true
                        } == true
                        if (!released) {
                            fun moveToFinger(position: Offset) {
                                val thumb = coordinates.thumb?.takeIf { it.isAttached } ?: return
                                val track = coordinates.track?.takeIf { it.isAttached } ?: return
                                val fingerY = thumb.localToWindow(position).y - track.localToWindow(Offset.Zero).y
                                dragCenter = fingerY.coerceIn(currentThumbHeight / 2f, currentTravel + currentThumbHeight / 2f)
                                val targetY = (dragCenter - currentThumbHeight / 2f).coerceIn(0f, currentTravel)
                                val target = targetY / currentTravel * (currentCount - 1).coerceAtLeast(0)
                                val index = target.toInt()
                                val measuredSize = state.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }?.size ?: 0
                                state.requestScrollToItem(index, ((target - index) * measuredSize).roundToInt())
                            }
                            try {
                                // Align on recognition itself, without waiting for another move.
                                moveToFinger(fingerPosition)
                                dragging = true
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Initial)
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    event.changes.forEach { it.consume() }
                                    if (!change.pressed) {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        break
                                    }
                                    moveToFinger(change.position)
                                }
                            } finally {
                                dragging = false
                                dragCenter = Float.NaN
                            }
                        }
                    }
                },
        ) {
            val bubbleWidth = (10f + (markerIndex + 1).toString().length.minus(1) * 5f) * (1f - shrink)
            val bubbleHeight = 19f * (1f - shrink)
            val ink = Color(0xFFA3A3A3).copy(alpha = .70f)
            Canvas(Modifier.matchParentSize()) {
                val railWidth = (3f + emphasis * 1.5f).dp.toPx()
                val right = size.width - 3.dp.toPx()
                val railLeft = right - 3.dp.toPx()
                val centerY = size.height / 2f
                drawRoundRect(
                    ink, Offset(railLeft, centerY - thumbHeight / 2f),
                    Size(railWidth, thumbHeight), CornerRadius(railWidth / 2f),
                )
                val w = bubbleWidth.dp.toPx()
                val h = bubbleHeight.dp.toPx()
                // The reference has a round left lobe and two tapered shoulders
                // against the rail, rather than a speech-bubble point at its center.
                if (w > .5f && h > .5f) {
                    val join = railLeft
                    val path = Path().apply {
                        moveTo(join, centerY - h * .53f)
                        if (markerIndex + 1 >= 10) {
                            val radius = h * .48f
                            val capCenter = join - w + radius
                            cubicTo(join - w * .12f, centerY - radius, capCenter + radius * .35f, centerY - radius, capCenter, centerY - radius)
                            cubicTo(capCenter - radius * .5523f, centerY - radius, join - w, centerY - radius * .5523f, join - w, centerY)
                            cubicTo(join - w, centerY + radius * .5523f, capCenter - radius * .5523f, centerY + radius, capCenter, centerY + radius)
                            cubicTo(capCenter + radius * .35f, centerY + radius, join - w * .12f, centerY + radius, join, centerY + h * .53f)
                        } else {
                        cubicTo(join - w * .08f, centerY - h * .45f, join - w * .48f, centerY - h * .5f, join - w * .76f, centerY - h * .32f)
                        cubicTo(join - w, centerY - h * .18f, join - w, centerY - h * .08f, join - w, centerY)
                        cubicTo(join - w, centerY + h * .08f, join - w, centerY + h * .18f, join - w * .76f, centerY + h * .32f)
                        cubicTo(join - w * .48f, centerY + h * .5f, join - w * .08f, centerY + h * .45f, join, centerY + h * .53f)
                        }
                        close()
                    }
                    drawPath(path, ink)
                }
            }
            if (shrink < .9f && reveal > 0f) {
                Box(
                    Modifier.align(Alignment.CenterEnd).padding(end = 5.dp)
                        .size(bubbleWidth.dp, bubbleHeight.dp).clipToBounds(),
                    contentAlignment = Alignment.Center,
                ) {
                        Text(
                            (markerIndex + 1).toString(), color = Color.White.copy(alpha = (1f - shrink) * .95f),
                            modifier = Modifier.graphicsLayer { scaleX = if (markerIndex + 1 >= 10) .85f else 1f },
                            fontSize = 11.sp, lineHeight = 13.sp,
                            fontFamily = FontFamily(android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.NORMAL)),
                            fontWeight = FontWeight.Medium, maxLines = 1,
                        )
                }
            }
        }
    }
}
