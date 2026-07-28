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
 * Armor:
 * - Run marketinde çıkmaz.
 * - Sandıktan düşmez.
 * - Run sırasında değiştirilemez.
 * - Yalnızca Preset üzerinden seçilir.
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

    /*
     * Armor'un doğrudan Max HP hesabına yaptığı
     * temel katkı.
     *
     * PlayerStats içindeki MAX_HP Gear bonusuna
     * dönüştürülecektir.
     */
    val baseMaximumHealthScore:
    Int,

    /*
     * Armor'un damage reduction hesabına yaptığı
     * temel Armor Class katkısı.
     */
    val baseArmorClassScore:
    Int,

    /*
     * Movement Speed, Wisdom, Luck vb. ek
     * bonuslar ve cezalar.
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
        require(
            displayName.isNotBlank()
        ) {
            "Armor display name cannot be blank."
        }

        require(
            description.isNotBlank()
        ) {
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

        /*
         * Negatif değerler Robe gibi savunmasız
         * Armor'lar için geçerlidir.
         *
         * Aşırı yanlış veri girişini önlemek için
         * makul bir güvenlik aralığı kullanılır.
         */
        require(
            baseMaximumHealthScore in
                -10..10
        ) {
            "Armor Max HP score must be between -10 and 10."
        }

        require(
            baseArmorClassScore in
                -10..10
        ) {
            "Armor Class score must be between -10 and 10."
        }

        /*
         * Temel HP ve Armor değerleri ayrı alanlarda
         * olduğu için modifier listesinde tekrar
         * bulunmalarına izin vermiyoruz.
         */
        require(
            statModifiers.none { modifier ->
                modifier.statType ==
                    StatType.MAX_HP ||
                    modifier.statType ==
                    StatType.ARMOR_CLASS
            }
        ) {
            "Use baseMaximumHealthScore and " +
                "baseArmorClassScore for Armor HP/Armor values."
        }
    }

    /*
     * Armor'un PlayerStats Gear katmanına
     * uygulanacak bütün değişiklikleri döndürür.
     */
    fun allStatModifiers():
        List<StatModifier> {

        val modifiers =
            mutableListOf<StatModifier>()

        if (baseMaximumHealthScore != 0) {
            modifiers +=
                StatModifier(
                    statType =
                        StatType.MAX_HP,

                    amount =
                        baseMaximumHealthScore
                )
        }

        if (baseArmorClassScore != 0) {
            modifiers +=
                StatModifier(
                    statType =
                        StatType.ARMOR_CLASS,

                    amount =
                        baseArmorClassScore
                )
        }

        modifiers +=
            statModifiers

        return modifiers
    }
}
