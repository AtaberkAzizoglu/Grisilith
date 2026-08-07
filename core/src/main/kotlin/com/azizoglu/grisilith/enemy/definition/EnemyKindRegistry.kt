package com.azizoglu.grisilith.enemy.definition

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.enemy.EnemyType
import kotlin.random.Random

/*
 * Enemy Kind tanımlarını Tag ve Type üzerinden bulur.
 *
 * Başlangıçta bütün Kind'lar spawnWeight = 1 kullanacağı için
 * eşit ihtimalle seçilir. İleride yalnızca ağırlık değerleri
 * değiştirilerek dağılım ayarlanabilir.
 */
class EnemyKindRegistry {

    private val definitions =
        linkedMapOf<ContentId, EnemyKindDefinition>()

    val allDefinitions:
        List<EnemyKindDefinition>
        get() =
            definitions.values
                .toList()

    fun register(
        definition: EnemyKindDefinition
    ) {
        require(
            definition.id !in
                definitions
        ) {
            "Duplicate Enemy Kind ID: ${definition.id}"
        }

        definitions[
            definition.id
        ] =
            definition
    }

    fun registerAll(
        newDefinitions: Iterable<EnemyKindDefinition>
    ) {
        newDefinitions.forEach { definition ->
            register(
                definition
            )
        }
    }

    fun findById(
        id: ContentId
    ): EnemyKindDefinition? {
        return definitions[id]
    }

    fun eligibleKinds(
        enemyTags: Set<EnemyTag>,
        enemyType: EnemyType
    ): List<EnemyKindDefinition> {
        if (enemyTags.isEmpty()) {
            return emptyList()
        }

        return definitions.values
            .filter { definition ->
                definition.enemyTags.any { tag ->
                    tag in enemyTags
                } &&
                    definition.supportsType(
                        enemyType
                    )
            }
    }

    fun chooseWeighted(
        enemyTags: Set<EnemyTag>,
        enemyType: EnemyType,
        random: Random = Random.Default
    ): EnemyKindDefinition? {

        val candidates =
            eligibleKinds(
                enemyTags =
                    enemyTags,

                enemyType =
                    enemyType
            )

        if (candidates.isEmpty()) {
            return null
        }

        val totalWeight =
            candidates.sumOf { definition ->
                definition.spawnWeight
                    .toDouble()
            }

        var roll =
            random.nextDouble(
                from = 0.0,
                until = totalWeight
            )

        candidates.forEach { definition ->
            roll -=
                definition.spawnWeight

            if (roll <= 0.0) {
                return definition
            }
        }

        return candidates.last()
    }
}
