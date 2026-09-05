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
        WeaponDamageCalculator(
            playerStats
        )

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

    private val igniteDashRuntime =
        IgniteDashRuntime(
            playerStats =
                playerStats
        )

    private val vortexPullRuntime =
        VortexPullRuntime()

    private val spellFocusEffectSpriteRenderer =
        SpellFocusEffectSpriteRenderer()

    private val persistentAreaManager =
        PersistentAreaManager()

    private val staticCloudsRuntime =
        StaticCloudsRuntime(
            areaManager =
                persistentAreaManager
        )

    private val impaleRuntime =
        ImpaleRuntime()

    private val erasingSoulsRuntime =
        ErasingSoulsRuntime()

    private val acidicPoolRuntime =
        AcidicPoolRuntime(
            areaManager =
                persistentAreaManager
        )

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


    private var vortexWeaponRuntime:
        WeaponRuntime? =
        null

    private var vortexWeapon:
        WeaponInstance? =
        null

    private var staticCloudsWeaponRuntime:
        WeaponRuntime? =
        null

    private var staticCloudsWeapon:
        WeaponInstance? =
        null

    private var acidicPoolWeaponRuntime:
        WeaponRuntime? =
        null

    private var acidicPoolWeapon:
        WeaponInstance? =
        null

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

    private val pendingVitriolicIgnitions =
        ArrayDeque<PendingVitriolicIgnition>()

    init {
        combatInteractionManager
            .register(
                characterTraitRuntime
            )

        combatInteractionManager
            .register(
                VitriolicIgnitionRule(
                    tryIgnitePool = {
                            worldX,
                            worldY ->

                        acidicPoolRuntime
                            .tryActivateVitriolicIgnition(
                                worldX,
                                worldY
                            )
                    },

                    onTriggered = {
                            worldX,
                            worldY ->

                        pendingVitriolicIgnitions
                            .addLast(
                                PendingVitriolicIgnition(
                                    centerX =
                                        worldX,

                                    centerY =
                                        worldY
                                )
                            )
                    }
                )
            )

        combatInteractionManager
            .register(
                PlagueStormRule(
                    isInsideVortex = {
                            worldX,
                            worldY ->

                        vortexPullRuntime
                            .contains(
                                worldX,
                                worldY
                            )
                    },

                    activatePlagueStorm = {
                        vortexPullRuntime
                            .activatePlagueStorm()
                    }
                )
            )
    }

    fun update(
        delta: Float,
        player: Player,
        enemyManager: EnemyManager,
        projectiles: MutableList<Projectile>,
        mapObstacles: List<MapObstacle>,
        worldWidth: Float,
        worldHeight: Float,
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

        resolvePendingVitriolicIgnitions(
            enemyManager =
                enemyManager,

            onDamageDealt =
                onDamageDealt,

            onEnemyKilled =
                onEnemyKilled
        )

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
                    .allEnemies,

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

                glyphSpellRuntime.cast(
                    request =
                        request,

                    enemyManager =
                        enemyManager,

                    worldWidth =
                        worldWidth,

                    worldHeight =
                        worldHeight,

                    onDamageDealt =
                        onDamageDealt,

                    onEnemyKilled =
                        onEnemyKilled
                )
            }

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
                    .allEnemies,

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
                    .allEnemies,

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
                    .allEnemies,

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

                runtime.update(
                    delta = delta,
                    slotIndex = slotIndex,
                    player = player,
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

        return when (
            ability.id
        ) {
            DefaultWeaponAbilities
                .IGNITE_DASH_ID -> {

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
                        player.centerX,

                    playerCenterY =
                        player.centerY,

                    enemies =
                        enemyManager
                            .allEnemies,

                    worldWidth =
                        worldWidth,

                    worldHeight =
                        worldHeight
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
                    player
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
                        player.centerX,

                    playerCenterY =
                        player.centerY,

                    enemies =
                        enemyManager
                            .allEnemies
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
                .allEnemies
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
            (
                activeSkillDamageCalculator
                    .calculateSpellSkillDamage(
                        weapon =
                            weapon,

                        damageType =
                            DamageType.ACID
                    ) *
                    DefaultWeaponAbilities
                        .ACIDIC_POOL_DAMAGE_PER_SECOND_MULTIPLIER
                )
                .roundToInt()
                .coerceAtLeast(
                    1
                )

        val modifiedDamage =
            tamgaRuntimeManager
                .modifyWeaponDamage(
                    enemy =
                        enemy,

                    baseDamage =
                        rawTickDamage
                )
                .coerceAtLeast(
                    1
                )

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
            (
                activeSkillDamageCalculator
                    .calculateSpellSkillDamage(
                        weapon =
                            weapon,

                        damageType =
                            DamageType.LIGHTNING
                    ) *
                    DefaultWeaponAbilities
                        .STATIC_CLOUDS_LIGHTNING_DPS_MULTIPLIER
                )
                .roundToInt()
                .coerceAtLeast(
                    1
                )

        val modifiedDamage =
            tamgaRuntimeManager
                .modifyWeaponDamage(
                    enemy =
                        enemy,

                    baseDamage =
                        lightningDamage
                )
                .coerceAtLeast(
                    1
                )

        val wasAlive =
            !enemy.isDead

        val appliedDamage =
            enemy.takeDamage(
                amount =
                    modifiedDamage,

                damageContext =
                    EnemyDamageContext(
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
            DefaultWeaponAbilities
                .STATIC_CLOUDS_STUN_CHANCE
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
            (
                activeSkillDamageCalculator
                    .calculateSpellSkillDamage(
                        weapon =
                            weapon,

                        damageType =
                            DamageType.AIR
                    ) *
                    DefaultWeaponAbilities
                        .VORTEX_PULL_DAMAGE_PER_SECOND_MULTIPLIER
                )
                .roundToInt()
                .coerceAtLeast(
                    1
                )

        val vortexDamage =
            tamgaRuntimeManager
                .modifyWeaponDamage(
                    enemy =
                        enemy,

                    baseDamage =
                        vortexRawDamage
                )
                .coerceAtLeast(
                    1
                )

        var totalAppliedDamage =
            enemy.takeDamage(
                amount =
                    vortexDamage,

                damageContext =
                    EnemyDamageContext(
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
                (
                    activeSkillDamageCalculator
                        .calculateSpellSkillDamage(
                            weapon =
                                weapon,

                            damageType =
                                DamageType.NECROTIC
                        ) *
                        PLAGUE_STORM_NECROTIC_TICK_MULTIPLIER
                    )
                    .roundToInt()
                    .coerceAtLeast(
                        1
                    )

            val plagueDamage =
                tamgaRuntimeManager
                    .modifyWeaponDamage(
                        enemy =
                            enemy,

                        baseDamage =
                            plagueRawDamage
                    )
                    .coerceAtLeast(
                        1
                    )

            totalAppliedDamage +=
                enemy.takeDamage(
                    amount =
                        plagueDamage,

                    damageContext =
                        EnemyDamageContext(
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

    fun resolveErasingSoulsChain(
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
                    DefaultWeaponAbilities
                        .ERASING_SOULS_DAMAGE_MULTIPLIER
                )
                .roundToInt()
                .coerceAtLeast(
                    1
                )

        return erasingSoulsRuntime.resolveChain(
            initialDeadEnemy = initialDeadEnemy,
            sourceWeapon = sourceWeapon,
            playerCenterX = player.centerX,
            playerCenterY = player.centerY,
            enemies = enemyManager.allEnemies,
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
        igniteDashRuntime
            .updateAimPosition(
                worldX =
                    worldX,

                worldY =
                    worldY
            )
    }

    fun cancelActiveAbilities() {
        impaleRuntime
            .cancel()

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

    fun renderActiveSkillEffects(
        shapeRenderer: ShapeRenderer,
        player: Player
    ) {
        igniteDashRuntime.render(
            shapeRenderer =
                shapeRenderer,

            player =
                player
        )

        vortexPullRuntime.render(
            shapeRenderer =
                shapeRenderer
        )

        staticCloudsRuntime.render(
            shapeRenderer =
                shapeRenderer
        )

        acidicPoolRuntime.render(
            shapeRenderer =
                shapeRenderer
        )

        defensiveLineVisualRenderer.render(
            shapeRenderer =
                shapeRenderer,

            player =
                player,

            bonusArmorClass =
                defensiveLineRuntime
                    .bonusArmorClass
        )

        erasingSoulsRuntime.render(
            shapeRenderer
        )

        if (
            spellFocusEffectSpriteRenderer
                .hasAnythingToRender(
                    staticCloudsRuntime =
                        staticCloudsRuntime,

                    igniteDashRuntime =
                        igniteDashRuntime,

                    erasingSoulsRuntime =
                        erasingSoulsRuntime,

                    vortexPullRuntime =
                        vortexPullRuntime,

                    acidicPoolRuntime =
                        acidicPoolRuntime,

                    glyphSpellRuntime =
                        glyphSpellRuntime
                )
        ) {
            val projectionMatrix =
                shapeRenderer
                    .projectionMatrix
                    .cpy()

            /*
             * renderActiveSkillEffects is called inside WorldRenderer's
             * Filled ShapeRenderer pass. Close it briefly for SpriteBatch,
             * then restore the same pass so the caller can continue safely.
             */
            shapeRenderer.end()

            spellFocusEffectSpriteRenderer
                .render(
                    projectionMatrix =
                        projectionMatrix,

                    staticCloudsRuntime =
                        staticCloudsRuntime,

                    igniteDashRuntime =
                        igniteDashRuntime,

                    erasingSoulsRuntime =
                        erasingSoulsRuntime,

                    vortexPullRuntime =
                        vortexPullRuntime,

                    acidicPoolRuntime =
                        acidicPoolRuntime,

                    glyphSpellRuntime =
                        glyphSpellRuntime
                )

            shapeRenderer
                .projectionMatrix =
                projectionMatrix

            shapeRenderer.begin(
                ShapeRenderer.ShapeType.Filled
            )
        }

        glyphMotionRuntime.render(
            shapeRenderer
        )

        glyphSpellRuntime.render(
            shapeRenderer
        )
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
        defensiveLineRuntime.reset()
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

        persistentAreaManager.clearAll()

        impaleRuntime.reset()
        erasingSoulsRuntime.clear()

        runtimesByWeapon.values
            .forEach { runtime ->
                runtime.resetTimer()
            }
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
            0.95f

        private const val PLAGUE_STORM_NECROSIS_SECONDS =
            2f
    }

}
