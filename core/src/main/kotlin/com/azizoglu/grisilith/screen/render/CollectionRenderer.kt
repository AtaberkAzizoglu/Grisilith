package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.profile.PlayerProfile
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class CollectionRenderer(
    private val grisilithCoinRenderer: GrisilithCoinRenderer,
    private val purchasedStatPointRenderer: PurchasedStatPointRenderer
) {

    companion object {
        private const val VIEWPORT_WIDTH = 1280f
        private const val TITLE_Y = 650f
        private const val CURRENCY_Y = 680f
        private const val COIN_X = 1030f
        private const val STAT_POINT_X = 1160f
        private const val FIRST_CATEGORY_Y = 500f
        private const val CATEGORY_SPACING = 58f
    }

    private val categories =
        listOf(
            "CHARACTERS",
            "WEAPONS",
            "ARMORS",
            "ITEMS",
            "TAMGAS",
            "MAPS",
            "ENEMIES — COMING SOON"
        )

    fun render(
        batch: SpriteBatch,
        font: BitmapFont,
        profile: PlayerProfile
    ) {
        val oldScaleX = font.data.scaleX
        val oldScaleY = font.data.scaleY
        val oldColor = Color(font.color)

        renderTitle(batch, font)
        renderCurrencies(batch, font, profile)
        renderCategories(batch, font)

        font.data.setScale(oldScaleX, oldScaleY)
        font.color = oldColor
    }

    private fun renderTitle(
        batch: SpriteBatch,
        font: BitmapFont
    ) {
        font.data.setScale(2.2f)
        font.color = Color.WHITE

        font.draw(
            batch,
            "COLLECTION",
            0f,
            TITLE_Y,
            VIEWPORT_WIDTH,
            1,
            false
        )
    }

    private fun renderCurrencies(
        batch: SpriteBatch,
        font: BitmapFont,
        profile: PlayerProfile
    ) {
        grisilithCoinRenderer.render(
            batch = batch,
            font = font,
            amount = profile.grisilithCoins,
            x = COIN_X,
            y = CURRENCY_Y
        )

        purchasedStatPointRenderer.render(
            batch = batch,
            font = font,
            amount = profile.purchasedStatPoints,
            x = STAT_POINT_X,
            y = CURRENCY_Y
        )
    }

    private fun renderCategories(
        batch: SpriteBatch,
        font: BitmapFont
    ) {
        font.data.setScale(1.15f)

        categories.forEachIndexed { index, category ->
            font.color =
                if (category.contains("COMING SOON")) {
                    Color.GRAY
                } else {
                    Color.LIGHT_GRAY
                }

            font.draw(
                batch,
                category,
                0f,
                FIRST_CATEGORY_Y - index * CATEGORY_SPACING,
                VIEWPORT_WIDTH,
                1,
                false
            )
        }
    }
}
