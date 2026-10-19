package com.azizoglu.grisilith.weapon.runtime.executor

import com.azizoglu.grisilith.audio.ProjectileSound

import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.audio.GameAudio
import com.azizoglu.grisilith.content.ConditionType
import com.azizoglu.grisilith.combat.condition.ConditionApplication
import com.azizoglu.grisilith.combat.condition.ConditionDamageCalculator
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackContext
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackExecutor
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.azizoglu.grisilith.weapon.runtime.WeaponTargetSelector
import com.badlogic.gdx.math.Vector2
import kotlin.math.roundToInt
import com.azizoglu.grisilith.weapon.skill.DefaultWeaponAbilities

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

        private const val IVY_STAFF_ID =
            "WPN_IVY_STAFF"

        private const val AMULET_OF_TROLL_KING_DISPLAY_NAME =
            "Amulet of Troll King"

        private const val ORB_OF_SKY_ID =
            "WPN_ORB_OF_SKY"

        private const val ORB_LIGHTNING_CHAIN_RANGE =
            310f

        private const val ORB_CHAIN_DAMAGE_MULTIPLIER =
            0.50f

        private const val LONGBOW_RICOCHET_RANGE =
            360f

        private val LEISURELY_SHOT_ANGLES =
            floatArrayOf(
                15f,
                5f,
                -5f,
                -15f
            )

        private const val CROSSBOW_NORMAL_POST_HIT_RANGE =
            170f

        private const val CROSSBOW_UPGRADED_POST_HIT_RANGE =
            310f

        private const val HEAVY_BOLT_TRAVEL_RANGE_MULTIPLIER =
            20f

        /*
         * Steam Wand Lv.3 yelpazesi.
         */
        private const val STEAM_WAND_FAN_PROJECTILE_COUNT =
            5

        private const val STEAM_WAND_FAN_ANGLE_STEP =
            8f

        private const val STEAM_WAND_FAN_CENTER_DAMAGE_MULTIPLIER = 0.70f
        private const val STEAM_WAND_FAN_SIDE_DAMAGE_MULTIPLIER = 0.25f

        private const val TOTEM_OF_DARK_CLOUDS_ID =
            "WPN_TOTEM_OF_DARK_CLOUDS"

        private const val ENCHANTED_CHALK_DUST_ID =
            "WPN_ENCHANTED_CHALK_DUST"

        /* Lv.4: only the central shot remains; its existing damage is unchanged. */
        private const val CHALK_DUST_DAMAGE_MULTIPLIER =
            0.70f

        private const val CHALK_DUST_WALL_BOUNCES =
            2

        private const val CHALK_DUST_RANGE_MULTIPLIER =
            3f
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
                context.effectiveWeaponRange(
                    definition.baseRange *
                        levelStats.rangeMultiplier
                )
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
                    effectiveRange
            ) ?: return

        val weaponId =
            definition.id.value

        val attackNumber =
            context.weapon.registerNormalAttack()
        when(weaponId) {
            LONGBOW_ID -> GameAudio.projectile(ProjectileSound.ARROW,context.attackOriginX,context.attackOriginY)
            CROSSBOW_ID -> GameAudio.projectile(ProjectileSound.BOLT,context.attackOriginX,context.attackOriginY)
        }

        val isUpgradedSteamWand =
            weaponId == STEAM_WAND_ID &&
                context.weapon
                    .hasBasicAttackUpgrade

        if (isUpgradedSteamWand) {
            executeUpgradedSteamWandAttack(
                context = context,
                targetX = target.centerX,
                targetY = target.centerY,
                effectiveRange = effectiveRange,
                attackNumber = attackNumber
            )

            return
        }

        val isUpgradedChalkDust =
            weaponId ==
                ENCHANTED_CHALK_DUST_ID &&
                context.weapon
                    .hasBasicAttackUpgrade

        if (isUpgradedChalkDust) {
            executeUpgradedChalkDustAttack(
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
    private fun executeLeisurelyShot(
        context: WeaponAttackContext,
        targetX: Float,
        targetY: Float,
        effectiveRange: Float
    ) {
        val baseDirection =
            Vector2(
                targetX -
                    context.attackOriginX,
                targetY -
                    context.attackOriginY
            )

        if (baseDirection.isZero) {
            baseDirection.set(1f, 0f)
        }

        baseDirection.nor()

        LEISURELY_SHOT_ANGLES.forEach {
                angleDegrees ->

            val direction =
                Vector2(baseDirection)
                    .rotateDeg(angleDegrees)

            createProjectile(
                context = context,
                targetX =
                    context.attackOriginX +
                        direction.x *
                        effectiveRange,
                targetY =
                    context.attackOriginY +
                        direction.y *
                        effectiveRange
            )
        }
    }

    private fun executeUpgradedSteamWandAttack(
        context: WeaponAttackContext,
        targetX: Float,
        targetY: Float,
        effectiveRange: Float,
        attackNumber: Int
    ) {
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
                    context.attackOriginX,

                targetY -
                    context.attackOriginY
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
                context.attackOriginX +
                    projectileDirection.x *
                    effectiveRange

            val adjustedTargetY =
                context.attackOriginY +
                    projectileDirection.y *
                    effectiveRange

            createProjectile(
                context = context,
                targetX = adjustedTargetX,
                targetY = adjustedTargetY,
                damageMultiplier =
                    if (indexOffset == 0) STEAM_WAND_FAN_CENTER_DAMAGE_MULTIPLIER
                    else STEAM_WAND_FAN_SIDE_DAMAGE_MULTIPLIER,
                forcedDamageType =
                    DamageType.WATER
            )
        }
    }

    private fun executeUpgradedChalkDustAttack(
        context: WeaponAttackContext,
        targetX: Float,
        targetY: Float,
        effectiveRange: Float
    ) {
        // Keep the existing central shot's damage, two wall bounces and extended travel.
        createProjectile(context, targetX, targetY,
            damageMultiplier = CHALK_DUST_DAMAGE_MULTIPLIER,
            forcedDamageType = DamageType.AIR,
            wallBounceCount = CHALK_DUST_WALL_BOUNCES,
            travelRangeMultiplier = CHALK_DUST_RANGE_MULTIPLIER)
    }

    private fun createProjectile(
        context: WeaponAttackContext,
        targetX: Float,
        targetY: Float,
        damageMultiplier: Float = 1f,
        forcedDamageType:
        DamageType? = null,
        wallBounceCount: Int = 0,
        travelRangeMultiplier: Float = 1f,
        forcedMaximumTargets: Int? = null,
        forcedDamageMultiplierAfterFirstHit:
        Float? = null,
        sizeMultiplier: Float = 1f,
        activationDelaySeconds: Float = 0f,
        isHeavyBolt: Boolean = false,
        firstTargetBossDamageMultiplier:
        Float = 1f,
        carryFirstNormalTarget:
        Boolean = false
    ) {
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

        val isUpgradedIvyStaff =
            weaponId ==
                IVY_STAFF_ID &&
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

        val isTotemOfDarkClouds =
            weaponId ==
                TOTEM_OF_DARK_CLOUDS_ID

        val isEnchantedChalkDust =
            weaponId ==
                ENCHANTED_CHALK_DUST_ID

        val isUpgradedTotemOfDarkClouds =
            isTotemOfDarkClouds &&
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

                isUpgradedTotemOfDarkClouds -> {
                    DamageType.NECROTIC
                }

                context.weapon
                    .hasBasicAttackUpgrade &&
                    definition
                        .upgradedBasicAttackDamageType !=
                    null -> {

                    definition
                        .upgradedBasicAttackDamageType
                        ?: DamageType.NONE
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

        val isSpellFocus = com.azizoglu.grisilith.content.ContentTag.SPELL_FOCUS in definition.tags
        val projectileDamageType = if(isSpellFocus)
            com.azizoglu.grisilith.item.runtime.ActiveItemEffects.resolveNormalProjectileDamageType(selectedDamageType)
            else selectedDamageType
        if(isSpellFocus) GameAudio.magicalShot(projectileDamageType,context.attackOriginX,context.attackOriginY)

        val conditionApplication =
            conditionApplicationFor(
                context = context,
                weaponId = weaponId,
                selectedDamageType = selectedDamageType
            )

        /*
         * Orb’un normal seviyelerinde yalnızca
         * Lightning saldırısı sekebilir.
         */
        val isOrbLightningAttack =
            isOrbOfSky &&
                selectedDamageType ==
                DamageType.LIGHTNING

        val isTotemLightningAttack =
            isTotemOfDarkClouds &&
                selectedDamageType ==
                DamageType.LIGHTNING

        /*
         * Buradaki değerler ilk hedefin haricindeki
         * silaha özel hedef sayılarını belirtir.
         */
        val weaponSpecificAdditionalTargets =
            when {
                isUpgradedOrbOfSky -> {
                    4
                }

                isOrbLightningAttack -> {
                    1
                }

                isTotemLightningAttack -> {
                    1
                }

                isUpgradedLongbow -> {
                    1
                }

                else -> {
                    0
                }
            }

        val calculatedMaximumTargets =
            if (isEnchantedChalkDust) {
                1
            } else {
                (
                    definition.baseMaximumTargets +
                        levelStats.additionalTargets +
                        weaponSpecificAdditionalTargets
                    )
                    .coerceAtLeast(1)
            }

        val maximumTargets =
            forcedMaximumTargets
                ?: calculatedMaximumTargets

        val effectiveRange =
            context.effectiveWeaponRange(
                definition.baseRange *
                    levelStats.rangeMultiplier *
                    travelRangeMultiplier
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
                com.azizoglu.grisilith.content
                    .ContentTag.SPELL_FOCUS in
                definition.tags
            ) {
                0f
            } else {
                (
                    definition.baseKnockback *
                        levelStats.knockbackMultiplier
                    )
                    .coerceAtLeast(0f)
            }

        val calculatedSecondaryHitDamageMultiplier =
            when {
                isOrbLightningAttack -> {
                    ORB_CHAIN_DAMAGE_MULTIPLIER
                }

                isTotemLightningAttack -> {
                    0.50f
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

        val secondaryHitDamageMultiplier =
            forcedDamageMultiplierAfterFirstHit
                ?: calculatedSecondaryHitDamageMultiplier

        val ricochetCount =
            when {
                isUpgradedOrbOfSky -> {
                    4
                }

                isOrbLightningAttack -> {
                    1
                }

                isTotemLightningAttack -> {
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

                isTotemLightningAttack -> {
                    280f
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

        val nonCriticalDamage =
            damageCalculator
                .calculateNonCriticalDamage(
                    weapon =
                        context.weapon,

                    damageType =
                        selectedDamageType
                )

        val finalDamage =
            (
                nonCriticalDamage *
                    damageMultiplier
                )
                .roundToInt()
                .coerceAtLeast(1)

        val effectiveSizeMultiplier =
            sizeMultiplier *
                if (
                    isUpgradedIvyStaff
                ) {
                    DefaultWeaponAbilities
                        .IVY_STAFF_PROJECTILE_SIZE_MULTIPLIER
                } else {
                    1f
                }

        context.projectiles.add(
            Projectile(
                startX =
                    context.attackOriginX,

                startY =
                    context.attackOriginY,

                targetX =
                    targetX,

                targetY =
                    targetY,

                sourceWeaponName =
                    context.weapon.displayName,

                sourceWeapon =
                    context.weapon,

                sourceWeaponSlotIndex =
                    context.weaponSlotIndex,

                damage =
                    finalDamage,

                isCriticalHit =
                    false,

                maximumTargets =
                    maximumTargets,

                projectileSpeed =
                    effectiveProjectileSpeed,

                maximumTravelDistance =
                    effectiveRange,

                damageType =
                    projectileDamageType,
                chaosDamageResolved = isSpellFocus,

                knockback =
                    effectiveKnockback,

                damageMultiplierAfterFirstHit =
                    secondaryHitDamageMultiplier,

                ricochetCount =
                    ricochetCount,

                ricochetRange =
                    ricochetRange,

                travelDistanceAfterFirstHit =
                    postFirstHitTravelDistance,

                wallBounceCount =
                    wallBounceCount,

                sizeMultiplier =
                    effectiveSizeMultiplier,

                activationDelaySeconds =
                    activationDelaySeconds,

                isHeavyBolt =
                    isHeavyBolt,

                firstTargetBossDamageMultiplier =
                    firstTargetBossDamageMultiplier,

                carryFirstNormalTarget =
                    carryFirstNormalTarget,

                conditionApplication =
                    conditionApplication,

                armorClassReduction =
                    if (
                        isUpgradedIvyStaff
                    ) {
                        DefaultWeaponAbilities
                            .IVY_STAFF_ARMOR_CLASS_REDUCTION
                    } else {
                        0
                    },

                armorClassReductionDurationSeconds =
                    if (
                        isUpgradedIvyStaff
                    ) {
                        DefaultWeaponAbilities
                            .IVY_STAFF_ARMOR_CLASS_REDUCTION_SECONDS
                    } else {
                        0f
                    },

                attackerPiercingIgnoreRatio =
                    playerStats
                        .piercingIgnoreRatio(),

                canCriticallyHit =
                    damageCalculator
                        .canCriticallyHit(
                            weapon =
                                context.weapon,

                            damageType =
                                selectedDamageType
                        ),

                criticalHitChance =
                    damageCalculator
                        .criticalHitChance(
                            context.weapon
                        ),

                criticalDamageMultiplier =
                    damageCalculator
                        .criticalDamageMultiplier()
            )
        )
    }

    private fun conditionApplicationFor(
        context: WeaponAttackContext,
        weaponId: String,
        selectedDamageType: DamageType
    ): ConditionApplication? {

        val conditionType =
            when {
                weaponId == IVY_STAFF_ID ->
                    ConditionType.CORRODING

                weaponId == STEAM_WAND_ID &&
                    selectedDamageType == DamageType.WATER ->
                    ConditionType.WET

                context.weapon.displayName ==
                    AMULET_OF_TROLL_KING_DISPLAY_NAME &&
                    selectedDamageType == DamageType.WATER ->
                    ConditionType.WET

                weaponId == TOTEM_OF_DARK_CLOUDS_ID &&
                    selectedDamageType == DamageType.NECROTIC ->
                    ConditionType.NECROSIS

                context.weapon.displayName ==
                    "Tome of Eryndor" &&
                    selectedDamageType == DamageType.FIRE ->
                    ConditionType.BURNED

                else -> null
            } ?: return null

        return ConditionDamageCalculator.createApplication(
            type = conditionType,
            playerStats = playerStats,
            sourceWeaponRuntimeId = context.weapon.runtimeId,
            sourceWeaponDefinitionId = context.weapon.definition.id.value,
            sourceName = context.weapon.displayName,
            sourceCenterX = context.attackOriginX,
            sourceCenterY = context.attackOriginY
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
