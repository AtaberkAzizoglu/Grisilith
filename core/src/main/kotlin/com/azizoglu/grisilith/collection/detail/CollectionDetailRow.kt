package com.azizoglu.grisilith.collection.detail

data class CollectionDetailRow(
    val label: String,
    val value: String
) {

    init {
        require(
            label.isNotBlank()
        ) {
            "Collection detail row label cannot be blank."
        }

        require(
            value.isNotBlank()
        ) {
            "Collection detail row value cannot be blank."
        }
    }
}
