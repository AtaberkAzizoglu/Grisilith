package com.azizoglu.grisilith.item.world

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.enemy.definition.DefaultEnemyKinds
import com.azizoglu.grisilith.enemy.definition.EnemyTag
import com.azizoglu.grisilith.item.definition.ItemEffectId
import com.azizoglu.grisilith.item.runtime.ActiveItemEffects
import com.azizoglu.grisilith.item.runtime.ItemInventory
import com.azizoglu.grisilith.map.MapObstacle
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.stage.StageType
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import kotlin.math.roundToInt
import kotlin.random.Random

enum class LootDropKind { BEEF, HEAL_POTION, SOUL_ORB }

enum class TotemResult(val label: String) {
    HEALING_AURA("Healing Aura"),
    PROTECTERS_AREA("Protecters Area"),
    BURRIED_TREASURE("Burried Treasure"),
    HOURGLASS("Hourglass"),
    TOADS("Toads"),
    HALF_GOLD("Lose Half Gold"),
    HOSTILE_MINI_BOSS("Hostile Mini Boss"),
    SHRINK_MAP("Shrinking Ground"),
    TOTEM_CURSE("Totem Curse")
}

data class WorldPointSnapshot(val x: Float, val y: Float, val animationSeconds: Float)
data class TrapSnapshot(val x: Float, val y: Float, val stateIndex: Int)
data class LootSnapshot(val x: Float, val y: Float, val kind: LootDropKind, val ageSeconds: Float)
data class TotemSnapshot(
    val x: Float,
    val y: Float,
    val variant: Int,
    val chargeProgress: Float,
    val showChargeArea: Boolean,
    val rouletteRemainingSeconds: Float,
    val rouletteLabel: String?,
    val displayedResult: TotemResult?,
    val animationSeconds: Float
)

data class WorldItemVisualSnapshot(
    val brooms: List<WorldPointSnapshot>,
    val portal: WorldPointSnapshot?,
    val merchant: WorldPointSnapshot?,
    val traps: List<TrapSnapshot>,
    val loot: List<LootSnapshot>,
    val totem: TotemSnapshot?,
    val healingAuraRemainingSeconds: Float,
    val protectersAreaRemainingSeconds: Float,
    val blindFaithMistLevel: Int,
    val blindFaithAnimationSeconds: Float,
    val shrinkCenterX: Float?,
    val shrinkCenterY: Float?,
    val shrinkRadius: Float?
)

