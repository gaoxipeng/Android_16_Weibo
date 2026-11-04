package com.kyant.backdrop.backdrops

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.Density
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.internal.InverseLayerScope

private val DefaultOnDraw: ContentDrawScope.() -> Unit = { drawContent() }

@Composable
fun rememberLayerBackdrop(
    graphicsLayer: GraphicsLayer = rememberGraphicsLayer(),
    onDraw: ContentDrawScope.() -> Unit = DefaultOnDraw
): LayerBackdrop {
    return remember(graphicsLayer, onDraw) {
        LayerBackdrop(graphicsLayer, onDraw)
    }
}

@Stable
class LayerBackdrop internal constructor(
    val graphicsLayer: GraphicsLayer,
    internal val onDraw: ContentDrawScope.() -> Unit
) : Backdrop {

    override val isCoordinatesDependent: Boolean = true

    internal var layerCoordinates: LayoutCoordinates? by mutableStateOf(null)
    internal var lastLayerPositionInWindow: Offset? by mutableStateOf(null)
    internal var isFrozen: Boolean by mutableStateOf(false)

    private var inverseLayerScope: InverseLayerScope? = null

    override fun DrawScope.drawBackdrop(
        density: Density,
        coordinates: LayoutCoordinates?,
        layerBlock: (GraphicsLayerScope.() -> Unit)?
    ) {
        val coordinates = coordinates ?: return
        val layerCoordinates = layerCoordinates
        val sourcePositionInWindow = layerCoordinates
            ?.let { runCatching { it.positionInWindow() }.getOrNull() }
            ?: lastLayerPositionInWindow
            ?: return
        withTransform({
            if (layerBlock != null) {
                with(obtainInverseLayerScope()) { inverseTransform(density, layerBlock) }
            }
            val offset = if (layerCoordinates != null) {
                try {
                    layerCoordinates.localPositionOf(coordinates)
                } catch (_: Exception) {
                    // TODO: outer transformations lead to wrong position calculation
                    coordinates.positionInWindow() - sourcePositionInWindow
                }
            } else {
                // The source can detach for a frame while navigation is handing off
                // its page. Keep drawing its last recorded layer at the last known
                // window position instead of exposing an empty/transparent backdrop.
                coordinates.positionInWindow() - sourcePositionInWindow
            }
            translate(-offset.x, -offset.y)
        }) {
            drawLayer(graphicsLayer)
        }
    }

    private fun obtainInverseLayerScope(): InverseLayerScope {
        return inverseLayerScope?.apply { reset() }
            ?: InverseLayerScope().also { inverseLayerScope = it }
    }
}
