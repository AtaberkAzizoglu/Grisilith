package com.azizoglu.grisilith.save

import java.util.HashSet

data class RunSaveItemEffectState(
    val stage: Int = -1,
    val hunterRewardBossKills: Int = 0,
    val cashOutConsumedItems: Int = 0,
    val hitMeHarderStacks: Int = 0,
    val bestiaryKinds: HashSet<String> = hashSetOf(),
    val predestinationDamageTypeName: String? = null,
    val bountyEnemyTypeName: String? = null,
    val bountyEnemyKindId: String? = null,
    val bountyEnemyDisplayName: String? = null,
    val secondHeartAvailable: Boolean = true
)
