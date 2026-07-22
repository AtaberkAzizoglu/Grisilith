package com.azizoglu.grisilith.content

import com.azizoglu.grisilith.statistics.StatType

data class StatModifier(
    val statType: StatType,
    val amount: Int
) {
    init {
        require(amount != 0) {
            "Stat modifier amount cannot be zero."
        }
    }
}
