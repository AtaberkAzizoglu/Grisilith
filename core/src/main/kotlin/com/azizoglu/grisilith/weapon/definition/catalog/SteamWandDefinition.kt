package com.azizoglu.grisilith.weapon.definition.catalog

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.content.ContentUnlockState
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.weapon.definition.WeaponAttackPattern
import com.azizoglu.grisilith.weapon.definition.WeaponDefinition
import com.azizoglu.grisilith.weapon.definition.WeaponLevelProgressionFactory
import com.azizoglu.grisilith.weapon.definition.WeaponModifierFactory
import com.azizoglu.grisilith.weapon.skill.DefaultWeaponAbilities

object SteamWandDefinition {

    val definition =
        WeaponDefinition(
            id =
                ContentId(
                    "WPN_STEAM_WAND"
                ),

            displayName =
                "Steam Wand",

            description =
                "A spell focus that alternates randomly between Fire and Water attacks.",

            tags =
                setOf(
                    ContentTag.SPELL_FOCUS
                ),

            damageTypes =
                setOf(
                    DamageType.FIRE,
                    DamageType.WATER
                ),

            randomDamageTypePerAttack =
                true,

            upgradedBasicAttackDamageType =
                DamageType.WATER,

            attackPattern =
                WeaponAttackPattern
                    .STRAIGHT_PROJECTILE,

            normalAttackSpeedStat =
                StatType.HASTE,

            baseDamage = 18,
            baseAttacksPerSecond = 0.347f,
            maximumAttacksPerSecond = 2.50f,
            baseRange = 550f,
            baseProjectileSpeed = 660f,
            baseKnockback = 0f,

            baseProjectileCount = 1,
            baseMaximumTargets = 1,

            damageScaling =
                mapOf(
                    StatType.WISDOM to 0.70f,
                    StatType.ELEMENTAL_DAMAGE to 0.50f
                ),

            passiveStatModifiers =
                emptyList(),

            levelStats =
                WeaponLevelProgressionFactory
                    .create(

                        level2AttackSpeed = 1.15f,
                        level3AttackSpeed = 1.22f,
                        level4AttackSpeed = 1.22f,
                        level5AttackSpeed = 1.30f,


                        level2Modifiers =
                            listOf(
                                WeaponModifierFactory.create(
                                    statType = StatType.HASTE,
                                    amount = 1
                                )
                            ),

                        level4Modifiers =
                            listOf(
                                WeaponModifierFactory.create(
                                    statType = StatType.ELEMENTAL_DAMAGE,
                                    amount = 2
                                )
                            )
                    ),

            weaponAbility =
                DefaultWeaponAbilities
                    .igniteDash,

            upgradedWeaponAbility =
                DefaultWeaponAbilities
                    .igniteDashUpgraded,

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )
}
