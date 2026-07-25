package com.azizoglu.grisilith.weapon.runtime

enum class WeaponInventoryResult {

    /*
     * Yeni silah boş slota yerleştirildi.
     */
    ADDED_TO_EMPTY_SLOT,

    /*
     * İki aynı silah sürükleyip bırakılarak
     * birleştirildi.
     */
    MERGED,

    /*
     * Enchanted Chalk Dust'ın mevcut kopyası
     * doğrudan bir seviye yükseltildi.
     */
    UNIQUE_WEAPON_LEVELLED_UP,

    /*
     * Altı normal weapon slotu dolu.
     */
    INVENTORY_FULL,

    /*
     * Birleştirilmeye çalışılan silahlar
     * aynı silah veya aynı level değil.
     */
    INCOMPATIBLE_WEAPONS,

    /*
     * Silah zaten maksimum seviyede.
     */
    MAXIMUM_LEVEL_REACHED,

    /*
     * Kaynak veya hedef slot geçersiz ya da boş.
     */
    INVALID_SLOT
}
