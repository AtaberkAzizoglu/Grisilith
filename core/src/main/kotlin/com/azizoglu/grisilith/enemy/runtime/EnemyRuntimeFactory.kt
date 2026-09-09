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
                runtimeBundle
        )
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
            isSlimeFragment = true
        )
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
