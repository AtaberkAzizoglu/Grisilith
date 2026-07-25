package com.azizoglu.grisilith.weapon.runtime.executor

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackContext
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackExecutor
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.azizoglu.grisilith.weapon.runtime.WeaponTargetSelector
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2

class LongswordAttackExecutor(
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

        val levelStats =
            context.weapon.currentLevelStats

        val effectiveRange =
            (
                definition.baseRange *
                    levelStats.rangeMultiplier
                )
                .coerceAtLeast(0f)

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

        val halfConeAngle =
            (
                definition.areaAngleDegrees /
                    2f
                )
                .coerceAtLeast(0f)

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
            WeaponAreaAttackHelper.damageEnemy(
                enemy = enemy,
                context = context,
                damageCalculator =
                    damageCalculator
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
            MathUtils.acos(dotProduct) *
                MathUtils.radiansToDegrees

        return angleToEnemy <=
            halfConeAngle
    }
}
