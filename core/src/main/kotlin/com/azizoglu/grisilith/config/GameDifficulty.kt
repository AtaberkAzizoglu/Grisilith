package com.azizoglu.grisilith.config

enum class GameDifficulty(
    val displayName: String,
    val maximumStage: Int
) {
    EASY(
        displayName = "Easy",
        maximumStage = 20
    ),

    NORMAL(
        displayName = "Normal",
        maximumStage = 30
    ),

    HARD(
        displayName = "Hard",
        maximumStage = 40
    )
}
