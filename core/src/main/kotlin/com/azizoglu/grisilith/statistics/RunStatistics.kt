package com.azizoglu.grisilith.statistics

import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.save.RunSaveStatistics
import java.util.HashMap

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

    var totalUniqueEnemiesKilled: Int = 0
        private set

    val totalNormalEnemiesKilled: Int
        get() = (totalEnemiesKilled.toLong() - totalUniqueEnemiesKilled -
            totalBossesDefeated - totalFinalBossesDefeated).coerceAtLeast(0L).toInt()

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

    private val damageDealtByType = mutableMapOf<DamageType, Long>()
    private val damageTakenByType = mutableMapOf<DamageType, Long>()
    private val damageReducedByType = mutableMapOf<DamageType, Long>()

    val damageDealtTypeStatistics: Map<DamageType, Long> get() = damageDealtByType.toMap()
    val damageTakenTypeStatistics: Map<DamageType, Long> get() = damageTakenByType.toMap()
    val damageReducedTypeStatistics: Map<DamageType, Long> get() = damageReducedByType.toMap()
    val totalDamageReduced: Long get() = damageReducedByType.values.sum()

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
        amount: Int,
        requestedAmount: Int = amount,
        damageType: DamageType = DamageType.NONE
    ) {

        if (requestedAmount <= 0) {
            return
        }

        if (amount > 0) {
            totalDamageTaken += amount
        }

        if (damageType != DamageType.NONE) {
            if (amount > 0) {
                damageTakenByType[damageType] = damageTakenByType.getOrDefault(damageType, 0L) + amount
            }
            val reduced = (requestedAmount - amount).coerceAtLeast(0)
            if (reduced > 0) {
                damageReducedByType[damageType] = damageReducedByType.getOrDefault(damageType, 0L) + reduced
            }
        }
    }

    internal fun recordDamageTypeDealt(amount: Int, damageType: DamageType) {
        if (amount <= 0 || damageType == DamageType.NONE) return
        damageDealtByType[damageType] = damageDealtByType.getOrDefault(damageType, 0L) + amount
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

            EnemyType.UNIQUE -> totalUniqueEnemiesKilled += 1
            EnemyType.NORMAL -> Unit
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
            totalUniqueEnemiesKilled = totalUniqueEnemiesKilled,

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
                HashMap(
                    weaponDamage
                ),

            damageDealtByType = HashMap(damageDealtByType.mapKeys { it.key.name }),
            damageTakenByType = HashMap(damageTakenByType.mapKeys { it.key.name }),
            damageReducedByType = HashMap(damageReducedByType.mapKeys { it.key.name })
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
        totalUniqueEnemiesKilled = saveState.totalUniqueEnemiesKilled.coerceIn(0,
            (totalEnemiesKilled.toLong()-totalBossesDefeated-totalFinalBossesDefeated)
                .coerceAtLeast(0L).toInt())

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
        damageDealtByType.clear()
        damageTakenByType.clear()
        damageReducedByType.clear()

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

        restoreDamageTypes(saveState.damageDealtByType, damageDealtByType)
        restoreDamageTypes(saveState.damageTakenByType, damageTakenByType)
        restoreDamageTypes(saveState.damageReducedByType, damageReducedByType)
    }

    private fun restoreDamageTypes(source: Map<String, Long>, target: MutableMap<DamageType, Long>) {
        source.forEach { (name, amount) ->
            val type = DamageType.entries.firstOrNull { it.name == name } ?: return@forEach
            if (type != DamageType.NONE && amount > 0L) target[type] = amount
        }
    }
}
