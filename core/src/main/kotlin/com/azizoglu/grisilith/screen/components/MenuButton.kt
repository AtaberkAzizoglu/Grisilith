package com.azizoglu.grisilith.screen.components

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
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

        private val whitePixel:
            Texture by lazy {
            createWhitePixel()
        }

        private fun createWhitePixel():
            Texture {

            val pixmap =
                Pixmap(
                    1,
                    1,
                    Pixmap.Format.RGBA8888
                )

            pixmap.setColor(
                Color.WHITE
            )

            pixmap.fill()

            val texture =
                Texture(
                    pixmap
                )

            pixmap.dispose()

            return texture
        }
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

        val fillColor =
            when {
                !isEnabled -> {
                    disabledColor
                }

                pointerInside &&
                    pointerHeld &&
                    pointerWasPressedInside -> {
                    pressedColor
                }

                pointerInside -> {
                    hoveredColor
                }

                else -> {
                    normalColor
                }
            }

        drawRectangle(
            batch =
                batch,

            rectangle =
                bounds,

            color =
                borderColor
        )

        drawRectangle(
            batch =
                batch,

            rectangle =
                Rectangle(
                    bounds.x +
                        BORDER_SIZE,

                    bounds.y +
                        BORDER_SIZE,

                    bounds.width -
                        BORDER_SIZE * 2f,

                    bounds.height -
                        BORDER_SIZE * 2f
                ),

            color =
                fillColor
        )

        val oldScaleX =
            font.data.scaleX

        val oldScaleY =
            font.data.scaleY

        font.data.setScale(
            1.15f
        )

        glyphLayout.setText(
            font,
            text
        )

        font.color =
            if (isEnabled) {
                Color.WHITE
            } else {
                Color.DARK_GRAY
            }

        font.draw(
            batch,
            text,
            bounds.x +
                (
                    bounds.width -
                        glyphLayout.width
                    ) / 2f,
            bounds.y +
                (
                    bounds.height +
                        glyphLayout.height
                    ) / 2f
        )

        font.data.setScale(
            oldScaleX,
            oldScaleY
        )
    }

    private fun drawRectangle(
        batch:
        SpriteBatch,

        rectangle:
        Rectangle,

        color:
        Color
    ) {
        batch.color =
            color

        batch.draw(
            whitePixel,
            rectangle.x,
            rectangle.y,
            rectangle.width,
            rectangle.height
        )

        batch.color =
            Color.WHITE
    }
}
