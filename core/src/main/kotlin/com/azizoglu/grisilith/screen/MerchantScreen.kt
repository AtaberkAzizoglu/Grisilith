package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.armor.definition.DefaultArmors
import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.item.definition.DefaultItems
import com.azizoglu.grisilith.item.definition.ItemRarity
import com.azizoglu.grisilith.profile.PlayerProfileRepository
import com.azizoglu.grisilith.screen.components.MenuButton
import com.azizoglu.grisilith.screen.components.MenuPointer
import com.azizoglu.grisilith.screen.render.GrisilithCoinRenderer
import com.azizoglu.grisilith.screen.render.MerchantRenderer
import com.azizoglu.grisilith.screen.render.PurchasedStatPointRenderer
import com.azizoglu.grisilith.ui.typography.UiFontFactory
import com.azizoglu.grisilith.ui.input.VerticalScrollbarDragController
import com.azizoglu.grisilith.ui.theme.UiComponentRenderer
import com.azizoglu.grisilith.unlock.UnlockDefinition
import com.azizoglu.grisilith.unlock.UnlockManager
import com.azizoglu.grisilith.unlock.UnlockPurchaseResult
import com.azizoglu.grisilith.unlock.UnlockRegistry
import com.azizoglu.grisilith.unlock.UnlockType
import com.azizoglu.grisilith.weapon.definition.DefaultWeapons
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack
import com.badlogic.gdx.utils.viewport.FitViewport
import kotlin.math.ceil

