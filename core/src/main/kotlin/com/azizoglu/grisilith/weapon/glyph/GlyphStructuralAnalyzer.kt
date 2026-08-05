package com.azizoglu.grisilith.weapon.glyph

import com.badlogic.gdx.math.Vector2
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object GlyphStructuralAnalyzer {

    fun score(
        glyphType: GlyphType,
        points: List<Vector2>
    ): Float {

        if (
            points.size <
            3
        ) {
            return 0f
        }

        val normalized =
            GlyphGeometry.normalize(
                points
            )

        val startEndDistance =
            normalized.first()
                .dst(
                    normalized.last()
                )

        val intersections =
            GlyphGeometry
                .segmentIntersectionCount(
                    normalized
                )

        val angles =
            GlyphGeometry
                .turningAngles(
                    normalized
                )

        val sharpTurns =
            angles.count {
                it <=
                    112f
            }

        val bounds =
            bounds(
                normalized
            )

        return when (
            glyphType
        ) {
            GlyphType.CALL_LIGHTNING -> {
                weighted(
                    openScore =
                        scaleInverse(
                            startEndDistance,
                            0.10f,
                            0.70f
                        ),

                    intersectionScore =
                        if (
                            intersections ==
                            0
                        ) {
                            1f
                        } else {
                            0.25f
                        },

                    featureScore =
                        (
                            sharpTurns /
                                5f
                            )
                            .coerceIn(
                                0f,
                                1f
                            )
                )
            }

            GlyphType.TSUNAMI -> {
                val verticalBody =
                    (
                        bounds.height /
                            bounds.width
                                .coerceAtLeast(
                                    0.01f
                                )
                        )
                        .coerceIn(
                            0f,
                            2f
                        ) /
                        2f

                val rightSideRatio =
                    normalized.count {
                        it.x >
                            0f
                    }
                        .toFloat() /
                        normalized.size

                weighted(
                    openScore =
                        scaleInverse(
                            startEndDistance,
                            0.05f,
                            0.75f
                        ),

                    intersectionScore =
                        if (
                            intersections <=
                            1
                        ) {
                            1f
                        } else {
                            0.35f
                        },

                    featureScore =
                        (
                            verticalBody *
                                0.50f +
                                (
                                    1f -
                                        abs(
                                            rightSideRatio -
                                                0.68f
                                        ) *
                                        2f
                                    )
                                    .coerceIn(
                                        0f,
                                        1f
                                    ) *
                                0.50f
                            )
                )
            }

            GlyphType.METEOR -> {
                val closureScore =
                    scaleInverse(
                        startEndDistance,
                        0.08f,
                        0.65f
                    )

                val crossingScore =
                    (
                        intersections /
                            2f
                        )
                        .coerceIn(
                            0f,
                            1f
                        )

                weighted(
                    openScore =
                        closureScore,

                    intersectionScore =
                        crossingScore,

                    featureScore =
                        circularityScore(
                            normalized
                        )
                )
            }

            GlyphType.SINGULARITY -> {
                val closureScore =
                    scaleInverse(
                        startEndDistance,
                        0.03f,
                        0.55f
                    )

                val crossingScore =
                    (
                        intersections /
                            1f
                        )
                        .coerceIn(
                            0f,
                            1f
                        )

                val leftCount =
                    normalized.count {
                        it.x <
                            0f
                    }

                val rightCount =
                    normalized.count {
                        it.x >=
                            0f
                    }

                val balance =
                    1f -
                        abs(
                            leftCount -
                                rightCount
                        )
                            .toFloat() /
                        normalized.size

                weighted(
                    openScore =
                        closureScore,

                    intersectionScore =
                        crossingScore,

                    featureScore =
                        balance.coerceIn(
                            0f,
                            1f
                        )
                )
            }
        }
    }

    private fun weighted(
        openScore: Float,
        intersectionScore: Float,
        featureScore: Float
    ): Float {

        return (
            openScore *
                0.25f +
                intersectionScore *
                0.30f +
                featureScore *
                0.45f
            )
            .coerceIn(
                0f,
                1f
            )
    }

    private fun scaleInverse(
        value: Float,
        idealMaximum: Float,
        failureValue: Float
    ): Float {

        if (
            value <=
            idealMaximum
        ) {
            return 1f
        }

        if (
            value >=
            failureValue
        ) {
            return 0f
        }

        return 1f -
            (
                value -
                    idealMaximum
                ) /
            (
                failureValue -
                    idealMaximum
                )
    }

    private fun circularityScore(
        points: List<Vector2>
    ): Float {

        val center =
            Vector2()

        points.forEach {
            center.add(
                it
            )
        }

        center.scl(
            1f /
                points.size
        )

        val distances =
            points.map {
                it.dst(
                    center
                )
            }

        val average =
            distances.average()
                .toFloat()
                .coerceAtLeast(
                    0.001f
                )

        val deviation =
            distances
                .map {
                    abs(
                        it -
                            average
                    )
                }
                .average()
                .toFloat()

        return (
            1f -
                deviation /
                average
            )
            .coerceIn(
                0f,
                1f
            )
    }

    private data class Bounds(
        val width: Float,
        val height: Float
    )

    private fun bounds(
        points: List<Vector2>
    ): Bounds {

        var minimumX =
            Float.POSITIVE_INFINITY

        var maximumX =
            Float.NEGATIVE_INFINITY

        var minimumY =
            Float.POSITIVE_INFINITY

        var maximumY =
            Float.NEGATIVE_INFINITY

        points.forEach {
                point ->

            minimumX =
                min(
                    minimumX,
                    point.x
                )

            maximumX =
                max(
                    maximumX,
                    point.x
                )

            minimumY =
                min(
                    minimumY,
                    point.y
                )

            maximumY =
                max(
                    maximumY,
                    point.y
                )
        }

        return Bounds(
            width =
                (
                    maximumX -
                        minimumX
                    )
                    .coerceAtLeast(
                        0.01f
                    ),

            height =
                (
                    maximumY -
                        minimumY
                    )
                    .coerceAtLeast(
                        0.01f
                    )
        )
    }
}
