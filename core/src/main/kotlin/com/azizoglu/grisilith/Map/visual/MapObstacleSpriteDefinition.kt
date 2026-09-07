package com.azizoglu.grisilith.map.visual

import com.azizoglu.grisilith.map.MapObstacleType

data class MapObstacleSpriteDefinition(
    val type: MapObstacleType,
    val texturePath: String,
    val variantTexturePaths: List<String> = emptyList(),
    val sourceRegions: List<MapObstacleSourceRegion>,
    val widthMultiplier: Float,
    val heightMultiplier: Float,
    val verticalOffsetMultiplier: Float = 0f,
    val stretchToVisualBounds: Boolean = false
) {
    init {
        require(texturePath.isNotBlank())
        require(variantTexturePaths.none(String::isBlank))
        require(sourceRegions.isNotEmpty())
        require(widthMultiplier > 0f)
        require(heightMultiplier > 0f)
    }
}

data class MapObstacleSourceRegion(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
) {
    init {
        require(x >= 0)
        require(y >= 0)
        require(width > 0)
        require(height > 0)
    }
}
