package com.azizoglu.grisilith.economy

import kotlin.math.roundToInt

object MarketRerollPriceCalculator {

    private const val BASE_REROLL_PRICE = 8
    private const val STAGE_GROWTH = 0.035f
    private const val REROLL_GROWTH = 0.25f

    fun calculatePrice(
        currentStage: Int,
        rerollsUsedInCurrentMarket: Int
    ): Int {
        val safeRerollCount =
            rerollsUsedInCurrentMarket.coerceAtLeast(0)

        if (safeRerollCount == 0) {
            return 0
        }

        val safeStage =
            currentStage.coerceAtLeast(1)

        val stageMultiplier =
            1f +
                (safeStage - 1) *
                STAGE_GROWTH

        val paidRerollIndex =
            safeRerollCount - 1

        val rerollMultiplier =
            1f +
                paidRerollIndex *
                REROLL_GROWTH

        return (
            BASE_REROLL_PRICE *
                stageMultiplier *
                rerollMultiplier
            )
            .roundToInt()
            .coerceAtLeast(1)
    }
}
