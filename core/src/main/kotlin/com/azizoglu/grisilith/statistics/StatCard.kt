package com.azizoglu.grisilith.statistics

data class StatCard(
    val type: StatUpgradeType,
    val title: String,
    val amount: Int,
    val description: String
) {
    init {
        require(amount > 0) {
            "Stat card amount must be positive."
        }
    }
}
