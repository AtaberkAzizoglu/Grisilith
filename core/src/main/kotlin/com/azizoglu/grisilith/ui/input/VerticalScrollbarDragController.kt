package com.azizoglu.grisilith.ui.input

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

/** Shared pointer interaction for every vertical scrollbar in the game UI. */
class VerticalScrollbarDragController {

    private var dragging = false
    private var grabOffsetY = 0f

    fun reset() {
        dragging = false
        grabOffsetY = 0f
    }

    fun update(
        pointer: Vector2,
        pointerJustPressed: Boolean,
        pointerHeld: Boolean,
        pointerJustReleased: Boolean,
        trackBounds: Rectangle,
        thumbBounds: Rectangle,
        onScrollRatioChanged: (Float) -> Unit
    ): Boolean {
        if (pointerJustPressed && trackBounds.contains(pointer)) {
            dragging = true
            grabOffsetY =
                if (thumbBounds.contains(pointer)) {
                    pointer.y - thumbBounds.y
                } else {
                    thumbBounds.height / 2f
                }
            updateRatio(pointer.y, trackBounds, thumbBounds.height, onScrollRatioChanged)
            return true
        }

        if (!dragging) {
            return false
        }

        if (pointerHeld || pointerJustReleased) {
            updateRatio(pointer.y, trackBounds, thumbBounds.height, onScrollRatioChanged)
        }

        if (pointerJustReleased || !pointerHeld) {
            dragging = false
        }

        return true
    }

    private fun updateRatio(
        pointerY: Float,
        trackBounds: Rectangle,
        thumbHeight: Float,
        onScrollRatioChanged: (Float) -> Unit
    ) {
        val travel = (trackBounds.height - thumbHeight).coerceAtLeast(0f)
        if (travel <= 0f) {
            onScrollRatioChanged(0f)
            return
        }
        val thumbY =
            (pointerY - grabOffsetY)
                .coerceIn(trackBounds.y, trackBounds.y + travel)
        val ratio = 1f - (thumbY - trackBounds.y) / travel
        onScrollRatioChanged(ratio.coerceIn(0f, 1f))
    }
}
