package com.azizoglu.grisilith.combat

import com.azizoglu.grisilith.content.DamageType
import kotlin.math.roundToInt

/** Shared defense rules for player, enemies and summons. Affinities resolve first. */
object DamageDefense {
    fun resolve(amount: Int, type: DamageType, baseArmor: Int, armorClass: Int,
                baseMagicResistance: Int, magicResistance: Int, piercingIgnoreRatio: Float = 0f,
                @Suppress("UNUSED_PARAMETER") defenseTimeScale: Float = 1f): Int {
        if (amount <= 0) return 0
        val defense = when {
            type.isPhysical -> (baseArmor.coerceAtLeast(0).toDouble() + armorClass.coerceAtLeast(0)) *
                (1f - piercingIgnoreRatio.coerceIn(0f, 0.75f))
            type.isMagical -> baseMagicResistance.coerceAtLeast(0).toDouble() + magicResistance.coerceAtLeast(0)
            else -> 0.0 // Legacy untyped damage and unused Earth/Cold rules are unchanged.
        }
        // Percentages apply equally to every tick; defense must not scale with tick duration.
        // Retain the legacy argument for existing damage-context callers.
        return (amount * (100.0 / (100.0 + defense))).roundToInt().coerceAtLeast(1)
    }
}
