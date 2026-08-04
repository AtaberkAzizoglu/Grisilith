package com.azizoglu.grisilith.summon

import com.azizoglu.grisilith.weapon.summon.IvySummonChoice
import com.badlogic.gdx.math.MathUtils

class SummonSpawnScheduler(
    val sourceWeaponRuntimeId: String,
    val choice: IvySummonChoice
) {

    var remainingSpawnDelaySeconds: Float =
        randomRespawnDelay()
        private set

    var hasLivingWave: Boolean =
        false
        private set

    fun updateDelay(
        delta: Float
    ) {

        if (
            hasLivingWave ||
            delta <= 0f
        ) {
            return
        }

        remainingSpawnDelaySeconds =
            (
                remainingSpawnDelaySeconds -
                    delta
                )
                .coerceAtLeast(
                    0f
                )
    }

    val isReadyToSpawn: Boolean
        get() =
            !hasLivingWave &&
                remainingSpawnDelaySeconds <=
                0f

    fun markWaveSpawned() {
        hasLivingWave =
            true
    }

    fun markWaveEnded() {
        hasLivingWave =
            false

        remainingSpawnDelaySeconds =
            randomRespawnDelay()
    }

    private fun randomRespawnDelay():
        Float {

        return MathUtils.random(
            MINIMUM_RESPAWN_SECONDS,
            MAXIMUM_RESPAWN_SECONDS
        )
    }

    companion object {

        const val SUMMON_LIFETIME_SECONDS =
            10f

        const val MINIMUM_RESPAWN_SECONDS =
            15f

        const val MAXIMUM_RESPAWN_SECONDS =
            30f
    }
}
