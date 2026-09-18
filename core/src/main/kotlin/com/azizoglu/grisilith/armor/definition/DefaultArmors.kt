package com.azizoglu.grisilith.armor.definition

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.content.ContentUnlockState
import com.azizoglu.grisilith.content.StatModifier
import com.azizoglu.grisilith.statistics.StatType

object DefaultArmors {

    val leatherArmor =
        ArmorDefinition(
            id = ContentId("ARMOR_LEATHER"),
            displayName = "Leather Armor",
            description =
                "A light and flexible armor. " +
                    "It improves mobility and attack speed, " +
                    "but offers little protection and makes " +
                    "the use of magic more difficult.",
            tags = setOf(ContentTag.LIGHT_ARMOR),
            baseHealth = 9,
            baseArmor = 6,
            baseMagicResistance = 13,
            statModifiers =
                listOf(
                    StatModifier(
                        statType = StatType.MOVEMENT_SPEED,
                        amount = 1
                    ),
                    StatModifier(
                        statType = StatType.ATTACK_SPEED,
                        amount = 2
                    ),
                    StatModifier(
                        statType = StatType.WISDOM,
                        amount = -2
                    )
                ),
            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )

    val scaleMailArmor =
        ArmorDefinition(
            id = ContentId("ARMOR_SCALE_MAIL"),
            displayName = "Scale Mail Armor",
            description =
                "A balanced medium armor that provides " +
                    "reasonable protection without completely " +
                    "sacrificing mobility. It is uncomfortable " +
                    "for spellcasters.",
            tags = setOf(ContentTag.MEDIUM_ARMOR),
            baseHealth = 13,
            baseArmor = 12,
            baseMagicResistance = 10,
            statModifiers =
                listOf(
                    StatModifier(
                        statType = StatType.MOVEMENT_SPEED,
                        amount = -1
                    ),
                    StatModifier(
                        statType = StatType.ATTACK_SPEED,
                        amount = -1
                    ),
                    StatModifier(
                        statType = StatType.WISDOM,
                        amount = -4
                    )
                ),
            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )

    val plateArmor =
        ArmorDefinition(
            id = ContentId("ARMOR_PLATE"),
            displayName = "Plate Armor",
            description =
                "A heavy suit of plate armor that offers " +
                    "excellent protection at the cost of " +
                    "movement, attack speed and magical freedom.",
            tags = setOf(ContentTag.HEAVY_ARMOR),
            baseHealth = 18,
            baseArmor = 22,
            baseMagicResistance = 5,
            statModifiers =
                listOf(
                    StatModifier(
                        statType = StatType.MOVEMENT_SPEED,
                        amount = -3
                    ),
                    StatModifier(
                        statType = StatType.ATTACK_SPEED,
                        amount = -2
                    ),
                    StatModifier(
                        statType = StatType.WISDOM,
                        amount = -7
                    )
                ),
            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )

    val robeOfMage =
        ArmorDefinition(
            id = ContentId("ARMOR_ROBE_OF_MAGE"),
            displayName = "Robe of Mage",
            description =
                "A fragile magical robe that offers almost no " +
                    "physical protection, but greatly improves " +
                    "the freedom and magical ability of its wearer.",
            tags = setOf(ContentTag.CLOTHES),
            baseHealth = 6,
            baseArmor = 2,
            baseMagicResistance = 17,
            statModifiers =
                listOf(
                    StatModifier(
                        statType = StatType.MOVEMENT_SPEED,
                        amount = 2
                    ),
                    StatModifier(
                        statType = StatType.LUCK,
                        amount = 1
                    ),
                    StatModifier(
                        statType = StatType.WISDOM,
                        amount = 3
                    )
                ),
            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )

    val paddedGambeson = ArmorDefinition(
        id = ContentId("ARMOR_PADDED_GAMBESON"),
        displayName = "Padded Gambeson",
        description = "Light padded armor with quick attacks and modest physical and magical protection.",
        tags = setOf(ContentTag.LIGHT_ARMOR),
        baseHealth = 8, baseArmor = 7, baseMagicResistance = 11,
        statModifiers = listOf(
            StatModifier(StatType.ATTACK_SPEED, 4),
            StatModifier(StatType.WISDOM, -2)
        )
    )

    val barnacleArmor = ArmorDefinition(
        id = ContentId("ARMOR_BARNACLE"),
        displayName = "Barnacle Armor",
        description = "Medium armor that offers high vitality and balanced protection at the cost of speed.",
        tags = setOf(ContentTag.MEDIUM_ARMOR),
        baseHealth = 16, baseArmor = 9, baseMagicResistance = 12,
        statModifiers = listOf(
            StatModifier(StatType.MOVEMENT_SPEED, -2),
            StatModifier(StatType.ATTACK_SPEED, -2),
            StatModifier(StatType.WISDOM, -4)
        )
    )

    val fortressArmor = ArmorDefinition(
        id = ContentId("ARMOR_FORTRESS"),
        displayName = "Fortress Armor",
        description = "Extremely heavy armor with exceptional vitality and physical protection but little magical protection.",
        tags = setOf(ContentTag.HEAVY_ARMOR),
        baseHealth = 20, baseArmor = 25, baseMagicResistance = 2,
        statModifiers = listOf(
            StatModifier(StatType.MOVEMENT_SPEED, -5),
            StatModifier(StatType.ATTACK_SPEED, -4),
            StatModifier(StatType.WISDOM, -10)
        )
    )

    val arcaneSilkRobe = ArmorDefinition(
        id = ContentId("ARMOR_ARCANE_SILK_ROBE"),
        displayName = "Arcane Silk Robe",
        description = "Light magical clothes with excellent magical protection and little physical defense.",
        tags = setOf(ContentTag.CLOTHES),
        baseHealth = 5, baseArmor = 2, baseMagicResistance = 20,
        statModifiers = listOf(
            StatModifier(StatType.MOVEMENT_SPEED, 2),
            StatModifier(StatType.LUCK, 1),
            StatModifier(StatType.WISDOM, 4)
        )
    )

    val all:
        List<ArmorDefinition> =
        listOf(
            leatherArmor,
            scaleMailArmor,
            plateArmor,
            robeOfMage,
            paddedGambeson,
            barnacleArmor,
            fortressArmor,
            arcaneSilkRobe
        )
}
