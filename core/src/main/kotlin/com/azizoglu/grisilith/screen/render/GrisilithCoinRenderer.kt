package com.azizoglu.grisilith.screen.render

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class GrisilithCoinRenderer {

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
}
