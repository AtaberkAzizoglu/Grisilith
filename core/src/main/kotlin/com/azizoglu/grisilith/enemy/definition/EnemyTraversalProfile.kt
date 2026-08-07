package com.azizoglu.grisilith.enemy.definition

/*
 * Varsayılan Enemy obstacle'a takılır ve obstacle içinde doğamaz.
 *
 * Bald Eagle ve Ghost gibi istisnalar iki alanı da true kullanır.
 */
data class EnemyTraversalProfile(
    val ignoresObstacles: Boolean = false,
    val canSpawnInsideObstacles: Boolean = false
) {

    init {
        require(
            !canSpawnInsideObstacles ||
                ignoresObstacles
        ) {
            "An Enemy that can spawn inside obstacles must also ignore obstacles."
        }
    }

    companion object {

        val BLOCKED_BY_OBSTACLES =
            EnemyTraversalProfile()

        val INCORPOREAL =
            EnemyTraversalProfile(
                ignoresObstacles = true,
                canSpawnInsideObstacles = true
            )
    }
}
