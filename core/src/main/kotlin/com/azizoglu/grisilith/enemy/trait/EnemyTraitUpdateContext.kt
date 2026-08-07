package com.azizoglu.grisilith.enemy.trait

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyDamageContext

data class EnemyTraitUpdateContext(
    val owner: Enemy,
    val nearbyEnemies: List<Enemy>,
    val delta: Float
)

data class EnemyTraitDamageContext(
    val requestedDamage: Int,
    val damageContext: EnemyDamageContext
)

interface EnemyTrait {

    val id: ContentId

    fun update(
        context: EnemyTraitUpdateContext
    ) {
    }

    fun modifiers():
        EnemyTraitModifiers {
        return EnemyTraitModifiers.IDENTITY
    }

    fun shouldNegateIncomingDamage(
        owner: Enemy,
        context: EnemyTraitDamageContext
    ): Boolean {
        return false
    }

    fun onPlayerContact(
        owner: Enemy,
        dealtDamage: Int
    ) {
    }

    fun onEnemyContact(
        owner: Enemy,
        other: Enemy
    ) {
    }
}
