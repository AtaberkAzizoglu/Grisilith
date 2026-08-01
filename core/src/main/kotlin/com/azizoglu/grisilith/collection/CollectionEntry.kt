package com.azizoglu.grisilith.collection

import com.azizoglu.grisilith.content.ContentCategory
import com.azizoglu.grisilith.content.ContentDefinition

data class CollectionEntry(
    val definition: ContentDefinition,
    val unlocked: Boolean,
    val encountered: Boolean
) {

    val requiresEncounter:
        Boolean
        get() =
            definition.category ==
                ContentCategory.ENEMY ||
                definition.category ==
                ContentCategory.MAP

    val isUnknown:
        Boolean
        get() =
            requiresEncounter &&
                !encountered
}
