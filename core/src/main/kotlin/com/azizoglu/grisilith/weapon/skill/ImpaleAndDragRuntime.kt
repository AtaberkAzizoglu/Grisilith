package com.azizoglu.grisilith.weapon.skill

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.weapon.runtime.WeaponRuntime
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import kotlin.math.floor
import kotlin.math.roundToInt

class ImpaleAndDragRuntime {

    private var owningWeaponRuntime:
        WeaponRuntime? =
        null

    private var phase =
        ImpaleAndDragPhase.INACTIVE

    private var remainingDurationSeconds =
        0f

    private var travelledDistance =
        0f

    private val previousPlayerCenter =
        Vector2()

    private val impaledCorpses =
        mutableListOf<ImpaledCorpse>()

    private var currentSecondWindow =
        0f

    private var impalesInCurrentSecond =
        0

    private var pendingReward:
        ImpaleAndDragReward? =
        null

    val isActive:
        Boolean
        get() =
            phase !=
                ImpaleAndDragPhase.INACTIVE

    val currentPhase:
        ImpaleAndDragPhase
        get() =
            phase

    val impaledEnemyCount:
        Int
        get() =
            impaledCorpses.size

    fun tryBegin(
        weaponRuntime: WeaponRuntime,
        player: Player
    ): Boolean {
        if (
            isActive ||
            weaponRuntime.abilityState !=
            WeaponAbilityState.READY
        ) {
            return false
        }

        val activated =
            weaponRuntime
                .tryActivateAbility()

        if (!activated) {
            return false
        }

        owningWeaponRuntime =
            weaponRuntime

        remainingDurationSeconds =
            DefaultWeaponAbilities
                .IMPALE_AND_DRAG_DURATION_SECONDS

        travelledDistance =
            0f

        currentSecondWindow =
            0f

        impalesInCurrentSecond =
            0

        impaledCorpses.clear()

        pendingReward =
            null

        previousPlayerCenter.set(
            player.centerX,
            player.centerY
        )

        phase =
            if (
                player.actualMovementSpeed <=
                DefaultWeaponAbilities
                    .IMPALE_AND_DRAG_MOVEMENT_THRESHOLD
            ) {
                ImpaleAndDragPhase
                    .WAITING_FOR_MOVEMENT
            } else {
                ImpaleAndDragPhase
                    .WAITING_FOR_STOP
            }

        return true
    }

    fun update(
        delta: Float,
        player: Player,
        harvestMultiplier: Float
    ) {
        if (!isActive) {
            return
        }

        val safeDelta =
            delta.coerceAtLeast(
                0f
            )

        remainingDurationSeconds =
            (
                remainingDurationSeconds -
                    safeDelta
                )
                .coerceAtLeast(
                    0f
                )

        currentSecondWindow +=
            safeDelta

        while (
            currentSecondWindow >=
            1f
        ) {
            currentSecondWindow -=
                1f

            impalesInCurrentSecond =
                0
        }

        val isMoving =
            player.actualMovementSpeed >
                DefaultWeaponAbilities
                    .IMPALE_AND_DRAG_MOVEMENT_THRESHOLD

        when (
            phase
        ) {
            ImpaleAndDragPhase
                .WAITING_FOR_STOP -> {

                if (!isMoving) {
                    phase =
                        ImpaleAndDragPhase
                            .WAITING_FOR_MOVEMENT

                    previousPlayerCenter.set(
                        player.centerX,
                        player.centerY
                    )
                }
            }

            ImpaleAndDragPhase
                .WAITING_FOR_MOVEMENT -> {

                if (isMoving) {
                    phase =
                        ImpaleAndDragPhase
                            .DRAGGING

                    previousPlayerCenter.set(
                        player.centerX,
                        player.centerY
                    )
                }
            }

            ImpaleAndDragPhase
                .DRAGGING -> {

                if (!isMoving) {
                    finish(
                        harvestMultiplier
                    )

                    return
                }

                travelledDistance +=
                    Vector2.dst(
                        previousPlayerCenter.x,
                        previousPlayerCenter.y,
                        player.centerX,
                        player.centerY
                    )

                previousPlayerCenter.set(
                    player.centerX,
                    player.centerY
                )
            }

            ImpaleAndDragPhase
                .INACTIVE -> {
            }
        }

        if (
            remainingDurationSeconds <=
            0f
        ) {
            finish(
                harvestMultiplier
            )
        }
    }

    fun onEnemyKilled(
        enemy: Enemy
    ): Boolean {
        if (
            phase !=
            ImpaleAndDragPhase.DRAGGING ||
            travelledDistance <
            DefaultWeaponAbilities
                .IMPALE_AND_DRAG_MINIMUM_TRAVEL_DISTANCE ||
            impaledCorpses.size >=
            DefaultWeaponAbilities
                .IMPALE_AND_DRAG_MAXIMUM_CORPSES ||
            impalesInCurrentSecond >=
            DefaultWeaponAbilities
                .IMPALE_AND_DRAG_MAX_IMPALES_PER_SECOND ||
            enemy.type ==
            EnemyType.FINAL_BOSS
        ) {
            return false
        }

        impaledCorpses.add(
            ImpaledCorpse(
                enemyType =
                    enemy.type,

                originalGoldReward =
                    enemy.goldReward.toInt(),

                orderIndex =
                    impaledCorpses.size
            )
        )

        impalesInCurrentSecond++

        return true
    }

    fun consumeReward():
        ImpaleAndDragReward? {

        val reward =
            pendingReward

        pendingReward =
            null

        return reward
    }

