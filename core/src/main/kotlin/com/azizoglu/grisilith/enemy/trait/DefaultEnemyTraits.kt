package com.azizoglu.grisilith.enemy.trait

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.content.ConditionType
import com.azizoglu.grisilith.combat.HealingVisualType
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyDamageContext
import com.azizoglu.grisilith.enemy.EnemyType
import kotlin.math.roundToInt
import kotlin.random.Random
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.MathUtils
import com.azizoglu.grisilith.weapon.visual.attack.MeleeAttackVisualKind
import com.azizoglu.grisilith.weapon.visual.attack.MeleeAttackVisualManager

internal object DefaultEnemyTraits {

    fun create(
        id: ContentId
    ): EnemyTrait? {
        return when (
            id
        ) {
            EnemyTraitIds.AIMED_ARROW ->
                AimedArrowEnemyTrait()

            EnemyTraitIds.WITCH_ELEMENTAL_PROJECTILE ->
                WitchElementalProjectileEnemyTrait()
            EnemyTraitIds.WOLF_PACK ->
                WolfPackTrait()

            EnemyTraitIds.BOAR_CHARGE ->
                BoarChargeTrait()

            EnemyTraitIds.GRIZZLY_ANGRY ->
                GrizzlyAngryTrait()

            EnemyTraitIds.CROCODILE_JUMP ->
                CrocodileJumpTrait()

            EnemyTraitIds.SWARM_BEING ->
                SwarmBeingTrait()

            EnemyTraitIds.GORILLA_THROW ->
                GorillaThrowTrait()

            EnemyTraitIds.GORILLA_STOMP_THE_EARTH ->
                GorillaStompTheEarthTrait()

            EnemyTraitIds.SPEARMAN_THROW_THE_LANCE ->
                SpearmanThrowSpearEnemyTrait()

            EnemyTraitIds.HORSEMAN_BATTLE_CRY ->
                HorsemanSwipeAroundEnemyTrait()

            EnemyTraitIds.WITCH_SUMMON_FAMILIARS ->
                WitchcraftEnemyTrait()

            EnemyTraitIds.GHOST_INCORPOREAL_EVASION ->
                GhostIncorporealEvasionTrait()

            EnemyTraitIds.GHOST_HAUNTING_PRESENCE ->
                GhostStormEnemyTrait()

            EnemyTraitIds.SLIME_ACIDIC_PATH ->
                SlimeAcidicPathAndSpitTrait()

            EnemyTraitIds.SLIME_SPLIT_ON_DEATH ->
                SlimeRealSplitMarkerTrait()

            EnemyTraitIds.VAMPIRE_MISTY_TRANSPORTATION ->
                VampireMistyEscapeTrait()

            EnemyTraitIds.VAMPIRE_HUNGER ->
                VampireHungerTrait()

            EnemyTraitIds.MIMICRY ->
                MimicryTrait()

            EnemyTraitIds.BABY_CROCODILES ->
                BabyCrocodilesTrait()

            EnemyTraitIds.SPLIT_THE_SWARM ->
                SplitTheSwarmTrait()

            /*
             * Aktif Trait'ler sonraki paketlerde aynı factory'ye
             * eklenecek. Bilinmeyen ID hata üretmez; content hook
             * olarak saklanmaya devam eder.
             */
            else ->
                null
        }
    }
}

private class WolfPackTrait :
    EnemyTrait {

    override val id =
        EnemyTraitIds.WOLF_PACK

    private var currentModifiers =
        EnemyTraitModifiers.IDENTITY

    override fun update(
        context: EnemyTraitUpdateContext
    ) {
        val owner =
            context.owner

        val kindId =
            owner.kindId
                ?: run {
                    currentModifiers =
                        EnemyTraitModifiers.IDENTITY
                    return
                }

        var nearbyPackMembers =
            0

        context
            .nearbyEnemies
            .forEach { other ->
                if (
                    other === owner ||
                    other.isDead ||
                    other.kindId !=
                    kindId
                ) {
                    return@forEach
                }

                val differenceX =
                    other.centerX -
                        owner.centerX

                val differenceY =
                    other.centerY -
                        owner.centerY

                if (
                    differenceX *
                    differenceX +
                    differenceY *
                    differenceY <=
                    PACK_RADIUS *
                    PACK_RADIUS
                ) {
                    nearbyPackMembers++
                }
            }

        val countedMembers =
            nearbyPackMembers
                .coerceAtMost(
                    MAXIMUM_COUNTED_PACK_MEMBERS
                )

        val perMemberBonus =
            when (
                owner.type
            ) {
                EnemyType.NORMAL ->
                    NORMAL_BONUS_PER_MEMBER

                EnemyType.UNIQUE ->
                    UNIQUE_BONUS_PER_MEMBER

                EnemyType.MINI_BOSS ->
                    MINI_BOSS_BONUS_PER_MEMBER

                EnemyType.FINAL_BOSS ->
                    FINAL_BOSS_BONUS_PER_MEMBER
            }

        val totalBonus =
            countedMembers *
                perMemberBonus

        currentModifiers =
            EnemyTraitModifiers(
                maximumHealthMultiplier =
                    1f +
                        totalBonus,

                damageMultiplier =
                    1f +
                        totalBonus,

                movementSpeedMultiplier =
                    1f +
                        totalBonus,

                attackSpeedMultiplier =
                    1f
            )
    }

    override fun modifiers():
        EnemyTraitModifiers {
        return currentModifiers
    }

    companion object {

        private const val PACK_RADIUS =
            220f

        private const val MAXIMUM_COUNTED_PACK_MEMBERS =
            5

        private const val NORMAL_BONUS_PER_MEMBER =
            0.08f

        private const val UNIQUE_BONUS_PER_MEMBER =
            0.10f

        private const val MINI_BOSS_BONUS_PER_MEMBER =
            0.12f

        private const val FINAL_BOSS_BONUS_PER_MEMBER =
            0.14f
    }
}

