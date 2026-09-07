package com.azizoglu.grisilith.map

import com.azizoglu.grisilith.content.ConditionType
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.summon.SummonType
import com.azizoglu.grisilith.summon.SummonedEntity

object MapEnvironmentEffects {

    fun apply(
        obstacles: List<MapObstacle>,
        player: Player,
        playerIsAirborne: Boolean,
        enemies: List<Enemy>,
        summons: List<SummonedEntity>
    ) {
        val lakes =
            obstacles.filter {
                it.type == MapObstacleType.LAKE
            }

        applyToPlayer(
            lakes = lakes,
            player = player,
            isAirborne = playerIsAirborne
        )

        enemies.forEach { enemy ->
            val isInsideLake =
                !enemy.ignoresObstacles &&
                    lakes.any {
                        it.containsEffectPoint(
                            enemy.centerX,
                            enemy.centerY
                        )
                    }

            enemy.setTerrainMovementSpeedMultiplier(
                if (isInsideLake) LAKE_SPEED_MULTIPLIER else 1f
            )

            if (isInsideLake) {
                enemy.applyCondition(
                    ConditionType.WET,
                    WET_AFTER_EXIT_SECONDS
                )
            }
        }

        summons.forEach { summon ->
            val isAirborne =
                summon.type == SummonType.IMP

            val isInsideLake =
                !isAirborne &&
                    lakes.any {
                        it.containsEffectPoint(
                            summon.centerX,
                            summon.centerY
                        )
                    }

            summon.setTerrainMovementSpeedMultiplier(
                if (isInsideLake) LAKE_SPEED_MULTIPLIER else 1f
            )

            if (isInsideLake) {
                summon.applyCondition(
                    ConditionType.WET,
                    WET_AFTER_EXIT_SECONDS
                )
            }
        }
    }

    private fun applyToPlayer(
        lakes: List<MapObstacle>,
        player: Player,
        isAirborne: Boolean
    ) {
        val isInsideLake =
            !isAirborne &&
                lakes.any {
                    it.containsEffectPoint(
                        player.centerX,
                        player.centerY
                    )
                }

        player.setTerrainMovementSpeedMultiplier(
            if (isInsideLake) LAKE_SPEED_MULTIPLIER else 1f
        )

        if (isInsideLake) {
            player.applyCondition(
                ConditionType.WET,
                WET_AFTER_EXIT_SECONDS
            )
        }
    }

    private const val LAKE_SPEED_MULTIPLIER =
        0.5f

    private const val WET_AFTER_EXIT_SECONDS =
        5f
}
