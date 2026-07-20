package com.azizoglu.grisilith.statistics

class RunStatistics {

    var totalDamageDealt: Long = 0L
        private set

    var totalEnemiesKilled: Int = 0
        private set

    fun recordDamageDealt(amount: Int) {
        if (amount > 0) {
            totalDamageDealt += amount
        }
    }

    fun recordEnemyKilled() {
        totalEnemiesKilled++
    }
}
