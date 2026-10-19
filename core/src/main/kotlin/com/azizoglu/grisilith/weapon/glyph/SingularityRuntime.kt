package com.azizoglu.grisilith.weapon.glyph

import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.enemy.EnemyDamageContext
import com.azizoglu.grisilith.enemy.EnemyManager
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import kotlin.math.roundToInt

class SingularityRuntime(
    private val damageCalculator:
    WeaponDamageCalculator
) {

    data class VisualSnapshot(
        val centerX: Float,
        val centerY: Float,
        val radius: Float,
        val elapsedSeconds: Float,
        val openingProgress: Float,
        val collapseProgress: Float,
        val isCollapsing: Boolean
    )

    data class ExplosionVisualSnapshot(
        val centerX: Float,
        val centerY: Float,
        val radius: Float,
        val progress: Float
    )

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
                    activeDurationSeconds(request)
            )

        syncAudio()
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

        syncAudio()
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
        syncAudio()
    }

    private fun syncAudio() {
        val kind=when {
            active?.phase==Phase.ACTIVE->com.azizoglu.grisilith.audio.UiSound.GLYPH_SINGULARITY
            active!=null->com.azizoglu.grisilith.audio.UiSound.SINGULARITY_END
            explosionVisualRemainingSeconds>0f->com.azizoglu.grisilith.audio.UiSound.SINGULARITY_EXPLOSION
            else->null
        }
        val source=visualSnapshot()?.let{com.azizoglu.grisilith.audio.AudioSource(it.centerX,it.centerY,it.radius)}
            ?:explosionVisualSnapshot()?.let{com.azizoglu.grisilith.audio.AudioSource(it.centerX,it.centerY,it.radius)}
        com.azizoglu.grisilith.audio.GameAudio.sustain(this,kind,kind==com.azizoglu.grisilith.audio.UiSound.GLYPH_SINGULARITY,source)
    }

    fun visualSnapshot():
        VisualSnapshot? {

        val singularity =
            active
                ?: return null

        val collapseProgress =
            if (
                singularity.phase ==
                Phase.COLLAPSE
            ) {
                (
                    1f -
                        singularity.collapseRemainingSeconds /
                        COLLAPSE_DURATION_SECONDS
                    )
                    .coerceIn(
                        0f,
                        1f
                    )
            } else {
                0f
            }

        return VisualSnapshot(
            centerX =
                singularity.request.centerX,

            centerY =
                singularity.request.centerY,

            radius =
                pullRadius(singularity),

            elapsedSeconds =
                singularity.elapsedSeconds,

            openingProgress =
                (
                    singularity.elapsedSeconds /
                        OPENING_VISUAL_SECONDS
                    )
                    .coerceIn(
                        0f,
                        1f
                    ),

            collapseProgress =
                collapseProgress,

            isCollapsing =
                singularity.phase ==
                    Phase.COLLAPSE
        )
    }

    fun explosionVisualSnapshot():
        ExplosionVisualSnapshot? {

        if (
            explosionVisualRemainingSeconds <=
            0f
        ) {
            return null
        }

        return ExplosionVisualSnapshot(
            centerX =
                explosionVisualCenter.x,

            centerY =
                explosionVisualCenter.y,

            radius =
                explosionVisualRadius,

            progress =
                (
                    1f -
                        explosionVisualRemainingSeconds /
                        EXPLOSION_VISUAL_SECONDS
                    )
                    .coerceIn(
                        0f,
                        1f
                    )
        )
    }

    @Suppress("UNUSED_PARAMETER")
    fun render(
        shapeRenderer: ShapeRenderer
    ) {
        // Sprite visual is rendered by SpellFocusEffectSpriteRenderer.
    }

    fun clear() {
        com.azizoglu.grisilith.audio.GameAudio.stopSustain(this)
        active =
            null

        explosionVisualCenter.set(
            0f,
            0f
        )

        explosionVisualRadius =
            0f

        explosionVisualRemainingSeconds =
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
            .weaponTargets
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

                val pullStrength =
                    when {
                        distance <=
                            BASE_RADIUS ->
                            INNER_ENEMY_PULL_SPEED

                        distance <=
                            pullRadius(singularity) ->
                            OUTER_ENEMY_PULL_SPEED

                        else ->
                            return@forEach
                    }

                if(enemy.type==EnemyType.MINI_BOSS || enemy.type==EnemyType.FINAL_BOSS) {
                    enemy.applyBossGravity("singularity:${singularity.request.sourceWeapon.runtimeId}",
                        singularity.request.centerX,singularity.request.centerY)
                } else enemy.pullToward(
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

        val damageTickSeconds = damageTickSeconds(singularity)

        while (
            singularity.tickAccumulator >=
            damageTickSeconds
        ) {
            singularity.tickAccumulator -=
                damageTickSeconds

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
            distance <=
            1f
        ) {
            return
        }

        val pullSpeed =
            when {
                distance <=
                    BASE_RADIUS ->
                    INNER_ENEMY_PULL_SPEED *
                        PLAYER_PULL_RATIO

                distance <=
                    pullRadius(singularity) ->
                    OUTER_ENEMY_PULL_SPEED *
                        PLAYER_PULL_RATIO

                else ->
                    return
            }

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

        enemyManager
            .weaponTargets
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
                        damageCalculator.calculateMaximumHealthSpellTickDamage(
                            enemy.health.maximumHealth,
                            DamageType.NECROTIC,
                            TICK_DAMAGE_MULTIPLIER
                        ),

                    singularity =
                        singularity,

                    attackName =
                        "Singularity",
                    defenseIntervalSeconds = damageTickSeconds(singularity),

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

        val effectiveDeathCount =
            deathCount
                .coerceAtLeast(
                    1
                )

        val explosionRadius =
            (
                baseExplosionRadius(singularity) +
                    deathCount *
                    EXPLOSION_RADIUS_GROWTH_PER_DEATH
                )
                .coerceAtMost(
                    maximumExplosionRadius(singularity)
                )

        val explosionDamage =
            (
                damageCalculator
                    .calculateSpellSkillDamage(
                        weapon =
                            singularity
                                .request
                                .sourceWeapon,

                        damageType =
                            DamageType.NECROTIC
                    ) *
                    effectiveDeathCount *
                    EXPLOSION_DAMAGE_PER_DEATH_MULTIPLIER
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
        onEnemyKilled: (Enemy) -> Unit,
        defenseIntervalSeconds: Float = 1f
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
                        defenseTimeScale = defenseIntervalSeconds,
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

    private fun isUpgraded(singularity: ActiveSingularity): Boolean =
        singularity.request.sourceWeapon.hasWeaponAbilityUpgrade

    private fun activeDurationSeconds(request: GlyphCastRequest): Float =
        if (request.sourceWeapon.hasWeaponAbilityUpgrade) {
            UPGRADED_ACTIVE_DURATION_SECONDS
        } else {
            ACTIVE_DURATION_SECONDS
        }

    private fun pullRadius(singularity: ActiveSingularity): Float =
        if (isUpgraded(singularity)) UPGRADED_PULL_RADIUS else BASE_RADIUS

    private fun damageTickSeconds(singularity: ActiveSingularity): Float =
        if (isUpgraded(singularity)) UPGRADED_DAMAGE_TICK_SECONDS else DAMAGE_TICK_SECONDS

    private fun baseExplosionRadius(singularity: ActiveSingularity): Float =
        if (isUpgraded(singularity)) UPGRADED_BASE_EXPLOSION_RADIUS else BASE_EXPLOSION_RADIUS

    private fun maximumExplosionRadius(singularity: ActiveSingularity): Float =
        if (isUpgraded(singularity)) UPGRADED_MAXIMUM_EXPLOSION_RADIUS else MAXIMUM_EXPLOSION_RADIUS

    companion object {

        private const val ACTIVE_DURATION_SECONDS =
            10f

        private const val UPGRADED_ACTIVE_DURATION_SECONDS =
            13f

        private const val COLLAPSE_DURATION_SECONDS =
            1.20f

        private const val DAMAGE_TICK_SECONDS =
            0.333f

        private const val UPGRADED_DAMAGE_TICK_SECONDS =
            0.25f

        private const val BASE_RADIUS =
            222f

        private const val UPGRADED_PULL_RADIUS =
            300f

        private const val INNER_ENEMY_PULL_SPEED =
            310f

        private const val OUTER_ENEMY_PULL_SPEED =
            125f

        private const val PLAYER_PULL_RATIO =
            0.40f

        private const val TICK_DAMAGE_MULTIPLIER =
            0.00020f

        private const val BASE_EXPLOSION_RADIUS =
            250f

        private const val UPGRADED_BASE_EXPLOSION_RADIUS =
            300f

        private const val EXPLOSION_RADIUS_GROWTH_PER_DEATH =
            33f

        private const val MAXIMUM_EXPLOSION_RADIUS =
            750f

        private const val UPGRADED_MAXIMUM_EXPLOSION_RADIUS =
            1500f

        private const val EXPLOSION_DAMAGE_PER_DEATH_MULTIPLIER =
            1.55f

        private const val PROJECTILE_BEND_RADIUS =
            280f

        private const val PROJECTILE_BEND_STRENGTH =
            0.82f

        private const val EXPLOSION_VISUAL_SECONDS =
            0.90f

        private const val OPENING_VISUAL_SECONDS =
            0.78f
    }
}
