package com.azizoglu.grisilith.map.tile

data class MapTileSpriteDefinition(
    val texturePath: String,
    val tileType: MapTileType,
    val sourceTileSize: Int,
    val variationCount: Int,
    val worldTileSize: Float
) {
    init {
        require(texturePath.isNotBlank())
        require(sourceTileSize > 0)
        require(variationCount > 0)
        require(worldTileSize > 0f)
    }
}
