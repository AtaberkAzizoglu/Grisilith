package com.azizoglu.grisilith.item.definition

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.content.ContentUnlockState
import com.azizoglu.grisilith.content.StatModifier
import com.azizoglu.grisilith.statistics.StatType

object DefaultItems {

    private fun commonStatItem(
        id: String,
        displayName: String,
        description: String,
        statType: StatType
    ): ItemDefinition {
        return ItemDefinition(
            id =
                ContentId(id),

            displayName =
                displayName,

            description =
                description,

            tags =
                setOf(
                    ContentTag.PASSIVE_ITEM
                ),

            rarity =
                ItemRarity.COMMON,

            statModifiers =
                listOf(
                    StatModifier(
                        statType =
                            statType,

                        amount = 1
                    )
                ),

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )
    }

    private fun effectItem(
        id: String,
        displayName: String,
        description: String,
        rarity: ItemRarity,
        effectId: ItemEffectId? = null,
        statModifiers: List<StatModifier> = emptyList(),
        maximumInventoryCopies: Int? = null,
        sharedCopyLimitGroup: String? = null,
        sharedCopyLimit: Int? = null
    ) = ItemDefinition(
        id = ContentId(id),
        displayName = displayName,
        description = description,
        rarity = rarity,
        statModifiers = statModifiers,
        effectId = effectId,
        maximumInventoryCopies = maximumInventoryCopies,
        sharedCopyLimitGroup = sharedCopyLimitGroup,
        sharedCopyLimit = sharedCopyLimit
    )

    val obsidianWardPendant = commonStatItem(
        id = "ITEM_OBSIDIAN_WARD_PENDANT",
        displayName = "Obsidian Ward Pendant",
        description = "Increases Magic Resistance by 1.",
        statType = StatType.MAGIC_RESISTANCE
    )

    val spellScroll =
        commonStatItem(
            id =
                "ITEM_SPELL_SCROLL",

            displayName =
                "Spell Scroll",

            description =
                "Increases Wisdom by 1.",

            statType =
                StatType.WISDOM
        )

    val windBoots =
        commonStatItem(
            id =
                "ITEM_WIND_BOOTS",

            displayName =
                "Wind Boots",

            description =
                "Increases Movement Speed by 1.",

            statType =
                StatType.MOVEMENT_SPEED
        )

    val vampireFangs =
        commonStatItem(
            id =
                "ITEM_VAMPIRE_FANGS",

            displayName =
                "Vampire Fangs",

            description =
                "Increases Life Steal by 1.",

            statType =
                StatType.LIFE_STEAL
        )

    val luckyDice =
        commonStatItem(
            id =
                "ITEM_LUCKY_DICE",

            displayName =
                "Lucky Dice",

            description =
                "Increases Luck by 1.",

            statType =
                StatType.LUCK
        )

    val gladiatorsLocket =
        commonStatItem(
            id =
                "ITEM_GLADIATORS_LOCKET",

            displayName =
                "Gladiator's Locket",

            description =
                "Increases Strength by 1.",

            statType =
                StatType.STRENGTH
        )

    val lordBledasBanner =
        commonStatItem(
            id =
                "ITEM_LORD_BLEDAS_BANNER",

            displayName =
                "Lord Bleda's Banner",

            description =
                "Increases Attack Speed by 1.",

            statType =
                StatType.ATTACK_SPEED
        )

    val hourglass =
        commonStatItem(
            id =
                "ITEM_HOURGLASS",

            displayName =
                "Hourglass",

            description =
                "Increases Haste by 1.",

            statType =
                StatType.HASTE
        )

    val hillGiantsBelt =
        commonStatItem(
            id =
                "ITEM_HILL_GIANTS_BELT",

            displayName =
                "Hill Giant's Belt",

            description =
                "Increases Max HP by 1.",

            statType =
                StatType.MAX_HP
        )

    val chainMail =
        commonStatItem(
            id =
                "ITEM_CHAIN_MAIL",

            displayName =
                "Chain Mail",

            description =
                "Increases Armor Class by 1.",

            statType =
                StatType.ARMOR_CLASS
        )

    val sandals =
        commonStatItem(
            id =
                "ITEM_SANDALS",

            displayName =
                "Sandals",

            description =
                "Increases Dexterity by 1.",

            statType =
                StatType.DEXTERITY
        )

