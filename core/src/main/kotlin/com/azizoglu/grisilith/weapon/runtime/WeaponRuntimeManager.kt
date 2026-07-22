package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyManager
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.weapon.Projectile

class WeaponRuntimeManager(
    private val inventory:
    WeaponInventory,
    private val playerStats:
    PlayerStats
) {

    private val runtimesByWeapon =
        mutableMapOf<
            WeaponInstance,
            WeaponRuntime
            >()

    private val executorFactory =
        WeaponAttackExecutorFactory(
            playerStats = playerStats
        )

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
        synchronizeWithInventory()

        runtimesByWeapon.values
            .forEach { runtime ->

                runtime.update(
                    delta = delta,
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
            }
    }

    private fun synchronizeWithInventory() {
        val equippedWeapons =
            inventory.allWeapons()

        /*
         * Envanterden çıkarılmış silahların
         * runtime'larını kaldırır.
         */
        runtimesByWeapon.keys
            .toList()
            .filter { weapon ->
                weapon !in equippedWeapons
            }
            .forEach { removedWeapon ->
                runtimesByWeapon.remove(
                    removedWeapon
                )
            }

        /*
         * Yeni takılan her silah için ayrı
         * runtime ve ayrı timer oluşturur.
         */
        equippedWeapons
            .forEach { weapon ->

                runtimesByWeapon
                    .getOrPut(weapon) {
                        WeaponRuntime(
                            weapon = weapon,
                            playerStats = playerStats,
                            executorFactory =
                                executorFactory
                        )
                    }
            }
    }

    fun resetAllTimers() {
        runtimesByWeapon.values
            .forEach { runtime ->
                runtime.resetTimer()
            }
    }

    val activeRuntimeCount: Int
        get() = runtimesByWeapon.size
}
