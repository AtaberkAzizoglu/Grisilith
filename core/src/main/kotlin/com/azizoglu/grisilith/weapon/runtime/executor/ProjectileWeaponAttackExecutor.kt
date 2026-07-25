package com.azizoglu.grisilith.weapon.runtime.executor

import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackContext
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackExecutor
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.azizoglu.grisilith.weapon.runtime.WeaponTargetSelector
import com.badlogic.gdx.math.Vector2
import kotlin.math.roundToInt

class ProjectileWeaponAttackExecutor(
    private val damageCalculator:
    WeaponDamageCalculator,

    private val targetSelector:
    WeaponTargetSelector,

    private val playerStats:
    PlayerStats
) : WeaponAttackExecutor {

    companion object {
        private const val LONGBOW_ID =
            "WPN_LONGBOW"

        private const val CROSSBOW_ID =
            "WPN_CROSSBOW"

        private const val STEAM_WAND_ID =
            "WPN_STEAM_WAND"

        private const val ORB_OF_SKY_ID =
            "WPN_ORB_OF_SKY"

        private const val ORB_LIGHTNING_CHAIN_RANGE =
            310f

        private const val ORB_CHAIN_DAMAGE_MULTIPLIER =
            0.50f

        private const val LONGBOW_RICOCHET_RANGE =
            360f

        private const val CROSSBOW_NORMAL_POST_HIT_RANGE =
            170f

        private const val CROSSBOW_UPGRADED_POST_HIT_RANGE =
            310f

        /*
         * Steam Wand Lv.3 yelpazesi.
         */
        private const val STEAM_WAND_FAN_PROJECTILE_COUNT =
            5

        private const val STEAM_WAND_FAN_ANGLE_STEP =
            8f

        /*
         * 5 × 0.24 = normal saldırının
         * yaklaşık %120 toplam hasarı.
         */
        private const val STEAM_WAND_FAN_DAMAGE_MULTIPLIER =
            0.24f
    }

    override fun execute(
        context: WeaponAttackContext
    ) {
        val definition =
            context.weapon.definition

        val levelStats =
            context.weapon.currentLevelStats

        val effectiveRange =
            (
                definition.baseRange *
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

                maximumRange =
                    effectiveRange
            ) ?: return

        val weaponId =
            definition.id.value

        val isUpgradedSteamWand =
            weaponId == STEAM_WAND_ID &&
                context.weapon
                    .hasBasicAttackUpgrade

        if (isUpgradedSteamWand) {
            executeUpgradedSteamWandAttack(
                context = context,
                targetX = target.centerX,
                targetY = target.centerY,
                effectiveRange = effectiveRange
            )

            return
        }

        val projectileCount =
            definition.baseProjectileCount
                .coerceAtLeast(1)

        repeat(projectileCount) {
            createProjectile(
                context = context,
                targetX = target.centerX,
                targetY = target.centerY
            )
        }
    }

    /*
     * Steam Wand Lv.3:
     *
     * İlk iki saldırı:
     * → Tek Water projectile
     *
     * Her üçüncü saldırı:
     * → Beş Water projectile’lık yelpaze
     */
    private fun executeUpgradedSteamWandAttack(
        context: WeaponAttackContext,
        targetX: Float,
        targetY: Float,
        effectiveRange: Float
    ) {
        val attackNumber =
            context.weapon
                .registerNormalAttack()

        val isFanAttack =
            attackNumber % 3 == 0

        if (!isFanAttack) {
            createProjectile(
                context = context,
                targetX = targetX,
                targetY = targetY,
                forcedDamageType =
                    DamageType.WATER
            )

            return
        }

        val baseDirection =
            Vector2(
                targetX -
                    context.player.centerX,

                targetY -
                    context.player.centerY
            )

        if (baseDirection.isZero) {
            baseDirection.set(
                1f,
                0f
            )
        }

        baseDirection.nor()

        val middleIndex =
            STEAM_WAND_FAN_PROJECTILE_COUNT /
                2

        repeat(
            STEAM_WAND_FAN_PROJECTILE_COUNT
        ) { projectileIndex ->

            val indexOffset =
                projectileIndex -
                    middleIndex

            val angleOffset =
                indexOffset *
                    STEAM_WAND_FAN_ANGLE_STEP

            val projectileDirection =
                Vector2(
                    baseDirection
                )
                    .rotateDeg(
                        angleOffset
                    )

            val adjustedTargetX =
                context.player.centerX +
                    projectileDirection.x *
                    effectiveRange

            val adjustedTargetY =
                context.player.centerY +
                    projectileDirection.y *
                    effectiveRange

            createProjectile(
                context = context,
                targetX = adjustedTargetX,
                targetY = adjustedTargetY,
                damageMultiplier =
                    STEAM_WAND_FAN_DAMAGE_MULTIPLIER,
                forcedDamageType =
                    DamageType.WATER
            )
        }
    }

    private fun createProjectile(
        context: WeaponAttackContext,
        targetX: Float,
        targetY: Float,
        damageMultiplier: Float = 1f,
        forcedDamageType:
        DamageType? = null
    ) {
        /*
         * Her projectile’ın crit hesabı bağımsızdır.
         */
        val damageResult =
            damageCalculator.calculate(
                weapon = context.weapon
            )

        val definition =
            context.weapon.definition

        val levelStats =
            context.weapon.currentLevelStats

        val weaponId =
            definition.id.value

        val isUpgradedLongbow =
            weaponId == LONGBOW_ID &&
                context.weapon
                    .hasBasicAttackUpgrade

        val isCrossbow =
            weaponId == CROSSBOW_ID

        val isOrbOfSky =
            weaponId == ORB_OF_SKY_ID

        val isUpgradedOrbOfSky =
            isOrbOfSky &&
                context.weapon
                    .hasBasicAttackUpgrade

        /*
         * Orb Lv.3 sonrasında yalnızca Lightning
         * kullanır. Lv.1–2 hâlâ Air veya Lightning
         * arasında rastgele seçim yapar.
         */
        val selectedDamageType =
            when {
                forcedDamageType != null -> {
                    forcedDamageType
                }

                isUpgradedOrbOfSky -> {
                    DamageType.LIGHTNING
                }

                else -> {
                    selectDamageType(
                        damageTypes =
                            definition.damageTypes,

                        randomSelection =
                            definition
                                .randomDamageTypePerAttack
                    )
                }
            }

        /*
         * Orb’un normal seviyelerinde yalnızca
         * Lightning saldırısı sekebilir.
         */
        val isOrbLightningAttack =
            isOrbOfSky &&
                selectedDamageType ==
                DamageType.LIGHTNING

        val statAdditionalTargets =
            (
                playerStats
                    .projectileTargetCount() -
                    1
                )
                .coerceAtLeast(0)

        /*
         * Buradaki değerler ilk hedefin haricindeki
         * silaha özel hedef sayılarını belirtir.
         */
        val weaponSpecificAdditionalTargets =
            when {
                isUpgradedOrbOfSky -> {
                    /*
                     * İlk hedef + 4 sekme hedefi.
                     */
                    4
                }

                isOrbLightningAttack -> {
                    /*
                     * Lv.1–2:
                     * İlk hedef + 1 sekme hedefi.
                     */
                    1
                }

                isUpgradedLongbow -> {
                    1
                }

                else -> {
                    0
                }
            }

        val maximumTargets =
            (
                definition.baseMaximumTargets +
                    levelStats.additionalTargets +
                    statAdditionalTargets +
                    weaponSpecificAdditionalTargets
                )
                .coerceAtLeast(1)

        val effectiveRange =
            (
                definition.baseRange *
                    levelStats.rangeMultiplier
                )
                .coerceAtLeast(1f)

        val effectiveProjectileSpeed =
            definition
                .baseProjectileSpeed
                .coerceAtLeast(1f)

        /*
         * Orb’da yalnızca Air saldırısı küçük
         * knockback uygular.
         *
         * Lightning saldırılarında knockback yoktur.
         */
        val effectiveKnockback =
            if (
                isOrbOfSky &&
                selectedDamageType ==
                DamageType.LIGHTNING
            ) {
                0f
            } else {
                (
                    definition.baseKnockback *
                        levelStats.knockbackMultiplier
                    )
                    .coerceAtLeast(0f)
            }

        val secondaryHitDamageMultiplier =
            when {
                isOrbLightningAttack -> {
                    ORB_CHAIN_DAMAGE_MULTIPLIER
                }

                isCrossbow &&
                    context.weapon
                        .hasBasicAttackUpgrade -> {
                    1f
                }

                isCrossbow -> {
                    0.5f
                }

                else -> {
                    1f
                }
            }

        val ricochetCount =
            when {
                isUpgradedOrbOfSky -> {
                    4
                }

                isOrbLightningAttack -> {
                    1
                }

                isUpgradedLongbow -> {
                    1
                }

                else -> {
                    0
                }
            }

        val ricochetRange =
            when {
                isOrbLightningAttack -> {
                    ORB_LIGHTNING_CHAIN_RANGE
                }

                isUpgradedLongbow -> {
                    LONGBOW_RICOCHET_RANGE
                }

                else -> {
                    0f
                }
            }

        val postFirstHitTravelDistance =
            if (isCrossbow) {
                if (
                    context.weapon
                        .hasBasicAttackUpgrade
                ) {
                    CROSSBOW_UPGRADED_POST_HIT_RANGE
                } else {
                    CROSSBOW_NORMAL_POST_HIT_RANGE
                }
            } else {
                null
            }

        val finalDamage =
            (
                damageResult.damage *
                    damageMultiplier
                )
                .roundToInt()
                .coerceAtLeast(1)

        context.projectiles.add(
            Projectile(
                startX =
                    context.player.centerX,

                startY =
                    context.player.centerY,

                targetX =
                    targetX,

                targetY =
                    targetY,

                sourceWeaponName =
                    context.weapon.displayName,

                damage =
                    finalDamage,

                isCriticalHit =
                    damageResult.isCriticalHit,

                maximumTargets =
                    maximumTargets,

                projectileSpeed =
                    effectiveProjectileSpeed,

                maximumTravelDistance =
                    effectiveRange,

                damageType =
                    selectedDamageType,

                knockback =
                    effectiveKnockback,

                damageMultiplierAfterFirstHit =
                    secondaryHitDamageMultiplier,

                ricochetCount =
                    ricochetCount,

                ricochetRange =
                    ricochetRange,

                travelDistanceAfterFirstHit =
                    postFirstHitTravelDistance
            )
        )
    }

    private fun selectDamageType(
        damageTypes: Set<DamageType>,
        randomSelection: Boolean
    ): DamageType {
        if (damageTypes.isEmpty()) {
            return DamageType.NONE
        }

        if (
            randomSelection &&
            damageTypes.size > 1
        ) {
            return damageTypes.random()
        }

        return damageTypes.first()
    }
}
