package com.azizoglu.grisilith.enemy.trait

import com.azizoglu.grisilith.enemy.Enemy
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private data class EnemySkillProjectile(
    var x: Float,
    var y: Float,
    val velocityX: Float,
    val velocityY: Float,
    val damage: Int,
    val sourceName: String,
    val visualKind: EnemyTraitProjectileVisualKind,
    val radius: Float,
    var remainingSeconds: Float
)

private data class RestingSpear(
    val x: Float,
    val y: Float,
    val directionX: Float,
    val directionY: Float,
    var remainingSeconds: Float
)

internal class AimedArrowEnemyTrait : EnemyTrait {
    override val id = EnemyTraitIds.AIMED_ARROW

    private var cooldown = 1.5f
    private var aimRemaining = 0f
    private var aimX = 0f
    private var aimY = 0f
    private val projectiles = mutableListOf<EnemySkillProjectile>()
    private val pendingDamage = mutableListOf<EnemyTraitTargetDamage>()

    override fun update(context: EnemyTraitUpdateContext) {
        val dt = context.delta.coerceAtLeast(0f)
        if (context.owner.isDead) return

        if (aimRemaining > 0f) {
            aimX = context.targetCenterX
            aimY = context.targetCenterY
            aimRemaining -= dt
            if (aimRemaining <= 0f) fire(context)
        } else {
            cooldown -= dt
            if (cooldown <= 0f && targetInRange(context, context.owner.attackRange)) {
                aimRemaining = AIM_SECONDS
                aimX = context.targetCenterX
                aimY = context.targetCenterY
            }
        }

        updateProjectiles(context, projectiles, pendingDamage, leaveSpear = null)
    }

    private fun fire(context: EnemyTraitUpdateContext) {
        projectiles += createProjectile(
            owner = context.owner,
            targetX = aimX,
            targetY = aimY,
            speed = PROJECTILE_SPEED,
            damageMultiplier = DAMAGE_MULTIPLIER,
            sourceName = "Aimed Arrow",
            visualKind = EnemyTraitProjectileVisualKind.ARROW,
            radius = 8f
        )
        cooldown = COOLDOWN_SECONDS
    }

    override fun consumeTargetDamage() =
        pendingDamage.toList().also { pendingDamage.clear() }

    override fun contactDamageMultiplier(owner: Enemy) = 0f

    override fun render(owner: Enemy, shapeRenderer: ShapeRenderer) {
        if (aimRemaining > 0f) {
            drawDottedLine(shapeRenderer, owner.centerX, owner.centerY, aimX, aimY, Color.WHITE)
        }
    }

    override fun projectileVisuals() = projectiles.map { it.toVisual() }

    companion object {
        private const val AIM_SECONDS = 0.90f
        private const val COOLDOWN_SECONDS = 3.60f
        private const val PROJECTILE_SPEED = 690f
        private const val DAMAGE_MULTIPLIER = 2.15f
    }
}

internal class SpearmanThrowSpearEnemyTrait : EnemyTrait {
    override val id = EnemyTraitIds.SPEARMAN_THROW_THE_LANCE

    private enum class Phase { IDLE, AIMING, THROWING }

    private var phase = Phase.IDLE
    private var timer = 1.8f
    private var aimX = 0f
    private var aimY = 0f
    private val projectiles = mutableListOf<EnemySkillProjectile>()
    private val restingSpears = mutableListOf<RestingSpear>()
    private val pendingDamage = mutableListOf<EnemyTraitTargetDamage>()

