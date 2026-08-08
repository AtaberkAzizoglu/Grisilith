package com.azizoglu.grisilith.map.visual

import com.azizoglu.grisilith.map.MapObstacle
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import kotlin.math.max

class MapObstacleSpriteRenderer {

    private val batch =
        SpriteBatch()

    private var texture:
        Texture? =
        null

    private var regions:
        Array<Array<TextureRegion>>? =
        null

    fun hasVisualDefinition(
        obstacle: MapObstacle
    ): Boolean {
        return MapObstacleSpriteRegistry
            .contains(
                obstacle.type
            )
    }

    fun render(
        obstacles: List<MapObstacle>,
        camera: OrthographicCamera
    ) {
        val texture =
            ensureTexture()
                ?: return

        val regions =
            ensureRegions(
                texture
            )

        batch.projectionMatrix =
            camera.combined

        batch.begin()

        obstacles.forEach {
                obstacle ->

            val definition =
                MapObstacleSpriteRegistry
                    .find(
                        obstacle.type
                    )
                    ?: return@forEach

            val variant =
                obstacle.visualVariant
                    .coerceIn(
                        0,
                        3
                    )

            val region =
                regions[
                    definition.rowIndex
                ][
                    variant
                ]

            /*
             * Collision bounds are intentionally untouched.
             * The sprite is a visual layer that can extend beyond them.
             */
            val baseSize =
                max(
                    obstacle.visualBounds.width,
                    obstacle.visualBounds.height
                )

            val drawWidth =
                baseSize *
                    definition.widthMultiplier

            val drawHeight =
                baseSize *
                    definition.heightMultiplier

            val drawX =
                obstacle.visualBounds.x +
                    obstacle.visualBounds.width /
                    2f -
                    drawWidth /
                    2f

            /*
             * Bottom/feet style pivot:
             * artwork grows mostly upward from the obstacle bounds.
             */
            val drawY =
                obstacle.visualBounds.y +
                    obstacle.visualBounds.height /
                    2f -
                    drawHeight /
                    2f +
                    obstacle.visualBounds.height *
                    definition.verticalOffsetMultiplier

            batch.draw(
                region,
                drawX,
                drawY,
                drawWidth,
                drawHeight
            )
        }

        batch.end()
    }

    private fun ensureTexture():
        Texture? {

        texture
            ?.let {
                return it
            }

        val handle =
            Gdx.files.internal(
                MapObstacleSpriteRegistry
                    .TEXTURE_PATH
            )

        if (!handle.exists()) {
            return null
        }

        return Texture(handle)
            .also {
                    texture ->
                texture.setFilter(
                    Texture.TextureFilter.Nearest,
                    Texture.TextureFilter.Nearest
                )

                this.texture =
                    texture
            }
    }

    private fun ensureRegions(
        texture: Texture
    ): Array<Array<TextureRegion>> {

        regions
            ?.let {
                return it
            }

        val cellSize =
            MapObstacleSpriteRegistry
                .CELL_SIZE

        val built =
            Array(
                5
            ) {
                    row ->
                Array(
                    4
                ) {
                        column ->
                    TextureRegion(
                        texture,
                        column *
                            cellSize,
                        row *
                            cellSize,
                        cellSize,
                        cellSize
                    )
                }
            }

        regions =
            built

        return built
    }

    fun dispose() {
        texture
            ?.dispose()

        texture =
            null

        regions =
            null

        batch.dispose()
    }
}
