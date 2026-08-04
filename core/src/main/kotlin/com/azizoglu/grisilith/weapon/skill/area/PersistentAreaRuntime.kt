package com.azizoglu.grisilith.weapon.skill.area

class PersistentAreaRuntime {

    private var activeArea:
        PersistentArea? =
        null

    private var remainingSeconds =
        0f

    private var tickAccumulator =
        0f

    val area:
        PersistentArea?
        get() =
            activeArea

    val isActive:
        Boolean
        get() =
            activeArea !=
                null

    val remainingDurationSeconds:
        Float
        get() =
            remainingSeconds
                .coerceAtLeast(
                    0f
                )

    fun begin(
        area: PersistentArea
    ): Boolean {
        if (isActive) {
            return false
        }

        activeArea =
            area

        remainingSeconds =
            area.durationSeconds

        tickAccumulator =
            0f

        return true
    }

    fun update(
        delta: Float,

        followCenterX: Float,
        followCenterY: Float,

        onFrame: (
            area: PersistentArea,
            frameDelta: Float
        ) -> Unit,

        onTick: (
            area: PersistentArea
        ) -> Unit,

        onCompleted: () -> Unit
    ) {
        val currentArea =
            activeArea
                ?: return

        if (
            delta <=
            0f
        ) {
            return
        }

        if (
            currentArea.anchor ==
            PersistentAreaAnchor
                .FOLLOW_PLAYER
        ) {
            currentArea.center.set(
                followCenterX,
                followCenterY
            )
        }

        val safeDelta =
            delta.coerceAtMost(
                remainingSeconds
            )

        onFrame(
            currentArea,
            safeDelta
        )

        tickAccumulator +=
            safeDelta

        while (
            tickAccumulator >=
            currentArea
                .tickIntervalSeconds
        ) {
            tickAccumulator -=
                currentArea
                    .tickIntervalSeconds

            onTick(
                currentArea
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
        activeArea =
            null

        remainingSeconds =
            0f

        tickAccumulator =
            0f
    }
}
