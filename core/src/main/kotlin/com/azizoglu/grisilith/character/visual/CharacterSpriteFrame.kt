package com.azizoglu.grisilith.character.visual

data class CharacterSpriteFrame(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
) {

    init {
        require(
            x >=
                0
        )

        require(
            y >=
                0
        )

        require(
            width >
                0
        )

        require(
            height >
                0
        )
    }
}
