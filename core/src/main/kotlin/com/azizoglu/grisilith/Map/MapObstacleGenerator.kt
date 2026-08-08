package com.azizoglu.grisilith.map

import com.azizoglu.grisilith.config.GameMap
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle

object MapObstacleGenerator {

    private const val WORLD_EDGE_PADDING = 80f

    /*
     * Player size şu an 48f.
     * 48 × 2.5 = 120f güvenli yarıçap.
     */
    private const val PLAYER_SAFE_RADIUS = 120f

    private const val OBSTACLE_SPACING = 45f

    private const val MAX_GENERATION_ATTEMPTS = 500

    fun generate(
        gameMap: GameMap,
        playerStartX: Float,
        playerStartY: Float
    ): List<MapObstacle> {
        return when (gameMap) {
            GameMap.ROAD_TO_HOME ->
                generateRoadToHome(
                    worldWidth = gameMap.worldWidth,
                    worldHeight = gameMap.worldHeight,
                    playerStartX = playerStartX,
                    playerStartY = playerStartY
                )

            GameMap.TROUBLE_AT_HOME ->
                emptyList()

            GameMap.FOLLOWING_THE_DUNGEON_DELVER ->
                emptyList()
        }
    }

    private fun generateRoadToHome(
        worldWidth: Float,
        worldHeight: Float,
        playerStartX: Float,
        playerStartY: Float
    ): List<MapObstacle> {
        val obstacles =
            mutableListOf<MapObstacle>()

        repeat(6) {
            tryAddRandomObstacle(
                obstacles = obstacles,
                type = MapObstacleType.ROCK,
                width = MathUtils.random(
                    70f,
                    115f
                ),
                height = MathUtils.random(
                    60f,
                    105f
                ),
                worldWidth = worldWidth,
                worldHeight = worldHeight,
                playerStartX = playerStartX,
                playerStartY = playerStartY
            )
        }

        repeat(5) {
            tryAddRandomObstacle(
                obstacles = obstacles,
                type = MapObstacleType.TREE,
                width = MathUtils.random(
                    65f,
                    90f
                ),
                height = MathUtils.random(
                    90f,
                    135f
                ),
                worldWidth = worldWidth,
                worldHeight = worldHeight,
                playerStartX = playerStartX,
                playerStartY = playerStartY
            )
        }

        repeat(2) {
            tryAddRandomObstacle(
                obstacles = obstacles,
                type = MapObstacleType.BROKEN_WAGON,
                width = MathUtils.random(
                    120f,
                    165f
                ),
                height = MathUtils.random(
                    70f,
                    105f
                ),
                worldWidth = worldWidth,
                worldHeight = worldHeight,
                playerStartX = playerStartX,
                playerStartY = playerStartY
            )
        }

        return obstacles
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
        repeat(MAX_GENERATION_ATTEMPTS) {
            val maximumX =
                worldWidth -
                    WORLD_EDGE_PADDING -
                    width

            val maximumY =
                worldHeight -
                    WORLD_EDGE_PADDING -
                    height

            if (
                maximumX <= WORLD_EDGE_PADDING ||
                maximumY <= WORLD_EDGE_PADDING
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
                )
            ) {
                return@repeat
            }

            if (
                overlapsExistingObstacle(
                    candidateBounds = candidateBounds,
                    obstacles = obstacles
                )
            ) {
                return@repeat
            }

            obstacles.add(
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

        return safeArea.overlaps(bounds)
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

        return obstacles.any { obstacle ->
            expandedCandidate.overlaps(
                obstacle.visualBounds
            )
        }
    }
}
