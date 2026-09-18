package com.azizoglu.grisilith.enemy

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20

import com.azizoglu.grisilith.map.MapObstacle
import com.azizoglu.grisilith.summon.SummonedEntity
import com.azizoglu.grisilith.enemy.trait.EnemyTraitCombatTarget
import com.azizoglu.grisilith.enemy.trait.EnemyTraitTargetType
import com.azizoglu.grisilith.enemy.trait.CombatEntityType
import com.azizoglu.grisilith.enemy.runtime.EnemyRuntimeFactory
import com.azizoglu.grisilith.content.ConditionType

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.MathUtils

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

    val hasLivingGhost: Boolean
        get() = enemies.any {
            !it.isDead && it.kindId == com.azizoglu.grisilith.enemy.definition.EnemyKindIds.GHOST
        }

    fun spawn(enemy: Enemy) {
        enemies.add(enemy)
    }

    fun remove(enemy: Enemy): Boolean = enemies.remove(enemy)

    fun update(
        delta: Float,
        targetX: Float,
        targetY: Float,
        targetBounds: Rectangle,
        playerMaximumHealth: Int,
        summons: List<SummonedEntity>,
        gateCenterX: Float? = null,
        gateCenterY: Float? = null,
        gateBounds: Rectangle? = null,
        worldWidth: Float,
        worldHeight: Float,
        mapObstacles: List<MapObstacle>,
        onPlayerDamaged: (Enemy, Int, com.azizoglu.grisilith.content.DamageType) -> Unit,
        onSummonDamaged: (
            summon: SummonedEntity,
            damage: Int,
            damageType: com.azizoglu.grisilith.content.DamageType,
            source: Enemy
        ) -> Unit = {
                summon,
                damage,
                _,
                _ ->

            summon.takeDamage(
                damage
            )
        },
        onGateDamaged: (Enemy, Int) -> Unit = { _, _ -> },
        onPlayerCondition: (ConditionType, Float?, Int) -> Unit = { _, _, _ -> },
        onSummonCondition: (SummonedEntity, ConditionType, Float?, Int) -> Unit = { _, _, _, _ -> }
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

        val traitTargets =
            buildTraitTargets(
                playerCenterX = targetX,
                playerCenterY = targetY,
                playerBounds = targetBounds,
                playerMaximumHealth = playerMaximumHealth,
                summons = targetableSummons,
                gateCenterX = gateCenterX,
                gateCenterY = gateCenterY,
                gateBounds = gateBounds
            )

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

                    onContactDamage = { _, _ ->
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

            gateBounds?.let {
                enemy.keepSouthOfBarrier(
                    barrierY = it.y + it.height
                )
            }

            enemy.update(
                delta = delta,
                targetX = selectedTarget.centerX,
                targetY = selectedTarget.centerY,
                targetBounds = selectedTarget.bounds,
                targetEntityType =
                    when (selectedTarget.type) {
                        CombatTargetType.PLAYER -> CombatEntityType.PLAYER
                        CombatTargetType.SUMMON -> CombatEntityType.SUMMON
                        CombatTargetType.GATE -> CombatEntityType.STRUCTURE
                    },
                worldWidth = worldWidth,
                worldHeight = worldHeight,
                mapObstacles = mapObstacles,
                nearbyEnemies = enemies,
                combatTargets = traitTargets,
                onContactDamage = { damage, damageType ->
                    when (selectedTarget.type) {
                        CombatTargetType.PLAYER ->
                            onPlayerDamaged(enemy, damage, damageType)

                        CombatTargetType.SUMMON ->
                            selectedTarget.summon
                                ?.takeIf { !it.isExpired }
                                ?.let {
                                    onSummonDamaged(it, damage, damageType, enemy)
                                    if (enemy.kindId == com.azizoglu.grisilith.enemy.definition.EnemyKindIds.GHOST) {
                                        onSummonCondition(
                                            it,
                                            ConditionType.NECROSIS,
                                            5f,
                                            enemy.conditionDamageStatScore
                                        )
                                    }
                                }

                        CombatTargetType.GATE ->
                            onGateDamaged(enemy, damage)
                    }
                },
                onTraitTargetDamage = { event ->
                    if (event.amount > 0) when (event.targetType) {
                        EnemyTraitTargetType.PLAYER ->
                            onPlayerDamaged(enemy, event.amount, event.damageType)

                        EnemyTraitTargetType.SUMMON ->
                            targetableSummons
                                .getOrNull(event.targetIndex)
                                ?.takeIf { !it.isExpired }
                                ?.let { onSummonDamaged(it, event.amount, event.damageType, enemy) }

                        EnemyTraitTargetType.GATE -> {
                            val amount =
                                if (event.projectileOrMagic) {
                                    (event.amount * 0.5f)
                                        .toInt()
                                        .coerceAtLeast(1)
                                } else {
                                    event.amount
                                }
                            onGateDamaged(enemy, amount)
                        }
                    }
                    event.conditionType?.let { condition ->
                        when (event.targetType) {
                            EnemyTraitTargetType.PLAYER ->
                                onPlayerCondition(
                                    condition,
                                    event.conditionDurationSeconds,
                                    event.conditionStatScore
                                )
                            EnemyTraitTargetType.SUMMON ->
                                targetableSummons.getOrNull(event.targetIndex)
                                    ?.takeIf { !it.isExpired }
                                    ?.let {
                                        onSummonCondition(
                                            it,
                                            condition,
                                            event.conditionDurationSeconds,
                                            event.conditionStatScore
                                        )
                                    }
                            EnemyTraitTargetType.GATE -> Unit
                        }
                    }
                }
            )

            gateBounds?.let {
                enemy.keepSouthOfBarrier(
                    barrierY = it.y + it.height
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

    private fun buildTraitTargets(
        playerCenterX: Float,
        playerCenterY: Float,
        playerBounds: Rectangle,
        playerMaximumHealth: Int,
        summons: List<SummonedEntity>,
        gateCenterX: Float?,
        gateCenterY: Float?,
        gateBounds: Rectangle?
    ): List<EnemyTraitCombatTarget> {
        val result = mutableListOf(
            EnemyTraitCombatTarget(
                type = EnemyTraitTargetType.PLAYER,
                centerX = playerCenterX,
                centerY = playerCenterY,
                bounds = Rectangle(playerBounds),
                maximumHealth = playerMaximumHealth
            )
        )
        summons.forEachIndexed { index, summon ->
            result += EnemyTraitCombatTarget(
                type = EnemyTraitTargetType.SUMMON,
                index = index,
                centerX = summon.centerX,
                centerY = summon.centerY,
                bounds = Rectangle(summon.combatBounds),
                maximumHealth = summon.health.maximumHealth
            )
        }
        if (gateCenterX != null && gateCenterY != null && gateBounds != null) {
            result += EnemyTraitCombatTarget(
                type = EnemyTraitTargetType.GATE,
                centerX = gateCenterX,
                centerY = gateCenterY,
                bounds = Rectangle(gateBounds)
            )
        }
        return result
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

        val closest = candidates.minBy {
            distanceSquared(
                enemyX = enemy.centerX,
                enemyY = enemy.centerY,
                targetX = it.centerX,
                targetY = it.centerY
            )
        }

        /*
         * A boss already committed to the gate must become peelable. When the
         * player comes right beside the gate, switch that boss to the player
         * so defending the objective is physically possible.
         */
        if (
            closest.type == CombatTargetType.GATE &&
            enemy.type in setOf(EnemyType.MINI_BOSS, EnemyType.FINAL_BOSS) &&
            gateBounds != null &&
            distanceSquaredToRectangle(playerCenterX, playerCenterY, gateBounds) <=
                BOSS_GATE_PEEL_RADIUS * BOSS_GATE_PEEL_RADIUS
        ) {
            return candidates.first { it.type == CombatTargetType.PLAYER }
        }

        return closest
    }

    private fun distanceSquaredToRectangle(
        x: Float,
        y: Float,
        rectangle: Rectangle
    ): Float {
        val closestX = x.coerceIn(rectangle.x, rectangle.x + rectangle.width)
        val closestY = y.coerceIn(rectangle.y, rectangle.y + rectangle.height)
        val dx = x - closestX
        val dy = y - closestY
        return dx * dx + dy * dy
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
                enemy.isDead || !enemy.isTargetable
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

    fun spawnPendingSlimeFragments(
        runtimeFactory: EnemyRuntimeFactory,
        worldWidth: Float,
        worldHeight: Float
    ) {
        val additions = mutableListOf<Enemy>()
        enemies.toList().forEach { parent ->
            if (!parent.consumeSlimeSplitRequest()) return@forEach
            val offsets = arrayOf(
                -0.65f to -0.45f,
                0.65f to -0.45f,
                -0.65f to 0.45f,
                0.65f to 0.45f
            )
            offsets.forEach { offset ->
                val child = runtimeFactory.createSlimeFragment(
                    parent = parent,
                    startX = (parent.centerX + parent.visualSize * offset.first)
                        .coerceIn(0f, worldWidth - parent.visualSize * 0.55f),
                    startY = (parent.centerY + parent.visualSize * offset.second)
                        .coerceIn(0f, worldHeight - parent.visualSize * 0.55f)
                )
                additions += child
            }
        }
        enemies.addAll(additions)
    }

    fun spawnPendingSpecialEnemies(
        runtimeFactory: EnemyRuntimeFactory,
        worldWidth: Float,
        worldHeight: Float,
        obstacles: List<MapObstacle>
    ) {
        val additions = mutableListOf<Enemy>()
        enemies.toList().forEach { parent ->
            repeat(parent.consumeBabyCrocodileSpawnRequests()) {
                val position = nearbySpawnPosition(parent, 62f, worldWidth, worldHeight, obstacles)
                    ?: return@repeat
                additions += runtimeFactory.createBabyCrocodile(parent, position.first, position.second)
            }

            if (parent.consumeSwarmSplitRequest()) {
                parent.applySwarmSplit()
                val position = nearbySpawnPosition(parent, parent.visualSize * 0.8f, worldWidth, worldHeight, emptyList())
                    ?: ((parent.centerX + parent.visualSize * 0.65f) to parent.centerY)
                additions += runtimeFactory.createSwarmSplitChild(
                    parent,
                    position.first.coerceIn(0f, (worldWidth - parent.visualSize).coerceAtLeast(0f)),
                    position.second.coerceIn(0f, (worldHeight - parent.visualSize).coerceAtLeast(0f))
                )
            }
        }
        enemies.addAll(additions)
    }

    private fun nearbySpawnPosition(
        parent: Enemy,
        childSize: Float,
        worldWidth: Float,
        worldHeight: Float,
        obstacles: List<MapObstacle>
    ): Pair<Float, Float>? {
        repeat(16) {
            val angle = MathUtils.random(0f, MathUtils.PI2)
            val distance = parent.visualSize * MathUtils.random(0.65f, 1.15f)
            val x = (parent.centerX + MathUtils.cos(angle) * distance - childSize / 2f)
                .coerceIn(0f, (worldWidth - childSize).coerceAtLeast(0f))
            val y = (parent.centerY + MathUtils.sin(angle) * distance - childSize / 2f)
                .coerceIn(0f, (worldHeight - childSize).coerceAtLeast(0f))
            val bounds = Rectangle(x, y, childSize, childSize)
            if (obstacles.none { it.blocksMovement && bounds.overlaps(it.bounds) }) return x to y
        }
        return null
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

    companion object {
        private const val BOSS_GATE_PEEL_RADIUS = 100f
    }
}
