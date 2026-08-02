package com.azizoglu.grisilith.config

enum class GameDifficulty(
    val displayName: String,
    val maximumStage: Int,
    val maximumGrisilithCoinsPerRun: Int
) {
    EASY(
        displayName = "Easy",
        maximumStage = 1,
        maximumGrisilithCoinsPerRun = 2
    ),

    NORMAL(
        displayName = "Normal",
        maximumStage = 35,
        maximumGrisilithCoinsPerRun = 3
    ),

    HARD(
        displayName = "Hard",
        maximumStage = 50,
        maximumGrisilithCoinsPerRun = 6
    )
}
