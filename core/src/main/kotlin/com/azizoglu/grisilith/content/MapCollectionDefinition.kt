package com.azizoglu.grisilith.content

import com.azizoglu.grisilith.config.GameMap

data class MapCollectionDefinition(
    val gameMap: GameMap
) : ContentDefinition {

    override val id =
        ContentId(
            CollectionEncounterIds.map(
                gameMap
            )
        )

    override val displayName:
        String =
        gameMap.displayName

    override val description:
        String =
        gameMap.description

    override val category:
        ContentCategory =
        ContentCategory.MAP

    override val tags:
        Set<ContentTag> =
        emptySet()

    /*
     * Map'in görünürlüğünü unlock değil,
     * encountered bilgisi yönetecek.
     */
    override val initialUnlockState:
        ContentUnlockState =
        ContentUnlockState.UNLOCKED
}
