package com.azizoglu.grisilith.summon

import com.azizoglu.grisilith.combat.condition.ConditionDamageCalculator
import com.azizoglu.grisilith.combat.damage.DamageNumberEvent
import com.azizoglu.grisilith.content.ConditionType
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyStats
import com.azizoglu.grisilith.enemy.EnemyDamageContext
import com.azizoglu.grisilith.map.MapObstacle
import com.badlogic.gdx.math.Rectangle
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.weapon.runtime.WeaponInventory
import com.azizoglu.grisilith.weapon.runtime.WeaponInstance
import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.weapon.skill.WeaponAbilityState
import com.azizoglu.grisilith.weapon.summon.IvySummonChoice
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.azizoglu.grisilith.item.runtime.ActiveItemEffects
import kotlin.math.roundToInt

class SummonManager(
    private val weaponInventory: WeaponInventory,
    private val playerStats: PlayerStats,
    private val currentStageProvider: () -> Int,
    private var mapObstacles: List<MapObstacle> =
        emptyList()
) {

    fun replaceMapObstacles(obstacles: List<MapObstacle>) {
        mapObstacles = obstacles
    }

    private val entities =
        mutableListOf<SummonedEntity>()

    private val schedulersByWeaponRuntimeId =
        linkedMapOf<String, SummonSpawnScheduler>()

    private var villageGuardDeathsSinceLastCheck =
        0

    private val removedSummonDamageNumberEvents =
        mutableListOf<DamageNumberEvent>()

    val activeSummons:
        List<SummonedEntity>
        get() =
            entities.toList()

    fun resolvePlayerCollisions(
        resolveCollision: (Rectangle) -> Boolean
    ) {
        entities.forEach { summon ->
            if (!summon.isExpired) {
                resolveCollision(summon.combatBounds)
            }
        }
    }

    fun resolveBodyCollisions(
        playerBounds: Rectangle
    ) {
        val active =
            entities.filterNot(SummonedEntity::isExpired)

        repeat(COLLISION_SOLVER_PASSES) {
            active.forEach { summon ->
                summon.separateFromPlayer(playerBounds)
            }

            for (firstIndex in active.indices) {
                for (secondIndex in firstIndex + 1 until active.size) {
                    active[firstIndex].separateFrom(active[secondIndex])
                }
            }
        }
    }

    fun update(
        delta: Float,
        playerCenterX: Float,
        playerCenterY: Float,
        worldWidth: Float,
        worldHeight: Float,
        enemies: List<Enemy>,
        projectiles: MutableList<Projectile>,
        onDamageDealt: (
            damage: Int,
            sourceName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit,
        onPlayerDamage: (
            damage: Int,
            sourceName: String
        ) -> Unit
    ) {

        if (
            delta <= 0f
        ) {
            return
        }

        synchronizeSchedulers()

        entities
            .toList()
            .forEach {
                    entity ->

                entity.update(
                    delta =
                        delta,

                    playerCenterX =
                        playerCenterX,

                    playerCenterY =
                        playerCenterY,

                    worldWidth =
                        worldWidth,

                    worldHeight =
                        worldHeight,

                    mapObstacles =
                        mapObstacles,

                    enemies =
                        enemies,

                    otherSummons =
                        entities,

                    onCowAttack = {
                            summon,
                            enemy ->

                        applyEnemyAttack(
                            summon =
                                summon,

                            enemy =
                                enemy,

                            attackName =
                                "Summon Animal",

                            damageType =
                                DamageType.BLUDGEONING,

                            conditionType =
                                null,

                            onDamageDealt =
                                onDamageDealt,

                            onEnemyKilled =
                                onEnemyKilled
                        )
                    },

                    onImpAttack = {
                            summon,
                            enemy ->

                        spawnImpFireProjectile(
                            summon =
                                summon,

                            targetCenterX =
                                enemy.centerX,

                            targetCenterY =
                                enemy.centerY,

                            projectiles =
                                projectiles
                        )
                    },

                    onToadAttack = {
                            summon,
                            enemy ->

                        spawnToadPoisonProjectile(
                            summon = summon,
                            targetCenterX = enemy.centerX,
                            targetCenterY = enemy.centerY,
                            projectiles = projectiles
                        )
                    },

                    onGuardAttack = {
                            summon,
                            enemy ->

                        applyEnemyAttack(
                            summon = summon,
                            enemy = enemy,
                            attackName = "Village Guard — Lance",
                            damageType = DamageType.PIERCING,
                            conditionType = null,
                            onDamageDealt = onDamageDealt,
                            onEnemyKilled = onEnemyKilled
                        )
                    },

                    onKnightEnemyAttack = {
                            summon,
                            enemy ->

                        applyEnemyAttack(
                            summon =
                                summon,

                            enemy =
                                enemy,

                            attackName =
                                "Call the Oathbreaker",

                            damageType =
                                DamageType.NECROTIC,

                            conditionType =
                                ConditionType.NECROSIS,

                            onDamageDealt =
                                onDamageDealt,

                            onEnemyKilled =
                                onEnemyKilled
                        )
                    },

                    onCowSummonAttack = {
                            summon,
                            target ->

                        applySummonAttack(
                            attacker =
                                summon,

                            target =
                                target,

                            attackName =
                                "Summon Animal"
                        )
                    },

                    onImpSummonAttack = {
                            summon,
                            target ->

                        spawnImpFireProjectile(
                            summon =
                                summon,

                            targetCenterX =
                                target.centerX,

                            targetCenterY =
                                target.centerY,

                            projectiles =
                                projectiles
                        )
                    },

                    onKnightSummonAttack = {
                            summon,
                            target ->

                        applySummonAttack(
                            attacker =
                                summon,

                            target =
                                target,

                            attackName =
                                "Call the Oathbreaker"
                        )
                    },

                    onKnightPlayerAttack = {
                            summon ->

                        onPlayerDamage(
                            summon.stats.baseDamage,
                            summon.sourceWeaponDisplayName +
                                " — Call the Oathbreaker"
                        )
                    }
                )
            }

        val endedWaveWeaponIds =
            entities
                .asSequence()
                .filter {
                    it.isExpired
                }
                .map {
                    it.sourceWeaponRuntimeId
                }
                .toSet()

        entities
            .filter { it.health.isDead }
            .forEach(ActiveItemEffects::onSummonDied)

        villageGuardDeathsSinceLastCheck +=
            entities.count {
                it.type == SummonType.GUARD &&
                    it.health.isDead
            }

        entities
            .filter { it.isExpired }
            .forEach {
                removedSummonDamageNumberEvents +=
                    it.consumeDamageNumberEvents()
            }

        entities.removeAll {
            it.isExpired
        }

        endedWaveWeaponIds
            .forEach {
                    weaponRuntimeId ->

                val stillHasLivingEntity =
                    entities.any {
                        it.sourceWeaponRuntimeId ==
                            weaponRuntimeId
                    }

                if (
                    !stillHasLivingEntity
                ) {
                    schedulersByWeaponRuntimeId[
                        weaponRuntimeId
                    ]
                        ?.markWaveEnded()
                }
            }

        /*
         * Faz 1.4a:
         * summon waves no longer auto-spawn.
         * Only cooldown timers are advanced here.
         */
        schedulersByWeaponRuntimeId
            .values
            .forEach {
                    scheduler ->

                scheduler.update(
                    delta
                )
            }
    }

    /*
     * Manual Ivy Staff activation entry point.
     *
     * Returns true only when:
     * - the selected slot contains an eligible Ivy Staff,
     * - a summon choice exists,
     * - the lifecycle is READY,
     * - the wave was actually spawned.
     */
    fun tryActivateIvyStaffInSlot(
        slotIndex: Int,
        playerCenterX: Float,
        playerCenterY: Float,
        worldWidth: Float,
        worldHeight: Float
    ): Boolean {

        synchronizeSchedulers()

        val weapon =
            weaponInventory
                .weaponAt(
                    slotIndex
                )
                ?: return false

        if (
            weapon.definition.id.value !=
            IVY_STAFF_ID ||
            !weapon.hasWeaponAbility ||
            weapon.ivySummonChoice ==
            null
        ) {
            return false
        }

        val scheduler =
            schedulersByWeaponRuntimeId[
                weapon.runtimeId
            ]
                ?: return false

        if (!scheduler.isReadyToActivate) {
            return false
        }

        return spawnWave(
            scheduler =
                scheduler,

            playerCenterX =
                playerCenterX,

            playerCenterY =
                playerCenterY,

            worldWidth =
                worldWidth,

            worldHeight =
                worldHeight
        )
    }

    /*
     * Future HUD/slot-rework API.
     * The current HUD does not need to consume these yet.
     */
    /*
     * Preferred API for future HUD / weapon-slot rendering.
     *
     * This returns one coherent snapshot instead of forcing UI code to call
     * state / active time / cooldown / choice separately.
     */
    fun slotPresentationState(
        slotIndex: Int
    ): SummonSlotPresentationState? {

        synchronizeSchedulers()

        val weapon =
            weaponInventory
                .weaponAt(
                    slotIndex
                )
                ?: return null

        if (
            weapon.definition.id.value !=
            IVY_STAFF_ID ||
            !weapon.hasWeaponAbility ||
            weapon.ivySummonChoice ==
            null
        ) {
            return null
        }

        val scheduler =
            schedulersByWeaponRuntimeId[
                weapon.runtimeId
            ]
                ?: return null

        val livingSummons =
            entities.count {
                it.sourceWeaponRuntimeId ==
                    weapon.runtimeId &&
                    !it.isExpired
            }

        val expectedWaveCount =
            waveCountFor(
                summonTypeFor(scheduler.choice),
                weapon
            )

        val remainingActive =
            entities
                .asSequence()
                .filter {
                    it.sourceWeaponRuntimeId ==
                        weapon.runtimeId &&
                        !it.isExpired
                }
                .map {
                    it.remainingLifetimeSeconds
                }
                .maxOrNull()
                ?: 0f

        return SummonSlotPresentationState(
            slotIndex =
                slotIndex,

            weaponRuntimeId =
                weapon.runtimeId,

            choice =
                scheduler.choice,

            state =
                scheduler.state,

            remainingActiveSeconds =
                remainingActive,

            remainingCooldownSeconds =
                scheduler
                    .remainingCooldownSeconds,

            activeSummonCount =
                livingSummons,

            totalSummonCountForWave =
                expectedWaveCount
        )
    }

    fun abilityStateForSlot(
        slotIndex: Int
    ): WeaponAbilityState? {

        synchronizeSchedulers()

        val weapon =
            weaponInventory
                .weaponAt(
                    slotIndex
                )
                ?: return null

        if (
            weapon.definition.id.value !=
            IVY_STAFF_ID ||
            !weapon.hasWeaponAbility ||
            weapon.ivySummonChoice ==
            null
        ) {
            return null
        }

        return schedulersByWeaponRuntimeId[
            weapon.runtimeId
        ]
            ?.state
    }

    fun remainingActiveSecondsForSlot(
        slotIndex: Int
    ): Float {

        val weapon =
            weaponInventory
                .weaponAt(
                    slotIndex
                )
                ?: return 0f

        return entities
            .asSequence()
            .filter {
                it.sourceWeaponRuntimeId ==
                    weapon.runtimeId &&
                    !it.isExpired
            }
            .map {
                it.remainingLifetimeSeconds
            }
            .maxOrNull()
            ?: 0f
    }

    fun remainingCooldownSecondsForSlot(
        slotIndex: Int
    ): Float {

        synchronizeSchedulers()

        val weapon =
            weaponInventory
                .weaponAt(
                    slotIndex
                )
                ?: return 0f

        return schedulersByWeaponRuntimeId[
            weapon.runtimeId
        ]
            ?.remainingCooldownSeconds
            ?: 0f
    }

    fun activeChoiceForSlot(
        slotIndex: Int
    ): IvySummonChoice? {

        val weapon =
            weaponInventory
                .weaponAt(
                    slotIndex
                )
                ?: return null

        return schedulersByWeaponRuntimeId[
            weapon.runtimeId
        ]
            ?.choice
    }

    fun allSlotPresentationStates():
        List<SummonSlotPresentationState> {

        val result =
            mutableListOf<SummonSlotPresentationState>()

        for (
        slotIndex in
        0 until
            MAX_WEAPON_SLOTS
        ) {
            slotPresentationState(
                slotIndex
            )
                ?.let {
                    result.add(
                        it
                    )
                }
        }

        return result
    }

    fun render(
        shapeRenderer: ShapeRenderer
    ) {

        entities.forEach {
            it.render(
                shapeRenderer
            )
        }
    }

    fun clear() {

        entities.clear()
        schedulersByWeaponRuntimeId.clear()
        villageGuardDeathsSinceLastCheck = 0
        removedSummonDamageNumberEvents.clear()
    }

    fun consumeDamageNumberEvents(): List<DamageNumberEvent> {
        val result =
            buildList {
                addAll(removedSummonDamageNumberEvents)
                entities.forEach {
                    addAll(it.consumeDamageNumberEvents())
                }
            }

        removedSummonDamageNumberEvents.clear()
        return result
    }

    fun healVillageGuards(healthRatio: Float) {
        entities
            .filter {
                it.type == SummonType.GUARD &&
                    !it.isExpired
            }
            .forEach {
                it.heal(
                    (it.health.maximumHealth * healthRatio)
                        .roundToInt()
                        .coerceAtLeast(1)
                )
            }
    }

    val livingVillageGuardCount: Int
        get() =
            entities.count {
                it.type == SummonType.GUARD &&
                    !it.isExpired
            }

    fun consumeVillageGuardDeaths(): Int {
        val deaths = villageGuardDeathsSinceLastCheck
        villageGuardDeathsSinceLastCheck = 0
        return deaths
    }

    fun spawnVillageGuard(
        stage: Int,
        sourceEnemyName: String,
        sourceEnemyStats: EnemyStats,
        centerX: Float,
        centerY: Float,
        leashCenterX: Float,
        leashCenterY: Float,
        maximumLeashDistance: Float
    ): Boolean {
        if (livingVillageGuardCount >= MAXIMUM_VILLAGE_GUARDS) {
            return false
        }

        val lanceLevel =
            (stage.coerceAtLeast(1) / 10 + 1)
                .coerceIn(1, 5)

        entities.add(
            SummonedEntity(
                sourceWeaponRuntimeId = "TROUBLE_AT_HOME_GUARD",
                sourceWeaponDefinitionId = "WPN_LANCE",
                sourceWeaponDisplayName =
                    "Village Guard — $sourceEnemyName (Lance Lv.$lanceLevel)",
                type = SummonType.GUARD,
                stats =
                    SummonStats.createGuard(
                        sourceStats = sourceEnemyStats,
                        lanceLevel = lanceLevel
                    ),
                startCenterX = centerX,
                startCenterY = centerY,
                lifetimeSeconds = Float.POSITIVE_INFINITY,
                leashCenterX = leashCenterX,
                leashCenterY = leashCenterY,
                maximumLeashDistance = maximumLeashDistance
            )
        )

        return true
    }

    fun spawnBoneTotemToads(
        count: Int,
        centerX: Float,
        centerY: Float,
        worldWidth: Float,
        worldHeight: Float
    ) {
        val stats = SummonStats.create(
            type = SummonType.TOAD,
            playerStats = playerStats,
            stage = currentStageProvider()
        )

        repeat(count.coerceAtLeast(0)) { index ->
            val angle = MathUtils.PI2 * index / count.coerceAtLeast(1)
            entities.add(
                SummonedEntity(
                    sourceWeaponRuntimeId = BONE_TOTEM_SOURCE_ID,
                    sourceWeaponDefinitionId = BONE_TOTEM_SOURCE_ID,
                    sourceWeaponDisplayName = "Bone Totem",
                    type = SummonType.TOAD,
                    stats = stats,
                    startCenterX = (centerX + MathUtils.cos(angle) * 46f).coerceIn(12f, worldWidth - 12f),
                    startCenterY = (centerY + MathUtils.sin(angle) * 46f).coerceIn(12f, worldHeight - 12f),
                    lifetimeSeconds = BONE_TOTEM_TOAD_LIFETIME_SECONDS
                )
            )
        }
    }

    fun trySpawnToadCycleToad(
        centerX: Float,
        centerY: Float,
        worldWidth: Float,
        worldHeight: Float
    ): Boolean {
        val livingCycleToads =
            entities.count {
                it.sourceWeaponRuntimeId == TOAD_CYCLE_SOURCE_ID &&
                    !it.isExpired
            }

        if (livingCycleToads >= TOAD_CYCLE_MAXIMUM_TOADS) {
            return false
        }

        val stats =
            SummonStats.create(
                type = SummonType.TOAD,
                playerStats = playerStats,
                stage = currentStageProvider()
            )

        val angle =
            MathUtils.PI2 *
                livingCycleToads /
                TOAD_CYCLE_MAXIMUM_TOADS

        entities.add(
            SummonedEntity(
                sourceWeaponRuntimeId = TOAD_CYCLE_SOURCE_ID,
                sourceWeaponDefinitionId = TOAD_CYCLE_SOURCE_ID,
                sourceWeaponDisplayName = "The Toad Cycle",
                type = SummonType.TOAD,
                stats = stats,
                startCenterX =
                    (centerX + MathUtils.cos(angle) * 40f)
                        .coerceIn(12f, worldWidth - 12f),
                startCenterY =
                    (centerY + MathUtils.sin(angle) * 40f)
                        .coerceIn(12f, worldHeight - 12f),
                lifetimeSeconds = Float.MAX_VALUE
            )
        )

        return true
    }

    fun hasLivingBoneTotemToads(): Boolean =
        entities.any {
            it.sourceWeaponRuntimeId == BONE_TOTEM_SOURCE_ID && !it.isExpired
        }

    fun reduceAllCooldowns(seconds: Float) {
        schedulersByWeaponRuntimeId.values.forEach {
            it.reduceCooldown(seconds)
        }
    }

    private fun spawnImpFireProjectile(
        summon: SummonedEntity,
        targetCenterX: Float,
        targetCenterY: Float,
        projectiles: MutableList<Projectile>
    ) {
        if (
            summon.isExpired
        ) {
            return
        }

        val sourceName =
            summon.sourceWeaponDisplayName +
                " — Summon Demon"

        val burnedApplication =
            ConditionDamageCalculator
                .createApplication(
                    type =
                        ConditionType.BURNED,

                    playerStats =
                        playerStats,

                    durationSeconds =
                        IMP_BURN_DURATION_SECONDS,

                    sourceWeaponRuntimeId =
                        summon.sourceWeaponRuntimeId,

                    sourceWeaponDefinitionId =
                        summon.sourceWeaponDefinitionId,

                    sourceName =
                        sourceName,

                    sourceCenterX =
                        summon.centerX,

                    sourceCenterY =
                        summon.centerY
                )

        /*
         * This is a normal world Projectile:
         * - existing FIRE atlas visual
         * - existing obstacle collision
         * - existing enemy projectile hitbox
         * - real travel time instead of instant summon damage
         *
         * sourceWeapon stays null so player-only Weapon/Tamga on-hit logic
         * cannot accidentally trigger from an Imp shot.
         */
        projectiles.add(
            Projectile(
                startX =
                    summon.centerX,

                startY =
                    summon.centerY,

                targetX =
                    targetCenterX,

                targetY =
                    targetCenterY,

                sourceWeaponName =
                    sourceName,

                sourceWeapon =
                    null,

                sourceWeaponSlotIndex =
                    -1,

                sourceSummonRuntimeId =
                    summon.runtimeId,

                damage =
                    summon.stats.baseDamage,

                isCriticalHit =
                    false,

                maximumTargets =
                    1,

                projectileSpeed =
                    IMP_PROJECTILE_SPEED,

                maximumTravelDistance =
                    IMP_PROJECTILE_MAXIMUM_TRAVEL_DISTANCE,

                damageType =
                    DamageType.FIRE,

                knockback =
                    0f,

                sizeMultiplier =
                    IMP_PROJECTILE_SIZE_MULTIPLIER,

                conditionApplication =
                    burnedApplication
            )
        )
    }

    private fun spawnToadPoisonProjectile(
        summon: SummonedEntity,
        targetCenterX: Float,
        targetCenterY: Float,
        projectiles: MutableList<Projectile>
    ) {
        if (summon.isExpired) return
        val sourceName =
            summon.sourceWeaponDisplayName +
                " — Toad"
        val poison = ConditionDamageCalculator.createApplication(
            type = ConditionType.POISONED,
            playerStats = playerStats,
            durationSeconds = TOAD_POISON_DURATION_SECONDS,
            sourceWeaponRuntimeId = summon.sourceWeaponRuntimeId,
            sourceWeaponDefinitionId = summon.sourceWeaponDefinitionId,
            sourceName = sourceName,
            sourceCenterX = summon.centerX,
            sourceCenterY = summon.centerY
        )
        projectiles.add(
            Projectile(
                startX = summon.centerX,
                startY = summon.centerY,
                targetX = targetCenterX,
                targetY = targetCenterY,
                sourceWeaponName = sourceName,
                sourceWeapon = null,
                sourceWeaponSlotIndex = -1,
                sourceSummonRuntimeId = summon.runtimeId,
                damage = summon.stats.baseDamage,
                isCriticalHit = false,
                maximumTargets = 1,
                projectileSpeed = TOAD_PROJECTILE_SPEED,
                maximumTravelDistance = TOAD_PROJECTILE_MAXIMUM_TRAVEL_DISTANCE,
                damageType = DamageType.POISON,
                knockback = 0f,
                sizeMultiplier = TOAD_PROJECTILE_SIZE_MULTIPLIER,
                conditionApplication = poison
            )
        )
    }

    private fun applySummonAttack(
        attacker: SummonedEntity,
        target: SummonedEntity,
        attackName: String
    ) {

        if (
            attacker.isExpired ||
            target.isExpired ||
            attacker.runtimeId ==
            target.runtimeId ||
            attacker.faction ==
            target.faction
        ) {
            return
        }

        /*
         * Faz 1.4b establishes real summon-vs-summon combat.
         * Status conditions on summoned targets are intentionally deferred
         * until the projectile/condition phase; direct base damage works now.
         */
        target.takeDamage(
            attacker.stats.baseDamage
        )
    }

    private fun applyEnemyAttack(
        summon: SummonedEntity,
        enemy: Enemy,
        attackName: String,
        damageType: DamageType,
        conditionType: ConditionType?,
        onDamageDealt: (
            damage: Int,
            sourceName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
    ) {

        if (
            summon.isExpired ||
            enemy.isDead
        ) {
            return
        }

        val sourceName =
            summon.sourceWeaponDisplayName +
                " — " +
                attackName

        val wasAlive =
            !enemy.isDead

        val appliedDamage =
            enemy.takeDamage(
                amount =
                    summon.stats.baseDamage,

                damageContext =
                    EnemyDamageContext(
                        damageType =
                            damageType,

                        sourceWeaponRuntimeId =
                            summon.sourceWeaponRuntimeId,

                        sourceWeaponDefinitionId =
                            summon.sourceWeaponDefinitionId,

                        sourceName =
                            sourceName,

                        sourceIsSummon =
                            true
                    )
            )

        if (
            appliedDamage >
            0
        ) {
            conditionType
                ?.let {
                        type ->

                    enemy.applyCondition(
                        ConditionDamageCalculator
                            .createApplication(
                                type =
                                    type,

                                playerStats =
                                    playerStats,

                                durationSeconds =
                                    SUMMON_CONDITION_DURATION_SECONDS,

                                sourceWeaponRuntimeId =
                                    summon.sourceWeaponRuntimeId,

                                sourceWeaponDefinitionId =
                                    summon.sourceWeaponDefinitionId,

                                sourceName =
                                    sourceName,

                                sourceCenterX =
                                    summon.centerX,

                                sourceCenterY =
                                    summon.centerY
                            )
                    )
                }

            onDamageDealt(
                appliedDamage,
                sourceName
            )
        }

        if (
            wasAlive &&
            enemy.isDead
        ) {
            onEnemyKilled(
                enemy
            )
        }
    }

    private fun synchronizeSchedulers() {

        val eligibleWeapons =
            weaponInventory
                .allWeapons()
                .filter {
                        weapon ->

                    weapon.definition.id.value ==
                        IVY_STAFF_ID &&
                        weapon.hasWeaponAbility &&
                        weapon.ivySummonChoice !=
                        null
                }

        val eligibleRuntimeIds =
            eligibleWeapons
                .map {
                    it.runtimeId
                }
                .toSet()

        val removedRuntimeIds =
            schedulersByWeaponRuntimeId
                .keys
                .filter {
                    it !in
                        eligibleRuntimeIds
                }

        removedRuntimeIds
            .forEach {
                    runtimeId ->

                schedulersByWeaponRuntimeId.remove(
                    runtimeId
                )

                entities.removeAll {
                    it.sourceWeaponRuntimeId ==
                        runtimeId
                }
            }

        eligibleWeapons
            .forEach {
                    weapon ->

                val choice =
                    weapon.ivySummonChoice
                        ?: return@forEach

                val existing =
                    schedulersByWeaponRuntimeId[
                        weapon.runtimeId
                    ]

                if (
                    existing == null ||
                    existing.choice !=
                    choice
                ) {
                    schedulersByWeaponRuntimeId[
                        weapon.runtimeId
                    ] =
                        SummonSpawnScheduler(
                            sourceWeaponRuntimeId =
                                weapon.runtimeId,

                            choice =
                                choice
                        )
                }
            }
    }

    private fun spawnWave(
        scheduler: SummonSpawnScheduler,
        playerCenterX: Float,
        playerCenterY: Float,
        worldWidth: Float,
        worldHeight: Float
    ): Boolean {

        val weapon =
            weaponInventory
                .allWeapons()
                .firstOrNull {
                    it.runtimeId ==
                        scheduler
                            .sourceWeaponRuntimeId
                }
                ?: return false

        val type =
            summonTypeFor(
                scheduler.choice
            )

        val stats =
            SummonStats.create(
                type =
                    type,

                playerStats =
                    playerStats,

                stage =
                    currentStageProvider(),

                shepherdScalingMultiplier =
                    shepherdScalingMultiplierFor(type, weapon)
            )

        val waveCount =
            waveCountFor(type, weapon)

        repeat(
            waveCount
        ) {
                index ->

            val angle =
                (
                    MathUtils.PI2 /
                        waveCount
                    ) *
                    index +
                    MathUtils.random(
                        -0.28f,
                        0.28f
                    )

            val spawnDistance =
                when (type) {
                    SummonType.KNIGHT_SPIRIT ->
                        KNIGHT_SPAWN_DISTANCE

                    else ->
                        62f +
                            index *
                            16f
                }

            val resolvedSpawn =
                if (
                    type ==
                    SummonType.KNIGHT_SPIRIT
                ) {
                    resolveKnightSpawnPoint(
                        playerCenterX =
                            playerCenterX,

                        playerCenterY =
                            playerCenterY,

                        preferredAngle =
                            angle,

                        worldWidth =
                            worldWidth,

                        worldHeight =
                            worldHeight
                    )
                } else {
                    resolveRegularSummonSpawnPoint(
                        playerCenterX =
                            playerCenterX,

                        playerCenterY =
                            playerCenterY,

                        angle =
                            angle,

                        distance =
                            spawnDistance,

                        worldWidth =
                            worldWidth,

                        worldHeight =
                            worldHeight
                    )
                }

            val startCenterX =
                resolvedSpawn.first

            val startCenterY =
                resolvedSpawn.second

            entities.add(
                SummonedEntity(
                    sourceWeaponRuntimeId =
                        weapon.runtimeId,

                    sourceWeaponDefinitionId =
                        weapon
                            .definition
                            .id
                            .value,

                    sourceWeaponDisplayName =
                        weapon.displayName,

                    type =
                        type,

                    stats =
                        stats,

                    startCenterX =
                        startCenterX,

                    startCenterY =
                        startCenterY,

                    lifetimeSeconds =
                        scheduler
                            .activeLifetimeSeconds
                )
            )
        }

        val waveRegistered =
            scheduler.markWaveSpawned()

        if (!waveRegistered) {
            entities.removeAll {
                it.sourceWeaponRuntimeId ==
                    weapon.runtimeId
            }

            return false
        }

        return true
    }

    private fun resolveKnightSpawnPoint(
        playerCenterX: Float,
        playerCenterY: Float,
        preferredAngle: Float,
        worldWidth: Float,
        worldHeight: Float
    ): Pair<Float, Float> {

        /*
         * Primary rule:
         * Knight should appear approximately 500 px from the player.
         *
         * We first search the exact 500 px ring. If every point is blocked
         * by an obstacle/world edge, only then do we try nearby fallback
         * rings. This avoids spawning the Knight inside rocks/trees/wagons.
         */
        val searchRadii =
            floatArrayOf(
                KNIGHT_SPAWN_DISTANCE,
                470f,
                530f,
                440f,
                560f
            )

        searchRadii.forEach {
                radius ->

            repeat(
                KNIGHT_SPAWN_ANGLE_ATTEMPTS
            ) {
                    attempt ->

                val alternatingStep =
                    if (
                        attempt ==
                        0
                    ) {
                        0
                    } else {
                        (
                            (attempt +
                                1) /
                                2
                            ) *
                            if (
                                attempt %
                                2 ==
                                1
                            ) {
                                1
                            } else {
                                -1
                            }
                    }

                val angle =
                    preferredAngle +
                        alternatingStep *
                        KNIGHT_SPAWN_ANGLE_STEP

                val candidateX =
                    playerCenterX +
                        MathUtils.cos(
                            angle
                        ) *
                        radius

                val candidateY =
                    playerCenterY +
                        MathUtils.sin(
                            angle
                        ) *
                        radius

                if (
                    isValidSummonSpawnPoint(
                        centerX =
                            candidateX,

                        centerY =
                            candidateY,

                        radius =
                            KNIGHT_SPAWN_COLLISION_RADIUS,

                        worldWidth =
                            worldWidth,

                        worldHeight =
                            worldHeight
                    )
                ) {
                    return candidateX to
                        candidateY
                }
            }
        }

        /*
         * Extremely defensive fallback for cramped maps.
         * Keep the old behavior rather than failing activation completely.
         */
        return resolveRegularSummonSpawnPoint(
            playerCenterX =
                playerCenterX,

            playerCenterY =
                playerCenterY,

            angle =
                preferredAngle,

            distance =
                KNIGHT_SPAWN_DISTANCE,

            worldWidth =
                worldWidth,

            worldHeight =
                worldHeight
        )
    }

    private fun resolveRegularSummonSpawnPoint(
        playerCenterX: Float,
        playerCenterY: Float,
        angle: Float,
        distance: Float,
        worldWidth: Float,
        worldHeight: Float
    ): Pair<Float, Float> {

        val rawCenterX =
            playerCenterX +
                MathUtils.cos(
                    angle
                ) *
                distance

        val rawCenterY =
            playerCenterY +
                MathUtils.sin(
                    angle
                ) *
                distance

        return rawCenterX.coerceIn(
            SUMMON_WORLD_MARGIN,
            (
                worldWidth -
                    SUMMON_WORLD_MARGIN
                )
                .coerceAtLeast(
                    SUMMON_WORLD_MARGIN
                )
        ) to
            rawCenterY.coerceIn(
                SUMMON_WORLD_MARGIN,
                (
                    worldHeight -
                        SUMMON_WORLD_MARGIN
                    )
                    .coerceAtLeast(
                        SUMMON_WORLD_MARGIN
                    )
            )
    }

    private fun isValidSummonSpawnPoint(
        centerX: Float,
        centerY: Float,
        radius: Float,
        worldWidth: Float,
        worldHeight: Float
    ): Boolean {

        if (
            centerX -
            radius <
            SUMMON_WORLD_MARGIN ||
            centerX +
            radius >
            worldWidth -
            SUMMON_WORLD_MARGIN ||
            centerY -
            radius <
            SUMMON_WORLD_MARGIN ||
            centerY +
            radius >
            worldHeight -
            SUMMON_WORLD_MARGIN
        ) {
            return false
        }

        val candidate =
            Rectangle(
                centerX -
                    radius -
                    KNIGHT_SPAWN_OBSTACLE_PADDING,

                centerY -
                    radius -
                    KNIGHT_SPAWN_OBSTACLE_PADDING,

                (
                    radius +
                        KNIGHT_SPAWN_OBSTACLE_PADDING
                    ) *
                    2f,

                (
                    radius +
                        KNIGHT_SPAWN_OBSTACLE_PADDING
                    ) *
                    2f
            )

        return mapObstacles.none {
            candidate.overlaps(
                it.bounds
            )
        }
    }

    private fun summonTypeFor(
        choice: IvySummonChoice
    ): SummonType {

        return when (choice) {

            IvySummonChoice.SUMMON_ANIMALS ->
                SummonType.COW

            IvySummonChoice.SUMMON_DEMONS ->
                SummonType.IMP

            IvySummonChoice.CALL_THE_KNIGHT ->
                SummonType.KNIGHT_SPIRIT
        }
    }

    companion object {

        private const val COLLISION_SOLVER_PASSES =
            3

        private const val MAXIMUM_VILLAGE_GUARDS =
            3

        private const val IVY_STAFF_ID =
            "WPN_IVY_STAFF"

        private const val MAX_WEAPON_SLOTS =
            6

        private const val BONE_TOTEM_SOURCE_ID =
            "BONE_TOTEM_TOADS"

        private const val BONE_TOTEM_TOAD_LIFETIME_SECONDS =
            40f

        private const val TOAD_CYCLE_SOURCE_ID =
            "CHARACTER_TRAIT_TOAD_CYCLE"

        private const val TOAD_CYCLE_MAXIMUM_TOADS =
            4

        private const val KNIGHT_SPAWN_DISTANCE =
            500f

        private const val KNIGHT_SPAWN_COLLISION_RADIUS =
            22f

        private const val KNIGHT_SPAWN_OBSTACLE_PADDING =
            18f

        private const val KNIGHT_SPAWN_ANGLE_ATTEMPTS =
            24

        private val KNIGHT_SPAWN_ANGLE_STEP =
            MathUtils.PI2 /
                KNIGHT_SPAWN_ANGLE_ATTEMPTS

        private const val SUMMON_WORLD_MARGIN =
            28f

        private const val SUMMON_CONDITION_DURATION_SECONDS =
            3f

        private const val IMP_PROJECTILE_SPEED =
            420f

        /*
         * Imp attack range is currently ~235px before scaling.
         * Leave enough travel distance for moving targets without allowing
         * a missed fireball to cross the whole map.
         */
        private const val IMP_PROJECTILE_MAXIMUM_TRAVEL_DISTANCE =
            340f

        private const val IMP_PROJECTILE_SIZE_MULTIPLIER =
            1.05f

        private const val IMP_BURN_DURATION_SECONDS =
            3f

        private const val TOAD_PROJECTILE_SPEED =
            360f

        private const val TOAD_PROJECTILE_MAXIMUM_TRAVEL_DISTANCE =
            145f

        private const val TOAD_PROJECTILE_SIZE_MULTIPLIER =
            0.72f

        private const val TOAD_POISON_DURATION_SECONDS =
            3f
    }

    private fun waveCountFor(
        type: SummonType,
        weapon: WeaponInstance
    ): Int =
        type.defaultCount +
            if (
                weapon.hasWeaponAbilityUpgrade &&
                type in setOf(SummonType.COW, SummonType.IMP)
            ) {
                1
            } else {
                0
            }

    private fun shepherdScalingMultiplierFor(
        type: SummonType,
        weapon: WeaponInstance
    ): Float =
        if (weapon.hasWeaponAbilityUpgrade) {
            if (type == SummonType.KNIGHT_SPIRIT) 1.55f else 1.40f
        } else {
            0.70f
        }
}
