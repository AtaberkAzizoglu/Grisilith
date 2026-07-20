package com.azizoglu.grisilith.economy

import kotlin.random.Random

object MarketOfferGenerator {

    fun generateOffers(
        currentStage: Int
    ): List<MarketOffer> {
        return MarketProductType.entries
            .shuffled()
            .take(3)
            .map { productType ->
                createOffer(
                    productType = productType,
                    currentStage = currentStage
                )
            }
    }

    private fun createOffer(
        productType: MarketProductType,
        currentStage: Int
    ): MarketOffer {
        val maximumLevel =
            when {
                currentStage >= 30 -> 5
                currentStage >= 20 -> 4
                currentStage >= 10 -> 3
                currentStage >= 5 -> 2
                else -> 1
            }

        val productLevel =
            Random.nextInt(
                from = 1,
                until = maximumLevel + 1
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
                        "+${10 * productLevel}% Attack Speed",
                    price = price
                )
            }

            MarketProductType.SKILL -> {
                MarketOffer(
                    productType = productType,
                    productLevel = productLevel,
                    title = "Skill Upgrade",
                    description =
                        "+${15 * productLevel} Max Health",
                    price = price
                )
            }

            MarketProductType.ITEM -> {
                MarketOffer(
                    productType = productType,
                    productLevel = productLevel,
                    title = "Item Upgrade",
                    description =
                        "+${8 * productLevel}% Movement Speed",
                    price = price
                )
            }
        }
    }
}
