package com.azizoglu.grisilith.character.visual

import kotlin.math.abs

class CharacterVisualState {

    var direction:
        CharacterVisualDirection =
        CharacterVisualDirection.DOWN
        private set

    var animation:
        CharacterVisualAnimation =
        CharacterVisualAnimation.IDLE
        private set

    private var animationTime =
        0f

    fun update(
        delta: Float,
        movementX: Float,
        movementY: Float
    ) {
        val safeDelta =
            delta.coerceAtLeast(
                0f
            )

        val isMoving =
            movementX * movementX +
                movementY * movementY >
                MOVEMENT_EPSILON_SQUARED

        val nextAnimation =
            if (isMoving) {
                CharacterVisualAnimation.WALK
            } else {
                CharacterVisualAnimation.IDLE
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
                    movementX =
                        movementX,

                    movementY =
                        movementY
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
    ): CharacterVisualDirection {

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
                CharacterVisualDirection.LEFT
            } else {
                CharacterVisualDirection.RIGHT
            }
        } else {
            if (
                movementY <
                0f
            ) {
                CharacterVisualDirection.DOWN
            } else {
                CharacterVisualDirection.UP
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
