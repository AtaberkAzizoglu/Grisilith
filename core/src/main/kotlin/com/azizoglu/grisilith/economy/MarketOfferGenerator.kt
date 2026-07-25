package com.azizoglu.grisilith.economy

import com.azizoglu.grisilith.content.ContentUnlockState
import com.azizoglu.grisilith.weapon.definition.DefaultWeapons
import com.azizoglu.grisilith.weapon.definition.WeaponDefinition
import com.azizoglu.grisilith.statistics.StatType

object MarketOfferGenerator {

    fun generateOffers(
        currentStage: Int,
        offerCount: Int = 4,
        luckScore: Int = 10
    ): List<MarketOffer> {
        if (offerCount <= 0) {
            return emptyList()
        }

        /*
         * Luck şimdilik rarity olmadığı için
         * Weapon seviyesini değiştirmiyor.
         *
         * Parametreyi sonraki market kalite ve
         * Item sistemi için koruyoruz.
         */
        val safeLuckScore =
            luckScore.coerceIn(
                0,
                50
            )

        val availableWeapons =
            DefaultWeapons.all
                .filter { weapon ->
                    weapon.initialUnlockState ==
                        ContentUnlockState.UNLOCKED
                }

        if (availableWeapons.isEmpty()) {
            return emptyList()
        }

        /*
         * Önce farklı silahlar göstermeye çalışır.
         * Katalog kart sayısından küçükse kalan
         * kartlarda tekrar çıkmasına izin verir.
         */
        val selectedWeapons =
            selectWeapons(
                availableWeapons =
                    availableWeapons,
                offerCount =
                    offerCount
            )

        return selectedWeapons.map { weapon ->
            createWeaponOffer(
                weapon = weapon,
                currentStage = currentStage,
                luckScore = safeLuckScore
            )
        }
    }

    private fun selectWeapons(
        availableWeapons:
        List<WeaponDefinition>,
        offerCount: Int
    ): List<WeaponDefinition> {
        if (
            availableWeapons.size >=
            offerCount
        ) {
            return availableWeapons
                .shuffled()
                .take(offerCount)
        }

        val result =
            availableWeapons
                .shuffled()
                .toMutableList()

        while (result.size < offerCount) {
            result.add(
                availableWeapons.random()
            )
        }

        return result
    }

    private fun createWeaponOffer(
        weapon: WeaponDefinition,
        currentStage: Int,
        luckScore: Int
    ): MarketOffer {
        /*
         * Silah kopyaları şimdilik daima Lv.1.
         *
         * Aynı silahlar envanterde ayrı tutulabilir
         * veya drag-and-merge ile birleştirilebilir.
         */
        val productLevel = 1

        val price =
            MarketPriceCalculator
                .calculatePrice(
                    productType =
                        MarketProductType.WEAPON,
                    productLevel =
                        productLevel,
                    currentStage =
                        currentStage
                )

        return MarketOffer(
            productType =
                MarketProductType.WEAPON,

            productLevel =
                productLevel,

            title =
                weapon.displayName,

            description =
                buildWeaponDescription(
                    weapon
                ),

            price =
                price,

            weaponDefinition =
                weapon
        )
    }

    private fun buildWeaponDescription(
        weapon: WeaponDefinition
    ): String {
        val tagText =
            when {
                weapon.normalAttackSpeedStat ==
                    StatType.HASTE->
                    "Spell Focus"


                else ->
                    weapon.tags
                        .firstOrNull()
                        ?.name
                        ?.replace(
                            "_",
                            " "
                        )
                        ?.lowercase()
                        ?.replaceFirstChar {
                                character ->
                            character.uppercase()
                        }
                        ?: "Weapon"
            }

        return (
            "Lv.1 $tagText | " +
                "Damage: ${weapon.baseDamage} | " +
                "Speed: ${formatAttackSpeed(
                    weapon.baseAttacksPerSecond
                )}"
            )
    }

    private fun formatAttackSpeed(
        value: Float
    ): String {
        return String.format(
            java.util.Locale.US,
            "%.2f",
            value
        )
    }
}
