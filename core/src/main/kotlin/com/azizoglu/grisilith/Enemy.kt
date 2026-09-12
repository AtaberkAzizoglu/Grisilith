package com.azizoglu.grisilith.enemy

import com.azizoglu.grisilith.combat.damage.DamageNumberEvent

import com.azizoglu.grisilith.combat.ContactDamageTimer
import com.azizoglu.grisilith.combat.HealthComponent
import com.azizoglu.grisilith.combat.healing.HealingVisualManager
import com.azizoglu.grisilith.combat.healing.HealingVisualAnchor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import kotlin.math.sqrt
import com.azizoglu.grisilith.combat.condition.ConditionApplication
import com.azizoglu.grisilith.combat.condition.ConditionRuntime
import com.azizoglu.grisilith.combat.condition.ConditionControlRules
import com.azizoglu.grisilith.combat.reaction.SpellReactionResolver
import com.azizoglu.grisilith.combat.affinity.DamageAffinityResolver
import com.azizoglu.grisilith.combat.interaction.CombatInteractionEvent
import com.azizoglu.grisilith.combat.interaction.CombatInteractionManager
import com.azizoglu.grisilith.combat.interaction.CombatInteractionTrigger
import com.azizoglu.grisilith.content.ConditionType
import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.map.MapObstacle
import com.azizoglu.grisilith.enemy.behavior.EnemyBehaviorController
import com.azizoglu.grisilith.enemy.behavior.EnemyCombatClock
import com.azizoglu.grisilith.enemy.runtime.EnemyRuntimeBundle
import com.azizoglu.grisilith.enemy.definition.EnemyKindIds
import com.azizoglu.grisilith.enemy.definition.EnemyAttackDamageTypes
import com.azizoglu.grisilith.enemy.navigation.EnemyNavigationAgent
import com.azizoglu.grisilith.enemy.trait.EnemyTraitDamageContext
import com.azizoglu.grisilith.enemy.trait.EnemyTraitCombatTarget
import com.azizoglu.grisilith.enemy.trait.CombatEntityType
import com.azizoglu.grisilith.enemy.trait.EnemyTraitTargetDamage
import com.azizoglu.grisilith.enemy.trait.EnemyTraitProjectileVisual
import com.azizoglu.grisilith.enemy.trait.EnemyTraitSpellVisual
import com.azizoglu.grisilith.enemy.trait.EnemyTraitGroundVisual
import com.azizoglu.grisilith.enemy.trait.EnemyTraitSpecialVisual
import com.azizoglu.grisilith.enemy.visual.EnemyVisualState
import com.azizoglu.grisilith.item.runtime.ActiveItemEffects
import com.azizoglu.grisilith.weapon.definition.DefaultWeapons
import kotlin.math.roundToInt

