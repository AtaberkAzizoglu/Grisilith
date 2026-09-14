package com.azizoglu.grisilith.enemy.visual

import com.azizoglu.grisilith.enemy.definition.EnemyKindIds

/*
 * Road to Home / Beast visual registry.
 *
 * The original PNG files are used unchanged.
 * The large preview sprite at the left side of each source image
 * is intentionally ignored; only the 4 x 5 animation area is mapped.
 *
 * Row order:
 * DOWN, LEFT, RIGHT, UP
 *
 * Column order:
 * IDLE, WALK 1, WALK 2, WALK 3, WALK 4
 */
object DefaultBeastEnemySpriteSheets {

    val baldEagle =
        EnemySpriteSheetDefinition(
            kindId =
                EnemyKindIds.BALD_EAGLE,

            texturePath =
                "enemies/beast/bald_eagle.png",

            sourceWidth =
                612,

            sourceHeight =
                408,

            animations =
                mapOf(
                    EnemyVisualDirection.DOWN to
                        EnemySpriteAnimationDefinition(
                            idleFrame =
                                EnemySpriteFrame(x = 200, y = 40, width = 53, height = 51),

                            walkFrames =
                                listOf(
                                    EnemySpriteFrame(x = 272, y = 40, width = 66, height = 48),
                                    EnemySpriteFrame(x = 357, y = 42, width = 65, height = 46),
                                    EnemySpriteFrame(x = 439, y = 51, width = 71, height = 37),
                                    EnemySpriteFrame(x = 527, y = 41, width = 69, height = 49)
                                ),

                            walkFrameDurationSeconds =
                                0.09f
                        ),

                    EnemyVisualDirection.LEFT to
                        EnemySpriteAnimationDefinition(
                            idleFrame =
                                EnemySpriteFrame(x = 200, y = 121, width = 52, height = 54),

                            walkFrames =
                                listOf(
                                    EnemySpriteFrame(x = 276, y = 129, width = 57, height = 47),
                                    EnemySpriteFrame(x = 361, y = 125, width = 57, height = 50),
                                    EnemySpriteFrame(x = 446, y = 125, width = 58, height = 52),
                                    EnemySpriteFrame(x = 535, y = 129, width = 55, height = 47)
                                ),

                            walkFrameDurationSeconds =
                                0.09f
                        ),

                    EnemyVisualDirection.RIGHT to
                        EnemySpriteAnimationDefinition(
                            idleFrame =
                                EnemySpriteFrame(x = 200, y = 208, width = 51, height = 55),

                            walkFrames =
                                listOf(
                                    EnemySpriteFrame(x = 277, y = 219, width = 55, height = 42),
                                    EnemySpriteFrame(x = 361, y = 214, width = 55, height = 48),
                                    EnemySpriteFrame(x = 446, y = 211, width = 56, height = 52),
                                    EnemySpriteFrame(x = 532, y = 220, width = 54, height = 41)
                                ),

                            walkFrameDurationSeconds =
                                0.09f
                        ),

                    EnemyVisualDirection.UP to
                        EnemySpriteAnimationDefinition(
                            idleFrame =
                                EnemySpriteFrame(x = 200, y = 299, width = 53, height = 53),

                            walkFrames =
                                listOf(
                                    EnemySpriteFrame(x = 274, y = 299, width = 62, height = 53),
                                    EnemySpriteFrame(x = 356, y = 300, width = 66, height = 51),
                                    EnemySpriteFrame(x = 440, y = 302, width = 70, height = 50),
                                    EnemySpriteFrame(x = 527, y = 299, width = 68, height = 53)
                                ),

                            walkFrameDurationSeconds =
                                0.09f
                        )
                )
        )

