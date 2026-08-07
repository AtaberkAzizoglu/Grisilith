package com.azizoglu.grisilith.enemy.runtime

import com.azizoglu.grisilith.enemy.runtime.ability.EnemyAbilityRuntime
import com.azizoglu.grisilith.enemy.runtime.ai.EnemyAiPipeline
import com.azizoglu.grisilith.enemy.runtime.weapon.EnemyWeaponRuntimeGroup

/*
 * Bir Enemy'nin davranış dışındaki hazır runtime parçalarını
 * tek nesnede toplar.
 *
 * Gerçek effect/projectile üretimi sonraki combat paketlerinde
 * bu hook'lara bağlanacaktır.
 */
data class EnemyRuntimeBundle(
    val abilityRuntime: EnemyAbilityRuntime,
    val weaponRuntime: EnemyWeaponRuntimeGroup,
    val aiPipeline: EnemyAiPipeline
) {

    companion object {

        val EMPTY =
            EnemyRuntimeBundle(
                abilityRuntime =
                    EnemyAbilityRuntime.EMPTY,

                weaponRuntime =
                    EnemyWeaponRuntimeGroup.EMPTY,

                aiPipeline =
                    EnemyAiPipeline.EMPTY
            )
    }
}
