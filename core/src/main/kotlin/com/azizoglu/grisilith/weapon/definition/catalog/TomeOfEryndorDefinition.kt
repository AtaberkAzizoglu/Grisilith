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

object TomeOfEryndorDefinition {

    val definition =
        WeaponDefinition(
            id =
                ContentId(
                    "WPN_TOME_OF_ERYNDOR"
                ),

            displayName =
                "Tome of Eryndor",

            description =
                "A spell focus that alternates randomly between Necrotic and Fire attacks.",

            tags =
                setOf(
                    ContentTag.SPELL_FOCUS
                ),

            damageTypes =
                setOf(
                    DamageType.NECROTIC,
                    DamageType.FIRE
                ),

            randomDamageTypePerAttack =
                true,

            attackPattern =
                WeaponAttackPattern
                    .STRAIGHT_PROJECTILE,

            normalAttackSpeedStat =
                StatType.HASTE,

            baseDamage = 18,
            baseAttacksPerSecond = 1.65f,
            baseRange = 720f,
            baseProjectileSpeed = 700f,
            baseKnockback = 0f,

            baseProjectileCount = 1,
            baseMaximumTargets = 1,

            damageScaling =
                mapOf(
                    StatType.WISDOM to 0.70f,
                    StatType.ELEMENTAL_DAMAGE to 0.30f,
                    StatType.MIGHT_OF_NATURE to 0.25f
                ),

            passiveStatModifiers =
                emptyList(),

            levelStats =
                WeaponLevelProgressionFactory
                    .create(
                        level2Damage = 1.05f,
                        level3Damage = 1.20f,
                        level4Damage = 1.32f,
                        level5Damage = 1.32f,

                        /*
                         * Lv.3 ışını eski saldırıdan daha
                         * kısa menzilli olacağı için range
                         * artışı vermiyoruz.
                         */

                        level2Modifiers =
                            listOf(
                                WeaponModifierFactory
                                    .create(
                                        statType =
                                            StatType.LUCK,
                                        amount = 1
                                    )
                            ),

                        level4Modifiers =
                            listOf(
                                WeaponModifierFactory
                                    .create(
                                        statType =
                                            StatType.ELEMENTAL_DAMAGE,
                                        amount = 2
                                    )
                            )
                    ),

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )
}
