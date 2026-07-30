package com.azizoglu.grisilith.profile

class PlayerProfile(
    initialGrisilithCoins: Int = 0,
    initialPurchasedStatPoints: Int = 0,
    initialUnlockedIds: Set<String> =
        emptySet()
) {

    var grisilithCoins: Int =
        initialGrisilithCoins
            .coerceAtLeast(0)
        private set

    var purchasedStatPoints: Int =
        initialPurchasedStatPoints
            .coerceAtLeast(0)
        private set

    private val mutableUnlockedIds =
        initialUnlockedIds
            .filter {
                it.isNotBlank()
            }
            .toMutableSet()

    val unlockedIds:
        Set<String>
        get() =
            mutableUnlockedIds
                .toSet()

    fun addGrisilithCoins(
        amount: Int
    ) {
        if (amount <= 0) {
            return
        }

        grisilithCoins +=
            amount
    }

    fun canSpendGrisilithCoins(
        amount: Int
    ): Boolean {

        return amount > 0 &&
            grisilithCoins >= amount
    }

    fun spendGrisilithCoins(
        amount: Int
    ): Boolean {

        if (
            !canSpendGrisilithCoins(
                amount
            )
        ) {
            return false
        }

        grisilithCoins -=
            amount

        return true
    }

    fun addPurchasedStatPoints(
        amount: Int
    ) {
        if (amount <= 0) {
            return
        }

        purchasedStatPoints +=
            amount
    }

    fun isUnlocked(
        unlockId: String
    ): Boolean {

        return unlockId in
            mutableUnlockedIds
    }

    fun unlock(
        unlockId: String
    ): Boolean {

        if (unlockId.isBlank()) {
            return false
        }

        return mutableUnlockedIds.add(
            unlockId
        )
    }
}
