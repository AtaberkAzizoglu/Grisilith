package com.azizoglu.grisilith.screen.render

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align

class PurchasedStatPointRenderer {

    private val glyphLayout = GlyphLayout()

    companion object {
        private const val AMOUNT_SCALE = 1f
        private const val SYMBOL_SIZE = 30f
        private const val AMOUNT_OFFSET_X = 38f
    }

    private val symbol by lazy {
        Texture(Gdx.files.internal("ui/market/stat_point_symbol.png")).also {
            it.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }
    }

    fun render(
        batch: SpriteBatch,
        font: BitmapFont,
        amount: Int,
        x: Float,
        y: Float
    ) {
        val oldScaleX = font.data.scaleX
        val oldScaleY = font.data.scaleY
        val oldColor = Color(font.color)

        renderSymbol(
            batch,
            Rectangle(x, y - SYMBOL_SIZE + 6f, SYMBOL_SIZE, SYMBOL_SIZE)
        )

        font.data.setScale(AMOUNT_SCALE)
        font.color = Color.WHITE

        font.draw(
            batch,
            amount.coerceAtLeast(0).toString(),
            x + AMOUNT_OFFSET_X,
            y
        )

        font.data.setScale(oldScaleX, oldScaleY)
        font.color = oldColor
    }

    fun renderSymbol(batch: SpriteBatch, bounds: Rectangle) {
        val oldColor = Color(batch.color)
        batch.color = Color.WHITE
        batch.draw(symbol, bounds.x, bounds.y, bounds.width, bounds.height)
        batch.color = oldColor
    }

    fun renderCentered(
        batch: SpriteBatch,
        font: BitmapFont,
        amount: Int,
        bounds: Rectangle,
        iconSize: Float = 24f
    ) {
        val text = amount.coerceAtLeast(0).toString()
        val oldColor = Color(font.color)
        glyphLayout.setText(font, text)
        val gap = 6f
        val totalWidth = iconSize + gap + glyphLayout.width
        val x = bounds.x + (bounds.width - totalWidth) / 2f
        val centerY = bounds.y + bounds.height / 2f
        renderSymbol(batch, Rectangle(x, centerY - iconSize / 2f, iconSize, iconSize))
        font.color = Color.WHITE
        font.draw(
            batch,
            text,
            x + iconSize + gap,
            centerY + glyphLayout.height / 2f,
            glyphLayout.width + 2f,
            Align.left,
            false
        )
        font.color = oldColor
    }
}
