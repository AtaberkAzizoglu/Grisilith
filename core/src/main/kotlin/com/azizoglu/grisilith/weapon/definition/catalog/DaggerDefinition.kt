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

object DaggerDefinition {

    val definition =
        WeaponDefinition(
            id =
                ContentId(
                    "WPN_DAGGER"
                ),

            displayName =
                "Dagger",

            description =
                "A fast weapon that stabs nearby targets or throws blades at range.",

            tags =
                setOf(
                    ContentTag.MELEE_WEAPON,
                    ContentTag.RANGED_WEAPON,
                    ContentTag.THROW
                ),

            damageTypes =
                setOf(
                    DamageType.PIERCING
                ),

            attackPattern =
                WeaponAttackPattern
                    .MELEE_OR_THROW,

            baseDamage = 2,
            baseAttacksPerSecond = 1.5f,

            /*
             * Çok kısa melee menzili.
             */
            baseRange = 90f,

            /*
             * Fırlatma menzili.
             */
            secondaryRange = 480f,

            baseProjectileSpeed = 780f,
            baseKnockback = 0f,

            damageScaling =
                mapOf(
                    StatType.DEXTERITY to 1f
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
                                            StatType.ATTACK_SPEED,
                                        amount = 1
                                    )
                            ),

                        level4Modifiers =
                            listOf(
                                WeaponModifierFactory
                                    .create(
                                        statType =
                                            StatType.CRIT_RATE,
                                        amount = 1
                                    )
                            )
                    ),

            weaponAbility =
                DefaultWeaponAbilities
                    .assassinate,

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )
}
