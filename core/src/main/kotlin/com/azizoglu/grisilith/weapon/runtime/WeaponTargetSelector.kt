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
        maximumRange: Float
    ): Enemy? {
        return when (targetingType) {
            WeaponTargetingType.NEAREST_ENEMY -> {
                selectNearestEnemy(
                    player = player,
                    enemyManager = enemyManager,
                    maximumRange = maximumRange
                )
            }
        }
    }

    private fun selectNearestEnemy(
        player: Player,
        enemyManager: EnemyManager,
        maximumRange: Float
    ): Enemy? {
        if (maximumRange <= 0f) {
            return null
        }

        val target =
            enemyManager.nearestEnemy(
                x = player.centerX,
                y = player.centerY
            ) ?: return null

        val distance =
            Vector2.dst(
                player.centerX,
                player.centerY,
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
