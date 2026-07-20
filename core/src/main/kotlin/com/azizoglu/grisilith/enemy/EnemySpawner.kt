package com.azizoglu.grisilith.enemy

import com.azizoglu.grisilith.stage.StageType
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.MathUtils
import kotlin.math.roundToInt

class EnemySpawner(
    private val worldWidth: Float,
    private val worldHeight: Float
) {
    private var spawnTimer = 0f
    private var bossSpawned = false

    init {
        require(worldWidth > 0f)
        require(worldHeight > 0f)
    }

    fun update(
        delta: Float,
        camera: OrthographicCamera,
        enemyManager: EnemyManager,
        currentStage: Int,
        stageType: StageType
    ) {
        spawnBossIfRequired(
            camera = camera,
            enemyManager = enemyManager,
            currentStage = currentStage,
            stageType = stageType
        )

        updateRegularEnemySpawning(
            delta = delta,
            camera = camera,
            enemyManager = enemyManager,
            currentStage = currentStage,
            stageType = stageType
        )
    }

    fun resetTimer() {
        spawnTimer = 0f
        bossSpawned = false
    }

    private fun spawnBossIfRequired(
        camera: OrthographicCamera,
        enemyManager: EnemyManager,
        currentStage: Int,
        stageType: StageType
    ) {
        if (
            bossSpawned ||
            stageType == StageType.NORMAL
        ) {
            return
        }

        val bossStats = when (stageType) {
            StageType.MINI_BOSS ->
                EnemyScaling.miniBossStats(
                    stage = currentStage
                )

            StageType.FINAL_BOSS ->
                EnemyScaling.finalBossStats(
                    stage = currentStage
                )

            StageType.NORMAL ->
                return
        }

        enemyManager.spawn(
            createEnemyOutsideCamera(
                camera = camera,
                stats = bossStats
            )
        )

        bossSpawned = true
    }

    private fun updateRegularEnemySpawning(
        delta: Float,
        camera: OrthographicCamera,
        enemyManager: EnemyManager,
        currentStage: Int,
        stageType: StageType
    ) {
        val baseSettings =
            EnemySpawnScaling.settingsForStage(
                stage = currentStage
            )

        val isBossStage =
            stageType != StageType.NORMAL

        /*
         * %50 spawn hızı:
         * Spawn aralığını iki katına çıkarıyoruz.
         *
         * %50 düşman sınırı:
         * Maksimum sayıyı yarıya indiriyoruz.
         */
        val spawnInterval =
            if (isBossStage) {
                baseSettings.spawnInterval * 2f
            } else {
                baseSettings.spawnInterval
            }

        val maximumEnemyCount =
            if (isBossStage) {
                (
                    baseSettings.maximumEnemyCount *
                        0.5f
                    )
                    .roundToInt()
                    .coerceAtLeast(1)
            } else {
                baseSettings.maximumEnemyCount
            }

        if (
            enemyManager.enemyCount >=
            maximumEnemyCount
        ) {
            return
        }

        spawnTimer += delta

        while (
            spawnTimer >= spawnInterval &&
            enemyManager.enemyCount <
            maximumEnemyCount
        ) {
            spawnTimer -= spawnInterval

            val enemyStats =
                chooseRegularEnemyStats(
                    currentStage = currentStage,
                    uniqueSpawnChance =
                        baseSettings.uniqueSpawnChance
                )

            enemyManager.spawn(
                createEnemyOutsideCamera(
                    camera = camera,
                    stats = enemyStats
                )
            )
        }
    }

    private fun chooseRegularEnemyStats(
        currentStage: Int,
        uniqueSpawnChance: Float
    ): EnemyStats {
        val isUnique =
            MathUtils.random() <
                uniqueSpawnChance

        return if (isUnique) {
            EnemyScaling.uniqueEnemyStats(
                stage = currentStage
            )
        } else {
            EnemyScaling.normalEnemyStats(
                stage = currentStage
            )
        }
    }

    private fun createEnemyOutsideCamera(
        camera: OrthographicCamera,
        stats: EnemyStats
    ): Enemy {
        val enemySize = stats.size
        val outsidePadding = 80f

        val halfCameraWidth =
            camera.viewportWidth *
                camera.zoom / 2f

        val halfCameraHeight =
            camera.viewportHeight *
                camera.zoom / 2f

        val visibleLeft =
            camera.position.x -
                halfCameraWidth

        val visibleRight =
            camera.position.x +
                halfCameraWidth

        val visibleBottom =
            camera.position.y -
                halfCameraHeight

        val visibleTop =
            camera.position.y +
                halfCameraHeight

        val spawnSide =
            MathUtils.random(0, 3)

        val spawnPosition =
            when (spawnSide) {
                0 -> Pair(
                    visibleLeft -
                        outsidePadding -
                        enemySize,

                    MathUtils.random(
                        visibleBottom,
                        visibleTop
                    )
                )

                1 -> Pair(
                    visibleRight +
                        outsidePadding,

                    MathUtils.random(
                        visibleBottom,
                        visibleTop
                    )
                )

                2 -> Pair(
                    MathUtils.random(
                        visibleLeft,
                        visibleRight
                    ),

                    visibleBottom -
                        outsidePadding -
                        enemySize
                )

                else -> Pair(
                    MathUtils.random(
                        visibleLeft,
                        visibleRight
                    ),

                    visibleTop +
                        outsidePadding
                )
            }

        val maximumX =
            (worldWidth - enemySize)
                .coerceAtLeast(0f)

        val maximumY =
            (worldHeight - enemySize)
                .coerceAtLeast(0f)

        val spawnX =
            spawnPosition.first.coerceIn(
                0f,
                maximumX
            )

        val spawnY =
            spawnPosition.second.coerceIn(
                0f,
                maximumY
            )

        return Enemy(
            startX = spawnX,
            startY = spawnY,
            stats = stats
        )
    }
}
