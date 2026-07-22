package com.azizoglu.grisilith.content

import com.azizoglu.grisilith.character.DefaultCharacters
import com.azizoglu.grisilith.weapon.definition.DefaultWeapons

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
        }
}
