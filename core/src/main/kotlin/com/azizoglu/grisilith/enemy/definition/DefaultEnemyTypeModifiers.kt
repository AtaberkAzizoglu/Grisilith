package com.azizoglu.grisilith.enemy.definition

import com.azizoglu.grisilith.enemy.EnemyType

/*
 * Type katmanı Kind base statlarının üzerine uygulanır.
 *
 * Güç sırası:
 * NORMAL < UNIQUE << MINI_BOSS << FINAL_BOSS
 *
 * Unique boyutu tasarım gereği her zaman Normal'in %50 fazlasıdır.
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
            healthMultiplier = 2.60f,
            armorMultiplier = 1.50f,
            damageMultiplier = 1.55f,
            movementSpeedMultiplier = 1.06f,
            attackSpeedMultiplier = 1.12f,
            sizeMultiplier = 1.50f,
            rewardMultiplier = 2.25f
        )

    /*
     * Mini Boss Kind'ları zaten yüksek base stat taşır.
     * Bu küçük ek katman 90 saniyelik Boss Stage'in gerçek bir
     * mücadele olmasını garanti eder.
     */
    val miniBoss =
        EnemyTypeModifier(
            enemyType = EnemyType.MINI_BOSS,
            healthMultiplier = 1.15f,
            armorMultiplier = 1.10f,
            damageMultiplier = 1.15f,
            movementSpeedMultiplier = 1f,
            attackSpeedMultiplier = 1.05f,
            sizeMultiplier = 1f,
            rewardMultiplier = 1.25f
        )

    /*
     * Final Boss Stage'in süre sınırı yoktur. Bu yüzden Final Boss
     * özellikle dayanıklı ve tehditkâr tutulur; yalnızca HP değil,
     * Armor, Damage ve saldırı temposu da güçlenir.
     */
    val finalBoss =
        EnemyTypeModifier(
            enemyType = EnemyType.FINAL_BOSS,
            healthMultiplier = 1.35f,
            armorMultiplier = 1.20f,
            damageMultiplier = 1.25f,
            movementSpeedMultiplier = 1.02f,
            attackSpeedMultiplier = 1.10f,
            sizeMultiplier = 1f,
            rewardMultiplier = 1.75f
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
