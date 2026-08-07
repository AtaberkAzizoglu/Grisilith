package com.azizoglu.grisilith.enemy.trait

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.map.MapObstacle
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

/*
 * Enemy Trait'lerin gerçek runtime yöneticisi.
 *
 * Yeni Trait eklemek için:
 * 1. ContentId oluştur.
 * 2. EnemyTrait implementasyonu yaz.
 * 3. DefaultEnemyTraits.create(...) içine kaydet.
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
        nearbyEnemies: List<Enemy>,
        targetCenterX: Float,
        targetCenterY: Float,
        targetBounds: Rectangle,
        worldWidth: Float,
        worldHeight: Float,
        obstacles: List<MapObstacle>
    ) {
        val context =
            EnemyTraitUpdateContext(
                owner = owner,
                nearbyEnemies = nearbyEnemies,
                delta = delta,
                targetCenterX = targetCenterX,
                targetCenterY = targetCenterY,
                targetBounds = Rectangle(targetBounds),
                worldWidth = worldWidth,
                worldHeight = worldHeight,
                obstacles = obstacles
            )

        var resolvedModifiers =
            EnemyTraitModifiers.IDENTITY

        traits.forEach { trait ->
            trait.update(context)
            resolvedModifiers =
                resolvedModifiers
                    .multipliedBy(
                        trait.modifiers()
                    )
        }

        currentModifiers =
            resolvedModifiers
    }

    fun resolveMovementOverride(
        owner: Enemy,
        output: Vector2
    ): Boolean {
        traits.forEach { trait ->
            if (
                trait.resolveMovementOverride(
                    owner = owner,
                    output = output
                )
            ) {
                return true
            }
        }

        return false
    }

    fun contactDamageMultiplier(
        owner: Enemy
    ): Float {
        var multiplier = 1f

        traits.forEach { trait ->
            multiplier *=
                trait.contactDamageMultiplier(
                    owner
                )
        }

        return multiplier
            .coerceAtLeast(
                1f
            )
    }

    fun shouldNegateIncomingDamage(
        owner: Enemy,
        context: EnemyTraitDamageContext
    ): Boolean {
        return traits.any { trait ->
            trait.shouldNegateIncomingDamage(
                owner = owner,
                context = context
            )
        }
    }

    fun onPlayerContact(
        owner: Enemy,
        dealtDamage: Int
    ) {
        traits.forEach { trait ->
            trait.onPlayerContact(
                owner = owner,
                dealtDamage = dealtDamage
            )
        }
    }

    fun onEnemyContact(
        owner: Enemy,
        other: Enemy
    ) {
        traits.forEach { trait ->
            trait.onEnemyContact(
                owner = owner,
                other = other
            )
        }
    }

    fun consumePlayerDamage():
        List<EnemyTraitPlayerDamage> {

        if (
            traits.isEmpty()
        ) {
            return emptyList()
        }

        val result =
            mutableListOf<EnemyTraitPlayerDamage>()

        traits.forEach { trait ->
            result.addAll(
                trait.consumePlayerDamage()
            )
        }

        return result
    }

    fun render(
        owner: Enemy,
        shapeRenderer: ShapeRenderer
    ) {
        traits.forEach { trait ->
            trait.render(
                owner = owner,
                shapeRenderer = shapeRenderer
            )
        }
    }

    companion object {

        val EMPTY =
            EnemyTraitRuntime(
                activeTraitIds = emptyList(),
                passiveTraitIds = emptyList(),
                traits = emptyList()
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
                        DefaultEnemyTraits.create(id)
                    }

            return EnemyTraitRuntime(
                activeTraitIds = activeTraitIds.toList(),
                passiveTraitIds = passiveTraitIds.toList(),
                traits = traits
            )
        }
    }
}
