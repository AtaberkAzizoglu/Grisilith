package com.azizoglu.grisilith.config

import com.azizoglu.grisilith.enemy.definition.EnemyTag

enum class GameMap(
    val displayName: String,
    val description: String,
    val worldWidth: Float,
    val worldHeight: Float,
    val isInitiallyUnlocked: Boolean,

    /*
     * Şimdilik her Map tek bir ana EnemyTag kullanır.
     * Set yapısı ileride aynı Map'in birden fazla EnemyTag
     * havuzunu birlikte kullanabilmesini sağlar.
     */
    val enemyTags: Set<EnemyTag>
) {
    ROAD_TO_HOME(
        displayName = "Road to Home",
        description =
            "A green wilderness crossed by a worn road.",
        worldWidth = 2200f,
        worldHeight = 1500f,
        isInitiallyUnlocked = true,
        enemyTags =
            setOf(
                EnemyTag.BEAST
            )
    ),

    TROUBLE_AT_HOME(
        displayName = "Trouble at Home",
        description =
            "A small village occupied by dangerous bandits.",
        worldWidth = 2500f,
        worldHeight = 2000f,
        isInitiallyUnlocked = false,
        enemyTags =
            setOf(
                EnemyTag.HUMANOID
            )
    ),

    FOLLOWING_THE_DUNGEON_DELVER(
        displayName = "Following the Dungeon Delver",
        description =
            "A wide underground chamber filled with undead.",
        worldWidth = 1750f,
        worldHeight = 1250f,
        isInitiallyUnlocked = false,
        enemyTags =
            setOf(
                EnemyTag.UNDEAD
            )
    );

    init {
        require(worldWidth > 0f) {
            "Map world width must be greater than zero."
        }

        require(worldHeight > 0f) {
            "Map world height must be greater than zero."
        }

        require(enemyTags.isNotEmpty()) {
            "Map $name must contain at least one EnemyTag."
        }
    }
}
