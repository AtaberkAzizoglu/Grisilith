package com.azizoglu.grisilith.chest

import com.azizoglu.grisilith.item.definition.ItemDefinition
import com.azizoglu.grisilith.statistics.StatType

/*
 * Sandık ödülü, sandık toplandığı anda
 * tamamen belirlenir.
 *
 * STAT ödülünde:
 * - statType doludur
 * - statAmount pozitif olur
 *
 * ITEM ödülünde:
 * - itemDefinition doludur
 */
data class ChestRewardRoll(
    val rewardType:
    ChestRewardType,

    val statType:
    StatType? = null,

    val statAmount:
    Int = 0,

    val itemDefinition:
    ItemDefinition? = null
) {

    init {
        when (rewardType) {
            ChestRewardType.STAT -> {
                require(
                    statType != null
                ) {
                    "STAT chest reward requires a StatType."
                }

                require(
                    statAmount > 0
                ) {
                    "STAT chest reward amount must be positive."
                }

                require(
                    itemDefinition == null
                )
            }

            ChestRewardType.ITEM -> {
                require(
                    itemDefinition != null
                ) {
                    "ITEM chest reward requires an ItemDefinition."
                }

                require(
                    statType == null
                )

                require(
                    statAmount == 0
                )
            }
        }
    }

    val title: String
        get() =
            when (rewardType) {
                ChestRewardType.STAT -> {
                    "${statType?.displayName ?: "Stat"} +$statAmount"
                }

                ChestRewardType.ITEM -> {
                    itemDefinition
                        ?.displayName
                        ?: "Item"
                }
            }

    val description: String
        get() =
            when (rewardType) {
                ChestRewardType.STAT -> {
                    "Permanently increases this Stat " +
                        "for the current run."
                }

                ChestRewardType.ITEM -> {
                    itemDefinition
                        ?.description
                        ?: "A mysterious Item."
                }
            }
}
