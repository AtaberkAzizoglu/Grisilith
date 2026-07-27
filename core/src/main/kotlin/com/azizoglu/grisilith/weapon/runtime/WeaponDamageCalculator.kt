package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.statistics.PlayerStats
import com.badlogic.gdx.math.MathUtils
import kotlin.math.roundToInt

class WeaponDamageCalculator(
    private val playerStats: PlayerStats
) {

    fun calculate(
        weapon: WeaponInstance
    ): WeaponDamageResult {
        val definition =
            weapon.definition

        val levelStats =
            weapon.currentLevelStats

        val damageBeforeCritical =
            calculateNonCriticalDamage(
                weapon
            )
                .toFloat()

        val canCriticallyHit =
            definition.damageTypes.any {
                    damageType ->

                damageType != DamageType.NONE &&
                    damageType.canCriticallyHit
            }

        val isCriticalHit =
            canCriticallyHit &&
                MathUtils.randomBoolean(
                    playerStats
                        .criticalHitChance()
                        .coerceIn(
                            0f,
                            1f
                        )
                )

        val finalDamage =
            if (isCriticalHit) {
                damageBeforeCritical *
                    playerStats
                        .criticalDamageMultiplier()
            } else {
                damageBeforeCritical
            }

        return WeaponDamageResult(
            damage =
                finalDamage
                    .roundToInt()
                    .coerceAtLeast(1),
            isCriticalHit =
                isCriticalHit
        )
    }

    private fun calculateStatScaling(
        weapon: WeaponInstance
    ): Float {
        val scalingEntries =
            weapon.definition.damageScaling

        if (scalingEntries.isEmpty()) {
            return 1f
        }

        var bonusMultiplier = 0f

        scalingEntries.forEach {
                statType,
                scalingWeight ->

            val score =
                playerStats.totalScore(
                    statType
                )

            /*
             * Score 10 nötr kabul edilir.
             *
             * Örnek:
             * Strength 12, weight 1.0
             * = +%16 hasar
             *
             * Her nötr üstü puan temel olarak
             * %8 katkı sağlar.
             */
            val scoreDifference =
                score - 10

            bonusMultiplier +=
                scoreDifference *
                    0.08f *
                    scalingWeight
        }

        return (
            1f + bonusMultiplier
            )
            .coerceAtLeast(0.20f)
    }

    fun calculateNonCriticalDamage(
        weapon: WeaponInstance
    ): Int {
        val definition =
            weapon.definition

        val levelStats =
            weapon.currentLevelStats

        val scalingMultiplier =
            calculateStatScaling(
                weapon = weapon
            )

        return (
            definition.baseDamage *
                levelStats.damageMultiplier *
                scalingMultiplier
            )
            .roundToInt()
            .coerceAtLeast(1)
    }
}
