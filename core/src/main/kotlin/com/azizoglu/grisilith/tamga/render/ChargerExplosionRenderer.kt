package com.azizoglu.grisilith.tamga.render

import com.azizoglu.grisilith.tamga.runtime.active.ChargerExplosionEffect
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Interpolation

class ChargerExplosionRenderer {

    fun render(
        shapeRenderer:
        ShapeRenderer,

        effects:
        List<ChargerExplosionEffect>
    ) {
        if (effects.isEmpty()) {
            return
        }

        effects.forEach {
                effect ->

            renderEffect(
                shapeRenderer =
                    shapeRenderer,

                effect =
                    effect
            )
        }
    }

    private fun renderEffect(
        shapeRenderer:
        ShapeRenderer,

        effect:
        ChargerExplosionEffect
    ) {
        val progress =
            effect.progress

        val expansionProgress =
            Interpolation
                .exp10Out
                .apply(
                    progress
                )

        val alpha =
            (
                1f -
                    progress
                )
                .coerceIn(
                    0f,
                    1f
                )

        val currentRadius =
            effect.radius *
                expansionProgress

        /*
         * İç dolgu.
         */
        shapeRenderer.color =
            Color(
                1f,
                0.34f,
                0.05f,
                0.28f *
                    alpha
            )

        shapeRenderer.circle(
            effect.centerX,
            effect.centerY,
            currentRadius *
                0.72f,
            64
        )

        /*
         * Parlak merkez.
         */
        shapeRenderer.color =
            Color(
                1f,
                0.82f,
                0.20f,
                0.60f *
                    alpha
            )

        shapeRenderer.circle(
            effect.centerX,
            effect.centerY,
            currentRadius *
                0.26f,
            48
        )
    }

    fun renderRings(
        shapeRenderer:
        ShapeRenderer,

        effects:
        List<ChargerExplosionEffect>
    ) {
        if (effects.isEmpty()) {
            return
        }

        effects.forEach {
                effect ->

            val progress =
                effect.progress

            val expansionProgress =
                Interpolation
                    .exp10Out
                    .apply(
                        progress
                    )

            val alpha =
                (
                    1f -
                        progress
                    )
                    .coerceIn(
                        0f,
                        1f
                    )

            val currentRadius =
                effect.radius *
                    expansionProgress

            shapeRenderer.color =
                Color(
                    1f,
                    0.16f,
                    0.02f,
                    alpha
                )

            shapeRenderer.circle(
                effect.centerX,
                effect.centerY,
                currentRadius,
                72
            )

            shapeRenderer.color =
                Color(
                    1f,
                    0.62f,
                    0.08f,
                    0.85f *
                        alpha
                )

            shapeRenderer.circle(
                effect.centerX,
                effect.centerY,
                currentRadius *
                    0.78f,
                64
            )

            shapeRenderer.color =
                Color(
                    1f,
                    0.88f,
                    0.35f,
                    0.70f *
                        alpha
                )

            shapeRenderer.circle(
                effect.centerX,
                effect.centerY,
                currentRadius *
                    0.48f,
                56
            )
        }
    }
}
