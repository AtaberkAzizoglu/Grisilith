package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyManager
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.weapon.definition.WeaponAttackPattern

class WeaponRuntime(
    val weapon: WeaponInstance,
    private val playerStats: PlayerStats,
    private val executorFactory:
    WeaponAttackExecutorFactory
) {

    private val attackTimer =
        WeaponAttackTimer()

    fun update(
        delta: Float,
        player: Player,
        enemyManager: EnemyManager,
        projectiles: MutableList<Projectile>,
        onDamageDealt: (
            damage: Int,
            weaponName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
    ) {
        val definition =
            weapon.definition

        if (
            definition.attackPattern ==
            WeaponAttackPattern.NONE
        ) {
            return
        }

        val executor =
            executorFactory.executorFor(
                definition.attackPattern
            ) ?: return

        attackTimer.update(
            delta = delta,
            attacksPerSecond =
                calculateAttacksPerSecond()
        ) {
            executor.execute(
                WeaponAttackContext(
                    weapon = weapon,
                    player = player,
                    enemyManager =
                        enemyManager,
                    projectiles =
                        projectiles,
                    onDamageDealt =
                        onDamageDealt,
                    onEnemyKilled =
                        onEnemyKilled
                )
            )
        }
    }

    private fun calculateAttacksPerSecond(): Float {
        val definition =
            weapon.definition

        val rarityStats =
            definition.statsFor(
                weapon.rarity
            )

        val normalAttackSpeedMultiplier =
            if (
                definition
                    .usesHasteForNormalAttack
            ) {
                playerStats
                    .hasteAttackSpeedMultiplier()
            } else {
                playerStats
                    .attackSpeedMultiplier()
            }

        return (
            definition.baseAttacksPerSecond *
                rarityStats
                    .attackSpeedMultiplier *
                normalAttackSpeedMultiplier
            )
            .coerceAtLeast(0.01f)
    }

    fun resetTimer() {
        attackTimer.reset()
    }
}
