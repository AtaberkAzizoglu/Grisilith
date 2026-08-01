package com.azizoglu.grisilith.content

import com.azizoglu.grisilith.config.GameMap
import com.azizoglu.grisilith.enemy.EnemyType

object CollectionEncounterIds {

    fun map(
        gameMap: GameMap
    ): String {
        return "MAP_${gameMap.name}"
    }

    fun enemy(
        enemyType: EnemyType
    ): String {
        return "ENEMY_${enemyType.name}"
    }
}
