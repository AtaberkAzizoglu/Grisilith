package com.azizoglu.grisilith.enemy.definition

import com.azizoglu.grisilith.enemy.EnemyType

/*
 * Aynı Enemy Kind'ın Normal, Unique, Mini Boss veya Final Boss
 * sürümüne dönüştürülürken kullanılacak bağımsız çarpanlar.
 *
 * Unique boyutu kesin olarak Normal boyutunun %50 fazlasıdır.
 */
data class EnemyTypeModifier(
    val enemyType: EnemyType,
    val healthMultiplier: Float,
    val armorMultiplier: Float,
    val damageMultiplier: Float,
    val movementSpeedMultiplier: Float,
    val attackSpeedMultiplier: Float,
    val sizeMultiplier: Float,
    val rewardMultiplier: Float
) {

    init {
        require(healthMultiplier > 0f)
        require(armorMultiplier >= 0f)
        require(damageMultiplier >= 0f)
        require(movementSpeedMultiplier >= 0f)
        require(attackSpeedMultiplier >= 0f)
        require(sizeMultiplier > 0f)
        require(rewardMultiplier >= 0f)
    }
}