private class GrizzlyAngryTrait :
    EnemyTrait {

    override val id =
        EnemyTraitIds.GRIZZLY_ANGRY

    private var activated =
        false

    override fun update(
        context: EnemyTraitUpdateContext
    ) {
        if (
            !activated &&
            context.owner
                .health
                .healthRatio <=
            ACTIVATION_HEALTH_RATIO
        ) {
            activated =
                true
        }
    }

    override fun modifiers():
        EnemyTraitModifiers {
        if (
            !activated
        ) {
            return EnemyTraitModifiers.IDENTITY
        }

        return EnemyTraitModifiers(
            damageMultiplier =
                1.40f,

            movementSpeedMultiplier =
                1.35f,

            attackSpeedMultiplier =
                1.35f
        )
    }

    override fun specialVisual(owner: Enemy): EnemyTraitSpecialVisual? =
        if (activated && !owner.isDead) {
            EnemyTraitSpecialVisual(
                kind = EnemyTraitSpecialVisualKind.GRIZZLY_RAGE,
                progress = 1f,
                scale = 1.15f
            )
        } else {
            null
        }

    companion object {

        private const val ACTIVATION_HEALTH_RATIO =
            0.50f
    }
}

private class GhostIncorporealEvasionTrait :
    EnemyTrait {

    override val id =
        EnemyTraitIds.GHOST_INCORPOREAL_EVASION

    override fun shouldNegateIncomingDamage(
        owner: Enemy,
        context: EnemyTraitDamageContext
    ): Boolean {
        if (
            owner.isDead ||
            context.requestedDamage <=
            0
        ) {
            return false
        }

        return Random.Default
            .nextFloat() <
            EVADE_CHANCE
    }

    companion object {

        private const val EVADE_CHANCE =
            0.35f
    }
}

private class VampireHungerTrait :
    EnemyTrait {

    override val id =
        EnemyTraitIds.VAMPIRE_HUNGER

    private var pulseCooldown = 0f
    private val pendingDamage = mutableListOf<EnemyTraitTargetDamage>()
    private val visuals = mutableListOf<ActiveHungerVisual>()
    private val enemyRetargetCooldowns = mutableMapOf<Enemy, Float>()
    private val combatTargetRetargetCooldowns =
        mutableMapOf<Pair<EnemyTraitTargetType, Int>, Float>()

    override fun update(
        context: EnemyTraitUpdateContext
    ) {
        val dt = context.delta.coerceAtLeast(0f)
        pulseCooldown = (pulseCooldown - dt).coerceAtLeast(0f)
        updateRetargetCooldowns(enemyRetargetCooldowns, dt)
        updateRetargetCooldowns(combatTargetRetargetCooldowns, dt)
        visuals.forEach { it.elapsed += dt }
        visuals.removeAll { it.elapsed >= HUNGER_VISUAL_SECONDS }
        if (context.owner.isDead || context.owner.isActiveSkillLocked || pulseCooldown > 0f) return

        val owner = context.owner
        lastOwnerX = owner.centerX
        lastOwnerY = owner.centerY
        val radiusSquared = HUNGER_RADIUS * HUNGER_RADIUS
        var successfulHits = 0

        context.nearbyEnemies.forEach { other ->
            if (other === owner || other.isDead) return@forEach
            if (enemyRetargetCooldowns.containsKey(other)) return@forEach
            val dx = other.centerX - owner.centerX
            val dy = other.centerY - owner.centerY
            if (dx * dx + dy * dy > radiusSquared) return@forEach
            val applied =
                other.takeDamage(
                    (other.health.maximumHealth * NON_PLAYER_DAMAGE_MAX_HEALTH_RATIO)
                        .roundToInt()
                        .coerceAtLeast(1),
                    EnemyDamageContext(
                        damageType = DamageType.NECROTIC,
                        sourceName = "Vampire Hunger",
                        conditionStatScore = owner.conditionDamageStatScore
                    )
                )
            if (applied > 0) {
                enemyRetargetCooldowns[other] = TARGET_RETARGET_LOCK_SECONDS
                successfulHits++
                visuals += ActiveHungerVisual(other.centerX, other.centerY)
            }
        }

        context.combatTargets.forEach { target ->
            if (target.entityType == CombatEntityType.STRUCTURE) return@forEach
            val targetKey = target.type to target.index
            if (combatTargetRetargetCooldowns.containsKey(targetKey)) return@forEach
            val dx = target.centerX - owner.centerX
            val dy = target.centerY - owner.centerY
            if (dx * dx + dy * dy > radiusSquared) return@forEach
            val ratio =
                if (target.entityType == CombatEntityType.PLAYER) {
                    PLAYER_DAMAGE_MAX_HEALTH_RATIO
                } else {
                    NON_PLAYER_DAMAGE_MAX_HEALTH_RATIO
                }
            pendingDamage += EnemyTraitTargetDamage(
                targetType = target.type,
                targetIndex = target.index,
                amount = (target.maximumHealth * ratio).roundToInt().coerceAtLeast(1),
                sourceName = "Vampire Hunger",
                damageType = DamageType.NECROTIC,
                projectileOrMagic = true
            )
            combatTargetRetargetCooldowns[targetKey] = TARGET_RETARGET_LOCK_SECONDS
            successfulHits++
            visuals += ActiveHungerVisual(target.centerX, target.centerY)
        }

        if (successfulHits > 0) {
            val healRatio =
                if (owner.isVampireSecondForm) {
                    SECOND_FORM_HEAL_RATIO_PER_TARGET
                } else {
                    FIRST_FORM_HEAL_RATIO_PER_TARGET
                }
            owner.health.heal(
                (owner.health.maximumHealth * healRatio * successfulHits).roundToInt().coerceAtLeast(1),
                HealingVisualType.LIFE_STEAL
            )
        }
        pulseCooldown = PULSE_SECONDS
    }

    override fun consumeTargetDamage() = pendingDamage.toList().also { pendingDamage.clear() }

    override fun onPlayerContact(
        owner: Enemy,
        dealtDamage: Int,
        targetEntityType: CombatEntityType
    ) {
        if (
            owner.isDead ||
            dealtDamage <= 0 ||
            targetEntityType == CombatEntityType.STRUCTURE
        ) return
        val healRatio =
            if (owner.isVampireSecondForm) {
                SECOND_FORM_CONTACT_HEAL_RATIO
            } else {
                FIRST_FORM_CONTACT_HEAL_RATIO
            }
        owner.health.heal(
            (owner.health.maximumHealth * healRatio)
                .roundToInt()
                .coerceAtLeast(1),
            HealingVisualType.LIFE_STEAL
        )
    }

    override fun spellVisuals(): List<EnemyTraitSpellVisual> = visuals.map {
        EnemyTraitSpellVisual(
            kind = EnemyTraitSpellVisualKind.VAMPIRE_HUNGER,
            centerX = it.startX,
            centerY = it.startY,
            targetCenterX = it.ownerX,
            targetCenterY = it.ownerY,
            worldWidth = 88f,
            worldHeight = 88f,
            animationSeconds = it.elapsed,
            progress = (it.elapsed / HUNGER_VISUAL_SECONDS).coerceIn(0f, 0.999f),
            rotationDegrees =
                MathUtils.atan2(it.ownerY - it.startY, it.ownerX - it.startX) *
                    MathUtils.radiansToDegrees
        )
    }

    private fun <K> updateRetargetCooldowns(cooldowns: MutableMap<K, Float>, delta: Float) {
        val iterator = cooldowns.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val remaining = entry.value - delta
            if (remaining <= 0f) {
                iterator.remove()
            } else {
                entry.setValue(remaining)
            }
        }
    }

    private inner class ActiveHungerVisual(val startX: Float, val startY: Float) {
        val ownerX get() = lastOwnerX
        val ownerY get() = lastOwnerY
        var elapsed = 0f
    }

    private var lastOwnerX = 0f
    private var lastOwnerY = 0f

    companion object {

        private const val PULSE_SECONDS = 0.25f
        private const val TARGET_RETARGET_LOCK_SECONDS = 0.75f
        private const val HUNGER_RADIUS = 270f
        private const val PLAYER_DAMAGE_MAX_HEALTH_RATIO = 0.04f
        private const val NON_PLAYER_DAMAGE_MAX_HEALTH_RATIO = 0.08f
        private const val FIRST_FORM_HEAL_RATIO_PER_TARGET = 0.005f
        private const val SECOND_FORM_HEAL_RATIO_PER_TARGET = 0.01f
        private const val HUNGER_VISUAL_SECONDS = 0.72f
        private const val FIRST_FORM_CONTACT_HEAL_RATIO = 0.075f
        private const val SECOND_FORM_CONTACT_HEAL_RATIO = 0.15f
    }
}


