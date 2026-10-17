package com.azizoglu.grisilith.weapon

import com.badlogic.gdx.math.Vector2

/** Bends moving projectiles without changing their speed or teleporting them. */
object ProjectileTrajectoryInfluence {
    fun bendToward(velocity: Vector2, x: Float, y: Float, centerX: Float, centerY: Float,
                   radius: Float, maximumTurnDegreesPerSecond: Float, delta: Float) {
        if (delta <= 0f || radius <= 0f || velocity.isZero(0.001f)) return
        val desired = Vector2(centerX - x, centerY - y)
        val distance = desired.len()
        if (distance > radius || distance <= 1f) return
        val difference = (desired.angleDeg() - velocity.angleDeg() + 540f) % 360f - 180f
        val influence = 0.25f + 0.75f * (1f - distance / radius)
        val turn = maximumTurnDegreesPerSecond * influence * delta
        velocity.rotateDeg(difference.coerceIn(-turn, turn))
    }
}
