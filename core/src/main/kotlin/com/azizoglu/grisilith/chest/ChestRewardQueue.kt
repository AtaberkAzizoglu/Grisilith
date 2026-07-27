package com.azizoglu.grisilith.chest

class ChestRewardQueue {

    private val pendingRewards =
        mutableListOf<ChestRewardRoll>()

    val rewardCount: Int
        get() =
            pendingRewards.size

    val isEmpty: Boolean
        get() =
            pendingRewards.isEmpty()

    val isNotEmpty: Boolean
        get() =
            pendingRewards.isNotEmpty()

    fun addReward(
        reward:
        ChestRewardRoll
    ) {
        pendingRewards.add(
            reward
        )
    }

    fun peekFirst():
        ChestRewardRoll? {

        return pendingRewards
            .firstOrNull()
    }

    fun removeFirst():
        ChestRewardRoll? {

        if (
            pendingRewards.isEmpty()
        ) {
            return null
        }

        return pendingRewards
            .removeAt(
                0
            )
    }

    fun allRewards():
        List<ChestRewardRoll> {

        return pendingRewards
            .toList()
    }

    fun clear() {
        pendingRewards.clear()
    }
}