private abstract class LockedRushTrait :
    EnemyTrait {

    private enum class Phase {
        COOLDOWN,
        TELEGRAPH,
        RUSH
    }

    private var phase =
        Phase.COOLDOWN

    private var phaseSeconds =
        initialCooldownSeconds()

    private val lockedDirection =
        Vector2()

    private var warningEndX =
        0f

    private var warningEndY =
        0f

    protected abstract fun cooldownSeconds(): Float
    protected abstract fun initialCooldownSeconds(): Float
    protected abstract fun telegraphSeconds(): Float
    protected abstract fun rushSeconds(): Float
    protected abstract fun activationRange(): Float
    protected abstract fun rushSpeedMultiplier(): Float
    protected abstract fun baseContactDamageMultiplier(): Float

    override fun update(
        context: EnemyTraitUpdateContext
    ) {
        if (
            context.owner.isDead
        ) {
            return
        }

        phaseSeconds -=
            context.delta
                .coerceAtLeast(
                    0f
                )

        when (
            phase
        ) {
            Phase.COOLDOWN -> {
                if (
                    phaseSeconds >
                    0f
                ) {
                    return
                }

                val dx =
                    context.targetCenterX -
                        context.owner.centerX

                val dy =
                    context.targetCenterY -
                        context.owner.centerY

                if (
                    dx * dx +
                    dy * dy >
                    activationRange() *
                    activationRange()
                ) {
                    phaseSeconds =
                        0.20f

                    return
                }

                lockedDirection
                    .set(
                        dx,
                        dy
                    )

                if (
                    lockedDirection.isZero
                ) {
                    phaseSeconds =
                        0.20f
                    return
                }

                lockedDirection.nor()

                warningEndX =
                    context.owner.centerX +
                        lockedDirection.x *
                        activationRange()

                warningEndY =
                    context.owner.centerY +
                        lockedDirection.y *
                        activationRange()

                phase =
                    Phase.TELEGRAPH

                phaseSeconds =
                    telegraphSeconds()
            }

            Phase.TELEGRAPH -> {
                if (
                    phaseSeconds >
                    0f
                ) {
                    return
                }

                phase =
                    Phase.RUSH

                phaseSeconds =
                    rushSeconds()
            }

            Phase.RUSH -> {
                if (
                    phaseSeconds >
                    0f
                ) {
                    return
                }

                phase =
                    Phase.COOLDOWN

                phaseSeconds =
                    cooldownSeconds()
            }
        }
    }

    override fun resolveMovementOverride(
        owner: Enemy,
        output: Vector2
    ): Boolean {

        return when (
            phase
        ) {
            Phase.TELEGRAPH -> {
                output.setZero()
                true
            }

            Phase.RUSH -> {
                output
                    .set(
                        lockedDirection
                    )
                    .scl(
                        rushSpeedMultiplier() *
                            if (owner.kindId == com.azizoglu.grisilith.enemy.definition.EnemyKindIds.CROCODILE) {
                                owner.crocodileDashMultiplier
                            } else {
                                1f
                            }
                    )

                true
            }

            Phase.COOLDOWN ->
                false
        }
    }

    override fun contactDamageMultiplier(
        owner: Enemy
    ): Float {

        if (
            phase !=
            Phase.RUSH
        ) {
            return 1f
        }

        val qualityMultiplier =
            when (
                owner.type
            ) {
                EnemyType.NORMAL ->
                    1f

                EnemyType.UNIQUE ->
                    1.18f

                EnemyType.MINI_BOSS ->
                    1.35f

                EnemyType.FINAL_BOSS ->
                    1.55f
            }

        return baseContactDamageMultiplier() *
            qualityMultiplier
    }

    override fun render(
        owner: Enemy,
        shapeRenderer: ShapeRenderer
    ) {
        if (
            phase !=
            Phase.TELEGRAPH
        ) {
            return
        }

        shapeRenderer.color =
            Color.RED

        shapeRenderer.rectLine(
            owner.centerX,
            owner.centerY,
            warningEndX,
            warningEndY,
            5f
        )
    }
}

