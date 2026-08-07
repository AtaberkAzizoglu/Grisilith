package com.azizoglu.grisilith.enemy.runtime.ai

import com.azizoglu.grisilith.enemy.runtime.ability.EnemyAbilityRuntime
import com.azizoglu.grisilith.enemy.runtime.weapon.EnemyWeaponRuntimeGroup

/*
 * Enemy AI güncelleme sırasını sabitler:
 *
 * 1. Passive hooks
 * 2. Active ability hooks
 * 3. Weapon hooks
 *
 * Bu paket gerçek saldırı veya effect üretmez. Sonraki combat
 * uygulamaları bu sıralı hook noktalarına bağlanacaktır.
 */
class EnemyAiPipeline(
    private val abilityRuntime:
    EnemyAbilityRuntime,

    private val weaponRuntime:
    EnemyWeaponRuntimeGroup
) {

    private var elapsedSeconds =
        0f

    val runtimeSeconds: Float
        get() =
            elapsedSeconds

    fun update(
        delta: Float
    ) {
        elapsedSeconds +=
            delta.coerceAtLeast(
                0f
            )

        updatePassiveHooks()
        updateActiveAbilityHooks()
        updateWeaponHooks()
    }

    private fun updatePassiveHooks() {
        /*
         * Wolf Pack, Angry, Incorporeal Evasion,
         * Acidic Path ve Vampire Hunger buraya bağlanacak.
         */
        abilityRuntime
            .passiveAbilityIds
    }

    private fun updateActiveAbilityHooks() {
        /*
         * Charge, Jump, Throw ve Misty Transportation
         * buraya bağlanacak.
         */
        abilityRuntime
            .activeAbilityIds
    }

    private fun updateWeaponHooks() {
        /*
         * Melee, Ranged ve Spell Focus enemy saldırıları;
         * Level 3 basic upgrade ve Level 5 skill kontrolleriyle
         * bu runtime listesinden yürütülecek.
         */
        weaponRuntime
            .allWeapons
    }

    companion object {

        val EMPTY =
            EnemyAiPipeline(
                abilityRuntime =
                    EnemyAbilityRuntime.EMPTY,

                weaponRuntime =
                    EnemyWeaponRuntimeGroup.EMPTY
            )
    }
}
