package com.azizoglu.grisilith.item.runtime

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.enemy.EnemyDamageContext
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.item.definition.ItemDefinition
import com.azizoglu.grisilith.item.definition.ItemEffectId
import com.azizoglu.grisilith.item.definition.DefaultItems
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.summon.SummonedEntity
import com.azizoglu.grisilith.weapon.runtime.WeaponInstance
import com.azizoglu.grisilith.weapon.runtime.WeaponInventory
import com.azizoglu.grisilith.save.RunSaveItemEffectState
import java.util.HashSet
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

/**
 * The item inventory is run-scoped. This small binding makes dynamic item
 * effects available to the existing combat paths without copying their rules
 * into every weapon executor.
 */
object ActiveItemEffects {
    private var inventory: ItemInventory? = null
    private var weapons: WeaponInventory? = null
    private var stats: PlayerStats? = null
    private var goldProvider: () -> Int = { 0 }
    private var mainStatProvider: () -> StatType = { StatType.STRENGTH }
    private var tamgaCountProvider: () -> Int = { 0 }
    private var player: Player? = null
    private var enemyProvider: () -> List<Enemy> = { emptyList() }
    private var summonProvider: () -> List<SummonedEntity> = { emptyList() }
    private var currentStageProvider: () -> Int = { 1 }

    private var lastStage = -1
    private var secondHeartAvailable = true
    private var invulnerabilityRemaining = 0f
    private var criticalMomentumRemaining = 0f
    private var sharedFuryRemaining = 0f
    private var closeCallRemaining = 0f
    private var hunterRewardBossKills = 0
    private var cashOutConsumedItems = 0
    private var hitMeHarderStacks = 0
    private var predestinationDamageType: DamageType? = null
    private var bountyEnemyType: EnemyType? = null
    private val bestiaryKinds = mutableSetOf<String>()
    private val abilityReadySeconds = mutableMapOf<String, Float>()
    private val abilityDamageBonuses = mutableMapOf<String, Float>()
    private val abilityBonusRemaining = mutableMapOf<String, Float>()
    private val processedSummonDeaths = mutableSetOf<String>()
    private val legacyZones = mutableListOf<LegacyZone>()
    private var previousPlayerX = 0f
    private var previousPlayerY = 0f
    private var movingTowardWoundedEnemy = false

    private data class LegacyZone(
        val x: Float,
        val y: Float,
        var remainingSeconds: Float
    )

    private val magicalDamageTypes =
        setOf(
            DamageType.FIRE,
            DamageType.WATER,
            DamageType.AIR,
            DamageType.LIGHTNING,
            DamageType.ACID,
            DamageType.NECROTIC,
            DamageType.POISON
        )

    private val magicalDamageDealt =
        mutableMapOf<DamageType, Long>()

    private var jealousSignature = ""
    private var jealousWeaponRuntimeId: String? = null

    fun bind(
        inventory: ItemInventory,
        weaponInventory: WeaponInventory,
        playerStats: PlayerStats,
        player: Player,
        enemyProvider: () -> List<Enemy>,
        summonProvider: () -> List<SummonedEntity>,
        currentStageProvider: () -> Int,
        goldProvider: () -> Int,
        mainStatProvider: () -> StatType,
        tamgaCountProvider: () -> Int
    ) {
        this.inventory = inventory
        weapons = weaponInventory
        stats = playerStats
        this.player = player
        this.enemyProvider = enemyProvider
        this.summonProvider = summonProvider
        this.currentStageProvider = currentStageProvider
        this.goldProvider = goldProvider
        this.mainStatProvider = mainStatProvider
        this.tamgaCountProvider = tamgaCountProvider
        magicalDamageDealt.clear()
        jealousSignature = ""
        jealousWeaponRuntimeId = null
        lastStage = -1
        secondHeartAvailable = true
        invulnerabilityRemaining = 0f
        criticalMomentumRemaining = 0f
        sharedFuryRemaining = 0f
        closeCallRemaining = 0f
        hunterRewardBossKills = 0
        cashOutConsumedItems = 0
        hitMeHarderStacks = 0
        predestinationDamageType = null
        bountyEnemyType = null
        bestiaryKinds.clear()
        abilityReadySeconds.clear()
        abilityDamageBonuses.clear()
        abilityBonusRemaining.clear()
        processedSummonDeaths.clear()
        legacyZones.clear()
        previousPlayerX = player.centerX
        previousPlayerY = player.centerY
    }

