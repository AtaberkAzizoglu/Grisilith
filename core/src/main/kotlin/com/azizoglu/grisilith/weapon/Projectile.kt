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

    /*
     * Bu committe veri olarak taşınıyor.
     * Gerçek itme davranışı sonraki adımda
     * Enemy hareket sistemine bağlanacak.
     */
    val knockback: Float = 0f
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
            safeMaximumTravelDistance
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

            val appliedDamage =
                enemy.takeDamage(
                    damage
                )

            if (appliedDamage > 0) {
                onDamageDealt(
                    appliedDamage
                )
            }

            if (
                wasAliveBeforeHit &&
                enemy.isDead
            ) {
                onEnemyKilled(enemy)
            }

            remainingTargets--

            if (remainingTargets <= 0) {
                isActive = false
            }

            /*
             * Bir frame içinde yalnızca bir yeni
             * düşmana çarpar. Piercing varsa sonraki
             * frame diğer düşmanlara devam edebilir.
             */
            return
        }
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
