package com.azizoglu.grisilith.enemy.visual

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.enemy.definition.EnemyKindIds

/*
 * Humanoids use clean, portrait-free atlases. Spearman and Horseman keep each
 * direction in a separate strip because their long weapons otherwise cross a
 * neighbouring row and leak fragments into the rendered frame.
 */
object DefaultHumanoidEnemySpriteSheets {

    private fun frames(
        xBounds: IntArray,
        y: Int,
        height: Int
    ): List<EnemySpriteFrame> =
        (0 until 5).map { column ->
            EnemySpriteFrame(
                x = xBounds[column],
                y = y,
                width = xBounds[column + 1] - xBounds[column],
                height = height
            )
        }

    private fun fullAtlasDefinition(
        kindId: ContentId,
        texturePath: String,
        xBounds: IntArray,
        yBounds: IntArray,
        frameDuration: Float
    ): EnemySpriteSheetDefinition =
        EnemySpriteSheetDefinition(
            kindId = kindId,
            texturePath = texturePath,
            sourceWidth = 1402,
            sourceHeight = 1122,
            animations = EnemyVisualDirection.entries.mapIndexed { row, direction ->
                val directionFrames = frames(
                    xBounds = xBounds,
                    y = yBounds[row],
                    height = yBounds[row + 1] - yBounds[row]
                )
                direction to EnemySpriteAnimationDefinition(
                    idleFrame = directionFrames.first(),
                    walkFrames = directionFrames.drop(1),
                    walkFrameDurationSeconds = frameDuration
                )
            }.toMap()
        )

    private fun stripAnimation(
        texturePath: String,
        sourceWidth: Int,
        sourceHeight: Int,
        xBounds: IntArray,
        frameDuration: Float,
        flipX: Boolean = false
    ): EnemySpriteAnimationDefinition {
        val directionFrames = frames(xBounds, 0, sourceHeight)
        return EnemySpriteAnimationDefinition(
            idleFrame = directionFrames.first(),
            walkFrames = directionFrames.drop(1),
            walkFrameDurationSeconds = frameDuration,
            texturePath = texturePath,
            sourceWidth = sourceWidth,
            sourceHeight = sourceHeight,
            flipX = flipX
        )
    }

    private fun spearmanDefinition(): EnemySpriteSheetDefinition {
        val front = stripAnimation(
            "enemies/humanoid/spearman_front.png", 2172, 724,
            intArrayOf(0, 463, 883, 1304, 1719, 2172), 0.13f
        )
        val left = stripAnimation(
            "enemies/humanoid/spearman_left.png", 2172, 724,
            intArrayOf(0, 443, 877, 1298, 1730, 2172), 0.13f
        )
        val back = stripAnimation(
            "enemies/humanoid/spearman_back.png", 2043, 770,
            intArrayOf(0, 418, 834, 1245, 1651, 2043), 0.13f
        )
        return EnemySpriteSheetDefinition(
            kindId = EnemyKindIds.SPEARMAN,
            texturePath = "enemies/humanoid/spearman_front.png",
            sourceWidth = 2172,
            sourceHeight = 724,
            animations = mapOf(
                EnemyVisualDirection.DOWN to front,
                EnemyVisualDirection.LEFT to left,
                EnemyVisualDirection.RIGHT to left.copy(flipX = true),
                EnemyVisualDirection.UP to back
            )
        )
    }

    private fun horsemanDefinition(): EnemySpriteSheetDefinition {
        val front = stripAnimation(
            "enemies/humanoid/horseman_front.png", 2172, 724,
            intArrayOf(0, 486, 895, 1301, 1709, 2172), 0.10f
        )
        val left = stripAnimation(
            "enemies/humanoid/horseman_left.png", 2172, 724,
            intArrayOf(0, 451, 888, 1318, 1755, 2172), 0.10f
        )
        val back = stripAnimation(
            "enemies/humanoid/horseman_back.png", 2161, 728,
            intArrayOf(0, 441, 866, 1296, 1734, 2161), 0.10f
        )
        return EnemySpriteSheetDefinition(
            kindId = EnemyKindIds.HORSEMAN,
            texturePath = "enemies/humanoid/horseman_front.png",
            sourceWidth = 2172,
            sourceHeight = 724,
            animations = mapOf(
                EnemyVisualDirection.DOWN to front,
                EnemyVisualDirection.LEFT to left,
                EnemyVisualDirection.RIGHT to left.copy(flipX = true),
                EnemyVisualDirection.UP to back
            )
        )
    }

    val all = listOf(
        fullAtlasDefinition(
            EnemyKindIds.BANDIT,
            "enemies/humanoid/bandit_clean.png",
            intArrayOf(0, 279, 563, 841, 1118, 1402),
            intArrayOf(0, 298, 554, 814, 1122),
            0.11f
        ),
        fullAtlasDefinition(
            EnemyKindIds.ARCHER,
            "enemies/humanoid/archer_clean.png",
            intArrayOf(0, 293, 569, 843, 1118, 1402),
            intArrayOf(0, 308, 574, 831, 1122),
            0.12f
        ),
        spearmanDefinition(),
        horsemanDefinition(),
        fullAtlasDefinition(
            EnemyKindIds.WITCH,
            "enemies/humanoid/witch_clean.png",
            intArrayOf(0, 287, 562, 845, 1126, 1402),
            intArrayOf(0, 284, 547, 802, 1122),
            0.15f
        )
    )
}