    val grayWolf =
        EnemySpriteSheetDefinition(
            kindId =
                EnemyKindIds.GRAY_WOLF,

            texturePath =
                "enemies/beast/gray_wolf.png",

            sourceWidth =
                612,

            sourceHeight =
                408,

            animations =
                mapOf(
                    EnemyVisualDirection.DOWN to
                        EnemySpriteAnimationDefinition(
                            idleFrame =
                                EnemySpriteFrame(x = 221, y = 38, width = 30, height = 60),

                            walkFrames =
                                listOf(
                                    EnemySpriteFrame(x = 306, y = 38, width = 31, height = 60),
                                    EnemySpriteFrame(x = 386, y = 38, width = 31, height = 60),
                                    EnemySpriteFrame(x = 465, y = 38, width = 31, height = 60),
                                    EnemySpriteFrame(x = 544, y = 38, width = 30, height = 59)
                                ),

                            walkFrameDurationSeconds =
                                0.12f
                        ),

                    EnemyVisualDirection.LEFT to
                        EnemySpriteAnimationDefinition(
                            idleFrame =
                                EnemySpriteFrame(x = 204, y = 129, width = 60, height = 45),

                            walkFrames =
                                listOf(
                                    EnemySpriteFrame(x = 292, y = 132, width = 63, height = 42),
                                    EnemySpriteFrame(x = 368, y = 133, width = 64, height = 40),
                                    EnemySpriteFrame(x = 447, y = 133, width = 63, height = 39),
                                    EnemySpriteFrame(x = 525, y = 133, width = 65, height = 40)
                                ),

                            walkFrameDurationSeconds =
                                0.12f
                        ),

                    EnemyVisualDirection.RIGHT to
                        EnemySpriteAnimationDefinition(
                            idleFrame =
                                EnemySpriteFrame(x = 206, y = 215, width = 63, height = 43),

                            walkFrames =
                                listOf(
                                    EnemySpriteFrame(x = 291, y = 217, width = 64, height = 42),
                                    EnemySpriteFrame(x = 370, y = 218, width = 65, height = 40),
                                    EnemySpriteFrame(x = 448, y = 218, width = 65, height = 41),
                                    EnemySpriteFrame(x = 529, y = 218, width = 64, height = 41)
                                ),

                            walkFrameDurationSeconds =
                                0.12f
                        ),

                    EnemyVisualDirection.UP to
                        EnemySpriteAnimationDefinition(
                            idleFrame =
                                EnemySpriteFrame(x = 221, y = 292, width = 30, height = 67),

                            walkFrames =
                                listOf(
                                    EnemySpriteFrame(x = 307, y = 293, width = 30, height = 64),
                                    EnemySpriteFrame(x = 386, y = 292, width = 30, height = 68),
                                    EnemySpriteFrame(x = 466, y = 293, width = 29, height = 64),
                                    EnemySpriteFrame(x = 545, y = 292, width = 29, height = 64)
                                ),

                            walkFrameDurationSeconds =
                                0.12f
                        )
                )
        )