private class BoarChargeTrait :
    LockedRushTrait() {

    override val id =
        EnemyTraitIds.BOAR_CHARGE

    override fun cooldownSeconds() =
        10f

    override fun initialCooldownSeconds() =
        3f

    override fun telegraphSeconds() =
        1f

    override fun rushSeconds() =
        1.15f

    override fun activationRange() =
        520f

    override fun rushSpeedMultiplier() =
        4.25f

    override fun baseContactDamageMultiplier() =
        2.75f
}

private class CrocodileJumpTrait :
    LockedRushTrait() {

    override val id =
        EnemyTraitIds.CROCODILE_JUMP

    override fun cooldownSeconds() =
        12f

    override fun initialCooldownSeconds() =
        4f

    override fun telegraphSeconds() =
        2f

    override fun rushSeconds() =
        0.55f

    override fun activationRange() =
        460f

    override fun rushSpeedMultiplier() =
        7.0f

    override fun baseContactDamageMultiplier() =
        3.50f
}


private class GorillaThrowTrait :
    EnemyTrait {

    override val id =
        EnemyTraitIds.GORILLA_THROW

    private enum class Phase {
        COOLDOWN,
        WINDUP
    }

    private data class ThrownRock(
        var x: Float,
        var y: Float,
        val velocityX: Float,
        val velocityY: Float,
        val damage: Int,
        val startX: Float,
        val startY: Float,
        val size: Float = 37.5f,
        var active: Boolean = true
    ) {
        val bounds =
            Rectangle()
    }

    private var phase =
        Phase.COOLDOWN

    private var phaseSeconds =
        INITIAL_COOLDOWN_SECONDS

    private var lockedTargetX =
        0f

    private var lockedTargetY =
        0f

    private var latestContext:
        EnemyTraitUpdateContext? =
        null

    private val rocks =
        mutableListOf<ThrownRock>()

    private val pendingDamage =
        mutableListOf<EnemyTraitPlayerDamage>()

    override fun update(
        context: EnemyTraitUpdateContext
    ) {
        latestContext =
            context

        if (
            context.owner.isDead
        ) {
            rocks.clear()
            return
        }

        phaseSeconds -=
            context.delta
                .coerceAtLeast(
                    0f
                )

        when (
            phase
        ) {
            Phase.COOLDOWN -> {
                if (
                    phaseSeconds <=
                    0f
                ) {
                    lockedTargetX =
                        context.targetCenterX

                    lockedTargetY =
                        context.targetCenterY

                    phase =
                        Phase.WINDUP

                    phaseSeconds =
                        WINDUP_SECONDS
                }
            }

            Phase.WINDUP -> {
                if (
                    phaseSeconds <=
                    0f
                ) {
                    launchRock(
                        context
                    )

                    phase =
                        Phase.COOLDOWN

                    phaseSeconds =
                        COOLDOWN_SECONDS
                }
            }
        }

        updateRocks(
            context
        )
    }

    private fun launchRock(
        context: EnemyTraitUpdateContext
    ) {
        val direction =
            Vector2(
                lockedTargetX -
                    context.owner.centerX,

                lockedTargetY -
                    context.owner.centerY
            )

        if (
            direction.isZero
        ) {
            direction.set(
                1f,
                0f
            )
        }

        direction
            .nor()
            .scl(
                PROJECTILE_SPEED
            )

        val qualityMultiplier =
            when (
                context.owner.type
            ) {
                EnemyType.NORMAL ->
                    1f

                EnemyType.UNIQUE ->
                    1.15f

                EnemyType.MINI_BOSS ->
                    1.35f

                EnemyType.FINAL_BOSS ->
                    1.55f
            }

        rocks.add(
            ThrownRock(
                x =
                    context.owner.centerX,

                y =
                    context.owner.centerY,

                velocityX =
                    direction.x,

                velocityY =
                    direction.y,

                damage =
                    (
                        context.owner.contactDamage *
                            BASE_DAMAGE_MULTIPLIER *
                            qualityMultiplier
                        )
                        .roundToInt()
                        .coerceAtLeast(
                            1
                        ),
                startX = context.owner.centerX,
                startY = context.owner.centerY
            )
        )
    }

    private fun updateRocks(
        context: EnemyTraitUpdateContext
    ) {
        val iterator =
            rocks.iterator()

        while (
            iterator.hasNext()
        ) {
            val rock =
                iterator.next()

            rock.x +=
                rock.velocityX *
                    context.delta

            rock.y +=
                rock.velocityY *
                    context.delta

            rock.bounds.set(
                rock.x -
                    rock.size /
                    2f,

                rock.y -
                    rock.size /
                    2f,

                rock.size,
                rock.size
            )

            if (
                rock.x <
                0f ||
                rock.y <
                0f ||
                rock.x >
                context.worldWidth ||
                rock.y >
                context.worldHeight
            ) {
                iterator.remove()
                continue
            }

            if (
                context.obstacles
                    .any { obstacle ->
                        rock.bounds.overlaps(
                            obstacle.bounds
                        )
                    }
            ) {
                iterator.remove()
                continue
            }

            if (
                rock.bounds.overlaps(
                    context.targetBounds
                )
            ) {
                pendingDamage.add(
                    EnemyTraitPlayerDamage(
                        amount =
                            rock.damage,

                        sourceName =
                            "Gorilla Throw",
                        damageType = com.azizoglu.grisilith.content.DamageType.BLUDGEONING
                    )
                )

                iterator.remove()
            }
        }
    }

    override fun consumePlayerDamage():
        List<EnemyTraitPlayerDamage> {
        if (
            pendingDamage.isEmpty()
        ) {
            return emptyList()
        }

        val result =
            pendingDamage.toList()

        pendingDamage.clear()

        return result
    }

    override fun projectileVisuals(): List<EnemyTraitProjectileVisual> =
        rocks.map { rock ->
            EnemyTraitProjectileVisual(
                kind = EnemyTraitProjectileVisualKind.ROCK,
                centerX = rock.x,
                centerY = rock.y,
                velocityX = rock.velocityX,
                velocityY = rock.velocityY,
                worldHeight = rock.size * 1.8f
            )
        }

    override fun render(
        owner: Enemy,
        shapeRenderer: ShapeRenderer
    ) {
        if (
            phase ==
            Phase.WINDUP
        ) {
            drawEnemyProjectileDottedLine(
                shapeRenderer,
                owner.centerX,
                owner.centerY,
                lockedTargetX,
                lockedTargetY,
                Color.WHITE
            )
        }

        rocks.forEach { rock ->
            drawEnemyProjectileDottedLine(
                shapeRenderer,
                rock.startX,
                rock.startY,
                rock.startX + rock.velocityX * 12f,
                rock.startY + rock.velocityY * 12f,
                Color(1f, 0.08f, 0.05f, 0.92f)
            )
        }
    }

    companion object {

        private const val INITIAL_COOLDOWN_SECONDS =
            3f

        private const val COOLDOWN_SECONDS =
            10f

        private const val WINDUP_SECONDS =
            0.50f

        private const val PROJECTILE_SPEED =
            460f

        private const val BASE_DAMAGE_MULTIPLIER =
            3.75f
    }
}

