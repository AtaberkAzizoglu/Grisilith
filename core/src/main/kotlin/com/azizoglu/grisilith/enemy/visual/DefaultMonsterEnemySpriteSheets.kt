package com.azizoglu.grisilith.enemy.visual

import com.azizoglu.grisilith.enemy.definition.EnemyKindIds

object DefaultMonsterEnemySpriteSheets {
    val mimic = EnemySpriteSheetDefinition(
        kindId = EnemyKindIds.MIMIC,
        texturePath = "enemies/monster/mimic.png",
        sourceWidth = 1448,
        sourceHeight = 1086,
        referenceSide = 230f,
        animations = mapOf(
            EnemyVisualDirection.DOWN to animation(
                EnemySpriteFrame(450, 81, 175, 209),
                listOf(
                    EnemySpriteFrame(656, 80, 169, 197),
                    EnemySpriteFrame(847, 81, 168, 190),
                    EnemySpriteFrame(1052, 79, 167, 199),
                    EnemySpriteFrame(1244, 78, 168, 199)
                )
            ),
            EnemyVisualDirection.LEFT to animation(
                EnemySpriteFrame(470, 307, 170, 219),
                listOf(
                    EnemySpriteFrame(660, 307, 180, 219),
                    EnemySpriteFrame(854, 307, 191, 221),
                    EnemySpriteFrame(1050, 308, 182, 220),
                    EnemySpriteFrame(1249, 307, 171, 221)
                )
            ),
            EnemyVisualDirection.RIGHT to animation(
                EnemySpriteFrame(459, 556, 181, 226),
                listOf(
                    EnemySpriteFrame(655, 554, 185, 230),
                    EnemySpriteFrame(850, 556, 190, 224),
                    EnemySpriteFrame(1050, 556, 185, 230),
                    EnemySpriteFrame(1245, 554, 185, 230)
                )
            ),
            EnemyVisualDirection.UP to animation(
                EnemySpriteFrame(464, 806, 152, 213),
                listOf(
                    EnemySpriteFrame(658, 806, 160, 217),
                    EnemySpriteFrame(852, 812, 153, 206),
                    EnemySpriteFrame(1052, 809, 169, 215),
                    EnemySpriteFrame(1256, 806, 157, 218)
                )
            )
        )
    )

    val all = listOf(mimic)

    private fun animation(
        idle: EnemySpriteFrame,
        walks: List<EnemySpriteFrame>
    ) = EnemySpriteAnimationDefinition(
        idleFrame = idle,
        walkFrames = walks,
        walkFrameDurationSeconds = 0.11f
    )
}