    fun unbind(playerStats: PlayerStats) {
        if (stats !== playerStats) return
        inventory = null
        weapons = null
        stats = null
        player = null
        magicalDamageDealt.clear()
    }

    fun isBoundTo(playerStats: PlayerStats): Boolean =
        stats === playerStats

    fun count(effectId: ItemEffectId): Int =
        inventory
            ?.allItems()
            ?.count { it.definition.effectId == effectId }
            ?: 0

    fun currentShepherdScore(): Int =
        stats?.totalScore(StatType.SHEPHERD) ?: 0

    fun resolveDamageContext(context: EnemyDamageContext): EnemyDamageContext {
        val copies = count(ItemEffectId.CHAOS_THEORY)
        if (copies <= 0 || context.damageType !in magicalDamageTypes) return context

        val conversionChance = (0.20 * copies).coerceAtMost(1.0)
        if (Random.nextDouble() >= conversionChance) return context

        val alternatives = magicalDamageTypes - context.damageType
        return context.copy(damageType = alternatives.random())
    }

    fun modifyOutgoingDamage(
        amount: Int,
        enemy: Enemy,
        context: EnemyDamageContext
    ): Int {
        if (amount <= 0 || inventory == null) return amount

        var multiplier = 1.0
        multiplier *= 1.0 + 0.06 * count(ItemEffectId.BLIND_FAITH)
        val sourceWeapon = weapon(context.sourceWeaponRuntimeId)
        val sourceTags =
            context.sourceWeaponTags.ifEmpty {
                sourceWeapon?.definition?.tags ?: emptySet()
            }

        val summonersPact = count(ItemEffectId.SUMMONERS_PACT)
        multiplier *= if (context.sourceIsSummon) {
            1.0 + 0.10 * summonersPact
        } else {
            (1.0 - 0.25 * summonersPact).coerceAtLeast(0.0)
        }

        multiplier *= 1.0 + 0.03 * count(ItemEffectId.RIGHT_INVESTMENT) *
            floor(goldProvider().coerceAtLeast(0) / 100.0)
        multiplier *= 1.0 + 0.05 * count(ItemEffectId.NO_MERCY)
        multiplier *= 1.08.pow(count(ItemEffectId.COMPRESSED_SPACE))
        multiplier *= 0.95.pow(count(ItemEffectId.REPULSION))
        multiplier *= 1.03.pow(count(ItemEffectId.MUTUAL_DESTRUCTION))
        if (count(ItemEffectId.HUNTERS_REWARD) > 0) {
            multiplier *= 1.0 + 0.05 * hunterRewardBossKills.coerceAtMost(5)
        }
        multiplier *= 1.0 + 0.01 * cashOutConsumedItems
        if (count(ItemEffectId.HIT_ME_HARDER) > 0) {
            multiplier *= 1.0 + 0.031 * hitMeHarderStacks.coerceAtMost(10)
        }

        val crowdCopies = count(ItemEffectId.CROWD_HUNGER)
        if (crowdCopies > 0) {
            val nearbyCount = enemyProvider().count { nearby ->
                !nearby.isDead && distanceSquared(
                    nearby.centerX,
                    nearby.centerY,
                    player?.centerX ?: 0f,
                    player?.centerY ?: 0f
                ) <= CROWD_HUNGER_RADIUS * CROWD_HUNGER_RADIUS
            }
            multiplier *= 1.0 + 0.01 * nearbyCount.coerceAtMost(5 * crowdCopies)
        }

        if (
            count(ItemEffectId.PREDESTINATION) > 0 &&
            context.damageType == predestinationDamageType
        ) {
            multiplier *= 1.0 + 0.025 * count(ItemEffectId.PREDESTINATION)
        }

        val activePlayer = player
        if (
            activePlayer != null &&
            legacyZones.any { zone ->
                distanceSquared(activePlayer.centerX, activePlayer.centerY, zone.x, zone.y) <=
                    LEGACY_RADIUS * LEGACY_RADIUS
            }
        ) {
            multiplier *= 1.12
        }

        val executioner = count(ItemEffectId.EXECUTIONERS_MARK)
        if (executioner > 0 && enemy.health.maximumHealth > 0) {
            val threshold =
                if (enemy.type == EnemyType.MINI_BOSS || enemy.type == EnemyType.FINAL_BOSS) 0.075
                else 0.15
            if (enemy.health.currentHealth.toDouble() / enemy.health.maximumHealth <= threshold) {
                multiplier *= 1.0 + 0.05 * executioner
            }
        }

        val isBoss = enemy.type == EnemyType.MINI_BOSS || enemy.type == EnemyType.FINAL_BOSS
        multiplier *= if (isBoss) {
            1.04.pow(count(ItemEffectId.COLLOSUS_HUNTER)) *
                0.96.pow(count(ItemEffectId.MASS_MURDERER))
        } else {
            0.96.pow(count(ItemEffectId.COLLOSUS_HUNTER)) *
                1.04.pow(count(ItemEffectId.MASS_MURDERER))
        }

        if (sourceWeapon != null) {
            multiplier *= weaponDamageMultiplier(
                weapon = sourceWeapon,
                context = context.copy(sourceWeaponTags = sourceTags)
            )
        }

        val collector = count(ItemEffectId.COLLECTOR)
        if (collector > 0) {
            val differentItems = inventory?.allItems()?.map { it.definition.id }?.distinct()?.size ?: 0
            multiplier *= 1.0 + (differentItems + tamgaCountProvider()) * 0.001 * collector
        }

        val jack = count(ItemEffectId.JACK_OF_ALL_TRADES)
        if (jack > 0) {
            val statTotal = stats?.let { playerStats -> StatType.entries.sumOf(playerStats::totalScore) } ?: 0
            multiplier *= 1.0 + statTotal / 100.0 * 0.035 * jack
        }

        if (
            count(ItemEffectId.SPECIALIST) > 0 &&
            ContentTag.SPELL_FOCUS in sourceTags &&
            context.damageType in magicalDamageTypes
        ) {
            val dominant = magicalDamageDealt.maxByOrNull { it.value }?.key ?: context.damageType
            multiplier *= if (dominant == context.damageType) 1.07 else 0.90
        }

        return (amount * multiplier).roundToInt().coerceAtLeast(if (multiplier > 0.0) 1 else 0)
    }

