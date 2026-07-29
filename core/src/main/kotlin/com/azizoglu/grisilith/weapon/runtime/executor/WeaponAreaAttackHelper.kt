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

        val wasAliveBeforeAttack =
            !enemy.isDead

        val appliedDamage =
            enemy.takeDamage(
                damageResult.damage
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

        /*
         * Önce base Weapon ölümünü bildiriyoruz.
         *
         * Böylece Flame Blades bonus hasarı ayrıca
         * öldürürse aynı Enemy iki kez bildirilmez.
         */
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
