package com.azizoglu.grisilith.economy

import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.content.ContentUnlockState
import com.azizoglu.grisilith.item.definition.DefaultItems
import com.azizoglu.grisilith.item.definition.ItemDefinition
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.weapon.definition.DefaultWeapons
import com.azizoglu.grisilith.weapon.definition.WeaponDefinition
import com.azizoglu.grisilith.character.CharacterDefinition
import com.azizoglu.grisilith.character.CharacterWeaponPreference
import com.azizoglu.grisilith.item.definition.ItemRarity
import kotlin.random.Random

object MarketOfferGenerator {

    /*
 * Ana market kategorileri Luck'tan etkilenmez.
 */
    private const val WEAPON_PRODUCT_CHANCE =
        0.35f

    private const val ITEM_PRODUCT_CHANCE =
        0.35f

    /*
     * Kalan %30 Stat'tır.
     */
    private const val STAT_PRODUCT_CHANCE =
        0.30f

    private const val MAXIMUM_LUCK =
        50

    /*
     * 50 Luck'ta tercih edilen Weapon grubu:
     * %33,33 → %42
     */
    private const val PREFERRED_GROUP_CHANCE_AT_MAX_LUCK =
        0.42f

    /*
     * Sahip olunan her farklı Weapon için:
     * Her Luck başına doğrudan +%0,1 puan.
     *
     * 50 Luck → +%5 puan.
     */
    private const val OWNED_WEAPON_CHANCE_PER_LUCK =
        0.001f

    /*
     * Tercih edilen Stat:
     * 0 Luck  → ×1,00
     * 50 Luck → ×1,50
     */
    private const val PREFERRED_STAT_WEIGHT_PER_LUCK =
        0.01f

    /*
     * Item rarity bonusu:
     *
     * 50 Luck:
     * Uncommon  ×1,05
     * Rare      ×1,10
     * Legendary ×1,15
     * Artifact  ×1,20
     */
    private const val ITEM_RARITY_WEIGHT_PER_LUCK =
        0.001f

    private enum class WeaponMarketGroup {
        MELEE,
        RANGED,
        SPELL_FOCUS
    }

    fun generateOffers(
        currentStage: Int,
        offerCount: Int = 4,
        luckScore: Int = 10,

        selectedCharacter:
        CharacterDefinition,

        /*
         * Yalnızca benzersiz Weapon ID'leri gelir.
         * Level ve kopya sayısı hesaba katılmaz.
         */
        ownedWeaponIds:
        Set<String> =
            emptySet()
    ): List<MarketOffer> {
        if (offerCount <= 0) {
            return emptyList()
        }

        val safeStage =
            currentStage.coerceAtLeast(1)

        val safeLuck =
            luckScore.coerceIn(
                0,
                MAXIMUM_LUCK
            )

        return List(
            offerCount
        ) {
            generateSingleOffer(
                currentStage =
                    safeStage,

                luckScore =
                    safeLuck,

                selectedCharacter =
                    selectedCharacter,

                ownedWeaponIds =
                    ownedWeaponIds
            )
        }
    }

    private fun generateSingleOffer(
        currentStage: Int,
        luckScore: Int,
        selectedCharacter:
        CharacterDefinition,
        ownedWeaponIds:
        Set<String>
    ): MarketOffer {
        val productRoll =
            Random.nextFloat()

        /*
         * Luck bu ilk seçimi değiştirmez:
         *
         * 0.00–0.35 → Weapon
         * 0.35–0.70 → Item
         * 0.70–1.00 → Stat
         */
        return when {
            productRoll <
                WEAPON_PRODUCT_CHANCE -> {

                createRandomWeaponOffer(
                    currentStage =
                        currentStage,

                    luckScore =
                        luckScore,

                    preferredGroup =
                        selectedCharacter
                            .preferredMarketWeaponGroup,

                    ownedWeaponIds =
                        ownedWeaponIds
                )
                    ?: createRandomStatOffer(
                        currentStage =
                            currentStage,

                        luckScore =
                            luckScore,

                        preferredStats =
                            selectedCharacter
                                .preferredMarketStats
                    )
            }

            productRoll <
                WEAPON_PRODUCT_CHANCE +
                ITEM_PRODUCT_CHANCE -> {

                createRandomItemOffer(
                    currentStage =
                        currentStage,

                    luckScore =
                        luckScore
                )
                    ?: createRandomStatOffer(
                        currentStage =
                            currentStage,

                        luckScore =
                            luckScore,

                        preferredStats =
                            selectedCharacter
                                .preferredMarketStats
                    )
            }

            else -> {
                createRandomStatOffer(
                    currentStage =
                        currentStage,

                    luckScore =
                        luckScore,

                    preferredStats =
                        selectedCharacter
                            .preferredMarketStats
                )
            }
        }
    }

