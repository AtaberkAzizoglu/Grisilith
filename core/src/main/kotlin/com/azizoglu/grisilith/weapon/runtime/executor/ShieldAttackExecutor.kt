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
         * Lv.3 sonrasında Strength'in Shield
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
                definition.baseRange *
                    levelStats.rangeMultiplier
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

                maximumRange =
                    effectiveRange
            ) ?: return

        val attackDirection =
            Vector2(
                primaryTarget.centerX -
                    context.player.centerX,

                primaryTarget.centerY -
                    context.player.centerY
            )

        if (attackDirection.isZero) {
            return
        }

        attackDirection.nor()

        /*
         * Shield açısı Lv.3'te değişmiyor.
         * Geliştirme esas olarak menzil ve
         * Strength scaling kazandırıyor.
         */
        val coneAngle =
            definition.areaAngleDegrees

        CombatDebugVisualManager.addCone(
            originX =
                context.player.centerX,

            originY =
                context.player.centerY,

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
                .allEnemies
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

        val appliedDamage =
            enemy.takeDamage(
                finalDamage
            )

        if (appliedDamage > 0) {
            context.onDamageDealt(
                appliedDamage,
                context.weapon.displayName
            )

            enemy.applyKnockback(
                directionX =
                    enemy.centerX -
                        context.player.centerX,

                directionY =
                    enemy.centerY -
                        context.player.centerY,

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
                    context.player.centerX,

                enemy.centerY -
                    context.player.centerY
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
