package com.azizoglu.grisilith.combat

class HealthComponent(
    maximumHealth: Int,
    var onHealingApplied: ((Int, HealingVisualType) -> Unit)? = null
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

    fun heal(
        amount: Int,
        visualType: HealingVisualType = HealingVisualType.HEAL
    ): Int {
        if (amount <= 0 || isDead) {
            return 0
        }

        val previousHealth = currentHealth

        currentHealth =
            (currentHealth + amount)
                .coerceAtMost(maximumHealth)

        val applied = currentHealth - previousHealth
        if (applied > 0) onHealingApplied?.invoke(applied, visualType)
        return applied
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

    fun setMaximumHealth(
        newMaximumHealth: Int,
        healAddedHealth: Boolean = true
    ) {
        if (newMaximumHealth <= 0) {
            return
        }

        val previousMaximumHealth =
            maximumHealth

        val maximumHealthDifference =
            newMaximumHealth -
                previousMaximumHealth

        maximumHealth =
            newMaximumHealth

        when {

            maximumHealthDifference > 0 &&
                healAddedHealth &&
                !isDead -> {

                currentHealth =
                    (
                        currentHealth +
                            maximumHealthDifference
                        )
                        .coerceAtMost(
                            maximumHealth
                        )
            }

            maximumHealthDifference < 0 -> {
                currentHealth =
                    currentHealth.coerceAtMost(
                        maximumHealth
                    )
            }
        }
    }

    fun restoreCurrentHealth(
        restoredHealth: Int
    ) {
        currentHealth =
            restoredHealth
                .coerceIn(
                    1,
                    maximumHealth
                )
    }
}
