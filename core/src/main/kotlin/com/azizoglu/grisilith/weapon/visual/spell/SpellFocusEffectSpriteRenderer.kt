package com.azizoglu.grisilith.weapon.visual.spell

import com.azizoglu.grisilith.weapon.skill.ErasingSoulsRuntime
import com.azizoglu.grisilith.weapon.skill.IgniteDashRuntime
import com.azizoglu.grisilith.weapon.skill.StaticCloudsRuntime
import com.azizoglu.grisilith.weapon.skill.VortexPullRuntime
import com.azizoglu.grisilith.weapon.skill.DefaultWeaponAbilities
import com.azizoglu.grisilith.weapon.skill.AcidicPoolRuntime
import com.azizoglu.grisilith.weapon.glyph.GlyphSpellRuntime
import com.azizoglu.grisilith.weapon.glyph.GlyphType
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Matrix4
import kotlin.math.floor

class SpellFocusEffectSpriteRenderer {

    private val batch =
        SpriteBatch()

    private val textures =
        mutableMapOf<String, Texture>()

    fun hasAnythingToRender(
        staticCloudsRuntime: StaticCloudsRuntime,
        igniteDashRuntime: IgniteDashRuntime,
        erasingSoulsRuntime: ErasingSoulsRuntime,
        vortexPullRuntime: VortexPullRuntime,
        acidicPoolRuntime: AcidicPoolRuntime,
        glyphSpellRuntime: GlyphSpellRuntime
    ): Boolean {
        return staticCloudsRuntime.isActive ||
            igniteDashRuntime.isAiming ||
            igniteDashRuntime.hasExplosionVisual ||
            erasingSoulsRuntime.visualSnapshots().isNotEmpty() ||
            vortexPullRuntime.isActive ||
            acidicPoolRuntime.isActive ||
            glyphSpellRuntime.pulseVisualSnapshots().isNotEmpty() ||
            glyphSpellRuntime.tsunamiVisualSnapshot() != null ||
            glyphSpellRuntime.singularityVisualSnapshot() != null ||
            glyphSpellRuntime.singularityExplosionVisualSnapshot() != null
    }

    fun render(
        projectionMatrix: Matrix4,
        staticCloudsRuntime: StaticCloudsRuntime,
        igniteDashRuntime: IgniteDashRuntime,
        erasingSoulsRuntime: ErasingSoulsRuntime,
        vortexPullRuntime: VortexPullRuntime,
        acidicPoolRuntime: AcidicPoolRuntime,
        glyphSpellRuntime: GlyphSpellRuntime
    ) {
        if (
            !hasAnythingToRender(
                staticCloudsRuntime,
                igniteDashRuntime,
                erasingSoulsRuntime,
                vortexPullRuntime,
                acidicPoolRuntime,
                glyphSpellRuntime
            )
        ) {
            return
        }

        batch.projectionMatrix =
            projectionMatrix

        batch.begin()

        renderStaticClouds(
            staticCloudsRuntime
        )

        renderVortex(
            vortexPullRuntime
        )

        renderIgniteDashAim(
            igniteDashRuntime
        )

        renderIgniteDash(
            igniteDashRuntime
        )

        renderErasingSouls(
            erasingSoulsRuntime
        )

        renderAcidicPool(
            acidicPoolRuntime
        )

        renderGlyphSpells(
            glyphSpellRuntime
        )

        batch.color =
            Color.WHITE

        batch.end()
    }

    private fun renderStaticClouds(
        runtime: StaticCloudsRuntime
    ) {
        if (!runtime.isActive) {
            return
        }

        val frame =
            loopFrame(
                runtime.visualAnimationSeconds,
                STATIC_CLOUD_FRAME_SECONDS,
                STATIC_CLOUD_FRAME_COUNT
            )

        drawCentered(
            path =
                framePath(
                    STATIC_CLOUD_PREFIX,
                    frame
                ),

            centerX =
                runtime.visualCenterX,

            centerY =
                runtime.visualCenterY,

            worldWidth =
                runtime.visualRadius *
                    STATIC_CLOUD_DIAMETER_MULTIPLIER,

            worldHeight =
                runtime.visualRadius *
                    STATIC_CLOUD_DIAMETER_MULTIPLIER,

            alpha =
                0.92f
        )
    }

