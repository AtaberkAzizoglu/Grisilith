package com.azizoglu.grisilith.tamga.definition

import com.azizoglu.grisilith.content.ContentCategory
import com.azizoglu.grisilith.content.ContentDefinition
import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.content.ContentUnlockState
import com.azizoglu.grisilith.content.StatModifier
import com.azizoglu.grisilith.statistics.StatType

data class TamgaDefinition(
    override val id:
    ContentId,

    override val displayName:
    String,

    override val description:
    String,

    /*
     * WEAPON:
     * Yalnızca Weapon Tamga slotu.
     *
     * ARMOR:
     * Yalnızca Armor Tamga slotu.
     *
     * GENERAL:
     * Her iki Tamga slotu.
     */
    val slotType:
    TamgaSlotType,

    val activationType:
    TamgaActivationType,

    val effectId:
    TamgaEffectId,

    /*
     * Tamga aktif veya pasif olarak hangi
     * Statlarla doğrudan etkileşime giriyor?
     *
     * Bunlar otomatik Stat bonusu değildir.
     * Detay ekranı ve runtime açıklamaları için
     * metadata olarak tutulur.
     */
    val interactableStats:
    Set<StatType> =
        emptySet(),

    /*
     * Tamga takılı olduğu sürece doğrudan
     * uygulanacak sabit Stat bonusları.
     *
     * Stage'e göre değişen veya şartlı bonuslar
     * burada tutulmaz; runtime effect tarafından
     * hesaplanır.
     */
    val statModifiers:
    List<StatModifier> =
        emptyList(),

    /*
     * Tamga'nın çalışması için seçili Weapon'da
     * bulunması gereken ContentTag değerleri.
     *
     * Örnek:
     * MELEE_WEAPON
     * SPELL_FOCUS
     */
    val requiredWeaponTags:
    Set<ContentTag> =
        emptySet(),

    /*
     * Tamga'nın çalışması için seçili Armor'da
     * bulunması gereken ContentTag değerleri.
     *
     * Örnek:
     * LIGHT_ARMOR
     * HEAVY_ARMOR
     */
    val requiredArmorTags:
    Set<ContentTag> =
        emptySet(),

    /*
     * AURA, FIRE_DAMAGE gibi genel ContentTag
     * değerleri.
     *
     * ACTIVE tagı activationType üzerinden
     * otomatik olarak eklenir.
     */
    val additionalTags:
    Set<ContentTag> =
        emptySet(),

    override val initialUnlockState:
    ContentUnlockState =
        ContentUnlockState.UNLOCKED
) : ContentDefinition {

    override val category:
        ContentCategory =
        ContentCategory.TAMGA

    override val tags:
        Set<ContentTag> =
        buildSet {
            addAll(
                additionalTags
            )

            if (
                activationType ==
                TamgaActivationType.ACTIVE
            ) {
                add(
                    ContentTag.ACTIVE
                )
            }
        }

    init {
        require(
            displayName.isNotBlank()
        ) {
            "Tamga display name cannot be blank."
        }

        require(
            description.isNotBlank()
        ) {
            "Tamga description cannot be blank."
        }

        /*
         * Weapon Tamga, Armor'a bağlı bir
         * gereksinim taşıyamaz.
         */
        if (
            slotType ==
            TamgaSlotType.WEAPON
        ) {
            require(
                requiredArmorTags.isEmpty()
            ) {
                "Weapon Tamga $id cannot require Armor tags."
            }
        }

        /*
         * Armor Tamga, Weapon'a bağlı bir
         * gereksinim taşıyamaz.
         */
        if (
            slotType ==
            TamgaSlotType.ARMOR
        ) {
            require(
                requiredWeaponTags.isEmpty()
            ) {
                "Armor Tamga $id cannot require Weapon tags."
            }
        }

        /*
         * Weapon gereksinimleri yalnızca gerçek
         * Weapon tür taglarından oluşabilir.
         */
        require(
            requiredWeaponTags.all { tag ->
                tag == ContentTag.MELEE_WEAPON ||
                    tag == ContentTag.RANGED_WEAPON ||
                    tag == ContentTag.SPELL_FOCUS
            }
        ) {
            "Tamga $id has an unsupported Weapon requirement."
        }

        /*
         * Armor gereksinimleri yalnızca gerçek
         * Armor tür taglarından oluşabilir.
         */
        require(
            requiredArmorTags.all { tag ->
                tag == ContentTag.LIGHT_ARMOR ||
                    tag == ContentTag.MEDIUM_ARMOR ||
                    tag == ContentTag.HEAVY_ARMOR ||
                    tag == ContentTag.CLOTHES
            }
        ) {
            "Tamga $id has an unsupported Armor requirement."
        }
    }

    val isActive: Boolean
        get() =
            activationType ==
                TamgaActivationType.ACTIVE

    val isPassive: Boolean
        get() =
            activationType ==
                TamgaActivationType.PASSIVE

    val canUseWeaponSlot: Boolean
        get() =
            slotType ==
                TamgaSlotType.WEAPON ||
                slotType ==
                TamgaSlotType.GENERAL

    val canUseArmorSlot: Boolean
        get() =
            slotType ==
                TamgaSlotType.ARMOR ||
                slotType ==
                TamgaSlotType.GENERAL
}
