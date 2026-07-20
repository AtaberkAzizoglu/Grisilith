package com.azizoglu.grisilith.economy

data class MarketSlot(
    val offer: MarketOffer?,
    val isLocked: Boolean = false,
    val isSold: Boolean = false
)
