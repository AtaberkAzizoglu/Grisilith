package com.azizoglu.grisilith.combat

import com.azizoglu.grisilith.content.DamageType
import kotlin.math.roundToInt

/** Shared defense rules for player, enemies and summons. Affinities resolve first. */
object DamageDefense {
    fun resolve(amount: Int, type: DamageType, baseArmor: Int, armorClass: Int,
                baseMagicResistance: Int, magicResistance: Int, piercingIgnoreRatio: Float = 0f,
                defenseTimeScale: Float = 1f): Int {
        if (amount <= 0) return 0
        val reduction = when {
            type.isPhysical -> baseArmor.coerceAtLeast(0).toFloat() * armorClass.coerceAtLeast(0) *
                (1f - piercingIgnoreRatio.coerceIn(0f, 0.75f))
            type.isMagical -> baseMagicResistance.coerceAtLeast(0).toFloat() * magicResistance.coerceAtLeast(0)
            else -> 0f // Legacy untyped damage and unused Earth/Cold rules are unchanged.
        }
        // Continuous fields pay one second's defense over one second, not once per tiny tick.
        return (amount - reduction * defenseTimeScale.coerceAtLeast(0f)).roundToInt().coerceAtLeast(1)
    }
}
