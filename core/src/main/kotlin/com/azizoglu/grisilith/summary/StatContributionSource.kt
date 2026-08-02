package com.azizoglu.grisilith.summary

enum class StatContributionSource(
    val displayName: String
) {
    PRESET_OR_BASE("Preset / Base"),
    STAGE_REWARDS("Stage Stat Rewards"),
    ITEMS("Items"),
    WEAPONS("Weapons"),
    ARMOR("Armor"),
    WEAPON_TAMGA("Weapon Tamga"),
    ARMOR_TAMGA("Armor Tamga"),
    OTHER("Other")
}
