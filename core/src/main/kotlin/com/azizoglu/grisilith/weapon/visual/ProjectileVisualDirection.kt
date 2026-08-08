package com.azizoglu.grisilith.weapon.visual

import kotlin.math.atan2

/*
 * Game movement direction -> source-sheet column.
 *
 * The source artwork is visually oriented opposite to the projectile
 * travel vector used by the game. We therefore map every resolved game
 * direction to the opposite-facing source frame.
 *
 * Source atlas column order:
 * 0=N, 1=NW, 2=W, 3=SW, 4=S, 5=SE, 6=E, 7=NE
 */
enum class ProjectileVisualDirection(
    val columnIndex: Int
) {
    NORTH(4),
    NORTH_WEST(5),
    WEST(6),
    SOUTH_WEST(7),
    SOUTH(0),
    SOUTH_EAST(1),
    EAST(2),
    NORTH_EAST(3);

    companion object {

        fun fromVelocity(
            x: Float,
            y: Float
        ): ProjectileVisualDirection {

            if (
                x == 0f &&
                y == 0f
            ) {
                return EAST
            }

            var degrees =
                Math.toDegrees(
                    atan2(
                        y.toDouble(),
                        x.toDouble()
                    )
                )
                    .toFloat()

            if (
                degrees <
                0f
            ) {
                degrees +=
                    360f
            }

            return when {
                degrees >= 337.5f ||
                    degrees < 22.5f ->
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
