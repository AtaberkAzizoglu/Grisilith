package com.azizoglu.grisilith.map.visual

import com.azizoglu.grisilith.map.MapObstacleType

data class MapObstacleSpriteDefinition(
    val type: MapObstacleType,
    val rowIndex: Int,
    val widthMultiplier: Float,
    val heightMultiplier: Float,
    val verticalOffsetMultiplier: Float = 0f
)
