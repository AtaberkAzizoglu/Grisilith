package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.statistics.PlayerStats
import com.badlogic.gdx.math.MathUtils
import kotlin.math.roundToInt

class WeaponDamageCalculator(
    private val playerStats:
    PlayerStats
) {

    fun calculate(
        weapon:
        WeaponInstance
    ): WeaponDamageResult {

        val damageType =
            weapon.definition
                .damageTypes
                .firstOrNull { type ->
                    type !=
                        DamageType.NONE
                }
                ?: DamageType.NONE

        return calculateForHit(
            weapon =
                weapon,

            damageType =
                damageType,

            allowCritical =
                true
        )
    }

    fun calculateForHit(
        weapon:
        WeaponInstance,

        damageType:
        DamageType,

        allowCritical:
        Boolean = true
    ): WeaponDamageResult {

        val nonCriticalDamage =
            calculateNonCriticalDamage(
                weapon
            )

        val isCriticalHit =
            allowCritical &&
                canCriticallyHit(
                    weapon =
                        weapon,

                    damageType =
                        damageType
                ) &&
                MathUtils.randomBoolean(
                    criticalHitChance()
                )

        val finalDamage =
            if (isCriticalHit) {
                nonCriticalDamage.toFloat() *
                    criticalDamageMultiplier()
            } else {
                nonCriticalDamage.toFloat()
            }

        return WeaponDamageResult(
            damage =
                finalDamage
                    .roundToInt()
                    .coerceAtLeast(
                        1
                    ),

            isCriticalHit =
                isCriticalHit
        )
    }

    fun canCriticallyHit(
        weapon:
        WeaponInstance,

        damageType:
        DamageType
    ): Boolean {

        val isMeleeOrRangedWeapon =
            weapon.definition
                .tags
                .any { tag ->
                    tag ==
                        ContentTag.MELEE_WEAPON ||
                        tag ==
                        ContentTag.RANGED_WEAPON
                }

        return isMeleeOrRangedWeapon &&
            damageType in
            PHYSICAL_CRITICAL_DAMAGE_TYPES &&
            damageType.canCriticallyHit
    }

    fun criticalHitChance():
        Float {

        return playerStats
            .criticalHitChance()
            .coerceIn(
                0f,
                1f
            )
    }

    fun criticalDamageMultiplier():
        Float {

        return playerStats
            .criticalDamageMultiplier()
            .coerceAtLeast(
                1f
            )
    }

    private fun calculateStatScaling(
        weapon:
        WeaponInstance
    ): Float {

        val scalingEntries =
            weapon.definition
                .damageScaling

        if (scalingEntries.isEmpty()) {
            return 1f
        }

        var bonusMultiplier =
            0f

        scalingEntries.forEach {
                statType,
                scalingWeight ->

            val score =
                playerStats.totalScore(
                    statType
                )

            val scoreDifference =
                score - 10

            bonusMultiplier +=
                scoreDifference *
                    0.08f *
                    scalingWeight
        }

        return (
            1f +
                bonusMultiplier
            )
            .coerceAtLeast(
                0.20f
            )
    }

    fun calculateNonCriticalDamage(
        weapon:
        WeaponInstance
    ): Int {

        val definition =
            weapon.definition

        val levelStats =
            weapon.currentLevelStats

        val scalingMultiplier =
            calculateStatScaling(
                weapon =
                    weapon
            )

        return (
            definition.baseDamage *
                levelStats.damageMultiplier *
                scalingMultiplier
            )
            .roundToInt()
            .coerceAtLeast(
                1
            )
    }

    companion object {

        private val PHYSICAL_CRITICAL_DAMAGE_TYPES =
            setOf(
                DamageType.SLASHING,
                DamageType.PIERCING,
                DamageType.BLUDGEONING
            )
    }
}
