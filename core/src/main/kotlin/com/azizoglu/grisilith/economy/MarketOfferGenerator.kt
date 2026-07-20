package com.azizoglu.grisilith.economy

import kotlin.random.Random

object MarketOfferGenerator {

    fun generateOffers(
        currentStage: Int,
        offerCount: Int = 3,
        luckScore: Int = 10
    ): List<MarketOffer> {
        if (offerCount <= 0) {
            return emptyList()
        }

        return List(offerCount) {
            createRandomOffer(
                currentStage = currentStage,
                luckScore = luckScore
            )
        }
    }

    private fun createRandomOffer(
        currentStage: Int,
        luckScore: Int
    ): MarketOffer {
        val productType =
            MarketProductType.entries.random()

        val maximumLevel =
            maximumLevelForStage(
                currentStage
            )

        val productLevel =
            rollProductLevel(
                maximumLevel = maximumLevel,
                luckScore = luckScore
            )

        val price =
            MarketPriceCalculator.calculatePrice(
                productType = productType,
                productLevel = productLevel,
                currentStage = currentStage
            )

        return when (productType) {
            MarketProductType.WEAPON -> {
                MarketOffer(
                    productType = productType,
                    productLevel = productLevel,
                    title = "Weapon Upgrade",
                    description =
                        "+$productLevel Attack Speed score",
                    price = price
                )
            }

            MarketProductType.SKILL -> {
                MarketOffer(
                    productType = productType,
                    productLevel = productLevel,
                    title = "Skill Upgrade",
                    description =
                        "+$productLevel Max HP score",
                    price = price
                )
            }

            MarketProductType.ITEM -> {
                MarketOffer(
                    productType = productType,
                    productLevel = productLevel,
                    title = "Item Upgrade",
                    description =
                        "+$productLevel Movement Speed score",
                    price = price
                )
            }
        }
    }

    private fun maximumLevelForStage(
        currentStage: Int
    ): Int {
        return when {
            currentStage >= 30 -> 5
            currentStage >= 20 -> 4
            currentStage >= 10 -> 3
            currentStage >= 5 -> 2
            else -> 1
        }
    }

    private fun rollProductLevel(
        maximumLevel: Int,
        luckScore: Int
    ): Int {
        if (maximumLevel <= 1) {
            return 1
        }

        val safeLuck =
            luckScore.coerceIn(
                1,
                30
            )

        /*
         * Luck yükseldikçe rastgele sonuç
         * maksimum seviyeye doğru kayar.
         */
        val luckPower =
            1f +
                (safeLuck - 10)
                    .coerceAtLeast(0) *
                0.06f

        val randomValue =
            Random.nextFloat()

        val luckAdjustedValue =
            randomValue.pow(
                1f / luckPower
            )

        return (
            1f +
                luckAdjustedValue *
                maximumLevel
            )
            .toInt()
            .coerceIn(
                1,
                maximumLevel
            )
    }

    private fun Float.pow(
        exponent: Float
    ): Float {
        return Math.pow(
            toDouble(),
            exponent.toDouble()
        ).toFloat()
    }
}
