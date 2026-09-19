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

    private var visualTimeSeconds =
        0f

    private var lightningFlashRemainingSeconds =
        0f

    private val lightningTarget =
        Vector2()

    val isActive:
        Boolean
        get() =
            areaRuntime.isActive

    val visualCenterX: Float
        get() =
            areaRuntime.area
                ?.center
                ?.x
                ?: 0f

    val visualCenterY: Float
        get() =
            areaRuntime.area
                ?.center
                ?.y
                ?: 0f

    val visualRadius: Float
        get() =
            areaRuntime.area
                ?.radius
                ?: 0f

    val visualAnimationSeconds: Float
        get() =
            visualTimeSeconds

    fun tryBegin(
        player: Player,
        upgraded: Boolean
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
                    if (upgraded) {
                        DefaultWeaponAbilities.STATIC_CLOUDS_UPGRADED_DURATION_SECONDS
                    } else {
                        DefaultWeaponAbilities.STATIC_CLOUDS_DURATION_SECONDS
                    },

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

        visualTimeSeconds +=
            delta.coerceAtLeast(
                0f
            )

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

    @Suppress("UNUSED_PARAMETER")
    fun render(
        shapeRenderer: ShapeRenderer
    ) {
        // Sprite visual is rendered by SpellFocusEffectSpriteRenderer.
    }

    fun clear() {
        areaRuntime.clear()

        visualRotationDegrees =
            0f

        visualTimeSeconds =
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
