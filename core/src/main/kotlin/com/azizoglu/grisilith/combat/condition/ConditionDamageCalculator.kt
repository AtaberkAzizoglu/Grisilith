package com.azizoglu.grisilith.combat.condition

import com.azizoglu.grisilith.content.ConditionType
import com.azizoglu.grisilith.statistics.PlayerStats

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

        val tickMaximumHealthRatio =
            if (definition.dealsPeriodicDamage) {
                val stat =
                    definition.scalingStat
                        ?: error("A damaging Condition requires a scaling Stat.")

                definition.maximumHealthRatioPerStatPoint *
                    playerStats.totalScore(stat).coerceAtLeast(0)
            } else {
                0f
            }

        return ConditionApplication(
            type = type,
            durationSeconds = durationSeconds,
            source = ConditionSource(
                damageType = definition.damageType,
                tickMaximumHealthRatio = tickMaximumHealthRatio,
                sourceWeaponRuntimeId = sourceWeaponRuntimeId,
                sourceWeaponDefinitionId = sourceWeaponDefinitionId,
                sourceName = sourceName,
                sourceCenterX = sourceCenterX,
                sourceCenterY = sourceCenterY
            )
        )
    }

    fun createApplication(
        type: ConditionType,
        sourceStatScore: Int,
        durationSeconds: Float = DefaultConditionDefinitions.DEFAULT_DURATION_SECONDS,
        sourceName: String? = null
    ): ConditionApplication {
        val definition = DefaultConditionDefinitions.definitionFor(type)
        val ratio = if (definition.dealsPeriodicDamage) {
            definition.maximumHealthRatioPerStatPoint * sourceStatScore.coerceAtLeast(0)
        } else 0f
        return ConditionApplication(
            type = type,
            durationSeconds = durationSeconds,
            source = ConditionSource(
                damageType = definition.damageType,
                tickMaximumHealthRatio = ratio,
                sourceName = sourceName
            )
        )
    }
}
