package com.azizoglu.grisilith.config

enum class GameMap(
    val displayName: String,
    val description: String,
    val worldWidth: Float,
    val worldHeight: Float,
    val isInitiallyUnlocked: Boolean
) {
    ROAD_TO_HOME(
        displayName = "Road to Home",
        description =
            "A green wilderness crossed by a worn road.",
        worldWidth = 2200f,
        worldHeight = 1400f,
        isInitiallyUnlocked = true
    ),

    TROUBLE_AT_HOME(
        displayName = "Trouble at Home",
        description =
            "A small village occupied by dangerous bandits.",
        worldWidth = 2800f,
        worldHeight = 1800f,
        isInitiallyUnlocked = false
    ),

    FOLLOWING_THE_DUNGEON_DELVER(
        displayName = "Following the Dungeon Delver",
        description =
            "A wide underground chamber filled with undead.",
        worldWidth = 2600f,
        worldHeight = 1700f,
        isInitiallyUnlocked = false
    )
}
