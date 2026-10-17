package com.azizoglu.grisilith.weapon.skill

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyType

object VortexPullTargetRules {

    fun pullMultiplier(
        enemy: Enemy,
        currentStage: Int
    ): Float {
        return when (
            enemy.type
        ) {
            EnemyType.NORMAL -> {
                DefaultWeaponAbilities
                    .VORTEX_PULL_NORMAL_PULL_MULTIPLIER
            }

            EnemyType.UNIQUE -> {
                uniquePullMultiplier(
                    currentStage
                )
            }

            EnemyType.MINI_BOSS, EnemyType.FINAL_BOSS -> 0f
        }
    }

    /*
     * Dokümandaki "Unique yalnızca yüksek stage'lerde kaçabilir"
     * kuralı için stage arttıkça çekişi azaltan merkezi kural.
     */
    private fun uniquePullMultiplier(
        currentStage: Int
    ): Float {
        val stage =
            currentStage
                .coerceAtLeast(
                    1
                )

        val reduction =
            when {
                stage >= 40 -> 0.40f
                stage >= 30 -> 0.28f
                stage >= 20 -> 0.16f
                else -> 0f
            }

        return (
            DefaultWeaponAbilities
                .VORTEX_PULL_UNIQUE_PULL_MULTIPLIER -
                reduction
            )
            .coerceAtLeast(
                0.18f
            )
    }
}
