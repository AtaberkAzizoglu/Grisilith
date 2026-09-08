package com.azizoglu.grisilith.map

object TroubleAtHomeLayout {

    const val WALL_HEIGHT =
        360f

    const val GATE_WIDTH =
        500f

    const val GATE_HEIGHT =
        360f

    const val GATE_COLLISION_WIDTH_RATIO =
        0.66f

    const val GATE_COLLISION_HEIGHT_RATIO =
        0.24f

    const val GATE_COLLISION_OFFSET_X_RATIO =
        0.17f

    const val ENEMY_WALL_PADDING =
        85f

    const val GUARD_SPAWN_OFFSET_Y =
        95f

    fun wallY(worldHeight: Float): Float =
        worldHeight - WALL_HEIGHT

    fun gateX(worldWidth: Float): Float =
        (
            (worldWidth / GATE_WIDTH).toInt() / 2 *
                GATE_WIDTH
            )
            .coerceIn(0f, worldWidth - GATE_WIDTH)

    fun gateY(worldHeight: Float): Float =
        wallY(worldHeight)

    fun enemyMaximumSpawnY(
        worldHeight: Float,
        enemySize: Float
    ): Float =
        wallY(worldHeight) -
            ENEMY_WALL_PADDING -
            enemySize
}
