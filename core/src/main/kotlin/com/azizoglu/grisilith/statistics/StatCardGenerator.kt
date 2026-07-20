package com.azizoglu.grisilith.statistics

object StatCardGenerator {

    fun generateCards(): List<StatCard> {
        return listOf(
            StatCard(
                type = StatUpgradeType.MAX_HEALTH,
                title = "Max Health",
                description = "+1 Max HP score"
            ),

            StatCard(
                type = StatUpgradeType.MOVEMENT_SPEED,
                title = "Movement Speed",
                description = "+1 Movement Speed score"
            ),

            StatCard(
                type = StatUpgradeType.ATTACK_SPEED,
                title = "Attack Speed",
                description = "+1 Attack Speed score"
            )
        ).shuffled()
    }
}
