package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.weapon.definition.WeaponDefinition
import java.util.UUID

class WeaponInstance(
    val definition: WeaponDefinition,
    level: Int = 1
) {

    companion object {
        const val MINIMUM_LEVEL = 1
        const val MAXIMUM_LEVEL = 5
    }

    val runtimeId: String =
        UUID.randomUUID().toString()

    var level: Int =
        level
        private set

    init {
        require(
            level in
                MINIMUM_LEVEL..MAXIMUM_LEVEL
        ) {
            "Weapon level must be between " +
                "$MINIMUM_LEVEL and $MAXIMUM_LEVEL."
        }
    }

    val displayName: String
        get() =
            definition.displayName

    val currentLevelStats
        get() =
            definition.statsForLevel(
                level
            )

    val isMaximumLevel: Boolean
        get() =
            level >= MAXIMUM_LEVEL

    val hasBasicAttackUpgrade: Boolean
        get() =
            currentLevelStats
                .basicAttackUpgraded

    val hasWeaponAbility: Boolean
        get() =
            currentLevelStats
                .weaponAbilityUnlocked

    fun canLevelUp(): Boolean {
        return !isMaximumLevel
    }

    fun increaseLevel(): Boolean {
        if (!canLevelUp()) {
            return false
        }

        level++
        return true
    }

    fun setLevel(
        newLevel: Int
    ) {
        require(
            newLevel in
                MINIMUM_LEVEL..MAXIMUM_LEVEL
        )

        level = newLevel
    }

    override fun equals(
        other: Any?
    ): Boolean {
        if (this === other) {
            return true
        }

        if (
            other !is WeaponInstance
        ) {
            return false
        }

        return runtimeId ==
            other.runtimeId
    }

    override fun hashCode(): Int {
        return runtimeId.hashCode()
    }
}
