package com.azizoglu.grisilith.content

import com.azizoglu.grisilith.character.CharacterDefinition
import com.azizoglu.grisilith.character.CharacterPresentation
import com.azizoglu.grisilith.weapon.definition.WeaponDefinition
import com.azizoglu.grisilith.weapon.definition.WeaponPresentation

object ContentPresentation {
    fun hover(definition:ContentDefinition,level:Int?=null):String=when(definition) {
        is WeaponDefinition -> WeaponPresentation.hover(definition,level)
        is com.azizoglu.grisilith.armor.definition.ArmorDefinition -> com.azizoglu.grisilith.armor.definition.ArmorPresentation.hover(definition)
        is CharacterDefinition -> CharacterPresentation.summary(definition)+"\n\nTrait\n${definition.trait.displayName}: ${definition.trait.description}"
        else -> definition.description
    }
}
