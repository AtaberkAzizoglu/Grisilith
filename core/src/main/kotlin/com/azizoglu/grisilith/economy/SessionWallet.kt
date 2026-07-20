package com.azizoglu.grisilith.economy

import kotlin.math.floor

class SessionWallet {

    private var exactGoldCoins = 0f

    val goldCoins: Int
        get() = floor(exactGoldCoins).toInt()

    fun addGold(amount: Float) {
        if (amount <= 0f) {
            return
        }

        exactGoldCoins += amount
    }

    fun spendGold(amount: Int): Boolean {
        if (amount <= 0 || amount > goldCoins) {
            return false
        }

        exactGoldCoins -= amount.toFloat()
        return true
    }

    fun reset() {
        exactGoldCoins = 0f
    }
}
