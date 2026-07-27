package com.azizoglu.grisilith.item.runtime

import com.azizoglu.grisilith.content.ContentId

class ItemInventory {

    private val items =
        mutableListOf<ItemInstance>()

    val itemCount: Int
        get() =
            items.size

    val isEmpty: Boolean
        get() =
            items.isEmpty()

    fun addItem(
        item: ItemInstance
    ): ItemInventoryResult {

        val maximumCopies =
            item.definition
                .maximumInventoryCopies
                ?: item.definition
                    .rarity
                    .maximumInventoryCopies

        if (maximumCopies != null) {
            val existingCopyCount =
                countByDefinitionId(
                    item.definition.id
                )

            if (
                existingCopyCount >=
                maximumCopies
            ) {
                return ItemInventoryResult
                    .MAXIMUM_COPIES_REACHED
            }
        }

        items.add(
            item
        )

        return ItemInventoryResult.ADDED
    }

    fun removeItem(
        runtimeId: String
    ): ItemInstance? {
        val itemIndex =
            items.indexOfFirst { item ->
                item.runtimeId ==
                    runtimeId
            }

        if (itemIndex < 0) {
            return null
        }

        return items.removeAt(
            itemIndex
        )
    }

    fun itemAt(
        index: Int
    ): ItemInstance? {
        return items.getOrNull(
            index
        )
    }

    fun countByDefinitionId(
        contentId: ContentId
    ): Int {
        return items.count { item ->
            item.definition.id ==
                contentId
        }
    }

    fun containsItem(
        contentId: ContentId
    ): Boolean {
        return countByDefinitionId(
            contentId
        ) > 0
    }

    /*
     * Dışarıya mutable liste verilmez.
     */
    fun allItems():
        List<ItemInstance> {

        return items.toList()
    }

    fun clear() {
        items.clear()
    }
}
