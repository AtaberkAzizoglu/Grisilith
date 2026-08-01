package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyManager
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.weapon.Projectile
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.FitViewport
import com.azizoglu.grisilith.enemy.EnemySpawner
import com.azizoglu.grisilith.statistics.RunStatistics
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.azizoglu.grisilith.util.GameTimeController
import com.badlogic.gdx.Input
import com.azizoglu.grisilith.stage.StageController
import com.azizoglu.grisilith.enemy.EnemyScaling
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.stage.StageType
import com.azizoglu.grisilith.economy.GoldCoinPickup
import com.azizoglu.grisilith.economy.SessionWallet
import com.azizoglu.grisilith.economy.MarketOffer
import com.azizoglu.grisilith.economy.MarketOfferGenerator
import com.azizoglu.grisilith.economy.MarketProductType
import com.azizoglu.grisilith.economy.MarketRerollPriceCalculator
import com.azizoglu.grisilith.economy.MarketSlot
import com.azizoglu.grisilith.economy.StageGoldRewardCalculator
import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.config.GameDifficulty
import com.azizoglu.grisilith.config.GameMap
import com.azizoglu.grisilith.map.MapObstacle
import com.azizoglu.grisilith.map.MapObstacleGenerator
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.character.CharacterDefinition
import com.azizoglu.grisilith.weapon.runtime.WeaponInstance
import com.azizoglu.grisilith.weapon.runtime.WeaponInventory
import com.azizoglu.grisilith.screen.render.HudRenderer
import com.azizoglu.grisilith.screen.render.MarketRenderer
import com.azizoglu.grisilith.screen.render.RunSummaryRenderer
import com.azizoglu.grisilith.screen.controller.StatCardController
import com.azizoglu.grisilith.screen.render.WorldRenderer
import com.azizoglu.grisilith.weapon.runtime.WeaponRuntimeManager
import com.azizoglu.grisilith.weapon.runtime.WeaponInventoryResult
import com.azizoglu.grisilith.item.runtime.ItemInventory
import com.azizoglu.grisilith.item.runtime.ItemStatBonusSynchronizer
import com.azizoglu.grisilith.item.definition.DefaultItems
import com.azizoglu.grisilith.item.runtime.ItemInstance
import com.azizoglu.grisilith.item.runtime.ItemInventoryResult
import com.azizoglu.grisilith.weapon.definition.DefaultWeapons
import com.azizoglu.grisilith.screen.market.MarketInputController
import com.azizoglu.grisilith.screen.market.MarketInteractionState
import com.azizoglu.grisilith.economy.WeaponSalePriceCalculator
import com.azizoglu.grisilith.weapon.runtime.WeaponStatBonusSynchronizer
import com.badlogic.gdx.InputProcessor
import com.azizoglu.grisilith.chest.ChestDropCalculator
import com.azizoglu.grisilith.chest.ChestDropSegmentTracker
import com.azizoglu.grisilith.chest.ChestDropSource
import com.azizoglu.grisilith.chest.ChestPickupManager
import com.azizoglu.grisilith.chest.ChestRewardQueue
import com.azizoglu.grisilith.chest.ChestRewardRoll
import com.azizoglu.grisilith.chest.ChestRewardRoller
import com.azizoglu.grisilith.chest.ChestRewardType
import com.azizoglu.grisilith.screen.controller.ChestRewardController
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.screen.pause.PauseAction
import com.azizoglu.grisilith.screen.pause.PauseInputController
import com.azizoglu.grisilith.screen.pause.PauseInteractionState
import com.azizoglu.grisilith.screen.render.PauseRenderer
import com.azizoglu.grisilith.preset.ResolvedPresetContent
import com.azizoglu.grisilith.armor.runtime.EquippedArmor
import com.azizoglu.grisilith.tamga.runtime.TamgaRuntimeManager
import com.azizoglu.grisilith.tamga.runtime.active.ActiveTamgaEndReason
import com.azizoglu.grisilith.tamga.render.ActiveTamgaHudRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.graphics.GL20
import com.azizoglu.grisilith.reward.RunRewardTracker
import com.azizoglu.grisilith.content.CollectionEncounterIds
import com.azizoglu.grisilith.profile.PlayerProfileRepository
import com.azizoglu.grisilith.statistics.career.CompletedRunStatistics
import com.azizoglu.grisilith.statistics.career.HighScoreRepository


