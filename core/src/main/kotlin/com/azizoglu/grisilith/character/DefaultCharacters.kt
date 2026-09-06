package com.azizoglu.grisilith.character

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.content.ContentUnlockState
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.character.trait.DefaultCharacterTraits

object DefaultCharacters {

    val SirImbert =
        CharacterDefinition(
            id =
                ContentId("CHR_SIR_IMBERT"),

            displayName =
                "Sir Imbert \"the Yellow\"",

            description =
                "The magic-phobic yellow knight will not stop until he and his comrades are the only ones left on the world who practice magic.",

            minionDescription =
                "Small armored weapon bearers.",

            trait =
                DefaultCharacterTraits.theHypocrisy,

            baseStats =
                createStats(
                    maxHp = 12,
                    armorClass = 13,
                    strength = 12,
                    dexterity = 7,
                    wisdom = 4,
                    haste = 2,
                    attackSpeed = 9,
                    critRate = 8,
                    critDamage = 7,
                    movementSpeed = 5,
                    luck = 4,
                    harvest = 5,
                    elementalDamage = 3,
                    mightOfNature = 1,
                    shepherd = 2,
                    piercing = 10,
                    enemyCount = 5,
                    hpRegeneration = 0,
                    lifeSteal = 1
                ),

            baseCritRatePercent =
                15,

            baseCritDamagePercent =
                125,

            preferredMarketStats =
                setOf(
                    StatType.STRENGTH,
                    StatType.ATTACK_SPEED,
                    StatType.ELEMENTAL_DAMAGE,
                    StatType.PIERCING,
                    StatType.ARMOR_CLASS,
                    StatType.MAX_HP
                ),

            preferredMarketWeaponGroup =
                CharacterWeaponPreference.MELEE,

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )

    val ywalethYoster =
        CharacterDefinition(
            id =
                ContentId(
                    "CHR_YWALETH_YOSTER"
                ),

            displayName =
                "Ywaleth Yoster",

            description =
                "When she died, the gods did not accept it and decided that the revenge she deserved was the revenge of the gods as well. She will continue to be among us, suffering until she finishes his work.",

            minionDescription =
                "Small hooded weapon bearers wearing dark cloaks.",

            trait =
                DefaultCharacterTraits.hurry,

            baseStats =
                createStats(
                    maxHp = 9,
                    armorClass = 7,
                    strength = 3,
                    dexterity = 15,
                    wisdom = 2,
                    haste = 0,
                    attackSpeed = 12,
                    critRate = 10,
                    critDamage = 11,
                    movementSpeed = 9,
                    luck = 5,
                    harvest = 9,
                    elementalDamage = 0,
                    mightOfNature = 1,
                    shepherd = 2,
                    piercing = 11,
                    enemyCount = 4,
                    hpRegeneration = 0,
                    lifeSteal = 0
                ),

            baseCritRatePercent =
                30,

            baseCritDamagePercent =
                150,

            preferredMarketStats =
                setOf(
                    StatType.DEXTERITY,
                    StatType.ATTACK_SPEED,
                    StatType.CRIT_RATE,
                    StatType.CRIT_DAMAGE,
                    StatType.MOVEMENT_SPEED
                ),

            preferredMarketWeaponGroup =
                CharacterWeaponPreference.RANGED,

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )

    val lavenaVol =
        CharacterDefinition(
            id =
                ContentId("CHR_LAVENA_VOL"),

            displayName =
                "Lavena Vol",

            description =
                "One of the greatest wizard of the time specialized in forbidden spells for the approaching danger. Now the approaching danger is her.",

            minionDescription =
                "Small mage weapon bearers.",

            trait =
                DefaultCharacterTraits.necromancy,

            baseStats =
                createStats(
                    maxHp = 4,
                    armorClass = 5,
                    strength = 2,
                    dexterity = 5,
                    wisdom = 15,
                    haste = 11,
                    attackSpeed = 5,
                    critRate = 3,
                    critDamage = 1,
                    movementSpeed = 9,
                    luck = 8,
                    harvest = 7,
                    elementalDamage = 10,
                    mightOfNature = 15,
                    shepherd = 4,
                    piercing = 0,
                    enemyCount = 3,
                    hpRegeneration = 0,
                    lifeSteal = 3
                ),

            baseCritRatePercent =
                5,

            baseCritDamagePercent =
                100,

            preferredMarketStats =
                setOf(
                    StatType.WISDOM,
                    StatType.HASTE,
                    StatType.ELEMENTAL_DAMAGE,
                    StatType.MIGHT_OF_NATURE,
                    StatType.SHEPHERD,
                    StatType.LIFE_STEAL
                ),

            preferredMarketWeaponGroup =
                CharacterWeaponPreference.SPELL_FOCUS,

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )

