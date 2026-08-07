package com.azizoglu.grisilith.map.enemy

import com.azizoglu.grisilith.config.GameMap
import com.azizoglu.grisilith.enemy.definition.EnemyTag

/*
 * Bir Map'in hangi Enemy havuzunu kullanacağını tanımlar.
 *
 * Enemy sayısı burada belirlenmez.
 * Stage ve Enemy Count sistemi kaç Enemy geleceğini,
 * bu profil ise hangi Enemy Kind'ların seçilebileceğini belirler.
 */
data class MapEnemyProfile(
    val gameMap: GameMap,

    val enemyTags: Set<EnemyTag> =
        gameMap.enemyTags,

    val kindOverrides:
    List<MapEnemyKindOverride> =
        emptyList(),

    val uniqueSpawnChanceMultiplier: Float =
        1f
) {

    init {
        require(enemyTags.isNotEmpty()) {
            "Map Enemy Profile must contain at least one EnemyTag."
        }

        require(uniqueSpawnChanceMultiplier >= 0f) {
            "Unique chance multiplier cannot be negative."
        }

        require(
            kindOverrides
                .map { override ->
                    override.enemyKindId
                }
                .distinct()
                .size ==
                kindOverrides.size
        ) {
            "Map Enemy Profile cannot contain duplicate Enemy Kind overrides."
        }
    }

    private val overridesByKindId =
        kindOverrides.associateBy { override ->
            override.enemyKindId
        }

    fun weightMultiplierFor(
        enemyKindId:
        com.azizoglu.grisilith.content.ContentId
    ): Float {
        return overridesByKindId[
            enemyKindId
        ]
            ?.resolvedWeightMultiplier
            ?: 1f
    }
}
