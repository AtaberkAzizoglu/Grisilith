package com.azizoglu.grisilith.enemy

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import kotlin.math.sin

class EnemySpawnWarning(
    val bounds: Rectangle,
    val enemy: Enemy,
    private var remainingSeconds: Float =
        WARNING_DURATION_SECONDS
) {

    val isReadyToSpawn: Boolean
        get() =
            remainingSeconds <= 0f

    fun update(
        delta: Float
    ) {
        remainingSeconds =
            (
                remainingSeconds -
                    delta.coerceAtLeast(
                        0f
                    )
                )
                .coerceAtLeast(
                    0f
                )
    }

    fun render(
        shapeRenderer: ShapeRenderer
    ) {
        val elapsed =
            WARNING_DURATION_SECONDS -
                remainingSeconds

        val alpha =
            (
                0.55f +
                    sin(
                        elapsed *
                            18f
                    ) *
                    0.35f
                )
                .coerceIn(
                    0.20f,
                    0.95f
                )

        shapeRenderer.color =
            Color(
                1f,
                0.08f,
                0.08f,
                alpha
            )

        val padding =
            6f

        val left =
            bounds.x -
                padding

        val right =
            bounds.x +
                bounds.width +
                padding

        val bottom =
            bounds.y -
                padding

        val top =
            bounds.y +
                bounds.height +
                padding

        shapeRenderer.rectLine(
            left,
            bottom,
            right,
            top,
            5f
        )

        shapeRenderer.rectLine(
            left,
            top,
            right,
            bottom,
            5f
        )
    }

    companion object {
        const val WARNING_DURATION_SECONDS =
            1f
    }
}
