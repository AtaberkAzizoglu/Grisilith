package com.azizoglu.grisilith.enemy.definition

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.content.DamageType

/** Stable damage metadata used by contact attacks and future resistance rules. */
object EnemyAttackDamageTypes {
    fun contactDamageType(kindId: ContentId?): DamageType = when (kindId) {
        EnemyKindIds.GRAY_WOLF,
        EnemyKindIds.BOAR,
        EnemyKindIds.BALD_EAGLE,
        EnemyKindIds.GRIZZLY_BEAR,
        EnemyKindIds.CROCODILE,
        EnemyKindIds.ARCHER,
        EnemyKindIds.SPEARMAN,
        EnemyKindIds.SKELETON_ARCHER -> DamageType.PIERCING

        EnemyKindIds.SILVERBACK_GORILLA -> DamageType.BLUDGEONING

        EnemyKindIds.BANDIT,
        EnemyKindIds.HORSEMAN,
        EnemyKindIds.SKELETON_WARRIOR -> DamageType.SLASHING

        EnemyKindIds.ZOMBIE,
        EnemyKindIds.GHOST -> DamageType.NECROTIC

        EnemyKindIds.SLIME -> DamageType.ACID

        // Witch attacks declare their element per projectile/spell.
        // Vampire Lord intentionally remains unspecified for its later design pass.
        else -> DamageType.NONE
    }
}
