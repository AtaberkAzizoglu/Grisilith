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
import com.azizoglu.grisilith.screen.render.ChestCharacterStatRow
import com.azizoglu.grisilith.screen.render.ChestRewardOverlayRenderer
import com.azizoglu.grisilith.statistics.StatType
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport

class ChestRewardController(
    private val rewardQueue: ChestRewardQueue,
    private val difficulty: GameDifficulty,

    private val currentStageProvider: () -> Int,
    private val luckScoreProvider: () -> Int,

    private val availableStatTypesProvider:
        () -> List<StatType>,

    private val availableItemDefinitionsProvider:
        () -> List<ItemDefinition>,

    camera: OrthographicCamera,
    cameraWidth: Float,
    cameraHeight: Float,
    viewport: Viewport,
    shapeRenderer: ShapeRenderer,
    batch: SpriteBatch,
    font: BitmapFont,
    characterNameProvider:
        () -> String,

    characterStatsProvider:
        () -> List<ChestCharacterStatRow>,

    private val onApplyReward:
        (ChestRewardRoll) -> Unit,

    private val onChestCompleted:
        () -> Unit,

    private val onAllRewardsCollected:
        (
        shouldOpenMarketAfterStatSelection:
        Boolean
    ) -> Unit,

    private val additionalDiscardAllowanceProvider:
        () -> Int = {
        0
    }
) {

    companion object {

        private const val REVEAL_DURATION_SECONDS =
            0.44f
    }

    private val openingSession =
        ChestOpeningSession()

    private val pointerPosition =
        Vector2()

    private val overlayRenderer =
        ChestRewardOverlayRenderer(
            camera =
                camera,

            cameraWidth =
                cameraWidth,

            cameraHeight =
                cameraHeight,

            shapeRenderer =
                shapeRenderer,

            batch =
                batch,

            font =
                font,

            characterNameProvider =
                characterNameProvider,

            characterStatsProvider =
                characterStatsProvider
        )

    private val viewport =
        viewport

    private var openMarketAfterStatSelection =
        false

    private var revealElapsedSeconds =
        0f

    private var isItemDetailOpen =
        false

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

        isItemDetailOpen =
            false

        isOpen =
            true
    }

    fun update(
        delta: Float
    ) {

        overlayRenderer
            .update(
                delta
            )

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
                REVEAL_DURATION_SECONDS

            openingSession
                .finishReveal()
        }
    }

    fun handleInput() {

        if (!isOpen) {
            return
        }

        if (
            isItemDetailOpen
        ) {
            handleItemDetailInput()
            return
        }

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

        updatePointerPosition()

        val layout =
            overlayRenderer
                .currentLayout()

        when (
            openingSession.state
        ) {
            ChestOpeningState.LOCKED,
            ChestOpeningState.UNLOCKED -> {
                if (
                    layout.chestBounds.contains(
                        pointerPosition
                    )
                ) {
                    handlePrimaryChestAction()
                }
            }

            ChestOpeningState.REWARD_SHOWN -> {
                when {
                    currentReward
                        ?.rewardType ==
                        ChestRewardType.ITEM &&
                        layout.rewardVisualBounds.contains(
                            pointerPosition
                        ) -> {
                        isItemDetailOpen =
                            true
                    }

                    layout.takeButtonBounds.contains(
                        pointerPosition
                    ) -> {
                        takeCurrentReward()
                    }

                    layout.discardButtonBounds.contains(
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

    private fun handleItemDetailInput() {

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            )
        ) {
            isItemDetailOpen =
                false

            return
        }

        if (
            !Gdx.input.isButtonJustPressed(
                Input.Buttons.LEFT
            )
        ) {
            return
        }

        updatePointerPosition()

        val bounds =
            overlayRenderer
                .currentLayout()
                .itemDetailBounds

        if (
            !bounds.contains(
                pointerPosition
            )
        ) {
            isItemDetailOpen =
                false
        }
    }

    private fun updatePointerPosition() {

        pointerPosition.set(
            Gdx.input.x.toFloat(),
            Gdx.input.y.toFloat()
        )

        viewport.unproject(
            pointerPosition
        )
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

        onChestCompleted()

        isItemDetailOpen =
            false

        finishOrPrepareNextChest()
    }

    private fun discardCurrentReward() {

        if (
            !openingSession
                .completeWithDiscard()
        ) {
            return
        }

        onChestCompleted()

        isItemDetailOpen =
            false

        finishOrPrepareNextChest()
    }

    private fun finishOrPrepareNextChest() {

        revealElapsedSeconds =
            0f

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

        isItemDetailOpen =
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

    fun renderShapes() {

        if (!isOpen) {
            return
        }

        overlayRenderer
            .renderShapes(
                state =
                    openingSession.state,

                reward =
                    openingSession.currentReward,

                revealProgress =
                    revealProgress(),

                discardEnabled =
                    openingSession.canDiscard,

                itemDetailOpen =
                    isItemDetailOpen
            )
    }

    fun renderTexts() {

        if (!isOpen) {
            return
        }

        overlayRenderer
            .renderTexts(
                state =
                    openingSession.state,

                reward =
                    openingSession.currentReward,

                currentChestNumber =
                    openingSession.currentChestNumber,

                totalChestCount =
                    openingSession.totalChestCount,

                remainingDiscardCount =
                    openingSession.remainingDiscardCount,

                itemDetailOpen =
                    isItemDetailOpen
            )
    }

    /*
     * Eski FirstScreen render çağrısı geçici olarak
     * kalmışsa kod derlensin diye bırakıldı.
     *
     * Ancak doğru görünüm için renderShapes() ve
     * renderTexts() ayrı ayrı çağrılmalıdır.
     */
    fun render() {
        renderTexts()
    }

    private fun revealProgress():
        Float {

        if (
            openingSession.state !=
            ChestOpeningState.REVEALING
        ) {
            return 0f
        }

        return (
            revealElapsedSeconds /
                REVEAL_DURATION_SECONDS
            )
            .coerceIn(
                0f,
                1f
            )
    }
}
