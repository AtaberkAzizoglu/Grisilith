package com.azizoglu.grisilith.weapon.skill

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.weapon.skill.area.PersistentArea
import com.azizoglu.grisilith.weapon.skill.area.PersistentAreaAnchor
import com.azizoglu.grisilith.weapon.skill.area.PersistentAreaManager
import com.azizoglu.grisilith.weapon.skill.area.PersistentAreaRuntime
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2

class AcidicPoolRuntime(
    areaManager: PersistentAreaManager
) {

    private val areaRuntime:
        PersistentAreaRuntime =
        areaManager.runtimeFor(
            AREA_ID
        )

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
                    _ ->
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

        /*
         * Placeholder görünüm:
         * Birkaç üst üste binen daire tam geometrik
         * olmayan, hafif bozuk bir havuz hissi verir.
         */
        shapeRenderer.color =
            Color(
                0.28f,
                0.62f,
                0.12f,
                0.30f
            )

        shapeRenderer.circle(
            area.center.x,
            area.center.y,
            area.radius,
            48
        )

        shapeRenderer.color =
            Color(
                0.42f,
                0.78f,
                0.16f,
                0.22f
            )

        shapeRenderer.circle(
            area.center.x -
                area.radius *
                0.18f,
            area.center.y +
                area.radius *
                0.08f,
            area.radius *
                0.78f,
            40
        )

        shapeRenderer.circle(
            area.center.x +
                area.radius *
                0.21f,
            area.center.y -
                area.radius *
                0.12f,
            area.radius *
                0.67f,
            40
        )
    }

    fun clear() {
        areaRuntime.clear()
    }

    companion object {

        private const val AREA_ID =
            "AREA_ACIDIC_POOL"
    }
}
