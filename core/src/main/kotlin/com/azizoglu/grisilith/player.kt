package com.azizoglu.grisilith.player

import com.azizoglu.grisilith.combat.HealthComponent
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatType
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.MathUtils
import kotlin.math.roundToInt

class Player(
    startX: Float,
    startY: Float,
    private val worldWidth: Float,
    private val worldHeight: Float,
    val stats: PlayerStats
) {
    private val position =
        Vector2(startX, startY)

    private val previousPosition =
        Vector2(startX, startY)

    private val movementDirection =
        Vector2()

    private val chargerMovementDirection =
        Vector2()

    private var chargerMovementSpeedMultiplier =
        1f

    private var chargerSteeringResponse =
        0f

    private var isChargerMovementActive =
        false

    private var forceStopThisFrame =
        false

    var actualMovementSpeed:
        Float =
        0f
        private set

    private val size = 48f

    private val collisionBounds =
        Rectangle()

    private var accumulatedRegeneration = 0f

    val health =
        HealthComponent(
            maximumHealth =
                stats.calculatedMaximumHealth()
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

    fun update(
        delta:
        Float
    ) {
        if (health.isDead) {
            actualMovementSpeed =
                0f

            return
        }

        val safeDelta =
            delta.coerceAtLeast(
                0f
            )

        previousPosition.set(
            position
        )

        readMovementInput()

        if (forceStopThisFrame) {
            /*
             * Charger başarıyla bittiğinde tek frame
             * boyunca oyuncuyu anlık olarak durdurur.
             */
            forceStopThisFrame =
                false

            movementDirection
                .setZero()

            chargerMovementDirection
                .setZero()
        } else if (
            isChargerMovementActive
        ) {
            updateChargerMovement(
                delta =
                    safeDelta
            )
        } else {
            updateNormalMovement(
                delta =
                    safeDelta
            )
        }

        keepInsideWorld()

        updateActualMovementSpeed(
            delta =
                safeDelta
        )
    }

    private fun updateNormalMovement(
        delta:
        Float
    ) {
        if (
            movementDirection
                .isZero
        ) {
            return
        }

        movementDirection
            .nor()

        position.mulAdd(
            movementDirection,
            calculatedMovementSpeed() *
                delta
        )
    }

    private fun updateChargerMovement(
        delta:
        Float
    ) {
        val desiredDirection =
            movementDirection

        /*
         * Hiç input yoksa mevcut momentum yönü korunur.
         * Charger kendi kendine ileri akmaya devam eder.
         */
        if (
            !desiredDirection
                .isZero
        ) {
            desiredDirection
                .nor()

            val interpolationAmount =
                (
                    chargerSteeringResponse *
                        delta
                    )
                    .coerceIn(
                        0f,
                        1f
                    )

            if (
                chargerMovementDirection
                    .isZero
            ) {
                chargerMovementDirection.set(
                    desiredDirection
                )
            } else {
                chargerMovementDirection
                    .lerp(
                        desiredDirection,
                        interpolationAmount
                    )
            }
        }

        /*
         * Zıt input verilirse lerp sırasında vektörün
         * uzunluğu küçülür. Böylece karakter yavaşlar.
         *
         * Ancak sırf ters input verildi diye Charger
         * runtime'ı burada doğrudan iptal edilmez.
         */
        val momentumStrength =
            chargerMovementDirection
                .len()
                .coerceIn(
                    0f,
                    1f
                )

        if (
            momentumStrength <=
            0.001f
        ) {
            return
        }

        val normalizedDirection =
            Vector2(
                chargerMovementDirection
            )
                .nor()

        position.mulAdd(
            normalizedDirection,

            calculatedMovementSpeed() *
                chargerMovementSpeedMultiplier *
                momentumStrength *
                delta
        )
    }

    private fun calculatedMovementSpeed(): Float {
        return stats.calculatedMovementSpeed()
    }

    private fun updateActualMovementSpeed(
        delta:
        Float
    ) {
        if (
            delta <=
            0f
        ) {
            actualMovementSpeed =
                0f

            return
        }

        actualMovementSpeed =
            position.dst(
                previousPosition
            ) /
                delta
    }

    private fun updateHealthRegeneration(
        delta: Float
    ) {
        if (
            delta <= 0f ||
            health.currentHealth >=
            health.maximumHealth
        ) {
            return
        }

        val regenerationPerSecond =
            stats.hpRegenerationPerSecond()

        if (regenerationPerSecond <= 0f) {
            return
        }

        accumulatedRegeneration +=
            regenerationPerSecond * delta

        val wholeHealthPoints =
            accumulatedRegeneration.toInt()

        if (wholeHealthPoints <= 0) {
            return
        }

        val healedAmount =
            health.heal(
                wholeHealthPoints
            )

        if (healedAmount > 0) {
            accumulatedRegeneration -=
                healedAmount.toFloat()
        }
    }

    fun addRunStatBonus(
        statType: StatType,
        amount: Int
    ): Int {
        val previousMaximumHealth =
            stats.calculatedMaximumHealth()

        val appliedIncrease =
            stats.addRunBonus(
                statType = statType,
                amount = amount
            )

        if (
            statType == StatType.MAX_HP &&
            appliedIncrease > 0
        ) {
            val newMaximumHealth =
                stats.calculatedMaximumHealth()

            val healthIncrease =
                newMaximumHealth -
                    previousMaximumHealth

            health.increaseMaximumHealth(
                amount = healthIncrease,
                healAddedHealth = true
            )
        }

        return appliedIncrease
    }

    fun synchronizeMaximumHealthWithStats(
        healAddedHealth: Boolean = true
    ) {
        val calculatedMaximumHealth =
            stats.calculatedMaximumHealth()

        if (
            health.maximumHealth ==
            calculatedMaximumHealth
        ) {
            return
        }

        health.setMaximumHealth(
            newMaximumHealth =
                calculatedMaximumHealth,

            healAddedHealth =
                healAddedHealth
        )
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

    fun beginChargerMovement(
        steeringResponse:
        Float
    ) {
        isChargerMovementActive =
            true

        chargerSteeringResponse =
            steeringResponse
                .coerceAtLeast(
                    0.01f
                )

        /*
         * Charger açıldığı anda mevcut input yönünden
         * başlarız. Böylece yön sıfırdan oluşmaz.
         */
        chargerMovementDirection.set(
            movementDirection
        )

        if (
            chargerMovementDirection
                .isZero
        ) {
            readMovementInputInto(
                chargerMovementDirection
            )
        }

        if (
            !chargerMovementDirection
                .isZero
        ) {
            chargerMovementDirection
                .nor()
        }
    }

    fun setChargerMovementSpeedMultiplier(
        multiplier:
        Float
    ) {
        chargerMovementSpeedMultiplier =
            multiplier
                .coerceAtLeast(
                    0f
                )
    }

    fun endChargerMovement(
        stopImmediately:
        Boolean
    ) {
        isChargerMovementActive =
            false

        chargerMovementSpeedMultiplier =
            1f

        chargerSteeringResponse =
            0f

        chargerMovementDirection
            .setZero()

        if (stopImmediately) {
            forceStopThisFrame =
                true
        }
    }

    fun takeDamage(amount: Int): Int {
        if (amount <= 0) {
            return 0
        }

        val reducedDamage =
            (
                amount *
                    stats.armorDamageMultiplier()
                )
                .roundToInt()
                .coerceAtLeast(1)

        return health.takeDamage(
            reducedDamage
        )
    }

    fun tryLifeSteal(
        appliedDamage: Int
    ) {
        if (
            appliedDamage <= 0 ||
            health.isDead ||
            health.currentHealth >=
            health.maximumHealth
        ) {
            return
        }

        val lifeStealChance =
            stats.lifeStealChance()
                .coerceIn(
                    0f,
                    1f
                )

        if (
            lifeStealChance <= 0f ||
            !MathUtils.randomBoolean(
                lifeStealChance
            )
        ) {
            return
        }

        health.heal(1)
    }

    fun render(
        shapeRenderer: ShapeRenderer
    ) {
        renderDefensiveLineGlow(
            shapeRenderer
        )

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
        readMovementInputInto(
            movementDirection
        )
    }

    private fun readMovementInputInto(
        target:
        Vector2
    ) {
        target.setZero()

        if (
            Gdx.input.isKeyPressed(
                Input.Keys.W
            ) ||
            Gdx.input.isKeyPressed(
                Input.Keys.UP
            )
        ) {
            target.y +=
                1f
        }

        if (
            Gdx.input.isKeyPressed(
                Input.Keys.S
            ) ||
            Gdx.input.isKeyPressed(
                Input.Keys.DOWN
            )
        ) {
            target.y -=
                1f
        }

        if (
            Gdx.input.isKeyPressed(
                Input.Keys.A
            ) ||
            Gdx.input.isKeyPressed(
                Input.Keys.LEFT
            )
        ) {
            target.x -=
                1f
        }

        if (
            Gdx.input.isKeyPressed(
                Input.Keys.D
            ) ||
            Gdx.input.isKeyPressed(
                Input.Keys.RIGHT
            )
        ) {
            target.x +=
                1f
        }
    }

    fun teleportCenterTo(
        targetCenterX: Float,
        targetCenterY: Float
    ) {
        position.x =
            (
                targetCenterX -
                    size / 2f
                )
                .coerceIn(
                    0f,
                    (
                        worldWidth -
                            size
                        )
                        .coerceAtLeast(
                            0f
                        )
                )

        position.y =
            (
                targetCenterY -
                    size / 2f
                )
                .coerceIn(
                    0f,
                    (
                        worldHeight -
                            size
                        )
                        .coerceAtLeast(
                            0f
                        )
                )

        previousPosition.set(
            position
        )

        actualMovementSpeed =
            0f
    }

    fun finalizeMovementAfterCollisions(
        delta:
        Float
    ) {
        updateActualMovementSpeed(
            delta =
                delta.coerceAtLeast(
                    0f
                )
        )
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


    private fun renderDefensiveLineGlow(
        shapeRenderer: ShapeRenderer
    ) {
        val currentBonus =
            stats.weaponSkillBonus(
                StatType.ARMOR_CLASS
            )

        if (currentBonus <= 0) {
            return
        }

        val maximumBonus =
            stats.weaponSkillBonusLimit(
                StatType.ARMOR_CLASS
            )

        val isMaximum =
            maximumBonus > 0 &&
                currentBonus >= maximumBonus

        shapeRenderer.color =
            if (isMaximum) {
                Color(
                    0.48f,
                    0.48f,
                    0.52f,
                    0.90f
                )
            } else {
                Color(
                    0.78f,
                    0.78f,
                    0.82f,
                    0.72f
                )
            }

        val padding =
            if (isMaximum) 7f else 5f

        val thickness =
            if (isMaximum) 4f else 3f

        shapeRenderer.rect(
            position.x - padding,
            position.y - padding,
            size + padding * 2f,
            thickness
        )

        shapeRenderer.rect(
            position.x - padding,
            position.y + size + padding - thickness,
            size + padding * 2f,
            thickness
        )

        shapeRenderer.rect(
            position.x - padding,
            position.y - padding,
            thickness,
            size + padding * 2f
        )

        shapeRenderer.rect(
            position.x + size + padding - thickness,
            position.y - padding,
            thickness,
            size + padding * 2f
        )
    }

}
