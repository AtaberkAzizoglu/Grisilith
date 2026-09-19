package com.azizoglu.grisilith.weapon.visual.attack

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.player.Player
import kotlin.math.atan2

class SoulDrainVisualEvent(
    private val enemy: Enemy,
    private val player: Player
) {
    var elapsedSeconds = 0f
        private set

    var centerX = enemy.centerX
        private set

    var centerY = enemy.centerY
        private set

    var rotationDegrees = 0f
        private set

    var isFinished = false
        private set

    val frameIndex: Int
        get() = when {
            elapsedSeconds < FIRST_ENEMY_FRAME_END -> 0
            elapsedSeconds < SECOND_ENEMY_FRAME_END -> 1
            elapsedSeconds < EXIT_FRAME_END -> 2
            else -> {
                val flightFrame =
                    ((elapsedSeconds - EXIT_FRAME_END) / FLIGHT_FRAME_DURATION)
                        .toInt()

                3 + flightFrame % 3
            }
        }

    val alpha: Float
        get() = when {
            elapsedSeconds < FADE_IN_DURATION ->
                (elapsedSeconds / FADE_IN_DURATION).coerceIn(0f, 1f)

            isFinished -> 0f
            else -> 1f
        }

    fun update(delta: Float) {
        if (isFinished) {
            return
        }

        val safeDelta = delta.coerceAtLeast(0f)
        elapsedSeconds += safeDelta

        if (elapsedSeconds < SECOND_ENEMY_FRAME_END) {
            // The first two supplied frames remain anchored to the victim.
            centerX = enemy.centerX
            centerY = enemy.centerY
            rotationDegrees = 0f
            return
        }

        val dx = player.centerX - centerX
        val dy = player.centerY - centerY
        val distanceSquared = dx * dx + dy * dy

        if (distanceSquared <= ARRIVAL_DISTANCE * ARRIVAL_DISTANCE) {
            if (elapsedSeconds >= EXIT_FRAME_END) {
                isFinished = true
            }
            return
        }

        val distance = kotlin.math.sqrt(distanceSquared)
        val step = (FLIGHT_SPEED * safeDelta).coerceAtMost(distance)

        centerX += dx / distance * step
        centerY += dy / distance * step
        rotationDegrees = Math.toDegrees(atan2(dy, dx).toDouble()).toFloat()

        if (elapsedSeconds >= MAXIMUM_LIFETIME_SECONDS) {
            isFinished = true
        }
    }

    companion object {
        private const val FIRST_ENEMY_FRAME_END = 0.12f
        private const val SECOND_ENEMY_FRAME_END = 0.24f
        private const val EXIT_FRAME_END = 0.36f
        private const val FLIGHT_FRAME_DURATION = 0.09f
        private const val FADE_IN_DURATION = 0.06f
        private const val FLIGHT_SPEED = 520f
        private const val ARRIVAL_DISTANCE = 12f
        private const val MAXIMUM_LIFETIME_SECONDS = 2.25f
    }
}
