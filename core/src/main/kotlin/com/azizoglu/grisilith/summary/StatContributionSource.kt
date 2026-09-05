package com.azizoglu.grisilith.summary

enum class StatContributionSource(
    val displayName: String
) {
    PRESET_OR_BASE(
        "Preset / Base"
    ),

    STAGE_REWARDS(
        "Stage & Chest Stat Rewards"
    ),

    ITEMS(
        "Items"
    ),

    WEAPONS(
        "Weapons"
    ),

    CHARACTER_TRAIT(
        "Character Trait"
    ),

    /*
     * PlayerStats şu anda Armor ve Tamga bonuslarını
     * tek gearBonuses katmanında saklıyor.
     *
     * Ayrı takip sistemi gelene kadar Summary'de yanlış
     * ayrım yapmak yerine doğru birleşik kaynak gösterilir.
     */
    ARMOR_AND_TAMGAS(
        "Armor & Tamgas"
    ),

    OTHER(
        "Other"
    )
}
