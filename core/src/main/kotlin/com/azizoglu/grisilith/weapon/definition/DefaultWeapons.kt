package com.azizoglu.grisilith.weapon.definition

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.content.ContentRarity
import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.content.ContentUnlockState
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.content.StatModifier
import com.azizoglu.grisilith.statistics.StatType

object DefaultWeapons {

    val shortsword =
        WeaponDefinition(
            id =
                ContentId("WPN_SHORTSWORD"),

            displayName =
                "Shortsword",

            description =
                "A fast melee weapon that delivers repeated light slashes.",

            tags =
                setOf(
                    ContentTag.MELEE_WEAPON,
                    ContentTag.LIGHT_WEAPON,
                    ContentTag.TWO_HANDED
                ),

            damageTypes =
                setOf(
                    DamageType.SLASHING
                ),

            attackPattern =
                WeaponAttackPattern
                    .SHORT_SLASH_LINES,

            baseDamage = 16,
            baseAttacksPerSecond = 2.8f,
            baseRange = 125f,
            baseProjectileSpeed = 0f,
            baseKnockback = 12f,

            areaAngleDegrees = 18f,
            areaRadius = 48f,

            damageScaling =
                mapOf(
                    StatType.STRENGTH to 1f
                ),

            passiveStatModifiers =
                listOf(
                    modifier(
                        StatType.MOVEMENT_SPEED,
                        1
                    ),
                    modifier(
                        StatType.CRIT_RATE,
                        1
                    )
                ),

            rarityStats =
                progression(
                    rareDamage = 1.15f,
                    veryRareDamage = 1.35f,
                    legendaryDamage = 1.60f,

                    rareModifiers =
                        listOf(
                            modifier(
                                StatType.MOVEMENT_SPEED,
                                1
                            )
                        ),

                    legendaryAttackSpeed = 1.15f,

                    legendaryModifiers =
                        listOf(
                            modifier(
                                StatType.ATTACK_SPEED,
                                2
                            )
                        )
                ),

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )

    val longsword =
        WeaponDefinition(
            id =
                ContentId("WPN_LONGSWORD"),

            displayName =
                "Longsword",

            description =
                "A balanced melee weapon with a reliable forward sweep.",

            tags =
                setOf(
                    ContentTag.MELEE_WEAPON,
                    ContentTag.VERSATILE
                ),

            damageTypes =
                setOf(
                    DamageType.SLASHING
                ),

            attackPattern =
                WeaponAttackPattern.FORWARD_CONE,

            baseDamage = 26,
            baseAttacksPerSecond = 1.8f,
            baseRange = 165f,
            baseProjectileSpeed = 0f,
            baseKnockback = 22f,

            areaAngleDegrees = 35f,
            areaRadius = 165f,

            damageScaling =
                mapOf(
                    StatType.STRENGTH to 1f
                ),

            passiveStatModifiers =
                listOf(
                    modifier(
                        StatType.STRENGTH,
                        1
                    ),
                    modifier(
                        StatType.CRIT_RATE,
                        1
                    )
                ),

            rarityStats =
                progression(
                    rareDamage = 1.17f,
                    veryRareDamage = 1.38f,
                    legendaryDamage = 1.70f,

                    rareModifiers =
                        listOf(
                            modifier(
                                StatType.HARVEST,
                                1
                            )
                        ),

                    legendaryModifiers =
                        listOf(
                            modifier(
                                StatType.CRIT_RATE,
                                1
                            )
                        )
                ),

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )

    val lance =
        WeaponDefinition(
            id =
                ContentId("WPN_LANCE"),

            displayName =
                "Lance",

            description =
                "A slow heavy weapon that delivers powerful area strikes.",

            tags =
                setOf(
                    ContentTag.MELEE_WEAPON,
                    ContentTag.HEAVY_WEAPON
                ),

            damageTypes =
                setOf(
                    DamageType.SLASHING
                ),

            attackPattern =
                WeaponAttackPattern
                    .TARGET_AREA_CIRCLE,

            baseDamage = 44,
            baseAttacksPerSecond = 1f,
            baseRange = 215f,
            baseProjectileSpeed = 0f,
            baseKnockback = 38f,

            areaRadius = 75f,

            damageScaling =
                mapOf(
                    StatType.STRENGTH to 1f,
                    StatType.CRIT_DAMAGE to 0.20f
                ),

            passiveStatModifiers =
                listOf(
                    modifier(
                        StatType.STRENGTH,
                        1
                    ),
                    modifier(
                        StatType.CRIT_DAMAGE,
                        1
                    )
                ),

            rarityStats =
                progression(
                    rareDamage = 1.18f,
                    veryRareDamage = 1.42f,
                    legendaryDamage = 1.80f,

                    rareModifiers =
                        listOf(
                            modifier(
                                StatType.CRIT_DAMAGE,
                                1
                            )
                        ),

                    legendaryPiercing = 1
                ),

            initialUnlockState =
                ContentUnlockState.LOCKED
        )

