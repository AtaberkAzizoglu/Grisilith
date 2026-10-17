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
                "Light armor for mobile physical fighters. Improves movement and attack speed, but offers limited health and physical protection and reduces Wisdom.",
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
                "A balanced choice for frontline fighters, with more health and physical protection than light armor. Reduces movement, attack speed and Wisdom.",
            tags = setOf(ContentTag.MEDIUM_ARMOR),
            baseHealth = 13,
            baseArmor = 12,
            baseMagicResistance = 10,
            statModifiers =
                listOf(
                    StatModifier(
                        statType = StatType.MOVEMENT_SPEED,
                        amount = -2
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
            displayName = "Full Plate Armor",
            description =
                "Heavy armor for durable melee fighters. Provides high health and physical protection, but weak magical protection and penalties to movement, attack speed and Wisdom.",
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
                "A robe for spellcasters who favor magic and mobility over physical toughness. Increases Wisdom, movement speed and Luck, but provides little health or armor.",
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
        displayName = "Padded Gambeson Armor",
        description = "Light padding for fast physical attacks. Grants a large attack-speed bonus without slowing movement, but provides modest health and protection and reduces Wisdom.",
        tags = setOf(ContentTag.LIGHT_ARMOR),
        baseHealth = 8,
        baseArmor = 7,
        baseMagicResistance = 11,
        statModifiers = listOf(
            StatModifier(StatType.ATTACK_SPEED, 4),
            StatModifier(StatType.WISDOM, -2)
        )
    )

    val barnacleArmor = ArmorDefinition(
        id = ContentId("ARMOR_BARNACLE"),
        displayName = "Barnacle Armor",
        description = "A good choice for slow, durable tanks. Offers strong health and better magical protection than physical protection, at the cost of movement, attack speed and Wisdom.",
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
        description = "The heaviest choice for a physical tank, with the highest base health and armor. Extremely weak against magic, with severe penalties to movement, attack speed and Wisdom.",
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
        description = "A good choice for spellcasters. Provides less health but more magical protection than the Robe of Mage, with a stronger Wisdom bonus and the same mobility and Luck bonuses.",
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
