package com.azizoglu.grisilith.enemy.runtime

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyStats
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.enemy.definition.EnemyKindDefinition
import com.azizoglu.grisilith.enemy.definition.EnemyKindRegistry
import com.azizoglu.grisilith.enemy.runtime.ability.EnemyAbilityRuntime
import com.azizoglu.grisilith.enemy.runtime.ai.EnemyAiPipeline
import com.azizoglu.grisilith.enemy.runtime.weapon.EnemyWeaponRuntimeGroup
import com.azizoglu.grisilith.enemy.trait.EnemyTraitRuntime
import com.azizoglu.grisilith.enemy.definition.DefaultEnemyKinds
import com.azizoglu.grisilith.config.GameMap

/*
 * EnemyKindDefinition'dan gerçek Enemy runtime nesnesi üretir.
 *
 * Spawn konumu doğrulaması bu sınıfın görevi değildir.
 * Map/Spawner geçerli X/Y konumunu bulduktan sonra factory'yi çağırır.
 */
class EnemyRuntimeFactory(
    private val registry:
    EnemyKindRegistry
) {

    fun create(
        kindId: ContentId,
        enemyType: EnemyType,
        stage: Int,
        startX: Float,
        startY: Float
    ): Enemy {

        return create(
            definition =
                requireDefinition(
                    kindId
                ),

            enemyType =
                enemyType,

            stage =
                stage,

            startX =
                startX,

            startY =
                startY
        )
    }

    fun create(
        definition: EnemyKindDefinition,
        enemyType: EnemyType,
        stage: Int,
        startX: Float,
        startY: Float
    ): Enemy {

        val stats =
            createStats(
                definition =
                    definition,

                enemyType =
                    enemyType,

                stage =
                    stage
            )

        val runtimeBundle =
            createRuntimeBundle(
                definition =
                    definition
            )

        return Enemy(
            startX =
                startX,

            startY =
                startY,

            stats =
                stats,

            runtimeBundle =
                runtimeBundle,

            spawnStage = stage
        )
    }

    fun createMimic(
        gameMap: GameMap,
        stage: Int,
        startX: Float,
        startY: Float
    ): Enemy {
        val baseStats = when (gameMap) {
            GameMap.ROAD_TO_HOME -> DefaultEnemyKinds.boar.baseStats.copy(
                contactDamage = DefaultEnemyKinds.grayWolf.baseStats.contactDamage,
                movementSpeed = DefaultEnemyKinds.grayWolf.baseStats.movementSpeed,
                attacksPerSecond = DefaultEnemyKinds.grayWolf.baseStats.attacksPerSecond,
                attackSpeedScore = DefaultEnemyKinds.grayWolf.baseStats.attackSpeedScore,
                mainDamageStat = DefaultEnemyKinds.grayWolf.baseStats.mainDamageStat
            )

            GameMap.TROUBLE_AT_HOME -> DefaultEnemyKinds.bandit.baseStats

            GameMap.FOLLOWING_THE_DUNGEON_DELVER -> DefaultEnemyKinds.zombie.baseStats.copy(
                contactDamage = DefaultEnemyKinds.skeletonWarrior.baseStats.contactDamage,
                mainDamageStat = DefaultEnemyKinds.skeletonWarrior.baseStats.mainDamageStat
            )
        }
        val definition = DefaultEnemyKinds.mimic.copy(baseStats = baseStats)
        return create(definition, EnemyType.UNIQUE, stage, startX, startY)
    }

    fun createBabyCrocodile(
        parent: Enemy,
        startX: Float,
        startY: Float
    ): Enemy {
        val boarStats = createStats(DefaultEnemyKinds.boar, EnemyType.NORMAL, parent.spawnStage)
            .copy(
                kindId = com.azizoglu.grisilith.enemy.definition.EnemyKindIds.CROCODILE,
                behaviorId = DefaultEnemyKinds.boar.behaviorId,
                goldReward = 0f
            )
        return Enemy(
            startX = startX,
            startY = startY,
            stats = boarStats,
            runtimeBundle = EnemyRuntimeBundle.EMPTY,
            spawnStage = parent.spawnStage,
            isBabyCrocodile = true,
            babyCrocodileParent = parent
        )
    }

    fun createSwarmSplitChild(
        parent: Enemy,
        startX: Float,
        startY: Float
    ): Enemy {
        val definition = requireDefinition(requireNotNull(parent.kindId))
        return Enemy(
            startX = startX,
            startY = startY,
            stats = parent.swarmCloneStats(),
            runtimeBundle = createRuntimeBundle(definition),
            spawnStage = parent.spawnStage,
            swarmSplitBudget = parent.swarmBudget()
        ).also { it.copySwarmRuntimeFrom(parent) }
    }

    fun createSlimeFragment(
        parent: Enemy,
        startX: Float,
        startY: Float
    ): Enemy {
        val definition = requireDefinition(
            requireNotNull(parent.kindId) {
                "Slime fragment parent must have a Kind ID."
            }
        )
        return Enemy(
            startX = startX,
            startY = startY,
            stats = parent.slimeFragmentStats(),
            runtimeBundle = createRuntimeBundle(definition),
            isSlimeFragment = true,
            spawnStage = parent.spawnStage
        ).also { fragment ->
            fragment.slimeRangedCooldownRemainingSeconds =
                parent.slimeRangedCooldownRemainingSeconds
        }
    }

    fun createStats(
        kindId: ContentId,
        enemyType: EnemyType,
        stage: Int
    ): EnemyStats {

        return createStats(
            definition =
                requireDefinition(
                    kindId
                ),

            enemyType =
                enemyType,

            stage =
                stage
        )
    }

    fun createStats(
        definition: EnemyKindDefinition,
        enemyType: EnemyType,
        stage: Int
    ): EnemyStats {

        return EnemyKindStatScaler
            .resolve(
                definition =
                    definition,

                enemyType =
                    enemyType,

                stage =
                    stage
            )
    }

    fun createRuntimeBundle(
        definition:
        EnemyKindDefinition
    ): EnemyRuntimeBundle {

        val abilityRuntime =
            EnemyAbilityRuntime(
                activeAbilityIds =
                    definition
                        .activeSkillIds,

                passiveAbilityIds =
                    definition
                        .passiveIds
            )

        val weaponRuntime =
            EnemyWeaponRuntimeGroup
                .fromDefinitions(
                    if (
                        definition
                            .usesEquipmentWeaponsInCombat
                    ) {
                        definition
                            .equipment
                            .weapons
                    } else {
                        emptyList()
                    }
                )

        val traitRuntime =
            EnemyTraitRuntime
                .fromIds(
                    activeTraitIds =
                        definition
                            .activeSkillIds,

                    passiveTraitIds =
                        definition
                            .passiveIds
                )

        return EnemyRuntimeBundle(
            abilityRuntime =
                abilityRuntime,

            weaponRuntime =
                weaponRuntime,

            aiPipeline =
                EnemyAiPipeline(
                    abilityRuntime =
                        abilityRuntime,

                    weaponRuntime =
                        weaponRuntime
                ),

            traitRuntime =
                traitRuntime
        )
    }

    private fun requireDefinition(
        kindId: ContentId
    ): EnemyKindDefinition {

        return requireNotNull(
            registry.findById(
                kindId
            )
        ) {
            "Unknown Enemy Kind ID: $kindId"
        }
    }
}
