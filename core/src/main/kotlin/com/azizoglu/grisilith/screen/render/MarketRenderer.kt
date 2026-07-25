package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.economy.MarketRerollPriceCalculator
import com.azizoglu.grisilith.economy.MarketSlot
import com.azizoglu.grisilith.economy.SessionWallet
import com.azizoglu.grisilith.stage.StageController
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class MarketRenderer(
    private val camera: OrthographicCamera,
    private val cameraWidth: Float,
    private val batch: SpriteBatch,
    private val font: BitmapFont,
    private val sessionWallet: SessionWallet,
    private val stageController: StageController
) {

    fun render(
        marketSlots: List<MarketSlot>,
        marketRerollsUsed: Int
    ) {
        val startX =
            camera.position.x -
                cameraWidth / 2f +
                220f

        val titleY =
            camera.position.y +
                220f

        /*
         * Kontrol yazılarında kullanılacağı için
         * loop başlamadan önce hesaplanır.
         */
        val rerollPrice =
            MarketRerollPriceCalculator
                .calculatePrice(
                    currentStage =
                        stageController.currentStage,
                    rerollsUsedInCurrentMarket =
                        marketRerollsUsed
                )

        font.draw(
            batch,
            "MINI BOSS MARKET",
            startX,
            titleY
        )

        font.draw(
            batch,
            "Gold: ${sessionWallet.goldCoins}",
            startX,
            titleY - 35f
        )

        marketSlots.forEachIndexed {
                index,
                slot ->

            val slotY =
                titleY -
                    90f -
                    index * 95f

            val offer =
                slot.offer

            when {
                slot.isSold -> {
                    font.draw(
                        batch,
                        "${index + 1} - SOLD",
                        startX,
                        slotY
                    )
                }

                offer == null -> {
                    font.draw(
                        batch,
                        "${index + 1} - EMPTY",
                        startX,
                        slotY
                    )
                }

                else -> {
                    val lockText =
                        if (slot.isLocked) {
                            " [LOCKED]"
                        } else {
                            ""
                        }

                    font.draw(
                        batch,
                        "${index + 1} - " +
                            "${offer.title}$lockText",
                        startX,
                        slotY
                    )

                    font.draw(
                        batch,
                        offer.description,
                        startX + 20f,
                        slotY - 27f
                    )

                    font.draw(
                        batch,
                        "Price: ${offer.price} Gold",
                        startX + 20f,
                        slotY - 54f
                    )
                }
            }
        }

        /*
         * Kontrol yazıları kart loop'unun dışında
         * yalnızca bir defa çizilir.
         */
        font.draw(
            batch,
            "1 / 2 / 3 / 4 - Buy",
            startX,
            titleY - 500f
        )

        font.draw(
            batch,
            "Q / W / E / T - Lock or Unlock",
            startX,
            titleY - 530f
        )

        font.draw(
            batch,
            "R - Reroll ($rerollPrice Gold)",
            startX,
            titleY - 560f
        )

        font.draw(
            batch,
            "ENTER - Return to Fight",
            startX,
            titleY - 590f
        )
    }
}
