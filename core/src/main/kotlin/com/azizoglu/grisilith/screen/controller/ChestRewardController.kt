package com.azizoglu.grisilith.screen.controller

import com.azizoglu.grisilith.chest.ChestDiscardAllowanceCalculator
import com.azizoglu.grisilith.chest.ChestOpeningSession
import com.azizoglu.grisilith.chest.ChestOpeningState
import com.azizoglu.grisilith.chest.ChestRewardQueue
import com.azizoglu.grisilith.chest.ChestRewardRoll
import com.azizoglu.grisilith.chest.ChestRewardRoller
import com.azizoglu.grisilith.config.GameDifficulty
import com.azizoglu.grisilith.item.definition.ItemDefinition
import com.azizoglu.grisilith.screen.render.ChestRewardOverlayRenderer
import com.azizoglu.grisilith.statistics.PlayerStats
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

    private val currentStageProvider: () -> Int,
    private val luckScoreProvider: () -> Int,

    private val preferredStatTypesProvider:
        () -> Set<StatType>,

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
    private val playerStats: PlayerStats,

    private val onApplyReward:
        (ChestRewardRoll) -> Unit,

    private val onChestCompleted:
        () -> Unit,

    private val onAllRewardsCollected:
        (
        shouldOpenMarketAfterStatSelection:
        Boolean
    ) -> Unit,

    private val remainingRunDiscardCountProvider:
        () -> Int,

    private val onRunDiscardUsed:
        () -> Unit
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

            font = font,
            playerStatsProvider = { playerStats },
            pointerPositionProvider = { pointerPosition }
        )

    private val viewport =
        viewport

    private var openMarketAfterStatSelection =
        false

    private var revealElapsedSeconds =
        0f

    private var batchRewards =
        emptyList<ChestRewardRoll>()

    private val discardedBatchRewardIndices =
        mutableSetOf<Int>()

    private var continueOpeningAll =
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

        openingSession.begin(
            chestCount =
                collectedChestCount,

            discardAllowance =
                remainingRunDiscardCountProvider()
                    .coerceAtLeast(
                        0
                    )
        )

        revealElapsedSeconds =
            0f

        batchRewards =
            emptyList()

        discardedBatchRewardIndices.clear()
        continueOpeningAll = false

        isOpen =
            true
    }

    fun update(
        delta: Float
    ) {

        if (isOpen) {
            updatePointerPosition()
        }

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

        if (batchRewards.isNotEmpty()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
                Gdx.input.isKeyJustPressed(Input.Keys.SPACE) ||
                Gdx.input.isKeyJustPressed(Input.Keys.T)
            ) {
                takeBatchRewards()
                return
            }
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                updatePointerPosition()
                val layout = overlayRenderer.currentLayout(batchRewards.size)
                val discardIndex = layout.rewardDiscardButtonBounds.indexOfFirst { it.contains(pointerPosition) }
                if (discardIndex >= 0) {
                    discardBatchReward(discardIndex)
                } else if (layout.takeButtonBounds.contains(pointerPosition)) {
                    takeBatchRewards()
                }
            }
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
                if (openingSession.state == ChestOpeningState.LOCKED &&
                    layout.openFiveButtonBounds.contains(pointerPosition) &&
                    remainingChestCount() >= 5
                ) {
                    openBatch(5, false)
                } else if (openingSession.state == ChestOpeningState.LOCKED &&
                    layout.openTenButtonBounds.contains(pointerPosition) &&
                    remainingChestCount() >= 10
                ) {
                    openBatch(10, false)
                } else if (openingSession.state == ChestOpeningState.LOCKED &&
                    layout.openAllButtonBounds.contains(pointerPosition)
                ) {
                    openBatch(remainingChestCount().coerceAtMost(10), true)
                } else if (
                    layout.chestBounds.contains(
                        pointerPosition
                    )
                ) {
                    handlePrimaryChestAction()
                }
            }

            ChestOpeningState.REWARD_SHOWN -> {
                when {
                    layout.takeButtonBounds.contains(
                        pointerPosition
                    ) -> {
                        takeCurrentReward()
                    }

                    layout.rewardDiscardButtonBounds.firstOrNull()?.contains(pointerPosition) == true -> {
                        discardCurrentReward()
                    }
                }
            }

            ChestOpeningState.REVEALING,
            ChestOpeningState.COMPLETED -> {
            }
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

        val reward = rollReward()

        openingSession
            .unlockCurrentChest(
                reward
            )
    }

    private fun rollReward(): ChestRewardRoll =
        ChestRewardRoller.rollReward(
            currentStage = currentStageProvider(),
            luckScore = luckScoreProvider(),
            preferredStatTypes = preferredStatTypesProvider(),
            availableStatTypes = availableStatTypesProvider(),
            availableItemDefinitions = availableItemDefinitionsProvider()
        )

    private fun remainingChestCount(): Int =
        (openingSession.totalChestCount - openingSession.openedChestCount).coerceAtLeast(0)

    private fun openBatch(count: Int, continueAll: Boolean) {
        val safeCount = count.coerceIn(1, remainingChestCount())
        val rolled = mutableListOf<ChestRewardRoll>()
        repeat(safeCount) {
            var candidate = rollReward()
            var attempts = 0
            while (
                candidate.itemDefinition != null &&
                rolled.any { it.itemDefinition?.id == candidate.itemDefinition?.id } &&
                attempts < 12
            ) {
                candidate = rollReward()
                attempts++
            }
            rolled += candidate
        }
        batchRewards = rolled
        discardedBatchRewardIndices.clear()
        continueOpeningAll = continueAll
    }

    private fun discardBatchReward(index: Int) {
        if (index !in batchRewards.indices || index in discardedBatchRewardIndices) return
        if (!openingSession.consumeBatchDiscard()) return
        discardedBatchRewardIndices += index
        onRunDiscardUsed()
    }

    private fun takeBatchRewards() {
        val rewards = batchRewards
        if (rewards.isEmpty()) return
        rewards.forEachIndexed { index, reward ->
            if (index !in discardedBatchRewardIndices) onApplyReward(reward)
            onChestCompleted()
        }
        openingSession.completeBatch(rewards.size)
        batchRewards = emptyList()
        discardedBatchRewardIndices.clear()
        if (openingSession.state == ChestOpeningState.COMPLETED) {
            finish()
        } else if (continueOpeningAll) {
            openBatch(remainingChestCount().coerceAtMost(10), true)
        } else {
            continueOpeningAll = false
        }
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

        finishOrPrepareNextChest()
    }

    private fun discardCurrentReward() {

        if (
            !openingSession
                .completeWithDiscard()
        ) {
            return
        }

        onRunDiscardUsed()

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

        batchRewards =
            emptyList()

        discardedBatchRewardIndices.clear()
        continueOpeningAll = false

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
                    if (batchRewards.isNotEmpty()) ChestOpeningState.REWARD_SHOWN else openingSession.state,

                reward =
                    openingSession.currentReward,

                rewards =
                    batchRewards,

                revealProgress =
                    revealProgress(),

                discardEnabled =
                    openingSession.canDiscard,

                discardedRewardIndices =
                    discardedBatchRewardIndices,

                remainingDiscardCount =
                    openingSession.remainingDiscardCount,

                remainingChestCount =
                    remainingChestCount()
            )
    }

    fun renderTexts() {

        if (!isOpen) {
            return
        }

        overlayRenderer
            .renderTexts(
                state =
                    if (batchRewards.isNotEmpty()) ChestOpeningState.REWARD_SHOWN else openingSession.state,

                reward =
                    openingSession.currentReward,

                rewards =
                    batchRewards,

                openedChestCount =
                    (
                        openingSession.openedChestCount +
                            batchRewards.size +
                            if (openingSession.currentReward != null) 1 else 0
                    ).coerceAtMost(openingSession.totalChestCount),

                totalChestCount =
                    openingSession.totalChestCount,

                remainingDiscardCount =
                    openingSession.remainingDiscardCount,

                discardedRewardIndices =
                    discardedBatchRewardIndices,

                remainingChestCount =
                    remainingChestCount()
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
