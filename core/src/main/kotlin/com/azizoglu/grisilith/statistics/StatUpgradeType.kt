package com.azizoglu.grisilith.statistics

enum class StatUpgradeType(
    val statType: StatType
) {
    MAX_HEALTH(
        StatType.MAX_HP
    ),

    ARMOR_CLASS(
        StatType.ARMOR_CLASS
    ),

    STRENGTH(
        StatType.STRENGTH
    ),

    DEXTERITY(
        StatType.DEXTERITY
    ),

    WISDOM(
        StatType.WISDOM
    ),

    COOLDOWN(
        StatType.COOLDOWN
    ),

    ATTACK_SPEED(
        StatType.ATTACK_SPEED
    ),

    CRIT_RATE(
        StatType.CRIT_RATE
    ),

    CRIT_DAMAGE(
        StatType.CRIT_DAMAGE
    ),

    MOVEMENT_SPEED(
        StatType.MOVEMENT_SPEED
    ),

    LUCK(
        StatType.LUCK
    ),

    HARVEST(
        StatType.HARVEST
    ),

    PIERCING(
        StatType.PIERCING
    ),

    ENEMY_COUNT(
        StatType.ENEMY_COUNT
    ),

    HP_REGENERATION(
        StatType.HP_REGENERATION
    ),

    LIFE_STEAL(
        StatType.LIFE_STEAL
    )
}
