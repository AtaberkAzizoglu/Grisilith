package com.azizoglu.grisilith.character.visual

import com.azizoglu.grisilith.content.ContentId

data class CharacterSpriteSheetDefinition(
    val characterId:
    ContentId,

    val texturePath:
    String,

    val sourceWidth:
    Int,

    val sourceHeight:
    Int,

    val animations:
    Map<
        CharacterVisualDirection,
        CharacterSpriteAnimationDefinition
        >,
    val feetInsetPixels: Int = 0,
    val contentHeightPixels: Int? = null
) {

    init {
        require(feetInsetPixels >= 0)
        require(contentHeightPixels == null || contentHeightPixels > 0)
        require(
            texturePath.isNotBlank()
        )

        require(
            sourceWidth >
                0
        )

        require(
            sourceHeight >
                0
        )

        require(
            animations.keys.containsAll(
                CharacterVisualDirection.entries
            )
        )
    }

    fun animationFor(
        direction:
        CharacterVisualDirection
    ): CharacterSpriteAnimationDefinition {

        return animations[
            direction
        ]
            ?: error(
                "Missing visual direction $direction for ${characterId.value}"
            )
    }
}
