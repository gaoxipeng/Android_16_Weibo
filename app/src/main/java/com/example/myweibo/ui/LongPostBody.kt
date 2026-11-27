package com.example.myweibo.ui

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myweibo.R
import com.example.myweibo.ui.liquidglass.drawBottomBarGlassBackdrop
import com.kyant.backdrop.Backdrop
import kotlin.math.roundToInt

/** Set by the home timeline; only renderers explicitly passed this value opt in. */
internal val LocalLongPostLineLimit = compositionLocalOf<Int?> { null }

internal const val LongPostToggleTag = "long-post-toggle"
internal const val LongPostCollapseGlassTag = "long-post-collapse-glass"
internal val LongPostToggleMinSize = 48.dp
private val FloatingCollapseSidePadding = 8.dp
private val FloatingCollapseVerticalInset = 6.dp
private val LongPostChevronSize = 16.dp
private val LongPostChevronGap = 4.dp
private const val LongPostExpandMillis = 220
internal const val LongPostCollapseMillis = 200

/** Renders one real text node and clips its revealed height while it changes. */
@Composable
internal fun LongPostBody(
    contentKey: String,
    lineLimit: Int?,
    initiallyExpanded: Boolean = false,
    homePost: HomeLongPost? = null,
    content: @Composable (Int, TextOverflow, ((TextLayoutResult) -> Unit)?) -> Unit,
) {
    if (lineLimit == null || lineLimit <= 0 || lineLimit == Int.MAX_VALUE) {
        content(Int.MAX_VALUE, TextOverflow.Clip, null)
        return
    }

    key(contentKey, lineLimit) {
        var fallbackExpanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
        val expanded = if (homePost != null) homePost.controller.isExpanded(homePost.key) else fallbackExpanded
        var canFold by remember { mutableStateOf(false) }
        var foldedHeight by remember { mutableIntStateOf(0) }
        var linkBounds by remember { mutableStateOf(emptyList<Rect>()) }
        val probeLimit = lineLimit + 1
        val textMeasurer = rememberTextMeasurer(cacheSize = 2)
        val transition = updateTransition(expanded, label = "long-post")
        val collapseMotion = homePost?.let { owner -> owner.controller.activeCollapse?.takeIf { it.key == owner.key } }
        // Compose's tween uses the system animator duration scale, including zero.
        val reveal = transition.animateFloat(
            transitionSpec = {
                when {
                    targetState -> tween(LongPostExpandMillis)
                    homePost != null -> snap() // The home controller already drove the shrink and scroll together.
                    else -> tween(LongPostCollapseMillis)
                }
            },
            label = "text-height",
        ) { if (it) 1f else 0f }
        val settled = !transition.isRunning && transition.currentState == expanded
        val showFullText = expanded || (homePost == null && (transition.currentState || transition.isRunning))
        val hasInlineControl = canFold && (homePost == null || !expanded)

        if (homePost != null && expanded && canFold) {
            SideEffect { homePost.controller.setBodyReady(homePost.key, settled) }
            DisposableEffect(homePost) {
                onDispose { homePost.controller.removeReadyBody(homePost.key) }
            }
        }

        val currentLinkBounds = remember { { linkBounds } }
        HomeLongPostTextSection(homePost, expanded && canFold, settled, currentLinkBounds) { backdropSource ->
            Column(Modifier.fillMaxWidth().then(backdropSource)) {
                Box(
                    Modifier.fillMaxWidth().clipToBounds().layout { measurable, constraints ->
                        val text = measurable.measure(constraints)
                        val collapsed = foldedHeight
                        val height = if (showFullText && canFold && collapsed in 1 until text.height) {
                            val fraction = collapseMotion?.remaining ?: reveal.value
                            (collapsed + (text.height - collapsed) * fraction).roundToInt()
                        } else text.height
                        layout(text.width, height) { text.placeRelative(0, 0) }
                    },
                ) {
                    content(
                        if (showFullText) Int.MAX_VALUE else if (canFold) lineLimit else probeLimit,
                        if (!showFullText && canFold) TextOverflow.Ellipsis else TextOverflow.Clip,
                        { layout ->
                            val input = layout.layoutInput
                            fun measure(maxLines: Int, overflow: TextOverflow) = textMeasurer.measure(
                                text = input.text,
                                style = input.style,
                                overflow = overflow,
                                softWrap = input.softWrap,
                                maxLines = maxLines,
                                placeholders = input.placeholders,
                                constraints = input.constraints,
                                layoutDirection = input.layoutDirection,
                                density = input.density,
                                fontFamilyResolver = input.fontFamilyResolver,
                            )
                            canFold = when (input.maxLines) {
                                Int.MAX_VALUE -> layout.lineCount > probeLimit
                                probeLimit -> layout.multiParagraph.didExceedMaxLines
                                // Recheck an already-capped body after reflow, off-tree so it
                                // never duplicates link semantics or adds a second Text node.
                                else -> measure(probeLimit, TextOverflow.Clip).multiParagraph.didExceedMaxLines
                            }
                            if (canFold) {
                                foldedHeight = if (input.maxLines == lineLimit) layout.size.height
                                    else measure(lineLimit, TextOverflow.Ellipsis).size.height
                            }
                            linkBounds = if (homePost != null && showFullText) {
                                input.text.getLinkAnnotations(0, input.text.length).mapNotNull { link ->
                                    if (link.start < link.end) layout.getPathForRange(link.start, link.end).getBounds()
                                    else null
                                }
                            } else emptyList()
                        },
                    )
                }
                when {
                    hasInlineControl -> {
                        val focusRequester = remember { FocusRequester() }
                        if (homePost != null && homePost.controller.restoreFocusTo == homePost.key) {
                            LaunchedEffect(homePost) {
                                focusRequester.requestFocus()
                                homePost.controller.consumeRestoreFocus(homePost.key)
                            }
                        }
                        LongPostToggle(
                            expanded = expanded,
                            surface = longPostToggleSurface(homePost?.key?.quotedStatusId != null),
                            modifier = Modifier.align(Alignment.End).then(
                                if (homePost != null) Modifier.focusRequester(focusRequester) else Modifier,
                            ),
                            enabled = homePost != null || settled,
                            onClick = {
                                if (homePost != null) homePost.controller.expand(homePost.key)
                                else if (settled) fallbackExpanded = !expanded
                            },
                        )
                    }
                    // Keep the same end-aligned slot, without a duplicate focus/semantics node.
                    canFold -> Spacer(Modifier.height(LongPostToggleMinSize))
                }
            }
        }
    }
}

