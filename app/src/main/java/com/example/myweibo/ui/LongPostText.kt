package com.example.myweibo.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.example.myweibo.ui.liquidglass.drawBottomBarGlassBackdrop
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.Constraints

private const val FoldedPostLines = 8
private const val PostPlaceholderTag = "post-inline-placeholder"

internal fun AnnotatedString.Builder.appendPostInlineContent(id: String, alternateText: String) {
    val start = length
    appendInlineContent(id, alternateText)
    addStringAnnotation(PostPlaceholderTag, id, start, length)
}

/**
 * Uses the PR's off-tree measurement approach so the first visible frame is
 * already folded, including emoticon and link-icon placeholders.
 */
@Composable
internal fun FoldLongPostText(
    text: AnnotatedString,
    contentKey: String,
    enabled: Boolean,
    style: TextStyle,
    inlineContent: Map<String, InlineTextContent> = emptyMap(),
    content: @Composable (AnnotatedString) -> Unit,
) {
    if (!enabled) {
        content(text)
        return
    }
    var expanded by rememberSaveable(contentKey) { mutableStateOf(false) }
    val transition = updateTransition(expanded, label = "post-expansion")
    val reveal by transition.animateFloat(
        transitionSpec = { tween(220, easing = FastOutSlowInEasing) },
        label = "post-height",
    ) { if (it) 1f else 0f }
    val controls = LocalLongPostControls.current
    val scope = rememberCoroutineScope()
    val bringIntoView = remember { BringIntoViewRequester() }
    var bounds by remember { mutableStateOf<Rect?>(null) }
    val themeColor = MaterialTheme.colorScheme.primary
    val measurer = rememberTextMeasurer(cacheSize = 4)
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val folded = remember(text, style, constraints.maxWidth, inlineContent, themeColor) {
            val width = Constraints.fixedWidth(constraints.maxWidth)
            fun measure(value: AnnotatedString) = measurer.measure(
                text = value,
                style = style,
                constraints = width,
                maxLines = FoldedPostLines,
                placeholders = value.getStringAnnotations(PostPlaceholderTag, 0, value.length).mapNotNull { range ->
                    inlineContent[range.item]?.let {
                        AnnotatedString.Range(it.placeholder, range.start, range.end)
                    }
                },
            )
            val original = measure(text)
            if (!original.hasVisualOverflow) {
                text
            } else {
                val placeholders = text.getStringAnnotations(PostPlaceholderTag, 0, text.length)
                fun candidate(end: Int): AnnotatedString {
                    var safeEnd = end.coerceIn(0, text.length)
                    if (safeEnd > 0 && safeEnd < text.length &&
                        Character.isHighSurrogate(text[safeEnd - 1]) && Character.isLowSurrogate(text[safeEnd])
                    ) safeEnd--
                    placeholders.firstOrNull { safeEnd > it.start && safeEnd < it.end }?.let { safeEnd = it.start }
                    while (safeEnd > 0 && text[safeEnd - 1].isWhitespace()) safeEnd--
                    return buildAnnotatedString {
                        append(text.subSequence(0, safeEnd))
                        append("…")
                        withLink(
                            LinkAnnotation.Clickable(
                                tag = "expand-full-post",
                                styles = TextLinkStyles(style = SpanStyle(color = themeColor)),
                                linkInteractionListener = { expanded = true },
                            ),
                        ) { append("全文") }
                    }
                }
                var low = 0
                var high = original.getLineEnd(FoldedPostLines - 1, visibleEnd = true)
                while (low < high) {
                    val middle = (low + high + 1) / 2
                    if (measure(candidate(middle)).hasVisualOverflow) high = middle - 1 else low = middle
                }
                candidate(low)
            }
        }
        val canFold = folded !== text
        val foldedHeight = remember(folded, style, constraints.maxWidth) {
            measurer.measure(
                text = folded,
                style = style,
                constraints = Constraints.fixedWidth(constraints.maxWidth),
                placeholders = folded.getStringAnnotations(PostPlaceholderTag, 0, folded.length).mapNotNull { range ->
                    inlineContent[range.item]?.let { AnnotatedString.Range(it.placeholder, range.start, range.end) }
                },
            ).size.height
        }
        val collapse: () -> Unit = {
            scope.launch {
                // Scroll the beginning of the same body into view before reducing its height.
                bringIntoView.bringIntoView(Rect(0f, 0f, constraints.maxWidth.toFloat(), foldedHeight.toFloat()))
                expanded = false
            }
            Unit
        }
        val latestCollapse by rememberUpdatedState(collapse)
        val showFull = expanded || transition.currentState || transition.isRunning
        val surface = MaterialTheme.colorScheme.surface
        val proseBackdrop = rememberLayerBackdrop(onDraw = {
            drawRect(surface)
            drawContent()
        })
        val area = bounds?.let { section -> controls?.viewport?.let(section::intersect) }
        val buttonHeight = with(androidx.compose.ui.platform.LocalDensity.current) { 32.dp.roundToPx() }
        val showCollapse = expanded && canFold && area != null && area.height >= buttonHeight &&
            area.width >= buttonHeight &&
            bounds!!.bottom > (controls?.readingBottom ?: Float.POSITIVE_INFINITY)
        val overlayKey = remember { Any() }
        DisposableEffect(controls, overlayKey) {
            onDispose { controls?.overlayEntries?.remove(overlayKey) }
        }
        SideEffect {
            if (controls?.useOverlay == true && showCollapse) {
                controls.overlayEntries[overlayKey] = LongPostOverlayEntry(
                    bounds!!, controls.viewport!!, controls.readingBottom, controls.rightEdge,
                    { latestCollapse() },
                )
            } else controls?.overlayEntries?.remove(overlayKey)
        }
        Layout(
            modifier = Modifier.fillMaxWidth().onGloballyPositioned { coordinates ->
                val origin = coordinates.localToRoot(Offset.Zero)
                bounds = Rect(origin.x, origin.y, origin.x + coordinates.size.width, origin.y + coordinates.size.height)
            },
            content = {
                Box(
                    Modifier.fillMaxWidth()
                        .bringIntoViewRequester(bringIntoView)
                        .clipToBounds()
                        .layout { measurable, incoming ->
                            val child = measurable.measure(incoming)
                            val height = if (canFold && showFull) {
                                (foldedHeight + (child.height - foldedHeight) * reveal).roundToInt()
                            } else child.height
                            layout(child.width, height) { child.placeRelative(0, 0) }
                        },
                ) {
                    Box(Modifier.fillMaxWidth().layerBackdrop(proseBackdrop)) {
                        content(if (showFull) text else folded)
                    }
                }
                if (showCollapse && controls != null && !controls.useOverlay) {
                    Box(
                        Modifier.drawBottomBarGlassBackdrop(
                            proseBackdrop, controls.frostAmount, controls.glassAmount, controls.lightTheme,
                        ).clip(CircleShape).clickable { latestCollapse() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text("收起全文", color = themeColor, style = MaterialTheme.typography.labelLarge)
                    }
                }
            },
        ) { measurables, incoming ->
            val body = measurables[0].measure(incoming)
            val toggle = measurables.getOrNull(1)?.measure(
                Constraints(maxWidth = body.width, minHeight = buttonHeight, maxHeight = buttonHeight),
            )
            layout(body.width, body.height) {
                body.placeRelative(0, 0)
                val origin = coordinates?.localToRoot(Offset.Zero)
                val readable = controls?.viewport
                if (toggle != null && origin != null && readable != null) {
                    // PR placement uses this frame's prose and viewport intersection.
                    val section = Rect(origin.x, origin.y, origin.x + body.width, origin.y + body.height)
                    val visible = section.intersect(readable)
                    if (visible.height >= toggle.height && visible.width >= toggle.width &&
                        section.bottom > controls.readingBottom) {
                        val right = minOf(controls.rightEdge, visible.right)
                        toggle.place(
                            (right - origin.x - toggle.width).roundToInt(),
                            (visible.bottom - origin.y - toggle.height).roundToInt(),
                        )
                    }
                }
            }
        }
    }
}

