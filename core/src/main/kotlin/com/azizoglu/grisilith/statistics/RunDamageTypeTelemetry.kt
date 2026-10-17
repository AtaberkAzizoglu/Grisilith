package com.azizoglu.grisilith.statistics

import com.azizoglu.grisilith.content.DamageType

/** Records the resolved damage type at the single point where enemy health is actually reduced. */
object RunDamageTypeTelemetry {
    private var activeStatistics: RunStatistics? = null

    fun bind(statistics: RunStatistics) {
        activeStatistics = statistics
    }

    fun unbind(statistics: RunStatistics) {
        if (activeStatistics === statistics) activeStatistics = null
    }

    fun recordEnemyDamage(amount: Int, damageType: DamageType) {
        activeStatistics?.recordDamageTypeDealt(amount, damageType)
    }
}