    val kepenek =
        commonStatItem(
            id =
                "ITEM_KEPENEK",

            displayName =
                "Kepenek",

            description =
                "Increases Shepherd by 1.",

            statType =
                StatType.SHEPHERD
        )

    val whetstone =
        commonStatItem(
            id =
                "ITEM_WHETSTONE",

            displayName =
                "Whetstone",

            description =
                "Increases Piercing by 1.",

            statType =
                StatType.PIERCING
        )

    val warHorn =
        commonStatItem(
            id =
                "ITEM_WAR_HORN",

            displayName =
                "War Horn",

            description =
                "Increases Enemy Count by 1.",

            statType =
                StatType.ENEMY_COUNT
        )

    val goddessSelinusAmulet =
        commonStatItem(
            id =
                "ITEM_GODDESS_SELINUS_AMULET",

            displayName =
                "Goddess Selinus' Amulet",

            description =
                "Increases HP Regeneration by 1.",

            statType =
                StatType.HP_REGENERATION
        )

    val copperRod =
        commonStatItem(
            id =
                "ITEM_COPPER_ROD",

            displayName =
                "Copper Rod",

            description =
                "Increases Might of Nature by 1.",

            statType =
                StatType.MIGHT_OF_NATURE
        )

    val amuletOfAzuth =
        commonStatItem(
            id =
                "ITEM_AMULET_OF_AZUTH",

            displayName =
                "Amulet of Azuth",

            description =
                "Increases Elemental Damage by 1.",

            statType =
                StatType.ELEMENTAL_DAMAGE
        )

    val griffinFeather =
        commonStatItem(
            id =
                "ITEM_GRIFFIN_FEATHER",

            displayName =
                "Griffin Feather",

            description =
                "Increases Harvest by 1.",

            statType =
                StatType.HARVEST
        )

    val warmastersOrder =
        commonStatItem(
            id =
                "ITEM_WARMASTERS_ORDER",

            displayName =
                "Warmaster's Order",

            description =
                "Increases Crit Rate by 1.",

            statType =
                StatType.CRIT_RATE
        )

    val heavyArrow =
        commonStatItem(
            id =
                "ITEM_HEAVY_ARROW",

            displayName =
                "Heavy Arrow",

            description =
                "Increases Crit Damage by 1.",

            statType =
                StatType.CRIT_DAMAGE
        )

    val executionersMark = effectItem(
        "ITEM_EXECUTIONERS_MARK",
        "Executioner's Mark",
        "Deals 5% more damage to normal and unique enemies below 15% health, and bosses below 7.5% health.",
        ItemRarity.LEGENDARY,
        ItemEffectId.EXECUTIONERS_MARK
    )

    val glassHeart = effectItem(
        "ITEM_GLASS_HEART",
        "Glass Heart",
        "Reduces maximum health by 60% and increases the character's main damage stat by 35%.",
        ItemRarity.LEGENDARY,
        ItemEffectId.GLASS_HEART
    )

    val heavySoul = effectItem(
        "ITEM_HEAVY_SOUL",
        "Heavy Soul",
        "Reduces Movement Speed by 5 and increases Armor Class by 5.",
        ItemRarity.RARE,
        statModifiers = listOf(
            StatModifier(StatType.MOVEMENT_SPEED, -5),
            StatModifier(StatType.ARMOR_CLASS, 5)
        )
    )

    val cruelPrecision = effectItem(
        "ITEM_CRUEL_PRECISION",
        "Cruel Precision",
        "Reduces Crit Rate by 3 and increases Crit Damage by 5.",
        ItemRarity.RARE,
        statModifiers = listOf(
            StatModifier(StatType.CRIT_RATE, -3),
            StatModifier(StatType.CRIT_DAMAGE, 5)
        )
    )

    val longShot = effectItem(
        "ITEM_LONG_SHOT",
        "Long Shot",
        "Ranged projectiles gain up to 10% damage based on distance travelled.",
        ItemRarity.ARTIFACT,
        ItemEffectId.LONG_SHOT
    )

    val specialist = effectItem(
        "ITEM_SPECIALIST",
        "Specialist",
        "Your most-used magical damage type deals 7% more damage; the others deal 10% less.",
        ItemRarity.ARTIFACT,
        ItemEffectId.SPECIALIST
    )

