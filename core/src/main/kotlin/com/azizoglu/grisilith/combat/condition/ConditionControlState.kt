package com.azizoglu.grisilith.combat.condition

data class ConditionControlState(
    val canMove: Boolean,
    val canAttack: Boolean,
    val isFrightened: Boolean,
    val sourceX: Float,
    val sourceY: Float,
    val movementSpeedPenalty: Float
) {

    fun modifiedMovementSpeed(
        baseMovementSpeed: Float
    ): Float {

        return (
            baseMovementSpeed -
                movementSpeedPenalty
            )
            .coerceAtLeast(
                0f
            )
    }
}
