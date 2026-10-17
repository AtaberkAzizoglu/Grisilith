package com.azizoglu.grisilith.screen.pause

import com.azizoglu.grisilith.statistics.StatPresentationGroup
import com.azizoglu.grisilith.statistics.StatType
import com.badlogic.gdx.math.Rectangle
import kotlin.math.ceil

data class PauseLayout(
    val fullScreen: Rectangle,
    val leftPanel: Rectangle,
    val weaponPanel: Rectangle,
    val gearPanel: Rectangle,
    val itemPanel: Rectangle,
    val centerPanel: Rectangle,
    val characterStatsPanel: Rectangle,
    val characterPortrait: Rectangle,
    val weaponSlots: List<Rectangle>,
    val armorSlot: Rectangle,
    val weaponTamgaSlot: Rectangle,
    val armorTamgaSlot: Rectangle,
    val returnToGameButton: Rectangle,
    val restartButton: Rectangle,
    val settingsButton: Rectangle,
    val mainMenuButton: Rectangle,
    val quitGameButton: Rectangle,
    val modalPanel: Rectangle,
    val modalConfirmButton: Rectangle,
    val modalCancelButton: Rectangle
) {
    companion object {
        const val WEAPON_SLOT_COUNT = 6
        const val ITEM_COLUMNS = 4
        private const val ITEM_CONTENT_TOP_PADDING = 42f
        private const val ITEM_CONTENT_BOTTOM_PADDING = 18f
        private const val ITEM_SLOT_GAP = 10f

        fun create(cameraX: Float, cameraY: Float, cameraWidth: Float, cameraHeight: Float): PauseLayout {
            val screenLeft = cameraX - cameraWidth / 2f
            val screenBottom = cameraY - cameraHeight / 2f
            val outerMargin = 34f
            val panelGap = 24f
            val leftPanelWidth = 390f
            val centerPanelWidth = 285f
            val rightPanelWidth = cameraWidth - outerMargin * 2f - panelGap * 2f - leftPanelWidth - centerPanelWidth
            val contentHeight = cameraHeight - outerMargin * 2f

            val leftPanel = Rectangle(screenLeft + outerMargin, screenBottom + outerMargin, leftPanelWidth, contentHeight)
            val centerPanel = Rectangle(leftPanel.x + leftPanel.width + panelGap, screenBottom + outerMargin, centerPanelWidth, contentHeight)
            val characterStatsPanel = Rectangle(centerPanel.x + centerPanel.width + panelGap, screenBottom + outerMargin, rightPanelWidth, contentHeight)

            // Layout regions only: the renderer draws one outer panel and line separators.
            val weaponPanelHeight = 190f
            val gearPanelHeight = 112f
            val sectionGap = 10f
            val weaponPanel = Rectangle(leftPanel.x, leftPanel.y + leftPanel.height - weaponPanelHeight, leftPanel.width, weaponPanelHeight)
            val gearPanel = Rectangle(leftPanel.x, weaponPanel.y - sectionGap - gearPanelHeight, leftPanel.width, gearPanelHeight)
            val itemPanel = Rectangle(leftPanel.x, leftPanel.y, leftPanel.width, gearPanel.y - sectionGap - leftPanel.y)

            val weaponSlots = createWeaponSlots(weaponPanel)
            val gearSlots = createGearSlots(gearPanel)
            val centerButtons = createCenterButtons(centerPanel)
            val portraitSize = 152f
            val characterPortrait = Rectangle(
                characterStatsPanel.x + (characterStatsPanel.width - portraitSize) / 2f,
                characterStatsPanel.y + characterStatsPanel.height - portraitSize - 70f,
                portraitSize,
                portraitSize
            )

            val modalWidth = 760f
            val modalHeight = 390f
            val modalPanel = Rectangle(cameraX - modalWidth / 2f, cameraY - modalHeight / 2f, modalWidth, modalHeight)
            val modalButtonWidth = 220f
            val modalButtonHeight = 58f
            val modalButtonGap = 35f
            val modalConfirmButton = Rectangle(cameraX - modalButtonGap / 2f - modalButtonWidth, modalPanel.y + 28f, modalButtonWidth, modalButtonHeight)
            val modalCancelButton = Rectangle(cameraX + modalButtonGap / 2f, modalPanel.y + 28f, modalButtonWidth, modalButtonHeight)

            return PauseLayout(
                Rectangle(screenLeft, screenBottom, cameraWidth, cameraHeight), leftPanel, weaponPanel, gearPanel, itemPanel,
                centerPanel, characterStatsPanel, characterPortrait, weaponSlots, gearSlots[0], gearSlots[1], gearSlots[2],
                centerButtons[0], centerButtons[1], centerButtons[2], centerButtons[3], centerButtons[4],
                modalPanel, modalConfirmButton, modalCancelButton
            )
        }

        fun maximumVisibleItemRows(itemPanel: Rectangle): Int {
            val slotSize = itemSlotSize(itemPanel)
            val availableHeight = itemPanel.height - ITEM_CONTENT_TOP_PADDING - ITEM_CONTENT_BOTTOM_PADDING
            return ((availableHeight + ITEM_SLOT_GAP) / (slotSize + ITEM_SLOT_GAP)).toInt().coerceAtLeast(1)
        }

        fun maximumVisibleItemEntries(itemPanel: Rectangle): Int = maximumVisibleItemRows(itemPanel) * ITEM_COLUMNS

        fun maximumItemScrollOffset(itemCount: Int, itemPanel: Rectangle): Int {
            val totalRows = ceil(itemCount / ITEM_COLUMNS.toFloat()).toInt()
            return ((totalRows - maximumVisibleItemRows(itemPanel)).coerceAtLeast(0)) * ITEM_COLUMNS
        }

        fun itemSlotRectangles(itemPanel: Rectangle): List<Rectangle> {
            val slotSize = itemSlotSize(itemPanel)
            val rowCount = maximumVisibleItemRows(itemPanel)
            val gridWidth = slotSize * ITEM_COLUMNS + ITEM_SLOT_GAP * (ITEM_COLUMNS - 1)
            val startX = itemPanel.x + (itemPanel.width - gridWidth) / 2f
            val firstY = itemPanel.y + itemPanel.height - ITEM_CONTENT_TOP_PADDING - slotSize
            return List(rowCount * ITEM_COLUMNS) { index ->
                val column = index % ITEM_COLUMNS
                val row = index / ITEM_COLUMNS
                Rectangle(startX + column * (slotSize + ITEM_SLOT_GAP), firstY - row * (slotSize + ITEM_SLOT_GAP), slotSize, slotSize)
            }
        }

        fun statGroupRectangles(panel: Rectangle): List<Rectangle> {
            val contentX = panel.x + 22f
            val contentWidth = panel.width - 44f
            val columnGap = 12f
            val rowGap = 10f
            val top = panel.y + panel.height - 250f
            val bottom = panel.y + 22f
            val availableHeight = top - bottom
            val groupWidth = (contentWidth - columnGap) / 2f
            val groupHeight = (availableHeight - rowGap) / 2f
            return List(4) { index ->
                val column = index % 2
                val row = index / 2
                Rectangle(contentX + column * (groupWidth + columnGap), bottom + (1 - row) * (groupHeight + rowGap), groupWidth, groupHeight)
            }
        }

        fun statRowRectangles(panel: Rectangle): Map<StatType, Rectangle> {
            val result = linkedMapOf<StatType, Rectangle>()
            StatPresentationGroup.ordered.zip(statGroupRectangles(panel)).forEach { (group, bounds) ->
                val rowHeight = ((bounds.height - 31f) / group.stats.size).coerceAtMost(31f)
                group.stats.forEachIndexed { row, stat ->
                    result[stat] = Rectangle(bounds.x + 5f, bounds.y + bounds.height - 31f - (row + 1) * rowHeight, bounds.width - 10f, rowHeight)
                }
            }
            return result
        }

        private fun itemSlotSize(itemPanel: Rectangle): Float =
            ((itemPanel.width - 34f - ITEM_SLOT_GAP * (ITEM_COLUMNS - 1)) / ITEM_COLUMNS).coerceAtMost(74f)

        private fun createWeaponSlots(panel: Rectangle): List<Rectangle> {
            val slotSize = 66f
            val columnGap = 18f
            val rowGap = 12f
            val gridWidth = slotSize * 3f + columnGap * 2f
            val startX = panel.x + (panel.width - gridWidth) / 2f
            val topRowY = panel.y + panel.height - 43f - slotSize
            return List(WEAPON_SLOT_COUNT) { index ->
                val column = index % 3
                val row = index / 3
                Rectangle(startX + column * (slotSize + columnGap), topRowY - row * (slotSize + rowGap), slotSize, slotSize)
            }
        }

        private fun createGearSlots(panel: Rectangle): List<Rectangle> {
            val slotSize = 72f
            val gap = 34f
            val totalWidth = slotSize * 3f + gap * 2f
            val startX = panel.x + (panel.width - totalWidth) / 2f
            val y = panel.y + (panel.height - slotSize) / 2f - 2f
            return List(3) { index -> Rectangle(startX + index * (slotSize + gap), y, slotSize, slotSize) }
        }

        private fun createCenterButtons(panel: Rectangle): List<Rectangle> {
            val buttonWidth = panel.width - 20f
            val buttonHeight = 64f
            val buttonGap = 14f
            val count = 5
            val totalHeight = buttonHeight * count + buttonGap * (count - 1)
            val firstY = panel.y + (panel.height + totalHeight) / 2f - buttonHeight - 10f
            return List(count) { index ->
                Rectangle(panel.x + (panel.width - buttonWidth) / 2f, firstY - index * (buttonHeight + buttonGap), buttonWidth, buttonHeight)
            }
        }
    }
}
