package com.azizoglu.grisilith.weapon.visual.carrier

import com.azizoglu.grisilith.character.visual.CharacterVisualDirection
import com.azizoglu.grisilith.weapon.visual.carrier.WeaponCarrierVisualSystem.CarrierSnapshot
import com.azizoglu.grisilith.weapon.visual.carrier.WeaponCarrierVisualSystem.MinionType
import com.azizoglu.grisilith.weapon.visual.carrier.WeaponCarrierVisualSystem.MinionVisualDirection
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import kotlin.math.sin

class WeaponCarrierRenderer {

    companion object {
        private const val PLAYER_WEAPON_SIZE =
            42f

        private const val MINION_WEAPON_SIZE =
            26f

        /*
         * Hotfix: carriers were visually too large next to the player.
         */
        private const val MINION_WORLD_HEIGHT =
            27f

        private const val MINION_WALK_FRAME_DURATION =
            0.11f

        /*
         * Subtle visual separation from the actual player character.
         * The filter is intentionally weak.
         */
        private const val MINION_GRAY_TINT =
            0.84f

        private const val MINION_ALPHA =
            0.96f

        private const val MINION_SPELL_SCALE =
            0.70f
    }

    private data class Rect(
        val x: Int,
        val y: Int,
        val w: Int,
        val h: Int
    )

    private val batch =
        SpriteBatch()

    private val textures =
        mutableMapOf<String, Texture>()

    private val regions =
        mutableMapOf<String, TextureRegion>()

    fun renderPlayerCarrier(
        snapshot: CarrierSnapshot,
        camera: OrthographicCamera,
        renderCharacter: () -> Unit
    ) {
        renderCharacter()
        renderHeldWeapon(snapshot, camera)
    }

    fun renderMinionCarrier(snapshot: CarrierSnapshot, camera: OrthographicCamera) {
        renderMinion(snapshot, camera)
        renderHeldWeapon(snapshot, camera)
    }

    private fun renderMinion(
        snapshot: CarrierSnapshot,
        camera: OrthographicCamera
    ) {
        val type =
            snapshot.minionType
                ?: return

        val direction =
            snapshot.minionVisualDirection
                ?: return

        val walkFrame =
            if (
                snapshot.minionIsMoving
            ) {
                (
                    snapshot.minionAnimationTime /
                        MINION_WALK_FRAME_DURATION
                    )
                    .toInt()
                    .mod(4)
            } else {
                -1
            }

        val frame =
            minionFrame(
                type = type,
                direction = direction,
                walkFrame = walkFrame
            )

        drawRegion(
            texturePath =
                frame.first,
            rect = frame.second,
            centerX =
                snapshot.centerX,
            centerY =
                snapshot.centerY,
            worldHeight =
                (128f / 116f) * when (type) {
                    MinionType.BARBARIAN -> 32f
                    else -> MINION_WORLD_HEIGHT
                },
            camera =
                camera,
            alpha =
                MINION_ALPHA,
            tint =
                MINION_GRAY_TINT
        )
    }

