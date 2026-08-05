package com.azizoglu.grisilith.armor.runtime

import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatType

/*
 * Kuşanılmış Armor'un:
 * - Base HP / Base Armor değerlerini ayrı alanlara,
 * - diğer modifier'larını Gear bonus katmanına
 * aktarır.
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

        if (currentArmorId == lastArmorId) {
            return
        }

        playerStats.setArmorBaseValues(
            baseHealth =
                currentArmor?.baseHealth ?: 1,
            baseArmor =
                currentArmor?.baseArmor ?: 0
        )

        val accumulatedBonuses =
            StatType.entries
                .associateWith { 0 }
                .toMutableMap()

        currentArmor
            ?.allStatModifiers()
            ?.forEach { modifier ->
                accumulatedBonuses[
                    modifier.statType
                ] =
                    (accumulatedBonuses[
                        modifier.statType
                    ] ?: 0) +
                        modifier.amount
            }

        playerStats.setGearBonuses(
            accumulatedBonuses
        )

        lastArmorId =
            currentArmorId
    }

    fun invalidate() {
        lastArmorId = null
    }
}
