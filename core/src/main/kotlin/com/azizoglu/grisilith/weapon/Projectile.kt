package com.azizoglu.grisilith.weapon

import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.enemy.Enemy
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

class Projectile(
    startX: Float,
    startY: Float,
    targetX: Float,
    targetY: Float,

    val sourceWeaponName: String,

    private val damage: Int,

    val isCriticalHit: Boolean,

    maximumTargets: Int,

    projectileSpeed: Float,

    maximumTravelDistance: Float,

    val damageType: DamageType,

    val knockback: Float = 0f,

    private val damageMultiplierAfterFirstHit:
    Float = 1f,

    ricochetCount: Int = 0,

    private val ricochetRange: Float = 0f,

    private val travelDistanceAfterFirstHit:
    Float? = null
) {

    private val position =
        Vector2(
            startX,
            startY
        )

    private val velocity =
        Vector2(
            targetX - startX,
            targetY - startY
        )

    private val size =
        if (isCriticalHit) {
            15f
        } else {
            11f
        }

    private val collisionBounds =
        Rectangle()

    private val safeProjectileSpeed =
        projectileSpeed
            .coerceAtLeast(1f)

    private val safeMaximumTravelDistance =
        maximumTravelDistance
            .coerceAtLeast(1f)

    private var travelledDistance =
        0f

    private var remainingTargets =
        maximumTargets
            .coerceAtLeast(1)

    private val hitEnemies =
        mutableSetOf<Enemy>()

    private var successfulHitCount =
        0

    private var remainingRicochets =
        ricochetCount.coerceAtLeast(0)

    private var currentMaximumTravelDistance =
        safeMaximumTravelDistance

    var isActive: Boolean = true
        private set

    init {
        if (velocity.isZero) {
            velocity.set(
                1f,
                0f
            )
        }

        velocity
            .nor()
            .scl(
                safeProjectileSpeed
            )
    }

    fun update(
        delta: Float,
        worldWidth: Float,
        worldHeight: Float
    ) {
        if (
            !isActive ||
            delta <= 0f
        ) {
            return
        }

        val movementX =
            velocity.x * delta

        val movementY =
            velocity.y * delta

        position.add(
            movementX,
            movementY
        )

        travelledDistance +=
            Vector2.len(
                movementX,
                movementY
            )

        if (
            travelledDistance >=
            currentMaximumTravelDistance
        ) {
            isActive = false
            return
        }

        val outsideWorld =
            position.x + size < 0f ||
                position.y + size < 0f ||
                position.x > worldWidth ||
                position.y > worldHeight

        if (outsideWorld) {
            isActive = false
        }
    }

    fun checkCollision(
        enemies: List<Enemy>,
        onDamageDealt: (Int) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
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
                enemy in hitEnemies ||
                !collisionBounds.overlaps(
                    enemy.bounds
                )
            ) {
                continue
            }

            hitEnemies.add(enemy)

            val wasAliveBeforeHit =
                !enemy.isDead

            val hitDamage =
                if (successfulHitCount == 0) {
                    damage
                } else {
                    (
                        damage *
                            damageMultiplierAfterFirstHit
                        )
                        .toInt()
                        .coerceAtLeast(1)
                }

            val appliedDamage =
                enemy.takeDamage(
                    hitDamage
                )

            if (appliedDamage > 0) {
                onDamageDealt(
                    appliedDamage
                )

                enemy.applyKnockback(
                    directionX =
                        velocity.x,

                    directionY =
                        velocity.y,

                    strength =
                        knockback
                )
            }

            if (
                wasAliveBeforeHit &&
                enemy.isDead
            ) {
                onEnemyKilled(enemy)
            }

            successfulHitCount++
            remainingTargets--

            if (
                remainingRicochets > 0 &&
                remainingTargets > 0
            ) {
                val ricochetTarget =
                    findNearestRicochetTarget(
                        fromEnemy = enemy,
                        enemies = enemies
                    )

                if (ricochetTarget != null) {
                    redirectToward(
                        targetX =
                            ricochetTarget.centerX,

                        targetY =
                            ricochetTarget.centerY
                    )

                    remainingRicochets--

                    travelledDistance = 0f

                    currentMaximumTravelDistance =
                        ricochetRange
                            .coerceAtLeast(1f)

                    return
                }

                remainingRicochets = 0
                isActive = false
                return
            }

            if (
                successfulHitCount == 1 &&
                travelDistanceAfterFirstHit != null &&
                remainingTargets > 0
            ) {
                travelledDistance = 0f

                currentMaximumTravelDistance =
                    travelDistanceAfterFirstHit
                        .coerceAtLeast(1f)
            }

            if (remainingTargets <= 0) {
                isActive = false
            }

            return
        }
    }

    private fun findNearestRicochetTarget(
        fromEnemy: Enemy,
        enemies: List<Enemy>
    ): Enemy? {
        if (ricochetRange <= 0f) {
            return null
        }

        return enemies
            .asSequence()
            .filter { enemy ->
                !enemy.isDead &&
                    enemy !== fromEnemy &&
                    enemy !in hitEnemies
            }
            .map { enemy ->
                enemy to
                    Vector2.dst2(
                        fromEnemy.centerX,
                        fromEnemy.centerY,
                        enemy.centerX,
                        enemy.centerY
                    )
            }
            .filter { (_, distanceSquared) ->
                distanceSquared <=
                    ricochetRange *
                    ricochetRange
            }
            .minByOrNull { (_, distanceSquared) ->
                distanceSquared
            }
            ?.first
    }

    private fun redirectToward(
        targetX: Float,
        targetY: Float
    ) {
        velocity.set(
            targetX - position.x,
            targetY - position.y
        )

        if (velocity.isZero) {
            velocity.set(
                1f,
                0f
            )
        }

        velocity
            .nor()
            .scl(
                safeProjectileSpeed
            )
    }

    fun render(
        shapeRenderer: ShapeRenderer
    ) {
        if (!isActive) {
            return
        }

        shapeRenderer.color =
            projectileColor()

        shapeRenderer.rect(
            position.x,
            position.y,
            size,
            size
        )
    }

    private fun projectileColor(): Color {
        if (isCriticalHit) {
            return Color.GOLD
        }

        return when (damageType) {
            DamageType.NONE ->
                Color.LIGHT_GRAY

            DamageType.SLASHING ->
                Color.WHITE

            DamageType.PIERCING ->
                Color.LIGHT_GRAY

            DamageType.BLUDGEONING ->
                Color.BROWN

            DamageType.AIR ->
                Color.CYAN

            DamageType.WATER ->
                Color.BLUE

            DamageType.FIRE ->
                Color.ORANGE

            DamageType.EARTH ->
                Color.BROWN

            DamageType.POISON ->
                Color.GREEN

            DamageType.ACID ->
                Color.LIME

            DamageType.LIGHTNING ->
                Color.YELLOW

            DamageType.NECROTIC ->
                Color.PURPLE

            DamageType.COLD ->
                Color.SKY
        }
    }
}
