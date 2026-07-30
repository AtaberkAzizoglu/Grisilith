package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.profile.PlayerProfile
import com.azizoglu.grisilith.unlock.UnlockDefinition
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class MerchantRenderer(
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
            605f

        private const val CURRENCY_Y =
            680f

        private const val COIN_X =
            1030f

        private const val STAT_POINT_X =
            1160f

        private const val PRODUCT_TITLE_Y =
            455f

        private const val PRODUCT_DESCRIPTION_Y =
            415f

        private const val PRODUCT_COST_Y =
            375f

        private const val MESSAGE_Y =
            170f
    }

    fun render(
        batch: SpriteBatch,
        font: BitmapFont,
        profile: PlayerProfile,
        product: UnlockDefinition?,
        message: String?
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

        renderProduct(
            batch =
                batch,

            font =
                font,

            product =
                product
        )

        renderMessage(
            batch =
                batch,

            font =
                font,

            message =
                message
        )

        font.data.setScale(
            oldScaleX,
            oldScaleY
        )

        font.color =
            oldColor
    }

    private fun renderTitle(
        batch: SpriteBatch,
        font: BitmapFont
    ) {
        font.data.setScale(
            2f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "MERCHANT FROM HALATHA",
            0f,
            TITLE_Y,
            VIEWPORT_WIDTH,
            1,
            false
        )
    }

    private fun renderSubtitle(
        batch: SpriteBatch,
        font: BitmapFont
    ) {
        font.data.setScale(
            0.9f
        )

        font.color =
            Color.GRAY

        font.draw(
            batch,
            "Permanent unlocks are purchased with Grisilith Coin.",
            0f,
            SUBTITLE_Y,
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

    private fun renderProduct(
        batch: SpriteBatch,
        font: BitmapFont,
        product: UnlockDefinition?
    ) {
        if (product == null) {
            font.data.setScale(
                1.2f
            )

            font.color =
                Color.GRAY

            font.draw(
                batch,
                "NO UNLOCKS AVAILABLE",
                0f,
                PRODUCT_TITLE_Y,
                VIEWPORT_WIDTH,
                1,
                false
            )

            return
        }

        font.data.setScale(
            1.35f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            product.displayName,
            0f,
            PRODUCT_TITLE_Y,
            VIEWPORT_WIDTH,
            1,
            false
        )

        font.data.setScale(
            0.9f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            product.description,
            0f,
            PRODUCT_DESCRIPTION_Y,
            VIEWPORT_WIDTH,
            1,
            false
        )

        font.data.setScale(
            1f
        )

        font.color =
            Color.GOLD

        font.draw(
            batch,
            "COST: ${product.cost} G",
            0f,
            PRODUCT_COST_Y,
            VIEWPORT_WIDTH,
            1,
            false
        )
    }

    private fun renderMessage(
        batch: SpriteBatch,
        font: BitmapFont,
        message: String?
    ) {
        if (message.isNullOrBlank()) {
            return
        }

        font.data.setScale(
            0.95f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            message,
            0f,
            MESSAGE_Y,
            VIEWPORT_WIDTH,
            1,
            false
        )
    }
}
