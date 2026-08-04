package com.azizoglu.grisilith.combat.condition

import com.azizoglu.grisilith.content.ConditionType

object ConditionControlRules {

    const val WET_MOVEMENT_SPEED_PENALTY =
        1f

    fun resolve(
        conditions: ConditionContainer,
        fallbackSourceX: Float,
        fallbackSourceY: Float
    ): ConditionControlState {

        val isStunned =
            conditions.contains(
                ConditionType.STUNNED
            )

        val isRooted =
            conditions.contains(
                ConditionType.ROOTED
            )

        val isParalyzed =
            conditions.contains(
                ConditionType.PARALYZED
            )

        val frightenedCondition =
            conditions.activeCondition(
                ConditionType.FRIGHTENED
            )

        val isFrightened =
            frightenedCondition != null &&
                !isStunned &&
                !isRooted &&
                !isParalyzed

        val source =
            frightenedCondition
                ?.source

        return ConditionControlState(
            canMove =
                !isStunned &&
                    !isRooted &&
                    !isParalyzed,

            canAttack =
                !isParalyzed,

            isFrightened =
                isFrightened,

            sourceX =
                source
                    ?.sourceCenterX
                    ?: fallbackSourceX,

            sourceY =
                source
                    ?.sourceCenterY
                    ?: fallbackSourceY,

            movementSpeedPenalty =
                if (
                    conditions.contains(
                        ConditionType.WET
                    )
                ) {
                    WET_MOVEMENT_SPEED_PENALTY
                } else {
                    0f
                }
        )
    }
}
