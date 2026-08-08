package com.azizoglu.grisilith.map.tile

object MapTileSpriteRegistry {

    val grass =
        MapTileSpriteDefinition(
            texturePath =
                "map/tiles/road_to_home_grass.png",

            tileType =
                MapTileType.GRASS,

            sourceTileSize =
                64,

            variationCount =
                4,

            /*
             * Oyundaki fiziksel tile boyutu.
             * Kaynak görseller 32x32 tasarlandı; world tarafında da
             * 32 birimlik grid kullanıyoruz.
             */
            worldTileSize =
                32f
        )
}
