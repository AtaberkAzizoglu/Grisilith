package com.azizoglu.grisilith.enemy.visual

data class EnemySpriteFrame(
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