    val boar =
        EnemySpriteSheetDefinition(
            kindId =
                EnemyKindIds.BOAR,

            texturePath =
                "enemies/beast/boar.png",

            sourceWidth =
                612,

            sourceHeight =
                408,

            animations =
                mapOf(
                    EnemyVisualDirection.DOWN to
                        EnemySpriteAnimationDefinition(
                            idleFrame =
                                EnemySpriteFrame(x = 212, y = 33, width = 40, height = 65),

                            walkFrames =
                                listOf(
                                    EnemySpriteFrame(x = 295, y = 34, width = 42, height = 64),
                                    EnemySpriteFrame(x = 376, y = 34, width = 43, height = 64),
                                    EnemySpriteFrame(x = 455, y = 34, width = 43, height = 64),
                                    EnemySpriteFrame(x = 538, y = 34, width = 43, height = 64)
                                ),

                            walkFrameDurationSeconds =
                                0.14f
                        ),

                    EnemyVisualDirection.LEFT to
                        EnemySpriteAnimationDefinition(
                            idleFrame =
                                EnemySpriteFrame(x = 198, y = 126, width = 68, height = 51),

                            walkFrames =
                                listOf(
                                    EnemySpriteFrame(x = 282, y = 127, width = 71, height = 50),
                                    EnemySpriteFrame(x = 364, y = 128, width = 70, height = 49),
                                    EnemySpriteFrame(x = 446, y = 128, width = 69, height = 49),
                                    EnemySpriteFrame(x = 530, y = 129, width = 69, height = 48)
                                ),

                            walkFrameDurationSeconds =
                                0.14f
                        ),

                    EnemyVisualDirection.RIGHT to
                        EnemySpriteAnimationDefinition(
                            idleFrame =
                                EnemySpriteFrame(x = 198, y = 212, width = 69, height = 51),

                            walkFrames =
                                listOf(
                                    EnemySpriteFrame(x = 282, y = 212, width = 69, height = 51),
                                    EnemySpriteFrame(x = 364, y = 212, width = 69, height = 51),
                                    EnemySpriteFrame(x = 443, y = 212, width = 73, height = 51),
                                    EnemySpriteFrame(x = 525, y = 212, width = 69, height = 51)
                                ),

                            walkFrameDurationSeconds =
                                0.14f
                        ),

                    EnemyVisualDirection.UP to
                        EnemySpriteAnimationDefinition(
                            idleFrame =
                                EnemySpriteFrame(x = 212, y = 292, width = 39, height = 70),

                            walkFrames =
                                listOf(
                                    EnemySpriteFrame(x = 295, y = 292, width = 41, height = 70),
                                    EnemySpriteFrame(x = 377, y = 291, width = 41, height = 71),
                                    EnemySpriteFrame(x = 456, y = 292, width = 41, height = 69),
                                    EnemySpriteFrame(x = 540, y = 292, width = 40, height = 70)
                                ),

                            walkFrameDurationSeconds =
                                0.14f
                        )
                )
        )

    val grizzlyBear =
        EnemySpriteSheetDefinition(
            kindId =
                EnemyKindIds.GRIZZLY_BEAR,

            texturePath =
                "enemies/beast/grizzly_bear.png",

            sourceWidth =
                612,

            sourceHeight =
                408,

            referenceSide =
                83f,

            animations =
                mapOf(
                    EnemyVisualDirection.DOWN to
                        EnemySpriteAnimationDefinition(
                            idleFrame =
                                EnemySpriteFrame(x = 201, y = 28, width = 53, height = 73),

                            walkFrames =
                                listOf(
                                    EnemySpriteFrame(x = 282, y = 30, width = 55, height = 71),
                                    EnemySpriteFrame(x = 362, y = 31, width = 55, height = 70),
                                    EnemySpriteFrame(x = 444, y = 31, width = 59, height = 70),
                                    EnemySpriteFrame(x = 529, y = 31, width = 63, height = 68)
                                ),

                            walkFrameDurationSeconds =
                                0.15f
                        ),

                    EnemyVisualDirection.LEFT to
                        EnemySpriteAnimationDefinition(
                            idleFrame =
                                EnemySpriteFrame(x = 196, y = 124, width = 66, height = 53),

                            walkFrames =
                                listOf(
                                    EnemySpriteFrame(x = 272, y = 128, width = 83, height = 49),
                                    EnemySpriteFrame(x = 355, y = 129, width = 80, height = 48),
                                    EnemySpriteFrame(x = 435, y = 129, width = 76, height = 48),
                                    EnemySpriteFrame(x = 521, y = 128, width = 78, height = 49)
                                ),

                            walkFrameDurationSeconds =
                                0.15f
                        ),

                    EnemyVisualDirection.RIGHT to
                        EnemySpriteAnimationDefinition(
                            idleFrame =
                                EnemySpriteFrame(x = 196, y = 209, width = 67, height = 53),

                            walkFrames =
                                listOf(
                                    EnemySpriteFrame(x = 272, y = 215, width = 72, height = 47),
                                    EnemySpriteFrame(x = 356, y = 215, width = 70, height = 48),
                                    EnemySpriteFrame(x = 436, y = 215, width = 77, height = 48),
                                    EnemySpriteFrame(x = 522, y = 215, width = 77, height = 48)
                                ),

                            walkFrameDurationSeconds =
                                0.15f
                        ),

                    EnemyVisualDirection.UP to
                        EnemySpriteAnimationDefinition(
                            idleFrame =
                                EnemySpriteFrame(x = 201, y = 284, width = 51, height = 83),

                            walkFrames =
                                listOf(
                                    EnemySpriteFrame(x = 282, y = 286, width = 49, height = 81),
                                    EnemySpriteFrame(x = 365, y = 286, width = 47, height = 81),
                                    EnemySpriteFrame(x = 451, y = 286, width = 46, height = 80),
                                    EnemySpriteFrame(x = 537, y = 286, width = 47, height = 81)
                                ),

                            walkFrameDurationSeconds =
                                0.15f
                        )
                )
        )

