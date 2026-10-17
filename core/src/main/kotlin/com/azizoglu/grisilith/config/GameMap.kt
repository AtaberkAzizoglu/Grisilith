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
        displayName = "Crolymnos Forest",
        description =
            "Ancient paths wind beneath Crolymnos' towering trees, where hungry beasts stalk every shadow. " +
                "Few travelers cross the forest after sundown and return unchanged.",
        worldWidth = 2200f,
        worldHeight = 1500f,
        isInitiallyUnlocked = true,
        enemyTags =
            setOf(
                EnemyTag.BEAST
            )
    ),

    TROUBLE_AT_HOME(
        displayName = "Vogtsweier Siege",
        description =
            "Vogtsweier's gates have held for generations, but raiders now press the village from every side. " +
                "Its defenders must endure until the last war horn falls silent.",
        worldWidth = 2500f,
        worldHeight = 2000f,
        isInitiallyUnlocked = true,
        enemyTags =
            setOf(
                EnemyTag.HUMANOID
            )
    ),

    FOLLOWING_THE_DUNGEON_DELVER(
        displayName = "Hollowgrave Depths",
        description =
            "A vanished delver opened passages that should have remained sealed beneath Hollowgrave. " +
                "Now the dead wander its halls, guarding whatever woke in the dark.",
        worldWidth = 1750f,
        worldHeight = 1250f,
        isInitiallyUnlocked = true,
        enemyTags =
            setOf(
                EnemyTag.UNDEAD
            )
    ),

    TEST_MAP(
        displayName = "Test Map",
        description = "A controlled 50-stage sandbox for testing builds and combat.",
        worldWidth = 2200f,
        worldHeight = 1500f,
        isInitiallyUnlocked = true,
        enemyTags = setOf(EnemyTag.BEAST)
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
