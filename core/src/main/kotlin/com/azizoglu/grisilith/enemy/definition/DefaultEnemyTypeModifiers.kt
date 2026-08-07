package com.azizoglu.grisilith.enemy.definition

import com.azizoglu.grisilith.enemy.EnemyType

/*
 * Enemy Kind katalogları Mini Boss ve Final Boss kimliklerini
 * kendi temel statlarında zaten taşır.
 *
 * Bu nedenle büyük type çarpanları yalnızca Normal Kind'ın
 * Unique sürümünü üretirken kullanılır. Mini Boss ve Final Boss
 * için küçük runtime ayarları bırakılmıştır.
 */
object DefaultEnemyTypeModifiers {

    val normal =
        EnemyTypeModifier(
            enemyType = EnemyType.NORMAL,
            healthMultiplier = 1f,
            armorMultiplier = 1f,
            damageMultiplier = 1f,
            movementSpeedMultiplier = 1f,
            attackSpeedMultiplier = 1f,
            sizeMultiplier = 1f,
            rewardMultiplier = 1f
        )

    val unique =
        EnemyTypeModifier(
            enemyType = EnemyType.UNIQUE,
            healthMultiplier = 2.25f,
            armorMultiplier = 1.35f,
            damageMultiplier = 1.40f,
            movementSpeedMultiplier = 1.05f,
            attackSpeedMultiplier = 1.10f,
            sizeMultiplier = 1.50f,
            rewardMultiplier = 2f
        )

    val miniBoss =
        EnemyTypeModifier(
            enemyType = EnemyType.MINI_BOSS,
            healthMultiplier = 1f,
            armorMultiplier = 1f,
            damageMultiplier = 1f,
            movementSpeedMultiplier = 1f,
            attackSpeedMultiplier = 1f,
            sizeMultiplier = 1f,
            rewardMultiplier = 1f
        )

    val finalBoss =
        EnemyTypeModifier(
            enemyType = EnemyType.FINAL_BOSS,
            healthMultiplier = 1f,
            armorMultiplier = 1f,
            damageMultiplier = 1f,
            movementSpeedMultiplier = 1f,
            attackSpeedMultiplier = 1f,
            sizeMultiplier = 1f,
            rewardMultiplier = 1f
        )

    val all:
        Map<EnemyType, EnemyTypeModifier> =
        listOf(
            normal,
            unique,
            miniBoss,
            finalBoss
        )
            .associateBy { modifier ->
                modifier.enemyType
            }

    fun forType(
        enemyType: EnemyType
    ): EnemyTypeModifier {
        return requireNotNull(
            all[enemyType]
        ) {
            "Missing EnemyType modifier for $enemyType."
        }
    }
}