    override fun update(context: EnemyTraitUpdateContext) {
        if (context.owner.isDead) return
        val dt = context.delta.coerceAtLeast(0f)

        when (phase) {
            Phase.IDLE -> {
                timer -= dt
                if (timer <= 0f && targetInRange(context, RANGE)) {
                    phase = Phase.AIMING
                    timer = AIM_SECONDS
                }
            }
            Phase.AIMING -> {
                aimX = context.targetCenterX
                aimY = context.targetCenterY
                timer -= dt
                if (timer <= 0f) {
                    phase = Phase.THROWING
                    timer = THROW_PREP_SECONDS
                }
            }
            Phase.THROWING -> {
                timer -= dt
                if (timer <= 0f) {
                    projectiles += createProjectile(
                        owner = context.owner,
                        targetX = aimX,
                        targetY = aimY,
                        speed = PROJECTILE_SPEED,
                        damageMultiplier = DAMAGE_MULTIPLIER,
                        sourceName = "Throw the Spear",
                        visualKind = EnemyTraitProjectileVisualKind.SPEAR,
                        radius = 24f
                    )
                    phase = Phase.IDLE
                    timer = COOLDOWN_SECONDS
                }
            }
        }

        updateProjectiles(context, projectiles, pendingDamage, restingSpears)
        restingSpears.forEach { it.remainingSeconds -= dt }
        restingSpears.removeAll { it.remainingSeconds <= 0f }
    }

    override fun resolveMovementOverride(owner: Enemy, output: Vector2): Boolean {
        if (phase != Phase.THROWING) return false
        output.setZero()
        return true
    }

    override fun consumeTargetDamage() =
        pendingDamage.toList().also { pendingDamage.clear() }

    override fun contactDamageMultiplier(owner: Enemy) =
        if (phase == Phase.IDLE) 1f else 0f

    override fun render(owner: Enemy, shapeRenderer: ShapeRenderer) {
        if (phase == Phase.AIMING || phase == Phase.THROWING) {
            drawDottedLine(
                shapeRenderer,
                owner.centerX,
                owner.centerY,
                aimX,
                aimY,
                Color(1f, 0.43f, 0.02f, 0.95f)
            )
        }
    }

    override fun projectileVisuals(): List<EnemyTraitProjectileVisual> =
        projectiles.map { it.toVisual() } +
            restingSpears.map { spear ->
                EnemyTraitProjectileVisual(
                    kind = EnemyTraitProjectileVisualKind.SPEAR,
                    centerX = spear.x,
                    centerY = spear.y,
                    velocityX = spear.directionX,
                    velocityY = spear.directionY,
                    worldHeight = 34f,
                    alpha = 0.72f
                )
            }

    override fun specialVisual(owner: Enemy): EnemyTraitSpecialVisual? {
        if (phase == Phase.IDLE) return null
        val elapsed = when (phase) {
            Phase.AIMING -> AIM_SECONDS - timer
            Phase.THROWING -> AIM_SECONDS + THROW_PREP_SECONDS - timer
            Phase.IDLE -> 0f
        }
        return EnemyTraitSpecialVisual(
            kind = EnemyTraitSpecialVisualKind.SPEARMAN_THROW,
            progress = (elapsed / (AIM_SECONDS + THROW_PREP_SECONDS)).coerceIn(0f, 0.999f)
        )
    }

    companion object {
        private const val RANGE = 900f
        private const val AIM_SECONDS = 1.25f
        private const val THROW_PREP_SECONDS = 0.75f
        private const val COOLDOWN_SECONDS = 10.0f
        private const val PROJECTILE_SPEED = 650f
        private const val DAMAGE_MULTIPLIER = 2.75f
    }
}

internal class HorsemanSwipeAroundEnemyTrait : EnemyTrait {
    override val id = EnemyTraitIds.HORSEMAN_BATTLE_CRY

    private val sequence = intArrayOf(0b0101, 0b1010, 0b0011, 0b1100, 0b1111)
    private var sequenceIndex = 0
    private var normalSequenceActive = false
    private var normalCooldown = 3f
    private var specialCooldown = SPECIAL_INTERVAL_SECONDS
    private var nextStepDelay = 0f
    private var sequenceTimeout = 0f
    private var chargeRemaining = 0f
    private var chargeDuration = NORMAL_CHARGE_SECONDS
    private var chargingSpecial = false
    private var chargingMask = 0
    private val pendingDamage = mutableListOf<EnemyTraitTargetDamage>()

