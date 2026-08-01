package com.azizoglu.grisilith.collection.detail

import com.azizoglu.grisilith.content.ContentId

data class CollectionDetailDefinition(
    val contentId: ContentId,

    /*
     * Kartta gösterilecek ana açıklama.
     *
     * ContentDefinition.description alanından bağımsızdır.
     */
    val description: String,

    /*
     * Weapon Type, Damage, Rarity vb.
     * özel bilgi satırları.
     */
    val rows: List<CollectionDetailRow> =
        emptyList(),

    /*
     * Kartın alt kısmında gösterilecek ek notlar.
     */
    val notes: List<String> =
        emptyList()
) {

    init {
        require(
            description.isNotBlank()
        ) {
            "Collection detail description cannot be blank."
        }

        require(
            notes.none {
                it.isBlank()
            }
        ) {
            "Collection detail notes cannot contain blank values."
        }
    }
}
