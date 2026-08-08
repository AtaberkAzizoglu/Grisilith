package com.azizoglu.grisilith.character.visual

data class CharacterSpriteAnimationDefinition(
    val idleFrame:
    CharacterSpriteFrame,

    val walkFrames:
    List<CharacterSpriteFrame>,

    val walkFrameDurationSeconds:
    Float
) {

    init {
        require(
            walkFrames.isNotEmpty()
        )

        require(
            walkFrameDurationSeconds >
                0f
        )
    }
}
