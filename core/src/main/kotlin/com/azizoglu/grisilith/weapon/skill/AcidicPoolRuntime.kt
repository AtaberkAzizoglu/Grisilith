package com.azizoglu.grisilith.weapon.skill

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.weapon.skill.area.PersistentArea
import com.azizoglu.grisilith.weapon.skill.area.PersistentAreaAnchor
import com.azizoglu.grisilith.weapon.skill.area.PersistentAreaManager
import com.azizoglu.grisilith.weapon.skill.area.PersistentAreaRuntime
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.MathUtils

class AcidicPoolRuntime(
    areaManager: PersistentAreaManager
) {

    private val reactiveTarget =
        ReactiveSpellFieldTarget(DamageType.FIRE)

    private val areaRuntime:
        PersistentAreaRuntime =
        areaManager.runtimeFor(
            AREA_ID
        )

    private var vitriolicIgnitionActive =
        false

    private var ignitionVisualRemainingSeconds =
        0f

    private var surfaceAnimationSeconds =
        0f

    val isVitriolicIgnitionActive: Boolean
        get() =
            vitriolicIgnitionActive

    val field:
        PersistentArea?
        get() =
            areaRuntime.area

    val isActive: Boolean
        get() =
            areaRuntime.isActive

    val visualCenterX: Float
        get() =
            areaRuntime.area
                ?.center
                ?.x
                ?: 0f

    val visualCenterY: Float
        get() =
            areaRuntime.area
                ?.center
                ?.y
                ?: 0f

    val visualRadius: Float
        get() =
            areaRuntime.area
                ?.radius
                ?: 0f

    val visualAnimationSeconds: Float
        get() =
            surfaceAnimationSeconds

    val ignitionVisualProgress: Float
        get() {
            if (
                ignitionVisualRemainingSeconds <=
                0f
            ) {
                return 1f
            }

            return (
                1f -
                    ignitionVisualRemainingSeconds /
                    IGNITION_VISUAL_SECONDS
                )
                .coerceIn(
                    0f,
                    1f
                )
        }

    val isIgnitionBurstVisible: Boolean
        get() =
            ignitionVisualRemainingSeconds >
                0f

    fun tryBegin(
        playerCenterX: Float,
        playerCenterY: Float,
        enemies: List<Enemy>,
        upgraded: Boolean,
        aimPosition: Vector2? = null
    ): Boolean {

        if (isActive) {
            return false
        }

        val placementRangeSquared =
            (if (upgraded) {
                DefaultWeaponAbilities.ACIDIC_POOL_UPGRADED_PLACEMENT_RANGE
            } else {
                DefaultWeaponAbilities.ACIDIC_POOL_PLACEMENT_RANGE
            })
                .let {
                        range ->

                    range *
                        range
                }

        /*
         * Vortex Pull gibi yalnızca menzilde uygun
         * bir düşman varsa kullanılabilir.
         *
         * Alan, menzildeki en yakın canlı düşmanın
         * merkezi üzerinde açılır.
         */
        val candidates =
            enemies
                .asSequence()
                .filter {
                        enemy ->

                    !enemy.isDead
                }
                .map {
                        enemy ->

                    enemy to
                        Vector2.dst2(
                            playerCenterX,
                            playerCenterY,
                            enemy.centerX,
                            enemy.centerY
                        )
                }
                .filter {
                        (_, distanceSquared) ->

                    distanceSquared <=
                        placementRangeSquared
                }
                .toList()

        val target =
            com.azizoglu.grisilith.weapon.runtime.AbilityTargeting.select(
                candidates.map { it.first }, playerCenterX, playerCenterY,
                kotlin.math.sqrt(placementRangeSquared))
        if (target == null && aimPosition == null) return false
        val center = aimPosition?.cpy()?.sub(playerCenterX, playerCenterY)
            ?.limit(kotlin.math.sqrt(placementRangeSquared))?.add(playerCenterX, playerCenterY)
            ?: Vector2(target!!.centerX, target.centerY)

        val began = areaRuntime.begin(
            PersistentArea(
                id =
                    AREA_ID,

                center =
                    Vector2(
                        center.x,
                        center.y
                    ),

                radius =
                    DefaultWeaponAbilities
                        .ACIDIC_POOL_RADIUS,

                durationSeconds =
                    if (upgraded) {
                        DefaultWeaponAbilities.ACIDIC_POOL_UPGRADED_DURATION_SECONDS
                    } else {
                        DefaultWeaponAbilities.ACIDIC_POOL_DURATION_SECONDS
                    },

                tickIntervalSeconds =
                    DefaultWeaponAbilities
                        .ACIDIC_POOL_TICK_SECONDS,

                anchor =
                    PersistentAreaAnchor
                        .FIXED_WORLD_POSITION
            )
        )
        if (began) reactiveTarget.reset()
        return began
    }

    fun contains(
        worldX: Float,
        worldY: Float
    ): Boolean {

        return areaRuntime
            .area
            ?.contains(
                worldX,
                worldY
            )
            ?: false
    }

    fun tryActivateVitriolicIgnition(
        worldX: Float,
        worldY: Float
    ): Boolean {

        if (
            vitriolicIgnitionActive ||
            !contains(
                worldX,
                worldY
            )
        ) {
            return false
        }

        vitriolicIgnitionActive =
            true

        ignitionVisualRemainingSeconds =
            IGNITION_VISUAL_SECONDS

        return true
    }

    fun tryDamageReactiveTarget(
        damageType: DamageType?,
        worldX: Float,
        worldY: Float
    ): Boolean {
        if (!isActive || vitriolicIgnitionActive || !contains(worldX, worldY)) return false
        if (!reactiveTarget.tryDamage(damageType)) return false
        return tryActivateVitriolicIgnition(worldX, worldY)
    }

    fun update(
        delta: Float,
        enemies: List<Enemy>,
        onDamageTick: (
            enemy: Enemy,
            tickIntervalSeconds: Float,
            poolCenterX: Float,
            poolCenterY: Float
        ) -> Unit,
        onCompleted: () -> Unit
    ) {

        areaRuntime.update(
            delta =
                delta,

            /*
             * FIXED_WORLD_POSITION kullandığı için
             * bu değerler alan merkezini değiştirmez.
             */
            followCenterX =
                0f,

            followCenterY =
                0f,

            onFrame = {
                    _,
                    frameDelta ->

                surfaceAnimationSeconds +=
                    frameDelta

                ignitionVisualRemainingSeconds =
                    (
                        ignitionVisualRemainingSeconds -
                            frameDelta
                        )
                        .coerceAtLeast(
                            0f
                        )
            },

            onTick = {
                    area ->

                enemies
                    .toList()
                    .forEach {
                            enemy ->

                        if (
                            enemy.isDead ||
                            !area.contains(
                                enemy.centerX,
                                enemy.centerY
                            )
                        ) {
                            return@forEach
                        }

                        onDamageTick(
                            enemy,
                            area.tickIntervalSeconds,
                            area.center.x,
                            area.center.y
                        )
                    }
            },

            onCompleted =
                onCompleted
        )
    }

    @Suppress("UNUSED_PARAMETER")
    fun render(
        shapeRenderer: ShapeRenderer
    ) {
        // Sprite visual is rendered by SpellFocusEffectSpriteRenderer.
    }

    fun clear() {
        areaRuntime.clear()
        reactiveTarget.clear()

        vitriolicIgnitionActive =
            false

        ignitionVisualRemainingSeconds =
            0f

        surfaceAnimationSeconds =
            0f
    }

    companion object {

        private const val AREA_ID =
            "AREA_ACIDIC_POOL"

        private const val IGNITION_VISUAL_SECONDS =
            0.78f

        private const val SURFACE_PULSE_SPEED =
            3.8f

        private const val BUBBLE_ROTATION_SPEED =
            0.85f

        private const val BUBBLE_COUNT =
            9
    }
}
