package com.azizoglu.grisilith.summary

import com.azizoglu.grisilith.config.GameDifficulty
import kotlin.math.roundToInt

object RunGradeCalculator {

    fun calculate(
        difficulty: GameDifficulty,
        wasSuccessful: Boolean,
        stageReached: Int,
        enemiesKilled: Int,
        miniBossesDefeated: Int,
        finalBossesDefeated: Int,
        totalDamageDealt: Float,
        grisilithCoinsEarned: Int
    ): RunGradeResult {

        val progressScore =
            stageReached.coerceAtLeast(0) *
                300

        val combatScore =
            enemiesKilled.coerceAtLeast(0) *
                5 +
                miniBossesDefeated.coerceAtLeast(0) *
                250 +
                finalBossesDefeated.coerceAtLeast(0) *
                1000 +
                (
                    totalDamageDealt.coerceAtLeast(0f) /
                        20f
                    ).roundToInt()

        val economyScore =
            grisilithCoinsEarned.coerceAtLeast(0) *
                300

        val resultBonus =
            if (wasSuccessful) {
                2000
            } else {
                0
            }

        val multiplier =
            when (difficulty) {
                GameDifficulty.EASY -> 1.00f
                GameDifficulty.Normal -> 1.35f
                GameDifficulty.HARD -> 1.75f
            }

        val totalScore =
            (
                (
                    progressScore +
                        combatScore +
                        economyScore +
                        resultBonus
                    ) *
                    multiplier
                )
                .roundToInt()
                .coerceAtLeast(0)

        return RunGradeResult(
            grade = gradeForScore(totalScore),
            totalScore = totalScore,
            progressScore = progressScore,
            combatScore = combatScore,
            economyScore = economyScore,
            resultBonus = resultBonus,
            difficultyMultiplier = multiplier
        )
    }

    private fun gradeForScore(
        score: Int
    ): RunSummaryGrade {

        return when {
            score >= 28000 -> RunSummaryGrade.SSS
            score >= 20000 -> RunSummaryGrade.SS
            score >= 14000 -> RunSummaryGrade.S
            score >= 9000 -> RunSummaryGrade.A
            score >= 5000 -> RunSummaryGrade.B
            score >= 2000 -> RunSummaryGrade.C
            else -> RunSummaryGrade.D
        }
    }
}
