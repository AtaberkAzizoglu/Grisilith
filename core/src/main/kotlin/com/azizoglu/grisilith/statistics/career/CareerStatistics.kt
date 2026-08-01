package com.azizoglu.grisilith.statistics.career

data class CareerStatistics(
    val enemiesKilled: Long = 0L,
    val bossesDefeated: Long = 0L,
    val finalBossesDefeated: Long = 0L,

    val damageDealt: Long = 0L,
    val damageTaken: Long = 0L,

    val goldCollected: Float = 0f,
    val goldSpent: Float = 0f,
    val grisilithCoinsEarned: Long = 0L,

    val totalPlayTimeSeconds: Long = 0L,

    val runsStarted: Long = 0L,
    val runsFinished: Long = 0L,
    val runsWon: Long = 0L,
    val runsLost: Long = 0L,

    val longestRunSeconds: Long = 0L,
    val highestDamage: Long = 0L,
    val mostGold: Float = 0f,
    val mostGrisilithCoins: Int = 0,
    val mostDamageTaken: Long = 0L
) {

    companion object {

        const val ACHIEVEMENT_TARGET =
            131_313L
    }

    fun achievementProgress(
        currentValue: Long
    ): Float {

        return (
            currentValue.toFloat() /
                ACHIEVEMENT_TARGET.toFloat()
            )
            .coerceIn(
                0f,
                1f
            )
    }
}
