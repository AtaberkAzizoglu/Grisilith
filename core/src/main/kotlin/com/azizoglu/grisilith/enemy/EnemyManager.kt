package com.azizoglu.grisilith.enemy

import com.azizoglu.grisilith.map.MapObstacle

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

class EnemyManager {

    private val enemies = mutableListOf<Enemy>()

    val allEnemies: List<Enemy>
        get() = enemies

    val enemyCount: Int
        get() = enemies.size

    fun spawn(enemy: Enemy) {
        enemies.add(enemy)
    }

    fun update(
        delta: Float,
        targetX: Float,
        targetY: Float,
        targetBounds: Rectangle,
        worldWidth: Float,
        worldHeight: Float,
        mapObstacles: List<MapObstacle>,
        onPlayerDamaged: (Int) -> Unit
    ) {
        enemies.forEach { enemy ->
            enemy.update(
                delta = delta,
                targetX = targetX,
                targetY = targetY,
                targetBounds = targetBounds,
                onContactDamage = onPlayerDamaged
            )
        }

        enemies.forEach { enemy ->
            enemy.resolveObstacleCollisions(
                obstacles =
                    mapObstacles
            )
        }

        resolveEnemySeparation()

        enemies.forEach { enemy ->
            enemy.keepInsideWorld(
                worldWidth = worldWidth,
                worldHeight = worldHeight
            )
        }
    }

    private fun resolveEnemySeparation() {
        /*
         * Her düşman çiftini yalnızca bir kez kontrol eder.
         */
        for (firstIndex in 0 until enemies.size) {
            val firstEnemy = enemies[firstIndex]

            for (
            secondIndex in firstIndex + 1
                until enemies.size
            ) {
                val secondEnemy =
                    enemies[secondIndex]

                firstEnemy.separateFrom(
                    secondEnemy
                )
            }
        }
    }

    fun resolvePlayerCollisions(
        resolveCollision: (Rectangle) -> Boolean
    ) {
        enemies.forEach { enemy ->
            if (!enemy.isDead) {
                resolveCollision(enemy.bounds)
            }
        }
    }

    fun hasLivingEnemyOfType(
        enemyType: EnemyType
    ): Boolean {
        return enemies.any { enemy ->
            !enemy.isDead &&
                enemy.type == enemyType
        }
    }

    fun nearestEnemy(
        x: Float,
        y: Float
    ): Enemy? {
        return enemies
            .asSequence()
            .filterNot { enemy ->
                enemy.isDead
            }
            .minByOrNull { enemy ->
                val differenceX =
                    enemy.centerX - x

                val differenceY =
                    enemy.centerY - y

                differenceX * differenceX +
                    differenceY * differenceY
            }
    }

    fun removeDeadEnemies(
        onEnemyRemoved: (Enemy) -> Unit = {}
    ) {
        val iterator =
            enemies.iterator()

        while (iterator.hasNext()) {
            val enemy =
                iterator.next()

            if (
                !enemy.isDead ||
                enemy.shouldRemainForExecutionMark
            ) {
                continue
            }

            onEnemyRemoved(enemy)
            iterator.remove()
        }
    }

    fun clearAll() {
        enemies.clear()
    }

    fun render(shapeRenderer: ShapeRenderer) {
        enemies.forEach { enemy ->
            enemy.render(shapeRenderer)
        }
    }
}
