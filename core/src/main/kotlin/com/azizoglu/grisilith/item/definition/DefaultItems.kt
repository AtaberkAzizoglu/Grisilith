package com.azizoglu.grisilith.item.definition

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.content.ContentUnlockState
import com.azizoglu.grisilith.content.StatModifier
import com.azizoglu.grisilith.statistics.StatType

object DefaultItems {

    private fun commonStatItem(
        id: String,
        displayName: String,
        description: String,
        statType: StatType
    ): ItemDefinition {
        return ItemDefinition(
            id =
                ContentId(id),

            displayName =
                displayName,

            description =
                description,

            tags =
                setOf(
                    ContentTag.PASSIVE_ITEM
                ),

            rarity =
                ItemRarity.COMMON,

            statModifiers =
                listOf(
                    StatModifier(
                        statType =
                            statType,

                        amount = 1
                    )
                ),

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )
    }

    val spellScroll =
        commonStatItem(
            id =
                "ITEM_SPELL_SCROLL",

            displayName =
                "Spell Scroll",

            description =
                "Increases Wisdom by 1.",

            statType =
                StatType.WISDOM
        )

    val windBoots =
        commonStatItem(
            id =
                "ITEM_WIND_BOOTS",

            displayName =
                "Wind Boots",

            description =
                "Increases Movement Speed by 1.",

            statType =
                StatType.MOVEMENT_SPEED
        )

    val vampireFangs =
        commonStatItem(
            id =
                "ITEM_VAMPIRE_FANGS",

            displayName =
                "Vampire Fangs",

            description =
                "Increases Life Steal by 1.",

            statType =
                StatType.LIFE_STEAL
        )

    val luckyDice =
        commonStatItem(
            id =
                "ITEM_LUCKY_DICE",

            displayName =
                "Lucky Dice",

            description =
                "Increases Luck by 1.",

            statType =
                StatType.LUCK
        )

    val gladiatorsLocket =
        commonStatItem(
            id =
                "ITEM_GLADIATORS_LOCKET",

            displayName =
                "Gladiator's Locket",

            description =
                "Increases Strength by 1.",

            statType =
                StatType.STRENGTH
        )

    val lordBledasBanner =
        commonStatItem(
            id =
                "ITEM_LORD_BLEDAS_BANNER",

            displayName =
                "Lord Bleda's Banner",

            description =
                "Increases Attack Speed by 1.",

            statType =
                StatType.ATTACK_SPEED
        )

    val hourglass =
        commonStatItem(
            id =
                "ITEM_HOURGLASS",

            displayName =
                "Hourglass",

            description =
                "Increases Haste by 1.",

            statType =
                StatType.HASTE
        )

    val hillGiantsBelt =
        commonStatItem(
            id =
                "ITEM_HILL_GIANTS_BELT",

            displayName =
                "Hill Giant's Belt",

            description =
                "Increases Max HP by 1.",

            statType =
                StatType.MAX_HP
        )

    val chainMail =
        commonStatItem(
            id =
                "ITEM_CHAIN_MAIL",

            displayName =
                "Chain Mail",

            description =
                "Increases Armor Class by 1.",

            statType =
                StatType.ARMOR_CLASS
        )

    val sandals =
        commonStatItem(
            id =
                "ITEM_SANDALS",

            displayName =
                "Sandals",

            description =
                "Increases Dexterity by 1.",

            statType =
                StatType.DEXTERITY
        )

    val kepenek =
        commonStatItem(
            id =
                "ITEM_KEPENEK",

            displayName =
                "Kepenek",

            description =
                "Increases Shepherd by 1.",

            statType =
                StatType.SHEPHERD
        )

    val whetstone =
        commonStatItem(
            id =
                "ITEM_WHETSTONE",

            displayName =
                "Whetstone",

            description =
                "Increases Piercing by 1.",

            statType =
                StatType.PIERCING
        )

    val warHorn =
        commonStatItem(
            id =
                "ITEM_WAR_HORN",

            displayName =
                "War Horn",

            description =
                "Increases Enemy Count by 1.",

            statType =
                StatType.ENEMY_COUNT
        )

    val goddessSelinusAmulet =
        commonStatItem(
            id =
                "ITEM_GODDESS_SELINUS_AMULET",

            displayName =
                "Goddess Selinus' Amulet",

            description =
                "Increases HP Regeneration by 1.",

            statType =
                StatType.HP_REGENERATION
        )

    val copperRod =
        commonStatItem(
            id =
                "ITEM_COPPER_ROD",

            displayName =
                "Copper Rod",

            description =
                "Increases Might of Nature by 1.",

            statType =
                StatType.MIGHT_OF_NATURE
        )

    val amuletOfAzuth =
        commonStatItem(
            id =
                "ITEM_AMULET_OF_AZUTH",

            displayName =
                "Amulet of Azuth",

            description =
                "Increases Elemental Damage by 1.",

            statType =
                StatType.ELEMENTAL_DAMAGE
        )

    val griffinFeather =
        commonStatItem(
            id =
                "ITEM_GRIFFIN_FEATHER",

            displayName =
                "Griffin Feather",

            description =
                "Increases Harvest by 1.",

            statType =
                StatType.HARVEST
        )

    val warmastersOrder =
        commonStatItem(
            id =
                "ITEM_WARMASTERS_ORDER",

            displayName =
                "Warmaster's Order",

            description =
                "Increases Crit Rate by 1.",

            statType =
                StatType.CRIT_RATE
        )

    val heavyArrow =
        commonStatItem(
            id =
                "ITEM_HEAVY_ARROW",

            displayName =
                "Heavy Arrow",

            description =
                "Increases Crit Damage by 1.",

            statType =
                StatType.CRIT_DAMAGE
        )

    val all:
        List<ItemDefinition> =
        listOf(
            spellScroll,
            windBoots,
            vampireFangs,
            luckyDice,
            gladiatorsLocket,
            lordBledasBanner,
            hourglass,
            hillGiantsBelt,
            chainMail,
            sandals,
            kepenek,
            whetstone,
            warHorn,
            goddessSelinusAmulet,
            copperRod,
            amuletOfAzuth,
            griffinFeather,
            warmastersOrder,
            heavyArrow
        )
}
