package com.azizoglu.grisilith.economy

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import kotlin.math.sin

enum class CurrencyPickupType {
    GOLD,
    GRISILITH
}

class CurrencyPickup(
    val type: CurrencyPickupType,
    startX: Float,
    startY: Float,
    landingX: Float,
    landingY: Float
) {

    private val spawnPosition =
        Vector2(
            startX,
            startY
        )

    private val landingPosition =
        Vector2(
            landingX,
            landingY
        )

    private val position =
        Vector2(
            startX,
            startY
        )

    private val attractionDirection =
        Vector2()

    private var ageSeconds =
        0f

    private var scatterProgress =
        0f

    private val bobPhase =
        MathUtils.random(
            0f,
            MathUtils.PI2
        )

    var isCollected: Boolean =
        false
        private set

    val centerX: Float
        get() =
            position.x

    val centerY: Float
        get() =
            position.y +
                visualBobOffset

    val baseWorldY: Float
        get() =
            position.y

    val visualBobOffset: Float
        get() =
            if (
                scatterProgress <
                1f
            ) {
                scatterArcHeight()
            } else {
                sin(
                    ageSeconds *
                        BOB_SPEED +
                        bobPhase
                ) *
                    BOB_AMPLITUDE
            }

    val pulseScale: Float
        get() =
            if (
                type ==
                CurrencyPickupType.GRISILITH
            ) {
                1f +
                    sin(
                        ageSeconds *
                            GRISILITH_PULSE_SPEED +
                            bobPhase
                    ) *
                        GRISILITH_PULSE_AMOUNT
            } else {
                1f
            }

    fun update(
        delta: Float,
        playerX: Float,
        playerY: Float,
        pickupRadius: Float
    ): Boolean {
        if (isCollected) {
            return false
        }

        val safeDelta =
            delta.coerceAtLeast(
                0f
            )

        ageSeconds +=
            safeDelta

        if (
            scatterProgress <
            1f
        ) {
            scatterProgress =
                (
                    scatterProgress +
                        safeDelta /
                            SCATTER_DURATION_SECONDS
                    )
                    .coerceIn(
                        0f,
                        1f
                    )

            val eased =
                Interpolation
                    .sineOut
                    .apply(
                        scatterProgress
                    )

            position.set(
                MathUtils.lerp(
                    spawnPosition.x,
                    landingPosition.x,
                    eased
                ),
                MathUtils.lerp(
                    spawnPosition.y,
                    landingPosition.y,
                    eased
                )
            )

            return false
        }

        attractionDirection.set(
            playerX -
                position.x,
            playerY -
                position.y
        )

        val distance =
            attractionDirection.len()

        if (
            distance <=
            COLLECTION_DISTANCE
        ) {
            isCollected = true
            return true
        }

        if (
            distance <=
                pickupRadius
                    .coerceAtLeast(
                        COLLECTION_DISTANCE
                    ) &&
            !attractionDirection.isZero
        ) {
            attractionDirection.nor()

            position.mulAdd(
                attractionDirection,
                (
                    MAGNET_SPEED *
                        safeDelta
                    )
                    .coerceAtMost(
                        distance
                    )
            )
        }

        return false
    }

    fun collectImmediately() {
        isCollected = true
    }

    private fun scatterArcHeight():
        Float {

        return sin(
            scatterProgress *
                MathUtils.PI
        ) *
            SCATTER_ARC_HEIGHT
    }

    companion object {
        private const val SCATTER_DURATION_SECONDS =
            0.22f

        private const val SCATTER_ARC_HEIGHT =
            7f

        private const val BOB_AMPLITUDE =
            2.2f

        private const val BOB_SPEED =
            4.8f

        private const val GRISILITH_PULSE_SPEED =
            5.6f

        private const val GRISILITH_PULSE_AMOUNT =
            0.08f

        private const val MAGNET_SPEED =
            520f

        private const val COLLECTION_DISTANCE =
            13f
    }
}
