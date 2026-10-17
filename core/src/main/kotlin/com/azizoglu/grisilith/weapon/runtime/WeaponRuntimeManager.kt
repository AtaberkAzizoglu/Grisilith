package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyManager
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.tamga.runtime.TamgaRuntimeManager
import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.weapon.skill.WeaponAbilityState
import com.azizoglu.grisilith.weapon.skill.DefensiveLineRuntime
import com.azizoglu.grisilith.weapon.skill.DefensiveLineVisualRenderer
import com.azizoglu.grisilith.weapon.skill.PhysicalWeaponAbilityRuntime
import com.azizoglu.grisilith.weapon.definition.WeaponAttackPattern
import com.azizoglu.grisilith.weapon.runtime.executor.ShortswordAttackExecutor
import com.azizoglu.grisilith.map.MapObstacle
import com.azizoglu.grisilith.weapon.skill.IgniteDashRuntime
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.azizoglu.grisilith.weapon.skill.DefaultWeaponAbilities
import com.azizoglu.grisilith.weapon.skill.VortexPullRuntime
import com.azizoglu.grisilith.weapon.visual.spell.SpellFocusEffectSpriteRenderer
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import kotlin.math.roundToInt
import com.azizoglu.grisilith.weapon.skill.StaticCloudsRuntime
import com.azizoglu.grisilith.weapon.skill.area.PersistentAreaManager
import com.azizoglu.grisilith.content.ConditionType
import com.azizoglu.grisilith.combat.condition.ConditionDamageCalculator
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.enemy.EnemyDamageContext
import com.badlogic.gdx.math.MathUtils
import com.azizoglu.grisilith.weapon.skill.ImpaleRuntime
import com.azizoglu.grisilith.weapon.skill.ImpaleAndDragReward
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.weapon.skill.ErasingSoulsRuntime
import com.azizoglu.grisilith.weapon.skill.AcidicPoolRuntime
import com.azizoglu.grisilith.weapon.glyph.GlyphMotionRuntime
import com.azizoglu.grisilith.weapon.glyph.GlyphFeedback
import com.azizoglu.grisilith.weapon.glyph.GlyphSpellRuntime
import com.azizoglu.grisilith.combat.interaction.CombatInteractionManager
import com.azizoglu.grisilith.combat.interaction.CombatInteractionEvent
import com.azizoglu.grisilith.combat.interaction.CombatInteractionTrigger
import com.azizoglu.grisilith.combat.interaction.VitriolicIgnitionRule
import com.azizoglu.grisilith.combat.interaction.PlagueStormRule
import com.badlogic.gdx.math.Vector2
import com.azizoglu.grisilith.character.trait.CharacterTraitDefinition
import com.azizoglu.grisilith.character.trait.CharacterTraitRuntime
import com.azizoglu.grisilith.settings.GameSettings
import com.badlogic.gdx.graphics.Color

