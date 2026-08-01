package com.azizoglu.grisilith.screen.controller

import com.azizoglu.grisilith.chest.ChestDiscardAllowanceCalculator
import com.azizoglu.grisilith.chest.ChestOpeningSession
import com.azizoglu.grisilith.chest.ChestOpeningState
import com.azizoglu.grisilith.chest.ChestRewardQueue
import com.azizoglu.grisilith.chest.ChestRewardRoll
import com.azizoglu.grisilith.chest.ChestRewardRoller
import com.azizoglu.grisilith.chest.ChestRewardType
import com.azizoglu.grisilith.config.GameDifficulty
import com.azizoglu.grisilith.item.definition.ItemDefinition
import com.azizoglu.grisilith.statistics.StatType
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
 * Yeni tek-sandık açma akışının Controller'ı.
 *
 * Görsel sistem Paket 3'te geliştirilecek. Bu sürüm:
 * - chest sayısını işler,
 * - iki tıklamalı açılmayı çalıştırır,
 * - TAKE / DISCARD davranışını uygular,
 * - rarity sonucunu üretir,
 * - sıradaki chest'e geçer.
 */
class ChestRewardController(
    private val rewardQueue: ChestRewardQueue,
    private val difficulty: GameDifficulty,

    private val currentStageProvider: () -> Int,
    private val luckScoreProvider: () -> Int,

    private val availableStatTypesProvider:
        () -> List<StatType>,

    private val availableItemDefinitionsProvider:
        () -> List<ItemDefinition>,

    private val camera: OrthographicCamera,
    private val cameraWidth: Float,
    private val cameraHeight: Float,
    private val viewport: Viewport,
    private val batch: SpriteBatch,
    private val font: BitmapFont,

    private val onApplyReward:
        (ChestRewardRoll) -> Unit,

    private val onAllRewardsCollected:
        (
        shouldOpenMarketAfterStatSelection:
        Boolean
    ) -> Unit,

    /*
     * Gelecekte discard hakkı artıran Item buraya
     * bağlanabilir. Şimdilik her zaman 0 dönecek.
     */
    private val additionalDiscardAllowanceProvider:
        () -> Int = {
        0
    }
) {

    companion object {

        private const val CHEST_WIDTH =
            250f

        private const val CHEST_HEIGHT =
            180f

        private const val ACTION_BUTTON_WIDTH =
            210f

        private const val ACTION_BUTTON_HEIGHT =
            64f

        /*
         * Paket 3'te gerçek animasyona dönüştürülecek.
         * Şimdilik REVEALING state'inin gerçekten ayrı
         * bir frame/state olmasını sağlar.
         */
        private const val REVEAL_DURATION_SECONDS =
            0.22f
    }

    private val openingSession =
        ChestOpeningSession()

    private val pointerPosition =
        Vector2()

    private val chestBounds =
        Rectangle()

    private val takeButtonBounds =
        Rectangle()

    private val discardButtonBounds =
        Rectangle()

    private var openMarketAfterStatSelection =
        false

    private var revealElapsedSeconds =
        0f

    var isOpen:
        Boolean =
        false
        private set

    val state:
        ChestOpeningState
        get() =
            openingSession.state

    val currentReward:
        ChestRewardRoll?
        get() =
            openingSession.currentReward

    val currentChestNumber:
        Int
        get() =
            openingSession.currentChestNumber

    val totalChestCount:
        Int
        get() =
            openingSession.totalChestCount

    val remainingDiscardCount:
        Int
        get() =
            openingSession.remainingDiscardCount

    val canDiscard:
        Boolean
        get() =
            openingSession.canDiscard

    fun begin(
        shouldOpenMarketAfterStatSelection:
        Boolean
    ) {

        val collectedChestCount =
            rewardQueue.takeAll()

        if (
            collectedChestCount <= 0
        ) {
            onAllRewardsCollected(
                shouldOpenMarketAfterStatSelection
            )

            return
        }

        openMarketAfterStatSelection =
            shouldOpenMarketAfterStatSelection

        val discardAllowance =
            ChestDiscardAllowanceCalculator
                .calculate(
                    difficulty =
                        difficulty,

                    futureItemBonus =
                        additionalDiscardAllowanceProvider()
                )

        openingSession.begin(
            chestCount =
                collectedChestCount,

            discardAllowance =
                discardAllowance
        )

        revealElapsedSeconds =
            0f

        isOpen =
            true

        updateBounds()
    }

    fun update(
        delta: Float
    ) {

        if (
            !isOpen ||
            openingSession.state !=
            ChestOpeningState.REVEALING
        ) {
            return
        }

        revealElapsedSeconds +=
            delta.coerceAtLeast(
                0f
            )

        if (
            revealElapsedSeconds >=
            REVEAL_DURATION_SECONDS
        ) {
            revealElapsedSeconds =
                0f

            openingSession
                .finishReveal()
        }
    }

    fun handleInput() {

        if (!isOpen) {
            return
        }

        updateBounds()

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ENTER
            ) ||
            Gdx.input.isKeyJustPressed(
                Input.Keys.SPACE
            )
        ) {
            handlePrimaryChestAction()
            return
        }

        if (
            openingSession.state ==
            ChestOpeningState.REWARD_SHOWN
        ) {
            if (
                Gdx.input.isKeyJustPressed(
                    Input.Keys.T
                )
            ) {
                takeCurrentReward()
                return
            }

            if (
                Gdx.input.isKeyJustPressed(
                    Input.Keys.D
                )
            ) {
                discardCurrentReward()
                return
            }
        }

        if (
            !Gdx.input.isButtonJustPressed(
                Input.Buttons.LEFT
            )
        ) {
            return
        }

        pointerPosition.set(
            Gdx.input.x.toFloat(),
            Gdx.input.y.toFloat()
        )

        viewport.unproject(
            pointerPosition
        )

        when (
            openingSession.state
        ) {
            ChestOpeningState.LOCKED,
            ChestOpeningState.UNLOCKED -> {
                if (
                    chestBounds.contains(
                        pointerPosition
                    )
                ) {
                    handlePrimaryChestAction()
                }
            }

            ChestOpeningState.REWARD_SHOWN -> {
                when {
                    takeButtonBounds.contains(
                        pointerPosition
                    ) -> {
                        takeCurrentReward()
                    }

                    discardButtonBounds.contains(
                        pointerPosition
                    ) -> {
                        discardCurrentReward()
                    }
                }
            }

            ChestOpeningState.REVEALING,
            ChestOpeningState.COMPLETED -> {
            }
        }
    }

    private fun handlePrimaryChestAction() {

        when (
            openingSession.state
        ) {
            ChestOpeningState.LOCKED -> {
                unlockCurrentChest()
            }

            ChestOpeningState.UNLOCKED -> {
                revealElapsedSeconds =
                    0f

                openingSession
                    .beginReveal()
            }

            ChestOpeningState.REVEALING,
            ChestOpeningState.REWARD_SHOWN,
            ChestOpeningState.COMPLETED -> {
            }
        }
    }

    private fun unlockCurrentChest() {

        val reward =
            ChestRewardRoller
                .rollReward(
                    currentStage =
                        currentStageProvider(),

                    luckScore =
                        luckScoreProvider(),

                    availableStatTypes =
                        availableStatTypesProvider(),

                    availableItemDefinitions =
                        availableItemDefinitionsProvider()
                )

        openingSession
            .unlockCurrentChest(
                reward
            )
    }

    private fun takeCurrentReward() {

        val reward =
            openingSession.currentReward
                ?: return

        onApplyReward(
            reward
        )

        if (
            !openingSession
                .completeWithTake()
        ) {
            return
        }

        finishOrPrepareNextChest()
    }

    private fun discardCurrentReward() {

        if (
            !openingSession
                .completeWithDiscard()
        ) {
            return
        }

        finishOrPrepareNextChest()
    }

    private fun finishOrPrepareNextChest() {

        if (
            openingSession.state ==
            ChestOpeningState.COMPLETED
        ) {
            finish()
        }
    }

    private fun finish() {

        isOpen =
            false

        openingSession.reset()

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

        updateBounds()

        /*
         * Paket 3'te:
         * - mor sis,
         * - transparan pencere,
         * - çizgili rarity parlaması,
         * - karakter portresi,
         * - item detay paneli
         * eklenecek.
         *
         * Bu geçici render, yeni akışın test edilmesini sağlar.
         */

        font.color =
            Color.GOLD

        font.draw(
            batch,
            "CHEST " +
                "$currentChestNumber / " +
                "$totalChestCount",
            0f,
            camera.position.y +
                cameraHeight / 2f -
                58f,
            cameraWidth,
            Align.center,
            false
        )

        when (
            openingSession.state
        ) {
            ChestOpeningState.LOCKED -> {
                renderChestPlaceholder(
                    title =
                        "LOCKED CHEST",

                    instruction =
                        "Click the chest to unlock it"
                )
            }

            ChestOpeningState.UNLOCKED -> {
                renderChestPlaceholder(
                    title =
                        unlockedTitle(),

                    instruction =
                        "Click the chest again to open it"
                )
            }

            ChestOpeningState.REVEALING -> {
                font.color =
                    Color.LIGHT_GRAY

                font.draw(
                    batch,
                    "OPENING...",
                    0f,
                    camera.position.y,
                    cameraWidth,
                    Align.center,
                    false
                )
            }

            ChestOpeningState.REWARD_SHOWN -> {
                renderCurrentReward()
            }

            ChestOpeningState.COMPLETED -> {
            }
        }
    }

    private fun renderChestPlaceholder(
        title: String,
        instruction: String
    ) {

        font.color =
            rewardAccentColor()

        font.draw(
            batch,
            title,
            chestBounds.x,
            chestBounds.y +
                chestBounds.height /
                2f +
                12f,
            chestBounds.width,
            Align.center,
            false
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            instruction,
            0f,
            chestBounds.y -
                28f,
            cameraWidth,
            Align.center,
            false
        )
    }

    private fun renderCurrentReward() {

        val reward =
            openingSession.currentReward
                ?: return

        font.color =
            rewardAccentColor()

        font.draw(
            batch,
            reward.title,
            0f,
            camera.position.y +
                80f,
            cameraWidth,
            Align.center,
            false
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            reward.description,
            camera.position.x -
                290f,
            camera.position.y +
                30f,
            580f,
            Align.center,
            true
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "TAKE  [T]",
            takeButtonBounds.x,
            takeButtonBounds.y +
                38f,
            takeButtonBounds.width,
            Align.center,
            false
        )

        font.color =
            if (canDiscard) {
                Color.WHITE
            } else {
                Color.DARK_GRAY
            }

        font.draw(
            batch,
            "DISCARD  [D]",
            discardButtonBounds.x,
            discardButtonBounds.y +
                38f,
            discardButtonBounds.width,
            Align.center,
            false
        )

        font.color =
            Color.GRAY

        font.draw(
            batch,
            "Discards left: " +
                remainingDiscardCount,
            0f,
            camera.position.y -
                cameraHeight /
                2f +
                48f,
            cameraWidth,
            Align.center,
            false
        )
    }

    private fun unlockedTitle():
        String {

        val reward =
            openingSession.currentReward
                ?: return "UNLOCKED"

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
                        ?.displayName
                        ?: "Item"

                "$rarity REWARD"
            }
        }
    }

    /*
     * Paket 3'te aynı renk ShapeRenderer çizgilerine
     * ve glow efektine aktarılacak.
     */
    fun rewardAccentColor():
        Color {

        val reward =
            openingSession.currentReward
                ?: return Color(
                    0.35f,
                    0.72f,
                    1f,
                    1f
                )

        if (
            reward.rewardType ==
            ChestRewardType.STAT
        ) {
            return Color(
                0.35f,
                0.72f,
                1f,
                1f
            )
        }

        return when (
            reward.itemDefinition
                ?.rarity
                ?.name
                ?.uppercase()
        ) {
            "UNCOMMON" -> {
                Color(
                    0.25f,
                    0.90f,
                    0.35f,
                    1f
                )
            }

            "RARE" -> {
                Color(
                    0.20f,
                    0.55f,
                    1f,
                    1f
                )
            }

            "LEGENDARY" -> {
                Color(
                    1f,
                    0.82f,
                    0.15f,
                    1f
                )
            }

            "ARTIFACT" -> {
                Color(
                    0.95f,
                    0.18f,
                    0.18f,
                    1f
                )
            }

            else -> {
                Color(
                    0.72f,
                    0.72f,
                    0.76f,
                    1f
                )
            }
        }
    }

    private fun updateBounds() {

        chestBounds.set(
            camera.position.x -
                CHEST_WIDTH /
                2f,
            camera.position.y -
                CHEST_HEIGHT /
                2f +
                25f,
            CHEST_WIDTH,
            CHEST_HEIGHT
        )

        takeButtonBounds.set(
            camera.position.x -
                ACTION_BUTTON_WIDTH -
                18f,
            camera.position.y -
                190f,
            ACTION_BUTTON_WIDTH,
            ACTION_BUTTON_HEIGHT
        )

        discardButtonBounds.set(
            camera.position.x +
                18f,
            camera.position.y -
                190f,
            ACTION_BUTTON_WIDTH,
            ACTION_BUTTON_HEIGHT
        )
    }
}
