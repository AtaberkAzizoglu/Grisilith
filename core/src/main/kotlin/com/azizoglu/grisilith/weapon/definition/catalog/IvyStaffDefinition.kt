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
                    DamageType.ACID,
                    DamageType.POISON
                ),

            randomDamageTypePerAttack =
                true,

            upgradedBasicAttackDamageType =
                DamageType.ACID,

            attackPattern =
                WeaponAttackPattern
                    .STRAIGHT_PROJECTILE,

            normalAttackSpeedStat =
                StatType.HASTE,

            baseDamage = 10,
            baseAttacksPerSecond = 0.90f,
            maximumAttacksPerSecond = 2.25f,
            baseRange = 560f,
            baseProjectileSpeed = 620f,
            baseKnockback = 0f,

            baseProjectileCount = 1,
            baseMaximumTargets = 1,

            damageScaling =
                mapOf(
                    StatType.WISDOM to 0.70f,
                    StatType.MIGHT_OF_NATURE to 0.50f,
                    StatType.SHEPHERD to 0.50f
                ),

            passiveStatModifiers =
                emptyList(),

            levelStats =
                WeaponLevelProgressionFactory
                    .create(
                        level2Damage = 1.55f,
                        level3Damage = 2.65f,
                        level4Damage = 3.45f,
                        level5Damage = 4.85f,

                        level2AttackSpeed = 1.15f,
                        level3AttackSpeed = 1.22f,
                        level4AttackSpeed = 1.22f,
                        level5AttackSpeed = 1.30f,


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
