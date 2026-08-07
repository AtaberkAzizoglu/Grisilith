package com.azizoglu.grisilith.enemy.definition

import com.azizoglu.grisilith.content.ContentId

/*
 * Enemy'lerin kullanacağı hafif ekipman tanımı.
 *
 * Runtime tarafı mevcut WeaponDefinition ve ArmorDefinition
 * verilerini okuyacak; Player inventory veya market sistemini
 * Enemy'ye taşımayacak.
 */
data class EnemyEquipmentDefinition(
    val weapons: List<EnemyWeaponDefinition> =
        emptyList(),

    val armorId: ContentId? =
        null
) {

    init {
        require(
            weapons
                .groupBy { weapon ->
                    weapon.weaponId to
                        weapon.level
                }
                .values
                .all { entries ->
                    entries.size == 1
                }
        ) {
            "Use EnemyWeaponDefinition.quantity instead of duplicate weapon entries."
        }
    }

    val isEmpty: Boolean
        get() =
            weapons.isEmpty() &&
                armorId == null

    val totalWeaponCount: Int
        get() =
            weapons.sumOf { weapon ->
                weapon.quantity
            }
}
