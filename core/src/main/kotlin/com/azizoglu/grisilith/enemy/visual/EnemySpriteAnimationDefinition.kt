package com.azizoglu.grisilith.enemy.visual

data class EnemySpriteAnimationDefinition(
    val idleFrame: EnemySpriteFrame,
    val walkFrames: List<EnemySpriteFrame>,
    val walkFrameDurationSeconds: Float
) {

    init {
        require(
            walkFrames.isNotEmpty()
        )

        require(
            walkFrameDurationSeconds > 0f
        )
    }
}
