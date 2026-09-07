package com.azizoglu.grisilith.map

object TroubleAtHomeLayout {

    const val WALL_HEIGHT =
        360f

    const val GATE_WIDTH =
        540f

    const val GATE_HEIGHT =
        360f

    const val ENEMY_WALL_PADDING =
        85f

    const val GUARD_SPAWN_OFFSET_Y =
        95f

    fun wallY(worldHeight: Float): Float =
        worldHeight - WALL_HEIGHT

    fun gateX(worldWidth: Float): Float =
        worldWidth / 2f - GATE_WIDTH / 2f

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
