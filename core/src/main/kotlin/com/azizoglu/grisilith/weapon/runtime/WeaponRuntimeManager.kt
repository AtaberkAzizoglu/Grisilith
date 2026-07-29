package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyManager
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.tamga.runtime.TamgaRuntimeManager
import com.azizoglu.grisilith.weapon.Projectile

class WeaponRuntimeManager(
    private val inventory:
    WeaponInventory,

    private val playerStats:
    PlayerStats,

    private val tamgaRuntimeManager:
    TamgaRuntimeManager
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

    private val statBonusSynchronizer =
        WeaponStatBonusSynchronizer(
            inventory = inventory,
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

        statBonusSynchronizer
            .synchronize()

        /*
         * Runtime'ları map sırasıyla değil gerçek
         * inventory slot sırasıyla güncelliyoruz.
         *
         * Quick Shot açısından slot 0, oyuncuya
         * gösterilen 1. Weapon slotudur.
         */
        inventory.weaponSlots
            .forEachIndexed {
                    slotIndex,
                    slot ->

                val weapon =
                    slot.weapon
                        ?: return@forEachIndexed

                val runtime =
                    runtimesByWeapon[
                        weapon
                    ]
                        ?: return@forEachIndexed

                runtime.update(
                    delta = delta,
                    slotIndex = slotIndex,
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

        equippedWeapons
            .forEach { weapon ->

                runtimesByWeapon
                    .getOrPut(
                        weapon
                    ) {
                        WeaponRuntime(
                            weapon = weapon,
                            playerStats =
                                playerStats,
                            executorFactory =
                                executorFactory,
                            tamgaRuntimeManager =
                                tamgaRuntimeManager
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

    fun refreshWeaponStatBonuses() {
        statBonusSynchronizer
            .invalidate()

        statBonusSynchronizer
            .synchronize()
    }

    val activeRuntimeCount: Int
        get() =
            runtimesByWeapon.size
}
