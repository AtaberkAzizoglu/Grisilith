package com.azizoglu.grisilith.weapon.visual.attack

enum class MeleeAttackVisualKind {
    DOUBLE_SLASH,
    DOUBLE_SLASH_X,
    RAPID_STROKES_UPGRADED,
    FIRE_BEAM,
    LONGSWORD_CONE,
    LANCE_THRUST,
    LANCE_SPIN,
    SHIELD_CONE
}

data class MeleeAttackVisualEvent(
    val kind: MeleeAttackVisualKind,
    val centerX: Float,
    val centerY: Float,
    val rotationDegrees: Float,
    val worldWidth: Float,
    val worldHeight: Float,
    val durationSeconds: Float,
    var elapsedSeconds: Float = 0f
) {
    val progress: Float
        get() =
            if (
                durationSeconds <=
                0f
            ) {
                1f
            } else {
                (
                    elapsedSeconds /
                        durationSeconds
                    )
                    .coerceIn(
                        0f,
                        1f
                    )
            }

    val isFinished: Boolean
        get() =
            elapsedSeconds >=
                durationSeconds
}
