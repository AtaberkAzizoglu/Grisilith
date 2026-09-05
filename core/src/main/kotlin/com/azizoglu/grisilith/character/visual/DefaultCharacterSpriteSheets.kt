package com.azizoglu.grisilith.character.visual

import com.azizoglu.grisilith.content.ContentId

/*
 * Character source PNG files are used unchanged.
 *
 * The portrait on the left side of each source PNG is intentionally
 * not part of gameplay animation. Only the directional sprite rows
 * on the right side are registered here.
 *
 * Direction row order:
 * DOWN, LEFT, RIGHT, UP
 *
 * Column order:
 * IDLE, WALK 1, WALK 2, WALK 3, WALK 4
 */
object DefaultCharacterSpriteSheets {

    val sirImbert =
        CharacterSpriteSheetDefinition(
            characterId =
                ContentId(
                    "CHR_SIR_IMBERT"
                ),

            texturePath =
                "characters/sir_imbert.png",

            sourceWidth =
                612,

            sourceHeight =
                408,

            animations =
                mapOf(
                    CharacterVisualDirection.DOWN to
                        CharacterSpriteAnimationDefinition(
                            idleFrame =
                                CharacterSpriteFrame(
                                    x = 223,
                                    y = 33,
                                    width = 46,
                                    height = 66
                                ),

                            walkFrames =
                                listOf(
                                    CharacterSpriteFrame(
                                        x = 307,
                                        y = 34,
                                        width = 43,
                                        height = 65
                                    ),
                                    CharacterSpriteFrame(
                                        x = 383,
                                        y = 34,
                                        width = 43,
                                        height = 65
                                    ),
                                    CharacterSpriteFrame(
                                        x = 460,
                                        y = 34,
                                        width = 44,
                                        height = 65
                                    ),
                                    CharacterSpriteFrame(
                                        x = 540,
                                        y = 34,
                                        width = 45,
                                        height = 65
                                    )
                                ),

                            walkFrameDurationSeconds =
                                0.12f
                        ),

                    CharacterVisualDirection.LEFT to
                        CharacterSpriteAnimationDefinition(
                            idleFrame =
                                CharacterSpriteFrame(
                                    x = 227,
                                    y = 117,
                                    width = 36,
                                    height = 64
                                ),

                            walkFrames =
                                listOf(
                                    CharacterSpriteFrame(
                                        x = 311,
                                        y = 120,
                                        width = 39,
                                        height = 63
                                    ),
                                    CharacterSpriteFrame(
                                        x = 385,
                                        y = 120,
                                        width = 39,
                                        height = 63
                                    ),
                                    CharacterSpriteFrame(
                                        x = 458,
                                        y = 120,
                                        width = 46,
                                        height = 62
                                    ),
                                    CharacterSpriteFrame(
                                        x = 541,
                                        y = 121,
                                        width = 44,
                                        height = 60
                                    )
                                ),

                            walkFrameDurationSeconds =
                                0.12f
                        ),

                    CharacterVisualDirection.RIGHT to
                        CharacterSpriteAnimationDefinition(
                            idleFrame =
                                CharacterSpriteFrame(
                                    x = 223,
                                    y = 202,
                                    width = 34,
                                    height = 64
                                ),

                            walkFrames =
                                listOf(
                                    CharacterSpriteFrame(
                                        x = 307,
                                        y = 203,
                                        width = 41,
                                        height = 63
                                    ),
                                    CharacterSpriteFrame(
                                        x = 380,
                                        y = 204,
                                        width = 40,
                                        height = 62
                                    ),
                                    CharacterSpriteFrame(
                                        x = 456,
                                        y = 204,
                                        width = 42,
                                        height = 61
                                    ),
                                    CharacterSpriteFrame(
                                        x = 537,
                                        y = 204,
                                        width = 42,
                                        height = 63
                                    )
                                ),

                            walkFrameDurationSeconds =
                                0.12f
                        ),

                    CharacterVisualDirection.UP to
                        CharacterSpriteAnimationDefinition(
                            idleFrame =
                                CharacterSpriteFrame(
                                    x = 223,
                                    y = 288,
                                    width = 44,
                                    height = 66
                                ),

                            walkFrames =
                                listOf(
                                    CharacterSpriteFrame(
                                        x = 305,
                                        y = 288,
                                        width = 43,
                                        height = 66
                                    ),
                                    CharacterSpriteFrame(
                                        x = 378,
                                        y = 288,
                                        width = 42,
                                        height = 66
                                    ),
                                    CharacterSpriteFrame(
                                        x = 457,
                                        y = 288,
                                        width = 42,
                                        height = 66
                                    ),
                                    CharacterSpriteFrame(
                                        x = 537,
                                        y = 288,
                                        width = 43,
                                        height = 66
                                    )
                                ),

                            walkFrameDurationSeconds =
                                0.12f
                        )
                )
        )

