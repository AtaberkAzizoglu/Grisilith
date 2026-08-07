package com.azizoglu.grisilith.enemy.runtime

import com.azizoglu.grisilith.enemy.runtime.ability.EnemyAbilityRuntime
import com.azizoglu.grisilith.enemy.runtime.ai.EnemyAiPipeline
import com.azizoglu.grisilith.enemy.runtime.weapon.EnemyWeaponRuntimeGroup
import com.azizoglu.grisilith.enemy.trait.EnemyTraitRuntime

/*
 * Bir Enemy'nin hazır runtime parçalarını tek nesnede toplar.
 *
 * abilityRuntime eski hook uyumluluğunu korur.
 * traitRuntime ise gerçek Enemy Trait davranışlarının çalıştığı
 * genişletilebilir katmandır.
 */
data class EnemyRuntimeBundle(
    val abilityRuntime: EnemyAbilityRuntime,
    val weaponRuntime: EnemyWeaponRuntimeGroup,
    val aiPipeline: EnemyAiPipeline,
    val traitRuntime: EnemyTraitRuntime
) {

    companion object {

        val EMPTY =
            EnemyRuntimeBundle(
                abilityRuntime =
                    EnemyAbilityRuntime.EMPTY,

                weaponRuntime =
                    EnemyWeaponRuntimeGroup.EMPTY,

                aiPipeline =
                    EnemyAiPipeline.EMPTY,

                traitRuntime =
                    EnemyTraitRuntime.EMPTY
            )
    }
}
