package com.azizoglu.grisilith.tamga.runtime.active

import com.azizoglu.grisilith.save.RunSaveActiveTamgaState
abstract class ActiveTamgaRuntime(
    private val durationSeconds: Float,
    private val cooldownSeconds: Float
) {

    private var activeElapsedSeconds =
        0f

    private var cooldownRemainingSeconds =
        0f

    var state:
        ActiveTamgaState =
        ActiveTamgaState.READY
        private set

    val isReady:
        Boolean
        get() =
            state ==
                ActiveTamgaState.READY

    val isActive:
        Boolean
        get() =
            state ==
                ActiveTamgaState.ACTIVE

    val isOnCooldown:
        Boolean
        get() =
            state ==
                ActiveTamgaState.COOLDOWN

    val activeProgress:
        Float
        get() {

            if (
                durationSeconds <=
                0f
            ) {
                return 0f
            }

            return (
                activeElapsedSeconds /
                    durationSeconds
                )
                .coerceIn(
                    0f,
                    1f
                )
        }

    val remainingActiveSeconds:
        Float
        get() =
            (
                durationSeconds -
                    activeElapsedSeconds
                )
                .coerceAtLeast(
                    0f
                )

    val remainingCooldownSeconds:
        Float
        get() =
            cooldownRemainingSeconds
                .coerceAtLeast(
                    0f
                )

    val cooldownProgress:
        Float
        get() {

            if (
                cooldownSeconds <=
                0f
            ) {
                return 1f
            }

            return (
                1f -
                    cooldownRemainingSeconds /
                    cooldownSeconds
                )
                .coerceIn(
                    0f,
                    1f
                )
        }

    fun tryActivate():
        Boolean {

        if (!isReady) {
            return false
        }

        activeElapsedSeconds =
            0f

        state =
            ActiveTamgaState.ACTIVE

        onActivated()

        return true
    }

    fun update(
        delta:
        Float
    ) {
        val safeDelta =
            delta.coerceAtLeast(
                0f
            )

        when (state) {

            ActiveTamgaState.READY -> {
                /*
                 * Hazır durumdayken timer işlemi yok.
                 */
            }

            ActiveTamgaState.ACTIVE -> {
                activeElapsedSeconds +=
                    safeDelta

                onActiveUpdate(
                    delta =
                        safeDelta,

                    progress =
                        activeProgress
                )

                /*
                 * onActiveUpdate içinde skill
                 * interrupt edilmiş olabilir.
                 */
                if (!isActive) {
                    return
                }

                if (
                    activeElapsedSeconds >=
                    durationSeconds
                ) {
                    finishActivePeriod(
                        ActiveTamgaEndReason
                            .COMPLETED
                    )
                }
            }

            ActiveTamgaState.COOLDOWN -> {
                cooldownRemainingSeconds -=
                    safeDelta

                if (
                    cooldownRemainingSeconds <=
                    0f
                ) {
                    cooldownRemainingSeconds =
                        0f

                    state =
                        ActiveTamgaState.READY

                    onCooldownFinished()
                }
            }
        }
    }

    protected fun interrupt() {
        if (!isActive) {
            return
        }

        finishActivePeriod(
            ActiveTamgaEndReason
                .INTERRUPTED
        )
    }

    private fun finishActivePeriod(
        reason:
        ActiveTamgaEndReason
    ) {
        if (!isActive) {
            return
        }

        onActiveEnded(
            reason
        )

        activeElapsedSeconds =
            0f

        cooldownRemainingSeconds =
            cooldownSeconds
                .coerceAtLeast(
                    0f
                )

        state =
            if (
                cooldownRemainingSeconds >
                0f
            ) {
                ActiveTamgaState.COOLDOWN
            } else {
                ActiveTamgaState.READY
            }
    }

    open fun reset() {
        val wasActive =
            isActive

        activeElapsedSeconds =
            0f

        cooldownRemainingSeconds =
            0f

        state =
            ActiveTamgaState.READY

        if (wasActive) {
            onActiveEnded(
                ActiveTamgaEndReason
                    .RESET
            )
        }

        onReset()
    }

    fun reduceCooldown(seconds: Float) {
        if (seconds <= 0f || state != ActiveTamgaState.COOLDOWN) return
        cooldownRemainingSeconds =
            (cooldownRemainingSeconds - seconds).coerceAtLeast(0f)
        if (cooldownRemainingSeconds == 0f) {
            state = ActiveTamgaState.READY
            onCooldownFinished()
        }
    }

    protected open fun onActivated() {
        /*
         * Alt runtime isterse uygular.
         */
    }

    protected open fun onActiveUpdate(
        delta:
        Float,

        progress:
        Float
    ) {
        /*
         * Alt runtime isterse uygular.
         */
    }

    protected open fun onActiveEnded(
        reason:
        ActiveTamgaEndReason
    ) {
        /*
         * Alt runtime isterse uygular.
         */
    }

    protected open fun onCooldownFinished() {
        /*
         * Alt runtime isterse uygular.
         */
    }

    protected open fun onReset() {
        /*
         * Alt runtime isterse uygular.
         */
    }

    fun exportSaveState():
        RunSaveActiveTamgaState {

        return RunSaveActiveTamgaState(
            stateName =
                state.name,

            activeElapsedSeconds =
                activeElapsedSeconds
                    .coerceAtLeast(
                        0f
                    ),

            cooldownRemainingSeconds =
                cooldownRemainingSeconds
                    .coerceAtLeast(
                        0f
                    )
        )
    }
}