    override fun update(context: EnemyTraitUpdateContext) {
        if (context.owner.isDead) return
        val dt = context.delta.coerceAtLeast(0f)

        if (chargeRemaining > 0f) {
            chargeRemaining -= dt
            if (chargeRemaining <= 0f) {
                applyHit(context, chargingMask)
                if (chargingSpecial) {
                    specialCooldown = SPECIAL_INTERVAL_SECONDS
                } else {
                    sequenceIndex++
                    if (sequenceIndex >= sequence.size) {
                        finishNormalSequence()
                    } else {
                        nextStepDelay = BETWEEN_ATTACK_SECONDS
                        sequenceTimeout = SEQUENCE_TIMEOUT_SECONDS
                    }
                }
            }
            return
        }

        specialCooldown = (specialCooldown - dt).coerceAtLeast(0f)

        if (normalSequenceActive) {
            nextStepDelay = (nextStepDelay - dt).coerceAtLeast(0f)
            sequenceTimeout -= dt
            if (sequenceTimeout <= 0f) {
                finishNormalSequence()
                return
            }
            val mask = sequence[sequenceIndex]
            if (nextStepDelay <= 0f && playerIsInsideMask(context, mask)) {
                beginCharge(mask, special = false)
            }
            return
        }

        normalCooldown = (normalCooldown - dt).coerceAtLeast(0f)

        if (specialCooldown <= 0f && playerIsInsideMask(context, ALL_SEGMENTS)) {
            beginCharge(ALL_SEGMENTS, special = true)
            return
        }

        if (normalCooldown <= 0f && playerIsInsideMask(context, sequence.first())) {
            normalSequenceActive = true
            sequenceIndex = 0
            beginCharge(sequence.first(), special = false)
        }
    }

    private fun beginCharge(mask: Int, special: Boolean) {
        chargingMask = mask
        chargingSpecial = special
        chargeDuration = if (mask == ALL_SEGMENTS) NORMAL_CHARGE_SECONDS * 1.5f else NORMAL_CHARGE_SECONDS
        chargeRemaining = chargeDuration
    }

    private fun finishNormalSequence() {
        normalSequenceActive = false
        sequenceIndex = 0
        nextStepDelay = 0f
        sequenceTimeout = 0f
        normalCooldown = NORMAL_COOLDOWN_SECONDS
    }

    private fun playerIsInsideMask(context: EnemyTraitUpdateContext, mask: Int): Boolean {
        val player = context.combatTargets.firstOrNull { it.type == EnemyTraitTargetType.PLAYER }
            ?: return false
        val dx = player.centerX - context.owner.centerX
        val dy = player.centerY - context.owner.centerY
        val radius = if (mask == ALL_SEGMENTS) BASE_RADIUS * 1.5f else BASE_RADIUS
        return dx * dx + dy * dy <= radius * radius &&
            (mask and (1 shl resolveQuadrant(dx, dy))) != 0
    }

    private fun applyHit(context: EnemyTraitUpdateContext, mask: Int) {
        val finalHit = mask == ALL_SEGMENTS
        val radius = if (finalHit) BASE_RADIUS * 1.5f else BASE_RADIUS
        context.combatTargets.forEach { target ->
            val dx = target.centerX - context.owner.centerX
            val dy = target.centerY - context.owner.centerY
            if (dx * dx + dy * dy > radius * radius) return@forEach
            val quadrant = resolveQuadrant(dx, dy)
            if ((mask and (1 shl quadrant)) == 0) return@forEach
            pendingDamage += target.damage(
                amount = (context.owner.contactDamage * if (finalHit) 2.0f else 1.45f)
                    .roundToInt()
                    .coerceAtLeast(1),
                source = "Swipe Around"
            )
        }
    }

