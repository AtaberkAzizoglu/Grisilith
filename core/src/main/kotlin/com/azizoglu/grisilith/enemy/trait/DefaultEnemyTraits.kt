package com.azizoglu.grisilith.enemy.trait

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyType
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

internal object DefaultEnemyTraits {

    fun create(
        id: ContentId
    ): EnemyTrait? {
        return when (
            id
        ) {
            EnemyTraitIds.WOLF_PACK ->
                WolfPackTrait()

            EnemyTraitIds.GRIZZLY_ANGRY ->
                GrizzlyAngryTrait()

            EnemyTraitIds.GHOST_INCORPOREAL_EVASION ->
                GhostIncorporealEvasionTrait()

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
