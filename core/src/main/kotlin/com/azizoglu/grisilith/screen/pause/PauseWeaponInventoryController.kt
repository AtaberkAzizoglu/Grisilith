package com.azizoglu.grisilith.screen.pause

import com.azizoglu.grisilith.weapon.runtime.WeaponInventory
import com.azizoglu.grisilith.weapon.runtime.WeaponInventoryResult
import com.azizoglu.grisilith.weapon.runtime.WeaponInstance

/** Reuses the market merge rules without resetting ability cooldowns. */
class PauseWeaponInventoryController(
    private val inventory: WeaponInventory,
    private val state: PauseInteractionState,
    private val onChanged: () -> Unit
) {
    private var pending: (() -> Unit)? = null

    fun drop(source: Int, target: Int) {
        pending = null
        if (source == target || target !in inventory.weaponSlots.indices) return
        val first = inventory.weaponAt(source) ?: return
        val second = inventory.weaponAt(target)
        if (second == null || first.definition.id != second.definition.id) {
            if (inventory.swapSlots(source, target)) onChanged()
            return
        }
        if (maxOf(first.level, second.level) >= WeaponInstance.MAXIMUM_LEVEL) return
        val merge = {
            // A modal must never consume a different weapon if the inventory changed.
            if (inventory.weaponAt(source) === first && inventory.weaponAt(target) === second &&
                inventory.mergeSlots(source, target) == WeaponInventoryResult.MERGED) onChanged()
        }
        val consumed = if (first.level > second.level) second else first
        if (consumed.level > 1) {
            state.mergeConfirmationText = "Consume the level ${consumed.level} ${consumed.definition.displayName} copy?\nThe stronger copy gains one level. This cannot be undone."
            state.openConfirmation(PauseConfirmationType.MERGE_WEAPONS)
            pending = merge
        } else merge()
    }

    fun confirm() {
        val action = pending
        pending = null
        state.closeAllModals()
        action?.invoke()
    }
}
