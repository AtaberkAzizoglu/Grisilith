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

            val definition =
                MapObstacleSpriteRegistry
                    .find(
                        gameMap = gameMap,
                        type = obstacle.type
                    )
                    ?: return@forEach

            val texture =
                ensureTexture(
                    definition.texturePath
                ) ?: return@forEach

            val source =
                definition.sourceRegions[
                    obstacle.visualVariant
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
                baseSize *
                    definition.heightMultiplier

            val drawWidth =
                drawHeight *
                    source.width /
                    source.height *
                    definition.widthMultiplier

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
