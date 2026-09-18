package com.azizoglu.grisilith.weapon.skill

import com.azizoglu.grisilith.content.DamageType

/**
 * Invisible one-hit target owned by a persistent spell field.
 * It deliberately does not participate in projectile collision resolution:
 * attacks probe it, but continue travelling normally.
 */
internal class ReactiveSpellFieldTarget(
    private val vulnerableDamageType: DamageType
) {
    val maximumHealth: Int = 1

    var currentHealth: Int = maximumHealth
        private set

    val isAlive: Boolean
        get() = currentHealth > 0

    fun reset() {
        currentHealth = maximumHealth
    }

    fun clear() {
        currentHealth = 0
    }

    fun tryDamage(damageType: DamageType?): Boolean {
        if (!isAlive || damageType != vulnerableDamageType) return false
        currentHealth = 0
        return true
    }
}
