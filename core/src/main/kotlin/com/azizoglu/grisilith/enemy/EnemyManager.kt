package com.azizoglu.grisilith.enemy

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20

import com.azizoglu.grisilith.map.MapObstacle
import com.azizoglu.grisilith.summon.SummonedEntity

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

class EnemyManager {

    private val enemies = mutableListOf<Enemy>()

    /*
     * Trouble at Home adds the gate as a third combat target family.
     * Every living Enemy chooses the physically closest valid target among
     * Player, friendly summons and the gate.
     */
    private enum class CombatTargetType {
        PLAYER,
        SUMMON,
        GATE
    }

    private data class CombatTarget(
        val type: CombatTargetType,
        val centerX: Float,
        val centerY: Float,
        val bounds: Rectangle,
        val summon: SummonedEntity? = null
    )

    val allEnemies: List<Enemy>
        get() = enemies

    val enemyCount: Int
        get() = enemies.size

    val playerVisionMultiplier: Float
        get() =
            enemies
                .asSequence()
                .filter { !it.isDead }
                .map { it.playerVisionMultiplier }
                .minOrNull()
                ?: 1f

    fun spawn(enemy: Enemy) {
        enemies.add(enemy)
    }

    fun update(
        delta: Float,
        targetX: Float,
        targetY: Float,
        targetBounds: Rectangle,
        summons: List<SummonedEntity>,
        gateCenterX: Float? = null,
        gateCenterY: Float? = null,
        gateBounds: Rectangle? = null,
        worldWidth: Float,
        worldHeight: Float,
        mapObstacles: List<MapObstacle>,
        onPlayerDamaged: (Enemy, Int) -> Unit,
        onSummonDamaged: (
            summon: SummonedEntity,
            damage: Int
        ) -> Unit = {
                summon,
                damage ->

            summon.takeDamage(
                damage
            )
        },
        onGateDamaged: (Enemy, Int) -> Unit = { _, _ -> }
    ) {
        /*
         * Only living/active Ivy summons are candidate targets.
         * Phantom Steed is not a SummonedEntity and therefore never appears
         * in this list.
         */
        val targetableSummons =
            summons.filter {
                !it.isExpired
            }

        enemies.forEach {
                enemy ->

            if (enemy.isDead) {
                /*
                 * IMPORTANT:
                 * Enemy.update(...) is still required after death.
                 *
                 * Enemy itself owns deathFadeRemainingSeconds and only
                 * decrements it from inside Enemy.update(...). If we skip
                 * dead enemies here, isDeathFadeComplete can never become
                 * true and removeDeadEnemies() can never remove the sprite.
                 *
                 * Enemy.update() already detects isDead, advances only the
                 * death fade/cleanup timers and returns before any movement
                 * or attack logic, so calling it here is safe.
                 */
                enemy.update(
                    delta =
                        delta,

                    targetX =
                        targetX,

                    targetY =
                        targetY,

                    targetBounds =
                        targetBounds,

                    worldWidth =
                        worldWidth,

                    worldHeight =
                        worldHeight,

                    mapObstacles =
                        mapObstacles,

                    nearbyEnemies =
                        enemies,

                    onContactDamage = {
                        /*
                         * Cannot be reached for a dead Enemy because
                         * Enemy.update() returns from its isDead branch.
                         */
                    }
                )

                return@forEach
            }

            val selectedTarget =
                resolveClosestTarget(
                    enemy =
                        enemy,

                    playerCenterX =
                        targetX,

                    playerCenterY =
                        targetY,

                    playerBounds =
                        targetBounds,

                    summons =
                        targetableSummons,

                    gateCenterX =
                        gateCenterX,

                    gateCenterY =
                        gateCenterY,

                    gateBounds =
                        gateBounds
                )

            enemy.update(
                delta = delta,
                targetX = selectedTarget.centerX,
                targetY = selectedTarget.centerY,
                targetBounds = selectedTarget.bounds,
                worldWidth = worldWidth,
                worldHeight = worldHeight,
                mapObstacles = mapObstacles,
                nearbyEnemies = enemies,
                onContactDamage = { damage ->
                    when (selectedTarget.type) {
                        CombatTargetType.PLAYER ->
                            onPlayerDamaged(enemy, damage)

                        CombatTargetType.SUMMON ->
                            selectedTarget.summon
                                ?.takeIf { !it.isExpired }
                                ?.let {
                                    onSummonDamaged(it, damage)
                                }

                        CombatTargetType.GATE ->
                            onGateDamaged(enemy, damage)
                    }
                }
            )
        }

        enemies.forEach {
                enemy ->

            enemy.resolveObstacleCollisions(
                obstacles =
                    mapObstacles
            )
        }

        resolveEnemySeparation()

        enemies.forEach {
                enemy ->

            enemy.keepInsideWorld(
                worldWidth =
                    worldWidth,

                worldHeight =
                    worldHeight
            )
        }
    }

    private fun resolveClosestTarget(
        enemy: Enemy,
        playerCenterX: Float,
        playerCenterY: Float,
        playerBounds: Rectangle,
        summons: List<SummonedEntity>,
        gateCenterX: Float?,
        gateCenterY: Float?,
        gateBounds: Rectangle?
    ): CombatTarget {
        val candidates =
            mutableListOf(
                CombatTarget(
                    type = CombatTargetType.PLAYER,
                    centerX = playerCenterX,
                    centerY = playerCenterY,
                    bounds = playerBounds
                )
            )

        summons.forEach { summon ->
            candidates +=
                CombatTarget(
                    type = CombatTargetType.SUMMON,
                    centerX = summon.centerX,
                    centerY = summon.centerY,
                    bounds = summon.combatBounds,
                    summon = summon
                )
        }

        if (
            gateCenterX != null &&
            gateCenterY != null &&
            gateBounds != null
        ) {
            candidates +=
                CombatTarget(
                    type = CombatTargetType.GATE,
                    centerX = gateCenterX,
                    centerY = gateCenterY,
                    bounds = gateBounds
                )
        }

        return candidates.minBy {
            distanceSquared(
                enemyX = enemy.centerX,
                enemyY = enemy.centerY,
                targetX = it.centerX,
                targetY = it.centerY
            )
        }
    }

    private fun distanceSquared(
        enemyX: Float,
        enemyY: Float,
        targetX: Float,
        targetY: Float
    ): Float {
        val differenceX =
            targetX -
                enemyX

        val differenceY =
            targetY -
                enemyY

        return differenceX *
            differenceX +
            differenceY *
            differenceY
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

                /*
                 * Trait contact hook separation'dan önce çalışır.
                 * Vampire Hunger gibi temas Trait'leri gerçek overlap'i
                 * bu noktada görür.
                 */
                firstEnemy
                    .resolveTraitContactWith(
                        secondEnemy
                    )

                secondEnemy
                    .resolveTraitContactWith(
                        firstEnemy
                    )

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
                !enemy.isDeathFadeComplete ||
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
        /*
         * ShapeRenderer alpha değeri tek başına yeterli değildir;
         * fade'in gerçekten görünmesi için blending açık tutulur.
         */
        Gdx.gl.glEnable(
            GL20.GL_BLEND
        )

        Gdx.gl.glBlendFunc(
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA
        )

        enemies.forEach { enemy ->
            enemy.render(
                shapeRenderer
            )
        }

        Gdx.gl.glDisable(
            GL20.GL_BLEND
        )
    }
}
