package com.azizoglu.grisilith.combat.condition

import com.azizoglu.grisilith.content.ConditionType
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.statistics.StatType

/** Universal damage-type -> condition rule shared by weapons, enemies and summons. */
object DamageConditionRules {
    fun conditionFor(damageType: DamageType): ConditionType? = when (damageType) {
        DamageType.WATER -> ConditionType.WET
        DamageType.FIRE -> ConditionType.BURNED
        DamageType.NECROTIC -> ConditionType.NECROSIS
        DamageType.ACID -> ConditionType.CORRODING
        DamageType.POISON -> ConditionType.POISONED
        else -> null
    }

    fun scalingStatFor(conditionType: ConditionType): StatType? =
        DefaultConditionDefinitions.definitionFor(conditionType).scalingStat
}
