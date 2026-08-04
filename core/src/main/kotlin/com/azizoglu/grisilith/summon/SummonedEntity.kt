package com.azizoglu.grisilith.summon

import com.azizoglu.grisilith.combat.HealthComponent
import com.azizoglu.grisilith.enemy.Enemy
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import java.util.UUID

class SummonedEntity(
    val sourceWeaponRuntimeId: String,
    val sourceWeaponDefinitionId: String,
    val sourceWeaponDisplayName: String,
    val type: SummonType,
    val stats: SummonStats,
    startCenterX: Float,
    startCenterY: Float,
    lifetimeSeconds: Float
) {

    enum class KnightTargetType {
        NONE,
        ENEMY,
        PLAYER
    }

    val runtimeId: String =
        UUID.randomUUID()
            .toString()

    private val center =
        Vector2(
            startCenterX,
            startCenterY
        )

    private val movementDirection =
        Vector2()

    private val visualPhase =
        MathUtils.random(
            0f,
            MathUtils.PI2
        )

    private var attackCooldownRemainingSeconds =
        0f

    private var attackFlashRemainingSeconds =
        0f

    val health =
        HealthComponent(
            maximumHealth =
                stats.maximumHealth
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
                0f ||
                health.isDead

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
        worldHeight: Float,
        enemies: List<Enemy>,
        onCowAttack: (
            summon: SummonedEntity,
            enemy: Enemy
        ) -> Unit,
        onImpAttack: (
            summon: SummonedEntity,
            enemy: Enemy
        ) -> Unit,
        onKnightEnemyAttack: (
            summon: SummonedEntity,
            enemy: Enemy
        ) -> Unit,
        onKnightPlayerAttack: (
            summon: SummonedEntity
        ) -> Unit
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

        attackCooldownRemainingSeconds =
            (
                attackCooldownRemainingSeconds -
                    delta
                )
                .coerceAtLeast(
                    0f
                )

        attackFlashRemainingSeconds =
            (
                attackFlashRemainingSeconds -
                    delta
                )
                .coerceAtLeast(
                    0f
                )

        when (type) {

            SummonType.COW -> {
                updateCow(
                    delta =
                        delta,

                    playerCenterX =
                        playerCenterX,

                    playerCenterY =
                        playerCenterY,

                    enemies =
                        enemies,

                    onAttack =
                        onCowAttack
                )
            }

            SummonType.IMP -> {
                updateImp(
                    delta =
                        delta,

                    playerCenterX =
                        playerCenterX,

                    playerCenterY =
                        playerCenterY,

                    enemies =
                        enemies,

                    onAttack =
                        onImpAttack
                )
            }

            SummonType.KNIGHT_SPIRIT -> {
                updateKnight(
                    delta =
                        delta,

                    playerCenterX =
                        playerCenterX,

                    playerCenterY =
                        playerCenterY,

                    enemies =
                        enemies,

                    onEnemyAttack =
                        onKnightEnemyAttack,

                    onPlayerAttack =
                        onKnightPlayerAttack
                )
            }
        }

        keepInsideWorld(
            worldWidth =
                worldWidth,

            worldHeight =
                worldHeight
        )
    }

    fun takeDamage(
        amount: Int
    ): Int {

        if (
            amount <= 0 ||
            isExpired
        ) {
            return 0
        }

        return health.takeDamage(
            amount
        )
    }

    fun render(
        shapeRenderer: ShapeRenderer
    ) {

        val radius =
            visualRadius()

        shapeRenderer.color =
            baseColor()

        shapeRenderer.circle(
            center.x,
            center.y,
            radius,
            24
        )

        shapeRenderer.color =
            if (
                attackFlashRemainingSeconds >
                0f
            ) {
                Color(
                    1f,
                    0.92f,
                    0.46f,
                    0.96f
                )
            } else if (
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

        renderHealthBar(
            shapeRenderer =
                shapeRenderer,

            radius =
                radius
        )
    }

    private fun updateCow(
        delta: Float,
        playerCenterX: Float,
        playerCenterY: Float,
        enemies: List<Enemy>,
        onAttack: (
            summon: SummonedEntity,
            enemy: Enemy
        ) -> Unit
    ) {

        val target =
            nearestLivingEnemy(
                enemies
            )

        if (target == null) {
            moveTowardFormationPoint(
                delta =
                    delta,

                playerCenterX =
                    playerCenterX,

                playerCenterY =
                    playerCenterY,

                distance =
                    82f
            )

            return
        }

        if (
            isInsideEnemyAttackRange(
                enemy =
                    target,

                range =
                    stats.attackRange
            )
        ) {
            tryAttack(
                cooldownSeconds =
                    COW_ATTACK_INTERVAL_SECONDS
            ) {
                onAttack(
                    this,
                    target
                )
            }

            return
        }

        moveToward(
            targetX =
                target.centerX,

            targetY =
                target.centerY,

            delta =
                delta
        )
    }

    private fun updateImp(
        delta: Float,
        playerCenterX: Float,
        playerCenterY: Float,
        enemies: List<Enemy>,
        onAttack: (
            summon: SummonedEntity,
            enemy: Enemy
        ) -> Unit
    ) {

        val target =
            nearestLivingEnemy(
                enemies
            )

        if (target == null) {
            moveTowardFormationPoint(
                delta =
                    delta,

                playerCenterX =
                    playerCenterX,

                playerCenterY =
                    playerCenterY,

                distance =
                    108f
            )

            return
        }

        val distanceSquared =
            Vector2.dst2(
                center.x,
                center.y,
                target.centerX,
                target.centerY
            )

        val attackRange =
            stats.attackRange +
                target.bounds.width /
                2f

        val minimumRange =
            IMP_MINIMUM_COMFORT_DISTANCE

        when {
            distanceSquared >
                attackRange *
                attackRange -> {

                moveToward(
                    targetX =
                        target.centerX,

                    targetY =
                        target.centerY,

                    delta =
                        delta
                )
            }

            distanceSquared <
                minimumRange *
                minimumRange -> {

                moveAwayFrom(
                    targetX =
                        target.centerX,

                    targetY =
                        target.centerY,

                    delta =
                        delta
                )
            }

            else -> {
                tryAttack(
                    cooldownSeconds =
                        IMP_ATTACK_INTERVAL_SECONDS
                ) {
                    onAttack(
                        this,
                        target
                    )
                }
            }
        }
    }

    private fun updateKnight(
        delta: Float,
        playerCenterX: Float,
        playerCenterY: Float,
        enemies: List<Enemy>,
        onEnemyAttack: (
            summon: SummonedEntity,
            enemy: Enemy
        ) -> Unit,
        onPlayerAttack: (
            summon: SummonedEntity
        ) -> Unit
    ) {

        val playerDistanceSquared =
            Vector2.dst2(
                center.x,
                center.y,
                playerCenterX,
                playerCenterY
            )

        val targetsPlayer =
            playerDistanceSquared <=
                KNIGHT_PLAYER_AGGRO_RANGE *
                KNIGHT_PLAYER_AGGRO_RANGE

        if (targetsPlayer) {
            if (
                playerDistanceSquared <=
                stats.attackRange *
                stats.attackRange
            ) {
                tryAttack(
                    cooldownSeconds =
                        KNIGHT_ATTACK_INTERVAL_SECONDS
                ) {
                    onPlayerAttack(
                        this
                    )
                }
            } else {
                moveToward(
                    targetX =
                        playerCenterX,

                    targetY =
                        playerCenterY,

                    delta =
                        delta
                )
            }

            return
        }

        val enemyTarget =
            nearestLivingEnemy(
                enemies
            )

        if (enemyTarget == null) {
            /*
             * Knight oyuncunun dostu değildir.
             * Oyuncu aggro menzili dışındaysa ve Enemy yoksa
             * mevcut pozisyonunu korur.
             */
            return
        }

        if (
            isInsideEnemyAttackRange(
                enemy =
                    enemyTarget,

                range =
                    stats.attackRange
            )
        ) {
            tryAttack(
                cooldownSeconds =
                    KNIGHT_ATTACK_INTERVAL_SECONDS
            ) {
                onEnemyAttack(
                    this,
                    enemyTarget
                )
            }

            return
        }

        moveToward(
            targetX =
                enemyTarget.centerX,

            targetY =
                enemyTarget.centerY,

            delta =
                delta
        )
    }

    private fun nearestLivingEnemy(
        enemies: List<Enemy>
    ): Enemy? {

        return enemies
            .asSequence()
            .filter {
                !it.isDead
            }
            .minByOrNull {
                Vector2.dst2(
                    center.x,
                    center.y,
                    it.centerX,
                    it.centerY
                )
            }
    }

    private fun isInsideEnemyAttackRange(
        enemy: Enemy,
        range: Float
    ): Boolean {

        val effectiveRange =
            range +
                enemy.bounds.width /
                2f

        return Vector2.dst2(
            center.x,
            center.y,
            enemy.centerX,
            enemy.centerY
        ) <=
            effectiveRange *
            effectiveRange
    }

    private fun tryAttack(
        cooldownSeconds: Float,
        action: () -> Unit
    ) {

        if (
            attackCooldownRemainingSeconds >
            0f
        ) {
            return
        }

        action()

        attackCooldownRemainingSeconds =
            cooldownSeconds

        attackFlashRemainingSeconds =
            ATTACK_FLASH_SECONDS
    }

    private fun moveToward(
        targetX: Float,
        targetY: Float,
        delta: Float
    ) {

        movementDirection.set(
            targetX -
                center.x,

            targetY -
                center.y
        )

        if (
            movementDirection.isZero(
                0.001f
            )
        ) {
            return
        }

        movementDirection.nor()

        center.mulAdd(
            movementDirection,
            stats.movementSpeed *
                delta
        )
    }

    private fun moveAwayFrom(
        targetX: Float,
        targetY: Float,
        delta: Float
    ) {

        movementDirection.set(
            center.x -
                targetX,

            center.y -
                targetY
        )

        if (
            movementDirection.isZero(
                0.001f
            )
        ) {
            movementDirection.set(
                1f,
                0f
            )
        }

        movementDirection.nor()

        center.mulAdd(
            movementDirection,
            stats.movementSpeed *
                delta
        )
    }

    private fun moveTowardFormationPoint(
        delta: Float,
        playerCenterX: Float,
        playerCenterY: Float,
        distance: Float
    ) {

        val desiredX =
            playerCenterX +
                MathUtils.cos(
                    visualPhase
                ) *
                distance

        val desiredY =
            playerCenterY +
                MathUtils.sin(
                    visualPhase
                ) *
                distance

        movementDirection.set(
            desiredX -
                center.x,

            desiredY -
                center.y
        )

        if (
            movementDirection.isZero(
                0.001f
            )
        ) {
            return
        }

        movementDirection.nor()

        val maximumMovement =
            stats.movementSpeed *
                delta

        val remainingDistance =
            Vector2.dst(
                center.x,
                center.y,
                desiredX,
                desiredY
            )

        center.mulAdd(
            movementDirection,
            minOf(
                maximumMovement,
                remainingDistance
            )
        )
    }

    private fun keepInsideWorld(
        worldWidth: Float,
        worldHeight: Float
    ) {

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

    private fun visualRadius():
        Float {

        return when (type) {
            SummonType.COW ->
                18f

            SummonType.IMP ->
                14f

            SummonType.KNIGHT_SPIRIT ->
                20f
        }
    }

    private fun baseColor():
        Color {

        return when (type) {
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
    }

    private fun renderHealthBar(
        shapeRenderer: ShapeRenderer,
        radius: Float
    ) {

        val width =
            radius *
                2f

        val height =
            3f

        val x =
            center.x -
                width /
                2f

        val y =
            center.y +
                radius +
                5f

        shapeRenderer.color =
            Color(
                0.12f,
                0.08f,
                0.10f,
                0.90f
            )

        shapeRenderer.rect(
            x,
            y,
            width,
            height
        )

        shapeRenderer.color =
            Color(
                0.30f,
                0.86f,
                0.38f,
                0.95f
            )

        shapeRenderer.rect(
            x,
            y,
            width *
                health.healthRatio
                    .coerceIn(
                        0f,
                        1f
                    ),
            height
        )
    }

    companion object {

        private const val COW_ATTACK_INTERVAL_SECONDS =
            1.15f

        private const val IMP_ATTACK_INTERVAL_SECONDS =
            1.05f

        private const val IMP_MINIMUM_COMFORT_DISTANCE =
            105f

        private const val KNIGHT_ATTACK_INTERVAL_SECONDS =
            1.20f

        private const val KNIGHT_PLAYER_AGGRO_RANGE =
            210f

        private const val ATTACK_FLASH_SECONDS =
            0.10f
    }
}
