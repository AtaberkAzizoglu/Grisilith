package com.azizoglu.grisilith.enemy

import com.azizoglu.grisilith.map.MapObstacle
import com.azizoglu.grisilith.map.MapObstacleType
import com.azizoglu.grisilith.map.TroubleAtHomeLayout
import com.azizoglu.grisilith.config.GameMap
import com.azizoglu.grisilith.map.enemy.MapEnemyRuntimeContext
import com.azizoglu.grisilith.stage.StageType
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle

class EnemySpawner(
    private val worldWidth: Float,
    private val worldHeight: Float,
    private val mapEnemyContext:
    MapEnemyRuntimeContext
) {

    companion object {
        private const val MINIMUM_PLAYER_DISTANCE =
            200f

        private const val GATE_SPAWN_SAFE_DISTANCE =
            500f

        private const val WORLD_EDGE_PADDING =
            20f

        private const val MAXIMUM_POSITION_ATTEMPTS =
            40

        private const val REGULAR_SPAWN_DEADLINE_RATIO =
            0.85f

        private const val MINIMUM_SCHEDULE_INTERVAL =
            0.01f
    }

    private val spawnWarnings =
        mutableListOf<EnemySpawnWarning>()

    private var spawnTimer =
        0f

    private var bossScheduled =
        false

    private var bossEnteredWorld =
        false

    private var trackedStage =
        -1

    private var scheduledRegularEnemiesThisStage =
        0

    /*
     * true yalnızca 1 saniyelik uyarı tamamlanıp Boss gerçekten
     * EnemyManager içine eklendikten sonra döner.
     *
     * Böylece uyarı sırasında "Boss yaşamıyor" kontrolü Stage'i
     * yanlışlıkla anında tamamlamaz.
     */
    val hasSpawnedBoss: Boolean
        get() =
            bossEnteredWorld

    init {
        require(worldWidth > 0f)
        require(worldHeight > 0f)
    }

    fun update(
        delta: Float,
        enemyManager: EnemyManager,
        currentStage: Int,
        stageType: StageType,
        stageDurationSeconds: Float,
        enemyCountMultiplier: Float,
        playerCenterX: Float,
        playerCenterY: Float,
        mapObstacles: List<MapObstacle>
    ) {
        synchronizeStage(
            currentStage =
                currentStage
        )

        updateWarnings(
            delta =
                delta,

            enemyManager =
                enemyManager
        )

        scheduleBossIfRequired(
            currentStage =
                currentStage,

            stageType =
                stageType,

            playerCenterX =
                playerCenterX,

            playerCenterY =
                playerCenterY,

            mapObstacles =
                mapObstacles
        )

        updateRegularEnemyScheduling(
            delta =
                delta,

            currentStage =
                currentStage,

            stageType =
                stageType,

            stageDurationSeconds =
                stageDurationSeconds,

            enemyCountMultiplier =
                enemyCountMultiplier,

            playerCenterX =
                playerCenterX,

            playerCenterY =
                playerCenterY,

            mapObstacles =
                mapObstacles
        )
    }

    fun renderWarnings(
        shapeRenderer: ShapeRenderer
    ) {
        spawnWarnings.forEach { warning ->
            warning.render(
                shapeRenderer
            )
        }
    }

    fun resetTimer() {
        spawnTimer =
            0f

        bossScheduled =
            false

        bossEnteredWorld =
            false

        trackedStage =
            -1

        scheduledRegularEnemiesThisStage =
            0

        spawnWarnings.clear()
    }

    fun spawnBonusMiniBoss(
        enemyManager: EnemyManager,
        currentStage: Int,
        playerCenterX: Float,
        playerCenterY: Float,
        mapObstacles: List<MapObstacle>
    ): Enemy? {
        val definition = mapEnemyContext.selectionResolver.choose(
            profile = mapEnemyContext.profile,
            enemyType = EnemyType.MINI_BOSS
        ) ?: return null
        val stats = mapEnemyContext.runtimeFactory.createStats(
            definition = definition,
            enemyType = EnemyType.MINI_BOSS,
            stage = currentStage
        )
        val position = findValidSpawnPosition(
            enemySize = stats.size,
            canSpawnInsideObstacles = stats.canSpawnInsideObstacles,
            playerCenterX = playerCenterX,
            playerCenterY = playerCenterY,
            mapObstacles = mapObstacles
        ) ?: return null
        return mapEnemyContext.runtimeFactory.create(
            definition = definition,
            enemyType = EnemyType.MINI_BOSS,
            stage = currentStage,
            startX = position.first,
            startY = position.second
        ).also(enemyManager::spawn)
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

        bossScheduled =
            false

        bossEnteredWorld =
            false

        scheduledRegularEnemiesThisStage =
            0

        spawnWarnings.clear()
    }

    private fun updateWarnings(
        delta: Float,
        enemyManager: EnemyManager
    ) {
        val iterator =
            spawnWarnings.iterator()

        while (
            iterator.hasNext()
        ) {
            val warning =
                iterator.next()

            warning.update(
                delta =
                    delta
            )

            if (
                !warning.isReadyToSpawn
            ) {
                continue
            }

            enemyManager.spawn(
                warning.enemy
            )

            if (
                warning.enemy.type ==
                EnemyType.MINI_BOSS ||
                warning.enemy.type ==
                EnemyType.FINAL_BOSS
            ) {
                bossEnteredWorld =
                    true
            }

            iterator.remove()
        }
    }

    private fun scheduleBossIfRequired(
        currentStage: Int,
        stageType: StageType,
        playerCenterX: Float,
        playerCenterY: Float,
        mapObstacles: List<MapObstacle>
    ) {
        if (
            bossScheduled ||
            stageType ==
            StageType.NORMAL
        ) {
            return
        }

        val enemyType =
            when (
                stageType
            ) {
                StageType.MINI_BOSS ->
                    EnemyType.MINI_BOSS

                StageType.FINAL_BOSS ->
                    EnemyType.FINAL_BOSS

                StageType.NORMAL ->
                    return
            }

        val definition =
            mapEnemyContext
                .selectionResolver
                .choose(
                    profile =
                        mapEnemyContext.profile,

                    enemyType =
                        enemyType
                )
                ?: return

        val stats =
            mapEnemyContext
                .runtimeFactory
                .createStats(
                    definition =
                        definition,

                    enemyType =
                        enemyType,

                    stage =
                        currentStage
                )

        val spawnPosition =
            findValidSpawnPosition(
                enemySize =
                    stats.size,

                canSpawnInsideObstacles =
                    stats.canSpawnInsideObstacles,

                playerCenterX =
                    playerCenterX,

                playerCenterY =
                    playerCenterY,

                mapObstacles =
                    mapObstacles
            )
                ?: return

        val enemy =
            mapEnemyContext
                .runtimeFactory
                .create(
                    definition =
                        definition,

                    enemyType =
                        enemyType,

                    stage =
                        currentStage,

                    startX =
                        spawnPosition.first,

                    startY =
                        spawnPosition.second
                )

        spawnWarnings.add(
            EnemySpawnWarning(
                bounds =
                    Rectangle(
                        spawnPosition.first,
                        spawnPosition.second,
                        stats.size,
                        stats.size
                    ),

                enemy =
                    enemy
            )
        )

        bossScheduled =
            true
    }

    private fun updateRegularEnemyScheduling(
        delta: Float,
        currentStage: Int,
        stageType: StageType,
        stageDurationSeconds: Float,
        enemyCountMultiplier: Float,
        playerCenterX: Float,
        playerCenterY: Float,
        mapObstacles: List<MapObstacle>
    ) {
        val baseSettings =
            EnemySpawnScaling
                .settingsForStage(
                    stage =
                        currentStage
                )

        val regularEnemyQuota =
            EnemySpawnScaling
                .regularEnemyQuotaForStage(
                    stage =
                        currentStage,

                    isBossStage =
                        stageType !=
                            StageType.NORMAL,

                    enemyCountMultiplier =
                        enemyCountMultiplier
                )

        if (
            scheduledRegularEnemiesThisStage >=
            regularEnemyQuota
        ) {
            return
        }

        val spawnDeadline =
            (
                stageDurationSeconds *
                    REGULAR_SPAWN_DEADLINE_RATIO
                )
                .coerceAtLeast(
                    EnemySpawnWarning
                        .WARNING_DURATION_SECONDS
                )

        val schedulingWindow =
            (
                spawnDeadline -
                    EnemySpawnWarning
                        .WARNING_DURATION_SECONDS
                )
                .coerceAtLeast(
                    MINIMUM_SCHEDULE_INTERVAL
                )

        val scheduleInterval =
            (
                schedulingWindow /
                    regularEnemyQuota
                        .toFloat()
                )
                .coerceAtLeast(
                    MINIMUM_SCHEDULE_INTERVAL
                )

        spawnTimer +=
            delta.coerceAtLeast(
                0f
            )

        while (
            spawnTimer >=
            scheduleInterval &&
            scheduledRegularEnemiesThisStage <
            regularEnemyQuota
        ) {
            val scheduled =
                scheduleRegularEnemy(
                    currentStage =
                        currentStage,

                    uniqueSpawnChance =
                        (
                            baseSettings
                                .uniqueSpawnChance *
                                mapEnemyContext
                                    .profile
                                    .uniqueSpawnChanceMultiplier
                            )
                            .coerceIn(
                                0f,
                                1f
                            ),

                    playerCenterX =
                        playerCenterX,

                    playerCenterY =
                        playerCenterY,

                    mapObstacles =
                        mapObstacles
                )

            if (
                !scheduled
            ) {
                /*
                 * Geçerli konum bulunamadığında zorla spawn edilmez.
                 * Timer korunur ve sonraki update'te yeniden denenir.
                 */
                break
            }

            spawnTimer -=
                scheduleInterval

            scheduledRegularEnemiesThisStage++
        }
    }

    private fun scheduleRegularEnemy(
        currentStage: Int,
        uniqueSpawnChance: Float,
        playerCenterX: Float,
        playerCenterY: Float,
        mapObstacles: List<MapObstacle>
    ): Boolean {

        val definition =
            mapEnemyContext
                .selectionResolver
                .choose(
                    profile =
                        mapEnemyContext.profile,

                    enemyType =
                        EnemyType.NORMAL
                )
                ?: return false

        val enemyType =
            if (
                MathUtils.random() <
                uniqueSpawnChance
            ) {
                EnemyType.UNIQUE
            } else {
                EnemyType.NORMAL
            }

        val stats =
            mapEnemyContext
                .runtimeFactory
                .createStats(
                    definition =
                        definition,

                    enemyType =
                        enemyType,

                    stage =
                        currentStage
                )

        val spawnPosition =
            findValidSpawnPosition(
                enemySize =
                    stats.size,

                canSpawnInsideObstacles =
                    stats.canSpawnInsideObstacles,

                playerCenterX =
                    playerCenterX,

                playerCenterY =
                    playerCenterY,

                mapObstacles =
                    mapObstacles
            )
                ?: return false

        val enemy =
            mapEnemyContext
                .runtimeFactory
                .create(
                    definition =
                        definition,

                    enemyType =
                        enemyType,

                    stage =
                        currentStage,

                    startX =
                        spawnPosition.first,

                    startY =
                        spawnPosition.second
                )

        spawnWarnings.add(
            EnemySpawnWarning(
                bounds =
                    Rectangle(
                        spawnPosition.first,
                        spawnPosition.second,
                        stats.size,
                        stats.size
                    ),

                enemy =
                    enemy
            )
        )

        return true
    }

    private fun findValidSpawnPosition(
        enemySize: Float,
        canSpawnInsideObstacles: Boolean,
        playerCenterX: Float,
        playerCenterY: Float,
        mapObstacles: List<MapObstacle>
    ): Pair<Float, Float>? {

        val minimumX =
            WORLD_EDGE_PADDING

        val minimumY =
            WORLD_EDGE_PADDING

        val maximumX =
            (
                worldWidth -
                    WORLD_EDGE_PADDING -
                    enemySize
                )
                .coerceAtLeast(
                    minimumX
                )

        val maximumY =
            (
                if (
                    mapEnemyContext.gameMap ==
                    GameMap.TROUBLE_AT_HOME
                ) {
                    TroubleAtHomeLayout.enemyMaximumSpawnY(
                        worldHeight = worldHeight,
                        enemySize = enemySize
                    )
                } else {
                    worldHeight -
                        WORLD_EDGE_PADDING -
                        enemySize
                }
                )
                .coerceAtLeast(
                    minimumY
                )

        val gateVisualBounds =
            if (mapEnemyContext.gameMap == GameMap.TROUBLE_AT_HOME) {
                mapObstacles
                    .firstOrNull { it.type == MapObstacleType.CITY_GATE }
                    ?.visualBounds
            } else {
                null
            }

        repeat(
            MAXIMUM_POSITION_ATTEMPTS
        ) {
            val spawnX =
                MathUtils.random(
                    minimumX,
                    maximumX
                )

            val spawnY =
                MathUtils.random(
                    minimumY,
                    maximumY
                )

            val centerX =
                spawnX +
                    enemySize /
                    2f

            val centerY =
                spawnY +
                    enemySize /
                    2f

            val distanceX =
                centerX -
                    playerCenterX

            val distanceY =
                centerY -
                    playerCenterY

            val distanceSquared =
                distanceX *
                    distanceX +
                    distanceY *
                    distanceY

            if (
                distanceSquared <
                MINIMUM_PLAYER_DISTANCE *
                MINIMUM_PLAYER_DISTANCE
            ) {
                return@repeat
            }

            val candidateBounds =
                Rectangle(
                    spawnX,
                    spawnY,
                    enemySize,
                    enemySize
                )

            if (
                gateVisualBounds != null &&
                distanceSquaredToRectangle(
                    centerX,
                    centerY,
                    gateVisualBounds
                ) < GATE_SPAWN_SAFE_DISTANCE * GATE_SPAWN_SAFE_DISTANCE
            ) {
                return@repeat
            }

            if (
                !canSpawnInsideObstacles &&
                mapObstacles.any { obstacle ->
                    obstacle.blocksMovement &&
                        candidateBounds.overlaps(
                        obstacle.bounds
                    )
                }
            ) {
                return@repeat
            }

            return Pair(
                spawnX,
                spawnY
            )
        }

        return null
    }

    private fun distanceSquaredToRectangle(
        x: Float,
        y: Float,
        rectangle: Rectangle
    ): Float {
        val closestX = x.coerceIn(rectangle.x, rectangle.x + rectangle.width)
        val closestY = y.coerceIn(rectangle.y, rectangle.y + rectangle.height)
        val dx = x - closestX
        val dy = y - closestY
        return dx * dx + dy * dy
    }
}
