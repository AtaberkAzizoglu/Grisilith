package com.azizoglu.grisilith.enemy.runtime.ability

import com.azizoglu.grisilith.content.ContentId

/*
 * Enemy Kind tanımındaki aktif ve pasif ability referanslarını
 * runtime boyunca allocation üretmeden saklar.
 *
 * Bu sınıf effect uygulamaz; hazır ability hook'larını sunar.
 */
class EnemyAbilityRuntime(
    activeAbilityIds: List<ContentId>,
    passiveAbilityIds: List<ContentId>
) {

    val activeAbilityIds:
        List<ContentId> =
        activeAbilityIds.toList()

    val passiveAbilityIds:
        List<ContentId> =
        passiveAbilityIds.toList()

    fun hasActiveAbility(
        abilityId: ContentId
    ): Boolean {
        return abilityId in
            activeAbilityIds
    }

    fun hasPassiveAbility(
        abilityId: ContentId
    ): Boolean {
        return abilityId in
            passiveAbilityIds
    }

    companion object {

        val EMPTY =
            EnemyAbilityRuntime(
                activeAbilityIds =
                    emptyList(),

                passiveAbilityIds =
                    emptyList()
            )
    }
}