    val dagger =
        WeaponDefinition(
            id =
                ContentId("WPN_DAGGER"),

            displayName =
                "Dagger",

            description =
                "A rapid hybrid weapon that attacks in melee or throws blades.",

            tags =
                setOf(
                    ContentTag.MELEE_WEAPON,
                    ContentTag.RANGED_WEAPON,
                    ContentTag.LIGHT_WEAPON,
                    ContentTag.THROW,
                    ContentTag.TWO_HANDED
                ),

            damageTypes =
                setOf(
                    DamageType.SLASHING
                ),

            attackPattern =
                WeaponAttackPattern.MELEE_OR_THROW,

            baseDamage = 13,
            baseAttacksPerSecond = 3.2f,
            baseRange = 90f,
            secondaryRange = 480f,
            baseProjectileSpeed = 780f,
            baseKnockback = 0f,

            damageScaling =
                mapOf(
                    StatType.DEXTERITY to 1f
                ),

            passiveStatModifiers =
                listOf(
                    modifier(
                        StatType.ATTACK_SPEED,
                        2
                    )
                ),

            rarityStats =
                progression(
                    rareDamage = 1.14f,
                    veryRareDamage = 1.32f,
                    legendaryDamage = 1.55f,

                    rareAttackSpeed = 1.08f,

                    rareModifiers =
                        listOf(
                            modifier(
                                StatType.ATTACK_SPEED,
                                1
                            )
                        ),

                    legendaryModifiers =
                        listOf(
                            modifier(
                                StatType.CRIT_DAMAGE,
                                1
                            )
                        )
                ),

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )

