package com.azizoglu.grisilith.screen.controller

import com.azizoglu.grisilith.chest.ChestRewardChoiceSet
import com.azizoglu.grisilith.chest.ChestRewardQueue
import com.azizoglu.grisilith.chest.ChestRewardRoll
import com.azizoglu.grisilith.chest.ChestRewardType
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.Viewport

/*
 * Stage sonunda her toplanmış sandık için
 * üç rastgele ödül seçeneği gösterir.
 *
 * Oyuncu seçeneklerden yalnızca birini alır.
 */
class ChestRewardController(
    private val rewardQueue:
    ChestRewardQueue,

    private val camera:
    OrthographicCamera,

    private val cameraWidth:
    Float,

    private val cameraHeight:
    Float,

    private val viewport:
    Viewport,

    private val batch:
    SpriteBatch,

    private val font:
    BitmapFont,

    private val onApplyReward:
        (ChestRewardRoll) -> Unit,

    private val onAllRewardsCollected:
        (
        shouldOpenMarketAfterStatSelection:
        Boolean
    ) -> Unit
) {

    companion object {
        private const val CARD_WIDTH =
            280f

        private const val CARD_HEIGHT =
            300f

        private const val CARD_GAP =
            28f
    }

    private val pointerPosition =
        Vector2()

    private val cardRectangles =
        List(
            ChestRewardChoiceSet
                .CHOICE_COUNT
        ) {
            Rectangle()
        }

    private var openMarketAfterStatSelection =
        false

    var isOpen:
        Boolean = false
        private set

    val currentChoiceSet:
        ChestRewardChoiceSet?
        get() =
            if (isOpen) {
                rewardQueue.peekFirst()
            } else {
                null
            }

    fun begin(
        shouldOpenMarketAfterStatSelection:
        Boolean
    ) {
        if (rewardQueue.isEmpty) {
            onAllRewardsCollected(
                shouldOpenMarketAfterStatSelection
            )

            return
        }

        openMarketAfterStatSelection =
            shouldOpenMarketAfterStatSelection

        isOpen = true
    }

    fun handleInput() {
        if (!isOpen) {
            return
        }

        when {
            Gdx.input.isKeyJustPressed(
                Input.Keys.NUM_1
            ) -> {
                selectChoice(
                    0
                )
            }

            Gdx.input.isKeyJustPressed(
                Input.Keys.NUM_2
            ) -> {
                selectChoice(
                    1
                )
            }

            Gdx.input.isKeyJustPressed(
                Input.Keys.NUM_3
            ) -> {
                selectChoice(
                    2
                )
            }

            Gdx.input.isButtonJustPressed(
                Input.Buttons.LEFT
            ) -> {
                handlePointerSelection()
            }
        }
    }

    private fun handlePointerSelection() {
        pointerPosition.set(
            Gdx.input.x.toFloat(),
            Gdx.input.y.toFloat()
        )

        viewport.unproject(
            pointerPosition
        )

        updateCardRectangles()

        val selectedIndex =
            cardRectangles
                .indexOfFirst { rectangle ->
                    rectangle.contains(
                        pointerPosition
                    )
                }

        if (selectedIndex >= 0) {
            selectChoice(
                selectedIndex
            )
        }
    }

    private fun selectChoice(
        choiceIndex:
        Int
    ) {
        val choiceSet =
            rewardQueue.peekFirst()
                ?: run {
                    finish()
                    return
                }

        val selectedReward =
            choiceSet.choices
                .getOrNull(
                    choiceIndex
                )
                ?: return

        onApplyReward(
            selectedReward
        )

        /*
         * Seçilen sandık tamamlandı.
         * Diğer iki seçenek de bu paketle
         * birlikte kuyruktan kaldırılır.
         */
        rewardQueue.removeFirst()

        if (rewardQueue.isEmpty) {
            finish()
        }
    }

    private fun finish() {
        isOpen = false

        val shouldOpenMarket =
            openMarketAfterStatSelection

        openMarketAfterStatSelection =
            false

        onAllRewardsCollected(
            shouldOpenMarket
        )
    }

    fun render() {
        if (!isOpen) {
            return
        }

        val choiceSet =
            currentChoiceSet
                ?: return

        updateCardRectangles()

        font.color =
            Color.GOLD

        font.draw(
            batch,
            "CHOOSE A CHEST REWARD",
            camera.position.x -
                115f,
            camera.position.y +
                cameraHeight / 2f -
                70f
        )

        choiceSet.choices
            .forEachIndexed {
                    index,
                    reward ->

                renderRewardCard(
                    cardIndex =
                        index,

                    reward =
                        reward,

                    rectangle =
                        cardRectangles[index]
                )
            }

        font.color =
            Color.GRAY

        font.draw(
            batch,
            "Click a reward or press 1, 2 or 3",
            camera.position.x -
                130f,
            camera.position.y -
                cameraHeight / 2f +
                55f
        )

        font.draw(
            batch,
            "Remaining Chests: " +
                rewardQueue.rewardCount,
            camera.position.x -
                cameraWidth / 2f +
                30f,
            camera.position.y -
                cameraHeight / 2f +
                35f
        )
    }

    private fun renderRewardCard(
        cardIndex:
        Int,

        reward:
        ChestRewardRoll,

        rectangle:
        Rectangle
    ) {
        font.color =
            Color.WHITE

        font.draw(
            batch,
            "${cardIndex + 1}",
            rectangle.x +
                rectangle.width / 2f -
                4f,
            rectangle.y +
                rectangle.height -
                20f
        )

        font.color =
            when (
                reward.rewardType
            ) {
                ChestRewardType.STAT -> {
                    Color(
                        0.45f,
                        0.72f,
                        1f,
                        1f
                    )
                }

                ChestRewardType.ITEM -> {
                    Color(
                        0.45f,
                        0.90f,
                        0.58f,
                        1f
                    )
                }
            }

        font.draw(
            batch,
            reward.title,
            rectangle.x +
                12f,
            rectangle.y +
                rectangle.height -
                65f,
            rectangle.width -
                24f,
            Align.center,
            true
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            reward.description,
            rectangle.x +
                18f,
            rectangle.y +
                rectangle.height -
                115f,
            rectangle.width -
                36f,
            Align.center,
            true
        )

        font.color =
            Color.GOLD

        font.draw(
            batch,
            rewardTypeText(
                reward
            ),
            rectangle.x +
                12f,
            rectangle.y +
                55f,
            rectangle.width -
                24f,
            Align.center,
            false
        )

        font.color =
            Color.GRAY

        font.draw(
            batch,
            "SELECT",
            rectangle.x +
                12f,
            rectangle.y +
                22f,
            rectangle.width -
                24f,
            Align.center,
            false
        )
    }

    private fun rewardTypeText(
        reward:
        ChestRewardRoll
    ): String {
        return when (
            reward.rewardType
        ) {
            ChestRewardType.STAT -> {
                "STAT REWARD"
            }

            ChestRewardType.ITEM -> {
                val rarity =
                    reward.itemDefinition
                        ?.rarity
                        ?.name
                        ?: "UNKNOWN"

                "$rarity ITEM"
            }
        }
    }

    private fun updateCardRectangles() {
        val totalCardsWidth =
            CARD_WIDTH *
                ChestRewardChoiceSet
                    .CHOICE_COUNT +
                CARD_GAP *
                (
                    ChestRewardChoiceSet
                        .CHOICE_COUNT -
                        1
                    )

        val startX =
            camera.position.x -
                totalCardsWidth / 2f

        val cardY =
            camera.position.y -
                CARD_HEIGHT / 2f +
                15f

        cardRectangles
            .forEachIndexed {
                    index,
                    rectangle ->

                rectangle.set(
                    startX +
                        index *
                        (
                            CARD_WIDTH +
                                CARD_GAP
                            ),

                    cardY,

                    CARD_WIDTH,

                    CARD_HEIGHT
                )
            }
    }
}
