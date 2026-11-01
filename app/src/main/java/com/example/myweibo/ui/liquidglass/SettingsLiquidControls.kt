package com.example.myweibo.ui.liquidglass

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.util.lerp
import com.example.myweibo.ui.theme.isAppLightTheme
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import kotlinx.coroutines.flow.collectLatest

/** Backdrop Catalog-style toggle: elastic glass thumb over a liquid track. */
@Composable
fun SettingsLiquidToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLightTheme = isAppLightTheme()
    val accentColor = if (isLightTheme) Color(0xFF34C759) else Color(0xFF30D158)
    val trackColor = if (isLightTheme) Color(0xFF787878).copy(alpha = 0.2f)
    else Color(0xFF787880).copy(alpha = 0.36f)
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val density = LocalDensity.current
    val dragWidth = 20f.dp
    val scope = rememberCoroutineScope()
    val latestChecked by rememberUpdatedState(checked)
    val latestOnCheckedChange by rememberUpdatedState(onCheckedChange)
    var didDrag by remember { mutableStateOf(false) }
    var fraction by remember { mutableFloatStateOf(if (checked) 1f else 0f) }
    val damped = remember(scope) {
        DampedDragAnimation(
            animationScope = scope,
            initialValue = fraction,
            valueRange = 0f..1f,
            visibilityThreshold = 0.001f,
            initialScale = 1f,
            pressedScale = 1.5f,
            onDragStarted = {},
            onDragStopped = {
                val next = if (didDrag) targetValue >= 0.5f else !latestChecked
                fraction = if (next) 1f else 0f
                latestOnCheckedChange(next)
                didDrag = false
            },
            onDrag = { _, amount, _ ->
                didDrag = didDrag || amount.x != 0f
                val delta = amount.x / with(density) { dragWidth.toPx() }
                fraction = (fraction + if (isLtr) delta else -delta).fastCoerceIn(0f, 1f)
            },
        )
    }
    LaunchedEffect(damped) {
        snapshotFlow { fraction }.collectLatest(damped::updateValue)
    }
    LaunchedEffect(damped, checked) {
        val target = if (checked) 1f else 0f
        if (target != fraction) {
            fraction = target
            damped.animateToValue(target)
        }
    }

    val trackBackdrop = rememberLayerBackdrop {
        drawRoundRect(
            color = lerp(trackColor, accentColor, damped.value),
            cornerRadius = CornerRadius(size.height / 2f),
        )
    }
    Box(modifier, contentAlignment = Alignment.CenterStart) {
        Box(
            Modifier.layerBackdrop(trackBackdrop)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(percent = 50))
                .size(56.dp, 24.dp)
                .drawWithCache {
                    val aperture = Path()
                    onDrawBehind {
                        val progress = damped.pressProgress
                        val velocity = damped.velocity / 50f
                        var scaleX = damped.scaleX
                        var scaleY = damped.scaleY
                        scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                        scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                        val thumbWidth = 32.dp.toPx() * scaleX
                        val thumbHeight = 20.dp.toPx() * scaleY
                        val padding = 2.dp.toPx()
                        val drag = with(density) { dragWidth.toPx() } * damped.value
                        val centerX = if (isLtr) padding + 16.dp.toPx() + drag
                        else size.width - padding - 16.dp.toPx() - drag
                        val centerY = size.height / 2f
                        val apertureWidth = thumbWidth * progress
                        val apertureHeight = thumbHeight * progress
                        aperture.reset()
                        aperture.addRoundRect(
                            RoundRect(
                                rect = Rect(
                                    left = centerX - apertureWidth / 2f,
                                    top = centerY - apertureHeight / 2f,
                                    right = centerX + apertureWidth / 2f,
                                    bottom = centerY + apertureHeight / 2f,
                                ),
                                cornerRadius = CornerRadius(apertureHeight / 2f),
                            ),
                        )
                        clipPath(aperture, clipOp = ClipOp.Difference) {
                            drawRoundRect(
                                color = lerp(trackColor, accentColor, damped.value),
                                cornerRadius = CornerRadius(size.height / 2f),
                            )
                        }
                    }
                },
        )
        Box(
            Modifier.graphicsLayer {
                val padding = 2.dp.toPx()
                val dx = with(density) { dragWidth.toPx() }
                translationX = if (isLtr) androidx.compose.ui.util.lerp(padding, padding + dx, damped.value)
                else -androidx.compose.ui.util.lerp(padding, padding + dx, damped.value)
            }
                .then(damped.modifier)
                .drawBackdrop(
                    backdrop = rememberBackdrop(trackBackdrop) { draw ->
                        val p = damped.pressProgress
                        scale(androidx.compose.ui.util.lerp(2f / 3f, 0.75f, p), androidx.compose.ui.util.lerp(0f, 0.75f, p)) { draw() }
                    },
                    shape = { androidx.compose.foundation.shape.RoundedCornerShape(percent = 50) },
                    effects = {
                        val p = damped.pressProgress
                        blur(8f.dp.toPx() * (1f - p))
                        // The thumb is narrower than Backdrop Catalog's 40 dp
                        // control, so scale its refraction proportionally and
                        // avoid the extra color dispersion that makes the edge
                        // look overly busy at this size.
                        lens(4f.dp.toPx() * p, 8f.dp.toPx() * p, chromaticAberration = false)
                    },
                    highlight = {
                        Highlight.Ambient.copy(
                            width = Highlight.Ambient.width / 1.5f,
                            blurRadius = Highlight.Ambient.blurRadius / 1.5f,
                            alpha = damped.pressProgress,
                        )
                    },
                    shadow = { Shadow(radius = 4.dp, color = Color.Black.copy(alpha = 0.05f)) },
                    innerShadow = {
                        InnerShadow(radius = 4.dp * damped.pressProgress, alpha = damped.pressProgress)
                    },
                    layerBlock = {
                        scaleX = damped.scaleX
                        scaleY = damped.scaleY
                        val velocity = damped.velocity / 50f
                        scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                        scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                    },
                    onDrawSurface = { drawRect(Color.White.copy(alpha = 1f - damped.pressProgress)) },
                )
                .size(32.dp, 20.dp),
        )
    }
}

