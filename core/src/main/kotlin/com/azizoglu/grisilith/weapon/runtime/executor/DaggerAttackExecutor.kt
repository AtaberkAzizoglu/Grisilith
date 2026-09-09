package com.azizoglu.grisilith.weapon.runtime.executor

import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackContext
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackExecutor
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.azizoglu.grisilith.weapon.runtime.WeaponTargetSelector
import com.badlogic.gdx.math.Vector2
import com.azizoglu.grisilith.weapon.skill.DefaultWeaponAbilities
import kotlin.math.roundToInt

class DaggerAttackExecutor(
    private val damageCalculator:
    WeaponDamageCalculator,

    private val targetSelector:
    WeaponTargetSelector,

    private val playerStats:
    PlayerStats
) : WeaponAttackExecutor {

    private data class AbilityDamageResult(
        val damage: Int,
        val abilityTriggered: Boolean
    )

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

        val attackNumber =
            context.weapon
                .registerNormalAttack()

        val shouldRepeatAttack =
            context.weapon
                .hasBasicAttackUpgrade &&
                attackNumber % 3 == 0

        /*
         * Üçüncü saldırıda ilk bıçak hafifçe
         * sola yönelir. Normal saldırıda açı 0.
         */
        executeAttackAgainstTarget(
            context = context,
            target = target,
            throwAngleOffsetDegrees =
                if (shouldRepeatAttack) {
                    -4f
                } else {
                    0f
                }
        )

        if (shouldRepeatAttack) {
            /*
             * İlk saldırı hedefi öldürmüş olabilir.
             * İkinci saldırı için yeniden geçerli
             * bir hedef seçilir.
             */
            val secondTarget =
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
                )

            if (secondTarget != null) {
                executeAttackAgainstTarget(
                    context = context,
                    target = secondTarget,
                    throwAngleOffsetDegrees = 4f
                )
            }
        }
    }

    private fun executeAttackAgainstTarget(
        context: WeaponAttackContext,
        target: Enemy,
        throwAngleOffsetDegrees: Float = 0f
    ) {
        val definition =
            context.weapon.definition

        val levelStats =
            context.weapon.currentLevelStats

        val targetDistance =
            Vector2.dst(
                context.attackOriginX,
                context.attackOriginY,
                target.centerX,
                target.centerY
            )

        val effectiveMeleeRange =
            (
                context.effectiveWeaponRange(
                    definition.baseRange *
                        levelStats.rangeMultiplier
                )
                )
                .coerceAtLeast(0f)

        if (
            targetDistance <=
            effectiveMeleeRange
        ) {
            executeMeleeAttack(
                context = context,
                target = target
            )
        } else {
            executeThrowAttack(
                context = context,
                targetX = target.centerX,
                targetY = target.centerY,
                angleOffsetDegrees =
                    throwAngleOffsetDegrees
            )
        }
    }

    private fun abilityModifiedDamage(
        context: WeaponAttackContext,
        baseDamage: Int,
        isCriticalHit: Boolean
    ): AbilityDamageResult {

        if (
            !isCriticalHit ||
            context.weapon
                .unlockedAbility
                ?.id !=
            DefaultWeaponAbilities
                .ASSASSINATE_ID
        ) {
            return AbilityDamageResult(
                damage =
                    baseDamage
                        .coerceAtLeast(
                            1
                        ),

                abilityTriggered =
                    false
            )
        }

        val activated =
            context.triggerAutomaticAbility()

        if (
            !activated
        ) {
            return AbilityDamageResult(
                damage =
                    baseDamage
                        .coerceAtLeast(
                            1
                        ),

                abilityTriggered =
                    false
            )
        }

        return AbilityDamageResult(
            damage =
                (
                    baseDamage *
                        DefaultWeaponAbilities
                            .ASSASSINATE_DAMAGE_MULTIPLIER
                    )
                    .roundToInt()
                    .coerceAtLeast(
                        1
                    ),

            abilityTriggered =
                true
        )
    }

    private fun executeMeleeAttack(
        context: WeaponAttackContext,
        target: Enemy
    ) {
        if (target.isDead) {
            return
        }

        val damageType =
            context.weapon
                .definition
                .damageTypes
                .firstOrNull { type ->
                    type != DamageType.NONE
                }
                ?: DamageType.NONE

        val damageResult =
            damageCalculator
                .calculateForHit(
                    weapon =
                        context.weapon,

                    damageType =
                        damageType,

                    allowCritical =
                        true,

                    /*
                     * Dagger yakın mesafede
                     * Strength kullanır.
                     */
                    primaryPhysicalStatOverride =
                        com.azizoglu.grisilith.statistics
                            .StatType.STRENGTH
                )

        val definition =
            context.weapon.definition

        val levelStats =
            context.weapon.currentLevelStats

        val wasAliveBeforeAttack =
            !target.isDead

        val abilityDamage =
            abilityModifiedDamage(
                context =
                    context,

                baseDamage =
                    damageResult.damage,

                isCriticalHit =
                    damageResult.isCriticalHit
            )

        val finalDamage =
            context.modifiedDamageAgainst(
                enemy = target,
                baseDamage =
                    abilityDamage.damage
            )

        val appliedDamage =
            target.takeDamage(
                finalDamage
            )

        if (appliedDamage > 0) {
            context.onDamageDealt(
                appliedDamage,
                context.weapon.displayName
            )

            target.applyKnockback(
                directionX =
                    target.centerX -
                        context.attackOriginX,

                directionY =
                    target.centerY -
                        context.attackOriginY,

                strength =
                    definition.baseKnockback *
                        levelStats.knockbackMultiplier
            )
        }

        if (
            wasAliveBeforeAttack &&
            target.isDead
        ) {
            context.onEnemyKilled(
                target
            )
        }

        context.reportWeaponHit(
            enemy = target,
            appliedDamage =
                appliedDamage,
            damageType =
                context.weapon
                    .definition
                    .damageTypes
                    .firstOrNull { type ->
                        type !=
                            DamageType.NONE
                    }
                    ?: DamageType.NONE
        )
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
         * Oyuncudan hedefe olan yön.
         * Üçüncü saldırının iki bıçağında
         * küçük açı farkı uygulanabilir.
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
                    1f,

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

                    val canUseAssassinate =
                        context.weapon
                            .unlockedAbility
                            ?.id ==
                            DefaultWeaponAbilities
                                .ASSASSINATE_ID

                    if (
                        canUseAssassinate &&
                        context.triggerAutomaticAbility()
                    ) {
                        baseCriticalMultiplier *
                            DefaultWeaponAbilities
                                .ASSASSINATE_DAMAGE_MULTIPLIER
                    } else {
                        baseCriticalMultiplier
                    }
                }
            )
        )
    }
}
