package com.azizoglu.grisilith.enemy.trait

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.content.DamageType
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.atan2

private data class EnemySkillProjectile(
    var x: Float,
    var y: Float,
    val velocityX: Float,
    val velocityY: Float,
    val damage: Int,
    val sourceName: String,
    val damageType: DamageType,
    val visualKind: EnemyTraitProjectileVisualKind,
    val radius: Float,
    var remainingSeconds: Float,
    val ignoresObstacles: Boolean = false,
    val conditionType: com.azizoglu.grisilith.content.ConditionType? = null,
    val conditionDurationSeconds: Float? = null,
    val conditionTickDamage: Int = 0,
    val trajectoryStartX: Float = x,
    val trajectoryStartY: Float = y,
    val trajectoryEndX: Float = x + velocityX * 12f,
    val trajectoryEndY: Float = y + velocityY * 12f
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
            damageType = DamageType.PIERCING,
            visualKind = EnemyTraitProjectileVisualKind.ARROW,
            radius = 8f
        )
        cooldown = COOLDOWN_SECONDS
    }

    override fun consumeTargetDamage() =
        pendingDamage.toList().also { pendingDamage.clear() }

    override fun contactDamageMultiplier(owner: Enemy) = 1f

    override fun render(owner: Enemy, shapeRenderer: ShapeRenderer) {
        if (aimRemaining > 0f) {
            drawDottedLine(shapeRenderer, owner.centerX, owner.centerY, aimX, aimY, Color.WHITE)
        }
        drawProjectileTrajectories(shapeRenderer, projectiles)
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
    private var aimLineEndX = 0f
    private var aimLineEndY = 0f
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
                        damageType = DamageType.PIERCING,
                        visualKind = EnemyTraitProjectileVisualKind.SPEAR,
                        radius = 18f
                    )
                    phase = Phase.IDLE
                    timer = COOLDOWN_SECONDS
                }
            }
        }

        if (phase == Phase.AIMING || phase == Phase.THROWING) {
            val endpoint = rayToWorldEdge(
                startX = context.owner.centerX,
                startY = context.owner.centerY,
                throughX = aimX,
                throughY = aimY,
                worldWidth = context.worldWidth,
                worldHeight = context.worldHeight
            )
            aimLineEndX = endpoint.x
            aimLineEndY = endpoint.y
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
        1f

    override fun render(owner: Enemy, shapeRenderer: ShapeRenderer) {
        if (phase == Phase.AIMING || phase == Phase.THROWING) {
            drawDottedLine(
                shapeRenderer,
                owner.centerX,
                owner.centerY,
                aimLineEndX,
                aimLineEndY,
                Color.WHITE
            )
        }
        drawProjectileTrajectories(shapeRenderer, projectiles)
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
                    worldHeight = 68f,
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
    private var impactRemaining = 0f
    private var impactMask = 0
    private var impactScale = 1f
    private val pendingDamage = mutableListOf<EnemyTraitTargetDamage>()

    override fun update(context: EnemyTraitUpdateContext) {
        if (context.owner.isDead) return
        val dt = context.delta.coerceAtLeast(0f)

        impactRemaining = (impactRemaining - dt).coerceAtLeast(0f)

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
        val radius = BASE_RADIUS
        return dx * dx + dy * dy <= radius * radius &&
            (mask and (1 shl resolveQuadrant(dx, dy))) != 0
    }

    private fun applyHit(context: EnemyTraitUpdateContext, mask: Int) {
        val finalHit = mask == ALL_SEGMENTS
        val radius = BASE_RADIUS
        impactRemaining = IMPACT_VISUAL_SECONDS
        impactMask = mask
        impactScale = 1f
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
                source = "Swipe Around",
                damageType = DamageType.SLASHING
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
        1f

    override fun specialVisual(owner: Enemy): EnemyTraitSpecialVisual? {
        if (impactRemaining > 0f) {
            return EnemyTraitSpecialVisual(
                kind = EnemyTraitSpecialVisualKind.HORSEMAN_SWIPE,
                progress = (1f - impactRemaining / IMPACT_VISUAL_SECONDS)
                    .coerceIn(0f, 0.999f),
                scale = impactScale,
                segmentMask = impactMask,
                isImpact = true
            )
        }
        if (chargeRemaining <= 0f) return null
        return EnemyTraitSpecialVisual(
            kind = EnemyTraitSpecialVisualKind.HORSEMAN_SWIPE,
            progress = (1f - chargeRemaining / chargeDuration).coerceIn(0f, 0.999f),
            scale = 1f,
            segmentMask = chargingMask,
            isImpact = false
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
        private const val IMPACT_VISUAL_SECONDS = 0.46f
    }
}

internal class WitchElementalProjectileEnemyTrait : EnemyTrait {
    override val id = EnemyTraitIds.WITCH_ELEMENTAL_PROJECTILE

    private data class Element(
        val name: String,
        val color: Color,
        val visualKind: EnemyTraitProjectileVisualKind,
        val damageType: DamageType
    )
    private val elements = listOf(
        Element("Necrotic", Color(0.50f, 0.10f, 0.68f, 1f), EnemyTraitProjectileVisualKind.NECROTIC, DamageType.NECROTIC),
        Element("Lightning", Color(0.30f, 0.78f, 1f, 1f), EnemyTraitProjectileVisualKind.LIGHTNING, DamageType.LIGHTNING),
        Element("Air", Color(0.78f, 0.92f, 1f, 1f), EnemyTraitProjectileVisualKind.AIR, DamageType.AIR),
        Element("Poison", Color(0.35f, 0.90f, 0.18f, 1f), EnemyTraitProjectileVisualKind.POISON, DamageType.POISON)
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
                    damageType = selected.damageType,
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

    override fun contactDamageMultiplier(owner: Enemy) = 1f

    override fun render(owner: Enemy, shapeRenderer: ShapeRenderer) {
        if (aimRemaining > 0f) {
            drawDottedLine(shapeRenderer, owner.centerX, owner.centerY, aimX, aimY, Color.WHITE)
        }
        drawProjectileTrajectories(shapeRenderer, projectiles)
    }

    override fun projectileVisuals() = projectiles.map { it.toVisual() }
}

internal class WitchcraftEnemyTrait : EnemyTrait {
    override val id = EnemyTraitIds.WITCH_SUMMON_FAMILIARS

    private enum class Spell(
        val label: String,
        val radius: Float,
        val color: Color,
        val multiplier: Float,
        val damageType: DamageType
    ) {
        STATIC_CLOUD("Static Cloud", 225f, Color(0.45f, 0.78f, 1f, 0.42f), 2.0f, DamageType.LIGHTNING),
        METEOR("Meteor", 175f, Color(1f, 0.22f, 0.04f, 0.48f), 3.0f, DamageType.FIRE),
        TSUNAMI("Tsunami", 270f, Color(0.05f, 0.55f, 0.92f, 0.40f), 2.25f, DamageType.WATER),
        CALL_LIGHTNING("Call Lightning", 135f, Color(0.68f, 0.88f, 1f, 0.55f), 2.65f, DamageType.LIGHTNING)
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
        var tick: Float = 0f,
        var animationSeconds: Float = 0f
    )
    private data class TsunamiWave(
        var x: Float,
        var y: Float,
        val velocityX: Float,
        val velocityY: Float,
        var remaining: Float = 2.3f,
        var animationSeconds: Float = 0f,
        val hitTargets: MutableSet<Pair<EnemyTraitTargetType, Int>> = mutableSetOf()
    )
    private data class SpellPulse(
        val spell: Spell,
        val x: Float,
        val y: Float,
        var remaining: Float = PULSE_SECONDS
    )
    private val staticClouds = mutableListOf<StaticCloud>()
    private val tsunamiWaves = mutableListOf<TsunamiWave>()
    private val pulses = mutableListOf<SpellPulse>()

    override fun update(context: EnemyTraitUpdateContext) {
        if (context.owner.isDead) return
        val dt = context.delta.coerceAtLeast(0f)
        updateStaticClouds(context, dt)
        updateTsunamis(context, dt)
        pulses.forEach { it.remaining -= dt }
        pulses.removeAll { it.remaining <= 0f }
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
        pulses += SpellPulse(spell, targetX, targetY)
        context.combatTargets.forEach { target ->
            if (!circleOverlapsRectangle(targetX, targetY, spell.radius, target.bounds)) return@forEach
            pendingDamage += target.damage(
                amount = (context.owner.contactDamage * spell.multiplier).roundToInt().coerceAtLeast(1),
                source = spell.label,
                damageType = spell.damageType,
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
            cloud.animationSeconds += dt
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
                    damageType = Spell.STATIC_CLOUD.damageType,
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
            wave.animationSeconds += dt
            context.combatTargets.forEach { target ->
                val key = target.type to target.index
                if (key in wave.hitTargets) return@forEach
                if (!circleOverlapsRectangle(wave.x, wave.y, 115f, target.bounds)) return@forEach
                wave.hitTargets += key
                pendingDamage += target.damage(
                    amount = (context.owner.contactDamage * Spell.TSUNAMI.multiplier).roundToInt().coerceAtLeast(1),
                    source = Spell.TSUNAMI.label,
                    damageType = Spell.TSUNAMI.damageType,
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

    override fun spellVisuals(): List<EnemyTraitSpellVisual> =
        staticClouds.map { cloud ->
            EnemyTraitSpellVisual(
                kind = EnemyTraitSpellVisualKind.STATIC_CLOUD,
                centerX = cloud.x,
                centerY = cloud.y,
                worldWidth = Spell.STATIC_CLOUD.radius * 2.04f,
                worldHeight = Spell.STATIC_CLOUD.radius * 2.04f,
                animationSeconds = cloud.animationSeconds,
                alpha = 0.92f
            )
        } + tsunamiWaves.map { wave ->
            EnemyTraitSpellVisual(
                kind = EnemyTraitSpellVisualKind.TSUNAMI,
                centerX = wave.x,
                centerY = wave.y,
                worldWidth = 430f,
                worldHeight = 230f,
                animationSeconds = wave.animationSeconds,
                rotationDegrees = Math.toDegrees(
                    atan2(wave.velocityY.toDouble(), wave.velocityX.toDouble())
                ).toFloat(),
                alpha = 0.96f
            )
        } + pulses.map { pulse ->
            val isMeteor = pulse.spell == Spell.METEOR
            val diameter = pulse.spell.radius * if (isMeteor) 2.15f else 2f
            EnemyTraitSpellVisual(
                kind = if (isMeteor) {
                    EnemyTraitSpellVisualKind.METEOR
                } else {
                    EnemyTraitSpellVisualKind.CALL_LIGHTNING
                },
                centerX = pulse.x,
                centerY = pulse.y,
                worldWidth = diameter,
                worldHeight = diameter,
                progress = (1f - pulse.remaining / PULSE_SECONDS).coerceIn(0f, 0.999f),
                alpha = 0.98f
            )
        }

    override fun render(owner: Enemy, shapeRenderer: ShapeRenderer) {
        if (chargeRemaining > 0f) {
            shapeRenderer.color = spell.color
            shapeRenderer.circle(targetX, targetY, spell.radius, 64)
        }
    }

    override fun contactDamageMultiplier(owner: Enemy) = 1f

    companion object {
        private const val PULSE_SECONDS = 0.90f
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
                            amount = (context.owner.contactDamage * 0.22f).roundToInt().coerceAtLeast(1),
                            source = "Ghost Storm",
                            damageType = DamageType.NECROTIC,
                            projectileOrMagic = true,
                            conditionType = com.azizoglu.grisilith.content.ConditionType.NECROSIS,
                            conditionDurationSeconds = 5f,
                            conditionTickDamage = (context.owner.contactDamage * 0.60f).roundToInt()
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

    override fun contactDamageMultiplier(owner: Enemy) = 1f

    override fun consumeTargetDamage() =
        pendingDamage.toList().also { pendingDamage.clear() }

    override fun specialVisual(owner: Enemy): EnemyTraitSpecialVisual? {
        if (phase == Phase.WAITING) return null
        val duration = if (phase == Phase.CHARGING) CHARGE_SECONDS else STORM_SECONDS
        return EnemyTraitSpecialVisual(
            kind = EnemyTraitSpecialVisualKind.GHOST_STORM,
            progress = (1f - timer / duration).coerceIn(0f, 0.999f),
            scale = RADIUS / 285f,
            isImpact = phase == Phase.STORM
        )
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

internal class SlimeAcidicPathAndSpitTrait : EnemyTrait {
    override val id = EnemyTraitIds.SLIME_ACIDIC_PATH

    private data class Puddle(
        val x: Float,
        val y: Float,
        val diameter: Float,
        var age: Float = 0f,
        var remaining: Float,
        var tick: Float = 0f
    )

    private val puddles = mutableListOf<Puddle>()
    private val projectiles = mutableListOf<EnemySkillProjectile>()
    private val pendingDamage = mutableListOf<EnemyTraitTargetDamage>()
    private var lastDropX = Float.NaN
    private var lastDropY = Float.NaN
    private var aimRemaining = 0f
    private var aimX = 0f
    private var aimY = 0f

    override fun update(context: EnemyTraitUpdateContext) {
        if (context.owner.isDead) return
        val dt = context.delta.coerceAtLeast(0f)
        dropPathIfNeeded(context)
        updatePuddles(context, dt)
        updateProjectiles(context, projectiles, pendingDamage, null)

        context.owner.slimeRangedCooldownRemainingSeconds =
            (context.owner.slimeRangedCooldownRemainingSeconds - dt).coerceAtLeast(0f)

        if (aimRemaining > 0f) {
            aimRemaining -= dt
            if (aimRemaining <= 0f) launch(context)
            return
        }

        if (context.owner.slimeRangedCooldownRemainingSeconds <= 0f &&
            targetInRange(context, SLIME_AIM_RANGE)
        ) {
            aimX = context.targetCenterX
            aimY = context.targetCenterY
            aimRemaining = AIM_SECONDS
        }
    }

    private fun launch(context: EnemyTraitUpdateContext) {
        val owner = context.owner
        val isFragment = owner.isSlimeFragment
        val direction = Vector2(aimX - owner.centerX, aimY - owner.centerY)
        if (direction.isZero) direction.set(0f, -1f)
        direction.nor()
        projectiles += EnemySkillProjectile(
            x = owner.centerX,
            y = owner.centerY,
            velocityX = direction.x * if (isFragment) ACID_SPEED else SPIT_SPEED,
            velocityY = direction.y * if (isFragment) ACID_SPEED else SPIT_SPEED,
            damage = (owner.contactDamage * if (isFragment) 0.30f else 1.50f)
                .roundToInt().coerceAtLeast(1),
            sourceName = if (isFragment) "Acid Projectile" else "Slime Spit",
            damageType = DamageType.ACID,
            visualKind = EnemyTraitProjectileVisualKind.POISON,
            radius = if (isFragment) 18f else 54f,
            remainingSeconds = if (isFragment) 5f else 60f,
            ignoresObstacles = true,
            conditionType = com.azizoglu.grisilith.content.ConditionType.CORRODING,
            conditionDurationSeconds = 5f,
            conditionTickDamage =
                (owner.contactDamage * if (isFragment) 0.06f else 0.22f)
                    .roundToInt().coerceAtLeast(1)
        )
        owner.slimeRangedCooldownRemainingSeconds = RANGED_COOLDOWN_SECONDS
    }

    private fun dropPathIfNeeded(context: EnemyTraitUpdateContext) {
        val dx = context.owner.centerX - lastDropX
        val dy = context.owner.centerY - lastDropY
        if (!lastDropX.isNaN() && dx * dx + dy * dy < DROP_DISTANCE * DROP_DISTANCE) return
        val diameter = context.owner.visualSize * if (context.owner.isSlimeFragment) 0.72f else 1.05f
        puddles += Puddle(
            x = context.owner.centerX,
            y = context.owner.bounds.y + diameter * 0.20f,
            diameter = diameter,
            remaining = if (context.owner.isSlimeFragment) 8f else 13f
        )
        lastDropX = context.owner.centerX
        lastDropY = context.owner.centerY
    }

    private fun updatePuddles(context: EnemyTraitUpdateContext, dt: Float) {
        val iterator = puddles.iterator()
        while (iterator.hasNext()) {
            val puddle = iterator.next()
            puddle.age += dt
            puddle.remaining -= dt
            puddle.tick -= dt
            if (puddle.remaining <= 0f) {
                iterator.remove()
                continue
            }
            if (puddle.tick > 0f) continue
            puddle.tick += PATH_TICK_SECONDS
            context.combatTargets.forEach { target ->
                if (target.type == EnemyTraitTargetType.GATE) return@forEach
                if (!circleOverlapsRectangle(puddle.x, puddle.y, puddle.diameter / 2f, target.bounds)) return@forEach
                pendingDamage += target.damage(
                    amount = (context.owner.contactDamage * PATH_DAMAGE_MULTIPLIER)
                        .roundToInt().coerceAtLeast(1),
                    source = "Acidic Path",
                    damageType = DamageType.ACID,
                    projectileOrMagic = true,
                    conditionType = com.azizoglu.grisilith.content.ConditionType.CORRODING,
                    conditionDurationSeconds = 4f,
                    conditionTickDamage = (context.owner.contactDamage * 0.10f).roundToInt()
                )
            }
        }
    }

    override fun consumeTargetDamage() = pendingDamage.toList().also { pendingDamage.clear() }

    override fun projectileVisuals() = projectiles.map(EnemySkillProjectile::toVisual)

    override fun groundVisuals() = puddles.map { puddle ->
        val lifeProgress = (puddle.remaining / if (contextualFragmentDuration(puddle)) 8f else 13f)
            .coerceIn(0f, 1f)
        EnemyTraitGroundVisual(
            centerX = puddle.x,
            centerY = puddle.y,
            diameter = puddle.diameter,
            frameIndex = ((1f - lifeProgress) * 5.999f).toInt().coerceIn(0, 5),
            alpha = 0.90f
        )
    }

    private fun contextualFragmentDuration(puddle: Puddle): Boolean = puddle.diameter < 100f

    override fun render(owner: Enemy, shapeRenderer: ShapeRenderer) {
        if (aimRemaining > 0f) {
            drawDottedLine(shapeRenderer, owner.centerX, owner.centerY, aimX, aimY, Color.WHITE)
        }
        drawProjectileTrajectories(shapeRenderer, projectiles)
    }

    companion object {
        private const val DROP_DISTANCE = 58f
        private const val PATH_TICK_SECONDS = 0.65f
        private const val PATH_DAMAGE_MULTIPLIER = 0.18f
        private const val RANGED_COOLDOWN_SECONDS = 12f
        private const val AIM_SECONDS = 0.85f
        private const val SLIME_AIM_RANGE = 1600f
        private const val SPIT_SPEED = 155f
        private const val ACID_SPEED = 300f
    }
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
    damageType: DamageType,
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
        damageType = damageType,
        visualKind = visualKind,
        radius = radius * ENEMY_PROJECTILE_SIZE_MULTIPLIER,
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
        val target = context.combatTargets.firstOrNull {
            projectileOverlapsRectangle(projectile, it.bounds)
        }
        val obstacleHit = context.obstacles.any {
            projectileOverlapsRectangle(projectile, it.bounds)
        }
        val expired = projectile.remainingSeconds <= 0f ||
            projectile.x !in 0f..context.worldWidth ||
            projectile.y !in 0f..context.worldHeight
        if (target != null) {
            pendingDamage += target.damage(
                amount = projectile.damage,
                source = projectile.sourceName,
                damageType = projectile.damageType,
                projectileOrMagic = true,
                conditionType = projectile.conditionType,
                conditionDurationSeconds = projectile.conditionDurationSeconds,
                conditionTickDamage = projectile.conditionTickDamage
            )
        }
        if (target != null || (obstacleHit && !projectile.ignoresObstacles) || expired) {
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
    damageType: DamageType = DamageType.NONE,
    projectileOrMagic: Boolean = false,
    conditionType: com.azizoglu.grisilith.content.ConditionType? = null,
    conditionDurationSeconds: Float? = null,
    conditionTickDamage: Int = 0
) = EnemyTraitTargetDamage(
    targetType = type,
    targetIndex = index,
    amount = amount,
    sourceName = source,
    damageType = damageType,
    projectileOrMagic = projectileOrMagic,
    conditionType = conditionType,
    conditionDurationSeconds = conditionDurationSeconds,
    conditionTickDamage = conditionTickDamage
)

private fun EnemySkillProjectile.toVisual() = EnemyTraitProjectileVisual(
    kind = visualKind,
    centerX = x,
    centerY = y,
    velocityX = velocityX,
    velocityY = velocityY,
    worldHeight = (radius * 2f).coerceAtLeast(18f)
)

private const val ENEMY_PROJECTILE_SIZE_MULTIPLIER = 2f

private fun projectileOverlapsRectangle(
    projectile: EnemySkillProjectile,
    rectangle: Rectangle
): Boolean {
    if (projectile.visualKind != EnemyTraitProjectileVisualKind.SPEAR) {
        val closestX = projectile.x.coerceIn(rectangle.x, rectangle.x + rectangle.width)
        val closestY = projectile.y.coerceIn(rectangle.y, rectangle.y + rectangle.height)
        val dx = projectile.x - closestX
        val dy = projectile.y - closestY
        return dx * dx + dy * dy <= projectile.radius * projectile.radius
    }

    val direction = Vector2(projectile.velocityX, projectile.velocityY)
    if (direction.isZero) direction.set(1f, 0f)
    direction.nor()
    val halfLength = projectile.radius * 2.55f
    val halfThickness = projectile.radius * 0.55f
    val halfWidth = abs(direction.x) * halfLength + abs(direction.y) * halfThickness
    val halfHeight = abs(direction.y) * halfLength + abs(direction.x) * halfThickness
    return Rectangle(
        projectile.x - halfWidth,
        projectile.y - halfHeight,
        halfWidth * 2f,
        halfHeight * 2f
    ).overlaps(rectangle)
}

private fun rayToWorldEdge(
    startX: Float,
    startY: Float,
    throughX: Float,
    throughY: Float,
    worldWidth: Float,
    worldHeight: Float
): Vector2 {
    val direction = Vector2(throughX - startX, throughY - startY)
    if (direction.isZero) direction.set(1f, 0f)
    direction.nor()

    var distance = Float.POSITIVE_INFINITY
    if (direction.x > 0.0001f) distance = minOf(distance, (worldWidth - startX) / direction.x)
    if (direction.x < -0.0001f) distance = minOf(distance, (0f - startX) / direction.x)
    if (direction.y > 0.0001f) distance = minOf(distance, (worldHeight - startY) / direction.y)
    if (direction.y < -0.0001f) distance = minOf(distance, (0f - startY) / direction.y)

    val safeDistance = if (distance.isFinite()) distance.coerceAtLeast(0f) else 0f
    return Vector2(
        startX + direction.x * safeDistance,
        startY + direction.y * safeDistance
    )
}

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

private fun drawProjectileTrajectories(
    renderer: ShapeRenderer,
    projectiles: List<EnemySkillProjectile>
) {
    projectiles.forEach { projectile ->
        drawDottedLine(
            renderer,
            projectile.trajectoryStartX,
            projectile.trajectoryStartY,
            projectile.trajectoryEndX,
            projectile.trajectoryEndY,
            Color(1f, 0.08f, 0.05f, 0.92f)
        )
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