private class SwarmBeingTrait : EnemyTrait {
    override val id = EnemyTraitIds.SWARM_BEING

    override fun incomingDamageMultiplier(
        owner: Enemy,
        context: EnemyTraitDamageContext
    ): Float {
        val damageContext = context.damageContext

        val isMeleeAreaAttack =
            damageContext.sourceWeaponTags
                .contains(ContentTag.MELEE_WEAPON)

        val source =
            damageContext.sourceName
                ?.lowercase()
                .orEmpty()

        val isKnownAreaAttack =
            AREA_SOURCE_HINTS.any { hint ->
                source.contains(hint)
            }

        return if (
            isMeleeAreaAttack ||
            isKnownAreaAttack
        ) {
            AREA_DAMAGE_MULTIPLIER
        } else {
            SINGLE_TARGET_DAMAGE_MULTIPLIER
        }
    }

    companion object {
        private const val SINGLE_TARGET_DAMAGE_MULTIPLIER = 0.50f
        private const val AREA_DAMAGE_MULTIPLIER = 2f

        private val AREA_SOURCE_HINTS =
            listOf(
                "static clouds",
                "ignite dash",
                "erasing",
                "vortex",
                "plague storm",
                "acidic pool",
                "vitriolic",
                "call lightning",
                "meteor",
                "tsunami",
                "singularity",
                "combustion beam"
            )
    }
}

private class SplitTheSwarmTrait : EnemyTrait {
    override val id = EnemyTraitIds.SPLIT_THE_SWARM

    override fun resolveFinalIncomingDamage(
        owner: Enemy,
        context: EnemyTraitDamageContext,
        resolvedDamage: Int
    ): Int {
        if (resolvedDamage > 0 && !owner.isDead && !context.damageContext.isProjectileAttack) {
            owner.requestSwarmSplit()
        }
        return resolvedDamage
    }
}

private class BabyCrocodilesTrait : EnemyTrait {
    override val id = EnemyTraitIds.BABY_CROCODILES
    private var remaining = SUMMON_INTERVAL_SECONDS

    override fun update(context: EnemyTraitUpdateContext) {
        if (context.owner.isDead || context.owner.isBabyCrocodile) return
        remaining -= context.delta.coerceAtLeast(0f)
        while (remaining <= 0f) {
            context.owner.requestBabyCrocodileSpawn()
            remaining += SUMMON_INTERVAL_SECONDS
        }
    }

    companion object {
        private const val SUMMON_INTERVAL_SECONDS = 5f
    }
}

