package com.azizoglu.grisilith.weapon.runtime

class WeaponAttackTimer {

    private var elapsedSeconds = 0f

    fun update(
        delta: Float,
        attacksPerSecond: Float,
        onAttackReady: () -> Unit
    ) {
        if (
            delta <= 0f ||
            attacksPerSecond <= 0f
        ) {
            return
        }

        elapsedSeconds += delta

        val attackInterval =
            1f / attacksPerSecond

        /*
         * Çok yüksek attack speed veya düşük FPS
         * durumunda hazır olan saldırıları kaybetmemek
         * için while kullanıyoruz.
         */
        while (
            elapsedSeconds >= attackInterval
        ) {
            elapsedSeconds -= attackInterval
            onAttackReady()
        }
    }

    fun reset() {
        elapsedSeconds = 0f
    }
}
