package com.azizoglu.grisilith.enemy

data class EnemyStats(
    val type: EnemyType,
    val maximumHealth: Int,
    val contactDamage: Int,
    val movementSpeed: Float,
    val size: Float
)