class WeaponRuntimeManager(
    private val inventory:
    WeaponInventory,

    private val playerStats:
    PlayerStats,

    private val tamgaRuntimeManager:
    TamgaRuntimeManager,

    characterTrait:
    CharacterTraitDefinition,

    traitPlayer:
    Player,

    private val currentStageProvider:
        () -> Int = { 1 }
) {
    private data class PreparedCast(val weapon: WeaponInstance, val slot: Int, var seconds: Float = 1f)
    private val preparedCasts = mutableMapOf<Int, PreparedCast>()
    private val pointerAim = Vector2()
    private val castAim = Vector2()
    private val extendedCastAims = mutableMapOf<WeaponRuntime, Vector2>()
    private var currentPlayer: Player? = null
    private var currentEnemies: EnemyManager? = null
    private var bypassPreparation = false
    private var abilityOriginForSlot: ((Int) -> Pair<Float, Float>)? = null
    private var previewWorldWidth = 1f
    private var previewWorldHeight = 1f

    private fun abilityOrigin(weapon: WeaponInstance, player: Player): Pair<Float, Float> {
        val slot = inventory.weaponSlots.firstOrNull { it.weapon === weapon }?.index ?: 0
        val carrier = abilityOriginForSlot?.invoke(slot) ?: (player.centerX to player.centerY)
        val minion = slot > 0
        val height = when(weapon.definition.id.value) {
            "WPN_WARHAMMER" -> 60f
            "WPN_LANCE" -> 104f
            else -> 0f
        } * if(minion) 26f/42f else 1f
        val yOffset = if(height > 0f) height/2f - if(minion) 11f else 18f else 2f
        return (carrier.first + if(minion) 26f else 34f) to (carrier.second + yOffset)
    }

    private fun effectiveRange(base: Float): Float = (base + tamgaRuntimeManager.weaponRangeBonusPixels()) *
        com.azizoglu.grisilith.item.runtime.ActiveItemEffects.weaponRangeMultiplier()

    private fun normalRange(weapon: WeaponInstance): Float =
        effectiveRange(weapon.definition.baseRange * weapon.currentLevelStats.rangeMultiplier)

    fun abilityRangeFor(weapon: WeaponInstance): Float = when (weapon.unlockedAbility?.id) {
        DefaultWeaponAbilities.RAPID_STROKES_ID -> normalRange(weapon) * 1.25f
        DefaultWeaponAbilities.AIR_SLASH_ID -> effectiveRange(if (weapon.hasWeaponAbilityUpgrade) DefaultWeaponAbilities.AIR_SLASH_UPGRADED_RANGE else DefaultWeaponAbilities.AIR_SLASH_RANGE)
        DefaultWeaponAbilities.SPIN_ATTACK_ID -> normalRange(weapon) * .66f * if (weapon.hasWeaponAbilityUpgrade) DefaultWeaponAbilities.SPIN_ATTACK_UPGRADED_RADIUS_MULTIPLIER else DefaultWeaponAbilities.SPIN_ATTACK_BASE_RADIUS_MULTIPLIER
        DefaultWeaponAbilities.VORTEX_PULL_ID -> DefaultWeaponAbilities.VORTEX_PULL_PLACEMENT_RANGE
        DefaultWeaponAbilities.ACIDIC_POOL_ID -> if (weapon.hasWeaponAbilityUpgrade) DefaultWeaponAbilities.ACIDIC_POOL_UPGRADED_PLACEMENT_RANGE else DefaultWeaponAbilities.ACIDIC_POOL_PLACEMENT_RANGE
        DefaultWeaponAbilities.DEFENSIVE_LINE_BURST_ID -> DefaultWeaponAbilities.DEFENSIVE_LINE_BURST_RADIUS
        DefaultWeaponAbilities.KNIFE_SWARM_ID -> effectiveRange(DefaultWeaponAbilities.KNIFE_SWARM_RANGE)
        DefaultWeaponAbilities.STATIC_CLOUDS_ID -> DefaultWeaponAbilities.STATIC_CLOUDS_RADIUS
        DefaultWeaponAbilities.IGNITE_DASH_ID -> if (weapon.hasWeaponAbilityUpgrade) DefaultWeaponAbilities.IGNITE_DASH_UPGRADED_MAXIMUM_RANGE else DefaultWeaponAbilities.IGNITE_DASH_MAXIMUM_RANGE
        DefaultWeaponAbilities.GLYPH_CASTING_ID -> if (weapon.chalkDustSpellChoice?.glyphType == com.azizoglu.grisilith.weapon.glyph.GlyphType.CALL_LIGHTNING) 750f else if (weapon.hasWeaponAbilityUpgrade) 700f else 550f
        else -> when (weapon.definition.id.value) {
            "WPN_WARHAMMER" -> if (weapon.hasWeaponAbilityUpgrade) 444f else 340f
            "WPN_WHIP" -> effectiveRange(weapon.definition.baseRange)
            "WPN_SLING" -> effectiveRange(650f)
            else -> normalRange(weapon)
        }
    }

    private fun needsPreparation(weapon: WeaponInstance) = weapon.unlockedAbility?.id in setOf(
        DefaultWeaponAbilities.RAPID_STROKES_ID, DefaultWeaponAbilities.AIR_SLASH_ID,
        DefaultWeaponAbilities.SPIN_ATTACK_ID, DefaultWeaponAbilities.VORTEX_PULL_ID,
        DefaultWeaponAbilities.ACIDIC_POOL_ID, DefaultWeaponAbilities.LEISURELY_SHOT_ID,
        DefaultWeaponAbilities.HEAVY_BOLTS_ID) ||
        weapon.definition.id.value in setOf("WPN_WARHAMMER", "WPN_WHIP", "WPN_SLING")

    private fun hasAbilityTarget(weapon: WeaponInstance, player: Player, enemies: EnemyManager): Boolean {
        val (x, y) = abilityOrigin(weapon, player)
        return AbilityTargeting.select(enemies.weaponTargets, x, y, abilityRangeFor(weapon)) != null
    }

    private fun castPrepared(slot: Int, player: Player, enemies: EnemyManager, width: Float, height: Float): Boolean {
        val pending = preparedCasts.remove(slot) ?: return false
        if (inventory.weaponAt(slot) !== pending.weapon) return false
        bypassPreparation = true
        return try { tryActivateAbilityInSlot(slot, player, enemies, width, height) }
        finally { bypassPreparation = false }
    }

    private val runtimesByWeapon =
        mutableMapOf<
            WeaponInstance,
            WeaponRuntime
            >()

    private val executorFactory =
        WeaponAttackExecutorFactory(
            playerStats = playerStats
        )

    private val activeSkillDamageCalculator =
        WeaponDamageCalculator(playerStats)

    private val pendingExtendedAbilities = mutableListOf<Pair<WeaponRuntime,Int>>()

    private val statBonusSynchronizer =
        WeaponStatBonusSynchronizer(
            inventory = inventory,
            playerStats = playerStats
        )

    private val defensiveLineRuntime =
        DefensiveLineRuntime(
            playerStats = playerStats,
            currentStageProvider =
                currentStageProvider
        )

    private val defensiveLineVisualRenderer =
        DefensiveLineVisualRenderer()

    private val physicalWeaponAbilityRuntime =
        PhysicalWeaponAbilityRuntime(
            playerStats = playerStats,
            damageCalculator = activeSkillDamageCalculator,
            shortswordExecutor = executorFactory.executorFor(
                WeaponAttackPattern.SHORT_SLASH_LINES
            ) as ShortswordAttackExecutor,
            defensiveLineRuntime = defensiveLineRuntime
        )

    private class FocusChannel(stats: PlayerStats) {
        val ignite = IgniteDashRuntime(stats)
        val vortex = VortexPullRuntime()
        val areas = PersistentAreaManager()
        val clouds = StaticCloudsRuntime(areas)
        val pool = AcidicPoolRuntime(areas)
        var vortexOwner: WeaponRuntime? = null
        var vortexWeapon: WeaponInstance? = null
        var cloudOwner: WeaponRuntime? = null
        var cloudWeapon: WeaponInstance? = null
        var poolOwner: WeaponRuntime? = null
        var poolWeapon: WeaponInstance? = null
        val pending = ArrayDeque<PendingVitriolicIgnition>()
        val rules = mutableListOf<com.azizoglu.grisilith.combat.interaction.CombatInteractionRule>()
        fun clear() {
            ignite.cancel();vortex.clear();clouds.clear();pool.clear();areas.clearAll();pending.clear()
            vortexOwner=null;vortexWeapon=null;cloudOwner=null;cloudWeapon=null;poolOwner=null;poolWeapon=null
        }
    }
    private val focusChannels = mutableMapOf<String,FocusChannel>()
    private val fallbackFocus = FocusChannel(playerStats)
    private var selectedFocus = fallbackFocus
    private val igniteDashRuntime get() = selectedFocus.ignite
    private val vortexPullRuntime get() = selectedFocus.vortex

    private fun <T> inFocus(channel: FocusChannel, action: () -> T): T {
        val previous=selectedFocus;selectedFocus=channel
        return try { action() } finally { selectedFocus=previous }
    }
    private fun visitFocus(action: () -> Unit) {
        (focusChannels.values.toList().ifEmpty { listOf(fallbackFocus) }).forEach { inFocus(it,action) }
    }
    private fun focusFor(weapon: WeaponInstance): FocusChannel = focusChannels.getOrPut(weapon.runtimeId) {
        FocusChannel(playerStats).also { channel ->
            channel.rules += VitriolicIgnitionRule(
                tryIgnitePool = { x,y -> channel.pool.tryDamageReactiveTarget(DamageType.FIRE,x,y) },
                onTriggered = { x,y -> channel.pending.addLast(PendingVitriolicIgnition(x,y)) })
            channel.rules += PlagueStormRule(
                isInsideVortex = { x,y -> channel.vortex.contains(x,y) },
                activatePlagueStorm = {
                    channel.vortex.field?.let { channel.vortex.tryDamageReactiveTarget(DamageType.NECROTIC,it.center.x,it.center.y) } ?: false })
            channel.rules.forEach(combatInteractionManager::register)
        }
    }

    private val spellFocusEffectSpriteRenderer =
        SpellFocusEffectSpriteRenderer()

    private val persistentAreaManager get() = selectedFocus.areas
    private val staticCloudsRuntime get() = selectedFocus.clouds

    private val impaleRuntime =
        ImpaleRuntime()

    private val erasingSoulsRuntime =
        ErasingSoulsRuntime()

    private val acidicPoolRuntime get() = selectedFocus.pool

    private val glyphMotionRuntime =
        GlyphMotionRuntime(
            playerStats =
                playerStats
        )

    private val glyphSpellRuntime =
        GlyphSpellRuntime(
            playerStats =
                playerStats
        )

    private var glyphWeaponRuntime:
        WeaponRuntime? =
        null


    private var vortexWeaponRuntime: WeaponRuntime?
        get()=selectedFocus.vortexOwner
        set(value){selectedFocus.vortexOwner=value}
    private var vortexWeapon: WeaponInstance?
        get()=selectedFocus.vortexWeapon
        set(value){selectedFocus.vortexWeapon=value}
    private var staticCloudsWeaponRuntime: WeaponRuntime?
        get()=selectedFocus.cloudOwner
        set(value){selectedFocus.cloudOwner=value}
    private var staticCloudsWeapon: WeaponInstance?
        get()=selectedFocus.cloudWeapon
        set(value){selectedFocus.cloudWeapon=value}
    private var acidicPoolWeaponRuntime: WeaponRuntime?
        get()=selectedFocus.poolOwner
        set(value){selectedFocus.poolOwner=value}
    private var acidicPoolWeapon: WeaponInstance?
        get()=selectedFocus.poolWeapon
        set(value){selectedFocus.poolWeapon=value}

    private val combatInteractionManager =
        CombatInteractionManager()

    private val characterTraitRuntime =
        CharacterTraitRuntime(
            traitId = characterTrait.id,
            player = traitPlayer,
            playerStats = playerStats
        )

    private data class PendingVitriolicIgnition(
        val centerX: Float,
        val centerY: Float
    )

    private val pendingVitriolicIgnitions get() = selectedFocus.pending

    init { combatInteractionManager.register(characterTraitRuntime) }

    fun update(
        delta: Float,
        player: Player,
        enemyManager: EnemyManager,
        projectiles: MutableList<Projectile>,
        mapObstacles: List<MapObstacle>,
        worldWidth: Float,
        worldHeight: Float,
        attackOriginForSlot: (Int) -> Pair<Float, Float> = {
            player.centerX to player.centerY
        },
        onDamageDealt: (
            damage: Int,
            weaponName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit,
        onNormalAttackPerformed: (
            weapon: WeaponInstance,
            attacksPerSecond: Float,
            targetCenterX: Float,
            targetCenterY: Float
        ) -> Unit = {
                _,
                _,
                _,
                _ ->
        }
    ) {
        synchronizeWithInventory()
        currentPlayer = player
        currentEnemies = enemyManager
        abilityOriginForSlot = attackOriginForSlot
        previewWorldWidth = worldWidth
        previewWorldHeight = worldHeight
        preparedCasts.values.toList().forEach { cast ->
            cast.seconds -= delta.coerceAtLeast(0f)
            if (cast.seconds <= 0f) castPrepared(cast.slot, player, enemyManager, worldWidth, worldHeight)
        }
        pendingExtendedAbilities.toList().forEach { (runtime, slot) ->
            if(runtime.weapon in inventory.allWeapons()) {
            val origin=abilityOrigin(runtime.weapon, player)
                executorFactory.extendedRuntime.updateAim((extendedCastAims[runtime] ?: pointerAim).x,
                    (extendedCastAims[runtime] ?: pointerAim).y)
                runtime.executeAbilityAttack(slot,player,origin.first,origin.second,enemyManager,projectiles,
                    onDamageDealt,onEnemyKilled,executorFactory.extendedRuntime::activate)
            }
        }
        pendingExtendedAbilities.clear()
        extendedCastAims.clear()
        executorFactory.extendedRuntime.updateAim(pointerAim.x,pointerAim.y)
        executorFactory.extendedRuntime.projectileVelocityBender = ::bendVortexProjectileVelocity
        executorFactory.extendedRuntime.update(delta,inventory.allWeapons().map { it.runtimeId }.toSet())

        visitFocus {
        resolvePendingVitriolicIgnitions(
            enemyManager =
                enemyManager,

            onDamageDealt =
                onDamageDealt,

            onEnemyKilled =
                onEnemyKilled
        )

        }
        erasingSoulsRuntime.update(delta)

        glyphMotionRuntime.update(
            delta =
                delta,

            playerCenterX =
                player.centerX,

            playerCenterY =
                player.centerY,

            enemies =
                enemyManager
                    .weaponTargets,

            onDamageDealt =
                onDamageDealt,

            onEnemyKilled =
                onEnemyKilled,

            onDrawingResolved = {
                glyphWeaponRuntime
                    ?.endActiveAbilityEarly()

                glyphWeaponRuntime =
                    null
            }
        )

        glyphMotionRuntime
            .consumePendingCastRequest()
            ?.let {
                    request ->
                val automatic = GameSettings.abilityCasting == GameSettings.AbilityCasting.AUTO
                val (originX, originY) = abilityOrigin(request.sourceWeapon, player)
                val automaticTarget = if (automatic) AbilityTargeting.select(enemyManager.weaponTargets,
                    originX,originY,abilityRangeFor(request.sourceWeapon)) else null
                val tsunami = request.glyphType == com.azizoglu.grisilith.weapon.glyph.GlyphType.TSUNAMI
                if (automatic && automaticTarget == null && !tsunami) return@let
                val center = if(tsunami)Vector2(player.centerX,player.centerY) else if (automatic) Vector2(automaticTarget!!.centerX,automaticTarget.centerY) else
                    Vector2(pointerAim).sub(originX, originY).limit(abilityRangeFor(request.sourceWeapon))
                        .add(originX, originY)
                glyphSpellRuntime.cast(
                    request =
                        request.copy(centerX = center.x, centerY = center.y),

                    enemyManager =
                        enemyManager,

                    worldWidth =
                        worldWidth,

                    worldHeight =
                        worldHeight,

                    onDamageDealt =
                        onDamageDealt,

                    onEnemyKilled =
                        onEnemyKilled,
                    manuallyAimed = true
                )
            }

        glyphSpellRuntime.projectileVelocityBender = ::bendVortexProjectileVelocity
        glyphSpellRuntime.update(
            delta =
                delta,

            player =
                player,

            enemyManager =
                enemyManager,

            projectiles =
                projectiles,

            worldWidth =
                worldWidth,

            worldHeight =
                worldHeight,

            onDamageDealt =
                onDamageDealt,

            onEnemyKilled =
                onEnemyKilled
        )

        statBonusSynchronizer
            .synchronize()

        defensiveLineRuntime.update(
            delta = delta,
            player = player,
            equippedWeapons =
                inventory.allWeapons()
        )

        physicalWeaponAbilityRuntime.update(
            delta = delta,
            player = player,
            enemyManager = enemyManager,
            projectiles = projectiles,
            attackOriginForSlot = { slot -> inventory.weaponAt(slot)?.let { abilityOrigin(it, player) }
                ?: attackOriginForSlot(slot) },
            onDamageDealt = onDamageDealt,
            onEnemyKilled = onEnemyKilled
        )

        // All shared projectiles participate, including active abilities, spells and summons.
        focusChannels.values.forEach { it.vortex.bendProjectiles(projectiles, delta) }
        visitFocus {
        igniteDashRuntime.update(
            delta =
                delta,

            player =
                player,

            enemyManager =
                enemyManager,

            obstacleBounds =
                mapObstacles.map {
                    it.bounds
                },
            castOriginX = igniteDashRuntime.aimingWeapon?.let { abilityOrigin(it, player).first } ?: player.centerX,
            castOriginY = igniteDashRuntime.aimingWeapon?.let { abilityOrigin(it, player).second } ?: player.centerY,

            worldWidth =
                worldWidth,

            worldHeight =
                worldHeight,

            onDamageDealt =
                onDamageDealt,

            onEnemyKilled =
                onEnemyKilled
        )

        vortexPullRuntime.update(
            delta =
                delta,

            enemies =
                enemyManager
                    .weaponTargets,

            currentStage =
                currentStageProvider(),

            plagueMoveSpeedPixelsPerSecond =
                45f +
                    playerStats
                        .totalScore(
                            StatType.HASTE
                        )
                        .toFloat(),

            onPullEnemy = {
                    enemy,
                    centerX,
                    centerY,
                    pullMultiplier,
                    frameDelta ->

                enemy.pullToward(
                    targetCenterX =
                        centerX,

                    targetCenterY =
                        centerY,

                    pullSpeedPixelsPerSecond =
                        VORTEX_BASE_PULL_SPEED *
                            pullMultiplier,

                    delta =
                        frameDelta,

                    worldWidth =
                        worldWidth,

                    worldHeight =
                        worldHeight
                )
            },

            onDamageTick = {
                    enemy,
                    tickIntervalSeconds ->

                applyVortexDamageTick(
                    enemy =
                        enemy,

                    tickIntervalSeconds =
                        tickIntervalSeconds,

                    onDamageDealt =
                        onDamageDealt,

                    onEnemyKilled =
                        onEnemyKilled
                )
            },

            onCompleted = {
                vortexWeaponRuntime
                    ?.endActiveAbilityEarly()

                vortexWeaponRuntime =
                    null

                vortexWeapon =
                    null
            }
        )

        staticCloudsRuntime.update(
            delta =
                delta,

            player =
                player,

            enemies =
                enemyManager
                    .weaponTargets,
            originX = staticCloudsWeapon?.let { abilityOrigin(it, player).first } ?: player.centerX,
            originY = staticCloudsWeapon?.let { abilityOrigin(it, player).second } ?: player.centerY,

            onDamageTick = {
                    enemy,
                    tickIntervalSeconds ->

                applyStaticCloudsDamageTick(
                    enemy =
                        enemy,

                    tickIntervalSeconds =
                        tickIntervalSeconds,

                    onDamageDealt =
                        onDamageDealt,

                    onEnemyKilled =
                        onEnemyKilled
                )
            },

            onCompleted = {
                staticCloudsWeaponRuntime
                    ?.endActiveAbilityEarly()

                staticCloudsWeaponRuntime =
                    null

                staticCloudsWeapon =
                    null
            }
        )

        acidicPoolRuntime.update(
            delta =
                delta,

            enemies =
                enemyManager
                    .weaponTargets,

            onDamageTick = {
                    enemy,
                    tickIntervalSeconds,
                    poolCenterX,
                    poolCenterY ->

                applyAcidicPoolDamageTick(
                    enemy =
                        enemy,

                    tickIntervalSeconds =
                        tickIntervalSeconds,

                    poolCenterX =
                        poolCenterX,

                    poolCenterY =
                        poolCenterY,

                    onDamageDealt =
                        onDamageDealt,

                    onEnemyKilled =
                        onEnemyKilled
                )
            },

            onCompleted = {
                acidicPoolWeaponRuntime
                    ?.endActiveAbilityEarly()

                acidicPoolWeaponRuntime =
                    null

                acidicPoolWeapon =
                    null
            }
        )

        }

        impaleRuntime.update(
            player =
                player
        )

        /*
         * Runtime'ları map sırasıyla değil gerçek
         * inventory slot sırasıyla güncelliyoruz.
         *
         * Quick Shot açısından slot 0, oyuncuya
         * gösterilen 1. Weapon slotudur.
         */
        inventory.weaponSlots
            .forEachIndexed {
                    slotIndex,
                    slot ->

                val weapon =
                    slot.weapon
                        ?: return@forEachIndexed

                val runtime =
                    runtimesByWeapon[
                        weapon
                    ]
                        ?: return@forEachIndexed

                val attackOrigin = attackOriginForSlot(slotIndex)

                runtime.update(
                    delta = delta,
                    slotIndex = slotIndex,
                    player = player,
                    attackOriginX = attackOrigin.first,
                    attackOriginY = attackOrigin.second,
                    enemyManager =
                        enemyManager,
                    projectiles =
                        projectiles,
                    onDamageDealt =
                        onDamageDealt,
                    onEnemyKilled =
                        onEnemyKilled,
                    onNormalAttackPerformed = {
                            attackedWeapon,
                            attacksPerSecond,
                            targetCenterX,
                            targetCenterY ->

                        onNormalAttackPerformed(
                            attackedWeapon,
                            attacksPerSecond,
                            targetCenterX,
                            targetCenterY
                        )
                    }
                )
            }
    }

    fun currentAttacksPerSecondFor(
        weapon: WeaponInstance
    ): Float {
        synchronizeWithInventory()

        return runtimesByWeapon[
            weapon
        ]
            ?.currentAttacksPerSecond()
            ?: weapon.definition
                .baseAttacksPerSecond
                .coerceAtLeast(
                    0.01f
                )
    }

    private fun synchronizeWithInventory() {
        val equippedWeapons =
            inventory.allWeapons()
        val ownedIds=equippedWeapons.map { it.runtimeId }.toSet()
        focusChannels.keys.toList().filter { it !in ownedIds }.forEach { id ->
            focusChannels.remove(id)?.let { channel ->
                channel.clear();channel.rules.forEach(combatInteractionManager::unregister)
            }
        }

        runtimesByWeapon.keys
            .toList()
            .filter { weapon ->
                weapon !in equippedWeapons
            }
            .forEach { removedWeapon ->
                runtimesByWeapon.remove(
                    removedWeapon
                )
            }

        equippedWeapons
            .forEach { weapon ->

                runtimesByWeapon
                    .getOrPut(
                        weapon
                    ) {
                        WeaponRuntime(
                            weapon = weapon,
                            playerStats =
                                playerStats,
                            executorFactory =
                                executorFactory,
                            tamgaRuntimeManager =
                                tamgaRuntimeManager,

                            characterTraitRuntime =
                                characterTraitRuntime,

                            abilityDamageMultiplierProvider = {
                                    activeWeapon,
                                    activePlayer,
                                    targetEnemy ->

                                impaleRuntime
                                    .damageMultiplierFor(
                                        weapon =
                                            activeWeapon,

                                        player =
                                            activePlayer,

                                        enemy =
                                            targetEnemy
                                    )
                            }
                        )
                    }
            }
    }

    fun applyCharacterTraitOnWeaponHit(
        event: WeaponHitEvent,
        onDamageDealt: (
            damage: Int,
            sourceName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
    ) {
        characterTraitRuntime.onWeaponHit(
            event = event,
            onDamageDealt = onDamageDealt,
            onEnemyKilled = onEnemyKilled
        )
    }

    fun modifyWeaponDamage(
        enemy: Enemy,
        baseDamage: Int
    ): Int {
        val tamgaModifiedDamage =
            tamgaRuntimeManager
                .modifyWeaponDamage(
                    enemy = enemy,
                    baseDamage = baseDamage
                )

        return characterTraitRuntime
            .modifyAttackDamage(
                tamgaModifiedDamage
            )
            .coerceAtLeast(1)
    }

    fun updateCharacterTrait(
        delta: Float,
        trySpawnToad: () -> Boolean
    ) {
        characterTraitRuntime
            .updateToadCycle(
                delta = delta,
                trySpawnToad = trySpawnToad
            )
    }

    fun notifyCharacterTraitEnemyKilled(
        enemy: Enemy
    ) {
        characterTraitRuntime
            .onEnemyKilled(
                enemy
            )
    }

    fun releaseAimedPhysicalAbilityEarly(): Boolean =
        physicalWeaponAbilityRuntime
            .releaseAimedCastEarly()

    /** World-click confirms the most recently prepared slot, never UI button clicks. */
    fun confirmPreparedAbilityAtMouse(player: Player, enemies: EnemyManager, width: Float, height: Float): Boolean {
        val slot = preparedCasts.keys.lastOrNull()
        if (slot != null) return castPrepared(slot, player, enemies, width, height)
        return releaseAimedPhysicalAbilityEarly()
    }

    fun tryActivateAbilityInSlot(
        slotIndex: Int,
        player: Player,
        enemyManager: EnemyManager,
        worldWidth: Float,
        worldHeight: Float
    ): Boolean {

        synchronizeWithInventory()

        val weapon =
            inventory.weaponAt(
                slotIndex
            )
                ?: return false

        val runtime =
            runtimesByWeapon[
                weapon
            ]
                ?: return false

        val ability =
            weapon.unlockedAbility
                ?: return false

        currentPlayer = player
        currentEnemies = enemyManager
        if (ability.id == DefaultWeaponAbilities.RAPID_STROKES_ID && !hasAbilityTarget(weapon, player, enemyManager)) {
            preparedCasts.remove(slotIndex)
            return false
        }
        if (!bypassPreparation && GameSettings.prepareAbilities && needsPreparation(weapon)) {
            if (preparedCasts.containsKey(slotIndex)) return castPrepared(slotIndex, player, enemyManager, worldWidth, worldHeight)
            if (runtime.abilityState != WeaponAbilityState.READY) return false
            preparedCasts[slotIndex] = PreparedCast(weapon, slotIndex)
            return true
        }

        val auto = GameSettings.abilityCasting == GameSettings.AbilityCasting.AUTO
        if (auto && (needsPreparation(weapon) || ability.id == DefaultWeaponAbilities.IGNITE_DASH_ID ||
                ability.id == DefaultWeaponAbilities.KNIFE_SWARM_ID || weapon.definition.id.value == "WPN_BOOMERANG")) {
            val (originX, originY) = abilityOrigin(weapon, player)
            val target = AbilityTargeting.select(enemyManager.weaponTargets, originX, originY, abilityRangeFor(weapon))
            if (target == null && needsPreparation(weapon)) return false
            if (target != null) setCastAim(target.centerX, target.centerY) else setCastAim(pointerAim.x, pointerAim.y)
        } else setCastAim(pointerAim.x, pointerAim.y)

        if(executorFactory.extendedRuntime.canHandle(weapon)) {
            if(!runtime.tryActivateAbility()) return false
            pendingExtendedAbilities += runtime to slotIndex
            extendedCastAims[runtime] = castAim.cpy()
            return true
        }

        val previousFocus=selectedFocus
        if(ability.id in setOf(DefaultWeaponAbilities.IGNITE_DASH_ID,DefaultWeaponAbilities.VORTEX_PULL_ID,
            DefaultWeaponAbilities.STATIC_CLOUDS_ID,DefaultWeaponAbilities.ACIDIC_POOL_ID)) selectedFocus=focusFor(weapon)
        return try {
        when (
            ability.id
        ) {
            DefaultWeaponAbilities.RAPID_STROKES_ID,
            DefaultWeaponAbilities.AIR_SLASH_ID,
            DefaultWeaponAbilities.SPIN_ATTACK_ID,
            DefaultWeaponAbilities.DEFENSIVE_LINE_BURST_ID,
            DefaultWeaponAbilities.KNIFE_SWARM_ID,
            DefaultWeaponAbilities.LEISURELY_SHOT_ID,
            DefaultWeaponAbilities.HEAVY_BOLTS_ID -> {
                physicalWeaponAbilityRuntime.tryActivate(
                    weapon = weapon,
                    runtime = runtime,
                    slotIndex = slotIndex,
                    player = player
                )
            }

            DefaultWeaponAbilities
                .IGNITE_DASH_ID -> {
                igniteDashRuntime.updateAimPosition(castAim.x,castAim.y)
                igniteDashRuntime
                    .handleActivation(
                        weapon =
                            weapon,

                        weaponRuntime =
                            runtime
                    )
            }

            DefaultWeaponAbilities
                .VORTEX_PULL_ID -> {

                tryActivateVortexPull(
                    weaponRuntime =
                        runtime,

                    player =
                        player,

                    enemyManager =
                        enemyManager,

                    worldWidth =
                        worldWidth,

                    worldHeight =
                        worldHeight
                )
            }

            DefaultWeaponAbilities
                .STATIC_CLOUDS_ID -> {

                tryActivateStaticClouds(
                    weaponRuntime =
                        runtime,

                    player =
                        player
                )
            }

            DefaultWeaponAbilities
                .ACIDIC_POOL_ID -> {

                tryActivateAcidicPool(
                    weaponRuntime =
                        runtime,

                    player =
                        player,

                    enemyManager =
                        enemyManager
                )
            }

            DefaultWeaponAbilities
                .IMPALE_AND_DRAG_ID -> {

                impaleRuntime
                    .tryBegin(
                        weaponRuntime =
                            runtime,

                        player =
                            player
                    )
            }

            DefaultWeaponAbilities
                .GLYPH_CASTING_ID -> {

                when (
                    glyphMotionRuntime
                        .handleActivation(
                            weapon
                        )
                ) {
                    GlyphMotionRuntime
                        .ActivationResult
                        .STARTED -> {

                        val activated =
                            runtime
                                .tryActivateAbility()

                        if (
                            activated
                        ) {
                            glyphWeaponRuntime =
                                runtime
                        } else {
                            glyphMotionRuntime
                                .cancelActiveDrawing()
                        }

                        activated
                    }

                    GlyphMotionRuntime
                        .ActivationResult
                        .FINISHED -> {

                        runtime
                            .endActiveAbilityEarly()

                        glyphWeaponRuntime =
                            null

                        true
                    }

                    GlyphMotionRuntime
                        .ActivationResult
                        .REJECTED -> {

                        false
                    }
                }
            }

            else -> {
                runtime
                    .tryActivateAbility()
            }
        }
        } finally { selectedFocus=previousFocus }
    }

    private fun tryActivateVortexPull(
        weaponRuntime: WeaponRuntime,
        player: Player,
        enemyManager: EnemyManager,
        worldWidth: Float,
        worldHeight: Float
    ): Boolean {
        if (
            weaponRuntime.abilityState !=
            WeaponAbilityState.READY ||
            vortexPullRuntime.isActive
        ) {
            return false
        }

        /*
         * Önce uygun alan ve hedef kontrol edilir.
         * Başarısızsa WeaponAbilityRuntime ACTIVE yapılmadığı için
         * cooldown başlamaz.
         */
        val areaStarted =
            vortexPullRuntime
                .tryBegin(
                    playerCenterX =
                        abilityOrigin(weaponRuntime.weapon, player).first,

                    playerCenterY =
                        abilityOrigin(weaponRuntime.weapon, player).second,

                    enemies =
                        enemyManager
                            .weaponTargets,

                    worldWidth =
                        worldWidth,

                    worldHeight =
                        worldHeight,

                    upgraded =
                        weaponRuntime.weapon.hasWeaponAbilityUpgrade,
                    aimPosition = pointerAim.takeIf { GameSettings.abilityCasting != GameSettings.AbilityCasting.AUTO }
                )

        if (!areaStarted) {
            return false
        }

        val abilityActivated =
            weaponRuntime
                .tryActivateAbility()

        if (!abilityActivated) {
            vortexPullRuntime.clear()
            return false
        }

        vortexWeaponRuntime =
            weaponRuntime

        vortexWeapon =
            weaponRuntime.weapon

        return true
    }

    private fun tryActivateStaticClouds(
        weaponRuntime: WeaponRuntime,
        player: Player
    ): Boolean {
        if (staticCloudsRuntime.isActive) return staticCloudsRuntime.freezeAtCurrentPosition()
        if (
            weaponRuntime.abilityState !=
            WeaponAbilityState.READY ||
            staticCloudsRuntime.isActive
        ) {
            return false
        }

        val areaStarted =
            staticCloudsRuntime
                .tryBegin(
                    player,
                    weaponRuntime.weapon.hasWeaponAbilityUpgrade,
                    abilityOrigin(weaponRuntime.weapon, player).first,
                    abilityOrigin(weaponRuntime.weapon, player).second
                )

        if (!areaStarted) {
            return false
        }

        val activated =
            weaponRuntime
                .tryActivateAbility()

        if (!activated) {
            staticCloudsRuntime.clear()
            return false
        }

        staticCloudsWeaponRuntime =
            weaponRuntime

        staticCloudsWeapon =
            weaponRuntime.weapon

        return true
    }

    private fun tryActivateAcidicPool(
        weaponRuntime: WeaponRuntime,
        player: Player,
        enemyManager: EnemyManager
    ): Boolean {

        if (
            weaponRuntime.abilityState !=
            WeaponAbilityState.READY ||
            acidicPoolRuntime.isActive
        ) {
            return false
        }

        /*
         * Önce menzilde hedef aranır ve alan oluşturulur.
         * Hedef yoksa ability ACTIVE yapılmaz;
         * dolayısıyla cooldown başlamaz.
         */
        val areaStarted =
            acidicPoolRuntime
                .tryBegin(
                    playerCenterX =
                        abilityOrigin(weaponRuntime.weapon, player).first,

                    playerCenterY =
                        abilityOrigin(weaponRuntime.weapon, player).second,

                    enemies =
                        enemyManager
                            .weaponTargets,

                    upgraded =
                        weaponRuntime.weapon.hasWeaponAbilityUpgrade,
                    aimPosition = pointerAim.takeIf { GameSettings.abilityCasting != GameSettings.AbilityCasting.AUTO }
                )

        if (!areaStarted) {
            return false
        }

        val activated =
            weaponRuntime
                .tryActivateAbility()

        if (!activated) {
            acidicPoolRuntime.clear()
            return false
        }

        acidicPoolWeaponRuntime =
            weaponRuntime

        acidicPoolWeapon =
            weaponRuntime.weapon

        return true
    }

    private fun resolvePendingVitriolicIgnitions(
        enemyManager: EnemyManager,
        onDamageDealt: (
            damage: Int,
            weaponName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
    ) {

        val weapon =
            acidicPoolWeapon
                ?: run {
                    pendingVitriolicIgnitions
                        .clear()

                    return
                }

        val pool =
            acidicPoolRuntime
                .field
                ?: run {
                    pendingVitriolicIgnitions
                        .clear()

                    return
                }

        while (
            pendingVitriolicIgnitions
                .isNotEmpty()
        ) {
            pendingVitriolicIgnitions
                .removeFirst()

            val explosionDamage =
                (
                    activeSkillDamageCalculator
                        .calculateSpellSkillDamage(
                            weapon =
                                weapon,

                            damageType =
                                DamageType.FIRE
                        ) *
                        VITRIOLIC_IGNITION_DAMAGE_MULTIPLIER
                    )
                    .roundToInt()
                    .coerceAtLeast(
                        1
                    )

            val explosionRadius =
                VITRIOLIC_IGNITION_RADIUS

            enemyManager
                .weaponTargets
                .toList()
                .forEach {
                        enemy ->

                    if (
                        enemy.isDead ||
                        Vector2.dst2(
                            pool.center.x,
                            pool.center.y,
                            enemy.centerX,
                            enemy.centerY
                        ) >
                        explosionRadius *
                        explosionRadius
                    ) {
                        return@forEach
                    }

                    val wasAlive =
                        !enemy.isDead

                    val appliedDamage =
                        enemy.takeDamage(
                            amount =
                                explosionDamage,

                            damageContext =
                                EnemyDamageContext(
                                    damageType =
                                        DamageType.FIRE,

                                    sourceWeaponRuntimeId =
                                        weapon.runtimeId,

                                    sourceWeaponDefinitionId =
                                        weapon.definition.id.value,

                                    sourceName =
                                        weapon.displayName +
                                            " — Vitriolic Ignition"
                                )
                        )

                    if (
                        appliedDamage >
                        0
                    ) {
                        enemy.applyCondition(
                            ConditionDamageCalculator
                                .createApplication(
                                    type =
                                        ConditionType.BURNED,

                                    playerStats =
                                        playerStats,

                                    durationSeconds =
                                        DefaultWeaponAbilities
                                            .ACIDIC_POOL_CORRODING_DURATION_SECONDS,

                                    sourceWeaponRuntimeId =
                                        weapon.runtimeId,

                                    sourceWeaponDefinitionId =
                                        weapon.definition.id.value,

                                    sourceName =
                                        weapon.displayName +
                                            " — Vitriolic Ignition",

                                    sourceCenterX =
                                        pool.center.x,

                                    sourceCenterY =
                                        pool.center.y
                                )
                        )

                        onDamageDealt(
                            appliedDamage,
                            weapon.displayName +
                                " — Vitriolic Ignition"
                        )
                    }

                    if (
                        wasAlive &&
                        enemy.isDead
                    ) {
                        onEnemyKilled(
                            enemy
                        )
                    }
                }
        }
    }

    private fun applyAcidicPoolDamageTick(
        enemy: Enemy,
        tickIntervalSeconds: Float,
        poolCenterX: Float,
        poolCenterY: Float,
        onDamageDealt: (
            damage: Int,
            weaponName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
    ) {

        val weapon =
            acidicPoolWeapon
                ?: return

        if (
            enemy.isDead ||
            tickIntervalSeconds <=
            0f
        ) {
            return
        }

        val rawTickDamage =
            activeSkillDamageCalculator.calculateMaximumHealthSpellTickDamage(
                    enemy.health.maximumHealth,
                    DamageType.ACID,
                    DefaultWeaponAbilities.ACIDIC_POOL_DAMAGE_PER_SECOND_MULTIPLIER
                )
                .coerceAtLeast(
                    1
                )

        val modifiedDamage = rawTickDamage

        val wasAlive =
            !enemy.isDead

        val sourceName =
            weapon.displayName +
                " — Acidic Pool"

        val appliedDamage =
            enemy.takeDamage(
                amount =
                    modifiedDamage,

                damageContext =
                    EnemyDamageContext(
                        defenseTimeScale = tickIntervalSeconds,
                        damageType =
                            DamageType.ACID,

                        sourceWeaponRuntimeId =
                            weapon.runtimeId,

                        sourceWeaponDefinitionId =
                            weapon
                                .definition
                                .id
                                .value,

                        sourceName =
                            sourceName
                    )
            )

        if (
            appliedDamage >
            0
        ) {
            enemy.applyCondition(
                ConditionDamageCalculator
                    .createApplication(
                        type =
                            ConditionType.CORRODING,

                        playerStats =
                            playerStats,

                        durationSeconds =
                            DefaultWeaponAbilities
                                .ACIDIC_POOL_CORRODING_DURATION_SECONDS,

                        sourceWeaponRuntimeId =
                            weapon.runtimeId,

                        sourceWeaponDefinitionId =
                            weapon
                                .definition
                                .id
                                .value,

                        sourceName =
                            sourceName,

                        sourceCenterX =
                            poolCenterX,

                        sourceCenterY =
                            poolCenterY
                    )
            )

            if (
                acidicPoolRuntime
                    .isVitriolicIgnitionActive
            ) {
                enemy.applyCondition(
                    ConditionDamageCalculator
                        .createApplication(
                            type =
                                ConditionType.BURNED,

                            playerStats =
                                playerStats,

                            durationSeconds =
                                DefaultWeaponAbilities
                                    .ACIDIC_POOL_CORRODING_DURATION_SECONDS,

                            sourceWeaponRuntimeId =
                                weapon.runtimeId,

                            sourceWeaponDefinitionId =
                                weapon.definition.id.value,

                            sourceName =
                                weapon.displayName +
                                    " — Vitriolic Ignition",

                            sourceCenterX =
                                poolCenterX,

                            sourceCenterY =
                                poolCenterY
                        )
                )
            }

            onDamageDealt(
                appliedDamage,
                sourceName
            )
        }

        if (
            wasAlive &&
            enemy.isDead
        ) {
            onEnemyKilled(
                enemy
            )
        }
    }

    private fun applyStaticCloudsDamageTick(
        enemy: Enemy,
        tickIntervalSeconds: Float,
        onDamageDealt: (
            damage: Int,
            weaponName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
    ) {
        val weapon =
            staticCloudsWeapon
                ?: return

        if (
            enemy.isDead ||
            tickIntervalSeconds <=
            0f
        ) {
            return
        }

        val lightningDamage =
            activeSkillDamageCalculator.calculateMaximumHealthSpellTickDamage(
                    enemy.health.maximumHealth,
                    DamageType.LIGHTNING,
                    DefaultWeaponAbilities.STATIC_CLOUDS_LIGHTNING_DPS_MULTIPLIER
                )
                .coerceAtLeast(
                    1
                )

        val modifiedDamage = lightningDamage

        val wasAlive =
            !enemy.isDead

        val appliedDamage =
            enemy.takeDamage(
                amount =
                    modifiedDamage,

                damageContext =
                    EnemyDamageContext(
                        defenseTimeScale = tickIntervalSeconds,
                        damageType =
                            DamageType.LIGHTNING,

                        sourceWeaponRuntimeId =
                            weapon.runtimeId,

                        sourceWeaponDefinitionId =
                            weapon.definition.id.value,

                        sourceName =
                            weapon.displayName +
                                " — Static Clouds"
                    )
            )

        if (
            appliedDamage >
            0
        ) {
            onDamageDealt(
                appliedDamage,
                weapon.displayName
            )

            tryApplyStaticCloudsStun(
                enemy
            )
        }

        if (
            wasAlive &&
            enemy.isDead
        ) {
            onEnemyKilled(
                enemy
            )
        }
    }

    private fun tryApplyStaticCloudsStun(
        enemy: Enemy
    ) {
        if (
            enemy.isDead ||
            enemy.type ==
            EnemyType.MINI_BOSS ||
            enemy.type ==
            EnemyType.FINAL_BOSS
        ) {
            return
        }

        if (
            MathUtils.random() >
            if (staticCloudsWeapon?.hasWeaponAbilityUpgrade == true) {
                DefaultWeaponAbilities.STATIC_CLOUDS_UPGRADED_STUN_CHANCE
            } else {
                DefaultWeaponAbilities.STATIC_CLOUDS_STUN_CHANCE
            }
        ) {
            return
        }

        enemy.applyCondition(
            conditionType =
                ConditionType.STUNNED,

            durationSeconds =
                DefaultWeaponAbilities
                    .STATIC_CLOUDS_STUN_DURATION_SECONDS
        )
    }

    private fun applyVortexDamageTick(
        enemy: Enemy,
        tickIntervalSeconds: Float,
        onDamageDealt: (
            damage: Int,
            weaponName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
    ) {
        val weapon =
            vortexWeapon
                ?: return

        if (
            enemy.isDead ||
            tickIntervalSeconds <=
            0f
        ) {
            return
        }

        val wasAlive =
            !enemy.isDead

        val vortexRawDamage =
            activeSkillDamageCalculator.calculateMaximumHealthSpellTickDamage(
                    enemy.health.maximumHealth,
                    DamageType.AIR,
                    DefaultWeaponAbilities.VORTEX_PULL_DAMAGE_PER_SECOND_MULTIPLIER
                )
                .coerceAtLeast(
                    1
                )

        val vortexDamage = vortexRawDamage

        var totalAppliedDamage =
            enemy.takeDamage(
                amount =
                    vortexDamage,

                damageContext =
                    EnemyDamageContext(
                        defenseTimeScale = tickIntervalSeconds,
                        damageType =
                            DamageType.AIR,

                        sourceWeaponRuntimeId =
                            weapon.runtimeId,

                        sourceWeaponDefinitionId =
                            weapon.definition.id.value,

                        sourceName =
                            weapon.displayName +
                                " — Vortex Pull"
                    )
            )

        if (
            vortexPullRuntime
                .isPlagueStormActive &&
            !enemy.isDead
        ) {
            val plagueRawDamage =
                activeSkillDamageCalculator.calculateMaximumHealthSpellTickDamage(
                        enemy.health.maximumHealth,
                        DamageType.NECROTIC,
                        PLAGUE_STORM_NECROTIC_TICK_MULTIPLIER
                    )
                    .coerceAtLeast(
                        1
                    )

            val plagueDamage = plagueRawDamage

            totalAppliedDamage +=
                enemy.takeDamage(
                    amount =
                        plagueDamage,

                    damageContext =
                        EnemyDamageContext(
                            defenseTimeScale = tickIntervalSeconds,
                            damageType =
                                DamageType.NECROTIC,

                            sourceWeaponRuntimeId =
                                weapon.runtimeId,

                            sourceWeaponDefinitionId =
                                weapon.definition.id.value,

                            sourceName =
                                weapon.displayName +
                                    " — Plague Storm"
                        )
                )

            if (!enemy.isDead) {
                enemy.applyCondition(
                    ConditionDamageCalculator
                        .createApplication(
                            type =
                                ConditionType.NECROSIS,

                            playerStats =
                                playerStats,

                            durationSeconds =
                                PLAGUE_STORM_NECROSIS_SECONDS,

                            sourceWeaponRuntimeId =
                                weapon.runtimeId,

                            sourceWeaponDefinitionId =
                                weapon.definition.id.value,

                            sourceName =
                                weapon.displayName +
                                    " — Plague Storm",

                            sourceCenterX =
                                vortexPullRuntime
                                    .field
                                    ?.center
                                    ?.x,

                            sourceCenterY =
                                vortexPullRuntime
                                    .field
                                    ?.center
                                    ?.y
                        )
                )
            }
        }

        if (
            totalAppliedDamage >
            0
        ) {
            onDamageDealt(
                totalAppliedDamage,
                if (
                    vortexPullRuntime
                        .isPlagueStormActive
                ) {
                    weapon.displayName +
                        " — Plague Storm"
                } else {
                    weapon.displayName +
                        " — Vortex Pull"
                }
            )
        }

        if (
            wasAlive &&
            enemy.isDead
        ) {
            onEnemyKilled(
                enemy
            )
        }
    }

    fun vortexMovementMultiplier(enemy: Enemy): Float =
        focusChannels.values.minOfOrNull { it.vortex.movementMultiplier(enemy) } ?: 1f

    fun bendVortexProjectileVelocity(x: Float, y: Float, velocity: Vector2, delta: Float) {
        focusChannels.values.forEach { it.vortex.bendProjectileVelocity(x,y,velocity,delta) }
    }

    fun resolveErasingSoulsExplosion(
        initialDeadEnemy: Enemy,
        player: Player,
        enemyManager: EnemyManager,
        onDamageDealt: (Int, String) -> Unit
    ): List<Enemy> {
        val activeTomes =
            inventory.allWeapons().filter { weapon ->
                weapon.unlockedAbility?.id ==
                    DefaultWeaponAbilities.ERASING_SOULS_ID
            }

        if (activeTomes.isEmpty()) {
            return emptyList()
        }

        val lethalContext =
            initialDeadEnemy.lethalDamageContext
                ?: return emptyList()

        val sourceIsQualifiedTome =
            lethalContext.sourceWeaponRuntimeId?.let { id ->
                activeTomes.any { it.runtimeId == id }
            } ?: false

        val wasKilledByNecroticDamage =
            lethalContext.damageType ==
                DamageType.NECROTIC

        if (!sourceIsQualifiedTome &&
            !wasKilledByNecroticDamage
        ) {
            return emptyList()
        }

        val sourceWeapon =
            activeTomes.firstOrNull { tome ->
                tome.runtimeId ==
                    lethalContext.sourceWeaponRuntimeId
            } ?: activeTomes.first()

        val explosionDamage =
            (
                activeSkillDamageCalculator
                    .calculateSpellSkillDamage(
                        weapon =
                            sourceWeapon,

                        damageType =
                            DamageType.NECROTIC
                    ) *
                    if (sourceWeapon.hasWeaponAbilityUpgrade) {
                        DefaultWeaponAbilities.ERASING_SOULS_UPGRADED_DAMAGE_MULTIPLIER
                    } else {
                        DefaultWeaponAbilities.ERASING_SOULS_DAMAGE_MULTIPLIER
                    }
                )
                .roundToInt()
                .coerceAtLeast(
                    1
                )

        return erasingSoulsRuntime.resolveExplosion(
            initialDeadEnemy = initialDeadEnemy,
            sourceWeapon = sourceWeapon,
            playerCenterX = player.centerX,
            playerCenterY = player.centerY,
            enemies = enemyManager.weaponTargets,
            explosionDamage = explosionDamage,
            playerStats = playerStats,
            onDamageDealt = onDamageDealt,

            onExplosionStarted = {
                    deadEnemy ->

                combatInteractionManager
                    .notify(
                        CombatInteractionEvent(
                            trigger =
                                CombatInteractionTrigger
                                    .ERASING_SOULS_EXPLOSION,

                            enemy =
                                deadEnemy,

                            worldX =
                                deadEnemy.centerX,

                            worldY =
                                deadEnemy.centerY,

                            sourceWeaponRuntimeId =
                                sourceWeapon.runtimeId,

                            sourceWeaponDefinitionId =
                                sourceWeapon.definition.id.value,

                            sourceName =
                                sourceWeapon.displayName +
                                    " — Erasing the Souls"
                        )
                    )
            }
        )
    }

    fun onEnemyKilledForActiveSkills(
        enemy: Enemy
    ) {
        /*
         * Compatibility hook retained for FirstScreen.
         * Reworked Impale has no corpse-drag/reward behavior.
         */
    }

    fun consumeImpaleAndDragReward():
        ImpaleAndDragReward? {

        /*
         * Compatibility API only. Impale no longer creates bonus Gold or
         * Chest rewards, so there is never anything to consume.
         */
        return null
    }

    fun updateActiveSkillAim(
        worldX: Float,
        worldY: Float
    ) {
        WeaponTargetSelector.updateMouse(worldX, worldY)
        pointerAim.set(worldX, worldY)
        setCastAim(worldX, worldY)
    }

    private fun setCastAim(worldX: Float, worldY: Float) {
        castAim.set(worldX,worldY)
        executorFactory.extendedRuntime.updateAim(worldX,worldY)
        physicalWeaponAbilityRuntime.updateAim(
            worldX = worldX,
            worldY = worldY
        )

        visitFocus { igniteDashRuntime
            .updateAimPosition(
                worldX =
                    worldX,

                worldY =
                    worldY
            )
        }
    }

    fun cancelActiveAbilities() {
        preparedCasts.clear()
        extendedCastAims.clear()
        physicalWeaponAbilityRuntime.clear()
        pendingExtendedAbilities.clear()
        executorFactory.extendedRuntime.clear()

        impaleRuntime
            .cancel()

        visitFocus {
        igniteDashRuntime.cancel()
        vortexPullRuntime.clear()
        vortexWeaponRuntime = null
        vortexWeapon = null

        staticCloudsRuntime.clear()
        staticCloudsWeaponRuntime = null
        staticCloudsWeapon = null

        acidicPoolRuntime.clear()
        acidicPoolWeaponRuntime = null
        acidicPoolWeapon = null
        pendingVitriolicIgnitions.clear()
        }


        erasingSoulsRuntime.clear()

        glyphMotionRuntime.clear()
        glyphSpellRuntime.clear()
        glyphWeaponRuntime = null

        runtimesByWeapon.values
            .forEach {
                    runtime ->

                if (
                    runtime.abilityState ==
                    WeaponAbilityState.ACTIVE
                ) {
                    runtime
                        .endActiveAbilityEarly()
                }
            }
    }

    fun renderActiveSkillEffects(shapeRenderer: ShapeRenderer, player: Player) {
        renderRangeIndicators(shapeRenderer, player)
        executorFactory.extendedRuntime.render(shapeRenderer)
        physicalWeaponAbilityRuntime.render(shapeRenderer,player)
        defensiveLineVisualRenderer.render(shapeRenderer,player,defensiveLineRuntime.bonusArmorClass)
        val channels=focusChannels.values.toList().ifEmpty { listOf(fallbackFocus) }
        channels.forEachIndexed { index,channel -> inFocus(channel) {
            val origin = igniteDashRuntime.aimingWeapon?.let { abilityOrigin(it, player) }
                ?: (player.centerX to player.centerY)
            igniteDashRuntime.render(shapeRenderer,player,origin.first,origin.second)
            if(spellFocusEffectSpriteRenderer.hasAnythingToRender(staticCloudsRuntime,igniteDashRuntime,
                erasingSoulsRuntime,vortexPullRuntime,acidicPoolRuntime,glyphSpellRuntime,index==0)) {
                val projection=shapeRenderer.projectionMatrix.cpy()
                shapeRenderer.end()
                spellFocusEffectSpriteRenderer.render(projection,staticCloudsRuntime,igniteDashRuntime,
                    erasingSoulsRuntime,vortexPullRuntime,acidicPoolRuntime,glyphSpellRuntime,index==0)
                shapeRenderer.projectionMatrix=projection
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            }
        } }
        glyphMotionRuntime.render(shapeRenderer)
        glyphSpellRuntime.render(shapeRenderer)
    }


    private fun renderRangeIndicators(renderer: ShapeRenderer, player: Player) {
        com.badlogic.gdx.Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND)
        com.badlogic.gdx.Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
            com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA)
        fun ring(radius: Float, width: Float, color: Color, x: Float = player.centerX, y: Float = player.centerY) {
            if (radius <= 0f) return
            renderer.color = color
            val segments = 180
            repeat(segments) { i ->
                val a = i * MathUtils.PI2 / segments
                val b = (i + 1) * MathUtils.PI2 / segments
                renderer.rectLine(x + MathUtils.cos(a) * radius,
                    y + MathUtils.sin(a) * radius,
                    x + MathUtils.cos(b) * radius,
                    y + MathUtils.sin(b) * radius, width)
            }
        }
        inventory.allWeapons().forEach { weapon ->
            val main = inventory.weaponAt(0) === weapon
            val (originX, originY) = abilityOrigin(weapon, player)
            if (GameSettings.normalRange == GameSettings.NormalRange.ALL ||
                (main && GameSettings.normalRange == GameSettings.NormalRange.MAIN)) {
                val slot = inventory.weaponSlots.firstOrNull { it.weapon === weapon }?.index ?: 0
                val attackOrigin = abilityOriginForSlot?.invoke(slot) ?: (player.centerX to player.centerY)
                ring(normalRange(weapon), 1f, Color.WHITE, attackOrigin.first, attackOrigin.second)
            }
            val preparing = preparedCasts.values.any { it.weapon === weapon } ||
                focusChannels[weapon.runtimeId]?.ignite?.isAiming == true || glyphMotionRuntime.preparingWeapon === weapon ||
                physicalWeaponAbilityRuntime.isPreparing(weapon)
            if (weapon.chalkDustSpellChoice?.glyphType != com.azizoglu.grisilith.weapon.glyph.GlyphType.TSUNAMI &&
                weapon.unlockedAbility?.activationType == com.azizoglu.grisilith.weapon.skill.WeaponAbilityActivationType.ACTIVE && (GameSettings.abilityRange == GameSettings.AbilityRange.ALL ||
                (main && GameSettings.abilityRange == GameSettings.AbilityRange.MAIN) ||
                (GameSettings.abilityRange == GameSettings.AbilityRange.PREPARING && preparing))) {
                ring(abilityRangeFor(weapon), 1.8f, Color.RED, originX, originY)
            }
        }
        preparedCasts.values.forEach { cast ->
            val weapon = cast.weapon
            val (originX, originY) = abilityOrigin(weapon, player)
            val range = abilityRangeFor(weapon)
            val d = Vector2(pointerAim).sub(originX, originY)
            if (d.isZero(.001f)) d.set(1f, 0f)
            d.nor()
            val angle = d.angleDeg()
            renderer.setColor(1f, .08f, .08f, .5f)
            when (weapon.unlockedAbility?.id) {
                DefaultWeaponAbilities.AIR_SLASH_ID -> {
                    renderer.arc(originX,originY,range,angle-DefaultWeaponAbilities.AIR_SLASH_ANGLE_DEGREES/2f,
                        DefaultWeaponAbilities.AIR_SLASH_ANGLE_DEGREES,100)
                }
                DefaultWeaponAbilities.SPIN_ATTACK_ID -> renderer.circle(originX, originY, range, 100)
                DefaultWeaponAbilities.RAPID_STROKES_ID -> {
                    val target = currentEnemies?.weaponTargets?.filter { !it.isDead &&
                        Vector2.dst(originX,originY,it.centerX,it.centerY) <= range+it.bounds.width/2f }
                        ?.minByOrNull { Vector2.dst2(pointerAim.x,pointerAim.y,it.centerX,it.centerY) }
                    if (target != null) {
                        val lineLength = (if (weapon.hasBasicAttackUpgrade) 125f else 95f) *
                            if (weapon.hasWeaponAbilityUpgrade) DefaultWeaponAbilities.RAPID_STROKES_UPGRADED_SIZE_MULTIPLIER else 1f
                        val toward = Vector2(target.centerX-originX,target.centerY-originY).nor()
                        val rotations = if (weapon.hasBasicAttackUpgrade) listOf(45f,-45f) else listOf(90f)
                        rotations.forEach { rotation ->
                            val slash = toward.cpy().rotateDeg(rotation).scl(lineLength/2f)
                            renderer.rectLine(target.centerX-slash.x,target.centerY-slash.y,
                                target.centerX+slash.x,target.centerY+slash.y,30f)
                            renderer.circle(target.centerX-slash.x,target.centerY-slash.y,15f,48)
                            renderer.circle(target.centerX+slash.x,target.centerY+slash.y,15f,48)
                        }
                    }
                }
                DefaultWeaponAbilities.VORTEX_PULL_ID, DefaultWeaponAbilities.ACIDIC_POOL_ID -> {
                    val center = Vector2(pointerAim).sub(originX, originY).limit(range).add(originX, originY)
                    if (weapon.unlockedAbility?.id == DefaultWeaponAbilities.VORTEX_PULL_ID) {
                        val radius = DefaultWeaponAbilities.VORTEX_PULL_RADIUS
                        center.x = center.x.coerceIn(radius, kotlin.math.max(radius, previewWorldWidth-radius))
                        center.y = center.y.coerceIn(radius, kotlin.math.max(radius, previewWorldHeight-radius))
                    }
                    renderer.circle(center.x, center.y, if (weapon.unlockedAbility?.id == DefaultWeaponAbilities.VORTEX_PULL_ID)
                        DefaultWeaponAbilities.VORTEX_PULL_RADIUS else DefaultWeaponAbilities.ACIDIC_POOL_RADIUS, 100)
                }
                else -> when (weapon.definition.id.value) {
                    "WPN_WARHAMMER" -> repeat(if (weapon.hasWeaponAbilityUpgrade) 4 else 3) { i ->
                        renderer.circle(originX + d.x * (90f + i * 90f), originY + d.y * (90f + i * 90f),
                            if (weapon.hasWeaponAbilityUpgrade) 84f else 70f, 80)
                    }
                    "WPN_WHIP" -> {
                        val sweep = if (weapon.hasWeaponAbilityUpgrade) 120f else 80f
                        renderer.arc(originX, originY, range, angle - sweep / 2f, sweep, 100)
                    }
                    "WPN_SLING" -> {
                        val center=Vector2(pointerAim).sub(originX,originY).limit(range).add(originX,originY)
                        val radius=75f * if (weapon.hasWeaponAbilityUpgrade) 1.25f else 1f
                        renderer.circle(center.x,center.y,radius,100)
                        if (weapon.hasWeaponAbilityUpgrade) renderer.circle(center.x+d.x*160f,center.y+d.y*160f,radius,100)
                    }
                    else -> {
                        val bow = weapon.unlockedAbility?.id==DefaultWeaponAbilities.LEISURELY_SHOT_ID
                        val offsets = if(bow)listOf(-20f,-10f,0f,10f,20f) else listOf(0f)
                        offsets.forEach { offset ->
                            val line=d.cpy().rotateDeg(offset)
                            renderer.rectLine(originX,originY,originX+line.x*range,originY+line.y*range,if(bow)8f else 12f)
                        }
                    }
                }
            }
        }
        glyphMotionRuntime.preparingWeapon?.takeIf { GameSettings.prepareAbilities }?.let { weapon ->
            val (originX, originY) = abilityOrigin(weapon, player)
            val center = Vector2(pointerAim).sub(originX, originY)
                .limit(abilityRangeFor(weapon)).add(originX, originY)
            renderer.setColor(1f, .08f, .08f, .5f)
            when (weapon.chalkDustSpellChoice?.glyphType) {
                com.azizoglu.grisilith.weapon.glyph.GlyphType.METEOR -> renderer.circle(center.x,center.y,185f,120)
                com.azizoglu.grisilith.weapon.glyph.GlyphType.CALL_LIGHTNING -> renderer.circle(center.x,center.y,240f,120)
                com.azizoglu.grisilith.weapon.glyph.GlyphType.SINGULARITY -> {
                    renderer.circle(center.x,center.y,if (weapon.hasWeaponAbilityUpgrade) 300f else 222f,120)
                    // Initial explosion radius; deaths during the field dynamically grow it.
                    ring(if (weapon.hasWeaponAbilityUpgrade) 300f else 250f, 1.8f, Color.RED, center.x, center.y)
                }
                com.azizoglu.grisilith.weapon.glyph.GlyphType.TSUNAMI -> {
                    // Map-wide spell: no aimed preparation area.
                }
                null -> Unit
            }
        }
        tamgaRuntimeManager.chargerPreviewRadius()?.let { ring(it, 1f, Color.GRAY) }
        renderer.color = Color.WHITE
    }

    fun glyphFeedback():
        GlyphFeedback? {

        return glyphMotionRuntime
            .feedback()
    }

    fun abilityStateInSlot(
        slotIndex: Int
    ): WeaponAbilityState {

        val weapon =
            inventory.weaponAt(
                slotIndex
            )
                ?: return WeaponAbilityState.LOCKED

        if (weapon.unlockedAbility?.id == DefaultWeaponAbilities.RAPID_STROKES_ID) {
            val p = currentPlayer
            val enemies = currentEnemies
            if (runtimesByWeapon[weapon]?.abilityState != WeaponAbilityState.COOLDOWN &&
                (p == null || enemies == null || !hasAbilityTarget(weapon, p, enemies))) return WeaponAbilityState.LOCKED
        }
        if (preparedCasts[slotIndex]?.weapon === weapon) return WeaponAbilityState.ACTIVE

        if (
            weapon.unlockedAbility
                ?.id ==
            DefaultWeaponAbilities
                .GLYPH_CASTING_ID
        ) {
            return when {
                glyphMotionRuntime.isDrawing -> {
                    WeaponAbilityState.ACTIVE
                }

                glyphMotionRuntime
                    .cooldownRemainingSeconds >
                    0f -> {
                    WeaponAbilityState.COOLDOWN
                }

                else -> {
                    WeaponAbilityState.READY
                }
            }
        }

        return runtimesByWeapon[
            weapon
        ]
            ?.abilityState
            ?: WeaponAbilityState.LOCKED
    }

    fun abilityRemainingActiveSecondsInSlot(
        slotIndex: Int
    ): Float {

        val weapon =
            inventory.weaponAt(
                slotIndex
            )
                ?: return 0f

        if (
            weapon.unlockedAbility
                ?.id ==
            DefaultWeaponAbilities
                .GLYPH_CASTING_ID
        ) {
            return glyphMotionRuntime
                .remainingDrawingSeconds
        }

        return runtimesByWeapon[
            weapon
        ]
            ?.abilityRemainingActiveSeconds
            ?: 0f
    }

    fun abilityRemainingCooldownSecondsInSlot(
        slotIndex: Int
    ): Float {

        val weapon =
            inventory.weaponAt(
                slotIndex
            )
                ?: return 0f

        if (
            weapon.unlockedAbility
                ?.id ==
            DefaultWeaponAbilities
                .GLYPH_CASTING_ID
        ) {
            return glyphMotionRuntime
                .cooldownRemainingSeconds
        }

        return runtimesByWeapon[
            weapon
        ]
            ?.abilityRemainingCooldownSeconds
            ?: 0f
    }

    fun prepareForStageTransition() {
        /*
         * Açık hedefleme veya çalışan Active Skill
         * stage sınırını geçmez.
         *
         * endActiveAbilityEarly() skill'i cooldown'a
         * geçirir; cooldown değeri korunur.
         */
        cancelActiveAbilities()

        glyphMotionRuntime
            .cancelActiveDrawing()

        glyphWeaponRuntime =
            null

        /*
         * Normal saldırı zamanlayıcıları yeni stage'de
         * temiz başlar fakat ability cooldown'ları
         * korunur.
         */
        runtimesByWeapon.values
            .forEach { runtime ->
                runtime.resetAttackTimer()
            }
    }

    fun reduceAllAbilityCooldowns(seconds: Float) {
        if (seconds <= 0f) return
        runtimesByWeapon.values.forEach { it.reduceAbilityCooldown(seconds) }
        glyphMotionRuntime.reduceCooldown(seconds)
    }

    fun resetAllTimers() {
        preparedCasts.clear()
        extendedCastAims.clear()
        pendingExtendedAbilities.clear()
        executorFactory.extendedRuntime.clear()
        physicalWeaponAbilityRuntime.clear()
        defensiveLineRuntime.reset()
        visitFocus {
        igniteDashRuntime.reset()
        vortexPullRuntime.clear()
        vortexWeaponRuntime = null
        vortexWeapon = null

        staticCloudsRuntime.clear()
        staticCloudsWeaponRuntime = null
        staticCloudsWeapon = null

        acidicPoolRuntime.clear()
        acidicPoolWeaponRuntime = null
        acidicPoolWeapon = null
        pendingVitriolicIgnitions.clear()
        }


        visitFocus { persistentAreaManager.clearAll() }

        impaleRuntime.reset()
        erasingSoulsRuntime.clear()

        runtimesByWeapon.values
            .forEach { runtime ->
                runtime.resetTimer()
            }
    }

    fun dispose() {
        focusChannels.values.forEach { it.clear() }
        focusChannels.clear()
        fallbackFocus.clear()
        combatInteractionManager.clearRules()
        spellFocusEffectSpriteRenderer.dispose()
        executorFactory.extendedRuntime.dispose()
        physicalWeaponAbilityRuntime.dispose()
    }

    fun refreshWeaponStatBonuses() {
        statBonusSynchronizer
            .invalidate()

        statBonusSynchronizer
            .synchronize()
    }

    val activeRuntimeCount: Int
        get() =
            runtimesByWeapon.size

    companion object {
        /*
         * Normal enemy için merkez yönündeki temel hız.
         * Enemy type/stage dirençleri bu değeri çarpar.
         */
        private const val VORTEX_BASE_PULL_SPEED =
            235f

        private const val VITRIOLIC_IGNITION_RADIUS =
            280f

        private const val VITRIOLIC_IGNITION_DAMAGE_MULTIPLIER =
            7.77f

        private const val PLAGUE_STORM_NECROTIC_TICK_MULTIPLIER =
            0.00005f

        private const val PLAGUE_STORM_NECROSIS_SECONDS =
            2f
    }

}
