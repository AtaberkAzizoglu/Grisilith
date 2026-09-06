package com.azizoglu.grisilith.map.tile

import com.azizoglu.grisilith.config.GameMap
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import kotlin.math.ceil
import kotlin.math.floor

class MapGroundTileRenderer(
    private val gameMap: GameMap,
    private val worldWidth: Float,
    private val worldHeight: Float
) {

    private companion object {
        const val TILE_SEAM_OVERLAP =
            0.5f
    }

    private val groundDefinition =
        MapTileSpriteRegistry
            .groundFor(
                gameMap
            )

    private val batch =
        SpriteBatch()

    private val textures =
        mutableMapOf<String, Texture>()

    private val regions =
        mutableMapOf<
            MapTileSpriteDefinition,
            Array<TextureRegion>
            >()

    fun render(
        camera: OrthographicCamera
    ) {
        renderGround(
            camera
        )
    }

    private fun renderGround(
        camera: OrthographicCamera
    ) {
        val groundRegions =
            ensureRegions(
                groundDefinition
            ) ?: return

        val tileSize =
            groundDefinition.worldTileSize

        val viewportWidth =
            camera.viewportWidth *
                camera.zoom

        val viewportHeight =
            camera.viewportHeight *
                camera.zoom

        val left =
            camera.position.x -
                viewportWidth / 2f

        val right =
            camera.position.x +
                viewportWidth / 2f

        val bottom =
            camera.position.y -
                viewportHeight / 2f

        val top =
            camera.position.y +
                viewportHeight / 2f

        val minColumn =
            floor(
                left /
                    tileSize
            )
                .toInt()
                .coerceAtLeast(0)

        val maxColumn =
            ceil(
                right /
                    tileSize
            )
                .toInt()
                .coerceAtMost(
                    ceil(
                        worldWidth /
                            tileSize
                    )
                        .toInt()
                )

        val minRow =
            floor(
                bottom /
                    tileSize
            )
                .toInt()
                .coerceAtLeast(0)

        val maxRow =
            ceil(
                top /
                    tileSize
            )
                .toInt()
                .coerceAtMost(
                    ceil(
                        worldHeight /
                            tileSize
                    )
                        .toInt()
                )

        batch.projectionMatrix =
            camera.combined

        batch.begin()

        var row =
            minRow

        while (
            row <=
            maxRow
        ) {
            var column =
                minColumn

            while (
                column <=
                maxColumn
            ) {
                val variation =
                    deterministicVariation(
                        column = column,
                        row = row,
                        variationCount =
                            groundRegions.size
                    )

                batch.draw(
                    groundRegions[variation],
                    column * tileSize -
                        TILE_SEAM_OVERLAP,
                    row * tileSize -
                        TILE_SEAM_OVERLAP,
                    tileSize +
                        TILE_SEAM_OVERLAP * 2f,
                    tileSize +
                        TILE_SEAM_OVERLAP * 2f
                )

                column +=
                    1
            }

            row +=
                1
        }

        batch.end()
    }

    private fun deterministicVariation(
        column: Int,
        row: Int,
        variationCount: Int
    ): Int {
        var hash =
            column *
                73856093

        hash =
            hash xor
                (
                    row *
                        19349663
                    )

        hash =
            hash xor
                (
                    hash ushr
                        13
                    )

        return (
            hash and
                Int.MAX_VALUE
            ) %
            variationCount
    }

    private fun ensureRegions(
        definition: MapTileSpriteDefinition
    ): Array<TextureRegion>? {
        regions[definition]
            ?.let {
                return it
            }

        val texture =
            ensureTexture(
                definition.texturePath
            ) ?: return null

        return definition.sourceRegions
            .map {
                    source ->

                TextureRegion(texture)
                    .apply {
                        setRegion(
                            (source.x + 0.5f) /
                                texture.width,
                            (source.y + 0.5f) /
                                texture.height,
                            (source.x + source.width - 0.5f) /
                                texture.width,
                            (source.y + source.height - 0.5f) /
                                texture.height
                        )
                    }
            }
            .toTypedArray()
            .also {
                regions[definition] =
                    it
            }
    }

    private fun ensureTexture(
        path: String
    ): Texture? {
        textures[path]
            ?.let {
                return it
            }

        val handle =
            Gdx.files.internal(
                path
            )

        if (!handle.exists()) {
            return null
        }

        return Texture(handle)
            .also {
                    texture ->

                texture.setFilter(
                    Texture.TextureFilter.Linear,
                    Texture.TextureFilter.Linear
                )

                textures[path] =
                    texture
            }
    }

    fun dispose() {
        textures.values
            .forEach(
                Texture::dispose
            )

        textures.clear()
        regions.clear()
        batch.dispose()
    }
}
