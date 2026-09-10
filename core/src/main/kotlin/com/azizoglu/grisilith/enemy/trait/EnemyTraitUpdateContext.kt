package com.azizoglu.grisilith.enemy.trait

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.content.ConditionType
import com.azizoglu.grisilith.content.DamageType
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
    val obstacles: List<MapObstacle>,
    val combatTargets: List<EnemyTraitCombatTarget> = emptyList()
)

enum class EnemyTraitTargetType {
    PLAYER,
    SUMMON,
    GATE
}

data class EnemyTraitCombatTarget(
    val type: EnemyTraitTargetType,
    val index: Int = -1,
    val centerX: Float,
    val centerY: Float,
    val bounds: Rectangle
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
    val sourceName: String,
    val damageType: DamageType = DamageType.NONE
)

data class EnemyTraitTargetDamage(
    val targetType: EnemyTraitTargetType,
    val targetIndex: Int = -1,
    val amount: Int,
    val sourceName: String,
    val damageType: DamageType = DamageType.NONE,
    val projectileOrMagic: Boolean = false,
    val conditionType: ConditionType? = null,
    val conditionDurationSeconds: Float? = null,
    val conditionTickDamage: Int = 0
)

data class EnemyTraitGroundVisual(
    val centerX: Float,
    val centerY: Float,
    val diameter: Float,
    val frameIndex: Int,
    val alpha: Float = 1f
)

enum class EnemyTraitProjectileVisualKind {
    ARROW,
    SPEAR,
    AIR,
    POISON,
    LIGHTNING,
    NECROTIC
}

data class EnemyTraitProjectileVisual(
    val kind: EnemyTraitProjectileVisualKind,
    val centerX: Float,
    val centerY: Float,
    val velocityX: Float,
    val velocityY: Float,
    val worldHeight: Float,
    val alpha: Float = 1f
)

enum class EnemyTraitSpecialVisualKind {
    SPEARMAN_THROW,
    HORSEMAN_SWIPE,
    GHOST_STORM
}

data class EnemyTraitSpecialVisual(
    val kind: EnemyTraitSpecialVisualKind,
    val progress: Float,
    val scale: Float = 1f,
    val segmentMask: Int = 0,
    val isImpact: Boolean = false
)

enum class EnemyTraitSpellVisualKind {
    STATIC_CLOUD,
    METEOR,
    TSUNAMI,
    CALL_LIGHTNING
}

data class EnemyTraitSpellVisual(
    val kind: EnemyTraitSpellVisualKind,
    val centerX: Float,
    val centerY: Float,
    val worldWidth: Float,
    val worldHeight: Float,
    val animationSeconds: Float = 0f,
    val progress: Float = 0f,
    val rotationDegrees: Float = 0f,
    val alpha: Float = 1f
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

    fun consumeTargetDamage():
        List<EnemyTraitTargetDamage> {
        return emptyList()
    }

    fun projectileVisuals(): List<EnemyTraitProjectileVisual> = emptyList()

    fun spellVisuals(): List<EnemyTraitSpellVisual> = emptyList()

    fun groundVisuals(): List<EnemyTraitGroundVisual> = emptyList()

    fun specialVisual(owner: Enemy): EnemyTraitSpecialVisual? = null

    fun render(
        owner: Enemy,
        shapeRenderer: ShapeRenderer
    ) {
    }
}
