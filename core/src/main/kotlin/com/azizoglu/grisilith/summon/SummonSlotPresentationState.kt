package com.azizoglu.grisilith.summon

import com.azizoglu.grisilith.weapon.skill.WeaponAbilityState
import com.azizoglu.grisilith.weapon.summon.IvySummonChoice

/*
 * Read-only presentation model for the future weapon-slot UI rework.
 *
 * It intentionally contains NO rendering code and NO Texture dependency.
 * The future HUD can decide:
 * - READY  -> show summon icon
 * - ACTIVE -> show active/remaining state
 * - COOLDOWN -> show Ivy Staff icon + cooldown overlay
 */
data class SummonSlotPresentationState(
    val slotIndex: Int,
    val weaponRuntimeId: String,
    val choice: IvySummonChoice,
    val state: WeaponAbilityState,
    val remainingActiveSeconds: Float,
    val remainingCooldownSeconds: Float,
    val activeSummonCount: Int,
    val totalSummonCountForWave: Int
) {
    val isReady: Boolean
        get() =
            state ==
                WeaponAbilityState.READY

    val isActive: Boolean
        get() =
            state ==
                WeaponAbilityState.ACTIVE

    val isCoolingDown: Boolean
        get() =
            state ==
                WeaponAbilityState.COOLDOWN

    val cooldownProgress: Float
        get() {
            val total =
                when (choice) {
                    IvySummonChoice.SUMMON_ANIMALS ->
                        SummonSpawnScheduler
                            .COW_COOLDOWN_SECONDS

                    IvySummonChoice.SUMMON_DEMONS ->
                        SummonSpawnScheduler
                            .IMP_COOLDOWN_SECONDS

                    IvySummonChoice.CALL_THE_KNIGHT ->
                        SummonSpawnScheduler
                            .KNIGHT_COOLDOWN_SECONDS
                }

            if (
                total <=
                0f
            ) {
                return 0f
            }

            return (
                1f -
                    remainingCooldownSeconds /
                    total
                )
                .coerceIn(
                    0f,
                    1f
                )
        }

    val activeProgress: Float
        get() {
            val total =
                when (choice) {
                    IvySummonChoice.SUMMON_ANIMALS ->
                        SummonSpawnScheduler
                            .COW_ACTIVE_SECONDS

                    IvySummonChoice.SUMMON_DEMONS ->
                        SummonSpawnScheduler
                            .IMP_ACTIVE_SECONDS

                    IvySummonChoice.CALL_THE_KNIGHT ->
                        SummonSpawnScheduler
                            .KNIGHT_ACTIVE_SECONDS
                }

            if (
                total <=
                0f
            ) {
                return 0f
            }

            return (
                remainingActiveSeconds /
                    total
                )
                .coerceIn(
                    0f,
                    1f
                )
        }
}
