package com.azizoglu.grisilith.weapon

import com.azizoglu.grisilith.enemy.EnemyManager
import com.azizoglu.grisilith.player.Player

class AutoWeapon(
    attacksPerSecond: Float
) {
    private var attacksPerSecond =
        attacksPerSecond

    private var attackTimer = 0f

    init {
        require(attacksPerSecond > 0f) {
            "Attacks per second must be greater than zero."
        }
    }

    fun update(
        delta: Float,
        player: Player,
        enemyManager: EnemyManager,
        projectiles: MutableList<Projectile>
    ) {
        attackTimer += delta

        val attackInterval =
            1f / attacksPerSecond

        while (attackTimer >= attackInterval) {
            attackTimer -= attackInterval

            val target =
                enemyManager.nearestEnemy(
                    x = player.centerX,
                    y = player.centerY
                ) ?: continue

            projectiles.add(
                Projectile(
                    startX = player.centerX,
                    startY = player.centerY,
                    targetX = target.centerX,
                    targetY = target.centerY
                )
            )
        }
    }

    fun increaseAttackSpeed(
        percentageIncrease: Float
    ) {
        if (percentageIncrease <= 0f) {
            return
        }

        attacksPerSecond *=
            1f + percentageIncrease
    }
}
