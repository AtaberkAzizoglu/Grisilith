package com.azizoglu.grisilith.enemy.trait

data class EnemyTraitModifiers(
    val maximumHealthMultiplier: Float = 1f,
    val damageMultiplier: Float = 1f,
    val movementSpeedMultiplier: Float = 1f,
    val attackSpeedMultiplier: Float = 1f
) {

    fun multipliedBy(
        other: EnemyTraitModifiers
    ): EnemyTraitModifiers {
        return EnemyTraitModifiers(
            maximumHealthMultiplier =
                maximumHealthMultiplier *
                    other.maximumHealthMultiplier,

            damageMultiplier =
                damageMultiplier *
                    other.damageMultiplier,

            movementSpeedMultiplier =
                movementSpeedMultiplier *
                    other.movementSpeedMultiplier,

            attackSpeedMultiplier =
                attackSpeedMultiplier *
                    other.attackSpeedMultiplier
        )
    }

    companion object {
        val IDENTITY =
            EnemyTraitModifiers()
    }
}
