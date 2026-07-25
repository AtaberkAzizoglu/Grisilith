package com.azizoglu.grisilith.weapon.runtime

data class WeaponSlot(
    val index: Int,
    var weapon: WeaponInstance? = null
) {

    val isEmpty: Boolean
        get() =
            weapon == null

    fun clear() {
        weapon = null
    }
}
