package com.azizoglu.grisilith.weapon.skill

import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.weapon.runtime.WeaponInstance
import kotlin.math.floor

class DefensiveLineRuntime(
    private val playerStats: PlayerStats,
    private val currentStageProvider: () -> Int
) {
    private var stationarySeconds = 0f
    private var currentBonus = 0

    val bonusArmorClass:
        Int
        get() =
            currentBonus

    val isVisualActive:
        Boolean
        get() =
            currentBonus >
                0
    private var previousStage =
        currentStageProvider().coerceAtLeast(1)

    fun update(
        delta: Float,
        player: Player,
        equippedWeapons: List<WeaponInstance>
    ) {
        val shieldCount =
            equippedWeapons.count { weapon ->
                weapon.unlockedAbility?.id ==
                    DefaultWeaponAbilities.DEFENSIVE_LINE_ID
            }

        if (shieldCount <= 0) {
            reset()
            return
        }

        preserveHalfStacksOnStageChange(
            shieldCount
        )

        val maximumBonus =
            maximumBonusFor(
                shieldCount
            )

        if (
            player.actualMovementSpeed >
            0.5f
        ) {
            stationarySeconds = 0f
            setBonus(0, maximumBonus)
            return
        }

        stationarySeconds +=
            delta.coerceAtLeast(0f)

        val startDelay =
            DefaultWeaponAbilities
                .DEFENSIVE_LINE_START_DELAY_SECONDS

        val secondsPerPoint =
            DefaultWeaponAbilities
                .DEFENSIVE_LINE_SECONDS_PER_POINT
                .coerceAtLeast(
                    0.001f
                )

        val earnedBonus =
            if (
                stationarySeconds <
                startDelay
            ) {
                0
            } else {
                (
                    floor(
                        (
                            stationarySeconds -
                                startDelay
                            ) /
                            secondsPerPoint
                    )
                        .toInt() +
                        1
                    )
                    .coerceAtLeast(
                        1
                    )
            }

        setBonus(
            earnedBonus,
            maximumBonus
        )
    }

    fun reset() {
        stationarySeconds = 0f
        currentBonus = 0
        previousStage =
            currentStageProvider()
                .coerceAtLeast(1)

        playerStats.clearWeaponSkillBonuses()
    }

    private fun preserveHalfStacksOnStageChange(
        shieldCount: Int
    ) {
        val stage =
            currentStageProvider()
                .coerceAtLeast(1)

        if (stage == previousStage) {
            return
        }

        previousStage = stage

        val maximumBonus =
            maximumBonusFor(
                shieldCount
            )

        currentBonus =
            floor(currentBonus / 2f)
                .toInt()
                .coerceIn(
                    0,
                    maximumBonus
                )

        stationarySeconds =
            if (currentBonus <= 0) {
                0f
            } else {
                DefaultWeaponAbilities
                    .DEFENSIVE_LINE_START_DELAY_SECONDS +
                    (
                        currentBonus -
                            1
                        ) *
                    DefaultWeaponAbilities
                        .DEFENSIVE_LINE_SECONDS_PER_POINT
            }

        publishBonus(
            maximumBonus
        )
    }

    private fun maximumBonusFor(
        shieldCount: Int
    ): Int {
        return (
            DefaultWeaponAbilities
                .DEFENSIVE_LINE_BASE_LIMIT +
                (shieldCount - 1)
                    .coerceAtLeast(0)
            )
    }

    private fun setBonus(
        newBonus: Int,
        maximumBonus: Int
    ) {
        val safeBonus =
            newBonus.coerceIn(
                0,
                maximumBonus
            )

        if (
            safeBonus == currentBonus &&
            playerStats.weaponSkillBonusLimit(
                StatType.ARMOR_CLASS
            ) == maximumBonus
        ) {
            return
        }

        currentBonus = safeBonus
        publishBonus(maximumBonus)
    }

    private fun publishBonus(
        maximumBonus: Int
    ) {
        playerStats.setWeaponSkillBonuses(
            bonuses =
                if (currentBonus > 0) {
                    mapOf(
                        StatType.ARMOR_CLASS to
                            currentBonus
                    )
                } else {
                    emptyMap()
                },
            limits =
                mapOf(
                    StatType.ARMOR_CLASS to
                        maximumBonus
                )
        )
    }
}
