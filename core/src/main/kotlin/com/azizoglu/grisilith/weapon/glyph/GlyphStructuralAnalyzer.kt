package com.azizoglu.grisilith.weapon.glyph

import com.badlogic.gdx.math.Vector2
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object GlyphStructuralAnalyzer {

    data class Features(
        val isClosed: Boolean,
        val closureDistance: Float,
        val sharpCornerCount: Int,
        val intersectionCount: Int,
        val circularity: Float,
        val width: Float,
        val height: Float,
        val simplifiedPointCount: Int
    )

    fun extract(points: List<Vector2>): Features {
        val normalized = GlyphGeometry.normalize(points)
        if (normalized.size < 3) {
            return Features(false, 1f, 0, 0, 0f, 0f, 0f, normalized.size)
        }

        val closureDistance = normalized.first().dst(normalized.last())
        val simplified = GlyphGeometry.simplify(normalized, SIMPLIFICATION_EPSILON)
        val cornerSource = if (closureDistance <= CLOSED_DISTANCE) {
            closeForCornerAnalysis(simplified)
        } else {
            simplified
        }
        val sharpCornerCount = GlyphGeometry.turningAngles(cornerSource)
            .count { it <= SHARP_CORNER_MAXIMUM_ANGLE }
        val bounds = bounds(normalized)

        return Features(
            isClosed = closureDistance <= CLOSED_DISTANCE,
            closureDistance = closureDistance,
            sharpCornerCount = sharpCornerCount,
            intersectionCount = GlyphGeometry.segmentIntersectionCount(normalized),
            circularity = circularityScore(normalized),
            width = bounds.width,
            height = bounds.height,
            simplifiedPointCount = simplified.size
        )
    }

    fun isPlausible(glyphType: GlyphType, features: Features): Boolean = when (glyphType) {
        GlyphType.CALL_LIGHTNING ->
            !features.isClosed && features.intersectionCount <= 1 &&
                features.sharpCornerCount in 1..5

        GlyphType.METEOR ->
            features.isClosed && features.intersectionCount <= 1 &&
                features.sharpCornerCount in 2..5

        GlyphType.TSUNAMI ->
            features.isClosed && features.intersectionCount <= 1 &&
                features.sharpCornerCount in 3..7

        GlyphType.SINGULARITY ->
            features.isClosed && features.intersectionCount <= 1 &&
                features.circularity >= 0.60f && features.sharpCornerCount <= 3
    }

    fun score(
        glyphType: GlyphType,
        points: List<Vector2>,
        features: Features = extract(points)
    ): Float {
        if (!isPlausible(glyphType, features)) return 0f

        return when (glyphType) {
            GlyphType.CALL_LIGHTNING -> {
                val cornerScore = proximityScore(features.sharpCornerCount.toFloat(), 2f, 3f)
                val opennessScore = (features.closureDistance / 0.70f).coerceIn(0f, 1f)
                (cornerScore * 0.62f + opennessScore * 0.28f +
                    noIntersectionScore(features) * 0.10f).coerceIn(0f, 1f)
            }

            GlyphType.METEOR -> {
                val cornerScore = proximityScore(features.sharpCornerCount.toFloat(), 3f, 2f)
                val nonCircularScore = (1f - features.circularity).coerceIn(0f, 1f)
                (cornerScore * 0.62f + closureScore(features) * 0.28f +
                    nonCircularScore * 0.10f).coerceIn(0f, 1f)
            }

            GlyphType.TSUNAMI -> {
                val cornerScore = proximityScore(features.sharpCornerCount.toFloat(), 4f, 3f)
                val nonCircularScore = (1f - features.circularity).coerceIn(0f, 1f)
                (cornerScore * 0.64f + closureScore(features) * 0.24f +
                    nonCircularScore * 0.12f).coerceIn(0f, 1f)
            }

            GlyphType.SINGULARITY -> {
                val smoothnessScore =
                    (1f - features.sharpCornerCount / 4f).coerceIn(0f, 1f)
                (features.circularity * 0.66f + smoothnessScore * 0.22f +
                    closureScore(features) * 0.12f).coerceIn(0f, 1f)
            }
        }
    }

    private fun closureScore(features: Features): Float =
        (1f - features.closureDistance / CLOSED_DISTANCE).coerceIn(0f, 1f)

    private fun noIntersectionScore(features: Features): Float =
        if (features.intersectionCount == 0) 1f else 0.45f

    private fun proximityScore(value: Float, ideal: Float, tolerance: Float): Float =
        (1f - abs(value - ideal) / tolerance).coerceIn(0f, 1f)

    private fun closeForCornerAnalysis(points: List<Vector2>): List<Vector2> {
        if (points.size < 3) return points
        val result = points.map { Vector2(it) }.toMutableList()
        if (result.first().dst(result.last()) > 0.001f) {
            result.add(Vector2(result.first()))
        }
        result.add(0, Vector2(result[result.lastIndex - 1]))
        return result
    }

    private fun circularityScore(points: List<Vector2>): Float {
        val center = Vector2()
        points.forEach { center.add(it) }
        center.scl(1f / points.size)
        val distances = points.map { it.dst(center) }
        val average = distances.average().toFloat().coerceAtLeast(0.001f)
        val deviation = distances.map { abs(it - average) }.average().toFloat()
        return (1f - deviation / average).coerceIn(0f, 1f)
    }

    private data class Bounds(val width: Float, val height: Float)

    private fun bounds(points: List<Vector2>): Bounds {
        var minX = Float.POSITIVE_INFINITY
        var maxX = Float.NEGATIVE_INFINITY
        var minY = Float.POSITIVE_INFINITY
        var maxY = Float.NEGATIVE_INFINITY
        points.forEach {
            minX = min(minX, it.x)
            maxX = max(maxX, it.x)
            minY = min(minY, it.y)
            maxY = max(maxY, it.y)
        }
        return Bounds((maxX - minX).coerceAtLeast(0.01f),
            (maxY - minY).coerceAtLeast(0.01f))
    }

    private const val CLOSED_DISTANCE = 0.34f
    private const val SIMPLIFICATION_EPSILON = 0.050f
    private const val SHARP_CORNER_MAXIMUM_ANGLE = 142f
}
