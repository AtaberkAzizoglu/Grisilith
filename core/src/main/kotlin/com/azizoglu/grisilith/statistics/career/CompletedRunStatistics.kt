package com.azizoglu.grisilith.statistics.career

data class CompletedRunStatistics(
    val wasSuccessful: Boolean,
    val durationSeconds: Long,

    val enemiesKilled: Int,
    val bossesDefeated: Int,
    val finalBossesDefeated: Int,

    val damageDealt: Long,
    val damageTaken: Long,

    val goldCollected: Float,
    val goldSpent: Float,

    val grisilithCoinsEarned: Int,
    val score: Long = 0L
)
