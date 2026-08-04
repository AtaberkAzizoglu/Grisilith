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

object IvyStaffDefinition {

    val definition =
        WeaponDefinition(
            id =
                ContentId(
                    "WPN_IVY_STAFF"
                ),

            displayName =
                "Ivy Staff",

            description =
                "An acid spell focus that corrodes enemies and supports summoned allies.",

            tags =
                setOf(
                    ContentTag.SPELL_FOCUS,
                    ContentTag.SUMMON
                ),

            damageTypes =
                setOf(
                    DamageType.ACID
                ),

            attackPattern =
                WeaponAttackPattern
                    .STRAIGHT_PROJECTILE,

            normalAttackSpeedStat =
                StatType.HASTE,

            baseDamage = 18,
            baseAttacksPerSecond = 1.65f,
            baseRange = 720f,
            baseProjectileSpeed = 680f,
            baseKnockback = 0f,

            baseProjectileCount = 1,
            baseMaximumTargets = 1,

            damageScaling =
                mapOf(
                    StatType.WISDOM to 0.70f,
                    StatType.SHEPHERD to 0.45f
                ),

            passiveStatModifiers =
                emptyList(),

            levelStats =
                WeaponLevelProgressionFactory
                    .create(
                        level2Damage = 1.05f,
                        level3Damage = 1.22f,
                        level4Damage = 1.32f,
                        level5Damage = 1.32f,

                        level3Range = 1.05f,
                        level4Range = 1.05f,
                        level5Range = 1.05f,

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
                                            StatType.SHEPHERD,
                                        amount = 2
                                    )
                            )
                    ),

            weaponAbility =
                DefaultWeaponAbilities
                    .chooseToSummon,

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )
}
