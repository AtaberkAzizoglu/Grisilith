package com.azizoglu.grisilith.collection.filter

import com.azizoglu.grisilith.content.ContentCategory
import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.content.EnemyCollectionDefinition
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.item.definition.ItemDefinition
import com.azizoglu.grisilith.item.definition.ItemRarity
import com.azizoglu.grisilith.tamga.definition.TamgaActivationType
import com.azizoglu.grisilith.tamga.definition.TamgaDefinition
import com.azizoglu.grisilith.tamga.definition.TamgaSlotType
import com.badlogic.gdx.graphics.Color

object CollectionFilterRegistry {

    fun optionsFor(category: ContentCategory): List<CollectionFilterOption> =
        when (category) {
            ContentCategory.CHARACTER,
            ContentCategory.MAP -> emptyList()

            ContentCategory.WEAPON -> listOf(
                tag("weapon_melee", "weapon_type", "Melee Weapons", ContentTag.MELEE_WEAPON),
                tag("weapon_ranged", "weapon_type", "Ranged Weapons", ContentTag.RANGED_WEAPON),
                tag("weapon_spell", "weapon_type", "Spell Focus", ContentTag.SPELL_FOCUS)
            )

            ContentCategory.ARMOR -> listOf(
                tag("armor_light", "armor_type", "Light Armor", ContentTag.LIGHT_ARMOR),
                tag("armor_medium", "armor_type", "Medium Armor", ContentTag.MEDIUM_ARMOR),
                tag("armor_heavy", "armor_type", "Heavy Armor", ContentTag.HEAVY_ARMOR),
                tag("armor_clothes", "armor_type", "Clothes", ContentTag.CLOTHES)
            )

            ContentCategory.ITEM -> listOf(
                rarity(ItemRarity.COMMON, Color.LIGHT_GRAY),
                rarity(ItemRarity.UNCOMMON, Color(0.35f, 0.85f, 0.40f, 1f)),
                rarity(ItemRarity.RARE, Color(0.30f, 0.55f, 1f, 1f)),
                rarity(ItemRarity.LEGENDARY, Color(0.75f, 0.35f, 0.95f, 1f)),
                rarity(ItemRarity.ARTIFACT, Color.GOLD)
            )

            ContentCategory.TAMGA -> listOf(
                CollectionFilterOption(
                    "tamga_passive", "tamga_activation", "Passive"
                ) { it is TamgaDefinition && it.activationType == TamgaActivationType.PASSIVE },

                CollectionFilterOption(
                    "tamga_active", "tamga_activation", "Active"
                ) { it is TamgaDefinition && it.activationType == TamgaActivationType.ACTIVE },

                CollectionFilterOption(
                    "tamga_weapon", "tamga_slot", "Weapon Tamga"
                ) { it is TamgaDefinition && it.slotType == TamgaSlotType.WEAPON },

                CollectionFilterOption(
                    "tamga_armor", "tamga_slot", "Armor Tamga"
                ) { it is TamgaDefinition && it.slotType == TamgaSlotType.ARMOR },

                CollectionFilterOption(
                    "tamga_general", "tamga_slot", "General Tamga"
                ) { it is TamgaDefinition && it.slotType == TamgaSlotType.GENERAL }
            )

            ContentCategory.ENEMY -> listOf(
                enemy("enemy_minion", "Minion Enemies", EnemyType.NORMAL),
                enemy("enemy_unique", "Unique Enemies", EnemyType.UNIQUE),
                enemy("enemy_boss", "Bosses", EnemyType.MINI_BOSS),
                enemy("enemy_main_boss", "Main Bosses", EnemyType.FINAL_BOSS)
            )
        }

    private fun tag(
        id: String,
        groupId: String,
        name: String,
        tag: ContentTag
    ) = CollectionFilterOption(id, groupId, name) { tag in it.tags }

    private fun rarity(
        rarity: ItemRarity,
        color: Color
    ) = CollectionFilterOption(
        id = "item_${rarity.name.lowercase()}",
        groupId = "item_rarity",
        displayName = rarity.displayName,
        textColor = color
    ) {
        it is ItemDefinition && it.rarity == rarity
    }

    private fun enemy(
        id: String,
        name: String,
        type: EnemyType
    ) = CollectionFilterOption(id, "enemy_type", name) {
        it is EnemyCollectionDefinition && it.enemyType == type
    }
}
