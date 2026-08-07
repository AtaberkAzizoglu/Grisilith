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

object EnchantedChalkDustDefinition {

    val definition =
        WeaponDefinition(
            id =
                ContentId(
                    "WPN_ENCHANTED_CHALK_DUST"
                ),

            displayName =
                "Enchanted Chalk Dust",

            description =
                "A unique Air spell focus used to draw powerful magical glyphs.",

            tags =
                setOf(
                    ContentTag.SPELL_FOCUS,
                    ContentTag.SPECIAL,
                    ContentTag.GLYPH_MOTION
                ),

            damageTypes =
                setOf(
                    DamageType.AIR,
                    DamageType.LIGHTNING
                ),

            randomDamageTypePerAttack =
                true,

            upgradedBasicAttackDamageType =
                DamageType.AIR,

            attackPattern =
                WeaponAttackPattern
                    .STRAIGHT_PROJECTILE,

            normalAttackSpeedStat =
                StatType.HASTE,

            baseDamage = 6,
            baseAttacksPerSecond = 0.520f,
            maximumAttacksPerSecond = 3.75f,
            baseRange = 540f,
            baseProjectileSpeed = 880f,
            baseKnockback = 0f,

            baseProjectileCount = 1,
            baseMaximumTargets = 1,

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
                                            StatType.HARVEST,
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
                    .glyphCasting,

            initialUnlockState =
                ContentUnlockState.LOCKED
        )
}
