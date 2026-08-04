package com.azizoglu.grisilith.combat.condition

import com.azizoglu.grisilith.content.ConditionType

class ActiveCondition(
    application: ConditionApplication
) {
    val type: ConditionType = application.type

    private var remainingDuration: Float? =
        application.durationSeconds
            ?.coerceAtLeast(0f)

    private var currentSource: ConditionSource =
        application.source

    private var secondsUntilNextTick: Float =
        tickIntervalSeconds()
            ?: Float.POSITIVE_INFINITY

    val source: ConditionSource
        get() = currentSource

    val remainingDurationSeconds: Float?
        get() = remainingDuration

    val isPermanent: Boolean
        get() = remainingDuration == null

    val isExpired: Boolean
        get() = remainingDuration?.let { it <= 0f } ?: false

    fun update(
        delta: Float,
        onTick: (ConditionTick) -> Unit
    ) {
        if (delta <= 0f || isExpired) {
            return
        }

        val activeDelta =
            remainingDuration
                ?.let { duration ->
                    minOf(delta, duration)
                }
                ?: delta

        val interval = tickIntervalSeconds()

        if (
            interval != null &&
            currentSource.tickDamage > 0
        ) {
            secondsUntilNextTick -= activeDelta

            while (secondsUntilNextTick <= 0f) {
                onTick(
                    ConditionTick(
                        type = type,
                        source = currentSource
                    )
                )

                secondsUntilNextTick += interval
            }
        }

        remainingDuration =
            remainingDuration?.let { duration ->
                (duration - delta).coerceAtLeast(0f)
            }
    }

    fun refresh(
        application: ConditionApplication
    ) {
        require(application.type == type) {
            "Cannot refresh $type with ${application.type}."
        }

        val newDuration = application.durationSeconds

        remainingDuration =
            when {
                newDuration == null -> null
                remainingDuration == null -> null
                else -> maxOf(
                    remainingDuration ?: 0f,
                    newDuration.coerceAtLeast(0f)
                )
            }

        if (
            application.source.tickDamage >
            currentSource.tickDamage
        ) {
            currentSource = application.source
        }
    }

    private fun tickIntervalSeconds(): Float? {
        return DefaultConditionDefinitions
            .definitionFor(type)
            .tickIntervalSeconds
    }
}
