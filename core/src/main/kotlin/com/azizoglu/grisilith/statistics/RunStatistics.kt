package com.azizoglu.grisilith.statistics

import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.save.RunSaveStatistics

class RunStatistics {

    var totalDamageDealt: Long =
        0L
        private set

    var totalDamageTaken: Long =
        0L
        private set

    var totalEnemiesKilled: Int =
        0
        private set

    var totalBossesDefeated: Int =
        0
        private set

    var totalFinalBossesDefeated: Int =
        0
        private set

    var totalGoldCollected: Float =
        0f
        private set

    var totalGoldSpent: Float =
        0f
        private set

    var totalChestsOpened:
        Int =
        0
        private set


    private val weaponDamage =
        mutableMapOf<String, Long>()

    val weaponDamageStatistics:
        Map<String, Long>
        get() =
            weaponDamage.toMap()

    fun recordDamageDealt(
        amount: Int,
        weaponName: String
    ) {

        if (amount <= 0) {
            return
        }

        totalDamageDealt +=
            amount

        weaponDamage[weaponName] =
            weaponDamage.getOrDefault(
                weaponName,
                0L
            ) +
                amount
    }

    fun recordDamageTaken(
        amount: Int
    ) {

        if (amount <= 0) {
            return
        }

        totalDamageTaken +=
            amount
    }

    fun recordEnemyKilled(
        enemyType: EnemyType
    ) {

        totalEnemiesKilled +=
            1

        when (enemyType) {

            EnemyType.MINI_BOSS -> {
                totalBossesDefeated +=
                    1
            }

            EnemyType.FINAL_BOSS -> {
                totalFinalBossesDefeated +=
                    1
            }

            EnemyType.NORMAL,
            EnemyType.UNIQUE -> {
            }
        }
    }

    fun recordGoldCollected(
        amount: Float
    ) {

        if (amount <= 0f) {
            return
        }

        totalGoldCollected +=
            amount
    }

    fun recordGoldSpent(
        amount: Float
    ) {

        if (amount <= 0f) {
            return
        }

        totalGoldSpent +=
            amount
    }

    fun recordChestOpened() {

        totalChestsOpened +=
            1
    }

    fun exportSaveState():
        RunSaveStatistics {

        return RunSaveStatistics(
            totalDamageDealt =
                totalDamageDealt,

            totalDamageTaken =
                totalDamageTaken,

            totalEnemiesKilled =
                totalEnemiesKilled,

            totalMiniBossesDefeated =
                totalBossesDefeated,

            totalFinalBossesDefeated =
                totalFinalBossesDefeated,

            totalGoldCollected =
                totalGoldCollected,

            totalGoldSpent =
                totalGoldSpent,

            totalChestsOpened =
                totalChestsOpened,

            damageBySource =
                weaponDamage.toMap()
        )
    }

    fun restoreSaveState(
        saveState:
        RunSaveStatistics
    ) {

        totalDamageDealt =
            saveState.totalDamageDealt
                .coerceAtLeast(
                    0L
                )

        totalDamageTaken =
            saveState.totalDamageTaken
                .coerceAtLeast(
                    0L
                )

        totalEnemiesKilled =
            saveState.totalEnemiesKilled
                .coerceAtLeast(
                    0
                )

        totalBossesDefeated =
            saveState.totalMiniBossesDefeated
                .coerceAtLeast(
                    0
                )

        totalFinalBossesDefeated =
            saveState.totalFinalBossesDefeated
                .coerceAtLeast(
                    0
                )

        totalGoldCollected =
            saveState.totalGoldCollected
                .coerceAtLeast(
                    0f
                )

        totalGoldSpent =
            saveState.totalGoldSpent
                .coerceAtLeast(
                    0f
                )

        totalChestsOpened =
            saveState.totalChestsOpened
                .coerceAtLeast(
                    0
                )

        weaponDamage.clear()

        saveState.damageBySource
            .forEach {
                    source,
                    damage ->

                val normalizedSource =
                    source.trim()

                if (
                    normalizedSource.isNotEmpty() &&
                    damage >
                    0L
                ) {
                    weaponDamage[
                        normalizedSource
                    ] =
                        damage
                }
            }
    }
}