    val koba =
        CharacterDefinition(
            id =
                ContentId("CHR_KOBA"),
            displayName =
                "Koba",
            description =
                "A half-orc barbarian who fights almost bare-skinned, " +
                    "turning his remaining vitality into savage force.",
            minionDescription =
                "Half-naked barbarian weapon bearers.",
            trait =
                DefaultCharacterTraits.bloodweight,
            baseStats =
                createStats(
                    maxHp = 15,
                    armorClass = 6,
                    strength = 13,
                    dexterity = 5,
                    wisdom = 2,
                    haste = 2,
                    attackSpeed = 7,
                    critRate = 4,
                    critDamage = 8,
                    movementSpeed = 7,
                    luck = 2,
                    harvest = 4,
                    elementalDamage = 3,
                    mightOfNature = 2,
                    shepherd = 4,
                    piercing = 9,
                    enemyCount = 7,
                    hpRegeneration = 8,
                    lifeSteal = 3
                ),
            baseCritRatePercent =
                10,
            baseCritDamagePercent =
                135,
            preferredMarketStats =
                setOf(
                    StatType.MAX_HP,
                    StatType.STRENGTH,
                    StatType.HP_REGENERATION,
                    StatType.LIFE_STEAL,
                    StatType.ARMOR_CLASS
                ),
            preferredMarketWeaponGroup =
                CharacterWeaponPreference.MELEE,
            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )

    val linlosDustman =
        CharacterDefinition(
            id =
                ContentId("CHR_LINLOS_DUSTMAN"),
            displayName =
                "Linlos Dustman",
            description =
                "A halfling druid who lets poison, dust and fragile Toads " +
                    "fight in his place.",
            minionDescription =
                "Small druids carrying weapons among leaves and spores.",
            trait =
                DefaultCharacterTraits.theToadCycle,
            baseStats =
                createStats(
                    maxHp = 4,
                    armorClass = 5,
                    strength = 2,
                    dexterity = 8,
                    wisdom = 13,
                    haste = 9,
                    attackSpeed = 5,
                    critRate = 4,
                    critDamage = 3,
                    movementSpeed = 9,
                    luck = 7,
                    harvest = 9,
                    elementalDamage = 3,
                    mightOfNature = 7,
                    shepherd = 14,
                    piercing = 2,
                    enemyCount = 6,
                    hpRegeneration = 0,
                    lifeSteal = 0
                ),
            baseCritRatePercent =
                10,
            baseCritDamagePercent =
                110,
            preferredMarketStats =
                setOf(
                    StatType.SHEPHERD,
                    StatType.WISDOM,
                    StatType.HASTE,
                    StatType.ENEMY_COUNT,
                    StatType.MIGHT_OF_NATURE
                ),
            preferredMarketWeaponGroup =
                CharacterWeaponPreference.SPELL_FOCUS,
            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )

    val all:
        List<CharacterDefinition> =
        listOf(
            SirImbert,
            ywalethYoster,
            lavenaVol,
            koba,
            linlosDustman
        )

    private fun createStats(
        maxHp: Int,
        armorClass: Int,
        strength: Int,
        dexterity: Int,
        wisdom: Int,
        haste: Int,
        attackSpeed: Int,
        critRate: Int,
        critDamage: Int,
        movementSpeed: Int,
        luck: Int,
        harvest: Int,
        elementalDamage: Int,
        mightOfNature: Int,
        shepherd: Int,
        piercing: Int,
        enemyCount: Int,
        hpRegeneration: Int,
        lifeSteal: Int
    ): Map<StatType, Int> {
        return mapOf(
            StatType.MAX_HP to
                maxHp,

            StatType.ARMOR_CLASS to
                armorClass,

            StatType.STRENGTH to
                strength,

            StatType.DEXTERITY to
                dexterity,

            StatType.WISDOM to
                wisdom,

            StatType.HASTE to
                haste,

            StatType.ATTACK_SPEED to
                attackSpeed,

            StatType.CRIT_RATE to
                critRate,

            StatType.CRIT_DAMAGE to
                critDamage,

            StatType.MOVEMENT_SPEED to
                movementSpeed,

            StatType.LUCK to
                luck,

            StatType.HARVEST to
                harvest,

            StatType.ELEMENTAL_DAMAGE to
                elementalDamage,

            StatType.MIGHT_OF_NATURE to
                mightOfNature,

            StatType.SHEPHERD to
                shepherd,

            StatType.PIERCING to
                piercing,

            StatType.ENEMY_COUNT to
                enemyCount,

            StatType.HP_REGENERATION to
                hpRegeneration,

            StatType.LIFE_STEAL to
                lifeSteal
        )
    }
}
