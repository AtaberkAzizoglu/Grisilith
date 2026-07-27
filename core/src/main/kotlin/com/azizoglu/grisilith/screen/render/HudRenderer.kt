package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.economy.SessionWallet
import com.azizoglu.grisilith.enemy.EnemyManager
import com.azizoglu.grisilith.enemy.EnemySpawnScaling
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.stage.StageController
import com.azizoglu.grisilith.stage.StageType
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.RunStatistics
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.util.GameTimeController
import com.azizoglu.grisilith.weapon.runtime.WeaponInventory
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class HudRenderer(
    private val camera: OrthographicCamera,
    private val cameraWidth: Float,
    private val cameraHeight: Float,
    private val batch: SpriteBatch,
    private val font: BitmapFont,
    private val player: Player,
    private val playerStats: PlayerStats,
    private val stageController: StageController,
    private val sessionWallet: SessionWallet,
    private val enemyManager: EnemyManager,
    private val runStatistics: RunStatistics,
    private val gameTimeController: GameTimeController,
    private val weaponInventory: WeaponInventory
) {

    fun render(
        displayedStage: Int,
        remainingStageSeconds: Int,
        formattedGameTime: String,
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

        font.color =
            Color.WHITE

        renderRunInformation(
            displayedStage = displayedStage,
            remainingStageSeconds =
                remainingStageSeconds,
            formattedGameTime =
                formattedGameTime
        )

        renderWeaponInventory()

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

    private fun renderRunInformation(
        displayedStage: Int,
        remainingStageSeconds: Int,
        formattedGameTime: String
    ) {
        val leftX =
            camera.position.x -
                cameraWidth / 2f +
                20f

        var topY =
            camera.position.y +
                cameraHeight / 2f -
                20f

        font.draw(
            batch,
            "Stage: $displayedStage / " +
                "${stageController.maximumStage}",
            leftX,
            topY
        )

        topY -= 28f

        font.draw(
            batch,
            "Stage Type: " +
                "${stageController.currentStageType}",
            leftX,
            topY
        )

        topY -= 28f

        font.draw(
            batch,
            "Stage Time: " +
                "$remainingStageSeconds",
            leftX,
            topY
        )

        topY -= 28f

        font.draw(
            batch,
            "HP: ${player.health.currentHealth}" +
                " / ${player.health.maximumHealth}",
            leftX,
            topY
        )

        topY -= 28f

        font.draw(
            batch,
            "Gold: ${sessionWallet.goldCoins}",
            leftX,
            topY
        )

        topY -= 28f

        val isBossStage =
            stageController.currentStageType !=
                StageType.NORMAL

        val currentEnemyLimit =
            EnemySpawnScaling
                .maximumEnemyCountForStage(
                    stage =
                        stageController.currentStage,
                    isBossStage =
                        isBossStage,
                    enemyCountMultiplier =
                        playerStats
                            .enemyCountMultiplier()
                )

        font.draw(
            batch,
            "Active Enemies: " +
                "${enemyManager.enemyCount} / " +
                "$currentEnemyLimit",
            leftX,
            topY
        )

        topY -= 28f

        font.draw(
            batch,
            "Kills: " +
                "${runStatistics.totalEnemiesKilled}",
            leftX,
            topY
        )

        topY -= 28f

        font.draw(
            batch,
            "Damage: " +
                "${runStatistics.totalDamageDealt}",
            leftX,
            topY
        )

        topY -= 28f

        font.draw(
            batch,
            "Strength: " +
                playerStats.totalScore(
                    StatType.STRENGTH
                ),
            leftX,
            topY
        )

        topY -= 28f

        font.draw(
            batch,
            "Weapon STR Bonus: " +
                playerStats.weaponBonus(
                    StatType.STRENGTH
                ),
            leftX,
            topY
        )

        topY -= 28f

        font.draw(
            batch,
            "Weapon AS Bonus: " +
                playerStats.weaponBonus(
                    StatType.ATTACK_SPEED
                ),
            leftX,
            topY
        )

        topY -= 28f

        font.draw(
            batch,
            "Dexterity: " +
                playerStats.totalScore(
                    StatType.DEXTERITY
                ),
            leftX,
            topY
        )

        topY -= 28f

        font.draw(
            batch,
            "Haste: " +
                playerStats.totalScore(
                    StatType.HASTE
                ),
            leftX,
            topY
        )

        topY -= 28f

        font.draw(
            batch,
            "Attack Speed: " +
                playerStats.totalScore(
                    StatType.ATTACK_SPEED
                ),
            leftX,
            topY
        )

        topY -= 28f

        font.draw(
            batch,
            "Game Time: $formattedGameTime",
            leftX,
            topY
        )

        topY -= 28f

        font.draw(
            batch,
            "Game Speed: " +
                "${gameTimeController.gameSpeed}x",
            leftX,
            topY
        )
    }

    private fun renderWeaponInventory() {
        val startX =
            camera.position.x +
                cameraWidth / 2f -
                285f

        var currentY =
            camera.position.y +
                cameraHeight / 2f -
                20f

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "WEAPONS " +
                "(${weaponInventory.equippedWeaponCount}/" +
                "${WeaponInventory.WEAPON_SLOT_COUNT})",
            startX,
            currentY
        )

        currentY -= 30f

        weaponInventory.weaponSlots
            .forEach { slot ->

                val weapon =
                    slot.weapon

                val text =
                    if (weapon == null) {
                        "${slot.index + 1}: Empty"
                    } else {
                        "${slot.index + 1}: " +
                            "${weapon.displayName} " +
                            "[Lv.${weapon.level}]"
                    }

                font.draw(
                    batch,
                    text,
                    startX,
                    currentY
                )

                currentY -= 26f
            }
    }

    private fun renderCentralStatus() {
        when {
            player.health.isDead -> {
                font.draw(
                    batch,
                    "DEFEATED",
                    camera.position.x - 45f,
                    camera.position.y + 40f
                )
            }

            stageController.isRunComplete -> {
                font.draw(
                    batch,
                    "RUN COMPLETE",
                    camera.position.x - 55f,
                    camera.position.y + 40f
                )
            }

            gameTimeController.isPaused -> {
                font.draw(
                    batch,
                    "PAUSED",
                    camera.position.x - 35f,
                    camera.position.y
                )
            }
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
}
