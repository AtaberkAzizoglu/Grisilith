package com.azizoglu.grisilith.enemy.trait

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyType
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

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

            EnemyTraitIds.CROCODILE_BURROW ->
                CrocodileBurrowTrait()

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

    private var enemyCleaveCooldownRemaining =
        0f

    override fun update(
        context: EnemyTraitUpdateContext
    ) {
        enemyCleaveCooldownRemaining =
            (
                enemyCleaveCooldownRemaining -
                    context.delta
                        .coerceAtLeast(
                            0f
                        )
                )
                .coerceAtLeast(
                    0f
                )
    }

    override fun onPlayerContact(
        owner: Enemy,
        dealtDamage: Int
    ) {
        if (
            owner.isDead ||
            dealtDamage <=
            0
        ) {
            return
        }

        healOwner(
            owner =
                owner,

            minimumHeal =
                PLAYER_CONTACT_MINIMUM_HEAL,

            maximumHealthRatio =
                PLAYER_CONTACT_HEAL_RATIO
        )
    }

    override fun onEnemyContact(
        owner: Enemy,
        other: Enemy
    ) {
        if (
            owner.isDead ||
            other.isDead ||
            enemyCleaveCooldownRemaining >
            0f
        ) {
            return
        }

        val cleaveDamage =
            (
                owner.contactDamage *
                    ALLY_CLEAVE_DAMAGE_RATIO
                )
                .roundToInt()
                .coerceAtLeast(
                    1
                )

        val appliedDamage =
            other.takeDamage(
                amount =
                    cleaveDamage
            )

        if (
            appliedDamage <=
            0
        ) {
            return
        }

        healOwner(
            owner =
                owner,

            minimumHeal =
                ALLY_CONTACT_MINIMUM_HEAL,

            maximumHealthRatio =
                ALLY_CONTACT_HEAL_RATIO
        )

        enemyCleaveCooldownRemaining =
            ALLY_CLEAVE_COOLDOWN_SECONDS
    }

    private fun healOwner(
        owner: Enemy,
        minimumHeal: Int,
        maximumHealthRatio: Float
    ) {
        val proportionalHeal =
            (
                owner.health
                    .maximumHealth *
                    maximumHealthRatio
                )
                .roundToInt()

        owner.health.heal(
            max(
                minimumHeal,
                proportionalHeal
            )
        )
    }

    companion object {

        private const val PLAYER_CONTACT_HEAL_RATIO =
            0.0060f

        private const val PLAYER_CONTACT_MINIMUM_HEAL =
            35

        private const val ALLY_CONTACT_HEAL_RATIO =
            0.0030f

        private const val ALLY_CONTACT_MINIMUM_HEAL =
            20

        private const val ALLY_CLEAVE_DAMAGE_RATIO =
            0.20f

        private const val ALLY_CLEAVE_COOLDOWN_SECONDS =
            0.75f
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
                        rushSpeedMultiplier()
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
        val size: Float = 30f,
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

        shapeRenderer.color =
            Color(
                0.42f,
                0.30f,
                0.20f,
                1f
            )

        rocks.forEach { rock ->
            drawEnemyProjectileDottedLine(
                shapeRenderer,
                rock.startX,
                rock.startY,
                rock.startX + rock.velocityX * 12f,
                rock.startY + rock.velocityY * 12f,
                Color(1f, 0.08f, 0.05f, 0.92f)
            )
            shapeRenderer.circle(
                rock.x,
                rock.y,
                rock.size /
                    2f
            )
        }
    }

    companion object {

        private const val INITIAL_COOLDOWN_SECONDS =
            3f

        private const val COOLDOWN_SECONDS =
            10f

        private const val WINDUP_SECONDS =
            0.65f

        private const val PROJECTILE_SPEED =
            460f

        private const val BASE_DAMAGE_MULTIPLIER =
            3.75f
    }
}

