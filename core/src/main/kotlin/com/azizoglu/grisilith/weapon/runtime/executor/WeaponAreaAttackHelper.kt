package com.azizoglu.grisilith.weapon.runtime.executor

import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyDamageContext
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackContext
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.weapon.skill.DefaultWeaponAbilities

object WeaponAreaAttackHelper {

    fun damageEnemy(
        enemy: Enemy,
        context: WeaponAttackContext,
        damageCalculator:
        WeaponDamageCalculator
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
                weapon =
                    context.weapon
            )

        val damageType =
            definition.damageTypes
                .firstOrNull { type ->
                    type !=
                        DamageType.NONE
                }
                ?: DamageType.NONE

        /*
         * Crit ve Weapon scaling hesaplandıktan
         * sonra hedefe bağlı Tamga modifier'ları
         * uygulanır.
         */
        val finalDamage =
            context.modifiedDamageAgainst(
                enemy = enemy,
                baseDamage =
                    damageResult.damage
            )

        val wasAliveBeforeAttack =
            !enemy.isDead

        var totalAppliedDamage =
            enemy.takeDamage(
                amount = finalDamage,
                damageContext =
                    EnemyDamageContext(
                        damageType = damageType,
                        sourceWeaponRuntimeId =
                            context.weapon.runtimeId,
                        sourceWeaponDefinitionId =
                            context.weapon.definition.id.value,
                        sourceName =
                            context.weapon.displayName,
                        sourceWeaponTags =
                            definition.tags,
                        attackerPiercingIgnoreRatio =
                            context.player.stats
                                .piercingIgnoreRatio()
                    )
            )

        /*
         * Executioner:
         * A Level 5 Longsword immediately finishes NORMAL and
         * UNIQUE enemies that survive the hit at or below 15% HP.
         *
         * Boss and Mini Boss enemies are explicitly excluded.
         * The ability is consumed only when an actual execution
         * can happen, then enters its 5 second cooldown.
         */
        val canBeExecuted =
            !enemy.isDead &&
                enemy.type in
                setOf(
                    EnemyType.NORMAL,
                    EnemyType.UNIQUE
                ) &&
                enemy.health.healthRatio <=
                DefaultWeaponAbilities
                    .EXECUTIONER_HEALTH_THRESHOLD

        val isExecutionerWeapon =
            context.weapon
                .unlockedAbility
                ?.id ==
                DefaultWeaponAbilities
                    .EXECUTIONER_ID

        if (
            canBeExecuted &&
            isExecutionerWeapon &&
            context.triggerAutomaticAbility()
        ) {
            enemy.markExecuted()

            totalAppliedDamage +=
                enemy.takeDamage(
                    amount =
                        enemy.health.currentHealth,
                    damageContext =
                        EnemyDamageContext(
                            damageType = damageType,
                            sourceWeaponRuntimeId =
                                context.weapon.runtimeId,
                            sourceWeaponDefinitionId =
                                context.weapon.definition.id.value,
                            sourceName =
                                context.weapon.displayName,
                            sourceWeaponTags =
                                definition.tags,
                            attackerPiercingIgnoreRatio =
                                context.player.stats
                                    .piercingIgnoreRatio()
                        )
                )
        }

        if (
            totalAppliedDamage >
            0
        ) {
            context.onDamageDealt(
                totalAppliedDamage,
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
                totalAppliedDamage,
            damageType =
                damageType
        )
    }
}
