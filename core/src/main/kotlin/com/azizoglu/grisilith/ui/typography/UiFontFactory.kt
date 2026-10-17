package com.azizoglu.grisilith.ui.typography

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator

/**
 * Creates packaged, platform-independent fonts for the Grisilith UI.
 *
 * Every caller owns the returned BitmapFont and must dispose it with its
 * screen/renderer. Keeping creation here guarantees identical glyph coverage,
 * filtering and outline treatment on Windows and Android.
 */
object UiFontFactory {

    private const val ROOT = "ui/fonts"

    private const val DISPLAY_PATH = "$ROOT/AlmendraSC-Regular.ttf"
    private const val DECORATIVE_PATH = "$ROOT/EagleLake-Regular.ttf"
    private const val BODY_PATH = "$ROOT/Alegreya-Variable.ttf"
    private const val NUMERIC_PATH = "$ROOT/FiraSansCondensed-Regular.ttf"

    private val UI_CHARACTERS =
        FreeTypeFontGenerator.DEFAULT_CHARS +
            "ÇĞİÖŞÜçğıöşüÂâÊêÎîÔôÛûÄäÉéÈèÁáÀàÑñ" +
            "₺€£¥°±×÷≤≥…–—→←↑↓✓✕•"

    fun createDisplay(size: Int = 20): BitmapFont =
        create(DISPLAY_PATH, size, borderWidth = 0.65f)

    fun createDecorative(size: Int = 24): BitmapFont =
        create(DECORATIVE_PATH, size, borderWidth = 0.75f)

    fun createBody(size: Int = 18): BitmapFont =
        create(BODY_PATH, size, borderWidth = 0.45f)

    fun createNumeric(size: Int = 18): BitmapFont =
        create(NUMERIC_PATH, size, borderWidth = 0.50f)

    private fun create(
        path: String,
        size: Int,
        borderWidth: Float
    ): BitmapFont {
        val generator = FreeTypeFontGenerator(Gdx.files.internal(path))
        return try {
            val parameter = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
                this.size = size
                characters = UI_CHARACTERS
                color = Color.WHITE
                this.borderWidth = borderWidth
                borderColor = Color(0.035f, 0.025f, 0.045f, 0.96f)
                borderStraight = false
                kerning = true
                minFilter = Texture.TextureFilter.Linear
                magFilter = Texture.TextureFilter.Linear
                hinting = FreeTypeFontGenerator.Hinting.AutoMedium
            }
            generator.generateFont(parameter).also { font ->
                font.setUseIntegerPositions(false)
            }
        } finally {
            generator.dispose()
        }
    }
}
