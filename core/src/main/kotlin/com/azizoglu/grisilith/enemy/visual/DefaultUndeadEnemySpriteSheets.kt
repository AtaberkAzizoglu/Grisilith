package com.azizoglu.grisilith.enemy.visual

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.enemy.definition.EnemyKindIds

/*
 * Every Undead atlas uses equal, padded cells and a stable character centre.
 * The first frame doubles as idle; all five frames participate in the walk
 * loop so the generated contact/passing/stride phases remain intact.
 */
object DefaultUndeadEnemySpriteSheets {

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

    private fun definition(
        kindId: ContentId,
        texturePath: String,
        sourceWidth: Int,
        sourceHeight: Int,
        xBounds: IntArray,
        yBounds: IntArray,
        frameDuration: Float
    ): EnemySpriteSheetDefinition =
        EnemySpriteSheetDefinition(
            kindId = kindId,
            texturePath = texturePath,
            sourceWidth = sourceWidth,
            sourceHeight = sourceHeight,
            animations = EnemyVisualDirection.entries.mapIndexed { row, direction ->
                val directionFrames = frames(
                    xBounds = xBounds,
                    y = yBounds[row],
                    height = yBounds[row + 1] - yBounds[row]
                )
                direction to EnemySpriteAnimationDefinition(
                    idleFrame = directionFrames.first(),
                    walkFrames = directionFrames,
                    walkFrameDurationSeconds = frameDuration
                )
            }.toMap()
        )

    private fun standardDefinition(
        kindId: ContentId,
        fileName: String,
        frameDuration: Float
    ): EnemySpriteSheetDefinition =
        definition(
            kindId = kindId,
            texturePath = "enemies/undead/$fileName",
            sourceWidth = 1402,
            sourceHeight = 1122,
            xBounds = intArrayOf(0, 280, 561, 841, 1122, 1402),
            yBounds = intArrayOf(0, 280, 561, 842, 1122),
            frameDuration = frameDuration
        )

    val all = listOf(
        standardDefinition(
            EnemyKindIds.SKELETON_WARRIOR,
            "skeleton_warrior.png",
            0.12f
        ),
        standardDefinition(
            EnemyKindIds.SKELETON_ARCHER,
            "skeleton_archer.png",
            0.13f
        ),
        standardDefinition(
            EnemyKindIds.ZOMBIE,
            "zombie.png",
            0.18f
        ),
        standardDefinition(
            EnemyKindIds.GHOST,
            "ghost.png",
            0.14f
        ),
        definition(
            kindId = EnemyKindIds.SLIME,
            texturePath = "enemies/undead/slime.png",
            sourceWidth = 1536,
            sourceHeight = 1024,
            xBounds = intArrayOf(0, 307, 614, 922, 1229, 1536),
            yBounds = intArrayOf(0, 256, 512, 768, 1024),
            frameDuration = 0.13f
        ),
        standardDefinition(
            EnemyKindIds.VAMPIRE_LORD,
            "vampire_lord.png",
            0.11f
        )
    )
}
