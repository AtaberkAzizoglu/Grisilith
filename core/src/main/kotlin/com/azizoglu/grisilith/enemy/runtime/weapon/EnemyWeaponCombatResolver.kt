package com.azizoglu.grisilith.enemy.runtime.weapon

import com.azizoglu.grisilith.enemy.EnemyStats
import com.azizoglu.grisilith.weapon.definition.WeaponAttackPattern
import kotlin.math.roundToInt

data class EnemyWeaponCombatProfile(
    val damageMultiplier: Float, val attackSpeedMultiplier: Float,
    val rangeMultiplier: Float, val projectileSpeedMultiplier: Float,
    val criticalChance: Float, val criticalDamageMultiplier: Float,
    val piercingIgnoreRatio: Float
)

object EnemyWeaponCombatResolver {
    fun profile(stats: EnemyStats, attackPattern: WeaponAttackPattern): EnemyWeaponCombatProfile {
        val ranged = attackPattern in RANGED_PATTERNS
        return EnemyWeaponCombatProfile(
            damageMultiplier = stats.weaponDamageMultiplier * damageStatMultiplier(stats),
            attackSpeedMultiplier = 1f + stats.attackSpeedScore * ATTACK_SPEED_PER_SCORE,
            rangeMultiplier = if (ranged) stats.rangedWeaponRangeMultiplier else stats.meleeWeaponRangeMultiplier,
            projectileSpeedMultiplier = if (ranged) stats.projectileSpeedMultiplier else 1f,
            criticalChance = (stats.critRate * CRIT_RATE_PER_SCORE).coerceIn(0f, MAXIMUM_CRIT_CHANCE),
            criticalDamageMultiplier = BASE_CRITICAL_DAMAGE_MULTIPLIER + stats.critDamage * CRIT_DAMAGE_PER_SCORE,
            piercingIgnoreRatio = (stats.piercing * PIERCING_PER_SCORE).coerceIn(0f, MAXIMUM_PIERCING_RATIO)
        )
    }
    fun resolveDamage(baseWeaponDamage: Int, profile: EnemyWeaponCombatProfile, criticalHit: Boolean): Int {
        if (baseWeaponDamage <= 0) return 0
        val crit = if (criticalHit) profile.criticalDamageMultiplier else 1f
        return (baseWeaponDamage * profile.damageMultiplier * crit).roundToInt().coerceAtLeast(1)
    }
    private fun damageStatMultiplier(stats: EnemyStats): Float {
        val weighted = stats.mainDamageStat * 0.75f + stats.secondaryDamageStat * 0.25f
        return 1f + weighted * DAMAGE_PER_WEIGHTED_STAT
    }
    private val RANGED_PATTERNS = setOf(
        WeaponAttackPattern.TARGET_AREA_CIRCLE, WeaponAttackPattern.STRAIGHT_PROJECTILE,
        WeaponAttackPattern.PIERCING_PROJECTILE, WeaponAttackPattern.TOME_PROJECTILE_OR_BEAM,
        WeaponAttackPattern.TOTEM_PROJECTILE_OR_DRAIN
    )
    private const val DAMAGE_PER_WEIGHTED_STAT = 0.0125f
    private const val ATTACK_SPEED_PER_SCORE = 0.010f
    private const val CRIT_RATE_PER_SCORE = 0.005f
    private const val CRIT_DAMAGE_PER_SCORE = 0.010f
    private const val PIERCING_PER_SCORE = 0.010f
    private const val BASE_CRITICAL_DAMAGE_MULTIPLIER = 1.50f
    private const val MAXIMUM_CRIT_CHANCE = 0.50f
    private const val MAXIMUM_PIERCING_RATIO = 0.75f
}
