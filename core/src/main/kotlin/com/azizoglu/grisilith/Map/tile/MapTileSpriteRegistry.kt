package com.azizoglu.grisilith.map.tile

import com.azizoglu.grisilith.config.GameMap

object MapTileSpriteRegistry {

    private val grass =
        MapTileSpriteDefinition(
            texturePath =
                "map/tiles/road_to_home_grass.png",

            tileType =
                MapTileType.GRASS,

            sourceRegions =
                List(4) {
                        index ->

                    MapTileSourceRegion(
                        x = index * 64 + 1,
                        y = 1,
                        width = 62,
                        height = 61
                    )
                },

            /*
             * Oyundaki fiziksel tile boyutu.
             * Kaynak görseller 32x32 tasarlandı; world tarafında da
             * 32 birimlik grid kullanıyoruz.
             */
            worldTileSize =
                32f
        )

    private val dungeonFloor =
        MapTileSpriteDefinition(
            texturePath =
                "map/tiles/following_the_dungeon_delver_tiles.png",
            tileType =
                MapTileType.DUNGEON_FLOOR,
            sourceRegions =
                listOf(
                    MapTileSourceRegion(46, 345, 322, 311),
                    MapTileSourceRegion(421, 345, 321, 311),
                    MapTileSourceRegion(795, 345, 321, 311),
                    MapTileSourceRegion(1169, 345, 323, 311)
                ),
            worldTileSize =
                64f
        )

    fun groundFor(
        gameMap: GameMap
    ): MapTileSpriteDefinition {
        return when (gameMap) {
            GameMap.ROAD_TO_HOME,
            GameMap.TROUBLE_AT_HOME ->
                grass

            GameMap.FOLLOWING_THE_DUNGEON_DELVER ->
                dungeonFloor
        }
    }
}
