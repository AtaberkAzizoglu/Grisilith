package com.azizoglu.grisilith.summon

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import java.util.UUID

class SummonedEntity(
    val sourceWeaponRuntimeId: String,
    val type: SummonType,
    val stats: SummonStats,
    startCenterX: Float,
    startCenterY: Float,
    lifetimeSeconds: Float
) {

    val runtimeId: String =
        UUID.randomUUID()
            .toString()

    private val center =
        Vector2(
            startCenterX,
            startCenterY
        )

    private val visualPhase =
        MathUtils.random(
            0f,
            MathUtils.PI2
        )

    var remainingLifetimeSeconds: Float =
        lifetimeSeconds
            .coerceAtLeast(
                0f
            )
        private set

    val isExpired: Boolean
        get() =
            remainingLifetimeSeconds <=
                0f

    val centerX: Float
        get() =
            center.x

    val centerY: Float
        get() =
            center.y

    fun update(
        delta: Float,
        playerCenterX: Float,
        playerCenterY: Float,
        worldWidth: Float,
        worldHeight: Float
    ) {

        if (
            isExpired ||
            delta <= 0f
        ) {
            return
        }

        remainingLifetimeSeconds =
            (
                remainingLifetimeSeconds -
                    delta
                )
                .coerceAtLeast(
                    0f
                )

        /*
         * Paket 2A'da combat AI yoktur.
         *
         * Entity yalnızca oyuncunun çevresinde yumuşak bir
         * placeholder formasyonu korur. Cow/Imp/Knight davranışları
         * sonraki paketlerde bu ortak entity üzerine bağlanacaktır.
         */
        val desiredDistance =
            when (type) {
                SummonType.COW ->
                    86f

                SummonType.IMP ->
                    112f

                SummonType.KNIGHT_SPIRIT ->
                    156f
            }

        val desiredX =
            playerCenterX +
                MathUtils.cos(
                    visualPhase
                ) *
                desiredDistance

        val desiredY =
            playerCenterY +
                MathUtils.sin(
                    visualPhase
                ) *
                desiredDistance

        val interpolation =
            (
                delta *
                    2.4f
                )
                .coerceIn(
                    0f,
                    1f
                )

        center.lerp(
            Vector2(
                desiredX,
                desiredY
            ),
            interpolation
        )

        center.x =
            center.x.coerceIn(
                12f,
                (
                    worldWidth -
                        12f
                    )
                    .coerceAtLeast(
                        12f
                    )
            )

        center.y =
            center.y.coerceIn(
                12f,
                (
                    worldHeight -
                        12f
                    )
                    .coerceAtLeast(
                        12f
                    )
            )
    }

    fun render(
        shapeRenderer: ShapeRenderer
    ) {

        val radius =
            when (type) {
                SummonType.COW ->
                    18f

                SummonType.IMP ->
                    14f

                SummonType.KNIGHT_SPIRIT ->
                    20f
            }

        shapeRenderer.color =
            when (type) {
                SummonType.COW ->
                    Color(
                        0.82f,
                        0.82f,
                        0.72f,
                        0.90f
                    )

                SummonType.IMP ->
                    Color(
                        0.78f,
                        0.22f,
                        0.12f,
                        0.90f
                    )

                SummonType.KNIGHT_SPIRIT ->
                    Color(
                        0.48f,
                        0.58f,
                        0.76f,
                        0.84f
                    )
            }

        shapeRenderer.circle(
            center.x,
            center.y,
            radius,
            24
        )

        shapeRenderer.color =
            if (
                type.isFriendlyToPlayer
            ) {
                Color(
                    0.28f,
                    0.88f,
                    0.42f,
                    0.82f
                )
            } else {
                Color(
                    0.92f,
                    0.22f,
                    0.22f,
                    0.86f
                )
            }

        shapeRenderer.circle(
            center.x,
            center.y,
            radius *
                0.32f,
            18
        )
    }
}