    val chaosTheory = effectItem(
        "ITEM_CHAOS_THEORY",
        "Chaos Theory",
        "Magical damage has a stacking 20% chance per copy to change into another magical damage type.",
        ItemRarity.RARE,
        ItemEffectId.CHAOS_THEORY
    )

    val summonersPact = effectItem(
        "ITEM_SUMMONERS_PACT",
        "Summoner's Pact",
        "Summons gain 10% damage and health, while the player's damage is reduced by 25%.",
        ItemRarity.RARE,
        ItemEffectId.SUMMONERS_PACT
    )

    val bloodMoney = effectItem(
        "ITEM_BLOOD_MONEY",
        "Blood Money",
        "Gold rewards have a 10% chance to double, but enemies deal 20% more damage.",
        ItemRarity.RARE,
        ItemEffectId.BLOOD_MONEY
    )

    val greedySoul = effectItem(
        "ITEM_GREEDY_SOUL",
        "Greedy Soul",
        "Increases Pickup Range by 25% and reduces Movement Speed by 2.",
        ItemRarity.RARE,
        ItemEffectId.GREEDY_SOUL,
        statModifiers = listOf(
            StatModifier(StatType.MOVEMENT_SPEED, -2)
        )
    )

    val rightInvestment = effectItem(
        "ITEM_RIGHT_INVESTMENT",
        "Right Investment",
        "Deals 3% more damage for every 100 Gold Coin held.",
        ItemRarity.LEGENDARY,
        ItemEffectId.RIGHT_INVESTMENT
    )

    val noMercy = effectItem(
        "ITEM_NO_MERCY",
        "No Mercy",
        "Deals 5% more damage, but all healing is reduced by 75%.",
        ItemRarity.RARE,
        ItemEffectId.NO_MERCY
    )

    val compressedSpace = effectItem(
        "ITEM_COMPRESSED_SPACE",
        "Compressed Space",
        "Reduces Weapon Range by 25% and increases damage by 8%.",
        ItemRarity.LEGENDARY,
        ItemEffectId.COMPRESSED_SPACE
    )

    val repulsion = effectItem(
        "ITEM_REPULSION",
        "Repulsion",
        "Increases knockback by 20% and reduces damage by 5%.",
        ItemRarity.RARE,
        ItemEffectId.REPULSION
    )

    val collosusHunter = effectItem(
        "ITEM_COLLOSUS_HUNTER",
        "Collosus Hunter",
        "Deals 4% more damage to bosses and 4% less damage to normal enemies.",
        ItemRarity.RARE,
        ItemEffectId.COLLOSUS_HUNTER
    )

    val massMurderer = effectItem(
        "ITEM_MASS_MURDERER",
        "Mass Murderer",
        "Deals 4% more damage to normal enemies and 4% less damage to bosses.",
        ItemRarity.RARE,
        ItemEffectId.MASS_MURDERER
    )

    val oneWeaponArmy = effectItem(
        "ITEM_ONE_WEAPON_ARMY",
        "One Weapon Army",
        "When only one weapon slot is occupied, that weapon deals 7.5 times its normal damage.",
        ItemRarity.ARTIFACT,
        ItemEffectId.ONE_WEAPON_ARMY
    )

    val arsenal = effectItem(
        "ITEM_ARSENAL",
        "Arsenal",
        "All weapons deal 11% more damage for every different weapon owned.",
        ItemRarity.LEGENDARY,
        ItemEffectId.ARSENAL
    )

    val jealousWeapon = effectItem(
        "ITEM_JEALOUS_WEAPON",
        "Jealous Weapon",
        "One highest-level weapon gains 7% damage while the others lose 7%.",
        ItemRarity.LEGENDARY,
        ItemEffectId.JEALOUS_WEAPON
    )

    val weakestLink = effectItem(
        "ITEM_WEAKEST_LINK",
        "Weakest Link",
        "A uniquely lowest-level weapon deals 45% more damage.",
        ItemRarity.LEGENDARY,
        ItemEffectId.WEAKEST_LINK
    )

    val mainSlot = effectItem(
        "ITEM_MAIN_SLOT",
        "Main Slot",
        "The weapon in slot 1 deals 1.5% more damage.",
        ItemRarity.RARE,
        ItemEffectId.MAIN_SLOT
    )