    override fun resolveMovementOverride(owner: Enemy, output: Vector2): Boolean {
        if (chargeRemaining <= 0f) return false
        output.setZero()
        return true
    }

    override fun consumeTargetDamage() =
        pendingDamage.toList().also { pendingDamage.clear() }

    override fun contactDamageMultiplier(owner: Enemy) =
        if (chargeRemaining > 0f) 0f else 1f

    override fun render(owner: Enemy, shapeRenderer: ShapeRenderer) {
        if (chargeRemaining <= 0f) return
        val finalHit = chargingMask == ALL_SEGMENTS
        val radius = if (finalHit) BASE_RADIUS * 1.5f else BASE_RADIUS
        val progress = (1f - chargeRemaining / chargeDuration).coerceIn(0f, 1f)
        val color = Color(1f, 0.20f + progress * 0.35f, 0.04f, 0.18f + progress * 0.35f)
        drawQuadrantMask(shapeRenderer, owner.centerX, owner.centerY, radius, chargingMask, color)
    }

    override fun specialVisual(owner: Enemy): EnemyTraitSpecialVisual? {
        if (chargeRemaining <= 0f || chargingMask != ALL_SEGMENTS) return null
        return EnemyTraitSpecialVisual(
            kind = EnemyTraitSpecialVisualKind.HORSEMAN_SWIPE,
            progress = (1f - chargeRemaining / chargeDuration).coerceIn(0f, 0.999f),
            scale = if (chargingMask == ALL_SEGMENTS) 1.5f else 1f
        )
    }

    companion object {
        private const val BASE_RADIUS = 310f
        private const val ALL_SEGMENTS = 0b1111
        private const val NORMAL_CHARGE_SECONDS = 3.0f
        private const val BETWEEN_ATTACK_SECONDS = 5.0f
        private const val SEQUENCE_TIMEOUT_SECONDS = 15f
        private const val NORMAL_COOLDOWN_SECONDS = 10f
        private const val SPECIAL_INTERVAL_SECONDS = 25f
    }
}

internal class WitchElementalProjectileEnemyTrait : EnemyTrait {
    override val id = EnemyTraitIds.WITCH_ELEMENTAL_PROJECTILE

    private data class Element(
        val name: String,
        val color: Color,
        val visualKind: EnemyTraitProjectileVisualKind
    )
    private val elements = listOf(
        Element("Necrotic", Color(0.50f, 0.10f, 0.68f, 1f), EnemyTraitProjectileVisualKind.NECROTIC),
        Element("Lightning", Color(0.30f, 0.78f, 1f, 1f), EnemyTraitProjectileVisualKind.LIGHTNING),
        Element("Air", Color(0.78f, 0.92f, 1f, 1f), EnemyTraitProjectileVisualKind.AIR),
        Element("Poison", Color(0.35f, 0.90f, 0.18f, 1f), EnemyTraitProjectileVisualKind.POISON)
    )
    private var cooldown = 1.2f
    private var aimRemaining = 0f
    private var aimX = 0f
    private var aimY = 0f
    private var selected = elements.first()
    private val projectiles = mutableListOf<EnemySkillProjectile>()
    private val pendingDamage = mutableListOf<EnemyTraitTargetDamage>()

    override fun update(context: EnemyTraitUpdateContext) {
        if (context.owner.isDead) return
        val dt = context.delta.coerceAtLeast(0f)
        if (aimRemaining > 0f) {
            aimX = context.targetCenterX
            aimY = context.targetCenterY
            aimRemaining -= dt
            if (aimRemaining <= 0f) {
                projectiles += createProjectile(
                    owner = context.owner,
                    targetX = aimX,
                    targetY = aimY,
                    speed = 610f,
                    damageMultiplier = 1.55f,
                    sourceName = "${selected.name} Projectile",
                    visualKind = selected.visualKind,
                    radius = 12f
                )
                cooldown = 1.80f
            }
        } else {
            cooldown -= dt
            if (cooldown <= 0f && targetInRange(context, context.owner.attackRange)) {
                selected = elements.random()
                aimRemaining = 0.48f
                aimX = context.targetCenterX
                aimY = context.targetCenterY
            }
        }
        updateProjectiles(context, projectiles, pendingDamage, leaveSpear = null)
    }

