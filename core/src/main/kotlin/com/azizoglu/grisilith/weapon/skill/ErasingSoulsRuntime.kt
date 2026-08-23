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
import java.util.ArrayDeque

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
     * Bir ölüm zincirini iteratif şekilde çözer.
     *
     * Böylece çok sayıda düşmanın aynı anda ölmesi halinde
     * derin recursive callback zinciri oluşmaz.
     *
     * Dönen liste initialDeadEnemy HARİÇ olmak üzere,
     * patlamalarla yeni ölen düşmanları içerir.
     */
    fun resolveChain(
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
                    playerCenterY
            )
        ) {
            return emptyList()
        }

        val pendingDeaths =
            ArrayDeque<Enemy>()

        val processedDeaths =
            mutableSetOf<Enemy>()

        val newlyKilledEnemies =
            mutableListOf<Enemy>()

        pendingDeaths.addLast(
            initialDeadEnemy
        )

        while (
            pendingDeaths.isNotEmpty()
        ) {
            val deadEnemy =
                pendingDeaths.removeFirst()

            if (
                !processedDeaths.add(
                    deadEnemy
                )
            ) {
                continue
            }

            /*
             * Zincirin her halkası da oyuncunun Erasing Souls
             * tetik alanı içinde olmalıdır.
             */
            if (
                !isInsideTriggerArea(
                    enemy =
                        deadEnemy,

                    playerCenterX =
                        playerCenterX,

                    playerCenterY =
                        playerCenterY
                )
            ) {
                continue
            }

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
             * Snapshot kullanılır:
             * Aynı patlamada oluşan yeni ölümler liste üzerinde
             * güvenli şekilde kuyruğa eklenebilir.
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

                        pendingDeaths.addLast(
                            target
                        )
                    }
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
        playerCenterY: Float
    ): Boolean {

        return Vector2.dst2(
            playerCenterX,
            playerCenterY,
            enemy.centerX,
            enemy.centerY
        ) <=
            DefaultWeaponAbilities
                .ERASING_SOULS_TRIGGER_RADIUS
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
