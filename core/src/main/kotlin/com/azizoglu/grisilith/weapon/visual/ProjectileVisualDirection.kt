package com.azizoglu.grisilith.weapon.visual

import kotlin.math.atan2

/*
 * Column order in the supplied sheet:
 * N, NW, W, SW, S, SE, E, NE
 */
enum class ProjectileVisualDirection(
    val columnIndex: Int
) {
    NORTH(0),
    NORTH_WEST(1),
    WEST(2),
    SOUTH_WEST(3),
    SOUTH(4),
    SOUTH_EAST(5),
    EAST(6),
    NORTH_EAST(7);

    companion object {
        fun fromVelocity(
            x: Float,
            y: Float
        ): ProjectileVisualDirection {

            if (x == 0f && y == 0f) {
                return EAST
            }

            var degrees =
                Math.toDegrees(
                    atan2(
                        y.toDouble(),
                        x.toDouble()
                    )
                ).toFloat()

            if (degrees < 0f) {
                degrees += 360f
            }

            return when {
                degrees >= 337.5f || degrees < 22.5f ->
                    EAST

                degrees < 67.5f ->
                    NORTH_EAST

                degrees < 112.5f ->
                    NORTH

                degrees < 157.5f ->
                    NORTH_WEST

                degrees < 202.5f ->
                    WEST

                degrees < 247.5f ->
                    SOUTH_WEST

                degrees < 292.5f ->
                    SOUTH

                else ->
                    SOUTH_EAST
            }
        }
    }
}
