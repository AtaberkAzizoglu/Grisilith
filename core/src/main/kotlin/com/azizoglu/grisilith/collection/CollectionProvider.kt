package com.azizoglu.grisilith.collection

import com.azizoglu.grisilith.content.ContentCategory
import com.azizoglu.grisilith.content.ContentUnlockResolver
import com.azizoglu.grisilith.content.DefaultContentRegistry
import com.azizoglu.grisilith.profile.PlayerProfileRepository

object CollectionProvider {

    fun entries(
        category: ContentCategory
    ): List<CollectionEntry> {

        val profile =
            PlayerProfileRepository
                .profile

        return DefaultContentRegistry
            .instance
            .definitionsForCategory(
                category
            )
            .map { definition ->

                val encountered =
                    when (
                        definition.category
                    ) {

                        ContentCategory.MAP ->
                            profile.hasEncounteredMap(
                                definition.id.value
                            )

                        ContentCategory.ENEMY ->
                            profile.hasEncounteredEnemy(
                                definition.id.value
                            )

                        else ->
                            true
                    }

                CollectionEntry(
                    definition =
                        definition,

                    unlocked =
                        ContentUnlockResolver
                            .isUnlocked(
                                definition
                            ),

                    encountered =
                        encountered
                )
            }
            .sortedWith(
                compareByDescending<CollectionEntry> {
                    /*
                     * Bilinen içerikler bilinmeyenlerden önce.
                     */
                    !it.isUnknown
                }
                    .thenByDescending {
                        it.unlocked
                    }
                    .thenBy {
                        it.definition.displayName
                    }
            )
    }
}
