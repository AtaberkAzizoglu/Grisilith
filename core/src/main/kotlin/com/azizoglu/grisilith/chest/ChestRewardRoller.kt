package com.azizoglu.grisilith.chest

import kotlin.random.Random

object ChestRewardRoller {

    fun rollReward(
        luckScore: Int,
        random:
        Random = Random.Default
    ): ChestRewardRoll {
        val rewardType =
            ChestRewardTypeCalculator
                .rollRewardType(
                    luckScore =
                        luckScore,

                    random =
                        random
                )

        return ChestRewardRoll(
            rewardType =
                rewardType
        )
    }
}
