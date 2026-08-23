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
 * -> Adaptive Player / Weapon Scaling
 *
 * Base HP ve BaseArmor hiçbir zaman değiştirilmez; yalnız çözülmüş
 * runtime değerleri bunların üstünden hesaplanır.
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

        val adaptiveScaling =
            EnemyAdaptiveScalingRuntime
                .current()

        val scaledHealth =
            (
                definition.baseStats
                    .maximumHealth *
                    stageScaling.healthMultiplier *
                    typeModifier.healthMultiplier *
                    adaptiveScaling.healthMultiplier
                )
                .roundToInt()
                .coerceAtLeast(1)

        val scaledArmor =
            (
                definition.baseStats
                    .armorClass *
                    stageScaling.armorMultiplier *
                    typeModifier.armorMultiplier *
                    adaptiveScaling.armorClassMultiplier
                )
                .roundToInt()
                .coerceAtLeast(0)

        val scaledDamage =
            (
                definition.baseStats
                    .contactDamage *
                    stageScaling.damageMultiplier *
                    typeModifier.damageMultiplier *
                    adaptiveScaling.damageMultiplier
                )
                .roundToInt()
                .coerceAtLeast(0)

        val scaledMovementSpeed =
            (
                definition.baseStats
                    .movementSpeed *
                    stageScaling.movementSpeedMultiplier *
                    typeModifier.movementSpeedMultiplier *
                    adaptiveScaling.movementSpeedMultiplier
                )
                .coerceAtLeast(0f)

        val scaledAttackSpeed =
            (
                definition.baseStats
                    .attacksPerSecond *
                    stageScaling.attackSpeedMultiplier *
                    typeModifier.attackSpeedMultiplier *
                    adaptiveScaling.attackSpeedMultiplier
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

            baseArmor =
                definition.baseStats
                    .baseArmor,

            attacksPerSecond =
                scaledAttackSpeed,

            attackRange =
                definition.baseStats
                    .attackRange,

            weaponDamageMultiplier =
                definition.baseStats
                    .weaponDamageMultiplier *
                    stageScaling.damageMultiplier *
                    typeModifier.damageMultiplier *
                    adaptiveScaling.damageMultiplier,

            attackSpeedScore =
                definition.baseStats
                    .attackSpeedScore,

            mainDamageStat =
                definition.baseStats
                    .mainDamageStat,

            secondaryDamageStat =
                definition.baseStats
                    .secondaryDamageStat,

            baseCritRatePercent =
                definition.baseStats
                    .baseCritRatePercent,

            baseCritDamagePercent =
                definition.baseStats
                    .baseCritDamagePercent,

            critRate =
                definition.baseStats
                    .critRate,

            critDamage =
                definition.baseStats
                    .critDamage,

            piercing =
                definition.baseStats
                    .piercing,

            meleeWeaponRangeMultiplier =
                definition.baseStats
                    .meleeWeaponRangeMultiplier,

            rangedWeaponRangeMultiplier =
                definition.baseStats
                    .rangedWeaponRangeMultiplier,

            projectileSpeedMultiplier =
                definition.baseStats
                    .projectileSpeedMultiplier,

            damageAffinities =
                definition.baseStats
                    .damageAffinities,

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
             * Her stage küçük ve sürekli artış verir.
             * Tamamlanan her 5-stage bloğu ayrıca daha belirgin bir
             * dayanıklılık / offense sıçraması ekler.
             */
            healthMultiplier =
                1f +
                    (stage - 1) *
                    0.055f +
                    completedStageBlocks *
                    0.12f,

            armorMultiplier =
                1f +
                    (stage - 1) *
                    0.0225f +
                    completedStageBlocks *
                    0.06f,

            damageMultiplier =
                1f +
                    (stage - 1) *
                    0.026f +
                    completedStageBlocks *
                    0.055f,

            movementSpeedMultiplier =
                1f +
                    (stage - 1) *
                    0.001f,

            attackSpeedMultiplier =
                1f +
                    (stage - 1) *
                    0.00275f
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
