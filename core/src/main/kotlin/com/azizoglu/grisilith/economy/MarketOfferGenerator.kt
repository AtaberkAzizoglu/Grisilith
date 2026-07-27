package com.azizoglu.grisilith.economy

import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.content.ContentUnlockState
import com.azizoglu.grisilith.item.definition.DefaultItems
import com.azizoglu.grisilith.item.definition.ItemDefinition
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.weapon.definition.DefaultWeapons
import com.azizoglu.grisilith.weapon.definition.WeaponDefinition

object MarketOfferGenerator {

    private enum class WeaponMarketGroup {
        MELEE,
        RANGED,
        SPELL_FOCUS
    }

    fun generateOffers(
        currentStage: Int,
        offerCount: Int = 4,
        luckScore: Int = 10
    ): List<MarketOffer> {
        if (offerCount <= 0) {
            return emptyList()
        }

        val safeStage =
            currentStage.coerceAtLeast(1)

        /*
         * Henüz ağırlık hesabına bağlanmadı.
         * Sonraki Luck commit'i için korunuyor.
         */
        luckScore.coerceIn(
            0,
            50
        )

        return List(
            offerCount
        ) {
            generateSingleOffer(
                currentStage =
                    safeStage
            )
        }
    }

    private fun generateSingleOffer(
        currentStage: Int
    ): MarketOffer {
        /*
         * entries.random() ile üç kategori
         * eşit ihtimalle seçilir.
         */
        return when (
            MarketProductType.entries.random()
        ) {
            MarketProductType.WEAPON -> {
                createRandomWeaponOffer(
                    currentStage =
                        currentStage
                )
                    ?: createRandomStatOffer(
                        currentStage =
                            currentStage
                    )
            }

            MarketProductType.ITEM -> {
                createRandomItemOffer(
                    currentStage =
                        currentStage
                )
                    ?: createRandomStatOffer(
                        currentStage =
                            currentStage
                    )
            }

            MarketProductType.STAT -> {
                createRandomStatOffer(
                    currentStage =
                        currentStage
                )
            }
        }
    }

    private fun createRandomWeaponOffer(
        currentStage: Int
    ): MarketOffer? {
        val availableWeapons =
            DefaultWeapons.all
                .filter { weapon ->
                    weapon.initialUnlockState ==
                        ContentUnlockState.UNLOCKED
                }

        if (availableWeapons.isEmpty()) {
            return null
        }

        /*
         * Önce Weapon ana grubu eşit ihtimalle
         * seçilir.
         */
        val selectedGroup =
            WeaponMarketGroup.entries
                .random()

        val groupedWeapons =
            availableWeapons.filter { weapon ->
                belongsToWeaponGroup(
                    weapon = weapon,
                    group = selectedGroup
                )
            }

        /*
         * Seçilen grup boşsa tüm açık Weapon'lar
         * arasından fallback yapılır.
         */
        val selectedWeapon =
            if (groupedWeapons.isNotEmpty()) {
                groupedWeapons.random()
            } else {
                availableWeapons.random()
            }

        return createWeaponOffer(
            weapon =
                selectedWeapon,

            currentStage =
                currentStage
        )
    }

    private fun belongsToWeaponGroup(
        weapon: WeaponDefinition,
        group: WeaponMarketGroup
    ): Boolean {
        return when (group) {
            WeaponMarketGroup.MELEE -> {
                ContentTag.MELEE_WEAPON in
                    weapon.tags
            }

            WeaponMarketGroup.RANGED -> {
                ContentTag.RANGED_WEAPON in
                    weapon.tags
            }

            WeaponMarketGroup.SPELL_FOCUS -> {
                ContentTag.SPELL_FOCUS in
                    weapon.tags
            }
        }
    }

