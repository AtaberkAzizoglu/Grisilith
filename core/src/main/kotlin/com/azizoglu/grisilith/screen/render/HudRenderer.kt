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
import com.azizoglu.grisilith.summon.SummonManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.azizoglu.grisilith.item.runtime.ActiveItemEffects
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.ui.icon.ContentIconRenderer
import com.azizoglu.grisilith.ui.theme.UiComponentRenderer
import com.azizoglu.grisilith.input.RunInputBindings
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Align
import kotlin.math.ceil

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
    private val weaponRuntimeManager: WeaponRuntimeManager,
    private val summonManager: SummonManager
) {

    private val weaponSlotBounds =
        List(
            WeaponInventory.WEAPON_SLOT_COUNT
        ) {
            Rectangle()
        }

    private val gameSpeedButtonBounds =
        Rectangle()

    private val pauseButtonBounds =
        Rectangle()

    private val goldCoinRenderer =
        GoldCoinRenderer()

    private val grisilithCoinRenderer =
        GrisilithCoinRenderer()

    private val chestTexture by lazy {
        Texture(Gdx.files.internal("pickup/chest.png"), true).also {
            it.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear)
        }
    }

    fun renderShapes(
        showGameplayHud: Boolean
    ) {
        if (!showGameplayHud) {
            return
        }

        updateWeaponSlotBounds()
        updateGameSpeedButtonBounds()
        updatePauseButtonBounds()

        shapeRenderer.projectionMatrix =
            camera.combined

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        // Health and weapon frames are drawn once in the textured SpriteBatch pass.
        renderItemTrackerIcons()

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
        renderStatCardSelection: () -> Unit,
        currentScore: Long = 0L
    ) {
        batch.projectionMatrix =
            camera.combined

        batch.begin()

        if (showGameplayHud) {
            renderThemedHealthBar()
            renderThemedWeaponSlots()
            UiComponentRenderer.drawPauseButton(batch, pauseButtonBounds)

            renderRunInformationTexts(
                displayedStage =
                    displayedStage,

                remainingStageSeconds =
                    remainingStageSeconds,

                formattedGameTime =
                    formattedGameTime
            )

            if(GameSettings.showScore)renderScore(currentScore)
            renderWeaponSlotTexts()

            if (
                GameSettings
                    .showGameSpeedButton
            ) {
                UiComponentRenderer.drawGameSpeedButton(
                    batch,
                    gameSpeedButtonBounds,
                    gameTimeController.gameSpeed
                )
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

    private fun renderThemedHealthBar() {
        val left = camera.position.x - cameraWidth / 2f + HUD_MARGIN
        val top = camera.position.y + cameraHeight / 2f - HUD_MARGIN
        val ratio = if (player.health.maximumHealth <= 0) {
            0f
        } else {
            player.health.currentHealth.toFloat() / player.health.maximumHealth.toFloat()
        }
        UiComponentRenderer.drawHealthBar(
            batch,
            Rectangle(left, top - 57f, HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT),
            ratio
        )
    }

    /** A third line below the stage clock, away from health, currency and pause. */
    private fun renderScore(score: Long) {
        val oldX=font.data.scaleX;val oldY=font.data.scaleY;val oldColor=Color(font.color)
        val text="SCORE ${String.format(java.util.Locale.US,"%,d",score.coerceAtLeast(0L))}"
        val top=camera.position.y+cameraHeight/2f-HUD_MARGIN
        font.data.setScale(.80f)
        val width=com.badlogic.gdx.graphics.g2d.GlyphLayout(font,text).width
        if(width>290f)font.data.setScale(.80f*290f/width)
        font.color=Color(.04f,.025f,.01f,.90f)
        font.draw(batch,text,camera.position.x-149f,top-77f,300f,Align.center,false)
        font.color=Color(.95f,.83f,.54f,1f)
        font.draw(batch,text,camera.position.x-150f,top-76f,300f,Align.center,false)
        font.data.setScale(oldX,oldY);font.color=oldColor
    }

    private fun renderThemedWeaponSlots() {
        weaponSlotBounds.forEachIndexed { slotIndex, bounds ->
            val weapon = weaponInventory.weaponAt(slotIndex)
            val summonPresentation = summonManager.slotPresentationState(slotIndex)
            val state = summonPresentation?.state
                ?: weaponRuntimeManager.abilityStateInSlot(slotIndex)
            val hasActiveAbility = weapon?.unlockedAbility?.activationType ==
                WeaponAbilityActivationType.ACTIVE || summonPresentation != null

            UiComponentRenderer.drawSlot(
                batch,
                bounds,
                selected = hasActiveAbility && state == WeaponAbilityState.READY,
                locked = weapon == null
            )

        }
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

    fun isPauseButtonAt(
        worldX: Float,
        worldY: Float
    ): Boolean {
        updatePauseButtonBounds()
        val centerX = pauseButtonBounds.x + pauseButtonBounds.width / 2f
        val centerY = pauseButtonBounds.y + pauseButtonBounds.height / 2f
        val deltaX = worldX - centerX
        val deltaY = worldY - centerY
        return deltaX * deltaX + deltaY * deltaY <= PAUSE_BUTTON_RADIUS * PAUSE_BUTTON_RADIUS
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

                val summonPresentation =
                    summonManager.slotPresentationState(slotIndex)

                val state =
                    summonPresentation?.state
                        ?: weaponRuntimeManager
                            .abilityStateInSlot(slotIndex)

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
                    ability?.activationType == WeaponAbilityActivationType.ACTIVE ||
                    summonPresentation != null
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
                        summonPresentation?.remainingCooldownSeconds
                            ?: weaponRuntimeManager
                                .abilityRemainingCooldownSecondsInSlot(slotIndex)

                    val total =
                        if (summonPresentation != null) {
                            com.azizoglu.grisilith.summon.SummonSpawnScheduler(
                                sourceWeaponRuntimeId = summonPresentation.weaponRuntimeId,
                                choice = summonPresentation.choice
                            ).cooldownSeconds.coerceAtLeast(0.01f)
                        } else {
                            ability?.cooldownSeconds?.coerceAtLeast(0.01f) ?: 1f
                        }

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
        val left = camera.position.x - cameraWidth / 2f + HUD_MARGIN
        val top = camera.position.y + cameraHeight / 2f - HUD_MARGIN
        val centerX = camera.position.x
        val oldScaleX = font.data.scaleX
        val oldScaleY = font.data.scaleY

        font.data.setScale(1.05f)
        font.color = Color(1f, 0.82f, 0.18f, 1f)
        font.draw(
            batch,
            "STAGE $displayedStage / ${stageController.maximumStage}",
            centerX - 150f,
            top - 22f,
            300f,
            Align.center,
            false
        )

        val stageClock = if (stageController.hasTimeLimit) {
            val safeSeconds = remainingStageSeconds.coerceAtLeast(0)
            "%02d:%02d".format(safeSeconds / 60, safeSeconds % 60)
        } else {
            "∞"
        }
        font.data.setScale(0.9f)
        font.color = Color.WHITE
        font.draw(batch, stageClock, centerX - 80f, top - 49f, 160f, Align.center, false)

        font.color =
            Color(
                0.90f,
                0.90f,
                0.94f,
                1f
            )

        val healthText="${player.health.currentHealth} / ${player.health.maximumHealth} HP"
        val healthLayout=com.badlogic.gdx.graphics.g2d.GlyphLayout(font,healthText)
        val healthScale=minOf(1f,(HEALTH_BAR_WIDTH-20f)/healthLayout.width.coerceAtLeast(1f),
            (HEALTH_BAR_HEIGHT-6f)/healthLayout.height.coerceAtLeast(1f))
        if(healthScale<1f){font.data.setScale(font.data.scaleX*healthScale);healthLayout.setText(font,healthText)}
        font.draw(
            batch,
            healthText,
            left,
            top - 57f + (HEALTH_BAR_HEIGHT+healthLayout.height)/2f,
            HEALTH_BAR_WIDTH,
            Align.center,
            false
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

        goldCoinRenderer.render(
            batch =
                batch,
            font =
                font,
            amount =
                sessionWallet.goldCoins,
            x =
                left,
            y =
                top -
                    64f,
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

        grisilithCoinRenderer.render(
            batch =
                batch,
            font =
                font,
            amount =
                displayedGrisilith,
            x =
                left + 72f,
            y =
                top -
                    64f,
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

        batch.draw(chestTexture, left + 146f, top - 82f, 22f, 22f)
        font.color = Color.WHITE
        val chestsOnGround = activeWorldChestCountProvider().coerceAtLeast(0)
        font.draw(batch, "$stageChestsCollected ($chestsOnGround)", left + 173f, top - 64f)

        ActiveItemEffects.predestinationLabel()?.let { label ->
            font.draw(batch, "Empowered: $label", left + 25f, top - 105f)
        }

        ActiveItemEffects.bountyLabel()?.let { label ->
            font.draw(batch, "Bounty: $label", left + 25f, top - 129f)
        }

        font.data.setScale(oldScaleX, oldScaleY)
    }

    private fun renderItemTrackerIcons() {
        val left = camera.position.x - cameraWidth / 2f + HUD_MARGIN
        val top = camera.position.y + cameraHeight / 2f - HUD_MARGIN

        ActiveItemEffects.predestinationType()?.let { type ->
            shapeRenderer.color = damageTypeColor(type)
            shapeRenderer.rect(left, top - 218f, 18f, 10f)
            shapeRenderer.triangle(
                left + 18f, top - 221f,
                left + 18f, top - 205f,
                left + 25f, top - 213f
            )
        }

        ActiveItemEffects.bountyType()?.let { type ->
            shapeRenderer.color = when (type) {
                EnemyType.NORMAL -> Color(0.75f, 0.75f, 0.78f, 1f)
                EnemyType.UNIQUE -> Color(0.35f, 0.80f, 0.45f, 1f)
                EnemyType.MINI_BOSS -> Color(0.90f, 0.55f, 0.18f, 1f)
                EnemyType.FINAL_BOSS -> Color(0.85f, 0.18f, 0.20f, 1f)
            }
            shapeRenderer.triangle(
                left + 4f, top - 229f,
                left + 18f, top - 229f,
                left + 11f, top - 240f
            )
        }
    }

    private fun damageTypeColor(type: DamageType): Color = when (type) {
        DamageType.FIRE -> Color(1f, 0.28f, 0.08f, 1f)
        DamageType.WATER, DamageType.COLD -> Color(0.20f, 0.62f, 1f, 1f)
        DamageType.AIR -> Color(0.72f, 0.92f, 0.90f, 1f)
        DamageType.LIGHTNING -> Color(1f, 0.90f, 0.16f, 1f)
        DamageType.ACID, DamageType.POISON -> Color(0.45f, 0.90f, 0.18f, 1f)
        DamageType.NECROTIC -> Color(0.58f, 0.24f, 0.72f, 1f)
        DamageType.EARTH -> Color(0.55f, 0.36f, 0.18f, 1f)
        DamageType.SLASHING, DamageType.PIERCING, DamageType.BLUDGEONING ->
            Color(0.82f, 0.82f, 0.86f, 1f)
        DamageType.NONE -> Color.GRAY
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

                ContentIconRenderer.draw(
                    batch = batch,
                    definition = weapon.definition,
                    bounds = bounds
                )

                val summonPresentation = summonManager.slotPresentationState(slotIndex)
                val state = summonPresentation?.state
                    ?: weaponRuntimeManager.abilityStateInSlot(slotIndex)
                val isActiveAbility = weapon.unlockedAbility?.activationType ==
                    WeaponAbilityActivationType.ACTIVE || summonPresentation != null

                if (isActiveAbility && state == WeaponAbilityState.READY) {
                    UiComponentRenderer.drawActiveAbilityFrame(
                        batch,
                        bounds,
                        Color(0.32f, 0.78f, 1f, 1f)
                    )
                } else if (isActiveAbility && state == WeaponAbilityState.ACTIVE) {
                    UiComponentRenderer.drawActiveAbilityFrame(
                        batch,
                        bounds,
                        Color(0.42f, 1f, 0.66f, 1f)
                    )
                } else if (isActiveAbility && state == WeaponAbilityState.COOLDOWN) {
                    UiComponentRenderer.drawCooldownOverlay(batch, bounds, 1f)
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
                    bounds.x + 2f,
                    bounds.y + 16f,
                    bounds.width - 6f,
                    Align.right,
                    false
                )

                font.data.setScale(0.58f)
                if (GameSettings.showAbilityShortcuts && isActiveAbility) {
                    font.color = Color(0.80f, 0.92f, 1f, 1f)
                    font.draw(
                        batch,
                        "[${RunInputBindings.weaponAbilityLabel(slotIndex)}]",
                        bounds.x + 3f,
                        bounds.y + bounds.height - 4f,
                        bounds.width - 6f,
                        Align.right,
                        false
                    )
                }

                if (GameSettings.showAbilityCooldowns && isActiveAbility && state == WeaponAbilityState.COOLDOWN) {
                    val remaining = summonPresentation?.remainingCooldownSeconds
                        ?: weaponRuntimeManager.abilityRemainingCooldownSecondsInSlot(slotIndex)
                    font.color = Color(1f, 0.86f, 0.62f, 1f)
                    font.draw(
                        batch,
                        ceil(remaining.coerceAtLeast(0f)).toInt().toString(),
                        bounds.x + 4f,
                        bounds.y + 15f
                    )
                }
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
                0.5f -> "0.5x"
                2f -> "2x"
                else -> "1x"
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
            gameSpeedButtonBounds.x,
            centerY + 5f,
            gameSpeedButtonBounds.width,
            Align.center,
            false
        )

        font.data.setScale(
            oldScaleX,
            oldScaleY
        )
    }

    private fun updateWeaponSlotBounds() {
        val totalWidth =
            WeaponInventory.WEAPON_SLOT_COUNT * WEAPON_ICON_SIZE +
                (WeaponInventory.WEAPON_SLOT_COUNT - 1) * WEAPON_COLUMN_GAP
        val startX = camera.position.x - totalWidth / 2f
        val y = camera.position.y - cameraHeight / 2f + BOTTOM_HUD_MARGIN

        weaponSlotBounds
            .forEachIndexed {
                    slotIndex,
                    bounds ->

                val x = startX + slotIndex * (WEAPON_ICON_SIZE + WEAPON_COLUMN_GAP)

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

    private fun updatePauseButtonBounds() {
        val right = camera.position.x + cameraWidth / 2f - HUD_MARGIN
        val top = camera.position.y + cameraHeight / 2f - HUD_MARGIN
        pauseButtonBounds.set(
            right - PAUSE_BUTTON_RADIUS * 2f,
            top - PAUSE_BUTTON_RADIUS * 2f,
            PAUSE_BUTTON_RADIUS * 2f,
            PAUSE_BUTTON_RADIUS * 2f
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
            190f

        private const val HEALTH_BAR_HEIGHT =
            26f

        private const val WEAPON_ICON_SIZE =
            48f

        private const val WEAPON_COLUMN_GAP =
            10f

        private const val BOTTOM_HUD_MARGIN =
            18f

        private const val GAME_SPEED_BUTTON_RADIUS =
            25f

        private const val PAUSE_BUTTON_RADIUS =
            25f
    }
}
