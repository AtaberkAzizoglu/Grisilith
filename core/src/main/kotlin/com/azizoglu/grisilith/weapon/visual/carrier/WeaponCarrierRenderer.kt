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

        private const val MELEE_EFFECT_SIZE =
            84f

        private const val SHIELD_EFFECT_SIZE =
            92f

        private const val MINION_TEXTURE =
            "weapon_visuals/minions.png"

        private const val MELEE_TEXTURE =
            "weapon_visuals/melee_weapons.png"

        private const val RANGED_TEXTURE =
            "weapon_visuals/ranged_and_shield.png"

        private const val SHIELD_ATTACK_TEXTURE =
            "weapon_visuals/shield_attack.png"

        private const val SPELL_FOCUS_TEXTURE =
            "weapon_visuals/spell_focus_weapons.png"

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
        /*
         * Facing UP = weapon belongs behind the character.
         * Other directions = character first, weapon on top.
         */
        if (
            snapshot.direction ==
            CharacterVisualDirection.UP
        ) {
            renderHeldWeapon(
                snapshot =
                    snapshot,
                camera =
                    camera
            )

            renderCharacter()
        } else {
            renderCharacter()

            renderHeldWeapon(
                snapshot =
                    snapshot,
                camera =
                    camera
            )
        }

        renderAttackEffect(
            snapshot =
                snapshot,
            camera =
                camera
        )
    }

    fun renderMinionCarrier(
        snapshot: CarrierSnapshot,
        camera: OrthographicCamera
    ) {
        if (
            snapshot.direction ==
            CharacterVisualDirection.UP
        ) {
            renderHeldWeapon(
                snapshot =
                    snapshot,
                camera =
                    camera
            )

            renderMinion(
                snapshot =
                    snapshot,
                camera =
                    camera
            )
        } else {
            renderMinion(
                snapshot =
                    snapshot,
                camera =
                    camera
            )

            renderHeldWeapon(
                snapshot =
                    snapshot,
                camera =
                    camera
            )
        }

        /*
         * IMPORTANT:
         * Attack FX is deliberately centered on PLAYER in WorldRenderer.
         * Minions are only visual carriers; actual attacks still originate
         * from the existing player-based executors.
         */
    }

    fun renderAttackEffectAtPlayer(
        snapshot: CarrierSnapshot,
        playerCenterX: Float,
        playerCenterY: Float,
        camera: OrthographicCamera
    ) {
        if (!snapshot.attackActive) {
            return
        }

        val attackSnapshot =
            snapshot.copy(
                centerX = playerCenterX,
                centerY = playerCenterY
            )

        renderAttackEffect(
            snapshot =
                attackSnapshot,
            camera =
                camera
        )
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

        val rect =
            minionRect(
                type = type,
                direction = direction,
                walkFrame = walkFrame
            )

        drawRegion(
            texturePath =
                MINION_TEXTURE,
            rect = rect,
            centerX =
                snapshot.centerX,
            centerY =
                snapshot.centerY,
            worldHeight =
                MINION_WORLD_HEIGHT,
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

        val phase =
            if (
                snapshot.attackActive
            ) {
                sin(
                    snapshot.attackProgress *
                        Math.PI
                )
                    .toFloat()
            } else {
                0f
            }

        val recoil =
            phase *
                when {
                    definitionId ==
                        "WPN_LONGBOW" ||
                        definitionId ==
                        "WPN_CROSSBOW" -> {
                        -4f
                    }

                    isSpellFocus -> {
                        2.2f
                    }

                    else -> {
                        3f
                    }
                }

        val pulseScale =
            if (
                isSpellFocus &&
                snapshot.attackActive
            ) {
                1f +
                    phase *
                    0.10f
            } else {
                1f
            }

        val spellFocusDistance =
            if (isSpellFocus) {
                if (isMinion) {
                    8f
                } else {
                    11f
                }
            } else {
                0f
            }

        val offset =
            heldOffset(
                direction =
                    snapshot.direction,
                amount =
                    recoil +
                        spellFocusDistance
            )

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
                }
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

    private fun spellFocusFrame(
        definitionId: String,
        snapshot: CarrierSnapshot
    ): Pair<String, Rect>? {
        val row =
            when (definitionId) {
                "WPN_IVY_STAFF" -> 0
                "WPN_STEAM_WAND" -> 1
                "WPN_TOME_OF_ERYNDOR" -> 2
                "WPN_ORB_OF_SKY" -> 3
                "WPN_TOTEM_OF_DARK_CLOUDS" -> 4
                "WPN_ENCHANTED_CHALK_DUST" -> 5

                /*
                 * Explicit user rule:
                 * Amulet of Troll King receives no held visual.
                 */
                "WPN_AMULET_OF_TROLL_KING" ->
                    return null

                else ->
                    return null
            }

        val directionIndex =
            if (
                snapshot.slotIndex >
                0 &&
                snapshot.minionVisualDirection !=
                null
            ) {
                spellDirectionIndex(
                    snapshot.minionVisualDirection
                )
            } else {
                spellDirectionIndex(
                    snapshot.direction
                )
            }

        return SPELL_FOCUS_TEXTURE to
            spellFocusRect(
                row = row,
                column =
                    directionIndex
            )
    }

    private fun spellDirectionIndex(
        direction:
        MinionVisualDirection
    ): Int {
        /*
         * Supplied sheet:
         * N, NW, W, SW, S, SE, E, NE
         */
        return when (direction) {
            MinionVisualDirection.NORTH -> 0
            MinionVisualDirection.NORTH_WEST -> 1
            MinionVisualDirection.WEST -> 2
            MinionVisualDirection.SOUTH_WEST -> 3
            MinionVisualDirection.SOUTH -> 4
            MinionVisualDirection.SOUTH_EAST -> 5
            MinionVisualDirection.EAST -> 6
            MinionVisualDirection.NORTH_EAST -> 7
        }
    }

    private fun spellDirectionIndex(
        direction:
        CharacterVisualDirection
    ): Int {
        return when (direction) {
            CharacterVisualDirection.UP -> 0
            CharacterVisualDirection.LEFT -> 2
            CharacterVisualDirection.DOWN -> 4
            CharacterVisualDirection.RIGHT -> 6
        }
    }

    private fun spellFocusRect(
        row: Int,
        column: Int
    ): Rect {
        /*
         * The supplied source is presentation-style rather than a strict
         * uniform atlas. These bounds isolate only the actual weapon art,
         * excluding labels and direction glyphs.
         */
        val xBounds =
            intArrayOf(
                200,
                335,
                480,
                620,
                780,
                920,
                1080,
                1220,
                1393
            )

        val yBounds =
            arrayOf(
                40 to 178,
                226 to 357,
                417 to 538,
                606 to 710,
                768 to 884,
                978 to 1103
            )

        val safeColumn =
            column.coerceIn(
                0,
                7
            )

        val safeRow =
            row.coerceIn(
                0,
                yBounds.lastIndex
            )

        val yRange =
            yBounds[safeRow]

        return Rect(
            x =
                xBounds[
                    safeColumn
                ],
            y =
                yRange.first,
            w =
                xBounds[
                    safeColumn +
                        1
                ] -
                    xBounds[
                        safeColumn
                    ],
            h =
                yRange.second -
                    yRange.first
        )
    }

    private fun renderAttackEffect(
        snapshot: CarrierSnapshot,
        camera: OrthographicCamera
    ) {
        if (!snapshot.attackActive) {
            return
        }

        val id =
            snapshot.weapon
                .definition
                .id
                .value

        when (id) {
            "WPN_SHIELD" -> {
                drawRegion(
                    texturePath =
                        SHIELD_ATTACK_TEXTURE,
                    rect =
                        shieldEffectFrame(
                            snapshot.direction
                        ),
                    centerX =
                        snapshot.centerX,
                    centerY =
                        snapshot.centerY,
                    worldHeight =
                        SHIELD_EFFECT_SIZE,
                    camera =
                        camera,
                    alpha =
                        1f -
                            snapshot.attackProgress *
                            0.45f
                )
            }

            "WPN_DOUBLE_SWORDS",
            "WPN_LONGSWORD",
            "WPN_LANCE",
            "WPN_DAGGER" -> {
                val effect =
                    meleeEffectFrame(
                        id = id,
                        direction =
                            snapshot.direction
                    )
                        ?: return

                drawRegion(
                    texturePath =
                        effect.first,
                    rect =
                        effect.second,
                    centerX =
                        snapshot.centerX,
                    centerY =
                        snapshot.centerY,
                    worldHeight =
                        MELEE_EFFECT_SIZE,
                    camera =
                        camera,
                    alpha =
                        1f -
                            snapshot.attackProgress *
                            0.60f
                )
            }

            /*
             * Longbow/Crossbow already show projectiles.
             * Their attack feeling is the held-weapon recoil above.
             */
        }
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

    private fun minionRect(
        type: MinionType,
        direction: MinionVisualDirection,
        walkFrame: Int
    ): Rect {
        /*
         * New sheet layout:
         * Three 512px-wide character panels.
         * Each direction row contains:
         *   IDLE, WALK 1, WALK 2, WALK 3, WALK 4
         *
         * walkFrame -1 = IDLE.
         */
        val panelX =
            when (type) {
                MinionType.SOLDIER -> 0
                MinionType.SCOUT -> 512
                MinionType.MAGE_APPRENTICE -> 1024
            }

        val localX =
            when (walkFrame) {
                0 -> 176
                1 -> 255
                2 -> 334
                3 -> 413
                else -> 92
            }

        val width =
            when (walkFrame) {
                0 -> 68
                1 -> 70
                2 -> 69
                3 -> 70
                else -> 73
            }

        val y =
            when (direction) {
                MinionVisualDirection.SOUTH -> 314
                MinionVisualDirection.SOUTH_WEST -> 395
                MinionVisualDirection.WEST -> 476
                MinionVisualDirection.NORTH_WEST -> 557
                MinionVisualDirection.NORTH -> 638
                MinionVisualDirection.NORTH_EAST -> 719
                MinionVisualDirection.EAST -> 800
                MinionVisualDirection.SOUTH_EAST -> 881
            }

        return Rect(
            x = panelX + localX,
            y = y,
            w = width,
            h = 79
        )
    }

    private fun heldWeaponFrame(
        definitionId: String,
        direction:
        CharacterVisualDirection
    ): Pair<String, Rect>? {
        return when (definitionId) {
            "WPN_DOUBLE_SWORDS" ->
                MELEE_TEXTURE to
                    when (direction) {
                        CharacterVisualDirection.DOWN ->
                            Rect(28, 25, 190, 190)
                        CharacterVisualDirection.UP ->
                            Rect(238, 25, 180, 190)
                        CharacterVisualDirection.LEFT ->
                            Rect(430, 35, 185, 170)
                        CharacterVisualDirection.RIGHT ->
                            Rect(620, 35, 185, 170)
                    }

            "WPN_LONGSWORD" ->
                MELEE_TEXTURE to
                    when (direction) {
                        CharacterVisualDirection.DOWN ->
                            Rect(45, 265, 120, 285)
                        CharacterVisualDirection.UP ->
                            Rect(230, 265, 120, 285)
                        CharacterVisualDirection.LEFT ->
                            Rect(425, 265, 150, 285)
                        CharacterVisualDirection.RIGHT ->
                            Rect(610, 265, 150, 285)
                    }

            "WPN_LANCE" ->
                MELEE_TEXTURE to
                    when (direction) {
                        CharacterVisualDirection.DOWN ->
                            Rect(35, 540, 130, 455)
                        CharacterVisualDirection.UP ->
                            Rect(220, 540, 130, 455)
                        CharacterVisualDirection.LEFT ->
                            Rect(390, 540, 190, 455)
                        CharacterVisualDirection.RIGHT ->
                            Rect(610, 540, 190, 455)
                    }

            "WPN_DAGGER" ->
                RANGED_TEXTURE to
                    when (direction) {
                        CharacterVisualDirection.DOWN ->
                            Rect(54, 12, 95, 150)
                        CharacterVisualDirection.UP ->
                            Rect(1280, 12, 95, 150)
                        CharacterVisualDirection.LEFT ->
                            Rect(604, 18, 170, 135)
                        CharacterVisualDirection.RIGHT ->
                            Rect(780, 18, 170, 135)
                    }

            "WPN_LONGBOW" ->
                RANGED_TEXTURE to
                    when (direction) {
                        CharacterVisualDirection.DOWN ->
                            Rect(50, 175, 120, 355)
                        CharacterVisualDirection.UP ->
                            Rect(1370, 175, 120, 355)
                        CharacterVisualDirection.LEFT ->
                            Rect(530, 175, 190, 355)
                        CharacterVisualDirection.RIGHT ->
                            Rect(1150, 175, 190, 355)
                    }

            "WPN_CROSSBOW" ->
                RANGED_TEXTURE to
                    when (direction) {
                        CharacterVisualDirection.DOWN ->
                            Rect(45, 530, 275, 205)
                        CharacterVisualDirection.UP ->
                            Rect(890, 530, 275, 205)
                        CharacterVisualDirection.LEFT ->
                            Rect(490, 545, 380, 175)
                        CharacterVisualDirection.RIGHT ->
                            Rect(1160, 545, 360, 175)
                    }

            "WPN_SHIELD" ->
                RANGED_TEXTURE to
                    when (direction) {
                        CharacterVisualDirection.DOWN ->
                            Rect(135, 724, 190, 275)
                        CharacterVisualDirection.LEFT ->
                            Rect(408, 724, 180, 275)
                        CharacterVisualDirection.RIGHT ->
                            Rect(675, 724, 190, 275)
                        CharacterVisualDirection.UP ->
                            Rect(1180, 724, 190, 275)
                    }

            else ->
                null
        }
    }

    private fun meleeEffectFrame(
        id: String,
        direction:
        CharacterVisualDirection
    ): Pair<String, Rect>? {
        return when (id) {
            "WPN_DOUBLE_SWORDS" ->
                MELEE_TEXTURE to
                    when (direction) {
                        CharacterVisualDirection.DOWN ->
                            Rect(845, 10, 220, 275)
                        CharacterVisualDirection.UP ->
                            Rect(1050, 10, 220, 275)
                        CharacterVisualDirection.LEFT ->
                            Rect(1180, 10, 340, 275)
                        CharacterVisualDirection.RIGHT ->
                            Rect(820, 10, 340, 275)
                    }

            "WPN_LONGSWORD",
            "WPN_DAGGER" ->
                MELEE_TEXTURE to
                    when (direction) {
                        CharacterVisualDirection.DOWN ->
                            Rect(850, 300, 620, 285)
                        CharacterVisualDirection.UP ->
                            Rect(850, 300, 620, 285)
                        CharacterVisualDirection.LEFT ->
                            Rect(850, 300, 620, 285)
                        CharacterVisualDirection.RIGHT ->
                            Rect(850, 300, 620, 285)
                    }

            "WPN_LANCE" ->
                MELEE_TEXTURE to
                    when (direction) {
                        CharacterVisualDirection.DOWN ->
                            Rect(840, 690, 680, 230)
                        CharacterVisualDirection.UP ->
                            Rect(840, 690, 680, 230)
                        CharacterVisualDirection.LEFT ->
                            Rect(840, 690, 680, 230)
                        CharacterVisualDirection.RIGHT ->
                            Rect(840, 690, 680, 230)
                    }

            else ->
                null
        }
    }

    private fun shieldEffectFrame(
        direction:
        CharacterVisualDirection
    ): Rect {
        return when (direction) {
            CharacterVisualDirection.DOWN ->
                Rect(618, 0, 315, 365)
            CharacterVisualDirection.UP ->
                Rect(610, 650, 315, 374)
            CharacterVisualDirection.LEFT ->
                Rect(0, 350, 540, 285)
            CharacterVisualDirection.RIGHT ->
                Rect(995, 365, 541, 285)
        }
    }

    private fun drawRegion(
        texturePath: String,
        rect: Rect,
        centerX: Float,
        centerY: Float,
        worldHeight: Float,
        camera: OrthographicCamera,
        alpha: Float = 1f,
        tint: Float = 1f
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
            drawWidth,
            drawHeight
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
