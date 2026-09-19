package com.azizoglu.grisilith.weapon.runtime.executor

import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.content.ConditionType
import com.azizoglu.grisilith.combat.condition.ConditionDamageCalculator
import com.azizoglu.grisilith.enemy.EnemyDamageContext
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackContext
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackExecutor
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.azizoglu.grisilith.weapon.runtime.WeaponTargetSelector
import com.azizoglu.grisilith.weapon.visual.attack.SoulDrainVisualManager
import kotlin.math.roundToInt

class TotemOfDarkCloudsAttackExecutor(
    private val damageCalculator:
    WeaponDamageCalculator,

    private val targetSelector:
    WeaponTargetSelector,

    private val projectileExecutor:
    ProjectileWeaponAttackExecutor,

    private val playerStats:
    PlayerStats
) : WeaponAttackExecutor {

    companion object {
        /*
         * Her dördüncü saldırının hasar bonusu.
         * Denge aşamasında tekrar ayarlanabilir.
         */
        private const val DRAIN_ATTACK_DAMAGE_MULTIPLIER =
            1.35f

    }

    override fun execute(
        context: WeaponAttackContext
    ) {
        /*
         * Lv.1–2:
         * Ortak projectile executor,
         * Necrotic/Lightning seçimini ve
         * Lightning sekmesini çalıştırır.
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

        /*
         * Sayaç WeaponInstance içinde tutulduğu
         * için her Totem kopyası bağımsızdır.
         */
        val attackNumber =
            context.weapon
                .registerNormalAttack()

        val isDrainAttack =
            attackNumber % 4 == 0

        if (isDrainAttack) {
            executeDrainAttack(
                context
            )
        } else {
            /*
             * ProjectileWeaponAttackExecutor
             * Totem Lv.3+ için damage type'ı
             * zorla Necrotic yapıyor.
             */
            projectileExecutor.execute(
                context
            )
        }
    }

    private fun executeDrainAttack(
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

        val target =
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

        /*
         * Hasar projectile oyuncuya dönmeden
         * önce, hedefte anında gerçekleşir.
         */
        val nonCriticalDamage =
            damageCalculator
                .calculateNonCriticalDamage(
                    weapon =
                        context.weapon,

                    damageType =
                        DamageType.NECROTIC
                )

        val finalDamage =
            (
                nonCriticalDamage *
                    DRAIN_ATTACK_DAMAGE_MULTIPLIER
                )
                .roundToInt()
                .coerceAtLeast(1)

        val wasAliveBeforeAttack =
            !target.isDead

        val tamgaModifiedDamage =
            context.modifiedDamageAgainst(
                enemy = target,
                baseDamage =
                    finalDamage
            )

        val appliedDamage =
            target.takeDamage(
                amount = tamgaModifiedDamage,
                damageContext = EnemyDamageContext(
                    damageType = DamageType.NECROTIC,
                    sourceWeaponRuntimeId = context.weapon.runtimeId,
                    sourceWeaponDefinitionId = context.weapon.definition.id.value,
                    sourceName = context.weapon.displayName
                )
            )

        if (appliedDamage > 0) {
            target.applyCondition(
                ConditionDamageCalculator.createApplication(
                    type = ConditionType.NECROSIS,
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
        }

        if (
            wasAliveBeforeAttack &&
            target.isDead
        ) {
            context.onEnemyKilled(
                target
            )
        }

        context.reportWeaponHit(
            enemy = target,
            appliedDamage =
                appliedDamage,
            damageType =
                DamageType.NECROTIC
        )

        SoulDrainVisualManager.spawn(
            enemy = target,
            player = context.player
        )
    }
}
