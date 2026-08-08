package com.azizoglu.grisilith.map.visual

import com.azizoglu.grisilith.map.MapObstacleType

object MapObstacleSpriteRegistry {

    const val TEXTURE_PATH =
        "map/obstacles/road_to_home_obstacles.png"

    const val CELL_SIZE =
        192

    private val definitions =
        mapOf(
            MapObstacleType.ROCK to
                MapObstacleSpriteDefinition(
                    type = MapObstacleType.ROCK,
                    rowIndex = 0,
                    widthMultiplier = 1.18f,
                    heightMultiplier = 1.18f
                ),

            MapObstacleType.TREE to
                MapObstacleSpriteDefinition(
                    type = MapObstacleType.TREE,
                    rowIndex = 1,
                    widthMultiplier = 1.55f,
                    heightMultiplier = 1.55f,
                    verticalOffsetMultiplier = 0.12f
                ),

            MapObstacleType.WELL to
                MapObstacleSpriteDefinition(
                    type = MapObstacleType.WELL,
                    rowIndex = 2,
                    widthMultiplier = 1.18f,
                    heightMultiplier = 1.18f
                ),

            MapObstacleType.HAY_BALE to
                MapObstacleSpriteDefinition(
                    type = MapObstacleType.HAY_BALE,
                    rowIndex = 3,
                    widthMultiplier = 1.18f,
                    heightMultiplier = 1.18f
                ),

            MapObstacleType.BROKEN_WAGON to
                MapObstacleSpriteDefinition(
                    type = MapObstacleType.BROKEN_WAGON,
                    rowIndex = 4,
                    widthMultiplier = 1.22f,
                    heightMultiplier = 1.22f
                )
        )

    fun find(
        type: MapObstacleType
    ): MapObstacleSpriteDefinition? {
        return definitions[
            type
        ]
    }

    fun contains(
        type: MapObstacleType
    ): Boolean {
        return definitions.containsKey(
            type
        )
    }
}
