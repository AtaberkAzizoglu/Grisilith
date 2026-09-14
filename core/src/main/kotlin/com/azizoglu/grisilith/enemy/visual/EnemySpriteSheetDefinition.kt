package com.azizoglu.grisilith.enemy.visual

import com.azizoglu.grisilith.content.ContentId

data class EnemySpriteSheetDefinition(
    val kindId: ContentId,
    val texturePath: String,
    val sourceWidth: Int,
    val sourceHeight: Int,
    val referenceSide: Float? = null,
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
            sourceWidth > 0
        )

        require(
            sourceHeight > 0
        )

        require(referenceSide == null || referenceSide > 0f)

        require(
            animations.keys.containsAll(
                EnemyVisualDirection.entries
            )
        )
    }

    fun animationFor(
        direction: EnemyVisualDirection
    ): EnemySpriteAnimationDefinition {
        return animations[
            direction
        ]
            ?: error(
                "Missing visual direction $direction for ${kindId.value}"
            )
    }
}
