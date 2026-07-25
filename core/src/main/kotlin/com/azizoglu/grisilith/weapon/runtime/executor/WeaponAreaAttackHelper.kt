package com.azizoglu.grisilith.weapon.runtime.executor

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

        /*
         * Her hedef için crit ayrı hesaplanır.
         */
        val damageResult =
            damageCalculator.calculate(
                weapon = context.weapon
            )

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

            /*
             * Alan saldırılarında her düşman,
             * oyuncudan kendisine doğru itilir.
             */
            enemy.applyKnockback(
                directionX =
                    enemy.centerX -
                        context.player.centerX,

                directionY =
                    enemy.centerY -
                        context.player.centerY,

                strength =
                    definition.baseKnockback *
                        levelStats.knockbackMultiplier
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
    }
}
