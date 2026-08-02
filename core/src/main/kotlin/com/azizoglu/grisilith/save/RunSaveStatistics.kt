package com.azizoglu.grisilith.save

data class RunSaveStatistics(
    val totalDamageDealt: Long = 0L,
    val totalDamageTaken: Long = 0L,
    val totalEnemiesKilled: Int = 0,
    val totalMiniBossesDefeated: Int = 0,
    val totalFinalBossesDefeated: Int = 0,
    val totalGoldCollected: Float = 0f,
    val totalGoldSpent: Float = 0f,
    val totalChestsOpened: Int = 0,
    val damageBySource: Map<String, Long> = emptyMap()
)