private fun drawEnemyProjectileDottedLine(
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

private class SlimeAcidicPathTrait :
    EnemyTrait {

    override val id =
        EnemyTraitIds.SLIME_ACIDIC_PATH

    private data class AcidPuddle(
        val x: Float,
        val y: Float,
        val radius: Float,
        var remainingSeconds: Float,
        var tickSeconds: Float =
            0f
    )

    private val puddles =
        mutableListOf<AcidPuddle>()

    private val pendingDamage =
        mutableListOf<EnemyTraitPlayerDamage>()

    private var lastDropX =
        Float.NaN

    private var lastDropY =
        Float.NaN

    /*
     * Player tarafında henüz ortak ConditionRuntime bulunmadığı için
     * Corroding burada Trait-local bir debuff olarak temsil edilir:
     * puddle oyuncuya değdiğinde kısa süreli acid vulnerability oluşur.
     * Player Condition sistemi ileride ortaklaştırılırsa yalnızca bu
     * sayaç ConditionType.CORRODING dispatch'ine çevrilebilir.
     */
    private var corrosionRemainingSeconds =
        0f

    override fun update(
        context: EnemyTraitUpdateContext
    ) {
        if (
            context.owner.isDead
        ) {
            return
        }

        corrosionRemainingSeconds =
            (
                corrosionRemainingSeconds -
                    context.delta
                )
                .coerceAtLeast(
                    0f
                )

        maybeDropPuddle(
            context
        )

        updatePuddles(
            context
        )
    }

    private fun maybeDropPuddle(
        context: EnemyTraitUpdateContext
    ) {
        val ownerX =
            context.owner.centerX

        val ownerY =
            context.owner.centerY

        if (
            lastDropX.isNaN() ||
            lastDropY.isNaN()
        ) {
            dropPuddle(
                context
            )
            return
        }

        val dx =
            ownerX -
                lastDropX

        val dy =
            ownerY -
                lastDropY

        if (
            dx * dx +
            dy * dy >=
            DROP_DISTANCE *
            DROP_DISTANCE
        ) {
            dropPuddle(
                context
            )
        }
    }

    private fun dropPuddle(
        context: EnemyTraitUpdateContext
    ) {
        val radius =
            (
                context.owner
                    .bounds
                    .width /
                    2f
                )
                .coerceAtLeast(
                    if (context.owner.isSlimeFragment) {
                        FRAGMENT_MINIMUM_RADIUS
                    } else {
                        MINIMUM_RADIUS
                    }
                )

        puddles.add(
            AcidPuddle(
                x =
                    context.owner.centerX,

                y =
                    context.owner.centerY,

                radius =
                    radius,

                remainingSeconds =
                    if (context.owner.isSlimeFragment) {
                        FRAGMENT_PUDDLE_DURATION_SECONDS
                    } else {
                        PUDDLE_DURATION_SECONDS
                    }
            )
        )

        lastDropX =
            context.owner.centerX

        lastDropY =
            context.owner.centerY
    }

    private fun updatePuddles(
        context: EnemyTraitUpdateContext
    ) {
        val iterator =
            puddles.iterator()

        while (
            iterator.hasNext()
        ) {
            val puddle =
                iterator.next()

            puddle.remainingSeconds -=
                context.delta

            puddle.tickSeconds -=
                context.delta

            if (
                puddle.remainingSeconds <=
                0f
            ) {
                iterator.remove()
                continue
            }

            if (
                puddle.tickSeconds >
                0f
            ) {
                continue
            }

            puddle.tickSeconds +=
                TICK_SECONDS

            if (
                !circleOverlapsRectangle(
                    circleX =
                        puddle.x,

                    circleY =
                        puddle.y,

                    radius =
                        puddle.radius,

                    rectangle =
                        context.targetBounds
                )
            ) {
                continue
            }

            corrosionRemainingSeconds =
                CORROSION_SECONDS

            val qualityMultiplier =
                when (
                    context.owner.type
                ) {
                    EnemyType.NORMAL ->
                        1f

                    EnemyType.UNIQUE ->
                        1.15f

                    EnemyType.MINI_BOSS ->
                        1.30f

                    EnemyType.FINAL_BOSS ->
                        1.50f
                }

            val corrosionMultiplier =
                if (
                    corrosionRemainingSeconds >
                    0f
                ) {
                    CORROSION_DAMAGE_MULTIPLIER
                } else {
                    1f
                }

            val tickDamage =
                (
                    context.owner
                        .contactDamage *
                        DAMAGE_PER_TICK_MULTIPLIER *
                        qualityMultiplier *
                        corrosionMultiplier
                    )
                    .roundToInt()
                    .coerceAtLeast(
                        1
                    )

            pendingDamage.add(
                EnemyTraitPlayerDamage(
                    amount =
                        tickDamage,

                    sourceName =
                        "Acidic Path",
                    damageType = com.azizoglu.grisilith.content.DamageType.ACID
                )
            )
        }
    }

    private fun circleOverlapsRectangle(
        circleX: Float,
        circleY: Float,
        radius: Float,
        rectangle: Rectangle
    ): Boolean {

        val closestX =
            circleX.coerceIn(
                rectangle.x,
                rectangle.x +
                    rectangle.width
            )

        val closestY =
            circleY.coerceIn(
                rectangle.y,
                rectangle.y +
                    rectangle.height
            )

        val dx =
            circleX -
                closestX

        val dy =
            circleY -
                closestY

        return dx * dx +
            dy * dy <=
            radius *
            radius
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

    override fun render(
        owner: Enemy,
        shapeRenderer: ShapeRenderer
    ) {
        shapeRenderer.color =
            Color(
                0.18f,
                0.72f,
                0.15f,
                0.42f
            )

        puddles.forEach { puddle ->
            shapeRenderer.circle(
                puddle.x,
                puddle.y,
                puddle.radius
            )
        }
    }

    companion object {

        private const val PUDDLE_DURATION_SECONDS =
            10f

        private const val FRAGMENT_PUDDLE_DURATION_SECONDS =
            7f

        private const val TICK_SECONDS =
            0.50f

        private const val DROP_DISTANCE =
            72f

        private const val MINIMUM_RADIUS =
            42f

        private const val FRAGMENT_MINIMUM_RADIUS =
            20f

        /*
         * Saniyede iki tick ile yaklaşık 1.30 × contactDamage DPS.
         * Acidic Path bilinçli olarak standart DoT'lardan daha sert.
         */
        private const val DAMAGE_PER_TICK_MULTIPLIER =
            0.65f

        private const val CORROSION_SECONDS =
            3f

        private const val CORROSION_DAMAGE_MULTIPLIER =
            1.15f
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

private class CrocodileBurrowTrait : EnemyTrait {
    override val id = EnemyTraitIds.CROCODILE_BURROW

    private enum class Phase {
        COOLDOWN,
        BURROWING,
        EMERGING,
        CHARGE
    }

    private var phase = Phase.COOLDOWN
    private var phaseSeconds = INITIAL_COOLDOWN_SECONDS
    private var targetX = 0f
    private var targetY = 0f
    private val lockedChargeDirection = Vector2()

    override fun update(context: EnemyTraitUpdateContext) {
        if (context.owner.isDead) return

        targetX = context.targetCenterX
        targetY = context.targetCenterY
        phaseSeconds = (phaseSeconds - context.delta.coerceAtLeast(0f)).coerceAtLeast(0f)

        val dx = targetX - context.owner.centerX
        val dy = targetY - context.owner.centerY
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

        when (phase) {
            Phase.COOLDOWN -> {
                if (phaseSeconds <= 0f && distance >= BURROW_TRIGGER_DISTANCE) {
                    phase = Phase.BURROWING
                }
            }

            Phase.BURROWING -> {
                if (distance <= CHARGE_TRIGGER_DISTANCE) {
                    phase = Phase.EMERGING
                    phaseSeconds = EMERGE_SECONDS
                    lockedChargeDirection.set(dx, dy)
                    if (!lockedChargeDirection.isZero) lockedChargeDirection.nor()
                }
            }

            Phase.EMERGING -> {
                if (phaseSeconds <= 0f) {
                    phase = Phase.CHARGE
                    phaseSeconds = CHARGE_SECONDS
                }
            }

            Phase.CHARGE -> {
                if (phaseSeconds <= 0f) {
                    phase = Phase.COOLDOWN
                    phaseSeconds = COOLDOWN_SECONDS
                }
            }
        }
    }

    override fun resolveMovementOverride(owner: Enemy, output: Vector2): Boolean {
        return when (phase) {
            Phase.BURROWING -> {
                output.set(targetX - owner.centerX, targetY - owner.centerY)
                if (!output.isZero) output.nor()
                true
            }
            Phase.EMERGING -> {
                output.setZero()
                true
            }
            Phase.CHARGE -> {
                output.set(lockedChargeDirection)
                true
            }
            else -> false
        }
    }

    override fun modifiers(): EnemyTraitModifiers {
        return when (phase) {
            Phase.BURROWING -> EnemyTraitModifiers(movementSpeedMultiplier = BURROW_SPEED_MULTIPLIER)
            Phase.CHARGE -> EnemyTraitModifiers(movementSpeedMultiplier = CHARGE_SPEED_MULTIPLIER)
            else -> EnemyTraitModifiers.IDENTITY
        }
    }

    override fun contactDamageMultiplier(owner: Enemy): Float {
        return when (phase) {
            Phase.BURROWING, Phase.EMERGING -> 0f
            Phase.CHARGE -> CHARGE_DAMAGE_MULTIPLIER
            else -> 1f
        }
    }

    override fun shouldNegateIncomingDamage(owner: Enemy, context: EnemyTraitDamageContext): Boolean {
        return phase == Phase.BURROWING
    }

    override fun render(owner: Enemy, shapeRenderer: ShapeRenderer) {
        if (phase != Phase.BURROWING) return
        shapeRenderer.color = Color(0.30f, 0.18f, 0.08f, 0.65f)
        shapeRenderer.ellipse(owner.centerX - 48f, owner.centerY - 18f, 96f, 36f)
    }

    companion object {
        private const val INITIAL_COOLDOWN_SECONDS = 12f
        private const val COOLDOWN_SECONDS = 45f
        private const val BURROW_TRIGGER_DISTANCE = 650f
        private const val CHARGE_TRIGGER_DISTANCE = 322f
        private const val EMERGE_SECONDS = 0.40f
        private const val CHARGE_SECONDS = 0.65f
        private const val BURROW_SPEED_MULTIPLIER = 3.50f
        private const val CHARGE_SPEED_MULTIPLIER = 6.50f
        private const val CHARGE_DAMAGE_MULTIPLIER = 3.25f
    }
}

private class GorillaStompTheEarthTrait : EnemyTrait {
    override val id = EnemyTraitIds.GORILLA_STOMP_THE_EARTH

    private enum class Phase { COOLDOWN, CASTING }
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
                phase = Phase.COOLDOWN
                remaining = COOLDOWN_SECONDS
            }
        }
    }

    override fun resolveMovementOverride(owner: Enemy, output: Vector2): Boolean {
        if (phase != Phase.CASTING) return false
        output.setZero()
        return true
    }

    override fun consumePlayerDamage(): List<EnemyTraitPlayerDamage> {
        if (pendingDamage.isEmpty()) return emptyList()
        return pendingDamage.toList().also { pendingDamage.clear() }
    }

    override fun render(owner: Enemy, shapeRenderer: ShapeRenderer) {
        if (phase != Phase.CASTING) return
        shapeRenderer.color = Color(1f, 0.08f, 0.04f, 0.65f)
        shapeRenderer.circle(owner.centerX, owner.centerY, RADIUS, 64)
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
        private const val CAST_SECONDS = 1.05f
        private const val RADIUS = 285f
        private const val DAMAGE_MULTIPLIER = 3.20f
    }
}

private class GhostHauntingPresenceTrait : EnemyTrait {
    override val id = EnemyTraitIds.GHOST_HAUNTING_PRESENCE

    override fun playerVisionMultiplier(owner: Enemy): Float {
        return if (owner.isDead) 1f else 0.75f
    }
}

private class SlimeSplitOnDeathTrait : EnemyTrait {
    override val id = EnemyTraitIds.SLIME_SPLIT_ON_DEATH

    private var splitActive = false
    private var partMaximumHealth = 0
    private val partHealth = IntArray(PART_COUNT)

    override fun resolveFinalIncomingDamage(
        owner: Enemy,
        context: EnemyTraitDamageContext,
        resolvedDamage: Int
    ): Int {
        if (resolvedDamage <= 0) return 0

        if (!splitActive) {
            if (resolvedDamage < owner.health.currentHealth) return resolvedDamage

            splitActive = true
            partMaximumHealth =
                (owner.health.maximumHealth * HEALTH_PER_PART_RATIO)
                    .roundToInt()
                    .coerceAtLeast(1)
            for (i in partHealth.indices) partHealth[i] = partMaximumHealth
            return 0
        }

        var remainingDamage = resolvedDamage
        for (i in partHealth.indices) {
            if (remainingDamage <= 0) break
            if (partHealth[i] <= 0) continue
            val applied = minOf(partHealth[i], remainingDamage)
            partHealth[i] -= applied
            remainingDamage -= applied
        }

        return if (partHealth.all { it <= 0 }) {
            owner.health.currentHealth.coerceAtLeast(1)
        } else {
            0
        }
    }

    override fun resolveMovementOverride(owner: Enemy, output: Vector2): Boolean {
        if (!splitActive) return false
        output.setZero()
        return true
    }

    override fun contactDamageMultiplier(owner: Enemy): Float {
        return if (splitActive) 0f else 1f
    }

    override fun render(owner: Enemy, shapeRenderer: ShapeRenderer) {
        if (!splitActive) return
        val offsets = arrayOf(-55f to -35f, 55f to -35f, -55f to 35f, 55f to 35f)
        shapeRenderer.color = Color(0.18f, 0.72f, 0.22f, 0.78f)
        offsets.forEachIndexed { index, offset ->
            if (partHealth[index] <= 0) return@forEachIndexed
            val ratio = partHealth[index].toFloat() / partMaximumHealth.coerceAtLeast(1).toFloat()
            shapeRenderer.circle(
                owner.centerX + offset.first,
                owner.centerY + offset.second,
                30f + 16f * ratio,
                24
            )
        }
    }

    companion object {
        private const val PART_COUNT = 4
        private const val HEALTH_PER_PART_RATIO = 0.14f
    }
}

private class VampireMistyEscapeTrait : EnemyTrait {
    override val id = EnemyTraitIds.MISTY_ESCAPE

    private var active = false
    private var cooldown = 0f
    private var targetX = 0f
    private var targetY = 0f
    private var healAccumulator = 0f

    override fun update(context: EnemyTraitUpdateContext) {
        if (context.owner.isDead) return
        val dt = context.delta.coerceAtLeast(0f)
        targetX = context.targetCenterX
        targetY = context.targetCenterY

        if (!active) {
            cooldown = (cooldown - dt).coerceAtLeast(0f)
            if (cooldown <= 0f && context.owner.health.healthRatio <= ACTIVATION_HEALTH_RATIO) {
                active = true
                healAccumulator = 0f
            }
            return
        }

        healAccumulator += context.owner.health.maximumHealth * HEAL_RATIO_PER_SECOND * dt
        val heal = healAccumulator.toInt()
        if (heal > 0) {
            context.owner.health.heal(heal)
            healAccumulator -= heal.toFloat()
        }

        if (context.owner.health.healthRatio >= TARGET_HEALTH_RATIO) {
            active = false
            cooldown = COOLDOWN_SECONDS
            healAccumulator = 0f
        }
    }

    override fun resolveMovementOverride(owner: Enemy, output: Vector2): Boolean {
        if (!active) return false
        output.set(owner.centerX - targetX, owner.centerY - targetY)
        if (!output.isZero) output.nor()
        return true
    }

    override fun modifiers(): EnemyTraitModifiers {
        return if (active) {
            EnemyTraitModifiers(movementSpeedMultiplier = MIST_SPEED_MULTIPLIER)
        } else {
            EnemyTraitModifiers.IDENTITY
        }
    }

    override fun shouldNegateIncomingDamage(owner: Enemy, context: EnemyTraitDamageContext): Boolean {
        return active
    }

    override fun contactDamageMultiplier(owner: Enemy): Float {
        return if (active) 0f else 1f
    }

    override fun render(owner: Enemy, shapeRenderer: ShapeRenderer) {
        if (!active) return
        shapeRenderer.color = Color(0.72f, 0.72f, 0.78f, 0.42f)
        shapeRenderer.ellipse(owner.centerX - 70f, owner.centerY - 42f, 140f, 84f)
    }

    companion object {
        private const val ACTIVATION_HEALTH_RATIO = 0.25f
        private const val TARGET_HEALTH_RATIO = 0.66f
        private const val HEAL_RATIO_PER_SECOND = 0.18f
        private const val MIST_SPEED_MULTIPLIER = 2.40f
        private const val COOLDOWN_SECONDS = 60f
    }
}

