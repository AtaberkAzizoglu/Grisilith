package com.azizoglu.grisilith.combat

class HealthComponent(
    val maximumHealth: Int
) {
    init {
        require(maximumHealth > 0) {
            "Maximum health must be greater than zero."
        }
    }

    var currentHealth: Int = maximumHealth
        private set

    val isDead: Boolean
        get() = currentHealth <= 0

    val healthRatio: Float
        get() = currentHealth.toFloat() / maximumHealth.toFloat()

    fun takeDamage(amount: Int): Int {
        if (amount <= 0 || isDead) {
            return 0
        }

        val previousHealth = currentHealth
        currentHealth = (currentHealth - amount).coerceAtLeast(0)

        return previousHealth - currentHealth
    }

    fun heal(amount: Int): Int {
        if (amount <= 0 || isDead) {
            return 0
        }

        val previousHealth = currentHealth
        currentHealth = (currentHealth + amount).coerceAtMost(maximumHealth)

        return currentHealth - previousHealth
    }
}
