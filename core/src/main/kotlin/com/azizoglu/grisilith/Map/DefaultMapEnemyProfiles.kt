package com.azizoglu.grisilith.map.enemy

import com.azizoglu.grisilith.config.GameMap

/*
 * İlk Map -> EnemyTag eşleşmeleri:
 *
 * Road to Home                 -> Beast
 * Trouble at Home              -> Humanoid
 * Following the Dungeon Delver -> Undead
 *
 * Başlangıçta bütün uygun Enemy Kind'lar eşit ağırlıktadır.
 */
object DefaultMapEnemyProfiles {

    val roadToHome =
        MapEnemyProfile(
            gameMap =
                GameMap.ROAD_TO_HOME
        )

    val troubleAtHome =
        MapEnemyProfile(
            gameMap =
                GameMap.TROUBLE_AT_HOME
        )

    val followingTheDungeonDelver =
        MapEnemyProfile(
            gameMap =
                GameMap.FOLLOWING_THE_DUNGEON_DELVER
        )

    val testMap =
        MapEnemyProfile(gameMap = GameMap.TEST_MAP)

    val all:
        List<MapEnemyProfile> =
        listOf(
            roadToHome,
            troubleAtHome,
            followingTheDungeonDelver,
            testMap
        )

    private val byMap =
        all.associateBy { profile ->
            profile.gameMap
        }

    fun forMap(
        gameMap: GameMap
    ): MapEnemyProfile {
        return requireNotNull(
            byMap[
                gameMap
            ]
        ) {
            "Missing MapEnemyProfile for $gameMap."
        }
    }
}
