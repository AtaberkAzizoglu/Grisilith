package com.azizoglu.grisilith.weapon.runtime.executor

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.audio.GameAudio
import com.azizoglu.grisilith.audio.UiSound
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
        // One sound per actual normal swing, not per enemy hit or Air Slash ability.
        GameAudio.play(UiSound.LONGSWORD_SWING)

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
         * Lv.4+ receives a larger gameplay range through level stats.
         * The visual reads the SAME effectiveRange so its size grows with it.
         */
        MeleeAttackVisualManager.spawn(
            kind =
                MeleeAttackVisualKind
                    .LONGSWORD_CONE,
            centerX =
                context.attackOriginX + attackDirection.x * effectiveRange * 0.42f,
            centerY =
                context.attackOriginY + attackDirection.y * effectiveRange * 0.42f,
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
                effectiveConeAngle,

            upgraded =
                context.weapon
                    .hasBasicAttackUpgrade
        )

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
            MathUtils.acos(dotProduct) *
                MathUtils.radiansToDegrees

        return angleToEnemy <=
            halfConeAngle
    }
}
