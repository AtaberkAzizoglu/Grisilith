package com.azizoglu.grisilith.item.runtime

import com.azizoglu.grisilith.item.definition.ItemDefinition
import java.util.UUID

class ItemInstance(
    val definition: ItemDefinition
) {

    /*
     * Aynı Item'dan birden fazla bulunduğunda
     * her kopyayı ayrı ayrı tanımak için.
     */
    val runtimeId: String =
        UUID.randomUUID().toString()

    val displayName: String
        get() =
            definition.displayName

    override fun equals(
        other: Any?
    ): Boolean {
        if (this === other) {
            return true
        }

        if (
            other !is ItemInstance
        ) {
            return false
        }

        return runtimeId ==
            other.runtimeId
    }

    override fun hashCode(): Int {
        return runtimeId.hashCode()
    }
}