/** Backdrop Catalog-style slider with a damped, magnifying glass thumb. */
@Composable
fun SettingsLiquidSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    visibilityThreshold: Float = 0.001f,
) {
    val isLightTheme = isAppLightTheme()
    val accentColor = if (isLightTheme) Color(0xFF0088FF) else Color(0xFF0091FF)
    val trackColor = if (isLightTheme) Color(0xFF787878).copy(alpha = 0.2f)
    else Color(0xFF787880).copy(alpha = 0.36f)
    val trackBackdrop = rememberLayerBackdrop()
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val scope = rememberCoroutineScope()
    val latestValueChange by rememberUpdatedState(onValueChange)
    val latestFinished by rememberUpdatedState(onValueChangeFinished)
    var didDrag by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
        val trackWidth = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val latestTrackWidth by rememberUpdatedState(trackWidth)
        val damped = remember(scope, valueRange, visibilityThreshold) {
            DampedDragAnimation(
                animationScope = scope,
                initialValue = value,
                valueRange = valueRange,
                visibilityThreshold = visibilityThreshold,
                initialScale = 1f,
                pressedScale = 1.5f,
                onDragStarted = {},
                onDragStopped = {
                    if (didDrag) latestValueChange(targetValue)
                    latestFinished()
                    didDrag = false
                },
                onDrag = { _, amount, _ ->
                    didDrag = didDrag || amount.x != 0f
                    val delta = (valueRange.endInclusive - valueRange.start) * amount.x / latestTrackWidth
                    latestValueChange((targetValue + if (isLtr) delta else -delta).coerceIn(valueRange))
                },
            )
        }
        LaunchedEffect(damped, value) {
            if (damped.targetValue != value) damped.updateValue(value)
        }

        Box(Modifier.layerBackdrop(trackBackdrop)) {
            Box(
                Modifier.clip(androidx.compose.foundation.shape.RoundedCornerShape(percent = 50))
                    .background(trackColor)
                    .pointerInput(damped, valueRange, trackWidth) {
                        detectTapGestures { position ->
                            val p = (position.x / latestTrackWidth).coerceIn(0f, 1f)
                            val target = if (isLtr) {
                                valueRange.start + (valueRange.endInclusive - valueRange.start) * p
                            } else {
                                valueRange.endInclusive - (valueRange.endInclusive - valueRange.start) * p
                            }
                            damped.animateToValue(target)
                            latestValueChange(target)
                            latestFinished()
                        }
                    }
                    .height(6.dp)
                    .fillMaxWidth(),
            )
            Box(
                Modifier.clip(androidx.compose.foundation.shape.RoundedCornerShape(percent = 50))
                    .background(accentColor)
                    .height(6.dp)
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        val fraction = damped.progress.coerceIn(0f, 1f)
                        val width = (constraints.maxWidth * fraction).fastRoundToInt()
                        layout(width, placeable.height) { placeable.place(0, 0) }
                    },
            )
        }

        Box(
            Modifier.graphicsLayer {
                translationX = (-size.width / 2f + trackWidth * damped.progress)
                    .fastCoerceIn(-size.width / 4f, trackWidth - size.width * 3f / 4f) * if (isLtr) 1f else -1f
            }
                .then(damped.modifier)
                .drawBackdrop(
                    backdrop = rememberBackdrop(trackBackdrop) { draw ->
                        val p = damped.pressProgress
                        scale(androidx.compose.ui.util.lerp(2f / 3f, 1f, p), androidx.compose.ui.util.lerp(0f, 1f, p)) { draw() }
                    },
                    shape = { androidx.compose.foundation.shape.RoundedCornerShape(percent = 50) },
                    effects = {
                        val p = damped.pressProgress
                        blur(8f.dp.toPx() * (1f - p))
                        lens(10f.dp.toPx() * p, 14f.dp.toPx() * p, chromaticAberration = true)
                    },
                    highlight = { Highlight.Ambient.copy(alpha = damped.pressProgress) },
                    shadow = { Shadow(radius = 7.dp, color = Color.Black.copy(alpha = 0.12f)) },
                    innerShadow = { InnerShadow(radius = 4.dp * damped.pressProgress, alpha = damped.pressProgress) },
                    layerBlock = {
                        scaleX = damped.scaleX
                        scaleY = damped.scaleY
                        val velocity = damped.velocity / 10f
                        scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                        scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                    },
                    onDrawSurface = { drawRect(Color.White.copy(alpha = 1f - damped.pressProgress)) },
                )
                .size(40.dp, 24.dp),
        )
    }
}

private fun Modifier.drawBehindRect(
    inactive: Color,
    active: Color,
    fraction: Float,
): Modifier = this.then(
    Modifier.drawBehind { drawRect(lerp(inactive, active, fraction.coerceIn(0f, 1f))) },
)
