package com.azizoglu.grisilith.weapon.definition

import com.azizoglu.grisilith.content.ContentRarity
import com.azizoglu.grisilith.content.StatModifier

data class WeaponRarityStats(
    val rarity: ContentRarity,

    /*
     * Değerler Common taban değerlerinin
     * üzerine uygulanan kümülatif çarpanlardır.
     */
    val damageMultiplier: Float = 1f,
    val attackSpeedMultiplier: Float = 1f,
    val rangeMultiplier: Float = 1f,
    val knockbackMultiplier: Float = 1f,

    /*
     * Normal saldırının vurabileceği
     * fazladan hedef sayısı.
     */
    val additionalPiercingTargets: Int = 0,

    /*
     * Bu rarity seviyesinin verdiği
     * garantili character stat bonusları.
     */
    val grantedStatModifiers:
    List<StatModifier> =
        emptyList()
) {
    init {
        require(damageMultiplier > 0f)
        require(attackSpeedMultiplier > 0f)
        require(rangeMultiplier > 0f)
        require(knockbackMultiplier >= 0f)
        require(additionalPiercingTargets >= 0)
    }
}
