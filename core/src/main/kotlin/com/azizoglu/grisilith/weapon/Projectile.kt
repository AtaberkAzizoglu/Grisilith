package com.azizoglu.grisilith.weapon

import com.azizoglu.grisilith.enemy.Enemy
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

class Projectile(
    startX: Float,
    startY: Float,
    targetX: Float,
    targetY: Float
) {
    private val position = Vector2(
        startX,
        startY
    )

    private val direction = Vector2(
        targetX - startX,
        targetY - startY
    ).nor()

    private val collisionBounds = Rectangle()

    private val size = 14f
    private val speed = 700f
    private val damage = 10

    var isActive: Boolean = true
        private set

    fun update(
        delta: Float,
        worldWidth: Float,
        worldHeight: Float
    ) {
        if (!isActive) {
            return
        }

        position.mulAdd(
            direction,
            speed * delta
        )

        val isOutsideWorld =
            position.x < -size ||
                position.y < -size ||
                position.x > worldWidth ||
                position.y > worldHeight

        if (isOutsideWorld) {
            isActive = false
        }
    }

    fun checkCollision(
        enemies: List<Enemy>,
        onDamageDealt: (Int) -> Unit,
        onEnemyKilled: () -> Unit
    ) {
        if (!isActive) {
            return
        }

        collisionBounds.set(
            position.x,
            position.y,
            size,
            size
        )

        for (enemy in enemies) {
            if (
                enemy.isDead ||
                !collisionBounds.overlaps(enemy.bounds)
            ) {
                continue
            }

            val wasAliveBeforeHit = !enemy.isDead
            val appliedDamage = enemy.takeDamage(damage)

            onDamageDealt(appliedDamage)

            if (wasAliveBeforeHit && enemy.isDead) {
                onEnemyKilled()
            }

            isActive = false
            return
        }
    }

    fun render(shapeRenderer: ShapeRenderer) {
        if (!isActive) {
            return
        }

        shapeRenderer.color = Color.YELLOW

        shapeRenderer.rect(
            position.x,
            position.y,
            size,
            size
        )
    }
}
