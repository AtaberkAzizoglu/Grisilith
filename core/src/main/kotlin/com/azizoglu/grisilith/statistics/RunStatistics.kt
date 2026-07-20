package com.azizoglu.grisilith.statistics

class RunStatistics {

    var totalDamageDealt: Long = 0L
        private set

    var totalDamageTaken: Long = 0L
        private set

    var totalEnemiesKilled: Int = 0
        private set

    var totalGoldCollected: Float = 0f
        private set

    private val weaponDamage =
        mutableMapOf<String, Long>()

    val weaponDamageStatistics: Map<String, Long>
        get() = weaponDamage.toMap()

    fun recordDamageDealt(
        amount: Int,
        weaponName: String
    ) {
        if (amount <= 0) {
            return
        }

        totalDamageDealt += amount

        weaponDamage[weaponName] =
            weaponDamage.getOrDefault(
                weaponName,
                0L
            ) + amount
    }

    fun recordDamageTaken(amount: Int) {
        if (amount <= 0) {
            return
        }

        totalDamageTaken += amount
    }

    fun recordEnemyKilled() {
        totalEnemiesKilled++
    }

    fun recordGoldCollected(amount: Float) {
        if (amount <= 0f) {
            return
        }

        totalGoldCollected += amount
    }
}
