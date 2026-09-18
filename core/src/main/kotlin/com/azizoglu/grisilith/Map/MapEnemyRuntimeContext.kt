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

    var profile:
        MapEnemyProfile =
        DefaultMapEnemyProfiles
            .forMap(
                gameMap
            )
        private set

    fun selectEnemyTag(tag: com.azizoglu.grisilith.enemy.definition.EnemyTag) {
        require(tag != com.azizoglu.grisilith.enemy.definition.EnemyTag.MONSTER) {
            "Monster family is not available in Test Map controls."
        }
        profile = MapEnemyProfile(gameMap = gameMap, enemyTags = setOf(tag))
    }

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
