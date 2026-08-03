package com.azizoglu.grisilith.save

import java.util.HashSet

data class RunSaveRewardState(
    val grisilithCoinsEarned:
    Int =
        0,

    val defeatedHardFinalBossIds:
    HashSet<String> =
        hashSetOf()
)
