package com.azizoglu.grisilith.weapon.summon

enum class IvySummonChoice(
    val displayName: String,
    val description: String
) {

    SUMMON_ANIMALS(
        displayName =
            "SUMMON ANIMALS",

        description =
            "Summons durable allied cows: two at Level 3, three at Level 5."
    ),

    SUMMON_DEMONS(
        displayName =
            "SUMMON DEMONS",

        description =
            "Summons fragile allied Imps with powerful ranged Fire attacks: one at Level 3, two at Level 5."
    ),

    CALL_THE_KNIGHT(
        displayName =
            "CALL THE OATHBREAKER",

        description =
            "Calls a neutral Oathbreaker."
    );

    companion object {

        fun fromSavedName(
            savedName: String?
        ): IvySummonChoice? {

            val normalized =
                savedName
                    ?.trim()
                    ?.takeIf {
                        it.isNotEmpty()
                    }
                    ?: return null

            return entries
                .firstOrNull {
                    it.name ==
                        normalized
                }
        }
    }
}
