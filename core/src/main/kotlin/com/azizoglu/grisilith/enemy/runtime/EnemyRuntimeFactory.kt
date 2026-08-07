package com.azizoglu.grisilith.enemy.runtime

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyStats
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.enemy.definition.EnemyKindDefinition
import com.azizoglu.grisilith.enemy.definition.EnemyKindRegistry

/*
 * EnemyKindDefinition'dan gerçek Enemy runtime nesnesi üretir.
 *
 * Spawn konumu doğrulaması bu sınıfın görevi değildir.
 * Map/Spawner paketi geçerli X/Y konumunu bulduktan sonra
 * factory'yi çağıracaktır.
 */
class EnemyRuntimeFactory(
    private val registry:
    EnemyKindRegistry
) {

    fun create(
        kindId: ContentId,
        enemyType: EnemyType,
        stage: Int,
        startX: Float,
        startY: Float
    ): Enemy {

        val definition =
            requireDefinition(
                kindId
            )

        return create(
            definition =
                definition,

            enemyType =
                enemyType,

            stage =
                stage,

            startX =
                startX,

            startY =
                startY
        )
    }

    fun create(
        definition: EnemyKindDefinition,
        enemyType: EnemyType,
        stage: Int,
        startX: Float,
        startY: Float
    ): Enemy {

        val stats =
            createStats(
                definition =
                    definition,

                enemyType =
                    enemyType,

                stage =
                    stage
            )

        return Enemy(
            startX =
                startX,

            startY =
                startY,

            stats =
                stats
        )
    }

    fun createStats(
        kindId: ContentId,
        enemyType: EnemyType,
        stage: Int
    ): EnemyStats {

        return createStats(
            definition =
                requireDefinition(
                    kindId
                ),

            enemyType =
                enemyType,

            stage =
                stage
        )
    }

    fun createStats(
        definition: EnemyKindDefinition,
        enemyType: EnemyType,
        stage: Int
    ): EnemyStats {

        return EnemyKindStatScaler
            .resolve(
                definition =
                    definition,

                enemyType =
                    enemyType,

                stage =
                    stage
            )
    }

    private fun requireDefinition(
        kindId: ContentId
    ): EnemyKindDefinition {

        return requireNotNull(
            registry.findById(
                kindId
            )
        ) {
            "Unknown Enemy Kind ID: $kindId"
        }
    }
}
