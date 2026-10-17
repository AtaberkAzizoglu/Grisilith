package com.azizoglu.grisilith.weapon.skill

import com.azizoglu.grisilith.content.ConditionType
import com.azizoglu.grisilith.combat.condition.ConditionDamageCalculator
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyDamageContext
import com.azizoglu.grisilith.weapon.runtime.WeaponInstance
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2

class ErasingSoulsRuntime {

    private data class ExplosionVisual(
        val centerX: Float,
        val centerY: Float,
        val radius: Float,
        var remainingSeconds: Float
    )

    data class VisualSnapshot(
        val centerX: Float,
        val centerY: Float,
        val radius: Float,
        val progress: Float
    )

    private val explosionVisuals =
        mutableListOf<ExplosionVisual>()

    /*
     * Resolves one corpse explosion. Victims are returned for bookkeeping
     * only, never to trigger secondary explosions.
     */
    fun resolveExplosion(
        initialDeadEnemy: Enemy,
        sourceWeapon: WeaponInstance,
        playerCenterX: Float,
        playerCenterY: Float,
        enemies: List<Enemy>,
        explosionDamage: Int,
        playerStats: PlayerStats,
        onDamageDealt: (
            appliedDamage: Int,
            sourceName: String
        ) -> Unit,

        onExplosionStarted: (
            deadEnemy: Enemy
        ) -> Unit = {}
    ): List<Enemy> {

        if (
            !initialDeadEnemy.isDead ||
            explosionDamage <= 0
        ) {
            return emptyList()
        }

        if (
            !isInsideTriggerArea(
                enemy =
                    initialDeadEnemy,

                playerCenterX =
                    playerCenterX,

                playerCenterY =
                    playerCenterY,

                sourceWeapon =
                    sourceWeapon
            )
        ) {
            return emptyList()
        }

        val newlyKilledEnemies =
            mutableListOf<Enemy>()
        val deadEnemy = initialDeadEnemy

        val explosionVisualRadius =
            maxOf(
                deadEnemy.bounds.width,
                deadEnemy.bounds.height
            ) /
                2f +
                DefaultWeaponAbilities
                    .ERASING_SOULS_EXPLOSION_RADIUS

        addExplosionVisual(
            centerX =
                deadEnemy.centerX,

            centerY =
                deadEnemy.centerY,

            radius =
                explosionVisualRadius
        )

        onExplosionStarted(
            deadEnemy
        )

        /*
         * Snapshot keeps callbacks from invalidating iteration.
         */
        enemies
            .toList()
            .forEach {
                    target ->

                if (
                    target === deadEnemy ||
                    target.isDead ||
                    !isInsideExplosion(
                        sourceEnemy =
                            deadEnemy,

                        target =
                            target
                    )
                ) {
                    return@forEach
                }

                val wasAlive =
                    !target.isDead

                val appliedDamage =
                    target.takeDamage(
                        amount =
                            explosionDamage,

                        damageContext =
                            EnemyDamageContext(
                                damageType =
                                    DamageType.NECROTIC,

                                sourceWeaponRuntimeId =
                                    sourceWeapon.runtimeId,

                                sourceWeaponDefinitionId =
                                    sourceWeapon
                                        .definition
                                        .id
                                        .value,

                                sourceName =
                                    sourceWeapon.displayName +
                                        " — Erasing the Souls"
                            )
                    )

                if (
                    appliedDamage >
                    0
                ) {
                    /*
                     * Ölü hedefte applyCondition zaten güvenli
                     * şekilde hiçbir şey yapmaz.
                     */
                    target.applyCondition(
                        ConditionDamageCalculator
                            .createApplication(
                                type =
                                    ConditionType.NECROSIS,

                                playerStats =
                                    playerStats,

                                durationSeconds =
                                    DefaultWeaponAbilities
                                        .ERASING_SOULS_NECROSIS_DURATION_SECONDS,

                                sourceWeaponRuntimeId =
                                    sourceWeapon.runtimeId,

                                sourceWeaponDefinitionId =
                                    sourceWeapon.definition.id.value,

                                sourceName =
                                    sourceWeapon.displayName +
                                        " — Erasing the Souls",

                                sourceCenterX =
                                    deadEnemy.centerX,

                                sourceCenterY =
                                    deadEnemy.centerY
                            )
                    )

                    onDamageDealt(
                        appliedDamage,
                        sourceWeapon.displayName +
                            " — Erasing the Souls"
                    )
                }

                if (
                    wasAlive &&
                    target.isDead
                ) {
                    newlyKilledEnemies.add(
                        target
                    )

                    /* Lv.3 and Lv.5 explosions no longer chain. */
                }
            }
        return newlyKilledEnemies
            .distinct()
    }

    fun update(
        delta: Float
    ) {

        if (
            delta <= 0f ||
            explosionVisuals.isEmpty()
        ) {
            return
        }

        explosionVisuals.forEach {
                visual ->

            visual.remainingSeconds =
                (
                    visual.remainingSeconds -
                        delta
                    )
                    .coerceAtLeast(
                        0f
                    )
        }

        explosionVisuals.removeAll {
            it.remainingSeconds <=
                0f
        }
    }

    fun visualSnapshots():
        List<VisualSnapshot> {

        return explosionVisuals
            .map { visual ->

                VisualSnapshot(
                    centerX =
                        visual.centerX,

                    centerY =
                        visual.centerY,

                    radius =
                        visual.radius,

                    progress =
                        (
                            1f -
                                visual.remainingSeconds /
                                EXPLOSION_VISUAL_SECONDS
                            )
                            .coerceIn(
                                0f,
                                1f
                            )
                )
            }
    }

    @Suppress("UNUSED_PARAMETER")
    fun render(
        shapeRenderer: ShapeRenderer
    ) {
        // Sprite visual is rendered by SpellFocusEffectSpriteRenderer.
    }

    fun clear() {
        explosionVisuals.clear()
    }

    private fun isInsideTriggerArea(
        enemy: Enemy,
        playerCenterX: Float,
        playerCenterY: Float,
        sourceWeapon: WeaponInstance
    ): Boolean {

        return Vector2.dst2(
            playerCenterX,
            playerCenterY,
            enemy.centerX,
            enemy.centerY
        ) <=
            (if (sourceWeapon.hasWeaponAbilityUpgrade) {
                DefaultWeaponAbilities.ERASING_SOULS_UPGRADED_TRIGGER_RADIUS
            } else {
                DefaultWeaponAbilities.ERASING_SOULS_TRIGGER_RADIUS
            })
                .let {
                    it *
                        it
                }
    }

    private fun isInsideExplosion(
        sourceEnemy: Enemy,
        target: Enemy
    ): Boolean {

        val sourceRadius =
            maxOf(
                sourceEnemy.bounds.width,
                sourceEnemy.bounds.height
            ) /
                2f

        val effectiveRadius =
            sourceRadius +
                DefaultWeaponAbilities
                    .ERASING_SOULS_EXPLOSION_RADIUS

        return Vector2.dst2(
            sourceEnemy.centerX,
            sourceEnemy.centerY,
            target.centerX,
            target.centerY
        ) <=
            effectiveRadius *
            effectiveRadius
    }

    private fun addExplosionVisual(
        centerX: Float,
        centerY: Float,
        radius: Float
    ) {

        explosionVisuals.add(
            ExplosionVisual(
                centerX =
                    centerX,

                centerY =
                    centerY,

                radius =
                    radius,

                remainingSeconds =
                    EXPLOSION_VISUAL_SECONDS
            )
        )
    }

    companion object {

        private const val EXPLOSION_VISUAL_SECONDS =
            0.65f
    }
}