    private fun createRandomWeaponOffer(
        currentStage: Int,
        luckScore: Int,
        preferredGroup:
        CharacterWeaponPreference?,
        ownedWeaponIds:
        Set<String>
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

        val selectedGroup =
            selectWeaponMarketGroup(
                luckScore =
                    luckScore,

                preferredGroup =
                    preferredGroup
            )

        val groupedWeapons =
            availableWeapons.filter { weapon ->
                belongsToWeaponGroup(
                    weapon =
                        weapon,

                    group =
                        selectedGroup
                )
            }

        val selectedWeapon =
            if (groupedWeapons.isNotEmpty()) {
                selectWeaponWithOwnedBonus(
                    availableWeapons =
                        groupedWeapons,

                    ownedWeaponIds =
                        ownedWeaponIds,

                    luckScore =
                        luckScore
                )
            } else {
                selectWeaponWithOwnedBonus(
                    availableWeapons =
                        availableWeapons,

                    ownedWeaponIds =
                        ownedWeaponIds,

                    luckScore =
                        luckScore
                )
            }

        return createWeaponOffer(
            weapon =
                selectedWeapon,

            currentStage =
                currentStage
        )
    }

    private fun selectWeaponMarketGroup(
        luckScore: Int,
        preferredGroup:
        CharacterWeaponPreference?
    ): WeaponMarketGroup {
        val groups =
            WeaponMarketGroup.entries

        if (preferredGroup == null) {
            return groups.random()
        }

        val preferredMarketGroup =
            when (preferredGroup) {
                CharacterWeaponPreference.MELEE ->
                    WeaponMarketGroup.MELEE

                CharacterWeaponPreference.RANGED ->
                    WeaponMarketGroup.RANGED

                CharacterWeaponPreference.SPELL_FOCUS ->
                    WeaponMarketGroup.SPELL_FOCUS
            }

        val safeLuck =
            luckScore.coerceIn(
                0,
                MAXIMUM_LUCK
            )

        val baseGroupChance =
            1f / groups.size

        val luckProgress =
            safeLuck.toFloat() /
                MAXIMUM_LUCK.toFloat()

        val preferredChance =
            baseGroupChance +
                (
                    PREFERRED_GROUP_CHANCE_AT_MAX_LUCK -
                        baseGroupChance
                    ) *
                luckProgress

        val otherGroupChance =
            (
                1f -
                    preferredChance
                ) /
                (
                    groups.size -
                        1
                    )

        val weightedGroups =
            groups.map { group ->
                group to
                    if (
                        group ==
                        preferredMarketGroup
                    ) {
                        preferredChance
                    } else {
                        otherGroupChance
                    }
            }

        return selectWeighted(
            weightedValues =
                weightedGroups
        )
    }
    private fun selectWeaponWithOwnedBonus(
        availableWeapons:
        List<WeaponDefinition>,

        ownedWeaponIds:
        Set<String>,

        luckScore:
        Int
    ): WeaponDefinition {
        require(
            availableWeapons.isNotEmpty()
        )

        if (
            availableWeapons.size ==
            1
        ) {
            return availableWeapons.first()
        }

        val safeLuck =
            luckScore.coerceIn(
                0,
                MAXIMUM_LUCK
            )

        val ownedWeapons =
            availableWeapons.filter { weapon ->
                weapon.id.value in
                    ownedWeaponIds
            }

        val unownedWeapons =
            availableWeapons.filterNot { weapon ->
                weapon.id.value in
                    ownedWeaponIds
            }

        /*
         * Grupta sahip olunan Weapon yoksa veya
         * grubun tamamına sahipsek ayrım yapılamaz.
         */
        if (
            ownedWeapons.isEmpty() ||
            unownedWeapons.isEmpty() ||
            safeLuck <= 0
        ) {
            return availableWeapons.random()
        }

        val baseChance =
            1f /
                availableWeapons.size

        val requestedBonusPerOwnedWeapon =
            safeLuck *
                OWNED_WEAPON_CHANCE_PER_LUCK

        /*
         * Unowned tarafın ihtimali sıfırın altına
         * düşmemeli. Bu nedenle toplam bonus,
         * unowned Weapon'ların toplam taban payıyla
         * sınırlanır.
         */
        val maximumTotalBonus =
            baseChance *
                unownedWeapons.size

        val requestedTotalBonus =
            requestedBonusPerOwnedWeapon *
                ownedWeapons.size

        val appliedTotalBonus =
            requestedTotalBonus.coerceAtMost(
                maximumTotalBonus
            )

        val appliedBonusPerOwnedWeapon =
            appliedTotalBonus /
                ownedWeapons.size

        val deductionPerUnownedWeapon =
            appliedTotalBonus /
                unownedWeapons.size

        val weightedWeapons =
            availableWeapons.map { weapon ->
                val chance =
                    if (
                        weapon.id.value in
                        ownedWeaponIds
                    ) {
                        baseChance +
                            appliedBonusPerOwnedWeapon
                    } else {
                        baseChance -
                            deductionPerUnownedWeapon
                    }

                weapon to
                    chance.coerceAtLeast(
                        0f
                    )
            }

        return selectWeighted(
            weightedValues =
                weightedWeapons
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
        currentStage: Int,
        luckScore: Int
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
                availableItems =
                    availableItems,

                luckScore =
                    luckScore
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
        List<ItemDefinition>,

        luckScore:
        Int
    ): ItemDefinition {
        val safeLuck =
            luckScore.coerceIn(
                0,
                MAXIMUM_LUCK
            )

        val weightedItems =
            availableItems.map { item ->
                val rarityTier =
                    when (item.rarity) {
                        ItemRarity.COMMON ->
                            0

                        ItemRarity.UNCOMMON ->
                            1

                        ItemRarity.RARE ->
                            2

                        ItemRarity.LEGENDARY ->
                            3

                        ItemRarity.ARTIFACT ->
                            4
                    }

                val luckMultiplier =
                    1f +
                        safeLuck *
                        ITEM_RARITY_WEIGHT_PER_LUCK *
                        rarityTier

                item to
                    (
                        item.rarity.marketWeight *
                            luckMultiplier
                        )
                        .coerceAtLeast(
                            0.001f
                        )
            }

        return selectWeighted(
            weightedValues =
                weightedItems
        )
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
        currentStage: Int,
        luckScore: Int,
        preferredStats:
        Set<StatType>
    ): MarketOffer {
        val safeLuck =
            luckScore.coerceIn(
                0,
                MAXIMUM_LUCK
            )

        val preferredWeight =
            1f +
                safeLuck *
                PREFERRED_STAT_WEIGHT_PER_LUCK

        val weightedStats =
            StatType.entries.map { statType ->
                statType to
                    if (
                        statType in
                        preferredStats
                    ) {
                        preferredWeight
                    } else {
                        1f
                    }
            }

        val selectedStat =
            selectWeighted(
                weightedValues =
                    weightedStats
            )

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
        weapon:
        WeaponDefinition
    ): String {
        val typeText =
            weaponTypeText(
                weapon
            )

        val orderedScaling =
            weapon.orderedDamageScaling()

        val primaryScalingText =
            orderedScaling
                .firstOrNull()
                ?.first
                ?.displayName
                ?: "None"

        val secondaryScalingText =
            orderedScaling
                .drop(1)
                .joinToString(
                    separator = ", "
                ) { scaling ->
                    scaling.first.displayName
                }

        return buildString {
            append(
                "Lv.1 $typeText"
            )

            append(
                " | Main: $primaryScalingText"
            )

            if (
                secondaryScalingText
                    .isNotBlank()
            ) {
                append(
                    " | Secondary: " +
                        secondaryScalingText
                )
            }
        }
    }

    private fun weaponTypeText(
        weapon:
        WeaponDefinition
    ): String {
        return when {
            ContentTag.SPELL_FOCUS in
                weapon.tags -> {
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

            /*
             * Eski tanımlarda SPELL_FOCUS etiketi
             * bulunmuyorsa Haste üzerinden güvenli
             * fallback yapılır.
             */
            weapon.normalAttackSpeedStat ==
                StatType.HASTE -> {
                "Spell Focus"
            }

            else -> {
                "Weapon"
            }
        }
    }

    private fun <T> selectWeighted(
        weightedValues:
        List<Pair<T, Float>>
    ): T {
        require(
            weightedValues.isNotEmpty()
        )

        val safeWeightedValues =
            weightedValues.map {
                    weightedValue ->

                weightedValue.first to
                    weightedValue.second
                        .coerceAtLeast(
                            0f
                        )
            }

        val totalWeight =
            safeWeightedValues.sumOf {
                    weightedValue ->

                weightedValue.second
                    .toDouble()
            }
                .toFloat()

        if (totalWeight <= 0f) {
            return safeWeightedValues
                .random()
                .first
        }

        var randomValue =
            Random.nextFloat() *
                totalWeight

        safeWeightedValues.forEach {
                weightedValue ->

            randomValue -=
                weightedValue.second

            if (randomValue <= 0f) {
                return weightedValue.first
            }
        }

        return safeWeightedValues
            .last()
            .first
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
