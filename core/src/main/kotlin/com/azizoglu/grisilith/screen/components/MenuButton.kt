package com.azizoglu.grisilith.screen.components

import com.badlogic.gdx.graphics.Color
import com.azizoglu.grisilith.ui.theme.ButtonVisualState
import com.azizoglu.grisilith.ui.theme.UiComponentRenderer
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

/*
 * Fare ve dokunmatik ekranla çalışan ortak
 * menü butonu.
 *
 * Klavye kısayolu içermez.
 */
class MenuButton(
    val text: String,

    x: Float,
    y: Float,
    width: Float,
    height: Float,

    var isEnabled: Boolean = true,

    val visualStyle: MenuButtonVisualStyle = MenuButtonVisualStyle.STANDARD,

    val isSelected: Boolean = false,

    private val textOffsetY: Float = 0f,

    private val onClick:
        () -> Unit
) {

    companion object {

        private val normalColor =
            Color(
                0.11f,
                0.15f,
                0.23f,
                1f
            )

        private val hoveredColor =
            Color(
                0.17f,
                0.24f,
                0.35f,
                1f
            )

        private val pressedColor =
            Color(
                0.09f,
                0.30f,
                0.25f,
                1f
            )

        private val disabledColor =
            Color(
                0.08f,
                0.08f,
                0.10f,
                1f
            )

        private val borderColor =
            Color(
                0.35f,
                0.55f,
                0.78f,
                1f
            )

        private const val BORDER_SIZE =
            2f

    }

    val bounds =
        Rectangle(
            x,
            y,
            width,
            height
        )

    private val glyphLayout =
        GlyphLayout()

    private var pointerWasPressedInside =
        false

    fun update(
        pointerPosition:
        Vector2,

        pointerJustPressed:
        Boolean,

        pointerJustReleased:
        Boolean
    ): Boolean {
        if (!isEnabled) {
            pointerWasPressedInside = false
            return false
        }

        val pointerInside =
            bounds.contains(
                pointerPosition
            )

        if (pointerJustPressed) {
            pointerWasPressedInside =
                pointerInside
        }

        if (pointerJustReleased) {
            val shouldClick =
                pointerWasPressedInside &&
                    pointerInside

            pointerWasPressedInside =
                false

            if (shouldClick) {
                onClick()
                return true
            }
        }

        return false
    }

    fun render(
        batch:
        SpriteBatch,

        font:
        BitmapFont,

        pointerPosition:
        Vector2,

        pointerHeld:
        Boolean
    ) {
        val pointerInside =
            isEnabled &&
                bounds.contains(
                    pointerPosition
                )

        val visualState =
            when {
                !isEnabled -> {
                    ButtonVisualState.DISABLED
                }

                pointerInside &&
                    pointerHeld &&
                    pointerWasPressedInside -> {
                    ButtonVisualState.PRESSED
                }

                pointerInside -> {
                    ButtonVisualState.HOVERED
                }

                else -> {
                    ButtonVisualState.NORMAL
                }
            }

        when (visualStyle) {
            MenuButtonVisualStyle.ARROW_LEFT, MenuButtonVisualStyle.ARROW_RIGHT ->
                UiComponentRenderer.drawStepperArrow(batch,bounds,visualStyle==MenuButtonVisualStyle.ARROW_RIGHT,visualState)
            MenuButtonVisualStyle.STANDARD ->
                UiComponentRenderer.drawButton(
                    batch = batch,
                    bounds = bounds,
                    state = if (isSelected && visualState == ButtonVisualState.NORMAL) {
                        ButtonVisualState.SELECTED
                    } else {
                        visualState
                    }
                )

            MenuButtonVisualStyle.TOGGLE ->
                UiComponentRenderer.drawToggle(
                    batch = batch,
                    bounds = bounds,
                    enabled = isEnabled,
                    selected = isSelected
                )

            MenuButtonVisualStyle.KEY_SLOT ->
                UiComponentRenderer.drawSlot(
                    batch = batch,
                    bounds = bounds,
                    selected = isSelected || visualState == ButtonVisualState.HOVERED,
                    locked = !isEnabled
                )
            MenuButtonVisualStyle.DROPDOWN -> UiComponentRenderer.drawDropdown(batch, bounds,
                isSelected || visualState == ButtonVisualState.HOVERED)
            MenuButtonVisualStyle.DROPDOWN_ROW -> UiComponentRenderer.drawDropdownRow(batch, bounds,
                isSelected || visualState == ButtonVisualState.HOVERED)
        }

        val oldScaleX =
            font.data.scaleX

        val oldScaleY =
            font.data.scaleY

        var textScale = 1.02f
        font.data.setScale(textScale)

        glyphLayout.setText(
            font,
            text
        )

        val horizontalPadding = when (visualStyle) {
            MenuButtonVisualStyle.TOGGLE -> 12f
            MenuButtonVisualStyle.KEY_SLOT, MenuButtonVisualStyle.ARROW_LEFT, MenuButtonVisualStyle.ARROW_RIGHT -> 8f
            MenuButtonVisualStyle.STANDARD -> minOf(42f, bounds.width * 0.28f)
            MenuButtonVisualStyle.DROPDOWN, MenuButtonVisualStyle.DROPDOWN_ROW -> 30f
        }
        val availableWidth = (bounds.width - horizontalPadding).coerceAtLeast(1f)
        val availableHeight = (bounds.height - 18f).coerceAtLeast(1f)
        if (glyphLayout.width > availableWidth || glyphLayout.height > availableHeight) {
            val widthRatio = availableWidth / glyphLayout.width.coerceAtLeast(1f)
            val heightRatio = availableHeight / glyphLayout.height.coerceAtLeast(1f)
            textScale = (textScale * minOf(widthRatio, heightRatio)).coerceAtLeast(0.42f)
            font.data.setScale(textScale)
            glyphLayout.setText(font, text)
        }

        font.color =
            if (isEnabled) {
                Color.WHITE
            } else {
                Color.DARK_GRAY
            }

        font.draw(
            batch,
            text,
            bounds.x,
            bounds.y +
                (
                    bounds.height +
                        glyphLayout.height
                    ) / 2f +
                textOffsetY,
            bounds.width,
            com.badlogic.gdx.utils.Align.center,
            false
        )

        font.data.setScale(
            oldScaleX,
            oldScaleY
        )
    }

}

enum class MenuButtonVisualStyle {
    STANDARD,
    TOGGLE,
    KEY_SLOT,
    DROPDOWN,
    DROPDOWN_ROW,
    ARROW_LEFT,
    ARROW_RIGHT
}
