package com.azizoglu.grisilith.weapon.glyph

import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyDamageContext
import com.azizoglu.grisilith.enemy.EnemyManager
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import kotlin.math.ln
import kotlin.math.roundToInt

class SingularityRuntime(
    private val damageCalculator:
    WeaponDamageCalculator
) {

    private data class ActiveSingularity(
        val request: GlyphCastRequest,
        var remainingActiveSeconds: Float,
        var tickAccumulator: Float = 0f,
        var elapsedSeconds: Float = 0f,
        var collapseRemainingSeconds: Float =
            COLLAPSE_DURATION_SECONDS,
        var phase: Phase =
            Phase.ACTIVE,
        val enteredEnemies:
        MutableSet<Enemy> =
            mutableSetOf(),
        val countedDeadEnemies:
        MutableSet<Enemy> =
            mutableSetOf()
    )

    private enum class Phase {
        ACTIVE,
        COLLAPSE
    }

    private var active:
        ActiveSingularity? =
        null

    private var explosionVisualCenter =
        Vector2()

    private var explosionVisualRadius =
        0f

    private var explosionVisualRemainingSeconds =
        0f

    val isActive: Boolean
        get() =
            active !=
                null

    fun begin(
        request: GlyphCastRequest
    ): Boolean {

        if (
            active !=
            null
        ) {
            return false
        }

        active =
            ActiveSingularity(
                request =
                    request,

                remainingActiveSeconds =
                    ACTIVE_DURATION_SECONDS
            )

        return true
    }

    fun update(
        delta: Float,
        player: Player,
        enemyManager: EnemyManager,
        projectiles: List<Projectile>,
        worldWidth: Float,
        worldHeight: Float,
        onDamageDealt: (
            damage: Int,
            sourceName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
    ) {

        if (
            delta <=
            0f
        ) {
            return
        }

        explosionVisualRemainingSeconds =
            (
                explosionVisualRemainingSeconds -
                    delta
                )
                .coerceAtLeast(
                    0f
                )

        val singularity =
            active
                ?: return

        when (
            singularity.phase
        ) {
            Phase.ACTIVE -> {
                updateActivePhase(
                    singularity =
                        singularity,

                    delta =
                        delta,

                    player =
                        player,

                    enemyManager =
                        enemyManager,

                    projectiles =
                        projectiles,

                    worldWidth =
                        worldWidth,

                    worldHeight =
                        worldHeight,

                    onDamageDealt =
                        onDamageDealt,

                    onEnemyKilled =
                        onEnemyKilled
                )
            }

            Phase.COLLAPSE -> {
                singularity
                    .collapseRemainingSeconds =
                    (
                        singularity
                            .collapseRemainingSeconds -
                            delta
                        )
                        .coerceAtLeast(
                            0f
                        )

                if (
                    singularity
                        .collapseRemainingSeconds <=
                    0f
                ) {
                    explode(
                        singularity =
                            singularity,

                        enemyManager =
                            enemyManager,

                        onDamageDealt =
                            onDamageDealt,

                        onEnemyKilled =
                            onEnemyKilled
                    )

                    active =
                        null
                }
            }
        }
    }

    fun render(
        shapeRenderer: ShapeRenderer
    ) {

        active
            ?.let {
                    singularity ->

                val collapseScale =
                    if (
                        singularity.phase ==
                        Phase.COLLAPSE
                    ) {
                        (
                            singularity
                                .collapseRemainingSeconds /
                                COLLAPSE_DURATION_SECONDS
                            )
                            .coerceIn(
                                0.08f,
                                1f
                            )
                    } else {
                        1f
                    }

                val pulse =
                    0.92f +
                        MathUtils.sin(
                            singularity
                                .elapsedSeconds *
                                5.5f
                        ) *
                        0.08f

                val renderedRadius =
                    BASE_RADIUS *
                        collapseScale *
                        pulse

                shapeRenderer.color =
                    Color(
                        0.08f,
                        0.02f,
                        0.12f,
                        0.80f
                    )

                shapeRenderer.circle(
                    singularity
                        .request
                        .centerX,

                    singularity
                        .request
                        .centerY,

                    renderedRadius,
                    64
                )

                shapeRenderer.color =
                    Color(
                        0.44f,
                        0.12f,
                        0.64f,
                        0.34f
                    )

                shapeRenderer.circle(
                    singularity
                        .request
                        .centerX,

                    singularity
                        .request
                        .centerY,

                    renderedRadius *
                        0.72f,
                    56
                )

                shapeRenderer.color =
                    Color(
                        0.70f,
                        0.42f,
                        0.90f,
                        0.30f
                    )

                repeat(
                    12
                ) {
                        index ->

                    val angle =
                        (
                            MathUtils.PI2 /
                                12f
                            ) *
                            index +
                            singularity
                                .elapsedSeconds *
                            0.85f

                    val outerX =
                        singularity
                            .request
                            .centerX +
                            MathUtils.cos(
                                angle
                            ) *
                            renderedRadius

                    val outerY =
                        singularity
                            .request
                            .centerY +
                            MathUtils.sin(
                                angle
                            ) *
                            renderedRadius

                    shapeRenderer.rectLine(
                        outerX,
                        outerY,
                        singularity
                            .request
                            .centerX,
                        singularity
                            .request
                            .centerY,
                        1.5f
                    )
                }
            }

        if (
            explosionVisualRemainingSeconds >
            0f
        ) {
            val progress =
                1f -
                    (
                        explosionVisualRemainingSeconds /
                            EXPLOSION_VISUAL_SECONDS
                        )
                        .coerceIn(
                            0f,
                            1f
                        )

            shapeRenderer.color =
                Color(
                    0.62f,
                    0.26f,
                    0.84f,
                    (
                        1f -
                            progress
                        ) *
                        0.55f
                )

            shapeRenderer.circle(
                explosionVisualCenter.x,
                explosionVisualCenter.y,
                explosionVisualRadius *
                    progress.coerceAtLeast(
                        0.08f
                    ),
                72
            )
        }
    }

    fun clear() {

        active =
            null

        explosionVisualRemainingSeconds =
            0f

        explosionVisualRadius =
            0f
    }

    private fun updateActivePhase(
        singularity: ActiveSingularity,
        delta: Float,
        player: Player,
        enemyManager: EnemyManager,
        projectiles: List<Projectile>,
        worldWidth: Float,
        worldHeight: Float,
        onDamageDealt: (
            damage: Int,
            sourceName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
    ) {

        singularity.elapsedSeconds +=
            delta

        singularity.remainingActiveSeconds =
            (
                singularity
                    .remainingActiveSeconds -
                    delta
                )
                .coerceAtLeast(
                    0f
                )

        enemyManager
            .allEnemies
            .forEach {
                    enemy ->

                val distance =
                    Vector2.dst(
                        singularity
                            .request
                            .centerX,

                        singularity
                            .request
                            .centerY,

                        enemy.centerX,
                        enemy.centerY
                    )

                if (
                    distance <=
                    BASE_RADIUS
                ) {
                    singularity
                        .enteredEnemies
                        .add(
                            enemy
                        )
                }

                if (
                    enemy.isDead
                ) {
                    if (
                        enemy in
                        singularity
                            .enteredEnemies
                    ) {
                        singularity
                            .countedDeadEnemies
                            .add(
                                enemy
                            )
                    }

                    return@forEach
                }

                if (
                    distance >
                    BASE_RADIUS
                ) {
                    return@forEach
                }

                val normalizedDistance =
                    (
                        distance /
                            BASE_RADIUS
                        )
                        .coerceIn(
                            0f,
                            1f
                        )

                val pullStrength =
                    ENEMY_MINIMUM_PULL_SPEED +
                        (
                            ENEMY_MAXIMUM_PULL_SPEED -
                                ENEMY_MINIMUM_PULL_SPEED
                            ) *
                        (
                            1f -
                                normalizedDistance
                            )

                enemy.pullToward(
                    targetCenterX =
                        singularity
                            .request
                            .centerX,

                    targetCenterY =
                        singularity
                            .request
                            .centerY,

                    pullSpeedPixelsPerSecond =
                        pullStrength,

                    delta =
                        delta,

                    worldWidth =
                        worldWidth,

                    worldHeight =
                        worldHeight
                )
            }

        applyPlayerPull(
            singularity =
                singularity,

            player =
                player,

            delta =
                delta
        )

        projectiles.forEach {
            it.bendToward(
                targetCenterX =
                    singularity
                        .request
                        .centerX,

                targetCenterY =
                    singularity
                        .request
                        .centerY,

                influenceRadius =
                    PROJECTILE_BEND_RADIUS,

                turnStrengthPerSecond =
                    PROJECTILE_BEND_STRENGTH,

                delta =
                    delta
            )
        }

        singularity.tickAccumulator +=
            delta

        while (
            singularity.tickAccumulator >=
            DAMAGE_TICK_SECONDS
        ) {
            singularity.tickAccumulator -=
                DAMAGE_TICK_SECONDS

            applyDamageTick(
                singularity =
                    singularity,

                enemyManager =
                    enemyManager,

                onDamageDealt =
                    onDamageDealt,

                onEnemyKilled =
                    onEnemyKilled
            )
        }

        if (
            singularity.remainingActiveSeconds <=
            0f
        ) {
            singularity.phase =
                Phase.COLLAPSE
        }
    }

    private fun applyPlayerPull(
        singularity: ActiveSingularity,
        player: Player,
        delta: Float
    ) {

        if (
            player.health.isDead
        ) {
            return
        }

        val distance =
            Vector2.dst(
                singularity
                    .request
                    .centerX,

                singularity
                    .request
                    .centerY,

                player.centerX,
                player.centerY
            )

        if (
            distance >
            BASE_RADIUS ||
            distance <=
            1f
        ) {
            return
        }

        val normalizedDistance =
            (
                distance /
                    BASE_RADIUS
                )
                .coerceIn(
                    0f,
                    1f
                )

        val pullSpeed =
            PLAYER_MINIMUM_PULL_SPEED +
                (
                    PLAYER_MAXIMUM_PULL_SPEED -
                        PLAYER_MINIMUM_PULL_SPEED
                    ) *
                (
                    1f -
                        normalizedDistance
                    )

        val direction =
            Vector2(
                singularity
                    .request
                    .centerX -
                    player.centerX,

                singularity
                    .request
                    .centerY -
                    player.centerY
            )

        if (
            direction.isZero(
                0.001f
            )
        ) {
            return
        }

        direction.nor()

        player.teleportCenterTo(
            targetCenterX =
                player.centerX +
                    direction.x *
                    pullSpeed *
                    delta,

            targetCenterY =
                player.centerY +
                    direction.y *
                    pullSpeed *
                    delta
        )
    }

    private fun applyDamageTick(
        singularity: ActiveSingularity,
        enemyManager: EnemyManager,
        onDamageDealt: (
            damage: Int,
            sourceName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
    ) {

        val tickDamage =
            (
                damageCalculator
                    .calculate(
                        singularity
                            .request
                            .sourceWeapon
                    )
                    .damage *
                    DPS_MULTIPLIER *
                    DAMAGE_TICK_SECONDS
                )
                .roundToInt()
                .coerceAtLeast(
                    1
                )

        enemyManager
            .allEnemies
            .toList()
            .forEach {
                    enemy ->

                if (
                    enemy.isDead ||
                    Vector2.dst2(
                        singularity
                            .request
                            .centerX,

                        singularity
                            .request
                            .centerY,

                        enemy.centerX,
                        enemy.centerY
                    ) >
                    BASE_RADIUS *
                    BASE_RADIUS
                ) {
                    return@forEach
                }

                damageEnemy(
                    enemy =
                        enemy,

                    damage =
                        tickDamage,

                    singularity =
                        singularity,

                    attackName =
                        "Singularity",

                    onDamageDealt =
                        onDamageDealt,

                    onEnemyKilled =
                        onEnemyKilled
                )
            }
    }

    private fun explode(
        singularity: ActiveSingularity,
        enemyManager: EnemyManager,
        onDamageDealt: (
            damage: Int,
            sourceName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
    ) {

        val deathCount =
            singularity
                .countedDeadEnemies
                .size

        val logarithmicGrowth =
            ln(
                1.0 +
                    deathCount
                        .toDouble()
            )
                .toFloat()

        val explosionRadius =
            (
                BASE_EXPLOSION_RADIUS +
                    logarithmicGrowth *
                    EXPLOSION_RADIUS_GROWTH
                )
                .coerceAtMost(
                    MAXIMUM_EXPLOSION_RADIUS
                )

        val explosionMultiplier =
            (
                BASE_EXPLOSION_DAMAGE_MULTIPLIER +
                    logarithmicGrowth *
                    EXPLOSION_DAMAGE_GROWTH
                )
                .coerceAtMost(
                    MAXIMUM_EXPLOSION_DAMAGE_MULTIPLIER
                )

        val explosionDamage =
            (
                damageCalculator
                    .calculate(
                        singularity
                            .request
                            .sourceWeapon
                    )
                    .damage *
                    explosionMultiplier
                )
                .roundToInt()
                .coerceAtLeast(
                    1
                )

        enemyManager
            .allEnemies
            .toList()
            .forEach {
                    enemy ->

                if (
                    enemy.isDead ||
                    Vector2.dst2(
                        singularity
                            .request
                            .centerX,

                        singularity
                            .request
                            .centerY,

                        enemy.centerX,
                        enemy.centerY
                    ) >
                    explosionRadius *
                    explosionRadius
                ) {
                    return@forEach
                }

                damageEnemy(
                    enemy =
                        enemy,

                    damage =
                        explosionDamage,

                    singularity =
                        singularity,

                    attackName =
                        "Singularity Collapse",

                    onDamageDealt =
                        onDamageDealt,

                    onEnemyKilled =
                        onEnemyKilled
                )
            }

        explosionVisualCenter.set(
            singularity
                .request
                .centerX,

            singularity
                .request
                .centerY
        )

        explosionVisualRadius =
            explosionRadius

        explosionVisualRemainingSeconds =
            EXPLOSION_VISUAL_SECONDS
    }

    private fun damageEnemy(
        enemy: Enemy,
        damage: Int,
        singularity: ActiveSingularity,
        attackName: String,
        onDamageDealt: (
            damage: Int,
            sourceName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
    ) {

        if (
            enemy.isDead
        ) {
            return
        }

        val sourceName =
            singularity
                .request
                .sourceWeapon
                .displayName +
                " — " +
                attackName

        val wasAlive =
            !enemy.isDead

        val appliedDamage =
            enemy.takeDamage(
                amount =
                    damage,

                damageContext =
                    EnemyDamageContext(
                        damageType =
                            DamageType.NECROTIC,

                        sourceWeaponRuntimeId =
                            singularity
                                .request
                                .sourceWeapon
                                .runtimeId,

                        sourceWeaponDefinitionId =
                            singularity
                                .request
                                .sourceWeapon
                                .definition
                                .id
                                .value,

                        sourceName =
                            sourceName
                    )
            )

        if (
            appliedDamage >
            0
        ) {
            onDamageDealt(
                appliedDamage,
                sourceName
            )
        }

        if (
            wasAlive &&
            enemy.isDead
        ) {
            singularity
                .countedDeadEnemies
                .add(
                    enemy
                )

            onEnemyKilled(
                enemy
            )
        }
    }

    companion object {

        private const val ACTIVE_DURATION_SECONDS =
            6f

        private const val COLLAPSE_DURATION_SECONDS =
            0.40f

        private const val DAMAGE_TICK_SECONDS =
            0.50f

        private const val BASE_RADIUS =
            180f

        private const val ENEMY_MINIMUM_PULL_SPEED =
            95f

        private const val ENEMY_MAXIMUM_PULL_SPEED =
            310f

        private const val PLAYER_MINIMUM_PULL_SPEED =
            22f

        private const val PLAYER_MAXIMUM_PULL_SPEED =
            92f

        private const val DPS_MULTIPLIER =
            1.10f

        private const val BASE_EXPLOSION_RADIUS =
            180f

        private const val EXPLOSION_RADIUS_GROWTH =
            62f

        private const val MAXIMUM_EXPLOSION_RADIUS =
            330f

        private const val BASE_EXPLOSION_DAMAGE_MULTIPLIER =
            2.40f

        private const val EXPLOSION_DAMAGE_GROWTH =
            0.82f

        private const val MAXIMUM_EXPLOSION_DAMAGE_MULTIPLIER =
            4.60f

        private const val PROJECTILE_BEND_RADIUS =
            280f

        private const val PROJECTILE_BEND_STRENGTH =
            0.82f

        private const val EXPLOSION_VISUAL_SECONDS =
            0.55f
    }
}
