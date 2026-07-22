package com.azizoglu.grisilith.weapon.runtime.executor

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackContext
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator

object WeaponAreaAttackHelper {

    fun damageEnemy(
        enemy: Enemy,
        context: WeaponAttackContext,
        damageCalculator: WeaponDamageCalculator
    ) {
        if (enemy.isDead) {
            return
        }

        /*
         * Her enemy isabeti için hasar ve crit
         * tamamen bağımsız hesaplanır.
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
        }

        if (
            wasAliveBeforeAttack &&
            enemy.isDead
        ) {
            context.onEnemyKilled(enemy)
        }
    }
}
