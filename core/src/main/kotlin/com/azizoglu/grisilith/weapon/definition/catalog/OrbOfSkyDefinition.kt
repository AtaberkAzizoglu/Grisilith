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

object OrbOfSkyDefinition {

    val definition =
        WeaponDefinition(
            id =
                ContentId(
                    "WPN_ORB_OF_SKY"
                ),

            displayName =
                "Orb of Sky",

            description =
                "A spell focus that alternates between Air and Lightning attacks.",

            tags =
                setOf(
                    ContentTag.SPELL_FOCUS
                ),

            damageTypes =
                setOf(
                    DamageType.AIR,
                    DamageType.LIGHTNING
                ),

            randomDamageTypePerAttack =
                true,

            upgradedBasicAttackDamageType =
                DamageType.LIGHTNING,

            attackPattern =
                WeaponAttackPattern
                    .STRAIGHT_PROJECTILE,

            normalAttackSpeedStat =
                StatType.HASTE,

            baseDamage = 14,
            baseAttacksPerSecond = 0.447f,
            maximumAttacksPerSecond = 3.20f,
            baseRange = 580f,
            baseProjectileSpeed = 920f,
            baseKnockback = 0f,

            baseProjectileCount = 1,
            baseMaximumTargets = 1,

            damageScaling =
                mapOf(
                    StatType.WISDOM to 0.70f,
                    StatType.ELEMENTAL_DAMAGE to 0.50f,
                    StatType.MIGHT_OF_NATURE to 0.50f
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
                                WeaponModifierFactory
                                    .create(
                                        statType =
                                            StatType.MOVEMENT_SPEED,
                                        amount = 1
                                    )
                            ),

                        level4Modifiers =
                            listOf(
                                WeaponModifierFactory
                                    .create(
                                        statType =
                                            StatType.MIGHT_OF_NATURE,
                                        amount = 2
                                    )
                            )
                    ),

            weaponAbility =
                DefaultWeaponAbilities
                    .vortexPull,

            upgradedWeaponAbility =
                DefaultWeaponAbilities
                    .vortexPullUpgraded,

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )
}
