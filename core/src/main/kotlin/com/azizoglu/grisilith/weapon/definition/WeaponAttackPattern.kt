package com.azizoglu.grisilith.weapon.definition

enum class WeaponAttackPattern {

    /*
     * Saldırı yapmayan Shield gibi içerikler.
     */
    NONE,

    /*
     * Shortsword:
     * Hedef yönünde kısa ve ince alan saldırıları.
     */
    SHORT_SLASH_LINES,

    /*
     * Longsword:
     * Hedef yönünde konik alan saldırısı.
     */
    FORWARD_CONE,

    /*
     * Lance:
     * Hedefin bulunduğu bölgede dairesel saldırı.
     */
    TARGET_AREA_CIRCLE,

    /*
     * Dagger:
     * Yakındaysa melee, uzaktaysa projectile.
     */
    MELEE_OR_THROW,

    /*
     * Standart tekli projectile.
     */
    STRAIGHT_PROJECTILE,

    /*
     * Hedefe çarptıktan sonra devam edebilen projectile.
     */
    PIERCING_PROJECTILE
}
