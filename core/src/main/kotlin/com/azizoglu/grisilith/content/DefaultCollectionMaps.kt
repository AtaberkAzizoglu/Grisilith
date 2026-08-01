package com.azizoglu.grisilith.content

import com.azizoglu.grisilith.config.GameMap

object DefaultCollectionMaps {

    val all:
        List<MapCollectionDefinition> =
        GameMap.entries.map { gameMap ->

            MapCollectionDefinition(
                gameMap =
                    gameMap
            )
        }
}
