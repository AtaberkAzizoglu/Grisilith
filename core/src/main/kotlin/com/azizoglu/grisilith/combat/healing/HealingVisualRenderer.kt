package com.azizoglu.grisilith.combat.healing

import com.azizoglu.grisilith.combat.HealingVisualType
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion

class HealingVisualRenderer {
    private val batch = SpriteBatch()
    private var texture: Texture? = null

    fun render(cameraCombined: com.badlogic.gdx.math.Matrix4) {
        if (HealingVisualManager.activeEvents.isEmpty()) return
        val sheet = texture ?: Texture(Gdx.files.internal(PATH)).also {
            it.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            texture = it
        }
        batch.projectionMatrix = cameraCombined
        batch.begin()
        HealingVisualManager.activeEvents.forEach { event ->
            val anchor = event.currentAnchor()
            val worldSize = (anchor.ownerSize * 1.05f).coerceIn(44f, 118f)
            val column = (event.progress * 4f).toInt().coerceIn(0, 3)
            val row = if (event.type == HealingVisualType.HEAL) 0 else 1
            val region = TextureRegion(sheet, column * 512, row * 512, 512, 512)
            val alpha = if (event.progress < 0.7f) 1f else (1f - event.progress) / 0.3f
            batch.color = Color(1f, 1f, 1f, alpha.coerceIn(0f, 1f))
            batch.draw(
                region,
                anchor.centerX - worldSize / 2f,
                anchor.centerY - worldSize * 0.28f,
                worldSize,
                worldSize
            )
        }
        batch.color = Color.WHITE
        batch.end()
    }

    fun dispose() {
        texture?.dispose()
        batch.dispose()
    }

    companion object {
        private const val PATH = "effects/heal_lifesteal.png"
    }
}