    val oddity = effectItem(
        "ITEM_ODDITY",
        "Oddity",
        "Odd-numbered weapon slots gain 1% Crit Chance; even-numbered slots gain 1% Attack Speed.",
        ItemRarity.RARE,
        ItemEffectId.ODDITY
    )

    val bookOfExpertize = effectItem(
        "ITEM_BOOK_OF_EXPERTIZE",
        "Book of Expertize",
        "Raises the maximum value of the character's main damage stat by 10.",
        ItemRarity.ARTIFACT,
        ItemEffectId.BOOK_OF_EXPERTIZE
    )

    val collector = effectItem(
        "ITEM_COLLECTOR",
        "Collector",
        "Deals 0.1% more damage for every different Item and Tamga owned.",
        ItemRarity.RARE,
        ItemEffectId.COLLECTOR
    )

    private fun conversionItem(
        id: String,
        displayName: String,
        description: String,
        effectId: ItemEffectId
    ) = effectItem(
        id,
        displayName,
        description,
        ItemRarity.LEGENDARY,
        effectId,
        sharedCopyLimitGroup = "CONVERSION",
        sharedCopyLimit = 3
    )

    val conversionHealth = conversionItem(
        "ITEM_CONVERSION_HEALTH",
        "Conversion: Health",
        "Converts 20% of Max HP into Strength when acquired.",
        ItemEffectId.CONVERSION_HEALTH
    )

    val conversionArmorClass = conversionItem(
        "ITEM_CONVERSION_ARMOR_CLASS",
        "Conversion: Armor Class",
        "Converts 20% of Armor Class into Strength when acquired.",
        ItemEffectId.CONVERSION_ARMOR_CLASS
    )

    val conversionShepherd = conversionItem(
        "ITEM_CONVERSION_SHEPHERD",
        "Conversion: Shepherd",
        "Converts 20% of Shepherd into Wisdom when acquired.",
        ItemEffectId.CONVERSION_SHEPHERD
    )

    val conversionAttackSpeed = conversionItem(
        "ITEM_CONVERSION_ATTACK_SPEED",
        "Conversion: Attack Speed",
        "Converts 20% of Attack Speed into Dexterity when acquired.",
        ItemEffectId.CONVERSION_ATTACK_SPEED
    )

    val conversionMovementSpeed = conversionItem(
        "ITEM_CONVERSION_MOVEMENT_SPEED",
        "Conversion: Movement Speed",
        "Converts 20% of Movement Speed into Dexterity when acquired.",
        ItemEffectId.CONVERSION_MOVEMENT_SPEED
    )

    val conversionEnemyCount = conversionItem(
        "ITEM_CONVERSION_ENEMY_COUNT",
        "Conversion: Enemy Count",
        "Converts 20% of Enemy Count into Piercing when acquired.",
        ItemEffectId.CONVERSION_ENEMY_COUNT
    )

    val jackOfAllTrades = effectItem(
        "ITEM_JACK_OF_ALL_TRADES",
        "Jack of All Trades",
        "Deals 3.5% more damage for every 100 total Stat points.",
        ItemRarity.ARTIFACT,
        ItemEffectId.JACK_OF_ALL_TRADES
    )

    val mutualDestruction = effectItem(
        "ITEM_MUTUAL_DESTRUCTION",
        "Mutual Destruction",
        "The player and enemies deal 3% more damage.",
        ItemRarity.RARE,
        ItemEffectId.MUTUAL_DESTRUCTION
    )

    val cursedMark = effectItem(
        "ITEM_CURSED_MARK",
        "Cursed Mark",
        "Increases Enemy Count by 2.",
        ItemRarity.RARE,
        statModifiers = listOf(
            StatModifier(StatType.ENEMY_COUNT, 2)
        )
    )

    val bloodSacrifice = effectItem(
        "ITEM_BLOOD_SACRIFICE", "Blood Sacrifice",
        "Weapon Skills have a 50% chance to cost half of current health and skip their cooldown. Cannot kill the player.",
        ItemRarity.LEGENDARY, ItemEffectId.BLOOD_SACRIFICE,
        maximumInventoryCopies = 1
    )

    val deepWounds = effectItem(
        "ITEM_DEEP_WOUNDS", "Deep Wounds",
        "Below 20% health, Attack Speed, Movement Speed and Crit Rate increase by 2.",
        ItemRarity.RARE, ItemEffectId.DEEP_WOUNDS
    )

