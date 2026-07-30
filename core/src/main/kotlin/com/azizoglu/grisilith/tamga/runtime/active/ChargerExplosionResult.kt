package com.azizoglu.grisilith.tamga.runtime.active

import com.azizoglu.grisilith.enemy.Enemy

data class ChargerExplosionResult(
    val enemy:
    Enemy,

    val appliedDamage:
    Int,

    val wasKilled:
    Boolean
)