    override fun consumeTargetDamage() =
        pendingDamage.toList().also { pendingDamage.clear() }

    override fun contactDamageMultiplier(owner: Enemy) = 0f

    override fun render(owner: Enemy, shapeRenderer: ShapeRenderer) {
        if (aimRemaining > 0f) {
            drawDottedLine(shapeRenderer, owner.centerX, owner.centerY, aimX, aimY, Color.WHITE)
        }
    }

    override fun projectileVisuals() = projectiles.map { it.toVisual() }
}

internal class WitchcraftEnemyTrait : EnemyTrait {
    override val id = EnemyTraitIds.WITCH_SUMMON_FAMILIARS

    private enum class Spell(val label: String, val radius: Float, val color: Color, val multiplier: Float) {
        STATIC_CLOUD("Static Cloud", 225f, Color(0.45f, 0.78f, 1f, 0.42f), 2.0f),
        METEOR("Meteor", 175f, Color(1f, 0.22f, 0.04f, 0.48f), 3.0f),
        TSUNAMI("Tsunami", 270f, Color(0.05f, 0.55f, 0.92f, 0.40f), 2.25f),
        CALL_LIGHTNING("Call Lightning", 135f, Color(0.68f, 0.88f, 1f, 0.55f), 2.65f)
    }

    private var cooldown = 4f
    private var chargeRemaining = 0f
    private var spell = Spell.STATIC_CLOUD
    private var targetX = 0f
    private var targetY = 0f
    private val pendingDamage = mutableListOf<EnemyTraitTargetDamage>()
    private data class StaticCloud(
        val x: Float,
        val y: Float,
        var remaining: Float = 4.2f,
        var tick: Float = 0f
    )
    private data class TsunamiWave(
        var x: Float,
        var y: Float,
        val velocityX: Float,
        val velocityY: Float,
        var remaining: Float = 2.3f,
        val hitTargets: MutableSet<Pair<EnemyTraitTargetType, Int>> = mutableSetOf()
    )
    private val staticClouds = mutableListOf<StaticCloud>()
    private val tsunamiWaves = mutableListOf<TsunamiWave>()

    override fun update(context: EnemyTraitUpdateContext) {
        if (context.owner.isDead) return
        val dt = context.delta.coerceAtLeast(0f)
        updateStaticClouds(context, dt)
        updateTsunamis(context, dt)
        if (chargeRemaining > 0f) {
            chargeRemaining -= dt
            if (chargeRemaining <= 0f) cast(context)
            return
        }
        cooldown -= dt
        if (cooldown <= 0f) {
            spell = Spell.values().random()
            targetX = context.targetCenterX
            targetY = context.targetCenterY
            chargeRemaining = 1.15f
            cooldown = 10f
        }
    }

    private fun cast(context: EnemyTraitUpdateContext) {
        if (spell == Spell.STATIC_CLOUD) {
            staticClouds += StaticCloud(targetX, targetY)
            return
        }
        if (spell == Spell.TSUNAMI) {
            val direction = Vector2(
                targetX - context.owner.centerX,
                targetY - context.owner.centerY
            )
            if (direction.isZero) direction.set(0f, -1f)
            direction.nor().scl(420f)
            tsunamiWaves += TsunamiWave(
                x = context.owner.centerX,
                y = context.owner.centerY,
                velocityX = direction.x,
                velocityY = direction.y
            )
            return
        }
        context.combatTargets.forEach { target ->
            if (!circleOverlapsRectangle(targetX, targetY, spell.radius, target.bounds)) return@forEach
            pendingDamage += target.damage(
                amount = (context.owner.contactDamage * spell.multiplier).roundToInt().coerceAtLeast(1),
                source = spell.label,
                projectileOrMagic = true
            )
        }
    }

