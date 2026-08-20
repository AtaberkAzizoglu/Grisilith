package com.azizoglu.grisilith.chest

import kotlin.math.sin

data class ChestWorldVisual(
    val worldX: Float,
    val worldY: Float,
    val phase: Float
) {
    var ageSeconds:
        Float =
        0f

    val visualY: Float
        get() =
            worldY +
                sin(
                    ageSeconds *
                        2.8f +
                        phase
                ) *
                    1.4f
}

class ChestWorldVisualManager {

    private val visuals =
        mutableListOf<ChestWorldVisual>()

    val allVisuals:
        List<ChestWorldVisual>
        get() =
            visuals

    fun spawn(
        worldX: Float,
        worldY: Float
    ) {
        visuals.add(
            ChestWorldVisual(
                worldX =
                    worldX,
                worldY =
                    worldY,
                phase =
                    (visuals.size *
                        0.87f)
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

        visuals.forEach {
            it.ageSeconds +=
                safeDelta
        }
    }

    fun removeNearestTo(
        worldX: Float,
        worldY: Float
    ) {
        val nearest =
            visuals.minByOrNull {
                    visual ->

                val dx =
                    visual.worldX -
                        worldX

                val dy =
                    visual.worldY -
                        worldY

                dx * dx +
                    dy * dy
            }
                ?: return

        visuals.remove(
            nearest
        )
    }

    fun clear() {
        visuals.clear()
    }
}