    private fun renderVortex(
        runtime: VortexPullRuntime
    ) {
        val field =
            runtime.field
                ?: return

        val plague =
            runtime.isPlagueStormActive

        val frameCount =
            if (plague) {
                PLAGUE_FRAME_COUNT
            } else {
                VORTEX_FRAME_COUNT
            }

        val frameSeconds =
            if (plague) {
                PLAGUE_FRAME_SECONDS
            } else {
                VORTEX_FRAME_SECONDS
            }

        val prefix =
            if (plague) {
                PLAGUE_PREFIX
            } else {
                VORTEX_PREFIX
            }

        val frame =
            loopFrame(
                runtime.visualAnimationSeconds,
                frameSeconds,
                frameCount
            )

        drawCentered(
            path =
                framePath(
                    prefix,
                    frame
                ),

            centerX =
                field.center.x,

            centerY =
                field.center.y,

            worldWidth =
                field.radius *
                    VORTEX_DIAMETER_MULTIPLIER,

            worldHeight =
                field.radius *
                    VORTEX_DIAMETER_MULTIPLIER,

            alpha =
                if (plague) {
                    0.94f
                } else {
                    0.90f
                }
        )
    }

    private fun renderIgniteDashAim(
        runtime: IgniteDashRuntime
    ) {
        if (!runtime.isAiming) {
            return
        }

        val diameter =
            DefaultWeaponAbilities
                .IGNITE_DASH_OUTER_EXPLOSION_RADIUS *
                2f

        drawCentered(
            path =
                IGNITE_AIM_MARKER_PATH,

            centerX =
                runtime.aimVisualCenterX,

            centerY =
                runtime.aimVisualCenterY,

            worldWidth =
                diameter,

            worldHeight =
                diameter,

            alpha =
                0.36f
        )

        /*
         * Second supplied cast marker:
         * centered on the same destination but sized exactly to the
         * inner full-damage radius.
         */
        val innerDiameter =
            DefaultWeaponAbilities
                .IGNITE_DASH_EXPLOSION_RADIUS *
                2f

        drawCentered(
            path =
                IGNITE_AIM_INNER_MARKER_PATH,

            centerX =
                runtime.aimVisualCenterX,

            centerY =
                runtime.aimVisualCenterY,

            worldWidth =
                innerDiameter,

            worldHeight =
                innerDiameter,

            alpha =
                0.56f
        )
    }

    private fun renderIgniteDash(
        runtime: IgniteDashRuntime
    ) {
        if (!runtime.hasExplosionVisual) {
            return
        }

        val frame =
            oneShotFrame(
                runtime.explosionVisualProgress,
                IGNITE_FRAME_COUNT
            )

        /*
         * The supplied explosion sheet represents the complete hit footprint.
         * Draw to the outer 250 px radius, while gameplay still resolves
         * separate 120 px inner and 250 px outer damage zones.
         */
        val diameter =
            DefaultWeaponAbilities
                .IGNITE_DASH_OUTER_EXPLOSION_RADIUS *
                2f

        drawCentered(
            path =
                framePath(
                    IGNITE_PREFIX,
                    frame
                ),

            centerX =
                runtime.explosionVisualCenterX,

            centerY =
                runtime.explosionVisualCenterY,

            worldWidth =
                diameter,

            worldHeight =
                diameter,

            alpha =
                0.96f
        )
    }

    private fun renderErasingSouls(
        runtime: ErasingSoulsRuntime
    ) {
        runtime
            .visualSnapshots()
            .forEach { visual ->

                val frame =
                    oneShotFrame(
                        visual.progress,
                        ERASING_FRAME_COUNT
                    )

                drawCentered(
                    path =
                        framePath(
                            ERASING_PREFIX,
                            frame
                        ),

                    centerX =
                        visual.centerX,

                    centerY =
                        visual.centerY,

                    worldWidth =
                        visual.radius *
                            2f,

                    worldHeight =
                        visual.radius *
                            2f,

                    alpha =
                        0.96f
                )
            }
    }

