package com.azizoglu.grisilith.item.definition

enum class ItemRarity(
    val displayName: String,

    val statMultiplier: Float,

    /*
     * Aynı ItemDefinition'dan envanterde taşınabilecek
     * varsayılan maksimum kopya sayısı.
     *
     * ItemDefinition.maximumInventoryCopies verilmişse
     * item-specific limit bunu override eder.
     */
    val maximumInventoryCopies: Int
) {
    COMMON(
        displayName = "Common",
        statMultiplier = 1f,
        maximumInventoryCopies = 7
    ),

    UNCOMMON(
        displayName = "Uncommon",
        statMultiplier = 1.50f,
        maximumInventoryCopies = 7
    ),

    RARE(
        displayName = "Rare",
        statMultiplier = 2f,
        maximumInventoryCopies = 5
    ),

    LEGENDARY(
        displayName = "Legendary",
        statMultiplier = 3f,
        maximumInventoryCopies = 3
    ),

    ARTIFACT(
        displayName = "Artifact",
        statMultiplier = 4f,
        maximumInventoryCopies = 1
    )
}
