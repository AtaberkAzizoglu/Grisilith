package com.azizoglu.grisilith.enemy

import com.azizoglu.grisilith.stage.StageType
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.MathUtils

class EnemySpawner(
    private val worldWidth: Float,
    private val worldHeight: Float
) {

    companion object {
        private const val MINIMUM_DYNAMIC_SPAWN_INTERVAL =
            0.01f
    }
    private var spawnTimer = 0f
    private var bossSpawned = false

    private var trackedStage =
        -1

    private var spawnedRegularEnemiesThisStage =
        0

    val hasSpawnedBoss: Boolean
        get() = bossSpawned

    init {
        require(worldWidth > 0f)
        require(worldHeight > 0f)
    }

    fun update(
        delta: Float,
        camera: OrthographicCamera,
        enemyManager: EnemyManager,
        currentStage: Int,
        stageType: StageType,
        stageDurationSeconds: Float,
        enemyCountMultiplier: Float
    ) {
        synchronizeStage(
            currentStage =
                currentStage
        )

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
            stageType = stageType,
            stageDurationSeconds =
                stageDurationSeconds,
            enemyCountMultiplier =
                enemyCountMultiplier
        )
    }

    fun resetTimer() {
        spawnTimer = 0f
        bossSpawned = false
        trackedStage = -1
        spawnedRegularEnemiesThisStage = 0
    }

    private fun synchronizeStage(
        currentStage: Int
    ) {
        if (
            trackedStage ==
            currentStage
        ) {
            return
        }

        trackedStage =
            currentStage

        spawnTimer =
            0f

        bossSpawned =
            false

        spawnedRegularEnemiesThisStage =
            0
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
        stageType: StageType,
        stageDurationSeconds: Float,
        enemyCountMultiplier: Float
    ) {
        val baseSettings =
            EnemySpawnScaling.settingsForStage(
                stage = currentStage
            )

        val isBossStage =
            stageType != StageType.NORMAL

        val regularEnemyQuota =
            EnemySpawnScaling
                .regularEnemyQuotaForStage(
                    stage =
                        currentStage,

                    isBossStage =
                        isBossStage,

                    enemyCountMultiplier =
                        enemyCountMultiplier
                )

        if (
            spawnedRegularEnemiesThisStage >=
            regularEnemyQuota
        ) {
            return
        }

        /*
         * Stage süresi değişmez.
         * Kota büyüdükçe spawn aralığı otomatik küçülür;
         * böylece normal ve unique düşmanların tamamı
         * aynı Stage süresi içinde gönderilir.
         */
        val spawnInterval =
            (
                stageDurationSeconds
                    .coerceAtLeast(
                        0.01f
                    ) /
                    regularEnemyQuota
                        .toFloat()
                )
                .coerceAtLeast(
                    MINIMUM_DYNAMIC_SPAWN_INTERVAL
                )

        spawnTimer +=
            delta.coerceAtLeast(
                0f
            )

        while (
            spawnTimer >=
            spawnInterval &&
            spawnedRegularEnemiesThisStage <
            regularEnemyQuota
        ) {
            spawnTimer -=
                spawnInterval

            val enemyStats =
                chooseRegularEnemyStats(
                    currentStage =
                        currentStage,

                    uniqueSpawnChance =
                        baseSettings
                            .uniqueSpawnChance
                )

            enemyManager.spawn(
                createEnemyOutsideCamera(
                    camera =
                        camera,

                    stats =
                        enemyStats
                )
            )

            spawnedRegularEnemiesThisStage++
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
