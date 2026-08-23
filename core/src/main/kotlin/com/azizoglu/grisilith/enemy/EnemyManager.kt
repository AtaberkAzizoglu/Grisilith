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
     * Per-enemy target lock.
     *
     * null summonRuntimeId = Player.
     * non-null = that active Ivy summon.
     *
     * The lock is intentionally sticky so an Enemy does not visibly flip
     * between Player/Cow/Imp/Knight every frame when distances are similar.
     */
    private data class CombatTargetLock(
        var summonRuntimeId: String? =
            null
    )

    private val targetLocks =
        mutableMapOf<Enemy, CombatTargetLock>()

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
        worldWidth: Float,
        worldHeight: Float,
        mapObstacles: List<MapObstacle>,
        onPlayerDamaged: (Int) -> Unit,
        onSummonDamaged: (
            summon: SummonedEntity,
            damage: Int
        ) -> Unit = {
                summon,
                damage ->

            summon.takeDamage(
                damage
            )
        }
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
                targetLocks.remove(
                    enemy
                )

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

            val selectedSummon =
                resolveSummonTarget(
                    enemy =
                        enemy,

                    playerCenterX =
                        targetX,

                    playerCenterY =
                        targetY,

                    summons =
                        targetableSummons
                )

            if (
                selectedSummon ==
                null
            ) {
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

                    onContactDamage =
                        onPlayerDamaged
                )
            } else {
                enemy.update(
                    delta =
                        delta,

                    targetX =
                        selectedSummon.centerX,

                    targetY =
                        selectedSummon.centerY,

                    targetBounds =
                        selectedSummon.combatBounds,

                    worldWidth =
                        worldWidth,

                    worldHeight =
                        worldHeight,

                    mapObstacles =
                        mapObstacles,

                    nearbyEnemies =
                        enemies,

                    onContactDamage = {
                            damage ->

                        if (
                            !selectedSummon.isExpired
                        ) {
                            onSummonDamaged(
                                selectedSummon,
                                damage
                            )
                        }
                    }
                )
            }
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

    private fun resolveSummonTarget(
        enemy: Enemy,
        playerCenterX: Float,
        playerCenterY: Float,
        summons: List<SummonedEntity>
    ): SummonedEntity? {

        val lock =
            targetLocks.getOrPut(
                enemy
            ) {
                CombatTargetLock()
            }

        val nearestSummon =
            summons.minByOrNull {
                distanceSquared(
                    enemyX =
                        enemy.centerX,

                    enemyY =
                        enemy.centerY,

                    targetX =
                        it.centerX,

                    targetY =
                        it.centerY
                )
            }

        val playerDistanceSquared =
            distanceSquared(
                enemyX =
                    enemy.centerX,

                enemyY =
                    enemy.centerY,

                targetX =
                    playerCenterX,

                targetY =
                    playerCenterY
            )

        /*
         * Existing summon lock is valid only while that summon still exists.
         */
        val lockedSummon =
            lock.summonRuntimeId
                ?.let {
                        runtimeId ->

                    summons.firstOrNull {
                        it.runtimeId ==
                            runtimeId
                    }
                }

        if (
            lock.summonRuntimeId !=
            null &&
            lockedSummon ==
            null
        ) {
            lock.summonRuntimeId =
                null
        }

        val currentTargetDistanceSquared =
            if (
                lockedSummon !=
                null
            ) {
                distanceSquared(
                    enemyX =
                        enemy.centerX,

                    enemyY =
                        enemy.centerY,

                    targetX =
                        lockedSummon.centerX,

                    targetY =
                        lockedSummon.centerY
                )
            } else {
                playerDistanceSquared
            }

        val nearestSummonDistanceSquared =
            nearestSummon
                ?.let {
                    distanceSquared(
                        enemyX =
                            enemy.centerX,

                        enemyY =
                            enemy.centerY,

                        targetX =
                            it.centerX,

                        targetY =
                            it.centerY
                    )
                }

        /*
         * Hysteresis:
         * a challenger must be meaningfully closer than the current target.
         *
         * TARGET_SWITCH_DISTANCE_RATIO = 0.85 means its linear distance
         * must be about 15% shorter before the Enemy visibly changes aggro.
         */
        val switchThresholdSquared =
            currentTargetDistanceSquared *
                TARGET_SWITCH_DISTANCE_RATIO *
                TARGET_SWITCH_DISTANCE_RATIO

        if (
            lockedSummon !=
            null
        ) {
            if (
                playerDistanceSquared <
                switchThresholdSquared
            ) {
                lock.summonRuntimeId =
                    null

                return null
            }

            if (
                nearestSummon !=
                null &&
                nearestSummon.runtimeId !=
                lockedSummon.runtimeId &&
                nearestSummonDistanceSquared !=
                null &&
                nearestSummonDistanceSquared <
                switchThresholdSquared
            ) {
                lock.summonRuntimeId =
                    nearestSummon.runtimeId

                return nearestSummon
            }

            return lockedSummon
        }

        if (
            nearestSummon !=
            null &&
            nearestSummonDistanceSquared !=
            null &&
            nearestSummonDistanceSquared <
            playerDistanceSquared *
            TARGET_SWITCH_DISTANCE_RATIO *
            TARGET_SWITCH_DISTANCE_RATIO
        ) {
            lock.summonRuntimeId =
                nearestSummon.runtimeId

            return nearestSummon
        }

        /*
         * When there is no strong hysteresis reason to switch, Player stays
         * the target. This preserves the original game feel.
         */
        return null
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

            targetLocks.remove(
                enemy
            )

            onEnemyRemoved(enemy)
            iterator.remove()
        }
    }

    fun clearAll() {
        enemies.clear()
        targetLocks.clear()
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
    companion object {
        /*
         * Candidate must be ~15% closer in linear distance to steal aggro.
         * This will be revisited in Stat & Mechanic Balancing.
         */
        private const val TARGET_SWITCH_DISTANCE_RATIO =
            0.85f
    }
}