    val secondHeart = effectItem(
        "ITEM_SECOND_HEART", "Second Heart",
        "Lethal damage consumes one copy, leaves you at 1 health and grants 3 seconds of invulnerability.",
        ItemRarity.LEGENDARY, ItemEffectId.SECOND_HEART,
        sharedCopyLimitGroup = "SECOND_HEART",
        sharedCopyLimit = 3
    )

    val brokenHeart = effectItem(
        "ITEM_BROKEN_HEART", "Broken Heart",
        "A spent Second Heart. It has no effect and cannot appear as a reward.",
        ItemRarity.LEGENDARY, ItemEffectId.BROKEN_HEART,
        sharedCopyLimitGroup = "SECOND_HEART",
        sharedCopyLimit = 3
    )

    val crowdHunger = effectItem(
        "ITEM_CROWD_HUNGER", "Crowd Hunger",
        "Deals 1% more damage per nearby enemy, up to 5% per copy.",
        ItemRarity.LEGENDARY, ItemEffectId.CROWD_HUNGER
    )

    val huntersReward = effectItem(
        "ITEM_HUNTERS_REWARD", "Hunter's Reward",
        "Bosses defeated after acquisition permanently grant 5% damage, up to 25%.",
        ItemRarity.ARTIFACT, ItemEffectId.HUNTERS_REWARD
    )

    val ghostProjectile = effectItem(
        "ITEM_GHOST_PROJECTILE", "Ghost Projectile",
        "Ranged projectiles have a 40% chance to pass through obstacles.",
        ItemRarity.RARE, ItemEffectId.GHOST_PROJECTILE,
        maximumInventoryCopies = 1
    )

    val criticalMomentum = effectItem(
        "ITEM_CRITICAL_MOMENTUM", "Critical Momentum",
        "Critical hits grant 1 Movement Speed for 5 seconds.",
        ItemRarity.RARE, ItemEffectId.CRITICAL_MOMENTUM
    )

    val sharedBlood = effectItem(
        "ITEM_SHARED_BLOOD", "Shared Blood",
        "Transfers 10% of incoming damage to living summons, divided equally.",
        ItemRarity.LEGENDARY, ItemEffectId.SHARED_BLOOD
    )

    val sharedFury = effectItem(
        "ITEM_SHARED_FURY", "Shared Fury",
        "When a summon dies, gain 1 Haste and 1 Wisdom for 5 seconds.",
        ItemRarity.RARE, ItemEffectId.SHARED_FURY
    )

    val summonedLegacy = effectItem(
        "ITEM_SUMMONED_LEGACY", "Summoned Legacy",
        "Dead summons leave a 150 px area for 8 seconds that grants 12% damage while inside.",
        ItemRarity.ARTIFACT, ItemEffectId.SUMMONED_LEGACY
    )

    val overflowingPower = effectItem(
        "ITEM_OVERFLOWING_POWER", "Overflowing Power",
        "A ready Weapon Skill gains 1% damage per unused second, up to 15% on its next use.",
        ItemRarity.ARTIFACT, ItemEffectId.OVERFLOWING_POWER
    )

    val thorns = effectItem(
        "ITEM_THORNS", "Thorns",
        "Contact damage reflects damage based on Armor Class.",
        ItemRarity.RARE, ItemEffectId.THORNS
    )

    val closeCall = effectItem(
        "ITEM_CLOSE_CALL", "Close Call",
        "An enemy projectile passing within 25 px without hitting grants 2 Crit Rate for 3 seconds.",
        ItemRarity.RARE, ItemEffectId.CLOSE_CALL
    )

    val fearOfDeath = effectItem(
        "ITEM_FEAR_OF_DEATH", "Fear of Death",
        "Elite or Boss deaths have a stacking 30% chance per copy to Frighten nearby normal enemies for 3 seconds.",
        ItemRarity.LEGENDARY, ItemEffectId.FEAR_OF_DEATH
    )

    val bloodScent = effectItem(
        "ITEM_BLOOD_SCENT", "Blood Scent",
        "Gain 1 Movement Speed while moving toward an enemy below 40% health.",
        ItemRarity.RARE, ItemEffectId.BLOOD_SCENT
    )

    val extraReward = effectItem(
        "ITEM_EXTRA_REWARD", "Extra Reward",
        "Picking up a Chest heals 6% of maximum health.",
        ItemRarity.RARE, ItemEffectId.EXTRA_REWARD
    )

