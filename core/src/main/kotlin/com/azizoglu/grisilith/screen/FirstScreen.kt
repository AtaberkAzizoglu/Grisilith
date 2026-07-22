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
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.character.CharacterDefinition
import com.azizoglu.grisilith.weapon.definition.DefaultWeapons
import com.azizoglu.grisilith.weapon.runtime.WeaponInstance
import com.azizoglu.grisilith.weapon.runtime.WeaponInventory
import com.azizoglu.grisilith.screen.render.HudRenderer
import com.azizoglu.grisilith.screen.render.MarketRenderer
import com.azizoglu.grisilith.screen.render.RunSummaryRenderer
import com.azizoglu.grisilith.screen.controller.StatCardController
import com.azizoglu.grisilith.screen.render.WorldRenderer
import com.azizoglu.grisilith.weapon.runtime.WeaponRuntimeManager


class FirstScreen(
    private val game: GrisilithGame,
    private val difficulty: GameDifficulty,
    private val gameMap: GameMap,
    private val selectedCharacter:
    CharacterDefinition
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
                selectedCharacter.baseStats
        )

    private val weaponInventory =
        WeaponInventory()

    private val weaponRuntimeManager =
        WeaponRuntimeManager(
            inventory = weaponInventory,
            playerStats = playerStats
        )

    private val player = Player(
        startX = worldWidth / 2f,
        startY = worldHeight / 2f,
        worldWidth = worldWidth,
        worldHeight = worldHeight,
        stats = playerStats
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

    private var marketSlots =
        emptyList<MarketSlot>()

    private var lockedSlotsForNextMarket =
        emptyList<MarketSlot>()

    private var marketRerollsUsed = 0

    private val stageController = StageController(
        normalStageDurationSeconds = 30f,
        miniBossStageDurationSeconds = 45f,
        finalBossStageDurationSeconds = 60f,
        maximumStage = 20
    )


    private val sessionWallet =
        SessionWallet()

    private val goldCoinPickups =
        mutableListOf<GoldCoinPickup>()


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
                runStatistics
        )

    private val marketRenderer =
        MarketRenderer(
            camera = camera,
            cameraWidth = CAMERA_WIDTH,
            batch = hudBatch,
            font = hudFont,
            sessionWallet =
                sessionWallet,
            stageController =
                stageController
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

    override fun show() {
        viewport.apply()

        camera.position.set(
            player.centerX,
            player.centerY,
            0f
        )

        weaponInventory.addWeapon(
            WeaponInstance(
                definition =
                    DefaultWeapons.staff
            )
        )

        weaponInventory.addWeapon(
            WeaponInstance(
                definition =
                    DefaultWeapons.wand
            )
        )

        weaponInventory.addWeapon(
            WeaponInstance(
                definition =
                    DefaultWeapons.theBook
            )
        )

        camera.update()
    }

    override fun render(delta: Float) {
        handleGameControls()

        if (
            player.health.isDead ||
            stageController.isRunComplete
        ) {
            isRunSummaryOpen = true
        }

        val gameplayStopped =
            player.health.isDead ||
                stageController.isRunComplete ||
                statCardController.isOpen ||
                isMarketOpen ||
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

    private fun handleGameControls() {
        if (isRunSummaryOpen) {
            handleRunSummaryControls()
            return
        }

        if (isMarketOpen) {
            handleMarketControls()
            return
        }

        if (statCardController.isOpen) {
            statCardController.handleInput()
            return
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            gameTimeController.togglePause()
        }

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

    private fun handleRunSummaryControls() {
        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.R
            )
        ) {
            game.startRun(
                difficulty = difficulty,
                gameMap = gameMap,
                character = selectedCharacter
            )
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

    private fun updateGame(delta: Float) {
        if (player.health.isDead) {
            return
        }

        val stageChanged =
            updateStage(delta)

        if (
            stageChanged ||
            stageController.isRunComplete
        ) {
            return
        }

        player.update(delta)

        mapObstacles.forEach { obstacle ->
            player.resolveObstacleCollision(
                obstacle.bounds
            )
        }

        enemyManager.resolvePlayerCollisions { enemyBounds ->
            player.resolveCollision(enemyBounds)
        }

        enemyManager.update(
            delta = delta,
            targetX = player.centerX,
            targetY = player.centerY,
            targetBounds = player.bounds,
            worldWidth = worldWidth,
            worldHeight = worldHeight
        ) { damage ->
            val appliedDamage =
                player.takeDamage(damage)

            runStatistics.recordDamageTaken(
                appliedDamage
            )
        }

        if (!player.health.isDead) {
            weaponRuntimeManager.update(
                delta = delta,
                player = player,
                enemyManager = enemyManager,
                projectiles = projectiles,
                onDamageDealt = {
                        appliedDamage,
                        weaponName ->

                    runStatistics.recordDamageDealt(
                        amount = appliedDamage,
                        weaponName = weaponName
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
                worldWidth = worldWidth,
                worldHeight = worldHeight
            )

            projectile.checkCollision(
                enemies = enemyManager.allEnemies,
                onDamageDealt = { appliedDamage ->
                    runStatistics.recordDamageDealt(
                        amount = appliedDamage,
                        weaponName =
                            projectile.sourceWeaponName
                    )

                    player.tryLifeSteal(
                        appliedDamage = appliedDamage
                    )
                }
            ) { killedEnemy ->
                handleEnemyKilled(
                    killedEnemy
                )
            }
        }

        projectiles.removeAll { projectile ->
            !projectile.isActive
        }

        goldCoinPickups.forEach { pickup ->
            pickup.update(
                delta = delta,
                targetX = player.centerX,
                targetY = player.centerY
            ) { collectedGold ->
                sessionWallet.addGold(collectedGold)
                runStatistics.recordGoldCollected(
                    collectedGold
                )
            }
        }

        goldCoinPickups.removeAll { pickup ->
            pickup.isCollected
        }

        enemyManager.removeDeadEnemies()

        /*
         * Mermiyle boss bu frame içinde öldüyse
         * aşamayı hemen tamamlar.
         */
        if (completeBossStageIfDefeated()) {
            return
        }

        updateCamera()

        enemySpawner.update(
            delta = delta,
            camera = camera,
            enemyManager = enemyManager,
            currentStage =
                stageController.currentStage,
            stageType =
                stageController.currentStageType,
            enemyCountMultiplier =
                playerStats.enemyCountMultiplier()
        )
    }

    private fun handleEnemyKilled(
        killedEnemy: Enemy
    ) {
        runStatistics.recordEnemyKilled()

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
        weaponRuntimeManager.resetAllTimers()

        statCardController.beginSelection(
            shouldOpenMarketAfterSelection =
                completedStageType ==
                    StageType.MINI_BOSS
        )
    }

    private fun renderGame() {
        worldRenderer.render(
            mapObstacles = mapObstacles,
            player = player,
            enemyManager = enemyManager,
            goldCoinPickups =
                goldCoinPickups,
            projectiles = projectiles
        )

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
                        marketRerollsUsed
                )
            },
            renderStatCardSelection = {
                statCardController.render()
            }
        )
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
                completedStage = completedStage,
                completedStageType = completedStageType
            )

            enemyManager.clearAll()
            projectiles.clear()
            weaponRuntimeManager.resetAllTimers()

            stageController.completeCurrentStage()

            return true
        }

        completeCurrentStage()

        return true
    }

    private fun openMarket() {
        isMarketOpen = true
        marketRerollsUsed = 0

        val carriedLockedSlots =
            lockedSlotsForNextMarket
                .take(3)
                .map { slot ->
                    MarketSlot(
                        offer = slot.offer,
                        isLocked = true,
                        isSold = false
                    )
                }

        val missingSlotCount =
            3 - carriedLockedSlots.size

        val newSlots =
            MarketOfferGenerator.generateOffers(
                currentStage =
                    stageController.currentStage,
                offerCount = missingSlotCount,
                luckScore =
                    playerStats.luckScore()
            ).map { offer ->
                MarketSlot(
                    offer = offer
                )
            }

        marketSlots =
            carriedLockedSlots + newSlots

        lockedSlotsForNextMarket =
            emptyList()
    }

    private fun closeMarket() {
        lockedSlotsForNextMarket =
            marketSlots
                .filter { slot ->
                    slot.isLocked &&
                        !slot.isSold &&
                        slot.offer != null
                }
                .map { slot ->
                    MarketSlot(
                        offer = slot.offer,
                        isLocked = true,
                        isSold = false
                    )
                }

        isMarketOpen = false
        marketSlots = emptyList()
        marketRerollsUsed = 0

        enemySpawner.resetTimer()
    }

    private fun handleMarketControls() {
        when {
            Gdx.input.isKeyJustPressed(
                Input.Keys.NUM_1
            ) -> purchaseMarketOffer(0)

            Gdx.input.isKeyJustPressed(
                Input.Keys.NUM_2
            ) -> purchaseMarketOffer(1)

            Gdx.input.isKeyJustPressed(
                Input.Keys.NUM_3
            ) -> purchaseMarketOffer(2)

            Gdx.input.isKeyJustPressed(
                Input.Keys.Q
            ) -> toggleMarketOfferLock(0)

            Gdx.input.isKeyJustPressed(
                Input.Keys.W
            ) -> toggleMarketOfferLock(1)

            Gdx.input.isKeyJustPressed(
                Input.Keys.E
            ) -> toggleMarketOfferLock(2)

            Gdx.input.isKeyJustPressed(
                Input.Keys.R
            ) -> rerollMarket()

            Gdx.input.isKeyJustPressed(
                Input.Keys.ENTER
            ) -> closeMarket()
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
            marketSlots.getOrNull(slotIndex)
                ?: return

        if (slot.isSold) {
            return
        }

        val offer =
            slot.offer
                ?: return

        val purchaseSuccessful =
            sessionWallet.spendGold(
                amount = offer.price
            )

        if (!purchaseSuccessful) {
            return
        }

        applyMarketOffer(offer)

        marketSlots =
            marketSlots.toMutableList().apply {
                this[slotIndex] =
                    slot.copy(
                        isLocked = false,
                        isSold = true
                    )
            }
    }

    private fun applyMarketOffer(
        offer: MarketOffer
    ) {
        when (offer.productType) {
            MarketProductType.WEAPON -> {
                player.addRunStatBonus(
                    statType = StatType.ATTACK_SPEED,
                    amount = offer.productLevel
                )
            }

            MarketProductType.SKILL -> {
                player.addRunStatBonus(
                    statType = StatType.MAX_HP,
                    amount = offer.productLevel
                )
            }

            MarketProductType.ITEM -> {
                player.addRunStatBonus(
                    statType = StatType.MOVEMENT_SPEED,
                    amount = offer.productLevel
                )
            }
        }
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
                    amount = rerollPrice
                )

            if (!rerollSuccessful) {
                return
            }
        }

        marketRerollsUsed++

        marketSlots =
            marketSlots.map { slot ->
                if (slot.isLocked) {
                    slot
                } else {
                    MarketSlot(
                        offer =
                            MarketOfferGenerator
                                .generateOffers(
                                    currentStage =
                                        stageController.currentStage,
                                    offerCount = 1,
                                    luckScore =
                                        playerStats.luckScore()
                                )
                                .first()
                    )
                }
            }
    }

}

