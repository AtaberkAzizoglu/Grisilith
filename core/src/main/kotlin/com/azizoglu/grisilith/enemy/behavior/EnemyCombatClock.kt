package com.azizoglu.grisilith.enemy.behavior

/*
 * Melee, ranged ve Spell Focus Enemy saldırıları için ortak
 * cooldown saati.
 *
 * Bu paket yalnızca zamanlamayı kurar. Gerçek Weapon saldırısı
 * sonraki combat entegrasyonunda consume() ile tetiklenecektir.
 */
class EnemyCombatClock(
    attacksPerSecond: Float
) {

    private val attacksPerSecond =
        attacksPerSecond
            .coerceAtLeast(
                0f
            )

    private val attackIntervalSeconds =
        if (
            this.attacksPerSecond >
            0f
        ) {
            1f /
                this.attacksPerSecond
        } else {
            Float.POSITIVE_INFINITY
        }

    private var remainingSeconds =
        attackIntervalSeconds

    val isReady: Boolean
        get() =
            attacksPerSecond >
                0f &&
                remainingSeconds <=
                0f

    fun update(
        delta: Float
    ) {
        if (
            attacksPerSecond <=
            0f
        ) {
            return
        }

        remainingSeconds -=
            delta.coerceAtLeast(
                0f
            )
    }

    fun consume(): Boolean {
        if (
            !isReady
        ) {
            return false
        }

        remainingSeconds +=
            attackIntervalSeconds

        return true
    }

    fun reset() {
        remainingSeconds =
            attackIntervalSeconds
    }
}
