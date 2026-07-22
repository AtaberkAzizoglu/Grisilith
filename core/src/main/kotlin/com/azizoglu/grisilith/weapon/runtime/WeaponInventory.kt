package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.content.ContentTag

class WeaponInventory {

    val weaponSlots =
        List(4) {
            WeaponSlot(it)
        }

    val secondarySlot =
        SecondaryWeaponSlot()

    fun addWeapon(
        weapon: WeaponInstance
    ): Boolean {

        weaponSlots.firstOrNull {

            it.isEmpty

        }?.let {

            it.weapon = weapon
            return true
        }

        return false
    }

    fun equipSecondaryWeapon(
        weapon: WeaponInstance
    ): Boolean {

        if (
            ContentTag.OFF_HAND !in
            weapon.definition.tags
        ) {
            return false
        }

        secondarySlot.weapon =
            weapon

        return true
    }

    fun allWeapons():
        List<WeaponInstance> {

        val list =
            mutableListOf<WeaponInstance>()

        weaponSlots.forEach {

            it.weapon?.let(list::add)
        }

        secondarySlot.weapon?.let(list::add)

        return list
    }
}
