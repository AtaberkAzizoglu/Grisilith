package com.azizoglu.grisilith.content

import com.azizoglu.grisilith.character.DefaultCharacters
import com.azizoglu.grisilith.item.definition.DefaultItems
import com.azizoglu.grisilith.weapon.definition.DefaultWeapons
import com.azizoglu.grisilith.armor.definition.DefaultArmors

object DefaultContentRegistry {

    val instance:
        ContentRegistry =
        ContentRegistry().apply {
            registerAll(
                DefaultCharacters.all
            )

            registerAll(
                DefaultWeapons.all
            )

            registerAll(
                DefaultArmors.all
            )

            registerAll(
                DefaultItems.all
            )
        }
}
