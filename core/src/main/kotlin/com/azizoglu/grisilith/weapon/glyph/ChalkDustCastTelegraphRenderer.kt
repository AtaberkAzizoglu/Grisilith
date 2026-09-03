package com.azizoglu.grisilith.weapon.glyph

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

class ChalkDustCastTelegraphRenderer {

    fun render(
        shapeRenderer: ShapeRenderer,
        glyphType: GlyphType,
        centerX: Float,
        centerY: Float,
        elapsedSeconds: Float
    ) {
        val frameIndex =
            frameIndexFor(
                elapsedSeconds
            )

        val path =
            assetPath(
                glyphType =
                    glyphType,

                frameIndex =
                    frameIndex
            )

        val texture =
            sharedTextures.getOrPut(
                path
            ) {
                Texture(
                    Gdx.files.internal(
                        path
                    )
                ).also {
                        loaded ->

                    loaded.setFilter(
                        Texture.TextureFilter.Linear,
                        Texture.TextureFilter.Linear
                    )
                }
            }

        val previousShapeType =
            shapeRenderer.currentType

        shapeRenderer.end()

        sharedBatch.projectionMatrix =
            shapeRenderer.projectionMatrix

        sharedBatch.begin()

        val size =
            CAST_WORLD_SIZE

        sharedBatch.draw(
            texture,
            centerX -
                size / 2f,
            centerY -
                size / 2f,
            size,
            size
        )

        sharedBatch.end()

        shapeRenderer.begin(
            previousShapeType
                ?: ShapeRenderer.ShapeType.Filled
        )
    }

    private fun frameIndexFor(
        elapsedSeconds: Float
    ): Int {
        val safeElapsed =
            elapsedSeconds
                .coerceIn(
                    0f,
                    TOTAL_CAST_SECONDS
                )

        /*
         * Frames 0..6 occupy exactly the first 2.0 seconds.
         * Frame 7 then remains on screen for the final 0.5 seconds.
         */
        if (
            safeElapsed >=
            FINAL_FRAME_START_SECONDS
        ) {
            return FINAL_FRAME_INDEX
        }

        val frame =
            (
                safeElapsed /
                    EARLY_FRAME_DURATION_SECONDS
                )
                .toInt()

        return frame.coerceIn(
            0,
            FINAL_FRAME_INDEX - 1
        )
    }

    private fun assetPath(
        glyphType: GlyphType,
        frameIndex: Int
    ): String {
        val folder =
            when (glyphType) {
                GlyphType.CALL_LIGHTNING ->
                    "call_lightning"

                GlyphType.SINGULARITY ->
                    "singularity"

                GlyphType.TSUNAMI ->
                    "tsunami"

                GlyphType.METEOR ->
                    "meteor"
            }

        return "effects/chalk_dust_cast/" +
            folder +
            "/" +
            folder +
            "_" +
            frameIndex +
            ".png"
    }

    companion object {
        private const val TOTAL_CAST_SECONDS =
            2.5f

        private const val FINAL_FRAME_DURATION_SECONDS =
            0.5f

        private const val FINAL_FRAME_START_SECONDS =
            TOTAL_CAST_SECONDS -
                FINAL_FRAME_DURATION_SECONDS

        private const val FINAL_FRAME_INDEX =
            7

        private const val EARLY_FRAME_DURATION_SECONDS =
            FINAL_FRAME_START_SECONDS /
                7f

        private const val CAST_WORLD_SIZE =
            184f

        private val sharedBatch by lazy {
            SpriteBatch()
        }

        private val sharedTextures =
            mutableMapOf<String, Texture>()
    }
}
