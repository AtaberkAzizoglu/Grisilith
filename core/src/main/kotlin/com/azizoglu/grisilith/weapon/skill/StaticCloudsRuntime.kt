package com.azizoglu.grisilith.weapon.skill

import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.weapon.skill.area.PersistentArea
import com.azizoglu.grisilith.weapon.skill.area.PersistentAreaAnchor
import com.azizoglu.grisilith.weapon.skill.area.PersistentAreaManager
import com.azizoglu.grisilith.weapon.skill.area.PersistentAreaRuntime
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.azizoglu.grisilith.enemy.Enemy

class StaticCloudsRuntime(
    areaManager:
    PersistentAreaManager
) {

    private val areaRuntime:
        PersistentAreaRuntime =
        areaManager.runtimeFor(
            AREA_ID
        )

    private var visualRotationDegrees =
        0f

    private var lightningFlashRemainingSeconds =
        0f

    private val lightningTarget =
        Vector2()

    val isActive:
        Boolean
        get() =
            areaRuntime.isActive

    fun tryBegin(
        player: Player
    ): Boolean {
        return areaRuntime.begin(
            PersistentArea(
                id =
                    AREA_ID,

                center =
                    Vector2(
                        player.centerX,
                        player.centerY
                    ),

                radius =
                    DefaultWeaponAbilities
                        .STATIC_CLOUDS_RADIUS,

                durationSeconds =
                    DefaultWeaponAbilities
                        .STATIC_CLOUDS_DURATION_SECONDS,

                tickIntervalSeconds =
                    DefaultWeaponAbilities
                        .STATIC_CLOUDS_TICK_SECONDS,

                anchor =
                    PersistentAreaAnchor
                        .FOLLOW_PLAYER
            )
        )
    }

    fun update(
        delta: Float,
        player: Player,
        enemies: List<Enemy>,
        onDamageTick: (
            enemy: Enemy,
            tickIntervalSeconds: Float
        ) -> Unit,
        onCompleted: () -> Unit
    ) {
        visualRotationDegrees =
            (
                visualRotationDegrees +
                    delta.coerceAtLeast(
                        0f
                    ) *
                    VISUAL_ROTATION_SPEED
                ) %
                360f

        lightningFlashRemainingSeconds =
            (
                lightningFlashRemainingSeconds -
                    delta.coerceAtLeast(
                        0f
                    )
                )
                .coerceAtLeast(
                    0f
                )

        areaRuntime.update(
            delta =
                delta,

            followCenterX =
                player.centerX,

            followCenterY =
                player.centerY,

            onFrame = {
                    _,
                    _ ->
            },

            onTick = {
                    area ->

                val livingTargets =
                    enemies.filter {
                            enemy ->

                        !enemy.isDead &&
                            area.contains(
                                enemy.centerX,
                                enemy.centerY
                            )
                    }

                livingTargets.forEach {
                        enemy ->

                    onDamageTick(
                        enemy,
                        area.tickIntervalSeconds
                    )
                }

                livingTargets
                    .randomOrNull()
                    ?.let {
                            target ->

                        lightningTarget.set(
                            target.centerX,
                            target.centerY
                        )

                        lightningFlashRemainingSeconds =
                            LIGHTNING_FLASH_SECONDS
                    }
            },

            onCompleted =
                onCompleted
        )
    }

    fun render(
        shapeRenderer: ShapeRenderer
    ) {
        val area =
            areaRuntime.area
                ?: return

        val centerX =
            area.center.x

        val centerY =
            area.center.y

        val radius =
            area.radius

        shapeRenderer.color =
            Color(
                0.30f,
                0.28f,
                0.42f,
                0.30f
            )

        shapeRenderer.circle(
            centerX,
            centerY,
            radius,
            64
        )

        shapeRenderer.color =
            Color(
                0.50f,
                0.50f,
                0.66f,
                0.75f
            )

        repeat(
            CLOUD_PARTICLE_COUNT
        ) {
                index ->

            val angle =
                visualRotationDegrees +
                    index *
                    (
                        360f /
                            CLOUD_PARTICLE_COUNT
                        )

            val orbitRadius =
                radius *
                    (
                        0.34f +
                            0.08f *
                            (
                                index %
                                    3
                                )
                        )

            val particleX =
                centerX +
                    MathUtils.cosDeg(
                        angle
                    ) *
                    orbitRadius

            val particleY =
                centerY +
                    MathUtils.sinDeg(
                        angle
                    ) *
                    orbitRadius

            shapeRenderer.circle(
                particleX,
                particleY,
                10f +
                    (
                        index %
                            2
                        ) *
                    3f,
                20
            )
        }

        shapeRenderer.color =
            Color(
                0.64f,
                0.72f,
                0.94f,
                0.90f
            )

        repeat(
            LIGHTNING_PLACEHOLDER_COUNT
        ) {
                index ->

            val angle =
                visualRotationDegrees *
                    -1f +
                    index *
                    (
                        360f /
                            LIGHTNING_PLACEHOLDER_COUNT
                        )

            val innerRadius =
                radius *
                    0.18f

            val outerRadius =
                radius *
                    0.70f

            shapeRenderer.rectLine(
                centerX +
                    MathUtils.cosDeg(
                        angle
                    ) *
                    innerRadius,

                centerY +
                    MathUtils.sinDeg(
                        angle
                    ) *
                    innerRadius,

                centerX +
                    MathUtils.cosDeg(
                        angle +
                            18f
                    ) *
                    outerRadius,

                centerY +
                    MathUtils.sinDeg(
                        angle +
                            18f
                    ) *
                    outerRadius,

                2f
            )
        }

        if (
            lightningFlashRemainingSeconds >
            0f
        ) {
            val alpha =
                (
                    lightningFlashRemainingSeconds /
                        LIGHTNING_FLASH_SECONDS
                    )
                    .coerceIn(
                        0f,
                        1f
                    )

            shapeRenderer.color =
                Color(
                    0.82f,
                    0.92f,
                    1f,
                    alpha
                )

            val cloudX =
                centerX +
                    MathUtils.cosDeg(
                        visualRotationDegrees
                    ) *
                    radius *
                    0.28f

            val cloudY =
                centerY +
                    MathUtils.sinDeg(
                        visualRotationDegrees
                    ) *
                    radius *
                    0.28f

            val middleX =
                (
                    cloudX +
                        lightningTarget.x
                    ) /
                    2f +
                    12f

            val middleY =
                (
                    cloudY +
                        lightningTarget.y
                    ) /
                    2f -
                    8f

            shapeRenderer.rectLine(
                cloudX,
                cloudY,
                middleX,
                middleY,
                4f
            )

            shapeRenderer.rectLine(
                middleX,
                middleY,
                lightningTarget.x,
                lightningTarget.y,
                4f
            )
        }
    }

    fun clear() {
        areaRuntime.clear()

        visualRotationDegrees =
            0f

        lightningFlashRemainingSeconds =
            0f
    }

    companion object {
        private const val AREA_ID =
            "STATIC_CLOUDS_PLAYER_AURA"

        private const val VISUAL_ROTATION_SPEED =
            34f

        private const val CLOUD_PARTICLE_COUNT =
            9

        private const val LIGHTNING_PLACEHOLDER_COUNT =
            4

        private const val LIGHTNING_FLASH_SECONDS =
            0.16f
    }
}
