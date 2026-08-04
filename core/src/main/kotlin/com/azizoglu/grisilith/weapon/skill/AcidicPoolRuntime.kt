package com.azizoglu.grisilith.weapon.skill

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.weapon.skill.area.PersistentArea
import com.azizoglu.grisilith.weapon.skill.area.PersistentAreaAnchor
import com.azizoglu.grisilith.weapon.skill.area.PersistentAreaManager
import com.azizoglu.grisilith.weapon.skill.area.PersistentAreaRuntime
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.MathUtils

class AcidicPoolRuntime(
    areaManager: PersistentAreaManager
) {

    private val areaRuntime:
        PersistentAreaRuntime =
        areaManager.runtimeFor(
            AREA_ID
        )

    private var vitriolicIgnitionActive =
        false

    private var ignitionVisualRemainingSeconds =
        0f

    private var surfaceAnimationSeconds =
        0f

    val isVitriolicIgnitionActive: Boolean
        get() =
            vitriolicIgnitionActive

    val field:
        PersistentArea?
        get() =
            areaRuntime.area

    val isActive: Boolean
        get() =
            areaRuntime.isActive

    fun tryBegin(
        playerCenterX: Float,
        playerCenterY: Float,
        enemies: List<Enemy>
    ): Boolean {

        if (isActive) {
            return false
        }

        val placementRangeSquared =
            DefaultWeaponAbilities
                .ACIDIC_POOL_PLACEMENT_RANGE
                .let {
                        range ->

                    range *
                        range
                }

        /*
         * Vortex Pull gibi yalnızca menzilde uygun
         * bir düşman varsa kullanılabilir.
         *
         * Alan, menzildeki en yakın canlı düşmanın
         * merkezi üzerinde açılır.
         */
        val target =
            enemies
                .asSequence()
                .filter {
                        enemy ->

                    !enemy.isDead
                }
                .map {
                        enemy ->

                    enemy to
                        Vector2.dst2(
                            playerCenterX,
                            playerCenterY,
                            enemy.centerX,
                            enemy.centerY
                        )
                }
                .filter {
                        (_, distanceSquared) ->

                    distanceSquared <=
                        placementRangeSquared
                }
                .minByOrNull {
                        (_, distanceSquared) ->

                    distanceSquared
                }
                ?.first
                ?: return false

        return areaRuntime.begin(
            PersistentArea(
                id =
                    AREA_ID,

                center =
                    Vector2(
                        target.centerX,
                        target.centerY
                    ),

                radius =
                    DefaultWeaponAbilities
                        .ACIDIC_POOL_RADIUS,

                durationSeconds =
                    DefaultWeaponAbilities
                        .ACIDIC_POOL_DURATION_SECONDS,

                tickIntervalSeconds =
                    DefaultWeaponAbilities
                        .ACIDIC_POOL_TICK_SECONDS,

                anchor =
                    PersistentAreaAnchor
                        .FIXED_WORLD_POSITION
            )
        )
    }

    fun contains(
        worldX: Float,
        worldY: Float
    ): Boolean {

        return areaRuntime
            .area
            ?.contains(
                worldX,
                worldY
            )
            ?: false
    }

    fun tryActivateVitriolicIgnition(
        worldX: Float,
        worldY: Float
    ): Boolean {

        if (
            vitriolicIgnitionActive ||
            !contains(
                worldX,
                worldY
            )
        ) {
            return false
        }

        vitriolicIgnitionActive =
            true

        ignitionVisualRemainingSeconds =
            IGNITION_VISUAL_SECONDS

        return true
    }

    fun update(
        delta: Float,
        enemies: List<Enemy>,
        onDamageTick: (
            enemy: Enemy,
            tickIntervalSeconds: Float,
            poolCenterX: Float,
            poolCenterY: Float
        ) -> Unit,
        onCompleted: () -> Unit
    ) {

        areaRuntime.update(
            delta =
                delta,

            /*
             * FIXED_WORLD_POSITION kullandığı için
             * bu değerler alan merkezini değiştirmez.
             */
            followCenterX =
                0f,

            followCenterY =
                0f,

            onFrame = {
                    _,
                    frameDelta ->

                surfaceAnimationSeconds +=
                    frameDelta

                ignitionVisualRemainingSeconds =
                    (
                        ignitionVisualRemainingSeconds -
                            frameDelta
                        )
                        .coerceAtLeast(
                            0f
                        )
            },

            onTick = {
                    area ->

                enemies
                    .toList()
                    .forEach {
                            enemy ->

                        if (
                            enemy.isDead ||
                            !area.contains(
                                enemy.centerX,
                                enemy.centerY
                            )
                        ) {
                            return@forEach
                        }

                        onDamageTick(
                            enemy,
                            area.tickIntervalSeconds,
                            area.center.x,
                            area.center.y
                        )
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

        val pulse =
            (
                MathUtils.sin(
                    surfaceAnimationSeconds *
                        SURFACE_PULSE_SPEED
                ) +
                    1f
                ) /
                2f

        val primaryColor =
            if (
                vitriolicIgnitionActive
            ) {
                Color(
                    0.72f,
                    0.22f,
                    0.05f,
                    0.34f +
                        pulse *
                        0.10f
                )
            } else {
                Color(
                    0.22f,
                    0.58f,
                    0.10f,
                    0.26f +
                        pulse *
                        0.08f
                )
            }

        val secondaryColor =
            if (
                vitriolicIgnitionActive
            ) {
                Color(
                    0.96f,
                    0.48f,
                    0.08f,
                    0.20f +
                        pulse *
                        0.12f
                )
            } else {
                Color(
                    0.42f,
                    0.78f,
                    0.16f,
                    0.18f +
                        pulse *
                        0.08f
                )
            }

        shapeRenderer.color =
            primaryColor

        shapeRenderer.circle(
            area.center.x,
            area.center.y,
            area.radius *
                (
                    0.96f +
                        pulse *
                        0.04f
                    ),
            52
        )

        shapeRenderer.color =
            secondaryColor

        shapeRenderer.circle(
            area.center.x -
                area.radius *
                0.18f,
            area.center.y +
                area.radius *
                0.08f,
            area.radius *
                (
                    0.74f +
                        pulse *
                        0.05f
                    ),
            44
        )

        shapeRenderer.circle(
            area.center.x +
                area.radius *
                0.21f,
            area.center.y -
                area.radius *
                0.12f,
            area.radius *
                (
                    0.63f +
                        (
                            1f -
                                pulse
                            ) *
                        0.05f
                    ),
            42
        )

        repeat(
            BUBBLE_COUNT
        ) {
                index ->

            val angle =
                surfaceAnimationSeconds *
                    BUBBLE_ROTATION_SPEED +
                    index *
                    (
                        MathUtils.PI2 /
                            BUBBLE_COUNT
                        )

            val distance =
                area.radius *
                    (
                        0.24f +
                            index %
                            3 *
                            0.13f
                        )

            val bubbleRadius =
                3f +
                    (
                        index %
                            3
                        ) *
                    1.6f +
                    pulse *
                    1.2f

            shapeRenderer.color =
                if (
                    vitriolicIgnitionActive
                ) {
                    Color(
                        1f,
                        0.62f,
                        0.12f,
                        0.58f
                    )
                } else {
                    Color(
                        0.66f,
                        0.94f,
                        0.22f,
                        0.48f
                    )
                }

            shapeRenderer.circle(
                area.center.x +
                    MathUtils.cos(
                        angle
                    ) *
                    distance,

                area.center.y +
                    MathUtils.sin(
                        angle
                    ) *
                    distance,

                bubbleRadius,
                16
            )
        }

        if (
            ignitionVisualRemainingSeconds >
            0f
        ) {
            val progress =
                1f -
                    (
                        ignitionVisualRemainingSeconds /
                            IGNITION_VISUAL_SECONDS
                        )
                        .coerceIn(
                            0f,
                            1f
                        )

            val explosionRadius =
                area.radius *
                    (
                        1f +
                            progress
                        ) *
                    1.02f

            val alpha =
                (
                    1f -
                        progress
                    ) *
                    0.72f

            shapeRenderer.color =
                Color(
                    1f,
                    0.54f,
                    0.08f,
                    alpha
                )

            shapeRenderer.circle(
                area.center.x,
                area.center.y,
                explosionRadius,
                64
            )

            shapeRenderer.color =
                Color(
                    1f,
                    0.88f,
                    0.42f,
                    alpha *
                        0.72f
                )

            shapeRenderer.circle(
                area.center.x,
                area.center.y,
                explosionRadius *
                    0.62f,
                56
            )
        }
    }

    fun clear() {
        areaRuntime.clear()

        vitriolicIgnitionActive =
            false

        ignitionVisualRemainingSeconds =
            0f

        surfaceAnimationSeconds =
            0f
    }

    companion object {

        private const val AREA_ID =
            "AREA_ACIDIC_POOL"

        private const val IGNITION_VISUAL_SECONDS =
            0.55f

        private const val SURFACE_PULSE_SPEED =
            3.8f

        private const val BUBBLE_ROTATION_SPEED =
            0.85f

        private const val BUBBLE_COUNT =
            9
    }
}
