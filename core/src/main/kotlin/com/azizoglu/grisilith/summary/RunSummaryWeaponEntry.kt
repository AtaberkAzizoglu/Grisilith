package com.azizoglu.grisilith.summary

data class RunSummaryWeaponEntry(
    val definitionId: String,
    val displayName: String,
    val description: String,
    val level: Int,
    val damageDealt: Float
)
