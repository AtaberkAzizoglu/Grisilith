package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyManager
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.weapon.Projectile

data class WeaponAttackContext(
    val weapon:
    WeaponInstance,

    val weaponSlotIndex:
    Int,

    val player:
    Player,

    val enemyManager:
    EnemyManager,

    val projectiles:
    MutableList<Projectile>,

    val onDamageDealt: (
        damage: Int,
        weaponName: String
    ) -> Unit,

    val onEnemyKilled:
        (Enemy) -> Unit,

    val onWeaponHit:
        (WeaponHitEvent) -> Unit
) {

    fun reportWeaponHit(
        enemy: Enemy,
        appliedDamage: Int,
        damageType: DamageType,
        isProjectile: Boolean = false
    ) {
        if (appliedDamage <= 0) {
            return
        }

        onWeaponHit(
            WeaponHitEvent(
                weapon = weapon,
                weaponSlotIndex =
                    weaponSlotIndex,
                enemy = enemy,
                appliedDamage =
                    appliedDamage,
                damageType =
                    damageType,
                isProjectile =
                    isProjectile
            )
        )
    }
}
