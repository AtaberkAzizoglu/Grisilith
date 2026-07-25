package com.azizoglu.grisilith.combat.debug

object CombatDebugVisualManager {

    private const val DEFAULT_DURATION_SECONDS =
        0.14f

    /*
     * false yapılırsa bütün debug saldırı
     * çizimleri tek yerden kapanır.
     */
    var isEnabled: Boolean = true

    private val visuals =
        mutableListOf<CombatDebugVisual>()

    fun addLine(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        upgraded: Boolean,
        durationSeconds: Float =
            DEFAULT_DURATION_SECONDS
    ) {
        if (!isEnabled) {
            return
        }

        visuals.add(
            CombatDebugVisual.Line(
                startX = startX,
                startY = startY,
                endX = endX,
                endY = endY,
                upgraded = upgraded,
                remainingSeconds =
                    durationSeconds.coerceAtLeast(0f)
            )
        )
    }

    fun addCone(
        originX: Float,
        originY: Float,
        directionX: Float,
        directionY: Float,
        range: Float,
        angleDegrees: Float,
        upgraded: Boolean,
        durationSeconds: Float =
            DEFAULT_DURATION_SECONDS
    ) {
        if (!isEnabled) {
            return
        }

        visuals.add(
            CombatDebugVisual.Cone(
                originX = originX,
                originY = originY,
                directionX = directionX,
                directionY = directionY,
                range = range.coerceAtLeast(0f),
                angleDegrees =
                    angleDegrees.coerceAtLeast(0f),
                upgraded = upgraded,
                remainingSeconds =
                    durationSeconds.coerceAtLeast(0f)
            )
        )
    }

    fun addCircle(
        centerX: Float,
        centerY: Float,
        radius: Float,
        upgraded: Boolean,
        durationSeconds: Float =
            DEFAULT_DURATION_SECONDS
    ) {
        if (!isEnabled) {
            return
        }

        visuals.add(
            CombatDebugVisual.Circle(
                centerX = centerX,
                centerY = centerY,
                radius = radius.coerceAtLeast(0f),
                upgraded = upgraded,
                remainingSeconds =
                    durationSeconds.coerceAtLeast(0f)
            )
        )
    }

    fun update(
        delta: Float
    ) {
        if (delta <= 0f) {
            return
        }

        visuals.forEach { visual ->
            visual.remainingSeconds -= delta
        }

        visuals.removeAll { visual ->
            visual.remainingSeconds <= 0f
        }
    }

    fun activeVisuals():
        List<CombatDebugVisual> {

        return visuals.toList()
    }

    fun clear() {
        visuals.clear()
    }
}
