package com.azizoglu.grisilith.weapon.glyph

import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyDamageContext
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.weapon.runtime.WeaponInstance
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import kotlin.math.ceil
import kotlin.math.roundToInt

class GlyphMotionRuntime(
    private val playerStats: PlayerStats
) {

    enum class ActivationResult {
        STARTED,
        FINISHED,
        REJECTED
    }

    private val recognizer =
        GlyphRecognizer()

    private val points =
        mutableListOf<Vector2>()

    private var sourceWeapon:
        WeaponInstance? =
        null

    private var elapsedSessionSeconds =
        0f

    private var elapsedDrawingSeconds =
        0f

    private var trailTickAccumulator =
        0f

    private var wisdomSnapshot =
        0

    private var drawing =
        false

    private var sessionVisible =
        false

    private var sharedCooldownRemainingSeconds =
        0f

    private var feedbackDisplayRemainingSeconds =
        0f

    private var lastRecognitionResult:
        GlyphRecognitionResult? =
        null

    private var pendingCastRequest:
        GlyphCastRequest? =
        null

    val isDrawing: Boolean
        get() =
            drawing

    val hasVisibleTrail: Boolean
        get() =
            sessionVisible &&
                points.size >=
                2

    val cooldownRemainingSeconds: Float
        get() =
            sharedCooldownRemainingSeconds

    val remainingDrawingSeconds: Float
        get() =
            if (
                drawing
            ) {
                (
                    GlyphCastingConfig
                        .DRAW_DURATION_SECONDS -
                        elapsedSessionSeconds
                    )
                    .coerceAtLeast(
                        0f
                    )
            } else {
                0f
            }

    fun handleActivation(
        weapon: WeaponInstance
    ): ActivationResult {

        if (
            drawing &&
            sourceWeapon ===
            weapon
        ) {
            finishDrawing()

            return ActivationResult
                .FINISHED
        }

        if (
            weapon.chalkDustSpellChoice == null ||
            drawing ||
            sessionVisible ||
            sharedCooldownRemainingSeconds >
            0f
        ) {
            return ActivationResult
                .REJECTED
        }

        sourceWeapon =
            weapon

        points.clear()

        elapsedSessionSeconds =
            0f

        elapsedDrawingSeconds =
            0f

        trailTickAccumulator =
            0f

        wisdomSnapshot =
            playerStats
                .totalScore(
                    StatType.WISDOM
                )
                .coerceAtLeast(
                    0
                )

        drawing =
            true

        sessionVisible =
            true

        lastRecognitionResult =
            null

        pendingCastRequest =
            null

        return ActivationResult
            .STARTED
    }

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

        feedbackDisplayRemainingSeconds =
            (
                feedbackDisplayRemainingSeconds -
                    delta
                )
                .coerceAtLeast(
                    0f
                )

        if (
            !sessionVisible
        ) {
            return
        }

        elapsedSessionSeconds +=
            delta

        if (
            drawing
        ) {
            elapsedDrawingSeconds +=
                delta

            recordPoint(
                playerCenterX,
                playerCenterY
            )
        }

        trailTickAccumulator +=
            delta

        while (
            trailTickAccumulator >=
            GlyphCastingConfig
                .TRAIL_DAMAGE_TICK_SECONDS
        ) {
            trailTickAccumulator -=
                GlyphCastingConfig
                    .TRAIL_DAMAGE_TICK_SECONDS

            applyTrailDamage(
                enemies =
                    enemies,

                onDamageDealt =
                    onDamageDealt,

                onEnemyKilled =
                    onEnemyKilled
            )
        }

        if (
            elapsedSessionSeconds >=
            GlyphCastingConfig
                .DRAW_DURATION_SECONDS
        ) {
            if (
                drawing
            ) {
                finishDrawing()
                onDrawingResolved()
            }

            clearTrail()
        }
    }

    fun render(
        shapeRenderer: ShapeRenderer
    ) {

        if (
            !hasVisibleTrail
        ) {
            return
        }

        shapeRenderer.color =
            Color(
                0.92f,
                0.90f,
                0.78f,
                0.42f
            )

        for (
        index in
        1 until points.size
        ) {
            shapeRenderer.rectLine(
                points[index - 1],
                points[index],
                TRAIL_WIDTH
            )
        }
    }

    fun feedback():
        GlyphFeedback? {

        if (
            feedbackDisplayRemainingSeconds <=
            0f
        ) {
            return null
        }

        val result =
            lastRecognitionResult
                ?: return null

        return GlyphFeedback(
            detectedName =
                result.glyphType
                    ?.displayName
                    ?: "Unknown",

            confidencePercent =
                result.confidencePercent,

            resultText =
                if (
                    result.accepted
                ) {
                    "Accepted"
                } else {
                    "Failed"
                },

            cooldownSeconds =
                ceil(
                    if (
                        result.accepted
                    ) {
                        GlyphCastingConfig
                            .SUCCESS_COOLDOWN_SECONDS
                    } else {
                        GlyphCastingConfig
                            .FAILURE_COOLDOWN_SECONDS
                    }
                )
                    .toInt(),

            remainingDisplaySeconds =
                feedbackDisplayRemainingSeconds
        )
    }

    fun consumePendingCastRequest():
        GlyphCastRequest? {

        val request =
            pendingCastRequest

        pendingCastRequest =
            null

        return request
    }

    fun clear() {

        points.clear()

        sourceWeapon =
            null

        elapsedSessionSeconds =
            0f

        elapsedDrawingSeconds =
            0f

        trailTickAccumulator =
            0f

        drawing =
            false

        sessionVisible =
            false

        lastRecognitionResult =
            null

        pendingCastRequest =
            null

        feedbackDisplayRemainingSeconds =
            0f

        sharedCooldownRemainingSeconds =
            0f
    }

    fun cancelActiveDrawing() {

        points.clear()

        sourceWeapon =
            null

        elapsedSessionSeconds =
            0f

        elapsedDrawingSeconds =
            0f

        trailTickAccumulator =
            0f

        drawing =
            false

        sessionVisible =
            false
    }

    private fun finishDrawing() {

        if (
            !drawing
        ) {
            return
        }

        drawing =
            false

        val result =
            if (
                elapsedDrawingSeconds <
                GlyphCastingConfig
                    .MINIMUM_DRAW_DURATION_SECONDS
            ) {
                GlyphRecognitionResult(
                    glyphType =
                        null,

                    confidence =
                        0f,

                    templateScore =
                        0f,

                    structuralScore =
                        0f,

                    accepted =
                        false,

                    reason =
                        "Drawing too short"
                )
            } else {
                val expectedGlyphType =
                    sourceWeapon
                        ?.chalkDustSpellChoice
                        ?.glyphType

                if (expectedGlyphType == null) {
                    GlyphRecognitionResult(
                        glyphType = null,
                        confidence = 0f,
                        templateScore = 0f,
                        structuralScore = 0f,
                        accepted = false,
                        reason = "No spell selected"
                    )
                } else {
                    recognizer.recognize(
                        rawPoints = points,
                        expectedGlyphType = expectedGlyphType
                    )
                }
            }

        lastRecognitionResult =
            result

        if (
            result.accepted &&
            result.glyphType !=
            null
        ) {
            val weapon =
                sourceWeapon

            if (
                weapon !=
                null
            ) {
                val center =
                    drawingCenter()

                pendingCastRequest =
                    GlyphCastRequest(
                        glyphType =
                            result.glyphType,

                        centerX =
                            center.x,

                        centerY =
                            center.y,

                        sourceWeapon =
                            weapon
                    )
            }
        }

        sharedCooldownRemainingSeconds =
            if (
                result.accepted
            ) {
                GlyphCastingConfig
                    .SUCCESS_COOLDOWN_SECONDS
            } else {
                GlyphCastingConfig
                    .FAILURE_COOLDOWN_SECONDS
            }

        feedbackDisplayRemainingSeconds =
            FEEDBACK_DISPLAY_SECONDS
    }

    private fun drawingCenter():
        Vector2 {

        if (
            points.isEmpty()
        ) {
            return Vector2()
        }

        val center =
            Vector2()

        points.forEach {
            center.add(
                it
            )
        }

        center.scl(
            1f /
                points.size
        )

        return center
    }

    private fun recordPoint(
        worldX: Float,
        worldY: Float
    ) {

        val point =
            Vector2(
                worldX,
                worldY
            )

        val previous =
            points.lastOrNull()

        if (
            previous == null ||
            previous.dst2(
                point
            ) >=
            MINIMUM_POINT_DISTANCE *
            MINIMUM_POINT_DISTANCE
        ) {
            points.add(
                point
            )
        }
    }

    private fun applyTrailDamage(
        enemies: List<Enemy>,
        onDamageDealt: (
            damage: Int,
            sourceName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
    ) {

        val weapon =
            sourceWeapon
                ?: return

        if (
            points.size <
            2
        ) {
            return
        }

        val requestedDamage =
            (
                wisdomSnapshot *
                    GlyphCastingConfig
                        .TRAIL_WISDOM_DAMAGE_MULTIPLIER
                )
                .roundToInt()
                .coerceAtLeast(
                    1
                )

        val sourceName =
            weapon.displayName +
                " — Glyph Trail"

        enemies
            .toList()
            .forEach {
                    enemy ->

                if (
                    enemy.isDead ||
                    !touchesTrail(
                        enemy.centerX,
                        enemy.centerY,
                        enemy.bounds.width /
                            2f
                    )
                ) {
                    return@forEach
                }

                val wasAlive =
                    !enemy.isDead

                val appliedDamage =
                    enemy.takeDamage(
                        amount =
                            requestedDamage,

                        damageContext =
                            EnemyDamageContext(
                                damageType =
                                    DamageType.AIR,

                                sourceWeaponRuntimeId =
                                    weapon.runtimeId,

                                sourceWeaponDefinitionId =
                                    weapon
                                        .definition
                                        .id
                                        .value,

                                sourceName =
                                    sourceName
                            )
                    )

                if (
                    appliedDamage >
                    0
                ) {
                    onDamageDealt(
                        appliedDamage,
                        sourceName
                    )
                }

                if (
                    wasAlive &&
                    enemy.isDead
                ) {
                    onEnemyKilled(
                        enemy
                    )
                }
            }
    }

    private fun touchesTrail(
        centerX: Float,
        centerY: Float,
        enemyRadius: Float
    ): Boolean {

        val allowedDistance =
            TRAIL_DAMAGE_RADIUS +
                enemyRadius

        for (
        index in
        1 until points.size
        ) {
            if (
                GlyphGeometry
                    .distanceToSegment(
                        pointX =
                            centerX,

                        pointY =
                            centerY,

                        start =
                            points[index - 1],

                        end =
                            points[index]
                    ) <=
                allowedDistance
            ) {
                return true
            }
        }

        return false
    }

    private fun clearTrail() {

        points.clear()

        sourceWeapon =
            null

        elapsedSessionSeconds =
            0f

        elapsedDrawingSeconds =
            0f

        trailTickAccumulator =
            0f

        drawing =
            false

        sessionVisible =
            false
    }

    companion object {

        private const val MINIMUM_POINT_DISTANCE =
            8f

        private const val TRAIL_WIDTH =
            5f

        private const val TRAIL_DAMAGE_RADIUS =
            10f

        private const val FEEDBACK_DISPLAY_SECONDS =
            4.5f
    }
}
