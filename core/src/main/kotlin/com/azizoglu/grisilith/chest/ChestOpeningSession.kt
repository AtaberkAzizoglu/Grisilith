package com.azizoglu.grisilith.chest

class ChestOpeningSession {

    var totalChestCount: Int = 0
        private set

    var openedChestCount: Int = 0
        private set

    var remainingDiscardCount: Int = 0
        private set

    var state: ChestOpeningState =
        ChestOpeningState.COMPLETED
        private set

    var currentReward: ChestRewardRoll? = null
        private set

    val currentChestNumber: Int
        get() =
            if (totalChestCount <= 0) {
                0
            } else {
                (openedChestCount + 1)
                    .coerceAtMost(totalChestCount)
            }

    val hasRemainingChests: Boolean
        get() = openedChestCount < totalChestCount

    val canDiscard: Boolean
        get() =
            state == ChestOpeningState.REWARD_SHOWN &&
                remainingDiscardCount > 0

    fun begin(
        chestCount: Int,
        discardAllowance: Int
    ) {
        require(chestCount >= 0) {
            "Chest count cannot be negative."
        }

        require(discardAllowance >= 0) {
            "Discard allowance cannot be negative."
        }

        totalChestCount = chestCount
        openedChestCount = 0
        remainingDiscardCount = discardAllowance
        currentReward = null

        state =
            if (chestCount > 0) {
                ChestOpeningState.LOCKED
            } else {
                ChestOpeningState.COMPLETED
            }
    }

    fun unlockCurrentChest(
        reward: ChestRewardRoll
    ): Boolean {
        if (state != ChestOpeningState.LOCKED) {
            return false
        }

        currentReward = reward
        state = ChestOpeningState.UNLOCKED
        return true
    }

    fun beginReveal(): Boolean {
        if (state != ChestOpeningState.UNLOCKED) {
            return false
        }

        state = ChestOpeningState.REVEALING
        return true
    }

    fun finishReveal(): Boolean {
        if (state != ChestOpeningState.REVEALING) {
            return false
        }

        state = ChestOpeningState.REWARD_SHOWN
        return true
    }

    fun completeWithTake(): Boolean {
        if (state != ChestOpeningState.REWARD_SHOWN) {
            return false
        }

        finishCurrentChest()
        return true
    }

    fun completeWithDiscard(): Boolean {
        if (!canDiscard) {
            return false
        }

        remainingDiscardCount -= 1
        finishCurrentChest()
        return true
    }

    private fun finishCurrentChest() {
        openedChestCount += 1
        currentReward = null

        state =
            if (hasRemainingChests) {
                ChestOpeningState.LOCKED
            } else {
                ChestOpeningState.COMPLETED
            }
    }

    fun completeBatch(chestCount: Int): Boolean {
        if (state != ChestOpeningState.LOCKED || chestCount <= 0) return false
        val safeCount = chestCount.coerceAtMost(totalChestCount - openedChestCount)
        openedChestCount += safeCount
        currentReward = null
        state = if (hasRemainingChests) ChestOpeningState.LOCKED else ChestOpeningState.COMPLETED
        return safeCount > 0
    }

    fun consumeBatchDiscard(): Boolean {
        if (state != ChestOpeningState.LOCKED || remainingDiscardCount <= 0) return false
        remainingDiscardCount -= 1
        return true
    }

    fun reset() {
        totalChestCount = 0
        openedChestCount = 0
        remainingDiscardCount = 0
        currentReward = null
        state = ChestOpeningState.COMPLETED
    }
}
