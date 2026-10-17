package com.azizoglu.grisilith.weapon.definition

import com.azizoglu.grisilith.content.StatModifier

object WeaponLevelProgressionFactory {

    // Weapon level upgrades add speed, stats and mechanics, never a blanket damage multiplier.
    fun create(

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
                        1f,
                    attackSpeedMultiplier =
                        1.15f,
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
                        1f,
                    attackSpeedMultiplier =
                        1.22f,
                    rangeMultiplier =
                        level3Range,
                    knockbackMultiplier =
                        level3Knockback,
                    additionalTargets =
                        level3AdditionalTargets,
                    grantedStatModifiers =
                        level2Modifiers,
                    weaponAbilityUnlocked =
                        true
                ),

            4 to
                WeaponLevelStats(
                    level = 4,
                    damageMultiplier =
                        1f,
                    attackSpeedMultiplier =
                        1.22f,
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
                        true,
                    weaponAbilityUnlocked =
                        true
                ),

            5 to
                WeaponLevelStats(
                    level = 5,
                    damageMultiplier =
                        1f,
                    attackSpeedMultiplier =
                        1.30f,
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
                        true,
                    weaponAbilityUpgraded =
                        true
                )
        )
    }
}
