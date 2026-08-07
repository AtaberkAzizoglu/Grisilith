package com.azizoglu.grisilith.enemy.visual

import kotlin.math.abs

class EnemyVisualState {

    var direction:
        EnemyVisualDirection =
        EnemyVisualDirection.DOWN
        private set

    var animation:
        EnemyVisualAnimation =
        EnemyVisualAnimation.IDLE
        private set

    private var animationTime =
        0f

    fun update(
        delta: Float,
        movementX: Float,
        movementY: Float
    ) {
        val safeDelta =
            delta.coerceAtLeast(0f)

        val isMoving =
            movementX * movementX +
                movementY * movementY >
                MOVEMENT_EPSILON_SQUARED

        val nextAnimation =
            if (isMoving) {
                EnemyVisualAnimation.WALK
            } else {
                EnemyVisualAnimation.IDLE
            }

        if (
            nextAnimation !=
            animation
        ) {
            animation =
                nextAnimation

            animationTime =
                0f
        } else {
            animationTime +=
                safeDelta
        }

        if (isMoving) {
            direction =
                resolveDirection(
                    movementX = movementX,
                    movementY = movementY
                )
        }
    }

    fun resetAnimationTime() {
        animationTime =
            0f
    }

    fun currentAnimationTime(): Float {
        return animationTime
    }

    private fun resolveDirection(
        movementX: Float,
        movementY: Float
    ): EnemyVisualDirection {

        val absoluteX =
            abs(
                movementX
            )

        val absoluteY =
            abs(
                movementY
            )

        return if (
            absoluteX >
            absoluteY
        ) {
            if (
                movementX <
                0f
            ) {
                EnemyVisualDirection.LEFT
            } else {
                EnemyVisualDirection.RIGHT
            }
        } else {
            if (
                movementY <
                0f
            ) {
                EnemyVisualDirection.DOWN
            } else {
                EnemyVisualDirection.UP
            }
        }
    }

    companion object {

        private const val MOVEMENT_EPSILON =
            0.01f

        private const val MOVEMENT_EPSILON_SQUARED =
            MOVEMENT_EPSILON *
                MOVEMENT_EPSILON
    }
}
