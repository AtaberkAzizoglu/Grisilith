package com.azizoglu.grisilith.enemy.visual

import com.azizoglu.grisilith.enemy.Enemy
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import kotlin.math.floor
import kotlin.math.max

class EnemySpriteRenderer {

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
        enemies: List<Enemy>,
        camera: OrthographicCamera
    ) {
        if (
            enemies.isEmpty()
        ) {
            return
        }

        batch.projectionMatrix =
            camera.combined

        batch.enableBlending()
        batch.begin()

        enemies.forEach {
                enemy ->

            renderEnemy(
                enemy
            )
        }

        batch.color =
            Color.WHITE

        batch.end()
    }

    fun hasVisualDefinition(
        enemy: Enemy
    ): Boolean {
        return EnemySpriteSheetRegistry
            .contains(
                enemy.kindId
            )
    }

    private fun renderEnemy(
        enemy: Enemy
    ) {
        val definition =
            EnemySpriteSheetRegistry
                .find(
                    enemy.kindId
                )
                ?: return

        if (
            enemy.isDead &&
            enemy.deathFadeAlpha <=
            0f
        ) {
            return
        }

        val visualState =
            enemy.currentVisualState

        val animation =
            definition.animationFor(
                visualState.direction
            )

        val frame =
            when (
                visualState.animation
            ) {
                EnemyVisualAnimation.IDLE ->
                    animation.idleFrame

                EnemyVisualAnimation.WALK ->
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
         * Visual size is independent from the enemy collision hitbox.
         * Our beast sheets already use the intended in-game canvas sizes
         * (32 / 48 / 72 / 108), so the frame's largest source side is the
         * correct world-space visual size.
         */
        /*
         * Kaynak frame'lerin aspect ratio'sunu koruyoruz.
         * Böylece Crocodile gibi uzun veya Bald Eagle gibi geniş
         * sprite'lar kareye zorlanıp bozulmuyor.
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
            1f

        val drawWidth =
            frame.width *
                scale

        val drawHeight =
            frame.height *
                scale

        val drawX =
            enemy.centerX -
                drawWidth /
                2f

        val drawY =
            enemy.centerY -
                drawHeight /
                2f

        batch.color =
            Color(
                1f,
                1f,
                1f,
                enemy.deathFadeAlpha
            )

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
        EnemySpriteAnimationDefinition,

        animationTime:
        Float
    ): EnemySpriteFrame {

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
        EnemySpriteSheetDefinition
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
        frame: EnemySpriteFrame
    ): TextureRegion {

        val key =
            RegionKey(
                texture = texture,
                x = frame.x,
                y = frame.y,
                width = frame.width,
                height = frame.height
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
