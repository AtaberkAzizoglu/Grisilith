package com.azizoglu.grisilith.summary

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
    val goldCollected: Float,
    val grisilithCoinsEarned: Int,
    val chestsOpened: Int,
    val gradeResult: RunGradeResult,
    val stats: List<RunSummaryStatEntry>,
    val weapons: List<RunSummaryWeaponEntry>,
    val armor: RunSummaryArmorEntry,
    val tamgas: List<RunSummaryTamgaEntry>,
    val items: List<RunSummaryItemEntry>,
    val otherDamageSources: Map<String, Float> =
        emptyMap()
)
