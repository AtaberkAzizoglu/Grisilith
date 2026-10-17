package com.azizoglu.grisilith.save
import java.util.HashMap

data class RunSaveStatistics(
    val totalDamageDealt: Long = 0L,
    val totalDamageTaken: Long = 0L,
    val totalEnemiesKilled: Int = 0,
    val totalMiniBossesDefeated: Int = 0,
    val totalFinalBossesDefeated: Int = 0,
    val totalGoldCollected: Float = 0f,
    val totalGoldSpent: Float = 0f,
    val totalChestsOpened: Int = 0,
    val damageBySource: HashMap<String, Long> = hashMapOf(),
    val damageDealtByType: HashMap<String, Long> = hashMapOf(),
    val damageTakenByType: HashMap<String, Long> = hashMapOf(),
    val damageReducedByType: HashMap<String, Long> = hashMapOf(),
    val totalUniqueEnemiesKilled: Int = 0)
