package com.azizoglu.grisilith.weapon.skill

import com.azizoglu.grisilith.content.ConditionType
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyManager
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.azizoglu.grisilith.weapon.runtime.WeaponInstance
import com.azizoglu.grisilith.weapon.runtime.WeaponRuntime
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import kotlin.math.roundToInt

class IgniteDashRuntime(
    playerStats: PlayerStats
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

            shapeRenderer.circle(
                resolvedTargetPosition.x,
                resolvedTargetPosition.y,
                AIM_MARKER_RADIUS,
                32
            )

            val remainingRatio =
                (
                    remainingAimSeconds /
                        DefaultWeaponAbilities
                            .IGNITE_DASH_AIM_DURATION_SECONDS
                    )
                    .coerceIn(
                        0f,
                        1f
                    )

            shapeRenderer.color =
                Color(
                    1f,
                    0.72f,
                    0.16f,
                    0.86f
                )

            shapeRenderer.circle(
                resolvedTargetPosition.x,
                resolvedTargetPosition.y,
                AIM_MARKER_RADIUS *
                    remainingRatio,
                32
            )
        }

        if (
            explosionRemainingSeconds >
            0f
        ) {
            val progress =
                1f -
                    (
                        explosionRemainingSeconds /
                            EXPLOSION_VISUAL_SECONDS
                        )
                        .coerceIn(
                            0f,
                            1f
                        )

            shapeRenderer.color =
                Color(
                    1f,
                    0.22f,
                    0.04f,
                    (
                        1f -
                            progress
                        )
                        .coerceIn(
                            0f,
                            1f
                        )
                )

            shapeRenderer.circle(
                explosionCenter.x,
                explosionCenter.y,
                DefaultWeaponAbilities
                    .IGNITE_DASH_EXPLOSION_RADIUS *
                    progress,
                48
            )
        }
    }

    private fun resolveTargetPosition(
        player: Player,
        obstacleBounds: List<Rectangle>,
        worldWidth: Float,
        worldHeight: Float
    ) {
        val desiredTarget =
            if (
                hasAimPosition
            ) {
                requestedAimPosition
            } else {
                Vector2(
                    player.centerX +
                        DefaultWeaponAbilities
                            .IGNITE_DASH_MAXIMUM_RANGE,

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
                    DefaultWeaponAbilities
                        .IGNITE_DASH_MAXIMUM_RANGE
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
                .calculate(
                    weapon
                )
                .damage

        val explosionDamage =
            (
                baseDamage *
                    DefaultWeaponAbilities
                        .IGNITE_DASH_DAMAGE_MULTIPLIER
                )
                .roundToInt()
                .coerceAtLeast(
                    1
                )

        enemyManager.allEnemies
            .toList()
            .forEach {
                    enemy ->

                if (
                    enemy.isDead ||
                    Vector2.dst(
                        explosionCenter.x,
                        explosionCenter.y,
                        enemy.centerX,
                        enemy.centerY
                    ) >
                    DefaultWeaponAbilities
                        .IGNITE_DASH_EXPLOSION_RADIUS
                ) {
                    return@forEach
                }

                val wasAlive =
                    !enemy.isDead

                val appliedDamage =
                    enemy.takeDamage(
                        explosionDamage
                    )

                if (
                    appliedDamage >
                    0
                ) {
                    enemy.applyCondition(
                        conditionType =
                            ConditionType.BURNED,

                        durationSeconds =
                            DefaultWeaponAbilities
                                .IGNITE_DASH_BURN_DURATION_SECONDS
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
            0.42f
    }
}
