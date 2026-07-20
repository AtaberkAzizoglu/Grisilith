package com.azizoglu.grisilith.economy

import kotlin.math.pow
import kotlin.math.roundToInt

object MarketPriceCalculator {

    private const val STAGE_PRICE_GROWTH = 0.04f

    fun calculatePrice(
        productType: MarketProductType,
        productLevel: Int,
        currentStage: Int,
        basePriceOverride: Int? = null
    ): Int {
        val safeLevel = productLevel.coerceAtLeast(1)
        val safeStage = currentStage.coerceAtLeast(1)

        val basePrice =
            basePriceOverride
                ?: basePriceFor(productType)

        val levelMultiplier =
            levelMultiplierFor(
                productType = productType,
                productLevel = safeLevel
            )

        val stageMultiplier =
            1f +
                (safeStage - 1) *
                STAGE_PRICE_GROWTH

        return (
            basePrice *
                levelMultiplier *
                stageMultiplier
            )
            .roundToInt()
            .coerceAtLeast(1)
    }

    private fun basePriceFor(
        productType: MarketProductType
    ): Int {
        return when (productType) {
            MarketProductType.WEAPON -> 18
            MarketProductType.SKILL -> 16
            MarketProductType.ITEM -> 12
        }
    }

    private fun levelMultiplierFor(
        productType: MarketProductType,
        productLevel: Int
    ): Float {
        val growthPerLevel = when (productType) {
            MarketProductType.WEAPON -> 1.55f
            MarketProductType.SKILL -> 1.65f
            MarketProductType.ITEM -> 1.45f
        }

        return growthPerLevel.pow(
            productLevel - 1
        )
    }
}
