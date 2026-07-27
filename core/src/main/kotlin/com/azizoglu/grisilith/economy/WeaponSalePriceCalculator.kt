package com.azizoglu.grisilith.economy

import kotlin.math.roundToInt

object WeaponSalePriceCalculator {

    private const val SALE_PRICE_RATIO =
        0.40f

    /*
     * Satış fiyatı:
     *
     * O stage'deki Lv.1 Weapon market fiyatı
     * × Weapon level
     * × %40
     */
    fun calculateSalePrice(
        weaponLevel: Int,
        currentStage: Int
    ): Int {
        val safeWeaponLevel =
            weaponLevel.coerceAtLeast(1)

        val levelOneMarketPrice =
            MarketPriceCalculator
                .calculatePrice(
                    productType =
                        MarketProductType.WEAPON,

                    productLevel = 1,

                    currentStage =
                        currentStage
                )

        return (
            levelOneMarketPrice *
                safeWeaponLevel *
                SALE_PRICE_RATIO
            )
            .roundToInt()
            .coerceAtLeast(1)
    }
}
