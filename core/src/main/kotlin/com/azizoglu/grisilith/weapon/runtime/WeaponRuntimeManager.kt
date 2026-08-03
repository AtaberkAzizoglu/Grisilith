package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyManager
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.tamga.runtime.TamgaRuntimeManager
import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.weapon.skill.WeaponAbilityState
import com.azizoglu.grisilith.weapon.skill.DefensiveLineRuntime
import com.azizoglu.grisilith.map.MapObstacle
import com.azizoglu.grisilith.weapon.skill.IgniteDashRuntime
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

class WeaponRuntimeManager(
    private val inventory:
    WeaponInventory,

    private val playerStats:
    PlayerStats,

    private val tamgaRuntimeManager:
    TamgaRuntimeManager,

    private val currentStageProvider:
        () -> Int = { 1 }
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

    private val defensiveLineRuntime =
        DefensiveLineRuntime(
            playerStats = playerStats,
            currentStageProvider =
                currentStageProvider
        )

    private val igniteDashRuntime =
        IgniteDashRuntime(
            playerStats =
                playerStats
        )

    fun update(
        delta: Float,
        player: Player,
        enemyManager: EnemyManager,
        projectiles: MutableList<Projectile>,
        mapObstacles: List<MapObstacle>,
        worldWidth: Float,
        worldHeight: Float,
        onDamageDealt: (
            damage: Int,
            weaponName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
    ) {
        synchronizeWithInventory()

        statBonusSynchronizer
            .synchronize()

        defensiveLineRuntime.update(
            delta = delta,
            player = player,
            equippedWeapons =
                inventory.allWeapons()
        )

        igniteDashRuntime.update(
            delta =
                delta,

            player =
                player,

            enemyManager =
                enemyManager,

            obstacleBounds =
                mapObstacles.map {
                    it.bounds
                },

            worldWidth =
                worldWidth,

            worldHeight =
                worldHeight,

            onDamageDealt =
                onDamageDealt,

            onEnemyKilled =
                onEnemyKilled
        )

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

    fun tryActivateAbilityInSlot(
        slotIndex: Int
    ): Boolean {

        synchronizeWithInventory()

        val weapon =
            inventory.weaponAt(
                slotIndex
            )
                ?: return false

        val runtime =
            runtimesByWeapon[
                weapon
            ]
                ?: return false

        return igniteDashRuntime
            .handleActivation(
                weapon =
                    weapon,

                weaponRuntime =
                    runtime
            )
    }

    fun updateActiveSkillAim(
        worldX: Float,
        worldY: Float
    ) {
        igniteDashRuntime
            .updateAimPosition(
                worldX =
                    worldX,

                worldY =
                    worldY
            )
    }

    fun cancelActiveAbilities() {
        igniteDashRuntime.cancel()

        runtimesByWeapon.values
            .forEach {
                    runtime ->

                if (
                    runtime.abilityState ==
                    WeaponAbilityState.ACTIVE
                ) {
                    runtime
                        .endActiveAbilityEarly()
                }
            }
    }

    fun renderActiveSkillEffects(
        shapeRenderer: ShapeRenderer,
        player: Player
    ) {
        igniteDashRuntime.render(
            shapeRenderer =
                shapeRenderer,

            player =
                player
        )
    }

    fun abilityStateInSlot(
        slotIndex: Int
    ): WeaponAbilityState {

        val weapon =
            inventory.weaponAt(
                slotIndex
            )
                ?: return WeaponAbilityState.LOCKED

        return runtimesByWeapon[
            weapon
        ]
            ?.abilityState
            ?: WeaponAbilityState.LOCKED
    }

    fun abilityRemainingActiveSecondsInSlot(
        slotIndex: Int
    ): Float {

        val weapon =
            inventory.weaponAt(
                slotIndex
            )
                ?: return 0f

        return runtimesByWeapon[
            weapon
        ]
            ?.abilityRemainingActiveSeconds
            ?: 0f
    }

    fun abilityRemainingCooldownSecondsInSlot(
        slotIndex: Int
    ): Float {

        val weapon =
            inventory.weaponAt(
                slotIndex
            )
                ?: return 0f

        return runtimesByWeapon[
            weapon
        ]
            ?.abilityRemainingCooldownSeconds
            ?: 0f
    }

    fun prepareForStageTransition() {
        /*
         * Açık hedefleme veya çalışan Active Skill
         * stage sınırını geçmez.
         *
         * endActiveAbilityEarly() skill'i cooldown'a
         * geçirir; cooldown değeri korunur.
         */
        cancelActiveAbilities()

        /*
         * Normal saldırı zamanlayıcıları yeni stage'de
         * temiz başlar fakat ability cooldown'ları
         * korunur.
         */
        runtimesByWeapon.values
            .forEach { runtime ->
                runtime.resetAttackTimer()
            }
    }

    fun resetAllTimers() {
        defensiveLineRuntime.reset()
        igniteDashRuntime.reset()

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
