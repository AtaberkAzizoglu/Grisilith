package com.azizoglu.grisilith.map.visual

import com.azizoglu.grisilith.config.GameMap
import com.azizoglu.grisilith.map.MapObstacle
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import kotlin.math.max

class MapObstacleSpriteRenderer(
    private val gameMap: GameMap
) {

    private val batch =
        SpriteBatch()

    private val textures =
        mutableMapOf<String, Texture>()

    fun hasVisualDefinition(
        obstacle: MapObstacle
    ): Boolean {
        return MapObstacleSpriteRegistry
            .contains(
                gameMap = gameMap,
                type = obstacle.type
            )
    }

    fun render(
        obstacles: List<MapObstacle>,
        camera: OrthographicCamera
    ) {
        batch.projectionMatrix =
            camera.combined

        batch.begin()

        obstacles.forEach {
                obstacle ->

            if (!obstacle.rendersVisual) {
                return@forEach
            }

            val definition =
                MapObstacleSpriteRegistry
                    .find(
                        gameMap = gameMap,
                        type = obstacle.type
                    )
                    ?: return@forEach

            val variantIndex =
                obstacle.visualVariant

            val texturePath =
                if (definition.variantTexturePaths.isNotEmpty()) {
                    definition.variantTexturePaths[
                        variantIndex.mod(definition.variantTexturePaths.size)
                    ]
                } else {
                    definition.texturePath
                }

            val texture =
                ensureTexture(texturePath)
                    ?: return@forEach

            val source =
                definition.sourceRegions[
                    variantIndex
                        .mod(
                            definition.sourceRegions.size
                        )
                ]

            val region =
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

            val baseSize =
                max(
                    obstacle.visualBounds.width,
                    obstacle.visualBounds.height
                )

            val drawHeight =
                if (definition.stretchToVisualBounds) {
                    obstacle.visualBounds.height
                } else {
                    baseSize * definition.heightMultiplier
                }

            val drawWidth =
                if (definition.stretchToVisualBounds) {
                    obstacle.visualBounds.width
                } else {
                    drawHeight * source.width / source.height *
                        definition.widthMultiplier
                }

            val drawX =
                obstacle.visualBounds.x +
                    obstacle.visualBounds.width / 2f -
                    drawWidth / 2f

            val drawY =
                obstacle.visualBounds.y +
                    obstacle.visualBounds.height / 2f -
                    drawHeight / 2f +
                    obstacle.visualBounds.height *
                    definition.verticalOffsetMultiplier

            if (definition.tileVertically) {
                renderVerticalTiles(
                    region = region,
                    x = obstacle.visualBounds.x,
                    y = obstacle.visualBounds.y,
                    width = obstacle.visualBounds.width,
                    height = obstacle.visualBounds.height,
                    sourceAspectRatio = source.height.toFloat() / source.width
                )
            } else {
                batch.draw(
                    region,
                    drawX,
                    drawY,
                    drawWidth,
                    drawHeight
                )
            }
        }

        batch.end()
    }

    private fun renderVerticalTiles(
        region: TextureRegion,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        sourceAspectRatio: Float
    ) {
        val tileHeight = width * sourceAspectRatio
        var drawY = y

        while (drawY < y + height) {
            val remainingHeight = y + height - drawY
            val currentHeight = tileHeight.coerceAtMost(remainingHeight)
            val visibleRatio = currentHeight / tileHeight
            val clippedRegion = TextureRegion(region)

            if (visibleRatio < 1f) {
                clippedRegion.regionHeight =
                    (region.regionHeight * visibleRatio)
                        .toInt()
                        .coerceAtLeast(1)
            }

            batch.draw(
                clippedRegion,
                x,
                drawY,
                width,
                currentHeight
            )

            drawY += currentHeight
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
        batch.dispose()
    }
}