    val ywalethYoster =
        CharacterSpriteSheetDefinition(
            characterId =
                ContentId(
                    "CHR_YWALETH_YOSTER"
                ),

            texturePath =
                "characters/ywaleth_yoster.png",

            sourceWidth =
                612,

            sourceHeight =
                408,

            animations =
                mapOf(
                    CharacterVisualDirection.DOWN to
                        CharacterSpriteAnimationDefinition(
                            idleFrame =
                                CharacterSpriteFrame(
                                    x = 217,
                                    y = 31,
                                    width = 37,
                                    height = 68
                                ),

                            walkFrames =
                                listOf(
                                    CharacterSpriteFrame(
                                        x = 305,
                                        y = 31,
                                        width = 36,
                                        height = 68
                                    ),
                                    CharacterSpriteFrame(
                                        x = 383,
                                        y = 31,
                                        width = 37,
                                        height = 68
                                    ),
                                    CharacterSpriteFrame(
                                        x = 462,
                                        y = 31,
                                        width = 37,
                                        height = 68
                                    ),
                                    CharacterSpriteFrame(
                                        x = 542,
                                        y = 31,
                                        width = 35,
                                        height = 68
                                    )
                                ),

                            walkFrameDurationSeconds =
                                0.11f
                        ),

                    CharacterVisualDirection.LEFT to
                        CharacterSpriteAnimationDefinition(
                            idleFrame =
                                CharacterSpriteFrame(
                                    x = 219,
                                    y = 118,
                                    width = 31,
                                    height = 65
                                ),

                            walkFrames =
                                listOf(
                                    CharacterSpriteFrame(
                                        x = 307,
                                        y = 118,
                                        width = 33,
                                        height = 64
                                    ),
                                    CharacterSpriteFrame(
                                        x = 384,
                                        y = 118,
                                        width = 34,
                                        height = 63
                                    ),
                                    CharacterSpriteFrame(
                                        x = 467,
                                        y = 118,
                                        width = 31,
                                        height = 64
                                    ),
                                    CharacterSpriteFrame(
                                        x = 545,
                                        y = 118,
                                        width = 33,
                                        height = 63
                                    )
                                ),

                            walkFrameDurationSeconds =
                                0.11f
                        ),

                    CharacterVisualDirection.RIGHT to
                        CharacterSpriteAnimationDefinition(
                            idleFrame =
                                CharacterSpriteFrame(
                                    x = 217,
                                    y = 204,
                                    width = 31,
                                    height = 64
                                ),

                            walkFrames =
                                listOf(
                                    CharacterSpriteFrame(
                                        x = 306,
                                        y = 204,
                                        width = 31,
                                        height = 64
                                    ),
                                    CharacterSpriteFrame(
                                        x = 384,
                                        y = 204,
                                        width = 32,
                                        height = 63
                                    ),
                                    CharacterSpriteFrame(
                                        x = 464,
                                        y = 204,
                                        width = 31,
                                        height = 64
                                    ),
                                    CharacterSpriteFrame(
                                        x = 545,
                                        y = 204,
                                        width = 30,
                                        height = 63
                                    )
                                ),

                            walkFrameDurationSeconds =
                                0.11f
                        ),

                    CharacterVisualDirection.UP to
                        CharacterSpriteAnimationDefinition(
                            idleFrame =
                                CharacterSpriteFrame(
                                    x = 218,
                                    y = 288,
                                    width = 36,
                                    height = 69
                                ),

                            walkFrames =
                                listOf(
                                    CharacterSpriteFrame(
                                        x = 306,
                                        y = 288,
                                        width = 34,
                                        height = 69
                                    ),
                                    CharacterSpriteFrame(
                                        x = 384,
                                        y = 288,
                                        width = 35,
                                        height = 69
                                    ),
                                    CharacterSpriteFrame(
                                        x = 463,
                                        y = 288,
                                        width = 36,
                                        height = 69
                                    ),
                                    CharacterSpriteFrame(
                                        x = 543,
                                        y = 288,
                                        width = 36,
                                        height = 69
                                    )
                                ),

                            walkFrameDurationSeconds =
                                0.11f
                        )
                )
        )

