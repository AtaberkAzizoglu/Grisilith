package com.azizoglu.grisilith.enemy.definition

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.enemy.EnemyType

/*
 * İlk Enemy Kind kataloğu.
 *
 * Bu değerler doğrudan final balance değildir. Her Kind'ın
 * göreli kimliğini kurar; Stage, Difficulty, Map ve EnemyType
 * çarpanları runtime/factory paketlerinde ayrıca uygulanacaktır.
 */
object DefaultEnemyKinds {

    private val WPN_LONGSWORD =
        ContentId("WPN_LONGSWORD")

    private val WPN_CROSSBOW =
        ContentId("WPN_CROSSBOW")

    private val WPN_LANCE =
        ContentId("WPN_LANCE")

    private val WPN_LONGBOW =
        ContentId("WPN_LONGBOW")

    private val WPN_DOUBLE_SWORDS =
        ContentId("WPN_DOUBLE_SWORDS")

    private val WPN_TOME_OF_ERYNDOR =
        ContentId("WPN_TOME_OF_ERYNDOR")

    private val WPN_AMULET_OF_TROLL_KING =
        ContentId("WPN_AMULET_OF_TROLL_KING")

    private val WPN_ORB_OF_SKY =
        ContentId("WPN_ORB_OF_SKY")

    private val ARMOR_LEATHER =
        ContentId("ARMOR_LEATHER")

    private val ARMOR_SCALE_MAIL =
        ContentId("ARMOR_SCALE_MAIL")

    private val ARMOR_PLATE =
        ContentId("ARMOR_PLATE")

    private val ARMOR_ROBE_OF_MAGE =
        ContentId("ARMOR_ROBE_OF_MAGE")

