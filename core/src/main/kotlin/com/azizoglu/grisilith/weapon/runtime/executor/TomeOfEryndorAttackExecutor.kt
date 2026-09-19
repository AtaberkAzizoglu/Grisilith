package com.azizoglu.grisilith.weapon.runtime.executor

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyDamageContext
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackContext
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackExecutor
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.azizoglu.grisilith.weapon.runtime.WeaponTargetSelector
import com.badlogic.gdx.math.Vector2
import kotlin.math.roundToInt
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.content.ConditionType
import com.azizoglu.grisilith.combat.condition.ConditionDamageCalculator
import com.azizoglu.grisilith.statistics.PlayerStats
import com.badlogic.gdx.math.MathUtils
import com.azizoglu.grisilith.weapon.visual.attack.MeleeAttackVisualKind
import com.azizoglu.grisilith.weapon.visual.attack.MeleeAttackVisualManager

class TomeOfEryndorAttackExecutor(
    private val damageCalculator:
    WeaponDamageCalculator,

    private val targetSelector:
    WeaponTargetSelector,

    /*
     * Lv.1–2 normal projectile davranışını
     * mevcut ortak executor üzerinden çalıştırır.
     */
    private val projectileExecutor:
    ProjectileWeaponAttackExecutor,

    private val playerStats:
    PlayerStats
) : WeaponAttackExecutor {

    companion object {

        /*
         * Lv.3 sonrasında ışın menzili,
         * normal projectile menzilinden biraz kısa.
         */
        private const val BEAM_RANGE_MULTIPLIER =
            0.85f

        /*
         * Işının toplam hitbox genişliğinin
         * yaklaşık yarısı.
         */
        private const val BEAM_HALF_WIDTH =
            18f

        /*
         * İlk hedeften sonraki düşmanların
         * alacağı hasar.
         */
        private const val SECONDARY_TARGET_DAMAGE_MULTIPLIER =
            0.75f
    }

    override fun execute(
        context: WeaponAttackContext
    ) {
        /*
         * Lv.1–2 eski Fire/Necrotic projectile
         * sistemini kullanmaya devam eder.
         */
        if (
            !context.weapon
                .hasBasicAttackUpgrade
        ) {
            projectileExecutor.execute(
                context
            )

            return
        }

        executeUpgradedFireBeam(
            context
        )
    }

    private fun executeUpgradedFireBeam(
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
                        levelStats.rangeMultiplier *
                        BEAM_RANGE_MULTIPLIER
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

        val startX =
            context.attackOriginX

        val startY =
            context.attackOriginY

        val endX =
            startX +
                attackDirection.x *
                effectiveRange

        val endY =
            startY +
                attackDirection.y *
                effectiveRange

        val attackAngle =
            MathUtils.atan2(attackDirection.y, attackDirection.x) *
                MathUtils.radiansToDegrees

        MeleeAttackVisualManager.spawn(
            kind = MeleeAttackVisualKind.FIRE_BEAM,
            centerX = startX + attackDirection.x * effectiveRange * 0.5f,
            centerY = startY + attackDirection.y * effectiveRange * 0.5f,
            rotationDegrees = attackAngle,
            worldWidth = effectiveRange,
            worldHeight = BEAM_HALF_WIDTH * 3.4f,
            durationSeconds = 0.28f
        )

        /*
         * Önce ışına değen bütün düşmanları bulur,
         * ardından oyuncuya yakınlığa göre sıralar.
         *
         * Böylece ilk hedef ve sonraki hedef
         * hasarları doğru uygulanır.
         */
        val targets =
            context.enemyManager
                .allEnemies
                .asSequence()
                .filter { enemy ->
                    isEnemyInsideBeam(
                        enemy = enemy,
                        startX = startX,
                        startY = startY,
                        endX = endX,
                        endY = endY
                    )
                }
                .sortedBy { enemy ->
                    Vector2.dst2(
                        startX,
                        startY,
                        enemy.centerX,
                        enemy.centerY
                    )
                }
                .toList()

        targets.forEachIndexed {
                targetIndex,
                enemy ->

            val damageMultiplier =
                if (targetIndex == 0) {
                    1f
                } else {
                    SECONDARY_TARGET_DAMAGE_MULTIPLIER
                }

            damageEnemy(
                enemy = enemy,
                context = context,
                damageMultiplier =
                    damageMultiplier
            )
        }
    }

    private fun isEnemyInsideBeam(
        enemy: Enemy,
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float
    ): Boolean {
        if (enemy.isDead) {
            return false
        }

        val distanceToBeam =
            distanceFromPointToSegment(
                pointX =
                    enemy.centerX,

                pointY =
                    enemy.centerY,

                startX =
                    startX,

                startY =
                    startY,

                endX =
                    endX,

                endY =
                    endY
            )

        /*
         * Enemy genişliğini de hesaba katar.
         * Böylece yalnızca merkezi tam çizgide
         * olan düşmanlar değil, ışına fiziksel
         * olarak değen düşmanlar da vurulur.
         */
        return distanceToBeam <=
            BEAM_HALF_WIDTH +
            enemy.bounds.width / 2f
    }

    private fun damageEnemy(
        enemy: Enemy,
        context: WeaponAttackContext,
        damageMultiplier: Float
    ) {
        if (enemy.isDead) {
            return
        }

        /*
         * Her hedefin crit hesabı bağımsızdır.
         */
        val baseDamage =
            damageCalculator
                .calculateNonCriticalDamage(
                    weapon =
                        context.weapon,

                    damageType =
                        DamageType.FIRE
                )

        val finalDamage =
            (
                baseDamage *
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
                amount = tamgaModifiedDamage,
                damageContext =
                    EnemyDamageContext(
                        damageType = DamageType.FIRE,
                        sourceWeaponRuntimeId =
                            context.weapon.runtimeId,
                        sourceWeaponDefinitionId =
                            context.weapon.definition.id.value,
                        sourceName =
                            context.weapon.displayName
                    )
            )

        if (appliedDamage > 0) {
            enemy.applyCondition(
                ConditionDamageCalculator.createApplication(
                    type = ConditionType.BURNED,
                    playerStats = playerStats,
                    sourceWeaponRuntimeId = context.weapon.runtimeId,
                    sourceWeaponDefinitionId = context.weapon.definition.id.value,
                    sourceName = context.weapon.displayName,
                    sourceCenterX = context.attackOriginX,
                    sourceCenterY = context.attackOriginY
                )
            )

            context.onDamageDealt(
                appliedDamage,
                context.weapon.displayName
            )

            /*
             * Tome definition'da knockback 0 ise
             * hiçbir hareket oluşturmaz.
             */
            val definition =
                context.weapon.definition

            val levelStats =
                context.weapon.currentLevelStats

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
                DamageType.FIRE
        )
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
