package com.azizoglu.grisilith.screen.render

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class GrisilithCoinRenderer {

    companion object {
        private const val PLACEHOLDER_SYMBOL =
            "G"

        private const val SYMBOL_SCALE =
            1.15f

        private const val AMOUNT_SCALE =
            1f

        private const val SYMBOL_WIDTH =
            34f

        private const val AMOUNT_OFFSET_X =
            42f
    }

    fun render(
        batch: SpriteBatch,
        font: BitmapFont,
        amount: Int,
        x: Float,
        y: Float
    ) {
        val oldScaleX =
            font.data.scaleX

        val oldScaleY =
            font.data.scaleY

        val oldColor =
            Color(
                font.color
            )

        font.data.setScale(
            SYMBOL_SCALE
        )

        font.color =
            Color.GOLD

        font.draw(
            batch,
            PLACEHOLDER_SYMBOL,
            x,
            y,
            SYMBOL_WIDTH,
            1,
            false
        )

        font.data.setScale(
            AMOUNT_SCALE
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            amount
                .coerceAtLeast(0)
                .toString(),
            x + AMOUNT_OFFSET_X,
            y
        )

        font.data.setScale(
            oldScaleX,
            oldScaleY
        )

        font.color =
            oldColor
    }
}
