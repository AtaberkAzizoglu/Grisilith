package com.azizoglu.grisilith.weapon.glyph

import com.badlogic.gdx.math.Vector2
import kotlin.math.sqrt

class GlyphRecognizer {

    private data class Candidate(
        val glyphType: GlyphType,
        val templateScore: Float,
        val structuralScore: Float,
        val finalScore: Float
    )

    fun recognize(
        rawPoints: List<Vector2>
    ): GlyphRecognitionResult {

        val pathLength =
            GlyphGeometry
                .pathLength(
                    rawPoints
                )

        if (
            rawPoints.size <
            GlyphCastingConfig
                .MINIMUM_POINT_COUNT
        ) {
            return failure(
                reason =
                    "Not enough points"
            )
        }

        if (
            pathLength <
            GlyphCastingConfig
                .MINIMUM_PATH_LENGTH
        ) {
            return failure(
                reason =
                    "Path too short"
            )
        }

        val normalized =
            GlyphGeometry.normalize(
                rawPoints
            )

        val candidates =
            GlyphType.entries
                .map {
                        glyphType ->

                    val templateScore =
                        bestTemplateScore(
                            glyphType =
                                glyphType,

                            normalizedPoints =
                                normalized
                        )

                    val structuralScore =
                        GlyphStructuralAnalyzer
                            .score(
                                glyphType =
                                    glyphType,

                                points =
                                    normalized
                            )

                    val finalScore =
                        (
                            templateScore *
                                0.64f +
                                structuralScore *
                                0.36f
                            )
                            .coerceIn(
                                0f,
                                1f
                            )

                    Candidate(
                        glyphType =
                            glyphType,

                        templateScore =
                            templateScore,

                        structuralScore =
                            structuralScore,

                        finalScore =
                            finalScore
                    )
                }
                .sortedByDescending {
                    it.finalScore
                }

        val best =
            candidates.first()

        val second =
            candidates.getOrNull(
                1
            )

        val scoreMargin =
            best.finalScore -
                (
                    second?.finalScore
                        ?: 0f
                    )

        val accepted =
            best.finalScore >=
                ACCEPTANCE_SCORE &&
                scoreMargin >=
                MINIMUM_SCORE_MARGIN

        return GlyphRecognitionResult(
            glyphType =
                best.glyphType,

            confidence =
                best.finalScore,

            templateScore =
                best.templateScore,

            structuralScore =
                best.structuralScore,

            accepted =
                accepted,

            reason =
                if (
                    accepted
                ) {
                    "Accepted"
                } else if (
                    best.finalScore <
                    ACCEPTANCE_SCORE
                ) {
                    "Low confidence"
                } else {
                    "Ambiguous shape"
                }
        )
    }

    private fun bestTemplateScore(
        glyphType: GlyphType,
        normalizedPoints: List<Vector2>
    ): Float {

        val rotationAngles =
            rotationAnglesFor(
                glyphType
            )

        var bestDistance =
            Float.MAX_VALUE

        GlyphTemplates.all
            .asSequence()
            .filter {
                it.glyphType ==
                    glyphType
            }
            .forEach {
                    template ->

                rotationAngles
                    .forEach {
                            angle ->

                        val rotated =
                            GlyphGeometry.rotate(
                                normalizedPoints,
                                angle
                            )

                        val normalDistance =
                            GlyphGeometry
                                .averageDistance(
                                    rotated,
                                    template.points
                                )

                        val reversedDistance =
                            GlyphGeometry
                                .averageDistance(
                                    rotated.reversed(),
                                    template.points
                                )

                        bestDistance =
                            minOf(
                                bestDistance,
                                normalDistance,
                                reversedDistance
                            )
                    }
            }

        val halfDiagonal =
            sqrt(
                2f
            ) /
                2f

        return (
            1f -
                bestDistance /
                halfDiagonal
            )
            .coerceIn(
                0f,
                1f
            )
    }

    private fun rotationAnglesFor(
        glyphType: GlyphType
    ): List<Float> {

        val maximumRotation =
            if (
                glyphType ==
                GlyphType.METEOR
            ) {
                180
            } else {
                90
            }

        return (
            -maximumRotation..maximumRotation step
                ROTATION_STEP_DEGREES
            )
            .map {
                it.toFloat()
            }
    }

    private fun failure(
        reason: String
    ): GlyphRecognitionResult {

        return GlyphRecognitionResult(
            glyphType =
                null,

            confidence =
                0f,

            templateScore =
                0f,

            structuralScore =
                0f,

            accepted =
                false,

            reason =
                reason
        )
    }

    companion object {

        private const val ACCEPTANCE_SCORE =
            0.70f

        private const val MINIMUM_SCORE_MARGIN =
            0.08f

        private const val ROTATION_STEP_DEGREES =
            10
    }
}
