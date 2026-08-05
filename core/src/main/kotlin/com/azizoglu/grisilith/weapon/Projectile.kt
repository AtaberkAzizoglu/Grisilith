package com.azizoglu.grisilith.weapon

import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyDamageContext
import com.azizoglu.grisilith.combat.condition.ConditionApplication
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.azizoglu.grisilith.weapon.runtime.WeaponHitEvent
import com.azizoglu.grisilith.weapon.runtime.WeaponInstance
import com.azizoglu.grisilith.enemy.EnemyType

class Projectile(
    startX: Float,
    startY: Float,
    targetX: Float,
    targetY: Float,

    val sourceWeaponName: String,

    val sourceWeapon:
    WeaponInstance? = null,

    val sourceWeaponSlotIndex:
    Int = -1,

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
    Float? = null,

    private val ignoresEnemyCollisions:
    Boolean = false,

    wallBounceCount: Int = 0,

    sizeMultiplier: Float = 1f,

    private val activationDelaySeconds:
    Float = 0f,

    private val isHeavyBolt:
    Boolean = false,

    private val firstTargetBossDamageMultiplier:
    Float = 1f,

    private val carryFirstNormalTarget:
    Boolean = false,

    private val conditionApplication:
    ConditionApplication? = null,

    private val armorClassReduction:
    Int = 0,

    private val armorClassReductionDurationSeconds:
    Float = 0f
) {

    private val position =
        Vector2(
            startX,
            startY
        )

    private var activationDelayRemaining =
        activationDelaySeconds
            .coerceAtLeast(
                0f
            )

    private var carriedEnemy:
        Enemy? =
        null

    private val velocity =
        Vector2(
            targetX - startX,
            targetY - startY
        )

    private val size =
        (
            if (isCriticalHit) {
                15f
            } else {
                11f
            }
            ) *
            sizeMultiplier
                .coerceAtLeast(
                    0.1f
                )

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

    private var remainingWallBounces =
        wallBounceCount.coerceAtLeast(0)

    private var remainingTargets =
        maximumTargets
            .coerceAtLeast(1)


    private val queuedRicochetTargets =
        mutableListOf<Enemy>()

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

        if (
            activationDelayRemaining >
            0f
        ) {
            activationDelayRemaining =
                (
                    activationDelayRemaining -
                        delta
                    )
                    .coerceAtLeast(
                        0f
                    )

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

        carriedEnemy
            ?.let { enemy ->
                if (enemy.isDead) {
                    releaseCarriedEnemy()
                } else {
                    enemy.updateHeavyBoltCarryPosition(
                        centerX =
                            position.x +
                                size / 2f,

                        centerY =
                            position.y +
                                size / 2f,

                        worldWidth =
                            worldWidth,

                        worldHeight =
                            worldHeight
                    )
                }
            }

        travelledDistance +=
            Vector2.len(
                movementX,
                movementY
            )

        if (
            travelledDistance >=
            currentMaximumTravelDistance
        ) {
            deactivate()
            return
        }

        resolveWorldBoundaryCollision(
            worldWidth = worldWidth,
            worldHeight = worldHeight
        )
    }

    private fun resolveWorldBoundaryCollision(
        worldWidth: Float,
        worldHeight: Float
    ) {
        if (!isActive) {
            return
        }

        var collidedHorizontally =
            false

        var collidedVertically =
            false

        if (position.x < 0f) {
            position.x = 0f
            collidedHorizontally = true
        }

        if (
            position.x + size >
            worldWidth
        ) {
            position.x =
                worldWidth - size

            collidedHorizontally = true
        }

        if (position.y < 0f) {
            position.y = 0f
            collidedVertically = true
        }

        if (
            position.y + size >
            worldHeight
        ) {
            position.y =
                worldHeight - size

            collidedVertically = true
        }

        if (
            !collidedHorizontally &&
            !collidedVertically
        ) {
            return
        }

        if (remainingWallBounces <= 0) {
            deactivate()
            return
        }

        if (collidedHorizontally) {
            velocity.x =
                -velocity.x
        }

        if (collidedVertically) {
            velocity.y =
                -velocity.y
        }

        remainingWallBounces--

        travelledDistance = 0f
    }

    fun bendToward(
        targetCenterX: Float,
        targetCenterY: Float,
        influenceRadius: Float,
        turnStrengthPerSecond: Float,
        delta: Float
    ) {

        if (
            !isActive ||
            activationDelayRemaining >
            0f ||
            influenceRadius <=
            0f ||
            turnStrengthPerSecond <=
            0f ||
            delta <=
            0f
        ) {
            return
        }

        val projectileCenterX =
            position.x +
                size /
                2f

        val projectileCenterY =
            position.y +
                size /
                2f

        val distance =
            Vector2.dst(
                projectileCenterX,
                projectileCenterY,
                targetCenterX,
                targetCenterY
            )

        if (
            distance >
            influenceRadius ||
            distance <=
            1f
        ) {
            return
        }

        val desiredDirection =
            Vector2(
                targetCenterX -
                    projectileCenterX,

                targetCenterY -
                    projectileCenterY
            )

        if (
            desiredDirection.isZero(
                0.001f
            )
        ) {
            return
        }

        desiredDirection
            .nor()
            .scl(
                safeProjectileSpeed
            )

        val influence =
            (
                1f -
                    distance /
                    influenceRadius
                )
                .coerceIn(
                    0f,
                    1f
                )

        velocity.lerp(
            desiredDirection,
            (
                turnStrengthPerSecond *
                    influence *
                    delta
                )
                .coerceIn(
                    0f,
                    0.18f
                )
        )

        if (
            !velocity.isZero(
                0.001f
            )
        ) {
            velocity
                .nor()
                .scl(
                    safeProjectileSpeed
                )
        }
    }

    fun checkCollision(
        enemies: List<Enemy>,
        onDamageDealt: (Int) -> Unit,
        onEnemyKilled: (Enemy) -> Unit,
        onWeaponHit: (WeaponHitEvent) -> Unit,
        modifyWeaponDamage: (
            enemy: Enemy,
            baseDamage: Int
        ) -> Int
    ) {
        if (
            !isActive ||
            ignoresEnemyCollisions ||
            activationDelayRemaining >
            0f
        ) {
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

            val isFirstTarget =
                successfulHitCount ==
                    0

            val isFirstTargetBoss =
                isFirstTarget &&
                    enemy.type in
                    setOf(
                        EnemyType.MINI_BOSS,
                        EnemyType.FINAL_BOSS
                    )

            val hitDamage =
                if (isFirstTarget) {
                    if (
                        isHeavyBolt &&
                        isFirstTargetBoss
                    ) {
                        (
                            damage *
                                firstTargetBossDamageMultiplier
                            )
                            .toInt()
                            .coerceAtLeast(1)
                    } else {
                        damage
                    }
                } else {
                    (
                        damage *
                            damageMultiplierAfterFirstHit
                        )
                        .toInt()
                        .coerceAtLeast(1)
                }

            val modifiedHitDamage =
                if (
                    sourceWeapon != null &&
                    sourceWeaponSlotIndex >= 0
                ) {
                    modifyWeaponDamage(
                        enemy,
                        hitDamage
                    )
                        .coerceAtLeast(1)
                } else {
                    /*
                     * Hasarsız veya yalnızca görsel amaçlı
                     * projectile'larda Tamga çalıştırılmaz.
                     */
                    hitDamage
                }

            val appliedDamage =
                enemy.takeDamage(
                    amount = modifiedHitDamage,
                    damageContext =
                        EnemyDamageContext(
                            damageType = damageType,
                            sourceWeaponRuntimeId =
                                sourceWeapon?.runtimeId,
                            sourceWeaponDefinitionId =
                                sourceWeapon?.definition?.id?.value,
                            sourceName = sourceWeaponName
                        )
                )

            if (appliedDamage > 0) {
                conditionApplication
                    ?.let { application ->
                        enemy.applyCondition(
                            application
                        )
                    }

                if (
                    armorClassReduction >
                    0 &&
                    armorClassReductionDurationSeconds >
                    0f
                ) {
                    enemy.applyTemporaryArmorClassReduction(
                        amount =
                            armorClassReduction,

                        durationSeconds =
                            armorClassReductionDurationSeconds
                    )
                }

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
                isHeavyBolt &&
                isFirstTarget &&
                carryFirstNormalTarget &&
                !enemy.isDead &&
                enemy.type in
                setOf(
                    EnemyType.NORMAL,
                    EnemyType.UNIQUE
                )
            ) {
                carriedEnemy =
                    enemy

                enemy.beginHeavyBoltCarry()
            }

            if (
                wasAliveBeforeHit &&
                enemy.isDead
            ) {
                onEnemyKilled(enemy)
            }

            val hitSourceWeapon =
                sourceWeapon

            if (
                appliedDamage > 0 &&
                hitSourceWeapon != null &&
                sourceWeaponSlotIndex >= 0
            ) {
                onWeaponHit(
                    WeaponHitEvent(
                        weapon =
                            hitSourceWeapon,

                        weaponSlotIndex =
                            sourceWeaponSlotIndex,

                        enemy =
                            enemy,

                        appliedDamage =
                            appliedDamage,

                        damageType =
                            damageType,

                        isProjectile =
                            true
                    )
                )
            }

            successfulHitCount++

            if (!isHeavyBolt) {
                remainingTargets--
            }

            /*
 * Sekme hedefleri yalnızca ilk isabet
 * gerçekleştiği anda belirlenir.
 */
            if (
                successfulHitCount == 1 &&
                remainingRicochets > 0
            ) {
                queueRicochetTargets(
                    fromEnemy = enemy,
                    enemies = enemies,
                    maximumTargetCount =
                        remainingRicochets
                )

                /*
                 * Hedef seçimi tamamlandıktan sonra
                 * tekrar çevrede arama yapılmaması için
                 * bu değer sıfırlanır.
                 */
                remainingRicochets = 0
            }

            /*
             * Önceden kaydedilmiş sıradaki hedefe geçer.
             */
            if (
                queuedRicochetTargets.isNotEmpty() &&
                remainingTargets > 0
            ) {
                val redirected =
                    redirectToNextQueuedTarget()

                if (redirected) {
                    travelledDistance = 0f

                    currentMaximumTravelDistance =
                        ricochetRange
                            .coerceAtLeast(1f)

                    return
                }
            }

            /*
             * Bu projectile sekmeli bir saldırıysa fakat
             * kaydedilmiş başka hedef kalmadıysa yok olur.
             */
            if (
                ricochetRange > 0f &&
                successfulHitCount >= 1
            ) {
                deactivate()
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
                deactivate()
            }

            return
        }
    }

    fun checkObstacleCollision(
        obstacleBounds:
        List<Rectangle>
    ) {
        if (
            !isActive ||
            activationDelayRemaining >
            0f
        ) {
            return
        }

        collisionBounds.set(
            position.x,
            position.y,
            size,
            size
        )

        if (
            obstacleBounds.any {
                collisionBounds.overlaps(it)
            }
        ) {
            deactivate()
        }
    }

    private fun deactivate() {
        if (!isActive) {
            return
        }

        isActive =
            false

        releaseCarriedEnemy()
    }

    private fun releaseCarriedEnemy() {
        carriedEnemy
            ?.endHeavyBoltCarry()

        carriedEnemy =
            null
    }

    private fun queueRicochetTargets(
        fromEnemy: Enemy,
        enemies: List<Enemy>,
        maximumTargetCount: Int
    ) {
        queuedRicochetTargets.clear()

        if (
            ricochetRange <= 0f ||
            maximumTargetCount <= 0
        ) {
            return
        }

        val maximumDistanceSquared =
            ricochetRange *
                ricochetRange

        val selectedTargets =
            enemies
                .asSequence()
                .filter { candidate ->
                    !candidate.isDead &&
                        candidate !== fromEnemy &&
                        candidate !in hitEnemies
                }
                .map { candidate ->
                    candidate to
                        Vector2.dst2(
                            fromEnemy.centerX,
                            fromEnemy.centerY,
                            candidate.centerX,
                            candidate.centerY
                        )
                }
                .filter { (_, distanceSquared) ->
                    distanceSquared <=
                        maximumDistanceSquared
                }
                .sortedBy { (_, distanceSquared) ->
                    distanceSquared
                }
                .take(
                    maximumTargetCount
                )
                .map { (candidate, _) ->
                    candidate
                }
                .toList()

        queuedRicochetTargets.addAll(
            selectedTargets
        )
    }

    private fun redirectToNextQueuedTarget():
        Boolean {

        while (
            queuedRicochetTargets.isNotEmpty()
        ) {
            val nextTarget =
                queuedRicochetTargets
                    .removeAt(0)

            if (
                nextTarget.isDead ||
                nextTarget in hitEnemies
            ) {
                continue
            }

            redirectToward(
                targetX =
                    nextTarget.centerX,

                targetY =
                    nextTarget.centerY
            )

            return true
        }

        return false
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
        if (
            !isActive ||
            ignoresEnemyCollisions ||
            activationDelayRemaining >
            0f
        ) {
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
        if (isHeavyBolt) {
            return if (isCriticalHit) {
                Color.GOLD
            } else {
                Color(
                    0.60f,
                    0.60f,
                    0.68f,
                    1f
                )
            }
        }

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
