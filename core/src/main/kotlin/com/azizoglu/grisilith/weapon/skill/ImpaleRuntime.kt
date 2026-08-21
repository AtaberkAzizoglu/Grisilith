package com.azizoglu.grisilith.weapon.skill

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.weapon.runtime.WeaponInstance
import com.azizoglu.grisilith.weapon.runtime.WeaponRuntime
import com.badlogic.gdx.math.Vector2
import kotlin.math.sqrt

/*
 * Reworked Lance Level 5 active:
 *
 * - no corpse dragging
 * - no bonus gold
 * - no chest rewards
 * - 7 second duration (owned by WeaponAbilityRuntime)
 * - damage grows only while travelling on one perfectly straight line
 * - zero directional tolerance: any direction change starts a new line
 * - bonus applies only to the Lance that activated Impale
 * - only targets inside the forward 60 degree cone receive the bonus
 */
class ImpaleRuntime {

    private var owningWeaponRuntime:
        WeaponRuntime? =
        null

    private val previousPlayerCenter =
        Vector2()

    /*
     * Raw direction vector from the FIRST moving frame of the current
     * straight-line segment. It is intentionally not quantized.
     */
    private val straightDirection =
        Vector2()

    private var hasStraightDirection =
        false

    private var straightDistance =
        0f

    fun tryBegin(
        weaponRuntime: WeaponRuntime,
        player: Player
    ): Boolean {
        if (
            weaponRuntime.abilityState !=
            WeaponAbilityState.READY
        ) {
            return false
        }

        val activated =
            weaponRuntime
                .tryActivateAbility()

        if (!activated) {
            return false
        }

        owningWeaponRuntime =
            weaponRuntime

        previousPlayerCenter.set(
            player.centerX,
            player.centerY
        )

        straightDirection.setZero()
        hasStraightDirection = false
        straightDistance = 0f

        return true
    }

    fun update(
        player: Player
    ) {
        val runtime =
            owningWeaponRuntime
                ?: return

        if (
            runtime.abilityState !=
            WeaponAbilityState.ACTIVE
        ) {
            clearTracking()
            return
        }

        val currentX =
            player.centerX

        val currentY =
            player.centerY

        val moveX =
            currentX -
                previousPlayerCenter.x

        val moveY =
            currentY -
                previousPlayerCenter.y

        previousPlayerCenter.set(
            currentX,
            currentY
        )

        val distanceSquared =
            moveX * moveX +
                moveY * moveY

        if (
            distanceSquared <=
            0f
        ) {
            /*
             * Stopping does not erase already-earned momentum.
             * Only changing direction breaks the straight-line chain.
             */
            return
        }

        val segmentDistance =
            sqrt(
                distanceSquared
            )

        if (!hasStraightDirection) {
            straightDirection.set(
                moveX,
                moveY
            )

            hasStraightDirection = true

            straightDistance +=
                segmentDistance

            return
        }

        /*
         * ZERO TOLERANCE:
         * For two 2D vectors to point along the exact same straight line:
         * cross product must be exactly 0 and dot product must be positive.
         *
         * No angular epsilon is intentionally used.
         */
        val cross =
            straightDirection.x *
                moveY -
                straightDirection.y *
                moveX

        val dot =
            straightDirection.x *
                moveX +
                straightDirection.y *
                moveY

        val continuesExactLine =
            cross ==
                0f &&
                dot >
                0f

        if (continuesExactLine) {
            straightDistance +=
                segmentDistance
        } else {
            /*
             * Direction changed. The old charge is lost immediately and the
             * current movement becomes the first segment of a new line.
             */
            straightDirection.set(
                moveX,
                moveY
            )

            straightDistance =
                segmentDistance
        }
    }

    fun damageMultiplierFor(
        weapon: WeaponInstance,
        player: Player,
        enemy: Enemy
    ): Float {
        val runtime =
            owningWeaponRuntime
                ?: return 1f

        if (
            runtime.abilityState !=
            WeaponAbilityState.ACTIVE ||
            runtime.weapon !==
            weapon ||
            !hasStraightDirection
        ) {
            return 1f
        }

        if (
            !isInsideForwardCone(
                player =
                    player,

                enemy =
                    enemy
            )
        ) {
            return 1f
        }

        return when {
            straightDistance <
                150f ->
                1f

            straightDistance <
                400f ->
                1.25f

            straightDistance <
                800f ->
                1.50f

            straightDistance <
                1500f ->
                1.75f

            else ->
                2f
        }
    }

    private fun isInsideForwardCone(
        player: Player,
        enemy: Enemy
    ): Boolean {
        val directionLengthSquared =
            straightDirection.len2()

        if (
            directionLengthSquared <=
            0f
        ) {
            return false
        }

        val toTargetX =
            enemy.centerX -
                player.centerX

        val toTargetY =
            enemy.centerY -
                player.centerY

        val targetLengthSquared =
            toTargetX * toTargetX +
                toTargetY * toTargetY

        if (
            targetLengthSquared <=
            0f
        ) {
            return true
        }

        val dot =
            straightDirection.x *
                toTargetX +
                straightDirection.y *
                toTargetY

        if (
            dot <=
            0f
        ) {
            return false
        }

        val normalizedDot =
            dot /
                sqrt(
                    directionLengthSquared *
                        targetLengthSquared
                )

        /*
         * Total cone angle = 60 degrees -> 30 degrees on each side.
         * cos(30 degrees) = 0.8660254...
         */
        return normalizedDot >=
            COS_HALF_IMPALE_CONE
    }

    fun cancel() {
        owningWeaponRuntime
            ?.let {
                    runtime ->

                if (
                    runtime.abilityState ==
                    WeaponAbilityState.ACTIVE
                ) {
                    runtime
                        .endActiveAbilityEarly()
                }
            }

        clearTracking()
    }

    fun reset() {
        clearTracking()
    }

    private fun clearTracking() {
        owningWeaponRuntime =
            null

        previousPlayerCenter.setZero()
        straightDirection.setZero()
        hasStraightDirection = false
        straightDistance = 0f
    }

    companion object {
        private const val COS_HALF_IMPALE_CONE =
            0.8660254f
    }
}