    val gambler = effectItem(
        "ITEM_GAMBLER", "Gambler",
        "At each stage start, one eligible random Stat gains 1 and another loses 1.",
        ItemRarity.RARE, ItemEffectId.GAMBLER
    )

    val cashOut = effectItem(
        "ITEM_CASH_OUT", "Cash Out",
        "On acquisition, removes all other Items and permanently grants 1% damage per removed Item.",
        ItemRarity.LEGENDARY, ItemEffectId.CASH_OUT
    )

    val hitMeHarder = effectItem(
        "ITEM_HIT_ME_HARDER", "Hit Me Harder",
        "Each attack taken grants 3.1% damage, stacking up to 10 times.",
        ItemRarity.RARE, ItemEffectId.HIT_ME_HARDER,
        maximumInventoryCopies = 1
    )

    val predestination = effectItem(
        "ITEM_PREDESTINATION", "Predestination",
        "Each stage empowers one random Damage Type by 2.5%.",
        ItemRarity.RARE, ItemEffectId.PREDESTINATION
    )

    val bounty = effectItem(
        "ITEM_BOUNTY", "Bounty",
        "Each stage marks one enemy native to the current map; its Gold reward is multiplied by 1.5, or by 3 for bosses.",
        ItemRarity.LEGENDARY, ItemEffectId.BOUNTY,
        maximumInventoryCopies = 1
    )

    val bestiaryHunter = effectItem(
        "ITEM_BESTIARY_HUNTER", "Bestiary Hunter",
        "The first kill of each enemy kind grants a random Stat; boss kinds grant 2 instead of 1.",
        ItemRarity.RARE, ItemEffectId.BESTIARY_HUNTER
    )

    val bossTrophy = effectItem(
        "ITEM_BOSS_TROPHY", "Boss Trophy",
        "Each Boss kill permanently grants 1 Strength, Dexterity or Wisdom at random.",
        ItemRarity.LEGENDARY, ItemEffectId.BOSS_TROPHY
    )

    val littleRisk = effectItem(
        "ITEM_LITTLE_RISK", "Little Risk",
        "Adds 0.5 percentage points to Unique frequency and 2 points to Mini Boss Grisilith Coin chance per copy.",
        ItemRarity.RARE, ItemEffectId.LITTLE_RISK
    )

    val blindFaith = effectItem(
        "ITEM_BLIND_FAITH", "Blind Faith",
        "Deals 6% more damage per copy and hides enemy health bars. Mist begins at two copies and grows with further copies.",
        ItemRarity.RARE, ItemEffectId.BLIND_FAITH,
        maximumInventoryCopies = 5
    )

    val animatedBroom = effectItem(
        "ITEM_ANIMATED_BROOM", "Animated Broom",
        "Animates one untargetable broom per copy to collect Gold and Grisilith Coins.",
        ItemRarity.RARE, ItemEffectId.ANIMATED_BROOM
    )

    val callThePortal = effectItem(
        "ITEM_CALL_THE_PORTAL", "Call the Portal",
        "Adds a 1% chance per copy for an exit portal to appear halfway through a normal stage.",
        ItemRarity.LEGENDARY, ItemEffectId.CALL_THE_PORTAL
    )

    val isItMerchant = effectItem(
        "ITEM_IS_IT_MERCHANT", "Is It Merchant?",
        "Has a 5% chance per copy each stage to reveal a merchant that guarantees a market after the stage.",
        ItemRarity.LEGENDARY, ItemEffectId.IS_IT_MERCHANT
    )

    val pitTrap = effectItem(
        "ITEM_PIT_TRAP", "Pit Trap",
        "Standing still prepares a pit that executes up to four grounded normal enemies and releases their Gold when full.",
        ItemRarity.LEGENDARY, ItemEffectId.PIT_TRAP
    )

    val lootTheEnemies = effectItem(
        "ITEM_LOOT_THE_ENEMIES", "Loot the Enemies",
        "Enemies have a 5% loot chance per copy. Loot heals 10% maximum health, but Movement Speed is reduced by 1 per copy.",
        ItemRarity.RARE, ItemEffectId.LOOT_THE_ENEMIES,
        statModifiers = listOf(StatModifier(StatType.MOVEMENT_SPEED, -1)),
        maximumInventoryCopies = 5
    )

