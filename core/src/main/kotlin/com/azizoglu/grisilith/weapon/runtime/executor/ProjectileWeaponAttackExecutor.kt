package com.azizoglu.grisilith.weapon.runtime.executor

import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackContext
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackExecutor
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.azizoglu.grisilith.weapon.runtime.WeaponTargetSelector

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
        /*
         * Her oluşturulan projectile için
         * hasar ve kritik vuruş ayrı hesaplanır.
         *
         * Bir saldırı iki projectile üretirse
         * birisi critical, diğeri normal olabilir.
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
                    maximumTargets
            )
        )
    }
}
