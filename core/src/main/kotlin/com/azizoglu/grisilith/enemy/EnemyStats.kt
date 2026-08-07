package com.azizoglu.grisilith.enemy

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.enemy.definition.EnemyTraversalProfile

/*
 * Runtime sırasında Enemy tarafından kullanılan çözülmüş değerler.
 *
 * Yeni alanların varsayılan değerleri eski EnemyScaling ve mevcut
 * spawn akışının derlenmeye devam etmesini sağlar.
 */
data class EnemyStats(
    val type: EnemyType,
    val maximumHealth: Int,
    val contactDamage: Int,
    val movementSpeed: Float,
    val size: Float,
    val goldReward: Float,
    val armorClass: Int = 0,

    val attacksPerSecond: Float = 0f,
    val attackRange: Float = 0f,

    val kindId: ContentId? = null,
    val behaviorId: ContentId? = null,

    val traversalProfile: EnemyTraversalProfile =
        EnemyTraversalProfile.BLOCKED_BY_OBSTACLES
) {

    init {
        require(maximumHealth > 0)
        require(contactDamage >= 0)
        require(movementSpeed >= 0f)
        require(size > 0f)
        require(goldReward >= 0f)
        require(armorClass >= 0)
        require(attacksPerSecond >= 0f)
        require(attackRange >= 0f)
    }

    val ignoresObstacles: Boolean
        get() =
            traversalProfile
                .ignoresObstacles

    val canSpawnInsideObstacles: Boolean
        get() =
            traversalProfile
                .canSpawnInsideObstacles
}
