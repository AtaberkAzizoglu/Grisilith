package com.azizoglu.grisilith.summon

import com.azizoglu.grisilith.combat.HealthComponent
import com.azizoglu.grisilith.combat.damage.DamageNumberEvent
import com.azizoglu.grisilith.combat.condition.ConditionApplication
import com.azizoglu.grisilith.combat.condition.ConditionContainer
import com.azizoglu.grisilith.content.ConditionType
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.navigation.EnemyNavigationAgent
import com.azizoglu.grisilith.map.MapObstacle
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import java.util.UUID
import com.azizoglu.grisilith.summon.visual.SummonVisualDirection

class SummonedEntity(
    val sourceWeaponRuntimeId: String,
    val sourceWeaponDefinitionId: String,
    val sourceWeaponDisplayName: String,
    val type: SummonType,
    val stats: SummonStats,
    startCenterX: Float,
    startCenterY: Float,
    lifetimeSeconds: Float,
    private val leashCenterX: Float? = null,
    private val leashCenterY: Float? = null,
    private val maximumLeashDistance: Float? = null
) {

    enum class KnightTargetType {
        NONE,
        ENEMY,
        PLAYER
    }

    val runtimeId: String =
        UUID.randomUUID()
            .toString()

    private val center =
        Vector2(
            startCenterX,
            startCenterY
        )

    /*
     * Reused rectangle for Enemy target/contact checks.
     * This is gameplay-only and does not change the summon sprite size.
     */
    private val combatBoundsRectangle =
        Rectangle()

    private val movementDirection =
        Vector2()

    /*
     * Knight uses the same obstacle-aware A* navigation foundation as Enemy.
     * Cow/Imp movement is intentionally left unchanged in this phase.
     */
    private val knightNavigationAgent =
        EnemyNavigationAgent(
            agentSize =
                KNIGHT_NAVIGATION_AGENT_SIZE
        )

    private val navigationOutput =
        Vector2()

    private val visualPhase =
        MathUtils.random(
            0f,
            MathUtils.PI2
        )

    private var attackCooldownRemainingSeconds =
        0f

    private var attackFlashRemainingSeconds =
        0f

    private var guardAttackVisualRemainingSeconds =
        0f

    private val guardAttackVisualTarget =
        Vector2()

    private var guardTarget: Enemy? =
        null

    private var guardRetreatRemainingSeconds =
        0f

    private var previousGuardHealthRatio =
        1f

    private val pendingDamageNumberEvents =
        mutableListOf<DamageNumberEvent>()

    private var terrainMovementSpeedMultiplier =
        1f

    /*
     * Knight target hysteresis.
     * Acquire player at <=300 px; keep player target until >=600 px.
     */
    private var knightPlayerTargetLocked =
        false

    /*
     * Purely visual movement state.
     * Gameplay collision/hitbox is intentionally NOT derived from sprite size.
     */
    var visualDirection:
        SummonVisualDirection =
        SummonVisualDirection.SOUTH
        private set

    var walkAnimationTime: Float =
        0f
        private set

    var isVisuallyMoving: Boolean =
        false
        private set

    val health =
        HealthComponent(
            maximumHealth =
                stats.maximumHealth
        )

    /** Active gameplay conditions, shared with the world condition VFX layer. */
    val conditions =
        ConditionContainer()

    var remainingLifetimeSeconds: Float =
        lifetimeSeconds
            .coerceAtLeast(
                0f
            )
        private set

    val isExpired: Boolean
        get() =
            remainingLifetimeSeconds <=
                0f ||
                health.isDead

    val centerX: Float
        get() =
            center.x

    val centerY: Float
        get() =
            center.y

    val combatRadius: Float
        get() =
            visualRadius()

    val combatBounds: Rectangle
        get() {
            val radius =
                combatRadius

            combatBoundsRectangle.set(
                center.x -
                    radius,
                center.y -
                    radius,
                radius *
                    2f,
                radius *
                    2f
            )

            return combatBoundsRectangle
        }

    val faction: SummonCombatFaction
        get() =
            type.faction

    fun update(
        delta: Float,
        playerCenterX: Float,
        playerCenterY: Float,
        worldWidth: Float,
        worldHeight: Float,
        mapObstacles: List<MapObstacle>,
        enemies: List<Enemy>,
        otherSummons: List<SummonedEntity>,
        onCowAttack: (
            summon: SummonedEntity,
            enemy: Enemy
        ) -> Unit,
        onImpAttack: (
            summon: SummonedEntity,
            enemy: Enemy
        ) -> Unit,
        onToadAttack: (
            summon: SummonedEntity,
            enemy: Enemy
        ) -> Unit,
        onGuardAttack: (
            summon: SummonedEntity,
            enemy: Enemy
        ) -> Unit,
        onKnightEnemyAttack: (
            summon: SummonedEntity,
            enemy: Enemy
        ) -> Unit,
        onCowSummonAttack: (
            summon: SummonedEntity,
            target: SummonedEntity
        ) -> Unit,
        onImpSummonAttack: (
            summon: SummonedEntity,
            target: SummonedEntity
        ) -> Unit,
        onKnightSummonAttack: (
            summon: SummonedEntity,
            target: SummonedEntity
        ) -> Unit,
        onKnightPlayerAttack: (
            summon: SummonedEntity
        ) -> Unit
    ) {

        if (
            isExpired ||
            delta <= 0f
        ) {
            return
        }

        conditions.update(
            delta = delta,
            onTick = { tick ->
                if (tick.source.tickDamage > 0) {
                    takeDamage(tick.source.tickDamage)
                }
            }
        )

        val visualStartX =
            center.x

        val visualStartY =
            center.y

        remainingLifetimeSeconds =
            (
                remainingLifetimeSeconds -
                    delta
                )
                .coerceAtLeast(
                    0f
                )

        attackCooldownRemainingSeconds =
            (
                attackCooldownRemainingSeconds -
                    delta
                )
                .coerceAtLeast(
                    0f
                )

        if (
            type ==
            SummonType.KNIGHT_SPIRIT ||
            type == SummonType.GUARD
        ) {
            knightNavigationAgent
                .update(
                    delta
                )
        }

        attackFlashRemainingSeconds =
            (
                attackFlashRemainingSeconds -
                    delta
                )
                .coerceAtLeast(
                    0f
                )

        guardAttackVisualRemainingSeconds =
            (guardAttackVisualRemainingSeconds - delta)
                .coerceAtLeast(0f)

        guardRetreatRemainingSeconds =
            (guardRetreatRemainingSeconds - delta)
                .coerceAtLeast(0f)

        when (type) {

            SummonType.COW -> {
                updateCow(
                    delta =
                        delta,

                    playerCenterX =
                        playerCenterX,

                    playerCenterY =
                        playerCenterY,

                    enemies =
                        enemies,

                    otherSummons =
                        otherSummons,

                    onEnemyAttack =
                        onCowAttack,

                    onSummonAttack =
                        onCowSummonAttack
                )
            }

            SummonType.IMP -> {
                updateImp(
                    delta =
                        delta,

                    playerCenterX =
                        playerCenterX,

                    playerCenterY =
                        playerCenterY,

                    enemies =
                        enemies,

                    otherSummons =
                        otherSummons,

                    onEnemyAttack =
                        onImpAttack,

                    onSummonAttack =
                        onImpSummonAttack
                )
            }

            SummonType.KNIGHT_SPIRIT -> {
                updateKnight(
                    delta =
                        delta,

                    playerCenterX =
                        playerCenterX,

                    playerCenterY =
                        playerCenterY,

                    worldWidth =
                        worldWidth,

                    worldHeight =
                        worldHeight,

                    mapObstacles =
                        mapObstacles,

                    enemies =
                        enemies,

                    otherSummons =
                        otherSummons,

                    onEnemyAttack =
                        onKnightEnemyAttack,

                    onSummonAttack =
                        onKnightSummonAttack,

                    onPlayerAttack =
                        onKnightPlayerAttack
                )
            }

            SummonType.TOAD -> {
                updateImp(
                    delta = delta,
                    playerCenterX = playerCenterX,
                    playerCenterY = playerCenterY,
                    enemies = enemies,
                    otherSummons = otherSummons,
                    onEnemyAttack = onToadAttack,
                    onSummonAttack = onImpSummonAttack
                )
            }

            SummonType.GUARD -> {
                updateGuard(
                    delta = delta,
                    worldWidth = worldWidth,
                    worldHeight = worldHeight,
                    mapObstacles = mapObstacles,
                    enemies = enemies,
                    onEnemyAttack = onGuardAttack
                )
            }
        }

        constrainToLeash()

        keepInsideWorld(
            worldWidth =
                worldWidth,

            worldHeight =
                worldHeight
        )

        updateVisualMovementState(
            delta =
                delta,

            previousX =
                visualStartX,

            previousY =
                visualStartY
        )
    }

    fun applyCondition(application: ConditionApplication) {
        if (!isExpired) conditions.apply(application)
    }

    fun applyCondition(type: ConditionType, durationSeconds: Float? = null) {
        if (!isExpired) conditions.apply(type, durationSeconds)
    }

    fun hasCondition(type: ConditionType): Boolean = conditions.contains(type)

    fun removeCondition(type: ConditionType): Boolean = conditions.remove(type)

    fun setTerrainMovementSpeedMultiplier(multiplier: Float) {
        terrainMovementSpeedMultiplier =
            multiplier.coerceIn(0f, 1f)
    }

    fun takeDamage(
        amount: Int
    ): Int {

        if (
            amount <= 0 ||
            isExpired
        ) {
            return 0
        }

        val appliedDamage =
            health.takeDamage(amount)

        if (appliedDamage > 0) {
            pendingDamageNumberEvents +=
                DamageNumberEvent(
                    amount = appliedDamage,
                    worldX = center.x,
                    worldY = center.y + combatRadius
                )
        }

        return appliedDamage
    }

    fun heal(amount: Int): Int =
        health.heal(amount)

    fun consumeDamageNumberEvents(): List<DamageNumberEvent> {
        if (pendingDamageNumberEvents.isEmpty()) {
            return emptyList()
        }

        val result = pendingDamageNumberEvents.toList()
        pendingDamageNumberEvents.clear()
        return result
    }

    fun resolveObstacleCollision(obstacleBounds: Rectangle) {
        if (type == SummonType.IMP || !combatBounds.overlaps(obstacleBounds)) return
        val bounds = combatBounds
        val left = bounds.x + bounds.width - obstacleBounds.x
        val right = obstacleBounds.x + obstacleBounds.width - bounds.x
        val bottom = bounds.y + bounds.height - obstacleBounds.y
        val top = obstacleBounds.y + obstacleBounds.height - bounds.y
        if (minOf(left, right) < minOf(bottom, top)) {
            center.x += if (center.x < obstacleBounds.x + obstacleBounds.width / 2f) -left else right
        } else {
            center.y += if (center.y < obstacleBounds.y + obstacleBounds.height / 2f) -bottom else top
        }
    }

    fun separateFromPlayer(
        playerBounds: Rectangle
    ) {
        if (isExpired || !combatBounds.overlaps(playerBounds)) {
            return
        }

        moveOutside(
            ownBounds = combatBounds,
            otherBounds = playerBounds,
            ownCenterX = center.x,
            ownCenterY = center.y,
            moveFraction = 1f
        )
    }

    fun separateFrom(
        other: SummonedEntity
    ) {
        if (
            other === this ||
            isExpired ||
            other.isExpired
        ) {
            return
        }

        val ownBounds = combatBounds
        val otherBounds = other.combatBounds

        if (!ownBounds.overlaps(otherBounds)) {
            return
        }

        val overlapLeft =
            ownBounds.x + ownBounds.width - otherBounds.x
        val overlapRight =
            otherBounds.x + otherBounds.width - ownBounds.x
        val overlapBottom =
            ownBounds.y + ownBounds.height - otherBounds.y
        val overlapTop =
            otherBounds.y + otherBounds.height - ownBounds.y
        val horizontalOverlap = minOf(overlapLeft, overlapRight)
        val verticalOverlap = minOf(overlapBottom, overlapTop)

        if (horizontalOverlap < verticalOverlap) {
            val direction =
                if (
                    center.x < other.center.x ||
                    center.x == other.center.x && runtimeId < other.runtimeId
                ) {
                    -1f
                } else {
                    1f
                }
            val push = horizontalOverlap / 2f + COLLISION_EPSILON
            center.x += direction * push
            other.center.x -= direction * push
        } else {
            val direction =
                if (
                    center.y < other.center.y ||
                    center.y == other.center.y && runtimeId < other.runtimeId
                ) {
                    -1f
                } else {
                    1f
                }
            val push = verticalOverlap / 2f + COLLISION_EPSILON
            center.y += direction * push
            other.center.y -= direction * push
        }
    }

    private fun moveOutside(
        ownBounds: Rectangle,
        otherBounds: Rectangle,
        ownCenterX: Float,
        ownCenterY: Float,
        moveFraction: Float
    ) {
        val overlapLeft =
            ownBounds.x + ownBounds.width - otherBounds.x
        val overlapRight =
            otherBounds.x + otherBounds.width - ownBounds.x
        val overlapBottom =
            ownBounds.y + ownBounds.height - otherBounds.y
        val overlapTop =
            otherBounds.y + otherBounds.height - ownBounds.y
        val horizontalOverlap = minOf(overlapLeft, overlapRight)
        val verticalOverlap = minOf(overlapBottom, overlapTop)

        if (horizontalOverlap < verticalOverlap) {
            val direction =
                if (ownCenterX < otherBounds.x + otherBounds.width / 2f) -1f else 1f
            center.x +=
                direction *
                    (horizontalOverlap * moveFraction + COLLISION_EPSILON)
        } else {
            val direction =
                if (ownCenterY < otherBounds.y + otherBounds.height / 2f) -1f else 1f
            center.y +=
                direction *
                    (verticalOverlap * moveFraction + COLLISION_EPSILON)
        }
    }

    fun render(
        shapeRenderer: ShapeRenderer
    ) {
        /*
         * Sprite body is rendered by SummonSpriteRenderer.
         * Keep this method only for health/UI overlay compatibility.
         */
        renderHealthBar(
            shapeRenderer =
                shapeRenderer,

            radius =
                visualRadius()
        )
    }

    private fun updateCow(
        delta: Float,
        playerCenterX: Float,
        playerCenterY: Float,
        enemies: List<Enemy>,
        otherSummons: List<SummonedEntity>,
        onEnemyAttack: (
            summon: SummonedEntity,
            enemy: Enemy
        ) -> Unit,
        onSummonAttack: (
            summon: SummonedEntity,
            target: SummonedEntity
        ) -> Unit
    ) {

        val enemyTarget =
            nearestLivingEnemy(
                enemies
            )

        val summonTarget =
            nearestHostileSummon(
                otherSummons
            )

        val targetKind =
            chooseCloserTarget(
                enemyTarget =
                    enemyTarget,
                summonTarget =
                    summonTarget
            )

        when (targetKind) {
            TargetKind.NONE -> {
                moveTowardFormationPoint(
                    delta =
                        delta,

                    playerCenterX =
                        playerCenterX,

                    playerCenterY =
                        playerCenterY,

                    distance =
                        82f
                )
            }

            TargetKind.ENEMY -> {
                val target =
                    enemyTarget
                        ?: return

                if (
                    isInsideEnemyAttackRange(
                        enemy =
                            target,

                        range =
                            stats.attackRange
                    )
                ) {
                    tryAttack(
                        cooldownSeconds =
                            COW_ATTACK_INTERVAL_SECONDS
                    ) {
                        onEnemyAttack(
                            this,
                            target
                        )
                    }
                } else {
                    moveToward(
                        targetX =
                            target.centerX,

                        targetY =
                            target.centerY,

                        delta =
                            delta
                    )
                }
            }

            TargetKind.SUMMON -> {
                val target =
                    summonTarget
                        ?: return

                if (
                    isInsideSummonAttackRange(
                        target =
                            target,

                        range =
                            stats.attackRange
                    )
                ) {
                    tryAttack(
                        cooldownSeconds =
                            COW_ATTACK_INTERVAL_SECONDS
                    ) {
                        onSummonAttack(
                            this,
                            target
                        )
                    }
                } else {
                    moveToward(
                        targetX =
                            target.centerX,

                        targetY =
                            target.centerY,

                        delta =
                            delta
                    )
                }
            }
        }
    }

    private fun updateImp(
        delta: Float,
        playerCenterX: Float,
        playerCenterY: Float,
        enemies: List<Enemy>,
        otherSummons: List<SummonedEntity>,
        onEnemyAttack: (
            summon: SummonedEntity,
            enemy: Enemy
        ) -> Unit,
        onSummonAttack: (
            summon: SummonedEntity,
            target: SummonedEntity
        ) -> Unit
    ) {

        val enemyTarget =
            nearestLivingEnemy(
                enemies
            )

        val summonTarget =
            nearestHostileSummon(
                otherSummons
            )

        val targetKind =
            chooseCloserTarget(
                enemyTarget =
                    enemyTarget,
                summonTarget =
                    summonTarget
            )

        if (
            targetKind ==
            TargetKind.NONE
        ) {
            moveTowardFormationPoint(
                delta =
                    delta,

                playerCenterX =
                    playerCenterX,

                playerCenterY =
                    playerCenterY,

                distance =
                    108f
            )

            return
        }

        val targetX =
            if (
                targetKind ==
                TargetKind.ENEMY
            ) {
                enemyTarget
                    ?.centerX
                    ?: return
            } else {
                summonTarget
                    ?.centerX
                    ?: return
            }

        val targetY =
            if (
                targetKind ==
                TargetKind.ENEMY
            ) {
                enemyTarget
                    ?.centerY
                    ?: return
            } else {
                summonTarget
                    ?.centerY
                    ?: return
            }

        val targetRadius =
            if (
                targetKind ==
                TargetKind.ENEMY
            ) {
                (
                    enemyTarget
                        ?.bounds
                        ?.width
                        ?: 0f
                    ) /
                    2f
            } else {
                summonTarget
                    ?.combatRadius
                    ?: 0f
            }

        val distanceSquared =
            Vector2.dst2(
                center.x,
                center.y,
                targetX,
                targetY
            )

        val attackRange =
            stats.attackRange +
                targetRadius

        val minimumRange =
            IMP_MINIMUM_COMFORT_DISTANCE

        when {
            distanceSquared >
                attackRange *
                attackRange -> {

                moveToward(
                    targetX =
                        targetX,

                    targetY =
                        targetY,

                    delta =
                        delta
                )
            }

            distanceSquared <
                minimumRange *
                minimumRange -> {

                moveAwayFrom(
                    targetX =
                        targetX,

                    targetY =
                        targetY,

                    delta =
                        delta
                )
            }

            else -> {
                tryAttack(
                    cooldownSeconds =
                        IMP_ATTACK_INTERVAL_SECONDS
                ) {
                    if (
                        targetKind ==
                        TargetKind.ENEMY
                    ) {
                        onEnemyAttack(
                            this,
                            enemyTarget
                                ?: return@tryAttack
                        )
                    } else {
                        onSummonAttack(
                            this,
                            summonTarget
                                ?: return@tryAttack
                        )
                    }
                }
            }
        }
    }

    private fun updateKnight(
        delta: Float,
        playerCenterX: Float,
        playerCenterY: Float,
        worldWidth: Float,
        worldHeight: Float,
        mapObstacles: List<MapObstacle>,
        enemies: List<Enemy>,
        otherSummons: List<SummonedEntity>,
        onEnemyAttack: (
            summon: SummonedEntity,
            enemy: Enemy
        ) -> Unit,
        onSummonAttack: (
            summon: SummonedEntity,
            target: SummonedEntity
        ) -> Unit,
        onPlayerAttack: (
            summon: SummonedEntity
        ) -> Unit
    ) {

        val playerDistanceSquared =
            Vector2.dst2(
                center.x,
                center.y,
                playerCenterX,
                playerCenterY
            )

        if (
            !knightPlayerTargetLocked &&
            playerDistanceSquared <=
            KNIGHT_PLAYER_ACQUIRE_RANGE *
            KNIGHT_PLAYER_ACQUIRE_RANGE
        ) {
            knightPlayerTargetLocked =
                true
        }

        if (
            knightPlayerTargetLocked &&
            playerDistanceSquared >=
            KNIGHT_PLAYER_RELEASE_RANGE *
            KNIGHT_PLAYER_RELEASE_RANGE
        ) {
            knightPlayerTargetLocked =
                false
        }

        if (knightPlayerTargetLocked) {
            if (
                playerDistanceSquared <=
                stats.attackRange *
                stats.attackRange
            ) {
                tryAttack(
                    cooldownSeconds =
                        KNIGHT_ATTACK_INTERVAL_SECONDS
                ) {
                    onPlayerAttack(
                        this
                    )
                }
            } else {
                moveKnightToward(
                    targetX =
                        playerCenterX,

                    targetY =
                        playerCenterY,

                    delta =
                        delta,

                    worldWidth =
                        worldWidth,

                    worldHeight =
                        worldHeight,

                    mapObstacles =
                        mapObstacles
                )
            }

            return
        }

        val enemyTarget =
            nearestLivingEnemy(
                enemies
            )

        val summonTarget =
            nearestHostileSummon(
                otherSummons
            )

        when (
            chooseCloserTarget(
                enemyTarget =
                    enemyTarget,
                summonTarget =
                    summonTarget
            )
        ) {
            TargetKind.NONE -> {
                /*
                 * Rogue Knight has no formation behavior.
                 * It waits until a valid target enters the battlefield.
                 */
            }

            TargetKind.ENEMY -> {
                val target =
                    enemyTarget
                        ?: return

                if (
                    isInsideEnemyAttackRange(
                        enemy =
                            target,

                        range =
                            stats.attackRange
                    )
                ) {
                    tryAttack(
                        cooldownSeconds =
                            KNIGHT_ATTACK_INTERVAL_SECONDS
                    ) {
                        onEnemyAttack(
                            this,
                            target
                        )
                    }
                } else {
                    moveKnightToward(
                        targetX =
                            target.centerX,

                        targetY =
                            target.centerY,

                        delta =
                            delta,

                        worldWidth =
                            worldWidth,

                        worldHeight =
                            worldHeight,

                        mapObstacles =
                            mapObstacles
                    )
                }
            }

            TargetKind.SUMMON -> {
                val target =
                    summonTarget
                        ?: return

                if (
                    isInsideSummonAttackRange(
                        target =
                            target,

                        range =
                            stats.attackRange
                    )
                ) {
                    tryAttack(
                        cooldownSeconds =
                            KNIGHT_ATTACK_INTERVAL_SECONDS
                    ) {
                        onSummonAttack(
                            this,
                            target
                        )
                    }
                } else {
                    moveKnightToward(
                        targetX =
                            target.centerX,

                        targetY =
                            target.centerY,

                        delta =
                            delta,

                        worldWidth =
                            worldWidth,

                        worldHeight =
                            worldHeight,

                        mapObstacles =
                            mapObstacles
                    )
                }
            }
        }
    }

    private fun updateGuard(
        delta: Float,
        worldWidth: Float,
        worldHeight: Float,
        mapObstacles: List<MapObstacle>,
        enemies: List<Enemy>,
        onEnemyAttack: (
            summon: SummonedEntity,
            enemy: Enemy
        ) -> Unit
    ) {
        val currentHealthRatio = health.healthRatio
        val crossedRetreatThreshold =
            GUARD_RETREAT_HEALTH_THRESHOLDS.any { threshold ->
                previousGuardHealthRatio >= threshold &&
                    currentHealthRatio < threshold
            }

        if (crossedRetreatThreshold) {
            guardRetreatRemainingSeconds =
                GUARD_RETREAT_DURATION_SECONDS
        }

        previousGuardHealthRatio = currentHealthRatio

        val homeX = leashCenterX ?: center.x
        val homeY = leashCenterY ?: center.y

        val retainedTarget =
            guardTarget?.takeIf {
                !it.isDead &&
                    it in enemies &&
                    Vector2.dst2(
                        homeX,
                        homeY,
                        it.centerX,
                        it.centerY
                    ) <=
                    GUARD_TARGET_RELEASE_DISTANCE *
                    GUARD_TARGET_RELEASE_DISTANCE
            }

        val target =
            retainedTarget
                ?: enemies
                    .asSequence()
                    .filter { !it.isDead }
                    .filter {
                        Vector2.dst2(
                            homeX,
                            homeY,
                            it.centerX,
                            it.centerY
                        ) <=
                            GUARD_TARGET_ACQUIRE_DISTANCE *
                            GUARD_TARGET_ACQUIRE_DISTANCE
                    }
                    .minByOrNull {
                        Vector2.dst2(
                            center.x,
                            center.y,
                            it.centerX,
                            it.centerY
                        )
                    }

        guardTarget = target

        if (target == null) {
            returnToLeashCenter(
                delta = delta,
                worldWidth = worldWidth,
                worldHeight = worldHeight,
                mapObstacles = mapObstacles
            )
            return
        }

        val insideAttackRange =
            isInsideEnemyAttackRange(
                enemy = target,
                range = stats.attackRange
            )

        if (insideAttackRange) {
            val attacked = tryAttack(
                cooldownSeconds =
                    GUARD_LANCE_ATTACK_INTERVAL_SECONDS
            ) {
                onEnemyAttack(this, target)
            }

            if (attacked) {
                guardAttackVisualTarget.set(
                    target.centerX,
                    target.centerY
                )
                guardAttackVisualRemainingSeconds =
                    GUARD_ATTACK_VISUAL_SECONDS
            }
        }

        if (guardRetreatRemainingSeconds > 0f) {
            moveKnightAwayFrom(
                targetX = target.centerX,
                targetY = target.centerY,
                delta = delta,
                worldWidth = worldWidth,
                worldHeight = worldHeight,
                mapObstacles = mapObstacles
            )
        } else {
            moveKnightToward(
                targetX = target.centerX,
                targetY = target.centerY,
                delta = delta,
                worldWidth = worldWidth,
                worldHeight = worldHeight,
                mapObstacles = mapObstacles
            )
        }
    }

    private fun returnToLeashCenter(
        delta: Float,
        worldWidth: Float,
        worldHeight: Float,
        mapObstacles: List<MapObstacle>
    ) {
        val homeX = leashCenterX ?: return
        val homeY = leashCenterY ?: return

        if (
            Vector2.dst2(center.x, center.y, homeX, homeY) <=
            GUARD_IDLE_RETURN_DISTANCE * GUARD_IDLE_RETURN_DISTANCE
        ) {
            return
        }

        moveKnightToward(
            targetX = homeX,
            targetY = homeY,
            delta = delta,
            worldWidth = worldWidth,
            worldHeight = worldHeight,
            mapObstacles = mapObstacles
        )
    }

    fun renderAttackVisual(
        shapeRenderer: ShapeRenderer
    ) {
        if (
            type != SummonType.GUARD ||
            guardAttackVisualRemainingSeconds <= 0f
        ) {
            return
        }

        val deltaX = guardAttackVisualTarget.x - center.x
        val deltaY = guardAttackVisualTarget.y - center.y
        val distance =
            kotlin.math.sqrt(deltaX * deltaX + deltaY * deltaY)

        if (distance <= 0.001f) {
            return
        }

        val directionX = deltaX / distance
        val directionY = deltaY / distance
        val endX = guardAttackVisualTarget.x
        val endY = guardAttackVisualTarget.y
        val arrowBaseX = endX - directionX * GUARD_ARROW_LENGTH
        val arrowBaseY = endY - directionY * GUARD_ARROW_LENGTH
        val perpendicularX = -directionY * GUARD_ARROW_HALF_WIDTH
        val perpendicularY = directionX * GUARD_ARROW_HALF_WIDTH
        val alpha =
            (guardAttackVisualRemainingSeconds / GUARD_ATTACK_VISUAL_SECONDS)
                .coerceIn(0f, 1f)

        shapeRenderer.color =
            Color(0.66f, 0.68f, 0.72f, alpha)

        shapeRenderer.rectLine(
            center.x,
            center.y,
            arrowBaseX,
            arrowBaseY,
            GUARD_ATTACK_LINE_WIDTH
        )

        shapeRenderer.triangle(
            endX,
            endY,
            arrowBaseX + perpendicularX,
            arrowBaseY + perpendicularY,
            arrowBaseX - perpendicularX,
            arrowBaseY - perpendicularY
        )
    }

    private fun constrainToLeash() {
        val homeX = leashCenterX ?: return
        val homeY = leashCenterY ?: return
        val maximumDistance = maximumLeashDistance ?: return
        val differenceX = center.x - homeX
        val differenceY = center.y - homeY
        val distanceSquared =
            differenceX * differenceX +
                differenceY * differenceY

        if (distanceSquared <= maximumDistance * maximumDistance) {
            return
        }

        val distance =
            kotlin.math.sqrt(distanceSquared)
                .coerceAtLeast(0.001f)

        center.set(
            homeX + differenceX / distance * maximumDistance,
            homeY + differenceY / distance * maximumDistance
        )
    }

    private enum class TargetKind {
        NONE,
        ENEMY,
        SUMMON
    }

    private fun nearestHostileSummon(
        summons: List<SummonedEntity>
    ): SummonedEntity? {

        return summons
            .asSequence()
            .filter {
                it.runtimeId !=
                    runtimeId &&
                    !it.isExpired &&
                    it.faction !=
                    faction
            }
            .minByOrNull {
                Vector2.dst2(
                    center.x,
                    center.y,
                    it.centerX,
                    it.centerY
                )
            }
    }

    private fun chooseCloserTarget(
        enemyTarget: Enemy?,
        summonTarget: SummonedEntity?
    ): TargetKind {

        if (
            enemyTarget ==
            null &&
            summonTarget ==
            null
        ) {
            return TargetKind.NONE
        }

        if (
            enemyTarget ==
            null
        ) {
            return TargetKind.SUMMON
        }

        if (
            summonTarget ==
            null
        ) {
            return TargetKind.ENEMY
        }

        val enemyDistance =
            Vector2.dst2(
                center.x,
                center.y,
                enemyTarget.centerX,
                enemyTarget.centerY
            )

        val summonDistance =
            Vector2.dst2(
                center.x,
                center.y,
                summonTarget.centerX,
                summonTarget.centerY
            )

        return if (
            summonDistance <
            enemyDistance
        ) {
            TargetKind.SUMMON
        } else {
            TargetKind.ENEMY
        }
    }

    private fun isInsideSummonAttackRange(
        target: SummonedEntity,
        range: Float
    ): Boolean {

        val effectiveRange =
            range +
                target.combatRadius

        return Vector2.dst2(
            center.x,
            center.y,
            target.centerX,
            target.centerY
        ) <=
            effectiveRange *
            effectiveRange
    }

    private fun nearestLivingEnemy(
        enemies: List<Enemy>
    ): Enemy? {

        return enemies
            .asSequence()
            .filter {
                !it.isDead
            }
            .minByOrNull {
                Vector2.dst2(
                    center.x,
                    center.y,
                    it.centerX,
                    it.centerY
                )
            }
    }

    private fun isInsideEnemyAttackRange(
        enemy: Enemy,
        range: Float
    ): Boolean {

        val effectiveRange =
            range +
                enemy.bounds.width /
                2f

        return Vector2.dst2(
            center.x,
            center.y,
            enemy.centerX,
            enemy.centerY
        ) <=
            effectiveRange *
            effectiveRange
    }

    private fun tryAttack(
        cooldownSeconds: Float,
        action: () -> Unit
    ): Boolean {

        if (
            attackCooldownRemainingSeconds >
            0f
        ) {
            return false
        }

        action()

        attackCooldownRemainingSeconds =
            (
                cooldownSeconds /
                    stats.attackSpeedMultiplier
                )
                .coerceAtLeast(
                    MINIMUM_ATTACK_INTERVAL_SECONDS
                )

        attackFlashRemainingSeconds =
            ATTACK_FLASH_SECONDS

        return true
    }

    private fun moveKnightAwayFrom(
        targetX: Float,
        targetY: Float,
        delta: Float,
        worldWidth: Float,
        worldHeight: Float,
        mapObstacles: List<MapObstacle>
    ) {
        movementDirection.set(
            center.x - targetX,
            center.y - targetY
        )

        if (movementDirection.isZero(0.001f)) {
            movementDirection.set(1f, 0f)
        }

        movementDirection.nor()
        moveKnightWithCollision(
            direction = movementDirection,
            distance =
                stats.movementSpeed *
                    terrainMovementSpeedMultiplier *
                    delta,
            worldWidth = worldWidth,
            worldHeight = worldHeight,
            mapObstacles = mapObstacles
        )
    }

    private fun moveKnightToward(
        targetX: Float,
        targetY: Float,
        delta: Float,
        worldWidth: Float,
        worldHeight: Float,
        mapObstacles: List<MapObstacle>
    ) {

        movementDirection.set(
            targetX -
                center.x,

            targetY -
                center.y
        )

        if (
            movementDirection.isZero(
                0.001f
            )
        ) {
            return
        }

        movementDirection.nor()

        knightNavigationAgent
            .resolveMovementDirection(
                enemyCenterX =
                    center.x,

                enemyCenterY =
                    center.y,

                targetCenterX =
                    targetX,

                targetCenterY =
                    targetY,

                requestedDirection =
                    movementDirection,

                worldWidth =
                    worldWidth,

                worldHeight =
                    worldHeight,

                obstacles =
                    mapObstacles,

                output =
                    navigationOutput
            )

        if (
            navigationOutput.isZero(
                0.001f
            )
        ) {
            return
        }

        navigationOutput.nor()

        moveKnightWithCollision(
            direction =
                navigationOutput,

            distance =
                stats.movementSpeed *
                    terrainMovementSpeedMultiplier *
                    delta,

            worldWidth =
                worldWidth,

            worldHeight =
                worldHeight,

            mapObstacles =
                mapObstacles
        )
    }

    private fun moveKnightWithCollision(
        direction: Vector2,
        distance: Float,
        worldWidth: Float,
        worldHeight: Float,
        mapObstacles: List<MapObstacle>
    ) {

        if (
            distance <=
            0f
        ) {
            return
        }

        val proposedX =
            center.x +
                direction.x *
                distance

        val proposedY =
            center.y +
                direction.y *
                distance

        if (
            canKnightOccupy(
                centerX =
                    proposedX,

                centerY =
                    proposedY,

                worldWidth =
                    worldWidth,

                worldHeight =
                    worldHeight,

                mapObstacles =
                    mapObstacles
            )
        ) {
            center.set(
                proposedX,
                proposedY
            )

            return
        }

        /*
         * Cheap slide fallback:
         * if the full diagonal step is blocked, try each axis separately.
         * This complements A* and prevents the Knight sticking on obstacle
         * corners when a waypoint sits close to a collision edge.
         */
        val proposedOnlyX =
            center.x +
                direction.x *
                distance

        if (
            canKnightOccupy(
                centerX =
                    proposedOnlyX,

                centerY =
                    center.y,

                worldWidth =
                    worldWidth,

                worldHeight =
                    worldHeight,

                mapObstacles =
                    mapObstacles
            )
        ) {
            center.x =
                proposedOnlyX

            return
        }

        val proposedOnlyY =
            center.y +
                direction.y *
                distance

        if (
            canKnightOccupy(
                centerX =
                    center.x,

                centerY =
                    proposedOnlyY,

                worldWidth =
                    worldWidth,

                worldHeight =
                    worldHeight,

                mapObstacles =
                    mapObstacles
            )
        ) {
            center.y =
                proposedOnlyY
        }
    }

    private fun canKnightOccupy(
        centerX: Float,
        centerY: Float,
        worldWidth: Float,
        worldHeight: Float,
        mapObstacles: List<MapObstacle>
    ): Boolean {

        val radius =
            KNIGHT_COLLISION_RADIUS

        if (
            centerX -
            radius <
            KNIGHT_WORLD_MARGIN ||
            centerX +
            radius >
            worldWidth -
            KNIGHT_WORLD_MARGIN ||
            centerY -
            radius <
            KNIGHT_WORLD_MARGIN ||
            centerY +
            radius >
            worldHeight -
            KNIGHT_WORLD_MARGIN
        ) {
            return false
        }

        val bounds =
            Rectangle(
                centerX -
                    radius,

                centerY -
                    radius,

                radius *
                    2f,

                radius *
                    2f
            )

        return mapObstacles.none {
            bounds.overlaps(
                it.bounds
            )
        }
    }

    private fun moveToward(
        targetX: Float,
        targetY: Float,
        delta: Float
    ) {

        movementDirection.set(
            targetX -
                center.x,

            targetY -
                center.y
        )

        if (
            movementDirection.isZero(
                0.001f
            )
        ) {
            return
        }

        movementDirection.nor()

        center.mulAdd(
            movementDirection,
            stats.movementSpeed *
                terrainMovementSpeedMultiplier *
                delta
        )
    }

    private fun moveAwayFrom(
        targetX: Float,
        targetY: Float,
        delta: Float
    ) {

        movementDirection.set(
            center.x -
                targetX,

            center.y -
                targetY
        )

        if (
            movementDirection.isZero(
                0.001f
            )
        ) {
            movementDirection.set(
                1f,
                0f
            )
        }

        movementDirection.nor()

        center.mulAdd(
            movementDirection,
            stats.movementSpeed *
                terrainMovementSpeedMultiplier *
                delta
        )
    }

    private fun moveTowardFormationPoint(
        delta: Float,
        playerCenterX: Float,
        playerCenterY: Float,
        distance: Float
    ) {

        val desiredX =
            playerCenterX +
                MathUtils.cos(
                    visualPhase
                ) *
                distance

        val desiredY =
            playerCenterY +
                MathUtils.sin(
                    visualPhase
                ) *
                distance

        movementDirection.set(
            desiredX -
                center.x,

            desiredY -
                center.y
        )

        if (
            movementDirection.isZero(
                0.001f
            )
        ) {
            return
        }

        movementDirection.nor()

        val maximumMovement =
            stats.movementSpeed *
                terrainMovementSpeedMultiplier *
                delta

        val remainingDistance =
            Vector2.dst(
                center.x,
                center.y,
                desiredX,
                desiredY
            )

        center.mulAdd(
            movementDirection,
            minOf(
                maximumMovement,
                remainingDistance
            )
        )
    }

    private fun updateVisualMovementState(
        delta: Float,
        previousX: Float,
        previousY: Float
    ) {
        val deltaX =
            center.x -
                previousX

        val deltaY =
            center.y -
                previousY

        val distanceSquared =
            deltaX *
                deltaX +
                deltaY *
                deltaY

        isVisuallyMoving =
            distanceSquared >
                VISUAL_MOVEMENT_EPSILON_SQUARED

        if (!isVisuallyMoving) {
            walkAnimationTime =
                0f

            return
        }

        walkAnimationTime +=
            delta

        visualDirection =
            if (
                kotlin.math.abs(
                    deltaX
                ) >
                kotlin.math.abs(
                    deltaY
                )
            ) {
                if (
                    deltaX >=
                    0f
                ) {
                    SummonVisualDirection.EAST
                } else {
                    SummonVisualDirection.WEST
                }
            } else {
                if (
                    deltaY >=
                    0f
                ) {
                    SummonVisualDirection.NORTH
                } else {
                    SummonVisualDirection.SOUTH
                }
            }
    }

    private fun keepInsideWorld(
        worldWidth: Float,
        worldHeight: Float
    ) {

        center.x =
            center.x.coerceIn(
                12f,
                (
                    worldWidth -
                        12f
                    )
                    .coerceAtLeast(
                        12f
                    )
            )

        center.y =
            center.y.coerceIn(
                12f,
                (
                    worldHeight -
                        12f
                    )
                    .coerceAtLeast(
                        12f
                    )
            )
    }

    private fun visualRadius():
        Float {

        return when (type) {
            SummonType.COW ->
                18f

            SummonType.IMP ->
                14f

            SummonType.KNIGHT_SPIRIT ->
                20f

            SummonType.TOAD ->
                13f

            SummonType.GUARD ->
                20f
        }
    }

    private fun baseColor():
        Color {

        return when (type) {
            SummonType.COW ->
                Color(
                    0.82f,
                    0.82f,
                    0.72f,
                    0.90f
                )

            SummonType.IMP ->
                Color(
                    0.78f,
                    0.22f,
                    0.12f,
                    0.90f
                )

            SummonType.KNIGHT_SPIRIT ->
                Color(
                    0.48f,
                    0.58f,
                    0.76f,
                    0.84f
                )

            SummonType.TOAD ->
                Color(
                    0.45f,
                    0.34f,
                    0.16f,
                    0.92f
                )

            SummonType.GUARD ->
                Color(
                    0.48f,
                    0.42f,
                    0.34f,
                    0.95f
                )
        }
    }

    private fun renderHealthBar(
        shapeRenderer: ShapeRenderer,
        radius: Float
    ) {

        val width =
            radius *
                2f

        val height =
            3f

        val x =
            center.x -
                width /
                2f

        val y =
            center.y +
                radius +
                5f

        shapeRenderer.color =
            Color(
                0.12f,
                0.08f,
                0.10f,
                0.90f
            )

        shapeRenderer.rect(
            x,
            y,
            width,
            height
        )

        shapeRenderer.color =
            Color(
                0.30f,
                0.86f,
                0.38f,
                0.95f
            )

        shapeRenderer.rect(
            x,
            y,
            width *
                health.healthRatio
                    .coerceIn(
                        0f,
                        1f
                    ),
            height
        )
    }

    companion object {

        private const val COLLISION_EPSILON =
            0.05f

        private const val VISUAL_MOVEMENT_EPSILON_SQUARED =
            0.04f

        private const val MINIMUM_ATTACK_INTERVAL_SECONDS =
            0.10f

        private const val COW_ATTACK_INTERVAL_SECONDS =
            1.15f

        private const val IMP_ATTACK_INTERVAL_SECONDS =
            1.05f

        private const val IMP_MINIMUM_COMFORT_DISTANCE =
            105f

        private const val KNIGHT_ATTACK_INTERVAL_SECONDS =
            1.20f

        private const val GUARD_LANCE_ATTACK_INTERVAL_SECONDS =
            2.755f

        private const val GUARD_IDLE_RETURN_DISTANCE =
            120f

        private val GUARD_RETREAT_HEALTH_THRESHOLDS =
            floatArrayOf(0.50f, 0.25f, 0.10f)

        private const val GUARD_RETREAT_DURATION_SECONDS = 2f
        private const val GUARD_TARGET_ACQUIRE_DISTANCE = 500f
        private const val GUARD_TARGET_RELEASE_DISTANCE = 750f
        private const val GUARD_ATTACK_VISUAL_SECONDS = 0.20f
        private const val GUARD_ATTACK_LINE_WIDTH = 3f
        private const val GUARD_ARROW_LENGTH = 15f
        private const val GUARD_ARROW_HALF_WIDTH = 7f

        private const val KNIGHT_PLAYER_ACQUIRE_RANGE =
            300f

        private const val KNIGHT_PLAYER_RELEASE_RANGE =
            600f

        private const val KNIGHT_NAVIGATION_AGENT_SIZE =
            40f

        private const val KNIGHT_COLLISION_RADIUS =
            18f

        private const val KNIGHT_WORLD_MARGIN =
            20f

        private const val ATTACK_FLASH_SECONDS =
            0.10f
    }
}