private class MimicryTrait : EnemyTrait {
    override val id = EnemyTraitIds.MIMICRY

    private enum class Phase { DISGUISED, TRANSFORMING, ACTIVE }

    private var phase = Phase.DISGUISED
    private var phaseRemaining = 0f
    private var ambushBuffRemaining = 0f

    override fun update(context: EnemyTraitUpdateContext) {
        if (context.owner.isDead) return
        when (phase) {
            Phase.DISGUISED -> {
                val dx = context.targetCenterX - context.owner.centerX
                val dy = context.targetCenterY - context.owner.centerY
                if (dx * dx + dy * dy <= REVEAL_RADIUS * REVEAL_RADIUS) {
                    phase = Phase.TRANSFORMING
                    phaseRemaining = TRANSFORMATION_SECONDS
                }
            }

            Phase.TRANSFORMING -> {
                phaseRemaining -= context.delta.coerceAtLeast(0f)
                if (phaseRemaining <= 0f) {
                    phase = Phase.ACTIVE
                    ambushBuffRemaining = AMBUSH_BUFF_SECONDS
                }
            }

            Phase.ACTIVE -> {
                ambushBuffRemaining =
                    (ambushBuffRemaining - context.delta.coerceAtLeast(0f)).coerceAtLeast(0f)
            }
        }
    }

    override fun resolveMovementOverride(owner: Enemy, output: Vector2): Boolean {
        if (phase == Phase.ACTIVE) return false
        output.setZero()
        return true
    }

    override fun contactDamageMultiplier(owner: Enemy): Float =
        if (phase == Phase.ACTIVE) 1f else 0f

    override fun modifiers(): EnemyTraitModifiers =
        if (phase == Phase.ACTIVE && ambushBuffRemaining > 0f) {
            EnemyTraitModifiers(damageMultiplier = 1.25f, movementSpeedMultiplier = 1.25f)
        } else {
            EnemyTraitModifiers.IDENTITY
        }

    override fun isTargetable(owner: Enemy): Boolean = phase != Phase.DISGUISED

    override fun suppressHealthBar(owner: Enemy): Boolean = phase == Phase.DISGUISED

    override fun specialVisual(owner: Enemy): EnemyTraitSpecialVisual? =
        when (phase) {
            Phase.DISGUISED -> EnemyTraitSpecialVisual(
                kind = EnemyTraitSpecialVisualKind.MIMIC_TRANSFORMATION,
                progress = 0f
            )

            Phase.TRANSFORMING -> EnemyTraitSpecialVisual(
                kind = EnemyTraitSpecialVisualKind.MIMIC_TRANSFORMATION,
                progress = (1f - phaseRemaining / TRANSFORMATION_SECONDS).coerceIn(0f, 0.999f)
            )

            Phase.ACTIVE -> null
        }

    companion object {
        private const val REVEAL_RADIUS = 250f
        private const val TRANSFORMATION_SECONDS = 0.85f
        private const val AMBUSH_BUFF_SECONDS = 5f
    }
}

private class GorillaStompTheEarthTrait : EnemyTrait {
    override val id = EnemyTraitIds.GORILLA_STOMP_THE_EARTH

    private enum class Phase { COOLDOWN, CASTING, IMPACT }
    private var phase = Phase.COOLDOWN
    private var remaining = INITIAL_COOLDOWN_SECONDS
    private var targetBounds = Rectangle()
    private val pendingDamage = mutableListOf<EnemyTraitPlayerDamage>()

    override fun update(context: EnemyTraitUpdateContext) {
        if (context.owner.isDead) return
        targetBounds.set(context.targetBounds)
        remaining -= context.delta.coerceAtLeast(0f)

        when (phase) {
            Phase.COOLDOWN -> if (remaining <= 0f) {
                phase = Phase.CASTING
                remaining = CAST_SECONDS
            }
            Phase.CASTING -> if (remaining <= 0f) {
                if (circleOverlapsRectangle(context.owner.centerX, context.owner.centerY, RADIUS, targetBounds)) {
                    pendingDamage.add(
                        EnemyTraitPlayerDamage(
                            amount = (context.owner.contactDamage * DAMAGE_MULTIPLIER).roundToInt().coerceAtLeast(1),
                            sourceName = "Stomp the Earth",
                            damageType = com.azizoglu.grisilith.content.DamageType.BLUDGEONING
                        )
                    )
                }
                phase = Phase.IMPACT
                remaining = IMPACT_SECONDS
            }
            Phase.IMPACT -> if (remaining <= 0f) {
                phase = Phase.COOLDOWN
                remaining = COOLDOWN_SECONDS
            }
        }
    }

    override fun resolveMovementOverride(owner: Enemy, output: Vector2): Boolean {
        if (phase != Phase.CASTING && phase != Phase.IMPACT) return false
        output.setZero()
        return true
    }

    override fun consumePlayerDamage(): List<EnemyTraitPlayerDamage> {
        if (pendingDamage.isEmpty()) return emptyList()
        return pendingDamage.toList().also { pendingDamage.clear() }
    }

    override fun specialVisual(owner: Enemy): EnemyTraitSpecialVisual? {
        if (phase == Phase.COOLDOWN) return null
        return EnemyTraitSpecialVisual(
            kind = EnemyTraitSpecialVisualKind.GORILLA_SLAM,
            progress = when (phase) {
                Phase.CASTING -> (1f - remaining / CAST_SECONDS).coerceIn(0f, 0.999f)
                Phase.IMPACT -> (1f - remaining / IMPACT_SECONDS).coerceIn(0f, 0.999f)
                Phase.COOLDOWN -> 0f
            },
            scale = RADIUS / 285f,
            isImpact = phase == Phase.IMPACT
        )
    }

