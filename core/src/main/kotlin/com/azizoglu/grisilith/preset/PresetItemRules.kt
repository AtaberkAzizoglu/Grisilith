package com.azizoglu.grisilith.preset

import com.azizoglu.grisilith.item.definition.ItemDefinition
import com.azizoglu.grisilith.item.definition.ItemRarity

/** Higher rarities are run rewards, never starting equipment. */
object PresetItemRules {
    fun canStartWith(item: ItemDefinition): Boolean =
        item.rarity == ItemRarity.UNCOMMON || item.rarity == ItemRarity.RARE
}
