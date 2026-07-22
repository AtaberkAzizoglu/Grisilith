package com.azizoglu.grisilith.weapon.runtime.executor

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackContext
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackExecutor
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.azizoglu.grisilith.weapon.runtime.WeaponTargetSelector
import com.badlogic.gdx.math.Vector2

class LanceAttackExecutor(
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

        val effectiveAreaRadius =
            (
                definition.areaRadius *
                    rarityStats.rangeMultiplier
                )
                .coerceAtLeast(1f)

        val targets =
            context.enemyManager
                .allEnemies
                .filter { enemy ->
                    isInsideTargetArea(
                        enemy = enemy,
                        targetCenterX =
                            primaryTarget.centerX,
                        targetCenterY =
                            primaryTarget.centerY,
                        areaRadius =
                            effectiveAreaRadius
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

    private fun isInsideTargetArea(
        enemy: Enemy,
        targetCenterX: Float,
        targetCenterY: Float,
        areaRadius: Float
    ): Boolean {
        if (enemy.isDead) {
            return false
        }

        val distance =
            Vector2.dst(
                targetCenterX,
                targetCenterY,
                enemy.centerX,
                enemy.centerY
            )

        return distance <= areaRadius
    }
}