    private fun weaponDamageMultiplier(
        weapon: WeaponInstance,
        context: EnemyDamageContext
    ): Double {
        var multiplier = 1.0
        val equipped = weapons?.allWeapons().orEmpty()

        if (count(ItemEffectId.ONE_WEAPON_ARMY) > 0 && equipped.size == 1) {
            multiplier *= 7.5
        }

        multiplier *= 1.0 + equipped.map { it.definition.id }.distinct().size *
            0.11 * count(ItemEffectId.ARSENAL)

        val jealousCopies = count(ItemEffectId.JEALOUS_WEAPON)
        if (jealousCopies > 0) {
            multiplier *= if (weapon.runtimeId == jealousWeaponRuntimeId()) {
                1.0 + 0.07 * jealousCopies
            } else {
                (1.0 - 0.07 * jealousCopies).coerceAtLeast(0.0)
            }
        }

        val weakestCopies = count(ItemEffectId.WEAKEST_LINK)
        if (weakestCopies > 0) {
            val minimumLevel = equipped.minOfOrNull { it.level }
            val weakest = equipped.filter { it.level == minimumLevel }
            if (weakest.size == 1 && weakest.single().runtimeId == weapon.runtimeId) {
                multiplier *= 1.0 + 0.45 * weakestCopies
            }
        }

        val slotIndex = weapons?.weaponSlots?.indexOfFirst { it.weapon?.runtimeId == weapon.runtimeId } ?: -1
        if (slotIndex == 0) multiplier *= 1.0 + 0.015 * count(ItemEffectId.MAIN_SLOT)

        val longShot = count(ItemEffectId.LONG_SHOT)
        if (
            longShot > 0 &&
            ContentTag.RANGED_WEAPON in context.sourceWeaponTags &&
            ContentTag.SPELL_FOCUS !in context.sourceWeaponTags
        ) {
            multiplier *= 1.0 + 0.10 * context.projectileTravelProgress.coerceIn(0f, 1f) * longShot
        }

        return multiplier
    }

