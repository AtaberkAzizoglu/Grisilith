package com.azizoglu.grisilith.weapon.visual.attack

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class MeleeAttackVisualRenderer {

    private val batch =
        SpriteBatch()

    private val textures =
        mutableMapOf<String, Texture>()

    fun render(
        camera: OrthographicCamera
    ) {
        val events =
            MeleeAttackVisualManager
                .activeEvents

        if (
            events.isEmpty()
        ) {
            return
        }

        batch.projectionMatrix =
            camera.combined

        batch.begin()

        events.forEach {
                event ->

            val path =
                framePath(
                    event.kind,
                    event.progress
                )

            val texture =
                texture(
                    path
                )

            val alpha =
                fadeAlpha(
                    event.progress
                )

            batch.setColor(
                1f,
                1f,
                1f,
                alpha
            )

            val width =
                event.worldWidth

            val height =
                event.worldHeight

            val sourceFrameCount =
                if (event.kind == MeleeAttackVisualKind.FIRE_BEAM) 4 else 1

            val sourceFrameIndex =
                (event.progress * sourceFrameCount)
                    .toInt()
                    .coerceIn(0, sourceFrameCount - 1)

            val sourceWidth =
                texture.width / sourceFrameCount

            batch.draw(
                texture,
                event.centerX -
                    width /
                    2f,
                event.centerY -
                    height /
                    2f,
                width /
                    2f,
                height /
                    2f,
                width,
                height,
                1f,
                1f,
                event.rotationDegrees,
                sourceFrameIndex * sourceWidth,
                0,
                sourceWidth,
                texture.height,
                false,
                false
            )
        }

        batch.setColor(
            1f,
            1f,
            1f,
            1f
        )

        batch.end()
    }

    private fun framePath(
        kind: MeleeAttackVisualKind,
        progress: Float
    ): String {
        val frames =
            when (kind) {
                MeleeAttackVisualKind.DOUBLE_SLASH ->
                    DOUBLE_SLASH_FRAMES

                MeleeAttackVisualKind.DOUBLE_SLASH_X ->
                    DOUBLE_X_FRAMES

                MeleeAttackVisualKind.RAPID_STROKES_UPGRADED ->
                    RAPID_STROKES_FRAMES

                MeleeAttackVisualKind.FIRE_BEAM ->
                    FIRE_BEAM_FRAMES

                MeleeAttackVisualKind.LONGSWORD_CONE ->
                    LONGSWORD_FRAMES

                MeleeAttackVisualKind.LANCE_THRUST ->
                    LANCE_FRAMES

                MeleeAttackVisualKind.LANCE_SPIN ->
                    LANCE_SPIN_FRAMES

                MeleeAttackVisualKind.SHIELD_CONE ->
                    SHIELD_FRAMES
            }

        val index =
            (
                progress *
                    frames.size
                )
                .toInt()
                .coerceIn(
                    0,
                    frames.lastIndex
                )

        return frames[index]
    }

    private fun fadeAlpha(
        progress: Float
    ): Float {
        /*
         * User-requested small fade-in / fade-out.
         * Most of the lifetime remains fully opaque.
         */
        return when {
            progress <
                0.12f -> {
                (
                    progress /
                        0.12f
                    )
                    .coerceIn(
                        0f,
                        1f
                    )
            }

            progress >
                0.82f -> {
                (
                    (1f - progress) /
                        0.18f
                    )
                    .coerceIn(
                        0f,
                        1f
                    )
            }

            else ->
                1f
        }
    }

    private fun texture(
        path: String
    ): Texture {
        return textures.getOrPut(
            path
        ) {
            Texture(
                Gdx.files.internal(
                    path
                )
            ).also {
                it.setFilter(
                    Texture.TextureFilter.Linear,
                    Texture.TextureFilter.Linear
                )
            }
        }
    }

    fun dispose() {
        textures.values.forEach {
            it.dispose()
        }

        textures.clear()
        batch.dispose()
    }

    companion object {
        private const val ROOT =
            "weapon_visuals/melee_v3/"

        private val DOUBLE_SLASH_FRAMES =
            arrayOf(
                ROOT + "double_slash_0.png",
                ROOT + "double_slash_1.png",
                ROOT + "double_slash_2.png",
                ROOT + "double_slash_3.png",
                ROOT + "double_slash_4.png",
                ROOT + "double_slash_5.png",
                ROOT + "double_slash_6.png",
                ROOT + "double_slash_7.png"
            )

        private val DOUBLE_X_FRAMES =
            arrayOf(
                ROOT + "double_x_0.png",
                ROOT + "double_x_1.png",
                ROOT + "double_x_2.png",
                ROOT + "double_x_3.png",
                ROOT + "double_x_4.png",
                ROOT + "double_x_5.png",
                ROOT + "double_x_6.png",
                ROOT + "double_x_7.png"
            )

        private val RAPID_STROKES_FRAMES =
            arrayOf(
                "weapons_animations/weapon_skills/rapid_strokes/rapid_strokes_0.png",
                "weapons_animations/weapon_skills/rapid_strokes/rapid_strokes_1.png",
                "weapons_animations/weapon_skills/rapid_strokes/rapid_strokes_2.png",
                "weapons_animations/weapon_skills/rapid_strokes/rapid_strokes_3.png",
                "weapons_animations/weapon_skills/rapid_strokes/rapid_strokes_4.png",
                "weapons_animations/weapon_skills/rapid_strokes/rapid_strokes_5.png",
                "weapons_animations/weapon_skills/rapid_strokes/rapid_strokes_6.png",
                "weapons_animations/weapon_skills/rapid_strokes/rapid_strokes_7.png"
            )

        private val FIRE_BEAM_FRAMES =
            arrayOf(
                "weapons_animations/weapon_skills/fire_beam/fire_beam.png"
            )

        private val LONGSWORD_FRAMES =
            arrayOf(
                ROOT + "longsword_0.png",
                ROOT + "longsword_1.png",
                ROOT + "longsword_2.png",
                ROOT + "longsword_3.png",
                ROOT + "longsword_4.png",
                ROOT + "longsword_5.png",
                ROOT + "longsword_6.png",
                ROOT + "longsword_7.png"
            )

        private val LANCE_FRAMES =
            arrayOf(
                ROOT + "lance_0.png",
                ROOT + "lance_1.png",
                ROOT + "lance_2.png",
                ROOT + "lance_3.png",
                ROOT + "lance_4.png",
                ROOT + "lance_5.png",
                ROOT + "lance_6.png",
                ROOT + "lance_7.png"
            )

        private val SHIELD_FRAMES =
            arrayOf(
                ROOT + "shield_0.png",
                ROOT + "shield_1.png",
                ROOT + "shield_2.png",
                ROOT + "shield_3.png",
                ROOT + "shield_4.png",
                ROOT + "shield_5.png",
                ROOT + "shield_6.png",
                ROOT + "shield_7.png"
            )

        private val LANCE_SPIN_FRAMES =
            (0 until 16).map { "weapon_visuals/lance_spin_v4/frame_$it.png" }.toTypedArray()
    }
}