    private fun circleOverlapsRectangle(x: Float, y: Float, radius: Float, r: Rectangle): Boolean {
        val cx = x.coerceIn(r.x, r.x + r.width)
        val cy = y.coerceIn(r.y, r.y + r.height)
        val dx = x - cx
        val dy = y - cy
        return dx * dx + dy * dy <= radius * radius
    }

    companion object {
        private const val INITIAL_COOLDOWN_SECONDS = 7f
        private const val COOLDOWN_SECONDS = 16f
        private const val CAST_SECONDS = 1.00f
        private const val IMPACT_SECONDS = 0.72f
        private const val RADIUS = 285f
        private const val DAMAGE_MULTIPLIER = 3.20f
    }
}

private class VampireMistyEscapeTrait : EnemyTrait {
    override val id = EnemyTraitIds.MISTY_ESCAPE

    private enum class TeleportPhase {
        READY,
        PREPARING,
        ARRIVAL_WAIT
    }

    private var teleportCooldown = TELEPORT_INTERVAL_SECONDS
    private var teleportPhase = TeleportPhase.READY
    private var teleportPhaseRemaining = 0f
    private var teleportDestinationX = 0f
    private var teleportDestinationY = 0f
    private var longswordCooldown = LONGSWORD_INTERVAL_SECONDS
    private var longswordTelegraphActive = false
    private var longswordTelegraphRemaining = 0f
    private val longswordDirection = Vector2(1f, 0f)
    private var longswordAngle = 0f
    private val pendingDamage = mutableListOf<EnemyTraitTargetDamage>()

    override fun update(context: EnemyTraitUpdateContext) {
        val owner = context.owner
        if (owner.isDead) return
        val dt = context.delta.coerceAtLeast(0f)

        if (owner.isVampireEscaping) {
            val dx = owner.vampireEscapeTargetX - owner.centerX
            val dy = owner.vampireEscapeTargetY - owner.centerY
            if (dx * dx + dy * dy <= VAMPIRE_ESCAPE_STOP_DISTANCE * VAMPIRE_ESCAPE_STOP_DISTANCE) {
                owner.startVampireSecondFormTransformation()
            }
            return
        }
        if (!owner.isVampireSecondForm || owner.isVampireTransforming) return

        when (teleportPhase) {
            TeleportPhase.PREPARING -> {
                teleportPhaseRemaining = (teleportPhaseRemaining - dt).coerceAtLeast(0f)
                if (teleportPhaseRemaining <= 0f) {
                    owner.teleportCenter(
                        teleportDestinationX,
                        teleportDestinationY,
                        context.worldWidth,
                        context.worldHeight
                    )
                    teleportPhase = TeleportPhase.ARRIVAL_WAIT
                    teleportPhaseRemaining = TELEPORT_ARRIVAL_WAIT_SECONDS
                }
                return
            }

            TeleportPhase.ARRIVAL_WAIT -> {
                teleportPhaseRemaining = (teleportPhaseRemaining - dt).coerceAtLeast(0f)
                if (teleportPhaseRemaining <= 0f) {
                    teleportPhase = TeleportPhase.READY
                    owner.setVampireTeleportLocked(false)
                    teleportCooldown = TELEPORT_INTERVAL_SECONDS
                }
                return
            }

            TeleportPhase.READY -> Unit
        }

        if (owner.isActiveSkillLocked) return

        teleportCooldown = (teleportCooldown - dt).coerceAtLeast(0f)
        longswordCooldown = (longswordCooldown - dt).coerceAtLeast(0f)

        val playerTarget =
            context.combatTargets.firstOrNull { it.type == EnemyTraitTargetType.PLAYER }
        val playerDistanceSquared =
            playerTarget?.let {
                val dx = it.centerX - owner.centerX
                val dy = it.centerY - owner.centerY
                dx * dx + dy * dy
            } ?: 0f

        if (
            teleportCooldown <= 0f &&
            playerDistanceSquared > TELEPORT_MINIMUM_PLAYER_DISTANCE * TELEPORT_MINIMUM_PLAYER_DISTANCE
        ) {
            val angle = MathUtils.random(0f, MathUtils.PI2)
            val safeMargin = owner.visualSize / 2f + 8f
            teleportDestinationX =
                (context.targetCenterX + MathUtils.cos(angle) * TELEPORT_RADIUS)
                    .coerceIn(safeMargin, context.worldWidth - safeMargin)
            teleportDestinationY =
                (context.targetCenterY + MathUtils.sin(angle) * TELEPORT_RADIUS)
                    .coerceIn(safeMargin, context.worldHeight - safeMargin)
            teleportPhase = TeleportPhase.PREPARING
            teleportPhaseRemaining = TELEPORT_PREPARE_SECONDS
            owner.setVampireTeleportLocked(true)
            longswordTelegraphActive = false
            longswordTelegraphRemaining = 0f
            longswordCooldown = LONGSWORD_INTERVAL_SECONDS
            return
        }

        if (longswordTelegraphActive) {
            longswordTelegraphRemaining =
                (longswordTelegraphRemaining - dt).coerceAtLeast(0f)
            if (longswordTelegraphRemaining <= 0f) {
                performLongswordCone(context, longswordDirection)
                longswordTelegraphActive = false
                longswordCooldown = LONGSWORD_INTERVAL_SECONDS
            }
            return
        }

        if (
            longswordCooldown <= LONGSWORD_TELEGRAPH_SECONDS &&
            playerTarget != null &&
            playerDistanceSquared <= LONGSWORD_RANGE * LONGSWORD_RANGE
        ) {
            longswordDirection.set(
                playerTarget.centerX - owner.centerX,
                playerTarget.centerY - owner.centerY
            )
            if (!longswordDirection.isZero) {
                longswordDirection.nor()
                longswordAngle =
                    MathUtils.atan2(longswordDirection.y, longswordDirection.x) * MathUtils.radiansToDegrees
                longswordTelegraphActive = true
                longswordTelegraphRemaining = LONGSWORD_TELEGRAPH_SECONDS
            }
        }
    }

