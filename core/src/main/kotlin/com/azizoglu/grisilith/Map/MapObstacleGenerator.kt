package com.azizoglu.grisilith.map

import com.azizoglu.grisilith.config.GameMap
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle

object MapObstacleGenerator {

    private const val WORLD_EDGE_PADDING =
        80f

    private const val PLAYER_SAFE_RADIUS =
        120f

    private const val OBSTACLE_SPACING =
        45f

    private const val WALL_VISUAL_OVERLAP =
        12f

    private const val PATHWAY_WIDTH =
        280f

    private const val MAX_GENERATION_ATTEMPTS =
        500

    private data class GenerationRule(
        val type: MapObstacleType,
        val count: Int,
        val minimumWidth: Float,
        val maximumWidth: Float,
        val minimumHeight: Float,
        val maximumHeight: Float
    )

    fun generate(
        gameMap: GameMap,
        playerStartX: Float,
        playerStartY: Float
    ): List<MapObstacle> {
        return when (gameMap) {
            GameMap.ROAD_TO_HOME ->
                generateRandomLayout(
                    gameMap = gameMap,
                    playerStartX = playerStartX,
                    playerStartY = playerStartY,
                    initialObstacles = emptyList(),
                    rules = roadRules
                )

            GameMap.TROUBLE_AT_HOME ->
                generateRandomLayout(
                    gameMap = gameMap,
                    playerStartX = playerStartX,
                    playerStartY = playerStartY,
                    initialObstacles =
                        fixedVillageDefenses(
                            gameMap
                        ),
                    rules = villageRules
                )

            GameMap.FOLLOWING_THE_DUNGEON_DELVER ->
                generateRandomLayout(
                    gameMap = gameMap,
                    playerStartX = playerStartX,
                    playerStartY = playerStartY,
                    initialObstacles = emptyList(),
                    rules = dungeonRules
                )
        }
    }

    private fun generateRandomLayout(
        gameMap: GameMap,
        playerStartX: Float,
        playerStartY: Float,
        initialObstacles: List<MapObstacle>,
        rules: List<GenerationRule>
    ): List<MapObstacle> {
        val obstacles =
            initialObstacles
                .toMutableList()

        rules.forEach {
                rule ->

            if (
                rule.type == MapObstacleType.LAKE &&
                MathUtils.random() >= lakeGenerationChance(gameMap)
            ) {
                return@forEach
            }

            repeat(
                rule.count
            ) {
                tryAddRandomObstacle(
                    obstacles = obstacles,
                    type = rule.type,
                    width =
                        MathUtils.random(
                            rule.minimumWidth,
                            rule.maximumWidth
                        ),
                    height =
                        MathUtils.random(
                            rule.minimumHeight,
                            rule.maximumHeight
                        ),
                    worldWidth = gameMap.worldWidth,
                    worldHeight = gameMap.worldHeight,
                    playerStartX = playerStartX,
                    playerStartY = playerStartY
                )
            }
        }

        return obstacles
    }

    private fun fixedVillageDefenses(
        gameMap: GameMap
    ): List<MapObstacle> {
        val gateX = TroubleAtHomeLayout.gateX(gameMap.worldWidth)
        val wallY = TroubleAtHomeLayout.wallY(gameMap.worldHeight)

        return buildList {
            add(
                MapObstacle(
                    type = MapObstacleType.PATHWAY,
                    x =
                        gateX + TroubleAtHomeLayout.GATE_WIDTH / 2f -
                            PATHWAY_WIDTH / 2f,
                    y = 0f,
                    width = PATHWAY_WIDTH,
                    height = wallY,
                    visualVariant = 0
                )
            )

            var wallX = 0f

            while (wallX < gameMap.worldWidth) {
                val width =
                    TroubleAtHomeLayout.GATE_WIDTH
                        .coerceAtMost(gameMap.worldWidth - wallX)

                if (
                    wallX + width <= gateX ||
                    wallX >= gateX + TroubleAtHomeLayout.GATE_WIDTH
                ) {
                    val drawX =
                        (wallX - WALL_VISUAL_OVERLAP)
                            .coerceAtLeast(0f)
                    val drawRight =
                        (wallX + width + WALL_VISUAL_OVERLAP)
                            .coerceAtMost(gameMap.worldWidth)

                    add(
                        MapObstacle(
                            type = MapObstacleType.CITY_WALL,
                            x = drawX,
                            y = wallY,
                            width = drawRight - drawX,
                            height = TroubleAtHomeLayout.WALL_HEIGHT,
                            visualVariant = 0
                        )
                    )
                }

                wallX += TroubleAtHomeLayout.GATE_WIDTH
            }

            add(
                MapObstacle(
                type = MapObstacleType.CITY_GATE,
                x = gateX,
                y = TroubleAtHomeLayout.gateY(gameMap.worldHeight),
                width = TroubleAtHomeLayout.GATE_WIDTH,
                height = TroubleAtHomeLayout.GATE_HEIGHT,
                visualVariant = 0
                )
            )

            val gateCollisionWidth =
                TroubleAtHomeLayout.GATE_WIDTH *
                    TroubleAtHomeLayout.GATE_COLLISION_WIDTH_RATIO
            val sideBarrierWidth =
                (TroubleAtHomeLayout.GATE_WIDTH - gateCollisionWidth) / 2f

            add(
                MapObstacle(
                    type = MapObstacleType.CITY_WALL,
                    x = gateX,
                    y = wallY,
                    width = sideBarrierWidth,
                    height = TroubleAtHomeLayout.WALL_HEIGHT,
                    rendersVisual = false
                )
            )

            add(
                MapObstacle(
                    type = MapObstacleType.CITY_WALL,
                    x = gateX + TroubleAtHomeLayout.GATE_WIDTH - sideBarrierWidth,
                    y = wallY,
                    width = sideBarrierWidth,
                    height = TroubleAtHomeLayout.WALL_HEIGHT,
                    rendersVisual = false
                )
            )
        }
    }

