package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyManager
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.weapon.definition.WeaponTargetingType
import com.badlogic.gdx.math.Vector2
import com.azizoglu.grisilith.settings.GameSettings

class WeaponTargetSelector {
    companion object {
        private var mouseX = 0f
        private var mouseY = 0f
        fun updateMouse(x: Float, y: Float) { mouseX = x; mouseY = y }
    }

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

        val eligible = enemyManager.weaponTargets.filter { !it.isDead &&
            Vector2.dst2(originX, originY, it.centerX, it.centerY) <= maximumRange * maximumRange }
        return when (GameSettings.targeting) {
            GameSettings.Targeting.BOSS -> eligible.minWithOrNull(compareBy<Enemy> { AbilityTargeting.priority(it) }
                .thenBy { Vector2.dst2(originX, originY, it.centerX, it.centerY) })
            GameSettings.Targeting.MOUSE -> eligible.minByOrNull { Vector2.dst2(mouseX, mouseY, it.centerX, it.centerY) }
            GameSettings.Targeting.CLOSEST -> eligible.minByOrNull { Vector2.dst2(originX, originY, it.centerX, it.centerY) }
        }
    }
}
