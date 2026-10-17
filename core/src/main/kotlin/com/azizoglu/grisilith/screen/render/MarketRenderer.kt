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
import com.azizoglu.grisilith.statistics.StatPresentationGroup
import com.azizoglu.grisilith.weapon.runtime.WeaponInventory
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack
import com.azizoglu.grisilith.ui.icon.ContentIconRenderer
import com.azizoglu.grisilith.ui.stat.StatSymbolRenderer
import com.azizoglu.grisilith.ui.theme.UiComponentRenderer
import com.badlogic.gdx.utils.Align
import com.azizoglu.grisilith.screen.market.MarketInteractionState
import com.azizoglu.grisilith.settings.GameSettings
import com.azizoglu.grisilith.ui.theme.ButtonVisualState
import com.azizoglu.grisilith.ui.theme.PanelVisualStyle
import com.azizoglu.grisilith.economy.WeaponSalePriceCalculator
import com.azizoglu.grisilith.weapon.definition.WeaponAttackPattern
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.azizoglu.grisilith.item.definition.ItemDefinition
import com.azizoglu.grisilith.item.definition.ItemRarity
import java.util.Locale
import kotlin.math.roundToInt
import com.azizoglu.grisilith.weapon.runtime.WeaponInstance
import com.azizoglu.grisilith.weapon.definition.WeaponDefinition

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
    ItemInventory,

    private val runGrisilithCoinsProvider:
    () -> Int
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

    private val goldCoinRenderer =
        GoldCoinRenderer()

    private val grisilithCoinRenderer =
        GrisilithCoinRenderer()

    private val characterStatsRenderer =
        CharacterStatsPanelRenderer(batch, font) { playerStats }

    private val marketBackground by lazy {
        Texture(Gdx.files.internal("ui/market/traveling_merchant_tent.png")).also {
            it.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }
    }

    private val weaponOfferGlow by lazy {
        Texture(Gdx.files.internal("ui/market/effects/weapon_offer_glow.png")).also {
            it.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }
    }

    private val legendaryOfferGlow by lazy {
        Texture(Gdx.files.internal("ui/chests/effects/legendary_reward_glow.png"))
    }

    private val artifactOfferGlow by lazy {
        Texture(Gdx.files.internal("ui/chests/effects/artifact_reward_glow.png"))
    }

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
        itemScrollOffset: Float,
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

        renderCharacterAndStatsPanel(layout, interactionState)

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

        renderHoverTooltip(layout, marketSlots, itemScrollOffset, interactionState)

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
        batch.color = Color.WHITE
        batch.draw(marketBackground, layout.fullScreen.x, layout.fullScreen.y, layout.fullScreen.width, layout.fullScreen.height)
        drawFilledRectangle(layout.fullScreen, Color(0.015f, 0.02f, 0.03f, 0.34f))
    }

    private fun renderTopBar(
        layout: MarketLayout,
        rerollPrice: Int,
        interactionState:
        MarketInteractionState
    ) {
        UiComponentRenderer.drawPanel(batch, layout.stagePanel, PanelVisualStyle.TITLE)
        UiComponentRenderer.drawButton(
            batch,
            layout.goldPanel,
            if (layout.goldPanel.contains(interactionState.pointerPosition)) ButtonVisualState.HOVERED else ButtonVisualState.NORMAL
        )
        UiComponentRenderer.drawButton(
            batch,
            layout.returnButton,
            if (layout.returnButton.contains(interactionState.pointerPosition)) ButtonVisualState.HOVERED else ButtonVisualState.NORMAL
        )

        val completedStage = (stageController.currentStage - 1).coerceAtLeast(1)
        drawCenteredText("STAGE $completedStage", layout.stagePanel, Color.GOLD)

        val generalFailure =
            interactionState
                .marketFailureFeedback
                ?.takeIf { feedback ->
                    feedback.offerIndex == null
                }

        val shortcut = if (GameSettings.showAbilityShortcuts) " [R]" else ""
        val rerollText = when {
            generalFailure != null -> generalFailure.message
            rerollPrice <= 0 -> "REROLL: FREE$shortcut"
            else -> "REROLL: $rerollPrice$shortcut"
        }

        val currencyX = layout.stagePanel.x + layout.stagePanel.width + 24f
        val currencyY = layout.stagePanel.y + 31f
        goldCoinRenderer.render(batch, font, sessionWallet.goldCoins, currencyX, currencyY, 25f)
        grisilithCoinRenderer.render(batch, font, runGrisilithCoinsProvider(), currencyX + 116f, currencyY, 25f)

        drawCenteredText(rerollText, layout.goldPanel, if (generalFailure != null) Color(1f, 0.35f, 0.35f, 1f) else Color.GOLD)
        drawCenteredText("RETURN TO GAME", layout.returnButton, Color.WHITE)
    }

    private fun renderMarketOffers(
        layout: MarketLayout,
        marketSlots: List<MarketSlot>,
        interactionState:
        MarketInteractionState
    ) {
        UiComponentRenderer.drawPanel(batch, layout.weaponSellArea, PanelVisualStyle.MAIN)
        layout.offerCards.drop(1).forEach { card ->
            UiComponentRenderer.drawSimpleVerticalSeparator(
                batch,
                Rectangle(card.x - 2f, layout.weaponSellArea.y + 15f, 4f, layout.weaponSellArea.height - 30f)
            )
        }

        layout.offerCards.forEachIndexed { index, card ->
            val slot = marketSlots.getOrNull(index)
            renderCompactOffer(slot, card)
            renderOfferFailureOverlay(index, card, interactionState)

            val lock = layout.lockButtons[index]
            val hovered = lock.contains(interactionState.pointerPosition)
            val state = when {
                slot?.isSold == true -> ButtonVisualState.DISABLED
                hovered -> ButtonVisualState.HOVERED
                slot?.isLocked == true -> ButtonVisualState.SELECTED
                else -> ButtonVisualState.NORMAL
            }
            UiComponentRenderer.drawButton(batch, lock, state)
            val label = when {
                slot?.isSold == true -> "SOLD"
                slot?.isLocked == true -> "LOCKED"
                else -> "LOCK"
            }
            drawCenteredText(label, lock, if (slot?.isSold == true) Color.LIGHT_GRAY else Color.WHITE)
        }
    }

    private fun renderCompactOffer(slot: MarketSlot?, card: Rectangle) {
        val offer = slot?.offer
        if (slot?.isSold == true) {
            drawCenteredText("SOLD", Rectangle(card.x, card.y + 48f, card.width, card.height - 48f), Color.GRAY)
            return
        }
        if (offer == null) return

        val iconSize = minOf(86f, card.width - 42f)
        val icon = Rectangle(card.x + (card.width - iconSize) / 2f, card.y + 105f, iconSize, iconSize)
        drawMarketOfferGlow(offer.productType, offer.itemDefinition, icon, card)
        when (offer.productType) {
            MarketProductType.WEAPON -> offer.weaponDefinition?.let { ContentIconRenderer.draw(batch, it, icon) }
            MarketProductType.ITEM -> offer.itemDefinition?.let { ContentIconRenderer.draw(batch, it, icon) }
            MarketProductType.STAT -> offer.statType?.let { StatSymbolRenderer.drawIcon(batch, it, icon.x, icon.y, iconSize) }
        }

        val oldX = font.data.scaleX
        val oldY = font.data.scaleY
        font.data.setScale(0.60f)
        drawFittedItemName(offer.title, Rectangle(card.x + 5f, card.y + 69f, card.width - 10f, 22f))
        font.data.setScale(0.66f)
        font.color = Color.GOLD
        font.draw(batch, "${offer.price} GOLD", card.x, card.y + 68f, card.width, Align.center, false)
        font.data.setScale(oldX, oldY)
    }

    private fun drawMarketOfferGlow(
        productType: MarketProductType,
        item: ItemDefinition?,
        icon: Rectangle,
        card: Rectangle
    ) {
        val texture: Texture
        val multiplier: Float
        val alpha: Float
        when {
            productType == MarketProductType.WEAPON -> {
                texture = weaponOfferGlow
                multiplier = 1.48f
                alpha = 0.82f
            }
            item?.rarity == ItemRarity.ARTIFACT -> {
                texture = artifactOfferGlow
                multiplier = 1.42f
                alpha = 0.88f
            }
            item?.rarity == ItemRarity.LEGENDARY -> {
                texture = legendaryOfferGlow
                multiplier = 1.18f
                alpha = 0.70f
            }
            else -> return
        }
        val size = (icon.width * multiplier).coerceAtMost(minOf(card.width - 8f, card.height - 8f))
        val drawX = (icon.x + icon.width / 2f - size / 2f).coerceIn(card.x + 4f, card.x + card.width - size - 4f)
        val drawY = (icon.y + icon.height / 2f - size / 2f).coerceIn(card.y + 4f, card.y + card.height - size - 4f)
        val oldColor = Color(batch.color)
        batch.color = Color(1f, 1f, 1f, alpha)
        batch.draw(texture, drawX, drawY, size, size)
        batch.color = oldColor
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
                cardRectangle.x + 10f,
                cardRectangle.y + 62f,
                cardRectangle.width - 20f,
                cardRectangle.height - 70f
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
        val oldScaleX = font.data.scaleX
        val oldScaleY = font.data.scaleY
        val contentWidth = rectangle.width - 24f
        val contentHeight = rectangle.height - 24f
        var scale = 0.72f
        do {
            font.data.setScale(scale)
            glyphLayout.setText(font, text, color, contentWidth, Align.center, true)
            if (glyphLayout.width <= contentWidth && glyphLayout.height <= contentHeight) break
            scale -= 0.04f
        } while (scale >= 0.46f)

        font.color = color
        font.draw(
            batch,
            text,
            rectangle.x + 12f,
            rectangle.y + (rectangle.height + glyphLayout.height) / 2f,
            contentWidth,
            Align.center,
            true
        )
        font.data.setScale(oldScaleX, oldScaleY)
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

        val iconDefinition =
            offer.weaponDefinition
                ?: offer.itemDefinition

        val iconSize =
            minOf(72f, cardRectangle.width * 0.28f)

        val iconBounds =
            Rectangle(
                cardRectangle.x + cardRectangle.width - iconSize - TEXT_PADDING,
                cardRectangle.y + cardRectangle.height - iconSize - 15f,
                iconSize,
                iconSize
            )

        val renderedIcon =
            iconDefinition != null &&
                ContentIconRenderer.draw(
                    batch = batch,
                    definition = iconDefinition,
                    bounds = iconBounds
                )

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
                20f
        )

        if (offer.productType == MarketProductType.STAT && offer.statType != null) {
            StatSymbolRenderer.drawLabel(
                batch = batch,
                font = font,
                statType = offer.statType,
                x = cardRectangle.x + TEXT_PADDING,
                baselineY = cardRectangle.y + cardRectangle.height - 43f,
                iconSize = 20f,
                width = cardRectangle.width - TEXT_PADDING * 2f
            )
        } else {
            font.color = Color.WHITE
            font.draw(
                batch, offer.title,
                cardRectangle.x + TEXT_PADDING,
                cardRectangle.y + cardRectangle.height - 43f,
                cardRectangle.width - TEXT_PADDING * 2f - if (renderedIcon) iconSize + 6f else 0f,
                Align.left, true
            )
        }

        val oldScaleX =
            font.data.scaleX

        val oldScaleY =
            font.data.scaleY

        font.data.setScale(
            0.78f
        )

        if (
            offer.productType ==
            MarketProductType.WEAPON &&
            offer.weaponDefinition != null
        ) {
            renderMarketWeaponInformation(
                weaponDefinition =
                    offer.weaponDefinition,

                cardRectangle =
                    cardRectangle
            )
        } else {
            font.color =
                Color.LIGHT_GRAY

            StatSymbolRenderer.drawRichText(
                batch, font, offer.description,
                cardRectangle.x + TEXT_PADDING,
                cardRectangle.y + cardRectangle.height - 74f,
                cardRectangle.width - TEXT_PADDING * 2f,
                lineHeight = 16f,
                iconSize = 13f
            )
        }

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

    private fun renderMarketWeaponInformation(
        weaponDefinition:
        WeaponDefinition,

        cardRectangle:
        Rectangle
    ) {
        val startX =
            cardRectangle.x +
                TEXT_PADDING

        var currentY =
            cardRectangle.y +
                cardRectangle.height -
                74f

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            "Type: " +
                weaponTypeText(
                    weaponDefinition
                ),
            startX,
            currentY
        )

        currentY -=
            20f

        val orderedScaling =
            weaponDefinition
                .orderedDamageScaling()

        val primaryScaling =
            orderedScaling
                .firstOrNull()

        /*
         * Ana scaling Gold renkte ve ilk sırada.
         */
        if (primaryScaling != null) {
            font.color =
                Color.GOLD

            StatSymbolRenderer.drawRichText(
                batch, font,
                "Main Scaling: ${primaryScaling.first.displayName}",
                startX, currentY, 250f,
                lineHeight = 17f,
                iconSize = 14f
            )

            currentY -=
                20f
        }

        val secondaryScaling =
            orderedScaling
                .drop(1)

        if (secondaryScaling.isNotEmpty()) {
            font.color =
                Color.LIGHT_GRAY

            StatSymbolRenderer.drawRichText(
                batch, font,
                "Secondary: " + secondaryScaling.joinToString(", ") { it.first.displayName },
                startX, currentY,
                cardRectangle.width - TEXT_PADDING * 2f,
                lineHeight = 17f,
                iconSize = 14f
            )
        }
    }

    private fun weaponTypeText(
        definition:
        WeaponDefinition
    ): String {
        return when {
            com.azizoglu.grisilith
                .content.ContentTag
                .SPELL_FOCUS in
                definition.tags -> {
                "Spell Focus"
            }

            com.azizoglu.grisilith
                .content.ContentTag
                .MELEE_WEAPON in
                definition.tags -> {
                "Melee Weapon"
            }

            com.azizoglu.grisilith
                .content.ContentTag
                .RANGED_WEAPON in
                definition.tags -> {
                "Ranged Weapon"
            }

            definition.normalAttackSpeedStat ==
                StatType.HASTE -> {
                "Spell Focus"
            }

            else -> {
                "Weapon"
            }
        }
    }

    private fun renderWeaponInventory(
        layout: MarketLayout
    ) {
        UiComponentRenderer.drawHorizontalSeparator(
            batch,
            Rectangle(layout.weaponPanel.x + 12f, layout.weaponPanel.y + layout.weaponPanel.height - 9f, layout.weaponPanel.width - 24f, 8f)
        )
        font.color = Color.WHITE
        font.draw(
            batch,
            "WEAPONS  (${weaponInventory.equippedWeaponCount}/${WeaponInventory.WEAPON_SLOT_COUNT})",
            layout.weaponPanel.x,
            layout.weaponPanel.y + layout.weaponPanel.height - 15f,
            layout.weaponPanel.width,
            Align.center,
            false
        )

        layout.weaponSlots.forEachIndexed { index, slot ->
            val weapon = weaponInventory.weaponAt(index)
            UiComponentRenderer.drawSlot(batch, slot, locked = weapon == null)
            if (weapon != null) {
                ContentIconRenderer.draw(batch, weapon.definition, Rectangle(slot.x + 3f, slot.y + 3f, slot.width - 6f, slot.height - 6f))
                val oldX = font.data.scaleX
                val oldY = font.data.scaleY
                font.data.setScale(0.64f)
                val levelPanel = Rectangle(slot.x - 1f, slot.y - 28f, slot.width + 2f, 23f)
                drawFilledRectangle(levelPanel, Color(0.025f, 0.03f, 0.04f, 0.94f))
                drawHighlightBorder(levelPanel, Color(0.48f, 0.35f, 0.16f, 1f), 2f)
                font.color = Color.GOLD
                font.draw(batch, "LV. ${romanNumeral(weapon.level)}", levelPanel.x, levelPanel.y + 15f, levelPanel.width, Align.center, false)
                font.data.setScale(oldX, oldY)
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
        itemScrollOffset: Float
    ) {
        UiComponentRenderer.drawHorizontalSeparator(
            batch,
            Rectangle(layout.itemPanel.x, layout.itemPanel.y + layout.itemPanel.height - 8f, layout.itemPanel.width, 8f)
        )

        val groupedItems = groupedItemEntries()
        font.color = Color.WHITE
        font.draw(
            batch,
            "ITEMS  (${itemInventory.itemCount})",
            layout.itemPanel.x,
            layout.itemPanel.y + layout.itemPanel.height - 15f,
            layout.itemPanel.width,
            Align.center,
            false
        )

        if (groupedItems.isEmpty()) {
            drawCenteredText("NO ITEMS", layout.itemPanel, Color.GRAY)
            return
        }

        val safeScrollOffset = itemScrollOffset.coerceIn(0f, MarketLayout.maximumItemScrollPixels(groupedItems.size, layout.itemPanel))
        val clipBounds = Rectangle(
            layout.itemPanel.x,
            layout.itemPanel.y + 14f,
            layout.itemPanel.width,
            layout.itemPanel.height - 70f
        )
        val scissors = Rectangle()
        batch.flush()
        ScissorStack.calculateScissors(camera, batch.transformMatrix, clipBounds, scissors)
        val clipped = ScissorStack.pushScissors(scissors)
        groupedItems.forEachIndexed { index, entry ->
            val slot = MarketLayout.itemSlotRectangle(layout.itemPanel, index, safeScrollOffset)
            if (!slot.overlaps(clipBounds)) return@forEachIndexed
            ContentIconRenderer.draw(batch, entry.definition, slot)

            if (entry.count > 1) {
                val oldX = font.data.scaleX
                val oldY = font.data.scaleY
                font.data.setScale(0.62f)
                font.color = Color.BLACK
                font.draw(batch, entry.count.toString(), slot.x + slot.width - 27f, slot.y + 18f, 22f, Align.right, false)
                font.color = Color.WHITE
                font.draw(batch, entry.count.toString(), slot.x + slot.width - 28f, slot.y + 19f, 22f, Align.right, false)
                font.data.setScale(oldX, oldY)
            }

            drawFittedItemName(
                entry.definition.displayName,
                Rectangle(slot.x - 8f, slot.y - 28f, slot.width + 16f, 22f)
            )
        }
        if (clipped) {
            batch.flush()
            ScissorStack.popScissors()
        }
    }

    private fun drawFittedItemName(text: String, bounds: Rectangle) {
        val oldX = font.data.scaleX
        val oldY = font.data.scaleY
        var scale = 0.68f
        font.data.setScale(scale)
        glyphLayout.setText(font, text)
        if (glyphLayout.width > bounds.width) {
            scale = (scale * bounds.width / glyphLayout.width).coerceAtLeast(0.46f)
            font.data.setScale(scale)
        }
        font.color = Color.LIGHT_GRAY
        font.draw(batch, text, bounds.x, bounds.y + 16f, bounds.width, Align.center, false)
        font.data.setScale(oldX, oldY)
    }

    private fun renderCharacterAndStatsPanel(layout: MarketLayout, interactionState: MarketInteractionState) {
        UiComponentRenderer.drawPanel(batch, layout.characterPanel, PanelVisualStyle.MAIN)
        ContentIconRenderer.drawCharacterPortrait(batch, selectedCharacter.displayName, layout.characterPortrait)

        val oldX = font.data.scaleX
        val oldY = font.data.scaleY
        font.data.setScale(0.78f)
        font.color = Color.CYAN
        font.draw(
            batch,
            selectedCharacter.displayName,
            layout.characterPanel.x,
            layout.characterPortrait.y - 8f,
            layout.characterPanel.width,
            Align.center,
            false
        )
        font.data.setScale(oldX, oldY)

        characterStatsRenderer.render(
            layout.statsPanel,
            interactionState.pointerPosition,
            layout.fullScreen,
            drawBackground = false,
            contentScale = 1.48f
        )
    }

    private fun renderHoverTooltip(
        layout: MarketLayout,
        marketSlots: List<MarketSlot>,
        itemScrollOffset: Float,
        interactionState: MarketInteractionState
    ) {
        if (interactionState.isDetailModalOpen || interactionState.isAnyConfirmationOpen ||
            interactionState.isDraggingWeapon || interactionState.isDraggingMarketOffer
        ) return

        val pointer = interactionState.pointerPosition
        var title: String? = null
        var description: String? = null

        val offerIndex = layout.offerCards.indexOfFirst { it.contains(pointer) }
        if (offerIndex >= 0 && !layout.lockButtons[offerIndex].contains(pointer)) {
            marketSlots.getOrNull(offerIndex)?.takeUnless { it.isSold }?.offer?.let { offer ->
                title = offer.title
                description = offer.weaponDefinition?.let { com.azizoglu.grisilith.weapon.definition.WeaponPresentation.hover(it,offer.productLevel) } ?: offer.description
            }
        }

        if (title == null) {
            val weaponIndex = layout.weaponSlots.indexOfFirst { it.contains(pointer) }
            if (weaponIndex >= 0) {
                weaponInventory.weaponAt(weaponIndex)?.let { weapon ->
                    title = "${weapon.displayName}  ·  LEVEL ${romanNumeral(weapon.level)}"
                    description = com.azizoglu.grisilith.weapon.definition.WeaponPresentation.hover(weapon)
                }
            }
        }

        if (title == null) {
            val grouped = groupedItemEntries()
            val safeOffset = itemScrollOffset.coerceIn(0f, MarketLayout.maximumItemScrollPixels(grouped.size, layout.itemPanel))
            val visibleIndex = MarketLayout.itemIndexAt(layout.itemPanel, grouped.size, safeOffset, pointer.x, pointer.y)
            if (visibleIndex != null) {
                grouped.getOrNull(visibleIndex)?.let { entry ->
                    title = if (entry.count > 1) "${entry.definition.displayName}  x${entry.count}" else entry.definition.displayName
                    description = entry.definition.description
                }
            }
        }

        val tooltipTitle = title ?: return
        val tooltipDescription = description ?: return
        AdaptiveTooltipRenderer.draw(
            batch, font, camera, cameraWidth, cameraHeight, pointer,
            tooltipTitle, tooltipDescription, maxWidth = 440f
        )
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
                20f
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
            selectedCharacter.trait.displayName +
                ": " +
                selectedCharacter.trait.description,
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
                19f
        )

        val oldScaleX =
            font.data.scaleX

        val oldScaleY =
            font.data.scaleY

        font.data.setScale(
            0.70f
        )

        val gap = 5f
        val groupHeight = (layout.statsPanel.height - 45f - gap) / 2f
        val groupWidth = (layout.statsPanel.width - TEXT_PADDING * 2f - gap) / 2f
        StatPresentationGroup.ordered.forEachIndexed { index, group ->
            val column = index % 2
            val gridRow = index / 2
            val bounds = Rectangle(
                layout.statsPanel.x + TEXT_PADDING + column * (groupWidth + gap),
                layout.statsPanel.y + 7f + (1 - gridRow) * (groupHeight + gap),
                groupWidth,
                groupHeight
            )
            drawPanel(bounds, Color(0.075f, 0.09f, 0.14f, 1f))
            font.data.setScale(0.48f)
            font.color = Color(0.72f, 0.78f, 0.90f, 1f)
            font.draw(batch, group.title, bounds.x, bounds.y + bounds.height - 6f, bounds.width, Align.center, false)
            val lineHeight = ((bounds.height - 19f) / group.stats.size).coerceAtMost(13f)
            group.stats.forEachIndexed { row, statType ->
                val y = bounds.y + bounds.height - 20f - row * lineHeight
                font.data.setScale(0.54f)
                StatSymbolRenderer.drawLabel(
                    batch = batch,
                    font = font,
                    statType = statType,
                    x = bounds.x + 4f,
                    baselineY = y,
                    iconSize = 11f,
                    width = bounds.width - 29f
                )
                font.color = Color.WHITE
                font.draw(batch, playerStats.totalScore(statType).toString(), bounds.x + bounds.width - 24f, y)
            }
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
                19f
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

        val orderedDamageScaling =
            definition
                .orderedDamageScaling()

        val primaryDamageScaling =
            orderedDamageScaling
                .firstOrNull()

        val secondaryDamageScaling =
            orderedDamageScaling
                .drop(1)

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

        /*
    * Sol taraftaki bilgi sayısı arttığı için bu
    * bölüm normal HUD fontundan biraz daha küçük
    * çizilir.
    */
        val oldInformationScaleX =
            font.data.scaleX

        val oldInformationScaleY =
            font.data.scaleY

        font.data.setScale(
            0.76f
        )

        val informationX =
            layout.weaponDetailPanel.x +
                20f

        var informationY =
            layout.weaponDetailPanel.y +
                layout.weaponDetailPanel.height -
                78f

        /*
         * Küçültülmüş font için kontrollü satır aralığı.
         */
        val informationLineSpacing =
            18f

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            "Weapon Type: " +
                weaponTypeText(
                    definition
                ),
            informationX,
            informationY
        )

        informationY -=
            informationLineSpacing

        /*
         * Ana damage scaling her zaman ilk sırada
         * ve Gold renkte görünür.
         */
        if (primaryDamageScaling != null) {
            font.color =
                Color.GOLD

            StatSymbolRenderer.drawRichText(
                batch, font,
                "Main Damage Scaling: ${primaryDamageScaling.first.displayName}",
                informationX, informationY, 230f,
                lineHeight = 16f,
                iconSize = 13f
            )

            informationY -=
                informationLineSpacing
        }

        /*
         * Varsa ikincil damage scaling değerleri.
         */
        if (secondaryDamageScaling.isNotEmpty()) {
            font.color =
                Color.LIGHT_GRAY

            StatSymbolRenderer.drawRichText(
                batch, font,
                "Secondary Scaling: " + secondaryDamageScaling.joinToString(", ") { it.first.displayName },
                informationX, informationY, 230f,
                lineHeight = 16f,
                iconSize = 13f
            )

            informationY -=
                informationLineSpacing
        }

        /*
         * Kalan istatistikler scaling satırlarının
         * hemen altından devam eder.
         */
        font.color =
            Color.LIGHT_GRAY

        StatSymbolRenderer.drawRichText(
            batch, font, statsText,
            informationX,
            informationY,
            230f,
            lineHeight = 16f,
            iconSize = 13f
        )

        /*
         * Sonraki açıklamalar ve kapatma yazısı normal
         * font boyutuyla çizilmeye devam etsin.
         */
        font.data.setScale(
            oldInformationScaleX,
            oldInformationScaleY
        )

        font.color =
            Color.LIGHT_GRAY

        WeaponDetailPanel.drawCopy(batch,font,
            com.azizoglu.grisilith.weapon.definition.WeaponPresentation.hover(weapon),
            Rectangle(layout.weaponDetailPanel.x+275f,layout.weaponDetailPanel.y+50f,
                layout.weaponDetailPanel.width-295f,layout.weaponDetailPanel.height-130f))

        font.color =
            Color.GRAY

        val oldCloseTextScaleX =
            font.data.scaleX

        val oldCloseTextScaleY =
            font.data.scaleY

        font.data.setScale(
            0.72f
        )

        font.draw(
            batch,
            "Click outside to close.",
            layout.weaponDetailPanel.x +
                20f,
            layout.weaponDetailPanel.y +
                16f
        )

        font.data.setScale(
            oldCloseTextScaleX,
            oldCloseTextScaleY
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

                    WeaponAttackPattern.EXTENDED_PHYSICAL ->
                        com.azizoglu.grisilith.weapon.definition.catalog.NewPhysicalWeapons.patternLabel(weapon.definition.id.value)
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

        StatSymbolRenderer.drawRichText(
            batch, font, definition.description,
            layout.weaponDetailPanel.x + 20f,
            layout.weaponDetailPanel.y + layout.weaponDetailPanel.height - 84f,
            layout.weaponDetailPanel.width - 40f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "Owned: $copyCount / $maximumCopies\n\nTotal Bonus:",
            layout.weaponDetailPanel.x +
                20f,
            layout.weaponDetailPanel.y +
                105f
        )

        StatSymbolRenderer.drawModifiersVertical(
            batch = batch,
            font = font,
            modifiers = definition.statModifiers.map { modifier ->
                modifier.statType to modifier.amount * copyCount
            },
            x = layout.weaponDetailPanel.x + 20f,
            firstBaselineY = layout.weaponDetailPanel.y + 55f,
            lineHeight = 24f,
            iconSize = 18f
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

        UiComponentRenderer.drawDangerButton(
            batch,
            layout.mergeConfirmButton,
            if (layout.mergeConfirmButton.contains(interactionState.pointerPosition)) ButtonVisualState.HOVERED else ButtonVisualState.NORMAL
        )

        drawCenteredText(
            text =
                "SELL",

            rectangle =
                layout.mergeConfirmButton,

            color =
                Color.WHITE
        )

        UiComponentRenderer.drawButton(
            batch,
            layout.mergeCancelButton,
            if (layout.mergeCancelButton.contains(interactionState.pointerPosition)) ButtonVisualState.HOVERED else ButtonVisualState.NORMAL
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
        rectangle: Rectangle,
        highlightColor: Color = Color(0.30f, 1f, 0.48f, 1f),
        thickness: Float = 4f
    ) {
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
        UiComponentRenderer.drawPanel(
            batch = batch,
            bounds = rectangle
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
        marketBackground.dispose()
        weaponOfferGlow.dispose()
        legendaryOfferGlow.dispose()
        artifactOfferGlow.dispose()
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
