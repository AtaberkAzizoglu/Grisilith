package com.azizoglu.grisilith.enemy.runtime.weapon

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.enemy.definition.EnemyWeaponDefinition

/*
 * Enemy loadout içindeki tek Weapon kopyasının hafif runtime'ı.
 *
 * Level 3 normal attack ve Level 5 skill kullanım izni burada
 * merkezi olarak çözümlenir.
 */
class EnemyWeaponRuntime(
    val weaponId: ContentId,
    val level: Int,
    val slotIndex: Int
) {

    init {
        require(level in 1..5)
        require(slotIndex >= 0)
    }

    val canUseUpgradedBasicAttack: Boolean
        get() =
            level >= 3

    val canUseWeaponSkill: Boolean
        get() =
            level >= 5
}

class EnemyWeaponRuntimeGroup private constructor(
    val allWeapons: List<EnemyWeaponRuntime>
) {

    val isEmpty: Boolean
        get() =
            allWeapons.isEmpty()

    val weaponCount: Int
        get() =
            allWeapons.size

    companion object {

        val EMPTY =
            EnemyWeaponRuntimeGroup(
                allWeapons =
                    emptyList()
            )

        fun fromDefinitions(
            definitions:
            List<EnemyWeaponDefinition>
        ): EnemyWeaponRuntimeGroup {

            if (
                definitions.isEmpty()
            ) {
                return EMPTY
            }

            val runtimes =
                ArrayList<EnemyWeaponRuntime>()

            definitions.forEach { definition ->
                repeat(
                    definition.quantity
                ) {
                    runtimes.add(
                        EnemyWeaponRuntime(
                            weaponId =
                                definition.weaponId,

                            level =
                                definition.level,

                            slotIndex =
                                runtimes.size
                        )
                    )
                }
            }

            return EnemyWeaponRuntimeGroup(
                allWeapons =
                    runtimes.toList()
            )
        }
    }
}
