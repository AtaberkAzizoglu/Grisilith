package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.content.ContentTag
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
    Boolean,

    val isCriticalHit:
    Boolean
) {

    val isLifeStealEligible:
        Boolean
        get() =
            damageType in
                PHYSICAL_DAMAGE_TYPES &&
                weapon.definition.tags.any {
                        tag ->

                    tag ==
                        ContentTag.MELEE_WEAPON ||
                        tag ==
                        ContentTag.RANGED_WEAPON
                }

    companion object {

        private val PHYSICAL_DAMAGE_TYPES =
            setOf(
                DamageType.SLASHING,
                DamageType.PIERCING,
                DamageType.BLUDGEONING
            )
    }
}
