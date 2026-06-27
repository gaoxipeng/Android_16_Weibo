package com.example.myweibo.ui.liquidglass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceAtMost
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.BackdropEffectScope
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow
import dev.chrisbanes.haze.HazeState
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tanh

internal val LiquidGlassBlurRadius = 4.dp
internal val LiquidMenuGlassBlurRadius = 24.dp
internal val LiquidMenuGlassIdleBlurRadius = 48.dp
internal val LiquidMenuBorderWidth = 0.5.dp

internal fun liquidMenuBorderColor(isLightTheme: Boolean): Color =
    if (isLightTheme) Color(0x24000000) else Color(0x33FFFFFF)

internal fun DrawScope.drawLiquidTintedSurface(
    tint: Color,
    surfaceColor: Color,
) {
    if (tint.isSpecified) {
        drawRect(tint, blendMode = BlendMode.Hue)
        drawRect(tint.copy(alpha = 0.75f))
    } else {
        drawRect(surfaceColor)
    }
}

internal fun BackdropEffectScope.liquidMenuGlassEffects() {
    vibrancy()
    blur(LiquidMenuGlassBlurRadius.toPx())
    lens(12f.dp.toPx(), 24f.dp.toPx())
}

internal val LocalHazeState = staticCompositionLocalOf<HazeState?> { null }
internal val LocalLiquidMenuBackdrop = staticCompositionLocalOf<Backdrop?> { null }

