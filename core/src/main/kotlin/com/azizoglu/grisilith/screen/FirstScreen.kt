package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyManager
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.weapon.AutoWeapon
import com.azizoglu.grisilith.weapon.Projectile
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.FitViewport
import com.azizoglu.grisilith.enemy.EnemySpawner
import com.azizoglu.grisilith.statistics.RunStatistics
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import kotlin.collections.minusAssign
import kotlin.div
import com.azizoglu.grisilith.util.GameTimeController
import com.badlogic.gdx.Input
import com.azizoglu.grisilith.stage.StageController
import com.azizoglu.grisilith.enemy.EnemyScaling
import com.azizoglu.grisilith.enemy.EnemySpawnScaling
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.stage.StageType
import com.azizoglu.grisilith.economy.GoldCoinPickup
import com.azizoglu.grisilith.economy.SessionWallet
import com.azizoglu.grisilith.statistics.StatCard
import com.azizoglu.grisilith.statistics.StatCardGenerator
import com.azizoglu.grisilith.statistics.StatUpgradeType
import kotlin.div
import kotlin.times

class FirstScreen : ScreenAdapter() {

    companion object {
        private const val CAMERA_WIDTH = 1280f
        private const val CAMERA_HEIGHT = 720f

        private const val WORLD_WIDTH = 2200f
        private const val WORLD_HEIGHT = 1400f
    }

    private val shapeRenderer = ShapeRenderer()
    private val camera = OrthographicCamera()

    private val viewport = FitViewport(
        CAMERA_WIDTH,
        CAMERA_HEIGHT,
        camera
    )

    private val player = Player(
        startX = WORLD_WIDTH / 2f,
        startY = WORLD_HEIGHT / 2f,
        worldWidth = WORLD_WIDTH,
        worldHeight = WORLD_HEIGHT
    )

    private val projectiles = mutableListOf<Projectile>()

    private val runStatistics = RunStatistics()

    private val gameTimeController = GameTimeController()

    private val hudBatch = SpriteBatch()
    private val hudFont = BitmapFont()

    private val enemyManager = EnemyManager().apply {
        spawn(
            Enemy(
                startX = WORLD_WIDTH / 2f + 300f,
                startY = WORLD_HEIGHT / 2f,
                stats = EnemyScaling.normalEnemyStats(
                    stage = 1
                )
            )
        )
    }

    private val enemySpawner = EnemySpawner(
        worldWidth = WORLD_WIDTH,
        worldHeight = WORLD_HEIGHT
    )

    private val autoWeapon = AutoWeapon(
        attacksPerSecond = 2f
    )

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

    private var isChoosingStatCard = false

    private var pendingStatCards = emptyList<StatCard>()

    override fun show() {
        viewport.apply()

        camera.position.set(
            player.centerX,
            player.centerY,
            0f
        )

        camera.update()
    }