    private fun renderHeldWeapon(
        snapshot: CarrierSnapshot,
        camera: OrthographicCamera
    ) {
        val definitionId =
            snapshot.weapon
                .definition
                .id
                .value

        val spellFocusFrame =
            spellFocusFrame(
                definitionId =
                    definitionId,
                snapshot =
                    snapshot
            )

        val frame =
            spellFocusFrame
                ?: heldWeaponFrame(
                    definitionId =
                        definitionId,
                    direction =
                        snapshot.direction
                )
                ?: return

        val isMinion =
            snapshot.slotIndex >
                0

        val isSpellFocus =
            spellFocusFrame !=
                null

        // Two original blades, individually cut and slightly smaller than the old pair.
        if (definitionId == "WPN_DOUBLE_SWORDS") {
            val height = if (isMinion) 23f else 38f
            val distance = if (isMinion) 26f else 34f
            listOf("left" to -distance, "right" to distance).forEach { (side, dx) ->
                val path = "weapon_visuals/carriers_v4/double_sword_" + side + ".png"
                val texture = textureFor(path)
                drawRegion(path, Rect(0, 0, texture.width, texture.height),
                    snapshot.centerX + dx, snapshot.centerY + 2f, height, camera,
                    if(isMinion) MINION_ALPHA else 1f, if(isMinion) MINION_GRAY_TINT else 1f)
            }
            return
        }

        val baseSize =
            if (isSpellFocus) {
                spellFocusWorldHeight(
                    definitionId =
                        definitionId,
                    isMinion =
                        isMinion
                )
            } else if (
                !isMinion
            ) {
                PLAYER_WEAPON_SIZE
            } else {
                MINION_WEAPON_SIZE
            }

        val pulseScale = when(definitionId) {
            "WPN_DAGGER" -> .80f
            "WPN_WARHAMMER" -> 60f / PLAYER_WEAPON_SIZE
            "WPN_LANCE" -> 104f / PLAYER_WEAPON_SIZE
            else -> 1f
        }
        // Fixed screen-right placement, independent of facing and attack recoil.
        val tallWeapon = definitionId == "WPN_WARHAMMER" || definitionId == "WPN_LANCE"
        val renderedHeight = baseSize * pulseScale
        val offset = (if (isMinion) 26f else 34f) to
            if(tallWeapon) renderedHeight * .5f - (if(isMinion) 11f else 18f) else 2f

        /*
         * Generated held art remains stable; attack events are rendered by weapon runtimes.
         */
        val meleeRotation = when (definitionId) {
            "WPN_LONGSWORD", "WPN_DAGGER", "WPN_LONGBOW",
            "WPN_WHIP", "WPN_IVY_STAFF", "WPN_STEAM_WAND" -> 45f
            "WPN_CROSSBOW" -> -18f
            "WPN_BOOMERANG" -> -45f
            else -> 0f
        }

        drawRegion(
            texturePath =
                frame.first,
            rect =
                frame.second,
            centerX =
                snapshot.centerX +
                    offset.first,
            centerY =
                snapshot.centerY +
                    offset.second,
            worldHeight =
                baseSize *
                    pulseScale,
            camera =
                camera,
            alpha =
                if (isMinion) {
                    MINION_ALPHA
                } else {
                    1f
                },
            tint =
                if (isMinion) {
                    MINION_GRAY_TINT
                } else {
                    1f
                },
            rotationDegrees =
                meleeRotation
        )
    }

    private fun spellFocusWorldHeight(
        definitionId: String,
        isMinion: Boolean
    ): Float {
        /*
         * Requested visual classes:
         * Ivy Staff              = LARGE
         * Wand of Steam          = MEDIUM
         * Tome of Eryndor        = MEDIUM
         * Orb of Sky             = MEDIUM
         * Totem of Dark Clouds   = LARGE
         * Enchanted Chalk Dust   = SMALL
         * Amulet of Troll King   = NO VISUAL
         */
        val playerSize =
            when (definitionId) {
                "WPN_IVY_STAFF" ->
                    28f

                "WPN_STEAM_WAND" ->
                    20f

                "WPN_TOME_OF_ERYNDOR" ->
                    21f

                "WPN_ORB_OF_SKY" ->
                    19f

                "WPN_TOTEM_OF_DARK_CLOUDS" ->
                    27f

                "WPN_ENCHANTED_CHALK_DUST" ->
                    15f
                "WPN_AMULET_OF_TROLL_KING" -> 18f

                else ->
                    38f
            }

        return if (isMinion) {
            playerSize *
                MINION_SPELL_SCALE
        } else {
            playerSize
        }
    }

    private fun spellFocusFrame(definitionId: String, snapshot: CarrierSnapshot): Pair<String,Rect>? {
        val definition=com.azizoglu.grisilith.weapon.definition.DefaultWeapons.findById(definitionId) ?: return null
        return if(com.azizoglu.grisilith.content.ContentTag.SPELL_FOCUS in definition.tags)
            generatedWeaponFrame(definitionId) else null
    }

    private fun meleeEffectRotation(
        id: String,
        direction: CharacterVisualDirection,
        progress: Float
    ): Float {
        val base =
            when (direction) {
                CharacterVisualDirection.RIGHT ->
                    0f

                CharacterVisualDirection.DOWN ->
                    -90f

                CharacterVisualDirection.LEFT ->
                    180f

                CharacterVisualDirection.UP ->
                    90f
            }

        if (
            id ==
            "WPN_LANCE"
        ) {
            return base
        }

        val sweep =
            progress *
                2f -
                1f

        val amount =
            when (id) {
                "WPN_DAGGER" ->
                    48f

                "WPN_DOUBLE_SWORDS" ->
                    38f

                else ->
                    44f
            }

        return base +
            sweep *
            amount
    }

    private fun heldOffset(
        direction:
        CharacterVisualDirection,
        amount: Float
    ): Pair<Float, Float> {
        return when (
            direction
        ) {
            CharacterVisualDirection.DOWN ->
                13f to
                    (-2f + amount)

            CharacterVisualDirection.UP ->
                -13f to
                    (4f - amount)

            CharacterVisualDirection.LEFT ->
                (-14f - amount) to
                    2f

            CharacterVisualDirection.RIGHT ->
                (14f + amount) to
                    2f
        }
    }

