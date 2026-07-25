package com.azizoglu.grisilith.weapon.definition

import com.azizoglu.grisilith.content.StatModifier

object WeaponLevelProgressionFactory {

    fun create(
        level2Damage: Float = 1f,
        level3Damage: Float = level2Damage,
        level4Damage: Float = level3Damage,
        level5Damage: Float = level4Damage,

        level2AttackSpeed: Float = 1f,
        level3AttackSpeed: Float =
            level2AttackSpeed,
        level4AttackSpeed: Float =
            level3AttackSpeed,
        level5AttackSpeed: Float =
            level4AttackSpeed,

        level2Range: Float = 1f,
        level3Range: Float = level2Range,
        level4Range: Float = level3Range,
        level5Range: Float = level4Range,

        level2Knockback: Float = 1f,
        level3Knockback: Float =
            level2Knockback,
        level4Knockback: Float =
            level3Knockback,
        level5Knockback: Float =
            level4Knockback,

        level2AdditionalTargets: Int = 0,
        level3AdditionalTargets: Int =
            level2AdditionalTargets,
        level4AdditionalTargets: Int =
            level3AdditionalTargets,
        level5AdditionalTargets: Int =
            level4AdditionalTargets,

        level2Modifiers:
        List<StatModifier> =
            emptyList(),

        level4Modifiers:
        List<StatModifier> =
            emptyList()
    ): Map<Int, WeaponLevelStats> {

        return mapOf(
            1 to
                WeaponLevelStats(
                    level = 1
                ),

            2 to
                WeaponLevelStats(
                    level = 2,
                    damageMultiplier =
                        level2Damage,
                    attackSpeedMultiplier =
                        level2AttackSpeed,
                    rangeMultiplier =
                        level2Range,
                    knockbackMultiplier =
                        level2Knockback,
                    additionalTargets =
                        level2AdditionalTargets,
                    grantedStatModifiers =
                        level2Modifiers
                ),

            3 to
                WeaponLevelStats(
                    level = 3,
                    damageMultiplier =
                        level3Damage,
                    attackSpeedMultiplier =
                        level3AttackSpeed,
                    rangeMultiplier =
                        level3Range,
                    knockbackMultiplier =
                        level3Knockback,
                    additionalTargets =
                        level3AdditionalTargets,
                    grantedStatModifiers =
                        level2Modifiers,
                    basicAttackUpgraded =
                        true
                ),

            4 to
                WeaponLevelStats(
                    level = 4,
                    damageMultiplier =
                        level4Damage,
                    attackSpeedMultiplier =
                        level4AttackSpeed,
                    rangeMultiplier =
                        level4Range,
                    knockbackMultiplier =
                        level4Knockback,
                    additionalTargets =
                        level4AdditionalTargets,
                    grantedStatModifiers =
                        level2Modifiers +
                            level4Modifiers,
                    basicAttackUpgraded =
                        true
                ),

            5 to
                WeaponLevelStats(
                    level = 5,
                    damageMultiplier =
                        level5Damage,
                    attackSpeedMultiplier =
                        level5AttackSpeed,
                    rangeMultiplier =
                        level5Range,
                    knockbackMultiplier =
                        level5Knockback,
                    additionalTargets =
                        level5AdditionalTargets,
                    grantedStatModifiers =
                        level2Modifiers +
                            level4Modifiers,
                    basicAttackUpgraded =
                        true,
                    weaponAbilityUnlocked =
                        true
                )
        )
    }
}