    private fun updateStaticClouds(context: EnemyTraitUpdateContext, dt: Float) {
        val iterator = staticClouds.iterator()
        while (iterator.hasNext()) {
            val cloud = iterator.next()
            cloud.remaining -= dt
            cloud.tick -= dt
            if (cloud.remaining <= 0f) {
                iterator.remove()
                continue
            }
            if (cloud.tick > 0f) continue
            cloud.tick += 0.75f
            context.combatTargets.forEach { target ->
                if (!circleOverlapsRectangle(cloud.x, cloud.y, Spell.STATIC_CLOUD.radius, target.bounds)) return@forEach
                pendingDamage += target.damage(
                    amount = (context.owner.contactDamage * 0.55f).roundToInt().coerceAtLeast(1),
                    source = Spell.STATIC_CLOUD.label,
                    projectileOrMagic = true
                )
            }
        }
    }

    private fun updateTsunamis(context: EnemyTraitUpdateContext, dt: Float) {
        val iterator = tsunamiWaves.iterator()
        while (iterator.hasNext()) {
            val wave = iterator.next()
            wave.x += wave.velocityX * dt
            wave.y += wave.velocityY * dt
            wave.remaining -= dt
            context.combatTargets.forEach { target ->
                val key = target.type to target.index
                if (key in wave.hitTargets) return@forEach
                if (!circleOverlapsRectangle(wave.x, wave.y, 115f, target.bounds)) return@forEach
                wave.hitTargets += key
                pendingDamage += target.damage(
                    amount = (context.owner.contactDamage * Spell.TSUNAMI.multiplier).roundToInt().coerceAtLeast(1),
                    source = Spell.TSUNAMI.label,
                    projectileOrMagic = true
                )
            }
            if (
                wave.remaining <= 0f ||
                wave.x !in 0f..context.worldWidth ||
                wave.y !in 0f..context.worldHeight
            ) iterator.remove()
        }
    }

    override fun consumeTargetDamage() =
        pendingDamage.toList().also { pendingDamage.clear() }

    override fun render(owner: Enemy, shapeRenderer: ShapeRenderer) {
        staticClouds.forEach { cloud ->
            shapeRenderer.color = Spell.STATIC_CLOUD.color
            shapeRenderer.circle(cloud.x, cloud.y, Spell.STATIC_CLOUD.radius, 64)
        }
        tsunamiWaves.forEach { wave ->
            shapeRenderer.color = Spell.TSUNAMI.color
            shapeRenderer.circle(wave.x, wave.y, 115f, 40)
        }
        if (chargeRemaining > 0f) {
            shapeRenderer.color = spell.color
            shapeRenderer.circle(targetX, targetY, spell.radius, 64)
        }
    }
}

internal class GhostStormEnemyTrait : EnemyTrait {
    override val id = EnemyTraitIds.GHOST_HAUNTING_PRESENCE

    private enum class Phase { WAITING, CHARGING, STORM }
    private var phase = Phase.WAITING
    private var timer = 5f
    private var tick = 0f
    private val pendingDamage = mutableListOf<EnemyTraitTargetDamage>()

    override fun update(context: EnemyTraitUpdateContext) {
        if (context.owner.isDead) return
        val dt = context.delta.coerceAtLeast(0f)
        timer -= dt
        when (phase) {
            Phase.WAITING -> if (timer <= 0f) {
                phase = Phase.CHARGING
                timer = CHARGE_SECONDS
            }
            Phase.CHARGING -> if (timer <= 0f) {
                phase = Phase.STORM
                timer = STORM_SECONDS
                tick = 0f
            }
            Phase.STORM -> {
                tick -= dt
                if (tick <= 0f) {
                    tick += TICK_SECONDS
                    context.combatTargets.forEach { target ->
                        if (!circleOverlapsRectangle(context.owner.centerX, context.owner.centerY, RADIUS, target.bounds)) return@forEach
                        pendingDamage += target.damage(
                            amount = (context.owner.contactDamage * 0.70f).roundToInt().coerceAtLeast(1),
                            source = "Ghost Storm",
                            projectileOrMagic = true
                        )
                    }
                }
                if (timer <= 0f) {
                    phase = Phase.WAITING
                    timer = COOLDOWN_SECONDS
                }
            }
        }
    }

