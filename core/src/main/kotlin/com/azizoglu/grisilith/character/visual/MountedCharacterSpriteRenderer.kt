package com.azizoglu.grisilith.character.visual

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.player.Player
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import kotlin.math.floor

/*
 * Phantom Steed mounted-character renderer.
 *
 * IMPORTANT:
 * This is visual-only.
 * Player bounds, center, movement, combat hitbox and Tamga stats are untouched.
 *
 * The mounted sprite replaces the normal character sprite while Phantom Steed
 * is ACTIVE. It is NOT rendered as a second horse overlay.
 */
class MountedCharacterSpriteRenderer {

    private data class MountedDefinition(
        val assetRoot: String,
        val prefix: String,
        val worldHeight: Float,
        val bottomOffset: Float
    )

    private val batch =
        SpriteBatch()

    private val textures =
        mutableMapOf<String, Texture>()

    fun hasVisualDefinition(
        characterId: ContentId?
    ): Boolean {
        return definitionFor(
            characterId
        ) != null
    }

    fun render(
        player: Player,
        characterId: ContentId,
        camera: OrthographicCamera
    ) {
        val definition =
            definitionFor(
                characterId
            )
                ?: return

        val visualState =
            player.currentVisualState

        val directionName =
            when (
                visualState.direction
            ) {
                CharacterVisualDirection.DOWN ->
                    "down"

                CharacterVisualDirection.LEFT ->
                    "left"

                CharacterVisualDirection.RIGHT ->
                    "right"

                CharacterVisualDirection.UP ->
                    "up"
            }

        val frameIndex =
            when (
                visualState.animation
            ) {
                CharacterVisualAnimation.IDLE ->
                    0

                CharacterVisualAnimation.WALK ->
                    (
                        floor(
                            visualState
                                .currentAnimationTime() /
                                WALK_FRAME_DURATION_SECONDS
                        )
                            .toInt()
                        )
                        .mod(
                            FRAME_COUNT
                        )
            }

        val path =
            definition.assetRoot +
                definition.prefix +
                "_" +
                directionName +
                "_" +
                frameIndex +
                ".png"

        val texture =
            textures.getOrPut(
                path
            ) {
                Texture(
                    Gdx.files.internal(
                        path
                    )
                ).also {
                        loaded ->

                    loaded.setFilter(
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

        val drawX =
            player.centerX -
                worldWidth /
                2f

        /*
         * Mounted art replaces the whole character sprite.
         * Anchor to Player bounds bottom so the visual remains tied to the
         * same gameplay feet/contact point as the normal character.
         */
        val drawY =
            player.bounds.y +
                definition.bottomOffset

        batch.projectionMatrix =
            camera.combined

        batch.enableBlending()
        batch.begin()

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
            texture,
            drawX,
            drawY,
            worldWidth,
            worldHeight
        )

        batch.color =
            Color.WHITE

        batch.end()
    }

    private fun definitionFor(
        characterId: ContentId?
    ): MountedDefinition? {
        val id =
            characterId
                ?.value
                ?: return null

        return when (id) {
            "CHR_SIR_IMBERT" ->
                SIR_IMBERT

            "CHR_YWALETH_YOSTER" ->
                YWALETH_YOSTER

            "CHR_LAVENA_VOL" ->
                LAVENA_VOL

            else ->
                null
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

    companion object {
        private const val FRAME_COUNT =
            4

        private const val WALK_FRAME_DURATION_SECONDS =
            0.13f

        /*
         * Mounted sprites should visibly read as a horse + rider,
         * but visual scale MUST NOT affect Player combat bounds.
         */
        private val SIR_IMBERT =
            MountedDefinition(
                assetRoot =
                    "characters/mounted_phantom_steed/sir_imbert/",

                prefix =
                    "sir_imbert",

                worldHeight =
                    86f,

                bottomOffset =
                    -4f
            )

        private val YWALETH_YOSTER =
            MountedDefinition(
                assetRoot =
                    "characters/mounted_phantom_steed/ywaleth_yoster/",

                prefix =
                    "ywaleth_yoster",

                worldHeight =
                    84f,

                bottomOffset =
                    -4f
            )

        private val LAVENA_VOL =
            MountedDefinition(
                assetRoot =
                    "characters/mounted_phantom_steed/lavena_vol/",

                prefix =
                    "lavena_vol",

                worldHeight =
                    86f,

                bottomOffset =
                    -4f
            )
    }
}
