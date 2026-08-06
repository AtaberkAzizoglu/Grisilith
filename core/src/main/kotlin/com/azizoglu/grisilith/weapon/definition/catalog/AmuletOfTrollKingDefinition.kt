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

object AmuletOfTrollKingDefinition {

    val definition =
        WeaponDefinition(
            id =
                ContentId(
                    "WPN_AMULET_OF_TROLL_KING"
                ),

            displayName =
                "Amulet of Troll King",

            description =
                "A regenerating spell focus that alternates between Water and Acid attacks.",

            tags =
                setOf(
                    ContentTag.SPELL_FOCUS
                ),

            damageTypes =
                setOf(
                    DamageType.WATER,
                    DamageType.ACID
                ),

            randomDamageTypePerAttack =
                true,

            upgradedBasicAttackDamageType =
                DamageType.WATER,

            attackPattern =
                WeaponAttackPattern
                    .STRAIGHT_PROJECTILE,

            normalAttackSpeedStat =
                StatType.HASTE,

            baseDamage = 12,
            baseAttacksPerSecond = 0.75f,
            maximumAttacksPerSecond = 1.90f,
            baseRange = 530f,
            baseProjectileSpeed = 540f,
            baseKnockback = 0f,

            baseProjectileCount = 1,
            baseMaximumTargets = 1,

            /*
             * Acid için Might of Nature / Shepherd
             * yüksek olanı kullanma kuralı daha sonra
             * merkezi DamageCalculator'a eklenecek.
             *
             * Şimdilik Water ağırlıklı ortak scaling.
             */
            damageScaling =
                mapOf(
                    StatType.WISDOM to 0.70f,
                    StatType.ELEMENTAL_DAMAGE to 0.50f,
                    StatType.MIGHT_OF_NATURE to 0.50f
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


                        level2Modifiers =
                            listOf(
                                WeaponModifierFactory
                                    .create(
                                        statType =
                                            StatType.HP_REGENERATION,
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
                DefaultWeaponAbilities
                    .acidicPool,

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )
}
