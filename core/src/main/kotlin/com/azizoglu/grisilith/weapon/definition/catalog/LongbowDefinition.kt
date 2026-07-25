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

object LongbowDefinition {

    val definition =
        WeaponDefinition(
            id =
                ContentId(
                    "WPN_LONGBOW"
                ),

            displayName =
                "Longbow",

            description =
                "A balanced ranged weapon with reliable reach and accuracy.",

            tags =
                setOf(
                    ContentTag.RANGED_WEAPON
                ),

            damageTypes =
                setOf(
                    DamageType.PIERCING
                ),

            attackPattern =
                WeaponAttackPattern
                    .STRAIGHT_PROJECTILE,

            baseDamage = 22,
            baseAttacksPerSecond = 1.75f,
            baseRange = 960f,
            baseProjectileSpeed = 920f,
            baseKnockback = 0f,

            baseProjectileCount = 1,
            baseMaximumTargets = 1,

            damageScaling =
                mapOf(
                    StatType.DEXTERITY to 1f
                ),

            passiveStatModifiers =
                emptyList(),

            levelStats =
                WeaponLevelProgressionFactory
                    .create(
                        level2Damage = 1.08f,
                        level3Damage = 1.15f,
                        level4Damage = 1.25f,
                        level5Damage = 1.25f,

                        /*
                         * Lv.3 saldırısı hedefe
                         * çarptıktan sonra bir düşmana
                         * seker. additionalTargets bunu
                         * şimdilik mekanik işareti olarak
                         * tutuyor.
                         */
                        level3AdditionalTargets = 1,
                        level4AdditionalTargets = 1,
                        level5AdditionalTargets = 1,

                        level2Modifiers =
                            listOf(
                                WeaponModifierFactory
                                    .create(
                                        statType =
                                            StatType.CRIT_RATE,
                                        amount = 1
                                    )
                            ),

                        level4Modifiers =
                            listOf(
                                WeaponModifierFactory
                                    .create(
                                        statType =
                                            StatType.CRIT_DAMAGE,
                                        amount = 2
                                    )
                            )
                    ),

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )
}
