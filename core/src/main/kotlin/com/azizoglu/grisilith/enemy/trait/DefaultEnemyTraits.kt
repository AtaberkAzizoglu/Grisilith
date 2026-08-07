package com.azizoglu.grisilith.enemy.trait

import com.azizoglu.grisilith.content.ContentId
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
            EnemyTraitIds.WOLF_PACK ->
                WolfPackTrait()

            EnemyTraitIds.BOAR_CHARGE ->
                BoarChargeTrait()

            EnemyTraitIds.GRIZZLY_ANGRY ->
                GrizzlyAngryTrait()

            EnemyTraitIds.CROCODILE_JUMP ->
                CrocodileJumpTrait()

            EnemyTraitIds.GHOST_INCORPOREAL_EVASION ->
                GhostIncorporealEvasionTrait()

            EnemyTraitIds.SLIME_ACIDIC_PATH ->
                SlimeAcidicPathTrait()

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
            0.50f
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
            0.0075f

        private const val PLAYER_CONTACT_MINIMUM_HEAL =
            35

        private const val ALLY_CONTACT_HEAL_RATIO =
            0.0040f

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
        3.25f
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
        4.0f
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
                        )
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
                            "Gorilla Throw"
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
            shapeRenderer.color =
                Color(
                    1f,
                    0.25f,
                    0.08f,
                    0.90f
                )

            shapeRenderer.rectLine(
                owner.centerX,
                owner.centerY,
                lockedTargetX,
                lockedTargetY,
                4f
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
            4.75f
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
                    MINIMUM_RADIUS
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
                    PUDDLE_DURATION_SECONDS
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
                        "Acidic Path"
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
            6f

        private const val TICK_SECONDS =
            0.50f

        private const val DROP_DISTANCE =
            72f

        private const val MINIMUM_RADIUS =
            42f

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
