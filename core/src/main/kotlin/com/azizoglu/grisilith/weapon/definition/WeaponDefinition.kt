package com.azizoglu.grisilith.weapon.definition

import com.azizoglu.grisilith.content.ContentCategory
import com.azizoglu.grisilith.content.ContentDefinition
import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.content.ContentUnlockState
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.content.StatModifier
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.weapon.skill.WeaponAbilityDefinition

data class WeaponDefinition(
    override val id: ContentId,
    override val displayName: String,
    override val description: String,

    override val tags: Set<ContentTag>,

    val damageTypes: Set<DamageType>,

    /*
     * Steam Wand, Tome of Eryndor vb.
     * saldırı başına farklı damage type seçebilir.
     */
    val randomDamageTypePerAttack: Boolean = false,

    /*
     * Spell Focus normal saldırıları Lv.1–2'de
     * damageTypes içinden rastgele tür seçebilir.
     * Lv.3 ve sonrasında bu alan doluysa yalnızca
     * belirtilen tür kullanılır.
     */
    val upgradedBasicAttackDamageType:
    DamageType? = null,

    val targetingType:
    WeaponTargetingType =
        WeaponTargetingType.NEAREST_ENEMY,

    val attackPattern: WeaponAttackPattern,

    val normalAttackSpeedStat:
    StatType =
        StatType.ATTACK_SPEED,

    /*
     * Level 1 taban değerleri.
     */
    val baseDamage: Int,

    /*
     * ATTACK_SPEED kullanan silahlarda:
     * - baseAttacksPerSecond = Attack Speed 0 değeri
     * - maximumAttacksPerSecond = Attack Speed 50 değeri
     *
     * Spell Focus silahları Haste sistemini kullanmaya devam eder.
     */
    val baseAttacksPerSecond: Float,

    val maximumAttacksPerSecond:
    Float =
        baseAttacksPerSecond * 5f,

    val baseRange: Float,
    val baseProjectileSpeed: Float,
    val baseKnockback: Float,

    val baseProjectileCount: Int = 1,

    /*
     * İlk hedef dahildir.
     * 1 = yalnızca ilk hedef.
     */
    val baseMaximumTargets: Int = 1,

    val areaAngleDegrees: Float = 0f,
    val areaRadius: Float = 0f,

    /*
     * Dagger gibi melee ve ranged menzili
     * ayrı olan silahlar için.
     */
    val secondaryRange: Float? = null,

    val damageScaling:
    Map<StatType, Float>,


    /*
 * Silahın ana hasar çarpanını açıkça belirtir.
 *
 * null bırakılırsa damageScaling içindeki
 * en yüksek katsayılı Stat otomatik seçilir.
 *
 * Eşit katsayılarda Map içindeki ilk Stat
 * ana çarpan kabul edilir.
 */
    val primaryDamageScalingStat:
    StatType? = null,

    /*
     * Silah envanterde bulunduğu sürece
     * sürekli verilen taban bonuslar.
     */
    val passiveStatModifiers:
    List<StatModifier> =
        emptyList(),

    /*
     * 1–5 arasındaki bütün level değerleri.
     */
    val levelStats:
    Map<Int, WeaponLevelStats>,

    /*
     * Enchanted Chalk Dust gibi yalnızca
     * tek kopyası bulunabilen içerikler.
     */
    val maximumInventoryCopies: Int? = null,

    /*
     * Level 5'te açılan silaha özgü yetenek.
     *
     * Geçiş sürecinde null bırakılabilir. Weapon Skills
     * paketlerinde silahlar tek tek bu alana bağlanacaktır.
     */
    val weaponAbility:
    WeaponAbilityDefinition? = null,

    override val initialUnlockState:
    ContentUnlockState =
        ContentUnlockState.UNLOCKED
) : ContentDefinition {

    override val category:
        ContentCategory =
        ContentCategory.WEAPON

    val slotCost: Int
        get() = 1

    /*
 * Açıkça atanmış ana Stat varsa onu,
 * yoksa en yüksek katsayılı Stat'ı döndürür.
 */
    val resolvedPrimaryDamageScalingStat:
        StatType?
        get() =
            primaryDamageScalingStat
                ?: damageScaling
                    .maxByOrNull { entry ->
                        entry.value
                    }
                    ?.key

    /*
     * Ana çarpan ilk sırada, diğerleri ardından
     * azalan katsayı sırasıyla döner.
     */
    fun orderedDamageScaling():
        List<Pair<StatType, Float>> {

        val primaryStat =
            resolvedPrimaryDamageScalingStat

        if (primaryStat == null) {
            return emptyList()
        }

        val primaryEntry =
            damageScaling[primaryStat]
                ?.let { multiplier ->
                    primaryStat to multiplier
                }

        val secondaryEntries =
            damageScaling
                .filterKeys { statType ->
                    statType != primaryStat
                }
                .entries
                .sortedByDescending { entry ->
                    entry.value
                }
                .map { entry ->
                    entry.key to entry.value
                }

        return buildList {
            if (primaryEntry != null) {
                add(
                    primaryEntry
                )
            }

            addAll(
                secondaryEntries
            )
        }
    }

    /*
     * Yalnızca Spell Focus / Spell Weapon
     * normal saldırıları Haste kullanır.
     */

    init {
        require(displayName.isNotBlank())

        require(damageTypes.isNotEmpty()) {
            "Weapon $id must have at least one damage type."
        }

        require(baseDamage >= 0)
        require(baseAttacksPerSecond >= 0f)

        require(
            maximumAttacksPerSecond >=
                baseAttacksPerSecond
        ) {
            "Maximum attacks per second cannot be lower than base APS for $id."
        }

        require(baseRange >= 0f)
        require(baseProjectileSpeed >= 0f)
        require(baseKnockback >= 0f)
        require(baseProjectileCount >= 0)
        require(baseMaximumTargets >= 0)
        require(areaAngleDegrees >= 0f)
        require(areaRadius >= 0f)

        if (attackPattern == WeaponAttackPattern.NONE) {
            require(baseDamage == 0)
            require(baseAttacksPerSecond == 0f)
        } else {
            require(baseDamage > 0)
            require(baseAttacksPerSecond > 0f)
            require(baseRange > 0f)
            require(baseProjectileCount > 0)
            require(baseMaximumTargets > 0)
        }

        if (randomDamageTypePerAttack) {
            require(damageTypes.size > 1) {
                "Random damage selection requires multiple damage types."
            }
        }

        upgradedBasicAttackDamageType
            ?.let { upgradedType ->

                require(
                    upgradedType in damageTypes
                ) {
                    "Upgraded basic attack damage type " +
                        "$upgradedType must exist in damageTypes for $id."
                }
            }

        (1..5).forEach { level ->
            require(level in levelStats) {
                "Missing level $level stats for weapon $id."
            }

            require(
                levelStats.getValue(level).level ==
                    level
            ) {
                "Weapon level key and definition level do not match for $id."
            }
        }

        require(
            levelStats.getValue(3)
                .basicAttackUpgraded
        ) {
            "Weapon $id must unlock its basic attack upgrade at level 3."
        }

        require(
            levelStats.getValue(5)
                .weaponAbilityUnlocked
        ) {
            "Weapon $id must unlock its ability at level 5."
        }

        maximumInventoryCopies?.let { maximumCopies ->
            require(maximumCopies > 0)
        }

        damageScaling.forEach {
                statType,
                multiplier ->

            require(multiplier >= 0f) {
                "Negative scaling is not allowed: $statType"
            }
        }

        primaryDamageScalingStat
            ?.let { primaryStat ->

                require(
                    primaryStat in damageScaling
                ) {
                    "Primary damage scaling Stat " +
                        "$primaryStat must exist in " +
                        "damageScaling for weapon $id."
                }
            }
    }

    fun statsForLevel(
        level: Int
    ): WeaponLevelStats {
        val safeLevel =
            level.coerceIn(1, 5)

        return levelStats.getValue(
            safeLevel
        )
    }
}
