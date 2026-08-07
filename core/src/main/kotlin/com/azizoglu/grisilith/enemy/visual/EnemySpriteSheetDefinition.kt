package com.azizoglu.grisilith.enemy.visual

import com.azizoglu.grisilith.content.ContentId

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
            walkFrameDurationSeconds >
                0f
        )
    }
}

data class EnemySpriteSheetDefinition(
    val kindId: ContentId,
    val texturePath: String,
    val sourceWidth: Int,
    val sourceHeight: Int,
    val animations:
        Map<
            EnemyVisualDirection,
            EnemySpriteAnimationDefinition
        >
) {

    init {
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
                EnemyVisualDirection.entries
            )
        )
    }

    fun animationFor(
        direction:
        EnemyVisualDirection
    ): EnemySpriteAnimationDefinition {
        return animations[
            direction
        ]
            ?: error(
                "Missing visual direction $direction for ${kindId.value}"
            )
    }
}
