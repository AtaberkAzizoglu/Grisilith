package com.azizoglu.grisilith.summon.visual

import com.azizoglu.grisilith.summon.SummonedEntity
import com.azizoglu.grisilith.summon.SummonType
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion

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

        if (summon.type == SummonType.GUARD) {
            renderGuard(summon, camera)
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
            definition.worldHeight

        val worldWidth =
            worldHeight *
                (
                    texture.width.toFloat() /
                        texture.height.toFloat()
                    )

        /*
         * Cell crops intentionally retain transparent padding.
         * A small Y anchor adjustment places the character's feet/body around
         * the gameplay entity center without changing combat bounds.
         */
        val drawX =
            summon.centerX -
                worldWidth /
                2f

        val drawY =
            summon.centerY -
                worldHeight *
                definition.centerAnchorRatio

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

    private fun renderGuard(
        summon: SummonedEntity,
        camera: OrthographicCamera
    ) {
        val definition = GUARD
        val frameIndex =
            if (summon.isVisuallyMoving) {
                (summon.walkAnimationTime / definition.secondsPerFrame)
                    .toInt()
                    .mod(definition.frameCount)
            } else {
                0
            }

        val texture =
            textures.getOrPut(GUARD_SHEET_PATH) {
                Texture(
                    Gdx.files.internal(GUARD_SHEET_PATH)
                ).also {
                    it.setFilter(
                        Texture.TextureFilter.Nearest,
                        Texture.TextureFilter.Nearest
                    )
                }
            }

        val rowIndex =
            when (summon.visualDirection) {
                SummonVisualDirection.SOUTH -> 0
                SummonVisualDirection.EAST -> 1
                SummonVisualDirection.NORTH -> 2
                SummonVisualDirection.WEST -> 3
            }

        val sourceX = GUARD_FRAME_X[frameIndex]
        val sourceY = GUARD_ROW_Y[rowIndex]
        val sourceHeight = GUARD_ROW_HEIGHT[rowIndex]

        val region =
            TextureRegion(
                texture,
                sourceX,
                sourceY,
                GUARD_FRAME_WIDTH,
                sourceHeight
            )

        val worldHeight =
            sourceHeight * GUARD_PIXEL_TO_WORLD_SCALE

        val worldWidth =
            GUARD_FRAME_WIDTH * GUARD_PIXEL_TO_WORLD_SCALE

        val drawY =
            summon.centerY -
                GUARD_FEET_OFFSET_FROM_CENTER -
                GUARD_BOTTOM_PADDING[rowIndex] *
                    GUARD_PIXEL_TO_WORLD_SCALE

        batch.projectionMatrix = camera.combined
        batch.begin()
        batch.setColor(1f, 1f, 1f, 1f)
        batch.draw(
            region,
            summon.centerX - worldWidth / 2f,
            drawY,
            worldWidth,
            worldHeight
        )
        batch.end()
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
        val worldHeight: Float,
        val centerAnchorRatio: Float
    )

    companion object {
        private val COW =
            VisualDefinition(
                root =
                    "summon_visuals/cow/",

                prefix =
                    "cow",

                frameCount =
                    4,

                secondsPerFrame =
                    0.15f,

                worldHeight =
                    58f,

                centerAnchorRatio =
                    0.43f
            )

        private val IMP =
            VisualDefinition(
                root =
                    "summon_visuals/imp/",

                prefix =
                    "imp",

                frameCount =
                    4,

                secondsPerFrame =
                    0.13f,

                worldHeight =
                    52f,

                centerAnchorRatio =
                    0.44f
            )

        private val KNIGHT =
            VisualDefinition(
                root =
                    "summon_visuals/knight/",

                prefix =
                    "knight",

                frameCount =
                    5,

                secondsPerFrame =
                    0.14f,

                worldHeight =
                    72f,

                centerAnchorRatio =
                    0.44f
            )

        private val TOAD =
            VisualDefinition(
                root = "summon_visuals/toad/",
                prefix = "toad",
                frameCount = 4,
                secondsPerFrame = 0.16f,
                worldHeight = 38f,
                centerAnchorRatio = 0.45f
            )

        private val GUARD =
            VisualDefinition(
                root = "",
                prefix = "",
                frameCount = 5,
                secondsPerFrame = 0.14f,
                worldHeight = 76f,
                centerAnchorRatio = 0.46f
            )

        private const val GUARD_SHEET_PATH =
            "summon_visuals/guard_sheet.png"

        private const val GUARD_FRAME_WIDTH =
            196

        private val GUARD_FRAME_X =
            intArrayOf(438, 632, 836, 1034, 1233)

        private val GUARD_ROW_Y =
            intArrayOf(0, 277, 525, 772)

        private val GUARD_ROW_HEIGHT =
            intArrayOf(277, 248, 247, 252)

        private val GUARD_BOTTOM_PADDING =
            intArrayOf(9, 4, 0, 28)

        private const val GUARD_PIXEL_TO_WORLD_SCALE =
            0.274f

        private const val GUARD_FEET_OFFSET_FROM_CENTER =
            18f
    }
}
