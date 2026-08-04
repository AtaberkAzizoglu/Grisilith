package com.azizoglu.grisilith.combat.condition

import com.azizoglu.grisilith.content.ConditionType

data class ConditionApplication(
    val type: ConditionType,
    val durationSeconds: Float? =
        DefaultConditionDefinitions.DEFAULT_DURATION_SECONDS,
    val source: ConditionSource = ConditionSource.NONE
) {
    init {
        durationSeconds?.let { duration ->
            require(duration >= 0f) {
                "Condition duration cannot be negative."
            }
        }
    }
}
