package com.example.myweibo.ui

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import com.example.myweibo.data.FeedDisplaySettingsStore
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.isRenderEffectSupported
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

internal const val FloatingLongPostCollapseTag = "long-post-floating-collapse"
internal const val OuterLongPostCardTag = "long-post-outer-card"
internal const val QuotedLongPostCardTag = "long-post-quoted-card"
internal const val LongPostTextSectionTag = "long-post-text-section"

/** One lazy row can own both its own prose and the original prose in its quoted card. */
internal data class HomeLongPostKey(val rowKey: String, val quotedStatusId: String? = null)

internal data class HomeLongPostGlass(val frost: Float = 0f, val refraction: Float = 1f) {
    // No blur and no refraction is the user's unfiltered setting. Use solid instead
    // of showing a second, undistorted copy of the prose behind the label.
    val enabled: Boolean get() = isRenderEffectSupported() && (frost > 0f || refraction > 0f)
}

/** Retain expansion and its pre-open anchor, but only within one Home reading session. */
internal class HomeLongPostReadingSession {
    private var listState: LazyListState? = null
    private var controller: HomeLongPostController? = null

    fun controllerFor(state: LazyListState): HomeLongPostController {
        val current = controller
        if (listState === state && current != null) return current
        return HomeLongPostController(state).also {
            listState = state
            controller = it
        }
    }
}

