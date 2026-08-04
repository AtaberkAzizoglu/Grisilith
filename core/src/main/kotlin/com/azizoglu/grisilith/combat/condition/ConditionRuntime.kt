package com.azizoglu.grisilith.combat.condition

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyConditionDamageEvent
import com.azizoglu.grisilith.enemy.EnemyDamageContext

class ConditionRuntime(
    private val owner: Enemy
) {
    val conditions =
        ConditionContainer()

    private val pendingDamageEvents =
        mutableListOf<EnemyConditionDamageEvent>()

    fun update(
        delta: Float
    ) {
        if (
            delta <= 0f ||
            owner.isDead
        ) {
            return
        }

        conditions.update(
            delta = delta
        ) { tick ->
            val wasAlive =
                !owner.isDead

            val damageContext =
                EnemyDamageContext(
                    damageType =
                        tick.source.damageType,

                    sourceWeaponRuntimeId =
                        tick.source.sourceWeaponRuntimeId,

                    sourceWeaponDefinitionId =
                        tick.source.sourceWeaponDefinitionId,

                    sourceName =
                        tick.source.sourceName
                )

            val appliedDamage =
                owner.takeDamage(
                    amount =
                        tick.source.tickDamage,

                    damageContext =
                        damageContext
                )

            if (
                appliedDamage <= 0
            ) {
                return@update
            }

            pendingDamageEvents.add(
                EnemyConditionDamageEvent(
                    enemy =
                        owner,

                    conditionType =
                        tick.type,

                    appliedDamage =
                        appliedDamage,

                    damageContext =
                        damageContext,

                    killedEnemy =
                        wasAlive &&
                            owner.isDead
                )
            )
        }

        if (
            owner.isDead
        ) {
            conditions.clear()
        }
    }

    fun consumeDamageEvents():
        List<EnemyConditionDamageEvent> {

        if (
            pendingDamageEvents.isEmpty()
        ) {
            return emptyList()
        }

        val result =
            pendingDamageEvents.toList()

        pendingDamageEvents.clear()

        return result
    }

    fun clear() {
        conditions.clear()
        pendingDamageEvents.clear()
    }
}
