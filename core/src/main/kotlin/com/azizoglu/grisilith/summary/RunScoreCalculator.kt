package com.azizoglu.grisilith.summary

import com.azizoglu.grisilith.config.GameDifficulty
import com.azizoglu.grisilith.config.GameMap

/** Raw kills during gameplay; difficulty and reached-stage bonus on the results screen. */
object RunScoreCalculator {
    fun enemyScore(map: GameMap, normalEnemiesKilled: Int, uniqueEnemiesKilled: Int,
                   miniBossesDefeated: Int, finalBossesDefeated: Int): Long {
        if(map==GameMap.TEST_MAP)return 0L
        return normalEnemiesKilled.coerceAtLeast(0).toLong() +
            uniqueEnemiesKilled.coerceAtLeast(0).toLong()*3L +
            miniBossesDefeated.coerceAtLeast(0).toLong()*250L +
            finalBossesDefeated.coerceAtLeast(0).toLong()*5000L
    }

    private fun difficultyPercent(difficulty: GameDifficulty): Long = when(difficulty) {
        GameDifficulty.EASY -> 33L
        GameDifficulty.NORMAL -> 50L
        GameDifficulty.HARD -> 100L
    }

    fun difficultyMultiplier(difficulty: GameDifficulty): Float = difficultyPercent(difficulty)/100f

    fun stageMultiplier(difficulty: GameDifficulty, stageReached: Int): Float =
        1f+stageReached.coerceIn(0,difficulty.maximumStage)*.1f

    fun calculate(
        map: GameMap,
        difficulty: GameDifficulty,
        normalEnemiesKilled: Int,
        uniqueEnemiesKilled: Int,
        miniBossesDefeated: Int,
        finalBossesDefeated: Int,
        stageReached: Int
    ): Long {
        if (map == GameMap.TEST_MAP) return 0L
        val kills = enemyScore(map,normalEnemiesKilled,uniqueEnemiesKilled,miniBossesDefeated,finalBossesDefeated)
        val stages = stageReached.coerceIn(0, difficulty.maximumStage)
        // Exact integer arithmetic, one final round-to-nearest (half up).
        // Even Int.MAX_VALUE kills in every category fit in Long for legal stage counts.
        return (kills * difficultyPercent(difficulty) * (10L + stages) + 500L) / 1000L
    }
}
