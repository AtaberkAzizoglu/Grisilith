package com.azizoglu.grisilith.character.visual

import com.azizoglu.grisilith.content.ContentId

object CharacterSpriteSheetRegistry {

    private val definitions =
        DefaultCharacterSpriteSheets
            .all
            .associateBy {
                    definition ->
                definition.characterId
            }

    fun find(
        characterId:
        ContentId?
    ): CharacterSpriteSheetDefinition? {

        if (
            characterId ==
            null
        ) {
            return null
        }

        return definitions[
            characterId
        ]
    }

    fun contains(
        characterId:
        ContentId?
    ): Boolean {

        return find(
            characterId
        ) != null
    }

    val all:
        Collection<
            CharacterSpriteSheetDefinition
            >
        get() =
            definitions.values
}
