package com.azizoglu.grisilith.screen.pause

import com.badlogic.gdx.math.Vector2
import com.azizoglu.grisilith.statistics.StatType

class PauseInteractionState {
    var draggedWeaponSlotIndex: Int? = null
    var weaponDropTargetIndex: Int? = null
    var mergeConfirmationText: String = ""

    val pointerPosition =
        Vector2()

    var pressedButton:
        PauseButtonType? = null
        private set

    var hoveredButton:
        PauseButtonType? = null
        private set

    var detailedWeaponSlotIndex:
        Int? = null
        private set

    var detailedItemIndex:
        Int? = null
        private set

    var gearDetailType:
        PauseGearDetailType? = null
        private set

    var hoveredWeaponSlotIndex: Int? = null
        private set

    var hoveredItemIndex: Int? = null
        private set

    var hoveredGearDetailType: PauseGearDetailType? = null
        private set

    var hoveredStatType: StatType? = null
        private set

    var confirmationType:
        PauseConfirmationType? = null
        private set

    var itemScrollOffset:
        Int = 0
        private set

    val isDetailOpen: Boolean
        get() =
            detailedWeaponSlotIndex != null ||
                detailedItemIndex != null ||
                gearDetailType != null

    val isConfirmationOpen: Boolean
        get() =
            confirmationType != null

    val isAnyModalOpen: Boolean
        get() =
            isDetailOpen ||
                isConfirmationOpen

    fun setPressedButton(
        buttonType:
        PauseButtonType?
    ) {
        pressedButton =
            buttonType
    }

    fun setHoveredButton(
        buttonType:
        PauseButtonType?
    ) {
        hoveredButton =
            buttonType
    }

    fun setHoverDetails(
        weaponSlotIndex: Int?,
        itemIndex: Int?,
        gearType: PauseGearDetailType?,
        statType: StatType?
    ) {
        hoveredWeaponSlotIndex = weaponSlotIndex
        hoveredItemIndex = itemIndex
        hoveredGearDetailType = gearType
        hoveredStatType = statType
    }

    fun openWeaponDetail(
        slotIndex:
        Int
    ) {
        closeAllModals()

        setHoverDetails(null, null, null, null)

        detailedWeaponSlotIndex =
            slotIndex
    }

    fun openItemDetail(
        actualGroupedItemIndex:
        Int
    ) {
        closeAllModals()

        detailedItemIndex =
            actualGroupedItemIndex
    }

    fun openGearDetail(
        detailType:
        PauseGearDetailType
    ) {
        closeAllModals()

        gearDetailType =
            detailType
    }

    fun openConfirmation(
        type:
        PauseConfirmationType
    ) {
        closeAllModals()

        confirmationType =
            type

        pressedButton =
            null

        hoveredButton =
            null
    }

    fun closeAllModals() {
        detailedWeaponSlotIndex =
            null

        detailedItemIndex =
            null

        gearDetailType =
            null

        confirmationType =
            null
    }

    fun scrollItems(
        amount:
        Int,

        maximumOffset:
        Int
    ) {
        itemScrollOffset =
            (
                itemScrollOffset +
                    amount
                )
                .coerceIn(
                    0,
                    maximumOffset
                )
    }

    fun reset() {
        draggedWeaponSlotIndex = null
        weaponDropTargetIndex = null
        pointerPosition.set(
            0f,
            0f
        )

        pressedButton =
            null

        hoveredButton =
            null

        closeAllModals()

        setHoverDetails(null, null, null, null)

        itemScrollOffset =
            0
    }
}

enum class PauseButtonType {

    RETURN_TO_GAME,

    RESTART,

    SETTINGS,

    MAIN_MENU,

    QUIT_GAME
}

enum class PauseConfirmationType {
    MERGE_WEAPONS,

    RESTART_RUN,

    QUIT_TO_MAIN_MENU,

    EXIT_GAME
}

enum class PauseGearDetailType {

    ARMOR,

    WEAPON_TAMGA,

    ARMOR_TAMGA
}
