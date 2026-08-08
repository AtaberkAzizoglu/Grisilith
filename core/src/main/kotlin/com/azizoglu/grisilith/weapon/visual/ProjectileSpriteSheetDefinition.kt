package com.azizoglu.grisilith.weapon.visual

data class ProjectileSpriteSheetDefinition(
    val texturePath: String,
    val tileWidth: Int,
    val tileHeight: Int,
    val columns: Int,
    val rows: Int
) {
    init {
        require(texturePath.isNotBlank())
        require(tileWidth > 0)
        require(tileHeight > 0)
        require(columns > 0)
        require(rows > 0)
    }
}