/** Timeline-owned so an expanded body stays expanded when recycled by LazyColumn. */
internal class HomeLongPostController(
    private val listState: LazyListState,
    private val onBeforeCollapse: () -> Unit = {},
) {
    private data class FoldedAnchor(
        val rowOffset: Int,
        val ownerTopInRow: Float,
        val ownerHeight: Int,
        val viewportWidth: Int,
    )
    private data class CardGeometry(val topInRoot: Float, val height: Int)
    private data class TextGeometry(val bounds: Rect, val origin: Offset, val height: Int, val linkBounds: () -> List<Rect>)
    data class CollapsePlacement(val bounds: Rect, val overProse: Boolean)
    class CollapseMotion(
        val key: HomeLongPostKey,
        val startOffset: Int,
        val targetOffset: Int,
        val restoreFocus: Boolean,
    ) {
        var remaining by mutableFloatStateOf(1f)
    }

    private val expanded = mutableStateMapOf<HomeLongPostKey, FoldedAnchor>()
    private val readyBodies = mutableStateMapOf<HomeLongPostKey, Boolean>()
    private val cards = mutableStateMapOf<HomeLongPostKey, CardGeometry>()
    private val textSections = mutableStateMapOf<HomeLongPostKey, TextGeometry>()
    var readableViewport by mutableStateOf<Rect?>(null)
    var collapseGlass by mutableStateOf(HomeLongPostGlass())
    var activeCollapse by mutableStateOf<CollapseMotion?>(null)
        private set
    private var layoutChangeGeneration by mutableIntStateOf(0)
    var isLayoutPagingSuppressed by mutableStateOf(false)
        private set
    var restoreFocusTo by mutableStateOf<HomeLongPostKey?>(null)
        private set

    fun isExpanded(key: HomeLongPostKey): Boolean = expanded.containsKey(key)

    fun expand(key: HomeLongPostKey) {
        if (key in expanded || activeCollapse != null) return
        val layout = listState.layoutInfo
        val row = layout.visibleItemsInfo.firstOrNull { it.key == key.rowKey } ?: return
        expanded[key] = FoldedAnchor(
            -row.offset, ownerTopInRow(key), cards[key]?.height ?: row.size, layout.viewportSize.width,
        )
    }

    fun setBodyReady(key: HomeLongPostKey, canCollapse: Boolean) {
        readyBodies[key] = canCollapse
    }

    fun removeReadyBody(key: HomeLongPostKey) {
        readyBodies.remove(key)
    }

    fun updateCard(key: HomeLongPostKey, topInRoot: Float, height: Int) {
        cards[key] = CardGeometry(topInRoot, height)
    }

    fun updateTextSection(key: HomeLongPostKey, bounds: Rect, origin: Offset, height: Int, linkBounds: () -> List<Rect>) {
        textSections[key] = TextGeometry(bounds, origin, height, linkBounds)
    }

    fun removeTextSection(key: HomeLongPostKey) {
        textSections.remove(key)
    }

    fun suppressPagingForLayoutChange() {
        isLayoutPagingSuppressed = true
        layoutChangeGeneration++
    }

    suspend fun watchPagingSuppression() {
        snapshotFlow { layoutChangeGeneration }.collectLatest { generation ->
            if (isLayoutPagingSuppressed) {
                // Ignore an in-progress scroll (including one that starts during the
                // height change). A fresh scroll can page only after everything settles.
                snapshotFlow { activeCollapse == null && !listState.isScrollInProgress }.first { it }
                snapshotFlow { activeCollapse == null && listState.isScrollInProgress }.first { it }
                if (generation == layoutChangeGeneration) isLayoutPagingSuppressed = false
            }
        }
    }

    fun removeCard(key: HomeLongPostKey) {
        cards.remove(key)
    }

    fun retainRows(keys: Set<String>) {
        expanded.keys.filter { it.rowKey !in keys }.forEach { expanded.remove(it) }
        readyBodies.keys.filter { it.rowKey !in keys }.forEach { readyBodies.remove(it) }
        cards.keys.filter { it.rowKey !in keys }.forEach { cards.remove(it) }
        textSections.keys.filter { it.rowKey !in keys }.forEach { textSections.remove(it) }
        if (activeCollapse?.let { it.key.rowKey !in keys } == true) activeCollapse = null
        if (restoreFocusTo?.let { it.rowKey !in keys } == true) restoreFocusTo = null
    }

    fun hasVisibleCollapse(key: HomeLongPostKey, preferredWidth: Int, buttonHeight: Int, alignRight: Boolean): Boolean {
        val text = textSections[key] ?: return false
        if (listState.layoutInfo.visibleItemsInfo.none { it.key == key.rowKey }) return false
        return collapsePlacement(text.bounds, text.origin, text.height, text.linkBounds(), preferredWidth, buttonHeight, alignRight) != null
    }

    fun collapsePlacement(
        sectionBounds: Rect,
        sectionOrigin: Offset,
        sectionHeight: Int,
        linkBounds: List<Rect>,
        preferredWidth: Int,
        buttonHeight: Int,
        alignRight: Boolean,
    ): CollapsePlacement? {
        val viewport = readableViewport ?: return null
        val area = sectionBounds.intersect(viewport)
        val left = ceil(area.left - sectionOrigin.x)
        val top = ceil(area.top - sectionOrigin.y)
        val right = floor(area.right - sectionOrigin.x)
        val bottom = floor(area.bottom - sectionOrigin.y)
        val buttonWidth = minOf(preferredWidth, (right - left).toInt())
        if (buttonWidth < buttonHeight || bottom - top < buttonHeight) return null
        val x = if (alignRight) right - buttonWidth else left
        // Prefer the section's normal end-aligned slot. While floating over prose,
        // move above actual inline links rather than covering their hit targets.
        var button = Rect(x, bottom - buttonHeight, x + buttonWidth, bottom)
        val links = linkBounds.filter {
            it.left < button.right && it.right > button.left && it.bottom > top && it.top < bottom
        }.sortedByDescending { it.bottom }
        for (link in links) {
            if (button.overlaps(link)) {
                val y = floor(link.top)
                button = Rect(x, y - buttonHeight, x + buttonWidth, y)
                if (button.top < top) return null
            }
        }
        return CollapsePlacement(button, button.top < sectionHeight - buttonHeight)
    }

    fun collapse(key: HomeLongPostKey, readable: Rect, minVisiblePx: Int, restoreFocus: Boolean) {
        if (activeCollapse != null || readyBodies[key] != true) return
        val anchor = expanded[key] ?: return
        val layout = listState.layoutInfo
        val row = layout.visibleItemsInfo.firstOrNull { it.key == key.rowKey } ?: return
        val ownerTop = ownerTopInRow(key)
        // Changing the outer body can move the quote within this row. Preserve the
        // owner's signed pre-open position rather than its old distance from the row start.
        var ownerOffset = anchor.rowOffset - anchor.ownerTopInRow
        if (layout.viewportSize.width != anchor.viewportWidth) {
            // Reflow can change the card height or available viewport. Keep the original
            // offset unless it would leave too little of the folded card on screen.
            val outer = cards[HomeLongPostKey(key.rowKey)] ?: return
            val origin = outer.topInRoot - row.offset
            val foldedHeight = minOf(anchor.ownerHeight, cards[key]?.height ?: row.size)
            val visibleHeight = minOf(minVisiblePx.toFloat(), foldedHeight.toFloat(), readable.height)
            ownerOffset = ownerOffset.coerceIn(
                visibleHeight - (readable.bottom - origin),
                foldedHeight - visibleHeight - (readable.top - origin),
            )
        }
        val offset = (ownerTop + ownerOffset).roundToInt()
        activeCollapse = CollapseMotion(key, -row.offset, offset, restoreFocus)
        suppressPagingForLayoutChange()
        onBeforeCollapse()
    }

    suspend fun animateCollapse(motion: CollapseMotion) {
        try {
            // Drive both the row offset and its body's measured height from the same
            // frame and easing. In particular, the first frame retains the mid-read
            // offset rather than jumping to the folded anchor before the height changes.
            animate(initialValue = 1f, targetValue = 0f, animationSpec = tween(LongPostCollapseMillis)) { remaining, _ ->
                if (activeCollapse === motion) {
                    val offset = (motion.targetOffset + (motion.startOffset - motion.targetOffset) * remaining).roundToInt()
                    // Resolve against the keyed row each time, never a captured index.
                    val row = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == motion.key.rowKey }
                    if (row != null && row.offset != -offset) listState.requestScrollToItem(row.index, offset)
                    motion.remaining = remaining
                }
            }
            currentCoroutineContext().ensureActive()
            if (activeCollapse === motion) {
                expanded.remove(motion.key)
                if (motion.restoreFocus) restoreFocusTo = motion.key
            }
        } finally {
            if (activeCollapse === motion) activeCollapse = null
        }
    }

    fun detachViewport() {
        // The animation may not have been launched yet, so clear the motion as well as
        // cancelling the host's effect. Keep the expansion and the original folded anchor.
        activeCollapse = null
        readableViewport = null
    }

    private fun ownerTopInRow(key: HomeLongPostKey): Float {
        if (key.quotedStatusId == null) return 0f
        val owner = cards[key] ?: return 0f
        val outer = cards[HomeLongPostKey(key.rowKey)] ?: return 0f
        return owner.topInRoot - outer.topInRoot
    }

    fun consumeRestoreFocus(key: HomeLongPostKey) {
        if (restoreFocusTo == key) restoreFocusTo = null
    }
}