    private fun weapon(runtimeId: String?): WeaponInstance? =
        runtimeId?.let { id -> weapons?.allWeapons()?.firstOrNull { it.runtimeId == id } }

    private fun jealousWeaponRuntimeId(): String? {
        val equipped = weapons?.allWeapons().orEmpty()
        val signature = equipped.joinToString("|") { "${it.runtimeId}:${it.level}" }
        if (signature != jealousSignature) {
            jealousSignature = signature
            val maximumLevel = equipped.maxOfOrNull { it.level }
            jealousWeaponRuntimeId = equipped.filter { it.level == maximumLevel }.randomOrNull()?.runtimeId
        }
        return jealousWeaponRuntimeId
    }

    fun recordDamage(context: EnemyDamageContext, appliedDamage: Int) {
        if (appliedDamage <= 0 || context.damageType !in magicalDamageTypes) return
        magicalDamageDealt[context.damageType] =
            (magicalDamageDealt[context.damageType] ?: 0L) + appliedDamage
    }

    fun incomingDamageMultiplier(): Float =
        (1.20.pow(count(ItemEffectId.BLOOD_MONEY)) *
            1.03.pow(count(ItemEffectId.MUTUAL_DESTRUCTION))).toFloat()

    fun healingMultiplier(): Float =
        0.25.pow(count(ItemEffectId.NO_MERCY)).toFloat()

    fun maximumHealthMultiplier(playerStats: PlayerStats): Float =
        if (isBoundTo(playerStats)) 0.40.pow(count(ItemEffectId.GLASS_HEART)).toFloat() else 1f

    fun scoreMultiplier(playerStats: PlayerStats, statType: StatType): Float =
        if (isBoundTo(playerStats) && statType == mainStatProvider()) {
            (1.0 + 0.35 * count(ItemEffectId.GLASS_HEART)).toFloat()
        } else 1f

    fun scoreMaximum(playerStats: PlayerStats, statType: StatType): Int =
        if (isBoundTo(playerStats) && statType == mainStatProvider()) {
            PlayerStats.RUN_MAXIMUM_SCORE + 10 * count(ItemEffectId.BOOK_OF_EXPERTIZE)
        } else PlayerStats.RUN_MAXIMUM_SCORE

    fun pickupRangeMultiplier(): Float =
        (1.0 + 0.25 * count(ItemEffectId.GREEDY_SOUL)).toFloat()

    fun weaponRangeMultiplier(): Float =
        0.75.pow(count(ItemEffectId.COMPRESSED_SPACE)).toFloat()

    fun knockbackMultiplier(): Float =
        (1.0 + 0.20 * count(ItemEffectId.REPULSION)).toFloat()

    fun summonHealthMultiplier(playerStats: PlayerStats): Float =
        if (isBoundTo(playerStats)) {
            (1.0 + 0.10 * count(ItemEffectId.SUMMONERS_PACT)).toFloat()
        } else 1f

    fun modifyGoldReward(
        amount: Int,
        enemyType: EnemyType? = null
    ): Int {
        val copies = count(ItemEffectId.BLOOD_MONEY)
        val chance = 1.0 - 0.9.pow(copies)
        var result =
            if (copies > 0 && Random.nextDouble() < chance) amount * 2 else amount

        if (enemyType != null && enemyType == bountyEnemyType) {
            val bountyMultiplier =
                if (enemyType == EnemyType.MINI_BOSS || enemyType == EnemyType.FINAL_BOSS) 3f else 1.5f
            result = (result * bountyMultiplier).roundToInt()
        }
        return result
    }

