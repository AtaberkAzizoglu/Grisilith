package com.azizoglu.grisilith.content

enum class ContentTag {

    /*
     * Weapon ana türleri
     */
    MELEE_WEAPON,
    RANGED_WEAPON,
    SPELL_FOCUS,
    MAGICAL_WEAPON,

    /*
     * Weapon kullanım kuralları
     */
    LIGHT_WEAPON,
    HEAVY_WEAPON,
    AMMUNITION,
    VERSATILE,
    THROW,
    TWO_HANDED,
    OFF_HAND,
    DEFENSIVE,

    /*
     * Armor
     */
    LIGHT_ARMOR,
    MEDIUM_ARMOR,
    HEAVY_ARMOR,
    CLOTHES,
    MAGICAL_ARMOR,

    /*
     * Items
     */
    MAGICAL_ITEM,

    /*
     * Skill kategorileri
     */
    WEAPON_SKILL,
    GENERAL_SKILL,
    SPELL_SKILL,

    /*
     * Skill çalışma türleri
     */
    PASSIVE,
    HALF_PASSIVE,
    ACTIVE,
    AURA,

    /*
     * Skill davranışları
     */
    WEAPON_STYLE,
    PROJECTILE_TYPE,
    SPELL,
    SUMMON,
    GLYPH_MOTION,

    /*
     * Hasar bağlantılı skill tagleri
     */
    FIRE_DAMAGE,
    WATER_DAMAGE,
    AIR_DAMAGE,
    EARTH_DAMAGE,
    POISON_DAMAGE,
    ACID_DAMAGE,
    LIGHTNING_DAMAGE,
    NECROTIC_DAMAGE,
    COLD_DAMAGE
}
