package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyManager
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.weapon.definition.WeaponTargetingType
import com.badlogic.gdx.math.Vector2

class WeaponTargetSelector {

    fun selectTarget(
        targetingType: WeaponTargetingType,
        player: Player,
        enemyManager: EnemyManager,
        maximumRange: Float,
        originX: Float = player.centerX,
        originY: Float = player.centerY
    ): Enemy? {
        return when (targetingType) {
            WeaponTargetingType.NEAREST_ENEMY -> {
                selectNearestEnemy(
                    originX = originX,
                    originY = originY,
                    enemyManager = enemyManager,
                    maximumRange = maximumRange
                )
            }
        }
    }

    private fun selectNearestEnemy(
        originX: Float,
        originY: Float,
        enemyManager: EnemyManager,
        maximumRange: Float
    ): Enemy? {
        if (maximumRange <= 0f) {
            return null
        }

        val target =
            enemyManager.nearestEnemy(
                x = originX,
                y = originY
            ) ?: return null

        val distance =
            Vector2.dst(
                originX,
                originY,
                target.centerX,
                target.centerY
            )

        return if (distance <= maximumRange) {
            target
        } else {
            null
        }
    }
}
