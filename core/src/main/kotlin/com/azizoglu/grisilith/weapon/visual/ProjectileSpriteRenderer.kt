package com.azizoglu.grisilith.weapon.visual

import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.trait.EnemyTraitProjectileVisual
import com.azizoglu.grisilith.enemy.trait.EnemyTraitProjectileVisualKind
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.math.atan2

class ProjectileSpriteRenderer {

    companion object {
        private const val LONGBOW_ID =
            "WPN_LONGBOW"

        private const val CROSSBOW_ID =
            "WPN_CROSSBOW"

        private const val DAGGER_ID =
            "WPN_DAGGER"

        private val COLUMN_BOUNDS =
            intArrayOf(
                0,
                176,
                358,
                561,
                745,
                904,
                1098,
                1314,
                1536
            )

        private val ROW_BOUNDS =
            intArrayOf(
                0,
                164,
                280,
                390,
                489,
                590,
                696,
                841,
                1024
            )

        /*
         * All projectile ART is about 30% smaller than Faz 1.1a.
         * Gameplay collision sizes from Faz 1.1 are intentionally preserved
         * so the improved hit registration is not lost.
         */
        private const val GENERAL_VISUAL_COLLISION_SCALE =
            1.85f

        private const val LONGBOW_ARROW_MIN_HEIGHT =
            31f

        private const val CROSSBOW_BOLT_MIN_HEIGHT =
            28f

        private const val CROSSBOW_LENGTH_SCALE =
            0.78f

        private const val CROSSBOW_THICKNESS_SCALE =
            1.18f

        private const val CROSSBOW_EXTRA_THICKNESS_OFFSET =
            0.90f

        private const val BOW_PROJECTILE_VISUAL_SCALE =
            1.16f

        private const val DAGGER_PROJECTILE_VISUAL_SCALE =
            0.68f

        /* Every kind now uses only its right-facing source frame. */
        private const val SOURCE_RIGHT_COLUMN = 6

        private const val SPEAR_TEXTURE_PATH = "projectiles/spear_projectile.png"
        private const val DAGGER_TEXTURE_PATH = "projectiles/dagger_projectile.png"
    }

    private val definition =
        DefaultProjectileSpriteSheet.definition

    private val batch =
        SpriteBatch()

    private var texture:
        Texture? =
        null

    private var regions:
        Array<TextureRegion>? =
        null

    private var spearTexture: Texture? = null
    private var spearRegion: TextureRegion? = null
    private var daggerTexture: Texture? = null
    private var daggerRegion: TextureRegion? = null

    fun hasVisualDefinition(
        projectile: Projectile
    ): Boolean {
        return resolveVisualKind(projectile) != null
    }

