package com.azizoglu.grisilith.profile

data class PlayerProfileSnapshot(
    val version: Int =
        CURRENT_VERSION,

    val grisilithCoins: Int =
        0,

    val purchasedStatPoints: Int =
        0,

    val unlockedIds: Set<String> =
        emptySet(),

    val defeatedHardFinalBossIds: Set<String> =
        emptySet()
) {

    companion object {
        const val CURRENT_VERSION =
            3
    }
}