class WorldItemRuntime(
    private val inventory: ItemInventory,
    private val worldWidth: Float,
    private val worldHeight: Float
) {
    private data class Broom(var x: Float, var y: Float, var phase: Float = MathUtils.random(0f, 10f))
    private data class Trap(var x: Float, var y: Float, var victims: Int = 0, var storedGold: Int = 0)
    private data class Loot(var x: Float, var y: Float, val kind: LootDropKind, var age: Float = 0f)
    private data class Totem(var x: Float, var y: Float, val variant: Int, var charge: Float = 0f)

    private val brooms = mutableListOf<Broom>()
    private val traps = mutableListOf<Trap>()
    private val loot = mutableListOf<Loot>()
    private var portal: Vector2? = null
    private var merchant: Vector2? = null
    private var totem: Totem? = null
    private var trackedStage = -1
    private var portalHalfRollDone = false
    private var merchantUsedThisStage = false
    private var totemScheduledThisStage = false
    private var totemSpawnAttempted = false
    private var stillSeconds = 0f
    private var setupSeconds = 0f
    private var setupCenter: Vector2? = null
    private var pitCooldownSeconds = 0f
    private var lastPlayerX = 0f
    private var lastPlayerY = 0f
    private var animationSeconds = 0f
    private var rouletteSeconds = 0f
    private var rouletteResult: TotemResult? = null
    private var rouletteEntries = emptyList<TotemResult>()
    private var displayedResult: TotemResult? = null
    private var displayedResultSeconds = 0f
    private var healingAuraSeconds = 0f
    private var healingTick = 0f
    private var protectersAreaSeconds = 0f
    private var protectersTick = 0f
    private var shrinkStage = -1
    private var shrinkCenter: Vector2? = null
    private var curseStage = -1

    val hidesEnemyHealthBars: Boolean
        get() = count(ItemEffectId.BLIND_FAITH) > 0

    val merchantGuaranteesMarket: Boolean
        get() = merchantUsedThisStage

    fun merchantPosition(): Vector2? = merchant

    fun totemCollisionBounds(): Rectangle? = totem?.let {
        Rectangle(it.x - 28f, it.y - 18f, 56f, 38f)
    }

    fun onEnemyKilled(enemy: Enemy) {
        if (enemy.type == EnemyType.MINI_BOSS) {
            portal = Vector2(worldWidth * 0.5f, worldHeight * 0.5f)
        }

        val copies = count(ItemEffectId.LOOT_THE_ENEMIES)
        if (copies <= 0 || Random.nextFloat() >= (copies * 0.05f).coerceAtMost(0.25f)) return

        val tags = DefaultEnemyKinds.all
            .firstOrNull { it.id == enemy.kindId }
            ?.enemyTags
            ?: return

        val kind = when {
            EnemyTag.BEAST in tags -> LootDropKind.BEEF
            EnemyTag.HUMANOID in tags -> LootDropKind.HEAL_POTION
            EnemyTag.UNDEAD in tags -> LootDropKind.SOUL_ORB
            else -> return
        }
        loot.add(Loot(enemy.centerX, enemy.centerY, kind))
    }

    fun update(
        delta: Float,
        stage: Int,
        stageType: StageType,
        elapsedStageSeconds: Float,
        stageDurationSeconds: Float,
        player: Player,
        enemies: List<Enemy>,
        mapObstacles: List<MapObstacle>,
        pickupRadius: Float,
        collectCurrency: (Float, Float, Float) -> Boolean,
        nearestCurrency: (Float, Float) -> Pair<Float, Float>?,
        onPortalEntered: () -> Unit,
        onPitEnemyKilled: (Enemy) -> Int,
        onTrapGoldReleased: (Float, Float, Int) -> Unit,
        onHalfGold: () -> Unit,
        onSpawnRiskMiniBoss: () -> Unit,
        onHourglass: () -> Unit,
        onTreasure: (Float, Float) -> Unit,
        onToads: (Int, Float, Float) -> Unit,
        hasLivingToads: () -> Boolean,
        onAuraEnemyKilled: (Enemy) -> Unit
    ) {
        val dt = delta.coerceAtLeast(0f)
        animationSeconds += dt
        synchronizeStage(stage, mapObstacles, player, enemies)
        synchronizeBrooms(player)
        updateBrooms(dt, collectCurrency, nearestCurrency)
        updatePortal(stageType, elapsedStageSeconds, stageDurationSeconds, player, onPortalEntered)
        updateMerchant(player, pickupRadius)
        updateLoot(dt, player, pickupRadius)
        updatePitTrap(dt, player, enemies, onPitEnemyKilled, onTrapGoldReleased)
        updateTotem(
            dt, stage, elapsedStageSeconds, player, enemies, onHalfGold,
            onSpawnRiskMiniBoss, onHourglass, onTreasure, onToads, hasLivingToads,
            onAuraEnemyKilled, mapObstacles
        )
        applyExclusions(player, enemies)
    }

    private fun synchronizeStage(
        stage: Int,
        mapObstacles: List<MapObstacle>,
        player: Player,
        enemies: List<Enemy>
    ) {
        if (trackedStage == stage) return
        trackedStage = stage
        portal = null
        merchant = null
        merchantUsedThisStage = false
        portalHalfRollDone = false
        totem = null
        rouletteSeconds = 0f
        rouletteResult = null
        rouletteEntries = emptyList()
        displayedResult = null
        displayedResultSeconds = 0f
        healingAuraSeconds = 0f
        protectersAreaSeconds = 0f
        shrinkCenter = null
        shrinkStage = -1
        curseStage = -1
        totemSpawnAttempted = false
        totemScheduledThisStage = count(ItemEffectId.BONE_TOTEMS) > 0 && Random.nextFloat() < 0.35f

        if (Random.nextFloat() < count(ItemEffectId.IS_IT_MERCHANT) * 0.05f) {
            merchant = randomSafePoint(mapObstacles, player, enemies, 90f, 0f)
        }
        lastPlayerX = player.centerX
        lastPlayerY = player.centerY
    }

    private fun synchronizeBrooms(player: Player) {
        val desired = count(ItemEffectId.ANIMATED_BROOM)
        while (brooms.size < desired) {
            val index = brooms.size
            brooms.add(Broom(player.centerX + 32f * (index + 1), player.centerY))
        }
        while (brooms.size > desired) brooms.removeAt(brooms.lastIndex)
    }

    private fun updateBrooms(
        delta: Float,
        collectCurrency: (Float, Float, Float) -> Boolean,
        nearestCurrency: (Float, Float) -> Pair<Float, Float>?
    ) {
        brooms.forEachIndexed { index, broom ->
            broom.phase += delta
            val target = nearestCurrency(broom.x, broom.y)
            if (target != null) {
                val dx = target.first - broom.x
                val dy = target.second - broom.y
                val distance = kotlin.math.sqrt(dx * dx + dy * dy).coerceAtLeast(0.001f)
                val step = (175f * delta).coerceAtMost(distance)
                broom.x += dx / distance * step
                broom.y += dy / distance * step
            } else {
                val angle = broom.phase * 0.7f + index * 2.1f
                broom.x += MathUtils.cos(angle) * 95f * delta
                broom.y += MathUtils.sin(angle * 1.13f) * 95f * delta
            }
            broom.x = broom.x.coerceIn(24f, worldWidth - 24f)
            broom.y = broom.y.coerceIn(24f, worldHeight - 24f)
            collectCurrency(broom.x, broom.y, 52f)
        }
    }

    private fun updatePortal(
        stageType: StageType,
        elapsed: Float,
        duration: Float,
        player: Player,
        onEntered: () -> Unit
    ) {
        if (!portalHalfRollDone && stageType == StageType.NORMAL && elapsed >= duration * 0.5f) {
            portalHalfRollDone = true
            if (Random.nextFloat() < count(ItemEffectId.CALL_THE_PORTAL) * 0.01f) {
                portal = Vector2(worldWidth * 0.5f, worldHeight * 0.5f)
            }
        }
        val active = portal ?: return
        if (distanceSquared(player.centerX, player.centerY, active.x, active.y) <= 62f * 62f) {
            portal = null
            onEntered()
        }
    }

    private fun updateMerchant(player: Player, pickupRadius: Float) {
        val active = merchant ?: return
        val radius = pickupRadius.coerceAtLeast(42f)
        if (distanceSquared(player.centerX, player.centerY, active.x, active.y) <= radius * radius) {
            merchantUsedThisStage = true
            merchant = null
        }
    }

    private fun updateLoot(delta: Float, player: Player, pickupRadius: Float) {
        val radiusSquared = pickupRadius.coerceAtLeast(28f).let { it * it }
        loot.forEach { it.age += delta }
        val collected = loot.filter {
            distanceSquared(player.centerX, player.centerY, it.x, it.y) <= radiusSquared
        }
        collected.forEach {
            val amount = (player.health.maximumHealth * 0.10f * ActiveItemEffects.healingMultiplier())
                .roundToInt().coerceAtLeast(1)
            player.health.heal(amount)
        }
        loot.removeAll(collected.toSet())
    }

    private fun updatePitTrap(
        delta: Float,
        player: Player,
        enemies: List<Enemy>,
        onPitEnemyKilled: (Enemy) -> Int,
        onTrapGoldReleased: (Float, Float, Int) -> Unit
    ) {
        pitCooldownSeconds = (pitCooldownSeconds - delta).coerceAtLeast(0f)
        if (count(ItemEffectId.PIT_TRAP) <= 0) return

        val moved = distanceSquared(player.centerX, player.centerY, lastPlayerX, lastPlayerY) > 1.5f * 1.5f
        lastPlayerX = player.centerX
        lastPlayerY = player.centerY
        val maxTraps = count(ItemEffectId.PIT_TRAP)

        if (setupCenter == null && traps.size < maxTraps && pitCooldownSeconds <= 0f) {
            stillSeconds = if (moved) 0f else stillSeconds + delta
            if (stillSeconds >= 2f) {
                setupCenter = Vector2(player.centerX, player.centerY)
                setupSeconds = 0f
            }
        }

        setupCenter?.let { center ->
            if (distanceSquared(player.centerX, player.centerY, center.x, center.y) > 100f * 100f) {
                setupCenter = null
                setupSeconds = 0f
                stillSeconds = 0f
            } else {
                setupSeconds += delta
                if (setupSeconds >= 3f) {
                    traps.add(Trap(center.x, center.y))
                    setupCenter = null
                    setupSeconds = 0f
                    stillSeconds = 0f
                }
            }
        }

        traps.toList().forEach { trap ->
            val victim = enemies.firstOrNull {
                !it.isDead && it.type == EnemyType.NORMAL && !it.ignoresObstacles &&
                    distanceSquared(it.centerX, it.centerY, trap.x, trap.y) <= 42f * 42f
            } ?: return@forEach

            victim.takeDamage(victim.health.currentHealth.coerceAtLeast(1) * 100)
            if (victim.isDead) {
                trap.storedGold += onPitEnemyKilled(victim)
                trap.victims++
            }
            if (trap.victims >= 4) {
                onTrapGoldReleased(trap.x, trap.y, trap.storedGold)
                traps.remove(trap)
                pitCooldownSeconds = 30f
            }
        }
    }

    private fun updateTotem(
        delta: Float,
        stage: Int,
        elapsed: Float,
        player: Player,
        enemies: List<Enemy>,
        onHalfGold: () -> Unit,
        onSpawnRiskMiniBoss: () -> Unit,
        onHourglass: () -> Unit,
        onTreasure: (Float, Float) -> Unit,
        onToads: (Int, Float, Float) -> Unit,
        hasLivingToads: () -> Boolean,
        onAuraEnemyKilled: (Enemy) -> Unit,
        mapObstacles: List<MapObstacle>
    ) {
        if (totemScheduledThisStage && !totemSpawnAttempted && elapsed >= 15f) {
            totemSpawnAttempted = true
            totem = randomSafePoint(mapObstacles, player, enemies, 100f, 300f)?.let {
                Totem(it.x, it.y, Random.nextInt(4))
            }
        }

        val activeTotem = totem ?: return
        if (rouletteSeconds <= 0f && rouletteResult == null && activeTotem.charge < 5f) {
            val inside = distanceSquared(player.centerX, player.centerY, activeTotem.x, activeTotem.y) <= 150f * 150f
            activeTotem.charge = if (inside) {
                (activeTotem.charge + delta).coerceAtMost(5f)
            } else {
                (activeTotem.charge - delta).coerceAtLeast(0f)
            }
            if (activeTotem.charge >= 5f) startRoulette()
        }

        if (rouletteSeconds > 0f) {
            rouletteSeconds = (rouletteSeconds - delta).coerceAtLeast(0f)
            if (rouletteSeconds == 0f) {
                applyTotemResult(
                    rouletteResult, stage, activeTotem, onHalfGold, onSpawnRiskMiniBoss,
                    onHourglass, onTreasure, onToads
                )
            }
        }

        if (displayedResultSeconds > 0f) {
            displayedResultSeconds = (displayedResultSeconds - delta).coerceAtLeast(0f)
            if (displayedResultSeconds == 0f) displayedResult = null
        }
        if (displayedResult == TotemResult.TOADS && !hasLivingToads()) displayedResult = null

        healingAuraSeconds = (healingAuraSeconds - delta).coerceAtLeast(0f)
        if (healingAuraSeconds > 0f) {
            healingTick += delta
            while (healingTick >= 0.25f) {
                healingTick -= 0.25f
                if (distanceSquared(player.centerX, player.centerY, activeTotem.x, activeTotem.y) <= 170f * 170f) {
                    player.health.heal((player.health.maximumHealth * 0.015f).roundToInt().coerceAtLeast(1))
                }
            }
        } else if (displayedResult == TotemResult.HEALING_AURA) displayedResult = null

        protectersAreaSeconds = (protectersAreaSeconds - delta).coerceAtLeast(0f)
        if (protectersAreaSeconds > 0f) {
            protectersTick += delta
            while (protectersTick >= 0.33f) {
                protectersTick -= 0.33f
                enemies.filter {
                    !it.isDead && distanceSquared(it.centerX, it.centerY, activeTotem.x, activeTotem.y) <= 180f * 180f
                }.forEach {
                    it.takeDamage((it.health.maximumHealth * 0.04f).roundToInt().coerceAtLeast(1))
                    if (it.isDead) onAuraEnemyKilled(it)
                }
            }
        } else if (displayedResult == TotemResult.PROTECTERS_AREA) displayedResult = null

        if (shrinkStage == stage) {
            constrainPlayerToCircle(player, activeTotem.x, activeTotem.y, 1000f)
            enemies.forEach { it.constrainCenterToCircle(activeTotem.x, activeTotem.y, 1000f) }
        }
        if (curseStage == stage) {
            enemies.forEach {
                it.applyTemporaryCombatBuff(
                    durationSeconds = 1f,
                    damageMultiplier = 1.20f,
                    movementSpeedMultiplier = 1.20f,
                    attackSpeedMultiplier = 1.20f
                )
            }
        }
    }

    private fun startRoulette() {
        val rewards = mutableListOf<TotemResult>()
        fun add(effect: ItemEffectId, result: TotemResult) {
            if (count(effect) > 0) {
                rewards.add(result)
                if (rewards.count { it.ordinal <= TotemResult.TOADS.ordinal } % 2 == 1) {
                    rewards.add(
                        listOf(
                            TotemResult.HALF_GOLD,
                            TotemResult.HOSTILE_MINI_BOSS,
                            TotemResult.SHRINK_MAP,
                            TotemResult.TOTEM_CURSE
                        ).random()
                    )
                }
            }
        }
        add(ItemEffectId.BONE_TOTEM_HEALING_AURA, TotemResult.HEALING_AURA)
        add(ItemEffectId.BONE_TOTEM_PROTECTERS_AREA, TotemResult.PROTECTERS_AREA)
        add(ItemEffectId.BONE_TOTEM_BURRIED_TREASURE, TotemResult.BURRIED_TREASURE)
        add(ItemEffectId.BONE_TOTEM_HOURGLASS, TotemResult.HOURGLASS)
        add(ItemEffectId.BONE_TOTEM_TOADS, TotemResult.TOADS)
        rouletteResult = rewards.randomOrNull()
        rouletteEntries = rewards.toList()
        rouletteSeconds = if (rouletteResult == null) 0f else 1.5f
    }

    private fun applyTotemResult(
        result: TotemResult?,
        stage: Int,
        totem: Totem,
        onHalfGold: () -> Unit,
        onSpawnRiskMiniBoss: () -> Unit,
        onHourglass: () -> Unit,
        onTreasure: (Float, Float) -> Unit,
        onToads: (Int, Float, Float) -> Unit
    ) {
        displayedResult = result
        when (result) {
            TotemResult.HEALING_AURA -> { healingAuraSeconds = 12f; healingTick = 0f }
            TotemResult.PROTECTERS_AREA -> { protectersAreaSeconds = 10f; protectersTick = 0f }
            TotemResult.BURRIED_TREASURE -> { onTreasure(totem.x, totem.y); displayedResultSeconds = 5f }
            TotemResult.HOURGLASS -> { onHourglass(); displayedResultSeconds = 5f }
            TotemResult.TOADS -> onToads(ActiveItemEffects.currentShepherdScore() / 5, totem.x, totem.y)
            TotemResult.HALF_GOLD -> { onHalfGold(); displayedResultSeconds = 5f }
            TotemResult.HOSTILE_MINI_BOSS -> { onSpawnRiskMiniBoss(); displayedResultSeconds = 5f }
            TotemResult.SHRINK_MAP -> { shrinkStage = stage; shrinkCenter = Vector2(totem.x, totem.y); displayedResultSeconds = 5f }
            TotemResult.TOTEM_CURSE -> { curseStage = stage; displayedResultSeconds = 5f }
            null -> Unit
        }
    }

    private fun applyExclusions(player: Player, enemies: List<Enemy>) {
        merchant?.let { point ->
            enemies.filter { it.type == EnemyType.NORMAL || it.type == EnemyType.UNIQUE }
                .forEach { it.resolveCircularExclusion(point.x, point.y, 150f) }
        }
        totemCollisionBounds()?.let { player.resolveObstacleCollision(it) }
        totem?.let { point ->
            enemies.forEach { it.resolveCircularExclusion(point.x, point.y, 30f) }
        }
    }

    private fun randomSafePoint(
        obstacles: List<MapObstacle>,
        player: Player,
        enemies: List<Enemy>,
        clearance: Float,
        centerExclusion: Float
    ): Vector2? {
        repeat(300) {
            val x = MathUtils.random(100f, worldWidth - 100f)
            val y = MathUtils.random(100f, worldHeight - 100f)
            if (distanceSquared(x, y, worldWidth * 0.5f, worldHeight * 0.5f) < centerExclusion * centerExclusion) return@repeat
            if (distanceSquared(x, y, player.centerX, player.centerY) < clearance * clearance) return@repeat
            if (enemies.any { distanceSquared(x, y, it.centerX, it.centerY) < clearance * clearance }) return@repeat
            val area = Rectangle(x - clearance, y - clearance, clearance * 2f, clearance * 2f)
            if (obstacles.any { area.overlaps(it.bounds) }) return@repeat
            return Vector2(x, y)
        }
        return null
    }

    private fun constrainPlayerToCircle(player: Player, x: Float, y: Float, radius: Float) {
        // Player already owns world-boundary movement. The shrink visual is authoritative;
        // collision is resolved by a matching circular push in Player.
        player.constrainCenterToCircle(x, y, radius)
    }

    fun visualSnapshot(): WorldItemVisualSnapshot {
        val setup = setupCenter
        val activeTotem = totem
        val rouletteLabel = if (rouletteSeconds > 0f && rouletteEntries.isNotEmpty()) {
            rouletteEntries[((animationSeconds * 12f).toInt()).mod(rouletteEntries.size)].label
        } else null
        return WorldItemVisualSnapshot(
            brooms = brooms.map { WorldPointSnapshot(it.x, it.y, animationSeconds + it.phase) },
            portal = portal?.let { WorldPointSnapshot(it.x, it.y, animationSeconds) },
            merchant = merchant?.let { WorldPointSnapshot(it.x, it.y, animationSeconds) },
            traps = traps.map { TrapSnapshot(it.x, it.y, it.victims.coerceIn(0, 4)) } +
                listOfNotNull(setup?.let { TrapSnapshot(it.x, it.y, -1) }),
            loot = loot.map { LootSnapshot(it.x, it.y, it.kind, it.age) },
            totem = activeTotem?.let {
                TotemSnapshot(
                    x = it.x, y = it.y, variant = it.variant,
                    chargeProgress = (it.charge / 5f).coerceIn(0f, 1f),
                    showChargeArea = it.charge < 5f,
                    rouletteRemainingSeconds = rouletteSeconds,
                    rouletteLabel = rouletteLabel,
                    displayedResult = displayedResult,
                    animationSeconds = animationSeconds
                )
            },
            healingAuraRemainingSeconds = healingAuraSeconds,
            protectersAreaRemainingSeconds = protectersAreaSeconds,
            blindFaithMistLevel = (count(ItemEffectId.BLIND_FAITH) - 1).coerceIn(0, 4),
            blindFaithAnimationSeconds = animationSeconds,
            shrinkCenterX = shrinkCenter?.x,
            shrinkCenterY = shrinkCenter?.y,
            shrinkRadius = shrinkCenter?.let { 1000f }
        )
    }

    fun clearForRunEnd() {
        brooms.clear(); traps.clear(); loot.clear(); portal = null; merchant = null; totem = null
    }

    fun resolveEnemyExclusions(enemies: List<Enemy>) {
        merchant?.let { point ->
            enemies.filter { it.type == EnemyType.NORMAL || it.type == EnemyType.UNIQUE }
                .forEach { it.resolveCircularExclusion(point.x, point.y, 150f) }
        }
        totem?.let { point ->
            enemies.forEach { it.resolveCircularExclusion(point.x, point.y, 30f) }
        }
        shrinkCenter?.let { center ->
            enemies.forEach { it.constrainCenterToCircle(center.x, center.y, 1000f) }
        }
    }

    fun onStageEnded() {
        portal = null
        merchant = null
        totem = null
        rouletteSeconds = 0f
        rouletteResult = null
        rouletteEntries = emptyList()
        displayedResult = null
        displayedResultSeconds = 0f
        healingAuraSeconds = 0f
        protectersAreaSeconds = 0f
        shrinkCenter = null
        shrinkStage = -1
        curseStage = -1
    }

    private fun count(effectId: ItemEffectId): Int =
        inventory.allItems().count { it.definition.effectId == effectId }

    private fun distanceSquared(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return dx * dx + dy * dy
    }
}