    fun oddityCriticalChanceBonus(weapon: WeaponInstance?): Float {
        if (weapon == null) return 0f
        val slot = weapons?.weaponSlots?.indexOfFirst { it.weapon?.runtimeId == weapon.runtimeId } ?: -1
        return if (slot >= 0 && slot % 2 == 0) 0.01f * count(ItemEffectId.ODDITY) else 0f
    }

    fun oddityAttackSpeedMultiplier(weapon: WeaponInstance): Float {
        val slot = weapons?.weaponSlots?.indexOfFirst { it.weapon?.runtimeId == weapon.runtimeId } ?: -1
        return if (slot >= 0 && slot % 2 == 1) 1f + 0.01f * count(ItemEffectId.ODDITY) else 1f
    }

    fun conditionalScoreBonus(
        playerStats: PlayerStats,
        statType: StatType
    ): Int {
        if (!isBoundTo(playerStats)) return 0
        var bonus = 0
        val healthRatio = player?.health?.let {
            if (it.maximumHealth > 0) it.currentHealth.toFloat() / it.maximumHealth else 1f
        } ?: 1f

        if (healthRatio < 0.20f && statType in DEEP_WOUNDS_STATS) {
            bonus += 2 * count(ItemEffectId.DEEP_WOUNDS)
        }
        if (criticalMomentumRemaining > 0f && statType == StatType.MOVEMENT_SPEED) {
            bonus += count(ItemEffectId.CRITICAL_MOMENTUM)
        }
        if (sharedFuryRemaining > 0f && (statType == StatType.HASTE || statType == StatType.WISDOM)) {
            bonus += count(ItemEffectId.SHARED_FURY)
        }
        if (closeCallRemaining > 0f && statType == StatType.CRIT_RATE) {
            bonus += 2 * count(ItemEffectId.CLOSE_CALL)
        }
        if (movingTowardWoundedEnemy && statType == StatType.MOVEMENT_SPEED) {
            bonus += count(ItemEffectId.BLOOD_SCENT)
        }
        return bonus
    }

    fun update(delta: Float) {
        if (delta <= 0f) return
        invulnerabilityRemaining = (invulnerabilityRemaining - delta).coerceAtLeast(0f)
        criticalMomentumRemaining = (criticalMomentumRemaining - delta).coerceAtLeast(0f)
        sharedFuryRemaining = (sharedFuryRemaining - delta).coerceAtLeast(0f)
        closeCallRemaining = (closeCallRemaining - delta).coerceAtLeast(0f)
        legacyZones.forEach { it.remainingSeconds -= delta }
        legacyZones.removeAll { it.remainingSeconds <= 0f }
        abilityBonusRemaining.keys.toList().forEach { id ->
            val remaining = (abilityBonusRemaining[id] ?: 0f) - delta
            if (remaining <= 0f) {
                abilityBonusRemaining.remove(id)
                abilityDamageBonuses.remove(id)
            } else {
                abilityBonusRemaining[id] = remaining
            }
        }

        val stage = currentStageProvider().coerceAtLeast(1)
        if (stage != lastStage) {
            lastStage = stage
            beginStage()
        }

        updateBloodScentMovement()
    }

    private fun beginStage() {
        secondHeartAvailable = count(ItemEffectId.SECOND_HEART) > 0
        predestinationDamageType =
            if (count(ItemEffectId.PREDESTINATION) > 0) {
                DamageType.entries.filter { it != DamageType.NONE }.random()
            } else null
        bountyEnemyType =
            if (count(ItemEffectId.BOUNTY) > 0) EnemyType.entries.random() else null

        val gamblerCopies = count(ItemEffectId.GAMBLER)
        repeat(gamblerCopies) {
            val playerStats = stats ?: return@repeat
            val increaseCandidates = StatType.entries.filter { stat ->
                playerStats.totalScore(stat) <
                    playerStats.maximumScore(stat)
            }
            val increase = increaseCandidates.randomOrNull() ?: return@repeat
            val decreaseCandidates = StatType.entries.filter { stat ->
                stat != increase && playerStats.totalScore(stat) > PlayerStats.MINIMUM_SCORE
            }
            val decrease = decreaseCandidates.randomOrNull() ?: return@repeat
            playerStats.adjustRunBonus(increase, 1)
            playerStats.adjustRunBonus(decrease, -1)
        }
    }

