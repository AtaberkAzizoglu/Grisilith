package com.azizoglu.grisilith.enemy.visual

data class EnemySpriteAnimationDefinition(
    val idleFrame: EnemySpriteFrame,
    val walkFrames: List<EnemySpriteFrame>,
    val walkFrameDurationSeconds: Float,
    val texturePath: String? = null,
    val sourceWidth: Int? = null,
    val sourceHeight: Int? = null,
    val flipX: Boolean = false
) {

    init {
        require(
            walkFrames.isNotEmpty()
        )

        require(
            walkFrameDurationSeconds > 0f
        )

        require(
            (texturePath == null && sourceWidth == null && sourceHeight == null) ||
                (texturePath?.isNotBlank() == true &&
                    sourceWidth != null && sourceWidth > 0 &&
                    sourceHeight != null && sourceHeight > 0)
        )
    }
}
