package com.azizoglu.grisilith.screen.pause

import com.badlogic.gdx.math.Rectangle

data class PauseLayout(
    val fullScreen:
    Rectangle,

    val leftPanel:
    Rectangle,

    val weaponPanel:
    Rectangle,

    val gearPanel:
    Rectangle,

    val itemPanel:
    Rectangle,

    val centerPanel:
    Rectangle,

    val characterStatsPanel:
    Rectangle,

    val weaponSlots:
    List<Rectangle>,

    val armorSlot:
    Rectangle,

    val weaponTamgaSlot:
    Rectangle,

    val armorTamgaSlot:
    Rectangle,

    val returnToGameButton:
    Rectangle,

    val restartButton:
    Rectangle,

    val settingsButton:
    Rectangle,

    val quitGameButton:
    Rectangle,

    /*
     * Ortak detay ve confirmation penceresi.
     */
    val modalPanel:
    Rectangle,

    val modalConfirmButton:
    Rectangle,

    val modalCancelButton:
    Rectangle
) {

    companion object {

        const val WEAPON_SLOT_COUNT =
            6

        const val ITEM_ROW_HEIGHT =
            31f

        private const val ITEM_CONTENT_TOP_PADDING =
            44f

        private const val ITEM_CONTENT_BOTTOM_PADDING =
            14f

        fun create(
            cameraX:
            Float,

            cameraY:
            Float,

            cameraWidth:
            Float,

            cameraHeight:
            Float
        ): PauseLayout {
            val screenLeft =
                cameraX -
                    cameraWidth / 2f

            val screenBottom =
                cameraY -
                    cameraHeight / 2f

            val outerMargin =
                34f

            val panelGap =
                28f

            val leftPanelWidth =
                350f

            val centerPanelWidth =
                300f

            val rightPanelWidth =
                cameraWidth -
                    outerMargin * 2f -
                    panelGap * 2f -
                    leftPanelWidth -
                    centerPanelWidth

            val contentHeight =
                cameraHeight -
                    outerMargin * 2f

            val leftPanel =
                Rectangle(
                    screenLeft +
                        outerMargin,

                    screenBottom +
                        outerMargin,

                    leftPanelWidth,

                    contentHeight
                )

            val centerPanel =
                Rectangle(
                    leftPanel.x +
                        leftPanel.width +
                        panelGap,

                    screenBottom +
                        outerMargin,

                    centerPanelWidth,

                    contentHeight
                )

            val characterStatsPanel =
                Rectangle(
                    centerPanel.x +
                        centerPanel.width +
                        panelGap,

                    screenBottom +
                        outerMargin,

                    rightPanelWidth,

                    contentHeight
                )

            val weaponPanelHeight =
                210f

            val gearPanelHeight =
                125f

            val innerSectionGap =
                8f

            val weaponPanel =
                Rectangle(
                    leftPanel.x,

                    leftPanel.y +
                        leftPanel.height -
                        weaponPanelHeight,

                    leftPanel.width,

                    weaponPanelHeight
                )

            val gearPanel =
                Rectangle(
                    leftPanel.x,

                    weaponPanel.y -
                        innerSectionGap -
                        gearPanelHeight,

                    leftPanel.width,

                    gearPanelHeight
                )

            val itemPanel =
                Rectangle(
                    leftPanel.x,

                    leftPanel.y,

                    leftPanel.width,

                    gearPanel.y -
                        innerSectionGap -
                        leftPanel.y
                )

            val weaponSlots =
                createWeaponSlots(
                    weaponPanel
                )

            val gearSlots =
                createGearSlots(
                    gearPanel
                )

            val centerButtons =
                createCenterButtons(
                    centerPanel
                )

            val modalWidth =
                760f

            val modalHeight =
                390f

            val modalPanel =
                Rectangle(
                    cameraX -
                        modalWidth / 2f,

                    cameraY -
                        modalHeight / 2f,

                    modalWidth,

                    modalHeight
                )

            val modalButtonWidth =
                220f

            val modalButtonHeight =
                58f

            val modalButtonGap =
                35f

            val modalConfirmButton =
                Rectangle(
                    cameraX -
                        modalButtonGap / 2f -
                        modalButtonWidth,

                    modalPanel.y +
                        28f,

                    modalButtonWidth,

                    modalButtonHeight
                )

            val modalCancelButton =
                Rectangle(
                    cameraX +
                        modalButtonGap / 2f,

                    modalPanel.y +
                        28f,

                    modalButtonWidth,

                    modalButtonHeight
                )

            return PauseLayout(
                fullScreen =
                    Rectangle(
                        screenLeft,
                        screenBottom,
                        cameraWidth,
                        cameraHeight
                    ),

                leftPanel =
                    leftPanel,

                weaponPanel =
                    weaponPanel,

                gearPanel =
                    gearPanel,

                itemPanel =
                    itemPanel,

                centerPanel =
                    centerPanel,

                characterStatsPanel =
                    characterStatsPanel,

                weaponSlots =
                    weaponSlots,

                armorSlot =
                    gearSlots[0],

                weaponTamgaSlot =
                    gearSlots[1],

                armorTamgaSlot =
                    gearSlots[2],

                returnToGameButton =
                    centerButtons[0],

                restartButton =
                    centerButtons[1],

                settingsButton =
                    centerButtons[2],

                quitGameButton =
                    centerButtons[3],

                modalPanel =
                    modalPanel,

                modalConfirmButton =
                    modalConfirmButton,

                modalCancelButton =
                    modalCancelButton
            )
        }

        fun maximumVisibleItemRows(
            itemPanel:
            Rectangle
        ): Int {
            val availableHeight =
                itemPanel.height -
                    ITEM_CONTENT_TOP_PADDING -
                    ITEM_CONTENT_BOTTOM_PADDING

            return (
                availableHeight /
                    ITEM_ROW_HEIGHT
                )
                .toInt()
                .coerceAtLeast(
                    1
                )
        }

        fun itemRowRectangles(
            itemPanel:
            Rectangle
        ): List<Rectangle> {
            val rowCount =
                maximumVisibleItemRows(
                    itemPanel
                )

            val rowWidth =
                itemPanel.width -
                    28f

            val firstRowY =
                itemPanel.y +
                    itemPanel.height -
                    ITEM_CONTENT_TOP_PADDING -
                    ITEM_ROW_HEIGHT

            return List(
                rowCount
            ) { rowIndex ->
                Rectangle(
                    itemPanel.x +
                        14f,

                    firstRowY -
                        rowIndex *
                        ITEM_ROW_HEIGHT,

                    rowWidth,

                    ITEM_ROW_HEIGHT -
                        3f
                )
            }
        }

        private fun createWeaponSlots(
            weaponPanel:
            Rectangle
        ): List<Rectangle> {
            val horizontalPadding =
                18f

            val topPadding =
                42f

            val columnGap =
                14f

            val rowGap =
                14f

            val slotWidth =
                (
                    weaponPanel.width -
                        horizontalPadding * 2f -
                        columnGap * 2f
                    ) /
                    3f

            val slotHeight =
                62f

            val topRowY =
                weaponPanel.y +
                    weaponPanel.height -
                    topPadding -
                    slotHeight

            return List(
                WEAPON_SLOT_COUNT
            ) { slotIndex ->
                val column =
                    slotIndex %
                        3

                val row =
                    slotIndex /
                        3

                Rectangle(
                    weaponPanel.x +
                        horizontalPadding +
                        column *
                        (
                            slotWidth +
                                columnGap
                            ),

                    topRowY -
                        row *
                        (
                            slotHeight +
                                rowGap
                            ),

                    slotWidth,

                    slotHeight
                )
            }
        }

        private fun createGearSlots(
            gearPanel:
            Rectangle
        ): List<Rectangle> {
            val horizontalPadding =
                18f

            val gap =
                18f

            val slotWidth =
                (
                    gearPanel.width -
                        horizontalPadding * 2f -
                        gap * 2f
                    ) /
                    3f

            val slotHeight =
                68f

            val slotY =
                gearPanel.y +
                    (
                        gearPanel.height -
                            slotHeight
                        ) /
                    2f -
                    4f

            return List(
                3
            ) { index ->
                Rectangle(
                    gearPanel.x +
                        horizontalPadding +
                        index *
                        (
                            slotWidth +
                                gap
                            ),

                    slotY,

                    slotWidth,

                    slotHeight
                )
            }
        }

        private fun createCenterButtons(
            centerPanel:
            Rectangle
        ): List<Rectangle> {
            val buttonWidth =
                centerPanel.width -
                    30f

            val buttonHeight =
                58f

            val buttonGap =
                24f

            val totalHeight =
                buttonHeight *
                    4f +
                    buttonGap *
                    3f

            val firstButtonY =
                centerPanel.y +
                    (
                        centerPanel.height -
                            totalHeight
                        ) /
                    2f +
                    8f +
                    (
                        buttonHeight +
                            buttonGap
                        ) *
                    3f

            return List(
                4
            ) { index ->
                Rectangle(
                    centerPanel.x +
                        (
                            centerPanel.width -
                                buttonWidth
                            ) /
                        2f,

                    firstButtonY -
                        index *
                        (
                            buttonHeight +
                                buttonGap
                            ),

                    buttonWidth,

                    buttonHeight
                )
            }
        }
    }
}
