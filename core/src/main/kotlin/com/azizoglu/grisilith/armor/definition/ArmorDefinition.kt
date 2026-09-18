package com.azizoglu.grisilith.armor.definition

import com.azizoglu.grisilith.content.ContentCategory
import com.azizoglu.grisilith.content.ContentDefinition
import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.content.ContentUnlockState
import com.azizoglu.grisilith.content.StatModifier
import com.azizoglu.grisilith.statistics.StatType

/*
 * Preset sırasında kuşanılan tek Armor'u tanımlar.
 *
 * Base HP ve Base Armor normal Stat bonusları değildir:
 * - Current Max HP = Base HP * Max HP
 * - Raw Damage Reduce = Base Armor * Armor Class
 */
data class ArmorDefinition(
    override val id:
    ContentId,

    override val displayName:
    String,

    override val description:
    String,

    override val tags:
    Set<ContentTag>,

    val baseHealth:
    Int,

    val baseArmor:
    Int,

    val baseMagicResistance: Int = 0,

    /*
     * Movement Speed, Attack Speed, Wisdom, Luck vb.
     * gerçek Stat değişiklikleri.
     */
    val statModifiers:
    List<StatModifier> =
        emptyList(),

    override val initialUnlockState:
    ContentUnlockState =
        ContentUnlockState.UNLOCKED
) : ContentDefinition {

    override val category:
        ContentCategory =
        ContentCategory.ARMOR

    init {
        require(displayName.isNotBlank()) {
            "Armor display name cannot be blank."
        }

        require(description.isNotBlank()) {
            "Armor description cannot be blank."
        }

        require(
            tags.any { tag ->
                tag == ContentTag.LIGHT_ARMOR ||
                    tag == ContentTag.MEDIUM_ARMOR ||
                    tag == ContentTag.HEAVY_ARMOR ||
                    tag == ContentTag.CLOTHES
            }
        ) {
            "Armor $id must have an Armor type tag."
        }

        require(baseHealth > 0) {
            "Armor Base HP must be greater than zero."
        }

        require(baseArmor >= 0) {
            "Armor Base Armor cannot be negative."
        }

        require(baseMagicResistance >= 0) {
            "Armor Base Magic Resistance cannot be negative."
        }

        require(
            statModifiers.none { modifier ->
                modifier.statType == StatType.MAX_HP ||
                    modifier.statType == StatType.ARMOR_CLASS
            }
        ) {
            "Base HP and Base Armor must use their dedicated fields."
        }
    }

    /*
     * Gear bonus katmanına yalnızca gerçek Stat modifier'ları gider.
     * Base HP ve Base Armor bu listeye dahil edilmez.
     */
    fun allStatModifiers():
        List<StatModifier> =
        statModifiers
}
