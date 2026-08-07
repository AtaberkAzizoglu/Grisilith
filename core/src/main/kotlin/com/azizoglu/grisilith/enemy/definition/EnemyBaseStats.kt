package com.azizoglu.grisilith.enemy.definition

/*
 * Stage, Difficulty, Map ve EnemyType çarpanları uygulanmadan
 * önce bir Enemy Kind'ın taşıdığı temel değerler.
 */
data class EnemyBaseStats(
    val maximumHealth: Int,
    val armorClass: Int,
    val contactDamage: Int,
    val movementSpeed: Float,
    val attacksPerSecond: Float,
    val attackRange: Float
) {

    init {
        require(maximumHealth > 0) {
            "Enemy maximum health must be greater than zero."
        }

        require(armorClass >= 0) {
            "Enemy armor class cannot be negative."
        }

        require(contactDamage >= 0) {
            "Enemy contact damage cannot be negative."
        }

        require(movementSpeed >= 0f) {
            "Enemy movement speed cannot be negative."
        }

        require(attacksPerSecond >= 0f) {
            "Enemy attacks per second cannot be negative."
        }

        require(attackRange >= 0f) {
            "Enemy attack range cannot be negative."
        }
    }
}
