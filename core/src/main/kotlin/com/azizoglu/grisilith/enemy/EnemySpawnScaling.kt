package com.azizoglu.grisilith.enemy

import kotlin.math.roundToInt
import kotlin.math.ceil
import com.azizoglu.grisilith.item.runtime.ActiveItemEffects

object EnemySpawnScaling {

    private const val BASE_ENEMY_COUNT = 45
    private const val ENEMY_COUNT_PER_STAGE = 5

    private const val BASE_UNIQUE_CHANCE = 0.025f
    private const val UNIQUE_CHANCE_PER_STAGE = 0f
    private const val MAXIMUM_UNIQUE_CHANCE = 0.025f

    fun settingsForStage(
        stage: Int
    ): EnemySpawnSettings {
        val safeStage = stage.coerceAtLeast(1)

        val baseEnemyCount =
            BASE_ENEMY_COUNT +
                (safeStage - 1) *
                ENEMY_COUNT_PER_STAGE

        val uniqueSpawnChance =
            (
                (BASE_UNIQUE_CHANCE +
                    (safeStage - 1) * UNIQUE_CHANCE_PER_STAGE)
                    .coerceAtMost(MAXIMUM_UNIQUE_CHANCE) +
                    ActiveItemEffects.uniqueChanceBonus()
                ).coerceAtMost(
                    MAXIMUM_UNIQUE_CHANCE + 0.025f
                )

        return EnemySpawnSettings(
            baseEnemyCount = baseEnemyCount,
            uniqueSpawnChance = uniqueSpawnChance
        )
    }

    fun regularEnemyCountForStage(
        stage: Int,
        maximumStage: Int,
        enemyCountMultiplier: Float = 1f
    ): Int {
        val safeStage = stage.coerceAtLeast(1)
        val linearStageEnemyCount =
            settingsForStage(stage)
                .baseEnemyCount

        val earlyBalancedEnemyCount =
            if (safeStage <= EARLY_GAME_END_STAGE) {
                EARLY_GAME_START_COUNT +
                    (safeStage - 1) * EARLY_GAME_COUNT_PER_STAGE
            } else {
                linearStageEnemyCount
            }

        val safeMaximumStage = maximumStage.coerceAtLeast(1)
        val lateGameStartStage =
            ceil(safeMaximumStage * LATE_GAME_START_RATIO)
                .toInt()
                .coerceIn(1, safeMaximumStage)

        val lateGameProgress =
            if (safeStage <= lateGameStartStage || lateGameStartStage >= safeMaximumStage) {
                0f
            } else {
                (safeStage - lateGameStartStage).toFloat() /
                    (safeMaximumStage - lateGameStartStage).toFloat()
            }

        val lateGameMultiplier =
            1f + LATE_GAME_MAXIMUM_BONUS * lateGameProgress.coerceIn(0f, 1f)

        return (
            earlyBalancedEnemyCount *
                lateGameMultiplier *
                GLOBAL_ENEMY_COUNT_MULTIPLIER *
                enemyCountMultiplier
            )
            .roundToInt()
            .coerceAtLeast(1)
    }

    private const val EARLY_GAME_END_STAGE = 5
    private const val EARLY_GAME_START_COUNT = 15
    private const val EARLY_GAME_COUNT_PER_STAGE = 11
    private const val GLOBAL_ENEMY_COUNT_MULTIPLIER = 0.80f
    private const val LATE_GAME_START_RATIO = 0.80f
    private const val LATE_GAME_MAXIMUM_BONUS = 0.10f

}
