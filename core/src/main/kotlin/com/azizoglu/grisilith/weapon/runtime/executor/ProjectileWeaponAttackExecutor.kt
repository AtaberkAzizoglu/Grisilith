package com.azizoglu.grisilith.weapon.runtime.executor

import com.azizoglu.grisilith.content.DamageType
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

    companion object {
        private const val LONGBOW_ID =
            "WPN_LONGBOW"

        private const val CROSSBOW_ID =
            "WPN_CROSSBOW"

        private const val LONGBOW_RICOCHET_RANGE =
            360f

        private const val CROSSBOW_NORMAL_POST_HIT_RANGE =
            170f

        private const val CROSSBOW_UPGRADED_POST_HIT_RANGE =
            310f
    }

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

        val weaponId =
            definition.id.value

        val statAdditionalTargets =
            (
                playerStats
                    .projectileTargetCount() -
                    1
                )
                .coerceAtLeast(0)

        val isUpgradedLongbow =
            weaponId == LONGBOW_ID &&
                context.weapon
                    .hasBasicAttackUpgrade

        val isCrossbow =
            weaponId == CROSSBOW_ID

        val weaponSpecificAdditionalTargets =
            if (isUpgradedLongbow) {
                1
            } else {
                0
            }

        val maximumTargets =
            (
                definition.baseMaximumTargets +
                    levelStats.additionalTargets +
                    statAdditionalTargets +
                    weaponSpecificAdditionalTargets
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

        val secondaryHitDamageMultiplier =
            when {
                isCrossbow &&
                    context.weapon
                        .hasBasicAttackUpgrade -> {
                    1f
                }

                isCrossbow -> {
                    0.5f
                }

                else -> {
                    1f
                }
            }

        val ricochetCount =
            if (isUpgradedLongbow) {
                1
            } else {
                0
            }

        val ricochetRange =
            if (isUpgradedLongbow) {
                LONGBOW_RICOCHET_RANGE
            } else {
                0f
            }

        val postFirstHitTravelDistance =
            if (isCrossbow) {
                if (
                    context.weapon
                        .hasBasicAttackUpgrade
                ) {
                    CROSSBOW_UPGRADED_POST_HIT_RANGE
                } else {
                    CROSSBOW_NORMAL_POST_HIT_RANGE
                }
            } else {
                null
            }

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
                    effectiveKnockback,

                damageMultiplierAfterFirstHit =
                    secondaryHitDamageMultiplier,

                ricochetCount =
                    ricochetCount,

                ricochetRange =
                    ricochetRange,

                travelDistanceAfterFirstHit =
                    postFirstHitTravelDistance
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
