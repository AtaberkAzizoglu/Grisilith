package com.azizoglu.grisilith.collection.detail

import com.azizoglu.grisilith.content.ContentId

object DefaultCollectionDetails {

    /*
     * Her içerik için elle hazırlanmış Collection kartı
     * buradaki listeye eklenecek.
     *
     * contentId, gerçek içerikte kullanılan ID ile
     * tamamen aynı olmalıdır.
     */
    val all:
        List<CollectionDetailDefinition> =
        listOf(

            /*
             * ÖRNEK:
             *
             * Bu kaydı dilediğin gibi değiştirebilir
             * veya şimdilik tamamen silebilirsin.
             */
            CollectionDetailDefinition(
                contentId =
                    ContentId(
                        "WPN_LONGSWORD"
                    ),

                description =
                    "A balanced melee weapon designed for reliable " +
                        "close-range combat.",

                rows =
                    listOf(
                        CollectionDetailRow(
                            label =
                                "Weapon Group",

                            value =
                                "Melee Weapon"
                        ),

                        CollectionDetailRow(
                            label =
                                "Combat Role",

                            value =
                                "Balanced"
                        )
                    ),

                notes =
                    listOf(
                        "Additional Collection information can be written here."
                    )
            )
        )
}