    private fun renderAcidicPool(
        runtime: AcidicPoolRuntime
    ) {
        if (!runtime.isActive) {
            return
        }

        val diameter =
            runtime.visualRadius *
                2.06f

        if (
            !runtime.isVitriolicIgnitionActive
        ) {
            val frame =
                loopFrame(
                    runtime.visualAnimationSeconds,
                    ACIDIC_POOL_FRAME_SECONDS,
                    ACIDIC_POOL_FRAME_COUNT
                )

            drawCentered(
                path =
                    framePath(
                        ACIDIC_POOL_PREFIX,
                        frame
                    ),

                centerX =
                    runtime.visualCenterX,

                centerY =
                    runtime.visualCenterY,

                worldWidth =
                    diameter,

                worldHeight =
                    diameter,

                alpha =
                    0.94f
            )

            return
        }

        if (
            runtime.isIgnitionBurstVisible
        ) {
            val ignitionFrame =
                oneShotFrame(
                    runtime.ignitionVisualProgress,
                    VITRIOLIC_BURST_FRAME_COUNT
                )

            drawCentered(
                path =
                    framePath(
                        VITRIOLIC_BURST_PREFIX,
                        ignitionFrame
                    ),

                centerX =
                    runtime.visualCenterX,

                centerY =
                    runtime.visualCenterY,

                worldWidth =
                    diameter *
                        1.08f,

                worldHeight =
                    diameter *
                        1.08f,

                alpha =
                    0.98f
            )

            val explosionFrame =
                oneShotFrame(
                    runtime.ignitionVisualProgress,
                    POOL_EXPLOSION_FRAME_COUNT
                )

            drawCentered(
                path =
                    framePath(
                        POOL_EXPLOSION_PREFIX,
                        explosionFrame
                    ),

                centerX =
                    runtime.visualCenterX,

                centerY =
                    runtime.visualCenterY,

                worldWidth =
                    diameter *
                        1.75f,

                worldHeight =
                    diameter *
                        1.75f,

                alpha =
                    0.98f
            )
        } else {
            val frame =
                loopFrame(
                    runtime.visualAnimationSeconds,
                    VITRIOLIC_LOOP_FRAME_SECONDS,
                    VITRIOLIC_LOOP_FRAME_COUNT
                )

            drawCentered(
                path =
                    framePath(
                        VITRIOLIC_LOOP_PREFIX,
                        frame
                    ),

                centerX =
                    runtime.visualCenterX,

                centerY =
                    runtime.visualCenterY,

                worldWidth =
                    diameter,

                worldHeight =
                    diameter,

                alpha =
                    0.95f
            )
        }
    }

    private fun renderGlyphSpells(
        runtime: GlyphSpellRuntime
    ) {
        runtime
            .pulseVisualSnapshots()
            .forEach { pulse ->

                when (
                    pulse.type
                ) {
                    GlyphType.CALL_LIGHTNING -> {
                        val frame =
                            oneShotFrame(
                                pulse.progress,
                                CALL_LIGHTNING_FRAME_COUNT
                            )

                        drawCentered(
                            path =
                                framePath(
                                    CALL_LIGHTNING_PREFIX,
                                    frame
                                ),

                            centerX =
                                pulse.centerX,

                            centerY =
                                pulse.centerY,

                            worldWidth =
                                pulse.radius *
                                    2f,

                            worldHeight =
                                pulse.radius *
                                    2f,

                            alpha =
                                0.98f
                        )
                    }

                    GlyphType.METEOR -> {
                        val frame =
                            oneShotFrame(
                                pulse.progress,
                                METEOR_FRAME_COUNT
                            )

                        drawCentered(
                            path =
                                framePath(
                                    METEOR_PREFIX,
                                    frame
                                ),

                            centerX =
                                pulse.centerX,

                            centerY =
                                pulse.centerY,

                            worldWidth =
                                pulse.radius *
                                    2.15f,

                            worldHeight =
                                pulse.radius *
                                    2.15f,

                            alpha =
                                0.98f
                        )
                    }

                    else -> Unit
                }
            }

        runtime
            .tsunamiVisualSnapshot()
            ?.let { wave ->

                val frame =
                    loopFrame(
                        wave.animationSeconds,
                        TSUNAMI_FRAME_SECONDS,
                        TSUNAMI_FRAME_COUNT
                    )

                drawCenteredRotated(
                    path =
                        framePath(
                            TSUNAMI_PREFIX,
                            frame
                        ),

                    centerX =
                        wave.centerX,

                    centerY =
                        wave.centerY,

                    worldWidth =
                        wave.worldWidth,

                    worldHeight =
                        wave.worldHeight,

                    rotationDegrees =
                        wave.rotationDegrees,

                    alpha =
                        0.96f
                )
            }

        runtime
            .singularityVisualSnapshot()
            ?.let { singularity ->

                if (
                    singularity.openingProgress <
                    1f
                ) {
                    val frame =
                        oneShotFrame(
                            singularity.openingProgress,
                            SINGULARITY_OPEN_FRAME_COUNT
                        )

                    drawCentered(
                        path =
                            framePath(
                                SINGULARITY_OPEN_PREFIX,
                                frame
                            ),

                        centerX =
                            singularity.centerX,

                        centerY =
                            singularity.centerY,

                        worldWidth =
                            singularity.radius *
                                2f,

                        worldHeight =
                            singularity.radius *
                                2f,

                        alpha =
                            0.98f
                    )
                } else {
                    val frame =
                        loopFrame(
                            singularity.elapsedSeconds,
                            SINGULARITY_LOOP_FRAME_SECONDS,
                            SINGULARITY_LOOP_FRAME_COUNT
                        )

                    val collapseScale =
                        if (
                            singularity.isCollapsing
                        ) {
                            (
                                1f -
                                    singularity.collapseProgress *
                                    0.72f
                                )
                                .coerceAtLeast(
                                    0.20f
                                )
                        } else {
                            1f
                        }

                    drawCentered(
                        path =
                            framePath(
                                SINGULARITY_LOOP_PREFIX,
                                frame
                            ),

                        centerX =
                            singularity.centerX,

                        centerY =
                            singularity.centerY,

                        worldWidth =
                            singularity.radius *
                                2f *
                                collapseScale,

                        worldHeight =
                            singularity.radius *
                                2f *
                                collapseScale,

                        alpha =
                            0.97f
                    )
                }
            }

        runtime
            .singularityExplosionVisualSnapshot()
            ?.let { explosion ->

                val frame =
                    oneShotFrame(
                        explosion.progress,
                        SINGULARITY_EXPLOSION_FRAME_COUNT
                    )

                drawCentered(
                    path =
                        framePath(
                            SINGULARITY_EXPLOSION_PREFIX,
                            frame
                        ),

                    centerX =
                        explosion.centerX,

                    centerY =
                        explosion.centerY,

                    worldWidth =
                        explosion.radius *
                            2f,

                    worldHeight =
                        explosion.radius *
                            2f,

                    alpha =
                        0.98f
                )
            }
    }