internal data class HomeLongPost(val controller: HomeLongPostController, val key: HomeLongPostKey) {
    constructor(controller: HomeLongPostController, rowKey: String) : this(controller, HomeLongPostKey(rowKey))

    fun quote(statusId: String): HomeLongPost = copy(key = key.copy(quotedStatusId = statusId))
}

/** Register the painted card only at the home call sites that opt into reading controls. */
@Composable
internal fun Modifier.homeLongPostCard(homePost: HomeLongPost?): Modifier {
    if (homePost == null) return this
    DisposableEffect(homePost) {
        onDispose { homePost.controller.removeCard(homePost.key) }
    }
    val tag = if (homePost.key.quotedStatusId == null) OuterLongPostCardTag else QuotedLongPostCardTag
    return testTag(tag).onGloballyPositioned {
        homePost.controller.updateCard(homePost.key, it.positionInRoot().y, it.size.height)
    }
}

/** Keep the toggle in the prose layout so changing row geometry cannot leave it a frame behind. */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun HomeLongPostTextSection(
    homePost: HomeLongPost?,
    showCollapse: Boolean,
    settled: Boolean,
    linkBounds: () -> List<Rect>,
    content: @Composable (Modifier) -> Unit,
) {
    if (homePost == null) {
        content(Modifier)
        return
    }
    val controller = homePost.controller
    val density = LocalDensity.current
    val alignRight = LocalLayoutDirection.current == LayoutDirection.Ltr
    val buttonHeight = with(density) { LongPostToggleMinSize.roundToPx() }
    val buttonWidth = if (showCollapse) collapseToggleWidth() else buttonHeight
    var overProse by remember(homePost) { mutableStateOf(false) }
    val drawBacking = remember(homePost) { { overProse } }
    var hasKeyboardFocus by remember(homePost) { mutableStateOf(false) }
    val visible by remember(homePost, buttonWidth, buttonHeight, alignRight) {
        derivedStateOf { controller.hasVisibleCollapse(homePost.key, buttonWidth, buttonHeight, alignRight) }
    }
    val glass = controller.collapseGlass
    val surface = longPostToggleSurface(homePost.key.quotedStatusId != null)
    // Capture only the clipped prose sibling, never the toggle or the feed layer
    // that already contains it. Sampling either would create a RenderNode cycle.
    val backdrop = if (showCollapse && visible && glass.enabled) {
        val drawProse = remember<ContentDrawScope.() -> Unit>(surface) {
            {
                drawRect(surface)
                drawContent()
            }
        }
        rememberLayerBackdrop(onDraw = drawProse)
    } else null
    DisposableEffect(homePost) {
        onDispose { controller.removeTextSection(homePost.key) }
    }
    Layout(
        modifier = Modifier.fillMaxWidth().testTag(LongPostTextSectionTag).clipToBounds()
            .onGloballyPositioned {
                controller.updateTextSection(homePost.key, it.boundsInRoot(), it.positionInRoot(), it.size.height, linkBounds)
            },
        content = {
            content(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier)
            if (showCollapse) {
                // Compose the candidate on a cold row. Only placement exposes it; previous
                // global coordinates are useful for the glass layer, not for gating the button.
                LongPostToggle(
                    expanded = true,
                    surface = surface,
                    floating = true,
                    overProse = drawBacking,
                    glass = glass,
                    backdrop = backdrop,
                    enabled = settled && controller.activeCollapse == null,
                    onKeyboardFocusChanged = { hasKeyboardFocus = it },
                    onClick = {
                        controller.readableViewport?.let { readable ->
                            controller.collapse(homePost.key, readable, buttonHeight, hasKeyboardFocus)
                        }
                    },
                )
            }
        },
    ) { measurables, constraints ->
        val body = measurables[0].measure(constraints)
        val width = minOf(buttonWidth, body.width)
        val toggle = measurables.getOrNull(1)?.takeIf { width >= buttonHeight }
            ?.measure(Constraints.fixed(width, buttonHeight))
        layout(body.width, body.height) {
            body.placeRelative(0, 0)
            // Read placement coordinates here, not in composition: both current scroll
            // and this frame's measured text height bound the toggle before it is placed.
            val section = coordinates
            if (toggle != null && section != null) {
                val origin = section.positionInRoot()
                val sectionBounds = Rect(origin, Size(body.width.toFloat(), body.height.toFloat()))
                    .intersect(section.boundsInRoot())
                val target = controller.collapsePlacement(
                    sectionBounds, origin, body.height, linkBounds(), toggle.width, buttonHeight, alignRight,
                )
                if (target != null && target.bounds.width >= toggle.width) {
                    overProse = target.overProse
                    toggle.place(target.bounds.left.toInt(), target.bounds.top.toInt())
                }
            }
        }
    }
}

