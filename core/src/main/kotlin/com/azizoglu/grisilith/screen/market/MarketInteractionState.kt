package com.azizoglu.grisilith.screen.market

import com.badlogic.gdx.math.Vector2

class MarketInteractionState {


    data class MarketFailureFeedback(
        val message: String,

        val offerIndex: Int?,

        var remainingSeconds: Float
    )

    data class PendingWeaponMerge(
        val sourceSlotIndex: Int,
        val targetSlotIndex: Int,
        val sourceWeaponName: String,
        val sourceWeaponLevel: Int,
        val targetWeaponLevel: Int,
        val resultWeaponLevel: Int
    )

    data class PendingWeaponSale(
        val sourceSlotIndex: Int,
        val weaponName: String,
        val weaponLevel: Int,
        val salePrice: Int
    )

    /*
     * Kısa tıklamayla detayları gösterilen
     * Weapon slotu.
     */
    var detailedWeaponSlotIndex:
        Int? = null

    /*
 * Item listesinde detayları açık olan
 * ItemDefinition'ın ContentId.value değeri.
 */
    var detailedItemDefinitionId:
        String? = null

    /*
     * Tamga sistemi tamamlandığında gerçek Tamga
     * detayını gösterecek. Şimdilik placeholder.
     */
    var isTamgaDetailOpen:
        Boolean = false

    /*
     * Parmağın veya mouse'un ilk basıldığı slot.
     */
    var pressedWeaponSlotIndex:
        Int? = null

    /*
     * Aktif olarak sürüklenen kaynak slot.
     */
    var draggedWeaponSlotIndex:
        Int? = null

    val pointerPosition =
        Vector2()

    var pressedMarketOfferIndex:
        Int? = null

    var draggedMarketOfferIndex:
        Int? = null

    val isDraggingMarketOffer: Boolean
        get() =
            draggedMarketOfferIndex != null

    var pendingWeaponMerge:
        PendingWeaponMerge? = null

    var pendingWeaponSale:
        PendingWeaponSale? = null

    var marketFailureFeedback:
        MarketFailureFeedback? = null

    val isDraggingWeapon: Boolean
        get() =
            draggedWeaponSlotIndex != null

    val isMergeConfirmationOpen: Boolean
        get() =
            pendingWeaponMerge != null

    val isSaleConfirmationOpen: Boolean
        get() =
            pendingWeaponSale != null

    val isAnyConfirmationOpen: Boolean
        get() =
            isMergeConfirmationOpen ||
                isSaleConfirmationOpen

    val isDetailModalOpen: Boolean
        get() =
            detailedWeaponSlotIndex != null ||
                detailedItemDefinitionId != null ||
                isTamgaDetailOpen


    fun openSaleConfirmation(
        pendingSale:
        PendingWeaponSale
    ) {
        pendingWeaponSale =
            pendingSale

        detailedWeaponSlotIndex =
            null
    }

    fun closeSaleConfirmation() {
        pendingWeaponSale = null
    }

    fun showFailureFeedback(
        message: String,
        offerIndex: Int? = null
    ) {
        marketFailureFeedback =
            MarketFailureFeedback(
                message =
                    message,

                offerIndex =
                    offerIndex,

                remainingSeconds =
                    1.6f
            )
    }

    fun update(
        delta: Float
    ) {
        val feedback =
            marketFailureFeedback
                ?: return

        feedback.remainingSeconds -=
            delta.coerceAtLeast(0f)

        if (
            feedback.remainingSeconds <= 0f
        ) {
            marketFailureFeedback = null
        }
    }

    fun beginWeaponPress(
        slotIndex: Int,
        pointerX: Float,
        pointerY: Float
    ) {
        pressedWeaponSlotIndex =
            slotIndex

        pointerPosition.set(
            pointerX,
            pointerY
        )
    }

    fun beginMarketOfferPress(
        offerIndex: Int,
        pointerX: Float,
        pointerY: Float
    ) {
        pressedMarketOfferIndex =
            offerIndex

        pointerPosition.set(
            pointerX,
            pointerY
        )
    }

    fun beginMarketOfferDrag(
        offerIndex: Int
    ) {
        draggedMarketOfferIndex =
            offerIndex
    }

    fun finishMarketOfferInteraction() {
        pressedMarketOfferIndex = null
        draggedMarketOfferIndex = null
    }

    fun beginWeaponDrag(
        slotIndex: Int
    ) {
        draggedWeaponSlotIndex =
            slotIndex

        detailedWeaponSlotIndex =
            null
    }

    fun updatePointer(
        pointerX: Float,
        pointerY: Float
    ) {
        pointerPosition.set(
            pointerX,
            pointerY
        )
    }

    fun showWeaponDetails(
        slotIndex: Int
    ) {
        detailedWeaponSlotIndex =
            slotIndex

        detailedItemDefinitionId = null
        isTamgaDetailOpen = false
    }

    fun openMergeConfirmation(
        pendingMerge:
        PendingWeaponMerge
    ) {
        pendingWeaponMerge =
            pendingMerge

        detailedWeaponSlotIndex =
            null
    }

    fun closeMergeConfirmation() {
        pendingWeaponMerge = null
    }

    fun finishPointerInteraction() {
        pressedWeaponSlotIndex = null
        draggedWeaponSlotIndex = null
    }

    fun reset() {
        detailedWeaponSlotIndex = null
        detailedItemDefinitionId = null
        isTamgaDetailOpen = false

        pressedWeaponSlotIndex = null
        draggedWeaponSlotIndex = null

        pressedMarketOfferIndex = null
        draggedMarketOfferIndex = null

        pendingWeaponMerge = null
        pendingWeaponSale = null

        marketFailureFeedback = null

        pointerPosition.setZero()
    }

    fun showItemDetails(
        itemDefinitionId: String
    ) {
        detailedItemDefinitionId =
            itemDefinitionId

        detailedWeaponSlotIndex = null
        isTamgaDetailOpen = false
    }

    fun showTamgaDetails() {
        isTamgaDetailOpen = true

        detailedWeaponSlotIndex = null
        detailedItemDefinitionId = null
    }

    fun closeDetailModal() {
        detailedWeaponSlotIndex = null
        detailedItemDefinitionId = null
        isTamgaDetailOpen = false
    }
}
