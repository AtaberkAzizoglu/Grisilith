package com.azizoglu.grisilith.economy

import com.azizoglu.grisilith.stage.StageType

object StageGoldRewardCalculator {

    fun rewardFor(
        completedStage: Int,
        stageType: StageType
    ): Float {
        val safeStage =
            completedStage.coerceAtLeast(1)

        return when (stageType) {
            StageType.NORMAL -> {
                4f + safeStage * 0.8f
            }

            StageType.MINI_BOSS -> {
                15f + safeStage * 1.5f
            }

            StageType.FINAL_BOSS -> {
                50f + safeStage * 3f
            }
        }
    }
}
