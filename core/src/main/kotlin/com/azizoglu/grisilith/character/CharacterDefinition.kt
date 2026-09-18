package com.azizoglu.grisilith.character

import com.azizoglu.grisilith.content.ContentCategory
import com.azizoglu.grisilith.content.ContentDefinition
import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.content.ContentUnlockState
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.character.trait.CharacterTraitDefinition

data class CharacterDefinition(
    override val id: ContentId,
    override val displayName: String,
    override val description: String,
    val minionDescription: String,
    val baseStats: Map<StatType, Int>,

    val trait: CharacterTraitDefinition,

    val canEquipArmor: Boolean = true,
    val unarmoredBaseHealth: Int = 1,
    val unarmoredBaseArmor: Int = 0,
    val unarmoredBaseMagicResistance: Int = 0,

    /*
     * Karaktere özgü ham kritik değerleri.
     * Crit Rate ve Crit Damage Stat katkıları
     * bu değerlerin üzerine eklenir.
     */
    val baseCritRatePercent:
    Int = 0,

    val baseCritDamagePercent:
    Int = 100,

    val preferredMarketStats:
    Set<StatType> =
        emptySet(),

    val preferredMarketWeaponGroup:
    CharacterWeaponPreference? =
        null,

    override val initialUnlockState:
    ContentUnlockState =
        ContentUnlockState.UNLOCKED
) : ContentDefinition {

    override val category:
        ContentCategory =
        ContentCategory.CHARACTER

    override val tags:
        Set<ContentTag> =
        emptySet()

    init {
        require(unarmoredBaseHealth > 0 && unarmoredBaseArmor >= 0 && unarmoredBaseMagicResistance >= 0)
        require(displayName.isNotBlank()) {
            "Character name cannot be blank."
        }

        StatType.entries.forEach { statType ->
            require(statType in baseStats) {
                "Missing base stat $statType for $id."
            }

            val value =
                baseStats.getValue(statType)

            require(value in 0..20) {
                "Base stat $statType must be between 0 and 20 for $id."
            }
        }

        require(
            baseCritRatePercent in 0..100
        ) {
            "Base Crit Rate must be between 0 and 100 for $id."
        }

        require(
            baseCritDamagePercent >= 100
        ) {
            "Base Crit Damage must be at least 100 for $id."
        }

        require(
            preferredMarketStats.all { statType ->
                statType in StatType.entries
            }
        )
    }

    fun baseStat(
        statType: StatType
    ): Int {
        return baseStats.getValue(statType)
    }
}
