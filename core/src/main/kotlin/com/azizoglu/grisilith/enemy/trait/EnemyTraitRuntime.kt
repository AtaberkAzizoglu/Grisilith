package com.azizoglu.grisilith.enemy.trait

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.enemy.Enemy

/*
 * Enemy Trait'lerin gerçek runtime yöneticisi.
 *
 * Yeni Trait eklemek için:
 * 1. ContentId oluştur.
 * 2. EnemyTrait implementasyonu yaz.
 * 3. DefaultEnemyTraits.create(...) içine kaydet.
 *
 * Enemy.kt içine yeni trait'e özel if/when eklemek gerekmez.
 */
class EnemyTraitRuntime private constructor(
    val activeTraitIds: List<ContentId>,
    val passiveTraitIds: List<ContentId>,
    private val traits: List<EnemyTrait>
) {

    var currentModifiers:
        EnemyTraitModifiers =
        EnemyTraitModifiers.IDENTITY
        private set

    fun update(
        delta: Float,
        owner: Enemy,
        nearbyEnemies: List<Enemy>
    ) {
        val context =
            EnemyTraitUpdateContext(
                owner =
                    owner,

                nearbyEnemies =
                    nearbyEnemies,

                delta =
                    delta
            )

        var resolvedModifiers =
            EnemyTraitModifiers.IDENTITY

        traits.forEach { trait ->
            trait.update(
                context
            )

            resolvedModifiers =
                resolvedModifiers
                    .multipliedBy(
                        trait.modifiers()
                    )
        }

        currentModifiers =
            resolvedModifiers
    }

    fun shouldNegateIncomingDamage(
        owner: Enemy,
        context: EnemyTraitDamageContext
    ): Boolean {
        return traits.any { trait ->
            trait.shouldNegateIncomingDamage(
                owner =
                    owner,

                context =
                    context
            )
        }
    }

    fun onPlayerContact(
        owner: Enemy,
        dealtDamage: Int
    ) {
        traits.forEach { trait ->
            trait.onPlayerContact(
                owner =
                    owner,

                dealtDamage =
                    dealtDamage
            )
        }
    }

    fun onEnemyContact(
        owner: Enemy,
        other: Enemy
    ) {
        traits.forEach { trait ->
            trait.onEnemyContact(
                owner =
                    owner,

                other =
                    other
            )
        }
    }

    companion object {

        val EMPTY =
            EnemyTraitRuntime(
                activeTraitIds =
                    emptyList(),

                passiveTraitIds =
                    emptyList(),

                traits =
                    emptyList()
            )

        fun fromIds(
            activeTraitIds: List<ContentId>,
            passiveTraitIds: List<ContentId>
        ): EnemyTraitRuntime {

            val allIds =
                (
                    passiveTraitIds +
                        activeTraitIds
                    )
                    .distinct()

            val traits =
                allIds
                    .mapNotNull { id ->
                        DefaultEnemyTraits
                            .create(
                                id
                            )
                    }

            return EnemyTraitRuntime(
                activeTraitIds =
                    activeTraitIds
                        .toList(),

                passiveTraitIds =
                    passiveTraitIds
                        .toList(),

                traits =
                    traits
            )
        }
    }
}
