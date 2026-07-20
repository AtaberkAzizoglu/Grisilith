package com.azizoglu.grisilith.economy

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2

class GoldCoinPickup(
    startX: Float,
    startY: Float,
    private val value: Float
) {
    private val position = Vector2(
        startX,
        startY
    )

    private val movementDirection = Vector2()

    private val size = 16f
    private val flightSpeed = 650f
    private val collectionDistance = 26f
    private val waitingDuration = 0.25f

    private var elapsedWaitingTime = 0f

    var isCollected: Boolean = false
        private set

    fun update(
        delta: Float,
        targetX: Float,
        targetY: Float,
        onCollected: (Float) -> Unit
    ) {
        if (isCollected) {
            return
        }

        elapsedWaitingTime += delta

        if (elapsedWaitingTime < waitingDuration) {
            return
        }

        movementDirection.set(
            targetX - centerX,
            targetY - centerY
        )

        val distanceToTarget =
            movementDirection.len()

        if (distanceToTarget <= collectionDistance) {
            isCollected = true
            onCollected(value)
            return
        }

        if (!movementDirection.isZero) {
            movementDirection.nor()

            position.mulAdd(
                movementDirection,
                flightSpeed * delta
            )
        }
    }

    fun render(shapeRenderer: ShapeRenderer) {
        if (isCollected) {
            return
        }

        shapeRenderer.color = Color.GOLD

        shapeRenderer.circle(
            centerX,
            centerY,
            size / 2f
        )
    }

    private val centerX: Float
        get() = position.x + size / 2f

    private val centerY: Float
        get() = position.y + size / 2f
}
