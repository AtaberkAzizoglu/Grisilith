package com.azizoglu.grisilith.weapon.glyph

import com.azizoglu.grisilith.combat.condition.ConditionDamageCalculator
import com.azizoglu.grisilith.content.ConditionType
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyDamageContext
import com.azizoglu.grisilith.enemy.EnemyManager
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import kotlin.math.roundToInt

class GlyphSpellRuntime(
    private val playerStats: PlayerStats
) {
    var projectileVelocityBender: ((Float, Float, Vector2, Float) -> Unit)? = null

    data class PulseVisualSnapshot(
        val type: GlyphType,
        val centerX: Float,
        val centerY: Float,
        val radius: Float,
        val progress: Float
    )

    data class TsunamiVisualSnapshot(
        val centerX: Float,
        val centerY: Float,
        val rotationDegrees: Float,
        val animationSeconds: Float,
        val worldWidth: Float,
        val worldHeight: Float
    )

    private val damageCalculator =
        WeaponDamageCalculator(
            playerStats
        )

    private val singularityRuntime =
        SingularityRuntime(
            damageCalculator =
                damageCalculator
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
        var visualAnimationSeconds: Float = 0f,
        val center: Vector2 = Vector2(start),
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
        onEnemyKilled: (Enemy) -> Unit,
        manuallyAimed: Boolean = false
    ) {

        when (
            request.glyphType
        ) {
            GlyphType.CALL_LIGHTNING -> {
                val targetedRequest =
                    if (manuallyAimed) request else requestForPriorityTarget(
                        request = request,
                        enemyManager = enemyManager,
                        maximumRange = CALL_LIGHTNING_TARGETING_RANGE
                    ) ?: return

                castCallLightning(
                    targetedRequest,
                    enemyManager,
                    onDamageDealt,
                    onEnemyKilled
                )
            }

            GlyphType.METEOR -> {
                val targetedRequest =
                    if (manuallyAimed) request else requestForPriorityTarget(
                        request = request,
                        enemyManager = enemyManager,
                        maximumRange =
                            if (request.sourceWeapon.hasWeaponAbilityUpgrade) {
                                METEOR_UPGRADED_TARGETING_RANGE
                            } else {
                                METEOR_TARGETING_RANGE
                            }
                    ) ?: return

                castMeteor(
                    targetedRequest,
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
                singularityRuntime.begin(
                    request
                )
            }
        }
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

        singularityRuntime.update(
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

    fun pulseVisualSnapshots():
        List<PulseVisualSnapshot> {

        return pulses
            .map { pulse ->

                PulseVisualSnapshot(
                    type =
                        pulse.type,

                    centerX =
                        pulse.centerX,

                    centerY =
                        pulse.centerY,

                    radius =
                        pulse.radius,

                    progress =
                        (
                            1f -
                                pulse.remainingSeconds /
                                PULSE_DURATION_SECONDS
                            )
                            .coerceIn(
                                0f,
                                1f
                            )
                )
            }
    }

    fun tsunamiVisualSnapshot():
        TsunamiVisualSnapshot? {

        val wave =
            tsunamiWave
                ?: return null

        val center =
            currentTsunamiCenter(
                wave
            )

        return TsunamiVisualSnapshot(
            centerX =
                center.x,

            centerY =
                center.y,

            rotationDegrees =
                wave.direction.angleDeg(),

            animationSeconds =
                wave.visualAnimationSeconds,

            worldWidth =
                TSUNAMI_FRONT_THICKNESS *
                    2.20f,

            worldHeight =
                if (wave.request.sourceWeapon.hasWeaponAbilityUpgrade) {
                    TSUNAMI_UPGRADED_FRONT_LENGTH
                } else {
                    TSUNAMI_FRONT_LENGTH
                }
        )
    }

    fun singularityVisualSnapshot():
        SingularityRuntime.VisualSnapshot? {

        return singularityRuntime
            .visualSnapshot()
    }

    fun singularityExplosionVisualSnapshot():
        SingularityRuntime.ExplosionVisualSnapshot? {

        return singularityRuntime
            .explosionVisualSnapshot()
    }

    @Suppress("UNUSED_PARAMETER")
    fun render(
        shapeRenderer: ShapeRenderer
    ) {
        // Sprite visual is rendered by SpellFocusEffectSpriteRenderer.
    }

    fun clear() {

        pulses.clear()
        tsunamiWave = null
        singularityRuntime.clear()
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
                .calculateSpellSkillDamage(
                    weapon =
                        request.sourceWeapon,

                    damageType =
                        DamageType.LIGHTNING
                )

        val upgraded =
            request.sourceWeapon.hasWeaponAbilityUpgrade

        val innerDamage =
            (
                baseDamage *
                    if (upgraded) {
                        LIGHTNING_UPGRADED_INNER_DAMAGE_MULTIPLIER
                    } else {
                        LIGHTNING_INNER_DAMAGE_MULTIPLIER
                    }
                )
                .roundToInt()
                .coerceAtLeast(
                    1
                )

        val outerDamage =
            (
                innerDamage *
                    LIGHTNING_OUTER_DAMAGE_MULTIPLIER
                )
                .roundToInt()
                .coerceAtLeast(
                    1
                )

        val chainDamage =
            (
                outerDamage *
                    LIGHTNING_CHAIN_DAMAGE_MULTIPLIER
                )
                .roundToInt()
                .coerceAtLeast(
                    1
                )

        val enemies =
            enemyManager
                .weaponTargets
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

                upgraded &&
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
                        .filter { Vector2.dst2(sourceEnemy.centerX, sourceEnemy.centerY,
                            it.centerX, it.centerY) <= LIGHTNING_CHAIN_RANGE * LIGHTNING_CHAIN_RANGE }
                        .minWithOrNull(compareBy<Enemy> { com.azizoglu.grisilith.weapon.runtime.AbilityTargeting.priority(it) }
                            .thenBy { Vector2.dst2(sourceEnemy.centerX, sourceEnemy.centerY, it.centerX, it.centerY) })
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
                    if (upgraded) {
                        LIGHTNING_OUTER_RADIUS
                    } else {
                        LIGHTNING_INNER_RADIUS
                    },

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
                    .calculateSpellSkillDamage(
                        weapon =
                            request.sourceWeapon,

                        damageType =
                            DamageType.FIRE
                    ) *
                    if (request.sourceWeapon.hasWeaponAbilityUpgrade) {
                        METEOR_UPGRADED_DAMAGE_MULTIPLIER
                    } else {
                        METEOR_DAMAGE_MULTIPLIER
                    }
                )
                .roundToInt()
                .coerceAtLeast(
                    1
                )

        enemyManager
            .weaponTargets
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

        val start = when(MathUtils.random(3)) {
            0 -> Vector2(0f,MathUtils.random(0f,worldHeight))
            1 -> Vector2(worldWidth,MathUtils.random(0f,worldHeight))
            2 -> Vector2(MathUtils.random(0f,worldWidth),0f)
            else -> Vector2(MathUtils.random(0f,worldWidth),worldHeight)
        }
        val direction = Vector2(request.centerX,request.centerY).sub(start)
        if(direction.isZero(.001f))direction.set(worldWidth/2f,worldHeight/2f).sub(start)
        direction.nor()
        val perpendicular = Vector2(-direction.y,direction.x)
        val mapDiagonal = Vector2.len(worldWidth,worldHeight)

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

    private fun requestForPriorityTarget(
        request: GlyphCastRequest,
        enemyManager: EnemyManager,
        maximumRange: Float
    ): GlyphCastRequest? {
        val rangeSquared = maximumRange * maximumRange
        val candidates =
            enemyManager.weaponTargets
                .asSequence()
                .filter { enemy ->
                    !enemy.isDead &&
                        Vector2.dst2(
                            request.centerX,
                            request.centerY,
                            enemy.centerX,
                            enemy.centerY
                        ) <= rangeSquared
                }
                .toList()

        if (candidates.isEmpty()) return null

        val target =
            com.azizoglu.grisilith.weapon.runtime.AbilityTargeting.select(
                candidates, request.centerX, request.centerY, maximumRange) ?: return null

        return request.copy(
            centerX = target.centerX,
            centerY = target.centerY
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

        projectileVelocityBender?.invoke(wave.center.x, wave.center.y, wave.direction, delta)
        wave.perpendicular.set(-wave.direction.y, wave.direction.x)
        wave.center.mulAdd(wave.direction, TSUNAMI_SPEED * delta)
        wave.travelledDistance +=
            TSUNAMI_SPEED *
                delta

        wave.visualAnimationSeconds +=
            delta

        val center =
            currentTsunamiCenter(
                wave
            )

        val damage =
            (
                damageCalculator
                    .calculateSpellSkillDamage(
                        weapon =
                            wave.request
                                .sourceWeapon,

                        damageType =
                            DamageType.WATER
                    ) *
                    if (wave.request.sourceWeapon.hasWeaponAbilityUpgrade) {
                        TSUNAMI_UPGRADED_DAMAGE_MULTIPLIER
                    } else {
                        TSUNAMI_DAMAGE_MULTIPLIER
                    }
                )
                .roundToInt()
                .coerceAtLeast(
                    1
                )

        enemyManager
            .weaponTargets
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
                    (if (wave.request.sourceWeapon.hasWeaponAbilityUpgrade) {
                        TSUNAMI_UPGRADED_FRONT_LENGTH
                    } else {
                        TSUNAMI_FRONT_LENGTH
                    }) /
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
                            if (wave.request.sourceWeapon.hasWeaponAbilityUpgrade) {
                                TSUNAMI_UPGRADED_KNOCKBACK_STRENGTH
                            } else {
                                TSUNAMI_KNOCKBACK_STRENGTH
                            }
                    )
                }
            }

        val halfFront = (if(wave.request.sourceWeapon.hasWeaponAbilityUpgrade)TSUNAMI_UPGRADED_FRONT_LENGTH else TSUNAMI_FRONT_LENGTH)/2f
        val halfThickness = TSUNAMI_FRONT_THICKNESS/2f
        val extentX = kotlin.math.abs(wave.perpendicular.x)*halfFront + kotlin.math.abs(wave.direction.x)*halfThickness
        val extentY = kotlin.math.abs(wave.perpendicular.y)*halfFront + kotlin.math.abs(wave.direction.y)*halfThickness
        if (wave.center.x+extentX<0f || wave.center.x-extentX>worldWidth ||
            wave.center.y+extentY<0f || wave.center.y-extentY>worldHeight) {
            tsunamiWave =
                null
        }
    }

    private fun currentTsunamiCenter(
        wave: TsunamiWave
    ): Vector2 {

        return Vector2(wave.center)
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
            0.86f

        private const val LIGHTNING_INNER_RADIUS =
            115f

        private const val LIGHTNING_OUTER_RADIUS =
            240f

        private const val LIGHTNING_INNER_DAMAGE_MULTIPLIER =
            7.5f

        private const val LIGHTNING_UPGRADED_INNER_DAMAGE_MULTIPLIER =
            10.45f

        private const val LIGHTNING_OUTER_DAMAGE_MULTIPLIER =
            0.72f

        private const val LIGHTNING_CHAIN_DAMAGE_MULTIPLIER =
            0.31f

        private const val LIGHTNING_CHAIN_RANGE =
            240f

        private const val CALL_LIGHTNING_TARGETING_RANGE =
            750f

        private const val METEOR_RADIUS =
            185f

        private const val METEOR_DAMAGE_MULTIPLIER =
            10f

        private const val METEOR_UPGRADED_DAMAGE_MULTIPLIER =
            15.55f

        private const val METEOR_TARGETING_RANGE =
            550f

        private const val METEOR_UPGRADED_TARGETING_RANGE =
            700f

        private const val METEOR_BURNED_SECONDS =
            3f

        private const val TSUNAMI_SPEED =
            620f

        private const val TSUNAMI_FRONT_LENGTH =
            550f

        private const val TSUNAMI_UPGRADED_FRONT_LENGTH =
            760f

        private const val TSUNAMI_FRONT_THICKNESS =
            118f

        private const val TSUNAMI_DAMAGE_MULTIPLIER =
            7.5f

        private const val TSUNAMI_UPGRADED_DAMAGE_MULTIPLIER =
            11.11f

        private const val TSUNAMI_WET_SECONDS =
            5f

        private const val TSUNAMI_KNOCKBACK_STRENGTH =
            30f

        private const val TSUNAMI_UPGRADED_KNOCKBACK_STRENGTH =
            50f
    }
}
