package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyManager
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.weapon.Projectile

data class WeaponAttackContext(
    val weapon: WeaponInstance,
    val player: Player,
    val enemyManager: EnemyManager,
    val projectiles: MutableList<Projectile>,
    val onDamageDealt: (
        damage: Int,
        weaponName: String
    ) -> Unit,
    val onEnemyKilled: (Enemy) -> Unit
)
