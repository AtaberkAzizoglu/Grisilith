package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.weapon.definition.WeaponAttackPattern
import com.azizoglu.grisilith.weapon.runtime.executor.DaggerAttackExecutor
import com.azizoglu.grisilith.weapon.runtime.executor.LanceAttackExecutor
import com.azizoglu.grisilith.weapon.runtime.executor.LongswordAttackExecutor
import com.azizoglu.grisilith.weapon.runtime.executor.ProjectileWeaponAttackExecutor
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

    private val longswordExecutor =
        LongswordAttackExecutor(
            damageCalculator =
                damageCalculator,

            targetSelector =
                targetSelector
        )

    private val lanceExecutor =
        LanceAttackExecutor(
            damageCalculator =
                damageCalculator,

            targetSelector =
                targetSelector
        )

    private val daggerExecutor =
        DaggerAttackExecutor(
            damageCalculator =
                damageCalculator,

            targetSelector =
                targetSelector,

            playerStats =
                playerStats
        )

    private val projectileExecutor =
        ProjectileWeaponAttackExecutor(
            damageCalculator =
                damageCalculator,

            targetSelector =
                targetSelector,

            playerStats =
                playerStats
        )

    fun executorFor(
        attackPattern: WeaponAttackPattern
    ): WeaponAttackExecutor? {
        return when (attackPattern) {
            WeaponAttackPattern
                .SHORT_SLASH_LINES -> {
                shortswordExecutor
            }

            WeaponAttackPattern
                .FORWARD_CONE -> {
                longswordExecutor
            }

            WeaponAttackPattern
                .TARGET_AREA_CIRCLE -> {
                lanceExecutor
            }

            WeaponAttackPattern
                .MELEE_OR_THROW -> {
                daggerExecutor
            }

            WeaponAttackPattern
                .STRAIGHT_PROJECTILE,

            WeaponAttackPattern
                .PIERCING_PROJECTILE -> {
                projectileExecutor
            }

            WeaponAttackPattern.NONE -> {
                null
            }
        }
    }
}
