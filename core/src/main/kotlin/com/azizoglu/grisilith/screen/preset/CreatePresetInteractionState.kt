package com.azizoglu.grisilith.screen.preset

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.preset.PresetStatAllocation
import com.azizoglu.grisilith.preset.RunPreset
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.tamga.definition.TamgaDefinition
import com.azizoglu.grisilith.tamga.definition.TamgaPresetSlot

class CreatePresetInteractionState {

    var selectedCharacterId:
        ContentId? =
        null
        private set

    var selectedWeaponId:
        ContentId? =
        null
        private set

    var selectedArmorId:
        ContentId? =
        null
        private set

    var selectedItemId:
        ContentId? =
        null
        private set

    var selectedWeaponTamgaId:
        ContentId? =
        null
        private set

    var selectedArmorTamgaId:
        ContentId? =
        null
        private set

    var statAllocation:
        PresetStatAllocation =
        PresetStatAllocation()
        private set

    var statPointBudget:
        Int =
        10
        private set

    var isCharacterPickerOpen:
        Boolean =
        false
        private set

    var inspectedCharacterId:
        ContentId? =
        null
        private set

    var inspectedEquipment:
        CreatePresetInspectedEquipment? =
        null
        private set

    var inspectedTamgaId:
        ContentId? =
        null
        private set

    var inspectedStatType:
        StatType? =
        null
        private set

    var draggedContent:
        CreatePresetDraggedContent? =
        null
        private set

    var showLockedContent:
        Boolean =
        false
        private set

    var confirmationType:
        CreatePresetConfirmationType? =
        null
        private set

    val isCharacterDetailOpen: Boolean
        get() =
            inspectedCharacterId != null

    val isEquipmentDetailOpen: Boolean
        get() =
            inspectedEquipment != null

    val isTamgaDetailOpen: Boolean
        get() =
            inspectedTamgaId != null

    val isStatEditorOpen: Boolean
        get() =
            confirmationType ==
                CreatePresetConfirmationType
                    .EDIT_STATS

    val isConfirmationOpen: Boolean
        get() =
            confirmationType ==
                CreatePresetConfirmationType
                    .DISCARD_AND_GO_BACK ||
                confirmationType ==
                CreatePresetConfirmationType
                    .SAVE_PRESET

    val isAnyModalOpen: Boolean
        get() =
            isCharacterPickerOpen ||
                isCharacterDetailOpen ||
                isEquipmentDetailOpen ||
                isTamgaDetailOpen ||
                inspectedStatType != null ||
                confirmationType != null

    val hasCharacterSelected: Boolean
        get() =
            selectedCharacterId != null

    val hasUnsavedChanges: Boolean
        get() =
            selectedCharacterId != null ||
                selectedWeaponId != null ||
                selectedArmorId != null ||
                selectedItemId != null ||
                selectedWeaponTamgaId != null ||
                selectedArmorTamgaId != null ||
                statAllocation.spentPointCount >
                0

    /*
     * Tamga slotları isteğe bağlıdır.
     */
    val isCompleteWithoutTamga: Boolean
        get() =
            selectedCharacterId != null &&
                selectedWeaponId != null &&
                selectedArmorId != null &&
                selectedItemId != null

    val remainingStatPoints: Int
        get() =
            (
                statPointBudget -
                    statAllocation
                        .spentPointCount
                )
                .coerceAtLeast(
                    0
                )

    fun loadFromPreset(
        preset:
        RunPreset
    ) {
        reset()

        selectedCharacterId =
            preset.characterId

        selectedWeaponId =
            preset.weaponId

        selectedArmorId =
            preset.armorId

        selectedItemId =
            preset.itemId

        selectedWeaponTamgaId =
            preset.weaponTamgaId

        selectedArmorTamgaId =
            preset.armorTamgaId

        statAllocation =
            preset.statAllocation

        statPointBudget =
            preset.statPointBudget
    }

