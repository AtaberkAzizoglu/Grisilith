package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.content.ContentRarity
import com.azizoglu.grisilith.weapon.definition.WeaponDefinition

data class WeaponInstance(

    val definition: WeaponDefinition,

    var rarity: ContentRarity =
        ContentRarity.COMMON,

    var level: Int = 1
) {

    init {
        require(level in 1..4)
    }

    val displayName: String
        get() =
            definition.displayName

    fun canLevelUp(): Boolean {
        return level < 4
    }
}
