package com.azizoglu.grisilith.summary

data class RunGradeResult(
    val grade: RunSummaryGrade,
    val totalScore: Int,
    val progressScore: Int,
    val combatScore: Int,
    val economyScore: Int,
    val resultBonus: Int,
    val difficultyMultiplier: Float
)
