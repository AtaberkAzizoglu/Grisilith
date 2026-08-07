package com.azizoglu.grisilith.map.enemy

import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.enemy.definition.EnemyKindDefinition
import com.azizoglu.grisilith.enemy.definition.EnemyKindRegistry
import kotlin.random.Random

/*
 * Map profiline göre Enemy Kind seçer.
 *
 * Registry içindeki spawnWeight ile Map override çarpanı
 * birlikte kullanılır.
 */
class MapEnemySelectionResolver(
    private val registry:
    EnemyKindRegistry,

    private val random:
    Random =
        Random.Default
) {

    fun choose(
        profile: MapEnemyProfile,
        enemyType: EnemyType
    ): EnemyKindDefinition? {

        val candidates =
            registry
                .eligibleKinds(
                    enemyTags =
                        profile.enemyTags,

                    enemyType =
                        enemyType
                )
                .mapNotNull { definition ->
                    val resolvedWeight =
                        definition.spawnWeight *
                            profile.weightMultiplierFor(
                                definition.id
                            )

                    if (resolvedWeight <= 0f) {
                        null
                    } else {
                        WeightedCandidate(
                            definition =
                                definition,

                            weight =
                                resolvedWeight
                        )
                    }
                }

        if (candidates.isEmpty()) {
            return null
        }

        val totalWeight =
            candidates.sumOf { candidate ->
                candidate.weight.toDouble()
            }

        var roll =
            random.nextDouble(
                from = 0.0,
                until = totalWeight
            )

        candidates.forEach { candidate ->
            roll -=
                candidate.weight

            if (roll <= 0.0) {
                return candidate.definition
            }
        }

        return candidates.last()
            .definition
    }

    private data class WeightedCandidate(
        val definition:
        EnemyKindDefinition,

        val weight:
        Float
    )
}
