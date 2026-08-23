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

object CrossbowDefinition {

    val definition =
        WeaponDefinition(
            id =
                ContentId(
                    "WPN_CROSSBOW"
                ),

            displayName =
                "Crossbow",

            description =
                "A slow and powerful ranged weapon whose bolts continue through a second target.",

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
                    .PIERCING_PROJECTILE,

            baseDamage = 14,
            baseAttacksPerSecond = 0.323f,
            baseRange = 880f,
            baseProjectileSpeed = 850f,
            baseKnockback = 13f,

            damageScaling =
                mapOf(
                    StatType.DEXTERITY to 1f,
                    StatType.PIERCING to 0.25f
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



                        /*
                         * Lv.3 sonrasındaki delme
                         * mesafesi artışının geçici
                         * sayısal karşılığı.
                         */
                        level3Range = 1.15f,
                        level4Range = 1.15f,
                        level5Range = 1.15f,

                        level2Modifiers =
                            listOf(
                                WeaponModifierFactory
                                    .create(
                                        statType =
                                            StatType.CRIT_DAMAGE,
                                        amount = 1
                                    )
                            ),

                        level4Modifiers =
                            listOf(
                                WeaponModifierFactory
                                    .create(
                                        statType =
                                            StatType.PIERCING,
                                        amount = 2
                                    )
                            )
                    ),

            weaponAbility =
                DefaultWeaponAbilities
                    .heavyBolts,

            initialUnlockState =
                ContentUnlockState.LOCKED
        )
}