    override fun resolveMovementOverride(owner: Enemy, output: Vector2): Boolean {
        if (phase == Phase.WAITING) return false
        output.setZero()
        return true
    }

    override fun shouldNegateIncomingDamage(owner: Enemy, context: EnemyTraitDamageContext): Boolean {
        return phase == Phase.STORM
    }

    override fun contactDamageMultiplier(owner: Enemy) = 0f

    override fun consumeTargetDamage() =
        pendingDamage.toList().also { pendingDamage.clear() }

    override fun render(owner: Enemy, shapeRenderer: ShapeRenderer) {
        if (phase == Phase.WAITING) return
        shapeRenderer.color =
            if (phase == Phase.CHARGING) Color(0.72f, 0.80f, 1f, 0.25f)
            else Color(0.70f, 0.86f, 1f, 0.50f)
        shapeRenderer.circle(owner.centerX, owner.centerY, RADIUS, 64)
        if (phase == Phase.STORM) {
            repeat(9) { index ->
                val angle = (index * 40f + timer * 150f) * 0.017453292f
                shapeRenderer.circle(
                    owner.centerX + cos(angle) * RADIUS * 0.70f,
                    owner.centerY + sin(angle) * RADIUS * 0.70f,
                    12f,
                    16
                )
            }
        }
    }

    companion object {
        private const val CHARGE_SECONDS = 1.25f
        private const val STORM_SECONDS = 2.40f
        private const val COOLDOWN_SECONDS = 12f
        private const val TICK_SECONDS = 0.60f
        private const val RADIUS = 285f
    }
}

internal class SlimeRealSplitMarkerTrait : EnemyTrait {
    override val id = EnemyTraitIds.SLIME_SPLIT_ON_DEATH
}

private fun targetInRange(context: EnemyTraitUpdateContext, range: Float): Boolean {
    val dx = context.targetCenterX - context.owner.centerX
    val dy = context.targetCenterY - context.owner.centerY
    return dx * dx + dy * dy <= range * range
}

private fun createProjectile(
    owner: Enemy,
    targetX: Float,
    targetY: Float,
    speed: Float,
    damageMultiplier: Float,
    sourceName: String,
    visualKind: EnemyTraitProjectileVisualKind,
    radius: Float
): EnemySkillProjectile {
    val direction = Vector2(targetX - owner.centerX, targetY - owner.centerY)
    if (direction.isZero) direction.set(0f, -1f)
    direction.nor()
    return EnemySkillProjectile(
        x = owner.centerX,
        y = owner.centerY,
        velocityX = direction.x * speed,
        velocityY = direction.y * speed,
        damage = (owner.contactDamage * damageMultiplier).roundToInt().coerceAtLeast(1),
        sourceName = sourceName,
        visualKind = visualKind,
        radius = radius,
        remainingSeconds = 2.8f
    )
}

