package com.azizoglu.grisilith.weapon.glyph

import com.azizoglu.grisilith.combat.condition.ConditionDamageCalculator
import com.azizoglu.grisilith.content.ConditionType
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyDamageContext
import com.azizoglu.grisilith.enemy.EnemyManager
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import kotlin.math.roundToInt

class GlyphSpellRuntime(
    private val playerStats: PlayerStats
) {

    private val damageCalculator =
        WeaponDamageCalculator(
            playerStats
        )

    private data class VisualPulse(
        val type: GlyphType,
        val centerX: Float,
        val centerY: Float,
        val radius: Float,
        var remainingSeconds: Float
    )

    private data class TsunamiWave(
        val request: GlyphCastRequest,
        val direction: Vector2,
        val perpendicular: Vector2,
        val start: Vector2,
        val totalDistance: Float,
        var travelledDistance: Float = 0f,
        val hitEnemies: MutableSet<Enemy> =
            mutableSetOf()
    )

    private val pulses =
        mutableListOf<VisualPulse>()

    private var tsunamiWave:
        TsunamiWave? =
        null

    fun cast(
        request: GlyphCastRequest,
        enemyManager: EnemyManager,
        worldWidth: Float,
        worldHeight: Float,
        onDamageDealt: (
            damage: Int,
            sourceName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
    ) {

        when (
            request.glyphType
        ) {
            GlyphType.CALL_LIGHTNING -> {
                castCallLightning(
                    request,
                    enemyManager,
                    onDamageDealt,
                    onEnemyKilled
                )
            }

            GlyphType.METEOR -> {
                castMeteor(
                    request,
                    enemyManager,
                    onDamageDealt,
                    onEnemyKilled
                )
            }

            GlyphType.TSUNAMI -> {
                beginTsunami(
                    request,
                    worldWidth,
                    worldHeight
                )
            }

            GlyphType.SINGULARITY -> {
                /*
                 * Singularity is intentionally implemented
                 * in the next dedicated package.
                 */
            }
        }
    }

    fun update(
        delta: Float,
        enemyManager: EnemyManager,
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

        pulses.forEach {
            it.remainingSeconds =
                (
                    it.remainingSeconds -
                        delta
                    )
                    .coerceAtLeast(
                        0f
                    )
        }

        pulses.removeAll {
            it.remainingSeconds <=
                0f
        }

        updateTsunami(
            delta,
            enemyManager,
            worldWidth,
            worldHeight,
            onDamageDealt,
            onEnemyKilled
        )
    }

    fun render(
        shapeRenderer: ShapeRenderer
    ) {

        pulses.forEach {
                pulse ->

            val alpha =
                (
                    pulse.remainingSeconds /
                        PULSE_DURATION_SECONDS
                    )
                    .coerceIn(
                        0f,
                        1f
                    )

            shapeRenderer.color =
                when (
                    pulse.type
                ) {
                    GlyphType.CALL_LIGHTNING ->
                        Color(
                            0.72f,
                            0.88f,
                            1f,
                            0.34f *
                                alpha
                        )

                    GlyphType.METEOR ->
                        Color(
                            1f,
                            0.30f,
                            0.10f,
                            0.38f *
                                alpha
                        )

                    else ->
                        Color(
                            0.75f,
                            0.75f,
                            0.90f,
                            0.25f *
                                alpha
                        )
                }

            shapeRenderer.circle(
                pulse.centerX,
                pulse.centerY,
                pulse.radius,
                48
            )
        }

        tsunamiWave
            ?.let {
                    wave ->

                val center =
                    currentTsunamiCenter(
                        wave
                    )

                shapeRenderer.color =
                    Color(
                        0.20f,
                        0.60f,
                        0.92f,
                        0.42f
                    )

                val halfLength =
                    TSUNAMI_FRONT_LENGTH /
                        2f

                val startX =
                    center.x -
                        wave.perpendicular.x *
                        halfLength

                val startY =
                    center.y -
                        wave.perpendicular.y *
                        halfLength

                val endX =
                    center.x +
                        wave.perpendicular.x *
                        halfLength

                val endY =
                    center.y +
                        wave.perpendicular.y *
                        halfLength

                shapeRenderer.rectLine(
                    startX,
                    startY,
                    endX,
                    endY,
                    TSUNAMI_FRONT_THICKNESS
                )
            }
    }

    fun clear() {

        pulses.clear()
        tsunamiWave = null
    }

    private fun castCallLightning(
        request: GlyphCastRequest,
        enemyManager: EnemyManager,
        onDamageDealt: (
            damage: Int,
            sourceName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
    ) {

        val baseDamage =
            damageCalculator
                .calculate(
                    request.sourceWeapon
                )
                .damage

        val innerDamage =
            (
                baseDamage *
                    LIGHTNING_INNER_DAMAGE_MULTIPLIER
                )
                .roundToInt()
                .coerceAtLeast(
                    1
                )

        val outerDamage =
            (
                baseDamage *
                    LIGHTNING_OUTER_DAMAGE_MULTIPLIER
                )
                .roundToInt()
                .coerceAtLeast(
                    1
                )

        val chainDamage =
            (
                baseDamage *
                    LIGHTNING_CHAIN_DAMAGE_MULTIPLIER
                )
                .roundToInt()
                .coerceAtLeast(
                    1
                )

        val enemies =
            enemyManager
                .allEnemies
                .filter {
                    !it.isDead
                }

        val outerHitEnemies =
            mutableListOf<Enemy>()

        enemies.forEach {
                enemy ->

            val distance =
                Vector2.dst(
                    request.centerX,
                    request.centerY,
                    enemy.centerX,
                    enemy.centerY
                )

            when {
                distance <=
                    LIGHTNING_INNER_RADIUS -> {

                    damageEnemy(
                        enemy,
                        innerDamage,
                        DamageType.LIGHTNING,
                        request,
                        "Call Lightning",
                        onDamageDealt,
                        onEnemyKilled
                    )
                }

                distance <=
                    LIGHTNING_OUTER_RADIUS -> {

                    val dealt =
                        damageEnemy(
                            enemy,
                            outerDamage,
                            DamageType.LIGHTNING,
                            request,
                            "Call Lightning",
                            onDamageDealt,
                            onEnemyKilled
                        )

                    if (
                        dealt
                    ) {
                        outerHitEnemies.add(
                            enemy
                        )
                    }
                }
            }
        }

        outerHitEnemies
            .forEach {
                    sourceEnemy ->

                val chainTarget =
                    enemies
                        .asSequence()
                        .filter {
                            it !==
                                sourceEnemy &&
                                !it.isDead
                        }
                        .minByOrNull {
                            Vector2.dst2(
                                sourceEnemy.centerX,
                                sourceEnemy.centerY,
                                it.centerX,
                                it.centerY
                            )
                        }
                        ?.takeIf {
                            Vector2.dst2(
                                sourceEnemy.centerX,
                                sourceEnemy.centerY,
                                it.centerX,
                                it.centerY
                            ) <=
                                LIGHTNING_CHAIN_RANGE *
                                LIGHTNING_CHAIN_RANGE
                        }

                if (
                    chainTarget !=
                    null
                ) {
                    damageEnemy(
                        chainTarget,
                        chainDamage,
                        DamageType.LIGHTNING,
                        request,
                        "Call Lightning — Chain",
                        onDamageDealt,
                        onEnemyKilled
                    )
                }
            }

        pulses.add(
            VisualPulse(
                type =
                    GlyphType.CALL_LIGHTNING,

                centerX =
                    request.centerX,

                centerY =
                    request.centerY,

                radius =
                    LIGHTNING_OUTER_RADIUS,

                remainingSeconds =
                    PULSE_DURATION_SECONDS
            )
        )
    }

    private fun castMeteor(
        request: GlyphCastRequest,
        enemyManager: EnemyManager,
        onDamageDealt: (
            damage: Int,
            sourceName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
    ) {

        val damage =
            (
                damageCalculator
                    .calculate(
                        request.sourceWeapon
                    )
                    .damage *
                    METEOR_DAMAGE_MULTIPLIER
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
                        request.centerX,
                        request.centerY,
                        enemy.centerX,
                        enemy.centerY
                    ) >
                    METEOR_RADIUS *
                    METEOR_RADIUS
                ) {
                    return@forEach
                }

                val dealt =
                    damageEnemy(
                        enemy,
                        damage,
                        DamageType.FIRE,
                        request,
                        "Meteor",
                        onDamageDealt,
                        onEnemyKilled
                    )

                if (
                    dealt &&
                    !enemy.isDead
                ) {
                    enemy.applyCondition(
                        ConditionDamageCalculator
                            .createApplication(
                                type =
                                    ConditionType.BURNED,

                                playerStats =
                                    playerStats,

                                durationSeconds =
                                    METEOR_BURNED_SECONDS,

                                sourceWeaponRuntimeId =
                                    request
                                        .sourceWeapon
                                        .runtimeId,

                                sourceWeaponDefinitionId =
                                    request
                                        .sourceWeapon
                                        .definition
                                        .id
                                        .value,

                                sourceName =
                                    request
                                        .sourceWeapon
                                        .displayName +
                                        " — Meteor",

                                sourceCenterX =
                                    request.centerX,

                                sourceCenterY =
                                    request.centerY
                            )
                    )
                }
            }

        pulses.add(
            VisualPulse(
                type =
                    GlyphType.METEOR,

                centerX =
                    request.centerX,

                centerY =
                    request.centerY,

                radius =
                    METEOR_RADIUS,

                remainingSeconds =
                    PULSE_DURATION_SECONDS
            )
        )
    }

    private fun beginTsunami(
        request: GlyphCastRequest,
        worldWidth: Float,
        worldHeight: Float
    ) {

        val angle =
            MathUtils.random(
                0f,
                MathUtils.PI2
            )

        val direction =
            Vector2(
                MathUtils.cos(
                    angle
                ),
                MathUtils.sin(
                    angle
                )
            )
                .nor()

        val perpendicular =
            Vector2(
                -direction.y,
                direction.x
            )

        val mapDiagonal =
            Vector2.len(
                worldWidth,
                worldHeight
            )

        val start =
            Vector2(
                request.centerX,
                request.centerY
            )
                .mulAdd(
                    direction,
                    -mapDiagonal
                )

        tsunamiWave =
            TsunamiWave(
                request =
                    request,

                direction =
                    direction,

                perpendicular =
                    perpendicular,

                start =
                    start,

                totalDistance =
                    mapDiagonal *
                        2f
            )
    }

    private fun updateTsunami(
        delta: Float,
        enemyManager: EnemyManager,
        worldWidth: Float,
        worldHeight: Float,
        onDamageDealt: (
            damage: Int,
            sourceName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
    ) {

        val wave =
            tsunamiWave
                ?: return

        wave.travelledDistance +=
            TSUNAMI_SPEED *
                delta

        val center =
            currentTsunamiCenter(
                wave
            )

        val damage =
            (
                damageCalculator
                    .calculate(
                        wave.request
                            .sourceWeapon
                    )
                    .damage *
                    TSUNAMI_DAMAGE_MULTIPLIER
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
                    enemy in
                    wave.hitEnemies
                ) {
                    return@forEach
                }

                val relative =
                    Vector2(
                        enemy.centerX -
                            center.x,

                        enemy.centerY -
                            center.y
                    )

                val forwardDistance =
                    kotlin.math.abs(
                        relative.dot(
                            wave.direction
                        )
                    )

                val sideDistance =
                    kotlin.math.abs(
                        relative.dot(
                            wave.perpendicular
                        )
                    )

                if (
                    forwardDistance >
                    TSUNAMI_FRONT_THICKNESS /
                    2f ||
                    sideDistance >
                    TSUNAMI_FRONT_LENGTH /
                    2f
                ) {
                    return@forEach
                }

                wave.hitEnemies.add(
                    enemy
                )

                val dealt =
                    damageEnemy(
                        enemy,
                        damage,
                        DamageType.WATER,
                        wave.request,
                        "Tsunami",
                        onDamageDealt,
                        onEnemyKilled
                    )

                if (
                    dealt &&
                    !enemy.isDead
                ) {
                    enemy.applyCondition(
                        ConditionDamageCalculator
                            .createApplication(
                                type =
                                    ConditionType.WET,

                                playerStats =
                                    playerStats,

                                durationSeconds =
                                    TSUNAMI_WET_SECONDS,

                                sourceWeaponRuntimeId =
                                    wave.request
                                        .sourceWeapon
                                        .runtimeId,

                                sourceWeaponDefinitionId =
                                    wave.request
                                        .sourceWeapon
                                        .definition
                                        .id
                                        .value,

                                sourceName =
                                    wave.request
                                        .sourceWeapon
                                        .displayName +
                                        " — Tsunami",

                                sourceCenterX =
                                    center.x,

                                sourceCenterY =
                                    center.y
                            )
                    )

                    enemy.applyKnockback(
                        directionX =
                            wave.direction.x,

                        directionY =
                            wave.direction.y,

                        strength =
                            TSUNAMI_KNOCKBACK_STRENGTH
                    )
                }
            }

        if (
            wave.travelledDistance >=
            wave.totalDistance
        ) {
            tsunamiWave =
                null
        }
    }

    private fun currentTsunamiCenter(
        wave: TsunamiWave
    ): Vector2 {

        return Vector2(
            wave.start
        )
            .mulAdd(
                wave.direction,
                wave.travelledDistance
            )
    }

    private fun damageEnemy(
        enemy: Enemy,
        damage: Int,
        damageType: DamageType,
        request: GlyphCastRequest,
        attackName: String,
        onDamageDealt: (
            damage: Int,
            sourceName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
    ): Boolean {

        if (
            enemy.isDead
        ) {
            return false
        }

        val sourceName =
            request.sourceWeapon
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
                            damageType,

                        sourceWeaponRuntimeId =
                            request
                                .sourceWeapon
                                .runtimeId,

                        sourceWeaponDefinitionId =
                            request
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
            onEnemyKilled(
                enemy
            )
        }

        return appliedDamage >
            0
    }

    companion object {

        private const val PULSE_DURATION_SECONDS =
            0.55f

        private const val LIGHTNING_INNER_RADIUS =
            86f

        private const val LIGHTNING_OUTER_RADIUS =
            178f

        private const val LIGHTNING_INNER_DAMAGE_MULTIPLIER =
            2.15f

        private const val LIGHTNING_OUTER_DAMAGE_MULTIPLIER =
            0.82f

        private const val LIGHTNING_CHAIN_DAMAGE_MULTIPLIER =
            0.48f

        private const val LIGHTNING_CHAIN_RANGE =
            240f

        private const val METEOR_RADIUS =
            155f

        private const val METEOR_DAMAGE_MULTIPLIER =
            2.45f

        private const val METEOR_BURNED_SECONDS =
            5f

        private const val TSUNAMI_SPEED =
            620f

        private const val TSUNAMI_FRONT_LENGTH =
            760f

        private const val TSUNAMI_FRONT_THICKNESS =
            118f

        private const val TSUNAMI_DAMAGE_MULTIPLIER =
            1.28f

        private const val TSUNAMI_WET_SECONDS =
            5f

        private const val TSUNAMI_KNOCKBACK_STRENGTH =
            8f
    }
}