    fun openCharacterPicker() {
        closeModals()

        isCharacterPickerOpen =
            true
    }

    fun inspectCharacter(
        characterId:
        ContentId
    ) {
        closeModals()

        inspectedCharacterId =
            characterId
    }

    fun chooseInspectedCharacter() {
        val inspectedId =
            inspectedCharacterId
                ?: return

        val changed =
            selectedCharacterId !=
                inspectedId

        selectedCharacterId =
            inspectedId

        inspectedCharacterId =
            null

        isCharacterPickerOpen =
            false

        if (
            changed
        ) {
            statAllocation =
                PresetStatAllocation()
        }
    }

    fun inspectWeapon(
        weaponId:
        ContentId
    ) {
        closeModals()

        inspectedEquipment =
            CreatePresetInspectedEquipment(
                contentId =
                    weaponId,

                type =
                    CreatePresetEquipmentType
                        .WEAPON
            )
    }

    fun inspectArmor(
        armorId:
        ContentId
    ) {
        closeModals()

        inspectedEquipment =
            CreatePresetInspectedEquipment(
                contentId =
                    armorId,

                type =
                    CreatePresetEquipmentType
                        .ARMOR
            )
    }

    fun inspectItem(
        itemId:
        ContentId
    ) {
        closeModals()

        inspectedEquipment =
            CreatePresetInspectedEquipment(
                contentId =
                    itemId,

                type =
                    CreatePresetEquipmentType
                        .ITEM
            )
    }

    fun inspectTamga(
        tamgaId:
        ContentId
    ) {
        closeModals()

        inspectedTamgaId =
            tamgaId
    }

    fun inspectStat(
        statType:
        StatType
    ) {
        inspectedStatType =
            statType
    }

    fun closeStatDescription() {
        inspectedStatType =
            null
    }

    fun openStatEditor() {
        if (
            !hasCharacterSelected
        ) {
            return
        }

        closeModals()

        confirmationType =
            CreatePresetConfirmationType
                .EDIT_STATS
    }

    fun increaseStat(
        statType:
        StatType,

        characterBaseScore:
        Int
    ) {
        statAllocation =
            statAllocation.increase(
                statType =
                    statType,

                characterBaseScore =
                    characterBaseScore,

                pointBudget =
                    statPointBudget
            )
    }

    fun decreaseStat(
        statType:
        StatType
    ) {
        statAllocation =
            statAllocation.decrease(
                statType
            )
    }

    fun beginWeaponDrag(
        weaponId:
        ContentId
    ) {
        if (
            !hasCharacterSelected
        ) {
            return
        }

        closeModals()

        draggedContent =
            CreatePresetDraggedContent(
                contentId =
                    weaponId,

                type =
                    CreatePresetEquipmentType
                        .WEAPON
            )
    }

    fun beginArmorDrag(
        armorId:
        ContentId
    ) {
        if (
            !hasCharacterSelected
        ) {
            return
        }

        closeModals()

        draggedContent =
            CreatePresetDraggedContent(
                contentId =
                    armorId,

                type =
                    CreatePresetEquipmentType
                        .ARMOR
            )
    }

    fun beginItemDrag(
        itemId:
        ContentId
    ) {
        if (
            !hasCharacterSelected
        ) {
            return
        }

        closeModals()

        draggedContent =
            CreatePresetDraggedContent(
                contentId =
                    itemId,

                type =
                    CreatePresetEquipmentType
                        .ITEM
            )
    }

    fun beginTamgaDrag(
        tamgaId:
        ContentId
    ) {
        if (
            !hasCharacterSelected
        ) {
            return
        }

        closeModals()

        draggedContent =
            CreatePresetDraggedContent(
                contentId =
                    tamgaId,

                type =
                    CreatePresetEquipmentType
                        .TAMGA
            )
    }

    fun finishDragOnWeaponSlot() {
        val dragged =
            draggedContent
                ?: return

        if (
            dragged.type ==
            CreatePresetEquipmentType
                .WEAPON
        ) {
            selectedWeaponId =
                dragged.contentId
        }

        cancelDrag()
    }

