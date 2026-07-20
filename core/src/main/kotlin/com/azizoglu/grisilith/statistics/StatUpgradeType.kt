package com.azizoglu.grisilith.statistics

enum class StatUpgradeType(
    val statType: StatType
) {
    MAX_HEALTH(
        StatType.MAX_HP
    ),

    MOVEMENT_SPEED(
        StatType.MOVEMENT_SPEED
    ),

    ATTACK_SPEED(
        StatType.ATTACK_SPEED
    )
}