    private fun updateBloodScentMovement() {
        val activePlayer = player ?: return
        val movementX = activePlayer.centerX - previousPlayerX
        val movementY = activePlayer.centerY - previousPlayerY
        previousPlayerX = activePlayer.centerX
        previousPlayerY = activePlayer.centerY
        movingTowardWoundedEnemy = false
        if (count(ItemEffectId.BLOOD_SCENT) <= 0 || movementX * movementX + movementY * movementY < 0.0001f) return

        movingTowardWoundedEnemy = enemyProvider().any { enemy ->
            if (enemy.isDead || enemy.health.maximumHealth <= 0 ||
                enemy.health.currentHealth.toFloat() / enemy.health.maximumHealth >= 0.40f) {
                false
            } else {
                val targetX = enemy.centerX - activePlayer.centerX
                val targetY = enemy.centerY - activePlayer.centerY
                movementX * targetX + movementY * targetY > 0f
            }
        }
    }

    fun onCriticalHit() {
        if (count(ItemEffectId.CRITICAL_MOMENTUM) > 0) {
            criticalMomentumRemaining = 5f
        }
    }

    fun onCloseCall() {
        if (count(ItemEffectId.CLOSE_CALL) > 0) closeCallRemaining = 3f
    }

    fun resolveDamageBeforeHealth(amount: Int): Int {
        if (amount <= 0 || invulnerabilityRemaining > 0f) return 0
        var playerDamage = amount

        val sharedCopies = count(ItemEffectId.SHARED_BLOOD)
        val livingSummons = summonProvider().filter { !it.isExpired }
        if (sharedCopies > 0 && livingSummons.isNotEmpty()) {
            val transferred = (amount * (0.10f * sharedCopies).coerceAtMost(0.90f)).roundToInt()
            playerDamage = (amount - transferred).coerceAtLeast(0)
            if (transferred > 0) {
                val baseShare = transferred / livingSummons.size
                val remainder = transferred % livingSummons.size
                livingSummons.forEachIndexed { index, summon ->
                    val share = baseShare + if (index < remainder) 1 else 0
                    if (share > 0) summon.takeDamage(share)
                    if (summon.health.isDead) onSummonDied(summon)
                }
            }
        }

        val currentHealth = player?.health?.currentHealth ?: Int.MAX_VALUE
        if (
            count(ItemEffectId.SECOND_HEART) > 0 &&
            playerDamage >= currentHealth
        ) {
            inventory?.replaceOneDefinition(
                from = DefaultItems.secondHeart,
                to = DefaultItems.brokenHeart
            )
            secondHeartAvailable = count(ItemEffectId.SECOND_HEART) > 0
            invulnerabilityRemaining = 3f
            playerDamage = (currentHealth - 1).coerceAtLeast(0)
        }

        if (playerDamage > 0 && count(ItemEffectId.HIT_ME_HARDER) > 0) {
            hitMeHarderStacks = (hitMeHarderStacks + 1).coerceAtMost(10)
        }
        return playerDamage
    }

    fun onSummonDied(summon: SummonedEntity) {
        if (!processedSummonDeaths.add(summon.runtimeId)) return
        if (count(ItemEffectId.SHARED_FURY) > 0) sharedFuryRemaining = 5f
        if (count(ItemEffectId.SUMMONED_LEGACY) > 0) {
            legacyZones.add(LegacyZone(summon.centerX, summon.centerY, 8f))
        }
    }

    fun reflectContactDamage(enemy: Enemy): Boolean {
        val copies = count(ItemEffectId.THORNS)
        val playerStats = stats ?: return false
        if (copies <= 0 || enemy.isDead) return false
        val reflected = (playerStats.totalScore(StatType.ARMOR_CLASS) * 2 * copies).coerceAtLeast(1)
        enemy.takeDamage(
            reflected,
            EnemyDamageContext(
                damageType = DamageType.PIERCING,
                sourceName = "Thorns"
            )
        )
        return enemy.isDead
    }