    override fun render(delta: Float) {
        handleGameControls()

        val gameplayStopped =
            player.health.isDead ||
                stageController.isRunComplete ||
                isChoosingStatCard

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

        if (isChoosingStatCard) {
            handleStatCardSelection()
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

        enemyManager.resolvePlayerCollisions { enemyBounds ->
            player.resolveCollision(enemyBounds)
        }

        enemyManager.update(
            delta = delta,
            targetX = player.centerX,
            targetY = player.centerY,
            targetBounds = player.bounds,
            worldWidth = WORLD_WIDTH,
            worldHeight = WORLD_HEIGHT
        ) { damage ->
            player.takeDamage(damage)
        }

        if (!player.health.isDead) {
            autoWeapon.update(
                delta = delta,
                player = player,
                enemyManager = enemyManager,
                projectiles = projectiles
            )
        }

        projectiles.forEach { projectile ->
            projectile.update(
                delta = delta,
                worldWidth = WORLD_WIDTH,
                worldHeight = WORLD_HEIGHT
            )

            projectile.checkCollision(
                enemies = enemyManager.allEnemies,
                onDamageDealt =
                    runStatistics::recordDamageDealt
            ) { killedEnemy ->
                runStatistics.recordEnemyKilled()

                goldCoinPickups.add(
                    GoldCoinPickup(
                        startX = killedEnemy.centerX,
                        startY = killedEnemy.centerY,
                        value = killedEnemy.goldReward
                    )
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
                stageController.currentStageType
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
        enemyManager.clearAll()
        projectiles.clear()
        goldCoinPickups.clear()

        pendingStatCards =
            StatCardGenerator.generateCards()

        isChoosingStatCard = true
    }

    private fun renderGame() {
        Gdx.gl.glClearColor(
            0.05f,
            0.05f,
            0.08f,
            1f
        )

        Gdx.gl.glClear(
            GL20.GL_COLOR_BUFFER_BIT
        )

        shapeRenderer.projectionMatrix =
            camera.combined

        renderGrid()
        renderWorldBorder()

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        player.render(shapeRenderer)
        enemyManager.render(shapeRenderer)

        goldCoinPickups.forEach { pickup ->
            pickup.render(shapeRenderer)
        }

        projectiles.forEach { projectile ->
            projectile.render(shapeRenderer)
        }

        shapeRenderer.end()
        renderHud()
    }

    private fun updateCamera() {
        val halfCameraWidth =
            CAMERA_WIDTH / 2f

        val halfCameraHeight =
            CAMERA_HEIGHT / 2f

        val minimumCameraX =
            halfCameraWidth

        val maximumCameraX =
            WORLD_WIDTH - halfCameraWidth

        val minimumCameraY =
            halfCameraHeight

        val maximumCameraY =
            WORLD_HEIGHT - halfCameraHeight

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

    private fun renderGrid() {
        val gridSize = 100f

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        )

        var x = 0f

        while (x <= WORLD_WIDTH) {
            shapeRenderer.line(
                x,
                0f,
                x,
                WORLD_HEIGHT
            )

            x += gridSize
        }

        var y = 0f

        while (y <= WORLD_HEIGHT) {
            shapeRenderer.line(
                0f,
                y,
                WORLD_WIDTH,
                y
            )

            y += gridSize
        }

        shapeRenderer.end()
    }

    private fun renderWorldBorder() {
        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        )

        shapeRenderer.rect(
            0f,
            0f,
            WORLD_WIDTH,
            WORLD_HEIGHT
        )

        shapeRenderer.end()
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

    private fun renderHud() {
        hudBatch.projectionMatrix =
            viewport.camera.combined

        hudBatch.begin()

        hudFont.color = Color.WHITE

        val leftX =
            camera.position.x - CAMERA_WIDTH / 2f + 20f

        var topY =
            camera.position.y + CAMERA_HEIGHT / 2f - 20f

        hudFont.draw(
            hudBatch,
            "Stage: ${displayedStage()} / " +
                "${stageController.maximumStage}",
            leftX,
            topY
        )

        topY -= 28f

        hudFont.draw(
            hudBatch,
            "Stage Type: ${stageController.currentStageType}",
            leftX,
            topY
        )

        topY -= 28f

        hudFont.draw(
            hudBatch,
            "Stage Time: ${remainingStageSeconds()}",
            leftX,
            topY
        )

        topY -= 28f

        hudFont.draw(
            hudBatch,
            "HP: ${player.health.currentHealth}" +
                " / ${player.health.maximumHealth}",
            leftX,
            topY
        )

        topY -= 28f

        hudFont.draw(
            hudBatch,
            "Gold Coin: ${sessionWallet.goldCoins}",
            leftX,
            topY
        )

        topY -= 28f

        val isBossStage =
            stageController.currentStageType !=
                StageType.NORMAL

        val currentEnemyLimit =
            EnemySpawnScaling.maximumEnemyCountForStage(
                stage = stageController.currentStage,
                isBossStage = isBossStage
            )

        hudFont.draw(
            hudBatch,
            "Active Enemies: " +
                "${enemyManager.enemyCount} / $currentEnemyLimit",
            leftX,
            topY
        )

        val spawnSettings =
            EnemySpawnScaling.settingsForStage(
                stage = stageController.currentStage
            )

        topY -= 28f

        hudFont.draw(
            hudBatch,
            "Spawn Interval: " +
                String.format(
                    java.util.Locale.US,
                    "%.2f",
                    spawnSettings.spawnInterval
                ) +
                "s",
            leftX,
            topY
        )

        topY -= 28f

        val uniqueChancePercentage =
            spawnSettings.uniqueSpawnChance * 100f

        hudFont.draw(
            hudBatch,
            "Unique Chance: " +
                String.format(
                    java.util.Locale.US,
                    "%.1f%%",
                    uniqueChancePercentage
                ),
            leftX,
            topY
        )

        topY -= 28f

        hudFont.draw(
            hudBatch,
            "Kills: ${runStatistics.totalEnemiesKilled}",
            leftX,
            topY
        )

        topY -= 28f

        hudFont.draw(
            hudBatch,
            "Damage: ${runStatistics.totalDamageDealt}",
            leftX,
            topY
        )

        topY -= 28f

        hudFont.draw(
            hudBatch,
            "FPS: ${Gdx.graphics.framesPerSecond}",
            leftX,
            topY
        )

        topY -= 28f

        hudFont.draw(
            hudBatch,
            "Game Time: ${formatGameTime()}",
            leftX,
            topY
        )

        topY -= 28f

        hudFont.draw(
            hudBatch,
            "Game Speed: ${gameTimeController.gameSpeed}x",
            leftX,
            topY
        )

        if (gameTimeController.isPaused) {
            hudFont.draw(
                hudBatch,
                "PAUSED",
                camera.position.x - 35f,
                camera.position.y
            )
        }

        if (stageController.isRunComplete) {
            hudFont.draw(
                hudBatch,
                "RUN COMPLETE",
                camera.position.x - 55f,
                camera.position.y + 40f
            )

            when {
                player.health.isDead -> {
                    hudFont.draw(
                        hudBatch,
                        "DEFEATED",
                        camera.position.x - 45f,
                        camera.position.y + 40f
                    )
                }

                stageController.isRunComplete -> {
                    hudFont.draw(
                        hudBatch,
                        "RUN COMPLETE",
                        camera.position.x - 55f,
                        camera.position.y + 40f
                    )
                }

                gameTimeController.isPaused -> {
                    hudFont.draw(
                        hudBatch,
                        "PAUSED",
                        camera.position.x - 35f,
                        camera.position.y
                    )
                }
            }

        }

        if (isChoosingStatCard) {
            renderStatCardSelection()
        }

        hudBatch.end()
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

        /*
         * Boss henüz spawn olmadıysa aşamayı
         * yanlışlıkla tamamlamıyoruz.
         */
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
            enemyManager.clearAll()
            projectiles.clear()
            goldCoinPickups.clear()

            stageController.completeCurrentStage()
            return true
        }

        completeCurrentStage()
        return true
    }

    private fun handleStatCardSelection() {
        when {
            Gdx.input.isKeyJustPressed(
                Input.Keys.NUM_1
            ) -> selectStatCard(0)

            Gdx.input.isKeyJustPressed(
                Input.Keys.NUM_2
            ) -> selectStatCard(1)

            Gdx.input.isKeyJustPressed(
                Input.Keys.NUM_3
            ) -> selectStatCard(2)
        }
    }

    private fun selectStatCard(
        cardIndex: Int
    ) {
        val selectedCard =
            pendingStatCards.getOrNull(cardIndex)
                ?: return

        applyStatCard(selectedCard)

        pendingStatCards = emptyList()
        isChoosingStatCard = false

        stageController.completeCurrentStage()
        enemySpawner.resetTimer()
    }

    private fun applyStatCard(
        card: StatCard
    ) {
        when (card.type) {
            StatUpgradeType.MAX_HEALTH -> {
                player.increaseMaximumHealth(
                    amount = 15
                )
            }

            StatUpgradeType.MOVEMENT_SPEED -> {
                player.increaseMovementSpeed(
                    percentageIncrease = 0.08f
                )
            }

            StatUpgradeType.ATTACK_SPEED -> {
                autoWeapon.increaseAttackSpeed(
                    percentageIncrease = 0.10f
                )
            }
        }
    }

    private fun renderStatCardSelection() {
        val startX =
            camera.position.x -
                CAMERA_WIDTH / 2f +
                220f

        val titleY =
            camera.position.y +
                180f

        hudFont.draw(
            hudBatch,
            "CHOOSE A STAT CARD",
            startX,
            titleY
        )

        pendingStatCards.forEachIndexed {
                index,
                card ->

            val cardY =
                titleY -
                    70f -
                    index * 100f

            hudFont.draw(
                hudBatch,
                "${index + 1} - ${card.title}",
                startX,
                cardY
            )

            hudFont.draw(
                hudBatch,
                card.description,
                startX + 25f,
                cardY - 30f
            )
        }
    }

}

