package com.azizoglu.grisilith.enemy

import com.azizoglu.grisilith.combat.ContactDamageTimer
import com.azizoglu.grisilith.combat.HealthComponent
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import kotlin.math.sqrt

class Enemy(
    startX: Float,
    startY: Float,
    private val stats: EnemyStats
) {
    private val position = Vector2(startX, startY)
    private val previousPosition = Vector2(startX, startY)
    private val movementDirection = Vector2()

    private val size = stats.size

    private val collisionBounds = Rectangle()

    private val contactDamageTimer = ContactDamageTimer(
        hitsPerSecond = 5f
    )

    val health = HealthComponent(
        maximumHealth = stats.maximumHealth
    )

    val contactDamage: Int
        get() = stats.contactDamage

    val isDead: Boolean
        get() = health.isDead

    val type: EnemyType
        get() = stats.type

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

    val goldReward: Float
        get() = stats.goldReward

    fun update(
        delta: Float,
        targetX: Float,
        targetY: Float,
        targetBounds: Rectangle,
        onContactDamage: (Int) -> Unit
    ) {
        if (isDead) {
            return
        }

        previousPosition.set(position)

        movementDirection.set(
            targetX - centerX,
            targetY - centerY
        )

        if (!movementDirection.isZero) {
            movementDirection.nor()

            position.mulAdd(
                movementDirection,
                stats.movementSpeed * delta
            )
        }

        val isTouchingPlayer =
            bounds.overlaps(targetBounds)

        if (isTouchingPlayer) {
            position.set(previousPosition)
        }

        contactDamageTimer.update(
            delta = delta,
            isTouching = isTouchingPlayer
        ) {
            onContactDamage(contactDamage)
        }
    }

    fun separateFrom(other: Enemy) {
        if (isDead || other.isDead || other === this) {
            return
        }

        var differenceX = centerX - other.centerX
        var differenceY = centerY - other.centerY

        var distanceSquared =
            differenceX * differenceX +
                differenceY * differenceY

        val minimumDistance =
            (size + other.size) / 2f

        val minimumDistanceSquared =
            minimumDistance * minimumDistance

        if (distanceSquared >= minimumDistanceSquared) {
            return
        }

        /*
         * İki düşman tam olarak aynı noktadaysa rastgele bir yön seçer.
         */
        if (distanceSquared < 0.0001f) {
            val angle = MathUtils.random(
                0f,
                MathUtils.PI2
            )

            differenceX = MathUtils.cos(angle)
            differenceY = MathUtils.sin(angle)
            distanceSquared = 1f
        }

        val distance = sqrt(distanceSquared)
        val overlap = minimumDistance - distance

        val normalizedX = differenceX / distance
        val normalizedY = differenceY / distance

        /*
         * İki düşman da çakışmanın yarısı kadar geri itilir.
         */
        val pushAmount = overlap / 2f

        position.x += normalizedX * pushAmount
        position.y += normalizedY * pushAmount

        other.position.x -= normalizedX * pushAmount
        other.position.y -= normalizedY * pushAmount
    }

    fun keepInsideWorld(
        worldWidth: Float,
        worldHeight: Float
    ) {
        val maximumX =
            (worldWidth - size).coerceAtLeast(0f)

        val maximumY =
            (worldHeight - size).coerceAtLeast(0f)

        position.x = position.x.coerceIn(
            0f,
            maximumX
        )

        position.y = position.y.coerceIn(
            0f,
            maximumY
        )
    }

    fun takeDamage(amount: Int): Int {
        return health.takeDamage(amount)
    }

    fun render(shapeRenderer: ShapeRenderer) {
        if (isDead) {
            return
        }

        shapeRenderer.color = when (stats.type) {
            EnemyType.NORMAL ->
                Color.RED

            EnemyType.UNIQUE ->
                Color.PURPLE

            EnemyType.MINI_BOSS ->
                Color.ORANGE

            EnemyType.FINAL_BOSS ->
                Color.GOLD
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
        val barHeight = 6f
        val barY = position.y + size + 7f

        shapeRenderer.color = Color.DARK_GRAY

        shapeRenderer.rect(
            position.x,
            barY,
            barWidth,
            barHeight
        )

        shapeRenderer.color = Color.GREEN

        shapeRenderer.rect(
            position.x,
            barY,
            barWidth * health.healthRatio,
            barHeight
        )
    }
}
