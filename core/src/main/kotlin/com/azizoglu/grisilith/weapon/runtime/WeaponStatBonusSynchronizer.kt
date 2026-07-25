package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.content.StatModifier
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatType

class WeaponStatBonusSynchronizer(
    private val inventory:
    WeaponInventory,

    private val playerStats:
    PlayerStats
) {

    /*
     * Aynı inventory ve aynı weapon levelleriyle
     * her frame tekrar hesap yapmamak için tutulur.
     */
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

        inventory.allWeapons()
            .forEach { weapon ->
                applyModifiers(
                    modifiers =
                        weapon.definition
                            .passiveStatModifiers,

                    destination =
                        accumulatedBonuses
                )

                applyModifiers(
                    modifiers =
                        weapon.currentLevelStats
                            .grantedStatModifiers,

                    destination =
                        accumulatedBonuses
                )
            }

        playerStats.setWeaponBonuses(
            accumulatedBonuses
        )

        lastInventorySignature =
            currentSignature
    }

    /*
     * Gerektiğinde zorla yeniden hesap yapılmasını
     * sağlar. Yeni run, load veya ileride preset
     * değiştirme akışında kullanılabilir.
     */
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

        return inventory.weaponSlots
            .joinToString(
                separator = "|"
            ) { slot ->

                val weapon =
                    slot.weapon

                if (weapon == null) {
                    "${slot.index}:EMPTY"
                } else {
                    "${slot.index}:" +
                        "${weapon.runtimeId}:" +
                        "${weapon.definition.id.value}:" +
                        "${weapon.level}"
                }
            }
    }
}
