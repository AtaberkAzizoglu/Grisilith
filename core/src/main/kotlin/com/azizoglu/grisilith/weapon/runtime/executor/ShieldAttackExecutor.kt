package com.azizoglu.grisilith.weapon.runtime.executor

import com.azizoglu.grisilith.combat.debug.CombatDebugVisualManager
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackContext
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackExecutor
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.azizoglu.grisilith.weapon.runtime.WeaponTargetSelector
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import kotlin.math.roundToInt
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.weapon.visual.attack.MeleeAttackVisualKind
import com.azizoglu.grisilith.weapon.visual.attack.MeleeAttackVisualManager

class ShieldAttackExecutor(
    private val damageCalculator:
    WeaponDamageCalculator,

    private val targetSelector:
    WeaponTargetSelector,

    private val playerStats:
    PlayerStats
) : WeaponAttackExecutor {

    companion object {
        /*
         * Lv.4 sonrasında Strength'in Shield
         * hasarına küçük katkısı.
         */
        private const val UPGRADED_STRENGTH_SCALING =
            0.15f
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
                context.effectiveWeaponRange(
                    definition.baseRange *
                        levelStats.rangeMultiplier
                )
                )
                .coerceAtLeast(1f)

        val primaryTarget =
            targetSelector.selectTarget(
                targetingType =
                    definition.targetingType,

                player =
                    context.player,

                enemyManager =
                    context.enemyManager,


                originX = context.attackOriginX,
                originY = context.attackOriginY,
                maximumRange =
                    effectiveRange
            ) ?: return

        context.weapon
            .registerNormalAttack()

        val attackDirection =
            Vector2(
                primaryTarget.centerX -
                    context.attackOriginX,

                primaryTarget.centerY -
                    context.attackOriginY
            )

        if (attackDirection.isZero) {
            return
        }

        attackDirection.nor()

        val attackAngle =
            MathUtils.atan2(
                attackDirection.y,
                attackDirection.x
            ) *
                MathUtils.radiansToDegrees

        val upgraded =
            context.weapon
                .hasBasicAttackUpgrade

        MeleeAttackVisualManager.spawn(
            kind =
                MeleeAttackVisualKind
                    .SHIELD_CONE,
            centerX =
                context.attackOriginX + attackDirection.x * effectiveRange * 0.42f,
            centerY =
                context.attackOriginY + attackDirection.y * effectiveRange * 0.42f,
            rotationDegrees =
                attackAngle,
            worldWidth =
                effectiveRange *
                    1.72f,
            worldHeight =
                effectiveRange *
                    if (upgraded) {
                        1.32f
                    } else {
                        1.12f
                    },
            durationSeconds =
                0.27f
        )

        /*
         * Shield açısı Lv.4'te değişmiyor.
         * Geliştirme esas olarak menzil ve
         * Strength scaling kazandırıyor.
         */
        val coneAngle =
            definition.areaAngleDegrees

        CombatDebugVisualManager.addCone(
            originX =
                context.attackOriginX,

            originY =
                context.attackOriginY,

            directionX =
                attackDirection.x,

            directionY =
                attackDirection.y,

            range =
                effectiveRange,

            angleDegrees =
                coneAngle,

            upgraded =
                context.weapon
                    .hasBasicAttackUpgrade
        )

        val halfConeAngle =
            coneAngle / 2f

        val targets =
            context.enemyManager
                .weaponTargets
                .filter { enemy ->
                    isInsideCone(
                        enemy = enemy,
                        context = context,
                        attackDirection =
                            attackDirection,
                        maximumRange =
                            effectiveRange,
                        halfConeAngle =
                            halfConeAngle
                    )
                }

        targets.forEach { enemy ->
            damageEnemy(
                enemy = enemy,
                context = context
            )
        }
    }

    private fun damageEnemy(
        enemy: Enemy,
        context: WeaponAttackContext
    ) {
        if (enemy.isDead) {
            return
        }

        val definition =
            context.weapon.definition

        val levelStats =
            context.weapon.currentLevelStats

        val damageResult =
            damageCalculator.calculate(
                weapon = context.weapon
            )

        val strengthBonus =
            if (
                context.weapon
                    .hasBasicAttackUpgrade
            ) {
                playerStats.totalScore(
                    StatType.STRENGTH
                ) *
                    UPGRADED_STRENGTH_SCALING
            } else {
                0f
            }

        val finalDamage =
            (
                damageResult.damage +
                    strengthBonus
                )
                .roundToInt()
                .coerceAtLeast(1)

        val wasAliveBeforeAttack =
            !enemy.isDead

        val tamgaModifiedDamage =
            context.modifiedDamageAgainst(
                enemy = enemy,
                baseDamage =
                    finalDamage
            )

        val appliedDamage =
            enemy.takeDamage(
                tamgaModifiedDamage
            )

        if (appliedDamage > 0) {
            context.onDamageDealt(
                appliedDamage,
                context.weapon.displayName
            )

            enemy.applyKnockback(
                directionX =
                    enemy.centerX -
                        context.attackOriginX,

                directionY =
                    enemy.centerY -
                        context.attackOriginY,

                strength =
                    definition.baseKnockback *
                        levelStats
                            .knockbackMultiplier
            )
        }

        if (
            wasAliveBeforeAttack &&
            enemy.isDead
        ) {
            context.onEnemyKilled(
                enemy
            )
        }

        context.reportWeaponHit(
            enemy = enemy,
            appliedDamage =
                appliedDamage,
            damageType =
                context.weapon
                    .definition
                    .damageTypes
                    .firstOrNull { type ->
                        type !=
                            DamageType.NONE
                    }
                    ?: DamageType.NONE
        )
    }

    private fun isInsideCone(
        enemy: Enemy,
        context: WeaponAttackContext,
        attackDirection: Vector2,
        maximumRange: Float,
        halfConeAngle: Float
    ): Boolean {
        if (enemy.isDead) {
            return false
        }

        val directionToEnemy =
            Vector2(
                enemy.centerX -
                    context.attackOriginX,

                enemy.centerY -
                    context.attackOriginY
            )

        val distance =
            directionToEnemy.len()

        if (
            distance <= 0f ||
            distance > maximumRange
        ) {
            return false
        }

        directionToEnemy.nor()

        val dotProduct =
            attackDirection.dot(
                directionToEnemy
            )
                .coerceIn(
                    -1f,
                    1f
                )

        val angleToEnemy =
            MathUtils.acos(
                dotProduct
            ) *
                MathUtils.radiansToDegrees

        return angleToEnemy <=
            halfConeAngle
    }
}