private fun updateProjectiles(
    context: EnemyTraitUpdateContext,
    projectiles: MutableList<EnemySkillProjectile>,
    pendingDamage: MutableList<EnemyTraitTargetDamage>,
    leaveSpear: MutableList<RestingSpear>?
) {
    val dt = context.delta.coerceAtLeast(0f)
    val iterator = projectiles.iterator()
    while (iterator.hasNext()) {
        val projectile = iterator.next()
        projectile.x += projectile.velocityX * dt
        projectile.y += projectile.velocityY * dt
        projectile.remainingSeconds -= dt
        val hitBounds = Rectangle(
            projectile.x - projectile.radius,
            projectile.y - projectile.radius,
            projectile.radius * 2f,
            projectile.radius * 2f
        )
        val target = context.combatTargets.firstOrNull { hitBounds.overlaps(it.bounds) }
        val obstacleHit = context.obstacles.any { hitBounds.overlaps(it.bounds) }
        val expired = projectile.remainingSeconds <= 0f ||
            projectile.x !in 0f..context.worldWidth ||
            projectile.y !in 0f..context.worldHeight
        if (target != null) {
            pendingDamage += target.damage(
                amount = projectile.damage,
                source = projectile.sourceName,
                projectileOrMagic = true
            )
        }
        if (target != null || obstacleHit || expired) {
            if (leaveSpear != null) {
                val direction = Vector2(projectile.velocityX, projectile.velocityY).nor()
                leaveSpear += RestingSpear(
                    projectile.x,
                    projectile.y,
                    direction.x,
                    direction.y,
                    2.3f
                )
            }
            iterator.remove()
        }
    }
}

private fun EnemyTraitCombatTarget.damage(
    amount: Int,
    source: String,
    projectileOrMagic: Boolean = false
) = EnemyTraitTargetDamage(
    targetType = type,
    targetIndex = index,
    amount = amount,
    sourceName = source,
    projectileOrMagic = projectileOrMagic
)

private fun EnemySkillProjectile.toVisual() = EnemyTraitProjectileVisual(
    kind = visualKind,
    centerX = x,
    centerY = y,
    velocityX = velocityX,
    velocityY = velocityY,
    worldHeight = (radius * 2f).coerceAtLeast(18f)
)

private fun drawDottedLine(
    renderer: ShapeRenderer,
    startX: Float,
    startY: Float,
    endX: Float,
    endY: Float,
    color: Color
) {
    val direction = Vector2(endX - startX, endY - startY)
    val length = direction.len()
    if (length <= 1f) return
    direction.nor()
    renderer.color = color
    var distance = 14f
    while (distance < length) {
        renderer.circle(
            startX + direction.x * distance,
            startY + direction.y * distance,
            3.2f,
            8
        )
        distance += 22f
    }
}

private fun resolveQuadrant(dx: Float, dy: Float): Int =
    when {
        dy >= abs(dx) -> 0
        dx >= abs(dy) -> 1
        -dy >= abs(dx) -> 2
        else -> 3
    }

private fun drawQuadrantMask(
    renderer: ShapeRenderer,
    centerX: Float,
    centerY: Float,
    radius: Float,
    mask: Int,
    color: Color
) {
    renderer.color = color
    repeat(4) { quadrant ->
        if ((mask and (1 shl quadrant)) == 0) return@repeat
        val centerAngle = when (quadrant) {
            0 -> 90f
            1 -> 0f
            2 -> 270f
            else -> 180f
        }
        val start = centerAngle - 45f
        repeat(12) { segment ->
            val angleA = (start + segment * 7.5f) * 0.017453292f
            val angleB = (start + (segment + 1) * 7.5f) * 0.017453292f
            renderer.triangle(
                centerX,
                centerY,
                centerX + cos(angleA) * radius,
                centerY + sin(angleA) * radius,
                centerX + cos(angleB) * radius,
                centerY + sin(angleB) * radius
            )
        }
    }
}

private fun circleOverlapsRectangle(
    centerX: Float,
    centerY: Float,
    radius: Float,
    rectangle: Rectangle
): Boolean {
    val closestX = centerX.coerceIn(rectangle.x, rectangle.x + rectangle.width)
    val closestY = centerY.coerceIn(rectangle.y, rectangle.y + rectangle.height)
    val dx = centerX - closestX
    val dy = centerY - closestY
    return dx * dx + dy * dy <= radius * radius
}