@Composable
fun LiquidButton(
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = true,
    inputEnabled: Boolean = true,
    tint: Color = Color.Unspecified,
    surfaceColor: Color = Color.Unspecified,
    onDoubleClick: (() -> Unit)? = null,
    useMenuGlassStyle: Boolean = false,
    showEdgeEffects: Boolean = true,
    showMenuBorder: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    val animationScope = rememberCoroutineScope()
    val interactiveHighlight = remember(animationScope) {
        InteractiveHighlight(animationScope = animationScope)
    }
    val interactionModifier =
        if (!inputEnabled) {
            Modifier
        } else if (onDoubleClick != null) {
            Modifier.pointerInput(onClick, onDoubleClick) {
                detectTapGestures(
                    onTap = { onClick() },
                    onDoubleTap = { onDoubleClick() },
                )
            }
        } else {
            Modifier.clickable(
                interactionSource = null,
                indication = if (isInteractive) null else LocalIndication.current,
                role = Role.Button,
                onClick = onClick,
            )
        }

    val pillShape = RoundedCornerShape(percent = 50)
    val isLightTheme = !isSystemInDarkTheme()
    val menuBorderColor = liquidMenuBorderColor(isLightTheme)

    Row(
        modifier
            .graphicsLayer { clip = false }
            .drawBackdrop(
                backdrop = backdrop,
                shape = { pillShape },
                effects = {
                    if (useMenuGlassStyle) {
                        liquidMenuGlassEffects()
                    } else {
                        vibrancy()
                        blur(LiquidGlassBlurRadius.toPx())
                        lens(12f.dp.toPx(), 24f.dp.toPx())
                    }
                },
                highlight = if (!showEdgeEffects || useMenuGlassStyle) null else ({ Highlight.Default }),
                shadow = if (!showEdgeEffects || useMenuGlassStyle) null else ({ Shadow.Default }),
                layerBlock = if (isInteractive) {
                    {
                        val width = size.width
                        val height = size.height
                        val progress = interactiveHighlight.pressProgress
                        val scale = lerp(1f, 1f + 4f.dp.toPx() / size.height, progress)
                        val maxOffset = size.minDimension
                        val initialDerivative = 0.05f
                        val offset = interactiveHighlight.offset
                        translationX = maxOffset * tanh(initialDerivative * offset.x / maxOffset)
                        translationY = maxOffset * tanh(initialDerivative * offset.y / maxOffset)
                        val maxDragScale = 4f.dp.toPx() / size.height
                        val offsetAngle = atan2(offset.y, offset.x)
                        scaleX =
                            scale +
                                maxDragScale * abs(cos(offsetAngle) * offset.x / size.maxDimension) *
                                (width / height).fastCoerceAtMost(1f)
                        scaleY =
                            scale +
                                maxDragScale * abs(sin(offsetAngle) * offset.y / size.maxDimension) *
                                (height / width).fastCoerceAtMost(1f)
                    }
                } else {
                    null
                },
                onDrawSurface = {
                    if (tint.isSpecified) {
                        drawLiquidTintedSurface(tint = tint, surfaceColor = surfaceColor)
                    } else if (surfaceColor.isSpecified) {
                        drawRect(surfaceColor)
                    }
                },
            )
            .then(
                if (useMenuGlassStyle || showMenuBorder) {
                    Modifier.border(LiquidMenuBorderWidth, menuBorderColor, pillShape)
                } else {
                    Modifier
                },
            )
            .then(interactionModifier)
            .then(
                if (isInteractive && inputEnabled) {
                    Modifier
                        .then(interactiveHighlight.modifier)
                        .then(interactiveHighlight.gestureModifier)
                } else {
                    Modifier
                },
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

fun liquidSurfaceColor(isLightTheme: Boolean): Color =
    if (isLightTheme) Color.White.copy(0.48f) else Color.White.copy(0.22f)

fun liquidIdleSurfaceColor(isLightTheme: Boolean): Color =
    if (isLightTheme) Color(0xFFF5F5F5) else Color(0xFF2C2C2C)

@Composable
fun SurfaceLiquidCapsule(
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    pill: Boolean = false,
    cornerRadius: Dp = 22.dp,
    useMenuGlassStyle: Boolean = false,
    tint: Color = Color.Unspecified,
    content: @Composable BoxScope.() -> Unit,
) {
    val resolvedBackdrop = backdrop ?: LocalLiquidMenuBackdrop.current
    val isLightTheme = !isSystemInDarkTheme()
    val surfaceColor = liquidSurfaceColor(isLightTheme)
    val menuBorderColor = liquidMenuBorderColor(isLightTheme)
    val shape = if (pill) RoundedCornerShape(percent = 50) else RoundedCornerShape(cornerRadius)

    if (resolvedBackdrop != null) {
        Box(
            modifier
                .graphicsLayer { clip = false }
                .drawBackdrop(
                    backdrop = resolvedBackdrop,
                    shape = { shape },
                    effects = {
                        if (useMenuGlassStyle) {
                            liquidMenuGlassEffects()
                        } else {
                            vibrancy()
                            blur(LiquidGlassBlurRadius.toPx())
                            lens(12f.dp.toPx(), 24f.dp.toPx())
                        }
                    },
                    highlight = if (useMenuGlassStyle) null else ({ Highlight.Default }),
                    shadow = if (useMenuGlassStyle) null else ({ Shadow.Default }),
                    onDrawSurface = {
                        drawLiquidTintedSurface(tint = tint, surfaceColor = surfaceColor)
                    },
                )
                .then(
                    if (useMenuGlassStyle) {
                        Modifier.border(LiquidMenuBorderWidth, menuBorderColor, shape)
                    } else {
                        Modifier
                    },
                ),
            content = content,
        )
    } else {
        Box(
            modifier
                .clip(shape)
                .background(
                    if (tint.isSpecified) tint.copy(alpha = 0.82f) else surfaceColor,
                    shape,
                )
                .then(
                    if (useMenuGlassStyle) {
                        Modifier.border(LiquidMenuBorderWidth, menuBorderColor, shape)
                    } else {
                        Modifier
                    },
                ),
            content = content,
        )
    }
}

@Composable
fun SurfaceLiquidTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backdrop: Backdrop? = null,
    textColor: Color = Color.Unspecified,
    style: TextStyle? = null,
) {
    val resolvedBackdrop = backdrop ?: LocalLiquidMenuBackdrop.current
    val isLightTheme = !isSystemInDarkTheme()
    val resolvedTextColor = when {
        textColor.isSpecified -> textColor
        isLightTheme -> Color(0xFF1F1F1F)
        else -> Color.White
    }
    val textStyle = style ?: MaterialTheme.typography.labelMedium

    if (resolvedBackdrop != null) {
        LiquidButton(
            onClick = onClick,
            backdrop = resolvedBackdrop,
            modifier = modifier,
            isInteractive = enabled,
            surfaceColor = liquidSurfaceColor(isLightTheme),
        ) {
            Text(
                text = text,
                color = resolvedTextColor,
                style = textStyle,
                textAlign = TextAlign.Center,
            )
        }
    } else {
        SurfaceLiquidCapsule(
            modifier = modifier.clickable(enabled = enabled, onClick = onClick),
            pill = true,
        ) {
            Text(
                text = text,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 12.dp),
                color = resolvedTextColor,
                style = textStyle,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun TransparentLiquidCapsule(
    modifier: Modifier = Modifier,
    backdrop: Backdrop,
    pill: Boolean = false,
    cornerRadius: Dp = 22.dp,
    surfaceColor: Color = Color.Unspecified,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = if (pill) RoundedCornerShape(percent = 50) else RoundedCornerShape(cornerRadius)
    val isLightTheme = !isSystemInDarkTheme()
    val borderColor = liquidMenuBorderColor(isLightTheme)
    Box(
        modifier
            .graphicsLayer { clip = false }
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    blur(LiquidGlassBlurRadius.toPx())
                    lens(12f.dp.toPx(), 24f.dp.toPx())
                },
                highlight = null,
                shadow = null,
                onDrawSurface = {
                    if (surfaceColor.isSpecified) {
                        drawRect(surfaceColor)
                    }
                },
            )
            .border(LiquidMenuBorderWidth, borderColor, shape),
        content = content,
    )
}

@Composable
fun TransparentLiquidTextButton(
    text: String,
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textColor: Color = Color.White,
    style: TextStyle? = null,
) {
    LiquidButton(
        onClick = onClick,
        backdrop = backdrop,
        modifier = modifier,
        isInteractive = enabled,
        showEdgeEffects = false,
        showMenuBorder = true,
    ) {
        Text(
            text = text,
            color = textColor,
            style = style ?: MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun SurfaceLiquidMenuCard(
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    cornerRadius: Dp = 14.dp,
    blurRadius: Dp = LiquidGlassBlurRadius,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val isLightTheme = !isSystemInDarkTheme()
    val shape = RoundedCornerShape(cornerRadius)
    val surfaceColor = liquidSurfaceColor(isLightTheme)
    val borderColor = liquidMenuBorderColor(isLightTheme)

    if (backdrop != null) {
        Column(
            modifier
                .graphicsLayer { clip = false }
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { shape },
                    effects = {
                        vibrancy()
                        blur(blurRadius.toPx())
                        lens(12f.dp.toPx(), 24f.dp.toPx())
                    },
                    highlight = null,
                    shadow = null,
                    onDrawSurface = { drawRect(surfaceColor) },
                )
                .border(LiquidMenuBorderWidth, borderColor, shape)
                .padding(contentPadding),
            horizontalAlignment = Alignment.Start,
            content = content,
        )
    } else {
        Column(
            modifier
                .clip(shape)
                .background(surfaceColor, shape)
                .border(LiquidMenuBorderWidth, borderColor, shape)
                .padding(contentPadding),
            horizontalAlignment = Alignment.Start,
            content = content,
        )
    }
}

@Composable
fun SurfaceLiquidIconButton(
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    onDoubleClick: (() -> Unit)? = null,
    isInteractive: Boolean = true,
    inputEnabled: Boolean = true,
    useMenuGlassStyle: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    LiquidButton(
        onClick = onClick,
        backdrop = backdrop,
        modifier = modifier,
        onDoubleClick = onDoubleClick,
        isInteractive = isInteractive,
        inputEnabled = inputEnabled,
        useMenuGlassStyle = useMenuGlassStyle,
        surfaceColor = liquidSurfaceColor(!isSystemInDarkTheme()),
        content = content,
    )
}

@Composable
fun TransparentLiquidIconButton(
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    onDoubleClick: (() -> Unit)? = null,
    isInteractive: Boolean = true,
    inputEnabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    LiquidButton(
        onClick = onClick,
        backdrop = backdrop,
        modifier = modifier,
        onDoubleClick = onDoubleClick,
        isInteractive = isInteractive,
        inputEnabled = inputEnabled,
        showEdgeEffects = false,
        showMenuBorder = true,
        content = content,
    )
}
