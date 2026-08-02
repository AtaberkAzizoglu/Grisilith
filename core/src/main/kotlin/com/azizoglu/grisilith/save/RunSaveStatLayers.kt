package com.azizoglu.grisilith.save

data class RunSaveStatLayers(
    val baseScores: Map<String, Int> = emptyMap(),
    val gearBonuses: Map<String, Int> = emptyMap(),
    val weaponBonuses: Map<String, Int> = emptyMap(),
    val itemBonuses: Map<String, Int> = emptyMap(),
    val runBonuses: Map<String, Int> = emptyMap(),
    val tamgaBonuses: Map<String, Int> = emptyMap(),
    val activeTamgaBonuses: Map<String, Int> = emptyMap()
)
