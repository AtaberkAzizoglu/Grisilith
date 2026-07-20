package com.azizoglu.grisilith.combat

class HealthComponent(
    maximumHealth: Int
) {
    init {
        require(maximumHealth > 0) {
            "Maximum health must be greater than zero."
        }
    }

    var maximumHealth: Int = maximumHealth
        private set

    var currentHealth: Int = maximumHealth
        private set

    val isDead: Boolean
        get() = currentHealth <= 0

    val healthRatio: Float
        get() {
            if (maximumHealth <= 0) {
                return 0f
            }

            return currentHealth.toFloat() /
                maximumHealth.toFloat()
        }

    fun takeDamage(amount: Int): Int {
        if (amount <= 0 || isDead) {
            return 0
        }

        val previousHealth = currentHealth

        currentHealth =
            (currentHealth - amount)
                .coerceAtLeast(0)

        return previousHealth - currentHealth
    }

    fun heal(amount: Int): Int {
        if (amount <= 0 || isDead) {
            return 0
        }

        val previousHealth = currentHealth

        currentHealth =
            (currentHealth + amount)
                .coerceAtMost(maximumHealth)

        return currentHealth - previousHealth
    }

    fun increaseMaximumHealth(
        amount: Int,
        healAddedHealth: Boolean = true
    ) {
        if (amount <= 0) {
            return
        }

        maximumHealth += amount

        if (healAddedHealth && !isDead) {
            currentHealth =
                (currentHealth + amount)
                    .coerceAtMost(maximumHealth)
        }
    }
}