    override fun resolveMovementOverride(owner: Enemy, output: Vector2): Boolean {
        if (owner.isVampireEscaping) {
            output.set(owner.vampireEscapeTargetX - owner.centerX, owner.vampireEscapeTargetY - owner.centerY)
            if (output.isZero) return true
            output.nor().scl(VAMPIRE_ESCAPE_SPEED_MULTIPLIER)
            return true
        }
        if (owner.isVampireTransforming || owner.isVampireTeleportLocked) {
            output.setZero()
            return true
        }
        return false
    }

    override fun shouldNegateIncomingDamage(owner: Enemy, context: EnemyTraitDamageContext): Boolean {
        return owner.isVampireEscaping || owner.isVampireTransforming
    }

    override fun contactDamageMultiplier(owner: Enemy): Float {
        return if (owner.isActiveSkillLocked) 0f else 1f
    }

    override fun consumeTargetDamage() = pendingDamage.toList().also { pendingDamage.clear() }

    override fun specialVisual(owner: Enemy): EnemyTraitSpecialVisual? {
        if (owner.isVampireTransforming) {
            return EnemyTraitSpecialVisual(
                EnemyTraitSpecialVisualKind.VAMPIRE_TRANSFORMATION,
                1f -
                    owner.vampireTransformationRemainingSeconds /
                    Enemy.VAMPIRE_TRANSFORMATION_DURATION_SECONDS
            )
        }
        if (teleportPhase == TeleportPhase.PREPARING) {
            return EnemyTraitSpecialVisual(
                EnemyTraitSpecialVisualKind.VAMPIRE_TELEPORT,
                1f - teleportPhaseRemaining / TELEPORT_PREPARE_SECONDS
            )
        }
        if (teleportPhase == TeleportPhase.ARRIVAL_WAIT) {
            return EnemyTraitSpecialVisual(
                EnemyTraitSpecialVisualKind.VAMPIRE_TELEPORT,
                teleportPhaseRemaining / TELEPORT_ARRIVAL_WAIT_SECONDS
            )
        }
        return null
    }

    override fun render(owner: Enemy, shapeRenderer: ShapeRenderer) {
        if (teleportPhase == TeleportPhase.PREPARING) {
            shapeRenderer.color = Color(0.92f, 0.05f, 0.05f, 0.50f)
            shapeRenderer.circle(
                teleportDestinationX,
                teleportDestinationY,
                TELEPORT_MARKER_RADIUS,
                48
            )
        }
        if (longswordTelegraphActive) {
            shapeRenderer.color = Color(1f, 0.43f, 0.05f, 0.50f)
            shapeRenderer.arc(
                owner.centerX,
                owner.centerY,
                LONGSWORD_RANGE,
                longswordAngle - LONGSWORD_HALF_ANGLE,
                LONGSWORD_HALF_ANGLE * 2f,
                36
            )
        }
    }

    private fun performLongswordCone(context: EnemyTraitUpdateContext, lockedDirection: Vector2) {
        val owner = context.owner
        val direction = Vector2(lockedDirection)
        val angle = MathUtils.atan2(direction.y, direction.x) * MathUtils.radiansToDegrees
        MeleeAttackVisualManager.spawn(
            MeleeAttackVisualKind.LONGSWORD_CONE,
            owner.centerX + direction.x * LONGSWORD_RANGE * 0.42f,
            owner.centerY + direction.y * LONGSWORD_RANGE * 0.42f,
            angle,
            LONGSWORD_RANGE * 0.86f,
            LONGSWORD_RANGE * 0.64f,
            0.25f
        )
        context.combatTargets.forEach { target ->
            if (target.entityType == CombatEntityType.STRUCTURE) return@forEach
            val toTarget = Vector2(target.centerX - owner.centerX, target.centerY - owner.centerY)
            if (toTarget.len2() > LONGSWORD_RANGE * LONGSWORD_RANGE || toTarget.isZero) return@forEach
            toTarget.nor()
            val targetAngle = MathUtils.acos(direction.dot(toTarget).coerceIn(-1f, 1f)) * MathUtils.radiansToDegrees
            if (targetAngle > LONGSWORD_HALF_ANGLE) return@forEach
            pendingDamage += EnemyTraitTargetDamage(
                targetType = target.type,
                targetIndex = target.index,
                amount = (owner.contactDamage * 0.80f).roundToInt().coerceAtLeast(1),
                sourceName = "Vampire Longsword Cone",
                damageType = DamageType.SLASHING
            )
        }
    }

    companion object {
        private const val TELEPORT_INTERVAL_SECONDS = 8f
        private const val TELEPORT_RADIUS = 350f
        private const val TELEPORT_MINIMUM_PLAYER_DISTANCE = 1000f
        private const val TELEPORT_PREPARE_SECONDS = 0.75f
        private const val TELEPORT_ARRIVAL_WAIT_SECONDS = 1.5f
        private const val TELEPORT_MARKER_RADIUS = 52f
        private const val LONGSWORD_INTERVAL_SECONDS = 2f
        private const val LONGSWORD_TELEGRAPH_SECONDS = 1f
        private const val LONGSWORD_RANGE = 260f
        private const val LONGSWORD_HALF_ANGLE = 15f
        private const val VAMPIRE_ESCAPE_STOP_DISTANCE = 10f
        private const val VAMPIRE_ESCAPE_SPEED_MULTIPLIER = 3.2f
    }
}