class MerchantScreen(
    private val game: GrisilithGame
) : ScreenAdapter() {

    companion object {
        private const val VIEWPORT_WIDTH = 1280f
        private const val VIEWPORT_HEIGHT = 720f
        private const val GRID_COLUMNS = 2
        private const val CARD_HEIGHT = 156f
        private const val CARD_GAP = 12f
        private const val GRID_PADDING = 8f
        private const val DRAG_THRESHOLD = 7f
        private const val WHEEL_SCROLL_AMOUNT = 78f
        private const val PURCHASE_FLASH_DURATION = 0.85f

        private val CATEGORY_ORDER =
            listOf(
                UnlockType.STAT_POINT,
                UnlockType.CHARACTER,
                UnlockType.WEAPON,
                UnlockType.ARMOR,
                UnlockType.ITEM,
                UnlockType.TAMGA
            )
    }

    private val camera = OrthographicCamera()
    private val viewport = FitViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera)
    private val batch = SpriteBatch()
    private val bodyFont = UiFontFactory.createBody(20)
    private val displayFont = UiFontFactory.createDisplay(30)
    private val pointer = MenuPointer(viewport)

    private val renderer =
        MerchantRenderer(
            grisilithCoinRenderer = GrisilithCoinRenderer(),
            purchasedStatPointRenderer = PurchasedStatPointRenderer()
        )

    private val catalog = UnlockRegistry.visible()
    private val productsByType =
        CATEGORY_ORDER.associateWith { type ->
            catalog.filter { it.type == type }.sortedBy { it.displayName.lowercase() }
        }
    private val categoryRectangles =
        CATEGORY_ORDER.mapIndexed { index, type ->
            type to Rectangle(42f, 505f - index * 66f, 214f, 54f)
        }.toMap(LinkedHashMap())

    private var selectedCategory = UnlockType.STAT_POINT
    private var selectedUnlockId: String? = null
    private var hoveredUnlockId: String? = null
    private var pressedUnlockId: String? = null
    private var pressedCategory: UnlockType? = null
    private var ownedFilterPressed = false
    private var showOwned = true
    private var filterDropdownOpen = false
    private var filterPointerCaptured = false
    private var statQuantityText = "1"
    private var statQuantityFieldFocused = false
    private var replaceQuantityOnNextType = false
    private val selectedFilters =
        mutableMapOf(
            UnlockType.WEAPON to MerchantCatalogFilter.WEAPON_ALPHABETICAL,
            UnlockType.ARMOR to MerchantCatalogFilter.ARMOR_ALL,
            UnlockType.ITEM to MerchantCatalogFilter.ITEM_ALL
        )
    private val productScrollbarDrag = VerticalScrollbarDragController()
    private var productScrollOffset = 0f
    private var previousDragY = 0f
    private var accumulatedDragDistance = 0f
    private var isDraggingProducts = false
    private var message: String? = null
    private var messageIsError = false
    private var purchaseFlashId: String? = null
    private var purchaseFlashRemaining = 0f
    private var previousInputProcessor: InputProcessor? = null

    private val backButton =
        MenuButton("BACK", 44f, 35f, 188f, 58f, textOffsetY = 0f) {
            game.showMainMenu()
        }

    private val buyButton =
        MenuButton("PURCHASE", 860f, 121f, 280f, 60f, isEnabled = false, textOffsetY = 0f) {
            purchaseSelectedUnlock()
        }

    private val buyStatPointButton =
        MenuButton("BUY", 1120f, 121f, 86f, 60f, isEnabled = false, textOffsetY = 0f) {
            purchaseSelectedUnlock()
        }

    private val decreaseStatQuantityButton =
        MenuButton("-", 930f, 129f, 42f, 44f, textOffsetY = 0f) {
            adjustStatQuantity(-1)
        }

    private val increaseStatQuantityButton =
        MenuButton("+", 1068f, 129f, 42f, 44f, textOffsetY = 0f) {
            adjustStatQuantity(1)
        }

    private val inputProcessor: InputProcessor =
        object : InputAdapter() {
            override fun scrolled(amountX: Float, amountY: Float): Boolean {
                if(MerchantRenderer.SHARED_DETAIL_VIEWPORT.contains(pointer.position) && renderer.scrollDetails(amountY*55f))return true
                if (!MerchantRenderer.PRODUCT_VIEWPORT.contains(pointer.position)) {
                    return false
                }
                scrollBy(amountY * WHEEL_SCROLL_AMOUNT)
                return true
            }

            override fun keyTyped(character: Char): Boolean {
                if (!statQuantityFieldFocused) return false
                when {
                    character == '\b' -> {
                        statQuantityText = statQuantityText.dropLast(1)
                        replaceQuantityOnNextType = false
                    }
                    character == '\r' || character == '\n' -> {
                        normalizeStatQuantity()
                        statQuantityFieldFocused = false
                    }
                    character.isDigit() -> {
                        val candidate =
                            if (replaceQuantityOnNextType) character.toString()
                            else statQuantityText + character
                        val parsed = candidate.toIntOrNull()
                        if (parsed != null && parsed <= 999) {
                            statQuantityText = candidate.trimStart('0').ifBlank { "0" }
                        }
                        replaceQuantityOnNextType = false
                    }
                }
                return true
            }
        }

    override fun show() {
        viewport.apply()
        camera.position.set(VIEWPORT_WIDTH / 2f, VIEWPORT_HEIGHT / 2f, 0f)
        camera.update()
        pointer.reset()

        selectedCategory = UnlockType.STAT_POINT
        selectedUnlockId = productsForSelectedCategory().firstOrNull()?.id
        hoveredUnlockId = null
        pressedUnlockId = null
        pressedCategory = null
        ownedFilterPressed = false
        showOwned = true
        filterDropdownOpen = false
        filterPointerCaptured = false
        statQuantityText = "1"
        statQuantityFieldFocused = false
        replaceQuantityOnNextType = false
        productScrollbarDrag.reset()
        productScrollOffset = 0f
        message = null
        messageIsError = false
        purchaseFlashId = null
        purchaseFlashRemaining = 0f

        previousInputProcessor = Gdx.input.inputProcessor
        Gdx.input.inputProcessor = inputProcessor
    }

    override fun render(delta: Float) {
        pointer.update()
        renderer.updateDetailScrollbar(pointer)
        purchaseFlashRemaining = (purchaseFlashRemaining - delta).coerceAtLeast(0f)
        if (purchaseFlashRemaining <= 0f) {
            purchaseFlashId = null
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.showMainMenu()
            return
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            scrollBy(430f * delta)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            scrollBy(-430f * delta)
        }

        handleCategoryInteraction()
        handleOwnedFilterInteraction()
        val filterConsumed = handleCatalogFilterInteraction()
        val scrollbarConsumed = handleProductScrollbarDrag()
        if (!filterConsumed && !scrollbarConsumed) {
            handleProductInteraction()
        }
        updateHoveredProduct()
        normalizeSelection()

        if (
            backButton.update(
                pointer.position,
                pointer.justPressed,
                pointer.justReleased
            )
        ) {
            return
        }

        val selectedProduct = selectedProduct()
        val activeBuyButton = activeBuyButton(selectedProduct)
        val statPointSelected = selectedProduct?.type == UnlockType.STAT_POINT
        activeBuyButton.isEnabled =
            if (statPointSelected) {
                selectedProduct?.let(::canPurchaseStatQuantity) == true
            } else {
                selectedProduct?.let(::canPurchase) == true
            }

        if (statPointSelected) {
            decreaseStatQuantityButton.isEnabled = statPurchaseQuantity() > 1
            increaseStatQuantityButton.isEnabled = statPurchaseQuantity() < 999
            decreaseStatQuantityButton.update(pointer.position, pointer.justPressed, pointer.justReleased)
            increaseStatQuantityButton.update(pointer.position, pointer.justPressed, pointer.justReleased)
            if (pointer.justReleased) {
                val wasFocused = statQuantityFieldFocused
                statQuantityFieldFocused = MerchantRenderer.STAT_QUANTITY_INPUT.contains(pointer.position)
                if (statQuantityFieldFocused && !wasFocused) {
                    replaceQuantityOnNextType = true
                } else if (!statQuantityFieldFocused) {
                    normalizeStatQuantity()
                }
            }
        } else {
            statQuantityFieldFocused = false
            normalizeStatQuantity()
        }
        activeBuyButton.update(pointer.position, pointer.justPressed, pointer.justReleased)

        Gdx.gl.glClearColor(0.025f, 0.018f, 0.03f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.projectionMatrix = camera.combined

        batch.begin()
        renderer.renderChrome(
            batch,
            bodyFont,
            displayFont,
            PlayerProfileRepository.profile,
            selectedCategory,
            categoryRectangles,
            productsByType,
            showOwned,
            ownedFilterPressed,
            currentFilter()?.label,
            filterDropdownOpen,
            pressedCategory,
            pointer.position,
            pointer.isHeld
        )
        batch.end()

        val scissors = Rectangle()
        ScissorStack.calculateScissors(
            camera,
            batch.transformMatrix,
            MerchantRenderer.PRODUCT_VIEWPORT,
            scissors
        )
        if (ScissorStack.pushScissors(scissors)) {
            val products = productsForSelectedCategory()
            batch.begin()
            renderer.renderProductCards(
                batch,
                bodyFont,
                displayFont,
                PlayerProfileRepository.profile,
                products,
                productCardRectangles(products),
                selectedUnlockId,
                hoveredUnlockId,
                pressedUnlockId,
                purchaseFlashId,
                purchaseFlashRemaining
            )
            batch.end()
            ScissorStack.popScissors()
        }

        batch.begin()
        val maximumScroll = maximumProductScroll()
        renderer.renderDetails(
            batch,
            bodyFont,
            displayFont,
            PlayerProfileRepository.profile,
            selectedProduct,
            message,
            messageIsError,
            if (statPointSelected) statPurchaseQuantity() else 1,
            visibleProductRatio(),
            if (maximumScroll <= 0f) 0f else productScrollOffset / maximumScroll,
            viewport
        )
        activeBuyButton.render(batch, bodyFont, pointer.position, pointer.isHeld)
        if (statPointSelected) {
            decreaseStatQuantityButton.render(batch, bodyFont, pointer.position, pointer.isHeld)
            increaseStatQuantityButton.render(batch, bodyFont, pointer.position, pointer.isHeld)
            renderer.renderStatQuantityInput(
                batch,
                bodyFont,
                statQuantityText,
                statQuantityFieldFocused
            )
        }
        backButton.render(batch, bodyFont, pointer.position, pointer.isHeld)
        val filterOptions = filterOptionsForSelectedCategory()
        renderer.renderFilterDropdownOverlay(
            batch,
            bodyFont,
            filterOptions.map { it.label },
            currentFilter()?.label,
            filterDropdownOpen
        )
        renderer.renderHoveredTooltip(
            batch,
            bodyFont,
            camera,
            pointer.position,
            hoveredUnlockId?.let(UnlockRegistry::find)
        )
        batch.end()
    }

    private fun handleOwnedFilterInteraction() {
        val inside = MerchantRenderer.OWNED_FILTER.contains(pointer.position)
        if (pointer.justPressed) {
            ownedFilterPressed = inside
        }
        if (pointer.justReleased) {
            if (ownedFilterPressed && inside) {
                showOwned = !showOwned
                productScrollOffset = 0f
                message = null
                normalizeSelection()
            }
            ownedFilterPressed = false
        }
    }

    private fun handleProductScrollbarDrag(): Boolean {
        val maximum = maximumProductScroll()
        if (maximum <= 0f) {
            productScrollbarDrag.reset()
            return false
        }
        val track = MerchantRenderer.PRODUCT_SCROLLBAR
        val thumb =
            UiComponentRenderer.bronzeScrollbarThumbBounds(
                track,
                visibleProductRatio(),
                productScrollOffset / maximum
            )
        val interactionTrack = Rectangle(track.x - 5f, track.y, track.width + 10f, track.height)
        return productScrollbarDrag.update(
            pointer.position,
            pointer.justPressed,
            pointer.isHeld,
            pointer.justReleased,
            interactionTrack,
            thumb
        ) { ratio ->
            productScrollOffset = ratio * maximum
        }
    }

    private fun handleCategoryInteraction() {
        val categoryAtPointer =
            categoryRectangles.entries.firstOrNull { it.value.contains(pointer.position) }?.key

        if (pointer.justPressed) {
            pressedCategory = categoryAtPointer
        }

        if (pointer.justReleased) {
            if (pressedCategory != null && pressedCategory == categoryAtPointer) {
                selectedCategory = pressedCategory!!
                productScrollOffset = 0f
                selectedUnlockId = productsForSelectedCategory().firstOrNull()?.id
                hoveredUnlockId = null
                message = null
                messageIsError = false
                filterDropdownOpen = false
                filterPointerCaptured = false
            }
            pressedCategory = null
        }
    }

    private fun handleCatalogFilterInteraction(): Boolean {
        val options = filterOptionsForSelectedCategory()
        if (options.isEmpty()) {
            filterDropdownOpen = false
            filterPointerCaptured = false
            return false
        }

        if (pointer.justPressed &&
            (MerchantRenderer.FILTER_DROPDOWN.contains(pointer.position) || filterDropdownOpen)
        ) {
            filterPointerCaptured = true
            return true
        }

        if (!filterPointerCaptured) return false

        if (pointer.justReleased) {
            when {
                MerchantRenderer.FILTER_DROPDOWN.contains(pointer.position) ->
                    filterDropdownOpen = !filterDropdownOpen

                filterDropdownOpen -> {
                    val optionIndex =
                        renderer.filterOptionBounds(options.size)
                            .indexOfFirst { it.contains(pointer.position) }
                    if (optionIndex >= 0) {
                        selectedFilters[selectedCategory] = options[optionIndex]
                        productScrollOffset = 0f
                        normalizeSelection()
                    }
                    filterDropdownOpen = false
                }
            }
            filterPointerCaptured = false
        }
        return true
    }

    private fun handleProductInteraction() {
        val insideProducts = MerchantRenderer.PRODUCT_VIEWPORT.contains(pointer.position)

        if (pointer.justPressed && insideProducts) {
            previousDragY = pointer.position.y
            accumulatedDragDistance = 0f
            isDraggingProducts = false
            pressedUnlockId = productAtPointer()?.id
        }

        if (pointer.isHeld && (insideProducts || pressedUnlockId != null)) {
            val deltaY = pointer.position.y - previousDragY
            accumulatedDragDistance += kotlin.math.abs(deltaY)
            if (accumulatedDragDistance >= DRAG_THRESHOLD) {
                isDraggingProducts = true
            }
            if (isDraggingProducts) {
                scrollBy(-deltaY)
            }
            previousDragY = pointer.position.y
        }

        if (pointer.justReleased) {
            if (!isDraggingProducts && pressedUnlockId != null) {
                val releasedProduct = productAtPointer()
                if (releasedProduct?.id == pressedUnlockId) {
                    selectedUnlockId = releasedProduct?.id
                    message = null
                    messageIsError = false
                }
            }
            pressedUnlockId = null
            isDraggingProducts = false
            accumulatedDragDistance = 0f
        }
    }

    private fun updateHoveredProduct() {
        hoveredUnlockId =
            if (!filterDropdownOpen &&
                !isDraggingProducts &&
                MerchantRenderer.PRODUCT_VIEWPORT.contains(pointer.position)
            ) {
                productAtPointer()?.id
            } else {
                null
            }
    }

    private fun productsForSelectedCategory(): List<UnlockDefinition> =
        productsByType[selectedCategory]
            .orEmpty()
            .asSequence()
            .filter { showOwned || !isOwned(it) }
            .filter(::matchesCurrentFilter)
            .sortedBy { it.displayName.lowercase() }
            .toList()

    private fun filterOptionsForSelectedCategory(): List<MerchantCatalogFilter> =
        MerchantCatalogFilter.entries.filter { it.category == selectedCategory }

    private fun currentFilter(): MerchantCatalogFilter? =
        selectedFilters[selectedCategory]

    private fun matchesCurrentFilter(product: UnlockDefinition): Boolean {
        val filter = currentFilter() ?: return true
        return when (filter) {
            MerchantCatalogFilter.WEAPON_ALPHABETICAL -> true
            MerchantCatalogFilter.WEAPON_MELEE ->
                DefaultWeapons.findById(product.id)?.tags?.contains(ContentTag.MELEE_WEAPON) == true
            MerchantCatalogFilter.WEAPON_RANGED ->
                DefaultWeapons.findById(product.id)?.tags?.contains(ContentTag.RANGED_WEAPON) == true
            MerchantCatalogFilter.WEAPON_SPELL_FOCUS ->
                DefaultWeapons.findById(product.id)?.tags?.contains(ContentTag.SPELL_FOCUS) == true
            MerchantCatalogFilter.ARMOR_ALL -> true
            MerchantCatalogFilter.ARMOR_LIGHT -> armorHasTag(product.id, ContentTag.LIGHT_ARMOR)
            MerchantCatalogFilter.ARMOR_MEDIUM -> armorHasTag(product.id, ContentTag.MEDIUM_ARMOR)
            MerchantCatalogFilter.ARMOR_HEAVY -> armorHasTag(product.id, ContentTag.HEAVY_ARMOR)
            MerchantCatalogFilter.ARMOR_CLOTHING -> armorHasTag(product.id, ContentTag.CLOTHES)
            MerchantCatalogFilter.ITEM_ALL -> true
            MerchantCatalogFilter.ITEM_UNCOMMON -> itemHasRarity(product.id, ItemRarity.UNCOMMON)
            MerchantCatalogFilter.ITEM_RARE -> itemHasRarity(product.id, ItemRarity.RARE)
            MerchantCatalogFilter.ITEM_LEGENDARY -> itemHasRarity(product.id, ItemRarity.LEGENDARY)
            MerchantCatalogFilter.ITEM_ARTIFACT -> itemHasRarity(product.id, ItemRarity.ARTIFACT)
        }
    }

    private fun armorHasTag(id: String, tag: ContentTag): Boolean =
        DefaultArmors.all.firstOrNull { it.id.value == id }?.tags?.contains(tag) == true

    private fun itemHasRarity(id: String, rarity: ItemRarity): Boolean =
        DefaultItems.all.firstOrNull { it.id.value == id }?.rarity == rarity

    private fun isOwned(product: UnlockDefinition): Boolean =
        product.type != UnlockType.STAT_POINT &&
            PlayerProfileRepository.isUnlocked(product.id)

    private fun productCardRectangles(
        products: List<UnlockDefinition>
    ): Map<String, Rectangle> {
        val viewportBounds = MerchantRenderer.PRODUCT_VIEWPORT
        val cardWidth =
            (viewportBounds.width - GRID_PADDING * 2f - CARD_GAP * (GRID_COLUMNS - 1)) /
                GRID_COLUMNS
        val top = viewportBounds.y + viewportBounds.height - GRID_PADDING

        return products.mapIndexed { index, product ->
            val column = index % GRID_COLUMNS
            val row = index / GRID_COLUMNS
            val x = viewportBounds.x + GRID_PADDING + column * (cardWidth + CARD_GAP)
            val y = top - CARD_HEIGHT - row * (CARD_HEIGHT + CARD_GAP) + productScrollOffset
            product.id to Rectangle(x, y, cardWidth, CARD_HEIGHT)
        }.toMap()
    }

    private fun productAtPointer(): UnlockDefinition? {
        if (!MerchantRenderer.PRODUCT_VIEWPORT.contains(pointer.position)) {
            return null
        }
        val products = productsForSelectedCategory()
        val rectangles = productCardRectangles(products)
        return products.firstOrNull { rectangles[it.id]?.contains(pointer.position) == true }
    }

    private fun productContentHeight(): Float {
        val productCount = productsForSelectedCategory().size
        if (productCount <= 0) {
            return MerchantRenderer.PRODUCT_VIEWPORT.height
        }
        val rows = ceil(productCount / GRID_COLUMNS.toFloat()).toInt()
        return GRID_PADDING * 2f + rows * CARD_HEIGHT + (rows - 1) * CARD_GAP
    }

    private fun maximumProductScroll(): Float =
        (productContentHeight() - MerchantRenderer.PRODUCT_VIEWPORT.height).coerceAtLeast(0f)

    private fun visibleProductRatio(): Float =
        (MerchantRenderer.PRODUCT_VIEWPORT.height / productContentHeight()).coerceIn(0f, 1f)

    private fun scrollBy(amount: Float) {
        productScrollOffset =
            MathUtils.clamp(productScrollOffset + amount, 0f, maximumProductScroll())
    }

    private fun selectedProduct(): UnlockDefinition? =
        selectedUnlockId?.let(UnlockRegistry::find)

    private fun normalizeSelection() {
        val products = productsForSelectedCategory()
        if (selectedUnlockId == null || products.none { it.id == selectedUnlockId }) {
            selectedUnlockId = products.firstOrNull()?.id
        }
        productScrollOffset = MathUtils.clamp(productScrollOffset, 0f, maximumProductScroll())
    }

    private fun canPurchase(product: UnlockDefinition): Boolean {
        val profile = PlayerProfileRepository.profile
        val owned = product.type != UnlockType.STAT_POINT && PlayerProfileRepository.isUnlocked(product.id)
        return !owned && UnlockRegistry.dependenciesAreMet(product, profile.unlockedIds)
    }

    private fun canPurchaseStatQuantity(product: UnlockDefinition): Boolean =
        canPurchase(product) &&
            PlayerProfileRepository.profile.canSpendGrisilithCoins(
                product.cost * statPurchaseQuantity()
            )

    private fun activeBuyButton(product: UnlockDefinition?): MenuButton =
        if (product?.type == UnlockType.STAT_POINT) buyStatPointButton else buyButton

    private fun purchaseSelectedUnlock() {
        val product = selectedProduct() ?: return
        val requestedQuantity =
            if (product.type == UnlockType.STAT_POINT) statPurchaseQuantity() else 1

        if (!PlayerProfileRepository.profile.canSpendGrisilithCoins(product.cost * requestedQuantity)) {
            message = "NOT ENOUGH GRISILITH COINS"
            messageIsError = true
            return
        }

        var successfulPurchases = 0
        var result = UnlockPurchaseResult.SUCCESS
        repeat(requestedQuantity) {
            result = UnlockManager.purchase(product.id)
            if (result == UnlockPurchaseResult.SUCCESS) successfulPurchases++ else return@repeat
        }

        message =
            if (successfulPurchases == requestedQuantity && requestedQuantity > 1) {
                "$successfulPurchases STAT POINTS PURCHASED"
            } else when (result) {
                UnlockPurchaseResult.SUCCESS -> "PURCHASE COMPLETE"
                UnlockPurchaseResult.NOT_ENOUGH_COINS -> "NOT ENOUGH GRISILITH COINS"
                UnlockPurchaseResult.ALREADY_UNLOCKED -> "ALREADY OWNED"
                UnlockPurchaseResult.LOCKED -> "REQUIREMENTS NOT MET"
                UnlockPurchaseResult.UNKNOWN_UNLOCK -> "PRODUCT COULD NOT BE FOUND"
            }
        messageIsError = result != UnlockPurchaseResult.SUCCESS

        if (result == UnlockPurchaseResult.SUCCESS) {
            purchaseFlashId = product.id
            purchaseFlashRemaining = PURCHASE_FLASH_DURATION
        }
    }

    private fun statPurchaseQuantity(): Int =
        statQuantityText.toIntOrNull()?.coerceIn(1, 999) ?: 1

    private fun normalizeStatQuantity() {
        statQuantityText = statPurchaseQuantity().toString()
    }

    private fun adjustStatQuantity(delta: Int) {
        statQuantityText = (statPurchaseQuantity() + delta).coerceIn(1, 999).toString()
        replaceQuantityOnNextType = false
    }

    override fun resize(width: Int, height: Int) {
        if (width > 0 && height > 0) {
            viewport.update(width, height, true)
        }
    }

    override fun hide() {
        if (Gdx.input.inputProcessor === inputProcessor) {
            Gdx.input.inputProcessor = previousInputProcessor
        }
        previousInputProcessor = null
    }

    override fun dispose() {
        hide()
        renderer.dispose()
        batch.dispose()
        bodyFont.dispose()
        displayFont.dispose()
    }
}

