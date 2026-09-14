package com.azizoglu.grisilith.enemy.visual

import com.azizoglu.grisilith.content.ContentId

object EnemySpriteSheetRegistry {

    private val definitions =
        (
            DefaultBeastEnemySpriteSheets.all +
                DefaultHumanoidEnemySpriteSheets.all +
                DefaultUndeadEnemySpriteSheets.all +
                DefaultMonsterEnemySpriteSheets.all
            )
            .associateBy {
                    definition ->
                definition.kindId
            }

    fun find(
        kindId: ContentId?
    ): EnemySpriteSheetDefinition? {

        if (
            kindId ==
            null
        ) {
            return null
        }

        return definitions[
            kindId
        ]
    }

    fun contains(
        kindId: ContentId?
    ): Boolean {
        return find(
            kindId
        ) != null
    }

    val all:
        Collection<
            EnemySpriteSheetDefinition
            >
        get() =
            definitions.values
}
