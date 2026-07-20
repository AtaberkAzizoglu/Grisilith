package com.azizoglu.grisilith.enemy

import kotlin.math.floor
import kotlin.math.roundToInt

object EnemyScaling {

    private const val BASE_HEALTH = 40
    private const val BASE_CONTACT_DAMAGE = 4
    private const val BASE_MOVEMENT_SPEED = 120f

    fun normalEnemyStats(stage: Int): EnemyStats {
        val scaledStats = calculateBaseStats(stage)

        return EnemyStats(
            type = EnemyType.NORMAL,
            maximumHealth = scaledStats.maximumHealth,
            contactDamage = scaledStats.contactDamage,
            movementSpeed = scaledStats.movementSpeed,
            size = 44f
        )
    }

    fun uniqueEnemyStats(stage: Int): EnemyStats {
        val scaledStats = calculateBaseStats(stage)

        return EnemyStats(
            type = EnemyType.UNIQUE,

            maximumHealth =
                (scaledStats.maximumHealth * 3f)
                    .roundToInt(),

            contactDamage =
                (scaledStats.contactDamage * 1.5f)
                    .roundToInt()
                    .coerceAtLeast(1),

            movementSpeed =
                scaledStats.movementSpeed * 1.10f,

            size = 72f
        )
    }

    fun miniBossStats(stage: Int): EnemyStats {
        val scaledStats = calculateBaseStats(stage)

        return EnemyStats(
            type = EnemyType.MINI_BOSS,

            maximumHealth =
                (scaledStats.maximumHealth * 12f)
                    .roundToInt(),

            contactDamage =
                (scaledStats.contactDamage * 2f)
                    .roundToInt()
                    .coerceAtLeast(1),

            movementSpeed =
                scaledStats.movementSpeed * 0.85f,

            size = 128f
        )
    }

    fun finalBossStats(stage: Int): EnemyStats {
        val scaledStats = calculateBaseStats(stage)

        return EnemyStats(
            type = EnemyType.FINAL_BOSS,

            maximumHealth =
                (scaledStats.maximumHealth * 28f)
                    .roundToInt(),

            contactDamage =
                (scaledStats.contactDamage * 2.8f)
                    .roundToInt()
                    .coerceAtLeast(1),

            movementSpeed =
                scaledStats.movementSpeed * 0.72f,

            size = 200f
        )
    }

    private fun calculateBaseStats(
        stage: Int
    ): ScaledBaseStats {
        val safeStage = stage.coerceAtLeast(1)

        val completedStageBlocks =
            floor((safeStage - 1) / 5f)

        val healthMultiplier =
            1f +
                (safeStage - 1) * 0.10f +
                completedStageBlocks * 0.08f

        val damageMultiplier =
            1f +
                (safeStage - 1) * 0.065f +
                completedStageBlocks * 0.06f

        val speedMultiplier =
            1f +
                (safeStage - 1) * 0.004f

        return ScaledBaseStats(
            maximumHealth =
                (BASE_HEALTH * healthMultiplier)
                    .roundToInt()
                    .coerceAtLeast(1),

            contactDamage =
                (BASE_CONTACT_DAMAGE * damageMultiplier)
                    .roundToInt()
                    .coerceAtLeast(1),

            movementSpeed =
                BASE_MOVEMENT_SPEED * speedMultiplier
        )
    }

    private data class ScaledBaseStats(
        val maximumHealth: Int,
        val contactDamage: Int,
        val movementSpeed: Float
    )
}
