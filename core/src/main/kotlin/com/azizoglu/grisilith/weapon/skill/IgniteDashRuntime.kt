package com.azizoglu.grisilith.weapon.skill

import com.azizoglu.grisilith.content.ConditionType
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.combat.condition.ConditionDamageCalculator
import com.azizoglu.grisilith.enemy.EnemyDamageContext
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyManager
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.azizoglu.grisilith.weapon.runtime.WeaponInstance
import com.azizoglu.grisilith.weapon.runtime.WeaponRuntime
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import kotlin.math.roundToInt

class IgniteDashRuntime(
    private val playerStats: PlayerStats
) {

    private val damageCalculator =
        WeaponDamageCalculator(
            playerStats
        )

    private var activeWeapon:
        WeaponInstance? =
        null

    private var activeWeaponRuntime:
        WeaponRuntime? =
        null

    private var remainingAimSeconds =
        0f

    private var executionRequested =
        false

    private var mouseConfirmBlockedUntilRelease =
        false

    private val requestedAimPosition =
        Vector2()

    private val resolvedTargetPosition =
        Vector2()

    private var hasAimPosition =
        false

    private var explosionRemainingSeconds =
        0f

    private val explosionCenter =
        Vector2()

    val isAiming:
        Boolean
        get() =
            activeWeapon !=
                null

    val hasExplosionVisual: Boolean
        get() =
            explosionRemainingSeconds >
                0f

    val explosionVisualCenterX: Float
        get() =
            explosionCenter.x

    val explosionVisualCenterY: Float
        get() =
            explosionCenter.y

    val aimVisualCenterX: Float
        get() =
            resolvedTargetPosition.x

    val aimVisualCenterY: Float
        get() =
            resolvedTargetPosition.y

    val explosionVisualProgress: Float
        get() {
            if (
                explosionRemainingSeconds <=
                0f
            ) {
                return 1f
            }

            return (
                1f -
                    explosionRemainingSeconds /
                    EXPLOSION_VISUAL_SECONDS
                )
                .coerceIn(
                    0f,
                    1f
                )
        }

    fun updateAimPosition(
        worldX: Float,
        worldY: Float
    ) {
        requestedAimPosition.set(
            worldX,
            worldY
        )

        hasAimPosition =
            true
    }

    fun handleActivation(
        weapon: WeaponInstance,
        weaponRuntime: WeaponRuntime
    ): Boolean {

        val ability =
            weapon.unlockedAbility
                ?: return false

        if (
            ability.id !=
            DefaultWeaponAbilities
                .IGNITE_DASH_ID
        ) {
            return weaponRuntime
                .tryActivateAbility()
        }

        if (
            activeWeapon ===
            weapon
        ) {
            executionRequested =
                true

            return true
        }

        if (
            isAiming
        ) {
            return false
        }

        val activated =
            weaponRuntime
                .tryActivateAbility()

        if (!activated) {
            return false
        }

        activeWeapon =
            weapon

        activeWeaponRuntime =
            weaponRuntime

        remainingAimSeconds =
            DefaultWeaponAbilities
                .IGNITE_DASH_AIM_DURATION_SECONDS

        executionRequested =
            false

        mouseConfirmBlockedUntilRelease =
            Gdx.input
                .isButtonPressed(
                    Input.Buttons.LEFT
                )

        hasAimPosition =
            false

        return true
    }

    fun update(
        delta: Float,
        player: Player,
        enemyManager: EnemyManager,
        obstacleBounds: List<Rectangle>,
        worldWidth: Float,
        worldHeight: Float,
        onDamageDealt: (
            damage: Int,
            weaponName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
    ) {
        updateExplosion(
            delta
        )

        val weapon =
            activeWeapon
                ?: return

        val runtime =
            activeWeaponRuntime
                ?: run {
                    cancel()
                    return
                }

        remainingAimSeconds =
            (
                remainingAimSeconds -
                    delta.coerceAtLeast(
                        0f
                    )
                )
                .coerceAtLeast(
                    0f
                )

        resolveTargetPosition(
            player =
                player,

            obstacleBounds =
                obstacleBounds,

            worldWidth =
                worldWidth,

            worldHeight =
                worldHeight
        )

        if (
            mouseConfirmBlockedUntilRelease
        ) {
            if (
                !Gdx.input
                    .isButtonPressed(
                        Input.Buttons.LEFT
                    )
            ) {
                mouseConfirmBlockedUntilRelease =
                    false
            }
        } else if (
            Gdx.input
                .isButtonJustPressed(
                    Input.Buttons.LEFT
                )
        ) {
            executionRequested =
                true
        }

        if (
            executionRequested ||
            remainingAimSeconds <=
            0f
        ) {
            executeTeleportAndExplosion(
                weapon =
                    weapon,

                weaponRuntime =
                    runtime,

                player =
                    player,

                enemyManager =
                    enemyManager,

                onDamageDealt =
                    onDamageDealt,

                onEnemyKilled =
                    onEnemyKilled
            )
        }
    }

    fun cancel() {
        activeWeaponRuntime
            ?.endActiveAbilityEarly()

        activeWeapon =
            null

        activeWeaponRuntime =
            null

        remainingAimSeconds =
            0f

        executionRequested =
            false

        mouseConfirmBlockedUntilRelease =
            false

        hasAimPosition =
            false
    }

    fun reset() {
        activeWeapon =
            null

        activeWeaponRuntime =
            null

        remainingAimSeconds =
            0f

        executionRequested =
            false

        mouseConfirmBlockedUntilRelease =
            false

        hasAimPosition =
            false

        explosionRemainingSeconds =
            0f
    }

    fun render(
        shapeRenderer: ShapeRenderer,
        player: Player
    ) {
        if (isAiming) {
            shapeRenderer.color =
                Color(
                    1f,
                    0.36f,
                    0.08f,
                    0.88f
                )

            shapeRenderer.rectLine(
                player.centerX,
                player.centerY,
                resolvedTargetPosition.x,
                resolvedTargetPosition.y,
                4f
            )
        }

        /*
         * Full 250 px damage-area casting marker and the destination
         * explosion are rendered by SpellFocusEffectSpriteRenderer.
         */
    }

    private fun resolveTargetPosition(
        player: Player,
        obstacleBounds: List<Rectangle>,
        worldWidth: Float,
        worldHeight: Float
    ) {
        val maximumRange =
            if (activeWeapon?.hasWeaponAbilityUpgrade == true) {
                DefaultWeaponAbilities.IGNITE_DASH_UPGRADED_MAXIMUM_RANGE
            } else {
                DefaultWeaponAbilities.IGNITE_DASH_MAXIMUM_RANGE
            }

        val desiredTarget =
            if (
                hasAimPosition
            ) {
                requestedAimPosition
            } else {
                Vector2(
                    player.centerX +
                        maximumRange,

                    player.centerY
                )
            }

        val direction =
            Vector2(
                desiredTarget.x -
                    player.centerX,

                desiredTarget.y -
                    player.centerY
            )

        if (
            direction.isZero(
                0.001f
            )
        ) {
            direction.set(
                1f,
                0f
            )
        }

        val distance =
            direction.len()
                .coerceAtMost(
                    maximumRange
                )

        direction.nor()

        val playerWidth =
            player.bounds.width

        val playerHeight =
            player.bounds.height

        val halfWidth =
            playerWidth /
                2f

        val halfHeight =
            playerHeight /
                2f

        val requestedX =
            (
                player.centerX +
                    direction.x *
                    distance
                )
                .coerceIn(
                    halfWidth,
                    worldWidth -
                        halfWidth
                )

        val requestedY =
            (
                player.centerY +
                    direction.y *
                    distance
                )
                .coerceIn(
                    halfHeight,
                    worldHeight -
                        halfHeight
                )

        val candidateBounds =
            Rectangle()

        var candidateDistance =
            Vector2(
                requestedX -
                    player.centerX,

                requestedY -
                    player.centerY
            )
                .len()

        while (
            candidateDistance >
            0f
        ) {
            val candidateX =
                player.centerX +
                    direction.x *
                    candidateDistance

            val candidateY =
                player.centerY +
                    direction.y *
                    candidateDistance

            candidateBounds.set(
                candidateX -
                    halfWidth,

                candidateY -
                    halfHeight,

                playerWidth,
                playerHeight
            )

            val overlapsObstacle =
                obstacleBounds.any {
                        obstacle ->
                    candidateBounds
                        .overlaps(
                            obstacle
                        )
                }

            if (!overlapsObstacle) {
                resolvedTargetPosition.set(
                    candidateX,
                    candidateY
                )

                return
            }

            candidateDistance -=
                TARGET_BACKTRACK_STEP
        }

        resolvedTargetPosition.set(
            player.centerX,
            player.centerY
        )
    }

    private fun executeTeleportAndExplosion(
        weapon: WeaponInstance,
        weaponRuntime: WeaponRuntime,
        player: Player,
        enemyManager: EnemyManager,
        onDamageDealt: (
            damage: Int,
            weaponName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
    ) {
        player.teleportCenterTo(
            targetCenterX =
                resolvedTargetPosition.x,

            targetCenterY =
                resolvedTargetPosition.y
        )

        explosionCenter.set(
            player.centerX,
            player.centerY
        )

        explosionRemainingSeconds =
            EXPLOSION_VISUAL_SECONDS

        val baseDamage =
            damageCalculator
                .calculateSpellSkillDamage(
                    weapon =
                        weapon,

                    damageType =
                        DamageType.FIRE
                )

        val innerExplosionDamage =
            (
                baseDamage *
                    if (weapon.hasWeaponAbilityUpgrade) {
                        DefaultWeaponAbilities.IGNITE_DASH_UPGRADED_DAMAGE_MULTIPLIER
                    } else {
                        DefaultWeaponAbilities.IGNITE_DASH_DAMAGE_MULTIPLIER
                    }
                )
                .roundToInt()
                .coerceAtLeast(
                    1
                )

        val outerExplosionDamage =
            (
                innerExplosionDamage *
                    DefaultWeaponAbilities
                        .IGNITE_DASH_OUTER_DAMAGE_RATIO
                )
                .roundToInt()
                .coerceAtLeast(
                    1
                )

        enemyManager.allEnemies
            .toList()
            .forEach {
                    enemy ->

                val distance =
                    Vector2.dst(
                        explosionCenter.x,
                        explosionCenter.y,
                        enemy.centerX,
                        enemy.centerY
                    )

                if (
                    enemy.isDead ||
                    distance >
                    DefaultWeaponAbilities
                        .IGNITE_DASH_OUTER_EXPLOSION_RADIUS
                ) {
                    return@forEach
                }

                val damageForDistance =
                    if (
                        distance <=
                        DefaultWeaponAbilities
                            .IGNITE_DASH_EXPLOSION_RADIUS
                    ) {
                        innerExplosionDamage
                    } else {
                        outerExplosionDamage
                    }

                val wasAlive =
                    !enemy.isDead

                val appliedDamage =
                    enemy.takeDamage(
                        amount =
                            damageForDistance,

                        damageContext =
                            EnemyDamageContext(
                                damageType =
                                    DamageType.FIRE,

                                sourceWeaponRuntimeId =
                                    weapon.runtimeId,

                                sourceWeaponDefinitionId =
                                    weapon.definition.id.value,

                                sourceName =
                                    weapon.displayName +
                                        " — Ignite Dash"
                            )
                    )

                if (
                    appliedDamage >
                    0
                ) {
                    enemy.applyCondition(
                        ConditionDamageCalculator
                            .createApplication(
                                type =
                                    ConditionType.BURNED,

                                playerStats =
                                    playerStats,

                                durationSeconds =
                                    DefaultWeaponAbilities
                                        .IGNITE_DASH_BURN_DURATION_SECONDS,

                                sourceWeaponRuntimeId =
                                    weapon.runtimeId,

                                sourceWeaponDefinitionId =
                                    weapon.definition.id.value,

                                sourceName =
                                    weapon.displayName +
                                        " — Ignite Dash",

                                sourceCenterX =
                                    player.centerX,

                                sourceCenterY =
                                    player.centerY
                            )
                    )

                    onDamageDealt(
                        appliedDamage,
                        weapon.displayName
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
            }

        weaponRuntime
            .endActiveAbilityEarly()

        activeWeapon =
            null

        activeWeaponRuntime =
            null

        remainingAimSeconds =
            0f

        executionRequested =
            false

        hasAimPosition =
            false
    }

    private fun updateExplosion(
        delta: Float
    ) {
        explosionRemainingSeconds =
            (
                explosionRemainingSeconds -
                    delta.coerceAtLeast(
                        0f
                    )
                )
                .coerceAtLeast(
                    0f
                )
    }

    companion object {
        private const val TARGET_BACKTRACK_STEP =
            8f

        private const val AIM_MARKER_RADIUS =
            24f

        private const val EXPLOSION_VISUAL_SECONDS =
            0.78f
    }
}
