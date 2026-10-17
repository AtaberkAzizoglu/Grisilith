package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.profile.PlayerProfileRepository

import com.azizoglu.grisilith.content.ContentCategory
import com.azizoglu.grisilith.profile.PlayerProfile
import com.azizoglu.grisilith.ui.icon.ContentIconRenderer
import com.azizoglu.grisilith.ui.theme.ButtonVisualState
import com.azizoglu.grisilith.ui.theme.PanelVisualStyle
import com.azizoglu.grisilith.ui.theme.UiComponentRenderer
import com.azizoglu.grisilith.unlock.UnlockDefinition
import com.azizoglu.grisilith.unlock.UnlockRegistry
import com.azizoglu.grisilith.unlock.UnlockType
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Align
import kotlin.math.sin

class MerchantRenderer(
    private val grisilithCoinRenderer: GrisilithCoinRenderer,
    private val purchasedStatPointRenderer: PurchasedStatPointRenderer
) {

    companion object {
        private const val VIEWPORT_WIDTH = 1280f
        private const val VIEWPORT_HEIGHT = 720f
        private const val TITLE_Y = 664f

        val MAIN_PANEL = Rectangle(24f, 24f, 1232f, 672f)
        val CATEGORY_PANEL = Rectangle(42f, 104f, 214f, 522f)
        val CATALOG_PANEL = Rectangle(270f, 104f, 478f, 522f)
        val DETAIL_PANEL = Rectangle(762f, 104f, 476f, 522f)
        val PRODUCT_VIEWPORT = Rectangle(286f, 130f, 426f, 400f)
        val PRODUCT_SCROLLBAR = Rectangle(720f, 130f, 18f, 400f)
        val FILTER_DROPDOWN = Rectangle(286f, 548f, 200f, 58f)
        val OWNED_FILTER = Rectangle(502f, 548f, 210f, 58f)
        val SHARED_DETAIL_VIEWPORT = Rectangle(774f,245f,450f,345f)
        val CURRENCY_PANEL = Rectangle(946f, 630f, 284f, 58f)
        val STAT_QUANTITY_INPUT = Rectangle(980f, 129f, 80f, 44f)
    }

    private val background =
        Texture(Gdx.files.internal("ui/market/merchant_background_hd.png")).also {
            it.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }

    private val whitePixel = createWhitePixel()
    private val glyphLayout = GlyphLayout()
    private val archiveRenderer=CollectionCategoryRenderer()
    private var detailEntry:com.azizoglu.grisilith.collection.CollectionEntry?=null
    private var detailId:String?=null
    private var detailScroll=0f
    private val detailScrollbarDrag=com.azizoglu.grisilith.ui.input.VerticalScrollbarDragController()
    fun updateDetailScrollbar(pointer:com.azizoglu.grisilith.screen.components.MenuPointer) {
        if(detailEntry==null)return
        val track=Rectangle(1227f,245f,7f,345f)
        detailScrollbarDrag.update(pointer.position,pointer.justPressed,pointer.isHeld,pointer.justReleased,
            Rectangle(track.x-4f,track.y,track.width+8f,track.height),
            UiComponentRenderer.bronzeScrollbarThumbBounds(track,345f/498f,detailScroll/153f)) { detailScroll=it*153f }
    }
    private fun sharedBounds()=Rectangle(SHARED_DETAIL_VIEWPORT.x,SHARED_DETAIL_VIEWPORT.y+SHARED_DETAIL_VIEWPORT.height-498f+detailScroll,SHARED_DETAIL_VIEWPORT.width,498f)
    fun scrollDetails(amount:Float):Boolean {
        if(detailEntry==null)return false
        detailScroll=(detailScroll+amount).coerceIn(0f,498f-SHARED_DETAIL_VIEWPORT.height)
        return true
    }
    private val productCardTexture =
        Texture(Gdx.files.internal("ui/market/merchant_product_card.png")).also {
            it.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }

    fun renderChrome(
        batch: SpriteBatch,
        bodyFont: BitmapFont,
        displayFont: BitmapFont,
        profile: PlayerProfile,
        selectedCategory: UnlockType,
        categoryRectangles: Map<UnlockType, Rectangle>,
        productsByType: Map<UnlockType, List<UnlockDefinition>>,
        showOwned: Boolean,
        ownedFilterPressed: Boolean,
        filterLabel: String?,
        filterDropdownOpen: Boolean,
        pressedCategory: UnlockType?,
        pointerPosition: Vector2,
        pointerHeld: Boolean
    ) {
        batch.color = Color.WHITE
        batch.draw(background, 0f, 0f, VIEWPORT_WIDTH, VIEWPORT_HEIGHT)
        batch.color = Color(0.025f, 0.02f, 0.035f, 0.34f)
        batch.draw(whitePixel, 0f, 0f, VIEWPORT_WIDTH, VIEWPORT_HEIGHT)
        batch.color = Color.WHITE

        UiComponentRenderer.drawStonePanel(batch, MAIN_PANEL)
        UiComponentRenderer.drawSimpleVerticalSeparator(batch, Rectangle(258f, 112f, 10f, 506f))
        UiComponentRenderer.drawSimpleVerticalSeparator(batch, Rectangle(751f, 112f, 10f, 506f))
        UiComponentRenderer.drawPanel(batch, CURRENCY_PANEL, PanelVisualStyle.HORIZONTAL)

        renderTitle(batch, displayFont)
        renderCurrencies(batch, bodyFont, profile)
        renderCategories(
            batch,
            bodyFont,
            displayFont,
            profile,
            selectedCategory,
            categoryRectangles,
            productsByType,
            pressedCategory,
            pointerPosition,
            pointerHeld
        )
        renderCatalogHeading(
            batch,
            bodyFont,
            displayFont,
            selectedCategory,
            showOwned,
            ownedFilterPressed,
            filterLabel,
            filterDropdownOpen,
            pointerPosition,
            pointerHeld
        )
        renderDetailHeading(batch, displayFont)
    }

    fun renderProductCards(
        batch: SpriteBatch,
        bodyFont: BitmapFont,
        displayFont: BitmapFont,
        profile: PlayerProfile,
        products: List<UnlockDefinition>,
        cardRectanglesById: Map<String, Rectangle>,
        selectedUnlockId: String?,
        hoveredUnlockId: String?,
        pressedUnlockId: String?,
        purchaseFlashId: String?,
        purchaseFlashAmount: Float
    ) {
        if (products.isEmpty()) {
            bodyFont.data.setScale(0.92f)
            bodyFont.color = Color(0.72f, 0.72f, 0.76f, 1f)
            bodyFont.draw(
                batch,
                "No products are available in this category.",
                PRODUCT_VIEWPORT.x + 20f,
                PRODUCT_VIEWPORT.y + PRODUCT_VIEWPORT.height / 2f,
                PRODUCT_VIEWPORT.width - 40f,
                Align.center,
                true
            )
            bodyFont.data.setScale(1f)
            return
        }

        products.forEach { product ->
            val card = cardRectanglesById[product.id] ?: return@forEach
            val owned = isOwned(product, profile)
            val dependenciesMet =
                UnlockRegistry.dependenciesAreMet(product, profile.unlockedIds)
            val state =
                when {
                    product.id == pressedUnlockId -> ButtonVisualState.PRESSED
                    product.id == selectedUnlockId -> ButtonVisualState.SELECTED
                    product.id == hoveredUnlockId -> ButtonVisualState.HOVERED
                    owned || !dependenciesMet -> ButtonVisualState.DISABLED
                    else -> ButtonVisualState.NORMAL
                }

            val previousColor = Color(batch.color)
            batch.color =
                when (state) {
                    ButtonVisualState.SELECTED -> Color(0.62f, 0.78f, 1f, 1f)
                    ButtonVisualState.HOVERED -> Color(1f, 0.94f, 0.78f, 1f)
                    ButtonVisualState.PRESSED -> Color(0.74f, 0.68f, 0.56f, 1f)
                    ButtonVisualState.DISABLED -> Color(0.52f, 0.52f, 0.54f, 0.78f)
                    ButtonVisualState.NORMAL -> Color.WHITE
                }
            batch.draw(productCardTexture, card.x, card.y, card.width, card.height)
            batch.color = previousColor

            if (product.id == purchaseFlashId && purchaseFlashAmount > 0f) {
                val pulse =
                    (0.10f + 0.10f * sin(purchaseFlashAmount * 18f))
                        .coerceAtLeast(0f)
                batch.color = Color(1f, 0.72f, 0.16f, pulse)
                batch.draw(
                    whitePixel,
                    card.x + 8f,
                    card.y + 8f,
                    card.width - 16f,
                    card.height - 16f
                )
                batch.color = Color.WHITE
            }

            val icon =
                Rectangle(
                    card.x + (card.width - 74f) / 2f,
                    card.y + 57f,
                    74f,
                    74f
                )
            UiComponentRenderer.drawSlot(
                batch,
                icon,
                selected = product.id == selectedUnlockId,
                locked = !dependenciesMet
            )

            val iconRendered =
                if (product.type == UnlockType.STAT_POINT) {
                    renderStatPointPurchaseIcon(batch, displayFont, icon)
                    true
                } else {
                    contentCategory(product.type)?.let { category ->
                    ContentIconRenderer.draw(
                        batch = batch,
                        contentId = product.id,
                        category = category,
                        bounds = icon,
                        locked = false
                    )
                    } ?: false
                }

            if (!iconRendered) {
                displayFont.data.setScale(0.78f)
                displayFont.color = Color(0.82f, 0.70f, 0.42f, 1f)
                displayFont.draw(
                    batch,
                    fallbackMark(product.type),
                    icon.x,
                    icon.y + 47f,
                    icon.width,
                    Align.center,
                    false
                )
            }

            bodyFont.data.setScale(0.82f)
            drawFittedCenteredLine(
                batch,
                bodyFont,
                product.displayName,
                Rectangle(card.x + 10f, card.y + 32f, card.width - 20f, 34f),
                if (owned) Color(0.65f, 0.65f, 0.68f, 1f) else Color.WHITE,
                minimumScale = 0.60f
            )

            bodyFont.data.setScale(0.78f)
            val footerBounds = Rectangle(card.x + 10f, card.y + 10f, card.width - 20f, 30f)
            when {
                owned ->
                    drawCenteredLine(
                        batch,
                        bodyFont,
                        "OWNED",
                        footerBounds,
                        Color(0.64f, 0.82f, 0.60f, 1f)
                    )
                !dependenciesMet ->
                    drawCenteredLine(
                        batch,
                        bodyFont,
                        "LOCKED",
                        footerBounds,
                        Color(0.72f, 0.42f, 0.42f, 1f)
                    )
                else ->
                    grisilithCoinRenderer.renderCentered(
                        batch,
                        bodyFont,
                        product.cost,
                        footerBounds,
                        iconSize = 17f,
                        color =
                            if (profile.grisilithCoins < product.cost) {
                                Color(0.92f, 0.35f, 0.30f, 1f)
                            } else {
                                Color.GOLD
                            }
                    )
            }
        }

        bodyFont.data.setScale(1f)
        displayFont.data.setScale(1f)
        batch.color = Color.WHITE
    }

    fun renderDetails(
        batch: SpriteBatch,
        bodyFont: BitmapFont,
        displayFont: BitmapFont,
        profile: PlayerProfile,
        selectedProduct: UnlockDefinition?,
        message: String?,
        messageIsError: Boolean,
        purchaseQuantity: Int,
        visibleRatio: Float,
        scrollRatio: Float,
        viewport: com.badlogic.gdx.utils.viewport.Viewport? = null
    ) {
        if (visibleRatio < 0.999f) {
            UiComponentRenderer.drawChronicleScrollbar(
                batch,
                PRODUCT_SCROLLBAR,
                visibleRatio,
                scrollRatio
            )
        }

        if (selectedProduct == null) {
            detailEntry=null;detailId=null;detailScroll=0f
            bodyFont.data.setScale(0.92f)
            bodyFont.color = Color(0.72f, 0.72f, 0.76f, 1f)
            bodyFont.draw(
                batch,
                "Select a product to inspect it.",
                DETAIL_PANEL.x + 28f,
                365f,
                DETAIL_PANEL.width - 56f,
                Align.center,
                true
            )
            return
        }

        val owned = isOwned(selectedProduct, profile)
        val dependenciesMet =
            UnlockRegistry.dependenciesAreMet(selectedProduct, profile.unlockedIds)
        val definition=com.azizoglu.grisilith.content.DefaultContentRegistry.instance.findById(com.azizoglu.grisilith.content.ContentId(selectedProduct.id))
        if(detailId!=selectedProduct.id) {detailScroll=0f;detailId=selectedProduct.id}
        detailEntry=definition?.takeIf{it.category!=ContentCategory.ITEM}?.let{com.azizoglu.grisilith.collection.CollectionEntry(it,owned,true)}
        val shared=detailEntry
        if(shared!=null && viewport!=null) {
            batch.flush()
            val lo=viewport.project(Vector2(SHARED_DETAIL_VIEWPORT.x,SHARED_DETAIL_VIEWPORT.y))
            val hi=viewport.project(Vector2(SHARED_DETAIL_VIEWPORT.x+SHARED_DETAIL_VIEWPORT.width,SHARED_DETAIL_VIEWPORT.y+SHARED_DETAIL_VIEWPORT.height))
            val clip=Rectangle(lo.x,lo.y,hi.x-lo.x,hi.y-lo.y)
            if(com.badlogic.gdx.scenes.scene2d.utils.ScissorStack.pushScissors(clip)) {
                archiveRenderer.renderDetails(batch,bodyFont,shared,sharedBounds())
                batch.flush();com.badlogic.gdx.scenes.scene2d.utils.ScissorStack.popScissors()
            }
            UiComponentRenderer.drawChronicleScrollbar(batch,Rectangle(1227f,245f,7f,345f),345f/498f,detailScroll/153f)
        } else {
        val icon = Rectangle(DETAIL_PANEL.x+(DETAIL_PANEL.width-120f)/2f, 424f, 120f, 120f)

        UiComponentRenderer.drawSlot(
            batch,
            icon,
            selected = true,
            locked = !dependenciesMet
        )
        val rendered =
            if (selectedProduct.type == UnlockType.STAT_POINT) {
                renderStatPointPurchaseIcon(batch, displayFont, icon)
                true
            } else {
                contentCategory(selectedProduct.type)?.let { category ->
                ContentIconRenderer.draw(
                    batch = batch,
                    contentId = selectedProduct.id,
                    category = category,
                    bounds = icon,
                    locked = false
                )
                } ?: false
            }
        if (!rendered) {
            displayFont.data.setScale(1.12f)
            displayFont.color = Color(0.82f, 0.70f, 0.42f, 1f)
            displayFont.draw(
                batch,
                fallbackMark(selectedProduct.type),
                icon.x,
                icon.y + 76f,
                icon.width,
                Align.center,
                false
            )
        }

        displayFont.data.setScale(0.72f)
        displayFont.color = Color(0.95f, 0.91f, 0.84f, 1f)
        displayFont.draw(
            batch,
            selectedProduct.displayName,
            DETAIL_PANEL.x + 24f,
            405f,
            DETAIL_PANEL.width - 48f,
            Align.center,
            true
        )

        bodyFont.data.setScale(0.72f)
        bodyFont.color = Color(0.72f, 0.66f, 0.54f, 1f)
        bodyFont.draw(
            batch,
            typeLabel(selectedProduct.type),
            DETAIL_PANEL.x + 24f,
            364f,
            DETAIL_PANEL.width - 48f,
            Align.center,
            false
        )

        UiComponentRenderer.drawHorizontalSeparator(
            batch,
            Rectangle(920f, 342f, 292f, 10f)
        )

        bodyFont.data.setScale(0.82f)
        bodyFont.color = Color(0.88f, 0.88f, 0.90f, 1f)
        bodyFont.draw(
            batch,
            compactDetailDescription(selectedProduct.description),
            DETAIL_PANEL.x + 28f,
            324f,
            DETAIL_PANEL.width - 56f,
            Align.left,
            true
        )

        if (selectedProduct.type == UnlockType.STAT_POINT) {
            bodyFont.data.setScale(0.76f)
            bodyFont.color = Color(0.52f, 0.84f, 0.90f, 1f)
            bodyFont.draw(
                batch,
                "Purchased stat pool: ${profile.purchasedStatPoints}",
                DETAIL_PANEL.x + 28f,
                258f,
                DETAIL_PANEL.width - 56f,
                Align.center,
                false
            )
        }

        bodyFont.data.setScale(0.82f)
        }
        val priceBounds = Rectangle(DETAIL_PANEL.x + 20f, 204f, DETAIL_PANEL.width - 40f, 32f)
        when {
            owned ->
                drawCenteredLine(
                    batch,
                    bodyFont,
                    "OWNED",
                    priceBounds,
                    Color(0.62f, 0.84f, 0.58f, 1f)
                )
            !dependenciesMet ->
                drawCenteredLine(
                    batch,
                    bodyFont,
                    "REQUIREMENTS NOT MET",
                    priceBounds,
                    Color(0.92f, 0.35f, 0.30f, 1f)
                )
            else -> {
                grisilithCoinRenderer.renderCentered(
                    batch,
                    bodyFont,
                    selectedProduct.cost * purchaseQuantity,
                    priceBounds,
                    iconSize = 21f,
                    color =
                        if (profile.grisilithCoins < selectedProduct.cost * purchaseQuantity) {
                            Color(0.92f, 0.35f, 0.30f, 1f)
                        } else {
                            Color.GOLD
                        }
                )
                if (profile.grisilithCoins < selectedProduct.cost * purchaseQuantity) {
                    bodyFont.data.setScale(0.66f)
                    drawCenteredLine(
                        batch,
                        bodyFont,
                        "NOT ENOUGH COINS",
                        Rectangle(DETAIL_PANEL.x + 20f, 229f, DETAIL_PANEL.width - 40f, 24f),
                        Color(0.92f, 0.35f, 0.30f, 1f)
                    )
                }
            }
        }

        if (!message.isNullOrBlank()) {
            bodyFont.data.setScale(0.72f)
            bodyFont.color =
                if (messageIsError) {
                    Color(0.96f, 0.36f, 0.31f, 1f)
                } else {
                    Color(0.68f, 0.90f, 0.58f, 1f)
                }
            bodyFont.draw(
                batch,
                message,
                DETAIL_PANEL.x + 20f,
                198f,
                DETAIL_PANEL.width - 40f,
                Align.center,
                false
            )
        }

        bodyFont.data.setScale(1f)
        displayFont.data.setScale(1f)
        batch.color = Color.WHITE
    }

    private fun renderTitle(batch: SpriteBatch, displayFont: BitmapFont) {
        displayFont.data.setScale(1.32f)
        displayFont.color = Color(0.95f, 0.91f, 0.82f, 1f)
        com.azizoglu.grisilith.ui.ScreenTitleArt.draw(batch,"MERCHANT",Rectangle(380f,620f,520f,52f))
        displayFont.data.setScale(1f)
    }

    private fun renderCurrencies(
        batch: SpriteBatch,
        bodyFont: BitmapFont,
        profile: PlayerProfile
    ) {
        bodyFont.data.setScale(0.94f)
        grisilithCoinRenderer.renderCentered(
            batch,
            bodyFont,
            profile.grisilithCoins,
            Rectangle(CURRENCY_PANEL.x + 10f, CURRENCY_PANEL.y + 7f, 158f, CURRENCY_PANEL.height - 14f),
            iconSize = 23f,
            color = Color.WHITE
        )
        purchasedStatPointRenderer.renderCentered(
            batch,
            bodyFont,
            profile.purchasedStatPoints,
            Rectangle(CURRENCY_PANEL.x + 168f, CURRENCY_PANEL.y + 7f, 106f, CURRENCY_PANEL.height - 14f),
            iconSize = 24f
        )
        bodyFont.data.setScale(1f)
    }

    private fun renderCategories(
        batch: SpriteBatch,
        bodyFont: BitmapFont,
        displayFont: BitmapFont,
        profile: PlayerProfile,
        selectedCategory: UnlockType,
        categoryRectangles: Map<UnlockType, Rectangle>,
        productsByType: Map<UnlockType, List<UnlockDefinition>>,
        pressedCategory: UnlockType?,
        pointerPosition: Vector2,
        pointerHeld: Boolean
    ) {
        displayFont.data.setScale(0.67f)
        displayFont.color = Color(0.90f, 0.78f, 0.48f, 1f)
        displayFont.draw(batch, "CATEGORIES", 48f, 592f, 202f, Align.center, false)
        UiComponentRenderer.drawHorizontalSeparator(batch, Rectangle(56f, 570f, 186f, 9f))

        categoryRectangles.forEach { (type, bounds) ->
            val state =
                when {
                    type == pressedCategory && pointerHeld -> ButtonVisualState.PRESSED
                    type == selectedCategory -> ButtonVisualState.SELECTED
                    bounds.contains(pointerPosition) -> ButtonVisualState.HOVERED
                    else -> ButtonVisualState.NORMAL
                }
            UiComponentRenderer.drawButton(batch, bounds, state)

            val products = productsByType[type].orEmpty()
            val ownedCount = products.count { isOwned(it, profile) }
            bodyFont.data.setScale(0.64f)
            drawFittedCenteredLine(
                batch,
                bodyFont,
                "${typeLabel(type)}  ($ownedCount / ${products.size})",
                Rectangle(bounds.x + 8f, bounds.y + 4f, bounds.width - 16f, bounds.height - 8f),
                Color.WHITE,
                minimumScale = 0.48f
            )
        }
        bodyFont.data.setScale(1f)
        displayFont.data.setScale(1f)
    }

    private fun renderCatalogHeading(
        batch: SpriteBatch,
        bodyFont: BitmapFont,
        displayFont: BitmapFont,
        type: UnlockType,
        showOwned: Boolean,
        filterPressed: Boolean,
        filterLabel: String?,
        filterDropdownOpen: Boolean,
        pointerPosition: Vector2,
        pointerHeld: Boolean
    ) {
        displayFont.data.setScale(0.67f)
        displayFont.color = Color(0.90f, 0.78f, 0.48f, 1f)
        displayFont.draw(batch, typeLabel(type), 292f, 622f)
        bodyFont.data.setScale(0.62f)
        bodyFont.color = Color(0.72f, 0.72f, 0.76f, 1f)
        if (filterLabel != null) {
            UiComponentRenderer.drawDropdown(batch, FILTER_DROPDOWN, filterDropdownOpen)
            bodyFont.data.setScale(0.72f)
            drawFittedCenteredLine(
                batch,
                bodyFont,
                filterLabel,
                Rectangle(FILTER_DROPDOWN.x + 10f, FILTER_DROPDOWN.y + 3f, FILTER_DROPDOWN.width - 20f, FILTER_DROPDOWN.height - 6f),
                Color.WHITE,
                minimumScale = 0.56f
            )
        }
        val filterState =
            when {
                filterPressed && pointerHeld -> ButtonVisualState.PRESSED
                OWNED_FILTER.contains(pointerPosition) -> ButtonVisualState.HOVERED
                !showOwned -> ButtonVisualState.SELECTED
                else -> ButtonVisualState.NORMAL
            }
        UiComponentRenderer.drawButton(batch, OWNED_FILTER, filterState)
        bodyFont.data.setScale(0.74f)
        drawFittedCenteredLine(
            batch,
            bodyFont,
            if (showOwned) "HIDE OWNED" else "SHOW OWNED",
            Rectangle(OWNED_FILTER.x + 12f, OWNED_FILTER.y + 3f, OWNED_FILTER.width - 24f, OWNED_FILTER.height - 6f),
            Color.WHITE,
            minimumScale = 0.58f
        )
        UiComponentRenderer.drawHorizontalSeparator(batch, Rectangle(286f, 534f, 426f, 9f))
        displayFont.data.setScale(1f)
        bodyFont.data.setScale(1f)
    }

    fun renderFilterDropdownOverlay(
        batch: SpriteBatch,
        font: BitmapFont,
        options: List<String>,
        selectedLabel: String?,
        open: Boolean
    ) {
        if (!open) return
        val rows = filterOptionBounds(options.size)
        if (rows.isNotEmpty()) {
            val bottom = rows.last().y
            UiComponentRenderer.drawPanel(
                batch,
                Rectangle(
                    FILTER_DROPDOWN.x - 14f,
                    bottom - 12f,
                    FILTER_DROPDOWN.width + 28f,
                    FILTER_DROPDOWN.y - bottom + 24f
                ),
                PanelVisualStyle.INNER
            )
        }
        rows.forEachIndexed { index, bounds ->
            val option = options[index]
            UiComponentRenderer.drawDropdownRow(batch, bounds, option == selectedLabel)
            font.data.setScale(0.72f)
            drawFittedCenteredLine(
                batch,
                font,
                option,
                Rectangle(bounds.x + 10f, bounds.y + 3f, bounds.width - 20f, bounds.height - 6f),
                Color.WHITE,
                minimumScale = 0.56f
            )
        }
        font.data.setScale(1f)
    }

    fun filterOptionBounds(optionCount: Int): List<Rectangle> =
        List(optionCount) { index ->
            Rectangle(
                FILTER_DROPDOWN.x,
                FILTER_DROPDOWN.y - (index + 1) * 52f,
                FILTER_DROPDOWN.width,
                52f
            )
        }

    fun renderStatQuantityInput(
        batch: SpriteBatch,
        font: BitmapFont,
        quantityText: String,
        focused: Boolean
    ) {
        UiComponentRenderer.drawStonePanel(batch, STAT_QUANTITY_INPUT)
        font.data.setScale(1.12f)
        drawCenteredLine(
            batch,
            font,
            quantityText.ifBlank { "_" },
            STAT_QUANTITY_INPUT,
            if (focused) Color(0.96f, 0.82f, 0.48f, 1f) else Color.WHITE,
            0f
        )
        font.data.setScale(1f)
    }

    private fun renderDetailHeading(batch: SpriteBatch, displayFont: BitmapFont) {
        displayFont.data.setScale(0.67f)
        displayFont.color = Color(0.90f, 0.78f, 0.48f, 1f)
        displayFont.draw(batch, "DETAILS", DETAIL_PANEL.x+24f, 622f)
        UiComponentRenderer.drawHorizontalSeparator(batch, Rectangle(DETAIL_PANEL.x+18f,598f,DETAIL_PANEL.width-36f,9f))
        displayFont.data.setScale(1f)
    }

    fun renderHoveredTooltip(
        batch: SpriteBatch,
        font: BitmapFont,
        camera: OrthographicCamera,
        pointer: Vector2,
        product: UnlockDefinition?
    ) {
        if(SHARED_DETAIL_VIEWPORT.contains(pointer)) detailEntry?.let {
            archiveRenderer.renderDetailHover(batch,font,it,sharedBounds(),pointer)
        }
        product ?: return
        val definition=com.azizoglu.grisilith.content.DefaultContentRegistry.instance.findById(com.azizoglu.grisilith.content.ContentId(product.id))
        if(definition is com.azizoglu.grisilith.character.CharacterDefinition) {
            CharacterHoverRenderer.draw(batch,font,pointer,definition);return
        }
        AdaptiveTooltipRenderer.draw(
            batch = batch,
            font = font,
            screenBounds=Rectangle(0f,0f,VIEWPORT_WIDTH,VIEWPORT_HEIGHT),
            pointer = pointer,
            title = product.displayName,
            body = definition?.let{com.azizoglu.grisilith.content.ContentPresentation.hover(it)} ?: product.description,
            maxWidth = 560f,titleScale=.90f,bodyScale=.76f
        )
    }

    private fun drawCenteredLine(
        batch: SpriteBatch,
        font: BitmapFont,
        text: String,
        bounds: Rectangle,
        color: Color,
        offsetY: Float = 0f
    ) {
        val oldColor = Color(font.color)
        glyphLayout.setText(font, text)
        font.color = color
        font.draw(
            batch,
            text,
            bounds.x,
            bounds.y + (bounds.height + glyphLayout.height) / 2f - 1f + offsetY,
            bounds.width,
            Align.center,
            false
        )
        font.color = oldColor
    }

    private fun drawFittedCenteredLine(
        batch: SpriteBatch,
        font: BitmapFont,
        text: String,
        bounds: Rectangle,
        color: Color,
        minimumScale: Float
    ) {
        val oldScaleX = font.data.scaleX
        val oldScaleY = font.data.scaleY
        glyphLayout.setText(font, text)
        if (glyphLayout.width > bounds.width) {
            val fittedScale =
                (oldScaleX * bounds.width / glyphLayout.width)
                    .coerceAtLeast(minimumScale)
            font.data.setScale(fittedScale)
        }
        drawCenteredLine(batch, font, text, bounds, color)
        font.data.setScale(oldScaleX, oldScaleY)
    }

    private fun renderStatPointPurchaseIcon(
        batch: SpriteBatch,
        displayFont: BitmapFont,
        bounds: Rectangle
    ) {
        val symbolSize = bounds.height * 0.70f
        purchasedStatPointRenderer.renderSymbol(
            batch,
            Rectangle(
                bounds.x + bounds.width - symbolSize - bounds.width * 0.06f,
                bounds.y + (bounds.height - symbolSize) / 2f,
                symbolSize,
                symbolSize
            )
        )
        displayFont.data.setScale(if (bounds.width < 90f) 0.55f else 0.78f)
        drawCenteredLine(
            batch,
            displayFont,
            "+1",
            Rectangle(
                bounds.x + bounds.width * 0.03f,
                bounds.y,
                bounds.width * 0.40f,
                bounds.height
            ),
            Color(0.88f, 0.78f, 0.52f, 1f)
        )
        displayFont.data.setScale(1f)
    }

    private fun isOwned(product: UnlockDefinition, profile: PlayerProfile): Boolean =
        product.type != UnlockType.STAT_POINT && PlayerProfileRepository.isUnlocked(product.id)

    private fun contentCategory(type: UnlockType): ContentCategory? =
        when (type) {
            UnlockType.WEAPON -> ContentCategory.WEAPON
            UnlockType.ARMOR -> ContentCategory.ARMOR
            UnlockType.ITEM -> ContentCategory.ITEM
            UnlockType.TAMGA -> ContentCategory.TAMGA
            UnlockType.CHARACTER -> ContentCategory.CHARACTER
            UnlockType.STAT_POINT -> null
        }

    private fun fallbackMark(type: UnlockType): String =
        when (type) {
            UnlockType.STAT_POINT -> "+1"
            UnlockType.CHARACTER -> "C"
            UnlockType.WEAPON -> "W"
            UnlockType.ARMOR -> "A"
            UnlockType.ITEM -> "I"
            UnlockType.TAMGA -> "T"
        }

    private fun compactDetailDescription(description: String): String {
        val normalized = description.replace('\n', ' ').trim()
        return if (normalized.length <= 190) {
            normalized
        } else {
            normalized.take(187).trimEnd() + "..."
        }
    }

    private fun typeLabel(type: UnlockType): String =
        when (type) {
            UnlockType.STAT_POINT -> "STAT POINTS"
            UnlockType.CHARACTER -> "CHARACTERS"
            UnlockType.WEAPON -> "WEAPONS"
            UnlockType.ARMOR -> "ARMORS"
            UnlockType.ITEM -> "ITEMS"
            UnlockType.TAMGA -> "TAMGAS"
        }

    private fun createWhitePixel(): Texture {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        return Texture(pixmap).also { pixmap.dispose() }
    }

    fun dispose() {
        background.dispose()
        productCardTexture.dispose()
        whitePixel.dispose()
        archiveRenderer.dispose()
    }
}
