package com.azizoglu.grisilith.weapon.visual

import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.weapon.Projectile
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import kotlin.math.max
import kotlin.math.sqrt

class ProjectileSpriteRenderer {

    companion object {
        private const val LONGBOW_ID =
            "WPN_LONGBOW"

        private const val CROSSBOW_ID =
            "WPN_CROSSBOW"

        /*
         * Faz 1.1a HOTFIX
         *
         * The supplied 1536x1024 image looks like an 8x8 sheet, but the
         * artwork is not actually locked to exact 192x128 cells.
         *
         * For example the Lightning row ends around y=840 while Necrotic
         * begins around y=845. The previous 128px grid cut Lightning at
         * y=896, which is why a Lightning projectile could contain part of
         * the Necrotic artwork.
         *
         * These are source-image boundaries, not generated graphics.
         * The original PNG remains unchanged.
         */
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
    }

    private val definition =
        DefaultProjectileSpriteSheet.definition

    private val batch =
        SpriteBatch()

    private var texture:
        Texture? =
        null

    private var regions:
        Array<Array<TextureRegion>>? =
        null

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

            val direction =
                ProjectileVisualDirection
                    .fromVelocity(
                        x =
                            projectile
                                .visualVelocityX,

                        y =
                            projectile
                                .visualVelocityY
                    )

            /*
             * HOTFIX: every visual was facing exactly the opposite direction.
             *
             * Source columns are:
             * 0 N, 1 NW, 2 W, 3 SW, 4 S, 5 SE, 6 E, 7 NE
             *
             * Opposite direction in this circular order is always +4.
             */
            val correctedColumnIndex =
                (
                    direction.columnIndex +
                        4
                    ) %
                    definition.columns

            val region =
                regions[
                    kind.rowIndex
                ][
                    correctedColumnIndex
                ]

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

            var drawWidth =
                drawHeight *
                    regionAspect

            /*
             * Crossbow keeps the code-only shorter/thicker bolt treatment.
             */
            if (weaponId == CROSSBOW_ID) {
                when (correctedColumnIndex) {
                    0,
                    4 -> {
                        drawHeight *=
                            CROSSBOW_LENGTH_SCALE

                        drawWidth *=
                            CROSSBOW_THICKNESS_SCALE
                    }

                    2,
                    6 -> {
                        drawWidth *=
                            CROSSBOW_LENGTH_SCALE

                        drawHeight *=
                            CROSSBOW_THICKNESS_SCALE
                    }

                    else -> {
                        drawWidth *=
                            0.86f

                        drawHeight *=
                            0.86f
                    }
                }
            }

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

                batch.draw(
                    region,
                    drawX - perpendicularX,
                    drawY - perpendicularY,
                    drawWidth,
                    drawHeight
                )

                batch.draw(
                    region,
                    drawX + perpendicularX,
                    drawY + perpendicularY,
                    drawWidth,
                    drawHeight
                )
            }

            batch.draw(
                region,
                drawX,
                drawY,
                drawWidth,
                drawHeight
            )
        }

        batch.end()
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
    ): Array<Array<TextureRegion>> {

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
            Array(
                definition.rows
            ) {
                    row ->

                val top =
                    ROW_BOUNDS[row]

                val bottom =
                    ROW_BOUNDS[row + 1]

                Array(
                    definition.columns
                ) {
                        column ->

                    val left =
                        COLUMN_BOUNDS[column]

                    val right =
                        COLUMN_BOUNDS[column + 1]

                    TextureRegion(
                        texture,
                        left,
                        top,
                        right - left,
                        bottom - top
                    )
                }
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

        batch.dispose()
    }
}
