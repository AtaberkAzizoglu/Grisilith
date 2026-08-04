package com.azizoglu.grisilith.weapon.summon

enum class IvySummonChoice(
    val displayName: String,
    val description: String
) {

    SUMMON_ANIMALS(
        displayName =
            "SUMMON ANIMALS",

        description =
            "Summons two durable cows that fight beside the player."
    ),

    SUMMON_DEMONS(
        displayName =
            "SUMMON DEMONS",

        description =
            "Summons one Imp with balanced health and damage."
    ),

    CALL_THE_KNIGHT(
        displayName =
            "CALL THE KNIGHT",

        description =
            "Calls a hostile Knight Spirit that can attack enemies and the player."
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
