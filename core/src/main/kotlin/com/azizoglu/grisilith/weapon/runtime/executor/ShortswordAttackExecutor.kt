package com.azizoglu.grisilith.weapon.runtime.executor

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackContext
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackExecutor
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.azizoglu.grisilith.weapon.runtime.WeaponTargetSelector
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.MathUtils
import kotlin.math.roundToInt
import com.azizoglu.grisilith.combat.debug.CombatDebugVisualManager
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.weapon.visual.attack.MeleeAttackVisualKind
import com.azizoglu.grisilith.weapon.visual.attack.MeleeAttackVisualManager

class ShortswordAttackExecutor(
    private val damageCalculator:
    WeaponDamageCalculator,

    private val targetSelector:
    WeaponTargetSelector
) : WeaponAttackExecutor {

    companion object {
        /*
         * Geçici prototip ölçüleri.
         * Görsel assetler geldiğinde tekrar
         * dengelenecek.
         */
        private const val NORMAL_LINE_LENGTH =
            95f

        private const val UPGRADED_LINE_LENGTH =
            125f

        private const val LINE_HALF_WIDTH =
            15f

        private const val SECONDARY_DAMAGE_MULTIPLIER =
            0.75f
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

        val playerToTarget =
            Vector2(
                primaryTarget.centerX -
                    context.attackOriginX,

                primaryTarget.centerY -
                    context.attackOriginY
            )

        if (playerToTarget.isZero) {
            playerToTarget.set(
                1f,
                0f
            )
        }

        playerToTarget.nor()

        val attackAngle =
            MathUtils.atan2(
                playerToTarget.y,
                playerToTarget.x
            ) *
                MathUtils.radiansToDegrees

        val upgraded =
            context.weapon
                .hasBasicAttackUpgrade

        val visualSize =
            if (upgraded) {
                UPGRADED_LINE_LENGTH *
                    0.90f
            } else {
                NORMAL_LINE_LENGTH *
                    0.88f
            }

        MeleeAttackVisualManager.spawn(
            kind =
                if (upgraded) {
                    MeleeAttackVisualKind
                        .DOUBLE_SLASH_X
                } else {
                    MeleeAttackVisualKind
                        .DOUBLE_SLASH
                },
            centerX =
                context.attackOriginX + playerToTarget.x * visualSize * 0.35f,
            centerY =
                context.attackOriginY + playerToTarget.y * visualSize * 0.35f,
            rotationDegrees =
                attackAngle,
            worldWidth =
                visualSize,
            worldHeight =
                visualSize,
            durationSeconds =
                0.24f
        )

        if (
            upgraded
        ) {
            executeCrossedSlash(
                context = context,
                primaryTarget =
                    primaryTarget,
                playerToTarget =
                    playerToTarget
            )
        } else {
            executeSingleSlash(
                context = context,
                primaryTarget =
                    primaryTarget,
                playerToTarget =
                    playerToTarget
            )
        }
    }

    private fun executeSingleSlash(
        context: WeaponAttackContext,
        primaryTarget: Enemy,
        playerToTarget: Vector2
    ) {
        /*
         * Normal saldırı hedefin üzerinde,
         * oyuncu-hedef yönüne dik tek çizgidir.
         */
        val slashDirection =
            Vector2(
                -playerToTarget.y,
                playerToTarget.x
            )

        addDebugLine(
            centerX =
                primaryTarget.centerX,

            centerY =
                primaryTarget.centerY,

            lineDirection =
                slashDirection,

            lineLength =
                NORMAL_LINE_LENGTH,

            upgraded = false
        )

        val targets =
            enemiesTouchingLine(
                context = context,
                centerX =
                    primaryTarget.centerX,
                centerY =
                    primaryTarget.centerY,
                lineDirection =
                    slashDirection,
                lineLength =
                    NORMAL_LINE_LENGTH
            )

        targets.forEach { enemy ->
            val damageMultiplier =
                if (enemy === primaryTarget) {
                    1f
                } else {
                    SECONDARY_DAMAGE_MULTIPLIER
                }

            damageEnemy(
                enemy = enemy,
                context = context,
                damageMultiplier =
                    damageMultiplier
            )
        }
    }

    private fun executeCrossedSlash(
        context: WeaponAttackContext,
        primaryTarget: Enemy,
        playerToTarget: Vector2
    ) {
        /*
         * Lv.3:
         * Hedef merkezli iki çapraz çizgi.
         */
        val firstDirection =
            Vector2(
                playerToTarget
            )
                .rotateDeg(45f)

        val secondDirection =
            Vector2(
                playerToTarget
            )
                .rotateDeg(-45f)

        addDebugLine(
            centerX =
                primaryTarget.centerX,
            centerY =
                primaryTarget.centerY,
            lineDirection =
                firstDirection,
            lineLength =
                UPGRADED_LINE_LENGTH,
            upgraded = true
        )

        addDebugLine(
            centerX =
                primaryTarget.centerX,
            centerY =
                primaryTarget.centerY,
            lineDirection =
                secondDirection,
            lineLength =
                UPGRADED_LINE_LENGTH,
            upgraded = true
        )

        val firstLineTargets =
            enemiesTouchingLine(
                context = context,
                centerX =
                    primaryTarget.centerX,
                centerY =
                    primaryTarget.centerY,
                lineDirection =
                    firstDirection,
                lineLength =
                    UPGRADED_LINE_LENGTH
            )

        val secondLineTargets =
            enemiesTouchingLine(
                context = context,
                centerX =
                    primaryTarget.centerX,
                centerY =
                    primaryTarget.centerY,
                lineDirection =
                    secondDirection,
                lineLength =
                    UPGRADED_LINE_LENGTH
            )

        /*
         * İki çizginin ikisine de değen düşman
         * aynı saldırıda iki defa hasar almasın.
         */
        val uniqueTargets =
            LinkedHashSet<Enemy>()

        uniqueTargets.addAll(
            firstLineTargets
        )

        uniqueTargets.addAll(
            secondLineTargets
        )

        uniqueTargets.forEach { enemy ->
            /*
             * Lv.3 saldırısında ana hedef ve
             * çizgilere değen diğer hedefler
             * tam hasar alır.
             */
            damageEnemy(
                enemy = enemy,
                context = context,
                damageMultiplier = 1f
            )
        }
    }

    private fun enemiesTouchingLine(
        context: WeaponAttackContext,
        centerX: Float,
        centerY: Float,
        lineDirection: Vector2,
        lineLength: Float
    ): List<Enemy> {
        val safeDirection =
            Vector2(
                lineDirection
            )

        if (safeDirection.isZero) {
            safeDirection.set(
                1f,
                0f
            )
        }

        safeDirection.nor()

        val halfLength =
            lineLength / 2f

        val startX =
            centerX -
                safeDirection.x *
                halfLength

        val startY =
            centerY -
                safeDirection.y *
                halfLength

        val endX =
            centerX +
                safeDirection.x *
                halfLength

        val endY =
            centerY +
                safeDirection.y *
                halfLength

        return context.enemyManager
            .allEnemies
            .filter { enemy ->
                !enemy.isDead &&
                    distanceFromPointToSegment(
                        pointX =
                            enemy.centerX,
                        pointY =
                            enemy.centerY,
                        startX = startX,
                        startY = startY,
                        endX = endX,
                        endY = endY
                    ) <=
                    LINE_HALF_WIDTH +
                    enemy.bounds.width /
                    2f
            }
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

    private fun damageEnemy(
        enemy: Enemy,
        context: WeaponAttackContext,
        damageMultiplier: Float
    ) {
        if (enemy.isDead) {
            return
        }

        val definition =
            context.weapon.definition

        val levelStats =
            context.weapon.currentLevelStats

        /*
         * Her düşmanın crit hesabı bağımsızdır.
         */
        val damageResult =
            damageCalculator.calculate(
                weapon = context.weapon
            )

        val finalDamage =
            (
                damageResult.damage *
                    damageMultiplier
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

    private fun addDebugLine(
        centerX: Float,
        centerY: Float,
        lineDirection: Vector2,
        lineLength: Float,
        upgraded: Boolean
    ) {
        val safeDirection =
            Vector2(
                lineDirection
            )

        if (safeDirection.isZero) {
            safeDirection.set(
                1f,
                0f
            )
        }

        safeDirection.nor()

        val halfLength =
            lineLength / 2f

        val startX =
            centerX -
                safeDirection.x *
                halfLength

        val startY =
            centerY -
                safeDirection.y *
                halfLength

        val endX =
            centerX +
                safeDirection.x *
                halfLength

        val endY =
            centerY +
                safeDirection.y *
                halfLength

        CombatDebugVisualManager.addLine(
            startX = startX,
            startY = startY,
            endX = endX,
            endY = endY,
            upgraded = upgraded
        )
    }
}
