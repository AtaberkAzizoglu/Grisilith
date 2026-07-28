package com.azizoglu.grisilith.preset

import com.azizoglu.grisilith.statistics.StatType

/*
 * Yalnızca Preset ekranında harcanmış ek
 * Stat puanlarını saklar.
 *
 * Character'ın base değerleri burada tutulmaz.
 */
data class PresetStatAllocation(
    val bonusPoints:
    Map<StatType, Int> =
        emptyMap()
) {

    init {
        require(
            bonusPoints.values.all { amount ->
                amount >= 0
            }
        ) {
            "Preset Stat bonuses cannot be negative."
        }
    }

    val spentPointCount: Int
        get() =
            bonusPoints.values.sum()

    fun bonusFor(
        statType:
        StatType
    ): Int {
        return bonusPoints[
            statType
        ] ?: 0
    }

    fun finalScore(
        statType:
        StatType,

        characterBaseScore:
        Int
    ): Int {
        return (
            characterBaseScore +
                bonusFor(
                    statType
                )
            )
            .coerceAtMost(
                PresetConstants
                    .MAXIMUM_PRE_RUN_STAT_SCORE
            )
    }

    /*
     * + butonu için kullanılır.
     */
    fun increase(
        statType:
        StatType,

        characterBaseScore:
        Int,

        pointBudget:
        Int
    ): PresetStatAllocation {
        if (
            spentPointCount >=
            pointBudget
        ) {
            return this
        }

        val currentBonus =
            bonusFor(
                statType
            )

        val currentFinalScore =
            characterBaseScore +
                currentBonus

        if (
            currentFinalScore >=
            PresetConstants
                .MAXIMUM_PRE_RUN_STAT_SCORE
        ) {
            return this
        }

        val updated =
            bonusPoints
                .toMutableMap()

        updated[
            statType
        ] =
            currentBonus +
                1

        return PresetStatAllocation(
            bonusPoints =
                updated
        )
    }

    /*
     * - butonu için kullanılır.
     *
     * Yalnızca Preset ile verilmiş puanı geri alır;
     * Character base değerinin altına inemez.
     */
    fun decrease(
        statType:
        StatType
    ): PresetStatAllocation {
        val currentBonus =
            bonusFor(
                statType
            )

        if (currentBonus <= 0) {
            return this
        }

        val updated =
            bonusPoints
                .toMutableMap()

        if (currentBonus == 1) {
            updated.remove(
                statType
            )
        } else {
            updated[
                statType
            ] =
                currentBonus -
                    1
        }

        return PresetStatAllocation(
            bonusPoints =
                updated
        )
    }

    fun finalScores(
        characterBaseStats:
        Map<StatType, Int>
    ): Map<StatType, Int> {
        return StatType.entries
            .associateWith { statType ->
                finalScore(
                    statType =
                        statType,

                    characterBaseScore =
                        characterBaseStats[
                            statType
                        ] ?: 0
                )
            }
    }
}
