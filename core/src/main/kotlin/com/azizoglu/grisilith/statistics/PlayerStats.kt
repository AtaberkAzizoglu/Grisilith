package com.azizoglu.grisilith.statistics

class PlayerStats(
    initialBaseScores: Map<StatType, Int> =
        defaultBaseScores()
) {
    companion object {
        const val MINIMUM_SCORE = 1
        const val PRE_RUN_MAXIMUM_SCORE = 20
        const val RUN_MAXIMUM_SCORE = 30

        private fun defaultBaseScores(): Map<StatType, Int> {
            return StatType.entries.associateWith {
                10
            }
        }
    }

    private val baseScores =
        mutableMapOf<StatType, Int>()

    private val gearBonuses =
        mutableMapOf<StatType, Int>()

    private val runBonuses =
        mutableMapOf<StatType, Int>()

    init {
        StatType.entries.forEach { statType ->
            val initialValue =
                initialBaseScores[statType] ?: 10

            baseScores[statType] =
                initialValue.coerceIn(
                    MINIMUM_SCORE,
                    PRE_RUN_MAXIMUM_SCORE
                )

            gearBonuses[statType] = 0
            runBonuses[statType] = 0
        }
    }

    fun baseScore(
        statType: StatType
    ): Int {
        return baseScores[statType] ?: MINIMUM_SCORE
    }

    fun gearBonus(
        statType: StatType
    ): Int {
        return gearBonuses[statType] ?: 0
    }

    fun runBonus(
        statType: StatType
    ): Int {
        return runBonuses[statType] ?: 0
    }

    fun totalScore(
        statType: StatType
    ): Int {
        return (
            baseScore(statType) +
                gearBonus(statType) +
                runBonus(statType)
            )
            .coerceIn(
                MINIMUM_SCORE,
                RUN_MAXIMUM_SCORE
            )
    }

    fun addRunBonus(
        statType: StatType,
        amount: Int
    ): Int {
        if (amount <= 0) {
            return 0
        }

        val previousTotal =
            totalScore(statType)

        val currentRunBonus =
            runBonus(statType)

        runBonuses[statType] =
            currentRunBonus + amount

        val newTotal =
            totalScore(statType)

        return newTotal - previousTotal
    }

    fun addGearBonus(
        statType: StatType,
        amount: Int
    ): Int {
        if (amount == 0) {
            return 0
        }

        val previousTotal =
            totalScore(statType)

        val currentGearBonus =
            gearBonus(statType)

        gearBonuses[statType] =
            currentGearBonus + amount

        val newTotal =
            totalScore(statType)

        return newTotal - previousTotal
    }

    fun setBaseScore(
        statType: StatType,
        score: Int
    ) {
        baseScores[statType] =
            score.coerceIn(
                MINIMUM_SCORE,
                PRE_RUN_MAXIMUM_SCORE
            )
    }

    fun clearRunBonuses() {
        StatType.entries.forEach { statType ->
            runBonuses[statType] = 0
        }
    }

    /*
     * Geçici oynanış formülleri.
     * Denge matematiğini ileride ayrı calculator
     * sınıflarına taşıyacağız.
     */

    fun calculatedMaximumHealth(): Int {
        val score =
            totalScore(StatType.MAX_HP)

        return (
            100 +
                (score - 10) * 10
            ).coerceAtLeast(10)
    }

    fun calculatedMovementSpeed(): Float {
        val score =
            totalScore(StatType.MOVEMENT_SPEED)

        val multiplier =
            1f + (score - 10) * 0.04f

        return (
            260f * multiplier
            ).coerceAtLeast(80f)
    }

    fun attackSpeedMultiplier(): Float {
        val score =
            totalScore(StatType.ATTACK_SPEED)

        return (
            1f + (score - 10) * 0.05f
            ).coerceAtLeast(0.20f)
    }
}
