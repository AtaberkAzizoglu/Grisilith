package com.azizoglu.grisilith.economy

class PickupRangeController(
    private val baseRadius: Float =
        DEFAULT_BASE_RADIUS
) {

    private var flatBonus =
        0f

    private var multiplier =
        1f

    val effectiveRadius: Float
        get() =
            (
                (
                    baseRadius +
                        flatBonus
                    ) *
                    multiplier
                )
                .coerceAtLeast(
                    MINIMUM_RADIUS
                )

    fun setFlatBonus(
        amount: Float
    ) {
        flatBonus =
            amount.coerceAtLeast(
                0f
            )
    }

    fun addFlatBonus(
        amount: Float
    ) {
        flatBonus =
            (
                flatBonus +
                    amount
                )
                .coerceAtLeast(
                    0f
                )
    }

    fun setMultiplier(
        value: Float
    ) {
        multiplier =
            value.coerceAtLeast(
                0.10f
            )
    }

    fun resetModifiers() {
        flatBonus = 0f
        multiplier = 1f
    }

    companion object {
        const val DEFAULT_BASE_RADIUS =
            62f

        private const val MINIMUM_RADIUS =
            10f
    }
}
