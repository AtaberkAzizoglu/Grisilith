package com.azizoglu.grisilith.enemy.definition

import com.azizoglu.grisilith.combat.affinity.DamageAffinityProfile
import com.azizoglu.grisilith.enemy.combat.EnemyCombatTuning

/*
 * Stage, Difficulty, Map ve EnemyType çarpanları uygulanmadan
 * önce bir Enemy Kind'ın taşıdığı temel değerler.
 */
data class EnemyBaseStats(
    val maximumHealth: Int,
    val armorClass: Int,
    val baseArmor: Int = 0,
    val contactDamage: Int,
    val movementSpeed: Float,
    val attacksPerSecond: Float,
    val attackRange: Float,

    /* Enemy Weapon / combat stat foundation. */
    val weaponDamageMultiplier: Float = 1f,
    val attackSpeedScore: Int = 0,
    val mainDamageStat: Int = 10,
    val secondaryDamageStat: Int = 10,
    val baseCritRatePercent: Float = 0f,
    val baseCritDamagePercent: Float = 100f,
    val critRate: Int = 0,
    val critDamage: Int = 0,
    val piercing: Int = 0,

    val meleeWeaponRangeMultiplier: Float =
        EnemyCombatTuning.DEFAULT_MELEE_WEAPON_RANGE_MULTIPLIER,

    val rangedWeaponRangeMultiplier: Float =
        EnemyCombatTuning.DEFAULT_RANGED_WEAPON_RANGE_MULTIPLIER,

    val projectileSpeedMultiplier: Float =
        EnemyCombatTuning.DEFAULT_PROJECTILE_SPEED_MULTIPLIER,

    val damageAffinities: DamageAffinityProfile =
        DamageAffinityProfile.NONE
) {

    init {
        require(maximumHealth > 0) {
            "Enemy maximum health must be greater than zero."
        }

        require(armorClass >= 0) {
            "Enemy armor class cannot be negative."
        }

        require(baseArmor >= 0) {
            "Enemy base armor cannot be negative."
        }

        require(contactDamage >= 0) {
            "Enemy contact damage cannot be negative."
        }

        require(movementSpeed >= 0f) {
            "Enemy movement speed cannot be negative."
        }

        require(attacksPerSecond >= 0f) {
            "Enemy attacks per second cannot be negative."
        }

        require(attackRange >= 0f) {
            "Enemy attack range cannot be negative."
        }

        require(weaponDamageMultiplier >= 0f) {
            "Enemy weapon damage multiplier cannot be negative."
        }

        require(attackSpeedScore >= 0)
        require(mainDamageStat >= 0)
        require(secondaryDamageStat >= 0)
        require(baseCritRatePercent >= 0f)
        require(baseCritDamagePercent >= 100f)
        require(critRate >= 0)
        require(critDamage >= 0)
        require(piercing >= 0)
        require(meleeWeaponRangeMultiplier >= 0f)
        require(rangedWeaponRangeMultiplier >= 0f)
        require(projectileSpeedMultiplier >= 0f)
    }
}
