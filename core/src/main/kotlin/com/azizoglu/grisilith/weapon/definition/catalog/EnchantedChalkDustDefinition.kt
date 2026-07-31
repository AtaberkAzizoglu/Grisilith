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
                    DamageType.AIR
                ),

            randomDamageTypePerAttack =
                false,

            attackPattern =
                WeaponAttackPattern
                    .STRAIGHT_PROJECTILE,

            normalAttackSpeedStat =
                StatType.HASTE,

            baseDamage = 15,
            baseAttacksPerSecond = 1.55f,
            baseRange = 670f,
            baseProjectileSpeed = 730f,
            baseKnockback = 3f,

            baseProjectileCount = 1,
            baseMaximumTargets = 1,

            damageScaling =
                mapOf(
                    StatType.WISDOM to 0.70f,
                    StatType.ELEMENTAL_DAMAGE to 0.45f
                ),

            passiveStatModifiers =
                emptyList(),

            levelStats =
                WeaponLevelProgressionFactory
                    .create(
                        level2Damage = 1.08f,
                        level3Damage = 1.20f,
                        level4Damage = 1.34f,
                        level5Damage = 1.34f,

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

            /*
             * Envanterde yalnızca bir instance.
             * Yeni alınan kopyalar mevcut olanı
             * level yükseltir.
             */
            maximumInventoryCopies = 1,

            initialUnlockState =
                ContentUnlockState.LOCKED
        )
}