    fun render(
        projectiles:
        List<Projectile>,

        camera:
        OrthographicCamera
    ) {
        val texture =
            ensureTexture()
                ?: return

        val regions =
            ensureRegions(
                texture
            )

        batch.projectionMatrix =
            camera.combined

        batch.enableBlending()
        batch.begin()

        projectiles.forEach {
                projectile ->

            if (
                !projectile
                    .isVisualRenderable
            ) {
                return@forEach
            }

            val kind =
                resolveVisualKind(
                    projectile
                )
                    ?: return@forEach

            val region =
                if (kind == ProjectileVisualKind.DAGGER) {
                    ensureDaggerRegion() ?: return@forEach
                } else {
                    regions[kind.rowIndex]
                }

            val weaponId =
                projectile
                    .sourceWeapon
                    ?.definition
                    ?.id
                    ?.value

            /*
             * Use the actual custom region ratio. This is important because
             * the source frames are irregular and should not be forced into
             * either a square or a fake global 3:2 cell ratio.
             */
            val regionAspect =
                region.regionWidth
                    .toFloat() /
                    region.regionHeight
                        .toFloat()
                        .coerceAtLeast(
                            1f
                        )

            val baseHeight =
                max(
                    kind.baseWorldHeight,
                    projectile
                        .visualCollisionSize *
                        GENERAL_VISUAL_COLLISION_SCALE
                )

            var drawHeight =
                when (weaponId) {
                    LONGBOW_ID ->
                        max(
                            LONGBOW_ARROW_MIN_HEIGHT,
                            baseHeight
                        )

                    CROSSBOW_ID ->
                        max(
                            CROSSBOW_BOLT_MIN_HEIGHT,
                            baseHeight *
                                0.90f
                        )

                    else ->
                        baseHeight
                }

            if (weaponId == LONGBOW_ID || weaponId == CROSSBOW_ID) {
                drawHeight *= BOW_PROJECTILE_VISUAL_SCALE
            }

            if (weaponId == DAGGER_ID) {
                drawHeight *= DAGGER_PROJECTILE_VISUAL_SCALE
            }

            var drawWidth =
                drawHeight *
                    regionAspect

            /*
             * Crossbow keeps the code-only shorter/thicker bolt treatment.
             */
            if (weaponId == CROSSBOW_ID) {
                drawWidth *= CROSSBOW_LENGTH_SCALE
                drawHeight *= CROSSBOW_THICKNESS_SCALE
            }

            val rotation = rotationDegrees(
                projectile.visualVelocityX,
                projectile.visualVelocityY
            )

            val drawX =
                projectile.visualCenterX -
                    drawWidth / 2f

            val drawY =
                projectile.visualCenterY -
                    drawHeight / 2f

            if (weaponId == CROSSBOW_ID) {
                val velocityLength =
                    sqrt(
                        projectile.visualVelocityX *
                            projectile.visualVelocityX +
                            projectile.visualVelocityY *
                            projectile.visualVelocityY
                    )
                        .coerceAtLeast(
                            0.001f
                        )

                val perpendicularX =
                    -projectile.visualVelocityY /
                        velocityLength *
                        CROSSBOW_EXTRA_THICKNESS_OFFSET

                val perpendicularY =
                    projectile.visualVelocityX /
                        velocityLength *
                        CROSSBOW_EXTRA_THICKNESS_OFFSET

                drawRotated(
                    region,
                    drawX - perpendicularX,
                    drawY - perpendicularY,
                    drawWidth,
                    drawHeight,
                    rotation
                )

                drawRotated(
                    region,
                    drawX + perpendicularX,
                    drawY + perpendicularY,
                    drawWidth,
                    drawHeight,
                    rotation
                )
            }

            drawRotated(
                region,
                drawX,
                drawY,
                drawWidth,
                drawHeight,
                rotation
            )
        }

        batch.end()
    }

    fun renderEnemyProjectiles(enemies: List<Enemy>, camera: OrthographicCamera) {
        val visuals = enemies.flatMap { it.traitProjectileVisuals }
        if (visuals.isEmpty()) return
        val atlasTexture = ensureTexture() ?: return
        val atlasRegions = ensureRegions(atlasTexture)
        batch.projectionMatrix = camera.combined
        batch.enableBlending()
        batch.begin()
        visuals.forEach { visual ->
            val region = enemyRegion(visual, atlasRegions) ?: return@forEach
            val aspect = region.regionWidth.toFloat() / region.regionHeight.coerceAtLeast(1).toFloat()
            val height = visual.worldHeight
            val width = height * aspect
            batch.color = Color(1f, 1f, 1f, visual.alpha.coerceIn(0f, 1f))
            drawRotated(
                region,
                visual.centerX - width / 2f,
                visual.centerY - height / 2f,
                width,
                height,
                rotationDegrees(visual.velocityX, visual.velocityY)
            )
        }
        batch.color = Color.WHITE
        batch.end()
    }

    private fun enemyRegion(
        visual: EnemyTraitProjectileVisual,
        atlasRegions: Array<TextureRegion>
    ): TextureRegion? = when (visual.kind) {
        EnemyTraitProjectileVisualKind.ARROW -> atlasRegions[ProjectileVisualKind.ARROW.rowIndex]
        EnemyTraitProjectileVisualKind.AIR -> atlasRegions[ProjectileVisualKind.AIR.rowIndex]
        EnemyTraitProjectileVisualKind.POISON -> atlasRegions[ProjectileVisualKind.POISON.rowIndex]
        EnemyTraitProjectileVisualKind.LIGHTNING -> atlasRegions[ProjectileVisualKind.LIGHTNING.rowIndex]
        EnemyTraitProjectileVisualKind.NECROTIC -> atlasRegions[ProjectileVisualKind.NECROTIC.rowIndex]
        EnemyTraitProjectileVisualKind.SPEAR -> ensureSpearRegion()
    }

