package com.azizoglu.grisilith.weapon.runtime.executor

import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.audio.GameAudio
import com.azizoglu.grisilith.audio.UiSound
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackContext
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackExecutor
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.azizoglu.grisilith.weapon.runtime.WeaponTargetSelector
import com.badlogic.gdx.math.Vector2
import com.azizoglu.grisilith.weapon.skill.DefaultWeaponAbilities

class DaggerAttackExecutor(
    private val damageCalculator:
    WeaponDamageCalculator,

    private val targetSelector:
    WeaponTargetSelector,

    private val playerStats:
    PlayerStats
) : WeaponAttackExecutor {

    override fun execute(
        context: WeaponAttackContext
    ) {
        val definition =
            context.weapon.definition

        val levelStats =
            context.weapon.currentLevelStats

        val effectiveThrowRange =
            context.effectiveWeaponRange(
                (
                    definition.secondaryRange
                        ?: definition.baseRange
                    ) *
                    levelStats.rangeMultiplier
            )
                .coerceAtLeast(0f)

        val target =
            targetSelector.selectTarget(
                targetingType =
                    definition.targetingType,

                player =
                    context.player,

                enemyManager =
                    context.enemyManager,


                originX = context.attackOriginX,
                originY = context.attackOriginY,
                maximumRange =
                    effectiveThrowRange
            ) ?: return

        context.weapon.registerNormalAttack()
        GameAudio.play(UiSound.DAGGER_THROW)

        // Every normal attack throws, including targets at point-blank range.
        executeThrowAttack(context, target.centerX, target.centerY)
    }

    private fun executeThrowAttack(
        context: WeaponAttackContext,
        targetX: Float,
        targetY: Float,
        angleOffsetDegrees: Float = 0f
    ) {
        val definition =
            context.weapon.definition

        val levelStats =
            context.weapon.currentLevelStats


        val maximumTargets =
            (
                definition.baseMaximumTargets +
                    levelStats.additionalTargets
                )
                .coerceAtLeast(1)

        val throwRange =
            context.effectiveWeaponRange(
                (
                    definition.secondaryRange
                        ?: definition.baseRange
                    ) *
                    levelStats.rangeMultiplier
            )
                .coerceAtLeast(1f)

        /*
         * Direction from the player to the throw target.
         */
        val throwDirection =
            Vector2(
                targetX -
                    context.attackOriginX,

                targetY -
                    context.attackOriginY
            )

        if (throwDirection.isZero) {
            return
        }

        throwDirection
            .nor()
            .rotateDeg(
                angleOffsetDegrees
            )

        /*
         * Projectile hedef koordinatı menzilin
         * sonuna yerleştirilir. Böylece küçük açı
         * farkı projectile yönüne yansır.
         */
        val adjustedTargetX =
            context.attackOriginX +
                throwDirection.x *
                throwRange

        val adjustedTargetY =
            context.attackOriginY +
                throwDirection.y *
                throwRange

        val damageType =
            definition.damageTypes
                .firstOrNull()
                ?: DamageType.NONE

        val nonCriticalProjectileDamage =
            damageCalculator
                .calculateNonCriticalDamage(
                    weapon =
                        context.weapon,

                    damageType =
                        damageType,

                    /*
                     * Dagger fırlatıldığında
                     * Dexterity kullanır.
                     */
                    primaryPhysicalStatOverride =
                        com.azizoglu.grisilith.statistics
                            .StatType.DEXTERITY
                )

        context.projectiles.add(
            Projectile(
                startX =
                    context.attackOriginX,

                startY =
                    context.attackOriginY,

                targetX =
                    adjustedTargetX,

                targetY =
                    adjustedTargetY,

                sourceWeaponName =
                    context.weapon.displayName,

                sourceWeapon =
                    context.weapon,

                sourceWeaponSlotIndex =
                    context.weaponSlotIndex,

                damage =
                    nonCriticalProjectileDamage,

                isCriticalHit =
                    false,

                maximumTargets =
                    maximumTargets,

                projectileSpeed =
                    definition
                        .baseProjectileSpeed
                        .coerceAtLeast(1f),

                maximumTravelDistance =
                    throwRange,

                damageType =
                    damageType,

                knockback =
                    (
                        definition.baseKnockback *
                            levelStats.knockbackMultiplier
                        )
                        .coerceAtLeast(0f),

                sizeMultiplier =
                    0.5f,

                attackerPiercingIgnoreRatio =
                    playerStats
                        .piercingIgnoreRatio(),

                canCriticallyHit =
                    damageCalculator
                        .canCriticallyHit(
                            weapon =
                                context.weapon,

                            damageType =
                                damageType
                        ),

                criticalHitChance =
                    damageCalculator
                        .criticalHitChance(
                            context.weapon
                        ),

                criticalDamageMultiplier =
                    damageCalculator
                        .criticalDamageMultiplier(),

                criticalDamageMultiplierProvider = {
                    val baseCriticalMultiplier =
                        damageCalculator
                            .criticalDamageMultiplier()

                    if (context.weapon.hasBasicAttackUpgrade) {
                        baseCriticalMultiplier *
                            DefaultWeaponAbilities
                                .DAGGER_BASIC_CRITICAL_DAMAGE_MULTIPLIER
                    } else {
                        baseCriticalMultiplier
                    }
                }
            )
        )
    }
}
