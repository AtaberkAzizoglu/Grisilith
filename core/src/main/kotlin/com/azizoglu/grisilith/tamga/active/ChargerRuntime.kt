package com.azizoglu.grisilith.tamga.runtime.active

import com.azizoglu.grisilith.player.Player

class ChargerRuntime :
    ActiveTamgaRuntime(
        durationSeconds =
            ACTIVE_DURATION_SECONDS,

        cooldownSeconds =
            COOLDOWN_SECONDS
    ) {

    companion object {

        /*
         * 3.5 saniye hızlanma,
         * ardından 0.8 saniye maksimum hız.
         */
        private const val ACTIVE_DURATION_SECONDS =
            4.3f

        /*
         * Patlama gücünün artabileceği maksimum süre.
         *
         * Charger toplamda 4.3 saniye çalışsa da
         * hasar ve alan artışı 3.5 saniyede dolar.
         */
        const val MAXIMUM_CHARGE_SECONDS =
            3.5f

        private const val ACCELERATION_DURATION_SECONDS =
            3.5f

        private const val COOLDOWN_SECONDS =
            120f

        /*
         * Başlangıçta +%10,
         * maksimumda +%75.
         */
        private const val MINIMUM_SPEED_BONUS =
            0.10f

        private const val MAXIMUM_SPEED_BONUS =
            0.75f

        /*
         * Aktivasyondan hemen sonraki kısa sürede
         * düşük hız nedeniyle Charger bozulmasın.
         */
        private const val STOP_CHECK_GRACE_SECONDS =
            0.18f

        /*
         * Gerçek hareket hızı bu değerin altındaysa
         * karakter durmuş kabul edilir.
         */
        private const val STOPPED_SPEED_EPSILON =
            4f

        /*
         * Charger sırasındaki dönüş tepkisi.
         */
        const val STEERING_RESPONSE =
            3.25f
    }

    private var player:
        Player? =
        null

    private var activeElapsedSeconds =
        0f

    private var pendingEndEvent:
        ChargerEndEvent? =
        null

    val currentMovementSpeedMultiplier:
        Float
        get() {

            if (!isActive) {
                return 1f
            }

            val accelerationProgress =
                (
                    activeElapsedSeconds /
                        ACCELERATION_DURATION_SECONDS
                    )
                    .coerceIn(
                        0f,
                        1f
                    )

            val easedProgress =
                easeOutCubic(
                    accelerationProgress
                )

            val bonus =
                MINIMUM_SPEED_BONUS +
                    (
                        MAXIMUM_SPEED_BONUS -
                            MINIMUM_SPEED_BONUS
                        ) *
                    easedProgress

            return 1f + bonus
        }

    fun attachPlayer(
        player:
        Player
    ) {
        this.player =
            player
    }

    fun updateAfterMovement(
        delta:
        Float
    ) {
        /*
         * Genel ActiveTamga zamanlayıcısını ilerletir.
         */
        update(
            delta
        )

        if (!isActive) {
            return
        }

        val attachedPlayer =
            player
                ?: return

        /*
         * Bu kontrol Player.update() ve collision
         * çözümünden sonra yapılmalıdır.
         */
        if (
            activeElapsedSeconds <
            STOP_CHECK_GRACE_SECONDS
        ) {
            return
        }

        val actualSpeed =
            attachedPlayer
                .actualMovementSpeed

        if (
            actualSpeed <=
            STOPPED_SPEED_EPSILON
        ) {
            /*
             * Eski sistemde bu, patlamayı tamamen iptal
             * ediyordu. Artık interrupt çağrısı yine bitiş
             * olayı oluşturacak ve erken patlama gerçekleşecek.
             */
            interrupt()
        }
    }

    fun consumeEndEvent():
        ChargerEndEvent? {

        val event =
            pendingEndEvent

        pendingEndEvent =
            null

        return event
    }

    override fun onActivated() {
        activeElapsedSeconds =
            0f

        pendingEndEvent =
            null

        player
            ?.beginChargerMovement(
                steeringResponse =
                    STEERING_RESPONSE
            )
    }

    override fun onActiveUpdate(
        delta:
        Float,

        progress:
        Float
    ) {
        activeElapsedSeconds +=
            delta

        player
            ?.setChargerMovementSpeedMultiplier(
                currentMovementSpeedMultiplier
            )
    }

    override fun onActiveEnded(
        reason:
        ActiveTamgaEndReason
    ) {
        val attachedPlayer =
            player

        if (attachedPlayer != null) {

            val finalChargedSeconds =
                activeElapsedSeconds
                    .coerceIn(
                        0f,
                        MAXIMUM_CHARGE_SECONDS
                    )

            /*
             * Artık hem başarılı bitişte hem de interrupt
             * durumunda patlama olayı üretilir.
             */
            pendingEndEvent =
                ChargerEndEvent(
                    reason =
                        reason,

                    centerX =
                        attachedPlayer.centerX,

                    centerY =
                        attachedPlayer.centerY,

                    chargedSeconds =
                        finalChargedSeconds
                )

            attachedPlayer
                .endChargerMovement(
                    stopImmediately =
                        reason ==
                            ActiveTamgaEndReason
                                .COMPLETED
                )
        }

        activeElapsedSeconds =
            0f
    }

    override fun onReset() {
        activeElapsedSeconds =
            0f

        pendingEndEvent =
            null

        player
            ?.endChargerMovement(
                stopImmediately =
                    false
            )
    }

    private fun easeOutCubic(
        value:
        Float
    ): Float {

        val inverse =
            1f -
                value.coerceIn(
                    0f,
                    1f
                )

        return 1f -
            inverse *
            inverse *
            inverse
    }
}
