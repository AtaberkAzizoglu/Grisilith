package com.azizoglu.grisilith.tamga.runtime

import com.azizoglu.grisilith.statistics.StatType

enum class HolyLightsMode(
    val statType: StatType
) {

    ARMOR_CLASS(
        StatType.ARMOR_CLASS
    ),

    WISDOM(
        StatType.WISDOM
    ),

    LIFE_STEAL(
        StatType.LIFE_STEAL
    );

    companion object {

        fun fromStatType(
            statType: StatType
        ): HolyLightsMode? {

            return entries.firstOrNull { mode ->
                mode.statType == statType
            }
        }
    }
}
