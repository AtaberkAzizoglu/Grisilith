package com.azizoglu.grisilith.weapon.skill

import com.azizoglu.grisilith.player.Player
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import kotlin.math.sin

/*
 * Rendered from WorldRenderer's renderBehindEntities callback.
 * Therefore the glow is naturally behind the character.
 */
class DefensiveLineVisualRenderer {

    private var animationTime =
        0f

    fun render(
        shapeRenderer: ShapeRenderer,
        player: Player,
        bonusArmorClass: Int
    ) {
        if (
            bonusArmorClass <=
            0
        ) {
            animationTime =
                0f

            return
        }

        animationTime +=
            Gdx.graphics.deltaTime
                .coerceAtMost(
                    0.05f
                )

        val texture =
            if (
                bonusArmorClass >
                5
            ) {
                highTexture
            } else {
                lowTexture
            }

        val pulse =
            0.94f +
                sin(
                    animationTime *
                        4.5f
                ) *
                0.06f

        val size =
            BASE_VISUAL_SIZE *
                pulse

        val drawX =
            player.centerX -
                size /
                2f

        val drawY =
            player.centerY -
                size /
                2f

        val projection =
            shapeRenderer
                .projectionMatrix

        /*
         * renderBehindEntities is in a Filled ShapeRenderer pass.
         * Pause it only long enough to draw the texture, then restore it.
         */
        shapeRenderer.end()

        batch.projectionMatrix =
            projection

        batch.begin()
        batch.setColor(
            1f,
            1f,
            1f,
            0.82f
        )

        batch.draw(
            texture,
            drawX,
            drawY,
            size,
            size
        )

        batch.setColor(
            1f,
            1f,
            1f,
            1f
        )
        batch.end()

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )
    }

    companion object {
        private const val BASE_VISUAL_SIZE =
            72f

        private val batch by lazy {
            SpriteBatch()
        }

        private val lowTexture by lazy {
            Texture(
                Gdx.files.internal(
                    "weapon_skills/defensive_line/defensive_line_low.png"
                )
            ).also {
                    texture ->

                texture.setFilter(
                    Texture.TextureFilter.Linear,
                    Texture.TextureFilter.Linear
                )
            }
        }

        private val highTexture by lazy {
            Texture(
                Gdx.files.internal(
                    "weapon_skills/defensive_line/defensive_line_high.png"
                )
            ).also {
                    texture ->

                texture.setFilter(
                    Texture.TextureFilter.Linear,
                    Texture.TextureFilter.Linear
                )
            }
        }
    }
}
