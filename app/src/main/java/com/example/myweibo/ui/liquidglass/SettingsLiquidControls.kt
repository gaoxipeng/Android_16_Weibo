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
import androidx.compose.runtime.withFrameNanos
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
import androidx.compose.ui.unit.Dp
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
import kotlinx.coroutines.flow.first
import kotlin.math.abs

/** Backdrop Catalog-style toggle: elastic glass thumb over a liquid track. */
@Composable
fun SettingsLiquidToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsLiquidSwitch(
        selectedIndex = if (checked) 1 else 0,
        onSelected = { onCheckedChange(it == 1) },
        modifier = modifier,
    )
}

@Composable
internal fun SettingsLiquidSwitch(
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    optionCount: Int = 2,
    trackWidth: Dp = 56.dp,
    trackHeight: Dp = 24.dp,
    thumbWidth: Dp = 32.dp,
    thumbColor: Color = Color.White,
    glassTrack: Modifier? = null,
    enabled: Boolean = true,
    onSelectionSettled: (() -> Unit)? = null,
    labels: (@Composable (Int, (Int) -> Unit) -> Unit)? = null,
) {
    val isLightTheme = isAppLightTheme()
    val accentColor = if (isLightTheme) Color(0xFF34C759) else Color(0xFF30D158)
    val trackColor = if (isLightTheme) Color(0xFF787878).copy(alpha = 0.2f)
    else Color(0xFF787880).copy(alpha = 0.36f)
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(percent = 50)
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val density = LocalDensity.current
    val lastIndex = optionCount - 1
    val dragWidth = trackWidth - thumbWidth - 4.dp
    val thumbHeight = trackHeight - 4.dp
    val scope = rememberCoroutineScope()
    val latestIndex by rememberUpdatedState(selectedIndex)
    val latestOnSelected by rememberUpdatedState(onSelected)
    val latestOnSettled by rememberUpdatedState(onSelectionSettled)
    val latestEnabled by rememberUpdatedState(enabled)
    var didDrag by remember { mutableStateOf(false) }
    var pressedIndex by remember { mutableStateOf(selectedIndex) }
    var settlingIndex by remember { mutableStateOf<Int?>(null) }
    var fraction by remember { mutableFloatStateOf(selectedIndex.toFloat() / lastIndex) }

    fun select(index: Int) {
        fraction = index.toFloat() / lastIndex
        if (latestOnSettled != null) {
            settlingIndex = index
        }
        latestOnSelected(index)
    }

    val damped = remember(scope, dragWidth, isLtr, density, optionCount) {
        DampedDragAnimation(
            animationScope = scope,
            initialValue = fraction,
            valueRange = 0f..1f,
            visibilityThreshold = 0.001f,
            initialScale = 1f,
            pressedScale = 1.5f,
            onDragStarted = { position ->
                didDrag = false
                val width = with(density) { trackWidth.toPx() }
                val x = if (isLtr) position.x else width - position.x
                pressedIndex = (x / width * optionCount).toInt().coerceIn(0, lastIndex)
            },
            onDragStopped = {
                if (latestEnabled && settlingIndex == null) {
                    val next = when {
                        didDrag -> (fraction * lastIndex).fastRoundToInt().coerceIn(0, lastIndex)
                        optionCount == 2 -> 1 - latestIndex
                        else -> pressedIndex
                    }
                    select(next)
                }
                didDrag = false
            },
            onDrag = { _, amount, _ ->
                if (latestEnabled && settlingIndex == null) {
                    didDrag = didDrag || amount.x != 0f
                    val delta = amount.x / with(density) { dragWidth.toPx() }
                    fraction = (fraction + if (isLtr) delta else -delta).fastCoerceIn(0f, 1f)
                }
            },
        )
    }
    LaunchedEffect(damped) {
        snapshotFlow { fraction }.collectLatest(damped::updateValue)
    }
    LaunchedEffect(damped, selectedIndex) {
        val target = selectedIndex.toFloat() / lastIndex
        if (target != fraction) {
            fraction = target
            damped.animateToValue(target)
        }
    }
    LaunchedEffect(damped, settlingIndex, enabled) {
        if (!enabled) {
            settlingIndex = null
            return@LaunchedEffect
        }
        val index = settlingIndex ?: return@LaunchedEffect
        // Let the shared gesture release schedule its spring before checking rest.
        withFrameNanos { }
        withFrameNanos { }
        val target = index.toFloat() / lastIndex
        snapshotFlow {
            abs(damped.value - target) <= 0.001f && damped.pressProgress <= 0.001f &&
                abs(damped.scaleX - 1f) <= 0.001f && abs(damped.scaleY - 1f) <= 0.001f
        }.first { it }
        latestOnSettled?.invoke()
        settlingIndex = null
    }

    val trackBackdrop = rememberLayerBackdrop {
        if (glassTrack != null) drawContent() else drawRoundRect(
            color = lerp(trackColor, accentColor, damped.value),
            cornerRadius = CornerRadius(size.height / 2f),
        )
    }
    val canInteract = enabled && settlingIndex == null
    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .then(if (glassTrack == null) Modifier.layerBackdrop(trackBackdrop) else Modifier)
                .clip(shape)
                .size(trackWidth, trackHeight)
                .drawWithCache {
                    val aperture = Path()
                    onDrawWithContent {
                        val progress = damped.pressProgress
                        val velocity = damped.velocity / 50f
                        var scaleX = damped.scaleX
                        var scaleY = damped.scaleY
                        scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                        scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                        val scaledThumbWidth = thumbWidth.toPx() * scaleX
                        val scaledThumbHeight = thumbHeight.toPx() * scaleY
                        val padding = 2.dp.toPx()
                        val drag = with(density) { dragWidth.toPx() } * damped.value
                        val centerX = if (isLtr) padding + thumbWidth.toPx() / 2f + drag
                        else size.width - padding - thumbWidth.toPx() / 2f - drag
                        val centerY = size.height / 2f
                        val apertureWidth = scaledThumbWidth * progress
                        val apertureHeight = scaledThumbHeight * progress
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
                            if (glassTrack == null) {
                                drawRoundRect(
                                    color = lerp(trackColor, accentColor, damped.value),
                                    cornerRadius = CornerRadius(size.height / 2f),
                                )
                            } else {
                                this@onDrawWithContent.drawContent()
                            }
                        }
                    }
                }
                // Record the full glass track for the thumb before the visible
                // pass cuts its opening during a press.
                .then(if (glassTrack != null) Modifier.layerBackdrop(trackBackdrop).then(glassTrack) else Modifier),
        )
        Box(
            Modifier.graphicsLayer {
                val padding = 2.dp.toPx()
                val dx = with(density) { dragWidth.toPx() }
                translationX = if (isLtr) androidx.compose.ui.util.lerp(padding, padding + dx, damped.value)
                else -androidx.compose.ui.util.lerp(padding, padding + dx, damped.value)
            }
                .then(if (optionCount == 2 && canInteract) damped.modifier else Modifier)
                .drawBackdrop(
                    backdrop = rememberBackdrop(trackBackdrop) { draw ->
                        val p = damped.pressProgress
                        scale(androidx.compose.ui.util.lerp(2f / 3f, 0.75f, p), androidx.compose.ui.util.lerp(0f, 0.75f, p)) { draw() }
                    },
                    shape = { shape },
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
                    onDrawSurface = { drawRect(thumbColor.copy(alpha = thumbColor.alpha * (1f - damped.pressProgress))) },
                )
                .size(thumbWidth, thumbHeight),
        )
        labels?.invoke(settlingIndex ?: selectedIndex) { index ->
            if (canInteract) {
                select(index)
                damped.animateToValue(index.toFloat() / lastIndex)
            }
        }
        if (optionCount > 2 && canInteract) {
            // Clip the touch target separately so the pressed thumb can grow
            // past the track without being cropped.
            Box(Modifier.matchParentSize().clip(shape).then(damped.modifier))
        }
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
