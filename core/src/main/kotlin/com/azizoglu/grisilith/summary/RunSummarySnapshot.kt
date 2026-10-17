package com.azizoglu.grisilith.summary

import com.azizoglu.grisilith.content.DamageType

data class RunSummarySnapshot(
    val wasSuccessful: Boolean,
    val difficultyLabel: String,
    val mapName: String,
    val characterName: String,
    val stageReached: Int,
    val formattedRunTime: String,
    val enemiesKilled: Int,
    val totalDamageDealt: Float,
    val totalDamageTaken: Float,
    val totalDamageReduced: Float = 0f,
    val damageDealtByType: Map<DamageType, Float> = emptyMap(),
    val damageTakenByType: Map<DamageType, Float> = emptyMap(),
    val damageReducedByType: Map<DamageType, Float> = emptyMap(),
    val goldCollected: Float,
    val grisilithCoinsEarned: Int,
    val chestsOpened: Int,
    val score: Long,
    val stats: List<RunSummaryStatEntry>,
    val weapons: List<RunSummaryWeaponEntry>,
    val armor: RunSummaryArmorEntry,
    val tamgas: List<RunSummaryTamgaEntry>,
    val items: List<RunSummaryItemEntry>,
    val otherDamageSources: Map<String, Float> =
        emptyMap(),
    val enemyScore: Long = score,
    val scoreBeforeStageBonus: Long = score,
    val scoreStageMultiplier: Float = 1f,
    val scoreDifficultyMultiplier: Float = 1f
)
