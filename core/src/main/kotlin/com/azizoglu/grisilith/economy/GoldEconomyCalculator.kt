package com.azizoglu.grisilith.economy

import com.azizoglu.grisilith.enemy.EnemyType
import com.badlogic.gdx.math.MathUtils
import kotlin.math.ceil

object GoldEconomyCalculator {

    const val MAXIMUM_HARVEST_SCORE =
        50

    fun enemyDrop(
        enemyType: EnemyType,
        harvestScore: Int
    ): Int {
        val harvestRatio =
            harvestRatio(
                harvestScore
            )

        return when (
            enemyType
        ) {
            EnemyType.NORMAL -> {
                1
            }

            EnemyType.UNIQUE -> {
                weightedRoll(
                    minimum = 1,
                    maximum = 4,
                    harvestRatio =
                        harvestRatio
                ) {
                        value ->

                    /*
                     * Harvest 0:
                     * 1/2/3/4 are equally likely.
                     *
                     * Harvest 50:
                     * only 3 and 4 remain possible,
                     * with 4 slightly favoured.
                     */
                    when (value) {
                        1,
                        2 -> 0f

                        3 -> 1f
                        4 -> 1.35f

                        else -> 0f
                    }
                }
            }

            EnemyType.MINI_BOSS -> {
                weightedRoll(
                    minimum = 10,
                    maximum = 25,
                    harvestRatio =
                        harvestRatio
                ) {
                        value ->

                    /*
                     * User supplied the max-Harvest Mini Boss range as an
                     * approximate value. To keep it proportional to Unique:
                     *
                     * Unique minimum shifts 1 -> 3 across a 1..4 range,
                     * i.e. two thirds of its span.
                     *
                     * Applying that same shift to 10..25 gives 20..25.
                     *
                     * Harvest 50 therefore produces only 20..25, with the
                     * higher values increasingly favoured.
                     */
                    if (
                        value <
                        20
                    ) {
                        0f
                    } else {
                        1f +
                            (
                                value -
                                    20
                                ) *
                            0.15f
                    }
                }
            }

            EnemyType.FINAL_BOSS -> {
                0
            }
        }
    }

    fun preBossStageGrowthBonus(
        currentGold: Int,
        harvestScore: Int,
        defeatedMiniBossCount: Int
    ): Int {
        if (
            currentGold <=
            0
        ) {
            return 0
        }

        val harvestGrowth =
            harvestRatio(
                harvestScore
            ) *
                MAXIMUM_HARVEST_GROWTH

        val bossGrowth =
            defeatedMiniBossCount
                .coerceAtLeast(
                    0
                ) *
                GROWTH_PER_DEFEATED_MINI_BOSS

        val totalGrowth =
            harvestGrowth +
                bossGrowth

        if (
            totalGrowth <=
            0f
        ) {
            return 0
        }

        /*
         * Explicit economy rule:
         * non-integer growth always rounds UP.
         */
        return ceil(
            currentGold *
                totalGrowth
        )
            .toInt()
            .coerceAtLeast(
                0
            )
    }

    fun isBossStage(
        stage: Int
    ): Boolean {
        return stage >
            1 &&
            stage %
            BOSS_STAGE_INTERVAL ==
            0
    }

    private fun harvestRatio(
        harvestScore: Int
    ): Float {
        return harvestScore
            .coerceIn(
                0,
                MAXIMUM_HARVEST_SCORE
            )
            .toFloat() /
            MAXIMUM_HARVEST_SCORE
                .toFloat()
    }

    private fun weightedRoll(
        minimum: Int,
        maximum: Int,
        harvestRatio: Float,
        maximumHarvestWeight:
            (Int) -> Float
    ): Int {
        val safeHarvest =
            harvestRatio.coerceIn(
                0f,
                1f
            )

        val values =
            (
                minimum..
                    maximum
                )
                .toList()

        val weights =
            values.map {
                    value ->

                val baseWeight =
                    1f

                val maxHarvestWeight =
                    maximumHarvestWeight(
                        value
                    )
                        .coerceAtLeast(
                            0f
                        )

                MathUtils.lerp(
                    baseWeight,
                    maxHarvestWeight,
                    safeHarvest
                )
                    .coerceAtLeast(
                        0f
                    )
            }

        val totalWeight =
            weights.sum()

        if (
            totalWeight <=
            0f
        ) {
            return minimum
        }

        var roll =
            MathUtils.random() *
                totalWeight

        values.forEachIndexed {
                index,
                value ->

            roll -=
                weights[index]

            if (
                roll <=
                0f
            ) {
                return value
            }
        }

        return maximum
    }

    private const val MAXIMUM_HARVEST_GROWTH =
        0.50f

    private const val GROWTH_PER_DEFEATED_MINI_BOSS =
        0.03f

    private const val BOSS_STAGE_INTERVAL =
        5
}
