package com.azizoglu.grisilith.screen.pause

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.utils.viewport.Viewport

class PauseInputController(
    private val viewport:
    Viewport,

    private val layoutProvider:
        () -> PauseLayout,

    private val interactionState:
    PauseInteractionState,

    private val weaponExistsAt:
        (Int) -> Boolean,

    private val groupedItemCountProvider:
        () -> Int
) {

    private var pointerWasHeld =
        false

    private var pressedButton:
        PauseButtonType? =
        null

    fun update():
        PauseAction? {

        updatePointerPosition()

        val layout =
            layoutProvider()

        handleItemScroll(
            layout
        )

        val pointerHeld =
            Gdx.input.isButtonPressed(
                Input.Buttons.LEFT
            )

        val pointerJustPressed =
            pointerHeld &&
                !pointerWasHeld

        val pointerJustReleased =
            !pointerHeld &&
                pointerWasHeld

        /*
         * Confirmation açıkken yalnızca
         * Confirm ve Cancel çalışabilir.
         */
        if (
            interactionState
                .isConfirmationOpen
        ) {
            val action =
                handleConfirmationInput(
                    layout =
                        layout,

                    pointerJustReleased =
                        pointerJustReleased
                )

            pointerWasHeld =
                pointerHeld

            return action
        }

        /*
         * Detay kartı açıkken herhangi bir
         * dış tıklama yalnızca kartı kapatır.
         */
        if (
            interactionState
                .isDetailOpen
        ) {
            if (
                pointerJustReleased &&
                !layout.modalPanel.contains(
                    interactionState.pointerPosition
                )
            ) {
                interactionState
                    .closeAllModals()
            }

            pointerWasHeld =
                pointerHeld

            return null
        }

        val hoveredButton =
            buttonAtPointer(
                layout
            )

        interactionState
            .setHoveredButton(
                hoveredButton
            )

        if (pointerJustPressed) {
            pressedButton =
                hoveredButton

            interactionState
                .setPressedButton(
                    pressedButton
                )
        }

        var action:
            PauseAction? =
            null

        if (pointerJustReleased) {
            val releasedButton =
                hoveredButton

            if (
                pressedButton != null &&
                pressedButton ==
                releasedButton
            ) {
                action =
                    handlePauseButtonRelease(
                        releasedButton
                    )
            } else {
                handleInventoryClick(
                    layout
                )
            }

            pressedButton =
                null

            interactionState
                .setPressedButton(
                    null
                )
        }

        pointerWasHeld =
            pointerHeld

        return action
    }

    private fun handlePauseButtonRelease(
        buttonType:
        PauseButtonType?
    ): PauseAction? {
        return when (buttonType) {
            PauseButtonType
                .RETURN_TO_GAME -> {
                PauseAction.RETURN_TO_GAME
            }

            PauseButtonType
                .RESTART -> {
                interactionState
                    .openConfirmation(
                        PauseConfirmationType
                            .RESTART_RUN
                    )

                null
            }

            PauseButtonType
                .SETTINGS -> {
                PauseAction.OPEN_SETTINGS
            }

            PauseButtonType
                .QUIT_GAME -> {
                interactionState
                    .openConfirmation(
                        PauseConfirmationType
                            .QUIT_TO_MAIN_MENU
                    )

                null
            }

            null -> {
                null
            }
        }
    }

    private fun handleConfirmationInput(
        layout:
        PauseLayout,

        pointerJustReleased:
        Boolean
    ): PauseAction? {
        if (!pointerJustReleased) {
            return null
        }

        val pointer =
            interactionState
                .pointerPosition

        if (
            layout.modalCancelButton
                .contains(
                    pointer
                )
        ) {
            interactionState
                .closeAllModals()

            return null
        }

        if (
            !layout.modalConfirmButton
                .contains(
                    pointer
                )
        ) {
            return null
        }

        return when (
            interactionState
                .confirmationType
        ) {
            PauseConfirmationType
                .RESTART_RUN -> {
                interactionState
                    .closeAllModals()

                PauseAction.RESTART
            }

            PauseConfirmationType
                .QUIT_TO_MAIN_MENU -> {
                interactionState
                    .closeAllModals()

                PauseAction.QUIT_GAME
            }

            null -> {
                null
            }
        }
    }

    private fun handleInventoryClick(
        layout:
        PauseLayout
    ) {
        val pointer =
            interactionState
                .pointerPosition

        val weaponSlotIndex =
            layout.weaponSlots
                .indexOfFirst { rectangle ->
                    rectangle.contains(
                        pointer
                    )
                }

        if (
            weaponSlotIndex >= 0 &&
            weaponExistsAt(
                weaponSlotIndex
            )
        ) {
            interactionState
                .openWeaponDetail(
                    weaponSlotIndex
                )

            return
        }

        when {
            layout.armorSlot
                .contains(
                    pointer
                ) -> {
                interactionState
                    .openGearDetail(
                        PauseGearDetailType
                            .ARMOR
                    )

                return
            }

            layout.weaponTamgaSlot
                .contains(
                    pointer
                ) -> {
                interactionState
                    .openGearDetail(
                        PauseGearDetailType
                            .WEAPON_TAMGA
                    )

                return
            }

            layout.armorTamgaSlot
                .contains(
                    pointer
                ) -> {
                interactionState
                    .openGearDetail(
                        PauseGearDetailType
                            .ARMOR_TAMGA
                    )

                return
            }
        }

        val visibleItemRowIndex =
            PauseLayout
                .itemRowRectangles(
                    layout.itemPanel
                )
                .indexOfFirst { rectangle ->
                    rectangle.contains(
                        pointer
                    )
                }

        if (visibleItemRowIndex < 0) {
            return
        }

        val actualItemIndex =
            interactionState
                .itemScrollOffset +
                visibleItemRowIndex

        if (
            actualItemIndex <
            groupedItemCountProvider()
        ) {
            interactionState
                .openItemDetail(
                    actualItemIndex
                )
        }
    }

    private fun handleItemScroll(
        layout:
        PauseLayout
    ) {
        if (
            interactionState
                .isAnyModalOpen
        ) {
            return
        }

        if (
            !layout.itemPanel.contains(
                interactionState.pointerPosition
            )
        ) {
            return
        }

        val scrollAmount =
            when {
                Gdx.input.isKeyJustPressed(
                    Input.Keys.UP
                ) -> {
                    -1
                }

                Gdx.input.isKeyJustPressed(
                    Input.Keys.DOWN
                ) -> {
                    1
                }

                else -> {
                    0
                }
            }

        if (scrollAmount == 0) {
            return
        }

        val maximumVisibleRows =
            PauseLayout
                .maximumVisibleItemRows(
                    layout.itemPanel
                )

        val maximumOffset =
            (
                groupedItemCountProvider() -
                    maximumVisibleRows
                )
                .coerceAtLeast(
                    0
                )

        interactionState.scrollItems(
            amount =
                scrollAmount,

            maximumOffset =
                maximumOffset
        )
    }

    private fun updatePointerPosition() {
        interactionState
            .pointerPosition
            .set(
                Gdx.input.x.toFloat(),
                Gdx.input.y.toFloat()
            )

        viewport.unproject(
            interactionState
                .pointerPosition
        )
    }

    private fun buttonAtPointer(
        layout:
        PauseLayout
    ): PauseButtonType? {
        val pointer =
            interactionState
                .pointerPosition

        return when {
            layout.returnToGameButton
                .contains(
                    pointer
                ) -> {
                PauseButtonType
                    .RETURN_TO_GAME
            }

            layout.restartButton
                .contains(
                    pointer
                ) -> {
                PauseButtonType
                    .RESTART
            }

            layout.settingsButton
                .contains(
                    pointer
                ) -> {
                PauseButtonType
                    .SETTINGS
            }

            layout.quitGameButton
                .contains(
                    pointer
                ) -> {
                PauseButtonType
                    .QUIT_GAME
            }

            else -> {
                null
            }
        }
    }

    fun reset() {
        pointerWasHeld =
            false

        pressedButton =
            null

        interactionState.reset()
    }
}

enum class PauseAction {

    RETURN_TO_GAME,

    RESTART,

    OPEN_SETTINGS,

    QUIT_GAME
}
