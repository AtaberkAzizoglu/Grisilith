package com.azizoglu.grisilith.profile

class PlayerProfile(
    initialGrisilithCoins: Int = 0,
    initialPurchasedStatPoints: Int = 0
) {

    var grisilithCoins: Int =
        initialGrisilithCoins
            .coerceAtLeast(0)
        private set

    var purchasedStatPoints: Int =
        initialPurchasedStatPoints
            .coerceAtLeast(0)
        private set

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
}
