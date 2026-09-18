package com.azizoglu.grisilith.content

import com.azizoglu.grisilith.config.GameMap

object DefaultCollectionMaps {

    val all:
        List<MapCollectionDefinition> =
        GameMap.entries.filterNot { it == GameMap.TEST_MAP }.map { gameMap ->

            MapCollectionDefinition(
                gameMap =
                    gameMap
            )
        }
}
