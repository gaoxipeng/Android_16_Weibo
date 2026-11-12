package com.kyant.backdrop.backdrops

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import com.kyant.backdrop.internal.recordLayer

fun Modifier.layerBackdrop(backdrop: LayerBackdrop, drawSource: Boolean = true): Modifier =
    this then LayerBackdropElement(backdrop, drawSource)

private class LayerBackdropElement(
    val backdrop: LayerBackdrop,
    val drawSource: Boolean,
) : ModifierNodeElement<LayerBackdropNode>() {

    override fun create(): LayerBackdropNode {
        return LayerBackdropNode(backdrop, drawSource)
    }

    override fun update(node: LayerBackdropNode) {
        if (node.backdrop != backdrop) {
            node.backdrop.layerCoordinates = null
            node.backdrop = backdrop
        }
        node.drawSource = drawSource
        node.invalidateDraw()
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "layerBackdrop"
        properties["backdrop"] = backdrop
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is LayerBackdropElement && backdrop == other.backdrop && drawSource == other.drawSource
    }

    override fun hashCode(): Int {
        return 31 * backdrop.hashCode() + drawSource.hashCode()
    }
}

private class LayerBackdropNode(
    var backdrop: LayerBackdrop,
    var drawSource: Boolean,
) : DrawModifierNode, GlobalPositionAwareModifierNode, Modifier.Node() {

    override fun ContentDrawScope.draw() {
        if (drawSource) drawContent()
        if (!backdrop.isFrozen) {
            recordLayer(backdrop.graphicsLayer) { backdrop.onDraw(this@draw) }
        }
    }

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        if (coordinates.isAttached) {
            backdrop.layerCoordinates = coordinates
            backdrop.lastLayerPositionInWindow = coordinates.positionInWindow()
        }
    }

    override fun onDetach() {
        backdrop.layerCoordinates = null
    }
}
