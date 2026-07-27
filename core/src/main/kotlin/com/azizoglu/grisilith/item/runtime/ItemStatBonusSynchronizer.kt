package com.azizoglu.grisilith.item.runtime

import com.azizoglu.grisilith.content.StatModifier
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatType

class ItemStatBonusSynchronizer(
    private val inventory:
    ItemInventory,

    private val playerStats:
    PlayerStats
) {

    private var lastInventorySignature:
        String? = null

    fun synchronize() {
        val currentSignature =
            createInventorySignature()

        if (
            currentSignature ==
            lastInventorySignature
        ) {
            return
        }

        val accumulatedBonuses =
            StatType.entries
                .associateWith { 0 }
                .toMutableMap()

        inventory.allItems()
            .forEach { item ->
                applyModifiers(
                    modifiers =
                        item.definition
                            .statModifiers,

                    destination =
                        accumulatedBonuses
                )
            }

        playerStats.setItemBonuses(
            accumulatedBonuses
        )

        lastInventorySignature =
            currentSignature
    }

    fun invalidate() {
        lastInventorySignature = null
    }

    private fun applyModifiers(
        modifiers: List<StatModifier>,
        destination:
        MutableMap<StatType, Int>
    ) {
        modifiers.forEach { modifier ->
            val currentAmount =
                destination[
                    modifier.statType
                ] ?: 0

            destination[
                modifier.statType
            ] =
                currentAmount +
                    modifier.amount
        }
    }

    private fun createInventorySignature():
        String {

        return inventory.allItems()
            .joinToString(
                separator = "|"
            ) { item ->
                "${item.runtimeId}:" +
                    item.definition.id.value
            }
    }
}
