package com.azizoglu.grisilith.screen.market

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.azizoglu.grisilith.audio.GameAudio
import com.azizoglu.grisilith.audio.UiSound
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport
import com.badlogic.gdx.InputAdapter
import kotlin.math.abs
import kotlin.math.roundToInt

class MarketInputController(
    private val viewport:
    Viewport,

    private val layoutProvider:
        () -> MarketLayout,

    private val interactionState:
    MarketInteractionState,

    private val itemScrollPixelsProvider:
        () -> Float,

    private val groupedItemCountProvider:
        () -> Int
) : InputAdapter() {

    companion object {

        private const val DRAG_HOLD_SECONDS =
            0.32f

        private const val DRAG_MOVEMENT_THRESHOLD =
            8f

        private const val ITEM_SCROLL_DRAG_THRESHOLD =
            8f
    }

    private val pointerPosition =
        Vector2()

    private val weaponPressStartPosition =
        Vector2()

    private val marketOfferPressStartPosition =
        Vector2()

    private var weaponPressSeconds =
        0f

    private var weaponDragStarted =
        false

    private var marketOfferPressSeconds =
        0f

    private var marketOfferDragStarted =
        false

    private var pendingWheelScroll =
        0

    private var itemPanelPointerActive =
        false

    private var itemPanelPointerScrolled =
        false

    private val itemPanelPressPosition =
        Vector2()

    private var itemPanelLastPointerY =
        0f


    override fun scrolled(
        amountX: Float,
        amountY: Float
    ): Boolean {
        val scrollAmount =
            amountY.roundToInt()

        if (scrollAmount == 0) {
            return false
        }

        pendingWheelScroll +=
            scrollAmount

        return true
    }

    fun handleInput(
        delta: Float,

        onPurchaseOffer:
            (Int) -> Unit,

        onMarketOfferDropped:
            (
            offerIndex: Int,
            targetWeaponSlotIndex: Int?,
            droppedOnItemPanel: Boolean,
            droppedOnStatsPanel: Boolean
        ) -> Unit,

        onToggleOfferLock:
            (Int) -> Unit,

        onReturnToGame:
            () -> Unit,

        onReroll:
            () -> Unit,

        canDragWeapon:
            (Int) -> Boolean,

        onWeaponClicked:
            (Int) -> Unit,

        onItemClicked:
            (Int) -> Unit,

        onItemScroll:
            (Float) -> Unit,

        onTamgaClicked:
            () -> Unit,

        onCloseDetailModal:
            () -> Unit,

        onWeaponDropped:
            (
            sourceSlotIndex: Int,
            targetSlotIndex: Int?,
            droppedOnSellArea: Boolean
        ) -> Unit,

        onConfirmMerge:
            () -> Unit,

        onCancelMerge:
            () -> Unit,

        onConfirmSale:
            () -> Unit,

        onCancelSale:
            () -> Unit
    ) {
        updatePointerPosition()
        interactionState.updatePointer(pointerPosition.x, pointerPosition.y)

        /*
         * Merge onayı açıkken alttaki market
         * etkileşimleri devre dışı kalır.
         */
        if (
            interactionState
                .isMergeConfirmationOpen
        ) {
            handleMergeConfirmationInput(
                onConfirmMerge =
                    onConfirmMerge,

                onCancelMerge =
                    onCancelMerge
            )

            return
        }

        if (
            interactionState
                .isSaleConfirmationOpen
        ) {
            handleSaleConfirmationInput(
                onConfirmSale =
                    onConfirmSale,

                onCancelSale =
                    onCancelSale
            )

            return
        }

        /*
         * Önce market kartlarını kontrol ediyoruz.
         */
        /*
 * Weapon, Item veya Tamga detayı açıkken
 * arkadaki market tamamen kilitlenir.
 *
 * Bu kontrol market kartlarından önce olmak
 * zorunda. Böylece modalı kapatan tıklama,
 * arkadaki ürünü satın alamaz.
 */
        if (
            interactionState
                .isDetailModalOpen
        ) {
            handleDetailModalInput(
                onCloseDetailModal =
                    onCloseDetailModal
            )

            return
        }

        /*
         * Item paneli; tıklama, fare tekerleği ve
         * mobil sürükleme işlemlerini yönetir.
         */
        val itemPanelConsumedInput =
            handleItemPanelPointerInput(
                onItemClicked =
                    onItemClicked,

                onItemScroll =
                    onItemScroll
            )

        if (itemPanelConsumedInput) {
            return
        }

        /*
         * Market teklif kartlarını kontrol eder.
         */
        handleMarketOfferPointerInput(
            delta =
                delta,

            onMarketOfferClicked =
                onPurchaseOffer,

            onMarketOfferDropped =
                onMarketOfferDropped
        )

        if (
            interactionState
                .pressedMarketOfferIndex !=
            null ||
            interactionState
                .isDraggingMarketOffer
        ) {
            return
        }

        /*
         * Sonra envanterdeki Weapon slotlarını
         * kontrol ediyoruz.
         */
        handleWeaponPointerInput(
            delta =
                delta,

            canDragWeapon =
                canDragWeapon,

            onWeaponClicked =
                onWeaponClicked,

            onWeaponDropped =
                onWeaponDropped
        )

        if (
            interactionState
                .pressedWeaponSlotIndex !=
            null ||
            interactionState
                .isDraggingWeapon
        ) {
            return
        }

        handleNormalPointerInput(
            onToggleOfferLock =
                onToggleOfferLock,

            onReturnToGame =
                onReturnToGame,

            onReroll =
                onReroll,

            onTamgaClicked =
                onTamgaClicked
        )

        handleRerollKeyboardInput(
            onReroll =
                onReroll
        )

    }

    private fun handleMarketOfferPointerInput(
        delta: Float,

        onMarketOfferClicked:
            (Int) -> Unit,

        onMarketOfferDropped:
            (
            offerIndex: Int,
            targetWeaponSlotIndex: Int?,
            droppedOnItemPanel: Boolean,
            droppedOnStatsPanel: Boolean
        ) -> Unit
    ) {
        val layout =
            layoutProvider()

        val pointerJustPressed =
            isPrimaryPointerJustPressed()

        val pointerHeld =
            isPrimaryPointerHeld()

        val pointerJustReleased =
            isMarketOfferPointerJustReleased()

        /*
         * Market kartına ilk basış.
         */
        if (
            pointerJustPressed &&
            interactionState
                .pressedMarketOfferIndex ==
            null
        ) {
            val pressedOfferIndex =
                layout.offerCards
                    .indexOfFirst { card ->
                        card.contains(
                            pointerPosition
                        )
                    }

            if (pressedOfferIndex >= 0) {
                /*
                 * Lock alanına basmak kart
                 * etkileşimini başlatmaz.
                 */
                val pressedOnLock =
                    layout.lockButtons[
                        pressedOfferIndex
                    ]
                        .contains(
                            pointerPosition
                        )

                if (!pressedOnLock) {
                    interactionState
                        .beginMarketOfferPress(
                            offerIndex =
                                pressedOfferIndex,

                            pointerX =
                                pointerPosition.x,

                            pointerY =
                                pointerPosition.y
                        )

                    marketOfferPressStartPosition
                        .set(
                            pointerPosition
                        )

                    marketOfferPressSeconds = 0f
                    marketOfferDragStarted = false

                    return
                }
            }
        }

        val pressedOfferIndex =
            interactionState
                .pressedMarketOfferIndex
                ?: return

        interactionState.updatePointer(
            pointerX =
                pointerPosition.x,

            pointerY =
                pointerPosition.y
        )

        if (
            pointerHeld &&
            !marketOfferDragStarted
        ) {
            marketOfferPressSeconds +=
                delta.coerceAtLeast(0f)

            val movedDistance =
                pointerPosition.dst(
                    marketOfferPressStartPosition
                )

            if (movedDistance >= DRAG_MOVEMENT_THRESHOLD) {
                marketOfferDragStarted = true

                interactionState
                    .beginMarketOfferDrag(
                        pressedOfferIndex
                    )
            }
        }

        if (!pointerJustReleased) {
            return
        }

        if (marketOfferDragStarted) {
            val targetWeaponSlotIndex =
                layout.weaponSlots
                    .indexOfFirst { weaponSlot ->
                        weaponSlot.contains(
                            pointerPosition
                        )
                    }
                    .takeIf { index ->
                        index >= 0
                    }

            val droppedOnItemPanel =
                layout.itemPanel.contains(
                    pointerPosition
                )

            val droppedOnStatsPanel =
                layout.statsPanel.contains(
                    pointerPosition
                )

            onMarketOfferDropped(
                pressedOfferIndex,
                targetWeaponSlotIndex,
                droppedOnItemPanel,
                droppedOnStatsPanel
            )
        } else {
            onMarketOfferClicked(
                pressedOfferIndex
            )
        }

        clearMarketOfferPointerState()
    }

    private fun handleWeaponPointerInput(
        delta: Float,

        canDragWeapon:
            (Int) -> Boolean,

        onWeaponClicked:
            (Int) -> Unit,

        onWeaponDropped:
            (
            sourceSlotIndex: Int,
            targetSlotIndex: Int?,
            droppedOnSellArea: Boolean
        ) -> Unit
    ) {
        val layout =
            layoutProvider()

        val pointerJustPressed =
            isPrimaryPointerJustPressed()

        val pointerHeld =
            isPrimaryPointerHeld()

        val pointerJustReleased =
            isWeaponPointerJustReleased()

        if (
            pointerJustPressed &&
            interactionState
                .pressedWeaponSlotIndex ==
            null
        ) {
            val pressedSlotIndex =
                layout.weaponSlots
                    .indexOfFirst { slot ->
                        slot.contains(
                            pointerPosition
                        )
                    }

            if (
                pressedSlotIndex >= 0 &&
                canDragWeapon(
                    pressedSlotIndex
                )
            ) {
                interactionState
                    .beginWeaponPress(
                        slotIndex =
                            pressedSlotIndex,

                        pointerX =
                            pointerPosition.x,

                        pointerY =
                            pointerPosition.y
                    )

                weaponPressStartPosition.set(
                    pointerPosition
                )

                weaponPressSeconds = 0f
                weaponDragStarted = false

                return
            }
        }

        val pressedSlotIndex =
            interactionState
                .pressedWeaponSlotIndex
                ?: return

        interactionState.updatePointer(
            pointerX =
                pointerPosition.x,

            pointerY =
                pointerPosition.y
        )

        if (
            pointerHeld &&
            !weaponDragStarted
        ) {
            weaponPressSeconds +=
                delta.coerceAtLeast(0f)

            val movedDistance =
                pointerPosition.dst(
                    weaponPressStartPosition
                )

            if (movedDistance >= DRAG_MOVEMENT_THRESHOLD) {
                weaponDragStarted = true

                interactionState
                    .beginWeaponDrag(
                        pressedSlotIndex
                    )
            }
        }

        if (!pointerJustReleased) {
            return
        }

        if (weaponDragStarted) {
            val targetSlotIndex =
                layout.weaponSlots
                    .indexOfFirst { slot ->
                        slot.contains(
                            pointerPosition
                        )
                    }
                    .takeIf { index ->
                        index >= 0
                    }

            val droppedOnSellArea =
                layout.weaponSellArea
                    .contains(
                        pointerPosition
                    )

            onWeaponDropped(
                pressedSlotIndex,
                targetSlotIndex,
                droppedOnSellArea
            )
        } else {
            onWeaponClicked(
                pressedSlotIndex
            )
        }

        clearWeaponPointerState()
    }

    private fun handleNormalPointerInput(
        onToggleOfferLock:
            (Int) -> Unit,

        onReturnToGame:
            () -> Unit,

        onReroll:
            () -> Unit,

        onTamgaClicked:
            () -> Unit
    ) {
        if (
            !isPrimaryPointerJustPressed()
        ) {
            return
        }

        val layout =
            layoutProvider()

        if (
            layout.returnButton.contains(
                pointerPosition
            )
        ) {
            onReturnToGame()
            GameAudio.play(UiSound.CLICK)
            return
        }

        if (
            layout.goldPanel.contains(
                pointerPosition
            )
        ) {
            onReroll()
            GameAudio.play(UiSound.CLICK)
            return
        }

        if (
            layout.tamgaPanel.contains(
                pointerPosition
            )
        ) {
            onTamgaClicked()
            GameAudio.play(UiSound.CLICK)
            return
        }

        layout.lockButtons
            .forEachIndexed {
                    index,
                    rectangle ->

                if (
                    rectangle.contains(
                        pointerPosition
                    )
                ) {
                    onToggleOfferLock(
                        index
                    )

                    return
                }
            }
    }

    private fun handleRerollKeyboardInput(
        onReroll:
            () -> Unit
    ) {
        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.R
            )
        ) {
            onReroll()
            GameAudio.play(UiSound.CLICK)
        }
    }

    private fun handleItemPanelPointerInput(
        onItemClicked:
            (Int) -> Unit,

        onItemScroll:
            (Float) -> Unit
    ): Boolean {
        val layout =
            layoutProvider()

        /*
         * Fare tekerleği yalnızca imleç Item
         * panelinin üzerindeyken kullanılabilir.
         */
        if (
            pendingWheelScroll != 0
        ) {
            val requestedScroll =
                pendingWheelScroll

            pendingWheelScroll = 0

            if (
                layout.itemPanel.contains(
                    pointerPosition
                )
            ) {
                onItemScroll(requestedScroll * 72f)

                return true
            }
        }

        val pointerJustPressed =
            isPrimaryPointerJustPressed()

        val pointerHeld =
            isPrimaryPointerHeld()

        /*
         * Item paneline ilk basış.
         */
        if (
            pointerJustPressed &&
            layout.itemPanel.contains(
                pointerPosition
            )
        ) {
            itemPanelPointerActive = true
            itemPanelPointerScrolled = false

            itemPanelPressPosition.set(
                pointerPosition
            )

            itemPanelLastPointerY =
                pointerPosition.y

            return true
        }

        if (!itemPanelPointerActive) {
            return false
        }

        /*
         * Pointer basılı tutulurken listeyi
         * yukarı veya aşağı kaydır.
         */
        if (pointerHeld) {
            val totalMovement =
                abs(
                    pointerPosition.y -
                        itemPanelPressPosition.y
                )

            if (
                totalMovement >=
                ITEM_SCROLL_DRAG_THRESHOLD
            ) {
                itemPanelPointerScrolled = true
            }

            val movementSinceLastStep =
                pointerPosition.y -
                    itemPanelLastPointerY

            if (movementSinceLastStep != 0f) {
                onItemScroll(movementSinceLastStep)
                itemPanelLastPointerY = pointerPosition.y
            }

            return true
        }

        /*
         * Pointer bırakıldı.
         *
         * Kaydırma yapılmadıysa bu işlem kısa
         * dokunma olarak kabul edilir ve Item
         * açıklaması açılır.
         */
        if (!itemPanelPointerScrolled) {
            val visibleRowIndex =
                visibleItemRowAt(
                    layout =
                        layout,

                    pointerX =
                        pointerPosition.x,

                    pointerY =
                        pointerPosition.y
                )

            if (visibleRowIndex != null) {
                onItemClicked(
                    visibleRowIndex
                )
            }
        }

        clearItemPanelPointerState()

        return true
    }

    private fun visibleItemRowAt(
        layout: MarketLayout,
        pointerX: Float,
        pointerY: Float
    ): Int? {
        return MarketLayout.itemIndexAt(
            layout.itemPanel,
            groupedItemCountProvider(),
            itemScrollPixelsProvider(),
            pointerX,
            pointerY
        )
    }

    private fun clearItemPanelPointerState() {
        itemPanelPointerActive = false
        itemPanelPointerScrolled = false

        itemPanelPressPosition.setZero()

        itemPanelLastPointerY = 0f
    }

    private fun handleDetailModalInput(
        onCloseDetailModal:
            () -> Unit
    ) {
        if (
            !isPrimaryPointerJustPressed()
        ) {
            return
        }

        val layout =
            layoutProvider()

        /*
         * Açıklama panelinin içine tıklanırsa
         * hiçbir şey olmaz.
         */
        if (
            layout.weaponDetailPanel.contains(
                pointerPosition
            )
        ) {
            return
        }

        /*
         * Panel dışındaki herhangi bir tıklama
         * yalnızca açıklamayı kapatır.
         */
        onCloseDetailModal()
    }

    private fun handleMergeConfirmationInput(
        onConfirmMerge:
            () -> Unit,

        onCancelMerge:
            () -> Unit
    ) {
        if (
            !isPrimaryPointerJustPressed()
        ) {
            return
        }

        val layout =
            layoutProvider()

        when {
            layout.mergeConfirmButton
                .contains(
                    pointerPosition
                ) -> {
                onConfirmMerge()
                GameAudio.play(UiSound.CONFIRM)
            }

            layout.mergeCancelButton
                .contains(
                    pointerPosition
                ) -> {
                onCancelMerge()
                GameAudio.play(UiSound.CLICK)
            }
        }
    }

    private fun handleSaleConfirmationInput(
        onConfirmSale:
            () -> Unit,

        onCancelSale:
            () -> Unit
    ) {
        if (
            !isPrimaryPointerJustPressed()
        ) {
            return
        }

        val layout =
            layoutProvider()

        when {
            layout.mergeConfirmButton
                .contains(
                    pointerPosition
                ) -> {
                onConfirmSale()
                GameAudio.play(UiSound.CONFIRM)
            }

            layout.mergeCancelButton
                .contains(
                    pointerPosition
                ) -> {
                onCancelSale()
                GameAudio.play(UiSound.CLICK)
            }
        }
    }

    private fun updatePointerPosition() {
        pointerPosition.set(
            Gdx.input.x.toFloat(),
            Gdx.input.y.toFloat()
        )

        viewport.unproject(
            pointerPosition
        )

        interactionState.updatePointer(
            pointerX =
                pointerPosition.x,

            pointerY =
                pointerPosition.y
        )
    }

    private fun isPrimaryPointerJustPressed():
        Boolean {

        return if (
            Gdx.app.type ==
            Application.ApplicationType.Desktop
        ) {
            Gdx.input.isButtonJustPressed(
                Input.Buttons.LEFT
            )
        } else {
            Gdx.input.justTouched()
        }
    }

    private fun isPrimaryPointerHeld():
        Boolean {

        return if (
            Gdx.app.type ==
            Application.ApplicationType.Desktop
        ) {
            Gdx.input.isButtonPressed(
                Input.Buttons.LEFT
            )
        } else {
            Gdx.input.isTouched
        }
    }

    private fun isWeaponPointerJustReleased():
        Boolean {

        return interactionState
            .pressedWeaponSlotIndex !=
            null &&
            !isPrimaryPointerHeld()
    }

    private fun isMarketOfferPointerJustReleased():
        Boolean {

        return interactionState
            .pressedMarketOfferIndex !=
            null &&
            !isPrimaryPointerHeld()
    }

    private fun clearWeaponPointerState() {
        interactionState
            .finishPointerInteraction()

        weaponPressSeconds = 0f
        weaponDragStarted = false

        weaponPressStartPosition.setZero()
    }

    private fun clearMarketOfferPointerState() {
        interactionState
            .finishMarketOfferInteraction()

        marketOfferPressSeconds = 0f
        marketOfferDragStarted = false

        marketOfferPressStartPosition.setZero()
    }

    fun reset() {
        pointerPosition.setZero()

        weaponPressStartPosition.setZero()
        marketOfferPressStartPosition.setZero()

        weaponPressSeconds = 0f
        weaponDragStarted = false

        marketOfferPressSeconds = 0f
        marketOfferDragStarted = false

        pendingWheelScroll = 0

        clearItemPanelPointerState()

        interactionState.reset()
    }
}
