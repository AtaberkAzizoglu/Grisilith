package com.azizoglu.grisilith.combat.condition

import com.azizoglu.grisilith.content.ConditionType

data class ConditionTick(
    val type: ConditionType,
    val source: ConditionSource
)
