package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatType
import com.badlogic.gdx.math.MathUtils
import kotlin.math.roundToInt

class WeaponDamageCalculator(
    private val playerStats: PlayerStats
) {

    fun calculate(
        weapon: WeaponInstance
    ): WeaponDamageResult {

        val damageType =
            weapon.definition
                .damageTypes
                .firstOrNull { type ->
                    type != DamageType.NONE
                }
                ?: DamageType.NONE

        return calculateForHit(
            weapon = weapon,
            damageType = damageType,
            allowCritical = true
        )
    }

    fun calculateForHit(
        weapon: WeaponInstance,
        damageType: DamageType,
        allowCritical: Boolean = true
    ): WeaponDamageResult {

        val nonCriticalDamage =
            calculateNonCriticalDamage(
                weapon = weapon,
                damageType = damageType
            )

        val isCriticalHit =
            allowCritical &&
                canCriticallyHit(
                    weapon = weapon,
                    damageType = damageType
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

        /*
         * Damage Number sistemi için son hesaplanan
         * normal hit bilgisini WeaponInstance üzerinde tutar.
         *
         * Projectile'lar kendi crit roll'ünü yaptığı için
         * onların WeaponHitEvent'i ayrıca kendi bilgisini taşır.
         */
        weapon.recordCalculatedCritical(
            isCriticalHit = isCriticalHit
        )

        return WeaponDamageResult(
            damage =
                finalDamage
                    .roundToInt()
                    .coerceAtLeast(1),
            isCriticalHit =
                isCriticalHit
        )
    }

    fun canCriticallyHit(
        weapon: WeaponInstance,
        damageType: DamageType
    ): Boolean {

        val isMeleeOrRangedWeapon =
            weapon.definition
                .tags
                .any { tag ->
                    tag == ContentTag.MELEE_WEAPON ||
                        tag == ContentTag.RANGED_WEAPON
                }

        return isMeleeOrRangedWeapon &&
            damageType in PHYSICAL_CRITICAL_DAMAGE_TYPES &&
            damageType.canCriticallyHit
    }

    fun criticalHitChance(): Float {

        return playerStats
            .criticalHitChance()
            .coerceIn(
                0f,
                1f
            )
    }

    fun criticalDamageMultiplier(): Float {

        return playerStats
            .criticalDamageMultiplier()
            .coerceAtLeast(
                1f
            )
    }

    fun calculateNonCriticalDamage(
        weapon: WeaponInstance
    ): Int {

        val damageType =
            weapon.definition
                .damageTypes
                .firstOrNull { type ->
                    type != DamageType.NONE
                }
                ?: DamageType.NONE

        return calculateNonCriticalDamage(
            weapon = weapon,
            damageType = damageType
        )
    }

    fun calculateNonCriticalDamage(
        weapon: WeaponInstance,
        damageType: DamageType
    ): Int {

        val definition =
            weapon.definition

        val levelStats =
            weapon.currentLevelStats

        /*
         * Spell Focus normal saldırıları ayrı scaling sistemini
         * kullanır. Attack Speed değil Haste tarafının kullanılması
         * zaten WeaponRuntime/Definition tarafında çözülüyor.
         */
        if (
            definition.tags.contains(
                ContentTag.SPELL_FOCUS
            )
        ) {
            return calculateSpellFocusNormalDamage(
                weapon = weapon,
                damageType = damageType
            )
        }

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

    /*
     * Spell Focus skill'lerinin ortak damage foundation'ı.
     *
     * Skill runtime'ları bunun üzerine kendi skill multiplier'larını
     * uygular: Meteor, Tsunami, Singularity vb.
     *
     * Wisdom ve damage type'ın bağlı olduğu secondary Stat birlikte
     * ölçeklenir.
     */
    fun calculateSpellSkillDamage(
        weapon: WeaponInstance,
        damageType: DamageType
    ): Int {

        val levelAdjustedBaseDamage =
            weapon.definition.baseDamage *
                weapon.currentLevelStats
                    .damageMultiplier

        val wisdom =
            playerStats
                .totalScore(
                    StatType.WISDOM
                )
                .coerceAtLeast(0)

        val relatedStat =
            playerStats
                .totalScore(
                    relatedSpellDamageStat(
                        damageType
                    )
                )
                .coerceAtLeast(0)

        /*
         * Skill Base Damage × Wisdom × ilgili Stat mantığının
         * kontrollü formu.
         *
         * Her iki Stat'ın doğrudan tam integer çarpımı
         * yüzlerce/binlerce hasara fırlayabileceği için score'lar
         * nötr 10 çevresinde normalize edilir.
         *
         * Skill'lerin asıl güç farkı runtime tarafındaki
         * özel multiplier'lardan gelir.
         */
        val wisdomMultiplier =
            wisdom.toFloat() /
                10f

        val relatedStatMultiplier =
            relatedStat.toFloat() /
                10f

        return (
            levelAdjustedBaseDamage *
                wisdomMultiplier *
                relatedStatMultiplier
            )
            .roundToInt()
            .coerceAtLeast(1)
    }

    private fun calculateSpellFocusNormalDamage(
        weapon: WeaponInstance,
        damageType: DamageType
    ): Int {

        val levelAdjustedBaseDamage =
            weapon.definition.baseDamage *
                weapon.currentLevelStats
                    .damageMultiplier

        val wisdom =
            playerStats
                .totalScore(
                    StatType.WISDOM
                )
                .toFloat()

        val relatedStat =
            playerStats
                .totalScore(
                    relatedSpellDamageStat(
                        damageType
                    )
                )
                .toFloat()

        /*
         * Onayladığımız Spell Focus formülü:
         *
         * Damage =
         * (Base Damage × Wisdom × 0.7)
         * +
         * (Base Damage × Related Stat × 0.5)
         */
        return (
            levelAdjustedBaseDamage *
                wisdom *
                0.70f +
                levelAdjustedBaseDamage *
                relatedStat *
                0.50f
            )
            .roundToInt()
            .coerceAtLeast(1)
    }

    private fun relatedSpellDamageStat(
        damageType: DamageType
    ): StatType {

        return when (damageType) {

            DamageType.FIRE,
            DamageType.WATER,
            DamageType.AIR,
            DamageType.EARTH ->
                StatType.ELEMENTAL_DAMAGE

            DamageType.ACID,
            DamageType.LIGHTNING,
            DamageType.NECROTIC,
            DamageType.COLD ->
                StatType.MIGHT_OF_NATURE

            DamageType.POISON ->
                StatType.SHEPHERD

            /*
             * Bu method Spell damage için kullanılıyor.
             * Beklenmedik damage type durumunda Wisdom'a dönerek
             * crash yerine güvenli bir fallback sağlıyoruz.
             */
            else ->
                StatType.WISDOM
        }
    }

    private fun calculateStatScaling(
        weapon: WeaponInstance
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

    companion object {

        private val PHYSICAL_CRITICAL_DAMAGE_TYPES =
            setOf(
                DamageType.SLASHING,
                DamageType.PIERCING,
                DamageType.BLUDGEONING
            )
    }
}