    val grizzlyBearAngry =
        EnemySpriteSheetDefinition(
            kindId = EnemyKindIds.GRIZZLY_BEAR,
            texturePath = "enemies/beast/grizzly_bear_angry.png",
            sourceWidth = 1536,
            sourceHeight = 1024,
            referenceSide = 217f,
            animations = mapOf(
                EnemyVisualDirection.DOWN to EnemySpriteAnimationDefinition(
                    idleFrame = EnemySpriteFrame(479, 59, 160, 203),
                    walkFrames = listOf(
                        EnemySpriteFrame(692, 67, 153, 191),
                        EnemySpriteFrame(901, 70, 156, 187),
                        EnemySpriteFrame(1109, 71, 159, 186),
                        EnemySpriteFrame(1323, 70, 171, 179)
                    ),
                    walkFrameDurationSeconds = 0.13f
                ),
                EnemyVisualDirection.LEFT to EnemySpriteAnimationDefinition(
                    idleFrame = EnemySpriteFrame(464, 304, 198, 146),
                    walkFrames = listOf(
                        EnemySpriteFrame(666, 313, 214, 138),
                        EnemySpriteFrame(880, 318, 215, 132),
                        EnemySpriteFrame(1095, 318, 215, 132),
                        EnemySpriteFrame(1310, 315, 203, 135)
                    ),
                    walkFrameDurationSeconds = 0.13f
                ),
                EnemyVisualDirection.RIGHT to EnemySpriteAnimationDefinition(
                    idleFrame = EnemySpriteFrame(466, 517, 199, 149),
                    walkFrames = listOf(
                        EnemySpriteFrame(665, 530, 213, 140),
                        EnemySpriteFrame(887, 530, 208, 137),
                        EnemySpriteFrame(1095, 530, 215, 140),
                        EnemySpriteFrame(1310, 530, 203, 138)
                    ),
                    walkFrameDurationSeconds = 0.13f
                ),
                EnemyVisualDirection.UP to EnemySpriteAnimationDefinition(
                    idleFrame = EnemySpriteFrame(495, 713, 147, 213),
                    walkFrames = listOf(
                        EnemySpriteFrame(701, 715, 137, 214),
                        EnemySpriteFrame(909, 715, 135, 211),
                        EnemySpriteFrame(1123, 713, 133, 217),
                        EnemySpriteFrame(1338, 715, 133, 214)
                    ),
                    walkFrameDurationSeconds = 0.13f
                )
            )
        )