internal data class LongPostOverlayEntry(
    val bounds: Rect,
    val viewport: Rect,
    val readingBottom: Float,
    val rightEdge: Float,
    val collapse: () -> Unit,
)

internal class LongPostControls(parent: LongPostControls? = null) {
    val overlayEntries: androidx.compose.runtime.snapshots.SnapshotStateMap<Any, LongPostOverlayEntry> =
        parent?.overlayEntries ?: mutableStateMapOf()
    var useOverlay = false
    var viewport by mutableStateOf<Rect?>(null)
    var readingBottom by mutableStateOf(Float.POSITIVE_INFINITY)
    var rightEdge by mutableStateOf(0f)
    var frostAmount by mutableStateOf(0f)
    var glassAmount by mutableStateOf(0f)
    var lightTheme by mutableStateOf(true)
}

internal val LocalLongPostControls = compositionLocalOf<LongPostControls?> { null }

@Composable
internal fun LongPostCollapseOverlay(
    controls: LongPostControls,
    barBounds: Rect?,
    backdrop: Backdrop,
    frostAmount: Float,
    glassAmount: Float,
    lightTheme: Boolean,
    modifier: Modifier = Modifier,
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val navigationBarHeight = WindowInsets.navigationBars.getBottom(density)
    val gap = with(density) { 12.dp.toPx() }
    var hostBounds by remember { mutableStateOf<Rect?>(null) }
    val currentHost = hostBounds
    val visibleBarBounds = barBounds?.takeIf { bar ->
        val host = currentHost
        host != null && bar.width > 0f && bar.height > 0f &&
            bar.top > host.top && bar.top < host.bottom - navigationBarHeight &&
            bar.right > host.left && bar.left < host.right
    }
    SideEffect {
        controls.readingBottom = visibleBarBounds?.top ?: currentHost?.let { it.bottom - navigationBarHeight }
            ?: Float.POSITIVE_INFINITY
        controls.viewport = currentHost?.let {
            Rect(it.left, it.top, it.right, minOf(it.bottom - navigationBarHeight, visibleBarBounds?.top ?: it.bottom) - gap)
        }
        controls.rightEdge = visibleBarBounds?.right ?: currentHost?.right ?: 0f
        controls.frostAmount = frostAmount
        controls.glassAmount = glassAmount
        controls.lightTheme = lightTheme
    }
    DisposableEffect(controls) {
        onDispose { controls.viewport = null }
    }
    Box(modifier.onGloballyPositioned { coordinates ->
        val origin = coordinates.localToRoot(Offset.Zero)
        hostBounds = Rect(origin.x, origin.y, origin.x + coordinates.size.width, origin.y + coordinates.size.height)
    }) {
        val entry = controls.overlayEntries.values.lastOrNull {
            val visible = it.bounds.intersect(it.viewport)
            visible.width > 0f && visible.height >= with(density) { 32.dp.toPx() } &&
                it.bounds.bottom > it.readingBottom
        }
        val origin = hostBounds
        if (entry != null && origin != null) {
            val visible = entry.bounds.intersect(entry.viewport)
            Layout(content = {
                Box(
                    Modifier.drawBottomBarGlassBackdrop(backdrop, frostAmount, glassAmount, lightTheme)
                        .clip(CircleShape).clickable { entry.collapse() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text("收起全文", color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge)
                }
            }) { measurables, constraints ->
                val height = with(density) { 32.dp.roundToPx() }
                val button = measurables.single().measure(Constraints(maxWidth = constraints.maxWidth, minHeight = height, maxHeight = height))
                layout(constraints.maxWidth, constraints.maxHeight) {
                    button.place(
                        (minOf(entry.rightEdge, visible.right) - origin.left - button.width).roundToInt(),
                        (visible.bottom - origin.top - button.height).roundToInt(),
                    )
                }
            }
        }
    }
}