    val grayWolf =
        EnemyKindDefinition(
            id = EnemyKindIds.GRAY_WOLF,
            displayName = "Gray Wolf",
            description =
                "A fast Beast that becomes stronger when another Gray Wolf is nearby.",
            enemyTags = setOf(EnemyTag.BEAST),
            allowedTypes = setOf(EnemyType.NORMAL),
            sizeCategory = EnemySizeCategory.MEDIUM,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 100,
                    armorClass = 8,
                    contactDamage = 14,
                    movementSpeed = 150f,
                    attacksPerSecond = 0.80f,
                    attackRange = 54f
                ),
            behaviorId =
                EnemyBehaviorIds.FAST_CHASE_AND_MELEE,
            passiveIds =
                listOf(
                    EnemyAbilityIds.WOLF_PACK
                )
        )

    val boar =
        EnemyKindDefinition(
            id = EnemyKindIds.BOAR,
            displayName = "Boar",
            description =
                "A durable and slow Beast that can charge in a straight line.",
            enemyTags = setOf(EnemyTag.BEAST),
            allowedTypes = setOf(EnemyType.NORMAL),
            sizeCategory = EnemySizeCategory.MEDIUM,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 145,
                    armorClass = 14,
                    contactDamage = 18,
                    movementSpeed = 92f,
                    attacksPerSecond = 0.55f,
                    attackRange = 58f
                ),
            behaviorId =
                EnemyBehaviorIds.SLOW_CHASE_AND_MELEE,
            activeSkillIds =
                listOf(
                    EnemyAbilityIds.BOAR_CHARGE
                )
        )

    val baldEagle =
        EnemyKindDefinition(
            id = EnemyKindIds.BALD_EAGLE,
            displayName = "Bald Eagle",
            description =
                "A very fast but fragile flying Beast that ignores obstacles.",
            enemyTags = setOf(EnemyTag.BEAST),
            allowedTypes = setOf(EnemyType.NORMAL),
            sizeCategory = EnemySizeCategory.SMALL,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 65,
                    armorClass = 3,
                    contactDamage = 8,
                    movementSpeed = 190f,
                    attacksPerSecond = 1.25f,
                    attackRange = 44f
                ),
            behaviorId =
                EnemyBehaviorIds.FAST_CHASE_AND_MELEE,
            traversalProfile =
                EnemyTraversalProfile.INCORPOREAL
        )

    val grizzlyBear =
        EnemyKindDefinition(
            id = EnemyKindIds.GRIZZLY_BEAR,
            displayName = "Grizzly Bear",
            description =
                "A high-health Mini Boss that becomes enraged below half health.",
            enemyTags = setOf(EnemyTag.BEAST),
            allowedTypes = setOf(EnemyType.MINI_BOSS),
            sizeCategory = EnemySizeCategory.LARGE,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 430,
                    armorClass = 8,
                    contactDamage = 30,
                    movementSpeed = 115f,
                    attacksPerSecond = 0.60f,
                    attackRange = 76f
                ),
            behaviorId =
                EnemyBehaviorIds.CHASE_AND_MELEE,
            passiveIds =
                listOf(
                    EnemyAbilityIds.GRIZZLY_ANGRY
                )
        )

    val beeSwarm =
        EnemyKindDefinition(
            id = EnemyKindIds.BEE_SWARM,
            displayName = "Bee Swarm",
            description =
                "A single swarm entity with high health, low damage and rapid attacks.",
            enemyTags = setOf(EnemyTag.BEAST),
            allowedTypes = setOf(EnemyType.MINI_BOSS),
            sizeCategory = EnemySizeCategory.LARGE,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 470,
                    armorClass = 4,
                    contactDamage = 9,
                    movementSpeed = 122f,
                    attacksPerSecond = 2.20f,
                    attackRange = 82f
                ),
            behaviorId =
                EnemyBehaviorIds.SWARM_CHASE
        )

    val crocodile =
        EnemyKindDefinition(
            id = EnemyKindIds.CROCODILE,
            displayName = "Crocodile",
            description =
                "A massive and very slow Beast that leaps toward the player.",
            enemyTags = setOf(EnemyTag.BEAST),
            allowedTypes = setOf(EnemyType.MINI_BOSS),
            sizeCategory = EnemySizeCategory.VERY_LARGE,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 390,
                    armorClass = 11,
                    contactDamage = 38,
                    movementSpeed = 72f,
                    attacksPerSecond = 0.42f,
                    attackRange = 98f
                ),
            behaviorId =
                EnemyBehaviorIds.SLOW_CHASE_AND_MELEE,
            activeSkillIds =
                listOf(
                    EnemyAbilityIds.CROCODILE_JUMP
                )
        )

    val silverbackGorilla =
        EnemyKindDefinition(
            id = EnemyKindIds.SILVERBACK_GORILLA,
            displayName = "Silverback Gorilla",
            description =
                "The Beast Final Boss. A powerful brawler that throws a stationary projectile.",
            enemyTags = setOf(EnemyTag.BEAST),
            allowedTypes = setOf(EnemyType.FINAL_BOSS),
            sizeCategory = EnemySizeCategory.VERY_LARGE,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 1100,
                    armorClass = 15,
                    contactDamage = 45,
                    movementSpeed = 118f,
                    attacksPerSecond = 0.78f,
                    attackRange = 105f
                ),
            behaviorId =
                EnemyBehaviorIds.CHASE_AND_MELEE,
            activeSkillIds =
                listOf(
                    EnemyAbilityIds.GORILLA_THROW
                )
        )

    val bandit =
        EnemyKindDefinition(
            id = EnemyKindIds.BANDIT,
            displayName = "Bandit",
            description =
                "A balanced Humanoid carrying a Level 3 Longsword and Scale Mail Armor.",
            enemyTags = setOf(EnemyTag.HUMANOID),
            allowedTypes = setOf(EnemyType.NORMAL),
            sizeCategory = EnemySizeCategory.MEDIUM,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 82,
                    armorClass = 12,
                    contactDamage = 10,
                    movementSpeed = 120f,
                    attacksPerSecond = 0.85f,
                    attackRange = 58f
                ),
            behaviorId =
                EnemyBehaviorIds.CHASE_AND_MELEE,
            equipment =
                EnemyEquipmentDefinition(
                    weapons =
                        listOf(
                            EnemyWeaponDefinition(
                                weaponId = WPN_LONGSWORD,
                                level = 3
                            )
                        ),
                    armorId = ARMOR_SCALE_MAIL
                )
        )

    val archer =
        EnemyKindDefinition(
            id = EnemyKindIds.ARCHER,
            displayName = "Archer",
            description =
                "A fragile ranged Humanoid carrying a Level 3 Crossbow and Leather Armor.",
            enemyTags = setOf(EnemyTag.HUMANOID),
            allowedTypes = setOf(EnemyType.NORMAL),
            sizeCategory = EnemySizeCategory.MEDIUM,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 66,
                    armorClass = 5,
                    contactDamage = 6,
                    movementSpeed = 120f,
                    attacksPerSecond = 0.72f,
                    attackRange = 560f
                ),
            behaviorId =
                EnemyBehaviorIds.KEEP_DISTANCE_AND_SHOOT,
            equipment =
                EnemyEquipmentDefinition(
                    weapons =
                        listOf(
                            EnemyWeaponDefinition(
                                weaponId = WPN_CROSSBOW,
                                level = 3
                            )
                        ),
                    armorId = ARMOR_LEATHER
                )
        )

    val spearman =
        EnemyKindDefinition(
            id = EnemyKindIds.SPEARMAN,
            displayName = "Spearman",
            description =
                "A heavily armored Mini Boss using a Level 5 Lance and Plate Armor.",
            enemyTags = setOf(EnemyTag.HUMANOID),
            allowedTypes = setOf(EnemyType.MINI_BOSS),
            sizeCategory = EnemySizeCategory.LARGE,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 350,
                    armorClass = 22,
                    contactDamage = 27,
                    movementSpeed = 105f,
                    attacksPerSecond = 0.52f,
                    attackRange = 92f
                ),
            behaviorId =
                EnemyBehaviorIds.CHASE_AND_MELEE,
            equipment =
                EnemyEquipmentDefinition(
                    weapons =
                        listOf(
                            EnemyWeaponDefinition(
                                weaponId = WPN_LANCE,
                                level = 5
                            )
                        ),
                    armorId = ARMOR_PLATE
                )
        )

    val horseman =
        EnemyKindDefinition(
            id = EnemyKindIds.HORSEMAN,
            displayName = "Horseman",
            description =
                "A very fast ranged Mini Boss using a Level 5 Longbow and Leather Armor.",
            enemyTags = setOf(EnemyTag.HUMANOID),
            allowedTypes = setOf(EnemyType.MINI_BOSS),
            sizeCategory = EnemySizeCategory.VERY_LARGE,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 300,
                    armorClass = 7,
                    contactDamage = 18,
                    movementSpeed = 175f,
                    attacksPerSecond = 0.95f,
                    attackRange = 620f
                ),
            behaviorId =
                EnemyBehaviorIds.KEEP_DISTANCE_AND_SHOOT,
            equipment =
                EnemyEquipmentDefinition(
                    weapons =
                        listOf(
                            EnemyWeaponDefinition(
                                weaponId = WPN_LONGBOW,
                                level = 5
                            )
                        ),
                    armorId = ARMOR_LEATHER
                )
        )

    val witch =
        EnemyKindDefinition(
            id = EnemyKindIds.WITCH,
            displayName = "Witch",
            description =
                "The Humanoid Final Boss. Uses all equipped Spell Focus weapons simultaneously.",
            enemyTags = setOf(EnemyTag.HUMANOID),
            allowedTypes = setOf(EnemyType.FINAL_BOSS),
            sizeCategory = EnemySizeCategory.LARGE,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 900,
                    armorClass = 3,
                    contactDamage = 12,
                    movementSpeed = 145f,
                    attacksPerSecond = 1f,
                    attackRange = 680f
                ),
            behaviorId =
                EnemyBehaviorIds.MULTI_WEAPON_CASTER,
            equipment =
                EnemyEquipmentDefinition(
                    weapons =
                        listOf(
                            EnemyWeaponDefinition(
                                weaponId = WPN_TOME_OF_ERYNDOR,
                                level = 4,
                                quantity = 1
                            ),
                            EnemyWeaponDefinition(
                                weaponId = WPN_AMULET_OF_TROLL_KING,
                                level = 4,
                                quantity = 1
                            ),
                            EnemyWeaponDefinition(
                                weaponId = WPN_ORB_OF_SKY,
                                level = 5,
                                quantity = 2
                            ),
                            EnemyWeaponDefinition(
                                weaponId = WPN_TOME_OF_ERYNDOR,
                                level = 5,
                                quantity = 2
                            )
                        ),
                    armorId = ARMOR_ROBE_OF_MAGE
                )
        )

    val skeletonWarrior =
        EnemyKindDefinition(
            id = EnemyKindIds.SKELETON_WARRIOR,
            displayName = "Skeleton Warrior",
            description =
                "A balanced Undead warrior using Level 3 Double Swords.",
            enemyTags = setOf(EnemyTag.UNDEAD),
            allowedTypes = setOf(EnemyType.NORMAL),
            sizeCategory = EnemySizeCategory.MEDIUM,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 92,
                    armorClass = 9,
                    contactDamage = 12,
                    movementSpeed = 118f,
                    attacksPerSecond = 1.10f,
                    attackRange = 55f
                ),
            behaviorId =
                EnemyBehaviorIds.CHASE_AND_MELEE,
            equipment =
                EnemyEquipmentDefinition(
                    weapons =
                        listOf(
                            EnemyWeaponDefinition(
                                weaponId = WPN_DOUBLE_SWORDS,
                                level = 3
                            )
                        )
                )
        )

    val skeletonArcher =
        EnemyKindDefinition(
            id = EnemyKindIds.SKELETON_ARCHER,
            displayName = "Skeleton Archer",
            description =
                "A lightly armored Undead archer using a Level 3 Longbow.",
            enemyTags = setOf(EnemyTag.UNDEAD),
            allowedTypes = setOf(EnemyType.NORMAL),
            sizeCategory = EnemySizeCategory.MEDIUM,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 78,
                    armorClass = 4,
                    contactDamage = 6,
                    movementSpeed = 104f,
                    attacksPerSecond = 0.82f,
                    attackRange = 600f
                ),
            behaviorId =
                EnemyBehaviorIds.KEEP_DISTANCE_AND_SHOOT,
            equipment =
                EnemyEquipmentDefinition(
                    weapons =
                        listOf(
                            EnemyWeaponDefinition(
                                weaponId = WPN_LONGBOW,
                                level = 3
                            )
                        )
                )
        )

    val zombie =
        EnemyKindDefinition(
            id = EnemyKindIds.ZOMBIE,
            displayName = "Zombie",
            description =
                "A slow and durable Undead enemy with strong armor.",
            enemyTags = setOf(EnemyTag.UNDEAD),
            allowedTypes = setOf(EnemyType.NORMAL),
            sizeCategory = EnemySizeCategory.MEDIUM,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 140,
                    armorClass = 14,
                    contactDamage = 11,
                    movementSpeed = 78f,
                    attacksPerSecond = 0.58f,
                    attackRange = 52f
                ),
            behaviorId =
                EnemyBehaviorIds.SLOW_CHASE_AND_MELEE
        )

    val ghost =
        EnemyKindDefinition(
            id = EnemyKindIds.GHOST,
            displayName = "Ghost",
            description =
                "An incorporeal Mini Boss that ignores obstacles and can avoid incoming attacks.",
            enemyTags = setOf(EnemyTag.UNDEAD),
            allowedTypes = setOf(EnemyType.MINI_BOSS),
            sizeCategory = EnemySizeCategory.LARGE,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 340,
                    armorClass = 10,
                    contactDamage = 25,
                    movementSpeed = 120f,
                    attacksPerSecond = 0.62f,
                    attackRange = 68f
                ),
            behaviorId =
                EnemyBehaviorIds.INCORPOREAL_CHASE,
            traversalProfile =
                EnemyTraversalProfile.INCORPOREAL,
            passiveIds =
                listOf(
                    EnemyAbilityIds.GHOST_INCORPOREAL_EVASION
                )
        )

    val slime =
        EnemyKindDefinition(
            id = EnemyKindIds.SLIME,
            displayName = "Slime",
            description =
                "A huge, slow Mini Boss that leaves damaging Acidic Path areas behind it.",
            enemyTags = setOf(EnemyTag.UNDEAD),
            allowedTypes = setOf(EnemyType.MINI_BOSS),
            sizeCategory = EnemySizeCategory.VERY_LARGE,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 520,
                    armorClass = 2,
                    contactDamage = 18,
                    movementSpeed = 72f,
                    attacksPerSecond = 0.55f,
                    attackRange = 88f
                ),
            behaviorId =
                EnemyBehaviorIds.SLOW_CHASE_AND_MELEE,
            passiveIds =
                listOf(
                    EnemyAbilityIds.SLIME_ACIDIC_PATH
                )
        )

    val vampireLord =
        EnemyKindDefinition(
            id = EnemyKindIds.VAMPIRE_LORD,
            displayName = "Vampire Lord",
            description =
                "The Undead Final Boss. Teleports, drains life and carries a Longsword with Scale Mail.",
            enemyTags = setOf(EnemyTag.UNDEAD),
            allowedTypes = setOf(EnemyType.FINAL_BOSS),
            sizeCategory = EnemySizeCategory.COLOSSAL,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 1050,
                    armorClass = 13,
                    contactDamage = 38,
                    movementSpeed = 150f,
                    attacksPerSecond = 0.90f,
                    attackRange = 82f
                ),
            behaviorId =
                EnemyBehaviorIds.CHASE_AND_MELEE,
            equipment =
                EnemyEquipmentDefinition(
                    weapons =
                        listOf(
                            EnemyWeaponDefinition(
                                weaponId = WPN_LONGSWORD,
                                level = 4
                            )
                        ),
                    armorId = ARMOR_SCALE_MAIL
                ),
            activeSkillIds =
                listOf(
                    EnemyAbilityIds.VAMPIRE_MISTY_TRANSPORTATION
                ),
            passiveIds =
                listOf(
                    EnemyAbilityIds.VAMPIRE_HUNGER
                )
        )

    val all:
        List<EnemyKindDefinition> =
        listOf(
            grayWolf,
            boar,
            baldEagle,
            grizzlyBear,
            beeSwarm,
            crocodile,
            silverbackGorilla,
            bandit,
            archer,
            spearman,
            horseman,
            witch,
            skeletonWarrior,
            skeletonArcher,
            zombie,
            ghost,
            slime,
            vampireLord
        )

    fun createRegistry():
        EnemyKindRegistry {

        return EnemyKindRegistry()
            .also { registry ->
                registry.registerAll(
                    all
                )
            }
    }
}
