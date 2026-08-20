package com.azizoglu.grisilith.screen.render

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class GoldCoinRenderer {

    private val texture by lazy {
        Texture(
            Gdx.files.internal(
                "currency/gold_coin.png"
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
        iconSize: Float = 24f,
        suffix: String = ""
    ) {
        batch.draw(
            texture,
            x,
            y -
                iconSize +
                5f,
            iconSize,
            iconSize
        )

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
                7f,
            y
        )
    }
}