    private fun ensureSpearRegion(): TextureRegion? {
        spearRegion?.let { return it }
        val handle = Gdx.files.internal(SPEAR_TEXTURE_PATH)
        if (!handle.exists()) return null
        val loaded = Texture(handle).also {
            it.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
        }
        require(loaded.width == 724 && loaded.height == 2172) {
            "Unexpected spear projectile size: ${loaded.width}x${loaded.height}"
        }
        spearTexture = loaded
        return TextureRegion(loaded, 0, 882, 712, 280).also { spearRegion = it }
    }

    private fun ensureDaggerRegion(): TextureRegion? {
        daggerRegion?.let { return it }
        val handle = Gdx.files.internal(DAGGER_TEXTURE_PATH)
        if (!handle.exists()) return null
        val loaded = Texture(handle).also {
            it.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
        }
        require(loaded.width == 1774 && loaded.height == 887) {
            "Unexpected dagger projectile size: ${loaded.width}x${loaded.height}"
        }
        daggerTexture = loaded
        /* Tight alpha crop; the generated source keeps generous safe padding. */
        return TextureRegion(loaded, 300, 268, 1170, 350)
            .also { daggerRegion = it }
    }

    private fun rotationDegrees(x: Float, y: Float): Float =
        Math.toDegrees(atan2(y.toDouble(), x.toDouble())).toFloat()

    private fun drawRotated(
        region: TextureRegion,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        rotation: Float
    ) {
        batch.draw(
            region,
            x,
            y,
            width / 2f,
            height / 2f,
            width,
            height,
            1f,
            1f,
            rotation
        )
    }

    private fun resolveVisualKind(
        projectile:
        Projectile
    ): ProjectileVisualKind? {

        val weaponId =
            projectile
                .sourceWeapon
                ?.definition
                ?.id
                ?.value

        if (
            weaponId == LONGBOW_ID ||
            weaponId == CROSSBOW_ID
        ) {
            return ProjectileVisualKind.ARROW
        }


        if (weaponId == DAGGER_ID) {
            return ProjectileVisualKind.DAGGER
        }

        /*
         * Spell-focus weapons are intentionally resolved from the projectile's
         * FINAL selected DamageType. That means weapons such as Orb of Sky,
         * Steam Wand and Totem of Dark Clouds keep using whatever element the
         * attack executor actually selected for this specific projectile.
         *
         * The visual corruption seen in Faz 1.1a was atlas slicing, not this
         * DamageType mapping.
         */
        return when (
            projectile.damageType
        ) {
            DamageType.AIR ->
                ProjectileVisualKind.AIR

            DamageType.WATER ->
                ProjectileVisualKind.WATER

            DamageType.FIRE ->
                ProjectileVisualKind.FIRE

            DamageType.POISON ->
                ProjectileVisualKind.POISON

            DamageType.ACID ->
                ProjectileVisualKind.ACID

            DamageType.LIGHTNING ->
                ProjectileVisualKind.LIGHTNING

            DamageType.NECROTIC ->
                ProjectileVisualKind.NECROTIC

            else ->
                null
        }
    }

    private fun ensureTexture():
        Texture? {

        texture
            ?.let {
                return it
            }

        val handle =
            Gdx.files.internal(
                definition.texturePath
            )

        if (!handle.exists()) {
            return null
        }

        return Texture(handle)
            .also {
                it.setFilter(
                    Texture.TextureFilter.Nearest,
                    Texture.TextureFilter.Nearest
                )

                texture =
                    it
            }
    }

    private fun ensureRegions(
        texture:
        Texture
    ): Array<TextureRegion> {

        regions
            ?.let {
                return it
            }

        require(
            texture.width == 1536 &&
                texture.height == 1024
        ) {
            "Unexpected projectile atlas size: " +
                "${texture.width}x${texture.height}; expected 1536x1024"
        }

        val built =
            Array(definition.rows) { row ->

                val top =
                    ROW_BOUNDS[row]

                val bottom =
                    ROW_BOUNDS[row + 1]

                val left = COLUMN_BOUNDS[SOURCE_RIGHT_COLUMN]
                val right = COLUMN_BOUNDS[SOURCE_RIGHT_COLUMN + 1]
                TextureRegion(texture, left, top, right - left, bottom - top)
            }

        regions =
            built

        return built
    }

    fun dispose() {
        texture
            ?.dispose()

        texture =
            null

        regions =
            null

        spearRegion = null
        spearTexture?.dispose()
        spearTexture = null

        daggerRegion = null
        daggerTexture?.dispose()
        daggerTexture = null

        batch.dispose()
    }
}
