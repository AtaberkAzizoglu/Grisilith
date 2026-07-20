package com.azizoglu.grisilith.statistics

object StatCardGenerator {

    fun generateCards(): List<StatCard> {
        return listOf(
            StatCard(
                type = StatUpgradeType.MAX_HEALTH,
                title = "Max Health",
                description = "+15 maximum health"
            ),
            StatCard(
                type = StatUpgradeType.MOVEMENT_SPEED,
                title = "Movement Speed",
                description = "+8% movement speed"
            ),
            StatCard(
                type = StatUpgradeType.ATTACK_SPEED,
                title = "Attack Speed",
                description = "+10% attack speed"
            )
        ).shuffled()
    }
}
