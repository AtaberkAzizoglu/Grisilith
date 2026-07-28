package com.azizoglu.grisilith.armor.definition

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.content.ContentUnlockState
import com.azizoglu.grisilith.content.StatModifier
import com.azizoglu.grisilith.statistics.StatType

object DefaultArmors {

    /*
     * Hızlı fakat düşük korumalı.
     *
     * +  = +1
     * -- = -2
     */
    val leatherArmor =
        ArmorDefinition(
            id =
                ContentId(
                    "ARMOR_LEATHER"
                ),

            displayName =
                "Leather Armor",

            description =
                "A light and flexible armor. " +
                    "It improves mobility and attack speed, " +
                    "but offers little protection and makes " +
                    "the use of magic more difficult.",

            tags =
                setOf(
                    ContentTag.LIGHT_ARMOR
                ),

            baseMaximumHealthScore =
                1,

            baseArmorClassScore =
                1,

            statModifiers =
                listOf(
                    StatModifier(
                        statType =
                            StatType.MOVEMENT_SPEED,

                        amount =
                            1
                    ),

                    StatModifier(
                        statType =
                            StatType.ATTACK_SPEED,

                        amount =
                            1
                    ),

                    StatModifier(
                        statType =
                            StatType.WISDOM,

                        amount =
                            -2
                    )
                ),

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )

    /*
     * Light ve Heavy Armor arasında dengeli.
     */
    val scaleMailArmor =
        ArmorDefinition(
            id =
                ContentId(
                    "ARMOR_SCALE_MAIL"
                ),

            displayName =
                "Scale Mail Armor",

            description =
                "A balanced medium armor that provides " +
                    "reasonable protection without completely " +
                    "sacrificing mobility. It is uncomfortable " +
                    "for spellcasters.",

            tags =
                setOf(
                    ContentTag.MEDIUM_ARMOR
                ),

            baseMaximumHealthScore =
                2,

            baseArmorClassScore =
                2,

            statModifiers =
                listOf(
                    StatModifier(
                        statType =
                            StatType.MOVEMENT_SPEED,

                        amount =
                            -1
                    ),

                    StatModifier(
                        statType =
                            StatType.ATTACK_SPEED,

                        amount =
                            -1
                    ),

                    StatModifier(
                        statType =
                            StatType.WISDOM,

                        amount =
                            -3
                    )
                ),

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )

    /*
     * Çok güvenli fakat ağır.
     *
     * Armor Class için listedeki + değerini temel
     * Armor Class skoruna dahil ettik.
     */
    val plateArmor =
        ArmorDefinition(
            id =
                ContentId(
                    "ARMOR_PLATE"
                ),

            displayName =
                "Plate Armor",

            description =
                "A heavy suit of plate armor that offers " +
                    "excellent protection at the cost of " +
                    "movement, attack speed and magical freedom.",

            tags =
                setOf(
                    ContentTag.HEAVY_ARMOR
                ),

            baseMaximumHealthScore =
                4,

            baseArmorClassScore =
                5,

            statModifiers =
                listOf(
                    StatModifier(
                        statType =
                            StatType.MOVEMENT_SPEED,

                        amount =
                            -2
                    ),

                    StatModifier(
                        statType =
                            StatType.ATTACK_SPEED,

                        amount =
                            -2
                    ),

                    StatModifier(
                        statType =
                            StatType.WISDOM,

                        amount =
                            -4
                    )
                ),

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )

    /*
     * Koruması çok düşük, Spell Caster odaklı.
     *
     * Wisdom isteğin doğrultusunda ++, yani +2.
     */
    val robeOfMage =
        ArmorDefinition(
            id =
                ContentId(
                    "ARMOR_ROBE_OF_MAGE"
                ),

            displayName =
                "Robe of Mage",

            description =
                "A fragile magical robe that offers almost no " +
                    "physical protection, but greatly improves " +
                    "the freedom and magical ability of its wearer.",

            tags =
                setOf(
                    ContentTag.CLOTHES
                ),

            baseMaximumHealthScore =
                -1,

            baseArmorClassScore =
                -1,

            statModifiers =
                listOf(
                    StatModifier(
                        statType =
                            StatType.LUCK,

                        amount =
                            1
                    ),

                    StatModifier(
                        statType =
                            StatType.MOVEMENT_SPEED,

                        amount =
                            1
                    ),

                    StatModifier(
                        statType =
                            StatType.WISDOM,

                        amount =
                            2
                    )
                ),

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )

    val all:
        List<ArmorDefinition> =
        listOf(
            leatherArmor,
            scaleMailArmor,
            plateArmor,
            robeOfMage
        )
}
