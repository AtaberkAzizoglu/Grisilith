package com.azizoglu.grisilith.enemy.definition

import com.azizoglu.grisilith.content.ContentCategory
import com.azizoglu.grisilith.content.ContentDefinition
import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.content.ContentUnlockState
import com.azizoglu.grisilith.enemy.EnemyType

/*
 * Gray Wolf, Witch, Ghost veya Vampire Lord gibi tek bir
 * Enemy Kind'ın değişmeyen içerik tanımı.
 *
 * Unique ayrı bir Kind değildir. NORMAL destekleyen bir Kind,
 * spawn roll başarılı olduğunda aynı tanımdan UNIQUE üretilir.
 */
data class EnemyKindDefinition(
    override val id: ContentId,
    override val displayName: String,
    override val description: String,

    val enemyTags: Set<EnemyTag>,
    val allowedTypes: Set<EnemyType>,
    val sizeCategory: EnemySizeCategory,
    val baseStats: EnemyBaseStats,

    val behaviorId: ContentId,

    val traversalProfile: EnemyTraversalProfile =
        EnemyTraversalProfile.BLOCKED_BY_OBSTACLES,

    val equipment: EnemyEquipmentDefinition =
        EnemyEquipmentDefinition(),

    /*
     * Equipment is kept as content metadata, but current enemies no longer
     * derive their combat actions from player items. A future enemy can opt
     * back into the old runtime without restoring deleted data.
     */
    val usesEquipmentWeaponsInCombat: Boolean =
        false,

    val activeSkillIds: List<ContentId> =
        emptyList(),

    val passiveIds: List<ContentId> =
        emptyList(),

    val spawnWeight: Float =
        1f,

    override val initialUnlockState: ContentUnlockState =
        ContentUnlockState.UNLOCKED
) : ContentDefinition {

    override val category:
        ContentCategory =
        ContentCategory.ENEMY

    /*
     * EnemyTag, ContentTag'den ayrı bir sınıflandırmadır.
     * Collection filtrelerinin mevcut davranışı değişmesin diye
     * bu pakette ContentTag eklenmez.
     */
    override val tags:
        Set<ContentTag> =
        emptySet()

    init {
        require(displayName.isNotBlank()) {
            "Enemy Kind display name cannot be blank."
        }

        require(description.isNotBlank()) {
            "Enemy Kind description cannot be blank."
        }

        require(enemyTags.isNotEmpty()) {
            "Enemy Kind $id must have at least one EnemyTag."
        }

        require(allowedTypes.isNotEmpty()) {
            "Enemy Kind $id must support at least one EnemyType."
        }

        require(
            EnemyType.UNIQUE !in allowedTypes
        ) {
            "Unique must derive from a NORMAL Enemy Kind and must not be declared directly."
        }

        require(spawnWeight > 0f) {
            "Enemy Kind spawn weight must be greater than zero."
        }

        require(
            activeSkillIds.distinct().size ==
                activeSkillIds.size
        ) {
            "Enemy Kind $id cannot contain duplicate active skill IDs."
        }

        require(
            passiveIds.distinct().size ==
                passiveIds.size
        ) {
            "Enemy Kind $id cannot contain duplicate passive IDs."
        }
    }

    fun supportsType(
        enemyType: EnemyType
    ): Boolean {
        return when (enemyType) {
            EnemyType.UNIQUE ->
                EnemyType.NORMAL in
                    allowedTypes

            else ->
                enemyType in
                    allowedTypes
        }
    }
}
