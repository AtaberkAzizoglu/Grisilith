package com.azizoglu.grisilith.enemy.runtime

import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.enemy.definition.EnemyKindDefinition
import com.azizoglu.grisilith.enemy.definition.EnemyKindRegistry
import com.azizoglu.grisilith.enemy.definition.EnemyTag
import kotlin.random.Random

/*
 * Registry seçimi ile Unique roll'ünü tek yerde toplar.
 *
 * Önce NORMAL Enemy Kind seçilir. Ardından başarılı roll aynı
 * Kind'ın UNIQUE sürümüne dönüştürülür.
 */
class EnemySpawnSelectionResolver(
    private val registry:
    EnemyKindRegistry,

    private val random:
    Random =
        Random.Default
) {

    fun chooseRegular(
        enemyTags: Set<EnemyTag>,
        uniqueChance: Float
    ): EnemySpawnSelection? {

        val definition =
            registry.chooseWeighted(
                enemyTags =
                    enemyTags,

                enemyType =
                    EnemyType.NORMAL,

                random =
                    random
            )
                ?: return null

        val safeUniqueChance =
            uniqueChance.coerceIn(
                0f,
                1f
            )

        val enemyType =
            if (
                random.nextFloat() <
                safeUniqueChance
            ) {
                EnemyType.UNIQUE
            } else {
                EnemyType.NORMAL
            }

        return EnemySpawnSelection(
            definition =
                definition,

            enemyType =
                enemyType
        )
    }

    fun chooseBoss(
        enemyTags: Set<EnemyTag>,
        enemyType: EnemyType
    ): EnemySpawnSelection? {

        require(
            enemyType ==
                EnemyType.MINI_BOSS ||
                enemyType ==
                EnemyType.FINAL_BOSS
        ) {
            "Boss selection requires MINI_BOSS or FINAL_BOSS."
        }

        val definition =
            registry.chooseWeighted(
                enemyTags =
                    enemyTags,

                enemyType =
                    enemyType,

                random =
                    random
            )
                ?: return null

        return EnemySpawnSelection(
            definition =
                definition,

            enemyType =
                enemyType
        )
    }
}

data class EnemySpawnSelection(
    val definition: EnemyKindDefinition,
    val enemyType: EnemyType
)
