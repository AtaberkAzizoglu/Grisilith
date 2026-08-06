package com.azizoglu.grisilith.enemy

import kotlin.math.roundToInt

object EnemySpawnScaling {

    private const val BASE_SPAWN_INTERVAL = 1.5f
    private const val MINIMUM_SPAWN_INTERVAL = 0.18f

    private const val BASE_MAXIMUM_ENEMY_COUNT = 40
    private const val ENEMY_COUNT_PER_STAGE = 6

    private const val BASE_UNIQUE_CHANCE = 0.5f
    private const val UNIQUE_CHANCE_PER_STAGE = 0.005f
    private const val MAXIMUM_UNIQUE_CHANCE = 0.20f

    fun settingsForStage(
        stage: Int
    ): EnemySpawnSettings {
        val safeStage = stage.coerceAtLeast(1)

        val spawnInterval =
            (
                BASE_SPAWN_INTERVAL -
                    (safeStage - 1) * 0.045f
                ).coerceAtLeast(
                    MINIMUM_SPAWN_INTERVAL
                )

        val maximumEnemyCount =
            BASE_MAXIMUM_ENEMY_COUNT +
                (safeStage - 1) *
                ENEMY_COUNT_PER_STAGE

        val uniqueSpawnChance =
            (
                BASE_UNIQUE_CHANCE +
                    (safeStage - 1) *
                    UNIQUE_CHANCE_PER_STAGE
                ).coerceAtMost(
                    MAXIMUM_UNIQUE_CHANCE
                )

        return EnemySpawnSettings(
            spawnInterval = spawnInterval,
            maximumEnemyCount = maximumEnemyCount,
            uniqueSpawnChance = uniqueSpawnChance
        )
    }

    fun regularEnemyQuotaForStage(
        stage: Int,
        isBossStage: Boolean,
        enemyCountMultiplier: Float = 1f
    ): Int {
        val baseStageEnemyCount =
            settingsForStage(stage)
                .maximumEnemyCount

        val stageTypeMultiplier =
            if (isBossStage) {
                0.5f
            } else {
                1f
            }

        return (
            baseStageEnemyCount *
                stageTypeMultiplier *
                enemyCountMultiplier
            )
            .roundToInt()
            .coerceAtLeast(1)
    }

}
