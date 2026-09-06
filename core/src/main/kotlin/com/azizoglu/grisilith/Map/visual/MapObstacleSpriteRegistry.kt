package com.azizoglu.grisilith.map.visual

import com.azizoglu.grisilith.config.GameMap
import com.azizoglu.grisilith.map.MapObstacleType

object MapObstacleSpriteRegistry {

    private const val LEGACY_ROAD_TEXTURE =
        "map/obstacles/road_to_home_obstacles_source.png"

    private const val SHARED_TEXTURE =
        "map/obstacles/shared_obstacles_2.png"

    private const val TREE_TEXTURE =
        "map/obstacles/tree_obstacles.png"

    private const val HOUSE_TEXTURE =
        "map/obstacles/trouble_at_home_house.png"

    /*
     * Use the original, non-resampled sheet and the complete alpha bounds of
     * each sprite. Tall trees and wide rocks/carts therefore cannot bleed
     * into, or be cut by, a neighbouring cell.
     */
    private val rockDefinition =
        legacyDefinition(
            type = MapObstacleType.ROCK,
            sourceRegions =
                listOf(
                    MapObstacleSourceRegion(78, 47, 93, 61),
                    MapObstacleSourceRegion(189, 42, 96, 67),
                    MapObstacleSourceRegion(300, 47, 114, 61),
                    MapObstacleSourceRegion(429, 43, 110, 68)
                ),
            heightMultiplier = 0.68f
        )

    private val treeDefinition =
        MapObstacleSpriteDefinition(
            type = MapObstacleType.TREE,
            texturePath = TREE_TEXTURE,
            sourceRegions =
                listOf(
                    MapObstacleSourceRegion(8, 268, 458, 535),
                    MapObstacleSourceRegion(462, 208, 353, 596),
                    MapObstacleSourceRegion(836, 216, 326, 588),
                    MapObstacleSourceRegion(1180, 186, 350, 618)
                ),
            widthMultiplier = 1f,
            heightMultiplier = 1.55f,
            verticalOffsetMultiplier = 0.275f
        )

    private val wellDefinition =
        legacyDefinition(
            type = MapObstacleType.WELL,
            sourceRegions =
                listOf(
                    MapObstacleSourceRegion(80, 198, 85, 80),
                    MapObstacleSourceRegion(192, 209, 91, 64),
                    MapObstacleSourceRegion(309, 198, 91, 79),
                    MapObstacleSourceRegion(431, 204, 102, 72)
                ),
            heightMultiplier = 0.92f
        )

    private val hayBaleDefinition =
        legacyDefinition(
            type = MapObstacleType.HAY_BALE,
            sourceRegions =
                listOf(
                    MapObstacleSourceRegion(75, 282, 97, 68),
                    MapObstacleSourceRegion(194, 280, 89, 71),
                    MapObstacleSourceRegion(300, 277, 110, 76),
                    MapObstacleSourceRegion(430, 289, 108, 59)
                ),
            heightMultiplier = 0.76f
        )

    private val wagonDefinition =
        legacyDefinition(
            type = MapObstacleType.BROKEN_WAGON,
            sourceRegions =
                listOf(
                    MapObstacleSourceRegion(55, 355, 125, 85),
                    MapObstacleSourceRegion(180, 355, 115, 85),
                    MapObstacleSourceRegion(293, 355, 128, 85),
                    MapObstacleSourceRegion(420, 355, 134, 85)
                ),
            heightMultiplier = 0.72f
        )

    private val roadDefinitions =
        mapOf(
            MapObstacleType.ROCK to rockDefinition,
            MapObstacleType.TREE to treeDefinition,
            MapObstacleType.WELL to wellDefinition,
            MapObstacleType.HAY_BALE to hayBaleDefinition,
            MapObstacleType.BROKEN_WAGON to wagonDefinition
        )

    private val crateDefinition =
        MapObstacleSpriteDefinition(
            type = MapObstacleType.CRATE,
            texturePath = SHARED_TEXTURE,
            sourceRegions =
                listOf(
                    MapObstacleSourceRegion(93, 4, 263, 252),
                    MapObstacleSourceRegion(451, 6, 261, 250),
                    MapObstacleSourceRegion(818, 5, 268, 251),
                    MapObstacleSourceRegion(1181, 9, 280, 247)
                ),
            widthMultiplier = 1f,
            heightMultiplier = 0.96f
        )