    val lavenaVol =
        CharacterSpriteSheetDefinition(
            characterId =
                ContentId(
                    "CHR_LAVENA_VOL"
                ),

            texturePath =
                "characters/lavena_vol.png",

            sourceWidth =
                612,

            sourceHeight =
                408,

            animations =
                mapOf(
                    CharacterVisualDirection.DOWN to
                        CharacterSpriteAnimationDefinition(
                            idleFrame =
                                CharacterSpriteFrame(
                                    x = 218,
                                    y = 27,
                                    width = 41,
                                    height = 70
                                ),

                            walkFrames =
                                listOf(
                                    CharacterSpriteFrame(
                                        x = 304,
                                        y = 27,
                                        width = 45,
                                        height = 70
                                    ),
                                    CharacterSpriteFrame(
                                        x = 385,
                                        y = 27,
                                        width = 44,
                                        height = 70
                                    ),
                                    CharacterSpriteFrame(
                                        x = 464,
                                        y = 28,
                                        width = 43,
                                        height = 69
                                    ),
                                    CharacterSpriteFrame(
                                        x = 544,
                                        y = 27,
                                        width = 43,
                                        height = 70
                                    )
                                ),

                            walkFrameDurationSeconds =
                                0.13f
                        ),

                    CharacterVisualDirection.LEFT to
                        CharacterSpriteAnimationDefinition(
                            idleFrame =
                                CharacterSpriteFrame(
                                    x = 226,
                                    y = 114,
                                    width = 33,
                                    height = 67
                                ),

                            walkFrames =
                                listOf(
                                    CharacterSpriteFrame(
                                        x = 312,
                                        y = 114,
                                        width = 36,
                                        height = 68
                                    ),
                                    CharacterSpriteFrame(
                                        x = 386,
                                        y = 114,
                                        width = 40,
                                        height = 67
                                    ),
                                    CharacterSpriteFrame(
                                        x = 465,
                                        y = 114,
                                        width = 39,
                                        height = 67
                                    ),
                                    CharacterSpriteFrame(
                                        x = 545,
                                        y = 117,
                                        width = 36,
                                        height = 64
                                    )
                                ),

                            walkFrameDurationSeconds =
                                0.13f
                        ),

                    CharacterVisualDirection.RIGHT to
                        CharacterSpriteAnimationDefinition(
                            idleFrame =
                                CharacterSpriteFrame(
                                    x = 218,
                                    y = 199,
                                    width = 33,
                                    height = 68
                                ),

                            walkFrames =
                                listOf(
                                    CharacterSpriteFrame(
                                        x = 305,
                                        y = 199,
                                        width = 37,
                                        height = 68
                                    ),
                                    CharacterSpriteFrame(
                                        x = 384,
                                        y = 200,
                                        width = 39,
                                        height = 67
                                    ),
                                    CharacterSpriteFrame(
                                        x = 463,
                                        y = 199,
                                        width = 40,
                                        height = 68
                                    ),
                                    CharacterSpriteFrame(
                                        x = 541,
                                        y = 199,
                                        width = 35,
                                        height = 68
                                    )
                                ),

                            walkFrameDurationSeconds =
                                0.13f
                        ),

                    CharacterVisualDirection.UP to
                        CharacterSpriteAnimationDefinition(
                            idleFrame =
                                CharacterSpriteFrame(
                                    x = 218,
                                    y = 289,
                                    width = 41,
                                    height = 66
                                ),

                            walkFrames =
                                listOf(
                                    CharacterSpriteFrame(
                                        x = 305,
                                        y = 288,
                                        width = 41,
                                        height = 67
                                    ),
                                    CharacterSpriteFrame(
                                        x = 384,
                                        y = 288,
                                        width = 41,
                                        height = 69
                                    ),
                                    CharacterSpriteFrame(
                                        x = 463,
                                        y = 288,
                                        width = 42,
                                        height = 68
                                    ),
                                    CharacterSpriteFrame(
                                        x = 541,
                                        y = 289,
                                        width = 41,
                                        height = 66
                                    )
                                ),

                            walkFrameDurationSeconds =
                                0.13f
                        )
                )
        )

    val koba =
        gridSpriteSheet(
            characterId = "CHR_KOBA",
            texturePath = "characters/koba.png",
            sourceWidth = 1389,
            sourceHeight = 998,
            frameXs = listOf(160, 445, 720, 1000),
            frameWidth = 250,
            walkFrameDurationSeconds = 0.12f
        )

    val linlosDustman =
        gridSpriteSheet(
            characterId = "CHR_LINLOS_DUSTMAN",
            texturePath = "characters/linlos_dustman.png",
            sourceWidth = 1536,
            sourceHeight = 1024,
            frameXs = listOf(80, 430, 795, 1165),
            frameWidth = 260,
            walkFrameDurationSeconds = 0.13f
        )

    val all:
        List<
            CharacterSpriteSheetDefinition
            > =
        listOf(
            sirImbert,
            ywalethYoster,
            lavenaVol,
            koba,
            linlosDustman
        )

    private fun gridSpriteSheet(
        characterId: String,
        texturePath: String,
        sourceWidth: Int,
        sourceHeight: Int,
        frameXs: List<Int>,
        frameWidth: Int,
        walkFrameDurationSeconds: Float
    ): CharacterSpriteSheetDefinition {
        fun animation(row: Int): CharacterSpriteAnimationDefinition {
            val y = row * sourceHeight / 4
            val bottom = (row + 1) * sourceHeight / 4
            val frames =
                frameXs.map { x ->
                    CharacterSpriteFrame(
                        x = x,
                        y = y,
                        width = frameWidth,
                        height = bottom - y
                    )
                }

            return CharacterSpriteAnimationDefinition(
                idleFrame = frames.first(),
                walkFrames = frames,
                walkFrameDurationSeconds = walkFrameDurationSeconds
            )
        }

        return CharacterSpriteSheetDefinition(
            characterId = ContentId(characterId),
            texturePath = texturePath,
            sourceWidth = sourceWidth,
            sourceHeight = sourceHeight,
            animations =
                mapOf(
                    CharacterVisualDirection.DOWN to animation(0),
                    CharacterVisualDirection.LEFT to animation(1),
                    CharacterVisualDirection.RIGHT to animation(2),
                    CharacterVisualDirection.UP to animation(3)
                )
        )
    }
}
