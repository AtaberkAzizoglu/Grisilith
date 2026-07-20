package com.azizoglu.grisilith.economy

data class MarketOffer(
    val productType: MarketProductType,
    val productLevel: Int,
    val title: String,
    val description: String,
    val price: Int
)
