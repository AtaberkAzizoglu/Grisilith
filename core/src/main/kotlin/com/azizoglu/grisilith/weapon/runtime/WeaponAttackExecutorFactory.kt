package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.weapon.definition.WeaponAttackPattern
import com.azizoglu.grisilith.weapon.runtime.executor.ShortswordAttackExecutor

class WeaponAttackExecutorFactory(
    playerStats: PlayerStats
) {

    private val damageCalculator =
        WeaponDamageCalculator(
            playerStats = playerStats
        )

    private val targetSelector =
        WeaponTargetSelector()

    private val shortswordExecutor =
        ShortswordAttackExecutor(
            damageCalculator =
                damageCalculator,
            targetSelector =
                targetSelector
        )

    fun executorFor(
        attackPattern: WeaponAttackPattern
    ): WeaponAttackExecutor? {
        return when (attackPattern) {
            WeaponAttackPattern
                .SHORT_SLASH_LINES -> {
                shortswordExecutor
            }

            WeaponAttackPattern.NONE -> {
                null
            }

            /*
             * Sonraki adımlarda:
             * Longsword, Lance, Dagger ve
             * projectile executor'ları gelecek.
             */
            else -> {
                null
            }
        }
    }
}
