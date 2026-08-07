package com.azizoglu.grisilith.enemy.definition

import com.azizoglu.grisilith.enemy.EnemyType

/*
 * İlk merkezi denge değerleri.
 *
 * Unique, Normal'den belirgin güçlü fakat Mini Boss'tan
 * belirgin zayıf kalacak şekilde ayrılmıştır.
 * Bu değerler ileride test aşamasında tek noktadan değiştirilebilir.
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
            healthMultiplier = 7f,
            armorMultiplier = 1.85f,
            damageMultiplier = 2.10f,
            movementSpeedMultiplier = 0.95f,
            attackSpeedMultiplier = 1.15f,
            sizeMultiplier = 1f,
            rewardMultiplier = 5f
        )

    val finalBoss =
        EnemyTypeModifier(
            enemyType = EnemyType.FINAL_BOSS,
            healthMultiplier = 16f,
            armorMultiplier = 2.50f,
            damageMultiplier = 3f,
            movementSpeedMultiplier = 1f,
            attackSpeedMultiplier = 1.25f,
            sizeMultiplier = 1f,
            rewardMultiplier = 12f
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