    val beeSwarm =
        EnemySpriteSheetDefinition(
            kindId =
                EnemyKindIds.BEE_SWARM,

            texturePath =
                "enemies/beast/bee_swarm.png",

            sourceWidth =
                577,

            sourceHeight =
                433,

            animations =
                mapOf(
                    EnemyVisualDirection.DOWN to
                        EnemySpriteAnimationDefinition(
                            idleFrame =
                                EnemySpriteFrame(x = 195, y = 39, width = 53, height = 65),

                            walkFrames =
                                listOf(
                                    EnemySpriteFrame(x = 270, y = 39, width = 60, height = 64),
                                    EnemySpriteFrame(x = 346, y = 41, width = 61, height = 62),
                                    EnemySpriteFrame(x = 425, y = 39, width = 62, height = 65),
                                    EnemySpriteFrame(x = 503, y = 41, width = 63, height = 63)
                                ),

                            walkFrameDurationSeconds =
                                0.08f
                        ),

                    EnemyVisualDirection.LEFT to
                        EnemySpriteAnimationDefinition(
                            idleFrame =
                                EnemySpriteFrame(x = 196, y = 131, width = 52, height = 62),

                            walkFrames =
                                listOf(
                                    EnemySpriteFrame(x = 269, y = 137, width = 61, height = 52),
                                    EnemySpriteFrame(x = 346, y = 139, width = 62, height = 51),
                                    EnemySpriteFrame(x = 426, y = 140, width = 61, height = 49),
                                    EnemySpriteFrame(x = 504, y = 142, width = 62, height = 45)
                                ),

                            walkFrameDurationSeconds =
                                0.08f
                        ),

                    EnemyVisualDirection.RIGHT to
                        EnemySpriteAnimationDefinition(
                            idleFrame =
                                EnemySpriteFrame(x = 195, y = 224, width = 53, height = 62),

                            walkFrames =
                                listOf(
                                    EnemySpriteFrame(x = 267, y = 230, width = 62, height = 52),
                                    EnemySpriteFrame(x = 345, y = 231, width = 62, height = 51),
                                    EnemySpriteFrame(x = 423, y = 231, width = 62, height = 51),
                                    EnemySpriteFrame(x = 503, y = 230, width = 62, height = 52)
                                ),

                            walkFrameDurationSeconds =
                                0.08f
                        ),

                    EnemyVisualDirection.UP to
                        EnemySpriteAnimationDefinition(
                            idleFrame =
                                EnemySpriteFrame(x = 195, y = 319, width = 53, height = 62),

                            walkFrames =
                                listOf(
                                    EnemySpriteFrame(x = 268, y = 312, width = 61, height = 68),
                                    EnemySpriteFrame(x = 346, y = 312, width = 61, height = 66),
                                    EnemySpriteFrame(x = 424, y = 312, width = 63, height = 66),
                                    EnemySpriteFrame(x = 503, y = 313, width = 63, height = 65)
                                ),

                            walkFrameDurationSeconds =
                                0.08f
                        )
                )
        )

    val crocodile =
        EnemySpriteSheetDefinition(
            kindId =
                EnemyKindIds.CROCODILE,

            texturePath =
                "enemies/beast/crocodile.png",

            sourceWidth =
                1402,

            sourceHeight =
                1122,

            referenceSide =
                296f,

            animations =
                mapOf(
                    EnemyVisualDirection.DOWN to
                        EnemySpriteAnimationDefinition(
                            idleFrame =
                                EnemySpriteFrame(x = 73, y = 34, width = 195, height = 272),

                            walkFrames =
                                listOf(
                                    EnemySpriteFrame(x = 348, y = 41, width = 193, height = 270),
                                    EnemySpriteFrame(x = 607, y = 39, width = 190, height = 270),
                                    EnemySpriteFrame(x = 869, y = 44, width = 203, height = 266),
                                    EnemySpriteFrame(x = 1166, y = 38, width = 184, height = 268)
                                ),

                            walkFrameDurationSeconds =
                                0.18f
                        ),

                    EnemyVisualDirection.LEFT to
                        EnemySpriteAnimationDefinition(
                            idleFrame =
                                EnemySpriteFrame(x = 10, y = 352, width = 294, height = 170),

                            walkFrames =
                                listOf(
                                    EnemySpriteFrame(x = 304, y = 349, width = 274, height = 176),
                                    EnemySpriteFrame(x = 576, y = 352, width = 269, height = 170),
                                    EnemySpriteFrame(x = 836, y = 352, width = 279, height = 173),
                                    EnemySpriteFrame(x = 1116, y = 346, width = 276, height = 176)
                                ),

                            walkFrameDurationSeconds =
                                0.18f
                        ),

                    EnemyVisualDirection.RIGHT to
                        EnemySpriteAnimationDefinition(
                            idleFrame =
                                EnemySpriteFrame(x = 20, y = 583, width = 289, height = 178),

                            walkFrames =
                                listOf(
                                    EnemySpriteFrame(x = 305, y = 583, width = 273, height = 178),
                                    EnemySpriteFrame(x = 572, y = 581, width = 277, height = 180),
                                    EnemySpriteFrame(x = 850, y = 583, width = 270, height = 178),
                                    EnemySpriteFrame(x = 1115, y = 585, width = 272, height = 173)
                                ),

                            walkFrameDurationSeconds =
                                0.18f
                        ),

                    EnemyVisualDirection.UP to
                        EnemySpriteAnimationDefinition(
                            idleFrame =
                                EnemySpriteFrame(x = 60, y = 787, width = 200, height = 296),

                            walkFrames =
                                listOf(
                                    EnemySpriteFrame(x = 341, y = 796, width = 204, height = 282),
                                    EnemySpriteFrame(x = 599, y = 794, width = 199, height = 283),
                                    EnemySpriteFrame(x = 873, y = 797, width = 208, height = 285),
                                    EnemySpriteFrame(x = 1151, y = 793, width = 198, height = 283)
                                ),

                            walkFrameDurationSeconds =
                                0.18f
                        )
                )
        )

