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
                resolveStableDirection(
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

    private fun resolveStableDirection(
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

        val horizontalDirection =
            if (
                movementX <
                0f
            ) {
                CharacterVisualDirection.LEFT
            } else {
                CharacterVisualDirection.RIGHT
            }

        val verticalDirection =
            if (
                movementY <
                0f
            ) {
                CharacterVisualDirection.DOWN
            } else {
                CharacterVisualDirection.UP
            }

        return when (
            direction
        ) {
            CharacterVisualDirection.LEFT,
            CharacterVisualDirection.RIGHT -> {

                val shouldSwitchToVertical =
                    absoluteY >
                        absoluteX *
                        AXIS_SWITCH_RATIO

                if (
                    shouldSwitchToVertical
                ) {
                    verticalDirection
                } else {
                    horizontalDirection
                }
            }

            CharacterVisualDirection.UP,
            CharacterVisualDirection.DOWN -> {

                val shouldSwitchToHorizontal =
                    absoluteX >
                        absoluteY *
                        AXIS_SWITCH_RATIO

                if (
                    shouldSwitchToHorizontal
                ) {
                    horizontalDirection
                } else {
                    verticalDirection
                }
            }
        }
    }

    companion object {

        private const val MOVEMENT_EPSILON =
            0.01f

        private const val MOVEMENT_EPSILON_SQUARED =
            MOVEMENT_EPSILON *
                MOVEMENT_EPSILON

        /*
         * Direction hysteresis:
         *
         * Diagonal movement often has X/Y values that are almost equal.
         * Without a margin, tiny frame-to-frame differences can make the
         * sprite alternate between horizontal and vertical directions.
         *
         * The new axis must be at least 20% stronger before switching.
         */
        private const val AXIS_SWITCH_RATIO =
            1.20f
    }
}
