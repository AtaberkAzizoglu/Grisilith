package com.azizoglu.grisilith.weapon.glyph

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.weapon.runtime.WeaponInstance
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

/**
 * Enhanced Chalk Dust cast flow.
 *
 * Permanent spell selection remains unchanged.
 * Drawing / recognition / trail damage are removed.
 *
 * Activation:
 *  - locks the cast to the player's current world position,
 *  - plays the selected spell's ground animation for 2.5 seconds,
 *  - keeps the final frame visible for the last 0.5 seconds,
 *  - then creates the normal GlyphCastRequest,
 *  - GlyphSpellRuntime remains responsible for the actual spell.
 */
class GlyphMotionRuntime(
    @Suppress("UNUSED_PARAMETER")
    private val playerStats: PlayerStats
) {
    enum class ActivationResult {
        STARTED,
        FINISHED,
        REJECTED
    }

    private val telegraphRenderer =
        ChalkDustCastTelegraphRenderer()

    private var sourceWeapon:
        WeaponInstance? =
        null

    private var castRemainingSeconds =
        0f

    private var sharedCooldownRemainingSeconds =
        0f

    private var pendingCastRequest:
        GlyphCastRequest? =
        null

    private var markerCenterX =
        0f

    private var markerCenterY =
        0f

    private var markerPositionCaptured =
        false

    val isDrawing: Boolean
        get() =
            sourceWeapon !=
                null

    val preparingWeapon: WeaponInstance? get() = sourceWeapon

    /*
     * Compatibility name retained because existing UI/runtime code may still
     * use hasVisibleTrail. It now means "cast telegraph is visible".
     */
    val hasVisibleTrail: Boolean
        get() =
            sourceWeapon !=
                null

    val cooldownRemainingSeconds: Float
        get() =
            sharedCooldownRemainingSeconds

    fun reduceCooldown(seconds: Float) {
        if (seconds <= 0f) return
        sharedCooldownRemainingSeconds =
            (sharedCooldownRemainingSeconds - seconds).coerceAtLeast(0f)
    }

    val remainingDrawingSeconds: Float
        get() =
            if (
                sourceWeapon !=
                null
            ) {
                castRemainingSeconds
            } else {
                0f
            }

    val telegraphProgress: Float
        get() =
            if (
                sourceWeapon ==
                null
            ) {
                0f
            } else {
                (
                    1f -
                        castRemainingSeconds /
                        TELEGRAPH_SECONDS
                    )
                    .coerceIn(
                        0f,
                        1f
                    )
            }

    private val telegraphElapsedSeconds:
        Float
        get() =
            (
                TELEGRAPH_SECONDS -
                    castRemainingSeconds
                )
                .coerceIn(
                    0f,
                    TELEGRAPH_SECONDS
                )

    fun handleActivation(
        weapon: WeaponInstance
    ): ActivationResult {
        if (
            weapon.chalkDustSpellChoice ==
            null ||
            sourceWeapon !=
            null ||
            sharedCooldownRemainingSeconds >
            0f
        ) {
            return ActivationResult.REJECTED
        }

        sourceWeapon =
            weapon

        castRemainingSeconds =
            TELEGRAPH_SECONDS

        markerPositionCaptured =
            false

        pendingCastRequest =
            null

        return ActivationResult.STARTED
    }

    @Suppress("UNUSED_PARAMETER")
    fun update(
        delta: Float,
        playerCenterX: Float,
        playerCenterY: Float,
        enemies: List<Enemy>,
        onDamageDealt: (
            damage: Int,
            sourceName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit,
        onDrawingResolved: () -> Unit
    ) {
        if (
            delta <=
            0f
        ) {
            return
        }

        sharedCooldownRemainingSeconds =
            (
                sharedCooldownRemainingSeconds -
                    delta
                )
                .coerceAtLeast(
                    0f
                )

        val weapon =
            sourceWeapon
                ?: return

        if (
            !markerPositionCaptured
        ) {
            markerCenterX =
                playerCenterX

            markerCenterY =
                playerCenterY

            markerPositionCaptured =
                true
        }

        castRemainingSeconds =
            (
                castRemainingSeconds -
                    delta
                )
                .coerceAtLeast(
                    0f
                )

        if (
            castRemainingSeconds >
            0f
        ) {
            return
        }

        val glyphType =
            weapon
                .chalkDustSpellChoice
                ?.glyphType

        if (
            glyphType !=
            null
        ) {
            pendingCastRequest =
                GlyphCastRequest(
                    glyphType =
                        glyphType,

                    centerX =
                        markerCenterX,

                    centerY =
                        markerCenterY,

                    sourceWeapon =
                        weapon
                )

            sharedCooldownRemainingSeconds =
                GlyphCastingConfig
                    .SUCCESS_COOLDOWN_SECONDS
        }

        sourceWeapon =
            null

        markerPositionCaptured =
            false

        onDrawingResolved()
    }

    fun render(
        shapeRenderer: ShapeRenderer
    ) {
        val glyphType =
            sourceWeapon
                ?.chalkDustSpellChoice
                ?.glyphType
                ?: return

        if (
            !markerPositionCaptured
        ) {
            return
        }

        telegraphRenderer.render(
            shapeRenderer =
                shapeRenderer,

            glyphType =
                glyphType,

            centerX =
                markerCenterX,

            centerY =
                markerCenterY,

            elapsedSeconds =
                telegraphElapsedSeconds
        )
    }

    /*
     * Compatibility retained for existing HUD code.
     * Recognition feedback no longer exists.
     */
    fun feedback():
        GlyphFeedback? =
        null

    fun consumePendingCastRequest():
        GlyphCastRequest? {

        val request =
            pendingCastRequest

        pendingCastRequest =
            null

        return request
    }

    fun clear() {
        sourceWeapon =
            null

        castRemainingSeconds =
            0f

        sharedCooldownRemainingSeconds =
            0f

        pendingCastRequest =
            null

        markerPositionCaptured =
            false
    }

    fun cancelActiveDrawing() {
        sourceWeapon =
            null

        castRemainingSeconds =
            0f

        pendingCastRequest =
            null

        markerPositionCaptured =
            false
    }

    companion object {
        private const val TELEGRAPH_SECONDS =
            2.5f
    }
}
