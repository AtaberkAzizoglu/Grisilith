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

object ShieldDefinition {

    val definition =
        WeaponDefinition(
            id =
                ContentId(
                    "WPN_SHIELD"
                ),

            displayName =
                "Shield",

            description =
                "A defensive weapon that performs short cone attacks with high knockback.",

            tags =
                setOf(
                    ContentTag.MELEE_WEAPON,
                    ContentTag.DEFENSIVE
                ),

            damageTypes =
                setOf(
                    DamageType.BLUDGEONING
                ),

            attackPattern =
                WeaponAttackPattern
                    .FORWARD_CONE,

            baseDamage = 10,
            baseAttacksPerSecond = 0.85f,
            baseRange = 80f,
            baseProjectileSpeed = 0f,
            baseKnockback = 55f,

            areaAngleDegrees = 45f,
            areaRadius = 80f,

            damageScaling =
                mapOf(
                    StatType.ARMOR_CLASS to 0.80f
                ),

            passiveStatModifiers =
                emptyList(),

            levelStats =
                WeaponLevelProgressionFactory
                    .create(
                        level2Damage = 1f,
                        level3Damage = 1.18f,
                        level4Damage = 1.18f,
                        level5Damage = 1.18f,

                        level3Range = 1.20f,
                        level4Range = 1.20f,
                        level5Range = 1.20f,

                        level2Knockback = 1.10f,
                        level3Knockback = 1.20f,
                        level4Knockback = 1.20f,
                        level5Knockback = 1.20f,

                        level2Modifiers =
                            listOf(
                                WeaponModifierFactory
                                    .create(
                                        statType =
                                            StatType.ARMOR_CLASS,
                                        amount = 2
                                    ),

                                WeaponModifierFactory
                                    .create(
                                        statType =
                                            StatType.ATTACK_SPEED,
                                        amount = -1
                                    )
                            ),

                        level4Modifiers =
                            listOf(
                                WeaponModifierFactory
                                    .create(
                                        statType =
                                            StatType.ARMOR_CLASS,
                                        amount = 3
                                    )
                            )
                    ),

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )
}
