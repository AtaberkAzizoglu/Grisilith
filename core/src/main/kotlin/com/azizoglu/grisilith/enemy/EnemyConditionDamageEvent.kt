package com.azizoglu.grisilith.enemy

import com.azizoglu.grisilith.content.ConditionType

data class EnemyConditionDamageEvent(
    val enemy: Enemy,
    val conditionType: ConditionType,
    val appliedDamage: Int,
    val damageContext: EnemyDamageContext,
    val killedEnemy: Boolean
)
