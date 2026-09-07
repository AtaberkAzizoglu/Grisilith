package com.azizoglu.grisilith.summon

import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.enemy.EnemyScaling
import com.azizoglu.grisilith.enemy.EnemyStats
import com.azizoglu.grisilith.item.runtime.ActiveItemEffects
import com.azizoglu.grisilith.weapon.definition.catalog.LanceDefinition
import kotlin.math.roundToInt

data class SummonStats(
    val maximumHealth: Int,
    val baseDamage: Int,
    val movementSpeed: Float,
    val attackRange: Float,
    val attackSpeedMultiplier: Float
) {

    init {
        require(maximumHealth > 0)
        require(baseDamage > 0)
        require(movementSpeed >= 0f)
        require(attackRange >= 0f)
        require(attackSpeedMultiplier > 0f)
    }

    companion object {

        private const val DAMAGE_BONUS_PER_SHEPHERD =
            0.020f

        private const val HEALTH_BONUS_PER_SHEPHERD =
            0.015f

        private const val ATTACK_SPEED_BONUS_PER_SHEPHERD =
            0.008f

        private const val MOVEMENT_SPEED_BONUS_PER_SHEPHERD =
            0.004f

        fun create(
            type: SummonType,
            playerStats: PlayerStats,
            stage: Int
        ): SummonStats {

            val shepherd =
                playerStats
                    .totalScore(
                        StatType.SHEPHERD
                    )
                    .coerceIn(
                        0,
                        PlayerStats.RUN_MAXIMUM_SCORE
                    )

            val damageMultiplier =
                1f +
                    shepherd *
                    DAMAGE_BONUS_PER_SHEPHERD

            val healthMultiplier =
                1f +
                    shepherd *
                    HEALTH_BONUS_PER_SHEPHERD

            val itemHealthMultiplier =
                ActiveItemEffects.summonHealthMultiplier(playerStats)

            val attackSpeedMultiplier =
                1f +
                    shepherd *
                    ATTACK_SPEED_BONUS_PER_SHEPHERD

            val movementSpeedMultiplier =
                1f +
                    shepherd *
                    MOVEMENT_SPEED_BONUS_PER_SHEPHERD

            val safeStage = stage.coerceAtLeast(1)

            return when (type) {
                SummonType.COW -> {
                    val baseline = EnemyScaling.normalEnemyStats(safeStage)
                    createScaled(
                        baseHealth = (baseline.maximumHealth * 1.65f).roundToInt(),
                        baseDamage = (baseline.contactDamage * 0.72f).roundToInt().coerceAtLeast(1),
                        baseMovementSpeed = baseline.movementSpeed * 0.72f,
                        attackRange = 54f,
                        healthMultiplier = healthMultiplier * itemHealthMultiplier,
                        damageMultiplier = damageMultiplier,
                        movementSpeedMultiplier = movementSpeedMultiplier,
                        attackSpeedMultiplier = attackSpeedMultiplier * 0.88f
                    )
                }

                SummonType.IMP -> {
                    val baseline = EnemyScaling.uniqueEnemyStats(safeStage)
                    createScaled(
                        baseHealth = (baseline.maximumHealth * 0.55f).roundToInt().coerceAtLeast(1),
                        baseDamage = (baseline.contactDamage * 1.55f).roundToInt().coerceAtLeast(1),
                        baseMovementSpeed = baseline.movementSpeed * 1.08f,
                        attackRange = 235f,
                        healthMultiplier = healthMultiplier * itemHealthMultiplier,
                        damageMultiplier = damageMultiplier,
                        movementSpeedMultiplier = movementSpeedMultiplier,
                        attackSpeedMultiplier = attackSpeedMultiplier * 1.18f
                    )
                }

                SummonType.KNIGHT_SPIRIT -> {
                    val baseline = EnemyScaling.miniBossStats(safeStage)
                    createScaled(
                        baseHealth = baseline.maximumHealth,
                        baseDamage = baseline.contactDamage,
                        baseMovementSpeed = baseline.movementSpeed,
                        attackRange = 72f,
                        healthMultiplier = healthMultiplier * itemHealthMultiplier,
                        damageMultiplier = damageMultiplier,
                        movementSpeedMultiplier = movementSpeedMultiplier,
                        attackSpeedMultiplier = attackSpeedMultiplier
                    )
                }

                SummonType.TOAD -> {
                    val baseline = EnemyScaling.normalEnemyStats(safeStage)
                    createScaled(
                        baseHealth = (baseline.maximumHealth * 0.20f).roundToInt().coerceAtLeast(1),
                        baseDamage = (baseline.contactDamage * 0.42f).roundToInt().coerceAtLeast(1),
                        baseMovementSpeed = baseline.movementSpeed * 0.95f,
                        attackRange = 105f,
                        healthMultiplier = healthMultiplier * itemHealthMultiplier,
                        damageMultiplier = damageMultiplier,
                        movementSpeedMultiplier = movementSpeedMultiplier,
                        attackSpeedMultiplier = attackSpeedMultiplier * 0.88f
                    )
                }

                SummonType.GUARD ->
                    createGuard(
                        sourceStats =
                            EnemyScaling
                                .normalEnemyStats(safeStage),
                        lanceLevel =
                            (safeStage / 10 + 1)
                                .coerceIn(1, 5)
                    )
            }
        }

        fun createGuard(
            sourceStats: EnemyStats,
            lanceLevel: Int
        ): SummonStats {
            val safeLevel = lanceLevel.coerceIn(1, 5)

            val lance = LanceDefinition.definition
            val levelStats = lance.statsForLevel(safeLevel)
            val levelDamageMultiplier =
                1f +
                    (safeLevel - 1) *
                    0.20f

            return SummonStats(
                maximumHealth =
                    sourceStats.maximumHealth,
                baseDamage =
                    (
                        sourceStats.contactDamage *
                            levelDamageMultiplier
                        )
                        .roundToInt()
                        .coerceAtLeast(1),
                movementSpeed =
                    sourceStats.movementSpeed,
                attackRange =
                    lance.baseRange *
                        levelStats.rangeMultiplier *
                        0.58f,
                attackSpeedMultiplier =
                    levelStats.attackSpeedMultiplier
            )
        }

        private fun createScaled(
            baseHealth: Int,
            baseDamage: Int,
            baseMovementSpeed: Float,
            attackRange: Float,
            healthMultiplier: Float,
            damageMultiplier: Float,
            movementSpeedMultiplier: Float,
            attackSpeedMultiplier: Float
        ): SummonStats {

            return SummonStats(
                maximumHealth =
                    (
                        baseHealth *
                            healthMultiplier
                        )
                        .roundToInt()
                        .coerceAtLeast(1),

                baseDamage =
                    (
                        baseDamage *
                            damageMultiplier
                        )
                        .roundToInt()
                        .coerceAtLeast(1),

                movementSpeed =
                    (
                        baseMovementSpeed *
                            movementSpeedMultiplier
                        )
                        .coerceAtLeast(0f),

                attackRange =
                    attackRange.coerceAtLeast(0f),

                attackSpeedMultiplier =
                    attackSpeedMultiplier
                        .coerceAtLeast(0.10f)
            )
        }
    }
}
