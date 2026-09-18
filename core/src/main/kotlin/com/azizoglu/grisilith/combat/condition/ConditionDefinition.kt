package com.azizoglu.grisilith.combat.condition

import com.azizoglu.grisilith.content.ConditionType
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.statistics.StatType

data class ConditionDefinition(
    val type: ConditionType,
    val damageType: DamageType = DamageType.NONE,
    val scalingStat: StatType? = null,
    val tickIntervalSeconds: Float? = null,
    val maximumHealthRatioPerStatPoint: Float = 0f
) {
    init {
        require(maximumHealthRatioPerStatPoint >= 0f) {
            "Condition stat scaling cannot be negative."
        }

        tickIntervalSeconds?.let { interval ->
            require(interval > 0f) {
                "Condition tick interval must be positive."
            }
        }
    }

    val dealsPeriodicDamage: Boolean
        get() =
            damageType != DamageType.NONE &&
                scalingStat != null &&
                tickIntervalSeconds != null &&
                maximumHealthRatioPerStatPoint > 0f
}
