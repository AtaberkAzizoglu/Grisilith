package com.azizoglu.grisilith.character.visual

import com.azizoglu.grisilith.content.ContentId

/** Normalized full-body atlases. Idle + four walk phases, stable feet pivots. */
object DefaultCharacterSpriteSheets {
    val sirImbert = aligned("CHR_SIR_IMBERT", "sir_imbert", .12f)
    val ywalethYoster = aligned("CHR_YWALETH_YOSTER", "ywaleth_yoster", .12f)
    val lavenaVol = aligned("CHR_LAVENA_VOL", "lavena_vol", .13f)
    val koba = aligned("CHR_KOBA", "koba", .12f)
    val linlosDustman = aligned("CHR_LINLOS_DUSTMAN", "linlos_dustman", .13f)
    val all = listOf(sirImbert, ywalethYoster, lavenaVol, koba, linlosDustman)

    private fun aligned(id: String, name: String, duration: Float): CharacterSpriteSheetDefinition {
        fun animation(row: Int): CharacterSpriteAnimationDefinition {
            val frames=(0..4).map { CharacterSpriteFrame(it*256,row*256,256,256) }
            return CharacterSpriteAnimationDefinition(frames[0],frames.drop(1),duration)
        }
        return CharacterSpriteSheetDefinition(
            characterId=ContentId(id), texturePath="characters/aligned_v4/$name.png",
            sourceWidth=1280, sourceHeight=1024, feetInsetPixels=6, contentHeightPixels=244,
            animations=mapOf(
                CharacterVisualDirection.DOWN to animation(0),
                CharacterVisualDirection.LEFT to animation(1),
                CharacterVisualDirection.RIGHT to animation(2),
                CharacterVisualDirection.UP to animation(3)))
    }
}
