package com.azizoglu.grisilith.content

import com.azizoglu.grisilith.enemy.EnemyType

object DefaultCollectionEnemies {

    val all:
        List<EnemyCollectionDefinition> =
        EnemyType.entries.map { enemyType ->

            EnemyCollectionDefinition(
                enemyType =
                    enemyType
            )
        }
}
