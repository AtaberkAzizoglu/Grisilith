package com.azizoglu.grisilith.combat.condition

import com.azizoglu.grisilith.content.DamageType

data class ConditionSource(
    val damageType: DamageType = DamageType.NONE,
    val tickDamage: Int = 0,
    val sourceWeaponRuntimeId: String? = null,
    val sourceWeaponDefinitionId: String? = null,
    val sourceName: String? = null,
    val sourceCenterX: Float? = null,
    val sourceCenterY: Float? = null
) {
    init {
        require(tickDamage >= 0) {
            "Condition tick damage cannot be negative."
        }
    }

    companion object {
        val NONE = ConditionSource()
    }
}
