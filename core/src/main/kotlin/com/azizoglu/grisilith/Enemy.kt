package com.azizoglu.grisilith.enemy

import com.azizoglu.grisilith.combat.ContactDamageTimer
import com.azizoglu.grisilith.combat.HealthComponent
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import kotlin.math.sqrt
import com.azizoglu.grisilith.combat.condition.ConditionApplication
import com.azizoglu.grisilith.combat.condition.ConditionRuntime
import com.azizoglu.grisilith.content.ConditionType

class Enemy(
    startX: Float,
    startY: Float,
    private val stats: EnemyStats
) {
    private var latestDamageContext =
        EnemyDamageContext.NONE

    private val position = Vector2(startX, startY)
    private val previousPosition = Vector2(startX, startY)
    private val movementDirection = Vector2()

    private val knockbackVelocity =
        Vector2()

    private var knockbackTimeRemaining =
        0f

    private val isBeingKnockedBack: Boolean
        get() =
            knockbackTimeRemaining > 0f &&
                !knockbackVelocity.isZero

    private var knockbackResistance =
        when (stats.type) {

            EnemyType.NORMAL -> 1f

            EnemyType.UNIQUE -> 0.80f

            EnemyType.MINI_BOSS -> 0.35f

            EnemyType.FINAL_BOSS -> 0.15f
        }

    private val size = stats.size

    private var executionMarkRemainingSeconds =
        0f

    private var carriedByHeavyBolt =
        false

    private val collisionBounds = Rectangle()

    private val contactDamageTimer = ContactDamageTimer(
        hitsPerSecond = 5f
    )

    val health = HealthComponent(
        maximumHealth = stats.maximumHealth
    )

    private val conditionRuntime =
        ConditionRuntime(
            owner = this
        )

    val conditions
        get() =
            conditionRuntime.conditions

    val isBurned: Boolean
        get() =
            conditions.contains(
                ConditionType.BURNED
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
        if (
            executionMarkRemainingSeconds >
            0f
        ) {
            executionMarkRemainingSeconds =
                (
                    executionMarkRemainingSeconds -
                        delta
                    )
                    .coerceAtLeast(
                        0f
                    )
        }

        if (isDead) {
            conditions.clear()
            return
        }

        if (carriedByHeavyBolt) {
            conditions.clear()
            return
        }

        conditionRuntime.update(
            delta = delta
        )

        if (isDead) {
            return
        }

        previousPosition.set(position)

        if (isBeingKnockedBack) {
            /*
             * Knockback aktifken düşman oyuncuya doğru
             * ilerlemez. Yalnızca darbenin yönünde gider.
             */
            position.mulAdd(
                knockbackVelocity,
                delta
            )

            knockbackTimeRemaining =
                (
                    knockbackTimeRemaining -
                        delta
                    )
                    .coerceAtLeast(0f)

            /*
             * Frame rate'ten bağımsız yavaşlama.
             * Yaklaşık 1200 px/s² kuvvet kaybeder.
             */
            val currentSpeed =
                knockbackVelocity.len()

            if (currentSpeed > 0f) {
                val newSpeed =
                    (
                        currentSpeed -
                            1200f * delta
                        )
                        .coerceAtLeast(0f)

                if (newSpeed <= 1f) {
                    knockbackVelocity.setZero()
                    knockbackTimeRemaining = 0f
                } else {
                    knockbackVelocity.setLength(
                        newSpeed
                    )
                }
            }
        } else {
            /*
             * Knockback bittikten sonra normal takip
             * hareketine geri döner.
             */
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
        }

        val isTouchingPlayer =
            bounds.overlaps(targetBounds)

        if (isTouchingPlayer) {
            position.set(previousPosition)
        }

        if (isTouchingPlayer) {
            position.set(
                previousPosition
            )

            /*
             * Oyuncuya doğru knockback oluşmuşsa
             * temas noktasında titremeyi engeller.
             */
            knockbackVelocity.setZero()
            knockbackTimeRemaining = 0f
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

    fun takeDamage(
        amount: Int,
        damageContext: EnemyDamageContext =
            EnemyDamageContext.NONE
    ): Int {
        val appliedDamage =
            health.takeDamage(amount)

        if (appliedDamage > 0) {
            latestDamageContext =
                damageContext
        }

        return appliedDamage
    }

    val lethalDamageContext: EnemyDamageContext?
        get() = if (isDead) {
            latestDamageContext
        } else {
            null
        }

    fun markExecuted(
        durationSeconds: Float = 0.45f
    ) {
        executionMarkRemainingSeconds =
            durationSeconds
                .coerceAtLeast(
                    0f
                )
    }

    val shouldRemainForExecutionMark:
        Boolean
        get() =
            executionMarkRemainingSeconds >
                0f

    val isCarriedByHeavyBolt:
        Boolean
        get() =
            carriedByHeavyBolt

    fun beginHeavyBoltCarry() {
        carriedByHeavyBolt =
            true
    }

    fun updateHeavyBoltCarryPosition(
        centerX: Float,
        centerY: Float,
        worldWidth: Float,
        worldHeight: Float
    ) {
        if (!carriedByHeavyBolt) {
            return
        }

        position.x =
            (
                centerX -
                    size / 2f
                )
                .coerceIn(
                    0f,
                    (
                        worldWidth -
                            size
                        )
                        .coerceAtLeast(0f)
                )

        position.y =
            (
                centerY -
                    size / 2f
                )
                .coerceIn(
                    0f,
                    (
                        worldHeight -
                            size
                        )
                        .coerceAtLeast(0f)
                )
    }

    fun endHeavyBoltCarry() {
        carriedByHeavyBolt =
            false
    }

    fun applyCondition(
        application: ConditionApplication
    ) {
        if (isDead) {
            return
        }

        conditions.apply(application)
    }

    fun applyCondition(
        conditionType: ConditionType,
        durationSeconds: Float? = null
    ) {
        if (isDead) {
            return
        }

        conditions.apply(
            type = conditionType,
            durationSeconds = durationSeconds
        )
    }

    fun consumeConditionDamageEvents():
        List<EnemyConditionDamageEvent> {

        return conditionRuntime
            .consumeDamageEvents()
    }

    fun hasCondition(
        conditionType: ConditionType
    ): Boolean {

        return conditions.contains(
            conditionType
        )
    }

    fun removeCondition(
        conditionType: ConditionType
    ): Boolean {

        return conditions.remove(
            conditionType
        )
    }

    fun pullToward(
        targetCenterX: Float,
        targetCenterY: Float,
        pullSpeedPixelsPerSecond: Float,
        delta: Float,
        worldWidth: Float,
        worldHeight: Float
    ) {
        if (
            isDead ||
            carriedByHeavyBolt ||
            pullSpeedPixelsPerSecond <=
            0f ||
            delta <=
            0f
        ) {
            return
        }

        val direction =
            Vector2(
                targetCenterX -
                    centerX,

                targetCenterY -
                    centerY
            )

        if (
            direction.isZero(
                0.001f
            )
        ) {
            return
        }

        val remainingDistance =
            direction.len()

        val requestedMovement =
            (
                pullSpeedPixelsPerSecond *
                    delta
                )
                .coerceAtMost(
                    remainingDistance
                )

        direction.nor()

        position.mulAdd(
            direction,
            requestedMovement
        )

        /*
         * Vortex hareketi normal knockback ile kavga etmesin.
         * Alan düşmanı çektiğinde mevcut knockback momentumu
         * temizlenir.
         */
        knockbackVelocity.setZero()

        knockbackTimeRemaining =
            0f

        keepInsideWorld(
            worldWidth =
                worldWidth,

            worldHeight =
                worldHeight
        )
    }

    fun applyKnockback(
        directionX: Float,
        directionY: Float,
        strength: Float
    ) {
        if (
            isDead ||
            strength <= 0f
        ) {
            return
        }

        val direction =
            Vector2(
                directionX,
                directionY
            )

        if (direction.isZero) {
            return
        }

        direction.nor()

        val impulseSpeed =
            strength *
                12f *
                knockbackResistance

        /*
         * Eski hızın üzerine eklemek yerine
         * bu vuruşun kuvvetini doğrudan uygular.
         *
         * Böylece hızlı veya eş zamanlı darbeler
         * knockback'i 5–10 katına çıkarmaz.
         */
        knockbackVelocity
            .set(direction)
            .scl(impulseSpeed)
            .limit(900f)

        knockbackTimeRemaining = 0.18f
    }

    fun render(shapeRenderer: ShapeRenderer) {
        if (isDead) {
            renderExecutionMark(
                shapeRenderer
            )

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

        if (isBurned) {
            renderBurnedEffect(
                shapeRenderer = shapeRenderer
            )
        }

        renderHealthBar(
            shapeRenderer = shapeRenderer
        )
    }

    private fun renderBurnedEffect(
        shapeRenderer: ShapeRenderer
    ) {
        val borderThickness =
            3f

        shapeRenderer.color =
            Color.ORANGE

        /*
         * Alt kenar
         */
        shapeRenderer.rect(
            position.x,
            position.y,
            size,
            borderThickness
        )

        /*
         * Üst kenar
         */
        shapeRenderer.rect(
            position.x,
            position.y +
                size -
                borderThickness,
            size,
            borderThickness
        )

        /*
         * Sol kenar
         */
        shapeRenderer.rect(
            position.x,
            position.y,
            borderThickness,
            size
        )

        /*
         * Sağ kenar
         */
        shapeRenderer.rect(
            position.x +
                size -
                borderThickness,
            position.y,
            borderThickness,
            size
        )
    }

    private fun renderExecutionMark(
        shapeRenderer: ShapeRenderer
    ) {
        if (
            executionMarkRemainingSeconds <=
            0f
        ) {
            return
        }

        val padding =
            size * 0.18f

        val minimumX =
            position.x +
                padding

        val minimumY =
            position.y +
                padding

        val maximumX =
            position.x +
                size -
                padding

        val maximumY =
            position.y +
                size -
                padding

        shapeRenderer.color =
            Color.RED

        shapeRenderer.rectLine(
            minimumX,
            minimumY,
            maximumX,
            maximumY,
            4f
        )

        shapeRenderer.rectLine(
            minimumX,
            maximumY,
            maximumX,
            minimumY,
            4f
        )
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
