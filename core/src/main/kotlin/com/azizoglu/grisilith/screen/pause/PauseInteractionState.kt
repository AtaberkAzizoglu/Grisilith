package com.azizoglu.grisilith.screen.pause

import com.badlogic.gdx.math.Vector2

class PauseInteractionState {

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

    fun openWeaponDetail(
        slotIndex:
        Int
    ) {
        closeAllModals()

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
        pointerPosition.set(
            0f,
            0f
        )

        pressedButton =
            null

        hoveredButton =
            null

        closeAllModals()

        itemScrollOffset =
            0
    }
}

enum class PauseButtonType {

    RETURN_TO_GAME,

    RESTART,

    SETTINGS,

    QUIT_GAME
}

enum class PauseConfirmationType {

    RESTART_RUN,

    QUIT_TO_MAIN_MENU
}

enum class PauseGearDetailType {

    ARMOR,

    WEAPON_TAMGA,

    ARMOR_TAMGA
}
