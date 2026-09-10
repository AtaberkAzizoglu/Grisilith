package com.azizoglu.grisilith.combat.damage

data class DamageNumberEvent(
    val amount: Int,
    val worldX: Float,
    val worldY: Float,
    var isCriticalHit: Boolean = false,
    val displayText: String? = null,
    val isMiss: Boolean = false
)
