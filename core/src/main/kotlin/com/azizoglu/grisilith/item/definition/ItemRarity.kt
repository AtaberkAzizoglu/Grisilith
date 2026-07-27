package com.azizoglu.grisilith.item.definition

enum class ItemRarity(
    val displayName: String,

    /*
     * Market randomizer içinde diğer
     * rarity'lere göre temel ağırlık.
     */
    val marketWeight: Float,

    /*
     * İleride aynı Item'ın daha yüksek
     * rarity sürümlerindeki stat çarpanı.
     */
    val statMultiplier: Float,

    /*
     * Aynı ItemDefinition'dan envanterde
     * bulunabilecek maksimum kopya sayısı.
     */
    val maximumInventoryCopies: Int
) {

    COMMON(
        displayName = "Common",
        marketWeight = 1f,
        statMultiplier = 1f,
        maximumInventoryCopies = 10
    ),

    UNCOMMON(
        displayName = "Uncommon",
        marketWeight = 0.60f,
        statMultiplier = 1.50f,
        maximumInventoryCopies = 5
    ),

    RARE(
        displayName = "Rare",
        marketWeight = 0.30f,
        statMultiplier = 2f,
        maximumInventoryCopies = 3
    ),

    LEGENDARY(
        displayName = "Legendary",
        marketWeight = 0.10f,
        statMultiplier = 3f,
        maximumInventoryCopies = 1
    ),

    ARTIFACT(
        displayName = "Artifact",
        marketWeight = 0.03f,
        statMultiplier = 4f,
        maximumInventoryCopies = 1
    )
}
