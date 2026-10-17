package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.item.runtime.ActiveItemEffects
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
        allowCritical: Boolean = true,
        primaryPhysicalStatOverride:
        StatType? = null
    ): WeaponDamageResult {

        val nonCriticalDamage =
            calculateNonCriticalDamage(
                weapon = weapon,
                damageType = damageType,
                primaryPhysicalStatOverride =
                    primaryPhysicalStatOverride
            )

        val isCriticalHit =
            allowCritical &&
                canCriticallyHit(
                    weapon = weapon,
                    damageType = damageType
                ) &&
                MathUtils.randomBoolean(
                    criticalHitChance(weapon)
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
            damageType in
            PHYSICAL_CRITICAL_DAMAGE_TYPES &&
            damageType.canCriticallyHit
    }

    /** Physical weapon abilities may crit even when their damage type is elemental (Air Slash).
     * Keep normal elemental attacks / spell-focus critical rules unchanged.
     */
    fun canAbilityCriticallyHit(weapon: WeaponInstance): Boolean =
        weapon.definition.tags.any {
            it == ContentTag.MELEE_WEAPON || it == ContentTag.RANGED_WEAPON
        }

    fun criticalHitChance(
        weapon: WeaponInstance? = null
    ): Float {

        return (playerStats
            .criticalHitChance() +
            ActiveItemEffects.oddityCriticalChanceBonus(weapon))
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
        damageType: DamageType,
        primaryPhysicalStatOverride:
        StatType? = null
    ): Int {

        val definition =
            weapon.definition

        /*
         * Spell Focus normal saldırıları kendi
         * Wisdom + ilgili magical Stat formülünü
         * kullanmaya devam eder.
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

        val isPhysicalWeapon =
            definition.tags.any { tag ->
                tag == ContentTag.MELEE_WEAPON ||
                    tag == ContentTag.RANGED_WEAPON
            }

        if (isPhysicalWeapon) {
            return calculatePhysicalNormalDamage(
                weapon = weapon,
                primaryStatOverride =
                    primaryPhysicalStatOverride
            )
        }

        /*
         * Beklenmedik / özel Weapon türleri için
         * eski generic scaling güvenli fallback
         * olarak korunuyor.
         */
        return calculateLegacyScaledDamage(
            weapon = weapon
        )
    }

    /*
     * MELEE / RANGED NORMAL DAMAGE
     *
     * Ana sistem:
     *
     * Melee:
     * Base Damage × Strength
     *
     * Ranged:
     * Base Damage × Dexterity
     *
     * Level multiplier ayrıca uygulanır.
     *
     * Dagger normal attacks use Dexterity. Knife Swarm explicitly preserves
     * its existing Strength-based formula until a separate balance decision.
     */
    private fun calculatePhysicalNormalDamage(
        weapon: WeaponInstance,
        primaryStatOverride:
        StatType?
    ): Int {

        val definition =
            weapon.definition

        val levelAdjustedBaseDamage =
            definition.baseDamage *
                weapon.currentLevelStats
                    .damageMultiplier

        /*
         * Shield ayrı bir defensive scaling kullanır.
         *
         * Base Damage × Armor Class × 0.45
         */
        if (
            definition.id.value ==
            SHIELD_ID
        ) {
            val armorClass =
                playerStats
                    .totalScore(
                        StatType.ARMOR_CLASS
                    )
                    .coerceAtLeast(0)

            return (
                levelAdjustedBaseDamage *
                    armorClass *
                    SHIELD_ARMOR_CLASS_SCALING
                )
                .roundToInt()
                .coerceAtLeast(1)
        }

        val primaryStat =
            primaryStatOverride
                ?: determinePhysicalPrimaryStat(
                    weapon = weapon
                )

        val primaryScore =
            playerStats
                .totalScore(
                    primaryStat
                )
                .coerceAtLeast(0)

        /*
         * Strength / Dexterity ana multiplier'dır.
         *
         * Örnek:
         *
         * Base Damage 16
         * Strength 12
         *
         * 16 × 12 = 192
         */
        val primaryDamage =
            levelAdjustedBaseDamage *
                primaryScore

        /*
         * Crossbow / Lance vb. silahlarda
         * definition.damageScaling içinde bulunan
         * ikinci Stat'lar tamamen kaybolmasın.
         *
         * Ana Strength / Dexterity burada tekrar
         * hesaplanmaz.
         */
        val secondaryMultiplier =
            calculateSecondaryPhysicalMultiplier(
                weapon = weapon,
                primaryStat = primaryStat
            )

        return (
            primaryDamage *
                secondaryMultiplier
            )
            .roundToInt()
            .coerceAtLeast(1)
    }

    private fun determinePhysicalPrimaryStat(
        weapon: WeaponInstance
    ): StatType {

        val tags =
            weapon.definition.tags

        /*
         * Saf Ranged Weapon -> Dexterity.
         *
         * Ability-specific legacy scaling can still override this selection.
         */
        if (
            ContentTag.RANGED_WEAPON in tags &&
            ContentTag.MELEE_WEAPON !in tags
        ) {
            return StatType.DEXTERITY
        }

        return StatType.STRENGTH
    }

    private fun calculateSecondaryPhysicalMultiplier(
        weapon: WeaponInstance,
        primaryStat: StatType
    ): Float {

        var bonusMultiplier =
            0f

        weapon.definition
            .damageScaling
            .forEach {
                    statType,
                    scalingWeight ->

                if (
                    statType ==
                    primaryStat
                ) {
                    return@forEach
                }

                /*
                 * Armor Class Shield tarafından
                 * özel olarak hesaplandığı için
                 * burada kullanılmaz.
                 */
                if (
                    weapon.definition.id.value ==
                    SHIELD_ID
                ) {
                    return@forEach
                }

                val score =
                    playerStats
                        .totalScore(
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

    /*
     * Spell Focus skill'lerinin ortak damage foundation'ı.
     *
     * Skill runtime'ları bunun üzerine kendi skill
     * multiplier'larını uygular.
     */
    fun calculateSpellSkillDamage(
        weapon: WeaponInstance,
        damageType: DamageType
    ): Int {

        /*
         * Spell ability foundation:
         *
         * Base Damage × (Wisdom + damage-type secondary stat)
         *
         * Ability-specific coefficient is applied by the ability runtime.
         * Weapon level damageMultiplier is intentionally NOT applied here;
         * Lv.5 spell formulas use the weapon's raw Base Damage.
         */
        val baseDamage =
            weapon.definition.baseDamage

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

        return (
            baseDamage *
                (wisdom + relatedStat)
            )
            .coerceAtLeast(1)
    }

    /** Percentage coefficient is expressed as a ratio (0.000165 = 0.0165%). */
    fun calculateMaximumHealthSpellTickDamage(
        targetMaximumHealth: Int,
        damageType: DamageType,
        ratioPerStatPoint: Float
    ): Int {
        val wisdom = playerStats.totalScore(StatType.WISDOM).coerceAtLeast(0)
        val related = playerStats.totalScore(relatedSpellDamageStat(damageType)).coerceAtLeast(0)
        return (targetMaximumHealth.coerceAtLeast(1).toDouble() * (wisdom + related) * ratioPerStatPoint)
            .roundToInt().coerceAtLeast(1)
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
         * Spell Focus normal attack:
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

            else ->
                StatType.WISDOM
        }
    }

    /*
     * Generic fallback.
     *
     * Şu an ana Melee/Ranged/Spell Focus damage
     * yollarında kullanılmıyor.
     */
    private fun calculateLegacyScaledDamage(
        weapon: WeaponInstance
    ): Int {

        val definition =
            weapon.definition

        val levelStats =
            weapon.currentLevelStats

        val scalingMultiplier =
            calculateLegacyStatScaling(
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

    private fun calculateLegacyStatScaling(
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

        private const val SHIELD_ID =
            "WPN_SHIELD"

        private const val SHIELD_ARMOR_CLASS_SCALING =
            0.45f

        private val PHYSICAL_CRITICAL_DAMAGE_TYPES =
            setOf(
                DamageType.SLASHING,
                DamageType.PIERCING,
                DamageType.BLUDGEONING
            )
    }
}
