package com.azizoglu.grisilith.statistics

import com.azizoglu.grisilith.enemy.EnemyType

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

}
