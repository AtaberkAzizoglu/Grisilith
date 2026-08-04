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

object TotemOfDarkCloudsDefinition {

    val definition =
        WeaponDefinition(
            id =
                ContentId(
                    "WPN_TOTEM_OF_DARK_CLOUDS"
                ),

            displayName =
                "Totem of Dark Clouds",

            description =
                "A dark spell focus that alternates between Necrotic and Lightning attacks.",

            tags =
                setOf(
                    ContentTag.SPELL_FOCUS
                ),

            damageTypes =
                setOf(
                    DamageType.NECROTIC,
                    DamageType.LIGHTNING
                ),

            randomDamageTypePerAttack =
                true,

            attackPattern =
                WeaponAttackPattern
                    .TOTEM_PROJECTILE_OR_DRAIN,

            normalAttackSpeedStat =
                StatType.HASTE,

            baseDamage = 17,
            baseAttacksPerSecond = 1.50f,
            baseRange = 680f,
            baseProjectileSpeed = 710f,
            baseKnockback = 0f,

            baseProjectileCount = 1,
            baseMaximumTargets = 1,

            damageScaling =
                mapOf(
                    StatType.WISDOM to 0.70f,
                    StatType.MIGHT_OF_NATURE to 0.50f
                ),

            passiveStatModifiers =
                emptyList(),

            levelStats =
                WeaponLevelProgressionFactory
                    .create(
                        level2Damage = 1.08f,
                        level3Damage = 1.24f,
                        level4Damage = 1.38f,
                        level5Damage = 1.38f,

                        level2Modifiers =
                            listOf(
                                WeaponModifierFactory
                                    .create(
                                        statType =
                                            StatType.LIFE_STEAL,
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
                    .staticClouds,

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )
}
