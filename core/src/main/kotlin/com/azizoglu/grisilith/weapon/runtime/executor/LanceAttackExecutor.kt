package com.azizoglu.grisilith.weapon.runtime.executor

import com.azizoglu.grisilith.combat.debug.CombatDebugVisualManager
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.audio.GameAudio
import com.azizoglu.grisilith.audio.UiSound
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackContext
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackExecutor
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.azizoglu.grisilith.weapon.runtime.WeaponTargetSelector
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.MathUtils
import com.azizoglu.grisilith.weapon.visual.attack.MeleeAttackVisualKind
import com.azizoglu.grisilith.weapon.visual.attack.MeleeAttackVisualManager

class LanceAttackExecutor(
    private val damageCalculator:
    WeaponDamageCalculator,

    private val targetSelector:
    WeaponTargetSelector
) : WeaponAttackExecutor {

    companion object {
        private const val UPGRADED_WIDTH_MULTIPLIER =
            1.35f
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

        context.weapon.registerNormalAttack()

        val direction =
            Vector2(
                primaryTarget.centerX -
                    context.attackOriginX,

                primaryTarget.centerY -
                    context.attackOriginY
            )

        if (direction.isZero) {
            return
        }

        direction.nor()
        GameAudio.play(UiSound.LANCE_THRUST)

        val attackAngle =
            MathUtils.atan2(
                direction.y,
                direction.x
            ) *
                MathUtils.radiansToDegrees

        val halfWidth =
            (
                definition.areaRadius *
                    if (
                        context.weapon
                            .hasBasicAttackUpgrade
                    ) {
                        UPGRADED_WIDTH_MULTIPLIER
                    } else {
                        1f
                    }
                )
                .coerceAtLeast(1f)

        MeleeAttackVisualManager.spawn(
            kind =
                MeleeAttackVisualKind
                    .LANCE_THRUST,
            centerX =
                context.attackOriginX + direction.x * effectiveRange * 0.50f,
            centerY =
                context.attackOriginY + direction.y * effectiveRange * 0.50f,
            rotationDegrees =
                attackAngle,
            worldWidth =
                effectiveRange *
                    1.04f,
            worldHeight =
                (
                    halfWidth *
                        2.2f
                    )
                    .coerceAtLeast(
                        46f
                    ),
            durationSeconds =
                0.24f
        )

        val startX =
            context.attackOriginX

        val startY =
            context.attackOriginY

        val endX =
            startX +
                direction.x *
                effectiveRange

        val endY =
            startY +
                direction.y *
                effectiveRange

        CombatDebugVisualManager.addLine(
            startX = startX,
            startY = startY,
            endX = endX,
            endY = endY,
            upgraded =
                context.weapon
                    .hasBasicAttackUpgrade
        )

        val targets =
            context.enemyManager
                .weaponTargets
                .filter { enemy ->
                    isInsideAttackLine(
                        enemy = enemy,
                        startX = startX,
                        startY = startY,
                        endX = endX,
                        endY = endY,
                        halfWidth = halfWidth
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

    private fun isInsideAttackLine(
        enemy: Enemy,
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        halfWidth: Float
    ): Boolean {
        if (enemy.isDead) {
            return false
        }

        val distance =
            distanceFromPointToSegment(
                pointX = enemy.centerX,
                pointY = enemy.centerY,
                startX = startX,
                startY = startY,
                endX = endX,
                endY = endY
            )

        return distance <=
            halfWidth +
            enemy.bounds.width / 2f
    }

    private fun distanceFromPointToSegment(
        pointX: Float,
        pointY: Float,
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float
    ): Float {
        val segmentX =
            endX - startX

        val segmentY =
            endY - startY

        val segmentLengthSquared =
            segmentX * segmentX +
                segmentY * segmentY

        if (
            segmentLengthSquared <=
            0.0001f
        ) {
            return Vector2.dst(
                pointX,
                pointY,
                startX,
                startY
            )
        }

        val projection =
            (
                (
                    pointX - startX
                    ) * segmentX +
                    (
                        pointY - startY
                        ) * segmentY
                ) /
                segmentLengthSquared

        val clampedProjection =
            projection.coerceIn(
                0f,
                1f
            )

        val closestX =
            startX +
                segmentX *
                clampedProjection

        val closestY =
            startY +
                segmentY *
                clampedProjection

        return Vector2.dst(
            pointX,
            pointY,
            closestX,
            closestY
        )
    }
}