    fun cancelForStageTransition(
        harvestMultiplier: Float
    ) {
        if (isActive) {
            finish(
                harvestMultiplier
            )
        }
    }

    fun reset() {
        owningWeaponRuntime =
            null

        phase =
            ImpaleAndDragPhase.INACTIVE

        remainingDurationSeconds =
            0f

        travelledDistance =
            0f

        impaledCorpses.clear()

        currentSecondWindow =
            0f

        impalesInCurrentSecond =
            0

        pendingReward =
            null
    }

    fun render(
        shapeRenderer: ShapeRenderer,
        player: Player
    ) {
        if (!isActive) {
            return
        }

        val phaseColor =
            when (
                phase
            ) {
                ImpaleAndDragPhase
                    .WAITING_FOR_STOP -> {
                    Color(
                        0.86f,
                        0.62f,
                        0.24f,
                        0.88f
                    )
                }

                ImpaleAndDragPhase
                    .WAITING_FOR_MOVEMENT -> {
                    Color(
                        0.52f,
                        0.72f,
                        0.92f,
                        0.88f
                    )
                }

                ImpaleAndDragPhase
                    .DRAGGING -> {
                    Color(
                        0.82f,
                        0.24f,
                        0.20f,
                        0.92f
                    )
                }

                ImpaleAndDragPhase
                    .INACTIVE -> {
                    Color.CLEAR
                }
            }

        shapeRenderer.color =
            phaseColor

        shapeRenderer.circle(
            player.centerX,
            player.centerY,
            30f,
            32
        )

        impaledCorpses
            .forEachIndexed {
                    index,
                    corpse ->

                val row =
                    index /
                        CORPSES_PER_ROW

                val column =
                    index %
                        CORPSES_PER_ROW

                val offsetX =
                    (
                        column -
                            (
                                CORPSES_PER_ROW -
                                    1
                                ) /
                            2f
                        ) *
                        CORPSE_SPACING

                val offsetY =
                    -48f -
                        row *
                        CORPSE_SPACING

                shapeRenderer.color =
                    corpseColor(
                        corpse.enemyType
                    )

                shapeRenderer.circle(
                    player.centerX +
                        offsetX,

                    player.centerY +
                        offsetY,

                    CORPSE_MARKER_RADIUS,
                    16
                )

                shapeRenderer.color =
                    Color(
                        0.72f,
                        0.12f,
                        0.12f,
                        0.95f
                    )

                shapeRenderer.rectLine(
                    player.centerX +
                        offsetX -
                        7f,

                    player.centerY +
                        offsetY -
                        7f,

                    player.centerX +
                        offsetX +
                        7f,

                    player.centerY +
                        offsetY +
                        7f,

                    3f
                )

                shapeRenderer.rectLine(
                    player.centerX +
                        offsetX -
                        7f,

                    player.centerY +
                        offsetY +
                        7f,

                    player.centerX +
                        offsetX +
                        7f,

                    player.centerY +
                        offsetY -
                        7f,

                    3f
                )
            }
    }

    private fun finish(
        harvestMultiplier: Float
    ) {
        val corpseCount =
            impaledCorpses.size

        val safeHarvestMultiplier =
            harvestMultiplier
                .coerceAtLeast(
                    1f
                )

        val extraGold =
            (
                corpseCount *
                    DefaultWeaponAbilities
                        .IMPALE_AND_DRAG_GOLD_PER_CORPSE *
                    safeHarvestMultiplier
                )
                .roundToInt()
                .coerceAtLeast(
                    0
                )

        val chestChance =
            (
                corpseCount *
                    DefaultWeaponAbilities
                        .IMPALE_AND_DRAG_BASE_CHEST_CHANCE_PER_CORPSE *
                    safeHarvestMultiplier
                )
                .coerceAtMost(
                    DefaultWeaponAbilities
                        .IMPALE_AND_DRAG_MAXIMUM_CHEST_CHANCE
                )

        val chestRewardCount =
            if (
                corpseCount >
                0 &&
                MathUtils.random() <
                chestChance
            ) {
                1
            } else {
                0
            }

        pendingReward =
            ImpaleAndDragReward(
                impaledEnemyCount =
                    corpseCount,

                extraGold =
                    extraGold,

                chestRewardCount =
                    chestRewardCount
            )

        owningWeaponRuntime
            ?.endActiveAbilityEarly()

        owningWeaponRuntime =
            null

        phase =
            ImpaleAndDragPhase.INACTIVE

        remainingDurationSeconds =
            0f

        travelledDistance =
            0f

        impaledCorpses.clear()

        currentSecondWindow =
            0f

        impalesInCurrentSecond =
            0
    }

    private fun corpseColor(
        enemyType:
        EnemyType
    ): Color {
        return when (
            enemyType
        ) {
            EnemyType.NORMAL -> {
                Color(
                    0.50f,
                    0.50f,
                    0.54f,
                    0.95f
                )
            }

            EnemyType.UNIQUE -> {
                Color(
                    0.34f,
                    0.64f,
                    0.94f,
                    0.95f
                )
            }

            EnemyType.MINI_BOSS -> {
                Color(
                    0.88f,
                    0.62f,
                    0.18f,
                    0.95f
                )
            }

            EnemyType.FINAL_BOSS -> {
                Color(
                    0.84f,
                    0.16f,
                    0.16f,
                    0.95f
                )
            }
        }
    }

    companion object {
        private const val CORPSES_PER_ROW =
            5

        private const val CORPSE_SPACING =
            22f

        private const val CORPSE_MARKER_RADIUS =
            8f
    }
}
