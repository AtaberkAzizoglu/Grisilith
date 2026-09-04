package com.azizoglu.grisilith.item.definition

import com.azizoglu.grisilith.content.ContentCategory
import com.azizoglu.grisilith.content.ContentDefinition
import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.content.ContentUnlockState
import com.azizoglu.grisilith.content.StatModifier

data class ItemDefinition(
    override val id: ContentId,

    override val displayName: String,

    override val description: String,

    override val tags: Set<ContentTag> =
        setOf(
            ContentTag.PASSIVE_ITEM
        ),

    val rarity: ItemRarity,

    /*
     * Item envanterde bulunduğu sürece
     * oyuncuya verilen pasif bonuslar.
     */
    val statModifiers: List<StatModifier>,

    val effectId: ItemEffectId? = null,

    /*
     * Aynı Item'dan kaç kopya taşınabileceği.
     *
     * null = sınırsız.
     */
    val maximumInventoryCopies: Int? = null,

    val sharedCopyLimitGroup: String? = null,

    val sharedCopyLimit: Int? = null,

    override val initialUnlockState:
    ContentUnlockState =
        ContentUnlockState.UNLOCKED
) : ContentDefinition {

    override val category:
        ContentCategory =
        ContentCategory.ITEM

    init {
        require(
            displayName.isNotBlank()
        ) {
            "Item display name cannot be blank."
        }

        require(
            description.isNotBlank()
        ) {
            "Item description cannot be blank."
        }

        require(
            statModifiers.isNotEmpty() ||
                effectId != null
        ) {
            "Item $id must have a stat modifier or runtime effect."
        }

        maximumInventoryCopies?.let { maximumCopies ->
            require(
                maximumCopies > 0
            ) {
                "Maximum inventory copies must be positive."
            }
        }

        require(
            (sharedCopyLimitGroup == null) ==
                (sharedCopyLimit == null)
        ) {
            "Shared copy-limit group and limit must be declared together for $id."
        }

        sharedCopyLimit?.let { limit ->
            require(limit > 0) {
                "Shared copy limit must be positive for $id."
            }
        }
    }
}
