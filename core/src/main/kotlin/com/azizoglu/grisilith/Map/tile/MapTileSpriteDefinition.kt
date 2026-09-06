package com.azizoglu.grisilith.map.tile

data class MapTileSpriteDefinition(
    val texturePath: String,
    val tileType: MapTileType,
    val sourceRegions: List<MapTileSourceRegion>,
    val worldTileSize: Float
) {
    init {
        require(texturePath.isNotBlank())
        require(sourceRegions.isNotEmpty())
        require(worldTileSize > 0f)
    }
}

data class MapTileSourceRegion(
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