    fun onEnemyKilled(enemy: Enemy) {
        val isBoss = enemy.type == EnemyType.MINI_BOSS || enemy.type == EnemyType.FINAL_BOSS
        if (isBoss && count(ItemEffectId.HUNTERS_REWARD) > 0) {
            hunterRewardBossKills = (hunterRewardBossKills + 1).coerceAtMost(5)
        }

        if ((enemy.type == EnemyType.UNIQUE || isBoss) && count(ItemEffectId.FEAR_OF_DEATH) > 0) {
            val chance = (0.30 * count(ItemEffectId.FEAR_OF_DEATH)).coerceAtMost(0.90)
            if (Random.nextDouble() < chance) {
                enemyProvider().filter { nearby ->
                    !nearby.isDead && nearby.type == EnemyType.NORMAL &&
                        distanceSquared(enemy.centerX, enemy.centerY, nearby.centerX, nearby.centerY) <=
                        FEAR_RADIUS * FEAR_RADIUS
                }.forEach { it.applyCondition(com.azizoglu.grisilith.content.ConditionType.FRIGHTENED, 3f) }
            }
        }

        val kindKey = enemy.kindId?.value ?: "TYPE_${enemy.type.name}"
        if (count(ItemEffectId.BESTIARY_HUNTER) > 0 && bestiaryKinds.add(kindKey)) {
            val amount = count(ItemEffectId.BESTIARY_HUNTER) * if (isBoss) 2 else 1
            stats?.adjustRunBonus(StatType.entries.random(), amount)
        }

        if (isBoss && count(ItemEffectId.BOSS_TROPHY) > 0) {
            val combatStats = listOf(StatType.STRENGTH, StatType.DEXTERITY, StatType.WISDOM)
            stats?.adjustRunBonus(combatStats.random(), count(ItemEffectId.BOSS_TROPHY))
        }
    }

    fun healFromChest() {
        val copies = count(ItemEffectId.EXTRA_REWARD)
        val activePlayer = player ?: return
        if (copies <= 0) return
        val amount = (activePlayer.health.maximumHealth * 0.06f * copies * healingMultiplier()).roundToInt()
        if (amount > 0) activePlayer.health.heal(amount)
    }

    fun rollGhostProjectile(weapon: WeaponInstance?): Boolean {
        if (weapon == null || count(ItemEffectId.GHOST_PROJECTILE) <= 0) return false
        val tags = weapon.definition.tags
        return ContentTag.RANGED_WEAPON in tags && ContentTag.SPELL_FOCUS !in tags && Random.nextFloat() < 0.40f
    }

    fun onAbilityState(weapon: WeaponInstance, isReady: Boolean, delta: Float) {
        if (count(ItemEffectId.OVERFLOWING_POWER) <= 0) return
        if (isReady) {
            abilityReadySeconds[weapon.runtimeId] =
                ((abilityReadySeconds[weapon.runtimeId] ?: 0f) + delta).coerceAtMost(15f)
            abilityDamageBonuses.remove(weapon.runtimeId)
        }
    }

    fun onAbilityActivated(weapon: WeaponInstance): Boolean {
        if (count(ItemEffectId.OVERFLOWING_POWER) > 0) {
            abilityDamageBonuses[weapon.runtimeId] =
                (abilityReadySeconds.remove(weapon.runtimeId) ?: 0f).coerceAtMost(15f) * 0.01f
            abilityBonusRemaining[weapon.runtimeId] = 2f
        }
        if (count(ItemEffectId.BLOOD_SACRIFICE) <= 0 || Random.nextFloat() >= 0.50f) return false
        player?.sacrificeHalfCurrentHealth()
        return true
    }

    fun abilityDamageMultiplier(weapon: WeaponInstance): Float =
        1f + (abilityDamageBonuses[weapon.runtimeId] ?: 0f)

    fun uniqueChanceBonus(): Float = 0.005f * count(ItemEffectId.LITTLE_RISK)

    fun miniBossCoinChanceBonus(): Float = 0.02f * count(ItemEffectId.LITTLE_RISK)

