package com.azizoglu.grisilith.save

data class RunSaveRewardState(
    val grisilithCoinsEarned: Int = 0,
    val defeatedHardFinalBossIds: Set<String> = emptySet()
)
