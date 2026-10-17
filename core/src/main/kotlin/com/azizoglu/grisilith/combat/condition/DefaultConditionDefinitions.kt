package com.azizoglu.grisilith.combat.condition

import com.azizoglu.grisilith.content.ConditionType
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.statistics.StatType

object DefaultConditionDefinitions {

    const val DEFAULT_DURATION_SECONDS = 3f
    const val DOT_TICK_INTERVAL_SECONDS = 0.5f

    private val definitions =
        listOf(
            ConditionDefinition(
                type = ConditionType.BURNED,
                damageType = DamageType.FIRE,
                scalingStat = StatType.ELEMENTAL_DAMAGE,
                tickIntervalSeconds = DOT_TICK_INTERVAL_SECONDS,
                maximumHealthRatioPerStatPoint = 0.0005f
            ),
            ConditionDefinition(
                type = ConditionType.POISONED,
                damageType = DamageType.POISON,
                scalingStat = StatType.SHEPHERD,
                tickIntervalSeconds = DOT_TICK_INTERVAL_SECONDS,
                maximumHealthRatioPerStatPoint = 0.0005f
            ),
            ConditionDefinition(
                type = ConditionType.CORRODING,
                damageType = DamageType.ACID,
                scalingStat = StatType.MIGHT_OF_NATURE,
                tickIntervalSeconds = DOT_TICK_INTERVAL_SECONDS,
                maximumHealthRatioPerStatPoint = 0.0006f
            ),
            ConditionDefinition(
                type = ConditionType.NECROSIS,
                damageType = DamageType.NECROTIC,
                scalingStat = StatType.MIGHT_OF_NATURE,
                tickIntervalSeconds = DOT_TICK_INTERVAL_SECONDS,
                maximumHealthRatioPerStatPoint = 0.0006f
            ),
            ConditionDefinition(type = ConditionType.WET),
            ConditionDefinition(type = ConditionType.STUNNED),
            ConditionDefinition(type = ConditionType.ROOTED),
            ConditionDefinition(type = ConditionType.PARALYZED),
            ConditionDefinition(type = ConditionType.FRIGHTENED),
            ConditionDefinition(type = ConditionType.SLOWED),
            ConditionDefinition(type = ConditionType.XXX_CURSE),
            ConditionDefinition(type = ConditionType.ARMOR_BREAK),
            ConditionDefinition(
                type = ConditionType.ACIDIC_PATH_DAMAGE,
                damageType = DamageType.ACID,
                scalingStat = StatType.MIGHT_OF_NATURE,
                tickIntervalSeconds = DOT_TICK_INTERVAL_SECONDS,
                maximumHealthRatioPerStatPoint = 0.07f / 6f
            )
        )
            .associateBy { definition ->
                definition.type
            }

    fun definitionFor(
        type: ConditionType
    ): ConditionDefinition {
        return definitions.getValue(type)
    }
}
