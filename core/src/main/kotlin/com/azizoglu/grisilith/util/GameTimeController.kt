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

        val gameDelta = realDelta * gameSpeed
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
        val ALLOWED_GAME_SPEEDS = setOf(
            0.5f,
            1f,
            2f
        )
    }
}
