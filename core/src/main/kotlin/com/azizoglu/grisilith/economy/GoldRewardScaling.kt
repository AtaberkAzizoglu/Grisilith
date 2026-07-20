package com.azizoglu.grisilith.economy

import com.azizoglu.grisilith.enemy.EnemyType

object GoldRewardScaling {

    private const val REWARD_GROWTH_PER_STAGE = 0.035f

    fun rewardFor(
        enemyType: EnemyType,
        stage: Int
    ): Float {
        val safeStage = stage.coerceAtLeast(1)

        val baseReward = when (enemyType) {
            EnemyType.NORMAL -> 1f
            EnemyType.UNIQUE -> 5f
            EnemyType.MINI_BOSS -> 25f
            EnemyType.FINAL_BOSS -> 100f
        }

        val stageMultiplier =
            1f +
                (safeStage - 1) *
                REWARD_GROWTH_PER_STAGE

        return baseReward * stageMultiplier
    }
}
