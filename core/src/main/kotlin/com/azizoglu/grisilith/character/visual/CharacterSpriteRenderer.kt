package com.azizoglu.grisilith.character.visual

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.player.Player
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import kotlin.math.floor
import kotlin.math.max

class CharacterSpriteRenderer {

    companion object {
        private const val CHARACTER_VISUAL_WORLD_SIZE =
            48f
    }

    private val batch =
        SpriteBatch()

    private val textures =
        mutableMapOf<
            String,
            Texture
            >()

    private val regions =
        mutableMapOf<
            RegionKey,
            TextureRegion
            >()

    fun render(
        player: Player,
        characterId: ContentId,
        camera: OrthographicCamera
    ) {
        val definition =
            CharacterSpriteSheetRegistry
                .find(
                    characterId
                )
                ?: return

        batch.projectionMatrix =
            camera.combined

        batch.enableBlending()
        batch.begin()

        renderPlayer(
            player =
                player,

            definition =
                definition
        )

        batch.color =
            Color.WHITE

        batch.end()
    }

    fun hasVisualDefinition(
        characterId: ContentId?
    ): Boolean {

        return CharacterSpriteSheetRegistry
            .contains(
                characterId
            )
    }

    private fun renderPlayer(
        player: Player,
        definition:
        CharacterSpriteSheetDefinition
    ) {
        val visualState =
            player.currentVisualState

        val animation =
            definition.animationFor(
                visualState.direction
            )

        val frame =
            when (
                visualState.animation
            ) {
                CharacterVisualAnimation.IDLE ->
                    animation.idleFrame

                CharacterVisualAnimation.WALK ->
                    selectWalkFrame(
                        animation =
                            animation,

                        animationTime =
                            visualState
                                .currentAnimationTime()
                    )
            }

        val texture =
            textureFor(
                definition
            )

        val region =
            regionFor(
                texture =
                    texture,

                frame =
                    frame
            )

        /*
         * IMPORTANT: visual size is independent from the collision hitbox.
         * Player.bounds may be a smaller feet/physics footprint, but the
         * character must keep its original 48 world-unit visual footprint.
         */
        val worldSize =
            CHARACTER_VISUAL_WORLD_SIZE

        /*
         * Character source frames are not perfect squares.
         * We preserve the source aspect ratio and fit the largest
         * sprite side inside the player's 48 world-unit footprint.
         */
        val sourceLargestSide =
            max(
                frame.width,
                frame.height
            )
                .toFloat()
                .coerceAtLeast(
                    1f
                )

        val scale =
            worldSize /
                sourceLargestSide

        val drawWidth =
            frame.width *
                scale

        val drawHeight =
            frame.height *
                scale

        /*
         * Horizontal center follows the collision body.
         * The bottom of the sprite follows the bottom of the player's
         * collision box, giving all directional frames a stable feet pivot.
         */
        val drawX =
            player.centerX -
                drawWidth /
                2f

        val drawY =
            player.centerY -
                CHARACTER_VISUAL_WORLD_SIZE /
                2f

        batch.color =
            if (
                player.health.isDead
            ) {
                Color(
                    0.45f,
                    0.45f,
                    0.45f,
                    1f
                )
            } else {
                Color.WHITE
            }

        batch.draw(
            region,
            drawX,
            drawY,
            drawWidth,
            drawHeight
        )
    }

    private fun selectWalkFrame(
        animation:
        CharacterSpriteAnimationDefinition,

        animationTime:
        Float
    ): CharacterSpriteFrame {

        val frameCount =
            animation
                .walkFrames
                .size

        val frameIndex =
            (
                floor(
                    animationTime /
                        animation
                            .walkFrameDurationSeconds
                )
                    .toInt()
                )
                .mod(
                    frameCount
                )

        return animation
            .walkFrames[
            frameIndex
        ]
    }

    private fun textureFor(
        definition:
        CharacterSpriteSheetDefinition
    ): Texture {

        return textures
            .getOrPut(
                definition.texturePath
            ) {
                val texture =
                    Texture(
                        Gdx.files.internal(
                            definition.texturePath
                        )
                    )

                texture.setFilter(
                    Texture.TextureFilter.Nearest,
                    Texture.TextureFilter.Nearest
                )

                require(
                    texture.width ==
                        definition.sourceWidth &&
                        texture.height ==
                        definition.sourceHeight
                ) {
                    "Unexpected texture size for ${definition.texturePath}: " +
                        "${texture.width}x${texture.height}; expected " +
                        "${definition.sourceWidth}x${definition.sourceHeight}"
                }

                texture
            }
    }

    private fun regionFor(
        texture: Texture,
        frame: CharacterSpriteFrame
    ): TextureRegion {

        val key =
            RegionKey(
                texture =
                    texture,

                x =
                    frame.x,

                y =
                    frame.y,

                width =
                    frame.width,

                height =
                    frame.height
            )

        return regions
            .getOrPut(
                key
            ) {
                TextureRegion(
                    texture,
                    frame.x,
                    frame.y,
                    frame.width,
                    frame.height
                )
            }
    }

    fun dispose() {

        regions.clear()

        textures.values
            .forEach {
                    texture ->
                texture.dispose()
            }

        textures.clear()

        batch.dispose()
    }

    private data class RegionKey(
        val texture: Texture,
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int
    )
}
