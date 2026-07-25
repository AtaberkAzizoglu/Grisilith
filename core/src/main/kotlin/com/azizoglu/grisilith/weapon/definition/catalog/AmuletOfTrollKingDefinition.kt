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

            attackPattern =
                WeaponAttackPattern
                    .STRAIGHT_PROJECTILE,

            normalAttackSpeedStat =
                StatType.HASTE,

            baseDamage = 17,
            baseAttacksPerSecond = 1.50f,
            baseRange = 680f,
            baseProjectileSpeed = 690f,
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
                    StatType.ELEMENTAL_DAMAGE to 0.50f
                ),

            passiveStatModifiers =
                emptyList(),

            levelStats =
                WeaponLevelProgressionFactory
                    .create(
                        level2Damage = 1.08f,
                        level3Damage = 1.24f,
                        level4Damage = 1.36f,
                        level5Damage = 1.36f,

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

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )
}
