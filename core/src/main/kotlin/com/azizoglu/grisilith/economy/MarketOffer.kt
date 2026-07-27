package com.azizoglu.grisilith.economy

import com.azizoglu.grisilith.item.definition.ItemDefinition
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.weapon.definition.WeaponDefinition

data class MarketOffer(
    val productType:
    MarketProductType,

    /*
     * Weapon için level.
     *
     * Item ve Stat tekliflerinde şimdilik 1.
     */
    val productLevel: Int,

    val title: String,

    val description: String,

    val price: Int,

    /*
     * Yalnızca WEAPON tekliflerinde doludur.
     */
    val weaponDefinition:
    WeaponDefinition? = null,

    /*
     * Yalnızca ITEM tekliflerinde doludur.
     */
    val itemDefinition:
    ItemDefinition? = null,

    /*
     * Yalnızca STAT tekliflerinde doludur.
     */
    val statType:
    StatType? = null,

    /*
     * Stat kartının vereceği puan.
     */
    val statAmount: Int = 0
) {

    init {
        require(
            productLevel >= 1
        )

        require(
            title.isNotBlank()
        )

        require(
            description.isNotBlank()
        )

        require(
            price >= 1
        )

        when (productType) {
            MarketProductType.WEAPON -> {
                require(
                    weaponDefinition != null
                ) {
                    "Weapon offer must contain a WeaponDefinition."
                }
            }

            MarketProductType.ITEM -> {
                require(
                    itemDefinition != null
                ) {
                    "Item offer must contain an ItemDefinition."
                }
            }

            MarketProductType.STAT -> {
                require(
                    statType != null
                ) {
                    "Stat offer must contain a StatType."
                }

                require(
                    statAmount > 0
                ) {
                    "Stat offer amount must be positive."
                }
            }
        }
    }
}
