package com.azizoglu.grisilith.enemy.navigation

import com.azizoglu.grisilith.map.MapObstacle
import com.badlogic.gdx.math.Vector2

/*
 * Enemy'nin A* rotasını düşük frekansta yeniler ve waypoint'leri takip eder.
 *
 * Doğrudan görüş varsa A* kullanılmaz. Böylece açık alanda Enemy'ler
 * eski davranışları kadar hafif çalışmaya devam eder.
 */
class EnemyNavigationAgent(
    private val agentSize: Float
) {

    private val path =
        mutableListOf<Vector2>()

    private var waypointIndex =
        0

    private var repathRemainingSeconds =
        0f

    private var lastTargetX =
        Float.NaN

    private var lastTargetY =
        Float.NaN

    fun update(
        delta: Float
    ) {
        repathRemainingSeconds =
            (
                repathRemainingSeconds -
                    delta.coerceAtLeast(
                        0f
                    )
                )
                .coerceAtLeast(
                    0f
                )
    }

    fun resolveMovementDirection(
        enemyCenterX: Float,
        enemyCenterY: Float,
        targetCenterX: Float,
        targetCenterY: Float,
        requestedDirection: Vector2,
        worldWidth: Float,
        worldHeight: Float,
        obstacles: List<MapObstacle>,
        output: Vector2
    ): Vector2 {

        if (
            obstacles.isEmpty()
        ) {
            clearPath()
            return output.set(
                requestedDirection
            )
        }

        val towardTargetX =
            targetCenterX -
                enemyCenterX

        val towardTargetY =
            targetCenterY -
                enemyCenterY

        val requestedTowardTarget =
            requestedDirection.x *
                towardTargetX +
                requestedDirection.y *
                towardTargetY >
                0f

        /*
         * Retreat/Fear gibi hedeften uzaklaşan hareketlerde A* hedefe
         * doğru rota üretmemeli. Collision çözümü güvenlik katmanı olarak kalır.
         */
        if (
            !requestedTowardTarget
        ) {
            clearPath()
            return output.set(
                requestedDirection
            )
        }

        if (
            GridAStarPathfinder
                .hasDirectPath(
                    startX =
                        enemyCenterX,

                    startY =
                        enemyCenterY,

                    targetX =
                        targetCenterX,

                    targetY =
                        targetCenterY,

                    agentSize =
                        agentSize,

                    worldWidth =
                        worldWidth,

                    worldHeight =
                        worldHeight,

                    obstacles =
                        obstacles
                )
        ) {
            clearPath()

            return output.set(
                requestedDirection
            )
        }

        if (
            shouldRepath(
                targetCenterX =
                    targetCenterX,

                targetCenterY =
                    targetCenterY
            )
        ) {
            rebuildPath(
                enemyCenterX =
                    enemyCenterX,

                enemyCenterY =
                    enemyCenterY,

                targetCenterX =
                    targetCenterX,

                targetCenterY =
                    targetCenterY,

                worldWidth =
                    worldWidth,

                worldHeight =
                    worldHeight,

                obstacles =
                    obstacles
            )
        }

        advanceReachedWaypoints(
            enemyCenterX =
                enemyCenterX,

            enemyCenterY =
                enemyCenterY
        )

        val waypoint =
            path.getOrNull(
                waypointIndex
            )
                ?: return output.set(
                    requestedDirection
                )

        return output.set(
            waypoint.x -
                enemyCenterX,

            waypoint.y -
                enemyCenterY
        )
    }

    private fun shouldRepath(
        targetCenterX: Float,
        targetCenterY: Float
    ): Boolean {

        if (
            path.isEmpty() ||
            waypointIndex >=
            path.size ||
            repathRemainingSeconds <=
            0f
        ) {
            return true
        }

        if (
            lastTargetX.isNaN() ||
            lastTargetY.isNaN()
        ) {
            return true
        }

        val differenceX =
            targetCenterX -
                lastTargetX

        val differenceY =
            targetCenterY -
                lastTargetY

        return (
            differenceX *
                differenceX +
                differenceY *
                differenceY
            ) >=
            TARGET_REPATH_DISTANCE *
            TARGET_REPATH_DISTANCE
    }

    private fun rebuildPath(
        enemyCenterX: Float,
        enemyCenterY: Float,
        targetCenterX: Float,
        targetCenterY: Float,
        worldWidth: Float,
        worldHeight: Float,
        obstacles: List<MapObstacle>
    ) {
        path.clear()

        path.addAll(
            GridAStarPathfinder
                .findPath(
                    startX =
                        enemyCenterX,

                    startY =
                        enemyCenterY,

                    targetX =
                        targetCenterX,

                    targetY =
                        targetCenterY,

                    agentSize =
                        agentSize,

                    worldWidth =
                        worldWidth,

                    worldHeight =
                        worldHeight,

                    obstacles =
                        obstacles
                )
        )

        waypointIndex =
            0

        lastTargetX =
            targetCenterX

        lastTargetY =
            targetCenterY

        repathRemainingSeconds =
            REPATH_INTERVAL_SECONDS
    }

    private fun advanceReachedWaypoints(
        enemyCenterX: Float,
        enemyCenterY: Float
    ) {
        while (
            waypointIndex <
            path.size
        ) {
            val waypoint =
                path[
                    waypointIndex
                ]

            val differenceX =
                waypoint.x -
                    enemyCenterX

            val differenceY =
                waypoint.y -
                    enemyCenterY

            val distanceSquared =
                differenceX *
                    differenceX +
                    differenceY *
                    differenceY

            if (
                distanceSquared >
                WAYPOINT_REACHED_DISTANCE *
                WAYPOINT_REACHED_DISTANCE
            ) {
                return
            }

            waypointIndex++
        }
    }

    private fun clearPath() {
        path.clear()
        waypointIndex =
            0
    }

    companion object {

        private const val REPATH_INTERVAL_SECONDS =
            0.75f

        private const val TARGET_REPATH_DISTANCE =
            96f

        private const val WAYPOINT_REACHED_DISTANCE =
            26f
    }
}
