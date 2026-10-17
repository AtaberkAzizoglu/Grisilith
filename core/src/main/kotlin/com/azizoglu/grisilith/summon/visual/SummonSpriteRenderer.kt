package com.azizoglu.grisilith.summon.visual

import com.azizoglu.grisilith.summon.SummonedEntity
import com.azizoglu.grisilith.summon.SummonType
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class SummonSpriteRenderer {

    private val batch =
        SpriteBatch()

    private val textures =
        mutableMapOf<String, Texture>()

    fun render(
        summon: SummonedEntity,
        camera: OrthographicCamera
    ) {
        if (summon.isExpired) {
            return
        }


        val definition =
            definitionFor(
                summon.type
            )

        val frameCount =
            definition.frameCount

        val frameIndex =
            if (
                summon.isVisuallyMoving
            ) {
                (
                    summon.walkAnimationTime /
                        definition.secondsPerFrame
                    )
                    .toInt()
                    .mod(
                        frameCount
                    )
            } else {
                0
            }

        val path =
            framePath(
                definition =
                    definition,

                direction =
                    summon.visualDirection,

                frameIndex =
                    frameIndex
            )

        val texture =
            textures.getOrPut(
                path
            ) {
                Texture(
                    Gdx.files.internal(
                        path
                    )
                ).also {
                        texture ->

                    /*
                     * These are detailed pixel-art sprites.
                     * Nearest prevents the runtime from blurring their edges.
                     */
                    texture.setFilter(
                        Texture.TextureFilter.Nearest,
                        Texture.TextureFilter.Nearest
                    )
                }
            }

        val worldHeight =
            texture.height * definition.pixelToWorldScale

        val worldWidth =
            worldHeight *
                (
                    texture.width.toFloat() /
                        texture.height.toFloat()
                    )

        // Repaired cuts share an eight-pixel feet margin. Keep original native
        // artwork scale and a stable feet pivot; transparent canvas size is irrelevant.
        val drawX =
            summon.centerX -
                worldWidth /
                2f

        val drawY =
            summon.centerY - definition.feetOffset - 8f * definition.pixelToWorldScale

        batch.projectionMatrix =
            camera.combined

        batch.begin()

        batch.setColor(
            1f,
            1f,
            1f,
            1f
        )

        batch.draw(
            texture,
            drawX,
            drawY,
            worldWidth,
            worldHeight
        )

        batch.end()
    }

    private fun framePath(
        definition: VisualDefinition,
        direction: SummonVisualDirection,
        frameIndex: Int
    ): String {

        return definition.root +
            definition.prefix +
            "_" +
            direction
                .name
                .lowercase() +
            "_" +
            frameIndex +
            ".png"
    }

    private fun definitionFor(
        type: SummonType
    ): VisualDefinition {

        return when (type) {
            SummonType.COW ->
                COW

            SummonType.IMP ->
                IMP

            SummonType.KNIGHT_SPIRIT ->
                KNIGHT

            SummonType.TOAD ->
                TOAD

            SummonType.GUARD ->
                GUARD
        }
    }

    fun dispose() {
        textures.values
            .forEach {
                it.dispose()
            }

        textures.clear()

        batch.dispose()
    }

    private data class VisualDefinition(
        val root: String,
        val prefix: String,
        val frameCount: Int,
        val secondsPerFrame: Float,
        val pixelToWorldScale: Float,
        val feetOffset: Float
    )

    companion object {
        // Preserve each family's original native artwork scale, independent of packing.
        private val COW = VisualDefinition("summon_visuals/cow/", "cow", 4, .15f, 58f/140f, 22f)
        private val IMP = VisualDefinition("summon_visuals/imp/", "imp", 4, .13f, 52f/145f, 16f)
        private val KNIGHT = VisualDefinition("summon_visuals/knight/", "knight", 5, .14f, 72f/175f, 29f)
        private val TOAD = VisualDefinition("summon_visuals/toad/", "toad", 4, .16f, 38f/256f, 15f)
        private val GUARD = VisualDefinition("summon_visuals/guard/", "guard", 5, .14f, .274f, 18f)
    }
}
