package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.content.ContentId

class WeaponInventory {

    companion object {
        const val WEAPON_SLOT_COUNT = 6

        /*
         * Revize weapon dosyasındaki kalıcı ID.
         * DefaultWeapons içine eklendiğinde de aynı
         * ContentId kullanılmalı.
         */
        private val ENCHANTED_CHALK_DUST_ID =
            ContentId(
                "WPN_ENCHANTED_CHALK_DUST"
            )
    }

    val weaponSlots:
        List<WeaponSlot> =
        List(
            WEAPON_SLOT_COUNT
        ) { index ->

            WeaponSlot(
                index = index
            )
        }

    val equippedWeaponCount: Int
        get() =
            weaponSlots.count { slot ->
                !slot.isEmpty
            }

    val isFull: Boolean
        get() =
            weaponSlots.none { slot ->
                slot.isEmpty
            }

    fun addWeapon(
        weapon: WeaponInstance
    ): Boolean {
        return addPurchasedWeapon(
            weapon
        ) ==
            WeaponInventoryResult
                .ADDED_TO_EMPTY_SLOT
    }

    /*
     * Market veya sandıktan gelen WeaponInstance
     * için merkezi ekleme kuralı.
     */
    fun addPurchasedWeapon(
        weapon: WeaponInstance
    ): WeaponInventoryResult {
        if (
            weapon.definition.id ==
            ENCHANTED_CHALK_DUST_ID
        ) {
            return addEnchantedChalkDust(
                weapon
            )
        }

        val emptySlot =
            weaponSlots.firstOrNull { slot ->
                slot.isEmpty
            }
                ?: return WeaponInventoryResult
                    .INVENTORY_FULL

        emptySlot.weapon =
            weapon

        return WeaponInventoryResult
            .ADDED_TO_EMPTY_SLOT
    }

    private fun addEnchantedChalkDust(
        weapon: WeaponInstance
    ): WeaponInventoryResult {
        val existingSlot =
            weaponSlots.firstOrNull { slot ->
                slot.weapon
                    ?.definition
                    ?.id ==
                    ENCHANTED_CHALK_DUST_ID
            }

        /*
         * Envanterde zaten Chalk Dust varsa,
         * yeni kopya eklenmez. İlk kopya level alır.
         */
        if (existingSlot != null) {
            val existingWeapon =
                existingSlot.weapon
                    ?: return WeaponInventoryResult
                        .INVALID_SLOT

            if (
                !existingWeapon.increaseLevel()
            ) {
                return WeaponInventoryResult
                    .MAXIMUM_LEVEL_REACHED
            }

            return WeaponInventoryResult
                .UNIQUE_WEAPON_LEVELLED_UP
        }

        /*
         * Henüz Chalk Dust yoksa normal şekilde
         * bir boş slota eklenir.
         */
        val emptySlot =
            weaponSlots.firstOrNull { slot ->
                slot.isEmpty
            }
                ?: return WeaponInventoryResult
                    .INVENTORY_FULL

        emptySlot.weapon =
            weapon

        return WeaponInventoryResult
            .ADDED_TO_EMPTY_SLOT
    }

    /*
     * Drag-and-drop ile iki slotu birleştirir.
     *
     * İlk güvenli kural:
     * - Aynı WeaponDefinition
     * - Aynı level
     * - Hedef Lv.5 değil
     *
     * Kaynak silah tüketilir, hedef bir level artar.
     */
    fun mergeSlots(
        sourceSlotIndex: Int,
        targetSlotIndex: Int
    ): WeaponInventoryResult {
        if (
            sourceSlotIndex ==
            targetSlotIndex
        ) {
            return WeaponInventoryResult
                .INVALID_SLOT
        }

        val sourceSlot =
            weaponSlots.getOrNull(
                sourceSlotIndex
            )
                ?: return WeaponInventoryResult
                    .INVALID_SLOT

        val targetSlot =
            weaponSlots.getOrNull(
                targetSlotIndex
            )
                ?: return WeaponInventoryResult
                    .INVALID_SLOT

        val sourceWeapon =
            sourceSlot.weapon
                ?: return WeaponInventoryResult
                    .INVALID_SLOT

        val targetWeapon =
            targetSlot.weapon
                ?: return WeaponInventoryResult
                    .INVALID_SLOT

        val sameWeapon =
            sourceWeapon.definition.id ==
                targetWeapon.definition.id

        val sameLevel =
            sourceWeapon.level ==
                targetWeapon.level

        if (
            !sameWeapon ||
            !sameLevel
        ) {
            return WeaponInventoryResult
                .INCOMPATIBLE_WEAPONS
        }

        if (
            !targetWeapon.increaseLevel()
        ) {
            return WeaponInventoryResult
                .MAXIMUM_LEVEL_REACHED
        }

        sourceSlot.clear()

        return WeaponInventoryResult.MERGED
    }

    /*
     * İki silahın yerini değiştirir.
     * Boş slota sürükleme de bu metotla çalışır.
     */
    fun swapSlots(
        firstSlotIndex: Int,
        secondSlotIndex: Int
    ): Boolean {
        if (
            firstSlotIndex ==
            secondSlotIndex
        ) {
            return false
        }

        val firstSlot =
            weaponSlots.getOrNull(
                firstSlotIndex
            )
                ?: return false

        val secondSlot =
            weaponSlots.getOrNull(
                secondSlotIndex
            )
                ?: return false

        val firstWeapon =
            firstSlot.weapon

        firstSlot.weapon =
            secondSlot.weapon

        secondSlot.weapon =
            firstWeapon

        return true
    }

    fun removeWeaponFromSlot(
        slotIndex: Int
    ): WeaponInstance? {
        val slot =
            weaponSlots.getOrNull(
                slotIndex
            )
                ?: return null

        val removedWeapon =
            slot.weapon

        slot.clear()

        return removedWeapon
    }

    fun weaponAt(
        slotIndex: Int
    ): WeaponInstance? {
        return weaponSlots
            .getOrNull(slotIndex)
            ?.weapon
    }

    fun firstWeaponById(
        contentId: ContentId
    ): WeaponInstance? {
        return weaponSlots
            .firstNotNullOfOrNull { slot ->

                slot.weapon
                    ?.takeIf { weapon ->
                        weapon.definition.id ==
                            contentId
                    }
            }
    }

    fun containsWeapon(
        contentId: ContentId
    ): Boolean {
        return firstWeaponById(
            contentId
        ) != null
    }

    fun allWeapons():
        List<WeaponInstance> {

        return weaponSlots
            .mapNotNull { slot ->
                slot.weapon
            }
    }
}
