package com.azizoglu.grisilith.character

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.content.ContentUnlockState
import com.azizoglu.grisilith.statistics.StatType

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

            preferredMarketStats =
                setOf(
                    StatType.STRENGTH,
                    StatType.ATTACK_SPEED,
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
                "No minions. Equipped weapons float around the character.",

            baseStats =
                createStats(
                    maxHp = 7,
                    armorClass = 5,
                    strength = 2,
                    dexterity = 5,
                    wisdom = 14,
                    haste = 11,
                    attackSpeed = 5,
                    critRate = 3,
                    critDamage = 1,
                    movementSpeed = 9,
                    luck = 8,
                    harvest = 6,
                    elementalDamage = 10,
                    mightOfNature = 10,
                    shepherd = 10,
                    piercing = 0,
                    enemyCount = 2,
                    hpRegeneration = 2,
                    lifeSteal = 0
                ),

            preferredMarketStats =
                setOf(
                    StatType.WISDOM,
                    StatType.HASTE,
                    StatType.ELEMENTAL_DAMAGE,
                    StatType.MIGHT_OF_NATURE,
                    StatType.SHEPHERD
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
            lavenaVol
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