    fun finishDragOnArmorSlot() {
        val dragged =
            draggedContent
                ?: return

        if (
            dragged.type ==
            CreatePresetEquipmentType
                .ARMOR
        ) {
            selectedArmorId =
                dragged.contentId
        }

        cancelDrag()
    }

    fun finishDragOnItemSlot() {
        val dragged =
            draggedContent
                ?: return

        if (
            dragged.type ==
            CreatePresetEquipmentType
                .ITEM
        ) {
            selectedItemId =
                dragged.contentId
        }

        cancelDrag()
    }

    /*
     * Tamga uyumluluğu ekran tarafından,
     * TamgaCompatibility kullanılarak kontrol edilir.
     *
     * State yalnızca geçerli seçimi kaydeder.
     */
    fun finishTamgaDrag(
        targetSlot:
        TamgaPresetSlot,

        tamga:
        TamgaDefinition
    ) {
        val dragged =
            draggedContent
                ?: return

        if (
            dragged.type !=
            CreatePresetEquipmentType
                .TAMGA ||
            dragged.contentId !=
            tamga.id
        ) {
            cancelDrag()
            return
        }

        when (targetSlot) {
            TamgaPresetSlot
                .WEAPON_TAMGA_SLOT -> {

                if (
                    tamga.canUseWeaponSlot
                ) {
                    selectedWeaponTamgaId =
                        tamga.id

                    if (
                        selectedArmorTamgaId ==
                        tamga.id
                    ) {
                        selectedArmorTamgaId =
                            null
                    }
                }
            }

            TamgaPresetSlot
                .ARMOR_TAMGA_SLOT -> {

                if (
                    tamga.canUseArmorSlot
                ) {
                    selectedArmorTamgaId =
                        tamga.id

                    if (
                        selectedWeaponTamgaId ==
                        tamga.id
                    ) {
                        selectedWeaponTamgaId =
                            null
                    }
                }
            }
        }

        cancelDrag()
    }

    fun clearWeaponTamga() {
        selectedWeaponTamgaId =
            null
    }

    fun clearArmorTamga() {
        selectedArmorTamgaId =
            null
    }

    fun cancelDrag() {
        draggedContent =
            null
    }

    fun toggleLockedContentVisibility() {
        showLockedContent =
            !showLockedContent
    }

    fun openConfirmation(
        type:
        CreatePresetConfirmationType
    ) {
        closeModals()

        confirmationType =
            type
    }

    fun closeModals() {
        isCharacterPickerOpen =
            false

        inspectedCharacterId =
            null

        inspectedEquipment =
            null

        inspectedTamgaId =
            null

        inspectedStatType =
            null

        confirmationType =
            null
    }

    fun reset() {
        selectedCharacterId =
            null

        selectedWeaponId =
            null

        selectedArmorId =
            null

        selectedItemId =
            null

        selectedWeaponTamgaId =
            null

        selectedArmorTamgaId =
            null

        statAllocation =
            PresetStatAllocation()

        statPointBudget =
            10

        isCharacterPickerOpen =
            false

        inspectedCharacterId =
            null

        inspectedEquipment =
            null

        inspectedTamgaId =
            null

        inspectedStatType =
            null

        draggedContent =
            null

        confirmationType =
            null

        showLockedContent =
            false
    }
}

data class CreatePresetDraggedContent(
    val contentId:
    ContentId,

    val type:
    CreatePresetEquipmentType
)

data class CreatePresetInspectedEquipment(
    val contentId:
    ContentId,

    val type:
    CreatePresetEquipmentType
)

enum class CreatePresetEquipmentType {

    WEAPON,

    ARMOR,

    ITEM,

    TAMGA
}

enum class CreatePresetConfirmationType {

    DISCARD_AND_GO_BACK,

    SAVE_PRESET,

    EDIT_STATS
}
