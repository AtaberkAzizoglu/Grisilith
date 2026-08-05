package com.azizoglu.grisilith.weapon.glyph

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align

class GlyphFeedbackRenderer(
    private val camera: OrthographicCamera,
    private val shapeRenderer: ShapeRenderer,
    private val batch: SpriteBatch,
    private val font: BitmapFont
) {

    fun render(
        feedback: GlyphFeedback?
    ) {

        if (
            feedback ==
            null
        ) {
            return
        }

        val alpha =
            (
                feedback
                    .remainingDisplaySeconds /
                    FADE_WINDOW_SECONDS
                )
                .coerceIn(
                    MINIMUM_ALPHA,
                    MAXIMUM_ALPHA
                )

        val left =
            camera.position.x -
                camera.viewportWidth /
                2f +
                18f

        val bottom =
            camera.position.y -
                camera.viewportHeight /
                2f +
                172f

        shapeRenderer.projectionMatrix =
            camera.combined

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        shapeRenderer.color =
            Color(
                0.04f,
                0.035f,
                0.06f,
                0.56f *
                    alpha
            )

        shapeRenderer.rect(
            left,
            bottom,
            BOX_WIDTH,
            BOX_HEIGHT
        )

        shapeRenderer.color =
            if (
                feedback.resultText ==
                "Accepted"
            ) {
                Color(
                    0.32f,
                    0.82f,
                    0.42f,
                    0.56f *
                        alpha
                )
            } else {
                Color(
                    0.88f,
                    0.34f,
                    0.30f,
                    0.56f *
                        alpha
                )
            }

        shapeRenderer.rect(
            left,
            bottom,
            4f,
            BOX_HEIGHT
        )

        shapeRenderer.end()

        batch.projectionMatrix =
            camera.combined

        batch.begin()

        font.data.setScale(
            0.72f
        )

        font.color =
            Color(
                1f,
                1f,
                1f,
                alpha
            )

        font.draw(
            batch,
            "Glyph: ${feedback.detectedName}",
            left +
                12f,
            bottom +
                BOX_HEIGHT -
                13f
        )

        font.data.setScale(
            0.63f
        )

        font.color =
            Color(
                0.86f,
                0.86f,
                0.90f,
                alpha
            )

        font.draw(
            batch,
            "Accuracy: ${feedback.confidencePercent}%   " +
                "${feedback.resultText}   " +
                "Cooldown: ${feedback.cooldownSeconds}s",

            left +
                12f,
            bottom +
                18f,
            BOX_WIDTH -
                24f,
            Align.left,
            false
        )

        font.data.setScale(
            1f
        )

        batch.end()
    }

    companion object {

        private const val BOX_WIDTH =
            310f

        private const val BOX_HEIGHT =
            58f

        private const val FADE_WINDOW_SECONDS =
            2.5f

        private const val MINIMUM_ALPHA =
            0.18f

        private const val MAXIMUM_ALPHA =
            0.78f
    }
}
