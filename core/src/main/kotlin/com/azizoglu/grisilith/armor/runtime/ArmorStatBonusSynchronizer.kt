package com.azizoglu.grisilith.armor.runtime

import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatType

/*
 * Kuşanılmış Armor'un bonuslarını PlayerStats
 * Gear katmanına aktarır.
 */
class ArmorStatBonusSynchronizer(
    private val equippedArmor:
    EquippedArmor,

    private val playerStats:
    PlayerStats
) {

    private var lastArmorId:
        String? =
        null

    fun synchronize() {
        val currentArmor =
            equippedArmor.definition

        val currentArmorId =
            currentArmor
                ?.id
                ?.value

        if (
            currentArmorId ==
            lastArmorId
        ) {
            return
        }

        val accumulatedBonuses =
            StatType.entries
                .associateWith { 0 }
                .toMutableMap()

        currentArmor
            ?.allStatModifiers()
            ?.forEach { modifier ->
                val currentAmount =
                    accumulatedBonuses[
                        modifier.statType
                    ] ?: 0

                accumulatedBonuses[
                    modifier.statType
                ] =
                    currentAmount +
                        modifier.amount
            }

        playerStats.setGearBonuses(
            accumulatedBonuses
        )

        lastArmorId =
            currentArmorId
    }

    fun invalidate() {
        lastArmorId =
            null
    }
}
