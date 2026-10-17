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
                "Sir Imbert",

            description =
                "Sir Imbert, the Yellow One, the Yellow Commander, the Hypocrite... " +
                    "this warrior, known by many titles, is one of the great fighters " +
                    "of the \"Yellow Knights,\" an organization that rose up against " +
                    "the Empire. He has spent his entire life in fear of magic, much " +
                    "like every other Yellow Knight. He dedicated his life to the " +
                    "organization at a young age and swore an oath to eradicate magic " +
                    "from the face of the earth, kinda. Yet there is a reason he is " +
                    "called the Hypocrite...",

            minionDescription =
                "Small armored weapon bearers.",

            trait =
                DefaultCharacterTraits.theHypocrisy,

            baseStats =
                createStats(
                    maxHp = 11,
                    armorClass = 14,
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
                "Ywaleth Yoster was born into an ordinary family, just like anyone " +
                    "else. That changed when a raid destroyed her village and everyone " +
                    "perished. The gods refused to accept Ywaleth's death. They " +
                    "commanded her to exact vengeance for herself and for them, then " +
                    "returned her to the realm of the living. Since that day, she has " +
                    "prepared herself for a reckoning far closer than she anticipates.",

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
                "Lavena Vol, one of the five founders of the legendary Ha'la'tha " +
                    "School of Magic, has practiced magic since the day she was born. " +
                    "She mastered the arcane arts at a young age, instructed great " +
                    "wizards and repeatedly protected her city, country and companions, " +
                    "sometimes even from themselves. She was the first to foresee the " +
                    "approaching danger; after being expelled for researching forbidden " +
                    "spells, she became that very approaching danger.",

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
                "Koba's life story is simple to tell: war and victory. He was a " +
                    "rising star of Splingald, a city-state that mastered warfare and " +
                    "magic. Rather than merely following the path of the ancestor whose " +
                    "name he bore, Koba embraced that ancestor's ideals and vowed to " +
                    "protect his kin from monsters of every kind. Two-legged or " +
                    "four-legged, speaking or spellcasting, they are all targets. His " +
                    "ultimate goal is to destroy the Dead Wizard...",
            minionDescription =
                "Half-naked barbarian weapon bearers.",
            trait =
                DefaultCharacterTraits.bloodweight,
            canEquipArmor = false,
            unarmoredBaseHealth = 14,
            unarmoredBaseArmor = 9,
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
                ContentUnlockState.LOCKED
        )

    val linlosDustman =
        CharacterDefinition(
            id =
                ContentId("CHR_LINLOS_DUSTMAN"),
            displayName =
                "Linlos Dustman",
            description =
                "Little is known about Linlos; there are only rumors. He is a hero " +
                    "who protects villagers, a ranger who guards the forests and a " +
                    "monster who hunts the worst of the worst. The only certainty is " +
                    "that he emerged from nowhere and poisoned everything and everyone. " +
                    "All for a good cause, right?",
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
                ContentUnlockState.LOCKED
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
        lifeSteal: Int,
        magicResistance: Int = 10
    ): Map<StatType, Int> {
        return mapOf(
            StatType.MAGIC_RESISTANCE to magicResistance,
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
