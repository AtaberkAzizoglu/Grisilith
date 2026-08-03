package com.azizoglu.grisilith.weapon.skill.area

import com.badlogic.gdx.math.Vector2

class AreaSkillRuntime {

    private var activeField:
        AreaSkillField? =
        null

    private var remainingSeconds =
        0f

    private var tickAccumulator =
        0f

    val field:
        AreaSkillField?
        get() =
            activeField

    val isActive:
        Boolean
        get() =
            activeField !=
                null

    val remainingDurationSeconds:
        Float
        get() =
            remainingSeconds
                .coerceAtLeast(
                    0f
                )

    fun begin(
        centerX: Float,
        centerY: Float,
        radius: Float,
        durationSeconds: Float
    ): Boolean {
        if (isActive) {
            return false
        }

        activeField =
            AreaSkillField(
                center =
                    Vector2(
                        centerX,
                        centerY
                    ),

                radius =
                    radius,

                durationSeconds =
                    durationSeconds
            )

        remainingSeconds =
            durationSeconds

        tickAccumulator =
            0f

        return true
    }

    fun update(
        delta: Float,
        tickIntervalSeconds: Float,
        onFrame: (
            field: AreaSkillField,
            frameDelta: Float
        ) -> Unit,
        onTick: (
            field: AreaSkillField
        ) -> Unit,
        onCompleted: () -> Unit
    ) {
        val currentField =
            activeField
                ?: return

        if (
            delta <=
            0f
        ) {
            return
        }

        val safeDelta =
            delta.coerceAtMost(
                remainingSeconds
            )

        onFrame(
            currentField,
            safeDelta
        )

        tickAccumulator +=
            safeDelta

        val safeTickInterval =
            tickIntervalSeconds
                .coerceAtLeast(
                    MINIMUM_TICK_INTERVAL
                )

        while (
            tickAccumulator >=
            safeTickInterval
        ) {
            tickAccumulator -=
                safeTickInterval

            onTick(
                currentField
            )
        }

        remainingSeconds =
            (
                remainingSeconds -
                    safeDelta
                )
                .coerceAtLeast(
                    0f
                )

        if (
            remainingSeconds <=
            0f
        ) {
            clear()
            onCompleted()
        }
    }

    fun clear() {
        activeField =
            null

        remainingSeconds =
            0f

        tickAccumulator =
            0f
    }

    companion object {
        private const val MINIMUM_TICK_INTERVAL =
            0.01f
    }
}