    val silverbackGorilla =
        EnemySpriteSheetDefinition(
            kindId =
                EnemyKindIds.SILVERBACK_GORILLA,

            texturePath =
                "enemies/beast/silverback_gorilla.png",

            sourceWidth =
                558,

            sourceHeight =
                447,

            animations =
                mapOf(
                    EnemyVisualDirection.DOWN to
                        EnemySpriteAnimationDefinition(
                            idleFrame =
                                EnemySpriteFrame(x = 191, y = 30, width = 60, height = 80),

                            walkFrames =
                                listOf(
                                    EnemySpriteFrame(x = 265, y = 32, width = 62, height = 78),
                                    EnemySpriteFrame(x = 340, y = 30, width = 60, height = 78),
                                    EnemySpriteFrame(x = 410, y = 32, width = 61, height = 77),
                                    EnemySpriteFrame(x = 484, y = 32, width = 61, height = 75)
                                ),

                            walkFrameDurationSeconds =
                                0.13f
                        ),

                    EnemyVisualDirection.LEFT to
                        EnemySpriteAnimationDefinition(
                            idleFrame =
                                EnemySpriteFrame(x = 191, y = 128, width = 59, height = 76),

                            walkFrames =
                                listOf(
                                    EnemySpriteFrame(x = 267, y = 128, width = 60, height = 74),
                                    EnemySpriteFrame(x = 335, y = 130, width = 66, height = 72),
                                    EnemySpriteFrame(x = 411, y = 130, width = 63, height = 70),
                                    EnemySpriteFrame(x = 485, y = 130, width = 65, height = 70)
                                ),

                            walkFrameDurationSeconds =
                                0.13f
                        ),

                    EnemyVisualDirection.RIGHT to
                        EnemySpriteAnimationDefinition(
                            idleFrame =
                                EnemySpriteFrame(x = 191, y = 221, width = 59, height = 77),

                            walkFrames =
                                listOf(
                                    EnemySpriteFrame(x = 261, y = 223, width = 63, height = 73),
                                    EnemySpriteFrame(x = 334, y = 223, width = 63, height = 72),
                                    EnemySpriteFrame(x = 407, y = 223, width = 64, height = 72),
                                    EnemySpriteFrame(x = 483, y = 223, width = 62, height = 72)
                                ),

                            walkFrameDurationSeconds =
                                0.13f
                        ),

                    EnemyVisualDirection.UP to
                        EnemySpriteAnimationDefinition(
                            idleFrame =
                                EnemySpriteFrame(x = 189, y = 312, width = 65, height = 86),

                            walkFrames =
                                listOf(
                                    EnemySpriteFrame(x = 264, y = 312, width = 63, height = 88),
                                    EnemySpriteFrame(x = 337, y = 313, width = 62, height = 86),
                                    EnemySpriteFrame(x = 409, y = 313, width = 64, height = 87),
                                    EnemySpriteFrame(x = 485, y = 312, width = 64, height = 88)
                                ),

                            walkFrameDurationSeconds =
                                0.13f
                        )
                )
        )

    val all:
        List<
            EnemySpriteSheetDefinition
            > =
        listOf(
            grayWolf,
            boar,
            baldEagle,
            grizzlyBear,
            beeSwarm,
            crocodile,
            silverbackGorilla
        )
}
