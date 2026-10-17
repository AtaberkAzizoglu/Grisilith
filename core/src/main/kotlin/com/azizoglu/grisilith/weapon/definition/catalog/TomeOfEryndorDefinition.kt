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

            upgradedBasicAttackDamageType =
                DamageType.FIRE,

            attackPattern =
                WeaponAttackPattern
                    .TOME_PROJECTILE_OR_BEAM,

            normalAttackSpeedStat =
                StatType.HASTE,

            baseDamage = 26,
            baseAttacksPerSecond = 0.239f,
            maximumAttacksPerSecond = 1.75f,
            baseRange = 500f,
            baseProjectileSpeed = 500f,
            baseKnockback = 0f,

            baseProjectileCount = 1,
            baseMaximumTargets = 1,

            damageScaling =
                mapOf(
                    StatType.WISDOM to 0.70f,
                    StatType.MIGHT_OF_NATURE to 0.50f,
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


                        /*
                         * Lv.4 ışını eski saldırıdan daha
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

            weaponAbility =
                DefaultWeaponAbilities.erasingSouls,

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )
}
