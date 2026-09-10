package com.azizoglu.grisilith.util

class GameTimeController {

    var gameSpeed: Float = 1f
        private set

    var isPaused: Boolean = false
        private set

    val elapsedGameTimeSeconds: Float
        get() = elapsedGameTime

    private var elapsedGameTime = 0f

    fun togglePause() {
        isPaused = !isPaused
    }

    fun setGameSpeed(speed: Float) {
        require(speed in ALLOWED_GAME_SPEEDS) {
            "Unsupported game speed: $speed"
        }

        gameSpeed = speed
    }

    fun calculateGameDelta(realDelta: Float): Float {
        if (isPaused) {
            return 0f
        }

        /*
         * Texture creation (notably the first Phantom Steed activation) can
         * block one render frame. That wall-clock stall must never be fed to
         * gameplay clocks, otherwise stage timers jump/overflow.
         */
        val safeRealDelta =
            realDelta.takeIf { it.isFinite() }
                ?.coerceIn(0f, MAX_GAMEPLAY_FRAME_DELTA_SECONDS)
                ?: 0f
        val gameDelta = safeRealDelta * gameSpeed
        elapsedGameTime += gameDelta

        return gameDelta
    }

    fun restoreElapsedGameTime(
        elapsedSeconds: Float
    ) {
        elapsedGameTime =
            elapsedSeconds.coerceAtLeast(0f)
    }

    companion object {
        private const val MAX_GAMEPLAY_FRAME_DELTA_SECONDS = 0.10f
        val ALLOWED_GAME_SPEEDS = setOf(
            0.5f,
            1f,
            2f
        )
    }
}
