package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.statistics.PlayerStats
import com.badlogic.gdx.math.MathUtils
import kotlin.math.roundToInt
import com.azizoglu.grisilith.statistics.StatType

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
                weapon =
                    weapon,

                damageType =
                    damageType
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
        WeaponInstance,

        damageType:
        DamageType =
            weapon.definition
                .damageTypes
                .firstOrNull()
                ?: DamageType.NONE
    ): Int {

        val definition =
            weapon.definition

        val levelStats =
            weapon.currentLevelStats

        val levelAdjustedBaseDamage =
            definition.baseDamage.toFloat() *
                levelStats.damageMultiplier

        val isSpellFocus =
            ContentTag.SPELL_FOCUS in
                definition.tags

        if (isSpellFocus) {
            val wisdom =
                playerStats.totalScore(
                    StatType.WISDOM
                )

            val relatedStat =
                relatedSpellDamageStat(
                    damageType
                )

            val relatedStatScore =
                relatedStat
                    ?.let { statType ->
                        playerStats.totalScore(
                            statType
                        )
                    }
                    ?: 0

            val wisdomDamage =
                levelAdjustedBaseDamage *
                    wisdom.toFloat() *
                    SPELL_WISDOM_SCALING

            val relatedStatDamage =
                levelAdjustedBaseDamage *
                    relatedStatScore.toFloat() *
                    SPELL_RELATED_STAT_SCALING

            return (
                wisdomDamage +
                    relatedStatDamage
                )
                .roundToInt()
                .coerceAtLeast(
                    1
                )
        }

        val scalingMultiplier =
            calculateStatScaling(
                weapon =
                    weapon
            )

        return (
            levelAdjustedBaseDamage *
                scalingMultiplier
            )
            .roundToInt()
            .coerceAtLeast(
                1
            )
    }

    fun calculateSpellSkillDamage(
        weapon:
        WeaponInstance,

        damageType:
        DamageType,

        skillPowerMultiplier:
        Float = 1f
    ): Int {

        val relatedStat =
            relatedSpellDamageStat(
                damageType
            )
                ?: return calculateNonCriticalDamage(
                    weapon =
                        weapon,

                    damageType =
                        damageType
                )

        val levelAdjustedBaseDamage =
            weapon.definition
                .baseDamage
                .toFloat() *
                weapon.currentLevelStats
                    .damageMultiplier

        val wisdom =
            playerStats.totalScore(
                StatType.WISDOM
            )

        val relatedStatScore =
            playerStats.totalScore(
                relatedStat
            )

        return (
            levelAdjustedBaseDamage *
                wisdom.toFloat() *
                relatedStatScore.toFloat() *
                SPELL_SKILL_SCALING *
                skillPowerMultiplier
            )
            .roundToInt()
            .coerceAtLeast(
                1
            )
    }

    fun relatedSpellDamageStat(
        damageType:
        DamageType
    ): StatType? {

        return when (damageType) {
            DamageType.FIRE,
            DamageType.WATER,
            DamageType.AIR -> {
                StatType.ELEMENTAL_DAMAGE
            }

            DamageType.ACID,
            DamageType.LIGHTNING,
            DamageType.NECROTIC -> {
                StatType.MIGHT_OF_NATURE
            }

            DamageType.POISON -> {
                StatType.SHEPHERD
            }

            else -> {
                null
            }
        }
    }

    companion object {

        private const val SPELL_WISDOM_SCALING =
            0.70f

        private const val SPELL_RELATED_STAT_SCALING =
            0.50f

        private const val SPELL_SKILL_SCALING =
            0.10f

        private val PHYSICAL_CRITICAL_DAMAGE_TYPES =
            setOf(
                DamageType.SLASHING,
                DamageType.PIERCING,
                DamageType.BLUDGEONING
            )
    }
}
