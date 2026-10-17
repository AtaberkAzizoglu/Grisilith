package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.statistics.StatType

data class ChestCharacterStatRow(
    val statType: StatType,
    val label: String,
    val value: String
)
