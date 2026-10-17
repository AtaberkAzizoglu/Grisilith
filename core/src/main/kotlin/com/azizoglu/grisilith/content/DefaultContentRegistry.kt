package com.azizoglu.grisilith.content

import com.azizoglu.grisilith.armor.definition.DefaultArmors
import com.azizoglu.grisilith.character.DefaultCharacters
import com.azizoglu.grisilith.item.definition.DefaultItems
import com.azizoglu.grisilith.tamga.definition.DefaultTamgas
import com.azizoglu.grisilith.weapon.definition.DefaultWeapons
import com.azizoglu.grisilith.enemy.definition.DefaultEnemyKinds

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

            registerAll(
                DefaultTamgas.all
            )

            registerAll(
                DefaultEnemyKinds.all
            )

            registerAll(
                DefaultCollectionMaps.all
            )
            registerAll(com.azizoglu.grisilith.summon.SummonCollectionDefinition.all)
        }
}
