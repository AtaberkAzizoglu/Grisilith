package com.azizoglu.grisilith.summon

import com.azizoglu.grisilith.weapon.skill.WeaponAbilityState
import com.azizoglu.grisilith.weapon.summon.IvySummonChoice


class SummonSpawnScheduler(
    val sourceWeaponRuntimeId: String,
    val choice: IvySummonChoice
) {

    var hasLivingWave: Boolean =
        false
        private set

    var remainingCooldownSeconds: Float =
        0f
        private set

    val state:
        WeaponAbilityState
        get() =
            when {
                hasLivingWave ->
                    WeaponAbilityState.ACTIVE

                remainingCooldownSeconds >
                    0f ->
                    WeaponAbilityState.COOLDOWN

                else ->
                    WeaponAbilityState.READY
            }

    val activeLifetimeSeconds: Float
        get() =
            when (choice) {
                IvySummonChoice.SUMMON_ANIMALS ->
                    COW_ACTIVE_SECONDS

                IvySummonChoice.SUMMON_DEMONS ->
                    IMP_ACTIVE_SECONDS

                IvySummonChoice.CALL_THE_KNIGHT ->
                    KNIGHT_ACTIVE_SECONDS
            }

    val cooldownSeconds: Float
        get() =
            when (choice) {
                IvySummonChoice.SUMMON_ANIMALS ->
                    COW_COOLDOWN_SECONDS

                IvySummonChoice.SUMMON_DEMONS ->
                    IMP_COOLDOWN_SECONDS

                IvySummonChoice.CALL_THE_KNIGHT ->
                    KNIGHT_COOLDOWN_SECONDS
            }

    val isReadyToActivate: Boolean
        get() =
            state ==
                WeaponAbilityState.READY

    fun update(
        delta: Float
    ) {
        if (
            delta <=
            0f ||
            hasLivingWave ||
            remainingCooldownSeconds <=
            0f
        ) {
            return
        }

        remainingCooldownSeconds =
            (
                remainingCooldownSeconds -
                    delta
                )
                .coerceAtLeast(
                    0f
                )
    }

    fun markWaveSpawned():
        Boolean {
        if (!isReadyToActivate) {
            return false
        }

        hasLivingWave =
            true

        /*
         * Important:
         * cooldown does NOT begin here.
         */
        remainingCooldownSeconds =
            0f

        return true
    }

    fun markWaveEnded() {
        if (!hasLivingWave) {
            return
        }

        hasLivingWave =
            false

        /*
         * Cooldown begins only when the LAST entity belonging to
         * this weapon wave has disappeared.
         */
        remainingCooldownSeconds =
            cooldownSeconds
    }

    fun reset() {
        hasLivingWave =
            false

        remainingCooldownSeconds =
            0f
    }

    fun reduceCooldown(seconds: Float) {
        if (seconds <= 0f || hasLivingWave) return
        remainingCooldownSeconds =
            (remainingCooldownSeconds - seconds).coerceAtLeast(0f)
    }

    companion object {
        const val COW_ACTIVE_SECONDS =
            15f

        const val COW_COOLDOWN_SECONDS =
            30f

        const val IMP_ACTIVE_SECONDS =
            20f

        const val IMP_COOLDOWN_SECONDS =
            20f

        const val KNIGHT_ACTIVE_SECONDS =
            30f

        const val KNIGHT_COOLDOWN_SECONDS =
            30f
    }
}
