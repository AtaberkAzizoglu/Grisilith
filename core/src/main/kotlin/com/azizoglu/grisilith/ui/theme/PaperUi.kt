package com.azizoglu.grisilith.ui.theme

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle

/** Shared paper controls for the run journal and preset editor. */
class PaperUi {
    private val buttonTexture = load("paper_button.png")
    private val popupTexture = load("paper_popup.png")
    private val scrollbarTexture = load("paper_scrollbar.png")
    private val button = trimmed(buttonTexture, "paper_button.png")
    private val popup = trimmed(popupTexture, "paper_popup.png")
    private val rail = TextureRegion(scrollbarTexture, 403, 54, 49, 920)
    private val grip = TextureRegion(scrollbarTexture, 1040, 426, 96, 194)

    private fun load(name: String) = Texture(Gdx.files.internal("ui/preset/$name")).apply {
        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }

    private fun trimmed(texture: Texture, name: String): TextureRegion {
        val pixels = Pixmap(Gdx.files.internal("ui/preset/$name"))
        var left = pixels.width
        var top = pixels.height
        var right = 0
        var bottom = 0
        for (y in 0 until pixels.height) for (x in 0 until pixels.width) {
            if ((pixels.getPixel(x, y) and 255) > 160) {
                left = minOf(left, x); right = maxOf(right, x)
                top = minOf(top, y); bottom = maxOf(bottom, y)
            }
        }
        pixels.dispose()
        return TextureRegion(texture, left, top, right - left + 1, bottom - top + 1)
    }

    fun button(batch: SpriteBatch, bounds: Rectangle, selected: Boolean = false, enabled: Boolean = true, danger: Boolean = false) {
        val previous = Color(batch.color)
        batch.color = when {
            !enabled -> Color(0.72f, 0.69f, 0.63f, 0.72f)
            danger -> if (selected) Color(1f, 0.48f, 0.38f, 1f) else Color(1f, 0.33f, 0.26f, 1f)
            selected -> Color(0.78f, 0.89f, 1f, 1f)
            else -> Color.WHITE
        }
        batch.draw(button, bounds.x, bounds.y, bounds.width, bounds.height)
        batch.color = previous
    }

    fun popup(batch: SpriteBatch, bounds: Rectangle) {
        batch.draw(popup, bounds.x, bounds.y, bounds.width, bounds.height)
    }

    fun thumb(bounds: Rectangle, visible: Float, offset: Float): Rectangle {
        val height = (bounds.height * visible.coerceIn(0f, 1f)).coerceIn(32f, bounds.height)
        return Rectangle(bounds.x - 3f, bounds.y + (bounds.height - height) * (1f - offset.coerceIn(0f, 1f)), bounds.width + 6f, height)
    }

    fun scrollbar(batch: SpriteBatch, bounds: Rectangle, visible: Float, offset: Float) {
        batch.draw(rail, bounds.x, bounds.y, bounds.width, bounds.height)
        val thumb = thumb(bounds, visible, offset)
        batch.draw(grip, thumb.x, thumb.y, thumb.width, thumb.height)
    }

    fun dispose() { buttonTexture.dispose(); popupTexture.dispose(); scrollbarTexture.dispose() }
}
