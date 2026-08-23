package com.azizoglu.grisilith.enemy.trait

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyDamageContext
import com.azizoglu.grisilith.map.MapObstacle
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

data class EnemyTraitUpdateContext(
    val owner: Enemy,
    val nearbyEnemies: List<Enemy>,
    val delta: Float,
    val targetCenterX: Float,
    val targetCenterY: Float,
    val targetBounds: Rectangle,
    val worldWidth: Float,
    val worldHeight: Float,
    val obstacles: List<MapObstacle>
)

data class EnemyTraitDamageContext(
    val requestedDamage: Int,
    val damageContext: EnemyDamageContext
)

/*
 * Trait tarafından oyuncuya gönderilecek hasar.
 * sourceName debug/stat ekranlarında kullanılabilsin diye tutulur.
 */
data class EnemyTraitPlayerDamage(
    val amount: Int,
    val sourceName: String
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

    /*
     * true dönen aktif Trait, Enemy'nin normal AI hareketinin
     * yerine kendi hareket yönünü uygular.
     */
    fun resolveMovementOverride(
        owner: Enemy,
        output: Vector2
    ): Boolean {
        return false
    }

    /*
     * Aktif Trait sırasında oyuncuya temas hasarı için ek çarpan.
     */
    fun contactDamageMultiplier(
        owner: Enemy
    ): Float {
        return 1f
    }

    fun incomingDamageMultiplier(
        owner: Enemy,
        context: EnemyTraitDamageContext
    ): Float {
        return 1f
    }

    fun resolveFinalIncomingDamage(
        owner: Enemy,
        context: EnemyTraitDamageContext,
        resolvedDamage: Int
    ): Int {
        return resolvedDamage
    }

    fun playerVisionMultiplier(
        owner: Enemy
    ): Float {
        return 1f
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

    fun consumePlayerDamage():
        List<EnemyTraitPlayerDamage> {
        return emptyList()
    }

    fun render(
        owner: Enemy,
        shapeRenderer: ShapeRenderer
    ) {
    }
}
