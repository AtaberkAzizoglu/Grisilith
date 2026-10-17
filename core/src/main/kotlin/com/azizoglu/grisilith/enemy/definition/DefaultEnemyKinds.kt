package com.azizoglu.grisilith.enemy.definition

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.enemy.EnemyType

/*
 * İlk Enemy Kind kataloğu.
 *
 * Bu değerler Faz 3 Map & Enemy denge tabanıdır.
 *
 * Normal Enemy'ler Stage 1'de artık tek vuruşta erimeyecek kadar
 * dayanıklıdır. Mini Boss ve Final Boss değerleri özellikle yüksek
 * tutulmuştur; Stage scaling ve EnemyType modifier'ları runtime'da
 * bunların üzerine uygulanır.
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
                "The forest's enthusiastic welcoming committee. It chases visitors down for a bite, and a nearby wolf makes the welcome faster and considerably less friendly.",
            enemyTags = setOf(EnemyTag.BEAST),
            allowedTypes = setOf(EnemyType.NORMAL),
            sizeCategory = EnemySizeCategory.MEDIUM,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 250,
                    armorClass = 6,
                    baseArmor = 3,
                    baseMagicResistance = 2,
                    magicResistance = 4,
                    contactDamage = 22,
                    movementSpeed = 150f,
                    attacksPerSecond = 0.85f,
                    attackRange = 54f,
                    weaponDamageMultiplier = 0.90f,
                    attackSpeedScore = 18,
                    mainDamageStat = 18,
                    secondaryDamageStat = 14,
                    baseCritRatePercent = 15f,
                    baseCritDamagePercent = 125f,
                    critRate = 0,
                    critDamage = 0,
                    piercing = 2
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
                "The forest's opinion on right of way, delivered tusks first. After lining up its route, it charges straight through anything still arguing about it.",
            enemyTags = setOf(EnemyTag.BEAST),
            allowedTypes = setOf(EnemyType.NORMAL),
            sizeCategory = EnemySizeCategory.MEDIUM,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 400,
                    armorClass = 14,
                    baseArmor = 3,
                    baseMagicResistance = 2,
                    magicResistance = 4,
                    contactDamage = 30,
                    movementSpeed = 92f,
                    attacksPerSecond = 0.60f,
                    attackRange = 58f,
                    weaponDamageMultiplier = 1.00f,
                    attackSpeedScore = 8,
                    mainDamageStat = 22,
                    secondaryDamageStat = 12,
                    baseCritRatePercent = 5f,
                    baseCritDamagePercent = 110f,
                    critRate = 0,
                    critDamage = 0,
                    piercing = 4
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
                "The forest's express delivery service, now delivering beak. It pursues visitors from above and considers walls somebody else's problem.",
            enemyTags = setOf(EnemyTag.BEAST),
            allowedTypes = setOf(EnemyType.NORMAL),
            sizeCategory = EnemySizeCategory.SMALL,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 170,
                    armorClass = 5,
                    baseArmor = 2,
                    baseMagicResistance = 2,
                    magicResistance = 6,
                    contactDamage = 12,
                    movementSpeed = 180f,
                    attacksPerSecond = 1.30f,
                    attackRange = 44f,
                    weaponDamageMultiplier = 0.82f,
                    attackSpeedScore = 24,
                    mainDamageStat = 14,
                    secondaryDamageStat = 22,
                    baseCritRatePercent = 40f,
                    baseCritDamagePercent = 150f,
                    critRate = 0,
                    critDamage = 0,
                    piercing = 4
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
                "The forest's complaints department. It follows you personally, and every wound makes its response stronger and its customer service worse.",
            enemyTags = setOf(EnemyTag.BEAST),
            allowedTypes = setOf(EnemyType.MINI_BOSS),
            sizeCategory = EnemySizeCategory.LARGE,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 5000,
                    armorClass = 13,
                    baseArmor = 3,
                    baseMagicResistance = 3,
                    magicResistance = 6,
                    contactDamage = 58,
                    movementSpeed = 145f,
                    attacksPerSecond = 0.68f,
                    attackRange = 76f,
                    weaponDamageMultiplier = 1.15f,
                    attackSpeedScore = 10,
                    mainDamageStat = 30,
                    secondaryDamageStat = 14,
                    baseCritRatePercent = 20f,
                    baseCritDamagePercent = 150f,
                    critRate = 0,
                    critDamage = 0,
                    piercing = 5
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
                "The forest's smallest residents have formed a very loud union. Fences cannot stop them and single-target swats achieve little; a broad hit hurts more, but may split the meeting into two angry committees.",
            enemyTags = setOf(EnemyTag.BEAST),
            allowedTypes = setOf(EnemyType.MINI_BOSS),
            sizeCategory = EnemySizeCategory.LARGE,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 3200,
                    armorClass = 4,
                    baseArmor = 1,
                    baseMagicResistance = 2,
                    magicResistance = 5,
                    contactDamage = 22,
                    movementSpeed = 132f,
                    attacksPerSecond = 2.35f,
                    attackRange = 82f,
                    weaponDamageMultiplier = 0.72f,
                    attackSpeedScore = 34,
                    mainDamageStat = 12,
                    secondaryDamageStat = 26,
                    baseCritRatePercent = 35f,
                    baseCritDamagePercent = 120f,
                    critRate = 0,
                    critDamage = 0,
                    piercing = 8
                ),
            behaviorId =
                EnemyBehaviorIds.SWARM_CHASE,
            traversalProfile =
                EnemyTraversalProfile.INCORPOREAL,
            passiveIds =
                listOf(
                    EnemyAbilityIds.SWARM_BEING,
                    EnemyAbilityIds.SPLIT_THE_SWARM
                )
        )

    val crocodile =
        EnemyKindDefinition(
            id = EnemyKindIds.CROCODILE,
            displayName = "Crocodile",
            description =
                "The forest's family picnic has arrived, and you are apparently lunch. Hungry youngsters keep joining in while the parent lines up a surprisingly athletic leap through anyone in its path.",
            enemyTags = setOf(EnemyTag.BEAST),
            allowedTypes = setOf(EnemyType.MINI_BOSS),
            sizeCategory = EnemySizeCategory.VERY_LARGE,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 6500,
                    armorClass = 15,
                    baseArmor = 4,
                    baseMagicResistance = 3,
                    magicResistance = 5,
                    contactDamage = 72,
                    movementSpeed = 72f,
                    attacksPerSecond = 0.48f,
                    attackRange = 98f,
                    weaponDamageMultiplier = 1.20f,
                    attackSpeedScore = 5,
                    mainDamageStat = 34,
                    secondaryDamageStat = 10,
                    baseCritRatePercent = 30f,
                    baseCritDamagePercent = 150f,
                    critRate = 0,
                    critDamage = 0,
                    piercing = 6
                ),
            behaviorId =
                EnemyBehaviorIds.SLOW_CHASE_AND_MELEE,
            activeSkillIds =
                listOf(
                    EnemyAbilityIds.CROCODILE_JUMP
                ),
            passiveIds =
                listOf(
                    EnemyAbilityIds.BABY_CROCODILES
                )
        )

    val silverbackGorilla =
        EnemyKindDefinition(
            id = EnemyKindIds.SILVERBACK_GORILLA,
            displayName = "Silverback Gorilla",
            description =
                "The forest's landlord would like you off the property. Stand close and it pounds a wide patch of ground; stand far away and the eviction notice arrives as a very large rock.",
            enemyTags = setOf(EnemyTag.BEAST),
            allowedTypes = setOf(EnemyType.FINAL_BOSS),
            sizeCategory = EnemySizeCategory.VERY_LARGE,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 20200,
                    armorClass = 18,
                    baseArmor = 3,
                    baseMagicResistance = 5,
                    magicResistance = 10,
                    contactDamage = 92,
                    movementSpeed = 165f,
                    attacksPerSecond = 0.90f,
                    attackRange = 105f,
                    weaponDamageMultiplier = 1.28f,
                    attackSpeedScore = 18,
                    mainDamageStat = 38,
                    secondaryDamageStat = 18,
                    baseCritRatePercent = 35f,
                    baseCritDamagePercent = 170f,
                    critRate = 0,
                    critDamage = 0,
                    piercing = 8
                ),
            behaviorId =
                EnemyBehaviorIds.CHASE_AND_MELEE,
            activeSkillIds =
                listOf(
                    EnemyAbilityIds.GORILLA_STOMP_THE_EARTH,
                    EnemyAbilityIds.GORILLA_THROW
                )
        )

    val bandit =
        EnemyKindDefinition(
            id = EnemyKindIds.BANDIT,
            spawnWeight = 4f,
            displayName = "Bandit",
            description =
                "An ambitious professional in the involuntary donations industry. Runs straight up to collect, with absolutely no interest in providing a receipt.",
            enemyTags = setOf(EnemyTag.HUMANOID),
            allowedTypes = setOf(EnemyType.NORMAL),
            sizeCategory = EnemySizeCategory.MEDIUM,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 325,
                    armorClass = 10,
                    baseArmor = 3,
                    baseMagicResistance = 4,
                    magicResistance = 8,
                    contactDamage = 26,
                    movementSpeed = 122f,
                    attacksPerSecond = 0.78f,
                    attackRange = 58f,
                    weaponDamageMultiplier = 0.95f,
                    attackSpeedScore = 14,
                    mainDamageStat = 18,
                    secondaryDamageStat = 16,
                    baseCritRatePercent = 20f,
                    baseCritDamagePercent = 130f,
                    critRate = 0,
                    critDamage = 0,
                    piercing = 5
                ),
            behaviorId =
                EnemyBehaviorIds.CHASE_AND_MELEE
        )

    val archer =
        EnemyKindDefinition(
            id = EnemyKindIds.ARCHER,
            displayName = "Archer",
            description =
                "A firm believer in conducting business remotely. Keeps a safe distance, takes an obvious moment to aim, then sends a slow arrow with a surprisingly expensive impact.",
            enemyTags = setOf(EnemyTag.HUMANOID),
            allowedTypes = setOf(EnemyType.NORMAL),
            sizeCategory = EnemySizeCategory.MEDIUM,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 200,
                    armorClass = 6,
                    baseArmor = 2,
                    baseMagicResistance = 4,
                    magicResistance = 8,
                    contactDamage = 16,
                    movementSpeed = 132f,
                    attacksPerSecond = 0.68f,
                    attackRange = 560f,
                    weaponDamageMultiplier = 0.90f,
                    attackSpeedScore = 16,
                    mainDamageStat = 12,
                    secondaryDamageStat = 24,
                    baseCritRatePercent = 30f,
                    baseCritDamagePercent = 150f,
                    critRate = 0,
                    critDamage = 0,
                    piercing = 12
                ),
            behaviorId =
                EnemyBehaviorIds.KEEP_DISTANCE_AND_SHOOT,
            activeSkillIds =
                listOf(
                    EnemyAbilityIds.AIMED_ARROW
                )
        )

    val spearman =
        EnemyKindDefinition(
            id = EnemyKindIds.SPEARMAN,
            displayName = "Spearman",
            description =
                "A specialist in keeping negotiations at spear length. Chases you with one spear, carefully tracks your movements, then throws another; apparently the equipment budget was approved.",
            enemyTags = setOf(EnemyTag.HUMANOID),
            allowedTypes = setOf(EnemyType.MINI_BOSS),
            sizeCategory = EnemySizeCategory.LARGE,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 7800,
                    armorClass = 17,
                    baseArmor = 5,
                    baseMagicResistance = 5,
                    magicResistance = 13,
                    contactDamage = 82,
                    movementSpeed = 105.6f,
                    attacksPerSecond = 0.54f,
                    attackRange = 98f,
                    weaponDamageMultiplier = 1.30f,
                    attackSpeedScore = 8,
                    mainDamageStat = 30,
                    secondaryDamageStat = 16,
                    baseCritRatePercent = 20f,
                    baseCritDamagePercent = 130f,
                    critRate = 0,
                    critDamage = 0,
                    piercing = 18
                ),
            behaviorId =
                EnemyBehaviorIds.CHASE_AND_MELEE,
            activeSkillIds =
                listOf(
                    EnemyAbilityIds.SPEARMAN_THROW_THE_LANCE
                )
        )

    val horseman =
        EnemyKindDefinition(
            id = EnemyKindIds.HORSEMAN,
            displayName = "Horseman",
            description =
                "Management supplied a horse, so now every problem is urgent. Closes the distance at speed and sweeps different stretches of ground with broad swings; personal space was not included in the contract.",
            enemyTags = setOf(EnemyTag.HUMANOID),
            allowedTypes = setOf(EnemyType.MINI_BOSS),
            sizeCategory = EnemySizeCategory.VERY_LARGE,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 6800,
                    armorClass = 15,
                    baseArmor = 4,
                    baseMagicResistance = 6,
                    magicResistance = 16,
                    contactDamage = 72,
                    movementSpeed = 202.5f,
                    attacksPerSecond = 0.82f,
                    attackRange = 82f,
                    weaponDamageMultiplier = 1.28f,
                    attackSpeedScore = 22,
                    mainDamageStat = 20,
                    secondaryDamageStat = 30,
                    baseCritRatePercent = 30f,
                    baseCritDamagePercent = 150f,
                    critRate = 0,
                    critDamage = 0,
                    piercing = 14
                ),
            behaviorId =
                EnemyBehaviorIds.CHASE_AND_MELEE,
            activeSkillIds =
                listOf(
                    EnemyAbilityIds.HORSEMAN_BATTLE_CRY
                )
        )

    val witch =
        EnemyKindDefinition(
            id = EnemyKindIds.WITCH,
            displayName = "Witch",
            description =
                "An independent consultant with far too many spell subscriptions. Stays back, throws elemental projectiles and mixes borrowed weapon magic into alarming combinations. Poison and Poisoned only get half the usual result; occupational exposure, presumably.",
            enemyTags = setOf(EnemyTag.HUMANOID),
            allowedTypes = setOf(EnemyType.FINAL_BOSS),
            sizeCategory = EnemySizeCategory.LARGE,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 24000,
                    armorClass = 21,
                    baseArmor = 4,
                    baseMagicResistance = 8,
                    magicResistance = 25,
                    contactDamage = 108,
                    movementSpeed = 155f,
                    attacksPerSecond = 1.65f,
                    attackRange = 680f,
                    weaponDamageMultiplier = 1.42f,
                    attackSpeedScore = 24,
                    mainDamageStat = 16,
                    secondaryDamageStat = 40,
                    baseCritRatePercent = 5f,
                    baseCritDamagePercent = 105f,
                    critRate = 0,
                    critDamage = 0,
                    piercing = 10
                ),
            behaviorId =
                EnemyBehaviorIds.MULTI_WEAPON_CASTER,
            activeSkillIds =
                listOf(
                    EnemyAbilityIds.WITCH_SUMMON_FAMILIARS,
                    EnemyAbilityIds.WITCH_ELEMENTAL_PROJECTILE
                )
        )

    val skeletonWarrior =
        EnemyKindDefinition(
            id = EnemyKindIds.SKELETON_WARRIOR,
            spawnWeight = 2f,
            displayName = "Skeleton Warrior",
            description =
                "Retirement was denied, so it still marches up to hit people. Having already completed the dying part, it ignores Poison, Necrotic damage, Poisoned and Necrosis. The pension remains outstanding.",
            enemyTags = setOf(EnemyTag.UNDEAD),
            allowedTypes = setOf(EnemyType.NORMAL),
            sizeCategory = EnemySizeCategory.MEDIUM,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 358,
                    armorClass = 11,
                    baseArmor = 3,
                    baseMagicResistance = 5,
                    magicResistance = 10,
                    contactDamage = 26,
                    movementSpeed = 122f,
                    attacksPerSecond = 0.78f,
                    attackRange = 58f,
                    weaponDamageMultiplier = 1.00f,
                    attackSpeedScore = 14,
                    mainDamageStat = 18,
                    secondaryDamageStat = 16,
                    baseCritRatePercent = 20f,
                    baseCritDamagePercent = 130f,
                    critRate = 0,
                    critDamage = 0,
                    piercing = 5
                ),
            behaviorId =
                EnemyBehaviorIds.CHASE_AND_MELEE
        )

    val skeletonArcher =
        EnemyKindDefinition(
            id = EnemyKindIds.SKELETON_ARCHER,
            displayName = "Skeleton Archer",
            description =
                "Still working from beyond the grave, preferably at a distance. Pauses to aim before firing slow, heavy-hitting arrows. Poison, Necrotic damage, Poisoned and Necrosis cannot make this shift any worse.",
            enemyTags = setOf(EnemyTag.UNDEAD),
            allowedTypes = setOf(EnemyType.NORMAL),
            sizeCategory = EnemySizeCategory.MEDIUM,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 200,
                    armorClass = 6,
                    baseArmor = 2,
                    baseMagicResistance = 5,
                    magicResistance = 11,
                    contactDamage = 16,
                    movementSpeed = 132f,
                    attacksPerSecond = 0.68f,
                    attackRange = 560f,
                    weaponDamageMultiplier = 0.90f,
                    attackSpeedScore = 16,
                    mainDamageStat = 12,
                    secondaryDamageStat = 24,
                    baseCritRatePercent = 30f,
                    baseCritDamagePercent = 150f,
                    critRate = 0,
                    critDamage = 0,
                    piercing = 13
                ),
            behaviorId =
                EnemyBehaviorIds.KEEP_DISTANCE_AND_SHOOT,
            activeSkillIds =
                listOf(
                    EnemyAbilityIds.AIMED_ARROW
                )
        )

    val zombie =
        EnemyKindDefinition(
            id = EnemyKindIds.ZOMBIE,
            spawnWeight = 2f,
            displayName = "Zombie",
            description =
                "Clocked out of life, never clocked out of work. Shambles after visitors and takes an unreasonable amount of punishment before stopping. Poison, Necrotic damage, Poisoned and Necrosis are not accepted sick-leave excuses.",
            enemyTags = setOf(EnemyTag.UNDEAD),
            allowedTypes = setOf(EnemyType.NORMAL),
            sizeCategory = EnemySizeCategory.MEDIUM,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 520,
                    armorClass = 16,
                    baseArmor = 4,
                    baseMagicResistance = 6,
                    magicResistance = 13,
                    contactDamage = 24,
                    movementSpeed = 70f,
                    attacksPerSecond = 0.62f,
                    attackRange = 52f,
                    weaponDamageMultiplier = 0.92f,
                    attackSpeedScore = 5,
                    mainDamageStat = 24,
                    secondaryDamageStat = 8,
                    baseCritRatePercent = 5f,
                    baseCritDamagePercent = 115f,
                    critRate = 0,
                    critDamage = 0,
                    piercing = 2
                ),
            behaviorId =
                EnemyBehaviorIds.SLOW_CHASE_AND_MELEE
        )

    val ghost =
        EnemyKindDefinition(
            id = EnemyKindIds.GHOST,
            displayName = "Ghost",
            description =
                "Death's most committed office lurker brings its own visibility-reducing fog. Drifts through obstacles, sometimes lets attacks pass through, and answers personal visits with area attacks. Poison and Necrotic damage, Poisoned and Necrosis all go unanswered.",
            enemyTags = setOf(EnemyTag.UNDEAD),
            allowedTypes = setOf(EnemyType.MINI_BOSS),
            sizeCategory = EnemySizeCategory.LARGE,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 6200,
                    armorClass = 8,
                    baseArmor = 1,
                    baseMagicResistance = 9,
                    magicResistance = 14,
                    contactDamage = 18,
                    movementSpeed = 120f,
                    attacksPerSecond = 0.70f,
                    attackRange = 68f,
                    weaponDamageMultiplier = 1.08f,
                    attackSpeedScore = 16,
                    mainDamageStat = 10,
                    secondaryDamageStat = 32,
                    baseCritRatePercent = 40f,
                    baseCritDamagePercent = 150f,
                    critRate = 0,
                    critDamage = 0,
                    piercing = 8
                ),
            behaviorId =
                EnemyBehaviorIds.INCORPOREAL_CHASE,
            traversalProfile =
                EnemyTraversalProfile.INCORPOREAL,
            passiveIds =
                listOf(
                    EnemyAbilityIds.GHOST_HAUNTING_PRESENCE,
                    EnemyAbilityIds.GHOST_INCORPOREAL_EVASION
                )
        )

    val slime =
        EnemyKindDefinition(
            id = EnemyKindIds.SLIME,
            displayName = "Slime",
            description =
                "The afterlife's cleaning department has misunderstood the assignment. Slides after visitors, leaving short-lived acid trails that hurt and corrode. Its first death creates four smaller messes; Poison, Necrotic damage, Poisoned and Necrosis cannot improve the situation.",
            enemyTags = setOf(EnemyTag.UNDEAD),
            allowedTypes = setOf(EnemyType.MINI_BOSS),
            sizeCategory = EnemySizeCategory.VERY_LARGE,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 7600,
                    armorClass = 5,
                    baseArmor = 1,
                    baseMagicResistance = 8,
                    magicResistance = 8,
                    contactDamage = 65,
                    movementSpeed = 72f,
                    attacksPerSecond = 0.62f,
                    attackRange = 88f,
                    weaponDamageMultiplier = 1.00f,
                    attackSpeedScore = 6,
                    mainDamageStat = 22,
                    secondaryDamageStat = 26,
                    baseCritRatePercent = 5f,
                    baseCritDamagePercent = 110f,
                    critRate = 0,
                    critDamage = 0,
                    piercing = 2
                ),
            behaviorId =
                EnemyBehaviorIds.SLOW_CHASE_AND_MELEE,
            traversalProfile =
                EnemyTraversalProfile.INCORPOREAL,
            passiveIds =
                listOf(
                    EnemyAbilityIds.SLIME_SPLIT_ON_DEATH,
                    EnemyAbilityIds.SLIME_ACIDIC_PATH
                )
        )

    val vampireLord =
        EnemyKindDefinition(
            id = EnemyKindIds.VAMPIRE_LORD,
            displayName = "Vampire Lord",
            description =
                "Upper management insists the graveyard is a family. This particular relative hunts up close, drains your life, sweeps the surrounding ground and commutes through living mist. Poison, Necrotic damage, Poisoned and Necrosis will not persuade it to take a day off.",
            enemyTags = setOf(EnemyTag.UNDEAD),
            allowedTypes = setOf(EnemyType.FINAL_BOSS),
            sizeCategory = EnemySizeCategory.LARGE,
            baseStats =
                EnemyBaseStats(
                    maximumHealth = 22000,
                    armorClass = 20,
                    baseArmor = 4,
                    baseMagicResistance = 10,
                    magicResistance = 22,
                    contactDamage = 105,
                    movementSpeed = 170f,
                    attacksPerSecond = 0.95f,
                    attackRange = 105f,
                    weaponDamageMultiplier = 1.35f,
                    attackSpeedScore = 26,
                    mainDamageStat = 34,
                    secondaryDamageStat = 28,
                    baseCritRatePercent = 50f,
                    baseCritDamagePercent = 200f,
                    critRate = 0,
                    critDamage = 0,
                    piercing = 16
                ),
            behaviorId =
                EnemyBehaviorIds.CHASE_AND_MELEE,
            activeSkillIds =
                listOf(
                    EnemyAbilityIds.VAMPIRE_MISTY_TRANSPORTATION,
                    EnemyAbilityIds.HORSEMAN_BATTLE_CRY
                ),
            passiveIds =
                listOf(
                    EnemyAbilityIds.VAMPIRE_HUNGER
                )
        )

    val mimic =
        EnemyKindDefinition(
            id = EnemyKindIds.MIMIC,
            displayName = "Mimic",
            description = "A monstrosity offering the most suspicious discount in the dungeon: free loot, collection in person. Looks like an ordinary chest until you approach, then abruptly revises the terms and conditions to include teeth.",
            enemyTags = setOf(EnemyTag.MONSTER),
            allowedTypes = setOf(EnemyType.NORMAL),
            sizeCategory = EnemySizeCategory.MEDIUM,
            baseStats = boar.baseStats,
            behaviorId = EnemyBehaviorIds.CHASE_AND_MELEE,
            passiveIds = listOf(EnemyAbilityIds.MIMICRY),
            spawnWeight = 0.01f
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
            vampireLord,
            mimic
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