class Enemy(
    startX: Float,
    startY: Float,
    private val stats: EnemyStats,

    private val runtimeBundle:
    EnemyRuntimeBundle =
        EnemyRuntimeBundle.EMPTY,

    val isSlimeFragment: Boolean = false
) {
    private var latestDamageContext =
        EnemyDamageContext.NONE

    private val position = Vector2(startX, startY)
    private val previousPosition = Vector2(startX, startY)
    private val movementDirection = Vector2()

    private val visualState =
        EnemyVisualState()


    private val behaviorController =
        EnemyBehaviorController(
            behaviorId =
                stats.behaviorId,

            preferredAttackRange =
                stats.attackRange
        )

    private val combatClock =
        EnemyCombatClock(
            attacksPerSecond =
                stats.attacksPerSecond
        )

    private val navigationAgent =
        EnemyNavigationAgent(
            agentSize =
                stats.size
        )

    private val traitRuntime =
        runtimeBundle
            .traitRuntime

    private var lastTraitHealthMultiplier =
        1f

    private var pendingSlimeSplit =
        false

    internal var slimeRangedCooldownRemainingSeconds = 0f

    private var slimeFragmentSpawnAnimationSeconds =
        if (isSlimeFragment) 0.5f else 0f

    val spawnVisualScale: Float
        get() {
            if (!isSlimeFragment || slimeFragmentSpawnAnimationSeconds <= 0f) return 1f
            val elapsed = 0.5f - slimeFragmentSpawnAnimationSeconds
            return if (elapsed <= 1f / 3f) {
                0.5f + (elapsed / (1f / 3f)) * 0.7f
            } else {
                1.2f - ((elapsed - 1f / 3f) / (1f / 6f)) * 0.2f
            }.coerceIn(0.5f, 1.2f)
        }

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

    private var size = stats.size
    private var lastWorldWidth = 0f
    private var lastWorldHeight = 0f

    var isVampireSecondForm: Boolean = false
        private set

    var vampireTransformationRemainingSeconds: Float = 0f
        private set

    var vampireMistyEscapeCooldownRemainingSeconds: Float = 0f
        private set

    var isVampireEscaping: Boolean = false
        private set

    var vampireEscapeTargetX: Float = 0f
        private set

    var vampireEscapeTargetY: Float = 0f
        private set

    var isVampireTeleportLocked: Boolean = false
        private set

    val isVampireTransforming: Boolean
        get() = vampireTransformationRemainingSeconds > 0f

    val isActiveSkillLocked: Boolean
        get() =
            isVampireEscaping ||
                isVampireTransforming ||
                isVampireTeleportLocked ||
                conditions.contains(ConditionType.STUNNED)

    private var executionMarkRemainingSeconds =
        0f

    /*
     * Enemy öldüğü anda ekrandan silinmez.
     * Kısa bir fade tamamlandıktan sonra EnemyManager tarafından kaldırılır.
     */
    private var deathFadeRemainingSeconds =
        DEATH_FADE_DURATION_SECONDS

    private val pendingDamageNumberEvents =
        mutableListOf<DamageNumberEvent>()

    private var carriedByHeavyBolt =
        false

    private var armorClassReduction =
        0

    private var armorClassReductionRemainingSeconds =
        0f

    private var temporaryCombatBuffRemainingSeconds = 0f
    private var temporaryDamageMultiplier = 1f
    private var temporaryMovementSpeedMultiplier = 1f
    private var temporaryAttackSpeedMultiplier = 1f

    val effectiveArmorClass: Int
        get() =
            ((stats.armorClass * if (isVampireSecondForm) 1.5f else 1f).roundToInt() -
                armorClassReduction).coerceAtLeast(0)

    private val collisionBounds = Rectangle()

    /*
     * Projectile hit detection gets its own, more generous rectangle.
     *
     * IMPORTANT:
     * This does NOT change the enemy's physical footprint used by:
     * - player contact
     * - enemy separation
     * - obstacle collision
     *
     * It only makes visible enemies easier and more reliable to hit
     * with fast/small projectiles.
     */
    private val projectileCollisionBounds =
        Rectangle()

    private val contactDamageTimer =
        ContactDamageTimer(
            hitsPerSecond =
                stats.attacksPerSecond
                    .takeIf { value ->
                        value > 0f
                    }
                    ?: DEFAULT_CONTACT_HITS_PER_SECOND
        )

    val health = HealthComponent(
        maximumHealth = stats.maximumHealth
    )

    init {
        health.onHealingApplied = { _, type ->
            HealingVisualManager.spawnFollowing(type) {
                HealingVisualAnchor(centerX, centerY, visualSize)
            }
        }
    }

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
        get() =
            (
                stats.contactDamage *
                    (if (isVampireSecondForm) 1.25f else 1f) *
                    traitRuntime
                        .currentModifiers
                        .damageMultiplier *
                    temporaryDamageMultiplier
                )
                .roundToInt()
                .coerceAtLeast(
                    1
                )

    val contactDamageType
        get() = EnemyAttackDamageTypes.contactDamageType(kindId)

    val isDead: Boolean
        get() = health.isDead

    val isDeathFadeComplete: Boolean
        get() =
            isDead &&
                deathFadeRemainingSeconds <=
                0f

    val deathFadeAlpha: Float
        get() =
            if (
                !isDead
            ) {
                1f
            } else {
                (
                    deathFadeRemainingSeconds /
                        DEATH_FADE_DURATION_SECONDS
                    )
                    .coerceIn(
                        0f,
                        1f
                    )
            }

    val type: EnemyType
        get() = stats.type

    val kindId
        get() =
            stats.kindId

    val currentVisualState:
        EnemyVisualState
        get() =
            visualState

    val traitProjectileVisuals: List<EnemyTraitProjectileVisual>
        get() = traitRuntime.projectileVisuals()

    val traitSpellVisuals: List<EnemyTraitSpellVisual>
        get() = traitRuntime.spellVisuals()

    val traitGroundVisuals: List<EnemyTraitGroundVisual>
        get() = traitRuntime.groundVisuals()

    val traitSpecialVisual: EnemyTraitSpecialVisual?
        get() = traitRuntime.specialVisual(this)

    val behaviorId
        get() =
            stats.behaviorId

    val attacksPerSecond: Float
        get() =
            stats.attacksPerSecond

    val attackRange: Float
        get() =
            stats.attackRange

    val playerVisionMultiplier: Float
        get() =
            traitRuntime.playerVisionMultiplier(this)

    val activeAbilityIds
        get() =
            runtimeBundle
                .abilityRuntime
                .activeAbilityIds

    val passiveAbilityIds
        get() =
            runtimeBundle
                .abilityRuntime
                .passiveAbilityIds

    val weaponRuntimes
        get() =
            runtimeBundle
                .weaponRuntime
                .allWeapons

    val gateDamageMultiplier: Float
        get() {
            val usesProjectileOrMagic =
                weaponRuntimes.any { runtime ->
                    val definition =
                        DefaultWeapons.findById(runtime.weaponId.value)

                    definition != null &&
                        (
                            ContentTag.RANGED_WEAPON in definition.tags ||
                                ContentTag.SPELL_FOCUS in definition.tags
                            )
                }

            return if (usesProjectileOrMagic) 0.5f else 1f
        }

    val ignoresObstacles: Boolean
        get() =
            stats.ignoresObstacles || kindId == EnemyKindIds.VAMPIRE_LORD

    val canSpawnInsideObstacles: Boolean
        get() =
            stats.canSpawnInsideObstacles || kindId == EnemyKindIds.VAMPIRE_LORD

    val centerX: Float
        get() = position.x + size / 2f

    val centerY: Float
        get() = position.y + size / 2f

    /*
     * The enemy's ORIGINAL visual canvas size.
     *
     * `bounds` is intentionally a much smaller feet/body collision footprint,
     * so renderers must never derive sprite size from bounds.width.
     */
    val visualSize: Float
        get() =
            size

    /*
     * position + size visual frame'dir.
     * bounds alt gövde / ayak gameplay footprint'idir.
     */
    val bounds: Rectangle
        get() {
            collisionBounds.set(
                position.x +
                    size * COLLISION_OFFSET_X_RATIO,
                position.y +
                    size * COLLISION_OFFSET_Y_RATIO,
                size * COLLISION_WIDTH_RATIO,
                size * COLLISION_HEIGHT_RATIO
            )

            return collisionBounds
        }

    /*
     * Wider projectile-only target area.
     *
     * The normal bounds stay as the smaller feet/body gameplay footprint.
     * This rectangle intentionally covers most of the visual body so arrows
     * and spell projectiles do not appear to pass through the enemy.
     */
    val projectileHitBounds: Rectangle
        get() {
            projectileCollisionBounds.set(
                position.x +
                    size * PROJECTILE_HIT_OFFSET_X_RATIO,
                position.y +
                    size * PROJECTILE_HIT_OFFSET_Y_RATIO,
                size * PROJECTILE_HIT_WIDTH_RATIO,
                size * PROJECTILE_HIT_HEIGHT_RATIO
            )

            return projectileCollisionBounds
        }

    val goldReward: Float
        get() = stats.goldReward

    private var terrainMovementSpeedMultiplier =
        1f

    fun setTerrainMovementSpeedMultiplier(multiplier: Float) {
        terrainMovementSpeedMultiplier =
            multiplier.coerceIn(0f, 1f)
    }

    fun update(
        delta: Float,
        targetX: Float,
        targetY: Float,
        targetBounds: Rectangle,
        targetEntityType: CombatEntityType = CombatEntityType.PLAYER,
        worldWidth: Float,
        worldHeight: Float,
        mapObstacles: List<MapObstacle>,
        nearbyEnemies: List<Enemy>,
        onContactDamage: (Int) -> Unit,
        combatTargets: List<EnemyTraitCombatTarget> = emptyList(),
        onTraitTargetDamage: (EnemyTraitTargetDamage) -> Unit = {}
    ) {
        lastWorldWidth = worldWidth
        lastWorldHeight = worldHeight
        if (slimeFragmentSpawnAnimationSeconds > 0f) {
            val appliedDelta = delta.coerceAtLeast(0f).coerceAtMost(slimeFragmentSpawnAnimationSeconds)
            position.y += FRAGMENT_BOUNCE_NORTH_SPEED * appliedDelta
            slimeFragmentSpawnAnimationSeconds -= appliedDelta
        }

        updateTemporaryCombatBuff(delta)

        if (
            armorClassReductionRemainingSeconds >
            0f
        ) {
            armorClassReductionRemainingSeconds =
                (
                    armorClassReductionRemainingSeconds -
                        delta.coerceAtLeast(
                            0f
                        )
                    )
                    .coerceAtLeast(
                        0f
                    )

            if (
                armorClassReductionRemainingSeconds <=
                0f
            ) {
                armorClassReduction =
                    0
            }
        }

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
            deathFadeRemainingSeconds =
                (
                    deathFadeRemainingSeconds -
                        delta.coerceAtLeast(
                            0f
                        )
                    )
                    .coerceAtLeast(
                        0f
                    )

            conditions.clear()
            return
        }

        vampireTransformationRemainingSeconds =
            (vampireTransformationRemainingSeconds - delta.coerceAtLeast(0f))
                .coerceAtLeast(0f)
        vampireMistyEscapeCooldownRemainingSeconds =
            (vampireMistyEscapeCooldownRemainingSeconds - delta.coerceAtLeast(0f))
                .coerceAtLeast(0f)

        if (carriedByHeavyBolt) {
            conditions.clear()
            return
        }

        conditionRuntime.update(
            delta = delta
        )

        traitRuntime.update(
            delta =
                delta,

            owner =
                this,

            nearbyEnemies =
                nearbyEnemies,

            targetCenterX =
                targetX,

            targetCenterY =
                targetY,

            targetBounds =
                targetBounds,

            worldWidth =
                worldWidth,

            worldHeight =
                worldHeight,

            obstacles =
                mapObstacles,

            combatTargets =
                combatTargets
        )

        synchronizeTraitHealthMultiplier()

        traitRuntime
            .consumePlayerDamage()
            .forEach { traitDamage ->
                if (
                    traitDamage.amount >
                    0
                ) {
                    onContactDamage(
                        traitDamage.amount
                    )
                }
            }

        traitRuntime
            .consumeTargetDamage()
            .forEach(onTraitTargetDamage)

        combatClock.update(
            delta =
                delta
        )

        runtimeBundle
            .aiPipeline
            .update(
                delta =
                    delta
            )

        if (isDead) {
            return
        }

        previousPosition.set(position)

        navigationAgent.update(
            delta =
                delta
        )

        val controlState =
            ConditionControlRules.resolve(
                conditions = conditions,
                fallbackSourceX = targetX,
                fallbackSourceY = targetY
            )

        if (isBeingKnockedBack) {
            /*
             * Condition'lar normal hareketi durdurabilir fakat
             * dışarıdan gelen knockback fiziksel yer değiştirme
             * olarak çalışmaya devam eder.
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
        } else if (controlState.canMove) {
            val traitOverridesMovement =
                traitRuntime
                    .resolveMovementOverride(
                        owner =
                            this,

                        output =
                            movementDirection
                    )

            if (
                traitOverridesMovement
            ) {
                /*
                 * Trait'in verdiği vektörün uzunluğu aynı zamanda
                 * normal Movement Speed'e uygulanan hız çarpanıdır.
                 */
            } else if (controlState.isFrightened) {
                movementDirection.set(
                    centerX - controlState.sourceX,
                    centerY - controlState.sourceY
                )
            } else {
                behaviorController
                    .resolveMovementDirection(
                        enemyCenterX =
                            centerX,

                        enemyCenterY =
                            centerY,

                        targetCenterX =
                            targetX,

                        targetCenterY =
                            targetY,

                        output =
                            movementDirection
                    )

                if (
                    !ignoresObstacles &&
                    !movementDirection.isZero
                ) {
                    navigationAgent
                        .resolveMovementDirection(
                            enemyCenterX =
                                centerX,

                            enemyCenterY =
                                centerY,

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
                                movementDirection
                        )
                }
            }

            if (!movementDirection.isZero) {
                val traitSpeedMultiplier =
                    if (
                        traitOverridesMovement
                    ) {
                        movementDirection.len()
                            .coerceAtLeast(
                                1f
                            )
                    } else {
                        1f
                    }

                movementDirection.nor()

                position.mulAdd(
                    movementDirection,
                    controlState.modifiedMovementSpeed(
                        stats.movementSpeed *
                            (if (isVampireSecondForm) 1.20f else 1f) *
                            terrainMovementSpeedMultiplier *
                            traitRuntime
                                .currentModifiers
                                .movementSpeedMultiplier *
                            temporaryMovementSpeedMultiplier *
                            traitSpeedMultiplier
                    ) * delta
                )
            }
        }

        val isTouchingPlayer =
            !isVampireEscaping && bounds.overlaps(targetBounds)

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

        val visualMovementX =
            position.x -
                previousPosition.x

        val visualMovementY =
            position.y -
                previousPosition.y

        visualState.update(
            delta = delta,
            movementX = visualMovementX,
            movementY = visualMovementY
        )

        contactDamageTimer.update(
            delta =
                delta *
                    traitRuntime
                        .currentModifiers
                        .attackSpeedMultiplier *
                    temporaryAttackSpeedMultiplier,

            isTouching =
                isTouchingPlayer &&
                    controlState.canAttack
        ) {
            val traitContactMultiplier =
                traitRuntime
                    .contactDamageMultiplier(
                        this
                    )

            if (traitContactMultiplier > 0f) {
                val criticalMultiplier =
                    rollContactCriticalMultiplier()

                val resolvedContactDamage =
                    (
                        contactDamage *
                            traitContactMultiplier *
                            criticalMultiplier
                        )
                        .roundToInt()
                        .coerceAtLeast(1)

                onContactDamage(resolvedContactDamage)

                traitRuntime.onPlayerContact(
                    owner = this,
                    dealtDamage = resolvedContactDamage,
                    targetEntityType = targetEntityType
                )
            }
        }
    }

    val isGenericAttackReady: Boolean
        get() =
            combatClock.isReady

    fun consumeGenericAttack(): Boolean {
        return combatClock.consume()
    }

    fun resetGenericAttackCooldown() {
        combatClock.reset()
    }

    fun resolveObstacleCollisions(
        obstacles: List<MapObstacle>
    ) {
        if (
            ignoresObstacles ||
            isDead ||
            carriedByHeavyBolt
        ) {
            return
        }

        obstacles.forEach { obstacle ->
            if (
                !bounds.overlaps(
                    obstacle.bounds
                )
            ) {
                return@forEach
            }

            val currentX =
                position.x

            val currentY =
                position.y

            /*
             * Önce yatay hareketi geri alıp dikey kaymayı koruruz.
             */
            position.x =
                previousPosition.x

            if (
                !bounds.overlaps(
                    obstacle.bounds
                )
            ) {
                return@forEach
            }

            /*
             * Yatay kayma mümkün değilse dikey hareketi geri alırız.
             */
            position.x =
                currentX

            position.y =
                previousPosition.y

            if (
                !bounds.overlaps(
                    obstacle.bounds
                )
            ) {
                return@forEach
            }

            /*
             * İki eksen de kapalıysa önceki güvenli konuma döneriz.
             */
            position.set(
                previousPosition
            )
        }
    }

    fun separateFrom(other: Enemy) {
        if (isDead || other.isDead || other === this) {
            return
        }

        val thisBounds =
            bounds

        val otherBounds =
            other.bounds

        var differenceX =
            (
                thisBounds.x +
                    thisBounds.width / 2f
                ) -
                (
                    otherBounds.x +
                        otherBounds.width / 2f
                    )

        var differenceY =
            (
                thisBounds.y +
                    thisBounds.height / 2f
                ) -
                (
                    otherBounds.y +
                        otherBounds.height / 2f
                    )

        var distanceSquared =
            differenceX * differenceX +
                differenceY * differenceY

        val minimumDistance =
            (
                thisBounds.width +
                    otherBounds.width
                ) /
                2f

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

    fun keepSouthOfBarrier(
        barrierY: Float,
        contactOverlap: Float = 1f
    ) {
        if (isDead || carriedByHeavyBolt) {
            return
        }

        val maximumY =
            barrierY - size +
                contactOverlap.coerceAtLeast(0f)

        if (position.y <= maximumY) {
            return
        }

        position.y = maximumY
        previousPosition.y = maximumY

        if (knockbackVelocity.y > 0f) {
            knockbackVelocity.y = 0f

            if (knockbackVelocity.isZero) {
                knockbackTimeRemaining = 0f
            }
        }
    }

    fun teleportCenter(targetCenterX: Float, targetCenterY: Float, worldWidth: Float, worldHeight: Float) {
        position.set(
            (targetCenterX - size / 2f).coerceIn(0f, (worldWidth - size).coerceAtLeast(0f)),
            (targetCenterY - size / 2f).coerceIn(0f, (worldHeight - size).coerceAtLeast(0f))
        )
        previousPosition.set(position)
    }

    fun takeDamage(
        amount: Int,
        damageContext: EnemyDamageContext =
            EnemyDamageContext.NONE
    ): Int {

        if (
            amount <= 0 ||
            isDead
        ) {
            return 0
        }

        val resolvedDamageContext =
            ActiveItemEffects.resolveDamageContext(damageContext)

        val itemModifiedAmount =
            ActiveItemEffects.modifyOutgoingDamage(
                amount = amount,
                enemy = this,
                context = resolvedDamageContext
            )

        if (itemModifiedAmount <= 0) return 0

        if (
            traitRuntime
                .shouldNegateIncomingDamage(
                    owner =
                        this,

                    context =
                        EnemyTraitDamageContext(
                            requestedDamage =
                                itemModifiedAmount,

                            damageContext =
                                resolvedDamageContext
                        )
                )
        ) {
            if (kindId == EnemyKindIds.GHOST) {
                pendingDamageNumberEvents.add(
                    DamageNumberEvent(
                        amount = 0,
                        worldX = centerX,
                        worldY = centerY + size * 0.60f,
                        displayText = "Miss",
                        isMiss = true
                    )
                )
            }
            return 0
        }

        val reactionResult =
            SpellReactionResolver
                .resolveIncomingDamage(
                    baseDamage =
                        itemModifiedAmount,

                    damageContext =
                        resolvedDamageContext,

                    conditions =
                        conditions
                )

        val traitDamageContext =
            EnemyTraitDamageContext(
                requestedDamage = reactionResult.resolvedDamage,
                damageContext = resolvedDamageContext
            )

        val traitModifiedDamage =
            (
                reactionResult.resolvedDamage *
                    traitRuntime.incomingDamageMultiplier(
                        owner = this,
                        context = traitDamageContext
                    )
                )
                .roundToInt()
                .coerceAtLeast(0)

        if (traitModifiedDamage <= 0) return 0

        val affinityModifiedDamage =
            DamageAffinityResolver
                .resolve(
                    requestedDamage =
                        traitModifiedDamage,

                    damageType =
                        resolvedDamageContext
                            .damageType,

                    profile =
                        stats
                            .damageAffinities
                )

        if (
            affinityModifiedDamage <=
            0
        ) {
            return 0
        }

        val armorModifiedDamage =
            if (
                resolvedDamageContext.isPhysicalWeaponDamage
            ) {
                val safePiercingRatio =
                    resolvedDamageContext
                        .attackerPiercingIgnoreRatio
                        .coerceIn(
                            0f,
                            0.75f
                        )

                /*
                 * Player ile aynı fiziksel armor modeli:
                 *
                 * Raw Armor Reduction =
                 * Base Armor × Effective Armor Class
                 *
                 * Piercing bu toplam azaltımın bir bölümünü ignore eder.
                 */
                val rawArmorReduction =
                    stats.baseArmor
                        .coerceAtLeast(0)
                        .toFloat() *
                        effectiveArmorClass
                            .coerceAtLeast(0)
                            .toFloat()

                val effectiveDamageReduction =
                    rawArmorReduction *
                        (1f - safePiercingRatio)

                (
                    affinityModifiedDamage
                        .toFloat() -
                        effectiveDamageReduction
                    )
                    .roundToInt()
                    .coerceAtLeast(1)
            } else {
                affinityModifiedDamage
                    .coerceAtLeast(1)
            }

        val traitResolvedFinalDamage =
            traitRuntime.resolveFinalIncomingDamage(
                owner = this,
                context = EnemyTraitDamageContext(
                    requestedDamage = armorModifiedDamage,
                    damageContext = resolvedDamageContext
                ),
                resolvedDamage = armorModifiedDamage
            )

        if (traitResolvedFinalDamage <= 0) return 0

        val triggersVampireMistyEscape =
            kindId == EnemyKindIds.VAMPIRE_LORD &&
                !isVampireSecondForm &&
                traitResolvedFinalDamage >= health.currentHealth

        val appliedDamage =
            if (triggersVampireMistyEscape) {
                val dealt = health.takeDamage((health.currentHealth - 1).coerceAtLeast(0))
                beginVampireMistyEscape()
                dealt
            } else {
                health.takeDamage(traitResolvedFinalDamage)
            }

        if (
            appliedDamage > 0 &&
            health.isDead &&
            !isSlimeFragment &&
            kindId == EnemyKindIds.SLIME
        ) {
            pendingSlimeSplit = true
        }

        if (
            appliedDamage >
            0
        ) {
            latestDamageContext =
                resolvedDamageContext

            ActiveItemEffects.recordDamage(
                context = resolvedDamageContext,
                appliedDamage = appliedDamage
            )

            pendingDamageNumberEvents.add(
                DamageNumberEvent(
                    amount =
                        appliedDamage,
                    worldX =
                        centerX,
                    worldY =
                        centerY +
                            size *
                            0.60f
                )
            )

            CombatInteractionManager
                .notifyGlobal(
                    CombatInteractionEvent(
                        trigger =
                            CombatInteractionTrigger
                                .DAMAGE_APPLIED,

                        damageType =
                            resolvedDamageContext
                                .damageType,

                        enemy =
                            this,

                        worldX =
                            centerX,

                        worldY =
                            centerY,

                        appliedDamage =
                            appliedDamage,

                        sourceWeaponRuntimeId =
                            resolvedDamageContext
                                .sourceWeaponRuntimeId,

                        sourceWeaponDefinitionId =
                            resolvedDamageContext
                                .sourceWeaponDefinitionId,

                        sourceName =
                            resolvedDamageContext
                                .sourceName
                    )
                )
        }

        reactionResult
            .consumedCondition
            ?.let {
                    conditionType ->

                conditions.remove(
                    conditionType
                )
            }

        return appliedDamage
    }

    fun consumeSlimeSplitRequest(): Boolean {
        if (!pendingSlimeSplit) return false
        pendingSlimeSplit = false
        return true
    }

    internal fun slimeFragmentStats(): EnemyStats {
        fun fragmentValue(value: Int) = (value * 0.30f).roundToInt().coerceAtLeast(1)
        return stats.copy(
            maximumHealth = fragmentValue(health.maximumHealth),
            contactDamage = fragmentValue(stats.contactDamage),
            movementSpeed = stats.movementSpeed * 1.75f,
            size = stats.size * 0.55f,
            goldReward = stats.goldReward * 0.25f,
            armorClass = (stats.armorClass * 0.30f).roundToInt().coerceAtLeast(0),
            baseArmor = (stats.baseArmor * 0.30f).roundToInt().coerceAtLeast(0),
            attacksPerSecond = (stats.attacksPerSecond * 0.30f).coerceAtLeast(0.10f),
            attackRange = (stats.attackRange * 0.55f).coerceAtLeast(20f),
            weaponDamageMultiplier = stats.weaponDamageMultiplier * 0.30f,
            attackSpeedScore = (stats.attackSpeedScore * 0.30f).roundToInt(),
            mainDamageStat = (stats.mainDamageStat * 0.30f).roundToInt(),
            secondaryDamageStat = (stats.secondaryDamageStat * 0.30f).roundToInt(),
            critRate = (stats.critRate * 0.30f).roundToInt(),
            critDamage = (stats.critDamage * 0.30f).roundToInt(),
            piercing = (stats.piercing * 0.30f).roundToInt()
        )
    }

    val lethalDamageContext: EnemyDamageContext?
        get() = if (isDead) {
            latestDamageContext
        } else {
            null
        }

    fun markLatestDamageNumberCritical(
        appliedDamage: Int
    ) {
        pendingDamageNumberEvents
            .asReversed()
            .firstOrNull { event ->
                !event.isCriticalHit &&
                    event.amount ==
                    appliedDamage
            }
            ?.isCriticalHit =
            true
    }

    fun consumeDamageNumberEvents():
        List<DamageNumberEvent> {

        if (
            pendingDamageNumberEvents.isEmpty()
        ) {
            return emptyList()
        }

        val result =
            pendingDamageNumberEvents.toList()

        pendingDamageNumberEvents.clear()

        return result
    }

    fun resolveTraitContactWith(
        other: Enemy
    ) {
        if (
            other === this ||
            isDead ||
            other.isDead ||
            !bounds.overlaps(
                other.bounds
            )
        ) {
            return
        }

        traitRuntime
            .onEnemyContact(
                owner =
                    this,

                other =
                    other
            )
    }

    private fun synchronizeTraitHealthMultiplier() {
        val resolvedMultiplier =
            traitRuntime
                .currentModifiers
                .maximumHealthMultiplier
                .coerceAtLeast(
                    0.10f
                )

        if (
            kotlin.math.abs(
                resolvedMultiplier -
                    lastTraitHealthMultiplier
            ) <
            0.0001f
        ) {
            return
        }

        val previousRatio =
            health.healthRatio

        val resolvedMaximumHealth =
            (
                stats.maximumHealth *
                    resolvedMultiplier
                )
                .roundToInt()
                .coerceAtLeast(
                    1
                )

        health.setMaximumHealth(
            newMaximumHealth =
                resolvedMaximumHealth,

            healAddedHealth =
                false
        )

        if (
            !health.isDead
        ) {
            health.restoreCurrentHealth(
                (
                    resolvedMaximumHealth *
                        previousRatio
                    )
                    .roundToInt()
                    .coerceIn(
                        1,
                        resolvedMaximumHealth
                    )
            )
        }

        lastTraitHealthMultiplier =
            resolvedMultiplier
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

    fun applyTemporaryArmorClassReduction(
        amount: Int,
        durationSeconds: Float
    ) {

        if (
            isDead ||
            amount <= 0 ||
            durationSeconds <= 0f
        ) {
            return
        }

        armorClassReduction =
            maxOf(
                armorClassReduction,
                amount
            )

        armorClassReductionRemainingSeconds =
            maxOf(
                armorClassReductionRemainingSeconds,
                durationSeconds
            )
    }

    fun applyTemporaryCombatBuff(
        durationSeconds: Float,
        damageMultiplier: Float,
        movementSpeedMultiplier: Float,
        attackSpeedMultiplier: Float
    ) {
        if (isDead || durationSeconds <= 0f) return

        temporaryCombatBuffRemainingSeconds =
            maxOf(temporaryCombatBuffRemainingSeconds, durationSeconds)

        temporaryDamageMultiplier =
            maxOf(temporaryDamageMultiplier, damageMultiplier.coerceAtLeast(1f))

        temporaryMovementSpeedMultiplier =
            maxOf(temporaryMovementSpeedMultiplier, movementSpeedMultiplier.coerceAtLeast(1f))

        temporaryAttackSpeedMultiplier =
            maxOf(temporaryAttackSpeedMultiplier, attackSpeedMultiplier.coerceAtLeast(1f))
    }

    private fun updateTemporaryCombatBuff(delta: Float) {
        if (temporaryCombatBuffRemainingSeconds <= 0f) return

        temporaryCombatBuffRemainingSeconds =
            (temporaryCombatBuffRemainingSeconds - delta.coerceAtLeast(0f))
                .coerceAtLeast(0f)

        if (temporaryCombatBuffRemainingSeconds <= 0f) {
            temporaryDamageMultiplier = 1f
            temporaryMovementSpeedMultiplier = 1f
            temporaryAttackSpeedMultiplier = 1f
        }
    }

    private fun rollContactCriticalMultiplier(): Float {
        val chance =
            (stats.baseCritRatePercent + stats.critRate * 2f)
                .coerceIn(0f, 100f) /
                100f

        if (chance <= 0f || !MathUtils.randomBoolean(chance)) return 1f

        return (
            (stats.baseCritDamagePercent + stats.critDamage * 3f) /
                100f
            )
            .coerceAtLeast(1f)
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
                ActiveItemEffects.knockbackMultiplier() *
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

    fun render(
        shapeRenderer: ShapeRenderer,
        renderBody: Boolean = true,
        renderHealthBar: Boolean = true
    ) {
        if (isDead) {
            if (
                renderBody &&
                deathFadeAlpha >
                0f
            ) {
                val baseColor =
                    when (stats.type) {
                        EnemyType.NORMAL ->
                            Color.RED

                        EnemyType.UNIQUE ->
                            Color.PURPLE

                        EnemyType.MINI_BOSS ->
                            Color.ORANGE

                        EnemyType.FINAL_BOSS ->
                            Color.GOLD
                    }

                shapeRenderer.color =
                    Color(
                        baseColor.r,
                        baseColor.g,
                        baseColor.b,
                        deathFadeAlpha
                    )

                shapeRenderer.rect(
                    position.x,
                    position.y,
                    size,
                    size
                )
            }

            renderExecutionMark(
                shapeRenderer
            )

            return
        }

        traitRuntime.render(
            owner =
                this,

            shapeRenderer =
                shapeRenderer
        )

        if (renderBody) {
            shapeRenderer.color =
                when (stats.type) {
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
        }

        if (renderHealthBar) {
            renderHealthBar(
                shapeRenderer = shapeRenderer
            )
        }
    }

    private fun beginVampireMistyEscape() {
        if (isVampireSecondForm || isVampireEscaping || kindId != EnemyKindIds.VAMPIRE_LORD) return
        isVampireEscaping = true
        vampireEscapeTargetX =
            if (centerX < lastWorldWidth / 2f) size / 2f + VAMPIRE_ESCAPE_EDGE_MARGIN
            else lastWorldWidth - size / 2f - VAMPIRE_ESCAPE_EDGE_MARGIN
        vampireEscapeTargetY =
            centerY.coerceIn(
                size / 2f + VAMPIRE_ESCAPE_EDGE_MARGIN,
                lastWorldHeight - size / 2f - VAMPIRE_ESCAPE_EDGE_MARGIN
            )
        vampireMistyEscapeCooldownRemainingSeconds = 180f
    }

    fun startVampireSecondFormTransformation() {
        if (isVampireSecondForm || kindId != EnemyKindIds.VAMPIRE_LORD) return
        val oldCenterX = centerX
        val oldCenterY = centerY
        isVampireEscaping = false
        isVampireSecondForm = true
        size = 82f
        position.set(oldCenterX - size / 2f, oldCenterY - size / 2f)
        previousPosition.set(position)
        health.setMaximumHealth(
            newMaximumHealth = (stats.maximumHealth * 1.50f).roundToInt(),
            healAddedHealth = false
        )
        health.restoreCurrentHealth(health.maximumHealth)
        HealingVisualManager.spawnFollowing(
            com.azizoglu.grisilith.combat.HealingVisualType.HEAL
        ) {
            HealingVisualAnchor(centerX, centerY, visualSize)
        }
        vampireTransformationRemainingSeconds = VAMPIRE_TRANSFORMATION_DURATION_SECONDS
    }

    fun setVampireTeleportLocked(locked: Boolean) {
        isVampireTeleportLocked = locked
    }

    fun resolveCircularExclusion(
        centerX: Float,
        centerY: Float,
        radius: Float
    ) {
        if (isDead || ignoresObstacles || radius <= 0f) return

        val dx = this.centerX - centerX
        val dy = this.centerY - centerY
        val distanceSquared = dx * dx + dy * dy
        val requiredDistance = radius + size * 0.5f
        if (distanceSquared >= requiredDistance * requiredDistance) return

        val distance = kotlin.math.sqrt(distanceSquared).coerceAtLeast(0.001f)
        val push = requiredDistance - distance
        position.x += dx / distance * push
        position.y += dy / distance * push
    }

    fun constrainCenterToCircle(
        circleCenterX: Float,
        circleCenterY: Float,
        radius: Float
    ) {
        if (isDead || radius <= 0f) return
        val dx = centerX - circleCenterX
        val dy = centerY - circleCenterY
        val distanceSquared = dx * dx + dy * dy
        val maximumDistance = (radius - size * 0.5f).coerceAtLeast(0f)
        if (distanceSquared <= maximumDistance * maximumDistance) return
        val distance = kotlin.math.sqrt(distanceSquared).coerceAtLeast(0.001f)
        position.x += circleCenterX + dx / distance * maximumDistance - centerX
        position.y += circleCenterY + dy / distance * maximumDistance - centerY
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
        val barWidth =
            size

        val barHeight =
            when (type) {
                EnemyType.FINAL_BOSS ->
                    11f

                else ->
                    6f
            }

        val barY =
            if (kindId == EnemyKindIds.VAMPIRE_LORD) {
                position.y + size * 1.62f + 8f
            } else {
                position.y + size + 7f
            }

        val backgroundColor =
            Color(
                0.10f,
                0.10f,
                0.12f,
                0.96f
            )

        /*
         * UNIQUE:
         * Red HP bar with a gold frame.
         */
        if (
            type ==
            EnemyType.UNIQUE
        ) {
            val border =
                2f

            shapeRenderer.color =
                Color(
                    0.96f,
                    0.69f,
                    0.08f,
                    1f
                )

            shapeRenderer.rect(
                position.x -
                    border,
                barY -
                    border,
                barWidth +
                    border *
                    2f,
                barHeight +
                    border *
                    2f
            )
        }

        /*
         * FINAL BOSS:
         * A thicker and more strongly framed purple bar.
         */
        if (
            type ==
            EnemyType.FINAL_BOSS
        ) {
            val border =
                2f

            shapeRenderer.color =
                Color(
                    0.26f,
                    0.04f,
                    0.36f,
                    1f
                )

            shapeRenderer.rect(
                position.x -
                    border,
                barY -
                    border,
                barWidth +
                    border *
                    2f,
                barHeight +
                    border *
                    2f
            )
        }

        shapeRenderer.color =
            backgroundColor

        shapeRenderer.rect(
            position.x,
            barY,
            barWidth,
            barHeight
        )

        shapeRenderer.color =
            when (type) {
                EnemyType.NORMAL ->
                    Color(
                        0.84f,
                        0.07f,
                        0.09f,
                        1f
                    )

                EnemyType.UNIQUE ->
                    Color(
                        0.84f,
                        0.07f,
                        0.09f,
                        1f
                    )

                EnemyType.MINI_BOSS ->
                    Color(
                        0.96f,
                        0.40f,
                        0.04f,
                        1f
                    )

                EnemyType.FINAL_BOSS ->
                    Color(
                        0.58f,
                        0.15f,
                        0.80f,
                        1f
                    )
            }

        shapeRenderer.rect(
            position.x,
            barY,
            barWidth *
                health.healthRatio,
            barHeight
        )

        if (isVampireSecondForm) {
            shapeRenderer.color = Color.BLACK
            val filledWidth = barWidth * health.healthRatio
            val patternInset = 0.5f
            val patternBottomY = barY + patternInset
            val patternTopY = barY + barHeight - patternInset
            val patternHeight = patternTopY - patternBottomY
            val filledEndX = position.x + filledWidth - patternInset
            val cellWidth = patternHeight
            var cellStartX = position.x + patternInset
            while (cellStartX < filledEndX) {
                val cellEndX = (cellStartX + cellWidth).coerceAtMost(filledEndX)
                val cellRatio = ((cellEndX - cellStartX) / cellWidth).coerceIn(0f, 1f)
                shapeRenderer.rectLine(
                    cellStartX,
                    patternBottomY,
                    cellEndX,
                    patternBottomY + patternHeight * cellRatio,
                    0.65f
                )
                shapeRenderer.rectLine(
                    cellStartX,
                    patternTopY,
                    cellEndX,
                    patternTopY - patternHeight * cellRatio,
                    0.65f
                )
                cellStartX += cellWidth
            }
        }
    }
    companion object {

        const val VAMPIRE_TRANSFORMATION_DURATION_SECONDS = 3.2f

        private const val VAMPIRE_ESCAPE_EDGE_MARGIN = 24f

        private const val FRAGMENT_BOUNCE_NORTH_SPEED = 18f

        private const val COLLISION_WIDTH_RATIO =
            0.58f

        private const val COLLISION_HEIGHT_RATIO =
            0.42f

        private const val COLLISION_OFFSET_X_RATIO =
            0.21f

        private const val COLLISION_OFFSET_Y_RATIO =
            0.04f
        private const val PROJECTILE_HIT_WIDTH_RATIO =
            0.88f

        private const val PROJECTILE_HIT_HEIGHT_RATIO =
            0.80f

        private const val PROJECTILE_HIT_OFFSET_X_RATIO =
            0.06f

        private const val PROJECTILE_HIT_OFFSET_Y_RATIO =
            0.06f

        private const val DEFAULT_CONTACT_HITS_PER_SECOND =
            1f

        private const val DEATH_FADE_DURATION_SECONDS =
            0.30f
    }

}

