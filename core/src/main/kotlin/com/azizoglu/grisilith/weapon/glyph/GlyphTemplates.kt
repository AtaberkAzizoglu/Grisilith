package com.azizoglu.grisilith.weapon.glyph

import com.badlogic.gdx.math.Vector2
import kotlin.math.cos
import kotlin.math.sin

object GlyphTemplates {

    data class Template(
        val glyphType: GlyphType,
        val points: List<Vector2>
    )

    val all:
        List<Template> =
        buildList {
            lightningTemplates()
                .forEach {
                    add(
                        Template(
                            glyphType =
                                GlyphType.CALL_LIGHTNING,

                            points =
                                GlyphGeometry.normalize(
                                    it
                                )
                        )
                    )
                }

            tsunamiTemplates()
                .forEach {
                    add(
                        Template(
                            glyphType =
                                GlyphType.TSUNAMI,

                            points =
                                GlyphGeometry.normalize(
                                    it
                                )
                        )
                    )
                }

            meteorTemplates()
                .forEach {
                    add(
                        Template(
                            glyphType =
                                GlyphType.METEOR,

                            points =
                                GlyphGeometry.normalize(
                                    it
                                )
                        )
                    )
                }

            singularityTemplates()
                .forEach {
                    add(
                        Template(
                            glyphType =
                                GlyphType.SINGULARITY,

                            points =
                                GlyphGeometry.normalize(
                                    it
                                )
                        )
                    )
                }
        }

    private fun lightningTemplates():
        List<List<Vector2>> {

        return listOf(
            points(
                0f to 0f,
                0.18f to 0.42f,
                -0.02f to 0.34f,
                0.38f to 0.72f,
                0.22f to 0.48f,
                0.62f to 1f
            ),

            points(
                0f to 0f,
                0.25f to 0.35f,
                0.08f to 0.30f,
                0.48f to 0.64f,
                0.30f to 0.56f,
                0.72f to 1f
            ),

            points(
                0f to 0f,
                -0.14f to 0.40f,
                0.08f to 0.35f,
                -0.30f to 0.72f,
                -0.12f to 0.57f,
                -0.42f to 1f
            )
        )
    }

    private fun tsunamiTemplates():
        List<List<Vector2>> {

        return listOf(
            createBTemplate(
                bulge =
                    0.62f
            ),

            createBTemplate(
                bulge =
                    0.78f
            ),

            createBTemplate(
                bulge =
                    0.50f
            )
        )
    }

    private fun meteorTemplates():
        List<List<Vector2>> {

        return listOf(
            createMeteorTemplate(
                circleScaleX =
                    1f,

                circleScaleY =
                    1f
            ),

            createMeteorTemplate(
                circleScaleX =
                    0.88f,

                circleScaleY =
                    1.08f
            ),

            createMeteorTemplate(
                circleScaleX =
                    1.08f,

                circleScaleY =
                    0.88f
            )
        )
    }

    private fun singularityTemplates():
        List<List<Vector2>> {

        return listOf(
            createInfinityTemplate(
                height =
                    0.55f
            ),

            createInfinityTemplate(
                height =
                    0.72f
            ),

            createInfinityTemplate(
                height =
                    0.42f
            )
        )
    }

    private fun createBTemplate(
        bulge: Float
    ): List<Vector2> {

        val result =
            mutableListOf<Vector2>()

        result.add(
            Vector2(
                0f,
                1f
            )
        )

        result.add(
            Vector2(
                0f,
                0f
            )
        )

        repeat(
            18
        ) {
                index ->

            val progress =
                index /
                    17f

            val angle =
                -90f +
                    progress *
                    180f

            result.add(
                Vector2(
                    cos(
                        Math.toRadians(
                            angle.toDouble()
                        )
                    )
                        .toFloat() *
                        bulge,

                    0.25f +
                        sin(
                            Math.toRadians(
                                angle.toDouble()
                            )
                        )
                            .toFloat() *
                        0.25f
                )
            )
        }

        repeat(
            18
        ) {
                index ->

            val progress =
                index /
                    17f

            val angle =
                -90f +
                    progress *
                    180f

            result.add(
                Vector2(
                    cos(
                        Math.toRadians(
                            angle.toDouble()
                        )
                    )
                        .toFloat() *
                        bulge,

                    0.75f +
                        sin(
                            Math.toRadians(
                                angle.toDouble()
                            )
                        )
                            .toFloat() *
                        0.25f
                )
            )
        }

        return result
    }

    private fun createMeteorTemplate(
        circleScaleX: Float,
        circleScaleY: Float
    ): List<Vector2> {

        val result =
            mutableListOf<Vector2>()

        repeat(
            42
        ) {
                index ->

            val progress =
                index /
                    41f

            val angle =
                progress *
                    Math.PI *
                    2.0

            result.add(
                Vector2(
                    cos(
                        angle
                    )
                        .toFloat() *
                        circleScaleX,

                    sin(
                        angle
                    )
                        .toFloat() *
                        circleScaleY
                )
            )
        }

        result.add(
            Vector2(
                -0.72f *
                    circleScaleX,

                -0.72f *
                    circleScaleY
            )
        )

        result.add(
            Vector2(
                0.72f *
                    circleScaleX,

                0.72f *
                    circleScaleY
            )
        )

        return result
    }

    private fun createInfinityTemplate(
        height: Float
    ): List<Vector2> {

        val result =
            mutableListOf<Vector2>()

        repeat(
            64
        ) {
                index ->

            val progress =
                index /
                    63f

            val angle =
                progress *
                    Math.PI *
                    2.0

            val denominator =
                1.0 +
                    sin(
                        angle
                    ) *
                    sin(
                        angle
                    )

            result.add(
                Vector2(
                    (
                        cos(
                            angle
                        ) /
                            denominator
                        )
                        .toFloat(),

                    (
                        sin(
                            angle
                        ) *
                            cos(
                                angle
                            ) /
                            denominator
                        )
                        .toFloat() *
                        height
                )
            )
        }

        return result
    }

    private fun points(
        vararg values:
        Pair<Float, Float>
    ): List<Vector2> {

        return values.map {
                value ->

            Vector2(
                value.first,
                value.second
            )
        }
    }
}
