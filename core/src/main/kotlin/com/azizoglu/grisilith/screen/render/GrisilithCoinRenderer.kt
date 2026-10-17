package com.azizoglu.grisilith.screen.render

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align

class GrisilithCoinRenderer {

    private val glyphLayout = GlyphLayout()

    private val texture by lazy {
        Texture(
            Gdx.files.internal(
                "currency/grisilith_coin.png"
            ),
            true
        ).also {
            it.setFilter(
                Texture.TextureFilter.MipMapLinearLinear,
                Texture.TextureFilter.Linear
            )
        }
    }

    fun render(
        batch: SpriteBatch,
        font: BitmapFont,
        amount: Int,
        x: Float,
        y: Float,
        iconSize: Float = 27f,
        suffix: String = ""
    ) {
        val oldColor =
            Color(
                font.color
            )

        batch.draw(
            texture,
            x,
            y -
                iconSize +
                6f,
            iconSize,
            iconSize
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            amount
                .coerceAtLeast(
                    0
                )
                .toString() +
                suffix,
            x +
                iconSize +
                8f,
            y
        )

        font.color =
            oldColor
    }

    fun renderCentered(
        batch: SpriteBatch,
        font: BitmapFont,
        amount: Int,
        bounds: Rectangle,
        iconSize: Float = 19f,
        color: Color = Color.GOLD
    ) {
        val text = amount.coerceAtLeast(0).toString()
        val oldFontColor = Color(font.color)
        val oldBatchColor = Color(batch.color)
        glyphLayout.setText(font, text)
        val gap = 6f
        val totalWidth = iconSize + gap + glyphLayout.width
        val startX = bounds.x + (bounds.width - totalWidth) / 2f
        val centerY = bounds.y + bounds.height / 2f

        batch.color = Color.WHITE
        batch.draw(
            texture,
            startX,
            centerY - iconSize / 2f,
            iconSize,
            iconSize
        )
        font.color = color
        font.draw(
            batch,
            text,
            startX + iconSize + gap,
            centerY + glyphLayout.height / 2f,
            glyphLayout.width + 2f,
            Align.left,
            false
        )

        font.color = oldFontColor
        batch.color = oldBatchColor
    }
}
