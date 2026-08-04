package com.azizoglu.grisilith.combat.interaction

import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.enemy.Enemy

data class CombatInteractionEvent(
    val trigger: CombatInteractionTrigger,
    val source: CombatInteractionSource = CombatInteractionSource.UNKNOWN,
    val damageType: DamageType? = null,
    val enemy: Enemy? = null,
    val worldX: Float,
    val worldY: Float,
    val appliedDamage: Int = 0,
    val sourceWeaponRuntimeId: String? = null,
    val sourceWeaponDefinitionId: String? = null,
    val sourceName: String? = null
)
