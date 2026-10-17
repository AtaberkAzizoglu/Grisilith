package com.azizoglu.grisilith.enemy.definition

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.content.DamageType

/** Stable damage metadata used by contact attacks and future resistance rules. */
object EnemyAttackDamageTypes {
    fun contactDamageType(kindId: ContentId?): DamageType = when (kindId) {
        EnemyKindIds.GRAY_WOLF,
        EnemyKindIds.BOAR,
        EnemyKindIds.BEE_SWARM,
        EnemyKindIds.MIMIC,
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
        EnemyKindIds.GHOST,
        EnemyKindIds.VAMPIRE_LORD -> DamageType.NECROTIC

        EnemyKindIds.SLIME -> DamageType.ACID

        // Individual Witch spells/projectiles override this contact/fallback element.
        EnemyKindIds.WITCH -> DamageType.LIGHTNING
        else -> DamageType.BLUDGEONING
    }
}
