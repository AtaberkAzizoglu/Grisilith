package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.character.CharacterDefinition
import com.azizoglu.grisilith.economy.MarketProductType
import com.azizoglu.grisilith.economy.MarketRerollPriceCalculator
import com.azizoglu.grisilith.economy.MarketSlot
import com.azizoglu.grisilith.economy.SessionWallet
import com.azizoglu.grisilith.item.runtime.ItemInventory
import com.azizoglu.grisilith.screen.market.MarketLayout
import com.azizoglu.grisilith.stage.StageController
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.weapon.runtime.WeaponInventory
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align
import com.azizoglu.grisilith.screen.market.MarketInteractionState
import com.azizoglu.grisilith.economy.WeaponSalePriceCalculator
import com.azizoglu.grisilith.weapon.definition.WeaponAttackPattern
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.azizoglu.grisilith.item.definition.ItemDefinition
import java.util.Locale
import kotlin.math.roundToInt
import com.azizoglu.grisilith.weapon.runtime.WeaponInstance

class MarketRenderer(
    private val camera:
    OrthographicCamera,

    private val cameraWidth:
    Float,

    private val cameraHeight:
    Float,

    private val batch:
    SpriteBatch,

    private val font:
    BitmapFont,

    private val sessionWallet:
    SessionWallet,

    private val stageController:
    StageController,

    private val selectedCharacter:
    CharacterDefinition,

    private val playerStats:
    PlayerStats,

    private val weaponInventory:
    WeaponInventory,

    private val itemInventory:
    ItemInventory
) {

    companion object {
        private const val PANEL_BORDER =
            2f

        private const val TEXT_PADDING =
            10f
    }

    /*
     * SpriteBatch içinde düz renkli Rectangle
     * çizebilmek için kullanılan 1x1 texture.
     */
    private val whitePixel:
        Texture =
        createWhitePixelTexture()

    private val glyphLayout =
        GlyphLayout()

    private val weaponDamageCalculator =
        WeaponDamageCalculator(
            playerStats =
                playerStats
        )

    private val backgroundColor =
        Color(
            0.025f,
            0.035f,
            0.055f,
            1f
        )

    private val panelColor =
        Color(
            0.075f,
            0.10f,
            0.15f,
            0.98f
        )

    private val cardColor =
        Color(
            0.105f,
            0.14f,
            0.20f,
            1f
        )

    private val borderColor =
        Color(
            0.36f,
            0.52f,
            0.72f,
            1f
        )

    private val lockedColor =
        Color(
            0.38f,
            0.24f,
            0.08f,
            1f
        )

    private val soldColor =
        Color(
            0.11f,
            0.11f,
            0.11f,
            1f
        )

    private val returnColor =
        Color(
            0.10f,
            0.31f,
            0.22f,
            1f
        )

    fun currentLayout():
        MarketLayout {

        return MarketLayout.create(
            cameraX =
                camera.position.x,

            cameraY =
                camera.position.y,

            cameraWidth =
                cameraWidth,

            cameraHeight =
                cameraHeight
        )
    }

    fun render(
        marketSlots: List<MarketSlot>,
        marketRerollsUsed: Int,
        itemScrollOffset: Int,
        interactionState: MarketInteractionState
    ) {
        val layout =
            currentLayout()

        val rerollPrice =
            MarketRerollPriceCalculator
                .calculatePrice(
                    currentStage =
                        stageController.currentStage,

                    rerollsUsedInCurrentMarket =
                        marketRerollsUsed
                )

        renderBackground(
            layout
        )

        renderTopBar(
            layout =
                layout,

            rerollPrice =
                rerollPrice,

            interactionState =
                interactionState
        )

        renderMarketOffers(
            layout = layout,
            marketSlots = marketSlots,
            interactionState =
                interactionState
        )

        renderWeaponInventory(
            layout
        )

        renderItemInventory(
            layout =
                layout,

            itemScrollOffset =
                itemScrollOffset
        )

        renderCharacterPanel(
            layout
        )

        renderStatsPanel(
            layout
        )

        renderTamgaPanel(
            layout
        )

        renderWeaponSellArea(
            layout =
                layout,

            interactionState =
                interactionState
        )

        renderValidMarketDropTargets(
            layout =
                layout,

            marketSlots =
                marketSlots,

            interactionState =
                interactionState
        )

        renderDraggedMarketOffer(
            marketSlots =
                marketSlots,

            interactionState =
                interactionState
        )

        renderDraggedWeapon(
            interactionState =
                interactionState
        )

        renderWeaponDetailPopup(
            layout =
                layout,
            interactionState =
                interactionState
        )

        renderItemDetailPopup(
            layout =
                layout,

            interactionState =
                interactionState
        )

        renderTamgaDetailPopup(
            layout =
                layout,

            interactionState =
                interactionState
        )

        renderMergeConfirmationPopup(
            layout =
                layout,

            interactionState =
                interactionState
        )

        renderSaleConfirmationPopup(
            layout =
                layout,

            interactionState =
                interactionState
        )

        batch.color =
            Color.WHITE

        font.color =
            Color.WHITE
    }

    private fun renderBackground(
        layout: MarketLayout
    ) {
        drawFilledRectangle(
            rectangle =
                layout.fullScreen,

            color =
                backgroundColor
        )
    }

    private fun renderTopBar(
        layout: MarketLayout,
        rerollPrice: Int,
        interactionState:
        MarketInteractionState
    ) {
        drawPanel(
            rectangle =
                layout.stagePanel,

            fillColor =
                panelColor
        )

        drawPanel(
            rectangle =
                layout.goldPanel,

            fillColor =
                panelColor
        )

        drawPanel(
            rectangle =
                layout.returnButton,

            fillColor =
                returnColor
        )

        val nextStage =
            (
                stageController.currentStage +
                    1
                )
                .coerceAtMost(
                    stageController.maximumStage
                )

        drawCenteredText(
            text =
                "NEXT STAGE: $nextStage",

            rectangle =
                layout.stagePanel,

            color =
                Color.WHITE
        )

        val generalFailure =
            interactionState
                .marketFailureFeedback
                ?.takeIf { feedback ->
                    feedback.offerIndex == null
                }

        val rerollText =
            if (generalFailure != null) {
                generalFailure.message
            } else if (rerollPrice <= 0) {
                "Gold Coin: " +
                    "${sessionWallet.goldCoins}" +
                    "    |    Click or press R to Reroll: FREE"
            } else {
                "Gold Coin: " +
                    "${sessionWallet.goldCoins}" +
                    "    |    Click or press R to Reroll: " +
                    "$rerollPrice Gold Coin"
            }

        drawCenteredText(
            text =
                rerollText,

            rectangle =
                layout.goldPanel,

            color =
                if (generalFailure != null) {
                    Color(
                        1f,
                        0.35f,
                        0.35f,
                        1f
                    )
                } else {
                    Color.GOLD
                }
        )

        drawCenteredText(
            text =
                "RETURN TO GAME",

            rectangle =
                layout.returnButton,

            color =
                Color.WHITE
        )
    }

    private fun renderMarketOffers(
        layout: MarketLayout,
        marketSlots: List<MarketSlot>,
        interactionState:
        MarketInteractionState
    ) {
        layout.offerCards
            .forEachIndexed {
                    index,
                    cardRectangle ->

                val slot =
                    marketSlots.getOrNull(
                        index
                    )

                val fillColor =
                    when {
                        slot?.isSold == true ->
                            soldColor

                        slot?.isLocked == true ->
                            lockedColor

                        else ->
                            cardColor
                    }

                drawPanel(
                    rectangle =
                        cardRectangle,

                    fillColor =
                        fillColor
                )

                renderOfferCardContent(
                    slot = slot,
                    cardRectangle =
                        cardRectangle
                )

                renderOfferFailureOverlay(
                    offerIndex =
                        index,

                    cardRectangle =
                        cardRectangle,

                    interactionState =
                        interactionState
                )

                val lockRectangle =
                    layout.lockButtons[index]

                drawPanel(
                    rectangle =
                        lockRectangle,

                    fillColor =
                        if (
                            slot?.isLocked ==
                            true
                        ) {
                            Color(
                                0.55f,
                                0.31f,
                                0.06f,
                                1f
                            )
                        } else {
                            Color(
                                0.13f,
                                0.18f,
                                0.25f,
                                1f
                            )
                        }
                )

                val lockText =
                    when {
                        slot?.isSold == true ->
                            "SOLD"

                        slot?.isLocked == true ->
                            "LOCKED"

                        else ->
                            "LOCK"
                    }

                drawCenteredText(
                    text =
                        lockText,

                    rectangle =
                        lockRectangle,

                    color =
                        Color.WHITE
                )
            }
    }

    private fun renderOfferFailureOverlay(
        offerIndex: Int,
        cardRectangle: Rectangle,
        interactionState:
        MarketInteractionState
    ) {
        val feedback =
            interactionState
                .marketFailureFeedback
                ?: return

        if (
            feedback.offerIndex !=
            offerIndex
        ) {
            return
        }

        val overlayRectangle =
            Rectangle(
                cardRectangle.x + 6f,
                cardRectangle.y + 41f,
                cardRectangle.width - 12f,
                cardRectangle.height - 47f
            )

        drawFilledRectangle(
            rectangle =
                overlayRectangle,

            color =
                Color(
                    0.48f,
                    0.06f,
                    0.06f,
                    0.94f
                )
        )

        drawCenteredWrappedText(
            text =
                feedback.message,

            rectangle =
                overlayRectangle,

            color =
                Color.WHITE
        )
    }

    private fun drawCenteredWrappedText(
        text: String,
        rectangle: Rectangle,
        color: Color
    ) {
        font.color =
            color

        font.draw(
            batch,
            text,
            rectangle.x + 8f,
            rectangle.y +
                rectangle.height / 2f +
                8f,
            rectangle.width - 16f,
            Align.center,
            true
        )
    }

    private fun renderOfferCardContent(
        slot: MarketSlot?,
        cardRectangle: Rectangle
    ) {
        val offer =
            slot?.offer

        if (slot?.isSold == true) {
            drawCenteredText(
                text =
                    "SOLD",

                rectangle =
                    Rectangle(
                        cardRectangle.x,
                        cardRectangle.y +
                            45f,
                        cardRectangle.width,
                        cardRectangle.height -
                            45f
                    ),

                color =
                    Color.LIGHT_GRAY
            )

            return
        }

        if (offer == null) {
            drawCenteredText(
                text =
                    "EMPTY",

                rectangle =
                    cardRectangle,

                color =
                    Color.LIGHT_GRAY
            )

            return
        }

        val productTypeText =
            when (
                offer.productType
            ) {
                MarketProductType.WEAPON ->
                    "WEAPON"

                MarketProductType.ITEM ->
                    "ITEM"

                MarketProductType.STAT ->
                    "STAT"
            }

        font.color =
            categoryColor(
                offer.productType
            )

        font.draw(
            batch,
            productTypeText,
            cardRectangle.x +
                TEXT_PADDING,
            cardRectangle.y +
                cardRectangle.height -
                13f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            offer.title,
            cardRectangle.x +
                TEXT_PADDING,
            cardRectangle.y +
                cardRectangle.height -
                36f,
            cardRectangle.width -
                TEXT_PADDING * 2f,
            Align.left,
            true
        )

        val oldScaleX =
            font.data.scaleX

        val oldScaleY =
            font.data.scaleY

        font.data.setScale(
            0.78f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            offer.description,
            cardRectangle.x +
                TEXT_PADDING,
            cardRectangle.y +
                cardRectangle.height -
                67f,
            cardRectangle.width -
                TEXT_PADDING * 2f,
            Align.left,
            true
        )

        font.data.setScale(
            oldScaleX,
            oldScaleY
        )

        font.color =
            Color.GOLD

        font.draw(
            batch,
            "${offer.price} Gold Coin",
            cardRectangle.x +
                TEXT_PADDING,
            cardRectangle.y +
                48f
        )
    }

    private fun renderWeaponInventory(
        layout: MarketLayout
    ) {
        drawPanel(
            rectangle =
                layout.weaponPanel,

            fillColor =
                panelColor
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "WEAPONS  " +
                "(${weaponInventory.equippedWeaponCount}/" +
                "${WeaponInventory.WEAPON_SLOT_COUNT})",
            layout.weaponPanel.x +
                TEXT_PADDING,
            layout.weaponPanel.y +
                layout.weaponPanel.height -
                13f
        )

        layout.weaponSlots
            .forEachIndexed {
                    index,
                    slotRectangle ->

                drawPanel(
                    rectangle =
                        slotRectangle,

                    fillColor =
                        cardColor
                )

                val weapon =
                    weaponInventory.weaponAt(
                        index
                    )

                if (weapon == null) {
                    drawCenteredText(
                        text =
                            "EMPTY",

                        rectangle =
                            slotRectangle,

                        color =
                            Color.GRAY
                    )
                } else {
                    font.color =
                        Color.WHITE

                    font.draw(
                        batch,
                        weapon.displayName,
                        slotRectangle.x +
                            8f,
                        slotRectangle.y +
                            slotRectangle.height -
                            11f,
                        slotRectangle.width -
                            16f,
                        Align.center,
                        true
                    )

                    font.color =
                        Color.GOLD

                    font.draw(
                        batch,
                        "Level " +
                            romanNumeral(
                                weapon.level
                            ),
                        slotRectangle.x +
                            8f,
                        slotRectangle.y +
                            21f,
                        slotRectangle.width -
                            16f,
                        Align.center,
                        false
                    )
                }
            }
    }

    private data class GroupedItemEntry(
        val definition:
        ItemDefinition,

        val count:
        Int
    )

    private fun groupedItemEntries():
        List<GroupedItemEntry> {

        return itemInventory
            .allItems()
            .groupBy { item ->
                item.definition.id
            }
            .values
            .mapNotNull { copies ->
                val firstCopy =
                    copies.firstOrNull()
                        ?: return@mapNotNull null

                GroupedItemEntry(
                    definition =
                        firstCopy.definition,

                    count =
                        copies.size
                )
            }
    }

    private fun renderItemInventory(
        layout: MarketLayout,
        itemScrollOffset: Int
    ) {
        drawPanel(
            rectangle =
                layout.itemPanel,

            fillColor =
                panelColor
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "ITEMS  (${itemInventory.itemCount})",
            layout.itemPanel.x +
                TEXT_PADDING,
            layout.itemPanel.y +
                layout.itemPanel.height -
                12f
        )

        val groupedItems =
            groupedItemEntries()

        if (groupedItems.isEmpty()) {
            font.color =
                Color.GRAY

            font.draw(
                batch,
                "No Items",
                layout.itemPanel.x +
                    TEXT_PADDING,
                layout.itemPanel.y +
                    layout.itemPanel.height -
                    42f
            )

            return
        }

        val maximumVisibleRows =
            (
                (
                    layout.itemPanel.height -
                        48f
                    ) / 24f
                )
                .toInt()
                .coerceAtLeast(1)

        val maximumScrollOffset =
            (
                groupedItems.size -
                    maximumVisibleRows
                )
                .coerceAtLeast(0)

        val safeScrollOffset =
            itemScrollOffset.coerceIn(
                0,
                maximumScrollOffset
            )

        groupedItems
            .drop(
                safeScrollOffset
            )
            .take(
                maximumVisibleRows
            )
            .forEachIndexed {
                    rowIndex,
                    entry ->

                val rowRectangle =
                    layout.itemRowRectangle(
                        rowIndex
                    )

                /*
                 * Çok hafif satır zemini; daha sonra
                 * görseller geldiğinde kaldırılabilir.
                 */
                drawFilledRectangle(
                    rectangle =
                        rowRectangle,

                    color =
                        Color(
                            0.09f,
                            0.12f,
                            0.17f,
                            0.70f
                        )
                )

                val nameText =
                    if (entry.count > 1) {
                        "${entry.definition.displayName}  x${entry.count}"
                    } else {
                        entry.definition.displayName
                    }

                font.color =
                    Color.LIGHT_GRAY

                font.draw(
                    batch,
                    nameText,
                    rowRectangle.x +
                        5f,
                    rowRectangle.y +
                        17f
                )

                val totalBonusText =
                    entry.definition
                        .statModifiers
                        .joinToString(
                            separator = ", "
                        ) { modifier ->
                            val totalAmount =
                                modifier.amount *
                                    entry.count

                            "${modifier.statType.displayName} " +
                                formatSignedValue(
                                    totalAmount
                                )
                        }

                font.color =
                    Color(
                        0.45f,
                        0.88f,
                        0.58f,
                        1f
                    )

                font.draw(
                    batch,
                    totalBonusText,
                    rowRectangle.x +
                        rowRectangle.width -
                        285f,
                    rowRectangle.y +
                        17f,
                    275f,
                    Align.right,
                    false
                )
            }

        val hasItemsAbove =
            safeScrollOffset > 0

        val hasItemsBelow =
            safeScrollOffset +
                maximumVisibleRows <
                groupedItems.size

        if (hasItemsAbove) {
            font.color =
                Color.GRAY

            font.draw(
                batch,
                "More above",
                layout.itemPanel.x +
                    layout.itemPanel.width -
                    115f,
                layout.itemPanel.y +
                    layout.itemPanel.height -
                    12f
            )
        }

        if (hasItemsBelow) {
            font.color =
                Color.GRAY

            font.draw(
                batch,
                "More below",
                layout.itemPanel.x +
                    layout.itemPanel.width -
                    115f,
                layout.itemPanel.y +
                    12f
            )
        }
    }

    private fun formatSignedValue(
        value: Int
    ): String {
        return if (value >= 0) {
            "+$value"
        } else {
            value.toString()
        }
    }

    private fun renderCharacterPanel(
        layout: MarketLayout
    ) {
        drawPanel(
            rectangle =
                layout.characterPanel,

            fillColor =
                panelColor
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "CHARACTER",
            layout.characterPanel.x +
                TEXT_PADDING,
            layout.characterPanel.y +
                layout.characterPanel.height -
                13f
        )

        font.color =
            Color.CYAN

        font.draw(
            batch,
            selectedCharacter.displayName,
            layout.characterPanel.x +
                TEXT_PADDING,
            layout.characterPanel.y +
                layout.characterPanel.height -
                42f
        )

        val oldScaleX =
            font.data.scaleX

        val oldScaleY =
            font.data.scaleY

        font.data.setScale(
            0.80f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            selectedCharacter.description,
            layout.characterPanel.x +
                TEXT_PADDING,
            layout.characterPanel.y +
                layout.characterPanel.height -
                66f,
            layout.characterPanel.width -
                TEXT_PADDING * 2f,
            Align.left,
            true
        )

        font.data.setScale(
            oldScaleX,
            oldScaleY
        )
    }

    private fun renderStatsPanel(
        layout: MarketLayout
    ) {
        drawPanel(
            rectangle =
                layout.statsPanel,

            fillColor =
                panelColor
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "STATS",
            layout.statsPanel.x +
                TEXT_PADDING,
            layout.statsPanel.y +
                layout.statsPanel.height -
                12f
        )

        val oldScaleX =
            font.data.scaleX

        val oldScaleY =
            font.data.scaleY

        font.data.setScale(
            0.70f
        )

        val availableHeight =
            layout.statsPanel.height -
                42f

        val statCount =
            StatType.entries.size

        val lineHeight =
            (
                availableHeight /
                    statCount
                )
                .coerceIn(
                    11.5f,
                    15f
                )

        var currentY =
            layout.statsPanel.y +
                layout.statsPanel.height -
                35f

        StatType.entries
            .forEach { statType ->
                /*
                 * Son satır panelin dışına çıkmasın.
                 */
                if (
                    currentY <
                    layout.statsPanel.y +
                    8f
                ) {
                    return@forEach
                }

                font.color =
                    Color.LIGHT_GRAY

                font.draw(
                    batch,
                    statType.displayName,
                    layout.statsPanel.x +
                        TEXT_PADDING,
                    currentY
                )

                font.color =
                    Color.WHITE

                font.draw(
                    batch,
                    playerStats
                        .totalScore(
                            statType
                        )
                        .toString(),
                    layout.statsPanel.x +
                        layout.statsPanel.width -
                        42f,
                    currentY
                )

                currentY -=
                    lineHeight
            }

        font.data.setScale(
            oldScaleX,
            oldScaleY
        )
    }

    private fun renderTamgaPanel(
        layout: MarketLayout
    ) {
        drawPanel(
            rectangle =
                layout.tamgaPanel,

            fillColor =
                panelColor
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "ACTIVE TAMGA",
            layout.tamgaPanel.x +
                TEXT_PADDING,
            layout.tamgaPanel.y +
                layout.tamgaPanel.height -
                12f
        )

        font.color =
            Color.GRAY

        font.draw(
            batch,
            "No Weapon Tamga | No Armor Tamga",
            layout.tamgaPanel.x +
                TEXT_PADDING,
            layout.tamgaPanel.y +
                17f
        )
    }

    private fun renderWeaponSellArea(
        layout: MarketLayout,
        interactionState:
        MarketInteractionState
    ) {
        val draggedSlotIndex =
            interactionState
                .draggedWeaponSlotIndex
                ?: return

        val weapon =
            weaponInventory.weaponAt(
                draggedSlotIndex
            ) ?: return

        val salePrice =
            WeaponSalePriceCalculator
                .calculateSalePrice(
                    weaponLevel =
                        weapon.level,

                    currentStage =
                        stageController.currentStage
                )

        drawPanel(
            rectangle =
                layout.weaponSellArea,

            fillColor =
                Color(
                    0.40f,
                    0.08f,
                    0.08f,
                    0.98f
                )
        )

        drawCenteredText(
            text =
                "SELL THE WEAPON",

            rectangle =
                Rectangle(
                    layout.weaponSellArea.x,
                    layout.weaponSellArea.y +
                        90f,
                    layout.weaponSellArea.width,
                    50f
                ),

            color =
                Color.WHITE
        )

        drawCenteredText(
            text =
                "${weapon.displayName} - " +
                    "Level ${romanNumeral(weapon.level)}",

            rectangle =
                Rectangle(
                    layout.weaponSellArea.x,
                    layout.weaponSellArea.y +
                        50f,
                    layout.weaponSellArea.width,
                    40f
                ),

            color =
                Color.LIGHT_GRAY
        )

        drawCenteredText(
            text =
                "Release here to sell for " +
                    "$salePrice Gold Coin",

            rectangle =
                Rectangle(
                    layout.weaponSellArea.x,
                    layout.weaponSellArea.y +
                        15f,
                    layout.weaponSellArea.width,
                    40f
                ),

            color =
                Color.GOLD
        )
    }

    private fun renderDraggedWeapon(
        interactionState:
        MarketInteractionState
    ) {
        val draggedSlotIndex =
            interactionState
                .draggedWeaponSlotIndex
                ?: return

        val weapon =
            weaponInventory.weaponAt(
                draggedSlotIndex
            ) ?: return

        val previewWidth =
            190f

        val previewHeight =
            70f

        val previewRectangle =
            Rectangle(
                interactionState
                    .pointerPosition.x -
                    previewWidth / 2f,

                interactionState
                    .pointerPosition.y -
                    previewHeight / 2f,

                previewWidth,
                previewHeight
            )

        drawPanel(
            rectangle =
                previewRectangle,

            fillColor =
                Color(
                    0.16f,
                    0.22f,
                    0.31f,
                    0.94f
                )
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            weapon.displayName,
            previewRectangle.x + 8f,
            previewRectangle.y +
                previewRectangle.height -
                15f,
            previewRectangle.width - 16f,
            Align.center,
            true
        )

        font.color =
            Color.GOLD

        font.draw(
            batch,
            "Level " +
                romanNumeral(
                    weapon.level
                ),
            previewRectangle.x + 8f,
            previewRectangle.y + 22f,
            previewRectangle.width - 16f,
            Align.center,
            false
        )
    }

    private fun renderWeaponDetailPopup(
        layout: MarketLayout,
        interactionState:
        MarketInteractionState
    ) {
        if (
            interactionState
                .isAnyConfirmationOpen
        ) {
            return
        }

        val detailedSlotIndex =
            interactionState
                .detailedWeaponSlotIndex
                ?: return

        val weapon =
            weaponInventory.weaponAt(
                detailedSlotIndex
            ) ?: return

        val definition =
            weapon.definition

        val levelStats =
            weapon.currentLevelStats

        val damage =
            weaponDamageCalculator
                .calculateNonCriticalDamage(
                    weapon
                )

        val attackSpeedStatMultiplier =
            when (
                definition.normalAttackSpeedStat
            ) {
                StatType.HASTE -> {
                    playerStats
                        .hasteAttackSpeedMultiplier()
                }

                else -> {
                    playerStats
                        .attackSpeedMultiplier()
                }
            }

        val attacksPerSecond =
            definition.baseAttacksPerSecond *
                levelStats.attackSpeedMultiplier *
                attackSpeedStatMultiplier

        val range =
            definition.baseRange *
                levelStats.rangeMultiplier

        val knockback =
            definition.baseKnockback *
                levelStats.knockbackMultiplier

        val criticalRatePercent =
            playerStats
                .criticalHitChance() *
                100f

        val criticalDamagePercent =
            playerStats
                .criticalDamageMultiplier() *
                100f

        val piercingScore =
            playerStats.totalScore(
                StatType.PIERCING
            )

        val maximumTargets =
            definition.baseMaximumTargets +
                levelStats.additionalTargets +
                (
                    playerStats
                        .projectileTargetCount() -
                        1
                    )
                    .coerceAtLeast(0)

        drawModalBackground(
            layout
        )

        drawPanel(
            rectangle =
                layout.weaponDetailPanel,

            fillColor =
                Color(
                    0.08f,
                    0.11f,
                    0.17f,
                    1f
                )
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            weapon.displayName,
            layout.weaponDetailPanel.x +
                20f,
            layout.weaponDetailPanel.y +
                layout.weaponDetailPanel.height -
                23f
        )

        font.color =
            Color.GOLD

        font.draw(
            batch,
            "Level " +
                romanNumeral(
                    weapon.level
                ),
            layout.weaponDetailPanel.x +
                20f,
            layout.weaponDetailPanel.y +
                layout.weaponDetailPanel.height -
                49f
        )

        val statsText =
            buildString {
                appendLine(
                    "Attack Model: " +
                        attackModelText(
                            weapon
                        )
                )

                appendLine(
                    "Damage: $damage"
                )

                appendLine(
                    "Attacks per Second: " +
                        formatDecimal(
                            attacksPerSecond
                        )
                )

                appendLine(
                    "Range: " +
                        range.roundToInt()
                )

                if (knockback > 0f) {
                    appendLine(
                        "Knockback: " +
                            knockback.roundToInt()
                    )
                }

                appendLine(
                    "Piercing: $piercingScore"
                )

                if (
                    maximumTargets >
                    1
                ) {
                    appendLine(
                        "Maximum Targets: " +
                            maximumTargets
                    )
                }

                appendLine(
                    "Critical Rate: " +
                        formatDecimal(
                            criticalRatePercent
                        ) +
                        "%"
                )

                append(
                    "Critical Damage: " +
                        formatDecimal(
                            criticalDamagePercent
                        ) +
                        "%"
                )
            }

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            statsText,
            layout.weaponDetailPanel.x +
                20f,
            layout.weaponDetailPanel.y +
                layout.weaponDetailPanel.height -
                80f,
            230f,
            Align.left,
            true
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            definition.description,
            layout.weaponDetailPanel.x +
                275f,
            layout.weaponDetailPanel.y +
                layout.weaponDetailPanel.height -
                80f,
            layout.weaponDetailPanel.width -
                295f,
            Align.left,
            true
        )

        font.color =
            Color.GRAY

        font.draw(
            batch,
            "Click outside to close.",
            layout.weaponDetailPanel.x +
                20f,
            layout.weaponDetailPanel.y +
                22f
        )
    }

    private fun attackModelText(
        weapon:
        com.azizoglu.grisilith.weapon
        .runtime.WeaponInstance
    ): String {
        /*
         * Bazı Weapon'ların attack pattern'i aynı
         * olsa da ekranda özel ve kısa şekil
         * gösterilmesi istenebilir.
         */
        return when (
            weapon.displayName
                .lowercase(
                    Locale.US
                )
        ) {
            "double swords" ->
                "X"

            else -> {
                when (
                    weapon.definition
                        .attackPattern
                ) {
                    WeaponAttackPattern
                        .SHORT_SLASH_LINES ->
                        "Slash"

                    WeaponAttackPattern
                        .FORWARD_CONE ->
                        "Cone"

                    WeaponAttackPattern
                        .TARGET_AREA_CIRCLE ->
                        "Circle"

                    WeaponAttackPattern
                        .MELEE_OR_THROW ->
                        "Stab / Throw"

                    WeaponAttackPattern
                        .STRAIGHT_PROJECTILE ->
                        "Projectile"

                    WeaponAttackPattern
                        .PIERCING_PROJECTILE ->
                        "Piercing Projectile"

                    WeaponAttackPattern
                        .LONG_PIERCING_LINE ->
                        "Piercing Line"

                    WeaponAttackPattern
                        .SHIELD_CONE ->
                        "Shield Cone"

                    WeaponAttackPattern
                        .TOME_PROJECTILE_OR_BEAM ->
                        "Projectile / Beam"

                    WeaponAttackPattern
                        .TOTEM_PROJECTILE_OR_DRAIN ->
                        "Projectile / Drain"

                    WeaponAttackPattern.NONE ->
                        "Passive"
                }
            }
        }
    }

    private fun renderItemDetailPopup(
        layout: MarketLayout,
        interactionState:
        MarketInteractionState
    ) {
        if (
            interactionState
                .isAnyConfirmationOpen
        ) {
            return
        }

        val itemDefinitionId =
            interactionState
                .detailedItemDefinitionId
                ?: return

        val copies =
            itemInventory
                .allItems()
                .filter { item ->
                    item.definition.id.value ==
                        itemDefinitionId
                }

        val firstItem =
            copies.firstOrNull()
                ?: return

        val definition =
            firstItem.definition

        val copyCount =
            copies.size

        val maximumCopies =
            definition.maximumInventoryCopies
                ?: definition.rarity
                    .maximumInventoryCopies

        val totalBonusText =
            definition.statModifiers
                .joinToString(
                    separator = "\n"
                ) { modifier ->
                    val totalAmount =
                        modifier.amount *
                            copyCount

                    "${modifier.statType.displayName}: " +
                        formatSignedValue(
                            totalAmount
                        )
                }

        drawModalBackground(
            layout
        )

        drawPanel(
            rectangle =
                layout.weaponDetailPanel,

            fillColor =
                Color(
                    0.08f,
                    0.13f,
                    0.10f,
                    1f
                )
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            definition.displayName,
            layout.weaponDetailPanel.x +
                20f,
            layout.weaponDetailPanel.y +
                layout.weaponDetailPanel.height -
                24f
        )

        font.color =
            Color(
                0.40f,
                0.90f,
                0.60f,
                1f
            )

        font.draw(
            batch,
            definition.rarity.displayName +
                " Item",
            layout.weaponDetailPanel.x +
                20f,
            layout.weaponDetailPanel.y +
                layout.weaponDetailPanel.height -
                51f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            definition.description,
            layout.weaponDetailPanel.x +
                20f,
            layout.weaponDetailPanel.y +
                layout.weaponDetailPanel.height -
                84f,
            layout.weaponDetailPanel.width -
                40f,
            Align.left,
            true
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "Owned: $copyCount / $maximumCopies\n\n" +
                "Total Bonus:\n" +
                totalBonusText,
            layout.weaponDetailPanel.x +
                20f,
            layout.weaponDetailPanel.y +
                105f
        )

        font.color =
            Color.GRAY

        font.draw(
            batch,
            "Click outside to close.",
            layout.weaponDetailPanel.x +
                20f,
            layout.weaponDetailPanel.y +
                22f
        )
    }

    private fun renderTamgaDetailPopup(
        layout: MarketLayout,
        interactionState:
        MarketInteractionState
    ) {
        if (
            !interactionState
                .isTamgaDetailOpen ||
            interactionState
                .isAnyConfirmationOpen
        ) {
            return
        }

        drawModalBackground(
            layout
        )

        drawPanel(
            rectangle =
                layout.weaponDetailPanel,

            fillColor =
                Color(
                    0.08f,
                    0.11f,
                    0.17f,
                    1f
                )
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "ACTIVE TAMGA",
            layout.weaponDetailPanel.x +
                20f,
            layout.weaponDetailPanel.y +
                layout.weaponDetailPanel.height -
                25f
        )

        font.color =
            Color.GRAY

        font.draw(
            batch,
            "No Weapon Tamga is active.\n" +
                "No Armor Tamga is active.",
            layout.weaponDetailPanel.x +
                20f,
            layout.weaponDetailPanel.y +
                layout.weaponDetailPanel.height -
                70f
        )

        font.draw(
            batch,
            "Click outside to close.",
            layout.weaponDetailPanel.x +
                20f,
            layout.weaponDetailPanel.y +
                22f
        )
    }

    private fun drawModalBackground(
        layout: MarketLayout
    ) {
        drawFilledRectangle(
            rectangle =
                layout.fullScreen,

            color =
                Color(
                    0f,
                    0f,
                    0f,
                    0.68f
                )
        )
    }

    private fun formatDecimal(
        value: Float
    ): String {
        return String.format(
            Locale.US,
            "%.2f",
            value
        )
    }

    private fun renderMergeConfirmationPopup(
        layout: MarketLayout,
        interactionState:
        MarketInteractionState
    ) {
        val pendingMerge =
            interactionState
                .pendingWeaponMerge
                ?: return

        drawFilledRectangle(
            rectangle =
                layout.fullScreen,

            color =
                Color(
                    0f,
                    0f,
                    0f,
                    0.65f
                )
        )

        drawPanel(
            rectangle =
                layout.mergeConfirmationPanel,

            fillColor =
                Color(
                    0.11f,
                    0.08f,
                    0.08f,
                    1f
                )
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "CONFIRM WEAPON MERGE",
            layout.mergeConfirmationPanel.x +
                20f,
            layout.mergeConfirmationPanel.y +
                layout.mergeConfirmationPanel.height -
                25f
        )

        val warningText =
            "You are using a Level " +
                romanNumeral(
                    pendingMerge
                        .sourceWeaponLevel
                ) +
                " ${pendingMerge.sourceWeaponName} " +
                "to upgrade a Level " +
                romanNumeral(
                    pendingMerge
                        .targetWeaponLevel
                ) +
                " ${pendingMerge.sourceWeaponName}.\n\n" +
                "The result will be a Level " +
                romanNumeral(
                    pendingMerge
                        .resultWeaponLevel
                ) +
                " ${pendingMerge.sourceWeaponName}, " +
                "and the Level " +
                romanNumeral(
                    pendingMerge
                        .sourceWeaponLevel
                ) +
                " Weapon will be consumed.\n\n" +
                "Are you sure?"

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            warningText,
            layout.mergeConfirmationPanel.x +
                20f,
            layout.mergeConfirmationPanel.y +
                layout.mergeConfirmationPanel.height -
                60f,
            layout.mergeConfirmationPanel.width -
                40f,
            Align.left,
            true
        )

        drawPanel(
            rectangle =
                layout.mergeConfirmButton,

            fillColor =
                Color(
                    0.15f,
                    0.42f,
                    0.23f,
                    1f
                )
        )

        drawCenteredText(
            text =
                "CONFIRM",

            rectangle =
                layout.mergeConfirmButton,

            color =
                Color.WHITE
        )

        drawPanel(
            rectangle =
                layout.mergeCancelButton,

            fillColor =
                Color(
                    0.45f,
                    0.16f,
                    0.16f,
                    1f
                )
        )

        drawCenteredText(
            text =
                "CANCEL",

            rectangle =
                layout.mergeCancelButton,

            color =
                Color.WHITE
        )
    }

    private fun renderSaleConfirmationPopup(
        layout: MarketLayout,
        interactionState:
        MarketInteractionState
    ) {
        val pendingSale =
            interactionState
                .pendingWeaponSale
                ?: return

        drawFilledRectangle(
            rectangle =
                layout.fullScreen,

            color =
                Color(
                    0f,
                    0f,
                    0f,
                    0.65f
                )
        )

        drawPanel(
            rectangle =
                layout.mergeConfirmationPanel,

            fillColor =
                Color(
                    0.11f,
                    0.08f,
                    0.08f,
                    1f
                )
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "CONFIRM WEAPON SALE",
            layout.mergeConfirmationPanel.x +
                20f,
            layout.mergeConfirmationPanel.y +
                layout.mergeConfirmationPanel.height -
                25f
        )

        val warningText =
            "Sell Level " +
                romanNumeral(
                    pendingSale.weaponLevel
                ) +
                " ${pendingSale.weaponName} for " +
                "${pendingSale.salePrice} Gold Coin?\n\n" +
                "This Weapon will be removed permanently " +
                "from the current run."

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            warningText,
            layout.mergeConfirmationPanel.x +
                20f,
            layout.mergeConfirmationPanel.y +
                layout.mergeConfirmationPanel.height -
                70f,
            layout.mergeConfirmationPanel.width -
                40f,
            Align.left,
            true
        )

        drawPanel(
            rectangle =
                layout.mergeConfirmButton,

            fillColor =
                Color(
                    0.45f,
                    0.16f,
                    0.16f,
                    1f
                )
        )

        drawCenteredText(
            text =
                "SELL",

            rectangle =
                layout.mergeConfirmButton,

            color =
                Color.WHITE
        )

        drawPanel(
            rectangle =
                layout.mergeCancelButton,

            fillColor =
                Color(
                    0.13f,
                    0.18f,
                    0.25f,
                    1f
                )
        )

        drawCenteredText(
            text =
                "CANCEL",

            rectangle =
                layout.mergeCancelButton,

            color =
                Color.WHITE
        )
    }

    private fun categoryColor(
        productType:
        MarketProductType
    ): Color {
        return when (
            productType
        ) {
            MarketProductType.WEAPON ->
                Color(
                    0.95f,
                    0.55f,
                    0.22f,
                    1f
                )

            MarketProductType.ITEM ->
                Color(
                    0.30f,
                    0.85f,
                    0.52f,
                    1f
                )

            MarketProductType.STAT ->
                Color(
                    0.35f,
                    0.73f,
                    1f,
                    1f
                )
        }
    }

    private fun drawHighlightBorder(
        rectangle: Rectangle
    ) {
        val thickness =
            4f

        val highlightColor =
            Color(
                0.30f,
                1f,
                0.48f,
                1f
            )

        drawFilledRectangle(
            rectangle =
                Rectangle(
                    rectangle.x,
                    rectangle.y +
                        rectangle.height -
                        thickness,
                    rectangle.width,
                    thickness
                ),

            color =
                highlightColor
        )

        drawFilledRectangle(
            rectangle =
                Rectangle(
                    rectangle.x,
                    rectangle.y,
                    rectangle.width,
                    thickness
                ),

            color =
                highlightColor
        )

        drawFilledRectangle(
            rectangle =
                Rectangle(
                    rectangle.x,
                    rectangle.y,
                    thickness,
                    rectangle.height
                ),

            color =
                highlightColor
        )

        drawFilledRectangle(
            rectangle =
                Rectangle(
                    rectangle.x +
                        rectangle.width -
                        thickness,
                    rectangle.y,
                    thickness,
                    rectangle.height
                ),

            color =
                highlightColor
        )
    }

    private fun drawPanel(
        rectangle: Rectangle,
        fillColor: Color
    ) {
        drawFilledRectangle(
            rectangle =
                rectangle,

            color =
                borderColor
        )

        drawFilledRectangle(
            rectangle =
                Rectangle(
                    rectangle.x +
                        PANEL_BORDER,

                    rectangle.y +
                        PANEL_BORDER,

                    rectangle.width -
                        PANEL_BORDER * 2f,

                    rectangle.height -
                        PANEL_BORDER * 2f
                ),

            color =
                fillColor
        )
    }

    private fun drawFilledRectangle(
        rectangle: Rectangle,
        color: Color
    ) {
        batch.color =
            color

        batch.draw(
            whitePixel,
            rectangle.x,
            rectangle.y,
            rectangle.width,
            rectangle.height
        )

        batch.color =
            Color.WHITE
    }

    private fun drawCenteredText(
        text: String,
        rectangle: Rectangle,
        color: Color
    ) {
        glyphLayout.setText(
            font,
            text
        )

        font.color =
            color

        val textX =
            rectangle.x +
                (
                    rectangle.width -
                        glyphLayout.width
                    ) / 2f

        val textY =
            rectangle.y +
                (
                    rectangle.height +
                        glyphLayout.height
                    ) / 2f

        font.draw(
            batch,
            text,
            textX,
            textY
        )
    }

    private fun romanNumeral(
        level: Int
    ): String {
        return when (
            level.coerceIn(
                1,
                5
            )
        ) {
            1 -> "I"
            2 -> "II"
            3 -> "III"
            4 -> "IV"
            else -> "V"
        }
    }

    fun dispose() {
        whitePixel.dispose()
    }

    private fun createWhitePixelTexture():
        Texture {

        val pixmap =
            Pixmap(
                1,
                1,
                Pixmap.Format.RGBA8888
            )

        pixmap.setColor(
            Color.WHITE
        )

        pixmap.fill()

        val texture =
            Texture(
                pixmap
            )

        pixmap.dispose()

        return texture
    }

    private fun renderValidMarketDropTargets(
        layout: MarketLayout,
        marketSlots: List<MarketSlot>,
        interactionState:
        MarketInteractionState
    ) {
        val draggedOfferIndex =
            interactionState
                .draggedMarketOfferIndex
                ?: return

        val offer =
            marketSlots.getOrNull(
                draggedOfferIndex
            )
                ?.offer
                ?: return

        when (
            offer.productType
        ) {
            MarketProductType.ITEM -> {
                drawHighlightBorder(
                    rectangle =
                        layout.itemPanel
                )
            }

            MarketProductType.STAT -> {
                drawHighlightBorder(
                    rectangle =
                        layout.statsPanel
                )
            }

            MarketProductType.WEAPON -> {
                val weaponDefinition =
                    offer.weaponDefinition
                        ?: return

                layout.weaponSlots
                    .forEachIndexed {
                            slotIndex,
                            rectangle ->

                        val existingWeapon =
                            weaponInventory.weaponAt(
                                slotIndex
                            )

                        val validTarget =
                            when {
                                existingWeapon == null -> {
                                    true
                                }

                                existingWeapon
                                    .definition.id !=
                                    weaponDefinition.id -> {
                                    false
                                }

                                existingWeapon.level >=
                                    WeaponInstance
                                        .MAXIMUM_LEVEL -> {
                                    false
                                }

                                else -> {
                                    true
                                }
                            }

                        if (validTarget) {
                            drawHighlightBorder(
                                rectangle =
                                    rectangle
                            )
                        }
                    }
            }
        }
    }
    private fun renderDraggedMarketOffer(
        marketSlots:
        List<MarketSlot>,

        interactionState:
        MarketInteractionState
    ) {
        val offerIndex =
            interactionState
                .draggedMarketOfferIndex
                ?: return

        val offer =
            marketSlots.getOrNull(
                offerIndex
            )
                ?.offer
                ?: return

        val previewRectangle =
            Rectangle(
                interactionState
                    .pointerPosition.x -
                    95f,

                interactionState
                    .pointerPosition.y -
                    35f,

                190f,
                70f
            )

        drawPanel(
            rectangle =
                previewRectangle,

            fillColor =
                Color(
                    0.12f,
                    0.24f,
                    0.18f,
                    0.95f
                )
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            offer.title,
            previewRectangle.x + 8f,
            previewRectangle.y +
                previewRectangle.height -
                18f,
            previewRectangle.width - 16f,
            Align.center,
            true
        )

        font.color =
            Color.GOLD

        font.draw(
            batch,
            "${offer.price} Gold Coin",
            previewRectangle.x + 8f,
            previewRectangle.y + 19f,
            previewRectangle.width - 16f,
            Align.center,
            false
        )
    }

    /*
     * MarketRenderer sınıfını kapatan tek parantez.
     */
}
