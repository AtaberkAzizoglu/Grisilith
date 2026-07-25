package com.azizoglu.grisilith.economy

import com.azizoglu.grisilith.weapon.definition.WeaponDefinition

data class MarketOffer(
    val productType: MarketProductType,
    val productLevel: Int,
    val title: String,
    val description: String,
    val price: Int,

    /*
     * WEAPON tekliflerinde gerçek silah tanımı.
     *
     * Armor ve Item sistemleri geldiğinde
     * onlar için de benzer alanlar eklenecek.
     */
    val weaponDefinition:
    WeaponDefinition? = null
)
