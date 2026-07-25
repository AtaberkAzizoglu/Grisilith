package com.azizoglu.grisilith.weapon.runtime.executor

import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackContext
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackExecutor
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.azizoglu.grisilith.weapon.runtime.WeaponTargetSelector
import com.azizoglu.grisilith.content.DamageType

class ProjectileWeaponAttackExecutor(
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

        val effectiveRange =
            (
                definition.baseRange *
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
                    effectiveRange
            ) ?: return

        val projectileCount =
            definition.baseProjectileCount
                .coerceAtLeast(1)

        repeat(projectileCount) {
            createProjectile(
                context = context,
                targetX = target.centerX,
                targetY = target.centerY
            )
        }
    }

    private fun createProjectile(
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

        val selectedDamageType =
            selectDamageType(
                damageTypes =
                    definition.damageTypes,
                randomSelection =
                    definition
                        .randomDamageTypePerAttack
            )

        val effectiveRange =
            (
                definition.baseRange *
                    levelStats.rangeMultiplier
                )
                .coerceAtLeast(1f)

        val effectiveProjectileSpeed =
            definition
                .baseProjectileSpeed
                .coerceAtLeast(1f)

        val effectiveKnockback =
            (
                definition.baseKnockback *
                    levelStats.knockbackMultiplier
                )
                .coerceAtLeast(0f)

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
                    effectiveProjectileSpeed,

                maximumTravelDistance =
                    effectiveRange,

                damageType =
                    selectedDamageType,

                knockback =
                    effectiveKnockback
            )
        )
    }

    private fun selectDamageType(
        damageTypes: Set<DamageType>,
        randomSelection: Boolean
    ): DamageType {
        if (damageTypes.isEmpty()) {
            return DamageType.NONE
        }

        if (
            randomSelection &&
            damageTypes.size > 1
        ) {
            return damageTypes.random()
        }

        return damageTypes.first()
    }

}