    val boneTotems = effectItem(
        "ITEM_BONE_TOTEMS", "Bone Totems",
        "Stages have a 35% chance to contain a chargeable Bone Totem that rolls one reward from the owned Totem List.",
        ItemRarity.RARE, ItemEffectId.BONE_TOTEMS,
        maximumInventoryCopies = 1
    )

    val boneTotemHealingAura = effectItem(
        "ITEM_BONE_TOTEM_HEALING_AURA", "Bone Totems List: Healing Aura",
        "Adds Healing Aura to the Bone Totem reward pool.",
        ItemRarity.RARE, ItemEffectId.BONE_TOTEM_HEALING_AURA,
        maximumInventoryCopies = 1
    )

    val boneTotemProtectersArea = effectItem(
        "ITEM_BONE_TOTEM_PROTECTERS_AREA", "Bone Totems List: Protecters Area",
        "Adds Protecters Area to the Bone Totem reward pool.",
        ItemRarity.RARE, ItemEffectId.BONE_TOTEM_PROTECTERS_AREA,
        maximumInventoryCopies = 1
    )

    val boneTotemBurriedTreasure = effectItem(
        "ITEM_BONE_TOTEM_BURRIED_TREASURE", "Bone Totems List: Burried Treasure",
        "Adds Burried Treasure to the Bone Totem reward pool.",
        ItemRarity.RARE, ItemEffectId.BONE_TOTEM_BURRIED_TREASURE,
        maximumInventoryCopies = 1
    )

    val boneTotemHourglass = effectItem(
        "ITEM_BONE_TOTEM_HOURGLASS", "Bone Totems List: Hourglass",
        "Adds Hourglass to the Bone Totem reward pool.",
        ItemRarity.RARE, ItemEffectId.BONE_TOTEM_HOURGLASS,
        maximumInventoryCopies = 1
    )

    val boneTotemToads = effectItem(
        "ITEM_BONE_TOTEM_TOADS", "Bone Totems List: Toads",
        "Adds Toads to the Bone Totem reward pool.",
        ItemRarity.RARE, ItemEffectId.BONE_TOTEM_TOADS,
        maximumInventoryCopies = 1
    )

    val all:
        List<ItemDefinition> =
        listOf(
            spellScroll,
            obsidianWardPendant,
            windBoots,
            vampireFangs,
            luckyDice,
            gladiatorsLocket,
            lordBledasBanner,
            hourglass,
            hillGiantsBelt,
            chainMail,
            sandals,
            kepenek,
            whetstone,
            warHorn,
            goddessSelinusAmulet,
            copperRod,
            amuletOfAzuth,
            griffinFeather,
            warmastersOrder,
            heavyArrow,
            executionersMark,
            glassHeart,
            heavySoul,
            cruelPrecision,
            longShot,
            specialist,
            chaosTheory,
            summonersPact,
            bloodMoney,
            greedySoul,
            rightInvestment,
            noMercy,
            compressedSpace,
            repulsion,
            collosusHunter,
            massMurderer,
            oneWeaponArmy,
            arsenal,
            jealousWeapon,
            weakestLink,
            mainSlot,
            oddity,
            bookOfExpertize,
            collector,
            conversionHealth,
            conversionArmorClass,
            conversionShepherd,
            conversionAttackSpeed,
            conversionMovementSpeed,
            conversionEnemyCount,
            jackOfAllTrades,
            mutualDestruction,
            cursedMark,
            bloodSacrifice,
            deepWounds,
            secondHeart,
            brokenHeart,
            crowdHunger,
            huntersReward,
            ghostProjectile,
            criticalMomentum,
            sharedBlood,
            sharedFury,
            summonedLegacy,
            overflowingPower,
            thorns,
            closeCall,
            fearOfDeath,
            bloodScent,
            extraReward,
            gambler,
            cashOut,
            hitMeHarder,
            predestination,
            bounty,
            bestiaryHunter,
            bossTrophy,
            littleRisk,
            blindFaith,
            animatedBroom,
            callThePortal,
            isItMerchant,
            pitTrap,
            lootTheEnemies,
            boneTotems,
            boneTotemHealingAura,
            boneTotemProtectersArea,
            boneTotemBurriedTreasure,
            boneTotemHourglass,
            boneTotemToads
        )
}
