package com.azizoglu.grisilith.content

import com.azizoglu.grisilith.enemy.EnemyType

data class EnemyCollectionDefinition(
    val enemyType: EnemyType
) : ContentDefinition {

    override val id =
        ContentId(
            CollectionEncounterIds.enemy(
                enemyType
            )
        )

    override val displayName:
        String =
        when (enemyType) {

            EnemyType.NORMAL ->
                "Normal Enemy"

            EnemyType.UNIQUE ->
                "Unique Enemy"

            EnemyType.MINI_BOSS ->
                "Mini Boss"

            EnemyType.FINAL_BOSS ->
                "Final Boss"
        }

    override val description:
        String =
        when (enemyType) {

            EnemyType.NORMAL ->
                "A standard enemy encountered throughout a run."

            EnemyType.UNIQUE ->
                "A stronger and less common variation of a normal enemy."

            EnemyType.MINI_BOSS ->
                "A powerful enemy encountered during Mini Boss stages."

            EnemyType.FINAL_BOSS ->
                "The final enemy that must be defeated to complete a run."
        }

    override val category:
        ContentCategory =
        ContentCategory.ENEMY

    override val tags:
        Set<ContentTag> =
        emptySet()

    override val initialUnlockState:
        ContentUnlockState =
        ContentUnlockState.UNLOCKED
}
