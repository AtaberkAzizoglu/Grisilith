package com.azizoglu.grisilith.screen.market

import com.badlogic.gdx.math.Rectangle
import kotlin.math.ceil

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
    val characterPortrait: Rectangle,
    val weaponDetailPanel: Rectangle,
    val mergeConfirmationPanel: Rectangle,
    val mergeConfirmButton: Rectangle,
    val mergeCancelButton: Rectangle
) {
    companion object {
        const val ITEM_COLUMNS = 5
        const val ITEM_SLOT_SIZE = 78f
        private const val ITEM_COLUMN_GAP = 24f
        private const val ITEM_ROW_GAP = 38f
        private const val ITEM_TOP_PADDING = 70f
        private const val ITEM_BOTTOM_PADDING = 22f

        fun create(cameraX: Float, cameraY: Float, cameraWidth: Float, cameraHeight: Float): MarketLayout {
            val left = cameraX - cameraWidth / 2f
            val bottom = cameraY - cameraHeight / 2f
            val padding = 24f
            val gap = 10f
            val rightWidth = 470f
            val mainWidth = cameraWidth - padding * 2f - gap - rightWidth
            val mainX = left + padding
            val rightX = mainX + mainWidth + gap
            val topY = bottom + cameraHeight - padding - 58f

            val stagePanel = Rectangle(mainX, topY, 230f, 58f)
            val goldPanel = Rectangle(mainX + mainWidth - 285f, topY, 285f, 58f)
            val returnButton = Rectangle(rightX + 20f, bottom + padding, rightWidth - 40f, 64f)

            val offerArea = Rectangle(mainX, topY - gap - 218f, mainWidth, 218f)
            val offerWidth = offerArea.width / 5f
            val offerCards = List(5) { index ->
                Rectangle(offerArea.x + index * offerWidth, offerArea.y, offerWidth, offerArea.height)
            }
            val lockButtons = offerCards.map { card ->
                Rectangle(card.x + 24f, card.y + 18f, card.width - 48f, 38f)
            }

            val contentY = bottom + padding
            val contentHeight = offerArea.y - gap - contentY
            val weaponPanelWidth = 270f
            val weaponPanel = Rectangle(mainX, contentY, weaponPanelWidth, contentHeight)
            val itemPanel = Rectangle(mainX + weaponPanelWidth + gap, contentY, mainWidth - weaponPanelWidth - gap, contentHeight)

            val slotSize = ITEM_SLOT_SIZE
            val slotGapX = 28f
            val slotGapY = 39f
            val totalSlotWidth = slotSize * 2f + slotGapX
            val totalSlotHeight = slotSize * 3f + slotGapY * 2f
            val slotStartX = weaponPanel.x + (weaponPanel.width - totalSlotWidth) / 2f
            val slotStartY = weaponPanel.y + weaponPanel.height - 48f - slotSize
            val weaponSlots = List(6) { index ->
                val column = index % 2
                val row = index / 2
                Rectangle(
                    slotStartX + column * (slotSize + slotGapX),
                    slotStartY - row * (slotSize + slotGapY),
                    slotSize,
                    slotSize
                )
            }

            val characterPanel = Rectangle(
                rightX,
                returnButton.y + returnButton.height + gap,
                rightWidth,
                topY + 48f - (returnButton.y + returnButton.height + gap)
            )
            val portraitSize = 142f
            val characterPortrait = Rectangle(
                characterPanel.x + (characterPanel.width - portraitSize) / 2f,
                characterPanel.y + characterPanel.height - portraitSize - 28f,
                portraitSize,
                portraitSize
            )
            val statsPanel = Rectangle(
                characterPanel.x + 8f,
                characterPanel.y + 10f,
                characterPanel.width - 16f,
                characterPortrait.y - characterPanel.y - 24f
            )

            val popupWidth = 520f
            val popupHeight = 250f
            val popupX = left + (cameraWidth - popupWidth) / 2f
            val popupY = bottom + (cameraHeight - popupHeight) / 2f
            val modal = Rectangle(popupX, popupY, popupWidth, popupHeight)
            val buttonWidth = 220f
            val buttonHeight = 56f

            return MarketLayout(
                Rectangle(left, bottom, cameraWidth, cameraHeight), stagePanel, goldPanel, returnButton,
                offerCards, lockButtons, offerArea, weaponPanel, weaponSlots, itemPanel, characterPanel,
                statsPanel, Rectangle(-10_000f, -10_000f, 0f, 0f), characterPortrait, modal,
                Rectangle(modal), Rectangle(popupX + 28f, popupY + 22f, buttonWidth, buttonHeight),
                Rectangle(popupX + popupWidth - buttonWidth - 28f, popupY + 22f, buttonWidth, buttonHeight)
            )
        }

        fun itemSlotRectangle(panel: Rectangle, itemIndex: Int, scrollPixels: Float): Rectangle {
            val gridWidth = ITEM_SLOT_SIZE * ITEM_COLUMNS + ITEM_COLUMN_GAP * (ITEM_COLUMNS - 1)
            val startX = panel.x + (panel.width - gridWidth) / 2f
            val firstY = panel.y + panel.height - ITEM_TOP_PADDING - ITEM_SLOT_SIZE
            val column = itemIndex % ITEM_COLUMNS
            val row = itemIndex / ITEM_COLUMNS
            return Rectangle(
                startX + column * (ITEM_SLOT_SIZE + ITEM_COLUMN_GAP),
                firstY - row * (ITEM_SLOT_SIZE + ITEM_ROW_GAP) + scrollPixels,
                ITEM_SLOT_SIZE,
                ITEM_SLOT_SIZE
            )
        }

        fun maximumVisibleItemRows(panel: Rectangle): Int {
            val available = panel.height - ITEM_TOP_PADDING - ITEM_BOTTOM_PADDING
            return ((available + ITEM_ROW_GAP) / (ITEM_SLOT_SIZE + ITEM_ROW_GAP)).toInt().coerceAtLeast(1)
        }

        fun maximumItemScrollPixels(itemCount: Int, panel: Rectangle): Float {
            val totalRows = ceil(itemCount / ITEM_COLUMNS.toFloat()).toInt()
            return ((totalRows - maximumVisibleItemRows(panel)).coerceAtLeast(0)) * (ITEM_SLOT_SIZE + ITEM_ROW_GAP)
        }

        fun itemIndexAt(panel: Rectangle, itemCount: Int, scrollPixels: Float, x: Float, y: Float): Int? {
            val clipTop = panel.y + panel.height - ITEM_TOP_PADDING + 5f
            val clipBottom = panel.y + ITEM_BOTTOM_PADDING
            return (0 until itemCount).firstOrNull { index ->
                val slot = itemSlotRectangle(panel, index, scrollPixels)
                slot.y + slot.height >= clipBottom && slot.y <= clipTop && slot.contains(x, y)
            }
        }
    }
}