/** Publish the real readable bounds before placing feed rows, including on a cold remount. */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun Modifier.homeLongPostViewport(
    controller: HomeLongPostController,
    navTopInRoot: () -> Float,
    topContentInsetPx: Float,
): Modifier {
    controller.activeCollapse?.let { motion ->
        LaunchedEffect(controller, motion) { controller.animateCollapse(motion) }
    }
    val context = LocalContext.current
    val preferences = remember(context) { FeedDisplaySettingsStore(context) }
    DisposableEffect(controller, preferences) {
        val stop = preferences.observeBottomBarGlass { frost, refraction ->
            controller.collapseGlass = HomeLongPostGlass(frost, refraction)
        }
        onDispose { stop() }
    }
    DisposableEffect(controller) {
        onDispose { controller.detachViewport() }
    }
    val systemBottom = WindowInsets.navigationBars.getBottom(LocalDensity.current)
    return layout { measurable, constraints ->
        val feed = measurable.measure(constraints)
        layout(feed.width, feed.height) {
            coordinates?.boundsInRoot()?.let { bounds ->
                controller.readableViewport = Rect(
                    bounds.left,
                    bounds.top + topContentInsetPx,
                    bounds.right,
                    minOf(bounds.bottom - systemBottom, navTopInRoot()),
                ).takeIf { it.bottom > it.top }
            }
            feed.placeRelative(0, 0)
        }
    }
}
