package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyType
import com.badlogic.gdx.math.Vector2

/** Shared automatic ability policy. Distance breaks ties within a rank. */
object AbilityTargeting {
    // Priority is independent of combat classification: a rogue summon must
    // not gain boss damage modifiers, immunities or rewards merely for targeting.
    fun priority(enemy: Enemy): Int = if(enemy.weaponTargetSummon != null) 1 else when (enemy.type) {
        EnemyType.FINAL_BOSS -> 0
        EnemyType.MINI_BOSS -> 1
        EnemyType.UNIQUE -> 2
        EnemyType.NORMAL -> 3
    }
    fun select(enemies: Iterable<Enemy>, x: Float, y: Float, range: Float,
               preferredX: Float = x, preferredY: Float = y): Enemy? {
        val eligible = enemies.filter { !it.isDead &&
            Vector2.dst(x, y, it.centerX, it.centerY) <= range + it.bounds.width * .5f }
        return eligible.minWithOrNull(compareBy<Enemy> { priority(it) }
            .thenBy { Vector2.dst2(preferredX, preferredY, it.centerX, it.centerY) })
    }
}
