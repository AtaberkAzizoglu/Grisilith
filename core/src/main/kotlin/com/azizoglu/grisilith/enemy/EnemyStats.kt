package com.azizoglu.grisilith.enemy

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.combat.affinity.DamageAffinityProfile
import com.azizoglu.grisilith.enemy.combat.EnemyCombatTuning
import com.azizoglu.grisilith.enemy.definition.EnemyTraversalProfile

/*
 * Runtime sırasında Enemy tarafından kullanılan çözülmüş değerler.
 *
 * Yeni alanların varsayılan değerleri eski EnemyScaling ve mevcut
 * spawn akışının derlenmeye devam etmesini sağlar.
 */
data class EnemyStats(
    val type: EnemyType,
    val maximumHealth: Int,
    val contactDamage: Int,
    val movementSpeed: Float,
    val size: Float,
    val goldReward: Float,
    val armorClass: Int = 0,
    val baseArmor: Int = 0,

    val attacksPerSecond: Float = 0f,
    val attackRange: Float = 0f,

    val weaponDamageMultiplier: Float = 1f,
    val attackSpeedScore: Int = 0,
    val mainDamageStat: Int = 10,
    val secondaryDamageStat: Int = 10,
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
        DamageAffinityProfile.NONE,

    val kindId: ContentId? = null,
    val behaviorId: ContentId? = null,

    val traversalProfile: EnemyTraversalProfile =
        EnemyTraversalProfile.BLOCKED_BY_OBSTACLES
) {

    init {
        require(maximumHealth > 0)
        require(contactDamage >= 0)
        require(movementSpeed >= 0f)
        require(size > 0f)
        require(goldReward >= 0f)
        require(armorClass >= 0)
        require(baseArmor >= 0)
        require(attacksPerSecond >= 0f)
        require(attackRange >= 0f)
        require(weaponDamageMultiplier >= 0f)
        require(attackSpeedScore >= 0)
        require(mainDamageStat >= 0)
        require(secondaryDamageStat >= 0)
        require(critRate >= 0)
        require(critDamage >= 0)
        require(piercing >= 0)
        require(meleeWeaponRangeMultiplier >= 0f)
        require(rangedWeaponRangeMultiplier >= 0f)
        require(projectileSpeedMultiplier >= 0f)
    }

    val ignoresObstacles: Boolean
        get() =
            traversalProfile
                .ignoresObstacles

    val canSpawnInsideObstacles: Boolean
        get() =
            traversalProfile
                .canSpawnInsideObstacles
}
