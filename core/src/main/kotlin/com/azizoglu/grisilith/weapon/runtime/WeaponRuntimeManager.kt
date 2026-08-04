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
import com.azizoglu.grisilith.weapon.skill.DefaultWeaponAbilities
import com.azizoglu.grisilith.weapon.skill.VortexPullRuntime
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import kotlin.math.roundToInt

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

    private val activeSkillDamageCalculator =
        WeaponDamageCalculator(
            playerStats
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

    private val vortexPullRuntime =
        VortexPullRuntime()

    private var vortexWeaponRuntime:
        WeaponRuntime? =
        null

    private var vortexWeapon:
        WeaponInstance? =
        null

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

        vortexPullRuntime.update(
            delta =
                delta,

            enemies =
                enemyManager
                    .allEnemies,

            currentStage =
                currentStageProvider(),

            onPullEnemy = {
                    enemy,
                    centerX,
                    centerY,
                    pullMultiplier,
                    frameDelta ->

                enemy.pullToward(
                    targetCenterX =
                        centerX,

                    targetCenterY =
                        centerY,

                    pullSpeedPixelsPerSecond =
                        VORTEX_BASE_PULL_SPEED *
                            pullMultiplier,

                    delta =
                        frameDelta,

                    worldWidth =
                        worldWidth,

                    worldHeight =
                        worldHeight
                )
            },

            onDamageTick = {
                    enemy,
                    tickIntervalSeconds ->

                applyVortexDamageTick(
                    enemy =
                        enemy,

                    tickIntervalSeconds =
                        tickIntervalSeconds,

                    onDamageDealt =
                        onDamageDealt,

                    onEnemyKilled =
                        onEnemyKilled
                )
            },

            onCompleted = {
                vortexWeaponRuntime
                    ?.endActiveAbilityEarly()

                vortexWeaponRuntime =
                    null

                vortexWeapon =
                    null
            }
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
        slotIndex: Int,
        player: Player,
        enemyManager: EnemyManager,
        worldWidth: Float,
        worldHeight: Float
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

        val ability =
            weapon.unlockedAbility
                ?: return false

        return when (
            ability.id
        ) {
            DefaultWeaponAbilities
                .IGNITE_DASH_ID -> {

                igniteDashRuntime
                    .handleActivation(
                        weapon =
                            weapon,

                        weaponRuntime =
                            runtime
                    )
            }

            DefaultWeaponAbilities
                .VORTEX_PULL_ID -> {

                tryActivateVortexPull(
                    weaponRuntime =
                        runtime,

                    player =
                        player,

                    enemyManager =
                        enemyManager,

                    worldWidth =
                        worldWidth,

                    worldHeight =
                        worldHeight
                )
            }

            else -> {
                runtime
                    .tryActivateAbility()
            }
        }
    }

    private fun tryActivateVortexPull(
        weaponRuntime: WeaponRuntime,
        player: Player,
        enemyManager: EnemyManager,
        worldWidth: Float,
        worldHeight: Float
    ): Boolean {
        if (
            weaponRuntime.abilityState !=
            WeaponAbilityState.READY ||
            vortexPullRuntime.isActive
        ) {
            return false
        }

        /*
         * Önce uygun alan ve hedef kontrol edilir.
         * Başarısızsa WeaponAbilityRuntime ACTIVE yapılmadığı için
         * cooldown başlamaz.
         */
        val areaStarted =
            vortexPullRuntime
                .tryBegin(
                    playerCenterX =
                        player.centerX,

                    playerCenterY =
                        player.centerY,

                    enemies =
                        enemyManager
                            .allEnemies,

                    worldWidth =
                        worldWidth,

                    worldHeight =
                        worldHeight
                )

        if (!areaStarted) {
            return false
        }

        val abilityActivated =
            weaponRuntime
                .tryActivateAbility()

        if (!abilityActivated) {
            vortexPullRuntime.clear()
            return false
        }

        vortexWeaponRuntime =
            weaponRuntime

        vortexWeapon =
            weaponRuntime.weapon

        return true
    }

    private fun applyVortexDamageTick(
        enemy: Enemy,
        tickIntervalSeconds: Float,
        onDamageDealt: (
            damage: Int,
            weaponName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
    ) {
        val weapon =
            vortexWeapon
                ?: return

        if (
            enemy.isDead ||
            tickIntervalSeconds <=
            0f
        ) {
            return
        }

        val calculatedDamage =
            activeSkillDamageCalculator
                .calculate(
                    weapon
                )
                .damage

        /*
         * Sabit tick aralığı kullandığımız için multiplier
         * DPS olarak değerlendirilir.
         *
         * Örnek:
         * normal damage 20,
         * DPS multiplier 0.85,
         * tick 0.25 sn
         * -> tick başına yaklaşık 4 damage.
         */
        val rawTickDamage =
            (
                calculatedDamage *
                    DefaultWeaponAbilities
                        .VORTEX_PULL_DAMAGE_PER_SECOND_MULTIPLIER *
                    tickIntervalSeconds
                )
                .roundToInt()
                .coerceAtLeast(
                    1
                )

        val modifiedDamage =
            tamgaRuntimeManager
                .modifyWeaponDamage(
                    enemy =
                        enemy,

                    baseDamage =
                        rawTickDamage
                )
                .coerceAtLeast(
                    1
                )

        val wasAlive =
            !enemy.isDead

        val appliedDamage =
            enemy.takeDamage(
                modifiedDamage
            )

        if (
            appliedDamage >
            0
        ) {
            onDamageDealt(
                appliedDamage,
                weapon.displayName
            )
        }

        if (
            wasAlive &&
            enemy.isDead
        ) {
            onEnemyKilled(
                enemy
            )
        }
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
        vortexPullRuntime.clear()
        vortexWeaponRuntime = null
        vortexWeapon = null

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

        vortexPullRuntime.render(
            shapeRenderer =
                shapeRenderer
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
        vortexPullRuntime.clear()
        vortexWeaponRuntime = null
        vortexWeapon = null

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

    companion object {
        /*
         * Normal enemy için merkez yönündeki temel hız.
         * Enemy type/stage dirençleri bu değeri çarpar.
         */
        private const val VORTEX_BASE_PULL_SPEED =
            235f
    }

}
