package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyManager
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.tamga.runtime.TamgaRuntimeManager
import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.weapon.definition.WeaponAttackPattern
import com.azizoglu.grisilith.weapon.skill.WeaponAbilityRuntime
import com.azizoglu.grisilith.weapon.skill.DefaultWeaponAbilities
import com.azizoglu.grisilith.weapon.skill.WeaponAbilityState

import kotlin.math.roundToInt

class WeaponRuntime(
    val weapon: WeaponInstance,
    private val playerStats: PlayerStats,
    private val executorFactory:
    WeaponAttackExecutorFactory,
    private val tamgaRuntimeManager:
    TamgaRuntimeManager,

    /*
     * Weapon-specific Level 5 damage modifiers that require world/runtime
     * context (currently Impale) are supplied by WeaponRuntimeManager.
     */
    private val abilityDamageMultiplierProvider:
        (
        weapon: WeaponInstance,
        player: Player,
        enemy: Enemy
    ) -> Float = {
            _,
            _,
            _ ->
        1f
    }
) {

    private val attackTimer =
        WeaponAttackTimer()

    private val abilityRuntime =
        WeaponAbilityRuntime(
            weapon
        )

    /*
     * Relentless is tracked per WeaponInstance.
     * First hit is normal damage. Every successful consecutive targeting of
     * the same Enemy increases the NEXT hit by 3.5%, clamped to +50%.
     */
    private var relentlessTarget:
        Enemy? =
        null

    private var relentlessConsecutiveHits =
        0

    val abilityState:
        WeaponAbilityState
        get() =
            abilityRuntime.state

    val abilityRemainingActiveSeconds:
        Float
        get() =
            abilityRuntime
                .remainingActiveSeconds

    val abilityRemainingCooldownSeconds:
        Float
        get() =
            abilityRuntime
                .remainingCooldownSeconds

    fun update(
        delta: Float,
        slotIndex: Int,
        player: Player,
        enemyManager: EnemyManager,
        projectiles: MutableList<Projectile>,
        onDamageDealt: (
            damage: Int,
            weaponName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit,
        onNormalAttackPerformed: (
            weapon: WeaponInstance,
            attacksPerSecond: Float,
            targetCenterX: Float,
            targetCenterY: Float
        ) -> Unit = {
                _,
                _,
                _,
                _ ->
        }
    ) {
        abilityRuntime.update(
            delta
        )

        val definition =
            weapon.definition

        if (
            definition.attackPattern ==
            WeaponAttackPattern.NONE
        ) {
            return
        }

        val executor =
            executorFactory.executorFor(
                definition.attackPattern
            )
                ?: return

        val currentAttacksPerSecond =
            calculateAttacksPerSecond()
                .coerceAtLeast(
                    0.01f
                )

        attackTimer.update(
            delta = delta,

            attacksPerSecond =
                currentAttacksPerSecond
        ) {
            val attacksBeforeExecution =
                weapon.completedNormalAttackCount

            var visualTargetCenterX =
                player.centerX
            var visualTargetCenterY =
                player.centerY
            var hasVisualTarget =
                false

            executor.execute(
                WeaponAttackContext(
                    weapon = weapon,

                    weaponSlotIndex =
                        slotIndex,

                    player = player,

                    enemyManager =
                        enemyManager,

                    projectiles =
                        projectiles,

                    onDamageDealt =
                        onDamageDealt,

                    onEnemyKilled =
                        onEnemyKilled,

                    onWeaponHit = {
                            hitEvent ->

                        player.tryLifeSteal(
                            isEligibleHit =
                                hitEvent
                                    .isLifeStealEligible
                        )

                        tamgaRuntimeManager
                            .onWeaponHit(
                                event =
                                    hitEvent,

                                onDamageDealt =
                                    onDamageDealt,

                                onEnemyKilled =
                                    onEnemyKilled
                            )
                    },

                    modifyWeaponDamage = {
                            enemy,
                            baseDamage ->

                        val tamgaModifiedDamage =
                            tamgaRuntimeManager
                                .modifyWeaponDamage(
                                    enemy = enemy,
                                    baseDamage =
                                        baseDamage
                                )
                                .coerceAtLeast(
                                    1
                                )

                        val abilityMultiplier =
                            abilityDamageMultiplierProvider(
                                weapon,
                                player,
                                enemy
                            )
                                .coerceAtLeast(
                                    0f
                                )

                        val abilityModifiedDamage =
                            (
                                tamgaModifiedDamage *
                                    abilityMultiplier
                                )
                                .roundToInt()
                                .coerceAtLeast(
                                    1
                                )

                        applyRelentlessDamage(
                            enemy =
                                enemy,

                            baseDamage =
                                abilityModifiedDamage
                        )
                    },

                    modifyWeaponRange = {
                            baseRange ->

                        baseRange +
                            tamgaRuntimeManager
                                .weaponRangeBonusPixels()
                    },

                    tryTriggerAutomaticAbility = {
                        abilityRuntime
                            .triggerAutomatic()
                    }
                )
            )

            val attacksAfterExecution =
                weapon.completedNormalAttackCount

            /*
             * Executor hedef bulamadıysa normal saldırı
             * sayacı değişmez. Böylece Quick Shot boş
             * saldırı timer'larından stack kazanmaz.
             */
            if (
                attacksAfterExecution >
                attacksBeforeExecution
            ) {
                onNormalAttackPerformed(
                    weapon,
                    currentAttacksPerSecond,
                    visualTargetCenterX,
                    visualTargetCenterY
                )

                tamgaRuntimeManager
                    .onWeaponAttack(
                        slotIndex = slotIndex,
                        weapon = weapon
                    )
            }
        }
    }

    private fun applyRelentlessDamage(
        enemy: Enemy,
        baseDamage: Int
    ): Int {
        if (
            weapon.unlockedAbility
                ?.id !=
            DefaultWeaponAbilities
                .RELENTLESS_ID
        ) {
            relentlessTarget =
                null

            relentlessConsecutiveHits =
                0

            return baseDamage
        }

        if (
            relentlessTarget !==
            enemy
        ) {
            relentlessTarget =
                enemy

            relentlessConsecutiveHits =
                0
        }

        val damageBonus =
            (
                relentlessConsecutiveHits *
                    DefaultWeaponAbilities
                        .RELENTLESS_DAMAGE_PER_CONSECUTIVE_HIT
                )
                .coerceAtMost(
                    DefaultWeaponAbilities
                        .RELENTLESS_MAXIMUM_DAMAGE_BONUS
                )

        val modifiedDamage =
            (
                baseDamage *
                    (
                        1f +
                            damageBonus
                        )
                )
                .roundToInt()
                .coerceAtLeast(
                    1
                )

        /*
         * The hit we just calculated becomes one completed step in the
         * chain. 15 hits would mathematically reach 52.5%, so the actual
         * damage bonus is hard-clamped to +50%.
         */
        relentlessConsecutiveHits++

        return modifiedDamage
    }

    private fun resetRelentless() {
        relentlessTarget =
            null

        relentlessConsecutiveHits =
            0
    }

    fun currentAttacksPerSecond():
        Float {
        return calculateAttacksPerSecond()
    }

    private fun calculateAttacksPerSecond():
        Float {

        val definition =
            weapon.definition

        val levelStats =
            weapon.currentLevelStats

        val statBasedAttacksPerSecond =
            when (
                definition.normalAttackSpeedStat
            ) {
                StatType.ATTACK_SPEED -> {
                    val progress =
                        playerStats
                            .attackSpeedProgress()

                    definition
                        .baseAttacksPerSecond +
                        (
                            definition
                                .maximumAttacksPerSecond -
                                definition
                                    .baseAttacksPerSecond
                            ) *
                        progress
                }

                StatType.HASTE -> {
                    val progress =
                        playerStats
                            .hasteProgress()

                    definition
                        .baseAttacksPerSecond +
                        (
                            definition
                                .maximumAttacksPerSecond -
                                definition
                                    .baseAttacksPerSecond
                            ) *
                        progress
                }

                else -> {
                    definition
                        .baseAttacksPerSecond
                }
            }

        val externalAttackSpeedMultiplier =
            if (
                definition.normalAttackSpeedStat ==
                StatType.ATTACK_SPEED
            ) {
                tamgaRuntimeManager
                    .attackSpeedMultiplier()
            } else {
                1f
            }

        return (
            statBasedAttacksPerSecond *
                levelStats.attackSpeedMultiplier *
                externalAttackSpeedMultiplier
            )
            .coerceAtLeast(
                0.01f
            )
    }

    fun tryActivateAbility():
        Boolean {

        return abilityRuntime
            .tryActivate()
    }

    fun triggerAutomaticAbility():
        Boolean {

        return abilityRuntime
            .triggerAutomatic()
    }

    fun endActiveAbilityEarly() {
        abilityRuntime
            .endActiveEarly()
    }

    fun resetAttackTimer() {
        attackTimer.reset()
        resetRelentless()
    }
    fun resetTimer() {
        attackTimer.reset()

        abilityRuntime.reset()
        resetRelentless()
    }
}