    private fun createWeaponOffer(
        weapon: WeaponDefinition,
        currentStage: Int
    ): MarketOffer {
        /*
         * Market Weapon teklifleri şimdilik
         * daima Lv.1 olarak gelir.
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

    private fun createRandomItemOffer(
        currentStage: Int
    ): MarketOffer? {
        val availableItems =
            DefaultItems.all
                .filter { item ->
                    item.initialUnlockState ==
                        ContentUnlockState.UNLOCKED
                }

        if (availableItems.isEmpty()) {
            return null
        }

        val selectedItem =
            selectItemByRarityWeight(
                availableItems
            )

        val productLevel = 1

        val price =
            MarketPriceCalculator
                .calculatePrice(
                    productType =
                        MarketProductType.ITEM,

                    productLevel =
                        productLevel,

                    currentStage =
                        currentStage,

                    basePriceOverride =
                        itemBasePrice(
                            selectedItem
                        )
                )

        return MarketOffer(
            productType =
                MarketProductType.ITEM,

            productLevel =
                productLevel,

            title =
                selectedItem.displayName,

            description =
                buildItemDescription(
                    selectedItem
                ),

            price =
                price,

            itemDefinition =
                selectedItem
        )
    }

    private fun selectItemByRarityWeight(
        availableItems:
        List<ItemDefinition>
    ): ItemDefinition {
        val totalWeight =
            availableItems.sumOf { item ->
                item.rarity
                    .marketWeight
                    .toDouble()
            }
                .toFloat()

        if (totalWeight <= 0f) {
            return availableItems.random()
        }

        var randomValue =
            kotlin.random.Random
                .nextFloat() *
                totalWeight

        availableItems.forEach { item ->
            randomValue -=
                item.rarity.marketWeight

            if (randomValue <= 0f) {
                return item
            }
        }

        return availableItems.last()
    }

    private fun itemBasePrice(
        item: ItemDefinition
    ): Int {
        return when (
            item.rarity
        ) {
            com.azizoglu.grisilith.item
                .definition.ItemRarity.COMMON ->
                12

            com.azizoglu.grisilith.item
                .definition.ItemRarity.UNCOMMON ->
                20

            com.azizoglu.grisilith.item
                .definition.ItemRarity.RARE ->
                32

            com.azizoglu.grisilith.item
                .definition.ItemRarity.LEGENDARY ->
                55

            com.azizoglu.grisilith.item
                .definition.ItemRarity.ARTIFACT ->
                85
        }
    }

    private fun buildItemDescription(
        item: ItemDefinition
    ): String {
        val modifierText =
            item.statModifiers
                .joinToString(
                    separator = ", "
                ) { modifier ->
                    "${modifier.statType.displayName} " +
                        "+${modifier.amount}"
                }

        return (
            "${item.rarity.displayName} Item | " +
                modifierText
            )
    }

    private fun createRandomStatOffer(
        currentStage: Int
    ): MarketOffer {
        val selectedStat =
            StatType.entries.random()

        /*
         * Şimdilik bütün market stat kartları
         * +1 verir.
         */
        val statAmount = 1

        val productLevel = 1

        val price =
            MarketPriceCalculator
                .calculatePrice(
                    productType =
                        MarketProductType.STAT,

                    productLevel =
                        productLevel,

                    currentStage =
                        currentStage
                )

        return MarketOffer(
            productType =
                MarketProductType.STAT,

            productLevel =
                productLevel,

            title =
                selectedStat.displayName,

            description =
                "Permanently increases " +
                    "${selectedStat.displayName} " +
                    "by $statAmount for this run.",

            price =
                price,

            statType =
                selectedStat,

            statAmount =
                statAmount
        )
    }

    private fun buildWeaponDescription(
        weapon: WeaponDefinition
    ): String {
        val tagText =
            when {
                weapon.normalAttackSpeedStat ==
                    StatType.HASTE -> {
                    "Spell Focus"
                }

                ContentTag.MELEE_WEAPON in
                    weapon.tags -> {
                    "Melee Weapon"
                }

                ContentTag.RANGED_WEAPON in
                    weapon.tags -> {
                    "Ranged Weapon"
                }

                else -> {
                    "Weapon"
                }
            }

        return (
            "Lv.1 $tagText | " +
                "Damage: ${weapon.baseDamage} | " +
                "Speed: ${
                    formatAttackSpeed(
                        weapon.baseAttacksPerSecond
                    )
                }"
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
