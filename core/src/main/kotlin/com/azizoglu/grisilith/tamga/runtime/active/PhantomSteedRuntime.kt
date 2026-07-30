package com.azizoglu.grisilith.tamga.runtime.active

import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatType

class PhantomSteedRuntime(
    private val playerStats: PlayerStats,
    private val currentStageProvider: () -> Int
) : ActiveTamgaRuntime(
    durationSeconds = ACTIVE_DURATION_SECONDS,
    cooldownSeconds = COOLDOWN_SECONDS
) {
    companion object {
        private const val ACTIVE_DURATION_SECONDS = 15f
        private const val COOLDOWN_SECONDS = 120f
        private const val STAGE_INTERVAL = 10

        private const val BASE_WEAPON_RANGE_PIXELS = 50f
        private const val WEAPON_RANGE_PER_INTERVAL_PIXELS = 10f

        private const val BASE_MAXIMUM_HEALTH_STAT = 2
        private const val BASE_MOVEMENT_SPEED_STAT = 2
        private const val BASE_HARVEST_PENALTY = 5
        private const val STAT_CHANGE_PER_INTERVAL = 1
    }

    private var player: Player? = null
    private var appliedStageInterval = -1

    var currentBonuses: PhantomSteedBonuses = bonusesForStage(1)
        private set

    val weaponRangeBonusPixels: Float
        get() = if (isActive) currentBonuses.weaponRangePixels else 0f

    fun attachPlayer(player: Player) {
        this.player = player
    }

    override fun onActivated() {
        applyBonuses(force = true)
    }

    override fun onActiveUpdate(delta: Float, progress: Float) {
        applyBonuses(force = false)
    }

    override fun onActiveEnded(reason: ActiveTamgaEndReason) {
        clearBonuses()
    }

    override fun onReset() {
        clearBonuses()
    }

    private fun applyBonuses(force: Boolean) {
        val stage = currentStageProvider().coerceAtLeast(1)
        val interval = (stage - 1) / STAGE_INTERVAL

        if (!force && interval == appliedStageInterval) {
            return
        }

        val wasAlreadyApplied = appliedStageInterval >= 0
        currentBonuses = bonusesForStage(stage)
        appliedStageInterval = interval

        playerStats.setActiveTamgaBonuses(
            mapOf(
                StatType.MAX_HP to currentBonuses.maximumHealthStat,
                StatType.MOVEMENT_SPEED to currentBonuses.movementSpeedStat,
                StatType.HARVEST to currentBonuses.harvestStat
            )
        )

        player?.synchronizeMaximumHealthWithStats(
            healAddedHealth = !wasAlreadyApplied
        )
    }

    private fun clearBonuses() {
        val hadBonuses = appliedStageInterval >= 0
        playerStats.clearActiveTamgaBonuses()
        appliedStageInterval = -1

        if (hadBonuses) {
            player?.synchronizeMaximumHealthWithStats(
                healAddedHealth = false
            )
        }
    }

    private fun bonusesForStage(stage: Int): PhantomSteedBonuses {
        val interval =
            (stage.coerceAtLeast(1) - 1) / STAGE_INTERVAL

        return PhantomSteedBonuses(
            weaponRangePixels =
                BASE_WEAPON_RANGE_PIXELS +
                    interval * WEAPON_RANGE_PER_INTERVAL_PIXELS,
            maximumHealthStat =
                BASE_MAXIMUM_HEALTH_STAT +
                    interval * STAT_CHANGE_PER_INTERVAL,
            movementSpeedStat =
                BASE_MOVEMENT_SPEED_STAT +
                    interval * STAT_CHANGE_PER_INTERVAL,
            harvestStat =
                -(BASE_HARVEST_PENALTY +
                    interval * STAT_CHANGE_PER_INTERVAL)
        )
    }
}
