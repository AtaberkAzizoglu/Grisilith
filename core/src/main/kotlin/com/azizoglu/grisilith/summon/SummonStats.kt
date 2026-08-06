package com.azizoglu.grisilith.summon

import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatType
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
            playerStats: PlayerStats
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

            val attackSpeedMultiplier =
                1f +
                    shepherd *
                    ATTACK_SPEED_BONUS_PER_SHEPHERD

            val movementSpeedMultiplier =
                1f +
                    shepherd *
                    MOVEMENT_SPEED_BONUS_PER_SHEPHERD

            return when (type) {

                SummonType.COW -> {
                    createScaled(
                        baseHealth = 95,
                        baseDamage = 5,
                        baseMovementSpeed = 76f,
                        attackRange = 54f,
                        healthMultiplier =
                            healthMultiplier,
                        damageMultiplier =
                            damageMultiplier,
                        movementSpeedMultiplier =
                            movementSpeedMultiplier,
                        attackSpeedMultiplier =
                            attackSpeedMultiplier
                    )
                }

                SummonType.IMP -> {
                    createScaled(
                        baseHealth = 58,
                        baseDamage = 9,
                        baseMovementSpeed = 105f,
                        attackRange = 235f,
                        healthMultiplier =
                            healthMultiplier,
                        damageMultiplier =
                            damageMultiplier,
                        movementSpeedMultiplier =
                            movementSpeedMultiplier,
                        attackSpeedMultiplier =
                            attackSpeedMultiplier
                    )
                }

                SummonType.KNIGHT_SPIRIT -> {
                    createScaled(
                        baseHealth = 82,
                        baseDamage = 12,
                        baseMovementSpeed = 98f,
                        attackRange = 72f,
                        healthMultiplier =
                            healthMultiplier,
                        damageMultiplier =
                            damageMultiplier,
                        movementSpeedMultiplier =
                            movementSpeedMultiplier,
                        attackSpeedMultiplier =
                            attackSpeedMultiplier
                    )
                }
            }
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
