package com.azizoglu.grisilith.combat.debug

sealed class CombatDebugVisual(
    open var remainingSeconds: Float
) {

    data class Line(
        val startX: Float,
        val startY: Float,
        val endX: Float,
        val endY: Float,
        val upgraded: Boolean,
        override var remainingSeconds: Float
    ) : CombatDebugVisual(
        remainingSeconds
    )

    data class Cone(
        val originX: Float,
        val originY: Float,
        val directionX: Float,
        val directionY: Float,
        val range: Float,
        val angleDegrees: Float,
        val upgraded: Boolean,
        override var remainingSeconds: Float
    ) : CombatDebugVisual(
        remainingSeconds
    )

    data class Circle(
        val centerX: Float,
        val centerY: Float,
        val radius: Float,
        val upgraded: Boolean,
        override var remainingSeconds: Float
    ) : CombatDebugVisual(
        remainingSeconds
    )
}
