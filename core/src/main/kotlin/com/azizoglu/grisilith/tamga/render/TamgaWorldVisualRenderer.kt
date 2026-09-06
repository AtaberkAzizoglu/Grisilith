package com.azizoglu.grisilith.tamga.render

import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.tamga.runtime.ArcaneAuraMode
import com.azizoglu.grisilith.tamga.runtime.ChargerFootstepVisual
import com.azizoglu.grisilith.tamga.runtime.FootstepDirection
import com.azizoglu.grisilith.tamga.runtime.HolyLightsMode
import com.azizoglu.grisilith.tamga.runtime.PanicPulseVisual
import com.azizoglu.grisilith.tamga.runtime.active.ChargerExplosionEffect
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion

class TamgaWorldVisualRenderer {

    private val batch = SpriteBatch()
    private val textures = mutableMapOf<String, Texture>()

    fun render(
        camera: OrthographicCamera,
        player: Player,
        animationSeconds: Float,
        holyLightsMode: HolyLightsMode?,
        holyLightsRange: Float,
        arcaneAuraMode: ArcaneAuraMode?,
        arcaneAuraRange: Float,
        footsteps: List<ChargerFootstepVisual>,
        chargerExplosions: List<ChargerExplosionEffect>,
        panicPulses: List<PanicPulseVisual>
    ) {
        batch.projectionMatrix = camera.combined
        batch.enableBlending()
        batch.begin()

        footsteps.forEach(::drawFootstep)

        holyLightsMode?.let { mode ->
            val row = when (mode) {
                HolyLightsMode.WISDOM -> 0
                HolyLightsMode.ARMOR_CLASS -> 1
                HolyLightsMode.LIFE_STEAL -> 2
            }
            drawAnimatedGridFrame(
                path = "tamga_visuals/holy_lights_sheet.png",
                columns = 4,
                rows = 3,
                frame = (animationSeconds / 0.18f).toInt().mod(4),
                row = row,
                centerX = player.centerX,
                centerY = player.centerY + 3f,
                width = holyLightsRange * 2f,
                height = holyLightsRange * 1.42f,
                alpha = 0.82f
            )
        }

        arcaneAuraMode?.let { mode ->
            val row = when (mode) {
                ArcaneAuraMode.ELEMENTAL_DAMAGE -> 0
                ArcaneAuraMode.SHEPHERD -> 1
                ArcaneAuraMode.MIGHT_OF_NATURE -> 2
            }
            drawAnimatedGridFrame(
                path = "tamga_visuals/arcane_aura_sheet.png",
                columns = 4,
                rows = 3,
                frame = (animationSeconds / 0.18f).toInt().mod(4),
                row = row,
                centerX = player.centerX,
                centerY = player.centerY + 3f,
                width = arcaneAuraRange * 2f,
                height = arcaneAuraRange * 1.42f,
                alpha = 0.82f
            )
        }

        chargerExplosions.forEach { effect ->
            val frame =
                (effect.progress * CHARGER_EXPLOSION_FRAMES.size)
                    .toInt()
                    .coerceIn(
                        0,
                        CHARGER_EXPLOSION_FRAMES.lastIndex
                    )

            drawPackedChargerExplosionFrame(
                effect = effect,
                frame = CHARGER_EXPLOSION_FRAMES[frame]
            )
        }

        panicPulses.forEach { pulse ->
            val frame =
                (pulse.progress * 12f)
                    .toInt()
                    .coerceIn(0, 11)
            drawAnimatedGridFrame(
                path = "tamga_visuals/panic_pulse_sheet.png",
                columns = 6,
                rows = 2,
                frame = frame.mod(6),
                row = frame / 6,
                centerX = pulse.x,
                centerY = pulse.y,
                width = PANIC_VISUAL_SIZE,
                height = PANIC_VISUAL_SIZE,
                alpha = 1f
            )
        }

        batch.color = Color.WHITE
        batch.end()
    }

    private fun drawFootstep(footstep: ChargerFootstepVisual) {
        val texture = texture("tamga_visuals/charger_footsteps_sheet.png")
        val frame =
            (footstep.progress * 4f)
                .toInt()
                .coerceIn(0, 3)
        val (row, rightGroup) = when (footstep.direction) {
            FootstepDirection.NORTH -> 0 to false
            FootstepDirection.NORTH_EAST -> 0 to true
            FootstepDirection.EAST -> 1 to false
            FootstepDirection.SOUTH_EAST -> 1 to true
            FootstepDirection.SOUTH -> 2 to false
            FootstepDirection.SOUTH_WEST -> 2 to true
            FootstepDirection.WEST -> 3 to false
            FootstepDirection.NORTH_WEST -> 3 to true
        }

        val halfWidth = texture.width / 2
        val rowHeight = texture.height / 4
        val frameXOffsets = intArrayOf(26, 171, 316, 461)
        val sourceX =
            (if (rightGroup) halfWidth else 0) +
                frameXOffsets[frame]
        val sourceY = row * rowHeight + FOOTSTEP_TOP_INSET
        val sourceWidth = FOOTSTEP_SOURCE_WIDTH
        val sourceHeight = rowHeight - FOOTSTEP_TOP_INSET - FOOTSTEP_BOTTOM_INSET
        val region =
            TextureRegion(
                texture,
                sourceX,
                sourceY,
                sourceWidth,
                sourceHeight
            )

        batch.setColor(
            1f,
            1f,
            1f,
            (1f - footstep.progress * 0.72f).coerceIn(0f, 1f)
        )
        batch.draw(
            region,
            footstep.x - FOOTSTEP_DRAW_WIDTH / 2f,
            footstep.y - FOOTSTEP_DRAW_HEIGHT / 2f,
            FOOTSTEP_DRAW_WIDTH,
            FOOTSTEP_DRAW_HEIGHT
        )
    }

