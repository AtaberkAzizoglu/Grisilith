package com.azizoglu.grisilith.enemy.definition

import com.azizoglu.grisilith.content.ContentId

/*
 * Enemy loadout içindeki tek bir Weapon girişini tanımlar.
 *
 * quantity alanı Witch gibi aynı Weapon'dan birden fazla kullanan
 * Enemy Kind'ları destekler.
 */
data class EnemyWeaponDefinition(
    val weaponId: ContentId,
    val level: Int,
    val quantity: Int = 1
) {

    init {
        require(level in 1..5) {
            "Enemy weapon level must be between 1 and 5."
        }

        require(quantity > 0) {
            "Enemy weapon quantity must be greater than zero."
        }
    }
}
