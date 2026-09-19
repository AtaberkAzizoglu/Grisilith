package com.azizoglu.grisilith.player

import com.azizoglu.grisilith.combat.HealthComponent
import com.azizoglu.grisilith.combat.HealingVisualType
import com.azizoglu.grisilith.combat.healing.HealingVisualManager
import com.azizoglu.grisilith.combat.healing.HealingVisualAnchor
import com.azizoglu.grisilith.combat.condition.ConditionApplication
import com.azizoglu.grisilith.combat.condition.ConditionContainer
import com.azizoglu.grisilith.content.ConditionType
import com.azizoglu.grisilith.combat.affinity.DamageAffinityProfile
import com.azizoglu.grisilith.combat.affinity.DamageAffinityResolver
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.combat.damage.DamageNumberEvent
import com.azizoglu.grisilith.character.visual.CharacterVisualState
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.item.runtime.ActiveItemEffects
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

    private val visualState =
        CharacterVisualState()

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

    private var terrainMovementSpeedMultiplier =
        1f

    private var damageAffinityProfile =
        DamageAffinityProfile.NONE

    val health =
        HealthComponent(
            maximumHealth =
                stats.calculatedMaximumHealth()
        )

    private val pendingDamageNumberEvents = mutableListOf<DamageNumberEvent>()

    init {
        health.onHealingApplied = { _, type ->
            HealingVisualManager.spawnFollowing(type) {
                HealingVisualAnchor(centerX, centerY, size)
            }
        }
    }

    /** Active gameplay conditions, also consumed by ConditionVisualRenderer. */
    val conditions =
        ConditionContainer()

    val centerX: Float
        get() = position.x + size / 2f

    val centerY: Float
        get() = position.y + size / 2f

    /*
     * position + size visual frame'i temsil eder.
     * bounds yalnızca alt gövde / ayak gameplay footprint'idir.
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

    val currentVisualState:
        CharacterVisualState
        get() =
            visualState

    /*
     * Visual carrier/minion systems need the player's current configured
     * movement speed even while the player is standing still.
     *
     * This is read-only and does not alter player movement.
     */
    val followerReferenceMovementSpeed:
        Float
        get() =
            calculatedMovementSpeed()

    fun update(
        delta:
        Float
    ) {
        if (health.isDead) {
            actualMovementSpeed =
                0f

            accumulatedRegeneration =
                0f

            return
        }

        val safeDelta =
            delta.coerceAtLeast(
                0f
            )

        conditions.update(
            delta = safeDelta,
            onTick = { tick ->
                val tickDamage = tick.source.resolvedTickDamage(health.maximumHealth)
                if (tickDamage > 0) {
                    takeDamage(tickDamage, tick.source.damageType)
                }
            }
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

        updateHealthRegeneration(
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
        return stats.calculatedMovementSpeed() *
            terrainMovementSpeedMultiplier
    }

    fun setTerrainMovementSpeedMultiplier(multiplier: Float) {
        terrainMovementSpeedMultiplier =
            multiplier.coerceIn(0f, 1f)
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
        if (delta <= 0f) {
            return
        }

        if (
            health.currentHealth >=
            health.maximumHealth
        ) {
            accumulatedRegeneration = 0f
            return
        }

        val regenerationPerSecond =
            stats.hpRegenerationPerSecond(
                currentMaximumHealth =
                    health.maximumHealth
            )

        if (regenerationPerSecond <= 0f) {
            accumulatedRegeneration = 0f
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
                (wholeHealthPoints *
                    ActiveItemEffects.healingMultiplier())
                    .toInt()
            )

        if (
            health.currentHealth >=
            health.maximumHealth
        ) {
            accumulatedRegeneration = 0f
        } else {
            accumulatedRegeneration =
                (
                    accumulatedRegeneration -
                        healedAmount.toFloat()
                    )
                    .coerceAtLeast(0f)
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

        accumulatedRegeneration =
            0f
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

    /*
     * Bağlamı bilinmeyen hasar savunmadan geçirilmez.
     * Eski çağrıların davranışı güvenli biçimde korunur.
     */
    fun takeDamage(
        amount: Int
    ): Int {
        return takeDamage(
            amount =
                amount,

            damageType =
                DamageType.NONE
        )
    }

    fun takeDamage(
        amount: Int,
        damageType: DamageType
    ): Int {
        val itemModifiedDamage =
            (amount * ActiveItemEffects.incomingDamageMultiplier())
                .roundToInt()

        val affinityResolvedDamage =
            DamageAffinityResolver
                .resolve(
                    requestedDamage =
                        itemModifiedDamage,

                    damageType =
                        damageType,

                    profile =
                        damageAffinityProfile
                )

        if (
            affinityResolvedDamage <=
            0
        ) {
            return 0
        }

        val defendedDamage =
            if (damageType.isMagical) {
                (affinityResolvedDamage - stats.rawMagicDamageReduction())
                    .roundToInt().coerceAtLeast(1)
            } else affinityResolvedDamage

        return applyHealthDamage(ActiveItemEffects.resolveDamageBeforeHealth(defendedDamage))
    }

    fun setDamageAffinityProfile(
        profile: DamageAffinityProfile
    ) {
        damageAffinityProfile =
            profile
    }

    fun damageAffinityProfile():
        DamageAffinityProfile {
        return damageAffinityProfile
    }

    /*
     * Yalnızca uygun Melee/Ranged fiziksel saldırılar için
     * düz Armor azaltması uygulanır.
     */
    fun takePhysicalWeaponDamage(
        amount: Int,
        attackerPiercingIgnoreRatio:
        Float,
        damageType: DamageType =
            DamageType.SLASHING
    ): Int {
        if (amount <= 0) {
            return 0
        }

        // Magical attacks never use physical Armor, even through an older caller.
        if (damageType.isMagical) return takeDamage(amount, damageType)

        val itemModifiedDamage =
            (amount * ActiveItemEffects.incomingDamageMultiplier())
                .roundToInt()

        val affinityResolvedDamage =
            DamageAffinityResolver
                .resolve(
                    requestedDamage =
                        itemModifiedDamage,

                    damageType =
                        damageType,

                    profile =
                        damageAffinityProfile
                )

        if (
            affinityResolvedDamage <=
            0
        ) {
            return 0
        }

        val damageReduction =
            stats.effectiveArmorDamageReduction(
                attackerPiercingIgnoreRatio =
                    attackerPiercingIgnoreRatio
            )

        val reducedDamage =
            (
                affinityResolvedDamage
                    .toFloat() -
                    damageReduction
                )
                .roundToInt()
                .coerceAtLeast(1)

        return applyHealthDamage(ActiveItemEffects.resolveDamageBeforeHealth(reducedDamage))
    }

    private fun applyHealthDamage(amount: Int): Int {
        val applied = health.takeDamage(amount)
        if (applied > 0) {
            pendingDamageNumberEvents += DamageNumberEvent(
                amount = applied,
                worldX = centerX,
                worldY = centerY + size * 0.72f
            )
        }
        return applied
    }

    fun consumeDamageNumberEvents(): List<DamageNumberEvent> {
        if (pendingDamageNumberEvents.isEmpty()) return emptyList()
        return pendingDamageNumberEvents.toList().also { pendingDamageNumberEvents.clear() }
    }

    fun constrainCenterToCircle(
        circleCenterX: Float,
        circleCenterY: Float,
        radius: Float
    ) {
        if (radius <= 0f) return
        val dx = centerX - circleCenterX
        val dy = centerY - circleCenterY
        val distanceSquared = dx * dx + dy * dy
        val maximumDistance = (radius - bounds.width * 0.5f).coerceAtLeast(0f)
        if (distanceSquared <= maximumDistance * maximumDistance) return
        val distance = kotlin.math.sqrt(distanceSquared).coerceAtLeast(0.001f)
        val targetCenterX = circleCenterX + dx / distance * maximumDistance
        val targetCenterY = circleCenterY + dy / distance * maximumDistance
        position.x += targetCenterX - centerX
        position.y += targetCenterY - centerY
    }

    fun sacrificeHalfCurrentHealth() {
        val current = health.currentHealth
        if (current <= 1) return
        health.takeDamage(
            (current / 2).coerceIn(1, current - 1)
        )
    }

    /*
     * Çağıran sistem saldırının:
     * - Melee veya Ranged Weapon'dan,
     * - BLUDGEONING / PIERCING / SLASHING tipinden,
     * - başarılı bir hedef vuruşundan
     * geldiğini doğrulamalıdır.
     */
    fun tryLifeSteal(
        isEligibleHit: Boolean
    ) {
        if (
            !isEligibleHit ||
            health.isDead ||
            health.currentHealth >=
            health.maximumHealth
        ) {
            return
        }

        val lifeStealChance =
            stats.lifeStealChance()

        if (
            lifeStealChance <= 0f ||
            !MathUtils.randomBoolean(
                lifeStealChance
            )
        ) {
            return
        }

        val maximumHealing =
            stats
                .maximumLifeStealHealingPerProc()

        val healingAmount =
            if (maximumHealing <= 1) {
                1
            } else {
                MathUtils.random(
                    1,
                    maximumHealing
                )
            }

        health.heal(
            (healingAmount *
                ActiveItemEffects.healingMultiplier())
                .toInt(),
            HealingVisualType.LIFE_STEAL
        )
    }

    fun applyCondition(application: ConditionApplication) {
        if (!health.isDead) conditions.apply(application)
    }

    fun applyCondition(type: ConditionType, durationSeconds: Float? = null) {
        if (!health.isDead) conditions.apply(type, durationSeconds)
    }

    fun hasCondition(type: ConditionType): Boolean = conditions.contains(type)

    fun removeCondition(type: ConditionType): Boolean = conditions.remove(type)

    fun render(
        shapeRenderer: ShapeRenderer,
        renderBody: Boolean = true
    ) {
        if (renderBody) {
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
        }

        renderHealthBar(
            shapeRenderer
        )
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
        val safeDelta =
            delta.coerceAtLeast(
                0f
            )

        updateActualMovementSpeed(
            delta =
                safeDelta
        )

        visualState.update(
            delta =
                safeDelta,

            movementX =
                position.x -
                    previousPosition.x,

            movementY =
                position.y -
                    previousPosition.y
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


    companion object {

        private const val COLLISION_WIDTH_RATIO =
            0.56f

        private const val COLLISION_HEIGHT_RATIO =
            0.40f

        private const val COLLISION_OFFSET_X_RATIO =
            0.22f

        private const val COLLISION_OFFSET_Y_RATIO =
            0.04f
    }

}

