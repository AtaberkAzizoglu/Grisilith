package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyManager
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.weapon.definition.WeaponAttackPattern
import com.azizoglu.grisilith.statistics.StatType

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

        val levelStats =
            weapon.currentLevelStats

        val characterAttackSpeedMultiplier =
            when (
                definition.normalAttackSpeedStat
            ) {
                StatType.HASTE -> {
                    playerStats
                        .hasteAttackSpeedMultiplier()
                }

                StatType.ATTACK_SPEED -> {
                    playerStats
                        .attackSpeedMultiplier()
                }

                else -> {
                    /*
                     * Normal saldırı hızında kullanılmasına
                     * izin vermediğimiz statlar için güvenli
                     * varsayılan değer.
                     */
                    1f
                }
            }

        return (
            definition.baseAttacksPerSecond *
                levelStats.attackSpeedMultiplier *
                characterAttackSpeedMultiplier
            )
            .coerceAtLeast(0.01f)
    }

    fun resetTimer() {
        attackTimer.reset()
    }
}
