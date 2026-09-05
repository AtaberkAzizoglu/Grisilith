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

object LanceDefinition {

    val definition =
        WeaponDefinition(
            id =
                ContentId(
                    "WPN_LANCE"
                ),

            displayName =
                "Lance",

            description =
                "A slow and powerful piercing weapon with exceptional melee reach.",

            tags =
                setOf(
                    ContentTag.MELEE_WEAPON,
                ),

            damageTypes =
                setOf(
                    DamageType.PIERCING
                ),

            /*
             * Geçici olarak mevcut çalışan executor.
             * Daha sonra uzun çizgi saldırısına
             * özel pattern ile değiştirilecek.
             */
            attackPattern =
                WeaponAttackPattern.LONG_PIERCING_LINE,

            baseDamage = 12,
            baseAttacksPerSecond = 0.363f,
            baseRange = 245f,
            baseProjectileSpeed = 0f,
            baseKnockback = 12f,

            areaRadius = 32f,

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



                        level3Range = 1.10f,
                        level4Range = 1.10f,
                        level5Range = 1.10f,

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
                    .impaleAndDrag,

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )
}
