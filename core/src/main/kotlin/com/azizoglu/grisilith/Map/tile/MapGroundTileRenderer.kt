package com.azizoglu.grisilith.map.tile

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import kotlin.math.ceil
import kotlin.math.floor

class MapGroundTileRenderer(
    private val worldWidth: Float,
    private val worldHeight: Float
) {

    private val definition =
        MapTileSpriteRegistry.grass

    private val batch =
        SpriteBatch()

    private var texture:
        Texture? =
        null

    private var regions:
        Array<TextureRegion>? =
        null

    fun render(
        camera: OrthographicCamera
    ) {
        val texture =
            ensureTexture()
                ?: return

        val regions =
            ensureRegions(
                texture
            )

        val tileSize =
            definition.worldTileSize

        /*
         * Sadece kameranın görebildiği tile'ları çiziyoruz.
         * Map tamamını her frame çizmek yerine viewport'a göre cull ediyoruz.
         */
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
                .coerceAtLeast(
                    0
                )

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
                .coerceAtLeast(
                    0
                )

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
                        column =
                            column,

                        row =
                            row,

                        variationCount =
                            regions.size
                    )

                batch.draw(
                    regions[
                        variation
                    ],

                    column *
                        tileSize,

                    row *
                        tileSize,

                    tileSize,
                    tileSize
                )

                column +=
                    1
            }

            row +=
                1
        }

        batch.end()
    }

    /*
     * Random çağrısı kullanmıyoruz.
     * Aynı tile koordinatı her frame aynı variation'ı döndürür;
     * bu yüzden hareket ederken texture titreşmez/değişmez.
     */
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

    private fun ensureTexture():
        Texture? {

        texture
            ?.let {
                return it
            }

        val handle =
            Gdx.files.internal(
                definition.texturePath
            )

        if (!handle.exists()) {
            return null
        }

        return Texture(
            handle
        )
            .also {
                    loadedTexture ->

                loadedTexture.setFilter(
                    Texture.TextureFilter.Nearest,
                    Texture.TextureFilter.Nearest
                )

                texture =
                    loadedTexture
            }
    }

    private fun ensureRegions(
        texture: Texture
    ): Array<TextureRegion> {

        regions
            ?.let {
                return it
            }

        val sourceSize =
            definition.sourceTileSize

        val built =
            Array(
                definition.variationCount
            ) {
                    index ->

                TextureRegion(
                    texture,
                    index *
                        sourceSize,
                    0,
                    sourceSize,
                    sourceSize
                )
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
