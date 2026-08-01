package com.azizoglu.grisilith.collection.detail

import com.azizoglu.grisilith.content.ContentId

object CollectionDetailRegistry {

    private val definitionsById:
        Map<ContentId, CollectionDetailDefinition> =
        DefaultCollectionDetails
            .all
            .associateBy {
                it.contentId
            }

    init {
        require(
            definitionsById.size ==
                DefaultCollectionDetails
                    .all
                    .size
        ) {
            "Duplicate Collection detail ContentId detected."
        }
    }

    fun find(
        contentId: ContentId
    ): CollectionDetailDefinition? {

        return definitionsById[
            contentId
        ]
    }
}
