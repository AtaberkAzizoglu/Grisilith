package com.azizoglu.grisilith.tamga.runtime.active

import com.azizoglu.grisilith.enemy.Enemy
import com.badlogic.gdx.math.Vector2
import kotlin.math.roundToInt

class ChargerExplosionRuntime {

    companion object {

        /*
         * Patlamanın şarjdan bağımsız başlangıç alanı.
         *
         * Eski değer 220 px idi.
         */
        private const val BASE_EXPLOSION_RADIUS =
            240f

        /*
         * Her şarj saniyesinde kazanılan ek alan.
         */
        private const val RADIUS_PER_CHARGED_SECOND =
            18f

        /*
         * Charger başarıyla tamamlanırsa verilen
         * ek patlama alanı.
         */
        private const val COMPLETED_RADIUS_BONUS =
            25f

        /*
         * Eski değer 520 idi.
         */
        const val KNOCKBACK_STRENGTH =
            620f

        /*
         * Patlamanın minimum taban hasarı.
         *
         * Eski değer 40 idi.
         */
        private const val BASE_DAMAGE =
            70f

        /*
         * Charger'ın aktif kaldığı her saniye
         * için kazanılan ek hasar.
         */
        private const val DAMAGE_PER_CHARGED_SECOND =
            8f

        /*
         * Charger normal şekilde tamamlanırsa
         * verilen ödül hasarı.
         */
        private const val COMPLETED_DAMAGE_BONUS =
            12f

        private const val ELEMENTAL_DAMAGE_MULTIPLIER =
            2.1f

        /*
         * Her 5 stage grubunda +5 hasar.
         *
         * Stage 1–5   → +0
         * Stage 6–10  → +5
         * Stage 11–15 → +10
         */
        private const val DAMAGE_PER_STAGE_GROUP =
            5f
    }

    private val activeEffects =
        mutableListOf<ChargerExplosionEffect>()

    val effects:
        List<ChargerExplosionEffect>
        get() =
            activeEffects

    fun createExplosion(
        centerX:
        Float,

        centerY:
        Float,

        elementalDamage:
        Int,

        currentStage:
        Int,

        chargedSeconds:
        Float,

        completedSuccessfully:
        Boolean,

        enemies:
        List<Enemy>,

        onDamageDealt:
            (
            appliedDamage: Int,
            sourceName: String
        ) -> Unit,

        onEnemyKilled:
            (Enemy) -> Unit
    ) {
        val safeChargedSeconds =
            chargedSeconds
                .coerceIn(
                    0f,
                    ChargerRuntime
                        .MAXIMUM_CHARGE_SECONDS
                )

        val damage =
            calculateDamage(
                elementalDamage =
                    elementalDamage,

                currentStage =
                    currentStage,

                chargedSeconds =
                    safeChargedSeconds,

                completedSuccessfully =
                    completedSuccessfully
            )

        val radius =
            calculateRadius(
                chargedSeconds =
                    safeChargedSeconds,

                completedSuccessfully =
                    completedSuccessfully
            )

        val effect =
            ChargerExplosionEffect(
                centerX =
                    centerX,

                centerY =
                    centerY,

                radius =
                    radius,

                damage =
                    damage,

                knockbackStrength =
                    KNOCKBACK_STRENGTH
            )

        activeEffects.add(
            effect
        )

        applyExplosion(
            effect =
                effect,

            enemies =
                enemies,

            onDamageDealt =
                onDamageDealt,

            onEnemyKilled =
                onEnemyKilled
        )
    }

    fun update(
        delta:
        Float
    ) {
        activeEffects.forEach {
                effect ->

            effect.update(
                delta
            )
        }

        activeEffects.removeAll {
                effect ->

            effect.isFinished
        }
    }

    fun reset() {
        activeEffects.clear()
    }

    private fun applyExplosion(
        effect:
        ChargerExplosionEffect,

        enemies:
        List<Enemy>,

        onDamageDealt:
            (
            appliedDamage: Int,
            sourceName: String
        ) -> Unit,

        onEnemyKilled:
            (Enemy) -> Unit
    ) {
        val radiusSquared =
            effect.radius *
                effect.radius

        enemies.forEach {
                enemy ->

            if (enemy.isDead) {
                return@forEach
            }

            val distanceSquared =
                Vector2.dst2(
                    effect.centerX,
                    effect.centerY,
                    enemy.centerX,
                    enemy.centerY
                )

            if (
                distanceSquared >
                radiusSquared
            ) {
                return@forEach
            }

            val wasAliveBeforeDamage =
                !enemy.isDead

            val appliedDamage =
                enemy.takeDamage(
                    effect.damage
                )

            if (
                appliedDamage >
                0
            ) {
                onDamageDealt(
                    appliedDamage,
                    "Charger"
                )
            }

            enemy.applyKnockback(
                directionX =
                    enemy.centerX -
                        effect.centerX,

                directionY =
                    enemy.centerY -
                        effect.centerY,

                strength =
                    effect.knockbackStrength
            )

            if (
                wasAliveBeforeDamage &&
                enemy.isDead
            ) {
                onEnemyKilled(
                    enemy
                )
            }
        }
    }

    private fun calculateDamage(
        elementalDamage:
        Int,

        currentStage:
        Int,

        chargedSeconds:
        Float,

        completedSuccessfully:
        Boolean
    ): Int {
        val safeElementalDamage =
            elementalDamage
                .coerceAtLeast(
                    0
                )

        val safeStage =
            currentStage
                .coerceAtLeast(
                    1
                )

        val safeChargedSeconds =
            chargedSeconds
                .coerceIn(
                    0f,
                    ChargerRuntime
                        .MAXIMUM_CHARGE_SECONDS
                )

        /*
         * Artışlar Stage 6, 11, 16, 21...
         * değerlerinde gerçekleşir.
         */
        val completedStageGroups =
            (
                safeStage -
                    1
                ) /
                5

        val stageDamage =
            completedStageGroups *
                DAMAGE_PER_STAGE_GROUP

        val chargedDamage =
            safeChargedSeconds *
                DAMAGE_PER_CHARGED_SECOND

        val completionBonus =
            if (completedSuccessfully) {
                COMPLETED_DAMAGE_BONUS
            } else {
                0f
            }

        return (
            BASE_DAMAGE +
                chargedDamage +
                completionBonus +
                safeElementalDamage *
                ELEMENTAL_DAMAGE_MULTIPLIER +
                stageDamage
            )
            .roundToInt()
            .coerceAtLeast(
                1
            )
    }

    private fun calculateRadius(
        chargedSeconds:
        Float,

        completedSuccessfully:
        Boolean
    ): Float {
        val safeChargedSeconds =
            chargedSeconds
                .coerceIn(
                    0f,
                    ChargerRuntime
                        .MAXIMUM_CHARGE_SECONDS
                )

        val completionBonus =
            if (completedSuccessfully) {
                COMPLETED_RADIUS_BONUS
            } else {
                0f
            }

        return BASE_EXPLOSION_RADIUS +
            safeChargedSeconds *
            RADIUS_PER_CHARGED_SECOND +
            completionBonus
    }
}
