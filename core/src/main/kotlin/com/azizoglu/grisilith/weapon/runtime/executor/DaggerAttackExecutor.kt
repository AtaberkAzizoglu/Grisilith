package com.azizoglu.grisilith.weapon.runtime.executor

import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackContext
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackExecutor
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.azizoglu.grisilith.weapon.runtime.WeaponTargetSelector
import com.badlogic.gdx.math.Vector2
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.content.DamageType

class DaggerAttackExecutor(
    private val damageCalculator:
    WeaponDamageCalculator,

    private val targetSelector:
    WeaponTargetSelector,

    private val playerStats:
    PlayerStats
) : WeaponAttackExecutor {

    override fun execute(
        context: WeaponAttackContext
    ) {
        val definition =
            context.weapon.definition

        val levelStats =
            context.weapon.currentLevelStats

        /*
         * Dagger'ın uzak saldırı menzili.
         * secondaryRange tanımlı değilse
         * normal menzil kullanılır.
         */
        val effectiveThrowRange =
            (
                (
                    definition.secondaryRange
                        ?: definition.baseRange
                    ) *
                    levelStats.rangeMultiplier
                )
                .coerceAtLeast(0f)

        val target =
            targetSelector.selectTarget(
                targetingType =
                    definition.targetingType,
                player =
                    context.player,
                enemyManager =
                    context.enemyManager,
                maximumRange =
                    effectiveThrowRange
            ) ?: return

        val targetDistance =
            Vector2.dst(
                context.player.centerX,
                context.player.centerY,
                target.centerX,
                target.centerY
            )

        val effectiveMeleeRange =
            (
                definition.baseRange *
                    levelStats.rangeMultiplier
                )
                .coerceAtLeast(0f)

        if (
            targetDistance <=
            effectiveMeleeRange
        ) {
            executeMeleeAttack(
                context = context,
                target = target
            )
        } else {
            executeThrowAttack(
                context = context,
                targetX = target.centerX,
                targetY = target.centerY
            )
        }
    }

    private fun executeMeleeAttack(
        context: WeaponAttackContext,
        target:
        com.azizoglu.grisilith.enemy.Enemy
    ) {
        /*
         * Yakın saldırının crit kontrolü
         * bu isabet için bağımsız yapılır.
         */
        val damageResult =
            damageCalculator.calculate(
                weapon = context.weapon
            )

        val wasAliveBeforeAttack =
            !target.isDead

        val appliedDamage =
            target.takeDamage(
                damageResult.damage
            )

        if (appliedDamage > 0) {
            context.onDamageDealt(
                appliedDamage,
                context.weapon.displayName
            )
        }

        if (
            wasAliveBeforeAttack &&
            target.isDead
        ) {
            context.onEnemyKilled(target)
        }
    }

    private fun executeThrowAttack(
        context: WeaponAttackContext,
        targetX: Float,
        targetY: Float
    ) {
        val damageResult =
            damageCalculator.calculate(
                weapon = context.weapon
            )

        val definition =
            context.weapon.definition

        val levelStats =
            context.weapon.currentLevelStats

        val statAdditionalTargets =
            (
                playerStats
                    .projectileTargetCount() -
                    1
                )
                .coerceAtLeast(0)

        val maximumTargets =
            (
                definition.baseMaximumTargets +
                    levelStats.additionalTargets +
                    statAdditionalTargets
                )
                .coerceAtLeast(1)

        val throwRange =
            (
                (
                    definition.secondaryRange
                        ?: definition.baseRange
                    ) *
                    levelStats.rangeMultiplier
                )
                .coerceAtLeast(1f)

        val damageType =
            definition.damageTypes
                .firstOrNull()
                ?: DamageType.NONE

        context.projectiles.add(
            Projectile(
                startX =
                    context.player.centerX,

                startY =
                    context.player.centerY,

                targetX =
                    targetX,

                targetY =
                    targetY,

                sourceWeaponName =
                    context.weapon.displayName,

                damage =
                    damageResult.damage,

                isCriticalHit =
                    damageResult.isCriticalHit,

                maximumTargets =
                    maximumTargets,

                projectileSpeed =
                    definition
                        .baseProjectileSpeed
                        .coerceAtLeast(1f),

                maximumTravelDistance =
                    throwRange,

                damageType =
                    damageType,

                knockback =
                    (
                        definition.baseKnockback *
                            levelStats.knockbackMultiplier
                        )
                        .coerceAtLeast(0f)
            )
        )
    }
}
