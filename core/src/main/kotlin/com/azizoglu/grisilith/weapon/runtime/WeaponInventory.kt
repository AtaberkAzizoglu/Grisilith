package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.save.RunSaveWeaponSlot

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

        if (emptySlot != null) {
            emptySlot.weapon =
                weapon

            return WeaponInventoryResult
                .ADDED_TO_EMPTY_SLOT
        }


        val firstUpgradeableMatchingSlot =
            weaponSlots.firstOrNull { slot ->
                val existingWeapon =
                    slot.weapon

                existingWeapon != null &&
                    existingWeapon.definition.id ==
                    weapon.definition.id &&
                    existingWeapon.level <
                    WeaponInstance.MAXIMUM_LEVEL
            }

        if (
            firstUpgradeableMatchingSlot != null
        ) {
            val existingWeapon =
                firstUpgradeableMatchingSlot.weapon
                    ?: return WeaponInventoryResult
                        .INVALID_SLOT

            if (
                !existingWeapon.increaseLevel()
            ) {
                return WeaponInventoryResult
                    .MAXIMUM_LEVEL_REACHED
            }

            return WeaponInventoryResult
                .MERGED
        }


        val hasMatchingWeapon =
            weaponSlots.any { slot ->
                slot.weapon
                    ?.definition
                    ?.id ==
                    weapon.definition.id
            }

        if (hasMatchingWeapon) {
            return WeaponInventoryResult
                .MAXIMUM_LEVEL_REACHED
        }


        return WeaponInventoryResult
            .INVENTORY_FULL
    }

    /*
 * Market kartından sürüklenen Weapon'ı
 * belirli bir slota satın alır.
 *
 * Boş hedef:
 * → Yeni Weapon o slota yerleşir.
 *
 * Aynı Weapon:
 * → Hedef yalnızca +1 level alır.
 *
 * Farklı Weapon:
 * → Satın alma başarısız olur.
 */
    fun addPurchasedWeaponToSlot(
        weapon: WeaponInstance,
        targetSlotIndex: Int
    ): WeaponInventoryResult {
        val targetSlot =
            weaponSlots.getOrNull(
                targetSlotIndex
            )
                ?: return WeaponInventoryResult
                    .INVALID_SLOT

        val targetWeapon =
            targetSlot.weapon

        /*
         * Boş slota doğrudan yerleştir.
         */
        if (targetWeapon == null) {
            /*
             * Tek kopya kuralı bulunan Weapon zaten
             * envanterdeyse ikinci kopya oluşturma.
             */
            if (
                weapon.definition.id ==
                ENCHANTED_CHALK_DUST_ID
            ) {
                val existingWeapon =
                    firstWeaponById(
                        weapon.definition.id
                    )

                if (existingWeapon != null) {
                    if (
                        !existingWeapon.increaseLevel()
                    ) {
                        return WeaponInventoryResult
                            .MAXIMUM_LEVEL_REACHED
                    }

                    return WeaponInventoryResult
                        .UNIQUE_WEAPON_LEVELLED_UP
                }
            }

            targetSlot.weapon =
                weapon

            return WeaponInventoryResult
                .ADDED_TO_EMPTY_SLOT
        }

        /*
         * Dolu hedef yalnızca aynı Weapon ise
         * satın alma yoluyla level atlayabilir.
         */
        val sameWeapon =
            targetWeapon.definition.id ==
                weapon.definition.id

        if (!sameWeapon) {
            return WeaponInventoryResult
                .INCOMPATIBLE_WEAPONS
        }

        if (
            !targetWeapon.increaseLevel()
        ) {
            return WeaponInventoryResult
                .MAXIMUM_LEVEL_REACHED
        }

        return WeaponInventoryResult.MERGED
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

        if (!sameWeapon) {
            return WeaponInventoryResult
                .INCOMPATIBLE_WEAPONS
        }


        val survivingSlot =
            when {
                sourceWeapon.level >
                    targetWeapon.level -> {
                    sourceSlot
                }

                else -> {
                    targetSlot
                }
            }

        val consumedSlot =
            if (
                survivingSlot === sourceSlot
            ) {
                targetSlot
            } else {
                sourceSlot
            }

        val survivingWeapon =
            survivingSlot.weapon
                ?: return WeaponInventoryResult
                    .INVALID_SLOT

        val consumedWeapon =
            consumedSlot.weapon
                ?: return WeaponInventoryResult
                    .INVALID_SLOT

        if (
            !survivingWeapon.increaseLevel()
        ) {
            return WeaponInventoryResult
                .MAXIMUM_LEVEL_REACHED
        }

        survivingWeapon
            .inheritIvySummonChoiceFrom(
                consumedWeapon
            )

        consumedSlot.clear()

        return WeaponInventoryResult.MERGED
    }

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

    fun exportSaveSlots():
        List<RunSaveWeaponSlot> {

        return weaponSlots
            .mapNotNull {
                    slot ->

                val weapon =
                    slot.weapon
                        ?: return@mapNotNull null

                RunSaveWeaponSlot(
                    slotIndex =
                        slot.index,

                    weaponDefinitionId =
                        weapon.definition.id
                            .toString(),

                    level =
                        weapon.level,

                    ivySummonChoiceName =
                        weapon
                            .ivySummonChoice
                            ?.name
                )
            }
    }

    fun clearAllSlots() {

        weaponSlots
            .forEach {
                it.clear()
            }
    }
}
