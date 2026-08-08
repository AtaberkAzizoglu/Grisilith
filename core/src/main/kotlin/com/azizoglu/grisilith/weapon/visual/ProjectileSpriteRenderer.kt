package com.azizoglu.grisilith.weapon.visual

import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.weapon.Projectile
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import kotlin.math.max

class ProjectileSpriteRenderer {

    companion object {
        private const val LONGBOW_ID =
            "WPN_LONGBOW"

        private const val CROSSBOW_ID =
            "WPN_CROSSBOW"
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

            val region =
                regions[
                    kind.rowIndex
                ][
                    direction.columnIndex
                ]

            /*
             * Existing collision size remains untouched.
             * This only controls how large the art appears.
             *
             * Heavy/large projectile variants still grow because
             * Projectile.visualCollisionSize already contains their
             * size multiplier.
             */
            val renderSize =
                max(
                    kind.baseWorldSize,
                    projectile
                        .visualCollisionSize *
                        2.25f
                )

            val drawX =
                projectile.visualCenterX -
                    renderSize / 2f

            val drawY =
                projectile.visualCenterY -
                    renderSize / 2f

            batch.draw(
                region,
                drawX,
                drawY,
                renderSize,
                renderSize
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

        /*
         * Ranged physical weapons use the Arrow row regardless
         * of their PIERCING damage type.
         */
        if (
            weaponId == LONGBOW_ID ||
            weaponId == CROSSBOW_ID
        ) {
            return ProjectileVisualKind
                .ARROW
        }

        /*
         * Phase 1 scope:
         * spell/ranged projectiles only.
         *
         * EARTH and COLD do not have rows in the supplied sheet,
         * so they intentionally keep the old ShapeRenderer fallback.
         * Dagger is also left for the later melee-weapon pass.
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

        val built =
            Array(
                definition.rows
            ) {
                    row ->

                Array(
                    definition.columns
                ) {
                        column ->

                    TextureRegion(
                        texture,

                        column *
                            definition.tileWidth,

                        row *
                            definition.tileHeight,

                        definition.tileWidth,
                        definition.tileHeight
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
