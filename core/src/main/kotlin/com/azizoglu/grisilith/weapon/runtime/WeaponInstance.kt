package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.weapon.definition.WeaponDefinition
import java.util.UUID
import com.azizoglu.grisilith.weapon.skill.WeaponAbilityDefinition
import com.azizoglu.grisilith.weapon.summon.IvySummonChoice
import com.azizoglu.grisilith.weapon.glyph.ChalkDustSpellChoice

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

    /*
     * Her WeaponInstance kendi saldırı sayacını
     * bağımsız olarak tutar.
     */
    private var normalAttackCount =
        0

    var ivySummonChoice:
        IvySummonChoice? =
        null
        private set

    var chalkDustSpellChoice:
        ChalkDustSpellChoice? =
        null
        private set

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

    val unlockedAbility:
        WeaponAbilityDefinition?
        get() =
            if (
                hasWeaponAbility
            ) {
                definition.weaponAbility
            } else {
                null
            }

    val completedNormalAttackCount: Int
        get() =
            normalAttackCount

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
        ) {
            "Weapon level must be between " +
                "$MINIMUM_LEVEL and $MAXIMUM_LEVEL."
        }

        level = newLevel
    }

    fun chooseIvySummon(
        choice: IvySummonChoice
    ): Boolean {

        if (
            ivySummonChoice !=
            null
        ) {
            return false
        }

        ivySummonChoice =
            choice

        return true
    }

    fun restoreIvySummonChoice(
        choice: IvySummonChoice?
    ) {
        ivySummonChoice =
            choice
    }

    fun inheritIvySummonChoiceFrom(
        other: WeaponInstance
    ) {

        if (
            ivySummonChoice ==
            null
        ) {
            ivySummonChoice =
                other.ivySummonChoice
        }
    }

    fun chooseChalkDustSpell(
        choice: ChalkDustSpellChoice
    ): Boolean {
        if (chalkDustSpellChoice != null) return false
        chalkDustSpellChoice = choice
        return true
    }

    fun restoreChalkDustSpellChoice(
        choice: ChalkDustSpellChoice?
    ) {
        chalkDustSpellChoice = choice
    }

    fun inheritChalkDustSpellChoiceFrom(
        other: WeaponInstance
    ) {
        if (chalkDustSpellChoice == null) {
            chalkDustSpellChoice = other.chalkDustSpellChoice
        }
    }

    fun registerNormalAttack(): Int {
        normalAttackCount++

        return normalAttackCount
    }

    fun resetNormalAttackCount() {
        normalAttackCount = 0
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
