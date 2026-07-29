package com.azizoglu.grisilith.weapon.runtime.executor

import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackContext
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator

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

        context.reportWeaponHit(
            enemy = enemy,
            appliedDamage =
                appliedDamage,
            damageType =
                damageType
        )
    }
}