    val longbow =
        WeaponDefinition(
            id =
                ContentId("WPN_LONGBOW"),

            displayName =
                "Longbow",

            description =
                "A balanced ranged weapon with reliable reach and accuracy.",

            tags =
                setOf(
                    ContentTag.RANGED_WEAPON,
                    ContentTag.AMMUNITION
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
            baseRange = 1050f,
            baseProjectileSpeed = 920f,
            baseKnockback = 10f,

            damageScaling =
                mapOf(
                    StatType.DEXTERITY to 1f
                ),

            passiveStatModifiers =
                listOf(
                    modifier(
                        StatType.DEXTERITY,
                        1
                    ),
                    modifier(
                        StatType.CRIT_RATE,
                        1
                    )
                ),

            rarityStats =
                progression(
                    rareDamage = 1.15f,
                    veryRareDamage = 1.34f,
                    legendaryDamage = 1.62f,

                    rareModifiers =
                        listOf(
                            modifier(
                                StatType.CRIT_DAMAGE,
                                1
                            )
                        ),

                    legendaryModifiers =
                        listOf(
                            modifier(
                                StatType.CRIT_RATE,
                                1
                            )
                        )
                ),

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )

    val crossbow =
        WeaponDefinition(
            id =
                ContentId("WPN_CROSSBOW"),

            displayName =
                "Crossbow",

            description =
                "A slow ranged weapon whose bolts can continue through a second target.",

            tags =
                setOf(
                    ContentTag.RANGED_WEAPON,
                    ContentTag.AMMUNITION
                ),

            damageTypes =
                setOf(
                    DamageType.PIERCING
                ),

            attackPattern =
                WeaponAttackPattern
                    .PIERCING_PROJECTILE,

            baseDamage = 37,
            baseAttacksPerSecond = 1.05f,
            baseRange = 1100f,
            baseProjectileSpeed = 850f,
            baseKnockback = 20f,

            baseMaximumTargets = 2,

            damageScaling =
                mapOf(
                    StatType.DEXTERITY to 0.90f
                ),

            passiveStatModifiers =
                listOf(
                    modifier(
                        StatType.DEXTERITY,
                        1
                    ),
                    modifier(
                        StatType.CRIT_RATE,
                        1
                    )
                ),

            rarityStats =
                progression(
                    rareDamage = 1.17f,
                    veryRareDamage = 1.40f,
                    legendaryDamage = 1.75f,

                    rareModifiers =
                        listOf(
                            modifier(
                                StatType.CRIT_DAMAGE,
                                1
                            )
                        ),

                    legendaryPiercing = 2
                ),

            initialUnlockState =
                ContentUnlockState.LOCKED
        )

    val staff =
        WeaponDefinition(
            id =
                ContentId("WPN_STAFF"),

            displayName =
                "Staff",

            description =
                "A poison-focused spell focus intended to support summoning skills.",

            tags =
                setOf(
                    ContentTag.SPELL_FOCUS
                ),

            damageTypes =
                setOf(
                    DamageType.POISON
                ),

            attackPattern =
                WeaponAttackPattern
                    .STRAIGHT_PROJECTILE,

            baseDamage = 18,
            baseAttacksPerSecond = 1.65f,
            baseRange = 850f,
            baseProjectileSpeed = 680f,
            baseKnockback = 0f,

            damageScaling =
                mapOf(
                    StatType.WISDOM to 0.70f,
                    StatType.SHEPHERD to 0.45f
                ),

            passiveStatModifiers =
                listOf(
                    modifier(
                        StatType.SHEPHERD,
                        1
                    ),
                    modifier(
                        StatType.HARVEST,
                        1
                    )
                ),

            rarityStats =
                progression(
                    rareDamage = 1.12f,
                    veryRareDamage = 1.28f,
                    legendaryDamage = 1.50f,

                    rareModifiers =
                        listOf(
                            modifier(
                                StatType.SHEPHERD,
                                1
                            )
                        ),

                    legendaryModifiers =
                        listOf(
                            modifier(
                                StatType.MOVEMENT_SPEED,
                                1
                            )
                        )
                ),

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )

    val wand =
        WeaponDefinition(
            id =
                ContentId("WPN_WAND"),

            displayName =
                "Wand",

            description =
                "A spell focus that randomly uses Air, Water, Fire or Earth damage.",

            tags =
                setOf(
                    ContentTag.SPELL_FOCUS
                ),

            damageTypes =
                setOf(
                    DamageType.AIR,
                    DamageType.WATER,
                    DamageType.FIRE,
                    DamageType.EARTH
                ),

            randomDamageTypePerAttack = true,

            attackPattern =
                WeaponAttackPattern
                    .STRAIGHT_PROJECTILE,

            baseDamage = 18,
            baseAttacksPerSecond = 1.65f,
            baseRange = 850f,
            baseProjectileSpeed = 720f,
            baseKnockback = 0f,

            damageScaling =
                mapOf(
                    StatType.WISDOM to 0.70f,
                    StatType.ELEMENTAL_DAMAGE to 0.50f
                ),

            passiveStatModifiers =
                listOf(
                    modifier(
                        StatType.ELEMENTAL_DAMAGE,
                        2
                    )
                ),

            rarityStats =
                progression(
                    rareDamage = 1.12f,
                    veryRareDamage = 1.28f,
                    legendaryDamage = 1.50f,

                    rareModifiers =
                        listOf(
                            modifier(
                                StatType.ELEMENTAL_DAMAGE,
                                1
                            )
                        ),

                    legendaryModifiers =
                        listOf(
                            modifier(
                                StatType.LUCK,
                                1
                            )
                        )
                ),

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )

    val theBook =
        WeaponDefinition(
            id =
                ContentId("WPN_THE_BOOK"),

            displayName =
                "The Book",

            description =
                "A spell focus that randomly uses Acid, Lightning, Necrotic or Cold damage.",

            tags =
                setOf(
                    ContentTag.SPELL_FOCUS
                ),

            damageTypes =
                setOf(
                    DamageType.ACID,
                    DamageType.LIGHTNING,
                    DamageType.NECROTIC,
                    DamageType.COLD
                ),

            randomDamageTypePerAttack = true,

            attackPattern =
                WeaponAttackPattern
                    .STRAIGHT_PROJECTILE,

            baseDamage = 18,
            baseAttacksPerSecond = 1.65f,
            baseRange = 850f,
            baseProjectileSpeed = 700f,
            baseKnockback = 0f,

            damageScaling =
                mapOf(
                    StatType.WISDOM to 0.70f,
                    StatType.MIGHT_OF_NATURE to 0.50f
                ),

            passiveStatModifiers =
                listOf(
                    modifier(
                        StatType.WISDOM,
                        1
                    ),
                    modifier(
                        StatType.LIFE_STEAL,
                        1
                    )
                ),

            rarityStats =
                progression(
                    rareDamage = 1.12f,
                    veryRareDamage = 1.28f,
                    legendaryDamage = 1.50f,

                    rareModifiers =
                        listOf(
                            modifier(
                                StatType.MIGHT_OF_NATURE,
                                1
                            )
                        ),

                    legendaryModifiers =
                        listOf(
                            modifier(
                                StatType.LIFE_STEAL,
                                1
                            )
                        )
                ),

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )

    /*
     * Shield saldırı yapmayan ve yalnızca
     * Secondary Weapon Slot'a takılabilen
     * özel bir WeaponDefinition'dır.
     */
    val shield =
        WeaponDefinition(
            id =
                ContentId("WPN_SHIELD"),

            displayName =
                "Shield",

            description =
                "A defensive off-hand that increases durability but reduces mobility.",

            tags =
                setOf(
                    ContentTag.OFF_HAND,
                    ContentTag.DEFENSIVE
                ),

            damageTypes =
                setOf(
                    DamageType.NONE
                ),

            attackPattern =
                WeaponAttackPattern.NONE,

            baseDamage = 0,
            baseAttacksPerSecond = 0f,
            baseRange = 0f,
            baseProjectileSpeed = 0f,
            baseKnockback = 0f,
            baseProjectileCount = 0,
            baseMaximumTargets = 0,

            damageScaling =
                emptyMap(),

            passiveStatModifiers =
                listOf(
                    modifier(
                        StatType.ARMOR_CLASS,
                        2
                    ),
                    modifier(
                        StatType.ATTACK_SPEED,
                        -1
                    ),
                    modifier(
                        StatType.MOVEMENT_SPEED,
                        -2
                    )
                ),

            rarityStats =
                mapOf(
                    ContentRarity.COMMON to
                        WeaponRarityStats(
                            rarity =
                                ContentRarity.COMMON
                        )
                ),

            isUpgradeable = false,
            marketCanOfferHigherRarity = false,

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )

    val all:
        List<WeaponDefinition> =
        listOf(
            shortsword,
            longsword,
            lance,
            dagger,
            longbow,
            crossbow,
            staff,
            wand,
            theBook,
            shield
        )

    private fun progression(
        rareDamage: Float,
        veryRareDamage: Float,
        legendaryDamage: Float,

        rareAttackSpeed: Float = 1f,
        legendaryAttackSpeed: Float = 1f,

        rareModifiers:
        List<StatModifier> =
            emptyList(),

        veryRareModifiers:
        List<StatModifier> =
            emptyList(),

        legendaryModifiers:
        List<StatModifier> =
            emptyList(),

        rarePiercing: Int = 0,
        veryRarePiercing: Int = 0,
        legendaryPiercing: Int = 0
    ): Map<ContentRarity, WeaponRarityStats> {
        return mapOf(
            ContentRarity.COMMON to
                WeaponRarityStats(
                    rarity =
                        ContentRarity.COMMON,
                    damageMultiplier = 1f
                ),

            ContentRarity.RARE to
                WeaponRarityStats(
                    rarity =
                        ContentRarity.RARE,
                    damageMultiplier =
                        rareDamage,
                    attackSpeedMultiplier =
                        rareAttackSpeed,
                    additionalPiercingTargets =
                        rarePiercing,
                    grantedStatModifiers =
                        rareModifiers
                ),

            ContentRarity.VERY_RARE to
                WeaponRarityStats(
                    rarity =
                        ContentRarity.VERY_RARE,
                    damageMultiplier =
                        veryRareDamage,
                    attackSpeedMultiplier =
                        rareAttackSpeed,
                    additionalPiercingTargets =
                        maxOf(
                            rarePiercing,
                            veryRarePiercing
                        ),
                    grantedStatModifiers =
                        rareModifiers +
                            veryRareModifiers
                ),

            ContentRarity.LEGENDARY to
                WeaponRarityStats(
                    rarity =
                        ContentRarity.LEGENDARY,
                    damageMultiplier =
                        legendaryDamage,
                    attackSpeedMultiplier =
                        rareAttackSpeed *
                            legendaryAttackSpeed,
                    additionalPiercingTargets =
                        maxOf(
                            rarePiercing,
                            veryRarePiercing,
                            legendaryPiercing
                        ),
                    grantedStatModifiers =
                        rareModifiers +
                            veryRareModifiers +
                            legendaryModifiers
                )
        )
    }

    private fun modifier(
        statType: StatType,
        amount: Int
    ): StatModifier {
        return StatModifier(
            statType = statType,
            amount = amount
        )
    }
}