    private fun tryAddRandomObstacle(
        obstacles: MutableList<MapObstacle>,
        type: MapObstacleType,
        width: Float,
        height: Float,
        worldWidth: Float,
        worldHeight: Float,
        playerStartX: Float,
        playerStartY: Float
    ) {
        if (
            type == MapObstacleType.LAKE &&
            obstacles.any { it.type == MapObstacleType.LAKE }
        ) {
            return
        }

        repeat(
            MAX_GENERATION_ATTEMPTS
        ) {
            val maximumX =
                worldWidth -
                    WORLD_EDGE_PADDING -
                    width

            val maximumY =
                worldHeight -
                    WORLD_EDGE_PADDING -
                    height

            if (
                maximumX <=
                WORLD_EDGE_PADDING ||
                maximumY <=
                WORLD_EDGE_PADDING
            ) {
                return
            }

            val x =
                MathUtils.random(
                    WORLD_EDGE_PADDING,
                    maximumX
                )

            val y =
                MathUtils.random(
                    WORLD_EDGE_PADDING,
                    maximumY
                )

            val candidateBounds =
                Rectangle(
                    x,
                    y,
                    width,
                    height
                )

            if (
                overlapsPlayerSafeArea(
                    bounds = candidateBounds,
                    playerStartX = playerStartX,
                    playerStartY = playerStartY
                ) ||
                overlapsExistingObstacle(
                    candidateBounds = candidateBounds,
                    obstacles = obstacles
                )
            ) {
                return@repeat
            }

            obstacles +=
                MapObstacle(
                    type = type,
                    x = x,
                    y = y,
                    width = width,
                    height = height,
                    visualVariant =
                        MathUtils.random(
                            0,
                            3
                        )
                )

            return
        }
    }

    private fun overlapsPlayerSafeArea(
        bounds: Rectangle,
        playerStartX: Float,
        playerStartY: Float
    ): Boolean {
        val safeArea =
            Rectangle(
                playerStartX -
                    PLAYER_SAFE_RADIUS,
                playerStartY -
                    PLAYER_SAFE_RADIUS,
                PLAYER_SAFE_RADIUS * 2f,
                PLAYER_SAFE_RADIUS * 2f
            )

        return safeArea.overlaps(
            bounds
        )
    }

    private fun overlapsExistingObstacle(
        candidateBounds: Rectangle,
        obstacles: List<MapObstacle>
    ): Boolean {
        val expandedCandidate =
            Rectangle(
                candidateBounds.x -
                    OBSTACLE_SPACING,
                candidateBounds.y -
                    OBSTACLE_SPACING,
                candidateBounds.width +
                    OBSTACLE_SPACING * 2f,
                candidateBounds.height +
                    OBSTACLE_SPACING * 2f
            )

        return obstacles.any {
                obstacle ->

            if (obstacle.type == MapObstacleType.PATHWAY) {
                return@any expandedCandidate.overlaps(obstacle.visualBounds)
            }

            expandedCandidate.overlaps(
                obstacle.visualBounds
            )
        }
    }

    private val roadRules =
        listOf(
            GenerationRule(
                MapObstacleType.LAKE,
                1,
                560f,
                720f,
                350f,
                470f
            ),
            GenerationRule(
                MapObstacleType.ROCK,
                6,
                70f,
                115f,
                60f,
                105f
            ),
            GenerationRule(
                MapObstacleType.TREE,
                5,
                65f,
                90f,
                90f,
                135f
            ),
            GenerationRule(
                MapObstacleType.BROKEN_WAGON,
                2,
                120f,
                165f,
                70f,
                105f
            )
        )

    private val villageRules =
        listOf(
            GenerationRule(
                MapObstacleType.LAKE,
                1,
                560f,
                720f,
                350f,
                470f
            ),
            GenerationRule(
                MapObstacleType.TREE,
                4,
                75f,
                105f,
                100f,
                145f
            ),
            GenerationRule(
                MapObstacleType.WELL,
                1,
                105f,
                135f,
                80f,
                110f
            ),
            GenerationRule(
                MapObstacleType.HAY_BALE,
                4,
                66f,
                96f,
                48f,
                68f
            ),
            GenerationRule(
                MapObstacleType.BROKEN_WAGON,
                2,
                125f,
                170f,
                75f,
                110f
            ),
            GenerationRule(
                MapObstacleType.CRATE,
                4,
                48f,
                66f,
                50f,
                70f
            ),
            GenerationRule(
                MapObstacleType.BARREL,
                3,
                44f,
                62f,
                50f,
                68f
            ),
            GenerationRule(
                MapObstacleType.LOG_PILE,
                3,
                105f,
                145f,
                70f,
                100f
            ),
            GenerationRule(
                MapObstacleType.BUSH,
                5,
                80f,
                120f,
                58f,
                82f
            )
        )

    private val dungeonRules =
        listOf(
            GenerationRule(
                MapObstacleType.CAVE_ROCK,
                20,
                75f,
                125f,
                65f,
                110f
            )
        )

    private fun lakeGenerationChance(
        gameMap: GameMap
    ): Float {
        return when (gameMap) {
            GameMap.ROAD_TO_HOME -> 0.40f
            GameMap.TROUBLE_AT_HOME -> 0.40f
            else -> 0f
        }
    }
}
