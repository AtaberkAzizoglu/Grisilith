package com.azizoglu.grisilith.enemy.definition

import com.azizoglu.grisilith.content.ContentId

/*
 * Bu paket yalnızca davranış referanslarını tanımlar.
 * Runtime uygulamaları sonraki combat paketlerinde eklenecek.
 */
object EnemyBehaviorIds {

    val CHASE_AND_MELEE =
        ContentId("ENEMY_BEHAVIOR_CHASE_AND_MELEE")

    val FAST_CHASE_AND_MELEE =
        ContentId("ENEMY_BEHAVIOR_FAST_CHASE_AND_MELEE")

    val KEEP_DISTANCE_AND_SHOOT =
        ContentId("ENEMY_BEHAVIOR_KEEP_DISTANCE_AND_SHOOT")

    val SLOW_CHASE_AND_MELEE =
        ContentId("ENEMY_BEHAVIOR_SLOW_CHASE_AND_MELEE")

    val MULTI_WEAPON_CASTER =
        ContentId("ENEMY_BEHAVIOR_MULTI_WEAPON_CASTER")

    val SWARM_CHASE =
        ContentId("ENEMY_BEHAVIOR_SWARM_CHASE")

    val INCORPOREAL_CHASE =
        ContentId("ENEMY_BEHAVIOR_INCORPOREAL_CHASE")
}
