package com.azizoglu.grisilith.player

import com.azizoglu.grisilith.combat.HealthComponent
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

class Player(
    startX: Float,
    startY: Float,
    private val worldWidth: Float,
    private val worldHeight: Float
) {
    private val position =
        Vector2(startX, startY)

    private val previousPosition =
        Vector2(startX, startY)

    private val movementDirection =
        Vector2()

    private val size = 48f
    private var speed = 260f

    private val collisionBounds =
        Rectangle()

    val health =
        HealthComponent(
            maximumHealth = 100
        )

    val centerX: Float
        get() = position.x + size / 2f

    val centerY: Float
        get() = position.y + size / 2f

    val bounds: Rectangle
        get() {
            collisionBounds.set(
                position.x,
                position.y,
                size,
                size
            )

            return collisionBounds
        }

    fun update(delta: Float) {
        if (health.isDead) {
            return
        }

        previousPosition.set(position)

        readMovementInput()

        if (!movementDirection.isZero) {
            movementDirection.nor()

            position.mulAdd(
                movementDirection,
                speed * delta
            )
        }

        keepInsideWorld()
    }

    fun resolveCollision(
        otherBounds: Rectangle
    ): Boolean {
        if (!bounds.overlaps(otherBounds)) {
            return false
        }

        position.set(previousPosition)
        return true
    }

    fun resolveObstacleCollision(
        obstacleBounds: Rectangle
    ) {
        if (!bounds.overlaps(obstacleBounds)) {
            return
        }

        val playerBounds = bounds

        val overlapLeft =
            playerBounds.x +
                playerBounds.width -
                obstacleBounds.x

        val overlapRight =
            obstacleBounds.x +
                obstacleBounds.width -
                playerBounds.x

        val overlapBottom =
            playerBounds.y +
                playerBounds.height -
                obstacleBounds.y

        val overlapTop =
            obstacleBounds.y +
                obstacleBounds.height -
                playerBounds.y

        val smallestHorizontalOverlap =
            minOf(
                overlapLeft,
                overlapRight
            )

        val smallestVerticalOverlap =
            minOf(
                overlapBottom,
                overlapTop
            )

        if (
            smallestHorizontalOverlap <
            smallestVerticalOverlap
        ) {
            if (
                centerX <
                obstacleBounds.x +
                obstacleBounds.width / 2f
            ) {
                position.x -= overlapLeft
            } else {
                position.x += overlapRight
            }
        } else {
            if (
                centerY <
                obstacleBounds.y +
                obstacleBounds.height / 2f
            ) {
                position.y -= overlapBottom
            } else {
                position.y += overlapTop
            }
        }

        keepInsideWorld()
    }

    fun takeDamage(amount: Int): Int {
        return health.takeDamage(amount)
    }

    fun increaseMaximumHealth(amount: Int) {
        health.increaseMaximumHealth(
            amount = amount,
            healAddedHealth = true
        )
    }

    fun increaseMovementSpeed(
        percentageIncrease: Float
    ) {
        if (percentageIncrease <= 0f) {
            return
        }

        speed *=
            1f + percentageIncrease
    }

    fun render(
        shapeRenderer: ShapeRenderer
    ) {
        shapeRenderer.color =
            if (health.isDead) {
                Color.DARK_GRAY
            } else {
                Color.BLUE
            }

        shapeRenderer.rect(
            position.x,
            position.y,
            size,
            size
        )

        renderHealthBar(shapeRenderer)
    }

    private fun renderHealthBar(
        shapeRenderer: ShapeRenderer
    ) {
        val barWidth = size
        val barHeight = 7f
        val barY =
            position.y + size + 8f

        shapeRenderer.color =
            Color.DARK_GRAY

        shapeRenderer.rect(
            position.x,
            barY,
            barWidth,
            barHeight
        )

        shapeRenderer.color =
            Color.GREEN

        shapeRenderer.rect(
            position.x,
            barY,
            barWidth *
                health.healthRatio,
            barHeight
        )
    }

    private fun readMovementInput() {
        movementDirection.setZero()

        if (
            Gdx.input.isKeyPressed(
                Input.Keys.W
            ) ||
            Gdx.input.isKeyPressed(
                Input.Keys.UP
            )
        ) {
            movementDirection.y += 1f
        }

        if (
            Gdx.input.isKeyPressed(
                Input.Keys.S
            ) ||
            Gdx.input.isKeyPressed(
                Input.Keys.DOWN
            )
        ) {
            movementDirection.y -= 1f
        }

        if (
            Gdx.input.isKeyPressed(
                Input.Keys.A
            ) ||
            Gdx.input.isKeyPressed(
                Input.Keys.LEFT
            )
        ) {
            movementDirection.x -= 1f
        }

        if (
            Gdx.input.isKeyPressed(
                Input.Keys.D
            ) ||
            Gdx.input.isKeyPressed(
                Input.Keys.RIGHT
            )
        ) {
            movementDirection.x += 1f
        }
    }

    private fun keepInsideWorld() {
        val maximumX =
            (worldWidth - size)
                .coerceAtLeast(0f)

        val maximumY =
            (worldHeight - size)
                .coerceAtLeast(0f)

        position.x =
            position.x.coerceIn(
                0f,
                maximumX
            )

        position.y =
            position.y.coerceIn(
                0f,
                maximumY
            )
    }
}
