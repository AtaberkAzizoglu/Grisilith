package com.azizoglu.grisilith.map.enemy

import com.azizoglu.grisilith.config.GameMap
import com.azizoglu.grisilith.enemy.definition.DefaultEnemyKinds
import com.azizoglu.grisilith.enemy.definition.EnemyKindRegistry
import com.azizoglu.grisilith.enemy.runtime.EnemyRuntimeFactory

/*
 * Bir run başlarken Map'e bağlı Enemy sistemini tek yerde kurar.
 *
 * Sonraki paketlerde EnemySpawner bu context üzerinden
 * Map profiline uygun Kind seçecek ve factory ile runtime üretecek.
 */
class MapEnemyRuntimeContext(
    val gameMap: GameMap,

    val registry:
    EnemyKindRegistry =
        DefaultEnemyKinds.createRegistry()
) {

    val profile:
        MapEnemyProfile =
        DefaultMapEnemyProfiles
            .forMap(
                gameMap
            )

    val selectionResolver =
        MapEnemySelectionResolver(
            registry =
                registry
        )

    val runtimeFactory =
        EnemyRuntimeFactory(
            registry =
                registry
        )
}