    private fun drawCenteredRotated(
        path: String,
        centerX: Float,
        centerY: Float,
        worldWidth: Float,
        worldHeight: Float,
        rotationDegrees: Float,
        alpha: Float
    ) {
        val texture =
            textureFor(
                path
            )

        batch.color =
            Color(
                1f,
                1f,
                1f,
                alpha.coerceIn(
                    0f,
                    1f
                )
            )

        batch.draw(
            texture,
            centerX -
                worldWidth /
                2f,
            centerY -
                worldHeight /
                2f,
            worldWidth /
                2f,
            worldHeight /
                2f,
            worldWidth,
            worldHeight,
            1f,
            1f,
            rotationDegrees,
            0,
            0,
            texture.width,
            texture.height,
            false,
            false
        )
    }

    private fun drawCentered(
        path: String,
        centerX: Float,
        centerY: Float,
        worldWidth: Float,
        worldHeight: Float,
        alpha: Float
    ) {
        val texture =
            textureFor(
                path
            )

        val sourceWidth =
            texture.width
                .toFloat()
                .coerceAtLeast(
                    1f
                )

        val sourceHeight =
            texture.height
                .toFloat()
                .coerceAtLeast(
                    1f
                )

        val requestedRatio =
            (
                worldWidth /
                    worldHeight
                )
                .coerceAtLeast(
                    0.001f
                )

        val sourceRatio =
            sourceWidth /
                sourceHeight

        val drawWidth: Float
        val drawHeight: Float

        if (
            sourceRatio >
            requestedRatio
        ) {
            drawWidth =
                worldWidth

            drawHeight =
                worldWidth /
                    sourceRatio
        } else {
            drawHeight =
                worldHeight

            drawWidth =
                worldHeight *
                    sourceRatio
        }

        batch.color =
            Color(
                1f,
                1f,
                1f,
                alpha.coerceIn(
                    0f,
                    1f
                )
            )

        batch.draw(
            texture,
            centerX -
                drawWidth /
                2f,
            centerY -
                drawHeight /
                2f,
            drawWidth,
            drawHeight
        )
    }

    private fun textureFor(
        path: String
    ): Texture {

        return textures
            .getOrPut(
                path
            ) {
                Texture(
                    Gdx.files.internal(
                        path
                    )
                ).also { texture ->
                    texture.setFilter(
                        Texture.TextureFilter.Linear,
                        Texture.TextureFilter.Linear
                    )
                }
            }
    }

