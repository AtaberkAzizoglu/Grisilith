package com.azizoglu.grisilith.weapon.definition

import com.azizoglu.grisilith.content.ContentCategory
import com.azizoglu.grisilith.content.ContentDefinition
import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.content.ContentRarity
import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.content.ContentUnlockState
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.content.StatModifier
import com.azizoglu.grisilith.statistics.StatType

data class WeaponDefinition(
    override val id: ContentId,
    override val displayName: String,
    override val description: String,

    override val tags: Set<ContentTag>,

    val damageTypes: Set<DamageType>,

    /*
     * Wand ve The Book gibi silahlarda
     * her saldırıda listedeki hasar
     * türlerinden biri seçilir.
     */
    val randomDamageTypePerAttack: Boolean = false,

    val targetingType:
    WeaponTargetingType =
        WeaponTargetingType.NEAREST_ENEMY,

    val attackPattern: WeaponAttackPattern,

    /*
     * Common rarity taban değerleri.
     */
    val baseDamage: Int,
    val baseAttacksPerSecond: Float,
    val baseRange: Float,
    val baseProjectileSpeed: Float,
    val baseKnockback: Float,

    val baseProjectileCount: Int = 1,

    /*
     * İlk hedef dahildir.
     * 1 = yalnızca ilk hedef.
     * 2 = ilk hedef ve bir ek hedef.
     */
    val baseMaximumTargets: Int = 1,

    /*
     * Cone gibi saldırılar için.
     */
    val areaAngleDegrees: Float = 0f,

    /*
     * Dairesel ve benzeri alan saldırıları için.
     */
    val areaRadius: Float = 0f,

    /*
     * Dagger gibi iki farklı menzile
     * sahip silahlar için.
     */
    val secondaryRange: Float? = null,

    /*
     * Örnek:
     * Strength -> 1.0
     * Crit Damage -> 0.25
     */
    val damageScaling:
    Map<StatType, Float>,

    /*
     * Silaha sahip olmanın verdiği
     * sürekli stat bonusları.
     */
    val passiveStatModifiers:
    List<StatModifier> =
        emptyList(),

    val rarityStats:
    Map<ContentRarity, WeaponRarityStats>,

    val isUpgradeable: Boolean = true,

    val marketCanOfferHigherRarity:
    Boolean =
        true,

    override val initialUnlockState:
    ContentUnlockState
) : ContentDefinition {

    override val category:
        ContentCategory =
        ContentCategory.WEAPON

    /*
     * Kullanıcının kesinleştirdiği kural:
     * bütün silahlar bir normal slot kullanır.
     */
    val slotCost: Int
        get() = 1

    val usesHasteForNormalAttack: Boolean
        get() =
            ContentTag.SPELL_FOCUS in tags

    val startingRarity:
        ContentRarity
        get() = ContentRarity.COMMON

    init {
        require(displayName.isNotBlank())

        require(damageTypes.isNotEmpty()) {
            "Weapon $id must have at least one damage type."
        }

        require(baseDamage >= 0)
        require(baseAttacksPerSecond >= 0f)
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

        if (isUpgradeable) {
            ContentRarity.entries.forEach { rarity ->
                require(rarity in rarityStats) {
                    "Missing $rarity stats for weapon $id."
                }
            }
        } else {
            require(ContentRarity.COMMON in rarityStats)
        }

        damageScaling.forEach {
                statType,
                multiplier ->

            require(multiplier >= 0f) {
                "Negative scaling is not allowed: $statType"
            }
        }
    }

    fun statsFor(
        rarity: ContentRarity
    ): WeaponRarityStats {
        if (!isUpgradeable) {
            return rarityStats.getValue(
                ContentRarity.COMMON
            )
        }

        return rarityStats.getValue(rarity)
    }
}
