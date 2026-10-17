package com.azizoglu.grisilith.weapon.skill

import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyDamageContext
import com.azizoglu.grisilith.enemy.EnemyManager
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.weapon.runtime.WeaponAttackContext
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.azizoglu.grisilith.weapon.runtime.WeaponInstance
import com.azizoglu.grisilith.weapon.runtime.WeaponRuntime
import com.azizoglu.grisilith.weapon.runtime.executor.ShortswordAttackExecutor
import com.azizoglu.grisilith.weapon.visual.attack.MeleeAttackVisualKind
import com.azizoglu.grisilith.weapon.visual.attack.MeleeAttackVisualManager
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.tan

/** Active abilities shared by the physical melee/ranged weapon families. */
class PhysicalWeaponAbilityRuntime(
    private val playerStats: PlayerStats,
    private val damageCalculator: WeaponDamageCalculator,
    private val shortswordExecutor: ShortswordAttackExecutor,
    private val defensiveLineRuntime: DefensiveLineRuntime
) {
    private enum class CastKind {
        RAPID_STROKES,
        AIR_SLASH,
        SPIN_ATTACK,
        DEFENSIVE_LINE_BURST,
        KNIFE_SWARM,
        LEISURELY_SHOT,
        HEAVY_BOLT
    }

    private data class Cast(
        val kind: CastKind,
        val weapon: WeaponInstance,
        val runtime: WeaponRuntime,
        val slotIndex: Int,
        var aimX: Float,
        var aimY: Float,
        var delaySeconds: Float = 0f,
        val defensiveLineBonus: Int = 0
    )

    private data class DelayedVolley(
        val weapon: WeaponInstance,
        val runtime: WeaponRuntime,
        val slotIndex: Int,
        val aimX: Float,
        val aimY: Float,
        var delaySeconds: Float
    )

    private data class DelayedKnifeSwarm(
        val weapon: WeaponInstance,
        val runtime: WeaponRuntime,
        val slotIndex: Int,
        var delaySeconds: Float
    )

    private data class BurstVisual(
        val centerX: Float,
        val centerY: Float,
        var remainingSeconds: Float = 0.55f
    )

    private val casts = mutableListOf<Cast>()
    private val delayedVolleys = mutableListOf<DelayedVolley>()
    private val delayedKnifeSwarms = mutableListOf<DelayedKnifeSwarm>()
    private val airSlashProjectiles = mutableListOf<Projectile>()
    private val burstVisuals = mutableListOf<BurstVisual>()
    private var lastAimX = 1f
    private var lastAimY = 0f

    private val batch = SpriteBatch()
    private val airSlashTexture by lazy {
        Texture(Gdx.files.internal(AIR_SLASH_TEXTURE)).also {
            it.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }
    }
    private val airSlashFrames by lazy {
        // Integer boundaries also support sheets whose dimensions are not multiples of four.
        Array(16) { index ->
            val column = index % 4
            val row = index / 4
            val x = column * airSlashTexture.width / 4
            val y = row * airSlashTexture.height / 4
            TextureRegion(airSlashTexture, x, y,
                (column + 1) * airSlashTexture.width / 4 - x,
                (row + 1) * airSlashTexture.height / 4 - y)
        }
    }
    private val burstTexture by lazy {
        Texture(Gdx.files.internal(DEFENSIVE_BURST_TEXTURE)).also {
            it.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }
    }

    fun updateAim(worldX: Float, worldY: Float) {
        lastAimX = worldX
        lastAimY = worldY
        casts.filter { it.kind == CastKind.LEISURELY_SHOT || it.kind == CastKind.HEAVY_BOLT }
            .forEach {
                it.aimX = worldX
                it.aimY = worldY
            }
    }

    fun isPreparing(weapon: WeaponInstance): Boolean = casts.any { it.weapon === weapon && it.delaySeconds > 0f }

    fun tryActivate(
        weapon: WeaponInstance,
        runtime: WeaponRuntime,
        slotIndex: Int,
        player: Player
    ): Boolean {
        val abilityId = weapon.unlockedAbility?.id ?: return false

        if (
            abilityId == DefaultWeaponAbilities.LEISURELY_SHOT_ID ||
            abilityId == DefaultWeaponAbilities.HEAVY_BOLTS_ID
        ) {
            val aimedKind =
                if (abilityId == DefaultWeaponAbilities.LEISURELY_SHOT_ID) {
                    CastKind.LEISURELY_SHOT
                } else {
                    CastKind.HEAVY_BOLT
                }

            casts.firstOrNull {
                it.weapon === weapon && it.kind == aimedKind
            }?.let {
                it.delaySeconds = 0f
                return true
            }
        }

        val kind = when (abilityId) {
            DefaultWeaponAbilities.RAPID_STROKES_ID -> CastKind.RAPID_STROKES
            DefaultWeaponAbilities.AIR_SLASH_ID -> CastKind.AIR_SLASH
            DefaultWeaponAbilities.SPIN_ATTACK_ID -> CastKind.SPIN_ATTACK
            DefaultWeaponAbilities.DEFENSIVE_LINE_BURST_ID -> CastKind.DEFENSIVE_LINE_BURST
            DefaultWeaponAbilities.KNIFE_SWARM_ID -> CastKind.KNIFE_SWARM
            DefaultWeaponAbilities.LEISURELY_SHOT_ID -> CastKind.LEISURELY_SHOT
            DefaultWeaponAbilities.HEAVY_BOLTS_ID -> CastKind.HEAVY_BOLT
            else -> return false
        }

        val lineBonus = if (kind == CastKind.DEFENSIVE_LINE_BURST) {
            defensiveLineRuntime.bonusArmorClass
        } else {
            0
        }
        if (kind == CastKind.DEFENSIVE_LINE_BURST && lineBonus <= 0) return false
        if (!runtime.tryActivateAbility()) return false

        casts += Cast(
            kind = kind,
            weapon = weapon,
            runtime = runtime,
            slotIndex = slotIndex,
            aimX = lastAimX.takeIf { it != 1f || lastAimY != 0f } ?: player.centerX + 1f,
            aimY = lastAimY.takeIf { lastAimX != 1f || it != 0f } ?: player.centerY,
            delaySeconds = 0f,
            defensiveLineBonus = lineBonus
        )

        if (kind == CastKind.DEFENSIVE_LINE_BURST) {
            defensiveLineRuntime.reset()
        }
        return true
    }

    fun update(
        delta: Float,
        player: Player,
        enemyManager: EnemyManager,
        projectiles: MutableList<Projectile>,
        attackOriginForSlot: (Int) -> Pair<Float, Float>,
        onDamageDealt: (Int, String) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
    ) {
        val safeDelta = delta.coerceAtLeast(0f)

        casts.toList().forEach { cast ->
            cast.delaySeconds = (cast.delaySeconds - safeDelta).coerceAtLeast(0f)
            if (cast.delaySeconds <= 0f) {
                executeCast(
                    cast, player, enemyManager, projectiles,
                    attackOriginForSlot, onDamageDealt, onEnemyKilled
                )
                cast.runtime.endActiveAbilityEarly()
                casts.remove(cast)
            }
        }

        delayedVolleys.toList().forEach { volley ->
            volley.delaySeconds -= safeDelta
            if (volley.delaySeconds <= 0f) {
                executeWithContext(
                    volley.weapon, volley.runtime, volley.slotIndex,
                    player, enemyManager, projectiles, attackOriginForSlot,
                    onDamageDealt, onEnemyKilled
                ) { context ->
                    fireLongbowVolley(context, volley.aimX, volley.aimY, upgraded = true)
                }
                delayedVolleys.remove(volley)
            }
        }

        delayedKnifeSwarms.toList().forEach { swarm ->
            swarm.delaySeconds -= safeDelta
            if (swarm.delaySeconds <= 0f) {
                executeWithContext(
                    swarm.weapon, swarm.runtime, swarm.slotIndex,
                    player, enemyManager, projectiles, attackOriginForSlot,
                    onDamageDealt, onEnemyKilled
                ) { context ->
                    fireKnifeSwarm(
                        context,
                        DefaultWeaponAbilities.KNIFE_SWARM_UPGRADED_ANGLE_OFFSET_DEGREES
                    )
                }
                delayedKnifeSwarms.remove(swarm)
            }
        }

        airSlashProjectiles.removeAll { !it.isActive }
        burstVisuals.forEach { it.remainingSeconds -= safeDelta }
        burstVisuals.removeAll { it.remainingSeconds <= 0f }
    }

    private fun executeCast(
        cast: Cast,
        player: Player,
        enemyManager: EnemyManager,
        projectiles: MutableList<Projectile>,
        attackOriginForSlot: (Int) -> Pair<Float, Float>,
        onDamageDealt: (Int, String) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
    ) {
        executeWithContext(
            cast.weapon, cast.runtime, cast.slotIndex, player, enemyManager,
            projectiles, attackOriginForSlot, onDamageDealt, onEnemyKilled
        ) { context ->
            when (cast.kind) {
                CastKind.RAPID_STROKES -> executeRapidStrokes(context, cast.aimX, cast.aimY)
                CastKind.AIR_SLASH -> fireAirSlash(context, cast.aimX, cast.aimY)
                CastKind.SPIN_ATTACK -> executeSpinAttack(context)
                CastKind.DEFENSIVE_LINE_BURST ->
                    executeDefensiveBurst(context, cast.defensiveLineBonus)
                CastKind.KNIFE_SWARM -> {
                    fireKnifeSwarm(context, 0f)
                    if (cast.weapon.hasWeaponAbilityUpgrade) {
                        delayedKnifeSwarms += DelayedKnifeSwarm(
                            cast.weapon,
                            cast.runtime,
                            cast.slotIndex,
                            DefaultWeaponAbilities.KNIFE_SWARM_UPGRADED_REPEAT_DELAY_SECONDS
                        )
                    }
                }
                CastKind.LEISURELY_SHOT -> {
                    fireLongbowVolley(
                        context, cast.aimX, cast.aimY,
                        upgraded = cast.weapon.hasWeaponAbilityUpgrade
                    )
                    if (cast.weapon.hasWeaponAbilityUpgrade) {
                        delayedVolleys += DelayedVolley(
                            cast.weapon, cast.runtime, cast.slotIndex,
                            cast.aimX, cast.aimY, 0.5f
                        )
                    }
                }
                CastKind.HEAVY_BOLT -> fireHeavyBolt(context, cast.aimX, cast.aimY)
            }
        }
    }

    private fun executeWithContext(
        weapon: WeaponInstance,
        runtime: WeaponRuntime,
        slotIndex: Int,
        player: Player,
        enemyManager: EnemyManager,
        projectiles: MutableList<Projectile>,
        attackOriginForSlot: (Int) -> Pair<Float, Float>,
        onDamageDealt: (Int, String) -> Unit,
        onEnemyKilled: (Enemy) -> Unit,
        action: (WeaponAttackContext) -> Unit
    ) {
        val origin = attackOriginForSlot(slotIndex)
        runtime.executeAbilityAttack(
            slotIndex, player, origin.first, origin.second, enemyManager,
            projectiles, onDamageDealt, onEnemyKilled, action
        )
    }

    private fun executeRapidStrokes(context: WeaponAttackContext, aimX: Float, aimY: Float) {
        val range = context.effectiveWeaponRange(
            context.weapon.definition.baseRange * context.weapon.currentLevelStats.rangeMultiplier
        ) * 1.25f
        val candidates = context.enemyManager.weaponTargets.filter {
            !it.isDead && Vector2.dst(
                context.attackOriginX, context.attackOriginY,
                it.centerX, it.centerY
            ) <= range + it.bounds.width / 2f
        }
        if (candidates.isEmpty()) return
        val count = if (context.weapon.hasWeaponAbilityUpgrade) {
            DefaultWeaponAbilities.RAPID_STROKES_UPGRADED_ATTACKS
        } else {
            DefaultWeaponAbilities.RAPID_STROKES_ATTACKS
        }
        val size = if (context.weapon.hasWeaponAbilityUpgrade) {
            DefaultWeaponAbilities.RAPID_STROKES_UPGRADED_SIZE_MULTIPLIER
        } else 1f
        repeat(count) {
            val living = candidates.filterNot { it.isDead }
            val pool = if (living.isEmpty()) candidates else living
            val target = if (com.azizoglu.grisilith.settings.GameSettings.abilityCasting ==
                com.azizoglu.grisilith.settings.GameSettings.AbilityCasting.AUTO) {
                com.azizoglu.grisilith.weapon.runtime.AbilityTargeting.select(pool,
                    context.attackOriginX, context.attackOriginY, range)
            } else pool.filterNot { it.isDead }.minByOrNull { Vector2.dst2(aimX, aimY, it.centerX, it.centerY) }
            shortswordExecutor.executeRapidStroke(
                context, target ?: return, size
            )
        }
    }

    private fun fireAirSlash(context: WeaponAttackContext, aimX: Float, aimY: Float) {
        val direction = direction(context, aimX, aimY)
        val upgraded = context.weapon.hasWeaponAbilityUpgrade
        val range = context.effectiveWeaponRange(
            if (upgraded) DefaultWeaponAbilities.AIR_SLASH_UPGRADED_RANGE
            else DefaultWeaponAbilities.AIR_SLASH_RANGE
        )
        val damage = damageCalculator.calculateNonCriticalDamage(
            context.weapon, DamageType.AIR
        )
        val scaledDamage = (damage * if (upgraded) {
            DefaultWeaponAbilities.AIR_SLASH_UPGRADED_DAMAGE_MULTIPLIER
        } else DefaultWeaponAbilities.AIR_SLASH_DAMAGE_MULTIPLIER).roundToInt().coerceAtLeast(1)
        val waveWidth = 2f * range * tan(
            DefaultWeaponAbilities.AIR_SLASH_ANGLE_DEGREES * 0.5f * MathUtils.degreesToRadians
        )
        val projectileStartX = context.attackOriginX - waveWidth / 2f
        val projectileStartY = context.attackOriginY - waveWidth / 2f
        val projectile = Projectile(
            startX = projectileStartX,
            startY = projectileStartY,
            targetX = projectileStartX + direction.x * range,
            targetY = projectileStartY + direction.y * range,
            sourceWeaponName = context.weapon.displayName + " — Air Slash",
            sourceWeapon = context.weapon,
            sourceWeaponSlotIndex = context.weaponSlotIndex,
            damage = scaledDamage,
            isCriticalHit = false,
            maximumTargets = Int.MAX_VALUE,
            projectileSpeed = 520f,
            maximumTravelDistance = range,
            damageType = DamageType.AIR,
            ignoresObstacleCollisions = true,
            ignoresWorldBoundaryCollisions = true,
            sizeMultiplier = waveWidth / 28f,
            attackerPiercingIgnoreRatio = playerStats.piercingIgnoreRatio(),
            canCriticallyHit = damageCalculator.canAbilityCriticallyHit(context.weapon),
            criticalHitChance = damageCalculator.criticalHitChance(context.weapon),
            criticalDamageMultiplier = damageCalculator.criticalDamageMultiplier(),
            targetDamageMultiplierProvider = if (upgraded) {
                { enemy ->
                    val missingPercent = (1f - enemy.health.healthRatio) * 100f
                    1f + missingPercent
                        .coerceAtMost(DefaultWeaponAbilities.AIR_SLASH_MAXIMUM_MISSING_HEALTH_PERCENT) *
                        DefaultWeaponAbilities.AIR_SLASH_MISSING_HEALTH_DAMAGE_PER_PERCENT
                }
            } else null,
            customVisualId = AIR_SLASH_VISUAL_ID
        )
        context.projectiles += projectile
        airSlashProjectiles += projectile
    }

    private fun executeSpinAttack(context: WeaponAttackContext) {
        val normalRadius = context.effectiveWeaponRange(
            context.weapon.definition.baseRange * context.weapon.currentLevelStats.rangeMultiplier
        ) * 0.66f
        val upgraded = context.weapon.hasWeaponAbilityUpgrade
        val radius = normalRadius * if (upgraded) {
            DefaultWeaponAbilities.SPIN_ATTACK_UPGRADED_RADIUS_MULTIPLIER
        } else DefaultWeaponAbilities.SPIN_ATTACK_BASE_RADIUS_MULTIPLIER
        val damageMultiplier = if (upgraded) {
            DefaultWeaponAbilities.SPIN_ATTACK_UPGRADED_DAMAGE_MULTIPLIER
        } else DefaultWeaponAbilities.SPIN_ATTACK_DAMAGE_MULTIPLIER
        MeleeAttackVisualManager.spawn(
            MeleeAttackVisualKind.LANCE_SPIN,
            context.attackOriginX, context.attackOriginY, 0f,
            radius * 2f, radius * 2f, 0.42f
        )
        context.enemyManager.weaponTargets.filter {
            !it.isDead && Vector2.dst(
                it.centerX, it.centerY,
                context.attackOriginX, context.attackOriginY
            ) <= radius + it.bounds.width / 2f
        }.forEach {
            damageDirect(context, it, DamageType.SLASHING, damageMultiplier, 0f)
        }
    }

    private fun executeDefensiveBurst(context: WeaponAttackContext, lineBonus: Int) {
        val armorForDamage = playerStats.totalScore(StatType.ARMOR_CLASS) + lineBonus
        // AC-scaled shield hit; the captured stacks were removed from totalScore at activation.
        val baseDamage = (context.weapon.definition.baseDamage * armorForDamage *
            DefaultWeaponAbilities.DEFENSIVE_LINE_BURST_AC_DAMAGE_MULTIPLIER)
            .roundToInt().coerceAtLeast(1)
        burstVisuals += BurstVisual(context.attackOriginX, context.attackOriginY)
        context.enemyManager.weaponTargets.filter {
            !it.isDead && Vector2.dst(
                it.centerX, it.centerY,
                context.attackOriginX, context.attackOriginY
            ) <= DefaultWeaponAbilities.DEFENSIVE_LINE_BURST_RADIUS + it.bounds.width / 2f
        }.forEach { enemy ->
            val knockback = DefaultWeaponAbilities.DEFENSIVE_LINE_BURST_KNOCKBACK *
                if (enemy.type == EnemyType.MINI_BOSS || enemy.type == EnemyType.FINAL_BOSS) 0.5f else 1f
            damageDirect(
                context, enemy, DamageType.BLUDGEONING, 1f, knockback,
                forcedBaseDamage = baseDamage
            )
        }
    }

    private fun fireKnifeSwarm(
        context: WeaponAttackContext,
        angleOffsetDegrees: Float
    ) {
        val count = DefaultWeaponAbilities.KNIFE_SWARM_PROJECTILES
        repeat(count) { index ->
            val direction = Vector2(1f, 0f).rotateDeg(
                angleOffsetDegrees + index * (360f / count)
            )
            createProjectile(
                context = context,
                direction = direction,
                range = DefaultWeaponAbilities.KNIFE_SWARM_RANGE,
                damageType = DamageType.PIERCING,
                projectileSpeed = context.weapon.definition.baseProjectileSpeed,
                /* Matches the regular thrown-dagger projectile scale. */
                sizeMultiplier = 0.5f,
                alwaysCritical = false
            )
        }
    }

    private fun fireLongbowVolley(
        context: WeaponAttackContext,
        aimX: Float,
        aimY: Float,
        upgraded: Boolean
    ) {
        val baseDirection = direction(context, aimX, aimY)
        val range = context.effectiveWeaponRange(
            context.weapon.definition.baseRange * context.weapon.currentLevelStats.rangeMultiplier
        )
        floatArrayOf(-20f, -10f, 0f, 10f, 20f).forEach { offset ->
            createProjectile(
                context = context,
                direction = Vector2(baseDirection).rotateDeg(offset),
                range = range,
                damageType = DamageType.PIERCING,
                projectileSpeed = context.weapon.definition.baseProjectileSpeed,
                ricochets = when {
                    upgraded -> 2
                    context.weapon.hasBasicAttackUpgrade -> 1
                    else -> 0
                },
                ricochetRange = 360f
            )
        }
    }

    private fun fireHeavyBolt(context: WeaponAttackContext, aimX: Float, aimY: Float) {
        val upgraded = context.weapon.hasWeaponAbilityUpgrade
        val baseRange = context.effectiveWeaponRange(
            context.weapon.definition.baseRange * context.weapon.currentLevelStats.rangeMultiplier
        )
        createProjectile(
            context = context,
            direction = direction(context, aimX, aimY),
            range = baseRange,
            damageType = DamageType.PIERCING,
            projectileSpeed = context.weapon.definition.baseProjectileSpeed,
            damageMultiplier = if (upgraded) 2f else 1.5f,
            sizeMultiplier = if (upgraded) 3f else 2.5f,
            maximumTargets = Int.MAX_VALUE,
            isHeavyBolt = true,
            bossMultiplier = if (upgraded) 1.5f else 1.25f,
            carryFirstTarget = upgraded
        )
    }

    private fun createProjectile(
        context: WeaponAttackContext,
        direction: Vector2,
        range: Float,
        damageType: DamageType,
        projectileSpeed: Float,
        damageMultiplier: Float = 1f,
        sizeMultiplier: Float = 1f,
        maximumTargets: Int = 1,
        ricochets: Int = 0,
        ricochetRange: Float = 0f,
        alwaysCritical: Boolean = false,
        isHeavyBolt: Boolean = false,
        bossMultiplier: Float = 1f,
        carryFirstTarget: Boolean = false
    ) {
        val dir = Vector2(direction).nor()
        val baseDamage = damageCalculator.calculateNonCriticalDamage(
            context.weapon, damageType,
            // Preserve Knife Swarm's existing damage; throw-only conversion is not a balance change.
            primaryPhysicalStatOverride = if (context.weapon.definition.id.value == "WPN_DAGGER") {
                StatType.STRENGTH
            } else null
        )
        context.projectiles += Projectile(
            startX = context.attackOriginX,
            startY = context.attackOriginY,
            targetX = context.attackOriginX + dir.x * range,
            targetY = context.attackOriginY + dir.y * range,
            sourceWeaponName = context.weapon.displayName,
            sourceWeapon = context.weapon,
            sourceWeaponSlotIndex = context.weaponSlotIndex,
            damage = (baseDamage * damageMultiplier).roundToInt().coerceAtLeast(1),
            isCriticalHit = alwaysCritical,
            maximumTargets = maximumTargets,
            projectileSpeed = projectileSpeed,
            maximumTravelDistance = range,
            damageType = damageType,
            knockback = context.weapon.definition.baseKnockback *
                context.weapon.currentLevelStats.knockbackMultiplier,
            damageMultiplierAfterFirstHit = 1f,
            ricochetCount = ricochets,
            ricochetRange = ricochetRange,
            sizeMultiplier = sizeMultiplier,
            isHeavyBolt = isHeavyBolt,
            firstTargetBossDamageMultiplier = 1f,
            carryFirstNormalTarget = carryFirstTarget,
            attackerPiercingIgnoreRatio = playerStats.piercingIgnoreRatio(),
            canCriticallyHit = alwaysCritical ||
                damageCalculator.canAbilityCriticallyHit(context.weapon),
            criticalHitChance = if (alwaysCritical) 1f
                else damageCalculator.criticalHitChance(context.weapon),
            criticalDamageMultiplier = damageCalculator.criticalDamageMultiplier(),
            targetDamageMultiplierProvider = if (bossMultiplier != 1f) {
                { enemy ->
                    if (enemy.type == EnemyType.MINI_BOSS || enemy.type == EnemyType.FINAL_BOSS) {
                        bossMultiplier
                    } else {
                        1f
                    }
                }
            } else null
        )
    }

    private fun damageDirect(
        context: WeaponAttackContext,
        enemy: Enemy,
        damageType: DamageType,
        damageMultiplier: Float,
        knockback: Float,
        forcedBaseDamage: Int? = null
    ) {
        if (enemy.isDead) return
        val damageResult = damageCalculator.calculateForHit(
            weapon = context.weapon,
            damageType = damageType,
            allowCritical = true
        )
        val raw = forcedBaseDamage?.let { base ->
            if (damageResult.isCriticalHit) {
                (base * damageCalculator.criticalDamageMultiplier()).roundToInt().coerceAtLeast(1)
            } else base
        } ?: damageResult.damage
        context.weapon.recordCalculatedCritical(damageResult.isCriticalHit)
        val damage = context.modifiedDamageAgainst(
            enemy, (raw * damageMultiplier).roundToInt().coerceAtLeast(1)
        )
        val wasAlive = !enemy.isDead
        val applied = enemy.takeDamage(
            damage,
            EnemyDamageContext(
                damageType = damageType,
                sourceWeaponRuntimeId = context.weapon.runtimeId,
                sourceWeaponDefinitionId = context.weapon.definition.id.value,
                sourceName = context.weapon.displayName,
                sourceWeaponTags = context.weapon.definition.tags,
                attackerPiercingIgnoreRatio = playerStats.piercingIgnoreRatio()
            )
        )
        if (applied > 0) {
            context.onDamageDealt(applied, context.weapon.displayName)
            if (knockback > 0f) {
                enemy.applyKnockback(
                    enemy.centerX - context.attackOriginX,
                    enemy.centerY - context.attackOriginY,
                    knockback
                )
            }
        }
        if (wasAlive && enemy.isDead) context.onEnemyKilled(enemy)
        context.reportWeaponHit(enemy, applied, damageType)
    }

    private fun direction(context: WeaponAttackContext, aimX: Float, aimY: Float): Vector2 {
        val result = Vector2(aimX - context.attackOriginX, aimY - context.attackOriginY)
        if (result.isZero) result.set(1f, 0f)
        return result.nor()
    }

    fun render(shapeRenderer: ShapeRenderer, player: Player) {
        renderTelegraphs(shapeRenderer, player)
        if (airSlashProjectiles.isEmpty() && burstVisuals.isEmpty()) return

        val projection = shapeRenderer.projectionMatrix.cpy()
        shapeRenderer.end()
        batch.projectionMatrix = projection
        batch.begin()

        airSlashProjectiles.forEach { projectile ->
            val progress = projectile.visualTravelProgress
            val alpha = (1f - (progress - 0.72f).coerceAtLeast(0f) / 0.28f)
                .coerceIn(0f, 1f)
            val rotation = atan2(
                projectile.visualVelocityY,
                projectile.visualVelocityX
            ) * MathUtils.radiansToDegrees
            // Compensate for the sheet's transparent cell padding, not the attack range.
            val width = if (projectile.sourceWeapon?.hasWeaponAbilityUpgrade == true) 430f else 332f
            val height = width * AIR_SLASH_TEXTURE_ASPECT
            val frame = airSlashFrames[(progress * airSlashFrames.size).toInt()
                .coerceIn(0, airSlashFrames.lastIndex)]
            batch.setColor(1f, 1f, 1f, alpha)
            batch.draw(
                frame,
                projectile.visualCenterX - width / 2f,
                projectile.visualCenterY - height / 2f,
                width / 2f, height / 2f,
                width, height, 1f, 1f, rotation
            )
        }

        burstVisuals.forEach { visual ->
            val progress = 1f - visual.remainingSeconds / 0.55f
            val size = 80f + 330f * progress
            batch.setColor(1f, 1f, 1f, (1f - progress).coerceIn(0f, 1f))
            batch.draw(
                burstTexture,
                visual.centerX - size / 2f,
                visual.centerY - size / 2f,
                size, size
            )
        }
        batch.color = Color.WHITE
        batch.end()
        shapeRenderer.projectionMatrix = projection
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
    }

    private fun renderTelegraphs(shapeRenderer: ShapeRenderer, player: Player) {
        val aimed = casts.filter {
            it.kind == CastKind.LEISURELY_SHOT || it.kind == CastKind.HEAVY_BOLT
        }
        if (aimed.isEmpty()) return
        shapeRenderer.end()
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        Gdx.gl.glLineWidth(3f)
        shapeRenderer.color = Color(1f, 0.72f, 0.18f, 0.85f)
        aimed.forEach { cast ->
            val base = Vector2(cast.aimX - player.centerX, cast.aimY - player.centerY)
            if (base.isZero) base.set(1f, 0f)
            base.nor()
            val range = if (cast.kind == CastKind.HEAVY_BOLT) 880f else 960f
            val angles = if (cast.kind == CastKind.LEISURELY_SHOT) {
                floatArrayOf(-20f, -10f, 0f, 10f, 20f)
            } else floatArrayOf(0f)
            angles.forEach { angle ->
                val d = Vector2(base).rotateDeg(angle)
                shapeRenderer.line(
                    player.centerX, player.centerY,
                    player.centerX + d.x * range,
                    player.centerY + d.y * range
                )
            }
        }
        Gdx.gl.glLineWidth(1f)
        shapeRenderer.end()
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
    }

    fun releaseAimedCastEarly(): Boolean {
        val aimedCast =
            casts.firstOrNull {
                it.kind == CastKind.LEISURELY_SHOT ||
                    it.kind == CastKind.HEAVY_BOLT
            }
                ?: return false

        aimedCast.delaySeconds = 0f
        return true
    }

    fun clear() {
        casts.clear()
        delayedVolleys.clear()
        delayedKnifeSwarms.clear()
        airSlashProjectiles.clear()
        burstVisuals.clear()
    }

    fun dispose() {
        airSlashTexture.dispose()
        burstTexture.dispose()
        batch.dispose()
    }

    companion object {
        private const val AIR_SLASH_VISUAL_ID = "AIR_SLASH"
        private const val AIR_SLASH_TEXTURE =
            "weapons_animations/weapon_skills/air_slash/air_slash_sheet_v3.png"
        private const val AIR_SLASH_TEXTURE_ASPECT = 1f
        private const val DEFENSIVE_BURST_TEXTURE =
            "weapons_animations/weapon_skills/defensive_line_burst/defensive_line_burst.png"
    }
}
