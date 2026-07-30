package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyManager
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.tamga.runtime.TamgaRuntimeManager
import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.weapon.definition.WeaponAttackPattern

class WeaponRuntime(
    val weapon: WeaponInstance,
    private val playerStats: PlayerStats,
    private val executorFactory:
    WeaponAttackExecutorFactory,
    private val tamgaRuntimeManager:
    TamgaRuntimeManager
) {

    private val attackTimer =
        WeaponAttackTimer()

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
        onEnemyKilled: (Enemy) -> Unit
    ) {
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

        attackTimer.update(
            delta = delta,

            attacksPerSecond =
                calculateAttacksPerSecond()
        ) {
            val attacksBeforeExecution =
                weapon.completedNormalAttackCount

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

                        tamgaRuntimeManager
                            .modifyWeaponDamage(
                                enemy = enemy,
                                baseDamage =
                                    baseDamage
                            )
                    },

                    modifyWeaponRange = {
                            baseRange ->

                        baseRange +
                            tamgaRuntimeManager
                                .weaponRangeBonusPixels()
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
                tamgaRuntimeManager
                    .onWeaponAttack(
                        slotIndex = slotIndex,
                        weapon = weapon
                    )
            }
        }
    }

    private fun calculateAttacksPerSecond():
        Float {

        val definition =
            weapon.definition

        val levelStats =
            weapon.currentLevelStats

        val characterAttackSpeedMultiplier =
            when (
                definition.normalAttackSpeedStat
            ) {
                StatType.HASTE -> {
                    playerStats
                        .hasteAttackSpeedMultiplier()
                }

                StatType.ATTACK_SPEED -> {
                    playerStats
                        .attackSpeedMultiplier() *
                        tamgaRuntimeManager
                            .attackSpeedMultiplier()
                }

                else -> {
                    1f
                }
            }

        return (
            definition.baseAttacksPerSecond *
                levelStats.attackSpeedMultiplier *
                characterAttackSpeedMultiplier
            )
            .coerceAtLeast(
                0.01f
            )
    }

    fun resetTimer() {
        attackTimer.reset()
    }
}
