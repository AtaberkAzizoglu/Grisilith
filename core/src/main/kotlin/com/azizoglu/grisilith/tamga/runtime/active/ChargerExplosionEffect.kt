package com.azizoglu.grisilith.tamga.runtime.active

import com.azizoglu.grisilith.content.DamageType

class ChargerExplosionEffect(
    val centerX: Float,
    val centerY: Float,
    val radius: Float,
    val damage: Int,
    val knockbackStrength: Float
) {

    companion object {

        const val DURATION_SECONDS =
            1.32f
    }

    val damageType:
        DamageType =
        DamageType.FIRE

    private var elapsedSeconds =
        0f

    val isFinished:
        Boolean
        get() =
            elapsedSeconds >=
                DURATION_SECONDS

    val progress:
        Float
        get() =
            (
                elapsedSeconds /
                    DURATION_SECONDS
                )
                .coerceIn(
                    0f,
                    1f
                )

    fun update(
        delta:
        Float
    ) {
        elapsedSeconds +=
            delta.coerceAtLeast(
                0f
            )
    }
}
