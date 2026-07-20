package com.azizoglu.grisilith.weapon

import com.azizoglu.grisilith.enemy.EnemyManager
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.statistics.PlayerStats

class AutoWeapon(
    private val baseAttacksPerSecond: Float,
    val displayName: String,
    private val playerStats: PlayerStats
) {
    private var attackTimer = 0f

    init {
        require(baseAttacksPerSecond > 0f) {
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

        val currentAttacksPerSecond =
            baseAttacksPerSecond *
                playerStats.attackSpeedMultiplier()

        val attackInterval =
            1f / currentAttacksPerSecond

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
                    targetY = target.centerY,
                    sourceWeaponName = displayName
                )
            )
        }
    }
}
