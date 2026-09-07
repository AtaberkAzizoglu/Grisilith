package com.azizoglu.grisilith.economy

import com.azizoglu.grisilith.chest.ChestItemRarityCalculator
import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.content.ContentUnlockResolver
import com.azizoglu.grisilith.item.definition.DefaultItems
import com.azizoglu.grisilith.item.definition.ItemDefinition
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.weapon.definition.DefaultWeapons
import com.azizoglu.grisilith.weapon.definition.WeaponDefinition
import com.azizoglu.grisilith.character.CharacterDefinition
import com.azizoglu.grisilith.character.CharacterWeaponPreference
import com.azizoglu.grisilith.item.definition.ItemRarity
import com.azizoglu.grisilith.item.definition.ItemPoolEligibility
import kotlin.random.Random

object MarketOfferGenerator {

    private const val MAXIMUM_LUCK =
        50

    /** Karakterin tercih ettiği Weapon grubu Luck 0'da %36, Luck 50'de %45. */
    private const val PREFERRED_GROUP_CHANCE_AT_ZERO_LUCK = 0.36f
    private const val PREFERRED_GROUP_CHANCE_AT_MAX_LUCK = 0.45f

    private const val WEAPON_OFFER_CHANCE = 0.60f

    /** Sahip olunan Weapon, Luck 0'da +%0,5; Luck 50'de +%5 puan kazanır. */
    private const val OWNED_WEAPON_CHANCE_AT_ZERO_LUCK = 0.005f
    private const val OWNED_WEAPON_CHANCE_AT_MAX_LUCK = 0.05f

    /** Sahip olunan Weapon grubu Luck 0'da +%1; Luck 50'de +%6 puan kazanır. */
    private const val OWNED_WEAPON_GROUP_CHANCE_AT_ZERO_LUCK = 0.01f
    private const val OWNED_WEAPON_GROUP_CHANCE_AT_MAX_LUCK = 0.06f

    private const val CHARACTER_ITEM_WEIGHT_AT_ZERO_LUCK = 1.08f
    private const val CHARACTER_ITEM_WEIGHT_AT_MAX_LUCK = 1.30f

    private enum class WeaponMarketGroup {
        MELEE,
        RANGED,
        SPELL_FOCUS
    }

