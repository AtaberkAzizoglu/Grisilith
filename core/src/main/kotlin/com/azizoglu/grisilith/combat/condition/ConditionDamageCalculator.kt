package com.azizoglu.grisilith.combat.condition

import com.azizoglu.grisilith.content.ConditionType
import com.azizoglu.grisilith.statistics.PlayerStats
import kotlin.math.roundToInt

object ConditionDamageCalculator {

    fun createApplication(
        type: ConditionType,
        playerStats: PlayerStats,
        durationSeconds: Float =
            DefaultConditionDefinitions.DEFAULT_DURATION_SECONDS,
        sourceWeaponRuntimeId: String? = null,
        sourceWeaponDefinitionId: String? = null,
        sourceName: String? = null,
        sourceCenterX: Float? = null,
        sourceCenterY: Float? = null
    ): ConditionApplication {
        val definition =
            DefaultConditionDefinitions.definitionFor(type)

        val tickDamage =
            if (definition.dealsPeriodicDamage) {
                val stat =
                    definition.scalingStat
                        ?: error("A damaging Condition requires a scaling Stat.")

                (
                    definition.baseTickDamage +
                        playerStats.totalScore(stat) *
                        definition.damagePerStatPoint
                    )
                    .roundToInt()
                    .coerceAtLeast(1)
            } else {
                0
            }

        return ConditionApplication(
            type = type,
            durationSeconds = durationSeconds,
            source = ConditionSource(
                damageType = definition.damageType,
                tickDamage = tickDamage,
                sourceWeaponRuntimeId = sourceWeaponRuntimeId,
                sourceWeaponDefinitionId = sourceWeaponDefinitionId,
                sourceName = sourceName,
                sourceCenterX = sourceCenterX,
                sourceCenterY = sourceCenterY
            )
        )
    }
}
