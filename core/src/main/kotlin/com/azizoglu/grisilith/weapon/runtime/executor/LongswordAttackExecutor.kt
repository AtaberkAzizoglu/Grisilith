package com.azizoglu.grisilith.weapon.runtime.executor

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackContext
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackExecutor
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.azizoglu.grisilith.weapon.runtime.WeaponTargetSelector
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.azizoglu.grisilith.combat.debug.CombatDebugVisualManager
import com.azizoglu.grisilith.weapon.visual.attack.MeleeAttackVisualKind
import com.azizoglu.grisilith.weapon.visual.attack.MeleeAttackVisualManager

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
                context.effectiveWeaponRange(
                    definition.baseRange *
                        levelStats.rangeMultiplier
                )
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

        context.weapon
            .registerNormalAttack()

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

        val attackAngle =
            MathUtils.atan2(
                attackDirection.y,
                attackDirection.x
            ) *
                MathUtils.radiansToDegrees

        val upgraded =
            context.weapon
                .hasBasicAttackUpgrade

        /*
         * Lv.3+ already receives a larger gameplay range through level stats.
         * The visual reads the SAME effectiveRange so its size grows with it.
         */
        MeleeAttackVisualManager.spawn(
            kind =
                MeleeAttackVisualKind
                    .LONGSWORD_CONE,
            centerX =
                primaryTarget.centerX,
            centerY =
                primaryTarget.centerY,
            rotationDegrees =
                attackAngle,
            worldWidth =
                effectiveRange *
                    0.86f,
            worldHeight =
                effectiveRange *
                    if (upgraded) {
                        0.64f
                    } else {
                        0.54f
                    },
            durationSeconds =
                0.25f
        )

        val effectiveConeAngle =
            if (
                context.weapon
                    .hasBasicAttackUpgrade
            ) {
                30f
            } else {
                definition.areaAngleDegrees
            }

        val halfConeAngle =
            (
                effectiveConeAngle /
                    2f
                )
                .coerceAtLeast(0f)

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
                effectiveConeAngle,

            upgraded =
                context.weapon
                    .hasBasicAttackUpgrade
        )

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
