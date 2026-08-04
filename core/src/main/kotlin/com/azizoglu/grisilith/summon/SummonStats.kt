package com.azizoglu.grisilith.summon

import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatType
import kotlin.math.roundToInt

data class SummonStats(
    val maximumHealth: Int,
    val baseDamage: Int,
    val movementSpeed: Float,
    val attackRange: Float
) {

    init {
        require(maximumHealth > 0)
        require(baseDamage > 0)
        require(movementSpeed >= 0f)
        require(attackRange >= 0f)
    }

    companion object {

        fun create(
            type: SummonType,
            playerStats: PlayerStats
        ): SummonStats {

            val shepherd =
                playerStats
                    .totalScore(
                        StatType.SHEPHERD
                    )
                    .coerceAtLeast(
                        0
                    )

            return when (type) {

                SummonType.COW -> {
                    SummonStats(
                        maximumHealth =
                            (
                                95f +
                                    shepherd *
                                    7.5f
                                )
                                .roundToInt()
                                .coerceAtLeast(
                                    1
                                ),

                        baseDamage =
                            (
                                5f +
                                    shepherd *
                                    0.45f
                                )
                                .roundToInt()
                                .coerceAtLeast(
                                    1
                                ),

                        movementSpeed =
                            76f +
                                shepherd *
                                0.75f,

                        attackRange =
                            54f
                    )
                }

                SummonType.IMP -> {
                    SummonStats(
                        maximumHealth =
                            (
                                58f +
                                    shepherd *
                                    5f
                                )
                                .roundToInt()
                                .coerceAtLeast(
                                    1
                                ),

                        baseDamage =
                            (
                                9f +
                                    shepherd *
                                    0.75f
                                )
                                .roundToInt()
                                .coerceAtLeast(
                                    1
                                ),

                        movementSpeed =
                            105f +
                                shepherd *
                                1.1f,

                        attackRange =
                            235f
                    )
                }

                SummonType.KNIGHT_SPIRIT -> {
                    SummonStats(
                        maximumHealth =
                            (
                                82f +
                                    shepherd *
                                    6.2f
                                )
                                .roundToInt()
                                .coerceAtLeast(
                                    1
                                ),

                        baseDamage =
                            (
                                12f +
                                    shepherd *
                                    0.9f
                                )
                                .roundToInt()
                                .coerceAtLeast(
                                    1
                                ),

                        movementSpeed =
                            98f +
                                shepherd *
                                0.9f,

                        attackRange =
                            72f
                    )
                }
            }
        }
    }
}
