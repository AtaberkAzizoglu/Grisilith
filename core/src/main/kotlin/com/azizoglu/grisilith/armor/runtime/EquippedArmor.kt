package com.azizoglu.grisilith.armor.runtime

import com.azizoglu.grisilith.armor.definition.ArmorDefinition
import com.azizoglu.grisilith.content.ContentId

/*
 * Oyuncunun tek Armor slotunu yönetir.
 *
 * Armor yalnızca Preset yüklenirken atanacaktır.
 */
class EquippedArmor {

    var definition:
        ArmorDefinition? =
        null
        private set

    val isEquipped: Boolean
        get() =
            definition != null

    fun equip(
        armor:
        ArmorDefinition
    ) {
        definition =
            armor
    }

    fun unequip():
        ArmorDefinition? {

        val previousArmor =
            definition

        definition =
            null

        return previousArmor
    }

    fun isEquipped(
        armorId:
        ContentId
    ): Boolean {
        return definition
            ?.id ==
            armorId
    }

    fun clear() {
        definition =
            null
    }
}
