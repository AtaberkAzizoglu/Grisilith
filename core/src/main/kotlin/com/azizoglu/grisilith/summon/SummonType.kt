package com.azizoglu.grisilith.summon

enum class SummonType(
    val displayName: String,
    val isFriendlyToPlayer: Boolean,
    val defaultCount: Int
) {

    COW(
        displayName =
            "Cow",

        isFriendlyToPlayer =
            true,

        defaultCount =
            2
    ),

    IMP(
        displayName =
            "Imp",

        isFriendlyToPlayer =
            true,

        defaultCount =
            1
    ),

    KNIGHT_SPIRIT(
        displayName =
            "Knight Spirit",

        isFriendlyToPlayer =
            false,

        defaultCount =
            1
    )
}
