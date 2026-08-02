package com.azizoglu.grisilith.chest

import com.azizoglu.grisilith.config.GameDifficulty

object ChestDiscardAllowanceCalculator {

    fun calculate(
        difficulty: GameDifficulty,
        futureItemBonus: Int = 0
    ): Int {
        val baseAllowance =
            when (difficulty) {
                GameDifficulty.EASY -> 2
                GameDifficulty.NORMAL -> 4
                GameDifficulty.HARD -> 7
            }

        return (baseAllowance + futureItemBonus)
            .coerceAtLeast(0)
    }
}