    private fun drawAnimatedGridFrame(
        path: String,
        columns: Int,
        rows: Int,
        frame: Int,
        row: Int,
        centerX: Float,
        centerY: Float,
        width: Float,
        height: Float,
        alpha: Float
    ) {
        val texture = texture(path)
        val cellWidth = texture.width / columns
        val cellHeight = texture.height / rows
        val region =
            TextureRegion(
                texture,
                frame.coerceIn(0, columns - 1) * cellWidth,
                row.coerceIn(0, rows - 1) * cellHeight,
                cellWidth,
                cellHeight
            )

        batch.setColor(1f, 1f, 1f, alpha.coerceIn(0f, 1f))
        batch.draw(
            region,
            centerX - width / 2f,
            centerY - height / 2f,
            width,
            height
        )
    }

    /*
     * Charger artwork is a tightly packed 11-frame sequence rather than a
     * regular 6 x 2 grid. The growing frames use progressively wider source
     * areas, then the second row contracts again. Keeping those source areas
     * and drawing them at one shared pixel scale preserves the intended
     * expansion while preventing neighbouring frames from being sliced into
     * one another.
     */
    private fun drawPackedChargerExplosionFrame(
        effect: ChargerExplosionEffect,
        frame: PackedSpriteFrame
    ) {
        val texture =
            texture(
                "tamga_visuals/charger_explosion_sheet.png"
            )

        val region =
            TextureRegion(
                texture,
                frame.x,
                frame.y,
                frame.width,
                frame.height
            )

        val diameter =
            effect.radius * 2f

        val drawWidth =
            diameter *
                frame.width /
                CHARGER_EXPLOSION_REFERENCE_SIZE

        val drawHeight =
            diameter *
                frame.height /
                CHARGER_EXPLOSION_REFERENCE_SIZE

        batch.setColor(
            1f,
            1f,
            1f,
            1f
        )

        batch.draw(
            region,
            effect.centerX - drawWidth / 2f,
            effect.centerY - drawHeight / 2f,
            drawWidth,
            drawHeight
        )
    }

    private fun texture(path: String): Texture =
        textures.getOrPut(path) {
            Texture(Gdx.files.internal(path)).also { texture ->
                texture.setFilter(
                    Texture.TextureFilter.Linear,
                    Texture.TextureFilter.Linear
                )
            }
        }

    fun dispose() {
        textures.values.forEach(Texture::dispose)
        textures.clear()
        batch.dispose()
    }

    private companion object {
        data class PackedSpriteFrame(
            val x: Int,
            val y: Int,
            val width: Int,
            val height: Int
        )

        /*
         * Boundaries sit in the transparent gaps between the irregularly
         * packed artwork: six expanding frames above, five contracting
         * frames below.
         */
        val CHARGER_EXPLOSION_FRAMES =
            arrayOf(
                PackedSpriteFrame(0, 0, 287, 362),
                PackedSpriteFrame(287, 0, 286, 362),
                PackedSpriteFrame(573, 0, 349, 362),
                PackedSpriteFrame(922, 0, 376, 362),
                PackedSpriteFrame(1298, 0, 399, 362),
                PackedSpriteFrame(1697, 0, 475, 362),
                PackedSpriteFrame(0, 362, 481, 362),
                PackedSpriteFrame(481, 362, 467, 362),
                PackedSpriteFrame(948, 362, 422, 362),
                PackedSpriteFrame(1370, 362, 442, 362),
                PackedSpriteFrame(1812, 362, 360, 362)
            )

        const val CHARGER_EXPLOSION_REFERENCE_SIZE =
            481f

        const val FOOTSTEP_SOURCE_WIDTH = 150
        const val FOOTSTEP_TOP_INSET = 85
        const val FOOTSTEP_BOTTOM_INSET = 53
        const val FOOTSTEP_DRAW_WIDTH = 42f
        const val FOOTSTEP_DRAW_HEIGHT = 52f
        const val PANIC_VISUAL_SIZE = 440f
    }
}
