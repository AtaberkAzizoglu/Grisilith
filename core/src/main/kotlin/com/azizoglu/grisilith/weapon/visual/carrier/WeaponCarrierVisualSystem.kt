package com.azizoglu.grisilith.weapon.visual.carrier

import com.azizoglu.grisilith.character.visual.CharacterVisualDirection
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.weapon.runtime.WeaponInstance
import com.azizoglu.grisilith.weapon.runtime.WeaponInventory
import com.azizoglu.grisilith.weapon.runtime.WeaponRuntimeManager
import kotlin.math.abs
import kotlin.math.sqrt

class WeaponCarrierVisualSystem(
    private val inventory: WeaponInventory
) {

    enum class MinionType {
        SOLDIER,
        SCOUT,
        MAGE_APPRENTICE
    }

    enum class MinionVisualDirection {
        SOUTH,
        SOUTH_WEST,
        WEST,
        NORTH_WEST,
        NORTH,
        NORTH_EAST,
        EAST,
        SOUTH_EAST
    }

    data class CarrierSnapshot(
        val slotIndex: Int,
        val weapon: WeaponInstance,
        val centerX: Float,
        val centerY: Float,
        val direction: CharacterVisualDirection,
        val minionType: MinionType?,
        val minionVisualDirection: MinionVisualDirection?,
        val minionIsMoving: Boolean,
        val minionAnimationTime: Float,
        val attackProgress: Float,
        val attackActive: Boolean,
        val attackTargetCenterX: Float,
        val attackTargetCenterY: Float
    ) {
        val depthY: Float
            get() = centerY - 16f
    }

    private data class MinionState(
        var x: Float,
        var y: Float,
        var direction: CharacterVisualDirection,
        var visualDirection: MinionVisualDirection,
        val type: MinionType,
        var isMoving: Boolean = false,
        var animationTime: Float = 0f,
        var wanderTimer: Float = 0f,
        var wanderStep: Int = 0,
        var wanderOffsetX: Float = 0f,
        var wanderOffsetY: Float = 0f
    )

    private data class AttackState(
        var elapsed: Float = 999f,
        var duration: Float = 0.12f,
        var targetCenterX: Float = 0f,
        var targetCenterY: Float = 0f
    )

    private val minions =
        mutableMapOf<Int, MinionState>()

    private val attackStates =
        mutableMapOf<String, AttackState>()

    fun update(
        delta: Float,
        player: Player,
        runtimeManager: WeaponRuntimeManager
    ) {
        val safeDelta =
            delta.coerceAtLeast(0f)

        attackStates.values
            .forEach {
                    state ->

                state.elapsed +=
                    safeDelta
            }

        val activeMinionSlots =
            inventory.weaponSlots
                .drop(1)
                .filter { !it.isEmpty }
                .map { it.index }
                .toSet()

        minions.keys
            .toList()
            .filter { it !in activeMinionSlots }
            .forEach { minions.remove(it) }

        inventory.weaponSlots
            .drop(1)
            .forEach { slot ->
                val weapon =
                    slot.weapon
                        ?: return@forEach

                val anchor =
                    anchorFor(
                        slotIndex = slot.index,
                        playerCenterX = player.centerX,
                        playerCenterY = player.centerY
                    )

                val state =
                    minions.getOrPut(
                        slot.index
                    ) {
                        MinionState(
                            x = player.centerX,
                            y = player.centerY,
                            direction =
                                player.currentVisualState.direction,
                            visualDirection =
                                minionDirectionFromCharacterDirection(
                                    player.currentVisualState.direction
                                ),
                            type =
                                typeFor(slot.index)
                        )
                    }

                updateWanderTarget(
                    state = state,
                    slotIndex = slot.index,
                    delta = safeDelta
                )

                val anchorDx =
                    anchor.first - state.x

                val anchorDy =
                    anchor.second - state.y

                val distanceFromAnchor =
                    sqrt(
                        anchorDx * anchorDx +
                            anchorDy * anchorDy
                    )

                /*
                 * Each carrier owns a preferred formation point around the
                 * player. Inside that local area it is free to wander, like
                 * a lightweight ambient NPC. If it falls too far away it
                 * stops wandering and walks directly back to its formation.
                 */
                val targetX =
                    if (
                        distanceFromAnchor >
                        RETURN_TO_FORMATION_DISTANCE
                    ) {
                        anchor.first
                    } else {
                        anchor.first +
                            state.wanderOffsetX
                    }

                val targetY =
                    if (
                        distanceFromAnchor >
                        RETURN_TO_FORMATION_DISTANCE
                    ) {
                        anchor.second
                    } else {
                        anchor.second +
                            state.wanderOffsetY
                    }

                moveState(
                    state = state,
                    targetX = targetX,
                    targetY = targetY,
                    movementSpeed =
                        player
                            .followerReferenceMovementSpeed *
                            MINION_PLAYER_SPEED_RATIO,
                    delta = safeDelta
                )

                if (state.isMoving) {
                    state.animationTime += safeDelta
                } else {
                    state.animationTime = 0f
                }

            }

        inventory.weaponAt(0)
            ?.let { weapon ->
            }

        val equippedRuntimeIds =
            inventory.allWeapons()
                .map { it.runtimeId }
                .toSet()

        attackStates.keys
            .toList()
            .filter { it !in equippedRuntimeIds }
            .forEach { attackStates.remove(it) }
    }

    fun snapshots(
        player: Player
    ): List<CarrierSnapshot> {
        val result =
            mutableListOf<CarrierSnapshot>()

        inventory.weaponAt(0)
            ?.let { weapon ->
                result +=
                    CarrierSnapshot(
                        slotIndex = 0,
                        weapon = weapon,
                        centerX = player.centerX,
                        centerY = player.centerY,
                        direction =
                            player.currentVisualState.direction,
                        minionType = null,
                        minionVisualDirection = null,
                        minionIsMoving = false,
                        minionAnimationTime = 0f,
                        attackProgress =
                            attackProgress(weapon),
                        attackActive =
                            attackIsActive(weapon),
                        attackTargetCenterX =
                            attackStates[
                                weapon.runtimeId
                            ]
                                ?.targetCenterX
                                ?: player.centerX,
                        attackTargetCenterY =
                            attackStates[
                                weapon.runtimeId
                            ]
                                ?.targetCenterY
                                ?: player.centerY
                    )
            }

        inventory.weaponSlots
            .drop(1)
            .forEach { slot ->
                val weapon =
                    slot.weapon
                        ?: return@forEach

                val minion =
                    minions[slot.index]
                        ?: return@forEach

                result +=
                    CarrierSnapshot(
                        slotIndex = slot.index,
                        weapon = weapon,
                        centerX = minion.x,
                        centerY = minion.y,
                        direction = minion.direction,
                        minionType = minion.type,
                        minionVisualDirection =
                            minion.visualDirection,
                        minionIsMoving =
                            minion.isMoving,
                        minionAnimationTime =
                            minion.animationTime,
                        attackProgress =
                            attackProgress(weapon),
                        attackActive =
                            attackIsActive(weapon),
                        attackTargetCenterX =
                            attackStates[
                                weapon.runtimeId
                            ]
                                ?.targetCenterX
                                ?: player.centerX,
                        attackTargetCenterY =
                            attackStates[
                                weapon.runtimeId
                            ]
                                ?.targetCenterY
                                ?: player.centerY
                    )
            }

        return result
    }

    fun notifyNormalAttack(
        weapon: WeaponInstance,
        attacksPerSecond: Float,
        targetCenterX: Float,
        targetCenterY: Float
    ) {
        val safeAttacksPerSecond =
            attacksPerSecond
                .coerceAtLeast(
                    0.01f
                )

        val attackInterval =
            1f /
                safeAttacksPerSecond

        val state =
            attackStates.getOrPut(
                weapon.runtimeId
            ) {
                AttackState()
            }

        state.duration =
            (
                attackInterval *
                    0.78f
                )
                .coerceAtMost(
                    0.30f
                )
                .coerceAtLeast(
                    0.025f
                )

        /*
         * One visual state per weapon. A new real attack restarts it;
         * nothing is queued or accumulated.
         */
        state.targetCenterX =
            targetCenterX
        state.targetCenterY =
            targetCenterY
        state.elapsed =
            0f
    }

    private fun attackIsActive(
        weapon: WeaponInstance
    ): Boolean {
        val state =
            attackStates[
                weapon.runtimeId
            ]
                ?: return false

        return state.elapsed <
            state.duration
    }

    private fun attackProgress(
        weapon: WeaponInstance
    ): Float {
        val state =
            attackStates[
                weapon.runtimeId
            ]
                ?: return 1f

        if (
            state.duration <=
            0f
        ) {
            return 1f
        }

        return (
            state.elapsed /
                state.duration
            )
            .coerceIn(
                0f,
                1f
            )
    }

    private fun moveState(
        state: MinionState,
        targetX: Float,
        targetY: Float,
        movementSpeed: Float,
        delta: Float
    ) {
        val dx =
            targetX - state.x

        val dy =
            targetY - state.y

        val distance =
            sqrt(
                dx * dx +
                    dy * dy
            )

        if (
            distance <
            MINION_STOP_DISTANCE
        ) {
            state.isMoving = false
            return
        }

        state.isMoving = true

        val moveDistance =
            (
                movementSpeed
                    .coerceAtLeast(
                        1f
                    ) *
                    delta
                )
                .coerceAtMost(
                    distance
                )

        val nx =
            dx / distance

        val ny =
            dy / distance

        state.x +=
            nx * moveDistance

        state.y +=
            ny * moveDistance

        state.visualDirection =
            resolveEightDirection(
                x = nx,
                y = ny,
                previous =
                    state.visualDirection
            )

        state.direction =
            collapseToFourDirections(
                state.visualDirection
            )
    }

    private fun updateWanderTarget(
        state: MinionState,
        slotIndex: Int,
        delta: Float
    ) {
        state.wanderTimer -=
            delta

        if (
            state.wanderTimer >
            0f
        ) {
            return
        }

        state.wanderStep += 1

        /*
         * Deterministic pseudo-random walk points.
         * No shared RNG means minions never retarget on the exact same beat.
         */
        val angle =
            (
                state.wanderStep *
                    2.399963f +
                    slotIndex *
                    1.173f
                )

        val radiusWave =
            (
                kotlin.math.sin(
                    state.wanderStep *
                        1.731f +
                        slotIndex
                ) *
                    0.5f +
                    0.5f
                )

        val radius =
            MINION_WANDER_MIN_RADIUS +
                (
                    MINION_WANDER_MAX_RADIUS -
                        MINION_WANDER_MIN_RADIUS
                    ) *
                radiusWave

        state.wanderOffsetX =
            kotlin.math.cos(
                angle
            ) *
                radius

        state.wanderOffsetY =
            kotlin.math.sin(
                angle
            ) *
                radius *
                0.72f

        state.wanderTimer =
            MINION_WANDER_MIN_SECONDS +
                (
                    (
                        state.wanderStep +
                            slotIndex
                        ) %
                        5
                    ) *
                0.23f
    }

    private fun resolveEightDirection(
        x: Float,
        y: Float,
        previous: MinionVisualDirection
    ): MinionVisualDirection {
        val ax = abs(x)
        val ay = abs(y)

        if (
            ax < 0.05f &&
            ay < 0.05f
        ) {
            return previous
        }

        /*
         * Keep diagonals for a fairly wide band.
         * This makes the minions use the supplied 8-direction artwork instead
         * of snapping to only four directions.
         */
        val diagonal =
            ax > ay * 0.45f &&
                ay > ax * 0.45f

        if (diagonal) {
            return when {
                x < 0f && y < 0f ->
                    MinionVisualDirection.SOUTH_WEST

                x < 0f && y >= 0f ->
                    MinionVisualDirection.NORTH_WEST

                x >= 0f && y < 0f ->
                    MinionVisualDirection.SOUTH_EAST

                else ->
                    MinionVisualDirection.NORTH_EAST
            }
        }

        return if (ax > ay) {
            if (x < 0f) {
                MinionVisualDirection.WEST
            } else {
                MinionVisualDirection.EAST
            }
        } else {
            if (y < 0f) {
                MinionVisualDirection.SOUTH
            } else {
                MinionVisualDirection.NORTH
            }
        }
    }

    private fun collapseToFourDirections(
        direction: MinionVisualDirection
    ): CharacterVisualDirection {
        return when (direction) {
            MinionVisualDirection.SOUTH,
            MinionVisualDirection.SOUTH_WEST,
            MinionVisualDirection.SOUTH_EAST ->
                CharacterVisualDirection.DOWN

            MinionVisualDirection.NORTH,
            MinionVisualDirection.NORTH_WEST,
            MinionVisualDirection.NORTH_EAST ->
                CharacterVisualDirection.UP

            MinionVisualDirection.WEST ->
                CharacterVisualDirection.LEFT

            MinionVisualDirection.EAST ->
                CharacterVisualDirection.RIGHT
        }
    }

    private fun minionDirectionFromCharacterDirection(
        direction: CharacterVisualDirection
    ): MinionVisualDirection {
        return when (direction) {
            CharacterVisualDirection.DOWN ->
                MinionVisualDirection.SOUTH

            CharacterVisualDirection.UP ->
                MinionVisualDirection.NORTH

            CharacterVisualDirection.LEFT ->
                MinionVisualDirection.WEST

            CharacterVisualDirection.RIGHT ->
                MinionVisualDirection.EAST
        }
    }

    private fun typeFor(
        slotIndex: Int
    ): MinionType {
        return when (
            (slotIndex - 1)
                .mod(3)
        ) {
            0 -> MinionType.SOLDIER
            1 -> MinionType.SCOUT
            else -> MinionType.MAGE_APPRENTICE
        }
    }

    private fun anchorFor(
        slotIndex: Int,
        playerCenterX: Float,
        playerCenterY: Float
    ): Pair<Float, Float> {
        val offset =
            when (
                slotIndex
            ) {
                1 -> -54f to -18f
                2 -> 54f to -18f
                3 -> -70f to 38f
                4 -> 70f to 38f
                else -> 0f to 68f
            }

        return (
            playerCenterX +
                offset.first
            ) to
            (
                playerCenterY +
                    offset.second
                )
    }

    companion object {
        private const val MINION_STOP_DISTANCE =
            2.5f

        private const val MINION_PLAYER_SPEED_RATIO =
            0.90f

        private const val RETURN_TO_FORMATION_DISTANCE =
            42f

        private const val MINION_WANDER_MIN_RADIUS =
            5f

        private const val MINION_WANDER_MAX_RADIUS =
            23f

        private const val MINION_WANDER_MIN_SECONDS =
            0.95f
    }
}
