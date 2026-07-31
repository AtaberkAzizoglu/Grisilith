package com.azizoglu.grisilith.reward

import com.azizoglu.grisilith.config.GameDifficulty
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.profile.PlayerProfileRepository
import com.badlogic.gdx.math.MathUtils

class RunRewardTracker(
    private val difficulty: GameDifficulty
) {

    companion object {
        private const val MINI_BOSS_COIN_CHANCE =
            0.05f
    }

    private val pendingHardFinalBossIds =
        mutableSetOf<String>()

    private var rewardsCommitted =
        false

    var grisilithCoinsEarned: Int =
        0
        private set

    val maximumGrisilithCoins: Int
        get() =
            difficulty.maximumGrisilithCoinsPerRun

    val remainingGrisilithCoinCapacity: Int
        get() =
            (
                maximumGrisilithCoins -
                    grisilithCoinsEarned
                )
                .coerceAtLeast(0)

    fun onEnemyKilled(
        enemyType: EnemyType,
        finalBossId: String? = null
    ): Int {
        return when (enemyType) {
            EnemyType.MINI_BOSS ->
                tryAwardMiniBossCoin()

            EnemyType.FINAL_BOSS ->
                awardFinalBossCoins(
                    finalBossId =
                        requireNotNull(finalBossId) {
                            "A stable finalBossId is required for FINAL_BOSS rewards."
                        }
                )

            EnemyType.NORMAL,
            EnemyType.UNIQUE ->
                0
        }
    }

    fun commitToProfile(): Boolean {
        if (rewardsCommitted) {
            return false
        }

        PlayerProfileRepository.applyRunRewards(
            grisilithCoins =
                grisilithCoinsEarned,

            defeatedHardFinalBossIds =
                pendingHardFinalBossIds
        )

        rewardsCommitted =
            true

        return true
    }

    private fun tryAwardMiniBossCoin(): Int {
        if (remainingGrisilithCoinCapacity <= 0) {
            return 0
        }

        if (!MathUtils.randomBoolean(MINI_BOSS_COIN_CHANCE)) {
            return 0
        }

        return addCoinsRespectingRunLimit(1)
    }

    private fun awardFinalBossCoins(
        finalBossId: String
    ): Int {
        val normalizedBossId =
            finalBossId.trim()

        require(normalizedBossId.isNotEmpty()) {
            "finalBossId cannot be blank."
        }

        val requestedReward =
            when (difficulty) {
                GameDifficulty.EASY,
                GameDifficulty.Normal ->
                    1

                GameDifficulty.HARD -> {
                    val wasPreviouslyDefeated =
                        PlayerProfileRepository
                            .profile
                            .hasDefeatedHardFinalBoss(
                                normalizedBossId
                            ) ||
                            normalizedBossId in
                            pendingHardFinalBossIds

                    if (wasPreviouslyDefeated) {
                        3
                    } else {
                        pendingHardFinalBossIds.add(
                            normalizedBossId
                        )

                        5
                    }
                }
            }

        return addCoinsRespectingRunLimit(
            requestedReward
        )
    }

    private fun addCoinsRespectingRunLimit(
        requestedAmount: Int
    ): Int {
        if (requestedAmount <= 0) {
            return 0
        }

        val awardedAmount =
            requestedAmount.coerceAtMost(
                remainingGrisilithCoinCapacity
            )

        grisilithCoinsEarned +=
            awardedAmount

        return awardedAmount
    }
}