    private fun framePath(
        prefix: String,
        frameIndexZeroBased: Int
    ): String {

        return "weapons_animations/spell_effects/" +
            prefix +
            "_" +
            (frameIndexZeroBased + 1)
                .toString()
                .padStart(
                    2,
                    '0'
                ) +
            ".png"
    }

    private fun loopFrame(
        animationSeconds: Float,
        secondsPerFrame: Float,
        frameCount: Int
    ): Int {

        return (
            floor(
                animationSeconds /
                    secondsPerFrame
            )
                .toInt()
                .mod(
                    frameCount
                )
            )
    }

    private fun oneShotFrame(
        progress: Float,
        frameCount: Int
    ): Int {

        return (
            progress
                .coerceIn(
                    0f,
                    0.999999f
                ) *
                frameCount
            )
            .toInt()
            .coerceIn(
                0,
                frameCount -
                    1
            )
    }

    fun dispose() {
        textures
            .values
            .forEach {
                    texture ->
                texture.dispose()
            }

        textures.clear()
        batch.dispose()
    }

    companion object {
        private const val STATIC_CLOUD_PREFIX =
            "static_clouds"

        private const val IGNITE_PREFIX =
            "ignite_dash"

        private const val IGNITE_AIM_MARKER_PATH =
            "weapons_animations/spell_effects/ignite_dash_cast_area.png"

        private const val IGNITE_AIM_INNER_MARKER_PATH =
            "weapons_animations/spell_effects/ignite_dash_cast_inner.png"

        private const val ERASING_PREFIX =
            "erasing_souls"

        private const val VORTEX_PREFIX =
            "vortex_pull"

        private const val PLAGUE_PREFIX =
            "plague_storm"

        private const val ACIDIC_POOL_PREFIX =
            "acidic_pool"

        private const val VITRIOLIC_BURST_PREFIX =
            "vitriolic_burst"

        private const val VITRIOLIC_LOOP_PREFIX =
            "vitriolic_loop"

        private const val POOL_EXPLOSION_PREFIX =
            "pool_explosion"

        private const val TSUNAMI_PREFIX =
            "tsunami"

        private const val CALL_LIGHTNING_PREFIX =
            "call_lightning"

        private const val METEOR_PREFIX =
            "meteor"

        private const val SINGULARITY_OPEN_PREFIX =
            "singularity_open"

        private const val SINGULARITY_LOOP_PREFIX =
            "singularity_loop"

        private const val SINGULARITY_EXPLOSION_PREFIX =
            "singularity_explosion"

        private const val STATIC_CLOUD_FRAME_COUNT =
            6

        private const val IGNITE_FRAME_COUNT =
            6

        private const val ERASING_FRAME_COUNT =
            6

        private const val VORTEX_FRAME_COUNT =
            10

        private const val PLAGUE_FRAME_COUNT =
            10

        private const val STATIC_CLOUD_FRAME_SECONDS =
            0.16f

        private const val VORTEX_FRAME_SECONDS =
            0.12f

        private const val PLAGUE_FRAME_SECONDS =
            0.12f

        private const val STATIC_CLOUD_DIAMETER_MULTIPLIER =
            2.04f

        private const val VORTEX_DIAMETER_MULTIPLIER =
            2.04f

        private const val ACIDIC_POOL_FRAME_COUNT =
            6

        private const val VITRIOLIC_BURST_FRAME_COUNT =
            4

        private const val VITRIOLIC_LOOP_FRAME_COUNT =
            4

        private const val POOL_EXPLOSION_FRAME_COUNT =
            6

        private const val TSUNAMI_FRAME_COUNT =
            10

        private const val CALL_LIGHTNING_FRAME_COUNT =
            6

        private const val METEOR_FRAME_COUNT =
            10

        private const val SINGULARITY_OPEN_FRAME_COUNT =
            6

        private const val SINGULARITY_LOOP_FRAME_COUNT =
            6

        private const val SINGULARITY_EXPLOSION_FRAME_COUNT =
            8

        private const val ACIDIC_POOL_FRAME_SECONDS =
            0.18f

        private const val VITRIOLIC_LOOP_FRAME_SECONDS =
            0.17f

        private const val TSUNAMI_FRAME_SECONDS =
            0.11f

        private const val SINGULARITY_LOOP_FRAME_SECONDS =
            0.14f
    }
}
