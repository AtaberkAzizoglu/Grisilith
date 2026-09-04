package com.azizoglu.grisilith.screen.market

import com.badlogic.gdx.math.Rectangle

data class MarketLayout(
    val fullScreen: Rectangle,

    val stagePanel: Rectangle,
    val goldPanel: Rectangle,
    val returnButton: Rectangle,

    val offerCards: List<Rectangle>,
    val lockButtons: List<Rectangle>,

    val weaponSellArea: Rectangle,

    val weaponPanel: Rectangle,
    val weaponSlots: List<Rectangle>,

    val itemPanel: Rectangle,

    val characterPanel: Rectangle,
    val statsPanel: Rectangle,
    val tamgaPanel: Rectangle,

    val weaponDetailPanel: Rectangle,

    val mergeConfirmationPanel:
    Rectangle,

    val mergeConfirmButton:
    Rectangle,

    val mergeCancelButton:
    Rectangle
) {

    fun itemRowRectangle(
        visibleRowIndex: Int
    ): Rectangle {
        val rowHeight =
            24f

        val firstRowTop =
            itemPanel.y +
                itemPanel.height -
                31f

        return Rectangle(
            itemPanel.x +
                8f,

            firstRowTop -
                rowHeight *
                (
                    visibleRowIndex +
                        1
                    ),

            itemPanel.width -
                16f,

            rowHeight
        )
    }

    companion object {

        private const val SCREEN_PADDING =
            20f

        private const val PANEL_GAP =
            12f

        fun create(
            cameraX: Float,
            cameraY: Float,
            cameraWidth: Float,
            cameraHeight: Float
        ): MarketLayout {

            val screenLeft =
                cameraX -
                    cameraWidth / 2f

            val screenBottom =
                cameraY -
                    cameraHeight / 2f

            val usableWidth =
                cameraWidth -
                    SCREEN_PADDING * 2f

            /*
             * Sağdaki Character / Stats /
             * Tamga kolonunun genişliği.
             */
            val rightColumnWidth =
                285f

            val mainColumnWidth =
                usableWidth -
                    rightColumnWidth -
                    PANEL_GAP

            val mainColumnX =
                screenLeft +
                    SCREEN_PADDING

            val rightColumnX =
                mainColumnX +
                    mainColumnWidth +
                    PANEL_GAP

            /*
             * Üst bilgi satırı.
             */
            val topBarY =
                screenBottom +
                    cameraHeight -
                    SCREEN_PADDING -
                    44f

            val stagePanel =
                Rectangle(
                    mainColumnX,
                    topBarY,
                    270f,
                    44f
                )

            val returnButton =
                Rectangle(
                    rightColumnX,
                    topBarY,
                    rightColumnWidth,
                    44f
                )

            val goldPanel =
                Rectangle(
                    stagePanel.x +
                        stagePanel.width +
                        PANEL_GAP,
                    topBarY,
                    mainColumnWidth -
                        stagePanel.width -
                        PANEL_GAP,
                    44f
                )

            /*
             * Beş market kartı.
             */
            val offerAreaY =
                topBarY -
                    PANEL_GAP -
                    174f

            val offerCardGap =
                10f

            val offerCardWidth =
                (
                    mainColumnWidth -
                        offerCardGap * 4f
                    ) / 5f

            val offerCards =
                List(5) { index ->
                    Rectangle(
                        mainColumnX +
                            index *
                            (
                                offerCardWidth +
                                    offerCardGap
                                ),
                        offerAreaY,
                        offerCardWidth,
                        174f
                    )
                }

            val weaponSellArea =
                Rectangle(
                    mainColumnX,
                    offerAreaY,
                    mainColumnWidth,
                    174f
                )

            val lockButtons =
                offerCards.map { card ->
                    Rectangle(
                        card.x + 10f,
                        card.y + 9f,
                        card.width - 20f,
                        29f
                    )
                }

            /*
             * Weapon paneli.
             */
            val weaponPanel =
                Rectangle(
                    mainColumnX,
                    offerAreaY -
                        PANEL_GAP -
                        202f,
                    mainColumnWidth,
                    202f
                )

            val weaponSlotGap =
                10f

            val weaponSlotWidth =
                (
                    weaponPanel.width -
                        40f -
                        weaponSlotGap * 2f
                    ) / 3f

            val weaponSlotHeight =
                68f

            val weaponSlots =
                List(6) { index ->
                    val column =
                        index % 3

                    val row =
                        index / 3

                    Rectangle(
                        weaponPanel.x +
                            20f +
                            column *
                            (
                                weaponSlotWidth +
                                    weaponSlotGap
                                ),

                        weaponPanel.y +
                            weaponPanel.height -
                            50f -
                            weaponSlotHeight -
                            row *
                            (
                                weaponSlotHeight +
                                    12f
                                ),

                        weaponSlotWidth,
                        weaponSlotHeight
                    )
                }

            /*
             * Item listesi.
             */
            val itemPanel =
                Rectangle(
                    mainColumnX,
                    screenBottom +
                        SCREEN_PADDING,
                    mainColumnWidth,
                    weaponPanel.y -
                        screenBottom -
                        SCREEN_PADDING -
                        PANEL_GAP
                )

            /*
             * Sağ kolon.
             */
            val characterPanel =
                Rectangle(
                    rightColumnX,
                    offerAreaY,
                    rightColumnWidth,
                    174f
                )

            /*
 * Tamga alanı başlık + tek bilgi satırı için
 * yeterli olacak kadar küçük tutulur.
 */
            val tamgaPanelHeight =
                62f

            val tamgaPanel =
                Rectangle(
                    rightColumnX,
                    screenBottom +
                        SCREEN_PADDING,
                    rightColumnWidth,
                    tamgaPanelHeight
                )

            /*
             * Tamga'dan kazanılan alan tamamen Stats
             * paneline verilir.
             */
            val statsPanelBottom =
                tamgaPanel.y +
                    tamgaPanel.height +
                    PANEL_GAP

            val statsPanel =
                Rectangle(
                    rightColumnX,
                    statsPanelBottom,
                    rightColumnWidth,
                    characterPanel.y -
                        statsPanelBottom -
                        PANEL_GAP
                )

            val popupWidth =
                520f

            val popupHeight =
                250f

            val popupX =
                screenLeft +
                    (
                        cameraWidth -
                            popupWidth
                        ) / 2f

            val popupY =
                screenBottom +
                    (
                        cameraHeight -
                            popupHeight
                        ) / 2f

            val weaponDetailPanel =
                Rectangle(
                    popupX,
                    popupY,
                    popupWidth,
                    popupHeight
                )

            val mergeConfirmationPanel =
                Rectangle(
                    popupX,
                    popupY,
                    popupWidth,
                    popupHeight
                )

            val popupButtonWidth =
                180f

            val popupButtonHeight =
                42f

            val mergeConfirmButton =
                Rectangle(
                    popupX + 55f,
                    popupY + 25f,
                    popupButtonWidth,
                    popupButtonHeight
                )

            val mergeCancelButton =
                Rectangle(
                    popupX +
                        popupWidth -
                        popupButtonWidth -
                        55f,

                    popupY + 25f,
                    popupButtonWidth,
                    popupButtonHeight
                )

            return MarketLayout(
                fullScreen =
                    Rectangle(
                        screenLeft,
                        screenBottom,
                        cameraWidth,
                        cameraHeight
                    ),

                stagePanel =
                    stagePanel,

                goldPanel =
                    goldPanel,

                returnButton =
                    returnButton,

                offerCards =
                    offerCards,

                lockButtons =
                    lockButtons,

                weaponSellArea =
                    weaponSellArea,

                weaponPanel =
                    weaponPanel,

                weaponSlots =
                    weaponSlots,

                itemPanel =
                    itemPanel,

                characterPanel =
                    characterPanel,

                statsPanel =
                    statsPanel,

                tamgaPanel =
                    tamgaPanel,

                weaponDetailPanel =
                    weaponDetailPanel,

                mergeConfirmationPanel =
                    mergeConfirmationPanel,

                mergeConfirmButton =
                    mergeConfirmButton,

                mergeCancelButton =
                    mergeCancelButton
            )
        }
    }
}
