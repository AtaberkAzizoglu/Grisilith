package com.azizoglu.grisilith.statistics

import kotlin.random.Random

object StatRewardAmountCalculator {

    private const val MAXIMUM_LUCK = 50

    fun rollAmount(
        currentStage: Int,
        luckScore: Int,
        random: Random = Random.Default
    ): Int {
        val safeStage =
            currentStage.coerceAtLeast(1)

        val safeLuck =
            luckScore.coerceIn(
                0,
                MAXIMUM_LUCK
            )

        val luckRatio =
            safeLuck.toFloat() /
                MAXIMUM_LUCK.toFloat()

        val weightedAmounts =
            when {
                safeStage < 20 ->
                    listOf(
                        1 to 1f
                    )

                safeStage < 40 ->
                    listOf(
                        1 to lerp(80f, 35f, luckRatio),
                        2 to lerp(20f, 65f, luckRatio)
                    )

                safeStage < 60 ->
                    listOf(
                        1 to lerp(65f, 20f, luckRatio),
                        2 to lerp(27f, 40f, luckRatio),
                        3 to lerp(8f, 40f, luckRatio)
                    )

                else ->
                    listOf(
                        1 to lerp(55f, 12f, luckRatio),
                        2 to lerp(28f, 28f, luckRatio),
                        3 to lerp(14f, 38f, luckRatio),
                        5 to lerp(3f, 22f, luckRatio)
                    )
            }

        return weightedRoll(
            weightedAmounts,
            random
        )
    }

    private fun weightedRoll(
        weightedAmounts: List<Pair<Int, Float>>,
        random: Random
    ): Int {
        require(weightedAmounts.isNotEmpty())

        val totalWeight =
            weightedAmounts.sumOf {
                it.second
                    .coerceAtLeast(0f)
                    .toDouble()
            }.toFloat()

        if (totalWeight <= 0f) {
            return weightedAmounts.first().first
        }

        var roll =
            random.nextFloat() *
                totalWeight

        weightedAmounts.forEach { weightedAmount ->
            roll -= weightedAmount.second

            if (roll <= 0f) {
                return weightedAmount.first
            }
        }

        return weightedAmounts.last().first
    }

    private fun lerp(
        start: Float,
        end: Float,
        ratio: Float
    ): Float {
        val safeRatio =
            ratio.coerceIn(
                0f,
                1f
            )

        return start +
            (end - start) *
            safeRatio
    }
}
