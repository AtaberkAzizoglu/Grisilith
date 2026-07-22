package com.azizoglu.grisilith.content

enum class ContentRarity(
    val displayName: String,
    val rank: Int
) {
    COMMON(
        displayName = "Common",
        rank = 1
    ),

    RARE(
        displayName = "Rare",
        rank = 2
    ),

    VERY_RARE(
        displayName = "Very Rare",
        rank = 3
    ),

    LEGENDARY(
        displayName = "Legendary",
        rank = 4
    );

    val isMaximumRarity: Boolean
        get() = this == LEGENDARY

    fun nextRarityOrNull(): ContentRarity? {
        return entries.firstOrNull { rarity ->
            rarity.rank == rank + 1
        }
    }
}
