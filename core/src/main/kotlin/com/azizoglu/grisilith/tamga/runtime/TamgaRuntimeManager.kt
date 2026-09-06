package com.azizoglu.grisilith.tamga.runtime

import com.azizoglu.grisilith.content.ConditionType
import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.tamga.definition.TamgaDefinition
import com.azizoglu.grisilith.tamga.definition.TamgaEffectId
import com.azizoglu.grisilith.tamga.render.TamgaWorldVisualRenderer
import com.azizoglu.grisilith.tamga.runtime.active.ActiveTamgaState
import com.azizoglu.grisilith.tamga.runtime.active.ChargerEndEvent
import com.azizoglu.grisilith.tamga.runtime.active.ChargerExplosionRuntime
import com.azizoglu.grisilith.tamga.runtime.active.ChargerRuntime
import com.azizoglu.grisilith.tamga.runtime.active.PhantomSteedRuntime
import com.azizoglu.grisilith.weapon.runtime.WeaponHitEvent
import com.azizoglu.grisilith.weapon.runtime.WeaponInstance
import com.azizoglu.grisilith.weapon.runtime.WeaponInventory
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.OrthographicCamera
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt

class TamgaRuntimeManager(
    private val weaponInventory:
    WeaponInventory,

    private val playerStats:
    PlayerStats,

    private val weaponTamga:
    TamgaDefinition?,

    private val armorTamga:
    TamgaDefinition?,

    private val characterPreferredStats:
    Set<StatType>,

    private val currentStageProvider:
        () -> Int
) {

    private val holyLightsRuntime =
        HolyLightsRuntime(
            playerStats =
                playerStats,

            preferredStats =
                characterPreferredStats,

            currentStageProvider =
                currentStageProvider
        )

    private val arcaneAuraRuntime =
        ArcaneAuraRuntime(
            playerStats =
                playerStats,

            weaponInventory =
                weaponInventory,

            currentStageProvider =
                currentStageProvider
        )

    private val chargerRuntime =
        ChargerRuntime()

    private val phantomSteedRuntime =
        PhantomSteedRuntime(
            playerStats = playerStats,
            currentStageProvider = currentStageProvider
        )

    private val chargerExplosionRuntime =
        ChargerExplosionRuntime()

    private val worldVisualRenderer =
        TamgaWorldVisualRenderer()

    private val chargerFootsteps =
        mutableListOf<ChargerFootstepVisual>()

    private val panicPulses =
        mutableListOf<PanicPulseVisual>()

    private var attachedPlayer: Player? = null
    private var visualAnimationSeconds = 0f
    private var chargerTrailWasActive = false
    private var chargerTrailAnchorX = 0f
    private var chargerTrailAnchorY = 0f
    private var panicCooldownSeconds = 0f
    companion object {

        private const val FIRST_WEAPON_SLOT_INDEX =
            0

        /*
         * Quick Shot
         */
        private const val QUICK_SHOT_BASE_STACK_LIMIT =
            3

        private const val QUICK_SHOT_STAGE_INTERVAL =
            10

        private const val QUICK_SHOT_RESET_SECONDS =
            2.5f

        private const val ATTACK_SPEED_PER_STACK_MULTIPLIER =
            0.05f

        /*
         * Flame Blades
         */
        private const val FLAME_BLADES_BASE_DAMAGE =
            5f

        private const val FLAME_BLADES_ELEMENTAL_SCALING =
            1.1f

        private const val HEIGHT_INSECURITY_BASE_BONUS =
            0.05f

        private const val HEIGHT_INSECURITY_STAGE_BONUS =
            0.05f

        private const val HEIGHT_INSECURITY_STAGE_INTERVAL =
            10

        private const val FLAME_BLADES_BURN_DURATION_SECONDS =
            5f

        private const val CHARGER_FOOTSTEP_DISTANCE = 30f
        private const val CHARGER_FOOTSTEP_BACK_OFFSET = 17f

        private const val PANIC_TRIGGER_RADIUS = 160f
        private const val PANIC_PULSE_RADIUS = 220f
        private const val PANIC_ENEMY_THRESHOLD = 8
        private const val PANIC_COOLDOWN_SECONDS = 22f
        private const val PANIC_KNOCKBACK_STRENGTH = 760f
    }

    /*
     * Quick Shot runtime değerleri
     */
    private var quickShotStacks =
        0

    private var quickShotIdleTime =
        0f

    private var trackedFirstSlotWeaponRuntimeId:
        String? = null

    val currentQuickShotStacks: Int
        get() =
            quickShotStacks

    val currentQuickShotMaximumStacks: Int
        get() =
            quickShotMaximumStacks()

    val quickShotTimeUntilReset: Float
        get() =
            (
                QUICK_SHOT_RESET_SECONDS -
                    quickShotIdleTime
                )
                .coerceAtLeast(
                    0f
                )

    fun update(
        delta: Float,
        player: Player,
        enemies: List<Enemy>
    ) {
        if (delta <= 0f) {
            return
        }

        visualAnimationSeconds += delta
        synchronizeFirstWeaponSlot()

        if (!isQuickShotActive()) {
            resetQuickShotStacks()
        } else if (quickShotStacks <= 0) {
            quickShotIdleTime =
                0f
        } else {
            quickShotIdleTime +=
                delta

            if (
                quickShotIdleTime >=
                QUICK_SHOT_RESET_SECONDS
            ) {
                resetQuickShotStacks()
            }
        }

        if (isPhantomSteedEquipped()) {
            phantomSteedRuntime.update(delta)
        } else if (
            phantomSteedRuntime.state !=
            ActiveTamgaState.READY
        ) {
            phantomSteedRuntime.reset()
        }

        chargerExplosionRuntime
            .update(
                delta
            )

        updateChargerFootsteps(delta)
        updatePanic(
            delta = delta,
            player = player,
            enemies = enemies
        )
    }

    fun triggerChargerExplosion(
        centerX:
        Float,

        centerY:
        Float,

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
        if (!isChargerEquipped()) {
            return
        }

        val elementalDamage =
            playerStats.totalScore(
                StatType.ELEMENTAL_DAMAGE
            )

        chargerExplosionRuntime
            .createExplosion(
                centerX =
                    centerX,

                centerY =
                    centerY,

                elementalDamage =
                    elementalDamage,

                currentStage =
                    currentStageProvider(),

                chargedSeconds =
                    chargedSeconds,

                completedSuccessfully =
                    completedSuccessfully,

                enemies =
                    enemies,

                onDamageDealt =
                    onDamageDealt,

                onEnemyKilled =
                    onEnemyKilled
            )
    }

    fun updateHolyLights(
        delta: Float,
        player: Player,
        enemies: List<Enemy>,
        onDamageDealt: (
            damage: Int,
            sourceName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit,
        onLifeStealDamage: (Int) -> Unit
    ) {
        if (!isHolyLightsActive()) {
            holyLightsRuntime
                .deactivate()

            return
        }

        holyLightsRuntime.update(
            delta = delta,
            player = player,
            enemies = enemies,
            onDamageDealt =
                onDamageDealt,
            onEnemyKilled =
                onEnemyKilled,
            onLifeStealDamage =
                onLifeStealDamage
        )
    }

    fun updateArcaneAura(
        delta: Float,
        player: Player,
        enemies: List<Enemy>,
        onDamageDealt: (
            damage: Int,
            sourceName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
    ) {
        /*
         * Arcane Aura takılı değilse runtime'ı
         * sıfırlarız ve işlem yapmayız.
         */
        if (!isArcaneAuraActive()) {
            arcaneAuraRuntime
                .deactivate()

            return
        }

        /*
         * Tamga aktifse gerçek aura runtime'ına
         * gerekli oyun verilerini göndeririz.
         */
        arcaneAuraRuntime.update(
            delta = delta,

            player = player,

            enemies = enemies,

            onDamageDealt =
                onDamageDealt,

            onEnemyKilled =
                onEnemyKilled
        )
    }

    fun renderHolyLights(
        shapeRenderer: ShapeRenderer,
        player: Player
    ) {
        if (!isHolyLightsActive()) {
            return
        }

        holyLightsRuntime.render(
            shapeRenderer =
                shapeRenderer,
            player =
                player
        )
    }

    fun renderArcaneAura(
        shapeRenderer: ShapeRenderer,
        player: Player
    ) {
        if (!isArcaneAuraActive()) {
            return
        }

        arcaneAuraRuntime.render(
            shapeRenderer =
                shapeRenderer,

            player =
                player
        )
    }

    fun renderWorldVisuals(
        camera: OrthographicCamera,
        player: Player
    ) {
        worldVisualRenderer.render(
            camera = camera,
            player = player,
            animationSeconds = visualAnimationSeconds,
            holyLightsMode =
                holyLightsRuntime.currentMode
                    .takeIf { isHolyLightsActive() },
            holyLightsRange = holyLightsRuntime.auraRange,
            arcaneAuraMode =
                arcaneAuraRuntime.currentMode
                    .takeIf { isArcaneAuraActive() },
            arcaneAuraRange = arcaneAuraRuntime.auraRange,
            footsteps = chargerFootsteps,
            chargerExplosions = chargerExplosionRuntime.effects,
            panicPulses = panicPulses
        )
    }

    private fun isHolyLightsActive():
        Boolean {

        return hasTamgaEffect(
            TamgaEffectId.HOLY_LIGHTS
        )
    }

    private fun isArcaneAuraActive():
        Boolean {

        return hasTamgaEffect(
            TamgaEffectId.ARCANE_AURA
        )
    }

    fun attachPlayer(
        player:
        Player
    ) {
        attachedPlayer = player
        chargerTrailAnchorX = player.centerX
        chargerTrailAnchorY = player.centerY

        chargerRuntime
            .attachPlayer(
                player
            )

        phantomSteedRuntime
            .attachPlayer(
                player
            )
    }

    fun tryActivateWeaponTamga():
        Boolean {

        if (!isChargerEquipped()) {
            return false
        }

        return chargerRuntime
            .tryActivate()
    }

    fun tryActivateArmorTamga():
        Boolean {

        if (!isPhantomSteedEquipped()) {
            return false
        }

        return phantomSteedRuntime
            .tryActivate()
    }

    fun reduceAllActiveCooldowns(seconds: Float) {
        chargerRuntime.reduceCooldown(seconds)
        phantomSteedRuntime.reduceCooldown(seconds)
    }

    fun updateChargerAfterMovement(
        delta:
        Float
    ) {
        if (!isChargerEquipped()) {
            if (
                chargerRuntime.state !=
                ActiveTamgaState.READY
            ) {
                chargerRuntime.reset()
            }

            return
        }

        chargerRuntime
            .updateAfterMovement(
                delta =
                    delta
            )
    }

    fun consumeChargerEndEvent():
        ChargerEndEvent? {

        return chargerRuntime
            .consumeEndEvent()
    }

    val hasActiveWeaponTamga:
        Boolean
        get() =
            isChargerEquipped()

    val activeWeaponTamgaState:
        ActiveTamgaState?
        get() =
            if (isChargerEquipped()) {
                chargerRuntime.state
            } else {
                null
            }

    val activeWeaponTamgaRemainingActiveSeconds:
        Float
        get() =
            if (isChargerEquipped()) {
                chargerRuntime.remainingActiveSeconds
            } else {
                0f
            }

    val activeWeaponTamgaRemainingCooldownSeconds:
        Float
        get() =
            if (isChargerEquipped()) {
                chargerRuntime.remainingCooldownSeconds
            } else {
                0f
            }

    val hasActiveArmorTamga:
        Boolean
        get() =
            isPhantomSteedEquipped()

    /*
     * WorldRenderer uses this only to decide which CHARACTER sprite set to
     * render. No separate Phantom Steed world entity/overlay is created.
     */
    val isPhantomSteedVisualActive:
        Boolean
        get() =
            isPhantomSteedEquipped() &&
                phantomSteedRuntime.isActive

    val activeArmorTamgaState:
        ActiveTamgaState?
        get() =
            if (isPhantomSteedEquipped()) {
                phantomSteedRuntime.state
            } else {
                null
            }

    val activeArmorTamgaRemainingActiveSeconds:
        Float
        get() =
            if (isPhantomSteedEquipped()) {
                phantomSteedRuntime.remainingActiveSeconds
            } else {
                0f
            }

    val activeArmorTamgaRemainingCooldownSeconds:
        Float
        get() =
            if (isPhantomSteedEquipped()) {
                phantomSteedRuntime.remainingCooldownSeconds
            } else {
                0f
            }

    fun weaponRangeBonusPixels(): Float {
        return if (isPhantomSteedEquipped()) {
            phantomSteedRuntime.weaponRangeBonusPixels
        } else {
            0f
        }
    }

    /*
     * Legacy compatibility hook.
     *
     * Phantom Steed is no longer drawn as an independent overlay.
     * The normal character sprite is replaced by a mounted sprite in
     * WorldRenderer while isPhantomSteedVisualActive == true.
     */
    fun renderPhantomSteed(
        shapeRenderer: ShapeRenderer,
        player: Player
    ) {
        // Intentionally empty.
    }

    private fun isPhantomSteedEquipped(): Boolean {
        return armorTamga
            ?.effectId ==
            TamgaEffectId.PHANTOM_STEED
    }

    private fun isChargerEquipped():
        Boolean {

        return weaponTamga
            ?.effectId ==
            TamgaEffectId.CHARGER
    }

    private fun updateChargerFootsteps(delta: Float) {
        chargerFootsteps.forEach { it.update(delta) }
        chargerFootsteps.removeAll { it.isFinished }

        val player = attachedPlayer ?: return
        if (!isChargerEquipped() || !chargerRuntime.isActive) {
            chargerTrailWasActive = false
            return
        }

        if (!chargerTrailWasActive) {
            chargerTrailWasActive = true
            chargerTrailAnchorX = player.centerX
            chargerTrailAnchorY = player.centerY
            return
        }

        val dx = player.centerX - chargerTrailAnchorX
        val dy = player.centerY - chargerTrailAnchorY
        val distance = sqrt(dx * dx + dy * dy)
        if (distance < CHARGER_FOOTSTEP_DISTANCE) return

        val normalX = dx / distance
        val normalY = dy / distance
        chargerFootsteps.add(
            ChargerFootstepVisual(
                x = player.centerX - normalX * CHARGER_FOOTSTEP_BACK_OFFSET,
                y = player.centerY - normalY * CHARGER_FOOTSTEP_BACK_OFFSET,
                direction = footstepDirection(dx, dy)
            )
        )
        chargerTrailAnchorX = player.centerX
        chargerTrailAnchorY = player.centerY
    }

    private fun footstepDirection(dx: Float, dy: Float): FootstepDirection {
        val degrees = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
        return when {
            degrees >= -22.5 && degrees < 22.5 -> FootstepDirection.EAST
            degrees >= 22.5 && degrees < 67.5 -> FootstepDirection.NORTH_EAST
            degrees >= 67.5 && degrees < 112.5 -> FootstepDirection.NORTH
            degrees >= 112.5 && degrees < 157.5 -> FootstepDirection.NORTH_WEST
            degrees >= 157.5 || degrees < -157.5 -> FootstepDirection.WEST
            degrees >= -157.5 && degrees < -112.5 -> FootstepDirection.SOUTH_WEST
            degrees >= -112.5 && degrees < -67.5 -> FootstepDirection.SOUTH
            else -> FootstepDirection.SOUTH_EAST
        }
    }

    private fun updatePanic(
        delta: Float,
        player: Player,
        enemies: List<Enemy>
    ) {
        panicPulses.forEach { it.update(delta) }
        panicPulses.removeAll { it.isFinished }

        if (!hasTamgaEffect(TamgaEffectId.PANIC)) {
            panicCooldownSeconds = 0f
            panicPulses.clear()
            return
        }

        panicCooldownSeconds =
            (panicCooldownSeconds - delta)
                .coerceAtLeast(0f)
        if (panicCooldownSeconds > 0f || player.health.isDead) return

        val triggerRadiusSquared = PANIC_TRIGGER_RADIUS * PANIC_TRIGGER_RADIUS
        val nearbyEnemies = enemies.filter { enemy ->
            !enemy.isDead &&
                squaredDistance(
                    player.centerX,
                    player.centerY,
                    enemy.centerX,
                    enemy.centerY
                ) <= triggerRadiusSquared
        }
        if (nearbyEnemies.size < PANIC_ENEMY_THRESHOLD) return

        panicPulses.add(
            PanicPulseVisual(
                x = player.centerX,
                y = player.centerY
            )
        )
        val pulseRadiusSquared = PANIC_PULSE_RADIUS * PANIC_PULSE_RADIUS
        enemies.forEach { enemy ->
            if (
                !enemy.isDead &&
                squaredDistance(
                    player.centerX,
                    player.centerY,
                    enemy.centerX,
                    enemy.centerY
                ) <= pulseRadiusSquared
            ) {
                enemy.applyKnockback(
                    directionX = enemy.centerX - player.centerX,
                    directionY = enemy.centerY - player.centerY,
                    strength = PANIC_KNOCKBACK_STRENGTH
                )
            }
        }
        panicCooldownSeconds = PANIC_COOLDOWN_SECONDS
    }

    private fun squaredDistance(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float
    ): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return dx * dx + dy * dy
    }

    fun modifyWeaponDamage(
        enemy: Enemy,
        baseDamage: Int
    ): Int {
        if (
            baseDamage <= 0 ||
            enemy.isDead
        ) {
            return baseDamage
                .coerceAtLeast(0)
        }

        var modifiedDamage =
            baseDamage

        modifiedDamage =
            applyHeightInsecurityModifier(
                enemy = enemy,
                currentDamage =
                    modifiedDamage
            )

        /*
         * İleride diğer damage modifier Tamgaları
         * bu zincire sırayla eklenecek.
         */
        return modifiedDamage
            .coerceAtLeast(1)
    }

    private fun applyHeightInsecurityModifier(
        enemy: Enemy,
        currentDamage: Int
    ): Int {
        if (!isHeightInsecurityActive()) {
            return currentDamage
        }

        if (!isHeightInsecurityTarget(enemy)) {
            return currentDamage
        }

        val damageMultiplier =
            1f +
                heightInsecurityBonusRatio()

        return (
            currentDamage *
                damageMultiplier
            )
            .roundToInt()
            .coerceAtLeast(1)
    }

    private fun heightInsecurityBonusRatio():
        Float {

        val currentStage =
            currentStageProvider()
                .coerceAtLeast(1)

        /*
         * Stage 1–9   = %5
         * Stage 10–19 = %10
         * Stage 20–29 = %15
         */
        val completedIntervals =
            currentStage /
                HEIGHT_INSECURITY_STAGE_INTERVAL

        return HEIGHT_INSECURITY_BASE_BONUS +
            completedIntervals *
            HEIGHT_INSECURITY_STAGE_BONUS
    }

    private fun isHeightInsecurityTarget(
        enemy: Enemy
    ): Boolean {

        return when (enemy.type) {
            EnemyType.UNIQUE,
            EnemyType.MINI_BOSS,
            EnemyType.FINAL_BOSS -> {
                true
            }

            EnemyType.NORMAL -> {
                false
            }
        }
    }

    private fun isHeightInsecurityActive():
        Boolean {

        return hasTamgaEffect(
            TamgaEffectId.HEIGHT_INSECURITY
        )
    }

    private fun hasTamgaEffect(
        effectId: TamgaEffectId
    ): Boolean {

        return weaponTamga
            ?.effectId ==
            effectId ||
            armorTamga
                ?.effectId ==
            effectId
    }


    /*
     * Quick Shot:
     * Gerçek bir Weapon saldırısı gerçekleştiğinde
     * WeaponRuntime tarafından çağrılır.
     */
    fun onWeaponAttack(
        slotIndex: Int,
        weapon: WeaponInstance
    ) {
        if (
            slotIndex !=
            FIRST_WEAPON_SLOT_INDEX
        ) {
            return
        }

        if (!isQuickShotActive()) {
            return
        }

        val equippedFirstWeapon =
            weaponInventory.weaponAt(
                FIRST_WEAPON_SLOT_INDEX
            )
                ?: return

        if (equippedFirstWeapon !== weapon) {
            return
        }

        if (!isRangedWeapon(weapon)) {
            resetQuickShotStacks()
            return
        }

        trackedFirstSlotWeaponRuntimeId =
            weapon.runtimeId

        quickShotIdleTime =
            0f

        quickShotStacks =
            (
                quickShotStacks + 1
                )
                .coerceAtMost(
                    quickShotMaximumStacks()
                )
    }

    /*
     * Flame Blades:
     * Bir Weapon gerçekten Enemy'ye hasar verdiğinde
     * çağrılır.
     */
    fun onWeaponHit(
        event: WeaponHitEvent,
        onDamageDealt: (
            damage: Int,
            weaponName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
    ) {
        if (
            event.appliedDamage <= 0 ||
            event.enemy.isDead
        ) {
            return
        }

        if (!isFlameBladesActive()) {
            return
        }

        val firstSlotWeapon =
            weaponInventory.weaponAt(
                FIRST_WEAPON_SLOT_INDEX
            )
                ?: return

        /*
         * Flame Blades yalnızca birinci slottaki
         * Weapon melee ise çalışır.
         */
        if (!isMeleeWeapon(firstSlotWeapon)) {
            return
        }

        val isFirstSlotAttack =
            event.weaponSlotIndex ==
                FIRST_WEAPON_SLOT_INDEX &&
                event.weapon ===
                firstSlotWeapon

        /*
         * Birinci slottaki melee Weapon hedefi
         * Burned yapar.
         */
        if (isFirstSlotAttack) {
            event.enemy.applyCondition(
                conditionType =
                    ConditionType.BURNED,

                durationSeconds =
                    FLAME_BLADES_BURN_DURATION_SECONDS
            )
        }

        /*
         * Hedef Burned değilse ek hasar yok.
         */
        if (
            !event.enemy.hasCondition(
                ConditionType.BURNED
            )
        ) {
            return
        }

        applyFlameBladesBonusDamage(
            enemy =
                event.enemy,

            sourceWeaponName =
                event.weapon.displayName,

            onDamageDealt =
                onDamageDealt,

            onEnemyKilled =
                onEnemyKilled
        )
    }

    /*
     * Quick Shot'un Attack Speed çarpanı.
     */
    fun attackSpeedMultiplier():
        Float {

        if (!isQuickShotActive()) {
            return 1f
        }

        return 1f +
            quickShotStacks *
            ATTACK_SPEED_PER_STACK_MULTIPLIER
    }

    fun reset() {
        trackedFirstSlotWeaponRuntimeId =
            null

        resetQuickShotStacks()
        holyLightsRuntime.reset()
        arcaneAuraRuntime.reset()
        chargerRuntime.reset()
        phantomSteedRuntime.reset()
        chargerExplosionRuntime.reset()
        chargerFootsteps.clear()
        panicPulses.clear()
        visualAnimationSeconds = 0f
        chargerTrailWasActive = false
        panicCooldownSeconds = 0f
    }

    fun dispose() {
        worldVisualRenderer.dispose()
    }

    private fun applyFlameBladesBonusDamage(
        enemy: Enemy,
        sourceWeaponName: String,
        onDamageDealt: (
            damage: Int,
            weaponName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
    ) {
        if (enemy.isDead) {
            return
        }

        val elementalDamage =
            playerStats.totalScore(
                StatType.ELEMENTAL_DAMAGE
            )

        val requestedBonusDamage =
            (
                FLAME_BLADES_BASE_DAMAGE +
                    elementalDamage *
                    FLAME_BLADES_ELEMENTAL_SCALING
                )
                .toInt()
                .coerceAtLeast(
                    1
                )

        val wasAliveBeforeDamage =
            !enemy.isDead

        /*
         * Burada yeniden WeaponHitEvent üretmiyoruz.
         * Böylece Flame Blades kendi kendini sonsuz
         * şekilde tetiklemez.
         */
        val appliedBonusDamage =
            enemy.takeDamage(
                requestedBonusDamage
            )

        if (appliedBonusDamage > 0) {
            onDamageDealt(
                appliedBonusDamage,
                sourceWeaponName
            )
        }

        if (
            wasAliveBeforeDamage &&
            enemy.isDead
        ) {
            onEnemyKilled(
                enemy
            )
        }
    }

    private fun synchronizeFirstWeaponSlot() {
        val firstWeapon =
            weaponInventory.weaponAt(
                FIRST_WEAPON_SLOT_INDEX
            )

        if (firstWeapon == null) {
            trackedFirstSlotWeaponRuntimeId =
                null

            resetQuickShotStacks()
            return
        }

        val previousRuntimeId =
            trackedFirstSlotWeaponRuntimeId

        if (previousRuntimeId == null) {
            trackedFirstSlotWeaponRuntimeId =
                firstWeapon.runtimeId

            if (!isRangedWeapon(firstWeapon)) {
                resetQuickShotStacks()
            }

            return
        }

        /*
         * Market, merge veya swap nedeniyle
         * ilk slot Weapon değiştiyse stack sıfırlanır.
         */
        if (
            previousRuntimeId !=
            firstWeapon.runtimeId
        ) {
            trackedFirstSlotWeaponRuntimeId =
                firstWeapon.runtimeId

            resetQuickShotStacks()
        }

        if (!isRangedWeapon(firstWeapon)) {
            resetQuickShotStacks()
        }
    }

    private fun quickShotMaximumStacks():
        Int {

        val currentStage =
            currentStageProvider()
                .coerceAtLeast(
                    1
                )

        val stageBonus =
            currentStage /
                QUICK_SHOT_STAGE_INTERVAL

        return QUICK_SHOT_BASE_STACK_LIMIT +
            stageBonus
    }

    private fun isQuickShotActive():
        Boolean {

        return hasTamgaEffect(
            TamgaEffectId.QUICK_SHOT
        )
    }

    private fun isFlameBladesActive():
        Boolean {

        return hasTamgaEffect(
            TamgaEffectId.FLAME_BLADES
        )
    }

    private fun isRangedWeapon(
        weapon: WeaponInstance
    ): Boolean {

        return ContentTag.RANGED_WEAPON in
            weapon.definition.tags
    }

    private fun isMeleeWeapon(
        weapon: WeaponInstance
    ): Boolean {

        return ContentTag.MELEE_WEAPON in
            weapon.definition.tags
    }

    private fun resetQuickShotStacks() {
        quickShotStacks =
            0

        quickShotIdleTime =
            0f
    }
}
