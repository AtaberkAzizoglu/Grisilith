package com.azizoglu.grisilith.combat.condition

import com.azizoglu.grisilith.content.ConditionType

class ActiveCondition(
    val type: ConditionType,
    durationSeconds: Float?
) {

    /*
     * null:
     * Condition süre dolana kadar değil, dışarıdan
     * kaldırılana veya Enemy ölünceye kadar aktiftir.
     */
    private var remainingDuration:
        Float? =
        durationSeconds
            ?.coerceAtLeast(
                0f
            )

    val isPermanent: Boolean
        get() =
            remainingDuration == null

    val isExpired: Boolean
        get() =
            remainingDuration
                ?.let { duration ->
                    duration <= 0f
                }
                ?: false

    fun update(
        delta: Float
    ) {
        val currentDuration =
            remainingDuration
                ?: return

        remainingDuration =
            (
                currentDuration -
                    delta
                )
                .coerceAtLeast(
                    0f
                )
    }

    fun refresh(
        durationSeconds: Float?
    ) {
        /*
         * Yeni uygulama kalıcıysa mevcut condition
         * doğrudan kalıcı hâle gelir.
         */
        if (durationSeconds == null) {
            remainingDuration =
                null

            return
        }

        /*
         * Zaten kalıcı olan condition süreliye
         * çevrilmez.
         */
        val currentDuration =
            remainingDuration
                ?: return

        remainingDuration =
            maxOf(
                currentDuration,
                durationSeconds
                    .coerceAtLeast(
                        0f
                    )
            )
    }
}
