package com.azizoglu.grisilith.weapon.glyph

import com.badlogic.gdx.math.Vector2
import kotlin.math.cos
import kotlin.math.sin

object GlyphTemplates {

    data class Template(
        val glyphType: GlyphType,
        val points: List<Vector2>
    )

    val all: List<Template> = buildList {
        lightningTemplates().forEach { addTemplate(GlyphType.CALL_LIGHTNING, it) }
        tsunamiTemplates().forEach { addTemplate(GlyphType.TSUNAMI, it) }
        meteorTemplates().forEach { addTemplate(GlyphType.METEOR, it) }
        singularityTemplates().forEach { addTemplate(GlyphType.SINGULARITY, it) }
    }

    private fun MutableList<Template>.addTemplate(
        glyphType: GlyphType,
        points: List<Vector2>
    ) {
        add(
            Template(
                glyphType = glyphType,
                points = GlyphGeometry.normalize(points)
            )
        )
    }

    private fun lightningTemplates(): List<List<Vector2>> = listOf(
        points(-1f to 0.72f, 0.95f to 0.72f, -0.92f to -0.72f, 1f to -0.72f),
        points(-1f to 0.58f, 0.82f to 0.58f, -0.72f to -0.62f, 1f to -0.62f),
        points(-0.88f to 0.82f, 1f to 0.76f, -0.82f to -0.74f, 0.88f to -0.68f)
    )

    private fun tsunamiTemplates(): List<List<Vector2>> = listOf(
        rectangle(0.78f, 0.72f),
        rectangle(0.92f, 0.62f),
        rectangle(0.66f, 0.88f)
    )

    private fun meteorTemplates(): List<List<Vector2>> = listOf(
        triangle(0f, 1f, -0.88f, -0.72f, 0.88f),
        triangle(-0.10f, 1f, -1f, -0.66f, 0.82f),
        triangle(0.12f, 0.92f, -0.82f, -0.78f, 1f)
    )

    private fun singularityTemplates(): List<List<Vector2>> = listOf(
        ellipse(1f, 1f),
        ellipse(1f, 0.82f),
        ellipse(0.84f, 1f)
    )

    private fun rectangle(halfWidth: Float, halfHeight: Float): List<Vector2> =
        points(
            -halfWidth to halfHeight,
            halfWidth to halfHeight,
            halfWidth to -halfHeight,
            -halfWidth to -halfHeight,
            -halfWidth to halfHeight
        )

    private fun triangle(
        topX: Float,
        topY: Float,
        leftX: Float,
        bottomY: Float,
        rightX: Float
    ): List<Vector2> =
        points(
            topX to topY,
            rightX to bottomY,
            leftX to bottomY,
            topX to topY
        )

    private fun ellipse(radiusX: Float, radiusY: Float): List<Vector2> =
        List(72) { index ->
            val progress = index / 71f
            val angle = progress * Math.PI * 2.0
            Vector2(
                cos(angle).toFloat() * radiusX,
                sin(angle).toFloat() * radiusY
            )
        }

    private fun points(vararg values: Pair<Float, Float>): List<Vector2> =
        values.map { Vector2(it.first, it.second) }
}
