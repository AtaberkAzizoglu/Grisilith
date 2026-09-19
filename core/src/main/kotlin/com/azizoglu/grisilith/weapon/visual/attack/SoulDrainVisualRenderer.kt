package com.azizoglu.grisilith.weapon.visual.attack

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class SoulDrainVisualRenderer {
    private val batch = SpriteBatch()

    private val texture by lazy {
        Texture(
            Gdx.files.internal(SPRITE_SHEET_PATH)
        ).also { loadedTexture ->
            loadedTexture.setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear
            )
        }
    }

    fun render(camera: OrthographicCamera) {
        val events = SoulDrainVisualManager.activeEvents
        if (events.isEmpty()) {
            return
        }

        batch.projectionMatrix = camera.combined
        batch.begin()

        events.forEach { event ->
            val width = VISUAL_WIDTH
            val height = VISUAL_HEIGHT
            val frameIndex = event.frameIndex

            batch.setColor(1f, 1f, 1f, event.alpha)
            batch.draw(
                texture,
                event.centerX - width / 2f,
                event.centerY - height / 2f,
                width / 2f,
                height / 2f,
                width,
                height,
                1f,
                1f,
                if (frameIndex >= 2) event.rotationDegrees else 0f,
                frameIndex * SOURCE_FRAME_WIDTH,
                0,
                SOURCE_FRAME_WIDTH,
                SOURCE_FRAME_HEIGHT,
                false,
                false
            )
        }

        batch.setColor(1f, 1f, 1f, 1f)
        batch.end()
    }

    fun dispose() {
        if (texture.isManaged) {
            texture.dispose()
        }
        batch.dispose()
    }

    companion object {
        private const val SPRITE_SHEET_PATH =
            "weapons_animations/weapon_skills/soul_drain/soul_drain.png"

        private const val SOURCE_FRAME_WIDTH = 362
        private const val SOURCE_FRAME_HEIGHT = 724

        /*
         * The source cells include deliberate transparent padding. Keeping a
         * shared canvas preserves the authored shrinking/launch transition.
         */
        private const val VISUAL_WIDTH = 68f
        private const val VISUAL_HEIGHT = 136f
    }
}
