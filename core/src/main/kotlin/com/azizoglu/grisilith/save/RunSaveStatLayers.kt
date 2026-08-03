package com.azizoglu.grisilith.save

import java.util.HashMap

data class RunSaveStatLayers(
    val baseScores:
    HashMap<String, Int> =
        hashMapOf(),

    val gearBonuses:
    HashMap<String, Int> =
        hashMapOf(),

    val weaponBonuses:
    HashMap<String, Int> =
        hashMapOf(),

    val itemBonuses:
    HashMap<String, Int> =
        hashMapOf(),

    val runBonuses:
    HashMap<String, Int> =
        hashMapOf(),

    val tamgaBonuses:
    HashMap<String, Int> =
        hashMapOf(),

    val activeTamgaBonuses:
    HashMap<String, Int> =
        hashMapOf()
)
