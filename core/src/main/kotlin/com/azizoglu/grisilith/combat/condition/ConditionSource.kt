package com.azizoglu.grisilith.combat.condition

import com.azizoglu.grisilith.content.DamageType
import kotlin.math.roundToInt

data class ConditionSource(
    val damageType: DamageType = DamageType.NONE,
    val tickDamage: Int = 0,
    val tickMaximumHealthRatio: Float = 0f,
    val sourceWeaponRuntimeId: String? = null,
    val sourceWeaponDefinitionId: String? = null,
    val sourceName: String? = null,
    val sourceCenterX: Float? = null,
    val sourceCenterY: Float? = null,
    /** Audio-only snapshot; does not alter condition damage or retain an enemy reference. */
    val sourceEnemyBoss: Boolean = false
) {
    init {
        require(tickDamage >= 0) {
            "Condition tick damage cannot be negative."
        }
        require(tickMaximumHealthRatio >= 0f) {
            "Condition maximum-health ratio cannot be negative."
        }
    }

    fun resolvedTickDamage(targetMaximumHealth: Int): Int =
        if (tickMaximumHealthRatio > 0f) {
            (targetMaximumHealth.coerceAtLeast(1) * tickMaximumHealthRatio)
                .roundToInt().coerceAtLeast(1)
        } else tickDamage

    companion object {
        val NONE = ConditionSource()
    }
}
