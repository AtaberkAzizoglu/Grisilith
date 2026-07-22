package com.azizoglu.grisilith.weapon.runtime.executor

import com.azizoglu.grisilith.weapon.runtime.WeaponAttackContext
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackExecutor
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.azizoglu.grisilith.weapon.runtime.WeaponTargetSelector

class ShortswordAttackExecutor(
    private val damageCalculator:
    WeaponDamageCalculator,
    private val targetSelector:
    WeaponTargetSelector
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

        val effectiveRange =
            (
                definition.baseRange *
                    rarityStats.rangeMultiplier
                )
                .coerceAtLeast(0f)

        val target =
            targetSelector.selectTarget(
                targetingType =
                    definition.targetingType,
                player = context.player,
                enemyManager =
                    context.enemyManager,
                maximumRange =
                    effectiveRange
            ) ?: return

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
            context.onEnemyKilled(
                target
            )
        }
    }
}
