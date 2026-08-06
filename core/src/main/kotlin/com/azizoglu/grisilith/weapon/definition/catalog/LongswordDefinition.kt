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

object LongswordDefinition {

    val definition =
        WeaponDefinition(
            id =
                ContentId(
                    "WPN_LONGSWORD"
                ),

            displayName =
                "Longsword",

            description =
                "A balanced melee weapon with a reliable forward cone attack.",

            tags =
                setOf(
                    ContentTag.MELEE_WEAPON,
                ),

            damageTypes =
                setOf(
                    DamageType.SLASHING
                ),

            attackPattern =
                WeaponAttackPattern
                    .FORWARD_CONE,

            baseDamage = 4,
            baseAttacksPerSecond = 1.4f,
            baseRange = 165f,
            baseProjectileSpeed = 0f,
            baseKnockback = 10f,

            areaAngleDegrees = 20f,
            areaRadius = 165f,

            damageScaling =
                mapOf(
                    StatType.STRENGTH to 1f
                ),

            passiveStatModifiers =
                emptyList(),

            levelStats =
                WeaponLevelProgressionFactory
                    .create(
                        level2Damage = 1.13f,
                        level3Damage = 1.26f,
                        level4Damage = 1.39f,
                        level5Damage = 1.52f,

                        level2AttackSpeed = 1.13f,
                        level3AttackSpeed = 1.26f,
                        level4AttackSpeed = 1.39f,
                        level5AttackSpeed = 1.52f,


                        level3Range = 1.30f,
                        level4Range = 1.30f,
                        level5Range = 1.30f,

                        level2Modifiers =
                            listOf(
                                WeaponModifierFactory
                                    .create(
                                        statType =
                                            StatType.HARVEST,
                                        amount = 1
                                    )
                            ),

                        level4Modifiers =
                            listOf(
                                WeaponModifierFactory
                                    .create(
                                        statType =
                                            StatType.CRIT_RATE,
                                        amount = 1
                                    )
                            )
                    ),

            weaponAbility =
                DefaultWeaponAbilities
                    .executioner,

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )
}
