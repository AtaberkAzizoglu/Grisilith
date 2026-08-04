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
        var remainingSeconds: Float
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
        ) -> Unit
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

            addExplosionVisual(
                centerX =
                    deadEnemy.centerX,

                centerY =
                    deadEnemy.centerY
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
                            centerX =
                                deadEnemy.centerX,

                            centerY =
                                deadEnemy.centerY,

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

    fun render(
        shapeRenderer: ShapeRenderer
    ) {

        explosionVisuals.forEach {
                visual ->

            val lifetimeRatio =
                (
                    visual.remainingSeconds /
                        EXPLOSION_VISUAL_SECONDS
                    )
                    .coerceIn(
                        0f,
                        1f
                    )

            val radius =
                DefaultWeaponAbilities
                    .ERASING_SOULS_EXPLOSION_RADIUS *
                    (
                        1f +
                            (
                                1f -
                                    lifetimeRatio
                                ) *
                            0.15f
                        )

            shapeRenderer.color =
                Color(
                    0.48f,
                    0.10f,
                    0.64f,
                    0.22f +
                        0.46f *
                        lifetimeRatio
                )

            shapeRenderer.circle(
                visual.centerX,
                visual.centerY,
                radius,
                40
            )

            shapeRenderer.color =
                Color(
                    0.82f,
                    0.42f,
                    0.98f,
                    0.35f +
                        0.50f *
                        lifetimeRatio
                )

            shapeRenderer.circle(
                visual.centerX,
                visual.centerY,
                radius *
                    0.42f,
                28
            )
        }
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
        centerX: Float,
        centerY: Float,
        target: Enemy
    ): Boolean {

        val targetRadius =
            target.bounds.width /
                2f

        val effectiveRadius =
            DefaultWeaponAbilities
                .ERASING_SOULS_EXPLOSION_RADIUS +
                targetRadius

        return Vector2.dst2(
            centerX,
            centerY,
            target.centerX,
            target.centerY
        ) <=
            effectiveRadius *
            effectiveRadius
    }

    private fun addExplosionVisual(
        centerX: Float,
        centerY: Float
    ) {

        explosionVisuals.add(
            ExplosionVisual(
                centerX =
                    centerX,

                centerY =
                    centerY,

                remainingSeconds =
                    EXPLOSION_VISUAL_SECONDS
            )
        )
    }

    companion object {

        private const val EXPLOSION_VISUAL_SECONDS =
            0.24f
    }
}