    private val barrelDefinition =
        MapObstacleSpriteDefinition(
            type = MapObstacleType.BARREL,
            texturePath = SHARED_TEXTURE,
            sourceRegions =
                listOf(
                    MapObstacleSourceRegion(107, 256, 205, 256),
                    MapObstacleSourceRegion(477, 265, 208, 247),
                    MapObstacleSourceRegion(830, 264, 248, 248),
                    MapObstacleSourceRegion(1217, 256, 246, 256)
                ),
            widthMultiplier = 1f,
            heightMultiplier = 1f
        )

    private val logPileDefinition =
        MapObstacleSpriteDefinition(
            type = MapObstacleType.LOG_PILE,
            texturePath = SHARED_TEXTURE,
            sourceRegions =
                listOf(
                    MapObstacleSourceRegion(38, 512, 336, 256),
                    MapObstacleSourceRegion(407, 512, 347, 256),
                    MapObstacleSourceRegion(780, 512, 360, 256),
                    MapObstacleSourceRegion(1165, 512, 352, 249)
                ),
            widthMultiplier = 1f,
            heightMultiplier = 0.62f
        )

    private val bushDefinition =
        MapObstacleSpriteDefinition(
            type = MapObstacleType.BUSH,
            texturePath = SHARED_TEXTURE,
            sourceRegions =
                listOf(
                    MapObstacleSourceRegion(31, 768, 343, 244),
                    MapObstacleSourceRegion(415, 768, 325, 244),
                    MapObstacleSourceRegion(790, 768, 335, 243),
                    MapObstacleSourceRegion(1165, 768, 334, 244)
                ),
            widthMultiplier = 1f,
            heightMultiplier = 0.62f
        )

    private val houseDefinition =
        MapObstacleSpriteDefinition(
            type = MapObstacleType.HOUSE,
            texturePath = HOUSE_TEXTURE,
            sourceRegions =
                listOf(
                    MapObstacleSourceRegion(188, 0, 1232, 875)
                ),
            widthMultiplier = 1f,
            heightMultiplier = 0.72f,
            verticalOffsetMultiplier = 0.02f
        )

    private val troubleAtHomeDefinitions =
        roadDefinitions +
            mapOf(
                MapObstacleType.CRATE to crateDefinition,
                MapObstacleType.BARREL to barrelDefinition,
                MapObstacleType.LOG_PILE to logPileDefinition,
                MapObstacleType.BUSH to bushDefinition,
                MapObstacleType.HOUSE to houseDefinition
            )

    private val dungeonDefinitions =
        mapOf(
            MapObstacleType.CAVE_ROCK to
                rockDefinition.copy(
                    type = MapObstacleType.CAVE_ROCK
                )
        )

    fun find(
        gameMap: GameMap,
        type: MapObstacleType
    ): MapObstacleSpriteDefinition? {
        return definitionsFor(gameMap)[type]
    }

    fun contains(
        gameMap: GameMap,
        type: MapObstacleType
    ): Boolean {
        return find(
            gameMap = gameMap,
            type = type
        ) != null
    }

    private fun definitionsFor(
        gameMap: GameMap
    ): Map<MapObstacleType, MapObstacleSpriteDefinition> {
        return when (gameMap) {
            GameMap.ROAD_TO_HOME -> roadDefinitions
            GameMap.TROUBLE_AT_HOME -> troubleAtHomeDefinitions
            GameMap.FOLLOWING_THE_DUNGEON_DELVER -> dungeonDefinitions
        }
    }

    private fun legacyDefinition(
        type: MapObstacleType,
        sourceRegions: List<MapObstacleSourceRegion>,
        heightMultiplier: Float,
        verticalOffsetMultiplier: Float = 0f
    ): MapObstacleSpriteDefinition {
        return MapObstacleSpriteDefinition(
            type = type,
            texturePath = LEGACY_ROAD_TEXTURE,
            sourceRegions = sourceRegions,
            widthMultiplier = 1f,
            heightMultiplier = heightMultiplier,
            verticalOffsetMultiplier = verticalOffsetMultiplier
        )
    }
}