    fun predestinationLabel(): String? = predestinationDamageType?.displayName

    fun bountyLabel(): String? = bountyEnemyType?.name

    fun predestinationType(): DamageType? = predestinationDamageType

    fun bountyType(): EnemyType? = bountyEnemyType

    fun renderLegacyZones(shapeRenderer: ShapeRenderer) {
        if (legacyZones.isEmpty()) return
        shapeRenderer.color = Color(0.45f, 0.72f, 1f, 0.18f)
        legacyZones.forEach { zone ->
            shapeRenderer.circle(zone.x, zone.y, LEGACY_RADIUS, 48)
        }
    }

    fun exportSaveState(): RunSaveItemEffectState =
        RunSaveItemEffectState(
            stage = lastStage,
            hunterRewardBossKills = hunterRewardBossKills,
            cashOutConsumedItems = cashOutConsumedItems,
            hitMeHarderStacks = hitMeHarderStacks,
            bestiaryKinds = HashSet(bestiaryKinds),
            predestinationDamageTypeName = predestinationDamageType?.name,
            bountyEnemyTypeName = bountyEnemyType?.name,
            secondHeartAvailable = secondHeartAvailable
        )

    fun restoreSaveState(state: RunSaveItemEffectState) {
        lastStage = state.stage
        hunterRewardBossKills = state.hunterRewardBossKills.coerceIn(0, 5)
        cashOutConsumedItems = state.cashOutConsumedItems.coerceAtLeast(0)
        hitMeHarderStacks = state.hitMeHarderStacks.coerceIn(0, 10)
        bestiaryKinds.clear()
        bestiaryKinds.addAll(state.bestiaryKinds.filter { it.isNotBlank() })
        predestinationDamageType =
            state.predestinationDamageTypeName?.let { name ->
                DamageType.entries.firstOrNull { it.name == name }
            }
        bountyEnemyType =
            state.bountyEnemyTypeName?.let { name ->
                EnemyType.entries.firstOrNull { it.name == name }
            }
        secondHeartAvailable = state.secondHeartAvailable
    }

    fun applyAcquisition(definition: ItemDefinition): Boolean {
        if (definition.effectId == ItemEffectId.SECOND_HEART) {
            secondHeartAvailable = true
        }

        if (definition.effectId == ItemEffectId.CASH_OUT) {
            val activeInventory = inventory ?: return false
            val removedCount =
                activeInventory.removeAllExceptDefinition(
                    definition
                )
            cashOutConsumedItems += removedCount
            return removedCount > 0
        }

        val conversion = when (definition.effectId) {
            ItemEffectId.CONVERSION_HEALTH -> StatType.MAX_HP to StatType.STRENGTH
            ItemEffectId.CONVERSION_ARMOR_CLASS -> StatType.ARMOR_CLASS to StatType.STRENGTH
            ItemEffectId.CONVERSION_SHEPHERD -> StatType.SHEPHERD to StatType.WISDOM
            ItemEffectId.CONVERSION_ATTACK_SPEED -> StatType.ATTACK_SPEED to StatType.DEXTERITY
            ItemEffectId.CONVERSION_MOVEMENT_SPEED -> StatType.MOVEMENT_SPEED to StatType.DEXTERITY
            ItemEffectId.CONVERSION_ENEMY_COUNT -> StatType.ENEMY_COUNT to StatType.PIERCING
            else -> null
        } ?: return false

        val playerStats = stats ?: return false
        val requested = (playerStats.totalScore(conversion.first) * 0.20f).roundToInt()
        if (requested <= 0) return false
        val removed = -playerStats.adjustRunBonus(conversion.first, -requested)
        if (removed > 0) playerStats.adjustRunBonus(conversion.second, removed)
        return false
    }

    private fun distanceSquared(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return dx * dx + dy * dy
    }

    private val DEEP_WOUNDS_STATS =
        setOf(StatType.ATTACK_SPEED, StatType.MOVEMENT_SPEED, StatType.CRIT_RATE)

    private const val CROWD_HUNGER_RADIUS = 250f
    private const val FEAR_RADIUS = 300f
    private const val LEGACY_RADIUS = 150f
}
