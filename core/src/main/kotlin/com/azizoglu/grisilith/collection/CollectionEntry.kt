package com.azizoglu.grisilith.collection

import com.azizoglu.grisilith.content.ContentCategory
import com.azizoglu.grisilith.content.ContentDefinition

data class CollectionEntry(
    val definition: ContentDefinition,
    val unlocked: Boolean,
    val encountered: Boolean
) {

    /*
     * Yalnızca Enemy ve Map içerikleri karşılaşma
     * bilgisine ihtiyaç duyar.
     */
    val requiresEncounter:
        Boolean
        get() =
            definition.category ==
                ContentCategory.ENEMY ||
                definition.category ==
                ContentCategory.MAP

    /*
     * Hiç karşılaşılmamış Enemy veya Map.
     *
     * Collection ekranında ismi ve açıklaması yerine
     * ???? gösterilir ve detay paneli açılamaz.
     */
    val isUnknown:
        Boolean
        get() =
            requiresEncounter &&
                !encountered
}
