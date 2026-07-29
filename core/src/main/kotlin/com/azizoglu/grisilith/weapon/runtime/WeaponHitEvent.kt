package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.enemy.Enemy

data class WeaponHitEvent(
    val weapon:
    WeaponInstance,

    val weaponSlotIndex:
    Int,

    val enemy:
    Enemy,

    /*
     * Enemy health değerinden gerçekten eksilen
     * base Weapon hasarı.
     */
    val appliedDamage:
    Int,

    val damageType:
    DamageType,

    val isProjectile:
    Boolean
)
