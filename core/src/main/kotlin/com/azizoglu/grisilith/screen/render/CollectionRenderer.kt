package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.profile.PlayerProfile
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align

class CollectionRenderer(
    private val grisilithCoinRenderer:
    GrisilithCoinRenderer,

    private val purchasedStatPointRenderer:
    PurchasedStatPointRenderer
) {

    companion object {

        private const val VIEWPORT_WIDTH =
            1280f

        private const val TITLE_Y =
            650f

        private const val SUBTITLE_Y =
            600f

        private const val CURRENCY_Y =
            680f

        private const val COIN_X =
            1030f

        private const val STAT_POINT_X =
            1160f
    }

    fun render(
        batch:
        SpriteBatch,

        font:
        BitmapFont,

        profile:
        PlayerProfile
    ) {

        val oldScaleX =
            font.data.scaleX

        val oldScaleY =
            font.data.scaleY

        val oldColor =
            Color(
                font.color
            )

        renderTitle(
            batch =
                batch,

            font =
                font
        )

        renderSubtitle(
            batch =
                batch,

            font =
                font
        )

        renderCurrencies(
            batch =
                batch,

            font =
                font,

            profile =
                profile
        )

        font.data.setScale(
            oldScaleX,
            oldScaleY
        )

        font.color =
            oldColor
    }

    private fun renderTitle(
        batch:
        SpriteBatch,

        font:
        BitmapFont
    ) {

        font.data.setScale(
            2.2f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "COLLECTION",
            0f,
            TITLE_Y,
            VIEWPORT_WIDTH,
            Align.center,
            false
        )
    }

    private fun renderSubtitle(
        batch:
        SpriteBatch,

        font:
        BitmapFont
    ) {

        font.data.setScale(
            0.85f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            "Choose a category.",
            0f,
            SUBTITLE_Y,
            VIEWPORT_WIDTH,
            Align.center,
            false
        )
    }

    private fun renderCurrencies(
        batch:
        SpriteBatch,

        font:
        BitmapFont,

        profile:
        PlayerProfile
    ) {

        grisilithCoinRenderer.render(
            batch =
                batch,

            font =
                font,

            amount =
                profile.grisilithCoins,

            x =
                COIN_X,

            y =
                CURRENCY_Y
        )

        purchasedStatPointRenderer.render(
            batch =
                batch,

            font =
                font,

            amount =
                profile.purchasedStatPoints,

            x =
                STAT_POINT_X,

            y =
                CURRENCY_Y
        )
    }
}
