package com.azizoglu.grisilith.summon

enum class SummonType(
    val displayName: String,
    val faction: SummonCombatFaction,
    val defaultCount: Int
) {

    COW(
        displayName =
            "Cow",

        faction =
            SummonCombatFaction.PLAYER_ALLY,

        defaultCount =
            2
    ),

    IMP(
        displayName =
            "Imp",

        faction =
            SummonCombatFaction.PLAYER_ALLY,

        defaultCount =
            1
    ),

    KNIGHT_SPIRIT(
        displayName =
            "Knight Spirit",

        faction =
            SummonCombatFaction.ROGUE,

        defaultCount =
            1
    ),

    TOAD(
        displayName = "Toad",
        faction = SummonCombatFaction.PLAYER_ALLY,
        defaultCount = 1
    );

    val isFriendlyToPlayer: Boolean
        get() =
            faction ==
                SummonCombatFaction.PLAYER_ALLY
}
