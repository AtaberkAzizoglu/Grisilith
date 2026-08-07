package com.azizoglu.grisilith.enemy.behavior

data class EnemyBehaviorProfile(
    val movementMode: EnemyMovementMode,
    val preferredRangeMultiplier: Float = 1f,
    val retreatRangeMultiplier: Float = 0.70f
) {

    init {
        require(preferredRangeMultiplier >= 0f)
        require(retreatRangeMultiplier >= 0f)
        require(
            retreatRangeMultiplier <=
                preferredRangeMultiplier
        ) {
            "Retreat range cannot exceed preferred range."
        }
    }
}
