package com.azizoglu.grisilith.tamga.runtime

enum class FootstepDirection {
    NORTH,
    NORTH_EAST,
    EAST,
    SOUTH_EAST,
    SOUTH,
    SOUTH_WEST,
    WEST,
    NORTH_WEST
}

class ChargerFootstepVisual(
    val x: Float,
    val y: Float,
    val direction: FootstepDirection
) {
    companion object {
        const val LIFETIME_SECONDS = 2.8f
    }

    private var elapsedSeconds = 0f

    val progress: Float
        get() =
            (elapsedSeconds / LIFETIME_SECONDS)
                .coerceIn(0f, 1f)

    val isFinished: Boolean
        get() = elapsedSeconds >= LIFETIME_SECONDS

    fun update(delta: Float) {
        elapsedSeconds += delta.coerceAtLeast(0f)
    }
}

class PanicPulseVisual(
    val x: Float,
    val y: Float
) {
    companion object {
        const val DURATION_SECONDS = 1.2f
    }

    private var elapsedSeconds = 0f

    val progress: Float
        get() =
            (elapsedSeconds / DURATION_SECONDS)
                .coerceIn(0f, 1f)

    val isFinished: Boolean
        get() = elapsedSeconds >= DURATION_SECONDS

    fun update(delta: Float) {
        elapsedSeconds += delta.coerceAtLeast(0f)
    }
}
