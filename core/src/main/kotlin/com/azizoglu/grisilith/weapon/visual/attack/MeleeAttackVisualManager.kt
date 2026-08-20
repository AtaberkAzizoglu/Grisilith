package com.azizoglu.grisilith.weapon.visual.attack

object MeleeAttackVisualManager {

    private val events =
        mutableListOf<MeleeAttackVisualEvent>()

    val activeEvents:
        List<MeleeAttackVisualEvent>
        get() =
            events

    fun spawn(
        kind: MeleeAttackVisualKind,
        centerX: Float,
        centerY: Float,
        rotationDegrees: Float,
        worldWidth: Float,
        worldHeight: Float,
        durationSeconds: Float
    ) {
        /*
         * This visual system is deliberately event-based.
         * Every executor emits exactly one visual event for one real attack.
         * It does not inspect/poll WeaponCarrier state or attack counters.
         */
        events.add(
            MeleeAttackVisualEvent(
                kind =
                    kind,
                centerX =
                    centerX,
                centerY =
                    centerY,
                rotationDegrees =
                    rotationDegrees,
                worldWidth =
                    worldWidth.coerceAtLeast(
                        1f
                    ),
                worldHeight =
                    worldHeight.coerceAtLeast(
                        1f
                    ),
                durationSeconds =
                    durationSeconds.coerceAtLeast(
                        0.05f
                    )
            )
        )
    }

    fun update(
        delta: Float
    ) {
        val safeDelta =
            delta.coerceAtLeast(
                0f
            )

        events.forEach {
            it.elapsedSeconds +=
                safeDelta
        }

        events.removeAll {
            it.isFinished
        }
    }

    fun clear() {
        events.clear()
    }
}
