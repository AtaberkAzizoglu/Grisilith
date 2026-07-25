package com.azizoglu.grisilith.combat.debug

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2

class CombatDebugRenderer(
    private val shapeRenderer:
    ShapeRenderer,

    private val camera:
    OrthographicCamera
) {

    fun render() {
        if (
            !CombatDebugVisualManager.isEnabled
        ) {
            return
        }

        val visuals =
            CombatDebugVisualManager
                .activeVisuals()

        if (visuals.isEmpty()) {
            return
        }

        shapeRenderer.projectionMatrix =
            camera.combined

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        )

        visuals.forEach { visual ->
            shapeRenderer.color =
                if (visual.isUpgraded()) {
                    Color.GOLD
                } else {
                    Color.WHITE
                }

            when (visual) {
                is CombatDebugVisual.Line -> {
                    renderLine(
                        visual
                    )
                }

                is CombatDebugVisual.Cone -> {
                    renderCone(
                        visual
                    )
                }

                is CombatDebugVisual.Circle -> {
                    renderCircle(
                        visual
                    )
                }
            }
        }

        shapeRenderer.end()
    }

    private fun renderLine(
        visual: CombatDebugVisual.Line
    ) {
        shapeRenderer.line(
            visual.startX,
            visual.startY,
            visual.endX,
            visual.endY
        )

        /*
         * Çizginin daha rahat görülmesi için
         * uç noktalarına küçük daireler.
         */
        shapeRenderer.circle(
            visual.startX,
            visual.startY,
            5f,
            12
        )

        shapeRenderer.circle(
            visual.endX,
            visual.endY,
            5f,
            12
        )
    }

    private fun renderCone(
        visual: CombatDebugVisual.Cone
    ) {
        val direction =
            Vector2(
                visual.directionX,
                visual.directionY
            )

        if (direction.isZero) {
            direction.set(
                1f,
                0f
            )
        }

        direction.nor()

        val halfAngle =
            visual.angleDegrees / 2f

        val leftDirection =
            Vector2(direction)
                .rotateDeg(halfAngle)

        val rightDirection =
            Vector2(direction)
                .rotateDeg(-halfAngle)

        val leftEndX =
            visual.originX +
                leftDirection.x *
                visual.range

        val leftEndY =
            visual.originY +
                leftDirection.y *
                visual.range

        val rightEndX =
            visual.originX +
                rightDirection.x *
                visual.range

        val rightEndY =
            visual.originY +
                rightDirection.y *
                visual.range

        shapeRenderer.line(
            visual.originX,
            visual.originY,
            leftEndX,
            leftEndY
        )

        shapeRenderer.line(
            visual.originX,
            visual.originY,
            rightEndX,
            rightEndY
        )

        /*
         * Koninin dış yayını birkaç kısa
         * çizgiyle gösterir.
         */
        val centerAngle =
            direction.angleDeg()

        val segmentCount = 14

        var previousX =
            leftEndX

        var previousY =
            leftEndY

        for (
        segmentIndex in 1..segmentCount
        ) {
            val progress =
                segmentIndex.toFloat() /
                    segmentCount.toFloat()

            val angle =
                centerAngle +
                    halfAngle -
                    visual.angleDegrees *
                    progress

            val currentX =
                visual.originX +
                    MathUtils.cosDeg(angle) *
                    visual.range

            val currentY =
                visual.originY +
                    MathUtils.sinDeg(angle) *
                    visual.range

            shapeRenderer.line(
                previousX,
                previousY,
                currentX,
                currentY
            )

            previousX = currentX
            previousY = currentY
        }
    }

    private fun renderCircle(
        visual: CombatDebugVisual.Circle
    ) {
        shapeRenderer.circle(
            visual.centerX,
            visual.centerY,
            visual.radius,
            40
        )
    }

    private fun CombatDebugVisual
        .isUpgraded(): Boolean {

        return when (this) {
            is CombatDebugVisual.Line ->
                upgraded

            is CombatDebugVisual.Cone ->
                upgraded

            is CombatDebugVisual.Circle ->
                upgraded
        }
    }
}
