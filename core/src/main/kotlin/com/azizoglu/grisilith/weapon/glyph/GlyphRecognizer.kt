package com.azizoglu.grisilith.weapon.glyph

import com.badlogic.gdx.math.Vector2
import kotlin.math.sqrt

class GlyphRecognizer {

    fun recognize(
        rawPoints: List<Vector2>,
        expectedGlyphType: GlyphType
    ): GlyphRecognitionResult {
        val pathLength = GlyphGeometry.pathLength(rawPoints)
        if (rawPoints.size < GlyphCastingConfig.MINIMUM_POINT_COUNT) return failure(expectedGlyphType,"Not enough points")
        if (pathLength < GlyphCastingConfig.MINIMUM_PATH_LENGTH) return failure(expectedGlyphType,"Path too short")

        val normalized = GlyphGeometry.normalize(rawPoints)
        val features = GlyphStructuralAnalyzer.extract(normalized)
        val structuralScore = GlyphStructuralAnalyzer.score(expectedGlyphType, normalized, features)
        val templateScore = bestTemplateScore(expectedGlyphType, normalized)
        val finalScore = (structuralScore * 0.72f + templateScore * 0.28f).coerceIn(0f,1f)
        val plausible = GlyphStructuralAnalyzer.isPlausible(expectedGlyphType, features)
        val accepted = plausible && finalScore >= 0.45f

        return GlyphRecognitionResult(
            glyphType = expectedGlyphType,
            confidence = finalScore,
            templateScore = templateScore,
            structuralScore = structuralScore,
            accepted = accepted,
            reason = if (accepted) "Accepted" else if (!plausible) "Wrong geometry" else "Low confidence"
        )
    }

    private fun bestTemplateScore(glyphType: GlyphType, normalizedPoints: List<Vector2>): Float {
        var bestDistance=Float.MAX_VALUE
        val maxRotation=if(glyphType==GlyphType.CALL_LIGHTNING) 90 else 180
        GlyphTemplates.all.asSequence().filter{it.glyphType==glyphType}.forEach { template ->
            (-maxRotation..maxRotation step 8).forEach { angle ->
                val rotated=GlyphGeometry.rotate(normalizedPoints,angle.toFloat())
                bestDistance=minOf(bestDistance,GlyphGeometry.averageDistance(rotated,template.points),GlyphGeometry.averageDistance(rotated.reversed(),template.points))
            }
        }
        return (1f-bestDistance/(sqrt(2f)/2f)).coerceIn(0f,1f)
    }

    private fun failure(expected: GlyphType, reason: String)=GlyphRecognitionResult(expected,0f,0f,0f,false,reason)
}
