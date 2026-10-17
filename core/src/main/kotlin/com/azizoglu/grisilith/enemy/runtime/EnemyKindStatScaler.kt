package com.azizoglu.grisilith.enemy.runtime

import com.azizoglu.grisilith.economy.GoldRewardScaling
import com.azizoglu.grisilith.enemy.EnemyStats
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.enemy.definition.DefaultEnemyTypeModifiers
import com.azizoglu.grisilith.enemy.definition.EnemyKindDefinition
import com.azizoglu.grisilith.enemy.definition.EnemyTag
import com.azizoglu.grisilith.combat.affinity.DamageAffinity
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.content.ConditionType
import com.azizoglu.grisilith.enemy.definition.EnemyKindIds
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

        val familyGrowth = familyGrowth(definition, enemyType, safeStage)

        val scaledHealth =
            (
                definition.baseStats
                    .maximumHealth *
                    stageScaling.healthMultiplier *
                    familyGrowth.healthMultiplier *
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
                    familyGrowth.armorMultiplier *
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
                    familyGrowth.damageMultiplier *
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
                    familyGrowth.attackSpeedMultiplier *
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

            baseMagicResistance = definition.baseStats.baseMagicResistance,
            magicResistance = (definition.baseStats.magicResistance * stageScaling.armorMultiplier *
                familyGrowth.armorMultiplier * typeModifier.armorMultiplier * adaptiveScaling.armorClassMultiplier)
                .roundToInt().coerceAtLeast(0),

            attacksPerSecond =
                scaledAttackSpeed,

            attackRange =
                definition.baseStats
                    .attackRange,

            weaponDamageMultiplier =
                definition.baseStats
                    .weaponDamageMultiplier *
                    stageScaling.damageMultiplier *
                    familyGrowth.damageMultiplier *
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

            damageAffinities = when {
                EnemyTag.UNDEAD in definition.enemyTags ->
                    definition.baseStats.damageAffinities
                        .withAffinity(DamageType.POISON, DamageAffinity.IMMUNE)
                        .withAffinity(DamageType.NECROTIC, DamageAffinity.IMMUNE)
                definition.id == EnemyKindIds.WITCH ->
                    definition.baseStats.damageAffinities
                        .withAffinity(DamageType.POISON, DamageAffinity.RESISTANT)
                else -> definition.baseStats.damageAffinities
            },

            conditionImmunities = if (EnemyTag.UNDEAD in definition.enemyTags) {
                setOf(ConditionType.POISONED, ConditionType.NECROSIS)
            } else emptySet(),

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

    private fun familyGrowth(
        definition: EnemyKindDefinition,
        enemyType: EnemyType,
        stage: Int
    ): FamilyGrowth {
        val progress = (stage - 1).coerceAtLeast(0)
        val bossFactor = when (enemyType) {
            EnemyType.MINI_BOSS -> 1f
            EnemyType.FINAL_BOSS -> 1.25f
            else -> 0.35f
        }

        return when {
            EnemyTag.HUMANOID in definition.enemyTags ->
                FamilyGrowth(
                    healthMultiplier = 1f + progress * 0.006f * bossFactor,
                    armorMultiplier = 1f + progress * 0.0015f * bossFactor,
                    damageMultiplier = 1f + progress * 0.00375f * bossFactor,
                    attackSpeedMultiplier = 1f + progress * 0.0011f * bossFactor
                )

            /*
             * Undying Growth: Undead scale more through persistence than raw
             * offense. It uses the same continuous family layer as Humanoids,
             * with a little more health and less damage/attack speed growth.
             */
            EnemyTag.UNDEAD in definition.enemyTags ->
                FamilyGrowth(
                    healthMultiplier = 1f + progress * 0.0065f * bossFactor,
                    armorMultiplier = 1f + progress * 0.0012f * bossFactor,
                    damageMultiplier = 1f + progress * 0.0032f * bossFactor,
                    attackSpeedMultiplier = 1f + progress * 0.0008f * bossFactor
                )

            else -> FamilyGrowth()
        }
    }

    private data class StageScaling(
        val healthMultiplier: Float,
        val armorMultiplier: Float,
        val damageMultiplier: Float,
        val movementSpeedMultiplier: Float,
        val attackSpeedMultiplier: Float
    )

    private data class FamilyGrowth(
        val healthMultiplier: Float = 1f,
        val armorMultiplier: Float = 1f,
        val damageMultiplier: Float = 1f,
        val attackSpeedMultiplier: Float = 1f
    )
}