@Composable
internal fun longPostToggleSurface(quoted: Boolean): Color = with(MaterialTheme.colorScheme) {
    if (quoted) background else surface
}

@Composable
private fun longPostToggleStyle() = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)

@Composable
internal fun collapseToggleWidth(): Int {
    val density = LocalDensity.current
    val label = stringResource(R.string.long_post_collapse)
    val style = longPostToggleStyle()
    val labelWidth = rememberTextMeasurer().measure(label, style = style, maxLines = 1).size.width
    return with(density) {
        maxOf(
            LongPostToggleMinSize.roundToPx(),
            labelWidth + LongPostChevronGap.roundToPx() + LongPostChevronSize.roundToPx() +
                2 * FloatingCollapseSidePadding.roundToPx(),
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun LongPostToggle(
    expanded: Boolean,
    surface: Color,
    modifier: Modifier = Modifier,
    floating: Boolean = false,
    overProse: (() -> Boolean)? = null,
    glass: HomeLongPostGlass = HomeLongPostGlass(),
    backdrop: Backdrop? = null,
    enabled: Boolean = true,
    onKeyboardFocusChanged: ((Boolean) -> Unit)? = null,
    onClick: () -> Unit,
) {
    val link = MaterialTheme.colorScheme.primary
    val linkLuminance = link.luminance()
    val surfaceLuminance = surface.luminance()
    val contrast = (maxOf(linkLuminance, surfaceLuminance) + 0.05f) /
        (minOf(linkLuminance, surfaceLuminance) + 0.05f)
    val onSurface = MaterialTheme.colorScheme.onSurface
    val hasBacking = overProse?.invoke() == true
    val color = if (!hasBacking && contrast >= 4.5f) link else onSurface
    Box(
        modifier = modifier
            .height(LongPostToggleMinSize)
            .defaultMinSize(minWidth = LongPostToggleMinSize)
            .testTag(if (floating) FloatingLongPostCollapseTag else LongPostToggleTag)
            .semantics { testTagsAsResourceId = true }
            .then(if (onKeyboardFocusChanged != null) Modifier.onFocusChanged {
                onKeyboardFocusChanged(it.isFocused)
            } else Modifier)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
    ) {
        if (overProse != null) {
            // This is a small visual pill within the unchanged 48 dp touch/placement slot.
            // Read placement in draw so the backing follows the geometry on the same frame.
            val capsule = lerp(surface.copy(alpha = 1f), onSurface, 0.055f)
            val backing = if (backdrop != null) {
                Modifier.drawBottomBarGlassBackdrop(
                    backdrop = backdrop,
                    frostAmount = glass.frost.coerceAtLeast(0.67f),
                    glassAmount = glass.refraction * 0.50f,
                    surfaceColor = capsule.copy(alpha = 0.94f),
                )
            } else {
                Modifier.background(capsule, CircleShape)
                    .border(1.dp, onSurface.copy(alpha = 0.16f), CircleShape)
            }
            Box(
                Modifier.matchParentSize()
                    .padding(vertical = FloatingCollapseVerticalInset)
                    .then(if (hasBacking && backdrop != null) Modifier.testTag(LongPostCollapseGlassTag) else Modifier)
                    .drawWithContent { if (overProse()) drawContent() }
                    .then(backing),
            )
        }
        Row(
            modifier = Modifier.align(Alignment.CenterEnd)
                .padding(horizontal = if (hasBacking) FloatingCollapseSidePadding else 0.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(if (expanded) R.string.long_post_collapse else R.string.long_post_expand),
                color = color,
                style = longPostToggleStyle(),
                modifier = Modifier.weight(1f, fill = false),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.width(LongPostChevronGap))
            Icon(
                painter = painterResource(R.drawable.ic_chevron_down),
                contentDescription = null,
                modifier = Modifier.size(LongPostChevronSize).rotate(if (expanded) 180f else 0f),
                tint = color,
            )
        }
    }
}