    fun generateOffers(
        currentStage: Int,
        offerCount: Int = 5,
        luckScore: Int = 10,

        selectedCharacter:
        CharacterDefinition,

        /*
         * Yalnızca benzersiz Weapon ID'leri gelir.
         * Level ve kopya sayısı hesaba katılmaz.
         */
        ownedWeaponIds:
        Set<String> =
            emptySet(),

        ownedItemIds:
        Set<String> =
            emptySet(),

        itemEligibility:
        (ItemDefinition) -> Boolean =
            { true }
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
                    ownedWeaponIds,

                ownedItemIds =
                    ownedItemIds,

                itemEligibility =
                    itemEligibility
            )
        }
    }

    private fun generateSingleOffer(
        currentStage: Int,
        luckScore: Int,
        selectedCharacter:
        CharacterDefinition,
        ownedWeaponIds:
        Set<String>,
        ownedItemIds:
        Set<String>,
        itemEligibility:
        (ItemDefinition) -> Boolean
    ): MarketOffer {

        val weaponOffer =
            {
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
            }

        val itemOffer =
            {
                createRandomItemOffer(
                    currentStage =
                        currentStage,

                    luckScore =
                        luckScore,

                    selectedCharacter =
                        selectedCharacter,

                    ownedItemIds =
                        ownedItemIds,

                    itemEligibility =
                        itemEligibility
                )
            }

        return if (
            Random.nextFloat() < WEAPON_OFFER_CHANCE
        ) {

            weaponOffer()
                ?: itemOffer()
                ?: error(
                    "Market cannot generate an offer: " +
                        "no unlocked Weapons or Items are available."
                )

        } else {

            itemOffer()
                ?: weaponOffer()
                ?: error(
                    "Market cannot generate an offer: " +
                        "no unlocked Weapons or Items are available."
                )
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
                    ContentUnlockResolver.isUnlocked(
                        weapon
                    )
                }

        if (availableWeapons.isEmpty()) {
            return null
        }

        val selectedGroup =
            selectWeaponMarketGroup(
                luckScore =
                    luckScore,

                preferredGroup =
                    preferredGroup,

                availableWeapons =
                    availableWeapons,

                ownedWeaponIds =
                    ownedWeaponIds
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
        CharacterWeaponPreference?,

        availableWeapons:
        List<WeaponDefinition>,

        ownedWeaponIds:
        Set<String>
    ): WeaponMarketGroup {
        val groups =
            WeaponMarketGroup.entries

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

        val preferredMarketGroup =
            preferredGroup?.let {
                when (it) {
                    CharacterWeaponPreference.MELEE ->
                        WeaponMarketGroup.MELEE

                    CharacterWeaponPreference.RANGED ->
                        WeaponMarketGroup.RANGED

                    CharacterWeaponPreference.SPELL_FOCUS ->
                        WeaponMarketGroup.SPELL_FOCUS
                }
            }

        val baseWeights =
            if (preferredMarketGroup == null) {
                groups.associateWith {
                    baseGroupChance
                }
            } else {
                val preferredChance =
                    PREFERRED_GROUP_CHANCE_AT_ZERO_LUCK +
                        (
                            PREFERRED_GROUP_CHANCE_AT_MAX_LUCK -
                                PREFERRED_GROUP_CHANCE_AT_ZERO_LUCK
                            ) * luckProgress

                val otherGroupChance =
                    (
                        1f -
                            preferredChance
                        ) /
                        (groups.size - 1)

                groups.associateWith { group ->
                    if (group == preferredMarketGroup) {
                        preferredChance
                    } else {
                        otherGroupChance
                    }
                }
            }

        val ownedGroups =
            groups.filter { group ->
                availableWeapons.any { weapon ->
                    weapon.id.value in ownedWeaponIds &&
                        belongsToWeaponGroup(
                            weapon = weapon,
                            group = group
                        )
                }
            }

        val unownedGroups =
            groups.filterNot {
                it in ownedGroups
            }

        val weightedGroups =
            if (
                ownedGroups.isEmpty() ||
                unownedGroups.isEmpty()
            ) {
                groups.map { group ->
                    group to baseWeights.getValue(group)
                }
            } else {
                val requestedBonusPerOwnedGroup =
                    OWNED_WEAPON_GROUP_CHANCE_AT_ZERO_LUCK +
                        (
                            OWNED_WEAPON_GROUP_CHANCE_AT_MAX_LUCK -
                                OWNED_WEAPON_GROUP_CHANCE_AT_ZERO_LUCK
                            ) * luckProgress

                val requestedTotalBonus =
                    requestedBonusPerOwnedGroup *
                        ownedGroups.size

                val maximumTotalBonus =
                    unownedGroups.sumOf { group ->
                        baseWeights
                            .getValue(group)
                            .toDouble()
                    }
                        .toFloat()

                val appliedTotalBonus =
                    requestedTotalBonus.coerceAtMost(
                        maximumTotalBonus
                    )

                val bonusPerOwnedGroup =
                    appliedTotalBonus /
                        ownedGroups.size

                val deductionPerUnownedGroup =
                    appliedTotalBonus /
                        unownedGroups.size

                groups.map { group ->
                    val adjustedChance =
                        if (group in ownedGroups) {
                            baseWeights.getValue(group) +
                                bonusPerOwnedGroup
                        } else {
                            baseWeights.getValue(group) -
                                deductionPerUnownedGroup
                        }

                    group to
                        adjustedChance.coerceAtLeast(0f)
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
            unownedWeapons.isEmpty()
        ) {
            return availableWeapons.random()
        }

        val baseChance =
            1f /
                availableWeapons.size

        val luckProgress = safeLuck / MAXIMUM_LUCK.toFloat()
        val requestedBonusPerOwnedWeapon =
            OWNED_WEAPON_CHANCE_AT_ZERO_LUCK +
                (
                    OWNED_WEAPON_CHANCE_AT_MAX_LUCK -
                        OWNED_WEAPON_CHANCE_AT_ZERO_LUCK
                    ) * luckProgress

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
        luckScore: Int,
        selectedCharacter: CharacterDefinition,
        ownedItemIds: Set<String>,
        itemEligibility: (ItemDefinition) -> Boolean
    ): MarketOffer? {
        val availableItems =
            DefaultItems.all
                .filter { item ->
                    ContentUnlockResolver.isUnlocked(
                        item
                    ) &&
                        item.effectId !=
                            com.azizoglu.grisilith.item.definition.ItemEffectId.BROKEN_HEART &&
                        ItemPoolEligibility.isEligible(item, ownedItemIds) &&
                        itemEligibility(item)
                }

        if (availableItems.isEmpty()) {
            return null
        }

        val selectedItem =
            selectItemByRarityWeight(
                availableItems =
                    availableItems,

                luckScore =
                    luckScore,

                selectedCharacter =
                    selectedCharacter,

                currentStage =
                    currentStage
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
        Int,

        selectedCharacter:
        CharacterDefinition,

        currentStage:
        Int
    ): ItemDefinition {
        val itemsByRarity =
            availableItems.groupBy {
                it.rarity
            }

        val distribution =
            ChestItemRarityCalculator.distribution(
                luckScore = luckScore,
                currentStage = currentStage
            )

        val rarityWeights =
            ItemRarity.entries.mapNotNull { rarity ->
                if (itemsByRarity[rarity].isNullOrEmpty()) {
                    null
                } else {
                    rarity to distribution.weightFor(rarity)
                }
            }

        val selectedRarity =
            selectWeighted(
                weightedValues = rarityWeights
            )

        val rarityItems = itemsByRarity.getValue(selectedRarity)
        val luckProgress =
            luckScore.coerceIn(0, MAXIMUM_LUCK) /
                MAXIMUM_LUCK.toFloat()
        val affinityWeight =
            CHARACTER_ITEM_WEIGHT_AT_ZERO_LUCK +
                (
                    CHARACTER_ITEM_WEIGHT_AT_MAX_LUCK -
                        CHARACTER_ITEM_WEIGHT_AT_ZERO_LUCK
                    ) * luckProgress

        return selectWeighted(
            rarityItems.map { item ->
                item to if (
                    isCharacterRelevantItem(
                        item,
                        selectedCharacter.preferredMarketStats
                    )
                ) affinityWeight else 1f
            }
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
                14

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


    private fun isCharacterRelevantItem(
        item: ItemDefinition,
        preferredStats: Set<StatType>
    ): Boolean {
        if (item.statModifiers.any { it.amount > 0 && it.statType in preferredStats }) {
            return true
        }

        val affinityStats = when (item.effectId) {
            com.azizoglu.grisilith.item.definition.ItemEffectId.LONG_SHOT,
            com.azizoglu.grisilith.item.definition.ItemEffectId.GHOST_PROJECTILE,
            com.azizoglu.grisilith.item.definition.ItemEffectId.CRITICAL_MOMENTUM,
            com.azizoglu.grisilith.item.definition.ItemEffectId.CLOSE_CALL,
            com.azizoglu.grisilith.item.definition.ItemEffectId.BLOOD_SCENT ->
                setOf(StatType.DEXTERITY, StatType.CRIT_RATE, StatType.MOVEMENT_SPEED)

            com.azizoglu.grisilith.item.definition.ItemEffectId.SPECIALIST,
            com.azizoglu.grisilith.item.definition.ItemEffectId.CHAOS_THEORY ->
                setOf(StatType.WISDOM, StatType.ELEMENTAL_DAMAGE, StatType.MIGHT_OF_NATURE)

            com.azizoglu.grisilith.item.definition.ItemEffectId.SUMMONERS_PACT,
            com.azizoglu.grisilith.item.definition.ItemEffectId.SHARED_BLOOD,
            com.azizoglu.grisilith.item.definition.ItemEffectId.SHARED_FURY,
            com.azizoglu.grisilith.item.definition.ItemEffectId.SUMMONED_LEGACY ->
                setOf(StatType.SHEPHERD, StatType.WISDOM)

            com.azizoglu.grisilith.item.definition.ItemEffectId.EXECUTIONERS_MARK,
            com.azizoglu.grisilith.item.definition.ItemEffectId.THORNS,
            com.azizoglu.grisilith.item.definition.ItemEffectId.CROWD_HUNGER ->
                setOf(StatType.STRENGTH, StatType.ARMOR_CLASS, StatType.MAX_HP)

            else -> emptySet()
        }
        return affinityStats.any { it in preferredStats }
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

        val effectText =
            if (modifierText.isBlank()) item.description
            else if (item.effectId == null) modifierText
            else "$modifierText | ${item.description}"

        return "${item.rarity.displayName} Item | $effectText"
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
