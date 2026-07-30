package com.azizoglu.grisilith.tamga.runtime.active

data class ChargerEndEvent(
    val reason: ActiveTamgaEndReason,
    val centerX: Float,
    val centerY: Float,

    /**
     * Charger'ın patlamadan önce ne kadar süre boyunca
     * güç topladığını belirtir.
     *
     * Bu değer 0 ile 3.5 saniye arasında tutulur.
     * Runtime'ın toplam süresi 4.3 saniye olsa bile
     * patlama gücü 3.5 saniyeden sonra daha fazla artmaz.
     */
    val chargedSeconds: Float
) {

    val wasCompletedSuccessfully: Boolean
        get() =
            reason == ActiveTamgaEndReason.COMPLETED
}
