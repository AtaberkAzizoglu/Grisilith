package com.azizoglu.grisilith.enemy.visual

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.enemy.trait.EnemyTraitSpecialVisualKind
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

        val specialVisual = enemy.traitSpecialVisual
        val spearmanThrow = specialVisual
            ?.takeIf { it.kind == EnemyTraitSpecialVisualKind.SPEARMAN_THROW }

        val frame: EnemySpriteFrame
        val texture: Texture
        val flipX: Boolean
        if (spearmanThrow != null) {
            val row = visualState.direction.ordinal
            /*
             * The charge owns the first three clean wind-up frames.
             * The projectile itself is rendered separately at release, so
             * airborne-spear/recovery cells cannot leak into adjacent cells.
             */
            val column = (spearmanThrow.progress * 3f).toInt().coerceIn(0, 2)
            frame = EnemySpriteFrame(
                x = SPEARMAN_THROW_X[column],
                y = SPEARMAN_THROW_Y[row],
                width = SPEARMAN_THROW_X[column + 1] - SPEARMAN_THROW_X[column],
                height = SPEARMAN_THROW_Y[row + 1] - SPEARMAN_THROW_Y[row]
            )
            texture = textureForPath(SPEARMAN_THROW_PATH, 1402, 1122)
            flipX = false
        } else {
            val animation = definition.animationFor(visualState.direction)
            frame = when (visualState.animation) {
                EnemyVisualAnimation.IDLE -> animation.idleFrame
                EnemyVisualAnimation.WALK -> selectWalkFrame(
                    animation = animation,
                    animationTime = visualState.currentAnimationTime()
                )
            }
            texture = animation.texturePath?.let { path ->
                textureForPath(
                    path,
                    checkNotNull(animation.sourceWidth),
                    checkNotNull(animation.sourceHeight)
                )
            } ?: textureFor(definition)
            flipX = animation.flipX
        }

        val region =
            regionFor(
                texture =
                    texture,

                frame =
                    frame,

                flipX =
                    flipX
            )

        /*
         * Boss scale affects rendering only.
         * Collision and projectile hitboxes remain unchanged.
         */
        val visualScale =
            when (
                enemy.type
            ) {
                EnemyType.MINI_BOSS,
                EnemyType.FINAL_BOSS ->
                    1.20f

                else ->
                    1f
            }

        val worldSize =
            enemy.visualSize
                .coerceAtLeast(
                    1f
                ) *
                visualScale

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
            worldSize /
                sourceLargestSide

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

        specialVisual
            ?.takeIf { it.kind == EnemyTraitSpecialVisualKind.HORSEMAN_SWIPE }
            ?.let { swipe -> renderHorsemanSwipe(enemy, swipe.progress, swipe.scale) }
    }

    private fun renderHorsemanSwipe(
        enemy: Enemy,
        progress: Float,
        scale: Float
    ) {
        val frameIndex = (progress * 8f).toInt().coerceIn(0, 7)
        val column = frameIndex % 4
        val row = frameIndex / 4
        val frame = EnemySpriteFrame(
            x = HORSEMAN_SWIPE_X[column],
            y = HORSEMAN_SWIPE_Y[row],
            width = HORSEMAN_SWIPE_X[column + 1] - HORSEMAN_SWIPE_X[column],
            height = HORSEMAN_SWIPE_Y[row + 1] - HORSEMAN_SWIPE_Y[row]
        )
        val texture = textureForPath(HORSEMAN_SWIPE_PATH, 1774, 887)
        val region = regionFor(texture, frame)
        val size = 620f * scale
        batch.color = Color(1f, 1f, 1f, enemy.deathFadeAlpha)
        batch.draw(
            region,
            enemy.centerX - size / 2f,
            enemy.centerY - size / 2f,
            size,
            size
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

        return textureForPath(
            definition.texturePath,
            definition.sourceWidth,
            definition.sourceHeight
        )
    }

    private fun textureForPath(
        path: String,
        expectedWidth: Int,
        expectedHeight: Int
    ): Texture {
        return textures
            .getOrPut(
                path
            ) {
                val texture =
                    Texture(
                        Gdx.files.internal(
                            path
                        )
                    )

                texture.setFilter(
                    Texture.TextureFilter.Nearest,
                    Texture.TextureFilter.Nearest
                )

                require(
                    texture.width ==
                        expectedWidth &&
                        texture.height ==
                        expectedHeight
                ) {
                    "Unexpected texture size for $path: " +
                        "${texture.width}x${texture.height}; expected " +
                        "${expectedWidth}x${expectedHeight}"
                }

                texture
            }
    }

    private fun regionFor(
        texture: Texture,
        frame: EnemySpriteFrame,
        flipX: Boolean = false
    ): TextureRegion {

        val key =
            RegionKey(
                texture = texture,
                x = frame.x,
                y = frame.y,
                width = frame.width,
                height = frame.height,
                flipX = flipX
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
                ).also { region ->
                    if (flipX) {
                        region.flip(true, false)
                    }
                }
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
        val height: Int,
        val flipX: Boolean
    )

    companion object {
        private const val SPEARMAN_THROW_PATH = "enemies/humanoid/spearman_throw.png"
        private val SPEARMAN_THROW_X = intArrayOf(0, 280, 561, 841, 1122, 1402)
        private val SPEARMAN_THROW_Y = intArrayOf(0, 280, 561, 842, 1122)

        private const val HORSEMAN_SWIPE_PATH = "enemy_effects/horseman_swipe.png"
        private val HORSEMAN_SWIPE_X = intArrayOf(0, 444, 887, 1330, 1774)
        private val HORSEMAN_SWIPE_Y = intArrayOf(0, 444, 887)
    }
}
