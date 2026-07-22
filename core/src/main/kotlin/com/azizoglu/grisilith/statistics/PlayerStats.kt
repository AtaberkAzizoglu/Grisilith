package com.azizoglu.grisilith.statistics

import kotlin.math.roundToInt

class PlayerStats(
    initialBaseScores: Map<StatType, Int> =
        defaultBaseScores()
) {
    companion object {
        const val MINIMUM_SCORE = 0
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

    fun hasteAttackSpeedMultiplier(): Float {
        val score =
            totalScore(
                StatType.HASTE
            )

        /*
         * Yalnızca Spell Focus normal saldırıları:
         * Staff, Wand ve The Book.
         *
         * Haste 10 = x1.00
         * Her puan ±%5 saldırı hızı.
         *
         * Haste 0  = x0.50
         * Haste 10 = x1.00
         * Haste 20 = x1.50
         * Haste 30 = x2.00
         */
        return (
            1f +
                (score - 10) *
                0.05f
            )
            .coerceAtLeast(0.20f)
    }

    fun armorDamageMultiplier(): Float {
        val armorScore =
            totalScore(
                StatType.ARMOR_CLASS
            )

        val damageReduction =
            armorScore.toFloat() /
                (armorScore + 50f)

        return (
            1f - damageReduction
            ).coerceIn(
                0.25f,
                1f
            )
    }

    fun hpRegenerationPerSecond(): Float {
        val score =
            totalScore(
                StatType.HP_REGENERATION
            )

        return (
            score - 10
            )
            .coerceAtLeast(0) *
            0.20f
    }

    fun lifeStealChance(): Float {
        val score =
            totalScore(
                StatType.LIFE_STEAL
            )

        return (
            score - 10
            )
            .coerceAtLeast(0) *
            0.015f
    }

    fun lightWeaponPowerScore(): Int {
        val strength =
            totalScore(
                StatType.STRENGTH
            )

        val dexterity =
            totalScore(
                StatType.DEXTERITY
            )

        return maxOf(
            strength,
            dexterity
        )
    }

    fun starterWeaponDamage(): Int {
        val powerScore =
            lightWeaponPowerScore()

        val multiplier =
            1f +
                (powerScore - 10) *
                0.08f

        return (
            20f * multiplier
            )
            .roundToInt()
            .coerceAtLeast(1)
    }

    fun criticalHitChance(): Float {
        val score =
            totalScore(
                StatType.CRIT_RATE
            )

        return (
            0.05f +
                (score - 10) *
                0.02f
            )
            .coerceIn(
                0f,
                0.95f
            )
    }

    fun criticalDamageMultiplier(): Float {
        val score =
            totalScore(
                StatType.CRIT_DAMAGE
            )

        return (
            1.50f +
                (score - 10) *
                0.05f
            )
            .coerceAtLeast(1f)
    }

    fun enemyCountMultiplier(): Float {
        val score =
            totalScore(
                StatType.ENEMY_COUNT
            )

        return (
            1f +
                (score - 10) *
                0.05f
            )
            .coerceAtLeast(0.25f)
    }

    fun magicPowerMultiplier(): Float {
        val score =
            totalScore(
                StatType.WISDOM
            )

        return (
            1f +
                (score - 10) *
                0.08f
            )
            .coerceAtLeast(0.20f)
    }

    fun hasteCooldownMultiplier(): Float {
        val score =
            totalScore(
                StatType.HASTE
            )

        return (
            1f -
                (score - 10) *
                0.03f
            )
            .coerceIn(
                0.25f,
                2f
            )
    }

    fun luckScore(): Int {
        return totalScore(
            StatType.LUCK
        )
    }

    fun harvestGoldMultiplier(): Float {
        val score =
            totalScore(
                StatType.HARVEST
            )

        return (
            1f +
                (score - 10) *
                0.04f
            )
            .coerceAtLeast(0.20f)
    }

    fun projectileTargetCount(): Int {
        val score =
            totalScore(
                StatType.PIERCING
            )

        val additionalTargets =
            (score - 10)
                .coerceAtLeast(0) /
                5

        return 1 + additionalTargets
    }
}