private enum class MerchantCatalogFilter(
    val category: UnlockType,
    val label: String
) {
    WEAPON_MELEE(UnlockType.WEAPON, "MELEE WEAPON"),
    WEAPON_RANGED(UnlockType.WEAPON, "RANGED WEAPON"),
    WEAPON_SPELL_FOCUS(UnlockType.WEAPON, "SPELL FOCUS WEAPON"),
    WEAPON_ALPHABETICAL(UnlockType.WEAPON, "ALL"),
    ARMOR_LIGHT(UnlockType.ARMOR, "LIGHT ARMOR"),
    ARMOR_MEDIUM(UnlockType.ARMOR, "MEDIUM ARMOR"),
    ARMOR_HEAVY(UnlockType.ARMOR, "HEAVY ARMOR"),
    ARMOR_CLOTHING(UnlockType.ARMOR, "CLOTHING"),
    ARMOR_ALL(UnlockType.ARMOR, "ALL"),
    ITEM_UNCOMMON(UnlockType.ITEM, "UNCOMMON"),
    ITEM_RARE(UnlockType.ITEM, "RARE"),
    ITEM_LEGENDARY(UnlockType.ITEM, "LEGENDARY"),
    ITEM_ARTIFACT(UnlockType.ITEM, "ARTIFACT"),
    ITEM_ALL(UnlockType.ITEM, "ALL")
}