    private fun minionFrame(type: MinionType, direction: MinionVisualDirection, walkFrame: Int): Pair<String, Rect> {
        val fourFrames = type == MinionType.BARBARIAN || type == MinionType.DRUID
        val column = if (walkFrame < 0) 0 else walkFrame.mod(4) + if(fourFrames) 0 else 1
        val path = "weapon_visuals/minions_aligned/" + when(type) {
            MinionType.SOLDIER -> "soldier"
            MinionType.SCOUT -> "scout"
            MinionType.MAGE_APPRENTICE -> "mage_apprentice"
            MinionType.BARBARIAN -> "barbarian"
            MinionType.DRUID -> "druid"
        } + ".png"
        val row = if(type == MinionType.BARBARIAN || type == MinionType.DRUID) {
            when(direction) {
                MinionVisualDirection.SOUTH, MinionVisualDirection.SOUTH_EAST -> 0
                MinionVisualDirection.SOUTH_WEST, MinionVisualDirection.WEST -> 1
                MinionVisualDirection.NORTH_EAST, MinionVisualDirection.EAST -> 2
                MinionVisualDirection.NORTH, MinionVisualDirection.NORTH_WEST -> 3
            }
        } else when(direction) {
            MinionVisualDirection.SOUTH -> 0
            MinionVisualDirection.SOUTH_WEST -> 1
            MinionVisualDirection.WEST -> 2
            MinionVisualDirection.NORTH_WEST -> 3
            MinionVisualDirection.NORTH -> 4
            MinionVisualDirection.NORTH_EAST -> 5
            MinionVisualDirection.EAST -> 6
            MinionVisualDirection.SOUTH_EAST -> 7
        }
        return path to Rect(column*128, row*128, 128, 128)
    }

    private fun generatedWeaponFrame(id: String): Pair<String,Rect>? {
        if(com.azizoglu.grisilith.weapon.definition.DefaultWeapons.findById(id)==null) return null
        val path="weapon_visuals/carriers_v4/"+id.removePrefix("WPN_").lowercase()+".png"
        val texture=textureFor(path)
        return path to Rect(0,0,texture.width,texture.height)
    }

    private fun heldWeaponFrame(definitionId: String, direction: CharacterVisualDirection): Pair<String,Rect>? =
        generatedWeaponFrame(definitionId)

    private fun drawRegion(
        texturePath: String,
        rect: Rect,
        centerX: Float,
        centerY: Float,
        worldHeight: Float,
        camera: OrthographicCamera,
        alpha: Float = 1f,
        tint: Float = 1f,
        rotationDegrees: Float = 0f
    ) {
        val texture =
            textureFor(texturePath)

        val safeX =
            rect.x.coerceIn(
                0,
                texture.width - 1
            )

        val safeY =
            rect.y.coerceIn(
                0,
                texture.height - 1
            )

        val safeW =
            rect.w.coerceAtMost(
                texture.width - safeX
            )

        val safeH =
            rect.h.coerceAtMost(
                texture.height - safeY
            )

        val key =
            "$texturePath:$safeX:$safeY:$safeW:$safeH"

        val region =
            regions.getOrPut(key) {
                TextureRegion(
                    texture,
                    safeX,
                    safeY,
                    safeW,
                    safeH
                )
            }

        val aspect =
            safeW.toFloat() /
                safeH.toFloat()
                    .coerceAtLeast(
                        1f
                    )

        val drawHeight =
            worldHeight

        val drawWidth =
            drawHeight *
                aspect

        batch.projectionMatrix =
            camera.combined

        batch.enableBlending()
        batch.begin()

        val safeTint =
            tint.coerceIn(
                0f,
                1f
            )

        batch.setColor(
            safeTint,
            safeTint,
            safeTint,
            alpha.coerceIn(
                0f,
                1f
            )
        )

        batch.draw(
            region,
            centerX -
                drawWidth / 2f,
            centerY -
                drawHeight / 2f,
            drawWidth / 2f,
            drawHeight / 2f,
            drawWidth,
            drawHeight,
            1f,
            1f,
            rotationDegrees
        )

        batch.setColor(
            1f,
            1f,
            1f,
            1f
        )

        batch.end()
    }

    private fun textureFor(
        path: String
    ): Texture {
        return textures.getOrPut(path) {
            Texture(
                Gdx.files.internal(path)
            ).also {
                it.setFilter(
                    Texture.TextureFilter.Nearest,
                    Texture.TextureFilter.Nearest
                )
            }
        }
    }

    fun dispose() {
        regions.clear()

        textures.values
            .forEach {
                it.dispose()
            }

        textures.clear()
        batch.dispose()
    }
}
