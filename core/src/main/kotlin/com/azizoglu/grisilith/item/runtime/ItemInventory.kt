package com.azizoglu.grisilith.item.runtime

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.save.RunSaveItemStack

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

        if (!canAddDefinition(item.definition)) {
            return ItemInventoryResult
                .MAXIMUM_COPIES_REACHED
        }

        items.add(
            item
        )

        return ItemInventoryResult.ADDED
    }

    fun canAddDefinition(
        definition:
        com.azizoglu.grisilith.item.definition.ItemDefinition
    ): Boolean {

        val maximumCopies =
            definition
                .maximumInventoryCopies
                ?: definition
                    .rarity
                    .maximumInventoryCopies

        if (maximumCopies != null) {
            val existingCopyCount =
                countByDefinitionId(
                    definition.id
                )

            if (
                existingCopyCount >=
                maximumCopies
            ) {
                return false
            }
        }

        val sharedGroup =
            definition.sharedCopyLimitGroup

        val sharedLimit =
            definition.sharedCopyLimit

        if (
            sharedGroup != null &&
            sharedLimit != null
        ) {
            val sharedCount =
                items.count { existing ->
                    existing.definition.sharedCopyLimitGroup ==
                        sharedGroup
                }

            if (sharedCount >= sharedLimit) {
                return false
            }
        }

        return true
    }

    fun consumeOneDefinition(definitionId: ContentId): Boolean {
        val index = items.indexOfFirst { it.definition.id == definitionId }
        if (index < 0) return false
        items.removeAt(index)
        return true
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

    fun replaceOneDefinition(
        from: com.azizoglu.grisilith.item.definition.ItemDefinition,
        to: com.azizoglu.grisilith.item.definition.ItemDefinition
    ): Boolean {
        val index = items.indexOfFirst { it.definition.id == from.id }
        if (index < 0) return false
        items[index] = ItemInstance(definition = to)
        return true
    }

    fun removeAllExceptDefinition(
        definition: com.azizoglu.grisilith.item.definition.ItemDefinition
    ): Int {
        val previousSize = items.size
        items.removeAll { it.definition.id != definition.id }
        return previousSize - items.size
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

    fun exportSaveStacks():
        List<RunSaveItemStack> {

        return items
            .groupBy {
                it.definition.id
                    .toString()
            }
            .map {
                    entry ->

                RunSaveItemStack(
                    itemDefinitionId =
                        entry.key,

                    count =
                        entry.value.size
                )
            }
            .sortedBy {
                it.itemDefinitionId
            }
    }
}
