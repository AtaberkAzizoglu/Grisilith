package com.azizoglu.grisilith.weapon.skill

import com.azizoglu.grisilith.weapon.runtime.WeaponInstance
import com.azizoglu.grisilith.item.runtime.ActiveItemEffects

class WeaponAbilityRuntime(
    private val weapon: WeaponInstance
) {

    private var activeRemainingSeconds =
        0f

    private var cooldownRemainingSeconds =
        0f

    private var skipNextCooldown = false

    val definition:
        WeaponAbilityDefinition?
        get() =
            weapon.unlockedAbility

    val state:
        WeaponAbilityState
        get() {
            val ability =
                definition
                    ?: return WeaponAbilityState.LOCKED

            if (
                ability.activationType ==
                WeaponAbilityActivationType.PASSIVE
            ) {
                return WeaponAbilityState.ACTIVE
            }

            if (
                activeRemainingSeconds >
                0f
            ) {
                return WeaponAbilityState.ACTIVE
            }

            if (
                cooldownRemainingSeconds >
                0f
            ) {
                return WeaponAbilityState.COOLDOWN
            }

            return WeaponAbilityState.READY
        }

    val remainingActiveSeconds:
        Float
        get() =
            activeRemainingSeconds
                .coerceAtLeast(
                    0f
                )

    val remainingCooldownSeconds:
        Float
        get() =
            cooldownRemainingSeconds
                .coerceAtLeast(
                    0f
                )

    fun update(
        delta: Float
    ) {
        if (
            delta <=
            0f
        ) {
            return
        }

        val ability =
            definition
                ?: return

        if (
            ability.activationType ==
            WeaponAbilityActivationType.PASSIVE
        ) {
            return
        }

        ActiveItemEffects.onAbilityState(
            weapon = weapon,
            isReady = state == WeaponAbilityState.READY,
            delta = delta
        )

        if (
            activeRemainingSeconds >
            0f
        ) {
            activeRemainingSeconds =
                (
                    activeRemainingSeconds -
                        delta
                    )
                    .coerceAtLeast(
                        0f
                    )

            if (
                activeRemainingSeconds ==
                0f
            ) {
                beginCooldown(
                    ability
                )
            }

            return
        }

        if (
            cooldownRemainingSeconds >
            0f
        ) {
            cooldownRemainingSeconds =
                (
                    cooldownRemainingSeconds -
                        delta
                    )
                    .coerceAtLeast(
                        0f
                    )
        }
    }

    fun tryActivate():
        Boolean {

        val ability =
            definition
                ?: return false

        if (
            ability.activationType !=
            WeaponAbilityActivationType.ACTIVE ||
            state !=
            WeaponAbilityState.READY
        ) {
            return false
        }

        if (
            ability.durationSeconds >
            0f
        ) {
            activeRemainingSeconds =
                ability.durationSeconds
        } else {
            beginCooldown(
                ability
            )
        }

        if (ActiveItemEffects.onAbilityActivated(weapon)) {
            skipNextCooldown = ability.durationSeconds > 0f
            cooldownRemainingSeconds = 0f
        }

        return true
    }

    fun triggerAutomatic():
        Boolean {

        val ability =
            definition
                ?: return false

        if (
            ability.activationType !=
            WeaponAbilityActivationType.AUTOMATIC ||
            state !=
            WeaponAbilityState.READY
        ) {
            return false
        }

        if (
            ability.durationSeconds >
            0f
        ) {
            activeRemainingSeconds =
                ability.durationSeconds
        } else {
            beginCooldown(
                ability
            )
        }

        if (ActiveItemEffects.onAbilityActivated(weapon)) {
            skipNextCooldown = ability.durationSeconds > 0f
            cooldownRemainingSeconds = 0f
        }

        return true
    }

    fun endActiveEarly() {

        val ability =
            definition
                ?: return

        if (
            activeRemainingSeconds <=
            0f
        ) {
            return
        }

        activeRemainingSeconds =
            0f

        beginCooldown(
            ability
        )
    }

    fun reset() {
        activeRemainingSeconds =
            0f

        cooldownRemainingSeconds =
            0f

        skipNextCooldown = false
    }

    private fun beginCooldown(
        ability:
        WeaponAbilityDefinition
    ) {
        if (skipNextCooldown) {
            skipNextCooldown = false
            cooldownRemainingSeconds = 0f
            return
        }

        cooldownRemainingSeconds =
            ability.cooldownSeconds
                .coerceAtLeast(
                    0f
                )
    }
}
