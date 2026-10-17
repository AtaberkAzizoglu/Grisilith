package com.azizoglu.grisilith.screen.pause

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
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
        () -> Int,
    private val onWeaponDrop: (Int, Int) -> Unit = { _, _ -> },
    private val onConfirmWeaponMerge: () -> Unit = {}
) : InputAdapter() {

    private var pointerWasHeld =
        false

    private var pressedButton:
        PauseButtonType? =
        null

    private var itemDragLastY: Float? = null
    private var weaponPressSlot: Int? = null
    private val weaponPressPosition = com.badlogic.gdx.math.Vector2()

    override fun scrolled(amountX: Float, amountY: Float): Boolean {
        val layout = layoutProvider()
        updatePointerPosition()
        if (interactionState.isAnyModalOpen || !layout.itemPanel.contains(interactionState.pointerPosition)) {
            return false
        }
        scrollItemRows(layout, if (amountY > 0f) 1 else -1)
        return true
    }

    fun update():
        PauseAction? {

        updatePointerPosition()

        val layout =
            layoutProvider()

        updateHoverDetails(layout)

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

        if (!interactionState.isAnyModalOpen && handleWeaponDrag(layout, pointerHeld, pointerJustPressed, pointerJustReleased)) {
            pointerWasHeld = pointerHeld
            return null
        }
        handleItemDrag(layout, pointerHeld)

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
                .MAIN_MENU -> {
                interactionState.openConfirmation(PauseConfirmationType.QUIT_TO_MAIN_MENU)
                null
            }

            PauseButtonType
                .QUIT_GAME -> {
                interactionState
                    .openConfirmation(
                        PauseConfirmationType
                            .EXIT_GAME
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
            PauseConfirmationType.MERGE_WEAPONS -> {
                onConfirmWeaponMerge()
                null
            }
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

                PauseAction.MAIN_MENU
            }

            PauseConfirmationType
                .EXIT_GAME -> {
                interactionState.closeAllModals()
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
                .itemSlotRectangles(
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

        val maximumOffset = PauseLayout.maximumItemScrollOffset(
            groupedItemCountProvider(),
            layout.itemPanel
        )

        interactionState.scrollItems(
            amount =
                scrollAmount * PauseLayout.ITEM_COLUMNS,

            maximumOffset =
                maximumOffset
        )
    }

    private fun scrollItemRows(layout: PauseLayout, rows: Int) {
        val maximumOffset = PauseLayout.maximumItemScrollOffset(groupedItemCountProvider(), layout.itemPanel)
        interactionState.scrollItems(rows * PauseLayout.ITEM_COLUMNS, maximumOffset)
    }

    private fun handleItemDrag(layout: PauseLayout, pointerHeld: Boolean) {
        val pointer = interactionState.pointerPosition
        if (!pointerHeld || !layout.itemPanel.contains(pointer) || interactionState.isAnyModalOpen) {
            itemDragLastY = null
            return
        }
        val previousY = itemDragLastY
        if (previousY == null) {
            itemDragLastY = pointer.y
            return
        }
        val delta = pointer.y - previousY
        if (kotlin.math.abs(delta) >= 18f) {
            scrollItemRows(layout, if (delta < 0f) 1 else -1)
            itemDragLastY = pointer.y
        }
    }

    private fun updateHoverDetails(layout: PauseLayout) {
        if (interactionState.isAnyModalOpen || interactionState.draggedWeaponSlotIndex != null) {
            interactionState.setHoverDetails(null, null, null, null)
            return
        }
        val pointer = interactionState.pointerPosition
        val weapon = layout.weaponSlots.indexOfFirst { it.contains(pointer) }
            .takeIf { it >= 0 && weaponExistsAt(it) }
        val visibleItem = PauseLayout.itemSlotRectangles(layout.itemPanel)
            .indexOfFirst { it.contains(pointer) }
        val item = if (visibleItem >= 0) {
            (interactionState.itemScrollOffset + visibleItem)
                .takeIf { it < groupedItemCountProvider() }
        } else null
        val gear = when {
            layout.armorSlot.contains(pointer) -> PauseGearDetailType.ARMOR
            layout.weaponTamgaSlot.contains(pointer) -> PauseGearDetailType.WEAPON_TAMGA
            layout.armorTamgaSlot.contains(pointer) -> PauseGearDetailType.ARMOR_TAMGA
            else -> null
        }
        val stat = PauseLayout.statRowRectangles(layout.characterStatsPanel)
            .entries.firstOrNull { it.value.contains(pointer) }?.key
        interactionState.setHoverDetails(weapon, item, gear, stat)
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

            layout.mainMenuButton.contains(pointer) -> {
                PauseButtonType.MAIN_MENU
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
        weaponPressSlot = null
        pointerWasHeld =
            false

        pressedButton =
            null

        interactionState.reset()

        itemDragLastY = null
    }

    private fun handleWeaponDrag(layout: PauseLayout, held: Boolean, pressed: Boolean, released: Boolean): Boolean {
        val pointer = interactionState.pointerPosition
        if (pressed) {
            weaponPressSlot = layout.weaponSlots.indexOfFirst { it.contains(pointer) }
                .takeIf { it >= 0 && weaponExistsAt(it) }
            weaponPressPosition.set(pointer)
        }
        val source = weaponPressSlot ?: return false
        if (held && pointer.dst2(weaponPressPosition) >= 64f) interactionState.draggedWeaponSlotIndex = source
        val dragging = interactionState.draggedWeaponSlotIndex != null
        interactionState.weaponDropTargetIndex = if (dragging) layout.weaponSlots.indexOfFirst { it.contains(pointer) }.takeIf { it >= 0 } else null
        if (released) {
            val target = interactionState.weaponDropTargetIndex
            weaponPressSlot = null
            interactionState.draggedWeaponSlotIndex = null
            interactionState.weaponDropTargetIndex = null
            pressedButton = null
            interactionState.setPressedButton(null)
            if (dragging && target != null) onWeaponDrop(source, target)
        }
        if (dragging) interactionState.setHoverDetails(null, null, null, null)
        return dragging
    }
}

enum class PauseAction {

    RETURN_TO_GAME,

    RESTART,

    OPEN_SETTINGS,

    MAIN_MENU,

    QUIT_GAME
}