class FirstScreen(
    private val game: GrisilithGame,
    private val difficulty: GameDifficulty,
    private val gameMap: GameMap,
    private val selectedCharacter:
    CharacterDefinition,

    private val selectedPreset:
    ResolvedPresetContent? = null
) : ScreenAdapter() {

    companion object {
        private const val CAMERA_WIDTH = 1280f
        private const val CAMERA_HEIGHT = 720f
    }

    private val worldWidth =
        gameMap.worldWidth

    private val worldHeight =
        gameMap.worldHeight

    private val shapeRenderer =
        ShapeRenderer()

    private val camera =
        OrthographicCamera()

    private val worldRenderer =
        WorldRenderer(
            shapeRenderer = shapeRenderer,
            camera = camera,
            worldWidth = worldWidth,
            worldHeight = worldHeight
        )

    private val viewport =
        FitViewport(
            CAMERA_WIDTH,
            CAMERA_HEIGHT,
            camera
        )

    private val playerStats =
        PlayerStats(
            initialBaseScores =
                selectedPreset
                    ?.preRunBaseStats
                    ?: selectedCharacter.baseStats
        ).apply {
            selectedPreset
                ?.let { preset ->
                    setGearBonuses(
                        preset.armorAndTamgaBonuses
                    )
                }
        }

    private val weaponInventory =
        WeaponInventory()

    private val weaponStatBonusSynchronizer =
        WeaponStatBonusSynchronizer(
            inventory =
                weaponInventory,

            playerStats =
                playerStats
        )

    private val player = Player(
        startX = worldWidth / 2f,
        startY = worldHeight / 2f,
        worldWidth = worldWidth,
        worldHeight = worldHeight,
        stats = playerStats
    )

    private val equippedArmor =
        EquippedArmor().apply {
            selectedPreset
                ?.armor
                ?.let { armor ->
                    equip(
                        armor
                    )
                }
        }

    private val itemInventory =
        ItemInventory()

    private val itemStatSynchronizer =
        ItemStatBonusSynchronizer(
            inventory =
                itemInventory,

            playerStats =
                playerStats
        )

    private val stageController = StageController(
        normalStageDurationSeconds = 30f,
        miniBossStageDurationSeconds = 45f,
        finalBossStageDurationSeconds = 60f,
        maximumStage = difficulty.maximumStage
    )


    private val tamgaRuntimeManager =
        TamgaRuntimeManager(
            weaponInventory =
                weaponInventory,

            playerStats =
                playerStats,

            weaponTamga =
                selectedPreset
                    ?.weaponTamga,

            armorTamga =
                selectedPreset
                    ?.armorTamga,

            characterPreferredStats =
                selectedPreset
                    ?.character
                    ?.preferredMarketStats
                    ?: emptySet(),

            currentStageProvider = {
                stageController.currentStage
            }
        )

    init {
        tamgaRuntimeManager
            .attachPlayer(
                player
            )
    }

    private val weaponRuntimeManager =
        WeaponRuntimeManager(
            inventory =
                weaponInventory,

            playerStats =
                playerStats,

            tamgaRuntimeManager =
                tamgaRuntimeManager
        )

    private val mapObstacles: List<MapObstacle> =
        MapObstacleGenerator.generate(
            gameMap = gameMap,
            playerStartX = player.centerX,
            playerStartY = player.centerY
        )

    private val projectiles = mutableListOf<Projectile>()

    private val runStatistics = RunStatistics()

    private val gameTimeController = GameTimeController()

    private val hudBatch = SpriteBatch()
    private val hudFont = BitmapFont()

    private val enemyManager = EnemyManager().apply {
        spawn(
            Enemy(
                startX = worldWidth / 2f + 300f,
                startY = worldHeight / 2f,
                stats = EnemyScaling.normalEnemyStats(
                    stage = 1
                )
            )
        )
    }

    private val enemySpawner = EnemySpawner(
        worldWidth = worldWidth,
        worldHeight = worldHeight
    )

    private var isRunSummaryOpen = false

    private var isMarketOpen = false

    private var isPauseMenuOpen =
        false

    private var hasInitializedRun =
        false

    private var marketSlots =
        emptyList<MarketSlot>()

    private var lockedSlotsForNextMarket:
        List<MarketSlot?> =
        List(4) {
            null
        }

    private var marketRerollsUsed = 0

    private var itemScrollOffset = 0

    private var previousInputProcessor:
        InputProcessor? = null

    private var hasCommittedCareerStatistics =
        false

    private val sessionWallet =
        SessionWallet()

    private val runRewardTracker =
        RunRewardTracker(
            difficulty =
                difficulty
        )

    private val finalBossRewardId =
        "map:${gameMap.name.lowercase()}"

    private val goldCoinPickups =
        mutableListOf<GoldCoinPickup>()

    private val chestPickupManager =
        ChestPickupManager()

    private val chestRewardQueue =
        ChestRewardQueue()

    private val chestDropSegmentTracker =
        ChestDropSegmentTracker()


    private val hudRenderer =
        HudRenderer(
            camera = camera,
            cameraWidth = CAMERA_WIDTH,
            cameraHeight = CAMERA_HEIGHT,
            batch = hudBatch,
            font = hudFont,
            player = player,
            playerStats = playerStats,
            stageController = stageController,
            sessionWallet = sessionWallet,
            enemyManager = enemyManager,
            runStatistics = runStatistics,
            gameTimeController =
                gameTimeController,
            weaponInventory =
                weaponInventory
        )

    private val activeTamgaHudRenderer =
        ActiveTamgaHudRenderer(
            camera = camera,
            cameraWidth = CAMERA_WIDTH,
            cameraHeight = CAMERA_HEIGHT,
            shapeRenderer = shapeRenderer,
            batch = hudBatch,
            font = hudFont
        )

    private val runSummaryRenderer =
        RunSummaryRenderer(
            camera = camera,
            cameraWidth = CAMERA_WIDTH,
            cameraHeight = CAMERA_HEIGHT,
            batch = hudBatch,
            font = hudFont,
            difficulty = difficulty,
            gameMap = gameMap,
            selectedCharacter =
                selectedCharacter,
            stageController =
                stageController,
            runStatistics =
                runStatistics,

            grisilithCoinsEarned =
                runRewardTracker
                    .grisilithCoinsEarned
        )

    private val marketInteractionState =
        MarketInteractionState()

    private val marketRenderer =
        MarketRenderer(
            camera =
                camera,

            cameraWidth =
                CAMERA_WIDTH,

            cameraHeight =
                CAMERA_HEIGHT,

            batch =
                hudBatch,

            font =
                hudFont,

            sessionWallet =
                sessionWallet,

            stageController =
                stageController,

            selectedCharacter =
                selectedCharacter,

            playerStats =
                playerStats,

            weaponInventory =
                weaponInventory,

            itemInventory =
                itemInventory
        )

    private val marketInputController =
        MarketInputController(
            viewport =
                viewport,

            layoutProvider = {
                marketRenderer
                    .currentLayout()
            },

            interactionState =
                marketInteractionState
        )

    private val pauseInteractionState =
        PauseInteractionState()

    private val pauseRenderer =
        PauseRenderer(
            camera =
                camera,

            cameraWidth =
                CAMERA_WIDTH,

            cameraHeight =
                CAMERA_HEIGHT,

            batch =
                hudBatch,

            font =
                hudFont,

            selectedCharacter =
                selectedCharacter,

            playerStats =
                playerStats,

            weaponInventory =
                weaponInventory,

            itemInventory =
                itemInventory
        )

    private val pauseInputController =
        PauseInputController(
            viewport =
                viewport,

            layoutProvider = {
                pauseRenderer
                    .currentLayout()
            },

            interactionState =
                pauseInteractionState,

            weaponExistsAt = {
                    slotIndex ->

                weaponInventory
                    .weaponAt(
                        slotIndex
                    ) !=
                    null
            },

            groupedItemCountProvider = {
                pauseRenderer
                    .groupedItemCount()
            }
        )

    private val statCardController =
        StatCardController(
            player = player,
            stageController =
                stageController,
            enemySpawner =
                enemySpawner,
            camera = camera,
            cameraWidth =
                CAMERA_WIDTH,
            batch = hudBatch,
            font = hudFont,
            onOpenMarket = {
                openMarket()
            }
        )

    private val chestRewardController =
        ChestRewardController(
            rewardQueue =
                chestRewardQueue,

            camera =
                camera,

            cameraWidth =
                CAMERA_WIDTH,

            cameraHeight =
                CAMERA_HEIGHT,

            viewport =
                viewport,

            batch =
                hudBatch,

            font =
                hudFont,

            onApplyReward = {
                    reward ->

                applyChestReward(
                    reward
                )
            },

            onAllRewardsCollected = {
                    shouldOpenMarketAfterStatSelection ->

                statCardController
                    .beginSelection(
                        shouldOpenMarketAfterSelection =
                            shouldOpenMarketAfterStatSelection
                    )
            }
        )

    override fun show() {
        viewport.apply()

        camera.position.set(
            player.centerX,
            player.centerY,
            0f
        )

        if (!hasInitializedRun) {

            hasInitializedRun =
                true

            HighScoreRepository
                .recordRunStarted()

            PlayerProfileRepository
                .markMapEncountered(
                    CollectionEncounterIds.map(
                        gameMap
                    )
                )

            initializeStartingLoadout()

            recordCurrentEnemyEncounters()
        }

        camera.update()
    }


    private fun initializeStartingLoadout() {
        val preset =
            selectedPreset

        if (
            preset == null
        ) {
            /*
             * Legacy Character Selection akışı geçici
             * olarak korunuyor.
             */
            weaponInventory.addWeapon(
                WeaponInstance(
                    definition =
                        DefaultWeapons.longbow,

                    level =
                        2
                )
            )

            synchronizeWeaponInventoryEffects()

            return
        }

        weaponInventory.addWeapon(
            WeaponInstance(
                definition =
                    preset.weapon,

                level =
                    WeaponInstance.MINIMUM_LEVEL
            )
        )

        preset.item
            ?.let { item ->
                itemInventory.addItem(
                    ItemInstance(
                        definition =
                            item
                    )
                )
            }

        weaponStatBonusSynchronizer
            .invalidate()

        weaponStatBonusSynchronizer
            .synchronize()

        itemStatSynchronizer
            .synchronize()

        player
            .synchronizeMaximumHealthWithStats(
                healAddedHealth =
                    true
            )

        tamgaRuntimeManager.reset()

        weaponRuntimeManager
            .resetAllTimers()
    }

    override fun render(
        delta: Float
    ) {
        handleGameControls(
            delta
        )

        if (
            player.health.isDead ||
            stageController.isRunComplete
        ) {

            if (!isRunSummaryOpen) {

                runRewardTracker
                    .commitToProfile()

                commitCareerStatistics()
            }

            isRunSummaryOpen =
                true
        }

        val gameplayStopped =
            player.health.isDead ||
                stageController.isRunComplete ||
                chestRewardController.isOpen ||
                statCardController.isOpen ||
                isMarketOpen ||
                isPauseMenuOpen ||
                isRunSummaryOpen

        val gameDelta =
            if (gameplayStopped) {
                0f
            } else {
                gameTimeController
                    .calculateGameDelta(delta)
            }

        if (
            !gameTimeController.isPaused &&
            !gameplayStopped
        ) {
            updateGame(gameDelta)
        }

        renderGame()
    }

    private fun commitCareerStatistics() {

        if (
            hasCommittedCareerStatistics
        ) {
            return
        }

        hasCommittedCareerStatistics =
            true

        HighScoreRepository
            .commitCompletedRun(
                CompletedRunStatistics(
                    wasSuccessful =
                        stageController
                            .isRunComplete,

                    durationSeconds =
                        gameTimeController
                            .elapsedGameTimeSeconds
                            .toLong()
                            .coerceAtLeast(
                                0L
                            ),

                    enemiesKilled =
                        runStatistics
                            .totalEnemiesKilled,

                    bossesDefeated =
                        runStatistics
                            .totalBossesDefeated,

                    finalBossesDefeated =
                        runStatistics
                            .totalFinalBossesDefeated,

                    damageDealt =
                        runStatistics
                            .totalDamageDealt,

                    damageTaken =
                        runStatistics
                            .totalDamageTaken,

                    goldCollected =
                        runStatistics
                            .totalGoldCollected,

                    goldSpent =
                        runStatistics
                            .totalGoldSpent,

                    grisilithCoinsEarned =
                        runRewardTracker
                            .grisilithCoinsEarned
                )
            )
    }

    private fun handleGameControls(
        delta: Float
    ) {
        if (isRunSummaryOpen) {
            handleRunSummaryControls()
            return
        }

        if (isPauseMenuOpen) {
            handlePauseControls()
            return
        }

        if (isMarketOpen) {
            handleMarketControls(
                delta
            )

            return
        }

        if (
            chestRewardController.isOpen
        ) {
            chestRewardController
                .handleInput()

            return
        }

        if (statCardController.isOpen) {
            statCardController.handleInput()
            return
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            )
        ) {
            openPauseMenu()
            return
        }

        /*
         * Active Tamga kısayolları:
         * Weapon Tamga -> Z
         * Armor Tamga  -> X
         */
        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.Z
            )
        ) {
            tamgaRuntimeManager
                .tryActivateWeaponTamga()
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.X
            )
        ) {
            tamgaRuntimeManager
                .tryActivateArmorTamga()
        }

        handleActiveTamgaButtonInput()

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            gameTimeController.setGameSpeed(0.5f)
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            gameTimeController.setGameSpeed(1f)
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            gameTimeController.setGameSpeed(2f)
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
            gameTimeController.setGameSpeed(3f)
        }
    }

    private fun handleActiveTamgaButtonInput() {
        if (
            !Gdx.input.justTouched()
        ) {
            return
        }

        val pointerPosition =
            Vector2(
                Gdx.input.x.toFloat(),
                Gdx.input.y.toFloat()
            )

        viewport.unproject(
            pointerPosition
        )

        when {
            tamgaRuntimeManager
                .hasActiveWeaponTamga &&
                activeTamgaHudRenderer
                    .isWeaponButtonHit(
                        pointerPosition.x,
                        pointerPosition.y
                    ) -> {

                tamgaRuntimeManager
                    .tryActivateWeaponTamga()
            }

            tamgaRuntimeManager
                .hasActiveArmorTamga &&
                activeTamgaHudRenderer
                    .isArmorButtonHit(
                        pointerPosition.x,
                        pointerPosition.y
                    ) -> {

                tamgaRuntimeManager
                    .tryActivateArmorTamga()
            }
        }
    }

    private fun openPauseMenu() {
        isPauseMenuOpen =
            true

        pauseInputController.reset()
    }

    private fun closePauseMenu() {
        isPauseMenuOpen =
            false

        pauseInputController.reset()
    }

    private fun handlePauseControls() {

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            )
        ) {
            if (
                pauseInteractionState
                    .isAnyModalOpen
            ) {
                pauseInteractionState
                    .closeAllModals()
            } else {
                closePauseMenu()
            }

            return
        }

        val pauseAction =
            pauseInputController.update()

        when (pauseAction) {
            PauseAction.RETURN_TO_GAME -> {
                closePauseMenu()
            }

            PauseAction.RESTART -> {

                restartRun()
            }

            PauseAction.OPEN_SETTINGS -> {
                game.showSettingsFromPausedRun(
                    pausedRun =
                        this
                )
            }

            PauseAction.QUIT_GAME -> {
                game.showMainMenu()
            }

            null -> {
            }
        }
    }


    private fun restartRun() {
        val preset =
            selectedPreset

        if (
            preset != null
        ) {
            game.startPresetRun(
                difficulty =
                    difficulty,

                gameMap =
                    gameMap,

                presetId =
                    preset.preset.presetId
            )

            return
        }

        game.startRun(
            difficulty =
                difficulty,

            gameMap =
                gameMap,

            character =
                selectedCharacter
        )
    }

    private fun handleRunSummaryControls() {
        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.R
            )
        ) {
            restartRun()
            return
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ENTER
            )
        ) {
            game.showMainMenu()
        }
    }

    private fun handleChargerEndEvent() {
        val event =
            tamgaRuntimeManager
                .consumeChargerEndEvent()
                ?: return

        when (event.reason) {

            ActiveTamgaEndReason.COMPLETED,
            ActiveTamgaEndReason.INTERRUPTED -> {

                tamgaRuntimeManager
                    .triggerChargerExplosion(
                        centerX =
                            event.centerX,

                        centerY =
                            event.centerY,

                        chargedSeconds =
                            event.chargedSeconds,

                        completedSuccessfully =
                            event.wasCompletedSuccessfully,

                        enemies =
                            enemyManager
                                .allEnemies,

                        onDamageDealt = {
                                appliedDamage,
                                sourceName ->

                            runStatistics
                                .recordDamageDealt(
                                    amount =
                                        appliedDamage,

                                    weaponName =
                                        sourceName
                                )
                        },

                        onEnemyKilled = {
                                killedEnemy ->

                            handleEnemyKilled(
                                killedEnemy
                            )
                        }
                    )
            }

            ActiveTamgaEndReason.RESET -> {
                /*
                 * Run resetinde patlama oluşturulmaz.
                 */
            }
        }
    }

    private fun collectAllGoldPickups() {
        goldCoinPickups.forEach { pickup ->
            pickup.collectImmediately { collectedGold ->
                sessionWallet.addGold(collectedGold)

                runStatistics.recordGoldCollected(
                    collectedGold
                )
            }
        }

        goldCoinPickups.clear()
    }

    private fun grantStageCompletionGold(
        completedStage: Int,
        completedStageType: StageType
    ) {
        val reward =
            StageGoldRewardCalculator.rewardFor(
                completedStage = completedStage,
                stageType = completedStageType
            )

        sessionWallet.addGold(reward)

        runStatistics.recordGoldCollected(
            reward
        )
    }

    private fun updateGame(
        delta: Float
    ) {
        if (player.health.isDead) {
            return
        }

        val stageChanged =
            updateStage(
                delta
            )

        if (
            stageChanged ||
            stageController.isRunComplete
        ) {
            return
        }

        player.update(
            delta
        )

        mapObstacles.forEach { obstacle ->
            player.resolveObstacleCollision(
                obstacle.bounds
            )
        }

        enemyManager.resolvePlayerCollisions {
                enemyBounds ->

            player.resolveCollision(
                enemyBounds
            )
        }

        /*
         * Player.update() sonrasında obstacle ve Enemy
         * collision çözümü konumu değiştirebilir.
         * Charger gerçek hareket hızını bu son konuma
         * göre değerlendirmelidir.
         */
        player.finalizeMovementAfterCollisions(
            delta =
                delta
        )

        tamgaRuntimeManager
            .updateChargerAfterMovement(
                delta =
                    delta
            )

        handleChargerEndEvent()

        enemyManager.update(
            delta = delta,

            targetX =
                player.centerX,

            targetY =
                player.centerY,

            targetBounds =
                player.bounds,

            worldWidth =
                worldWidth,

            worldHeight =
                worldHeight
        ) { damage ->

            val appliedDamage =
                player.takeDamage(
                    damage
                )

            runStatistics.recordDamageTaken(
                appliedDamage
            )
        }

        /*
         * Quick Shot timer ve diğer genel
         * Tamga runtime güncellemeleri.
         */
        tamgaRuntimeManager.update(
            delta = delta
        )

        /*
         * Holy Lights yalnızca takılıysa
         * manager içinde çalışır.
         */
        tamgaRuntimeManager
            .updateHolyLights(
                delta = delta,

                player =
                    player,

                enemies =
                    enemyManager.allEnemies,

                onDamageDealt = {
                        appliedDamage,
                        sourceName ->

                    runStatistics
                        .recordDamageDealt(
                            amount =
                                appliedDamage,

                            weaponName =
                                sourceName
                        )
                },

                onEnemyKilled = {
                        killedEnemy ->

                    handleEnemyKilled(
                        killedEnemy
                    )
                },

                onLifeStealDamage = {
                        appliedDamage ->

                    player.tryLifeSteal(
                        appliedDamage =
                            appliedDamage
                    )
                }
            )

        /*
         * Arcane Aura yalnızca takılıysa
         * manager içinde çalışır.
         */
        tamgaRuntimeManager
            .updateArcaneAura(
                delta = delta,

                player =
                    player,

                enemies =
                    enemyManager.allEnemies,

                onDamageDealt = {
                        appliedDamage,
                        sourceName ->

                    runStatistics
                        .recordDamageDealt(
                            amount =
                                appliedDamage,

                            weaponName =
                                sourceName
                        )
                },

                onEnemyKilled = {
                        killedEnemy ->

                    handleEnemyKilled(
                        killedEnemy
                    )
                }
            )

        /*
         * Aura hasarı oyuncuyu öldürmez ama
         * genel güvenlik için Weapon update'i
         * yalnızca oyuncu hayattaysa çalışır.
         */
        if (!player.health.isDead) {
            weaponRuntimeManager.update(
                delta = delta,

                player =
                    player,

                enemyManager =
                    enemyManager,

                projectiles =
                    projectiles,

                onDamageDealt = {
                        appliedDamage,
                        weaponName ->

                    runStatistics
                        .recordDamageDealt(
                            amount =
                                appliedDamage,

                            weaponName =
                                weaponName
                        )

                    player.tryLifeSteal(
                        appliedDamage =
                            appliedDamage
                    )
                },

                onEnemyKilled = {
                        killedEnemy ->

                    handleEnemyKilled(
                        killedEnemy
                    )
                }
            )
        }

        projectiles.forEach { projectile ->
            projectile.update(
                delta = delta,

                worldWidth =
                    worldWidth,

                worldHeight =
                    worldHeight
            )

            projectile.checkCollision(
                enemies =
                    enemyManager.allEnemies,

                onDamageDealt = {
                        appliedDamage ->

                    runStatistics
                        .recordDamageDealt(
                            amount =
                                appliedDamage,

                            weaponName =
                                projectile
                                    .sourceWeaponName
                        )

                    player.tryLifeSteal(
                        appliedDamage =
                            appliedDamage
                    )
                },

                onEnemyKilled = {
                        killedEnemy ->

                    handleEnemyKilled(
                        killedEnemy
                    )
                },

                onWeaponHit = {
                        hitEvent ->

                    tamgaRuntimeManager
                        .onWeaponHit(
                            event =
                                hitEvent,

                            onDamageDealt = {
                                    bonusDamage,
                                    weaponName ->

                                runStatistics
                                    .recordDamageDealt(
                                        amount =
                                            bonusDamage,

                                        weaponName =
                                            weaponName
                                    )

                                player.tryLifeSteal(
                                    appliedDamage =
                                        bonusDamage
                                )
                            },

                            onEnemyKilled = {
                                    killedEnemy ->

                                handleEnemyKilled(
                                    killedEnemy
                                )
                            }
                        )
                },

                modifyWeaponDamage = {
                        enemy,
                        baseDamage ->

                    tamgaRuntimeManager
                        .modifyWeaponDamage(
                            enemy =
                                enemy,

                            baseDamage =
                                baseDamage
                        )
                }
            )
        }

        projectiles.removeAll { projectile ->
            !projectile.isActive
        }

        goldCoinPickups.forEach { pickup ->
            pickup.update(
                delta = delta,

                targetX =
                    player.centerX,

                targetY =
                    player.centerY
            ) { collectedGold ->

                sessionWallet.addGold(
                    collectedGold
                )

                runStatistics.recordGoldCollected(
                    collectedGold
                )
            }
        }

        goldCoinPickups.removeAll { pickup ->
            pickup.isCollected
        }

        chestPickupManager.update(
            playerBounds =
                player.bounds
        ) {
            val availableStats =
                StatType.entries
                    .filter { statType ->
                        playerStats.totalScore(
                            statType
                        ) <
                            PlayerStats
                                .RUN_MAXIMUM_SCORE
                    }

            val availableItems =
                DefaultItems.all
                    .filter { definition ->
                        val maximumCopies =
                            definition
                                .maximumInventoryCopies
                                ?: definition
                                    .rarity
                                    .maximumInventoryCopies

                        maximumCopies == null ||
                            itemInventory
                                .countByDefinitionId(
                                    definition.id
                                ) <
                            maximumCopies
                    }

            val choiceSet =
                ChestRewardRoller
                    .rollChoiceSet(
                        luckScore =
                            playerStats
                                .luckScore(),

                        availableStatTypes =
                            availableStats,

                        availableItemDefinitions =
                            availableItems
                    )

            chestRewardQueue.addChoiceSet(
                choiceSet
            )
        }

        enemyManager.removeDeadEnemies { enemy ->

            runRewardTracker.onEnemyKilled(
                enemyType =
                    enemy.type,

                finalBossId =
                    if (
                        enemy.type ==
                        EnemyType.FINAL_BOSS
                    ) {
                        finalBossRewardId
                    } else {
                        null
                    }
            )
        }

        if (completeBossStageIfDefeated()) {
            return
        }

        updateCamera()

        enemySpawner.update(
            delta = delta,

            camera =
                camera,

            enemyManager =
                enemyManager,

            currentStage =
                stageController.currentStage,

            stageType =
                stageController
                    .currentStageType,

            enemyCountMultiplier =
                playerStats
                    .enemyCountMultiplier()
        )

        recordCurrentEnemyEncounters()
    }

    private fun handleEnemyKilled(
        killedEnemy: Enemy
    ) {

        runRewardTracker.onEnemyKilled(
            enemyType =
                killedEnemy.type,

            finalBossId =
                if (
                    killedEnemy.type ==
                    EnemyType.FINAL_BOSS
                ) {
                    finalBossRewardId
                } else {
                    null
                }
        )

        runStatistics.recordEnemyKilled(
            killedEnemy.type
        )

        val harvestedGoldReward =
            killedEnemy.goldReward *
                playerStats
                    .harvestGoldMultiplier()

        goldCoinPickups.add(
            GoldCoinPickup(
                startX =
                    killedEnemy.centerX,

                startY =
                    killedEnemy.centerY,

                value =
                    harvestedGoldReward
            )
        )

        tryDropChestFromEnemy(
            killedEnemy
        )
    }

    private fun applyChestReward(
        reward:
        ChestRewardRoll
    ) {
        when (reward.rewardType) {
            ChestRewardType.STAT -> {
                val statType =
                    reward.statType
                        ?: return

                val appliedAmount =
                    player.addRunStatBonus(
                        statType =
                            statType,

                        amount =
                            reward.statAmount
                    )

                if (
                    appliedAmount > 0 &&
                    statType ==
                    StatType.MAX_HP
                ) {
                    player
                        .synchronizeMaximumHealthWithStats(
                            healAddedHealth =
                                true
                        )
                }
            }

            ChestRewardType.ITEM -> {
                val itemDefinition =
                    reward.itemDefinition
                        ?: return

                val result =
                    itemInventory.addItem(
                        ItemInstance(
                            definition =
                                itemDefinition
                        )
                    )

                if (
                    result !=
                    ItemInventoryResult.ADDED
                ) {
                    return
                }

                itemStatSynchronizer
                    .synchronize()

                player
                    .synchronizeMaximumHealthWithStats(
                        healAddedHealth =
                            true
                    )
            }
        }
    }

    private fun tryDropChestFromEnemy(
        killedEnemy: Enemy
    ) {
        val dropSource =
            chestDropSourceFor(
                killedEnemy.type
            )

        val segmentMultiplier =
            chestDropSegmentTracker
                .currentDropChanceMultiplier()

        val shouldDropChest =
            ChestDropCalculator
                .shouldDropChest(
                    source =
                        dropSource,

                    luckScore =
                        playerStats.luckScore(),

                    segmentChanceMultiplier =
                        segmentMultiplier
                )

        if (!shouldDropChest) {
            return
        }

        chestPickupManager.spawn(
            worldX =
                killedEnemy.centerX,

            worldY =
                killedEnemy.centerY
        )

        chestDropSegmentTracker
            .recordDroppedChest()
    }

    private fun chestDropSourceFor(
        enemyType: EnemyType
    ): ChestDropSource {
        return when (enemyType) {
            EnemyType.NORMAL -> {
                ChestDropSource
                    .NORMAL_ENEMY
            }

            EnemyType.UNIQUE -> {
                ChestDropSource
                    .UNIQUE_ENEMY
            }

            EnemyType.MINI_BOSS -> {
                ChestDropSource
                    .MINI_BOSS
            }

            EnemyType.FINAL_BOSS -> {
                ChestDropSource
                    .FINAL_BOSS
            }
        }
    }

    private fun updateStage(
        delta: Float
    ): Boolean {
        val normalStageTimeCompleted =
            stageController.update(delta)

        if (!normalStageTimeCompleted) {
            return false
        }

        completeCurrentStage()
        return true
    }

    private fun completeCurrentStage() {
        val completedStage =
            stageController.currentStage

        val completedStageType =
            stageController.currentStageType

        collectAllGoldPickups()

        grantStageCompletionGold(
            completedStage = completedStage,
            completedStageType =
                completedStageType
        )

        enemyManager.clearAll()
        projectiles.clear()
        synchronizeWeaponInventoryEffects()
        weaponRuntimeManager.resetAllTimers()

        chestPickupManager
            .clearUncollectedChests()

        if (
            completedStageType ==
            StageType.MINI_BOSS
        ) {
            chestDropSegmentTracker
                .resetForNewBossInterval()
        }

        chestRewardController.begin(
            shouldOpenMarketAfterStatSelection =
                completedStageType ==
                    StageType.MINI_BOSS
        )
    }

    private fun renderGame() {
        Gdx.gl.glEnable(
            GL20.GL_BLEND
        )

        Gdx.gl.glBlendFunc(
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA
        )

        worldRenderer.render(
            mapObstacles =
                mapObstacles,

            player =
                player,

            enemyManager =
                enemyManager,

            goldCoinPickups =
                goldCoinPickups,

            chestPickupManager =
                chestPickupManager,

            projectiles =
                projectiles,

            renderBehindEntities = {
                    activeShapeRenderer ->

                tamgaRuntimeManager
                    .renderChargerExplosionFill(
                        shapeRenderer =
                            activeShapeRenderer
                    )

                tamgaRuntimeManager
                    .renderPhantomSteed(
                        shapeRenderer =
                            activeShapeRenderer,

                        player =
                            player
                    )

                tamgaRuntimeManager
                    .renderHolyLights(
                        shapeRenderer =
                            activeShapeRenderer,

                        player =
                            player
                    )

                tamgaRuntimeManager
                    .renderArcaneAura(
                        shapeRenderer =
                            activeShapeRenderer,

                        player =
                            player
                    )
            }
        )

        if (
            tamgaRuntimeManager
                .chargerExplosionEffects
                .isNotEmpty()
        ) {
            shapeRenderer.projectionMatrix =
                camera.combined

            shapeRenderer.begin(
                ShapeRenderer.ShapeType.Line
            )

            tamgaRuntimeManager
                .renderChargerExplosionRings(
                    shapeRenderer =
                        shapeRenderer
                )

            shapeRenderer.end()
        }

        Gdx.gl.glDisable(
            GL20.GL_BLEND
        )

        if (
            !isRunSummaryOpen &&
            !isMarketOpen &&
            !chestRewardController.isOpen &&
            !statCardController.isOpen &&
            !isPauseMenuOpen
        ) {
            activeTamgaHudRenderer.render(
                showWeaponButton =
                    tamgaRuntimeManager
                        .hasActiveWeaponTamga,

                weaponState =
                    tamgaRuntimeManager
                        .activeWeaponTamgaState,

                weaponRemainingActiveSeconds =
                    tamgaRuntimeManager
                        .activeWeaponTamgaRemainingActiveSeconds,

                weaponRemainingCooldownSeconds =
                    tamgaRuntimeManager
                        .activeWeaponTamgaRemainingCooldownSeconds,

                showArmorButton =
                    tamgaRuntimeManager
                        .hasActiveArmorTamga,

                armorState =
                    tamgaRuntimeManager
                        .activeArmorTamgaState,

                armorRemainingActiveSeconds =
                    tamgaRuntimeManager
                        .activeArmorTamgaRemainingActiveSeconds,

                armorRemainingCooldownSeconds =
                    tamgaRuntimeManager
                        .activeArmorTamgaRemainingCooldownSeconds
            )
        }

        hudRenderer.render(
            displayedStage =
                displayedStage(),

            remainingStageSeconds =
                remainingStageSeconds(),

            formattedGameTime =
                formatGameTime(),

            isRunSummaryOpen =
                isRunSummaryOpen,

            isMarketOpen =
                isMarketOpen,

            isShowingChestReward =
                chestRewardController.isOpen,

            isChoosingStatCard =
                statCardController.isOpen,

            renderRunSummary = {
                runSummaryRenderer.render(
                    displayedStage =
                        displayedStage(),

                    formattedGameTime =
                        formatGameTime()
                )
            },

            renderMarket = {
                marketRenderer.render(
                    marketSlots =
                        marketSlots,

                    marketRerollsUsed =
                        marketRerollsUsed,

                    itemScrollOffset =
                        itemScrollOffset,

                    interactionState =
                        marketInteractionState
                )
            },

            renderChestReward = {
                chestRewardController.render()
            },

            renderStatCardSelection = {
                statCardController.render()
            }
        )

        if (isPauseMenuOpen) {
            hudBatch.projectionMatrix =
                camera.combined

            hudBatch.begin()

            pauseRenderer.render(
                interactionState =
                    pauseInteractionState
            )

            hudBatch.end()
        }
    }

    private fun updateCamera() {
        val halfCameraWidth =
            CAMERA_WIDTH / 2f

        val halfCameraHeight =
            CAMERA_HEIGHT / 2f

        val minimumCameraX =
            halfCameraWidth

        val maximumCameraX =
            worldWidth - halfCameraWidth

        val minimumCameraY =
            halfCameraHeight

        val maximumCameraY =
            worldHeight - halfCameraHeight

        camera.position.x =
            player.centerX.coerceIn(
                minimumCameraX,
                maximumCameraX
            )

        camera.position.y =
            player.centerY.coerceIn(
                minimumCameraY,
                maximumCameraY
            )

        camera.update()
    }

    override fun resize(
        width: Int,
        height: Int
    ) {
        if (width <= 0 || height <= 0) {
            return
        }

        viewport.update(
            width,
            height,
            false
        )
    }

    override fun dispose() {

        if (
            Gdx.input.inputProcessor ===
            marketInputController
        ) {
            Gdx.input.inputProcessor =
                previousInputProcessor
        }

        previousInputProcessor = null

        marketRenderer.dispose()
        pauseRenderer.dispose()

        shapeRenderer.dispose()
        hudBatch.dispose()
        hudFont.dispose()
    }

    private fun formatGameTime(): String {
        val totalSeconds =
            gameTimeController
                .elapsedGameTimeSeconds
                .toInt()

        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        return String.format(
            "%02d:%02d",
            minutes,
            seconds
        )
    }

    private fun displayedStage(): Int {
        return stageController.currentStage.coerceAtMost(
            stageController.maximumStage
        )
    }

    private fun remainingStageSeconds(): Int {
        return kotlin.math.ceil(
            stageController.remainingStageTime
        ).toInt()
    }
    private fun completeBossStageIfDefeated(): Boolean {
        val requiredBossType =
            when (stageController.currentStageType) {
                StageType.NORMAL ->
                    return false

                StageType.MINI_BOSS ->
                    EnemyType.MINI_BOSS

                StageType.FINAL_BOSS ->
                    EnemyType.FINAL_BOSS
            }

        if (!enemySpawner.hasSpawnedBoss) {
            return false
        }

        val bossIsStillAlive =
            enemyManager.hasLivingEnemyOfType(
                requiredBossType
            )

        if (bossIsStillAlive) {
            return false
        }

        if (
            stageController.currentStageType ==
            StageType.FINAL_BOSS
        ) {
            val completedStage =
                stageController.currentStage

            val completedStageType =
                stageController.currentStageType

            collectAllGoldPickups()

            grantStageCompletionGold(
                completedStage =
                    completedStage,

                completedStageType =
                    completedStageType
            )

            chestPickupManager
                .clearUncollectedChests()

            enemyManager.clearAll()
            projectiles.clear()
            weaponRuntimeManager.resetAllTimers()

            stageController.completeCurrentStage()

            return true
        }

        completeCurrentStage()

        return true
    }

    private fun ownedWeaponDefinitionIds():
        Set<String> {

        return (
            0 until
                WeaponInventory
                    .WEAPON_SLOT_COUNT
            )
            .mapNotNull { slotIndex ->
                weaponInventory
                    .weaponAt(
                        slotIndex
                    )
                    ?.definition
                    ?.id
                    ?.value
            }
            .toSet()
    }

    private fun openMarket() {
        isMarketOpen = true
        marketRerollsUsed = 0
        itemScrollOffset = 0

        marketInputController.reset()

        previousInputProcessor =
            Gdx.input.inputProcessor

        Gdx.input.inputProcessor =
            marketInputController

        val marketSlotCount =
            4

        /*
         * Önce korunmayan slotların kaç tane
         * olduğunu hesaplıyoruz.
         */
        val missingSlotCount =
            (
                0 until marketSlotCount
                )
                .count { slotIndex ->
                    lockedSlotsForNextMarket
                        .getOrNull(
                            slotIndex
                        ) ==
                        null
                }

        /*
         * Yalnızca boş pozisyonlar için yeni
         * teklifler üretilir.
         */

        val generatedOffers =
            MarketOfferGenerator
                .generateOffers(
                    currentStage =
                        stageController.currentStage,

                    offerCount =
                        missingSlotCount,

                    luckScore =
                        playerStats.luckScore(),

                    selectedCharacter =
                        selectedCharacter,

                    ownedWeaponIds =
                        ownedWeaponDefinitionIds()
                )

        var generatedOfferIndex =
            0

        marketSlots =
            List(
                marketSlotCount
            ) { slotIndex ->

                val carriedLockedSlot =
                    lockedSlotsForNextMarket
                        .getOrNull(
                            slotIndex
                        )

                if (
                    carriedLockedSlot !=
                    null
                ) {
                    /*
                     * Kilitli kart aynı slot indeksinde
                     * tutulur.
                     */
                    MarketSlot(
                        offer =
                            carriedLockedSlot.offer,

                        isLocked =
                            true,

                        isSold =
                            false
                    )
                } else {
                    /*
                     * Kilitli olmayan pozisyona sıradaki
                     * yeni teklif yerleştirilir.
                     */
                    val generatedOffer =
                        generatedOffers
                            .getOrNull(
                                generatedOfferIndex
                            )

                    generatedOfferIndex++

                    MarketSlot(
                        offer =
                            generatedOffer,

                        isLocked =
                            false,

                        isSold =
                            false
                    )
                }
            }

        /*
         * Taşınan kilitli teklifler yeni markete
         * aktarıldı. Bir sonraki kapanışta yeniden
         * kaydedilecekler.
         */
        lockedSlotsForNextMarket =
            List(
                marketSlotCount
            ) {
                null
            }
    }

    private fun closeMarket() {
        val marketSlotCount =
            4

        /*
         * Her pozisyon ayrı ayrı kaydedilir.
         *
         * Böylece örneğin 3. slotta kilitlenen kart,
         * sonraki markette yine 3. slotta kalır.
         */
        lockedSlotsForNextMarket =
            List(
                marketSlotCount
            ) { slotIndex ->

                val slot =
                    marketSlots.getOrNull(
                        slotIndex
                    )

                if (
                    slot != null &&
                    slot.isLocked &&
                    !slot.isSold &&
                    slot.offer != null
                ) {
                    MarketSlot(
                        offer =
                            slot.offer,

                        isLocked =
                            true,

                        isSold =
                            false
                    )
                } else {
                    null
                }
            }

        if (
            Gdx.input.inputProcessor ===
            marketInputController
        ) {
            Gdx.input.inputProcessor =
                previousInputProcessor
        }

        previousInputProcessor =
            null

        marketInputController.reset()

        isMarketOpen = false

        marketSlots =
            emptyList()

        marketRerollsUsed = 0
        itemScrollOffset = 0

        enemySpawner.resetTimer()
    }

    private fun handleWeaponDrop(
        sourceSlotIndex: Int,
        targetSlotIndex: Int?,
        droppedOnSellArea: Boolean
    ) {

        val sourceWeapon =
            weaponInventory.weaponAt(
                sourceSlotIndex
            ) ?: return

        if (droppedOnSellArea) {
            requestWeaponSale(
                sourceSlotIndex =
                    sourceSlotIndex
            )

            return
        }

        if (targetSlotIndex == null) {
            return
        }

        if (
            sourceSlotIndex ==
            targetSlotIndex
        ) {
            return
        }

        val targetWeapon =
            weaponInventory.weaponAt(
                targetSlotIndex
            )

        /*
         * Boş slota bırakma:
         * Move işlemi swapSlots ile gerçekleşir.
         */
        if (targetWeapon == null) {
            weaponInventory.swapSlots(
                sourceSlotIndex,
                targetSlotIndex
            )

            weaponRuntimeManager
                .resetAllTimers()

            return
        }

        val sameWeapon =
            sourceWeapon.definition.id ==
                targetWeapon.definition.id

        /*
         * Farklı Weapon:
         * Slotlar yer değiştirir.
         */
        if (!sameWeapon) {
            weaponInventory.swapSlots(
                sourceSlotIndex,
                targetSlotIndex
            )

            weaponRuntimeManager
                .resetAllTimers()

            return
        }

        /*
         * Hedef Lv.5 ise yükseltilemez.
         */
        if (
            targetWeapon.level >=
            WeaponInstance.MAXIMUM_LEVEL
        ) {
            return
        }

        /*
 * Yüksek seviyeli Weapon korunur.
 * Düşük seviyeli Weapon tüketilir.
 *
 * Seviyeler eşitse hedef korunur,
 * kaynak tüketilir.
 */
        val survivingWeapon =
            if (
                sourceWeapon.level >
                targetWeapon.level
            ) {
                sourceWeapon
            } else {
                targetWeapon
            }

        val consumedWeapon =
            if (
                survivingWeapon === sourceWeapon
            ) {
                targetWeapon
            } else {
                sourceWeapon
            }

        val resultLevel =
            (
                survivingWeapon.level +
                    1
                )
                .coerceAtMost(
                    WeaponInstance.MAXIMUM_LEVEL
                )

        /*
         * Tüketilecek Weapon Lv.1 ise doğrudan merge.
         * Lv.2+ değerli bir Weapon kaybedilecekse
         * kullanıcıdan onay alınır.
         */
        if (
            consumedWeapon.level ==
            WeaponInstance.MINIMUM_LEVEL
        ) {
            performWeaponMerge(
                sourceSlotIndex =
                    sourceSlotIndex,

                targetSlotIndex =
                    targetSlotIndex
            )

            return
        }

        marketInteractionState
            .openMergeConfirmation(
                MarketInteractionState
                    .PendingWeaponMerge(
                        sourceSlotIndex =
                            sourceSlotIndex,

                        targetSlotIndex =
                            targetSlotIndex,

                        sourceWeaponName =
                            sourceWeapon.displayName,

                        /*
                         * Popup'taki sourceWeaponLevel alanı
                         * artık tüketilecek Weapon seviyesidir.
                         */
                        sourceWeaponLevel =
                            consumedWeapon.level,

                        /*
                         * Bu alan korunan Weapon seviyesini
                         * temsil eder.
                         */
                        targetWeaponLevel =
                            survivingWeapon.level,

                        resultWeaponLevel =
                            resultLevel
                    )
            )
    }

    private fun requestWeaponSale(
        sourceSlotIndex: Int
    ) {
        val weapon =
            weaponInventory.weaponAt(
                sourceSlotIndex
            ) ?: return

        val salePrice =
            WeaponSalePriceCalculator
                .calculateSalePrice(
                    weaponLevel =
                        weapon.level,

                    currentStage =
                        stageController.currentStage
                )

        /*
         * Lv.1 Weapon doğrudan satılır.
         */
        if (
            weapon.level ==
            WeaponInstance.MINIMUM_LEVEL
        ) {
            performWeaponSale(
                sourceSlotIndex =
                    sourceSlotIndex,

                salePrice =
                    salePrice
            )

            return
        }

        /*
         * Lv.2–5 Weapon için onay gerekir.
         */
        marketInteractionState
            .openSaleConfirmation(
                MarketInteractionState
                    .PendingWeaponSale(
                        sourceSlotIndex =
                            sourceSlotIndex,

                        weaponName =
                            weapon.displayName,

                        weaponLevel =
                            weapon.level,

                        salePrice =
                            salePrice
                    )
            )
    }

    private fun confirmPendingWeaponSale() {
        val pendingSale =
            marketInteractionState
                .pendingWeaponSale
                ?: return

        performWeaponSale(
            sourceSlotIndex =
                pendingSale.sourceSlotIndex,

            salePrice =
                pendingSale.salePrice
        )

        marketInteractionState
            .closeSaleConfirmation()
    }

    private fun performWeaponSale(
        sourceSlotIndex: Int,
        salePrice: Int
    ) {
        val removedWeapon =
            weaponInventory
                .removeWeaponFromSlot(
                    sourceSlotIndex
                )
                ?: return

        sessionWallet.addGold(
            salePrice.toFloat()
        )

        synchronizeWeaponInventoryEffects()

        weaponRuntimeManager
            .resetAllTimers()

        if (
            marketInteractionState
                .detailedWeaponSlotIndex ==
            sourceSlotIndex
        ) {
            marketInteractionState
                .detailedWeaponSlotIndex =
                null
        }
    }

    private fun synchronizeWeaponInventoryEffects() {
        weaponStatBonusSynchronizer
            .invalidate()

        weaponStatBonusSynchronizer
            .synchronize()

        player
            .synchronizeMaximumHealthWithStats(
                healAddedHealth = false
            )
    }

    private fun confirmPendingWeaponMerge() {
        val pendingMerge =
            marketInteractionState
                .pendingWeaponMerge
                ?: return

        performWeaponMerge(
            sourceSlotIndex =
                pendingMerge.sourceSlotIndex,

            targetSlotIndex =
                pendingMerge.targetSlotIndex
        )

        marketInteractionState
            .closeMergeConfirmation()
    }

    private fun performWeaponMerge(
        sourceSlotIndex: Int,
        targetSlotIndex: Int
    ) {
        val result =
            weaponInventory.mergeSlots(
                sourceSlotIndex =
                    sourceSlotIndex,

                targetSlotIndex =
                    targetSlotIndex
            )

        if (
            result ==
            WeaponInventoryResult.MERGED
        ) {
            synchronizeWeaponInventoryEffects()

            weaponRuntimeManager
                .resetAllTimers()
        }
    }

    private fun handleMarketControls(
        delta: Float
    ) {

        marketInteractionState.update(
            delta
        )

        marketInputController
            .handleInput(
                delta =
                    delta,

                onPurchaseOffer = {
                        slotIndex ->

                    purchaseMarketOffer(
                        slotIndex
                    )
                },

                onMarketOfferDropped = {
                        offerIndex,
                        targetWeaponSlotIndex,
                        droppedOnItemPanel,
                        droppedOnStatsPanel ->

                    handleMarketOfferDrop(
                        offerIndex =
                            offerIndex,

                        targetWeaponSlotIndex =
                            targetWeaponSlotIndex,

                        droppedOnItemPanel =
                            droppedOnItemPanel,

                        droppedOnStatsPanel =
                            droppedOnStatsPanel
                    )
                },

                onToggleOfferLock = {
                        slotIndex ->

                    toggleMarketOfferLock(
                        slotIndex
                    )
                },

                onReturnToGame = {
                    closeMarket()
                },

                onReroll = {
                    rerollMarket()
                },

                canDragWeapon = {
                        slotIndex ->

                    weaponInventory
                        .weaponAt(
                            slotIndex
                        ) != null
                },

                onWeaponClicked = {
                        slotIndex ->

                    marketInteractionState
                        .showWeaponDetails(
                            slotIndex
                        )
                },

                onItemClicked = {
                        visibleRowIndex ->

                    openItemDetailsByVisibleRow(
                        visibleRowIndex
                    )
                },

                onItemScroll = {
                        scrollSteps ->

                    scrollMarketItems(
                        requestedSteps =
                            scrollSteps
                    )
                },

                onTamgaClicked = {
                    marketInteractionState
                        .showTamgaDetails()
                },

                onCloseDetailModal = {
                    marketInteractionState
                        .closeDetailModal()
                },

                onWeaponDropped = {
                        sourceSlotIndex,
                        targetSlotIndex,
                        droppedOnSellArea ->

                    handleWeaponDrop(
                        sourceSlotIndex =
                            sourceSlotIndex,

                        targetSlotIndex =
                            targetSlotIndex,

                        droppedOnSellArea =
                            droppedOnSellArea
                    )
                },

                onConfirmMerge = {
                    confirmPendingWeaponMerge()
                },

                onCancelMerge = {
                    marketInteractionState
                        .closeMergeConfirmation()
                },

                onConfirmSale = {
                    confirmPendingWeaponSale()
                },

                onCancelSale = {
                    marketInteractionState
                        .closeSaleConfirmation()
                }
            )
    }

    private fun showMarketFailure(
        message: String,
        offerIndex: Int? = null
    ) {
        marketInteractionState
            .showFailureFeedback(
                message =
                    message,

                offerIndex =
                    offerIndex
            )
    }

    private fun scrollMarketItems(
        requestedSteps: Int
    ) {
        if (requestedSteps == 0) {
            return
        }

        val groupedItemCount =
            itemInventory
                .allItems()
                .distinctBy { item ->
                    item.definition.id
                }
                .size

        val layout =
            marketRenderer
                .currentLayout()

        val maximumVisibleRows =
            (
                (
                    layout.itemPanel.height -
                        48f
                    ) / 24f
                )
                .toInt()
                .coerceAtLeast(1)

        val maximumOffset =
            (
                groupedItemCount -
                    maximumVisibleRows
                )
                .coerceAtLeast(0)

        itemScrollOffset =
            (
                itemScrollOffset +
                    requestedSteps
                )
                .coerceIn(
                    0,
                    maximumOffset
                )
    }

    private fun openItemDetailsByVisibleRow(
        visibleRowIndex: Int
    ) {
        /*
         * Renderer ile aynı sıra:
         * İlk görülen kopyanın sırası korunur,
         * aynı Item'lar tek satırda gruplanır.
         */
        val groupedDefinitions =
            itemInventory
                .allItems()
                .distinctBy { item ->
                    item.definition.id
                }

        val actualItemIndex =
            itemScrollOffset +
                visibleRowIndex

        val itemDefinition =
            groupedDefinitions
                .getOrNull(
                    actualItemIndex
                )
                ?.definition
                ?: return

        marketInteractionState
            .showItemDetails(
                itemDefinition.id.value
            )
    }

    private fun handleMarketOfferDrop(
        offerIndex: Int,
        targetWeaponSlotIndex: Int?,
        droppedOnItemPanel: Boolean,
        droppedOnStatsPanel: Boolean
    ) {
        val marketSlot =
            marketSlots.getOrNull(
                offerIndex
            ) ?: return

        if (
            marketSlot.isSold ||
            marketSlot.offer == null
        ) {
            return
        }

        val offer =
            marketSlot.offer
                ?: return

        when (offer.productType) {
            MarketProductType.ITEM -> {

                if (!droppedOnItemPanel) {
                    showMarketFailure(
                        message =
                            "DROP ITEM ON ITEMS",

                        offerIndex =
                            offerIndex
                    )

                    return
                }

                purchaseMarketOffer(
                    offerIndex
                )
            }

            MarketProductType.WEAPON -> {
                /*
                 * Weapon belirli bir Weapon slotuna
                 * bırakılmalıdır.
                 */
                val targetSlotIndex =
                    targetWeaponSlotIndex
                        ?: run {
                            showMarketFailure(
                                message =
                                    "DROP ON EMPTY OR MATCHING SLOT",

                                offerIndex =
                                    offerIndex
                            )

                            return
                        }

                purchaseWeaponOfferIntoSlot(
                    offerIndex =
                        offerIndex,

                    targetSlotIndex =
                        targetSlotIndex
                )
            }

            MarketProductType.STAT -> {
                if (!droppedOnStatsPanel) {
                    showMarketFailure(
                        message =
                            "DROP STAT ON STATS",

                        offerIndex =
                            offerIndex
                    )

                    return
                }

                purchaseMarketOffer(
                    offerIndex
                )
            }
        }
    }

    private fun purchaseWeaponOfferIntoSlot(
        offerIndex: Int,
        targetSlotIndex: Int
    ) {
        val marketSlot =
            marketSlots.getOrNull(
                offerIndex
            ) ?: return

        if (marketSlot.isSold) {
            return
        }

        val offer =
            marketSlot.offer
                ?: return

        if (
            offer.productType !=
            MarketProductType.WEAPON
        ) {
            return
        }

        val weaponDefinition =
            offer.weaponDefinition
                ?: return

        if (
            sessionWallet.goldCoins <
            offer.price
        ) {
            showMarketFailure(
                message =
                    "NOT ENOUGH GOLD COIN",

                offerIndex =
                    offerIndex
            )

            return
        }

        val newWeapon =
            WeaponInstance(
                definition =
                    weaponDefinition,

                level =
                    offer.productLevel
                        .coerceIn(
                            WeaponInstance.MINIMUM_LEVEL,
                            WeaponInstance.MAXIMUM_LEVEL
                        )
            )

        val inventoryResult =
            weaponInventory
                .addPurchasedWeaponToSlot(
                    weapon =
                        newWeapon,

                    targetSlotIndex =
                        targetSlotIndex
                )

        val purchaseSucceeded =
            when (inventoryResult) {
                WeaponInventoryResult
                    .ADDED_TO_EMPTY_SLOT,

                WeaponInventoryResult
                    .MERGED,

                WeaponInventoryResult
                    .UNIQUE_WEAPON_LEVELLED_UP -> {
                    true
                }

                WeaponInventoryResult
                    .INVENTORY_FULL,

                WeaponInventoryResult
                    .INCOMPATIBLE_WEAPONS,

                WeaponInventoryResult
                    .MAXIMUM_LEVEL_REACHED,

                WeaponInventoryResult
                    .INVALID_SLOT -> {
                    false
                }
            }

        if (!purchaseSucceeded) {
            val failureMessage =
                when (
                    inventoryResult
                ) {
                    WeaponInventoryResult
                        .INCOMPATIBLE_WEAPONS -> {
                        "DROP ON EMPTY OR MATCHING SLOT"
                    }

                    WeaponInventoryResult
                        .MAXIMUM_LEVEL_REACHED -> {
                        "WEAPON IS ALREADY LEVEL V"
                    }

                    WeaponInventoryResult
                        .INVENTORY_FULL -> {
                        "WEAPON INVENTORY FULL"
                    }

                    else -> {
                        "PURCHASE UNAVAILABLE"
                    }
                }

            showMarketFailure(
                message =
                    failureMessage,

                offerIndex =
                    offerIndex
            )

            return
        }

        val paymentSuccessful =
            sessionWallet.spendGold(
                amount =
                    offer.price
            )

        if (!paymentSuccessful) {
            return
        }

        runStatistics.recordGoldSpent(
            offer.price.toFloat()
        )

        weaponRuntimeManager
            .resetAllTimers()

        marketSlots =
            marketSlots
                .toMutableList()
                .apply {
                    this[offerIndex] =
                        marketSlot.copy(
                            isLocked = false,
                            isSold = true
                        )
                }
    }

    private fun toggleMarketOfferLock(
        slotIndex: Int
    ) {
        val slot =
            marketSlots.getOrNull(slotIndex)
                ?: return

        if (
            slot.isSold ||
            slot.offer == null
        ) {
            return
        }

        marketSlots =
            marketSlots.toMutableList().apply {
                this[slotIndex] =
                    slot.copy(
                        isLocked = !slot.isLocked
                    )
            }
    }

    private fun purchaseMarketOffer(
        slotIndex: Int
    ) {
        val slot =
            marketSlots.getOrNull(
                slotIndex
            ) ?: return

        if (slot.isSold) {
            return
        }

        val offer =
            slot.offer
                ?: return

        /*
         * Yetersiz Gold varsa envantere dokunma.
         */
        if (
            sessionWallet.goldCoins <
            offer.price
        ) {
            showMarketFailure(
                message =
                    "NOT ENOUGH GOLD COIN",

                offerIndex =
                    slotIndex
            )

            return
        }

        val offerApplied =
            applyMarketOffer(
                offer
            )

        if (!offerApplied) {
            showMarketFailure(
                message =
                    purchaseFailureMessage(
                        offer
                    ),

                offerIndex =
                    slotIndex
            )

            return
        }

        if (
            offer.productType ==
            MarketProductType.WEAPON
        ) {
            synchronizeWeaponInventoryEffects()

            weaponRuntimeManager
                .resetAllTimers()
        }

        val paymentSuccessful =
            sessionWallet.spendGold(
                amount =
                    offer.price
            )

        /*
         * Yukarıda bakiye kontrol edildiği için
         * normalde false olmamalı.
         */
        if (!paymentSuccessful) {
            return
        }

        runStatistics.recordGoldSpent(
            offer.price.toFloat()
        )

        marketSlots =
            marketSlots
                .toMutableList()
                .apply {
                    this[slotIndex] =
                        slot.copy(
                            isLocked = false,
                            isSold = true
                        )
                }
    }

    private fun purchaseFailureMessage(
        offer: MarketOffer
    ): String {
        return when (
            offer.productType
        ) {
            MarketProductType.ITEM -> {
                "ITEM LIMIT REACHED"
            }

            MarketProductType.STAT -> {
                "STAT IS ALREADY AT MAXIMUM"
            }

            MarketProductType.WEAPON -> {
                val weaponDefinition =
                    offer.weaponDefinition
                        ?: return "PURCHASE UNAVAILABLE"

                val equippedWeapons =
                    (
                        0 until
                            WeaponInventory
                                .WEAPON_SLOT_COUNT
                        )
                        .mapNotNull { slotIndex ->
                            weaponInventory
                                .weaponAt(
                                    slotIndex
                                )
                        }

                val matchingWeapons =
                    equippedWeapons.filter {
                            weapon ->

                        weapon.definition.id ==
                            weaponDefinition.id
                    }

                when {
                    matchingWeapons.isNotEmpty() &&
                        matchingWeapons.all {
                                weapon ->

                            weapon.level >=
                                WeaponInstance
                                    .MAXIMUM_LEVEL
                        } -> {
                        "WEAPON IS ALREADY LEVEL V"
                    }

                    equippedWeapons.size >=
                        WeaponInventory
                            .WEAPON_SLOT_COUNT -> {
                        "WEAPON INVENTORY FULL"
                    }

                    else -> {
                        "PURCHASE UNAVAILABLE"
                    }
                }
            }
        }
    }

    private fun applyMarketOffer(
        offer: MarketOffer
    ): Boolean {
        return when (
            offer.productType
        ) {
            MarketProductType.WEAPON -> {
                purchaseWeaponOffer(
                    offer
                )
            }

            MarketProductType.ITEM -> {
                purchaseItemOffer(
                    offer
                )
            }

            MarketProductType.STAT -> {
                purchaseStatOffer(
                    offer
                )
            }
        }
    }

    private fun purchaseWeaponOffer(
        offer: MarketOffer
    ): Boolean {
        val weaponDefinition =
            offer.weaponDefinition
                ?: return false

        val newWeapon =
            WeaponInstance(
                definition =
                    weaponDefinition,

                level =
                    offer.productLevel
                        .coerceIn(
                            WeaponInstance.MINIMUM_LEVEL,
                            WeaponInstance.MAXIMUM_LEVEL
                        )
            )

        val result =
            weaponInventory
                .addPurchasedWeapon(
                    newWeapon
                )

        return when (result) {
            WeaponInventoryResult
                .ADDED_TO_EMPTY_SLOT -> {
                true
            }

            WeaponInventoryResult
                .UNIQUE_WEAPON_LEVELLED_UP -> {
                true
            }

            WeaponInventoryResult.MERGED -> {
                true
            }

            WeaponInventoryResult
                .INVENTORY_FULL,

            WeaponInventoryResult
                .INCOMPATIBLE_WEAPONS,

            WeaponInventoryResult
                .MAXIMUM_LEVEL_REACHED,

            WeaponInventoryResult
                .INVALID_SLOT -> {
                false
            }
        }
    }

    private fun purchaseItemOffer(
        offer: MarketOffer
    ): Boolean {
        val itemDefinition =
            offer.itemDefinition
                ?: return false

        val result =
            itemInventory.addItem(
                ItemInstance(
                    definition =
                        itemDefinition
                )
            )

        if (
            result !=
            ItemInventoryResult.ADDED
        ) {
            return false
        }

        itemStatSynchronizer.synchronize()


        player
            .synchronizeMaximumHealthWithStats(
                healAddedHealth = true
            )

        return true
    }

    private fun purchaseStatOffer(
        offer: MarketOffer
    ): Boolean {
        val statType =
            offer.statType
                ?: return false

        val statAmount =
            offer.statAmount

        if (statAmount <= 0) {
            return false
        }

        val appliedAmount =
            player.addRunStatBonus(
                statType =
                    statType,

                amount =
                    statAmount
            )

        return appliedAmount > 0
    }

    private fun rerollMarket() {
        val rerollPrice =
            MarketRerollPriceCalculator
                .calculatePrice(
                    currentStage =
                        stageController.currentStage,
                    rerollsUsedInCurrentMarket =
                        marketRerollsUsed
                )

        if (rerollPrice > 0) {
            val rerollSuccessful =
                sessionWallet.spendGold(
                    amount =
                        rerollPrice
                )

            if (!rerollSuccessful) {
                showMarketFailure(
                    message =
                        "NOT ENOUGH GOLD COIN",

                    offerIndex =
                        null
                )

                return
            }

            runStatistics.recordGoldSpent(
                rerollPrice.toFloat()
            )
        }

        marketRerollsUsed++

        val rerolledSlotCount =
            marketSlots.count { slot ->
                !slot.isLocked ||
                    slot.isSold
            }

        val newOffers =
            MarketOfferGenerator
                .generateOffers(
                    currentStage =
                        stageController.currentStage,

                    offerCount =
                        rerolledSlotCount,

                    luckScore =
                        playerStats.luckScore(),

                    selectedCharacter =
                        selectedCharacter,

                    ownedWeaponIds =
                        ownedWeaponDefinitionIds()
                )

        var newOfferIndex =
            0

        marketSlots =
            marketSlots.map { slot ->

                /*
                 * Kilitli ve satın alınmamış kart aynı
                 * pozisyonda, aynı teklifle korunur.
                 */
                if (
                    slot.isLocked &&
                    !slot.isSold &&
                    slot.offer != null
                ) {
                    slot
                } else {
                    val newOffer =
                        newOffers.getOrNull(
                            newOfferIndex
                        )

                    newOfferIndex++

                    MarketSlot(
                        offer =
                            newOffer,

                        isLocked =
                            false,

                        isSold =
                            false
                    )
                }
            }
    }

    private fun recordCurrentEnemyEncounters() {

        enemyManager
            .allEnemies
            .forEach { enemy ->

                PlayerProfileRepository
                    .markEnemyEncountered(
                        CollectionEncounterIds.enemy(
                            enemy.type
                        )
                    )
            }
    }

}
