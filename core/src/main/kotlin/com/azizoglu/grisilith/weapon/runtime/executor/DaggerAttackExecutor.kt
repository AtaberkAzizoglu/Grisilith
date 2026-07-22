package com.azizoglu.grisilith.weapon.runtime.executor

import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackContext
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackExecutor
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.azizoglu.grisilith.weapon.runtime.WeaponTargetSelector
import com.badlogic.gdx.math.Vector2
import com.azizoglu.grisilith.statistics.PlayerStats

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

        val rarityStats =
            definition.statsFor(
                context.weapon.rarity
            )

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
                    rarityStats.rangeMultiplier
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
                    rarityStats.rangeMultiplier
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
        /*
         * Projectile oluşturulurken bu atış için
         * hasar ve crit ayrı hesaplanır.
         */
        val damageResult =
            damageCalculator.calculate(
                weapon = context.weapon
            )

        val definition =
            context.weapon.definition

        val rarityStats =
            definition.statsFor(
                context.weapon.rarity
            )

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
                    rarityStats
                        .additionalPiercingTargets +
                    statAdditionalTargets
                )
                .coerceAtLeast(1)

        context.projectiles.add(
            Projectile(
                startX =
                    context.player.centerX,
                startY =
                    context.player.centerY,
                targetX = targetX,
                targetY = targetY,
                sourceWeaponName =
                    context.weapon.displayName,
                damage =
                    damageResult.damage,
                isCriticalHit =
                    damageResult.isCriticalHit,
                maximumTargets =
                    maximumTargets
            )
        )
    }
}
