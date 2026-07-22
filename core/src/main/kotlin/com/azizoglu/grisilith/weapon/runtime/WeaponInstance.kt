package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.content.ContentRarity
import com.azizoglu.grisilith.weapon.definition.WeaponDefinition
import java.util.UUID

class WeaponInstance(
    val definition: WeaponDefinition,
    rarity: ContentRarity =
        ContentRarity.COMMON,
    level: Int = 1
) {

    /*
     * Aynı silahın iki ayrı kopyasını ayırmak için
     * run içinde benzersiz runtime ID'si.
     *
     * Bu save ID'si değildir.
     */
    val runtimeId: String =
        UUID.randomUUID().toString()

    var rarity: ContentRarity =
        rarity
        private set

    var level: Int =
        level
        private set

    init {
        require(level in 1..4) {
            "Weapon level must be between 1 and 4."
        }

        if (!definition.isUpgradeable) {
            require(
                rarity == ContentRarity.COMMON
            ) {
                "Non-upgradeable weapons must remain Common."
            }
        }
    }

    val displayName: String
        get() =
            definition.displayName

    fun canLevelUp(): Boolean {
        return (
            definition.isUpgradeable &&
                level < 4
            )
    }

    fun canIncreaseRarity(): Boolean {
        return (
            definition.isUpgradeable &&
                !rarity.isMaximumRarity
            )
    }

    fun setLevel(
        newLevel: Int
    ) {
        require(newLevel in 1..4)

        level = newLevel
    }

    fun setRarity(
        newRarity: ContentRarity
    ) {
        require(
            definition.isUpgradeable
        ) {
            "${definition.displayName} cannot increase rarity."
        }

        rarity = newRarity
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
