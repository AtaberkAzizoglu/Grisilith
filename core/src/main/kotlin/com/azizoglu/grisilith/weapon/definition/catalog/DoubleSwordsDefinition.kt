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

object DoubleSwordsDefinition {

    val definition =
        WeaponDefinition(
            id =
                ContentId(
                    "WPN_DOUBLE_SWORDS"
                ),

            displayName =
                "Double Swords",

            description =
                "Fast paired swords that repeatedly slash the nearest enemies.",

            tags =
                setOf(
                    ContentTag.MELEE_WEAPON
                ),

            damageTypes =
                setOf(
                    DamageType.SLASHING
                ),

            attackPattern =
                WeaponAttackPattern
                    .SHORT_SLASH_LINES,

            baseDamage = 7,
            baseAttacksPerSecond = 0.808f,
            baseRange = 175f,
            baseProjectileSpeed = 0f,
            baseKnockback = 1f,

            areaAngleDegrees = 18f,
            areaRadius = 48f,

            damageScaling =
                mapOf(
                    StatType.STRENGTH to 1f
                ),

            passiveStatModifiers =
                emptyList(),

            levelStats =
                WeaponLevelProgressionFactory
                    .create(
                        level2Damage = 1.10f,
                        level3Damage = 1.20f,
                        level4Damage = 1.35f,
                        level5Damage = 1.50f,

                        level2AttackSpeed = 1.15f,
                        level3AttackSpeed = 1.22f,
                        level4AttackSpeed = 1.22f,
                        level5AttackSpeed = 1.30f,



                        level3Range = 1.40f,
                        level4Range = 1.40f,
                        level5Range = 1.40f,

                        level2Modifiers =
                            listOf(
                                WeaponModifierFactory
                                    .create(
                                        statType =
                                            StatType.STRENGTH,
                                        amount = 1
                                    ),

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
                                            StatType.ATTACK_SPEED,
                                        amount = 2
                                    )
                            )
                    ),

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )
}
