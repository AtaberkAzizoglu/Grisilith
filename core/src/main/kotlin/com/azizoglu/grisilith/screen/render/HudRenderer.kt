package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.economy.SessionWallet
import com.azizoglu.grisilith.economy.CurrencyPickupManager
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.reward.RunRewardTracker
import com.azizoglu.grisilith.settings.GameSettings
import com.azizoglu.grisilith.stage.StageController
import com.azizoglu.grisilith.util.GameTimeController
import com.azizoglu.grisilith.weapon.runtime.WeaponInventory
import com.azizoglu.grisilith.weapon.runtime.WeaponRuntimeManager
import com.azizoglu.grisilith.weapon.skill.WeaponAbilityActivationType
import com.azizoglu.grisilith.weapon.skill.WeaponAbilityState
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

class HudRenderer(
    private val camera: OrthographicCamera,
    private val cameraWidth: Float,
    private val cameraHeight: Float,
    private val shapeRenderer: ShapeRenderer,
    private val batch: SpriteBatch,
    private val font: BitmapFont,
    private val player: Player,
    private val stageController: StageController,
    private val sessionWallet: SessionWallet,
    private val runRewardTracker: RunRewardTracker,
    private val currencyPickupManager: CurrencyPickupManager,
    private val currentStageChestCollectedProvider: () -> Int,
    private val activeWorldChestCountProvider: () -> Int,
    private val gameTimeController: GameTimeController,
    private val weaponInventory: WeaponInventory,
    private val weaponRuntimeManager: WeaponRuntimeManager
) {

    private val weaponSlotBounds =
        List(
            WeaponInventory.WEAPON_SLOT_COUNT
        ) {
            Rectangle()
        }

    private val gameSpeedButtonBounds =
        Rectangle()

    private val goldCoinRenderer =
        GoldCoinRenderer()

    private val grisilithCoinRenderer =
        GrisilithCoinRenderer()

    fun renderShapes(
        showGameplayHud: Boolean
    ) {
        if (!showGameplayHud) {
            return
        }

        updateWeaponSlotBounds()
        updateGameSpeedButtonBounds()

        shapeRenderer.projectionMatrix =
            camera.combined

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        renderHealthBar()
        renderWeaponSlots()

        if (
            GameSettings
                .showGameSpeedButton
        ) {
            renderGameSpeedButton()
        }

        shapeRenderer.end()
    }

    fun render(
        displayedStage: Int,
        remainingStageSeconds: Int,
        formattedGameTime: String,
        showGameplayHud: Boolean,
        isRunSummaryOpen: Boolean,
        isMarketOpen: Boolean,
        isShowingChestReward: Boolean,
        isChoosingStatCard: Boolean,
        renderRunSummary: () -> Unit,
        renderMarket: () -> Unit,
        renderChestReward: () -> Unit,
        renderStatCardSelection: () -> Unit
    ) {
        batch.projectionMatrix =
            camera.combined

        batch.begin()

        if (showGameplayHud) {
            renderRunInformationTexts(
                displayedStage =
                    displayedStage,

                remainingStageSeconds =
                    remainingStageSeconds,

                formattedGameTime =
                    formattedGameTime
            )

            renderWeaponSlotTexts()

            if (
                GameSettings
                    .showGameSpeedButton
            ) {
                renderGameSpeedButtonText()
            }
        }

        renderCentralStatus()

        renderOpenScreenLayer(
            isRunSummaryOpen =
                isRunSummaryOpen,

            isMarketOpen =
                isMarketOpen,

            isShowingChestReward =
                isShowingChestReward,

            isChoosingStatCard =
                isChoosingStatCard,

            renderRunSummary =
                renderRunSummary,

            renderMarket =
                renderMarket,

            renderChestReward =
                renderChestReward,

            renderStatCardSelection =
                renderStatCardSelection
        )

        batch.end()
    }

    fun weaponSlotAt(
        worldX: Float,
        worldY: Float
    ): Int? {
        updateWeaponSlotBounds()

        return weaponSlotBounds
            .indexOfFirst {
                it.contains(
                    worldX,
                    worldY
                )
            }
            .takeIf {
                it >= 0
            }
    }

    fun isGameSpeedButtonAt(
        worldX: Float,
        worldY: Float
    ): Boolean {
        if (
            !GameSettings
                .showGameSpeedButton
        ) {
            return false
        }

        updateGameSpeedButtonBounds()

        val centerX =
            gameSpeedButtonBounds.x +
                gameSpeedButtonBounds.width /
                2f

        val centerY =
            gameSpeedButtonBounds.y +
                gameSpeedButtonBounds.height /
                2f

        val deltaX =
            worldX -
                centerX

        val deltaY =
            worldY -
                centerY

        return (
            deltaX *
                deltaX +
                deltaY *
                deltaY
            ) <=
            GAME_SPEED_BUTTON_RADIUS *
            GAME_SPEED_BUTTON_RADIUS
    }

    private fun renderHealthBar() {
        val left =
            camera.position.x -
                cameraWidth /
                2f +
                HUD_MARGIN

        val top =
            camera.position.y +
                cameraHeight /
                2f -
                HUD_MARGIN

        val barY =
            top -
                57f

        shapeRenderer.color =
            Color(
                0.12f,
                0.12f,
                0.15f,
                0.90f
            )

        shapeRenderer.rect(
            left,
            barY,
            HEALTH_BAR_WIDTH,
            HEALTH_BAR_HEIGHT
        )

        val ratio =
            if (
                player.health.maximumHealth <=
                0
            ) {
                0f
            } else {
                player.health.currentHealth
                    .toFloat() /
                    player.health.maximumHealth
                        .toFloat()
            }
                .coerceIn(
                    0f,
                    1f
                )

        shapeRenderer.color =
            Color(
                0.72f,
                0.12f,
                0.16f,
                1f
            )

        shapeRenderer.rect(
            left,
            barY,
            HEALTH_BAR_WIDTH *
                ratio,
            HEALTH_BAR_HEIGHT
        )
    }

    private fun renderWeaponSlots() {
        weaponSlotBounds
            .forEachIndexed {
                    slotIndex,
                    bounds ->

                val weapon =
                    weaponInventory
                        .weaponAt(
                            slotIndex
                        )

                val ability =
                    weapon
                        ?.unlockedAbility

                val state =
                    weaponRuntimeManager
                        .abilityStateInSlot(
                            slotIndex
                        )

                shapeRenderer.color =
                    iconBackgroundColor(
                        hasWeapon =
                            weapon != null,

                        activationType =
                            ability
                                ?.activationType,

                        state =
                            state
                    )

                shapeRenderer.rect(
                    bounds.x,
                    bounds.y,
                    bounds.width,
                    bounds.height
                )

                if (
                    ability
                        ?.activationType ==
                    WeaponAbilityActivationType.ACTIVE
                ) {
                    shapeRenderer.color =
                        abilityGlowColor(
                            state
                        )

                    val glow =
                        if (
                            state ==
                            WeaponAbilityState.ACTIVE
                        ) {
                            4f
                        } else {
                            2f
                        }

                    renderThinBorder(
                        x =
                            bounds.x -
                                glow,

                        y =
                            bounds.y -
                                glow,

                        width =
                            bounds.width +
                                glow *
                                2f,

                        height =
                            bounds.height +
                                glow *
                                2f,

                        thickness =
                            glow
                    )
                }

                if (
                    state ==
                    WeaponAbilityState.COOLDOWN
                ) {
                    val remaining =
                        weaponRuntimeManager
                            .abilityRemainingCooldownSecondsInSlot(
                                slotIndex
                            )

                    val total =
                        ability
                            ?.cooldownSeconds
                            ?.coerceAtLeast(
                                0.01f
                            )
                            ?: 1f

                    val ratio =
                        (
                            remaining /
                                total
                            )
                            .coerceIn(
                                0f,
                                1f
                            )

                    shapeRenderer.color =
                        Color(
                            0f,
                            0f,
                            0f,
                            0.68f
                        )

                    shapeRenderer.rect(
                        bounds.x,
                        bounds.y,
                        bounds.width,
                        bounds.height *
                            ratio
                    )
                }
            }
    }

    private fun renderGameSpeedButton() {
        val centerX =
            gameSpeedButtonBounds.x +
                gameSpeedButtonBounds.width /
                2f

        val centerY =
            gameSpeedButtonBounds.y +
                gameSpeedButtonBounds.height /
                2f

        shapeRenderer.color =
            when (
                gameTimeController.gameSpeed
            ) {
                0.5f -> {
                    Color(
                        0.30f,
                        0.42f,
                        0.58f,
                        0.90f
                    )
                }

                2f -> {
                    Color(
                        0.62f,
                        0.34f,
                        0.24f,
                        0.92f
                    )
                }

                else -> {
                    Color(
                        0.34f,
                        0.48f,
                        0.36f,
                        0.90f
                    )
                }
            }

        shapeRenderer.circle(
            centerX,
            centerY,
            GAME_SPEED_BUTTON_RADIUS,
            32
        )

        shapeRenderer.color =
            Color(
                0.78f,
                0.78f,
                0.84f,
                1f
            )

        renderCircleBorder(
            centerX =
                centerX,

            centerY =
                centerY,

            radius =
                GAME_SPEED_BUTTON_RADIUS,

            thickness =
                2f
        )
    }

    private fun renderRunInformationTexts(
        displayedStage: Int,
        remainingStageSeconds: Int,
        formattedGameTime: String
    ) {
        val left =
            camera.position.x -
                cameraWidth /
                2f +
                HUD_MARGIN

        val top =
            camera.position.y +
                cameraHeight /
                2f -
                HUD_MARGIN

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "Stage $displayedStage / " +
                "${stageController.maximumStage}",
            left,
            top
        )

        font.color =
            Color(
                0.90f,
                0.90f,
                0.94f,
                1f
            )

        font.draw(
            batch,
            "${player.health.currentHealth}" +
                " / ${player.health.maximumHealth} HP",
            left,
            top -
                28f
        )

        val autoGold =
            currencyPickupManager
                .displayedAutoGold

        val autoGoldSuffix =
            if (
                autoGold >
                0
            ) {
                " (+$autoGold)"
            } else {
                ""
            }

        font.draw(
            batch,
            "Gold Coin:",
            left,
            top -
                82f
        )

        goldCoinRenderer.render(
            batch =
                batch,
            font =
                font,
            amount =
                sessionWallet.goldCoins,
            x =
                left +
                    88f,
            y =
                top -
                    82f,
            iconSize =
                20f,
            suffix =
                autoGoldSuffix
        )

        val displayedGrisilith =
            currencyPickupManager
                .displayedGrisilithBalance(
                    runRewardTracker
                        .grisilithCoinsEarned
                )

        val autoGrisilith =
            currencyPickupManager
                .displayedAutoGrisilith

        val autoGrisilithSuffix =
            if (
                autoGrisilith >
                0
            ) {
                " (+$autoGrisilith)"
            } else {
                ""
            }

        font.draw(
            batch,
            "Grisilith Coin:",
            left,
            top -
                106f
        )

        grisilithCoinRenderer.render(
            batch =
                batch,
            font =
                font,
            amount =
                displayedGrisilith,
            x =
                left +
                    100f,
            y =
                top -
                    106f,
            iconSize =
                21f,
            suffix =
                autoGrisilithSuffix
        )

        font.color =
            Color(
                0.70f,
                0.70f,
                0.76f,
                1f
            )

        val stageChestsCollected =
            currentStageChestCollectedProvider()
                .coerceAtLeast(
                    0
                )

        val activeWorldChests =
            activeWorldChestCountProvider()
                .coerceAtLeast(
                    0
                )

        font.draw(
            batch,
            "Chest: $stageChestsCollected ($activeWorldChests)",
            left,
            top -
                130f
        )

        font.draw(
            batch,
            "Stage ${remainingStageSeconds}s",
            left,
            top -
                154f
        )

        font.draw(
            batch,
            "Run $formattedGameTime",
            left,
            top -
                178f
        )
    }

    private fun renderWeaponSlotTexts() {
        val oldScaleX =
            font.data.scaleX

        val oldScaleY =
            font.data.scaleY

        weaponSlotBounds
            .forEachIndexed {
                    slotIndex,
                    bounds ->

                val weapon =
                    weaponInventory
                        .weaponAt(
                            slotIndex
                        )

                if (
                    weapon ==
                    null
                ) {
                    return@forEachIndexed
                }

                font.data.setScale(
                    0.72f
                )

                font.color =
                    Color(
                        0.92f,
                        0.92f,
                        0.96f,
                        1f
                    )

                font.draw(
                    batch,
                    romanLevel(
                        weapon.level
                    ),
                    bounds.x +
                        bounds.width -
                        15f,
                    bounds.y +
                        bounds.height -
                        5f
                )

                font.data.setScale(
                    0.62f
                )

                font.color =
                    Color(
                        0.82f,
                        0.82f,
                        0.88f,
                        1f
                    )

                font.draw(
                    batch,
                    shortenedWeaponName(
                        weapon.displayName
                    ),
                    bounds.x -
                        2f,
                    bounds.y -
                        6f
                )
            }

        font.data.setScale(
            oldScaleX,
            oldScaleY
        )
    }

    private fun renderGameSpeedButtonText() {
        val oldScaleX =
            font.data.scaleX

        val oldScaleY =
            font.data.scaleY

        val centerX =
            gameSpeedButtonBounds.x +
                gameSpeedButtonBounds.width /
                2f

        val centerY =
            gameSpeedButtonBounds.y +
                gameSpeedButtonBounds.height /
                2f

        val text =
            when (
                gameTimeController.gameSpeed
            ) {
                0.5f -> "0.5"
                2f -> "2"
                else -> "1"
            }

        font.data.setScale(
            if (
                gameTimeController.gameSpeed ==
                0.5f
            ) {
                0.72f
            } else {
                0.92f
            }
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            text,
            centerX -
                if (
                    text ==
                    "0.5"
                ) {
                    12f
                } else {
                    4f
                },
            centerY +
                5f
        )

        font.data.setScale(
            oldScaleX,
            oldScaleY
        )
    }

    private fun updateWeaponSlotBounds() {
        val right =
            camera.position.x +
                cameraWidth /
                2f -
                HUD_MARGIN

        val top =
            camera.position.y +
                cameraHeight /
                2f -
                HUD_MARGIN

        weaponSlotBounds
            .forEachIndexed {
                    slotIndex,
                    bounds ->

                val column =
                    slotIndex %
                        WEAPON_COLUMNS

                val row =
                    slotIndex /
                        WEAPON_COLUMNS

                val x =
                    right -
                        WEAPON_ICON_SIZE *
                        WEAPON_COLUMNS -
                        WEAPON_COLUMN_GAP +
                        column *
                        (
                            WEAPON_ICON_SIZE +
                                WEAPON_COLUMN_GAP
                            )

                val y =
                    top -
                        WEAPON_ICON_SIZE -
                        row *
                        (
                            WEAPON_ICON_SIZE +
                                WEAPON_ROW_GAP
                            )

                bounds.set(
                    x,
                    y,
                    WEAPON_ICON_SIZE,
                    WEAPON_ICON_SIZE
                )
            }
    }

    private fun updateGameSpeedButtonBounds() {
        val left =
            camera.position.x -
                cameraWidth /
                2f +
                HUD_MARGIN

        val centerY =
            camera.position.y -
                36f

        gameSpeedButtonBounds.set(
            left,
            centerY -
                GAME_SPEED_BUTTON_RADIUS,
            GAME_SPEED_BUTTON_RADIUS *
                2f,
            GAME_SPEED_BUTTON_RADIUS *
                2f
        )
    }

    private fun iconBackgroundColor(
        hasWeapon: Boolean,
        activationType:
        WeaponAbilityActivationType?,
        state:
        WeaponAbilityState
    ): Color {
        if (!hasWeapon) {
            return Color(
                0.08f,
                0.08f,
                0.10f,
                0.58f
            )
        }

        return when {
            activationType !=
                WeaponAbilityActivationType.ACTIVE -> {
                Color(
                    0.24f,
                    0.24f,
                    0.29f,
                    0.90f
                )
            }

            state ==
                WeaponAbilityState.READY -> {
                Color(
                    0.20f,
                    0.32f,
                    0.46f,
                    0.94f
                )
            }

            state ==
                WeaponAbilityState.ACTIVE -> {
                Color(
                    0.18f,
                    0.44f,
                    0.32f,
                    0.96f
                )
            }

            else -> {
                Color(
                    0.18f,
                    0.18f,
                    0.22f,
                    0.90f
                )
            }
        }
    }

    private fun abilityGlowColor(
        state:
        WeaponAbilityState
    ): Color {
        return when (
            state
        ) {
            WeaponAbilityState.READY -> {
                Color(
                    0.32f,
                    0.72f,
                    0.96f,
                    0.88f
                )
            }

            WeaponAbilityState.ACTIVE -> {
                Color(
                    0.38f,
                    0.96f,
                    0.68f,
                    0.92f
                )
            }

            else -> {
                Color(
                    0.38f,
                    0.38f,
                    0.44f,
                    0.72f
                )
            }
        }
    }

    private fun romanLevel(
        level: Int
    ): String {
        return when (
            level.coerceIn(
                1,
                5
            )
        ) {
            1 -> "I"
            2 -> "II"
            3 -> "III"
            4 -> "IV"
            else -> "V"
        }
    }

    private fun shortenedWeaponName(
        name: String
    ): String {
        val maximumLength =
            11

        return if (
            name.length <=
            maximumLength
        ) {
            name
        } else {
            name.take(
                maximumLength -
                    1
            ) +
                "."
        }
    }

    private fun renderThinBorder(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        thickness: Float
    ) {
        shapeRenderer.rect(
            x,
            y,
            width,
            thickness
        )

        shapeRenderer.rect(
            x,
            y +
                height -
                thickness,
            width,
            thickness
        )

        shapeRenderer.rect(
            x,
            y,
            thickness,
            height
        )

        shapeRenderer.rect(
            x +
                width -
                thickness,
            y,
            thickness,
            height
        )
    }

    private fun renderCircleBorder(
        centerX: Float,
        centerY: Float,
        radius: Float,
        thickness: Float
    ) {
        shapeRenderer.circle(
            centerX,
            centerY,
            radius,
            32
        )

        shapeRenderer.color =
            Color(
                0.16f,
                0.16f,
                0.20f,
                1f
            )

        shapeRenderer.circle(
            centerX,
            centerY,
            (
                radius -
                    thickness
                )
                .coerceAtLeast(
                    1f
                ),
            32
        )
    }

    private fun renderCentralStatus() {
        if (
            gameTimeController.isPaused
        ) {
            font.color =
                Color.WHITE

            font.draw(
                batch,
                "PAUSED",
                camera.position.x -
                    35f,
                camera.position.y
            )
        }
    }

    private fun renderOpenScreenLayer(
        isRunSummaryOpen:
        Boolean,

        isMarketOpen:
        Boolean,

        isShowingChestReward:
        Boolean,

        isChoosingStatCard:
        Boolean,

        renderRunSummary:
            () -> Unit,

        renderMarket:
            () -> Unit,

        renderChestReward:
            () -> Unit,

        renderStatCardSelection:
            () -> Unit
    ) {
        when {
            isRunSummaryOpen -> {
                renderRunSummary()
            }

            isMarketOpen -> {
                renderMarket()
            }

            isShowingChestReward -> {
                renderChestReward()
            }

            isChoosingStatCard -> {
                renderStatCardSelection()
            }
        }
    }

    companion object {
        private const val HUD_MARGIN =
            18f

        private const val HEALTH_BAR_WIDTH =
            176f

        private const val HEALTH_BAR_HEIGHT =
            12f

        private const val WEAPON_COLUMNS =
            2

        private const val WEAPON_ICON_SIZE =
            48f

        private const val WEAPON_COLUMN_GAP =
            10f

        /*
         * İsim satırı için iconlar arasında biraz daha
         * dikey boşluk bırakılır.
         */
        private const val WEAPON_ROW_GAP =
            22f

        private const val GAME_SPEED_BUTTON_RADIUS =
            25f
    }
}
