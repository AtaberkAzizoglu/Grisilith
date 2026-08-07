package com.azizoglu.grisilith.enemy.runtime

import com.azizoglu.grisilith.economy.GoldRewardScaling
import com.azizoglu.grisilith.enemy.EnemyStats
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.enemy.definition.DefaultEnemyTypeModifiers
import com.azizoglu.grisilith.enemy.definition.EnemyKindDefinition
import kotlin.math.floor
import kotlin.math.roundToInt

/*
 * Enemy Kind temel değerlerini Stage ve EnemyType ile çözer.
 *
 * Sıra:
 * Kind Base Stats
 * -> Stage Scaling
 * -> EnemyType Modifier
 *
 * Scaling bilinçli olarak HP ve Damage tarafında Armor'dan daha hızlıdır.
 * Böylece ileri Stage'lerde Enemy'ler yalnızca hasar süngeri olmaz;
 * oyuncuya verdikleri baskı da artar.
 */
object EnemyKindStatScaler {

    fun resolve(
        definition: EnemyKindDefinition,
        enemyType: EnemyType,
        stage: Int
    ): EnemyStats {

        require(
            definition.supportsType(
                enemyType
            )
        ) {
            "${definition.id} does not support $enemyType."
        }

        val safeStage =
            stage.coerceAtLeast(1)

        val stageScaling =
            stageScaling(
                safeStage
            )

        val typeModifier =
            DefaultEnemyTypeModifiers
                .forType(
                    enemyType
                )

        val scaledHealth =
            (
                definition.baseStats
                    .maximumHealth *
                    stageScaling.healthMultiplier *
                    typeModifier.healthMultiplier
                )
                .roundToInt()
                .coerceAtLeast(1)

        val scaledArmor =
            (
                definition.baseStats
                    .armorClass *
                    stageScaling.armorMultiplier *
                    typeModifier.armorMultiplier
                )
                .roundToInt()
                .coerceAtLeast(0)

        val scaledDamage =
            (
                definition.baseStats
                    .contactDamage *
                    stageScaling.damageMultiplier *
                    typeModifier.damageMultiplier
                )
                .roundToInt()
                .coerceAtLeast(0)

        val scaledMovementSpeed =
            (
                definition.baseStats
                    .movementSpeed *
                    stageScaling.movementSpeedMultiplier *
                    typeModifier.movementSpeedMultiplier
                )
                .coerceAtLeast(0f)

        val scaledAttackSpeed =
            (
                definition.baseStats
                    .attacksPerSecond *
                    stageScaling.attackSpeedMultiplier *
                    typeModifier.attackSpeedMultiplier
                )
                .coerceAtLeast(0f)

        val resolvedSize =
            definition
                .sizeCategory
                .resolvedSize(
                    typeModifier
                        .sizeMultiplier
                )

        val baseReward =
            GoldRewardScaling
                .rewardFor(
                    enemyType =
                        enemyType,

                    stage =
                        safeStage
                )

        return EnemyStats(
            type =
                enemyType,

            maximumHealth =
                scaledHealth,

            contactDamage =
                scaledDamage,

            movementSpeed =
                scaledMovementSpeed,

            size =
                resolvedSize,

            goldReward =
                (
                    baseReward *
                        typeModifier
                            .rewardMultiplier
                    )
                    .coerceAtLeast(0f),

            armorClass =
                scaledArmor,

            attacksPerSecond =
                scaledAttackSpeed,

            attackRange =
                definition.baseStats
                    .attackRange,

            kindId =
                definition.id,

            behaviorId =
                definition.behaviorId,

            traversalProfile =
                definition.traversalProfile
        )
    }

    private fun stageScaling(
        stage: Int
    ): StageScaling {

        val completedStageBlocks =
            floor(
                (stage - 1) /
                    5f
            )

        return StageScaling(
            /*
             * Stage 1  = x1.00
             * Stage 5  ~= x1.68
             * Stage 10 ~= x2.50
             * Stage 20 ~= x4.30
             * Stage 50 ~= x9.02
             */
            healthMultiplier =
                1f +
                    (stage - 1) *
                    0.14f +
                    completedStageBlocks *
                    0.12f,

            /*
             * Armor kontrollü büyür; ileri Stage'lerde hasarı tamamen
             * kapatan bir duvara dönüşmemesi amaçlanır.
             */
            armorMultiplier =
                1f +
                    (stage - 1) *
                    0.04f,

            /*
             * Stage 1  = x1.00
             * Stage 5  ~= x1.40
             * Stage 10 ~= x1.80
             * Stage 20 ~= x2.68
             * Stage 50 ~= x5.64
             */
            damageMultiplier =
                1f +
                    (stage - 1) *
                    0.08f +
                    completedStageBlocks *
                    0.08f,

            /*
             * Hız artışları düşük tutulur; rota sistemi ve kalabalık
             * nedeniyle ileri Stage'lerde kontrolsüz hale gelmez.
             */
            movementSpeedMultiplier =
                1f +
                    (stage - 1) *
                    0.0025f,

            attackSpeedMultiplier =
                1f +
                    (stage - 1) *
                    0.005f
        )
    }

    private data class StageScaling(
        val healthMultiplier: Float,
        val armorMultiplier: Float,
        val damageMultiplier: Float,
        val movementSpeedMultiplier: Float,
        val attackSpeedMultiplier: Float
    )
}
