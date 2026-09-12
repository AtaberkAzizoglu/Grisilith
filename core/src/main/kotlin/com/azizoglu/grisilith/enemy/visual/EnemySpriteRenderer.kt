package com.azizoglu.grisilith.enemy.visual

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.enemy.trait.EnemyTraitSpecialVisualKind
import com.azizoglu.grisilith.enemy.trait.EnemyTraitSpellVisual
import com.azizoglu.grisilith.enemy.trait.EnemyTraitSpellVisualKind
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
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
            if (enemy.isVampireSecondForm) {
                DefaultUndeadEnemySpriteSheets.vampireSecondForm
            } else {
                EnemySpriteSheetRegistry.find(enemy.kindId) ?: return
            }

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
             * The charge uses the first two self-contained wind-up frames.
             * The third source cell's raised spear crosses into its neighbour;
             * excluding it prevents the cropped shaft/foreign pixel fragments
             * that were visible during the animation.
             * The projectile itself is rendered separately at release, so
             * airborne-spear/recovery cells cannot leak into adjacent cells.
             */
            val column = (spearmanThrow.progress * 2f).toInt().coerceIn(0, 1)
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
                visualScale *
                enemy.spawnVisualScale

        /*
         * Kaynak frame'lerin aspect ratio'sunu koruyoruz.
         * Böylece Crocodile gibi uzun veya Bald Eagle gibi geniş
         * sprite'lar kareye zorlanıp bozulmuyor.
         */
        val sourceLargestSide =
            if (spearmanThrow != null) {
                SPEARMAN_THROW_REFERENCE_SIDE
            } else {
                val animation = definition.animationFor(visualState.direction)
                (animation.walkFrames + animation.idleFrame)
                    .maxOf { max(it.width, it.height) }
                    .toFloat()
            }.coerceAtLeast(1f)

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

        /*
         * Tight crops differ slightly between walk frames. Keeping a stable
         * scale and a fixed feet/ground anchor prevents both size pulsing and
         * the one-pixel vertical jitter seen across every enemy family.
         */
        val drawY = enemy.bounds.y

        specialVisual
            ?.takeIf { it.kind == EnemyTraitSpecialVisualKind.HORSEMAN_SWIPE }
            ?.takeIf { !it.isImpact }
            ?.let { swipe ->
                renderHorsemanTelegraph(
                    enemy = enemy,
                    progress = swipe.progress,
                    scale = swipe.scale,
                    segmentMask = swipe.segmentMask
                )
            }

        specialVisual
            ?.takeIf { it.kind == EnemyTraitSpecialVisualKind.GHOST_STORM }
            ?.let { storm ->
                if (storm.isImpact) {
                    renderGhostStorm(enemy, storm.progress, storm.scale)
                } else {
                    renderGhostStormTelegraph(enemy, storm.scale)
                }
            }

        renderSpellVisuals(enemy.traitSpellVisuals)

        batch.color =
            Color(
                1f,
                1f,
                1f,
                enemy.deathFadeAlpha
            )

        val vampireBodyIsReplacedByMist =
            specialVisual?.kind == EnemyTraitSpecialVisualKind.VAMPIRE_TRANSFORMATION ||
                specialVisual?.kind == EnemyTraitSpecialVisualKind.VAMPIRE_TELEPORT
        if (!vampireBodyIsReplacedByMist) {
            batch.draw(
                region,
                drawX,
                drawY,
                drawWidth,
                drawHeight
            )
        }

        specialVisual
            ?.takeIf {
                it.kind == EnemyTraitSpecialVisualKind.VAMPIRE_TRANSFORMATION ||
                    it.kind == EnemyTraitSpecialVisualKind.VAMPIRE_TELEPORT
            }
            ?.let { renderVampireMist(enemy, it.progress) }

        specialVisual
            ?.takeIf { it.kind == EnemyTraitSpecialVisualKind.HORSEMAN_SWIPE }
            ?.takeIf { it.isImpact }
            ?.let { swipe ->
                if (swipe.segmentMask == ALL_HORSEMAN_SEGMENTS) {
                    renderHorsemanSwipe(enemy, swipe.progress, swipe.scale)
                } else {
                    renderHorsemanQuarterImpact(
                        enemy = enemy,
                        progress = swipe.progress,
                        scale = swipe.scale,
                        segmentMask = swipe.segmentMask
                    )
                }
            }
    }

    fun renderGroundVisuals(enemies: List<Enemy>, camera: OrthographicCamera) {
        val visuals = enemies.flatMap { it.traitGroundVisuals }
        if (visuals.isEmpty()) return
        val texture = textureForPath(SLIME_PATH_TEXTURE, 1536, 1024)
        batch.projectionMatrix = camera.combined
        batch.enableBlending()
        batch.begin()
        visuals.forEach { visual ->
            val column = visual.frameIndex.mod(3)
            val row = visual.frameIndex / 3
            val region = regionFor(
                texture,
                EnemySpriteFrame(column * 512, row * 512, 512, 512)
            )
            batch.color = Color(1f, 1f, 1f, visual.alpha)
            batch.draw(
                region,
                visual.centerX - visual.diameter / 2f,
                visual.centerY - visual.diameter / 2f,
                visual.diameter,
                visual.diameter
            )
        }
        batch.color = Color.WHITE
        batch.end()
    }

    private fun renderHorsemanTelegraph(
        enemy: Enemy,
        progress: Float,
        scale: Float,
        segmentMask: Int
    ) {
        batch.color = Color(1f, 1f, 1f, HORSEMAN_TELEGRAPH_ALPHA * enemy.deathFadeAlpha)
        val radius = HORSEMAN_TELEGRAPH_RADIUS * scale
        if (segmentMask == ALL_HORSEMAN_SEGMENTS) {
            val frameIndex = (progress * 4f).toInt().coerceIn(0, 3)
            val frame = EnemySpriteFrame(
                x = HORSEMAN_FULL_TELEGRAPH_X[frameIndex],
                y = 174,
                width = 410,
                height = 415
            )
            val texture = textureForPath(HORSEMAN_FULL_TELEGRAPH_PATH, 2048, 768)
            val region = regionFor(texture, frame)
            val diameter = radius * 2f
            batch.draw(
                region,
                enemy.centerX - radius,
                enemy.centerY - radius,
                diameter,
                diameter
            )
            return
        }

        val texture = textureForPath(HORSEMAN_QUARTER_TELEGRAPH_PATH, 1254, 1254)
        val region = regionFor(
            texture,
            EnemySpriteFrame(x = 80, y = 115, width = 1120, height = 1110)
        )
        repeat(4) { segment ->
            if ((segmentMask and (1 shl segment)) == 0) return@repeat
            val rotation = when (segment) {
                0 -> 45f
                1 -> -45f
                2 -> -135f
                else -> 135f
            }
            batch.draw(
                region,
                enemy.centerX,
                enemy.centerY,
                0f,
                0f,
                radius,
                radius,
                1f,
                1f,
                rotation
            )
        }
    }

    private fun renderGhostStormTelegraph(enemy: Enemy, scale: Float) {
        val texture = textureForPath(HORSEMAN_QUARTER_TELEGRAPH_PATH, 1254, 1254)
        val region = regionFor(texture, EnemySpriteFrame(80, 115, 1120, 1110))
        val radius = GHOST_STORM_RADIUS * scale
        batch.color = Color(1f, 1f, 1f, 0.50f * enemy.deathFadeAlpha)
        repeat(4) { segment ->
            batch.draw(
                region,
                enemy.centerX,
                enemy.centerY,
                0f,
                0f,
                radius,
                radius,
                1f,
                1f,
                45f - segment * 90f
            )
        }
    }

    private fun renderGhostStorm(enemy: Enemy, progress: Float, scale: Float) {
        val texture = textureForPath(GHOST_STORM_PATH, 2176, 320)
        val frameIndex = (progress * 8f).toInt().coerceIn(0, 7)
        val region = regionFor(
            texture,
            EnemySpriteFrame(frameIndex * 272, 0, 272, 320)
        )
        val diameter = GHOST_STORM_RADIUS * 2f * scale
        batch.color = Color(1f, 1f, 1f, 0.86f * enemy.deathFadeAlpha)
        batch.draw(
            region,
            enemy.centerX - diameter / 2f,
            enemy.centerY - diameter / 2f,
            diameter,
            diameter
        )
    }

    private fun renderSpellVisuals(visuals: List<EnemyTraitSpellVisual>) {
        visuals.forEach { visual ->
            if (visual.kind == EnemyTraitSpellVisualKind.VAMPIRE_HUNGER) {
                renderVampireHunger(visual)
                return@forEach
            }
            val (prefix, frameCount, frameSeconds) = when (visual.kind) {
                EnemyTraitSpellVisualKind.STATIC_CLOUD -> Triple("static_clouds", 6, 0.16f)
                EnemyTraitSpellVisualKind.METEOR -> Triple("meteor", 10, 0f)
                EnemyTraitSpellVisualKind.TSUNAMI -> Triple("tsunami", 10, 0.11f)
                EnemyTraitSpellVisualKind.CALL_LIGHTNING -> Triple("call_lightning", 6, 0f)
                EnemyTraitSpellVisualKind.VAMPIRE_HUNGER -> error("Handled above")
            }
            val frame = if (frameSeconds > 0f) {
                ((visual.animationSeconds / frameSeconds).toInt() % frameCount) + 1
            } else {
                (visual.progress * frameCount).toInt().coerceIn(0, frameCount - 1) + 1
            }
            val path = "effects/spell_focus/${prefix}_${frame.toString().padStart(2, '0')}.png"
            val handle = Gdx.files.internal(path)
            if (!handle.exists()) return@forEach
            val texture = textures.getOrPut(path) {
                Texture(handle).also {
                    it.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
                }
            }
            val region = TextureRegion(texture)
            batch.color = Color(1f, 1f, 1f, visual.alpha.coerceIn(0f, 1f))
            batch.draw(
                region,
                visual.centerX - visual.worldWidth / 2f,
                visual.centerY - visual.worldHeight / 2f,
                visual.worldWidth / 2f,
                visual.worldHeight / 2f,
                visual.worldWidth,
                visual.worldHeight,
                1f,
                1f,
                visual.rotationDegrees
            )
        }
        batch.color = Color.WHITE
    }

    private fun renderVampireMist(enemy: Enemy, progress: Float) {
        val texture = textureForPath(VAMPIRE_TRANSFORMATION_PATH, 2560, 1024)
        val frameIndex = (progress * 10f).toInt().coerceIn(0, 9)
        val frameColumn = frameIndex % 5
        val frameRow = frameIndex / 5
        val region =
            regionFor(
                texture,
                EnemySpriteFrame(frameColumn * 512, frameRow * 512, 512, 512)
            )
        val size = enemy.visualSize * 1.75f
        batch.color = Color(1f, 1f, 1f, enemy.deathFadeAlpha)
        batch.draw(region, enemy.centerX - size / 2f, enemy.centerY - size * 0.42f, size, size)
    }

    private fun renderVampireHunger(visual: EnemyTraitSpellVisual) {
        val texture = textureForPath(VAMPIRE_HUNGER_PATH, 2560, 512)
        val frameIndex = (visual.progress * 5f).toInt().coerceIn(0, 4)
        val region = regionFor(texture, EnemySpriteFrame(frameIndex * 512, 0, 512, 512))
        val travel = ((visual.progress - 0.60f) / 0.40f).coerceIn(0f, 1f)
        val x = MathUtils.lerp(visual.centerX, visual.targetCenterX, travel)
        val y = MathUtils.lerp(visual.centerY, visual.targetCenterY, travel)
        batch.color = Color(1f, 1f, 1f, visual.alpha)
        batch.draw(
            region,
            x - visual.worldWidth / 2f,
            y - visual.worldHeight / 2f,
            visual.worldWidth / 2f,
            visual.worldHeight / 2f,
            visual.worldWidth,
            visual.worldHeight,
            1f,
            1f,
            visual.rotationDegrees
        )
    }

    private fun renderHorsemanSwipe(
        enemy: Enemy,
        progress: Float,
        scale: Float
    ) {
        /* Full-area damage must read as 360 degrees from its first frame. */
        val frameIndex = 4 + (progress * 4f).toInt().coerceIn(0, 3)
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

    private fun renderHorsemanQuarterImpact(
        enemy: Enemy,
        progress: Float,
        scale: Float,
        segmentMask: Int
    ) {
        val texture = textureForPath(HORSEMAN_QUARTER_SLASH_PATH, 1254, 1254)
        val region = regionFor(
            texture,
            EnemySpriteFrame(x = 0, y = 0, width = 1254, height = 1254)
        )
        val pulseScale = 0.88f + progress * 0.16f
        val diameter = HORSEMAN_TELEGRAPH_RADIUS * 2f * scale * pulseScale
        batch.color = Color(
            1f,
            1f,
            1f,
            (1f - progress).coerceIn(0f, 1f) * enemy.deathFadeAlpha
        )
        repeat(4) { segment ->
            if ((segmentMask and (1 shl segment)) == 0) return@repeat
            val rotation = when (segment) {
                0 -> 45f
                1 -> -45f
                2 -> -135f
                else -> 135f
            }
            batch.draw(
                region,
                enemy.centerX - diameter / 2f,
                enemy.centerY - diameter / 2f,
                diameter / 2f,
                diameter / 2f,
                diameter,
                diameter,
                1f,
                1f,
                rotation
            )
        }
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
        private const val SPEARMAN_THROW_REFERENCE_SIDE = 281f

        private const val HORSEMAN_SWIPE_PATH = "enemy_effects/horseman_swipe.png"
        private val HORSEMAN_SWIPE_X = intArrayOf(0, 444, 887, 1330, 1774)
        private val HORSEMAN_SWIPE_Y = intArrayOf(0, 444, 887)

        private const val HORSEMAN_QUARTER_TELEGRAPH_PATH =
            "enemy_effects/horseman_quarter_telegraph.png"
        private const val HORSEMAN_FULL_TELEGRAPH_PATH =
            "enemy_effects/horseman_full_telegraph.png"
        private const val HORSEMAN_QUARTER_SLASH_PATH =
            "enemy_effects/horseman_quarter_slash.png"
        private val HORSEMAN_FULL_TELEGRAPH_X = intArrayOf(80, 565, 1050, 1540)
        private const val HORSEMAN_TELEGRAPH_ALPHA = 0.50f
        private const val HORSEMAN_TELEGRAPH_RADIUS = 310f
        private const val ALL_HORSEMAN_SEGMENTS = 0b1111
        private const val GHOST_STORM_PATH = "enemy_effects/ghost_storm_runtime.png"
        private const val GHOST_STORM_RADIUS = 285f
        private const val SLIME_PATH_TEXTURE = "enemy_effects/slime_acidic_path.png"
        private const val VAMPIRE_TRANSFORMATION_PATH = "enemy_effects/vampire_transformation.png"
        private const val VAMPIRE_HUNGER_PATH = "enemy_effects/vampire_hunger.png"
    }
}
